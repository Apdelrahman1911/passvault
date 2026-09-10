#!/usr/bin/python3
# New Windows05 request-only activation. Source-copy accepted C11 Git utilities
# only, never execute/import its consumed main. One reviewed synthetic CI job.

import os,stat,pathlib,json,hashlib,re,subprocess,signal,time,fcntl,selectors
CFG=json.loads(pathlib.Path('/root/projects/PassVault/passvault-linux/docs/audit-continuation/2026-09-08-linux/publication/WINDOWS-05-CONFIG.json').read_bytes())
S=pathlib.Path('/root/projects/PassVault/passvault-linux')
B=S/'docs/audit-continuation/2026-09-08-linux'
PUB=B/'publication'
P=S.parent;T=P/'passvault-publication-20260910-01'
URL='https://github.com/Apdelrahman1911/passvault.git'
BRANCH='codex/audit-continuation-linux-20260908'
BASE=CFG['source_commit']
BASE_TREE=CFG['source_tree']
START=time.monotonic();CANCEL=False;child=None;lockfd=None;parentfd=None;rootfd=None;recfd=None
rec={'schema':1,'owner':'/root','status':'FAIL_HOLD_NO_AUTOMATIC_RETRY','utc':__import__('datetime').datetime.now(__import__('datetime').timezone.utc).isoformat(),'review_sha256':CFG['review_sha256'],'source_workspace':str(S),'publication_clone':str(T),'commands':[],'resource_samples':[],'request_sha256':CFG['request_sha256'],'additional_tests':0,'additional_closures':0,'original_stores':'NO_GIT_COMMANDS_OBJECT_COPIES_PROBES_OR_WRITES_IN_THIS_SCOPE'}
def req(ok,msg):
 if not ok:raise RuntimeError(msg)
def check():
 req(not CANCEL and time.monotonic()-START<600,'cancelled or600s deadline')
def cancelled(*_):
 global CANCEL
 CANCEL=True
signal.signal(signal.SIGINT,cancelled);signal.signal(signal.SIGTERM,cancelled)
def digest(raw):return hashlib.sha256(raw).hexdigest()
def dtuple(a):return [a.st_dev,a.st_ino,a.st_uid,a.st_mode]
def durable():
 if recfd is None:return
 raw=(json.dumps(rec,ensure_ascii=False,indent=2)+'\n').encode();os.lseek(recfd,0,os.SEEK_SET);os.ftruncate(recfd,0)
 at=0
 while at<len(raw):at+=os.write(recfd,raw[at:])
 os.fsync(recfd)
def resource(label,launch=False):
 fs=os.statvfs(P);mem={l.split(':')[0]:int(l.split()[1])*1024 for l in pathlib.Path('/proc/meminfo').read_text().splitlines()}
 row={'label':label,'elapsed_seconds':time.monotonic()-START,'disk_available_bytes':fs.f_bavail*fs.f_frsize,'ram_available_bytes':mem['MemAvailable'],'ram_total_bytes':mem['MemTotal']}
 rec['resource_samples'].append(row)
 req(row['disk_available_bytes'] >= (12 if launch else 8)*1024**3 and mem['MemAvailable']>=mem['MemTotal']*(.25 if launch else .20),'resource floor')
def safe_parent(root,rel,create=False):
 rel=pathlib.PurePosixPath(rel);req(not rel.is_absolute() and '..' not in rel.parts and '\n' not in str(rel) and '\0' not in str(rel),'unsafe relative path')
 cur=root
 for part in rel.parts[:-1]:
  cur=cur/part
  if create and not os.path.lexists(cur):cur.mkdir(mode=0o700)
  st=cur.lstat();req(stat.S_ISDIR(st.st_mode) and st.st_uid==0,'unsafe directory component')
 return root/rel
