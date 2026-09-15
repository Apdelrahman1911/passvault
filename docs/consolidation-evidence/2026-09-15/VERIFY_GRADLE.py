#!/usr/bin/python3
"""Fresh finite integration cohort; never imports/replays audit execution helpers."""
import ctypes,fcntl,hashlib,json,os,signal,stat,subprocess,time
from pathlib import Path
H=Path('/root/projects/PassVault/consolidation-20260915');S=H/'source';R=H/'gradle-private';E=H/'gradle-evidence'
A=json.loads((H/'GRADLE-APPROVAL.json').read_text());assert A['accepted'] and A['sha256']==hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
assert not R.exists() and not R.is_symlink() and not E.exists()
lock=os.open('/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock',os.O_RDONLY|os.O_NOFOLLOW)
st=os.fstat(lock);assert [st.st_dev,st.st_ino,st.st_uid,st.st_mode&0o777]==[24,13719326,0,384];fcntl.flock(lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
D=Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root')
assert json.loads((D/'EXECUTION_SLOT.json').read_text())['operation']['active'] is False
assert not (H/'GRADLE-RESULT.json').exists()
assert ctypes.CDLL(None,use_errno=True).prctl(36,1,0,0,0)==0 # adopt original orphaned descendants

def tree():
 result={}
 for root,ds,fs in os.walk(S,followlinks=False):
  if Path(root)==S:ds.remove('.git')
  for n in ds:assert not (Path(root)/n).is_symlink()
  for n in fs:
   p=Path(root)/n;assert p.is_file() and not p.is_symlink();result[str(p.relative_to(S))]=hashlib.sha256(p.read_bytes()).hexdigest()
 return result
assert tree()==A['source_files']
outputs=[S/p for p in A['output_roots']];assert all(not p.exists() and not p.is_symlink() and p.is_relative_to(S) for p in outputs)
assert hashlib.sha256((H/'focused-tests.init.gradle').read_bytes()).hexdigest()==A['init_sha256']
def resources():
 fs=os.statvfs(H);m={x.split(':')[0]:int(x.split()[1])*1024 for x in Path('/proc/meminfo').read_text().splitlines() if ':' in x and x.split()[1].isdigit()}
 return {'disk':fs.f_bavail*fs.f_frsize,'ram_available':m['MemAvailable'],'ram_total':m['MemTotal']}
assert resources()['disk']>=8*1024**3 and resources()['ram_available']>=resources()['ram_total']*.25
# The only process tree we may signal is this controller's positively observed descendants.
def processes():
 result={}
 for p in Path('/proc').iterdir():
  if not p.name.isdigit():continue
  try:
   text=(p/'stat').read_text();f=text[text.rfind(')')+2:].split();result[int(p.name)]=(int(f[1]),int(f[19]),f[0])
  except (FileNotFoundError,ProcessLookupError,PermissionError):pass
 return result
owned={}
def observe():
 ps=processes();parents={os.getpid()};changed=True
 while changed:
  changed=False
  for pid,(ppid,birth,state) in ps.items():
   if ppid in parents and pid not in parents:parents.add(pid);owned[pid]=birth;changed=True
 return {pid:birth for pid,birth in owned.items() if pid in ps and ps[pid][1]==birth and ps[pid][2]!='Z'}
# No other JVM/native build is admitted at this start point; never stop somebody else's process.
for pid in processes():
 try:assert Path(f'/proc/{pid}/comm').read_text().strip() not in {'java','gradle','kotlinc','cmake','ninja','xcodebuild','emulator'}
 except (FileNotFoundError,ProcessLookupError):pass
env={'PATH':'/usr/lib/jvm/java-17-openjdk-amd64/bin:/usr/bin:/bin','JAVA_HOME':'/usr/lib/jvm/java-17-openjdk-amd64','HOME':str(R/'home'),'TMPDIR':str(R/'tmp'),'GRADLE_USER_HOME':str(R/'gradle'),'GRADLE_RO_DEP_CACHE':'/root/.gradle/caches/modules-2','ANDROID_HOME':'/opt/android-sdk','ANDROID_SDK_ROOT':'/opt/android-sdk','LANG':'C.UTF-8','LC_ALL':'C.UTF-8','JAVA_TOOL_OPTIONS':f'-Duser.home={R}/home -Djava.io.tmpdir={R}/tmp -Djna.tmpdir={R}/tmp -XX:ActiveProcessorCount=1 -XX:-UsePerfData -XX:-CreateCoredumpOnCrash','GIT_CONFIG_GLOBAL':'/dev/null','GIT_CONFIG_SYSTEM':'/dev/null','GIT_CONFIG_NOSYSTEM':'1'}
base=['./gradlew','--no-daemon','--max-workers=1','--no-parallel','--no-configure-on-demand','--no-configuration-cache','--no-build-cache','-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8','-Pkotlin.compiler.execution.strategy=in-process','-Pandroid.builder.sdkDownload=false','-I',str(H/'focused-tests.init.gradle')]
cmd=base+A['task_arguments']
r={'source_files':A['source_files'],'commands':[],'resources':[resources()],'started':time.time()};p=None;started=False;stop_ok=False;identity=None
for sig in [signal.SIGTERM,signal.SIGINT]:signal.signal(sig,lambda s,f: (_ for _ in ()).throw(InterruptedError()))
def run(name,argv,seconds):
 global p
 with (E/(name+'.log')).open('xb') as out:
  p=subprocess.Popen(argv,cwd=S,env=env,stdin=subprocess.DEVNULL,stdout=out,stderr=subprocess.STDOUT,start_new_session=True);deadline=time.monotonic()+seconds
  while p.poll() is None:
   observe();point=resources();assert time.monotonic()<deadline and point['disk']>=5*1024**3 and point['ram_available']>=point['ram_total']*.20
   assert (E/(name+'.log')).stat().st_size<16*1024**2;time.sleep(.3)
  code=p.returncode;assert (E/(name+'.log')).stat().st_size<16*1024**2;observe();r['commands'].append({'name':name,'argv':argv,'exit':code});p=None
 return code

def settle_owned():
 for sig in [signal.SIGTERM,signal.SIGKILL]:
  alive=observe()
  for pid,birth in alive.items():
   try:
    fd=os.pidfd_open(pid)
    try:
     now=processes().get(pid)
     if now and now[1]==birth:signal.pidfd_send_signal(fd,sig)
    finally:os.close(fd)
   except ProcessLookupError:pass
  deadline=time.monotonic()+5
  while observe() and time.monotonic()<deadline:time.sleep(.1)
  if not observe():break
 if p is not None:p.wait(timeout=5)
 # Adopted terminated children are ours and may now be reaped.
 while True:
  try:
   if os.waitpid(-1,os.WNOHANG)[0]==0:break
  except ChildProcessError:break
 return not observe()

def remove_owned(path):
 # Run-created allowlist only, no symlink traversal or special files. Reject before deleting any.
 if not path.exists() and not path.is_symlink():return
 rootst=path.lstat();assert stat.S_ISDIR(rootst.st_mode) and not stat.S_ISLNK(rootst.st_mode)
 entries=[]
 for root,ds,fs in os.walk(path,followlinks=False):
  for n in ds+fs:
   q=Path(root)/n;t=q.lstat();assert not stat.S_ISLNK(t.st_mode) and (stat.S_ISREG(t.st_mode) or stat.S_ISDIR(t.st_mode));entries.append((q,t.st_dev,t.st_ino,stat.S_ISDIR(t.st_mode)))
 rootst=path.lstat();assert stat.S_ISDIR(rootst.st_mode)
 for q,dev,ino,isdir in sorted(entries,key=lambda x:len(x[0].parts),reverse=True):
  t=q.lstat();assert (t.st_dev,t.st_ino)==(dev,ino)
  q.rmdir() if isdir else q.unlink()
 assert (path.lstat().st_dev,path.lstat().st_ino)==(rootst.st_dev,rootst.st_ino);path.rmdir()
try:
 E.mkdir();R.mkdir(mode=0o700);identity=(R.stat().st_dev,R.stat().st_ino)
 for n in ['home','tmp','gradle']:(R/n).mkdir()
 started=True;r['build_exit']=run('build',cmd,2700)
 r['status']='BUILD_COMPLETED'
except BaseException as error:r.update(status='FAILED_NO_RETRY',error=repr(error))
finally:
 try:
  # Stop the original command's positively owned tree after interruption, then original wrapper stop once.
  if p is not None:assert settle_owned()
  if started:
   stop_ok=run('stop',base[:1]+['--stop','--no-daemon'],60)==0
  r['stop_ok']=stop_ok;r['owned_settled']=settle_owned();assert r['owned_settled']
  # Reject link/special-file roots and entries before any output evidence read.
  for output in outputs:
   if not output.exists() and not output.is_symlink():continue
   assert stat.S_ISDIR(output.lstat().st_mode)
   for dr,ds,fs in os.walk(output,followlinks=False):
    for n in ds+fs:
     t=(Path(dr)/n).lstat();assert stat.S_ISREG(t.st_mode) or stat.S_ISDIR(t.st_mode)
  # Keep exact XMLs before any generated-output removal.
  for root in outputs:
   if root.name!='build' or not root.exists():continue
   result=root/'test-results'
   if result.exists():
    for x in result.glob('**/*.xml'):
     assert not x.is_symlink() and x.stat().st_size<=4*1024**2
     dest=E/'xml'/x.relative_to(S);dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(x.read_bytes())
  # Preserve the four selected Detekt checkstyle reports, not only JUnit XML.
  for module in ['core/domain','core/database','feature/credential','app-desktop']:
   report=S/module/'build/reports/detekt'
   if report.exists():
    for x in report.glob('*.xml'):
     assert not x.is_symlink() and x.stat().st_size<=4*1024**2
     dest=E/'static-analysis'/x.relative_to(S);dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(x.read_bytes())
  assert stop_ok # Failed original stop retains outputs/runtime; no automatic retry.
  for root in outputs:remove_owned(root)
  r['source_unchanged']=tree()==A['source_files'];assert r['source_unchanged']
  assert stop_ok and identity==(R.stat().st_dev,R.stat().st_ino)
  remove_owned(R);r['cleanup']='OWNED_OUTPUTS_AND_PRIVATE_RUNTIME_REMOVED'
 except BaseException as error:
  r['cleanup']='HOLD:'+repr(error)
  try:r['owned_settled']=settle_owned()
  except BaseException as e:r['settlement_error']=repr(e)
 r['ended']=time.time();r['resources'].append(resources());(H/'GRADLE-RESULT.json').write_text(json.dumps(r,indent=2)+'\n');os.close(lock)
raise SystemExit(0 if r.get('build_exit')==0 and r['cleanup']=='OWNED_OUTPUTS_AND_PRIVATE_RUNTIME_REMOVED' else 1)
