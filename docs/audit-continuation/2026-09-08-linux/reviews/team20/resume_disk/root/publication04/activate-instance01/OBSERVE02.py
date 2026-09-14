#!/usr/bin/python3.12
"""Bounded original macOS JNA CI metadata/compact-ZIP observer; never launches/reruns CI."""
import hashlib,json,os,signal,stat,subprocess,sys,time,zipfile
from pathlib import Path
D=Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/reviews/team20/resume_disk/root')
A=D/'publication04/activate-instance01'
E=D/'macos-jna-ci01'
GH='/usr/bin/gh'
REPO='Apdelrahman1911/passvault'
LIMIT=32*1024**2
child=None
cancelled=False
result={'status':'UNSTARTED','cases':0}
def require(ok,msg):
    if not ok: raise RuntimeError(msg)
def digest(b): return hashlib.sha256(b).hexdigest()
def interrupt(sig,frame):
    global cancelled
    cancelled=True
for sig in (signal.SIGTERM,signal.SIGINT):signal.signal(sig,interrupt)
require(len(sys.argv)==3 and sys.argv[1] in ('discover','status','artifacts','download'),'Fixed observation phase')
phase,number=sys.argv[1:]
require(number.isdecimal() and number==str(int(number)) and 1<=int(number)<=64,'Finite unique observation index')
require(digest(Path(__file__).read_bytes())==json.loads((A/'OBSERVER02-APPROVAL.json').read_text())['source_sha256'],'Independently reviewed observer bytes')
approval=json.loads((A/'OBSERVER02-APPROVAL.json').read_text())
require(approval['reviewer']=='/root/current_ledger' and approval['status']=='ACCEPT_BOUNDED_ORIGINAL_CI_OBSERVER','Independent observer approval')
require(digest(Path(GH).read_bytes())=='2fd925d68889746976958342fb749bf102bc7dc8bcba3abfa533a80ad7791673','Original gh image')
activation=json.loads((A/'RESULT.json').read_text())
require(activation['status']=='MACOS_ACTIVATION_PUSH_VERIFIED_RUN_MUST_BE_IDENTIFIED','Verified original activation only')
commit=activation['commit']
require(len(commit)==40 and all(c in '0123456789abcdef' for c in commit),'Exact original commit')
require(E.resolve()==E and E.is_dir() and not E.is_symlink(),'Root-admitted fresh evidence directory')
context=json.loads((E/'CONTEXT.json').read_text())
require(context['commit']==commit and context['no_other_audit_build_or_ci'] is True,'Original context')
require(time.time()<=context['activation_completed_epoch']+3600,'One hour total observation bound')
if phase=='discover':
    require(time.time()<=context['activation_completed_epoch']+300,'Five minute discovery bound')
    args=['api',f'repos/{REPO}/actions/runs?head_sha={commit}&event=push&per_page=5']
else:
    identity=json.loads((E/'ORIGINAL-RUN.json').read_text());run=identity['databaseId']
    require(type(run)is int and run>0 and identity['headSha']==commit and identity['event']=='push' and identity['attempt']==1 and identity['headBranch']=='codex/audit-continuation-linux-20260908','Original run identity')
    if phase=='status':args=['api',f'repos/{REPO}/actions/runs/{run}']
    else:
        terminal=json.loads((E/'TERMINAL.json').read_text())
        require(terminal['databaseId']==run and terminal['headSha']==commit and terminal['status']=='completed' and terminal['attempt']==1,'Original terminal before artifact retrieval')
        if phase=='artifacts':args=['api',f'repos/{REPO}/actions/runs/{run}/artifacts']
        else:
            artifact=json.loads((E/'ARTIFACT.json').read_text());aid=artifact['id']
            require(type(aid)is int and aid>0 and artifact['name']==f'macos-jna-lifetime-01-{run}-1' and artifact['expired']is False and 0<artifact['size_in_bytes']<=LIMIT,'Exact bounded compact artifact')
            require(artifact['workflow_run']['id']==run and artifact['workflow_run']['head_sha']==commit,'Artifact original run binding')
            args=['api',f'repos/{REPO}/actions/artifacts/{aid}/zip']
root=E/('observation-'+number)
require(not root.exists() and not root.is_symlink(),'Unconsumed observation namespace')
root.mkdir(mode=0o700)
root_identity=(root.stat().st_dev,root.stat().st_ino)
(root/'home').mkdir(mode=0o700);(root/'tmp').mkdir(mode=0o700)
env={'PATH':'/usr/bin:/bin','LANG':'C.UTF-8','LC_ALL':'C.UTF-8','TZ':'UTC','HOME':str(root/'home'),'TMPDIR':str(root/'tmp'),'GH_CONFIG_DIR':'/root/.config/gh','GH_PROMPT_DISABLED':'1'}
for key in ('GH_TOKEN','GITHUB_TOKEN'):
    if key in os.environ:env[key]=os.environ[key]
