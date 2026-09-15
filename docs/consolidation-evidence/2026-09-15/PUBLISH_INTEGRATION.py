#!/usr/bin/python3
"""Commit and push only the reviewed selective integration on its new branch."""
import fcntl,hashlib,json,os,signal,subprocess,time
from pathlib import Path
H=Path('/root/projects/PassVault/consolidation-20260915');S=H/'source';R=H/'publish-integration-private'
A=json.loads((H/'PUBLISH-INTEGRATION-APPROVAL.json').read_text())
assert A['accepted'] and A['helper_sha256']==hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
assert not R.exists() and not R.is_symlink() and not (H/'PUBLISH-INTEGRATION-RESULT.json').exists()
ref='refs/heads/codex/consolidate-completed-20260915';base='0dbc12c7f1b7770e75963c751c8c67af6e8b057a'
lock=os.open('/root/projects/PassVault/.audit-coordination-linux-20260914-c20/build.lock',os.O_RDONLY|os.O_NOFOLLOW)
t=os.fstat(lock);assert [t.st_dev,t.st_ino,t.st_uid,t.st_mode&0o777]==[24,13719326,0,384];fcntl.flock(lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
slot=Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root/EXECUTION_SLOT.json')
assert json.loads(slot.read_text())['operation']['active'] is False
assert os.statvfs(H).f_bavail*os.statvfs(H).f_frsize>=3*1024**3
for name,digest in A['files'].items():
 p=S/name;assert '..' not in Path(name).parts and not Path(name).is_absolute() and p.is_file() and not p.is_symlink()
 assert hashlib.sha256(p.read_bytes()).hexdigest()==digest
assert json.loads((H/'GRADLE-RESULT.json').read_text())['cleanup']=='OWNED_OUTPUTS_AND_PRIVATE_RUNTIME_REMOVED'
evidence=json.loads((H/'PUBLISH-EVIDENCE-RESULT.json').read_text())
assert evidence['status']=='NEW_EVIDENCE_REF_PUSHED_AND_VERIFIED' and evidence['commit']==A['evidence_commit']
env={'PATH':'/usr/bin:/bin','HOME':str(R/'home'),'TMPDIR':str(R/'tmp'),'GH_CONFIG_DIR':'/root/.config/gh','GH_PROMPT_DISABLED':'1','LANG':'C.UTF-8','GIT_CONFIG_GLOBAL':'/dev/null','GIT_CONFIG_SYSTEM':'/dev/null','GIT_CONFIG_NOSYSTEM':'1','GIT_NO_REPLACE_OBJECTS':'1','GIT_NO_LAZY_FETCH':'1','GIT_TERMINAL_PROMPT':'0','GIT_AUTHOR_NAME':'Codex','GIT_AUTHOR_EMAIL':'codex@openai.com','GIT_COMMITTER_NAME':'Codex','GIT_COMMITTER_EMAIL':'codex@openai.com'}
for k in ['GH_TOKEN','GITHUB_TOKEN']:
 if k in os.environ:env[k]=os.environ[k]
git=['/usr/bin/git','-c','core.hooksPath=/dev/null','-c','gc.auto=0','-c','maintenance.auto=false','-c','commit.gpgsign=false','-c','protocol.allow=never','-c','protocol.https.allow=always','-c','credential.helper=!gh auth git-credential']
result={'commands':[],'started':time.time(),'destination':ref,'base':base,'files':A['files'],'evidence_commit':A['evidence_commit']};p=None
for sig in [signal.SIGTERM,signal.SIGINT]:signal.signal(sig,lambda s,f: (_ for _ in ()).throw(InterruptedError()))
def run(args):
 global p
 result['last_settled']=False
 p=subprocess.Popen(git+args,cwd=S,env=env,stdin=subprocess.DEVNULL,stdout=subprocess.PIPE,stderr=subprocess.PIPE,start_new_session=True)
 child=p
 out,err=child.communicate(timeout=120);code=child.returncode;p=None;assert len(out)+len(err)<2*1024**2
 try:os.killpg(child.pid,0);settled=False
 except ProcessLookupError:settled=True
 result['last_settled']=settled
 result['commands'].append({'args':args,'exit':code,'stdout':out.decode(errors='replace'),'stderr':err.decode(errors='replace'),'settled':settled})
 assert settled;p=None;assert code==0
 return out.decode().strip()
try:
 R.mkdir(mode=0o700);identity=(R.stat().st_dev,R.stat().st_ino)
 for n in ['home','tmp']:(R/n).mkdir()
 assert run(['symbolic-ref','HEAD'])==ref and run(['rev-parse','HEAD'])==base
 assert run(['ls-remote','--refs','origin',ref])==''
 assert run(['diff','--cached','--name-only'])==''
 run(['add','--']+sorted(A['files']))
 assert run(['diff','--cached','--name-only']).splitlines()==sorted(A['files'])
 run(['diff','--cached','--check'])
 run(['commit','-m','Consolidate completed hardening fixes and preserve deferred beta gates'])
 commit=run(['rev-parse','HEAD']);tree=run(['rev-parse','HEAD^{tree}'])
 assert run(['status','--porcelain=v1','--untracked-files=all'])==''
 result.update(commit=commit,tree=tree)
 assert run(['ls-remote','--refs','origin',ref])==''
 run(['push','--porcelain','origin',commit+':'+ref])
 assert run(['ls-remote','--refs','origin',ref])==commit+'\t'+ref
 result['protected_refs_after']=run(['ls-remote','--refs','origin','refs/heads/main','refs/heads/testing','refs/heads/release','refs/tags/v1.0.7-rc.1017001'])
 result['status']='INTEGRATION_COMMITTED_PUSHED_VERIFIED_NOT_MERGED'
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
  assert result.get('last_settled',True) and all(x['settled'] for x in result['commands'])
  assert not R.is_symlink() and identity==(R.stat().st_dev,R.stat().st_ino)
  (R/'home').rmdir();(R/'tmp').rmdir();R.rmdir();result['cleanup']='EMPTY_PRIVATE_DIRS_REMOVED'
 except BaseException as e:result['cleanup']='HOLD:'+repr(e)
 result['ended']=time.time();(H/'PUBLISH-INTEGRATION-RESULT.json').write_text(json.dumps(result,indent=2)+'\n');os.close(lock)
raise SystemExit(0 if result['status']=='INTEGRATION_COMMITTED_PUSHED_VERIFIED_NOT_MERGED' and not result['cleanup'].startswith('HOLD:') else 1)