def read_file(p,cap=16*1024**2):
 check();a=p.lstat();req(stat.S_ISREG(a.st_mode) and a.st_nlink==1 and a.st_size<=cap,'not bounded single-linked regular file')
 fd=os.open(p,os.O_RDONLY|os.O_NOFOLLOW|os.O_CLOEXEC)
 try:
  fields=('dev','ino','uid','mode','nlink','size','mtime_ns','ctime_ns')
  def same(x):return all(getattr(x,'st_'+k)==getattr(a,'st_'+k) for k in fields)
  req(same(os.fstat(fd)),'open/read identity drift')
  chunks=[];total=0
  while True:
   raw=os.read(fd,65536)
   if not raw:break
   chunks.append(raw);total+=len(raw);req(total<=cap,'file byte cap')
  req(same(os.fstat(fd)) and same(p.lstat()),'end/read identity drift')
  return b''.join(chunks),a
 finally:os.close(fd)
def frozen(root,rows):
 out={}
 for r in rows:
  raw,s=read_file(safe_parent(root,r['path']));mode='100755' if s.st_mode&0o111 else '100644'
  req(digest(raw)==r['sha256'] and len(raw)==r['bytes'] and mode==r['git_mode'],'selected byte/mode drift:'+r['path'])
  out[r['path']]=(mode,hashlib.sha1(b'blob '+str(len(raw)).encode()+b'\0'+raw).hexdigest())
 return out
G=['git','--literal-pathspecs','-c','core.hooksPath=/dev/null','-c','core.fsmonitor=false','-c','gc.auto=0','-c','maintenance.auto=false','-c','commit.gpgsign=false','-c','credential.helper=','-c','credential.helper=!gh auth git-credential']
ENV={k:v for k,v in os.environ.items() if not k.startswith('GIT_') and k not in ('SSH_ASKPASS','GIT_ASKPASS')}
ENV.update(GIT_CONFIG_SYSTEM='/dev/null',GIT_CONFIG_GLOBAL='/dev/null',GIT_TERMINAL_PROMPT='0',GIT_OPTIONAL_LOCKS='0',GIT_NO_REPLACE_OBJECTS='1',GIT_NO_LAZY_FETCH='1',GH_PROMPT_DISABLED='1')
def git(args,cwd=None,seconds=60,allowed=(0,),input_bytes=None):
 global child
 check();argv=G+args;r={'argv':argv,'cwd':str(cwd or T),'status':'INTENT'};rec['commands'].append(r);durable()
 child=subprocess.Popen(argv,cwd=cwd or T,env=ENV,stdin=subprocess.PIPE if input_bytes is not None else subprocess.DEVNULL,stdout=subprocess.PIPE,stderr=subprocess.PIPE,start_new_session=True)
 r['pid']=child.pid;r['owned_session']=child.pid;durable();out=bytearray();err=bytearray();sent=0;deadline=min(START+600,time.monotonic()+seconds);sample=time.monotonic()+15
 try:
  with selectors.DefaultSelector() as sel:
   for stream,buf,cap in ((child.stdout,out,4*1024**2),(child.stderr,err,1024**2)):
    os.set_blocking(stream.fileno(),False);sel.register(stream,selectors.EVENT_READ,('read',buf,cap))
   if input_bytes is not None:
    req(len(input_bytes)<=131072,'stdin cap');os.set_blocking(child.stdin.fileno(),False);sel.register(child.stdin,selectors.EVENT_WRITE,('write',None,0));r['stdin_sha256']=digest(input_bytes);r['stdin_bytes']=len(input_bytes)
   while sel.get_map():
    check();req(time.monotonic()<deadline,'Git command deadline')
    if time.monotonic()>=sample:resource('during '+args[0]);durable();sample=time.monotonic()+15
    for key,_ in sel.select(.1):
     kind,buf,cap=key.data
     if kind=='write':
      if sent<len(input_bytes):
       try:sent+=os.write(key.fd,input_bytes[sent:sent+65536])
       except BlockingIOError:continue
      if sent==len(input_bytes):sel.unregister(key.fileobj);child.stdin.close()
     else:
      chunk=os.read(key.fd,65536)
      if not chunk:sel.unregister(key.fileobj);continue
      req(len(buf)+len(chunk)<=cap,'Git output cap; no success with truncation');buf.extend(chunk)
   rc=child.wait(timeout=max(.01,deadline-time.monotonic()))
  decoded=bytes(out).decode('utf-8','strict')
  r.update(status='COMPLETED',exit=rc,stdout_sha256=digest(out),stdout_bytes=len(out),stderr=bytes(err).decode('utf-8','strict'))
  if len(out)<=8192:r['stdout']=decoded
  if args[:3]==['diff','--cached','--check']:
   p=PUB/'WINDOWS-05-ACTIVATION-WHITESPACE.log';fd=os.open(p,os.O_WRONLY|os.O_CREAT|os.O_EXCL|os.O_NOFOLLOW,0o600)
   try:
    view=memoryview(out)
    while view:view=view[os.write(fd,view):]
    os.fsync(fd)
   finally:os.close(fd)
   r['stdout_file']=p.name
  durable()
  req(rc in allowed,'Git nonaccepted exit '+str(rc));check();return decoded
 finally:
  if child is not None:
   if child.poll() is None:
    r['owned_direct_child_kill_attempted']=True;child.kill()
    try:child.wait(timeout=5)
    except subprocess.TimeoutExpired:r['settlement']='DIRECT_CHILD_HOLD';raise
    r['abnormal_descendants']='NOT_PROVEN; no unrelated signal or automatic retry'
   r['direct_child_reaped']=child.poll() is not None
   for pipe in (child.stdin,child.stdout,child.stderr):
    if pipe is not None and not pipe.closed:pipe.close()
   child=None;durable()
