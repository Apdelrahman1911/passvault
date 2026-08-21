#!/usr/bin/env ruby

require "zlib"
require "find"

repository_root = File.expand_path("..", __dir__)
if ARGV.length > 1
  warn "Usage: #{File.basename($PROGRAM_NAME)} [store-assets-root]"
  exit 2
end
assets_root = ARGV.empty? ?
  File.join(repository_root, "release", "store-assets") :
  File.expand_path(ARGV.fetch(0), repository_root)
unless File.directory?(assets_root) && !File.symlink?(assets_root)
  warn "Store-assets root is missing or unsafe: #{assets_root}"
  exit 1
end
begin
  Find.find(assets_root) do |path|
    next if path == assets_root

    basename = File.basename(path)
    if basename == ".DS_Store" || basename == ".AppleDouble" || basename.start_with?("._")
      raise "operating-system metadata is forbidden: #{path.delete_prefix(assets_root + "/")}"
    end

    entry = File.lstat(path)
    raise "symlink or special filesystem entry" unless entry.file? || entry.directory?
  end
rescue StandardError => error
  warn "Store-assets tree is unsafe: #{error.message}"
  exit 1
end

def png_info(path)
  raise "missing" unless File.file?(path) && !File.symlink?(path)
  contents = File.binread(path)
  signature = "\x89PNG\r\n\x1a\n".b
  raise "not a PNG" unless contents.start_with?(signature)

  offset = signature.bytesize
  width = nil
  height = nil
  bit_depth = nil
  color_type = nil
  palette_entries = nil
  transparency = false
  idat_seen = false
  idat_ended = false
  idat_payload = +"".b
  iend_seen = false
  chunk_index = 0
  known_critical_chunks = %w[IHDR PLTE IDAT IEND].freeze

  while offset < contents.bytesize
    raise "truncated chunk header" if contents.bytesize - offset < 12

    chunk_length = contents.byteslice(offset, 4).unpack1("N")
    chunk_type = contents.byteslice(offset + 4, 4)
    chunk_end = offset + 12 + chunk_length
    raise "truncated chunk" if chunk_end > contents.bytesize
    raise "invalid chunk type" unless chunk_type.match?(/\A[A-Za-z]{4}\z/n) &&
      chunk_type.getbyte(2).between?(65, 90)

    chunk_data = contents.byteslice(offset + 8, chunk_length)
    recorded_crc = contents.byteslice(offset + 8 + chunk_length, 4).unpack1("N")
    calculated_crc = Zlib.crc32(chunk_type + chunk_data)
    raise "chunk CRC mismatch" unless recorded_crc == calculated_crc

    if chunk_type.getbyte(0).between?(65, 90) && !known_critical_chunks.include?(chunk_type)
      raise "unknown critical chunk"
    end

    case chunk_type
    when "IHDR"
      raise "IHDR must be the first and only IHDR chunk" unless chunk_index.zero? && width.nil?
      raise "invalid IHDR length" unless chunk_length == 13

      width, height, bit_depth, color_type, compression, filter, interlace =
        chunk_data.unpack("NNCCCCC")
      valid_depths = {
        0 => [1, 2, 4, 8, 16],
        2 => [8, 16],
        3 => [1, 2, 4, 8],
        4 => [8, 16],
        6 => [8, 16],
      }
      raise "invalid PNG dimensions" unless width.positive? && height.positive?
      raise "invalid PNG color type or bit depth" unless valid_depths.fetch(color_type, []).include?(bit_depth)
      raise "unsupported PNG compression or filter" unless compression.zero? && filter.zero?
      raise "invalid PNG interlace method" unless [0, 1].include?(interlace)
    when "PLTE"
      raise "PLTE appears before IHDR or after IDAT" if width.nil? || idat_seen
      raise "invalid or duplicate PLTE" if palette_entries || chunk_length.zero? ||
        chunk_length > 768 || (chunk_length % 3).nonzero? || [0, 4].include?(color_type)
      palette_entries = chunk_length / 3
      raise "indexed palette exceeds bit depth" if color_type == 3 && palette_entries > (1 << bit_depth)
    when "tRNS"
      raise "tRNS appears before IHDR or after IDAT" if width.nil? || idat_seen
      raise "duplicate or invalid tRNS" if transparency || [4, 6].include?(color_type)
      valid_length = case color_type
                     when 0 then chunk_length == 2
                     when 2 then chunk_length == 6
                     when 3 then palette_entries && chunk_length.positive? && chunk_length <= palette_entries
                     else false
                     end
      raise "invalid tRNS length" unless valid_length
      transparency = true
    when "IDAT"
      raise "IDAT appears before IHDR, after IEND, or non-contiguously" if width.nil? || iend_seen || idat_ended
      raise "indexed PNG is missing PLTE" if color_type == 3 && palette_entries.nil?
      idat_seen = true
      idat_payload << chunk_data
    when "IEND"
      raise "invalid IEND" unless chunk_length.zero? && idat_seen && !iend_seen
      iend_seen = true
      raise "trailing data after IEND" unless chunk_end == contents.bytesize
    else
      idat_ended = true if idat_seen
    end

    offset = chunk_end
    chunk_index += 1
  end

  raise "missing IHDR, IDAT, or IEND" unless width && idat_seen && iend_seen

  begin
    decoded_pixels = Zlib::Inflate.inflate(idat_payload)
  rescue Zlib::Error
    raise "invalid compressed image data"
  end
  channels = { 0 => 1, 2 => 3, 3 => 1, 4 => 2, 6 => 4 }.fetch(color_type)
  passes = if interlace.zero?
             [[0, 0, 1, 1]]
           else
             [
               [0, 0, 8, 8],
               [4, 0, 8, 8],
               [0, 4, 4, 8],
               [2, 0, 4, 4],
               [0, 2, 2, 4],
               [1, 0, 2, 2],
               [0, 1, 1, 2],
             ]
           end
  decoded_offset = 0
  passes.each do |x_start, y_start, x_step, y_step|
    next if width <= x_start || height <= y_start

    pass_width = (width - x_start + x_step - 1) / x_step
    pass_height = (height - y_start + y_step - 1) / y_step
    row_bytes = (pass_width * channels * bit_depth + 7) / 8
    pass_height.times do
      filter_type = decoded_pixels.getbyte(decoded_offset)
      raise "invalid or truncated scanline filter" unless filter_type&.between?(0, 4)

      decoded_offset += 1 + row_bytes
    end
  end
  raise "unexpected decompressed image length" unless decoded_offset == decoded_pixels.bytesize

  alpha = [4, 6].include?(color_type) || transparency
  [width, height, alpha, bit_depth, color_type]
