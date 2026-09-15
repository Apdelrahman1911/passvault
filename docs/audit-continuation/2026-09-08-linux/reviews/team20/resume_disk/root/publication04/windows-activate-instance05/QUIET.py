"""One bounded fresh dedicated-branch status read. No dispatch/rerun/cancel."""
from pathlib import Path
import hashlib,json,os,signal,subprocess,time
H=Path(__file__).parent
OUT=H/'CI-QUIET.json'
assert not OUT.exists()
a=json.loads((H/'QUIET-APPROVAL.json').read_text())
assert a['reviewer']=='/root/current_ledger' and a['status']=='ACCEPT_ONE_BOUNDED_BRANCH_STATUS_READ'
assert hashlib.sha256(Path(__file__).read_bytes()).hexdigest()==a['source_sha256']
assert hashlib.sha256(Path('/usr/bin/gh').read_bytes()).hexdigest()=='2fd925d68889746976958342fb749bf102bc7dc8bcba3abfa533a80ad7791673'
ROOT=H/'quiet01';assert not ROOT.exists() and not ROOT.is_symlink()
original=None;created=[]
env={'PATH':'/usr/bin:/bin','LANG':'C.UTF-8','LC_ALL':'C.UTF-8','TZ':'UTC','HOME':str(ROOT/'home'),'TMPDIR':str(ROOT/'tmp'),'GH_CONFIG_DIR':'/root/.config/gh','GH_PROMPT_DISABLED':'1'}
for key in ('GH_TOKEN','GITHUB_TOKEN'):
 if key in os.environ:env[key]=os.environ[key]
argv=['/usr/bin/gh','run','list','--repo','Apdelrahman1911/passvault','--branch','codex/audit-continuation-linux-20260908','--limit','20','--json','databaseId,status,conclusion,headSha,workflowName']
p=None;r={'scope':'Dedicatedbranch20statuses,no dispatch/cancel/rerun; notglobalidleoroldruntimeinquiry','started_epoch':time.time(),'argv':argv}
def interrupted(signum,frame):raise InterruptedError('ORIGINAL_READ_INTERRUPTED')
for sig in (signal.SIGTERM,signal.SIGINT):signal.signal(sig,interrupted)
try:
 fs=os.statvfs(H);assert fs.f_bavail*fs.f_frsize>=3*1024**3
 ROOT.mkdir(mode=0o700);original=(ROOT.stat().st_dev,ROOT.stat().st_ino)
 for name in ('home','tmp'):(ROOT/name).mkdir(mode=0o700);created.append(name)
 p=subprocess.Popen(argv,cwd=ROOT,env=env,stdin=subprocess.DEVNULL,stdout=subprocess.PIPE,stderr=subprocess.PIPE,start_new_session=True)
 out,err=p.communicate(timeout=60)
 r['exit']=p.returncode
 assert p.returncode==0 and len(out)<=256*1024 and len(err)<=65536
 runs=json.loads(out);assert type(runs)is list and len(runs)<=20
 assert all(type(x)is dict and set(x)=={'databaseId','status','conclusion','headSha','workflowName'} for x in runs)
 r['runs']=runs;r['no_active_listed']=all(x['status']=='completed' for x in runs)
 r['qualification']='Finitebranchcheck plus rootcompleteauditlaunchaccounting, notglobalexhaustiveness; no oldrun evidence fetched.'
except BaseException as e:r['error']=type(e).__name__
finally:
 if p is not None:
  if p.poll() is None:
   os.killpg(p.pid,signal.SIGTERM)
   try:p.wait(timeout=5)
   except subprocess.TimeoutExpired:os.killpg(p.pid,signal.SIGKILL);p.wait(timeout=5)
  r['exit']=p.returncode
  try:os.killpg(p.pid,0);r['original_group_settled']=False
  except ProcessLookupError:r['original_group_settled']=True
 try:
  if original is not None:
   assert (ROOT.stat().st_dev,ROOT.stat().st_ino)==original and not ROOT.is_symlink()
   for name in created:(ROOT/name).rmdir()
   ROOT.rmdir()
  r['cleanup']='EMPTY_ORIGINAL_DIRECTORIES_REMOVED'
 except BaseException as e:r['cleanup']='HOLD_'+type(e).__name__
 r['completed_epoch']=time.time();OUT.write_text(json.dumps(r,indent=2)+'\n')
raise SystemExit(0 if r.get('no_active_listed') and r.get('original_group_settled') and r.get('cleanup')=='EMPTY_ORIGINAL_DIRECTORIES_REMOVED' and 'error' not in r else 1)
