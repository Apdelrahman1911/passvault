#!/usr/bin/env ruby
# frozen_string_literal: true

require "optparse"
require_relative "lib/strict_json"

# Candidate C remains the built source. A later testing invocation D can attest
# the same subject only when C is its ancestor and both complete trees equal T.
# Never infer D from the subject/predicate or from an unverified bundle.
module PassVault
  module CandidateAttestation
    class Invalid < StandardError; end

    SHA = /\A[0-9a-fA-F]{40}\z/
    WORKFLOWS = %w[testing-release candidate-readiness].freeze
    MAX_OUTPUT_BYTES = 4 * 1024 * 1024
    MAX_VERIFIED_ROWS = 256
    GH_TIMEOUT_SECONDS = 180
    GIT_TIMEOUT_SECONDS = 30
    VERIFICATION_TIMEOUT_SECONDS = 240

    def self.cancel!
      @cancelled = true
    end

    def self.check_cancelled!
      raise Interrupt, "Candidate verification cancelled" if @cancelled
    end

    # POSIX host contract: default SIGCHLD, with this method as the sole reaper of
    # its children. The dedicated CLI enforces the disposition, never on import.
    def self.capture(environment, *arguments, timeout_seconds:, output_limit: MAX_OUTPUT_BYTES, inherit_environment: true)
      output = +"".b
      consumed = 0
      deadline = Process.clock_gettime(Process::CLOCK_MONOTONIC) + timeout_seconds
      pipes = []
      stdout = stdout_writer = stderr = stderr_writer = nil
      pid = nil
      reaped = false
      status = nil
      begin
        check_cancelled!
        # No Open3 waiter may reap this child independently. Its unreaped PID
        # anchors our process group until all intended signalling has finished.
        Thread.handle_interrupt(Exception => :never) do
          stdout, stdout_writer = IO.pipe
          pipes.concat([stdout, stdout_writer])
          stderr, stderr_writer = IO.pipe
          pipes.concat([stderr, stderr_writer])
          pid = Process.spawn(environment, *arguments, in: File::NULL,
            out: stdout_writer, err: stderr_writer, pgroup: true, unsetenv_others: !inherit_environment)
        end
        stdout_writer.close
        stderr_writer.close
        streams = [stdout, stderr]
        loop do
          check_cancelled!
          remaining = deadline - Process.clock_gettime(Process::CLOCK_MONOTONIC)
          raise Invalid, "Verifier subprocess exceeded its time limit" if remaining <= 0

          if streams.empty?
            Thread.handle_interrupt(Exception => :never) do
              settled = Process.waitpid2(pid, Process::WNOHANG)
              if settled
                reaped = true
                status = settled.last
              end
            end
            break if reaped

            sleep [remaining, 0.05].min
            next
          end
          readable = IO.select(streams, nil, nil, [remaining, 0.05].min)&.first || []
          readable.each do |stream|
            chunk = stream.read_nonblock(16 * 1024, exception: false)
            if chunk.nil?
              streams.delete(stream)
              stream.close
            elsif chunk != :wait_readable
              consumed += chunk.bytesize
              raise Invalid, "Verifier subprocess exceeded its output limit" if consumed > output_limit

              # Count/drain diagnostics too, but parse and retain only stdout.
              output << chunk if stream == stdout
            end
          end
        end
        check_cancelled!
        [output.force_encoding(Encoding::UTF_8), status]
      ensure
        Thread.handle_interrupt(Exception => :never) do
          begin
            if pid && !reaped
              begin
                Process.kill("TERM", -pid)
              rescue Errno::ESRCH
                nil
              end
              # Do not reap during the grace period: even a terminated leader's
              # unreaped PID must remain reserved before the final group signal.
              sleep 1
              begin
                Process.kill("KILL", -pid)
              rescue Errno::ESRCH
                nil
              end
              Process.waitpid(pid)
              reaped = true
            end
          ensure
            pipes.each { |pipe| pipe.close unless pipe.closed? }
          end
        end
      end
    end

    def self.git(*arguments, deadline:)
      remaining = deadline - Process.clock_gettime(Process::CLOCK_MONOTONIC)
      raise Invalid, "Candidate verification exceeded its time limit" if remaining <= 0

      output, status = capture({}, "git", "--no-replace-objects", *arguments,
        timeout_seconds: [GIT_TIMEOUT_SECONDS, remaining].min)
      status.success? ? output.strip : nil
    end

    def self.tree(commit, deadline:)
      # Require an actual commit object, not an annotated tag that peels to one.
      return unless git("rev-parse", "--verify", "#{commit}^{commit}", deadline: deadline) == commit

      git("rev-parse", "--verify", "#{commit}^{tree}", deadline: deadline)
    end

    def self.verified_digest(row)
      %w[verificationResult signature certificate sourceRepositoryDigest].each do |key|
        return unless row.is_a?(Hash)

        row = row[key]
      end
      row.downcase if row.is_a?(String) && SHA.match?(row)
    end

    def self.verify!(subject, options)
      deadline = Process.clock_gettime(Process::CLOCK_MONOTONIC) + VERIFICATION_TIMEOUT_SECONDS
      repository = options.fetch(:repository)
      workflow = options.fetch(:workflow)
      commit = options.fetch(:commit)
      expected_tree = options.fetch(:tree)
      unless repository.match?(%r{\A[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\z}) &&
             WORKFLOWS.any? { |name| workflow == "#{repository}/.github/workflows/#{name}.yml" } &&
             options.fetch(:ref) == "refs/heads/testing" && options[:hosted] == true &&
             SHA.match?(commit) && SHA.match?(expected_tree)
        raise Invalid, "Invalid candidate attestation policy inputs"
      end
      commit = commit.downcase
      expected_tree = expected_tree.downcase
      subject = File.expand_path(subject)
      raise Invalid, "Candidate subject must be a regular file" unless File.lstat(subject).file?

      output, status = capture(
        { "GH_HOST" => "github.com" },
        "gh", "attestation", "verify", subject,
        "--repo", repository, "--signer-workflow", workflow,
        "--source-ref", options.fetch(:ref), "--deny-self-hosted-runners",
        "--cert-oidc-issuer", "https://token.actions.githubusercontent.com",
        "--predicate-type", "https://slsa.dev/provenance/v1", "--format", "json",
        timeout_seconds: GH_TIMEOUT_SECONDS,
      )
      raise Invalid, "Candidate signature or signer policy verification failed" unless status.success?

      # gh exports successful verified results, not raw downloaded attestations.
      # stdout is intentionally separate from stderr, even when gh exits zero.
      rows = StrictJson.parse(output)
      raise Invalid, "Expected a nonempty verified attestation result array" unless rows.is_a?(Array) && !rows.empty?
      raise Invalid, "Too many verified attestation results" if rows.length > MAX_VERIFIED_ROWS
      unless tree(commit, deadline: deadline) == expected_tree
        raise Invalid, "Candidate commit/tree is unavailable or mismatched"
      end

      accepted = rows.any? do |row|
        digest = verified_digest(row)
        next false unless digest
        next true if digest == commit

        tree(digest, deadline: deadline) == expected_tree &&
          !git("merge-base", "--is-ancestor", commit, digest, deadline: deadline).nil?
      end
      raise Invalid, "No verified signer source matches the candidate commit or permitted exact tree" unless accepted

      true
    end
  end
end

if $PROGRAM_NAME == __FILE__
  begin
    # SIG_IGN/SA_NOCLDWAIT could release a PID before our explicit wait. This
    # dedicated process owns its children; no unrelated waiters are installed.
    Signal.trap("CHLD", "DEFAULT")
    # Traps only request cancellation. Raising from a trap could interrupt the
    # spawn/reap ownership transition; the capture loop raises at safe points.
    %w[INT TERM HUP].each { |name| Signal.trap(name) { PassVault::CandidateAttestation.cancel! } }
    options = {}
    OptionParser.new do |parser|
      parser.on("--repo REPOSITORY") { |value| options[:repository] = value }
      parser.on("--signer-workflow WORKFLOW") { |value| options[:workflow] = value }
      parser.on("--source-ref REF") { |value| options[:ref] = value }
      parser.on("--candidate-commit SHA") { |value| options[:commit] = value }
      parser.on("--candidate-tree SHA") { |value| options[:tree] = value }
      parser.on("--deny-self-hosted-runners") { options[:hosted] = true }
    end.parse!
    raise PassVault::CandidateAttestation::Invalid, "Expected exactly one candidate subject" unless ARGV.one?

    PassVault::CandidateAttestation.verify!(ARGV.first, options)
    puts "Verified candidate attestation and source-tree binding"
  rescue Interrupt
    warn "Candidate attestation verification cancelled"
    exit 130
  rescue PassVault::CandidateAttestation::Invalid, OptionParser::ParseError, KeyError,
         JSON::ParserError, SystemCallError => error
    warn "Candidate attestation rejected: #{error.message}"
    exit 1
  end
end
