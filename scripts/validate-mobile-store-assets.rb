#!/usr/bin/env ruby

repository_root = File.expand_path("..", __dir__)
assets_root = File.join(repository_root, "release", "store-assets")

def png_info(path)
  raise "missing" unless File.file?(path) && !File.symlink?(path)
  header = File.binread(path, 26)
  raise "not a PNG" unless header.byteslice(0, 8) == "\x89PNG\r\n\x1a\n".b
  raise "missing IHDR" unless header.byteslice(12, 4) == "IHDR"
  width, height = header.byteslice(16, 8).unpack("NN")
  color_type = header.getbyte(25)
  [width, height, [4, 6].include?(color_type)]
end

errors = []

%w[en-US ar].each do |locale|
  locale_root = File.join(assets_root, "android", locale, "images")
  icon = File.join(locale_root, "icon.png")
  begin
    width, height, alpha = png_info(icon)
    errors << "Android #{locale} icon must be 512x512" unless [width, height] == [512, 512]
    errors << "Android #{locale} icon must be a 32-bit PNG with alpha" unless alpha
  rescue StandardError
    errors << "Add android/#{locale}/images/icon.png"
  end

  feature_graphic = File.join(locale_root, "featureGraphic.png")
  begin
    width, height, alpha = png_info(feature_graphic)
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
      width, height, alpha = png_info(path)
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
      width, height, alpha = png_info(path)
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
