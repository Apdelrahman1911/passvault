"""Fixed bounded read-only public AGP class DATA; never import/execute vendor code."""
import hashlib
import io
import json
from pathlib import Path
import struct
import urllib.request
import zipfile

ROOT = Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/android-parent-api05')
URL = 'https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/9.4.0/gradle-9.4.0.jar'
SIZE = 12137130
SHA = '606a2136d291e69ba18c9b21672ff62de0af65c1eb3a9f69c0e7da6a490348ca'
MEMBER = 'com/android/build/gradle/internal/tasks/ProguardConfigurableTask.class'
# No proxy credentials/cookies/auth handlers or environment-derived HTTP proxy.
class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise ValueError('Unexpected redirect; no retry')

for name in ('ProguardConfigurableTask.class', 'METHODS.json'):
    if (ROOT / name).exists() or (ROOT / name).is_symlink():
        raise ValueError('Output already occupied; no retry')
opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), NoRedirect())
with opener.open(URL, timeout=20) as response:
    if response.status != 200 or response.geturl() != URL:
        raise ValueError('Unexpected response')
    jar = response.read(SIZE + 1)
if len(jar) != SIZE or hashlib.sha256(jar).hexdigest() != SHA:
    raise ValueError('Exact known JAR size/hash mismatch')
with zipfile.ZipFile(io.BytesIO(jar)) as z:
    entries = [x for x in z.infolist() if x.filename == MEMBER]
    if len(entries) != 1 or not 0 < entries[0].file_size <= 256 * 1024:
        raise ValueError('Exact bounded class member required')
    data = z.read(entries[0])
# Fixed JVM class-file metadata reader; retain original bytes for independent interpretation.
stream = io.BytesIO(data)
def take(n):
    b = stream.read(n)
    if len(b) != n:
        raise ValueError('Truncated class file')
    return b
def u1(): return take(1)[0]
def u2(): return struct.unpack('>H', take(2))[0]
def u4(): return struct.unpack('>I', take(4))[0]
if u4() != 0xCAFEBABE:
    raise ValueError('Class magic')
minor, major = u2(), u2()
cp = [None] * u2()
i = 1
while i < len(cp):
    tag = u1()
    if tag == 1:
        cp[i] = take(u2()).decode('utf-8', errors='backslashreplace')
    elif tag in (7, 8, 16, 19, 20):
        cp[i] = u2()
    elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
        take(4)
    elif tag in (5, 6):
        take(8)
        i += 1
    elif tag == 15:
        take(3)
    else:
        raise ValueError('Unknown constant-pool tag')
    i += 1
flags, this, parent = u2(), u2(), u2()
interfaces = [u2() for _ in range(u2())]
def attributes():
    for _ in range(u2()):
        u2()
        take(u4())
def members():
    rows = []
    for _ in range(u2()):
        access, name, signature = u2(), u2(), u2()
        rows.append({'access_flags': access, 'name': cp[name], 'descriptor': cp[signature]})
        attributes()
    return rows
fields, methods = members(), members()
attributes()
if stream.read(1):
    raise ValueError('Trailing class bytes')
result = {'url': URL, 'jar_bytes': SIZE, 'jar_sha256': SHA, 'member': MEMBER,
          'class_bytes': len(data), 'class_sha256': hashlib.sha256(data).hexdigest(),
          'class': cp[cp[this]], 'superclass': cp[cp[parent]], 'major': major,
          'minor': minor, 'access_flags': flags, 'methods': methods,
          'qualification': 'Class metadata DATA, not method purity or runtime execution. UTF8 replacement preserves non-ASCII uncertainty; exact original class retained. No vendor code imported/executed.'}
if result['class'] != 'com/android/build/gradle/internal/tasks/ProguardConfigurableTask':
    raise ValueError('Wrong class')
with (ROOT / 'ProguardConfigurableTask.class').open('xb') as f:
    f.write(data)
with (ROOT / 'METHODS.json').open('x') as f:
    json.dump(result, f, indent=2, sort_keys=True)
    f.write('\n')
print(json.dumps({'retained_class_bytes': len(data), 'methods': len(methods),
                  'class_sha256': result['class_sha256'], 'temporary_disk_bytes': 0}))
