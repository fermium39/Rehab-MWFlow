const CACHE='rehab-mw-flow-fitflow-2026.11.04.33';
const OFFLINE='./index.html';
self.addEventListener('install',event=>{
 event.waitUntil(caches.open(CACHE).then(()=>self.skipWaiting()));
});
self.addEventListener('activate',event=>{
 event.waitUntil((async()=>{
  const keys=await caches.keys();
  await Promise.all(keys.filter(key=>key.startsWith('rehab-mw-flow-')&&key!==CACHE).map(key=>caches.delete(key)));
  await self.clients.claim();
 })());
});
self.addEventListener('message',event=>{
 if(event.data?.type==='SKIP_WAITING')self.skipWaiting();
});
self.addEventListener('fetch',event=>{
 const request=event.request;
 if(request.method!=='GET')return;
 const url=new URL(request.url);
 if(url.origin!==self.location.origin)return;
 if(request.mode==='navigate'||url.pathname.endsWith('/index.html')){
  event.respondWith((async()=>{
   try{
    // Always ask the server for HTML; cache is an offline fallback only.
    const response=await fetch(request,{cache:'no-store'});
    if(response.ok){
     const copy=response.clone();
     event.waitUntil(caches.open(CACHE).then(cache=>cache.put(OFFLINE,copy)));
    }
    return response;
   }catch(err){
    const cached=await caches.match(OFFLINE);
    if(cached)return cached;
    throw err;
   }
  })());
  return;
 }
 // Assets are network-first, then offline fallback, so new images are not stuck.
 event.respondWith((async()=>{
  try{
   const response=await fetch(request);
   if(response.ok){
    const copy=response.clone();
    event.waitUntil(caches.open(CACHE).then(cache=>cache.put(request,copy)));
   }
   return response;
  }catch(err){
   const cached=await caches.match(request);
   if(cached)return cached;
   throw err;
  }
 })());
});
