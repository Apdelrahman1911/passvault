#!/usr/bin/python3
"""One fresh, synthetic release-regression batch. No Store/build/network operations."""
import os,json,fcntl,subprocess,signal,time,hashlib
from pathlib import Path
H=Path('/root/projects/PassVault/consolidation-20260915');S=H/'source';R=H/'release-test-private'
approval=json.loads((H/'RELEASE-TEST-APPROVAL.json').read_text())
assert approval['sha256']==hashlib.sha256(Path(__file__).read_bytes()).hexdigest() and approval['accepted'] is True
assert not R.exists() and not R.is_symlink() and not (H/'RELEASE-TEST-RESULT.json').exists()
lock=os.open('/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock',os.O_RDONLY|os.O_NOFOLLOW)
st=os.fstat(lock);assert [st.st_dev,st.st_ino,st.st_uid,st.st_mode&0o777]==[24,13719326,0,384];fcntl.flock(lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
D=Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root')
assert json.loads((D/'EXECUTION_SLOT.json').read_text())['operation']['active'] is False
# Bind every regular source-tree file; .git is transport metadata and never executed.
def manifest():
 result={}
 for root,ds,fs in os.walk(S,followlinks=False):
  if Path(root)==S:ds.remove('.git')
  for n in ds:assert not (Path(root)/n).is_symlink()
  for n in fs:
   p=Path(root)/n;assert not p.is_symlink();result[str(p.relative_to(S))]=hashlib.sha256(p.read_bytes()).hexdigest()
 return result
before=manifest();assert before==approval['source_files']
env={'PATH':'/usr/bin:/bin','HOME':str(R/'home'),'TMPDIR':str(R/'tmp'),'LANG':'C.UTF-8','LC_ALL':'C.UTF-8','GIT_CONFIG_GLOBAL':'/dev/null','GIT_CONFIG_SYSTEM':'/dev/null','GIT_CONFIG_NOSYSTEM':'1','GIT_NO_REPLACE_OBJECTS':'1','GIT_TERMINAL_PROMPT':'0'}
commands=[('release-regressions',['/usr/bin/ruby','scripts/test-release-regressions.rb'])]
r={'commands':[],'start':time.time(),'scope':'Synthetic production-boundary release fixtures, not PVA029, not signing or provider evidence','source_files':before};p=None;identity=None;created=[]
for sig in [signal.SIGTERM,signal.SIGINT]:signal.signal(sig,lambda s,f: (_ for _ in ()).throw(InterruptedError()))
try:
 R.mkdir(mode=0o700);identity=(R.stat().st_dev,R.stat().st_ino)
 for n in ['home','tmp']:(R/n).mkdir();created.append(n)
 for label,cmd in commands:
  assert os.statvfs(H).f_bavail*os.statvfs(H).f_frsize>=3*1024**3
  with (H/(label+'.stdout')).open('xb') as out,(H/(label+'.stderr')).open('xb') as err:
   p=subprocess.Popen(cmd,cwd=S,env=env,stdin=subprocess.DEVNULL,stdout=out,stderr=err,start_new_session=True);deadline=time.monotonic()+180
   while p.poll() is None:
    assert time.monotonic()<deadline and os.statvfs(H).f_bavail*os.statvfs(H).f_frsize>=3*1024**3
    assert (H/(label+'.stdout')).stat().st_size<2*1024**2 and (H/(label+'.stderr')).stat().st_size<2*1024**2
    time.sleep(.1)
   code=p.returncode
   assert (H/(label+'.stdout')).stat().st_size<2*1024**2 and (H/(label+'.stderr')).stat().st_size<2*1024**2
  try:os.killpg(p.pid,0);settled=False
  except ProcessLookupError:settled=True
  r['commands'].append({'label':label,'argv':cmd,'exit':code,'settled':settled});assert settled;p=None
  # Preserve failures; independent second suite may still run after a failed first.
 r['source_unchanged']=manifest()==before;assert r['source_unchanged']
 r['status']='CAPTURED'
except BaseException as e:r.update(status='FAILED_NO_RETRY',error=repr(e))
finally:
 if p is not None:
  try:
   if p.poll() is None:
    try:os.killpg(p.pid,signal.SIGTERM)
    except ProcessLookupError:pass
    try:p.wait(timeout=5)
    except subprocess.TimeoutExpired:
     try:os.killpg(p.pid,signal.SIGKILL)
     except ProcessLookupError:pass
     p.wait(timeout=5)
   try:os.killpg(p.pid,0);r['last_group_settled']=False
   except ProcessLookupError:r['last_group_settled']=True
  except BaseException as error:
   r['last_group_settled']=False;r['settlement_error']=repr(error)
 try:
  assert identity is not None and not R.is_symlink() and (R.stat().st_dev,R.stat().st_ino)==identity
  assert all(x['settled'] for x in r['commands']) and r.get('last_group_settled',True)
  for n in created:(R/n).rmdir()
  R.rmdir();r['cleanup']='EMPTY_OWNED_HOME_TMP_REMOVED'
 except BaseException as e:r['cleanup']='HOLD:'+repr(e)
 r['end']=time.time();(H/'RELEASE-TEST-RESULT.json').write_text(json.dumps(r,indent=2)+'\n');os.close(lock)
raise SystemExit(0 if r['status']=='CAPTURED' and r['cleanup'].startswith('EMPTY_') and all(x['exit']==0 for x in r['commands']) else 1)
