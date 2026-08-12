#!/usr/bin/env ruby

module PassVault
  module PrivatePath
    module_function

    def regular_file_within?(candidate, root)
      return false unless File.directory?(root) && !File.symlink?(root)
      return false unless File.file?(candidate) && !File.symlink?(candidate)

      real_root = File.realpath(root)
      real_candidate = File.realpath(candidate)
      real_candidate.start_with?(real_root + File::SEPARATOR)
    rescue SystemCallError
      false
    end

    def directory_within?(candidate, root)
      return false unless File.directory?(root) && !File.symlink?(root)
      return false unless File.directory?(candidate) && !File.symlink?(candidate)

      real_root = File.realpath(root)
      real_candidate = File.realpath(candidate)
      real_candidate == real_root || real_candidate.start_with?(real_root + File::SEPARATOR)
    rescue SystemCallError
      false
    end
  end
end