end

errors = []

%w[en-US ar].each do |locale|
  locale_root = File.join(assets_root, "android", locale, "images")
  icon = File.join(locale_root, "icon.png")
  begin
    width, height, alpha, bit_depth, color_type = png_info(icon)
    errors << "Android #{locale} icon must be 512x512" unless [width, height] == [512, 512]
    errors << "Android #{locale} icon must be an 8-bit RGBA PNG" unless alpha && bit_depth == 8 && color_type == 6
  rescue StandardError
    errors << "Add android/#{locale}/images/icon.png"
  end

  feature_graphic = File.join(locale_root, "featureGraphic.png")
  begin
    width, height, alpha, = png_info(feature_graphic)
    errors << "Android #{locale} feature graphic must be 1024x500" unless [width, height] == [1024, 500]
    errors << "Android #{locale} feature graphic must not use alpha" if alpha
  rescue StandardError
    errors << "Add android/#{locale}/images/featureGraphic.png"
  end

  screenshots = Dir.glob(File.join(locale_root, "phoneScreenshots", "*.png")).sort
  errors << "Add at least four Android #{locale} phone screenshots" if screenshots.length < 4
  errors << "Android #{locale} supports at most eight phone screenshots" if screenshots.length > 8
  screenshots.each do |path|
    begin
      width, height, alpha, = png_info(path)
      valid_size = width >= 1_080 && height >= 1_920 && height <= width * 2
      errors << "#{path.delete_prefix(assets_root + "/")} has invalid phone dimensions" unless valid_size
      errors << "#{path.delete_prefix(assets_root + "/")} must not use alpha" if alpha
    rescue StandardError
      errors << "#{path.delete_prefix(assets_root + "/")} is not a valid PNG"
    end
  end
end

apple_phone_sizes = [
  [1_260, 2_736], [2_736, 1_260],
  [1_290, 2_796], [2_796, 1_290],
  [1_320, 2_868], [2_868, 1_320],
]
apple_ipad_sizes = [
  [2_048, 2_732], [2_732, 2_048],
  [2_064, 2_752], [2_752, 2_064],
]

%w[en-US ar-SA].each do |locale|
  screenshots = Dir.glob(File.join(assets_root, "ios", locale, "*.png")).sort
  phone_count = 0
  ipad_count = 0
  screenshots.each do |path|
    begin
      width, height, alpha, = png_info(path)
      dimensions = [width, height]
      phone_count += 1 if apple_phone_sizes.include?(dimensions)
      ipad_count += 1 if apple_ipad_sizes.include?(dimensions)
      unless apple_phone_sizes.include?(dimensions) || apple_ipad_sizes.include?(dimensions)
        errors << "#{path.delete_prefix(assets_root + "/")} is not an accepted iPhone or iPad size"
      end
      errors << "#{path.delete_prefix(assets_root + "/")} must not use alpha" if alpha
    rescue StandardError
      errors << "#{path.delete_prefix(assets_root + "/")} is not a valid PNG"
    end
  end
  errors << "Add at least one 6.9-inch iPhone screenshot for iOS #{locale}" if phone_count.zero?
  errors << "Add at least one 12.9-inch iPad screenshot for iOS #{locale}" if ipad_count.zero?
  errors << "iOS #{locale} supports at most ten 6.9-inch iPhone screenshots" if phone_count > 10
  errors << "iOS #{locale} supports at most ten 12.9-inch iPad screenshots" if ipad_count > 10
end

if errors.any?
  warn errors.map { |error| "- #{error}" }.join("\n")
  exit 1
end

puts "Mobile store assets satisfy the production file and dimension gates."
