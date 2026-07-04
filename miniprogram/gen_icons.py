import struct
import zlib
import os

def create_png(path, r, g, b, size=81):
    w, h = size, size

    def row_data(y):
        pixels = bytearray()
        for x in range(w):
            cx, cy = w // 2, h // 2
            dx, dy = abs(x - cx), abs(y - cy)
            radius = w // 3
            margin = 6
            if x < margin or x >= w - margin or y < margin or y >= h - margin:
                pixels.extend([0, 0, 0, 0])
            elif dx * dx + dy * dy <= radius * radius:
                pixels.extend([r, g, b, 255])
            else:
                pixels.extend([0, 0, 0, 0])
        return bytes(pixels)

    raw = b''.join(row_data(y) for y in range(h))

    def make_chunk(ctype, data):
        c = ctype + data
        crc = struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)
        return struct.pack('>I', len(data)) + c + crc

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = make_chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))

    filtered = b''
    for y in range(h):
        filtered += b'\x00' + raw[y * w * 4:(y + 1) * w * 4]
    idat = make_chunk(b'IDAT', zlib.compress(filtered))
    iend = make_chunk(b'IEND', b'')

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(sig + ihdr + idat + iend)

base = r'd:\桌面\AI装修生图\house-magic-space\miniprogram\images'

create_png(base + r'\tab-home.png', 0x99, 0x99, 0x99)
create_png(base + r'\tab-home-active.png', 0x2C, 0x3E, 0x50)
create_png(base + r'\tab-case.png', 0x99, 0x99, 0x99)
create_png(base + r'\tab-case-active.png', 0x2C, 0x3E, 0x50)
create_png(base + r'\tab-explore.png', 0x99, 0x99, 0x99)
create_png(base + r'\tab-explore-active.png', 0x2C, 0x3E, 0x50)
create_png(base + r'\tab-mine.png', 0x99, 0x99, 0x99)
create_png(base + r'\tab-mine-active.png', 0x2C, 0x3E, 0x50)

print('8 icons created')
