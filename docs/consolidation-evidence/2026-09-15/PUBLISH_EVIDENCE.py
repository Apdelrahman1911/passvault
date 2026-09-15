#!/usr/bin/python3
"""Create one new evidence ref with an alternate index; never modify protected refs."""
import fcntl, hashlib, json, os, signal, subprocess, time
from pathlib import Path
H=Path('/root/projects/PassVault/consolidation-20260915');S=H/'source';R=H/'publish-evidence-private'
A=json.loads((H/'PUBLISH-EVIDENCE-APPROVAL.json').read_text())
assert A['accepted'] and A['helper_sha256']==hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
assert not R.exists() and not R.is_symlink() and not (H/'PUBLISH-EVIDENCE-RESULT.json').exists()
base='df30e43c26184c30a9845c80c368d3ad5413f125';ref='refs/heads/codex/consolidation-evidence-20260915'
lock=os.open('/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock',os.O_RDONLY|os.O_NOFOLLOW)
t=os.fstat(lock);assert [t.st_dev,t.st_ino,t.st_uid,t.st_mode&0o777]==[24,13719326,0,384];fcntl.flock(lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
slot=Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/EXECUTION_SLOT.json')
assert json.loads(slot.read_text())['operation']['active'] is False
assert os.statvfs(H).f_bavail*os.statvfs(H).f_frsize>=3*1024**3
files=A['files'];assert len(files)<100
for name,digest in files.items():
 p=H/name;assert '..' not in Path(name).parts and not Path(name).is_absolute() and p.is_file() and not p.is_symlink()
 assert p.stat().st_size<4*1024**2 and hashlib.sha256(p.read_bytes()).hexdigest()==digest
assert sum((H/n).stat().st_size for n in files)<12*1024**2
assert json.loads((H/'GRADLE-RESULT.json').read_text())['cleanup']=='OWNED_OUTPUTS_AND_PRIVATE_RUNTIME_REMOVED'
env={'PATH':'/usr/bin:/bin','HOME':str(R/'home'),'TMPDIR':str(R/'tmp'),'GH_CONFIG_DIR':'/root/.config/gh','GH_PROMPT_DISABLED':'1','LANG':'C.UTF-8','GIT_CONFIG_GLOBAL':'/dev/null','GIT_CONFIG_SYSTEM':'/dev/null','GIT_CONFIG_NOSYSTEM':'1','GIT_NO_REPLACE_OBJECTS':'1','GIT_NO_LAZY_FETCH':'1','GIT_TERMINAL_PROMPT':'0','GIT_INDEX_FILE':str(R/'index'),'GIT_AUTHOR_NAME':'Codex','GIT_AUTHOR_EMAIL':'codex@openai.com','GIT_COMMITTER_NAME':'Codex','GIT_COMMITTER_EMAIL':'codex@openai.com'}
for k in ['GH_TOKEN','GITHUB_TOKEN']:
 if k in os.environ:env[k]=os.environ[k]
git=['/usr/bin/git','-c','core.hooksPath=/dev/null','-c','gc.auto=0','-c','maintenance.auto=false','-c','commit.gpgsign=false','-c','protocol.allow=never','-c','protocol.https.allow=always','-c','credential.helper=!gh auth git-credential']
index_before=hashlib.sha256((S/'.git/index').read_bytes()).hexdigest()
head_before=(S/'.git/HEAD').read_bytes()
result={'commands':[],'started':time.time(),'destination':ref,'base':base,'files':files};p=None
for sig in [signal.SIGTERM,signal.SIGINT]:signal.signal(sig,lambda s,f: (_ for _ in ()).throw(InterruptedError()))
def run(args, data=None):
 global p
 result['last_settled']=False
 p=subprocess.Popen(git+args,cwd=S,env=env,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE,start_new_session=True)
 child=p
 out,err=child.communicate(data,timeout=120);code=child.returncode;p=None
 assert len(out)+len(err)<2*1024**2
 try:os.killpg(child.pid,0);settled=False
 except ProcessLookupError:settled=True
 result['last_settled']=settled
 result['commands'].append({'args':args,'exit':code,'stdout':out.decode(errors='replace'),'stderr':err.decode(errors='replace'),'settled':settled})
 assert settled;p=None;assert code==0
 return out.decode().strip()
try:
 R.mkdir(mode=0o700);identity=(R.stat().st_dev,R.stat().st_ino)
 for n in ['home','tmp']:(R/n).mkdir()
 assert run(['ls-remote','--refs','origin',ref])==''
 assert run(['rev-parse','HEAD'])=='0dbc12c7f1b7770e75963c751c8c67af6e8b057a'
 run(['read-tree',base])
 for name in sorted(files):
  blob=run(['hash-object','-w','--stdin'],(H/name).read_bytes())
  dest='docs/consolidation-evidence/2026-09-15/'+name
  run(['update-index','--add','--cacheinfo','100644',blob,dest])
 tree=run(['write-tree'])
 changed=run(['diff-tree','--no-commit-id','--name-only','-r',base,tree]).splitlines()
 assert changed==sorted('docs/consolidation-evidence/2026-09-15/'+n for n in files)
 commit=run(['commit-tree',tree,'-p',base],b'Preserve selective consolidation verification evidence\n\nNo protected branch, release, Store or build-number changes.\n')
 result.update(commit=commit,tree=tree)
 # Ordinary new-ref push only: no force, no tag writes, no branch deletion.
 assert run(['ls-remote','--refs','origin',ref])==''
 run(['push','--porcelain','origin',commit+':'+ref])
 assert run(['ls-remote','--refs','origin',ref])==commit+'\t'+ref
 assert run(['rev-parse','HEAD'])=='0dbc12c7f1b7770e75963c751c8c67af6e8b057a'
 result['status']='NEW_EVIDENCE_REF_PUSHED_AND_VERIFIED'
except BaseException as e:result.update(status='FAILED_RETAIN_NO_AUTOMATIC_RETRY',error=repr(e))
finally:
 if p is not None:
  try:
   assert p.returncode is None, 'Reaped leader: residual group is HOLD, not signal authority'
   os.killpg(p.pid,signal.SIGTERM)
   try:p.communicate(timeout=5)
   except subprocess.TimeoutExpired:
    assert p.returncode is None, 'Reaped leader after TERM: retain HOLD'
    os.killpg(p.pid,signal.SIGKILL);p.communicate(timeout=5)
   try:os.killpg(p.pid,0);result['last_settled']=False
   except ProcessLookupError:result['last_settled']=True
  except ProcessLookupError:result['last_settled']=True
  except BaseException as e:result['last_settled']=False;result['settlement_error']=repr(e)
 try:
  result['normal_index_unchanged']=hashlib.sha256((S/'.git/index').read_bytes()).hexdigest()==index_before
  result['head_unchanged']=(S/'.git/HEAD').read_bytes()==head_before
  assert result['normal_index_unchanged'] and result['head_unchanged']
  assert result.get('last_settled',True) and all(x['settled'] for x in result['commands'])
  assert not R.is_symlink() and identity==(R.stat().st_dev,R.stat().st_ino)
  # Only the owned alternate index and empty private directories are disposable.
  i=R/'index'
  if i.exists():assert i.is_file() and not i.is_symlink();i.unlink()
  (R/'home').rmdir();(R/'tmp').rmdir();R.rmdir();result['cleanup']='EMPTY_PRIVATE_AND_ALTERNATE_INDEX_REMOVED'
 except BaseException as e:result['cleanup']='HOLD:'+repr(e)
 result['ended']=time.time();(H/'PUBLISH-EVIDENCE-RESULT.json').write_text(json.dumps(result,indent=2)+'\n');os.close(lock)
raise SystemExit(0 if result['status']=='NEW_EVIDENCE_REF_PUSHED_AND_VERIFIED' and not result['cleanup'].startswith('HOLD:') else 1)