def remote(raw):return {line.split('\t')[1]:line.split('\t')[0] for line in raw.splitlines()}
def entries(raw,index=False):
 result={}
 for row in raw.split('\0'):
  if not row:continue
  left,path=row.split('\t',1);a,b,c=left.split()
  if index:req(c=='0','index conflict');mode,oid=a,b
  else:req(b=='blob','non-blob tree member');mode,oid=a,c
  req(path not in result,'duplicate Git entry');result[path]=(mode,oid)
 return result
def root_check():
 req(rootfd is not None and dtuple(os.fstat(rootfd))==rec['namespace_identity'] and dtuple(T.lstat())==rec['namespace_identity'],'original clone root drift')
 req(dtuple(P.lstat())==CFG['namespace_parent'],'clone parent drift')
def inventory(label):
 root_check();count=0;size=0;directories=0;device=os.fstat(rootfd).st_dev
 for path,dirs,files in os.walk(T,followlinks=False):
  check();p=pathlib.Path(path);a=p.lstat();req(stat.S_ISDIR(a.st_mode) and a.st_dev==device and a.st_uid==0,'unsafe clone directory');directories+=1
  for name in dirs:
   a=(p/name).lstat();req(stat.S_ISDIR(a.st_mode) and a.st_dev==23 and a.st_uid==0,'clone symlink/special directory')
  for name in files:
   a=(p/name).lstat();req(stat.S_ISREG(a.st_mode) and a.st_nlink==1 and a.st_dev in (23,24) and a.st_uid==0,'clone shared/link/special file')
   count+=1;size+=a.st_size;req(count<=100000 and size<=512*1024**2,'clone512MiB/count bound')
 rec.setdefault('namespace_inventories',[]).append({'label':label,'files':count,'directories':directories,'logical_bytes':size})
 durable()