out=root/('artifact.zip' if phase=='download' else 'response.json');err=root/'stderr.tmp'
result.update(phase=phase,argv=[GH,*args],commit=commit)
try:
    fs=os.statvfs(E);require(fs.f_bavail*fs.f_frsize>=3*1024**3,'Three GiB DATA floor')
    with out.open('xb') as stdout,err.open('xb') as stderr:
        child=subprocess.Popen([GH,*args],stdin=subprocess.DEVNULL,stdout=stdout,stderr=stderr,cwd=root,env=env,start_new_session=True,close_fds=True)
        result['pid']=child.pid;deadline=time.monotonic()+60
        while child.poll() is None:
            require(not cancelled and time.monotonic()<deadline,'Original cancellation/deadline')
            require(out.stat().st_size<=(LIMIT if phase=='download' else 2*1024**2) and err.stat().st_size<=65536,'Compact byte limit')
            time.sleep(.2)
        require(child.returncode==0 and not cancelled,'Original gh failed; no automatic retry')
    data=out.read_bytes();require(len(data)<=(LIMIT if phase=='download' else 2*1024**2),'Final byte limit')
    result['response']={'bytes':len(data),'sha256':digest(data)}
    if phase=='download':
        # Validate every ZIP member before extracting any. Never execute contents.
        with zipfile.ZipFile(out) as archive:
            members=archive.infolist();names=set();total=0
            require(1<=len(members)<=128,'Compact member count')
            for member in members:
                name=member.filename;mode=member.external_attr>>16
                require(name not in names and '/' not in name and '\\' not in name and name not in ('','.','..') and '\x00' not in name and Path(name).suffix in ('.json','.log','.xml'),'Flat allowlisted evidence names')
                require(not member.is_dir() and not stat.S_ISLNK(mode) and stat.S_IFMT(mode) in (0,stat.S_IFREG) and not member.flag_bits&1,'Ordinary unencrypted evidence member')
                require(0<=member.file_size<=16*1024**2,'Individual uncompressed bound')
                names.add(name);total+=member.file_size
            require(total<=64*1024**2,'Total uncompressed budget')
            target=E/'artifacts';require(not target.exists() and not target.is_symlink(),'Fresh extraction only');target.mkdir(mode=0o700)
            for member in members:
                data=archive.read(member);require(len(data)==member.file_size,'CRC/size validated')
                with (target/member.filename).open('xb') as dest:dest.write(data)
            result['extracted']={'members':len(members),'bytes':total,'path':str(target)}
        out.unlink();result['transport_zip_cleanup']='REMOVED_AFTER_VERIFIED_EXTRACTION'
    result['status']='ORIGINAL_RESPONSE_CAPTURED_NOT_APPLICATION_VERIFICATION'
except BaseException as failure:
    result.update(status='FAILED_NO_AUTOMATIC_RETRY',error=type(failure).__name__+': '+str(failure))
finally:
    if child is not None:
        if child.poll() is None:
            os.killpg(child.pid,signal.SIGTERM)
            try:child.wait(timeout=10)
            except subprocess.TimeoutExpired:os.killpg(child.pid,signal.SIGKILL);child.wait(timeout=10)
        result['exit']=child.returncode
        try:os.killpg(child.pid,0)
        except ProcessLookupError:result['original_group_settled']=True
        else:result['original_group_settled']=False
    try:
        require(root.resolve()==root and (root.stat().st_dev,root.stat().st_ino)==root_identity,'Original cleanup root')
        require(child is None or result.get('original_group_settled') is True,'Unsettled group retains private paths HOLD')
        if err.exists():
            data=err.read_bytes();result['stderr']={'bytes':len(data),'sha256':digest(data)};err.unlink()
        (root/'home').rmdir();(root/'tmp').rmdir();result['private_cleanup']='EMPTY_PRIVATE_HOME_TMP_REMOVED'
    except BaseException as failure:result['private_cleanup']='HOLD_'+type(failure).__name__
    (root/'RESULT.json').write_text(json.dumps(result,indent=2)+'\n')
raise SystemExit(0 if result['status']=='ORIGINAL_RESPONSE_CAPTURED_NOT_APPLICATION_VERIFICATION' and result.get('original_group_settled') is True else 1)
