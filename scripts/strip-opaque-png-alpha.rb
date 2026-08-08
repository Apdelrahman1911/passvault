#!/usr/bin/env ruby

require "zlib"

PNG_SIGNATURE = "\x89PNG\r\n\x1a\n".b

def png_chunk(type, data)
  [data.bytesize].pack("N") + type + data + [Zlib.crc32(type + data)].pack("N")
end

def paeth_predictor(left, above, upper_left)
  prediction = left + above - upper_left
  left_distance = (prediction - left).abs
  above_distance = (prediction - above).abs
  upper_left_distance = (prediction - upper_left).abs

  return left if left_distance <= above_distance && left_distance <= upper_left_distance
  return above if above_distance <= upper_left_distance

  upper_left
end

def unfilter_row(filter, filtered, previous, bytes_per_pixel)
  reconstructed = Array.new(filtered.length, 0)
  filtered.each_with_index do |byte, index|
    left = index >= bytes_per_pixel ? reconstructed[index - bytes_per_pixel] : 0
    above = previous ? previous[index] : 0
    upper_left = previous && index >= bytes_per_pixel ? previous[index - bytes_per_pixel] : 0
    predictor = case filter
                when 0 then 0
                when 1 then left
                when 2 then above
                when 3 then (left + above) / 2
                when 4 then paeth_predictor(left, above, upper_left)
                else raise "Unsupported PNG filter #{filter}"
                end
    reconstructed[index] = (byte + predictor) & 0xff
  end
  reconstructed
end

def parse_png(path)
  contents = File.binread(path)
  raise "Invalid PNG signature: #{path}" unless contents.start_with?(PNG_SIGNATURE)

  offset = PNG_SIGNATURE.bytesize
  chunks = []
  while offset < contents.bytesize
    length = contents.byteslice(offset, 4)&.unpack1("N")
    raise "Truncated PNG chunk: #{path}" unless length

    type = contents.byteslice(offset + 4, 4)
    data = contents.byteslice(offset + 8, length)
    crc = contents.byteslice(offset + 8 + length, 4)&.unpack1("N")
    raise "Truncated PNG chunk: #{path}" unless type&.bytesize == 4 && data&.bytesize == length && crc
    raise "Invalid PNG checksum: #{path}" unless Zlib.crc32(type + data) == crc

    chunks << [type, data]
    offset += 12 + length
    break if type == "IEND"
  end
  chunks
end

def strip_opaque_alpha(source, destination)
  chunks = parse_png(source)
  header = chunks.find { |type, _data| type == "IHDR" }&.last
  raise "PNG is missing IHDR: #{source}" unless header&.bytesize == 13

  width, height, bit_depth, color_type, compression, filter_method, interlace = header.unpack("NNCCCCC")
  unless bit_depth == 8 && color_type == 6 && compression.zero? && filter_method.zero? && interlace.zero?
    raise "Expected a non-interlaced 8-bit RGBA PNG: #{source}"
  end

  compressed = chunks.each_with_object([]) do |(type, data), parts|
    parts << data if type == "IDAT"
  end.join
  scanlines = Zlib::Inflate.inflate(compressed)
  rgba_stride = width * 4
  expected_bytes = height * (rgba_stride + 1)
  raise "Unexpected PNG scanline length: #{source}" unless scanlines.bytesize == expected_bytes

  output = String.new(capacity: height * (width * 3 + 1), encoding: Encoding::BINARY)
  previous = nil
  offset = 0
  height.times do
    filter = scanlines.getbyte(offset)
    filtered = scanlines.byteslice(offset + 1, rgba_stride).bytes
    row = unfilter_row(filter, filtered, previous, 4)
    output << "\x00"
    row.each_slice(4) do |red, green, blue, alpha|
      raise "PNG contains non-opaque pixels: #{source}" unless alpha == 0xff
      output << red << green << blue
    end
    previous = row
    offset += rgba_stride + 1
  end

  rgb_header = [width, height, 8, 2, 0, 0, 0].pack("NNCCCCC")
  encoded = PNG_SIGNATURE +
            png_chunk("IHDR", rgb_header) +
            png_chunk("IDAT", Zlib::Deflate.deflate(output, Zlib::BEST_COMPRESSION)) +
            png_chunk("IEND", "".b)
  File.binwrite(destination, encoded)
end

unless ARGV.length == 2
  warn "Usage: #{File.basename($PROGRAM_NAME)} <source.png> <destination.png>"
  exit 2
end

strip_opaque_alpha(File.expand_path(ARGV[0]), File.expand_path(ARGV[1]))