try:
 rp=CFG['request_path'];raw,_=read_file(safe_parent(S,rp));request=json.loads(raw)
 req(digest(raw)==CFG['request_sha256'] and request['source_commit']==BASE and request['source_tree']==BASE_TREE,'request/source binding')
 req(rp=='docs/audit-continuation/2026-09-08-linux/requests/windows-cohort-05.json' and request['suite']=='windows-native-24-cohort-v5','only Windows05 request')
 req(request['owner']=='/root' and request['exclusive_build_slot'] is True,'root sole slot request')
 reviewraw,_=read_file(B/CFG['review_path']);review=json.loads(reviewraw)
 req(digest(reviewraw)==CFG['review_sha256'] and review['reviewer']=='/root/native_review'
     and review['disposition']==CFG['review_disposition'] and review['request_sha256']==CFG['request_sha256']
     and review['source_commit']==BASE and review['source_tree']==BASE_TREE,'genuine exact instance review required')
 adopt_raw,_=read_file(B/CFG['root_admission_path']);adopt=json.loads(adopt_raw)
 req(digest(adopt_raw)==CFG['root_admission_sha256'] and adopt['owner']=='/root'
     and adopt['status']=='ADMIT_ONE_WINDOWS_COHORT05_REQUEST_ONLY_ACTIVATION_AND_TARGET_RUN'
     and adopt['request_sha256']==CFG['request_sha256'],'root execution/cleanup admission required')
 slotraw,_=read_file(B/'EXECUTION_SLOT.json');slot=json.loads(slotraw)
 req(digest(slotraw)==CFG['slot_sha256'] and slot['state']=='RESERVED_WINDOWS_COHORT05_AWAITING_ACTIVATION'
     and slot['windows_cohort05']['reservation_active'] is True,'exact Windows05 reservation required')
 selected=CFG['source_inputs'];expected=frozen(S,selected)
 requestrow={'path':rp,'sha256':digest(raw),'bytes':len(raw),'git_mode':'100644'}
 requested=frozen(S,[requestrow])
 lp=P/'.audit-coordination-linux-20260908';req(dtuple(lp.lstat())==[23,661121,0,16832],'original coordination parent changed')
 parentfd=os.open(lp,os.O_RDONLY|os.O_DIRECTORY|os.O_NOFOLLOW|os.O_CLOEXEC)
 lockfd=os.open('build.lock',os.O_RDWR|os.O_NOFOLLOW|os.O_CLOEXEC,dir_fd=parentfd);a=os.fstat(lockfd)
 req((a.st_dev,a.st_ino,a.st_uid,a.st_mode,a.st_nlink,a.st_size)==(24,14189001,0,33152,1,0),'original lock changed')
 fcntl.flock(lockfd,fcntl.LOCK_EX|fcntl.LOCK_NB)
 req(read_file(B/'EXECUTION_SLOT.json')[0]==slotraw and frozen(S,selected)==expected and frozen(S,[requestrow])==requested,'under-lock binding')
 recfd=os.open(PUB/CFG['receipt_name'],os.O_WRONLY|os.O_CREAT|os.O_EXCL|os.O_NOFOLLOW,0o600);durable()
 resource('before request activation',launch=True)
 req(dtuple(T.lstat())==[23,1080545,0,16832],'original publication clone changed')
 rootfd=os.open(T,os.O_RDONLY|os.O_DIRECTORY|os.O_NOFOLLOW|os.O_CLOEXEC);rec['namespace_identity']=[23,1080545,0,16832];root_check();inventory('before activation')
 gd=T/'.git'
 for rel in ('commondir','objects/info/alternates','objects/info/http-alternates'):req(not os.path.lexists(gd/rel),'external sharing not allowed')
 dirs=git(['rev-parse','--absolute-git-dir','--git-common-dir']).splitlines();req(len(dirs)==2 and all((T/x).resolve()==gd for x in dirs),'external Git store')
 req(git(['rev-parse','HEAD','HEAD^{tree}']).splitlines()==[BASE,BASE_TREE],'activation base identity mismatch')
 req(git(['symbolic-ref','--short','HEAD']).strip()==BRANCH,'activation branch mismatch')
 base=entries(git(['ls-tree','-r','-z','--full-tree','HEAD']))
 req(entries(git(['ls-files','--stage','-z']),True)==base and rp not in base,'index differs or request already committed')
 req(git(['diff','--name-only','-z'])=='' and git(['ls-files','--others','--exclude-standard','-z'])=='','preexisting clone work; do not overwrite')
 req(git(['remote','get-url','--push','--all','origin']).splitlines()==[URL],'wrong push destination')
 req(frozen(T,selected)==expected and all(base[p]==v for p,v in expected.items()),'published source differs')
 want=CFG['remotes'];remote_args=['ls-remote',URL,*sorted(want)]
 rec['remote_before']=remote(git(remote_args));req(rec['remote_before']==want,'remote advanced; revalidation required')
 dest=safe_parent(T,rp,create=True);req(not os.path.lexists(dest),'activation path occupied')
 fd=os.open(dest,os.O_WRONLY|os.O_CREAT|os.O_EXCL|os.O_NOFOLLOW|os.O_CLOEXEC,0o600)
 try:
  at=0
  while at<len(raw):at+=os.write(fd,raw[at:])
  os.fsync(fd)
 finally:os.close(fd)
 req(frozen(T,[requestrow])==requested and frozen(S,[requestrow])==requested,'request copy drift')
 req(git(['hash-object','-w','--no-filters','--',rp]).strip()==requested[rp][1],'raw request blob mismatch')
 git(['update-index','--add','-z','--index-info'],input_bytes=('100644 '+requested[rp][1]+'\t'+rp+'\0').encode())
 whole=dict(base);whole.update(requested)
 req(entries(git(['ls-files','--stage','-z']),True)==whole,'nonrequest index change')
 req(git(['diff','--cached','--name-status']).splitlines()==['A\t'+rp],'activation must add only request')
 req(git(['diff','--cached','--check'])=='','request whitespace failure')
 req(frozen(T,selected)==expected and frozen(S,selected)==expected and frozen(T,[requestrow])==requested,'precommit source/request drift')
 req(remote(git(remote_args))==want,'remote advanced before activation commit')
 req(read_file(B/'EXECUTION_SLOT.json')[0]==slotraw,'slot drift before commit')
 git(['-c','user.name=Codex','-c','user.email=codex@openai.com','commit','-m','Validate Windows native writer and secret-lifetime controls'])
 commit,tree=git(['rev-parse','HEAD','HEAD^{tree}']).splitlines()
 req(git(['rev-list','--parents','-n','1','HEAD']).split()==[commit,BASE],'activation not sole P child')
 req(entries(git(['ls-tree','-r','-z','--full-tree','HEAD']))==whole,'activation tree differs')
 req(git(['diff-tree','--no-commit-id','--name-status','-r',BASE,commit]).splitlines()==['A\t'+rp],'activation delta differs')
 rec.update(commit=commit,tree=tree,parent=BASE,parent_tree=BASE_TREE,request=rp,request_blob=requested[rp][1],root_admission_sha256=CFG['root_admission_sha256'])
 req(remote(git(remote_args))==want and read_file(B/'EXECUTION_SLOT.json')[0]==slotraw,'remote/slot advanced before activation push')
 git(['push','--no-signed','--no-follow-tags','--recurse-submodules=no','origin','HEAD:refs/heads/'+BRANCH],seconds=120)
 want_after=dict(want);want_after['refs/heads/'+BRANCH]=commit
 rec['remote_after']=remote(git(remote_args));req(rec['remote_after']==want_after,'activation remote mismatch')
 inventory('after activation');resource('after activation')
 rec.update(status='PUBLISHED_EXACT_WINDOWS05_REQUEST_ONLY_ACTIVATION',clone_is_build_workspace=False,slot='WINDOWS05_RESERVED_PENDING_ACTUAL_RESULT_AND_INDEPENDENT_SETTLEMENT')
except BaseException as error:
 rec['error']=type(error).__name__+': '+str(error)[:3000]
finally:
 for fd,label in ((rootfd,'root'),(lockfd,'lock'),(parentfd,'coordination_parent')):
  if fd is not None:
   try:
    if label=='lock':fcntl.flock(fd,fcntl.LOCK_UN)
    os.close(fd)
   except OSError as error:rec[label+'_close_error']=repr(error);rec['status']='FAIL_CLEANUP_HOLD'
 rec['elapsed_seconds']=time.monotonic()-START
 rec['cleanup']='Normal foreground owned Git children reaped; original publication clone retained. No local build/cache/service or wrapper-stop duty. Actual Windows job cleanup remains independently reconciled before slot release; no old store/HOLD changes, no abnormal descendant claim or retry.'
 durable()
 if recfd is not None:os.close(recfd)
 print(json.dumps({'status':rec['status'],'error':rec.get('error'),'commit':rec.get('commit'),'tree':rec.get('tree'),'commands':len(rec['commands']),'elapsed_seconds':rec['elapsed_seconds']}))
 if rec['status']!='PUBLISHED_EXACT_WINDOWS05_REQUEST_ONLY_ACTIVATION':raise SystemExit(1)
