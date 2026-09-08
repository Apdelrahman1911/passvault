#!/usr/bin/env ruby
# frozen_string_literal: true

# Synthetic regression fixtures for the exact installation/EXIT blocks, not a
# signing, Keychain, Xcode, physical-device, signal-delivery or crash test.
require "fileutils"
require "open3"
require "pathname"
require "rbconfig"
require "tmpdir"

module PassVault
  module IosProfileCleanupTest
    SCENARIOS = %w[
      created_success later_failure copy_failed copy_failed_after_bytes
      chmod_failed handled_exit_after_copy handled_exit_after_chmod
      handled_exit_before_publish handled_exit_after_publish existing_equal
      existing_different existing_symlink existing_dangling_symlink existing_directory publish_conflict
      publish_failed replacement_before_cleanup symlink_replacement_before_cleanup
      profile_cleanup_failed root_cleanup_failed
    ].freeze
    PROFILE_BYTES = "inert synthetic profile; no certificate or signing material\n"
    OTHER_BYTES = "inert preexisting or competing profile\n"
    UUID = "00000000-0000-0000-0000-000000000000"
    ROOT = Pathname.new(__dir__).parent
    module_function

    def assert(value, message)
      raise message unless value
    end

    def exact_block(source, first, after)
      start = source.index(first)
      finish = source.index(after)
      assert(start && !source.index(first, start + first.length), "Missing or ambiguous production block start")
      assert(finish && !source.index(after, finish + after.length), "Missing or ambiguous production block end")
      assert(start < finish, "Reordered production block")
      source[start...finish]
    end

    def snapshot(path)
      return nil unless path.exist? || path.symlink?

      state = path.lstat
      {
        type: state.ftype, device: state.dev, inode: state.ino,
        mode: state.mode & 0o777,
        bytes: state.file? ? path.binread : nil,
        target: state.symlink? ? path.readlink.to_s : nil,
      }
    end

    # Helpers intercept only a bounded synthetic file operation or reject it.
    # The explicit failure propagation in ruby() is required: its production
    # caller is inside "if !", where Bash does not apply ordinary errexit.
    def operation_doubles
      <<~'SH'
        cp() {
          [[ "$#" == 2 && "$1" == "$profile_path" ]] || return 98
          [[ "$2" == "${profile_staging:-}" || "$2" == "$installed_profile" ]] || return 98
          printf '%s\n' copy_attempt >> "$trace"
          [[ "$scenario" != copy_failed ]] || return 41
          command cp "$@" || return "$?"
          printf '%s\n' copied >> "$trace"
          [[ "$scenario" != copy_failed_after_bytes ]] || return 41
          [[ "$scenario" != handled_exit_after_copy ]] || exit 143
        }
        chmod() {
          [[ "$#" == 2 && "$1" == 600 ]] || return 98
          [[ "$2" == "${profile_staging:-}" || "$2" == "$installed_profile" ]] || return 98
          printf '%s\n' chmod_attempt >> "$trace"
          [[ "$scenario" != chmod_failed ]] || return 42
          command chmod "$@" || return "$?"
          printf '%s\n' chmod_done >> "$trace"
          [[ "$scenario" != handled_exit_after_chmod ]] || exit 143
        }
        ruby() {
          [[ "$#" == 4 && "$1" == -e &&
            "$2" == 'File.link(ARGV.fetch(0), ARGV.fetch(1))' &&
            "$3" == "$profile_staging" && "$4" == "$installed_profile" ]] || return 98
          printf '%s\n' publish_attempt >> "$trace"
          [[ "$scenario" != handled_exit_before_publish ]] || exit 143
          [[ "$scenario" != publish_failed ]] || return 55
          if [[ "$scenario" == publish_conflict ]]; then
            printf '%s\n' 'inert preexisting or competing profile' > "$installed_profile"
          fi
          command "$PASSVAULT_FIXTURE_RUBY" "$@" || return "$?"
          printf '%s\n' published >> "$trace"
          [[ "$scenario" != handled_exit_after_publish ]] || exit 143
        }
        rm() {
          if [[ "$#" == 3 && "$1" == -f && "$2" == -- && "$3" == "$installed_profile" ]]; then
            printf '%s\n' remove_profile >> "$trace"
            [[ "$scenario" != profile_cleanup_failed ]] || return 46
          elif [[ "$#" == 3 && "$1" == -rf && "$2" == -- && "$3" == "$verification_root" ]]; then
            printf '%s\n' remove_root >> "$trace"
            [[ "$scenario" != root_cleanup_failed ]] || return 47
          else
            return 98
          fi
          command rm "$@" || return "$?"
        }
      SH
    end

    def verify_scenario(scenario, source: ROOT.join("scripts/verify-ios-release-signing.sh").read)
      assert(SCENARIOS.include?(scenario), "Unknown scenario")
      bootstrap = exact_block(source, 'verification_root="', "export IOS_DISTRIBUTION_CERTIFICATE_PASSWORD")
      installation = exact_block(source,
        'profile_directory="$HOME/Library/MobileDevice/Provisioning Profiles"',
        'keychain_password="$("$openssl_binary" rand -hex 32)"')
      observation = nil

      # The caller's isolated TMPDIR owns every file. Dir.mktmpdir cleanup also
      # covers intentionally failed production cleanup without following links.
      Dir.mktmpdir("passvault-ios-profile-fixture.") do |directory|
        root = Pathname.new(directory).realpath
        home = root.join("home")
        temporary = root.join("tmp")
        FileUtils.mkdir_p([home, temporary], mode: 0o700)
        installed = home.join("Library/MobileDevice/Provisioning Profiles", "#{UUID}.mobileprovision")
        FileUtils.mkdir_p(installed.parent, mode: 0o700)
        profile = root.join("synthetic-input.mobileprovision")
        profile.write(PROFILE_BYTES)
        kept_target = root.join("synthetic-unowned.mobileprovision")
        kept_target.write(OTHER_BYTES)
        case scenario
        when "existing_equal" then installed.write(PROFILE_BYTES)
        when "existing_different" then installed.write(OTHER_BYTES)
        when "existing_symlink" then File.symlink(kept_target, installed)
        when "existing_dangling_symlink" then File.symlink(root.join("absent-synthetic-target"), installed)
        when "existing_directory" then installed.mkdir
        end
        original_installed = snapshot(installed)
        original_target = snapshot(kept_target)
        trace = root.join("operations.txt")
        trace.write("")
        paths = root.join("owned-paths.txt")

        environment = {
          "HOME" => home.to_s, "TMPDIR" => temporary.to_s,
          "PATH" => [File.dirname(RbConfig.ruby), "/usr/bin", "/bin"].join(File::PATH_SEPARATOR),
          "LANG" => "C", "LC_ALL" => "C",
          "PASSVAULT_FIXTURE_RUBY" => RbConfig.ruby,
          "scenario" => scenario, "trace" => trace.to_s,
          "profile_path" => profile.to_s, "profile_uuid" => UUID,
          "fixture_paths" => paths.to_s, "kept_target" => kept_target.to_s,
        }
        guards = <<~'SH'
          set -euo pipefail
          # No provider command may escape to a real implementation.
          forbidden_provider() { printf '%s\n' unexpected_provider >> "$trace"; return 97; }
          security() { forbidden_provider; }
          xcodebuild() { forbidden_provider; }
          xcrun() { forbidden_provider; }
          codesign() { forbidden_provider; }
          openssl() { forbidden_provider; }
          git() { forbidden_provider; }
          gh() { forbidden_provider; }
          curl() { forbidden_provider; }
          wget() { forbidden_provider; }
          java() { forbidden_provider; }
          passvault_capture_user_keychains() {
            printf '%s\n' '/synthetic/keychain-path-not-opened' > "$1"
          }
          passvault_restore_user_keychains() { forbidden_provider; }
        SH
        script = guards + bootstrap
        script += "printf '%s\\n' \"$verification_root\" > \"$fixture_paths\"\n"
        script += operation_doubles + installation
        script += <<~'SH'
          # Inspect publication before invoking the exact production EXIT trap.
          printf '%s\n' installation_completed >> "$trace"
          case "$scenario" in
            replacement_before_cleanup)
              command rm -f -- "$installed_profile"
              # Equal bytes do not confer ownership of a replacement inode.
              command cp "$profile_path" "$installed_profile"
              ;;
            symlink_replacement_before_cleanup)
              command rm -f -- "$installed_profile"
              # This resolves to the same inode: only ! -L protects the link.
              command ln -s "$profile_staging" "$installed_profile"
              ;;
          esac
          [[ "$scenario" != later_failure ]] || exit 19
          exit 0
        SH
        stdout, stderr, status = Open3.capture3(environment, "/bin/bash", "--noprofile", "--norc", "-c", script,
          chdir: root.to_s, unsetenv_others: true)
        events = trace.read.lines.map(&:chomp)
        assert(!events.include?("unexpected_provider"), "Synthetic fixture attempted a real provider boundary")
        assert(paths.file?, "Fixture did not reach production bootstrap")
        verification_root = Pathname.new(paths.read.strip)
        assert(verification_root.parent == temporary && verification_root.basename.to_s.start_with?("passvault-ios-signed-verify."),
          "Unexpected synthetic cleanup target")

        expected_exit = case scenario
                        when "later_failure" then 19
                        when "copy_failed", "copy_failed_after_bytes" then 41
                        when "chmod_failed" then 42
                        when /^handled_exit_/ then 143
                        when "existing_different", "existing_symlink", "existing_dangling_symlink", "existing_directory",
                             "publish_conflict", "publish_failed", "profile_cleanup_failed", "root_cleanup_failed" then 1
                        else 0
                        end
        assert(status.exitstatus == expected_exit, "#{scenario}: expected exit #{expected_exit}, got #{status.exitstatus}; #{stderr}")
        assert(stdout.empty?, "Unexpected synthetic stdout")
        assert(snapshot(kept_target) == original_target, "Preexisting synthetic target changed")
        assert(profile.read == PROFILE_BYTES, "Input bytes changed")

        preserved = %w[existing_equal existing_different existing_symlink existing_dangling_symlink existing_directory]
        if preserved.include?(scenario)
          assert(snapshot(installed) == original_installed, "Preexisting profile/path was changed or removed")
          assert(!events.include?("copy_attempt"), "Preexisting profile entered copy branch")
          assert(!events.include?("remove_profile"), "Cleanup claimed a preexisting profile")
        elsif %w[publish_conflict replacement_before_cleanup].include?(scenario)
          expected_bytes = scenario == "publish_conflict" ? OTHER_BYTES : PROFILE_BYTES
          assert(installed.file? && !installed.symlink? && installed.read == expected_bytes,
            "Competing/replacement profile was removed or overwritten")
          assert(!events.include?("remove_profile"), "Cleanup claimed a competing inode")
        elsif scenario == "symlink_replacement_before_cleanup"
          expected_target = verification_root.join("profile-to-install.mobileprovision").to_s
          assert(installed.symlink? && installed.readlink.to_s == expected_target,
            "Same-inode symlink was removed or changed")
          assert(!events.include?("remove_profile"), "Cleanup followed a replaced symlink")
        elsif scenario == "profile_cleanup_failed"
          assert(installed.file? && installed.read == PROFILE_BYTES, "Expected injected removal failure residue missing")
          assert(events.include?("remove_profile"), "Injected profile cleanup failure was not reached")
          assert(stderr.include?("Unable to remove the temporary installed provisioning profile."),
            "Profile cleanup failure was not reported")
        else
          assert(!installed.exist? && !installed.symlink?, "Owned installed profile remained after cleanup")
        end

        if scenario == "root_cleanup_failed"
          assert(verification_root.directory?, "Expected injected root cleanup failure residue missing")
          assert(stderr.include?("Unable to remove the temporary signed-verification directory."),
            "Root cleanup failure was not reported")
        else
          assert(!verification_root.exist?, "Owned verification directory remained")
        end
        assert(events.include?("remove_root"), "Cleanup did not attempt the owned verification root")
        if %w[created_success later_failure handled_exit_after_publish profile_cleanup_failed root_cleanup_failed].include?(scenario)
          assert(events.include?("published"), "Fixture did not reach exclusive publication")
          assert(events.include?("remove_profile"), "Owned published inode did not reach cleanup")
        end
        if %w[created_success later_failure existing_equal replacement_before_cleanup
              symlink_replacement_before_cleanup profile_cleanup_failed root_cleanup_failed].include?(scenario)
          assert(events.include?("installation_completed"), "Successful installation branch was not reached")
        else
          assert(!events.include?("installation_completed"), "Failure unexpectedly reached installation completion")
        end
        observation = { scenario: scenario, exit: status.exitstatus, events: events }
      end
      observation
    end
  end
end

if $PROGRAM_NAME == __FILE__
  selected = ARGV.empty? ? PassVault::IosProfileCleanupTest::SCENARIOS : ARGV
  failures = []
  selected.each do |scenario|
    begin
      PassVault::IosProfileCleanupTest.verify_scenario(scenario)
      puts "PASS iOS profile cleanup #{scenario}"
    rescue StandardError => error
      failures << scenario
      warn "FAIL iOS profile cleanup #{scenario}: #{error.message}"
    end
  end
  puts "iOS profile cleanup: #{selected.length - failures.length} PASS, #{failures.length} FAIL"
  exit(failures.empty? ? 0 : 1)
end
