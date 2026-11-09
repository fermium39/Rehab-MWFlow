// FitFlow offline shell and persistent image cache.
const CACHE='rehab-mw-flow-fitflow-2026.11.09.01';
const IMAGE_CACHE='fitflow-image-assets-v1';
const OFFLINE='./index.html';
const MAX_IMAGES=180;

self.addEventListener('install',event=>{
 event.waitUntil(caches.open(CACHE).then(()=>self.skipWaiting()));
});
self.addEventListener('activate',event=>{
 event.waitUntil((async()=>{
  const keys=await caches.keys();
  // Preserve image cache between app releases.
  await Promise.all(keys.filter(key=>key.startsWith('rehab-mw-flow-')&&key!==CACHE).map(key=>caches.delete(key)));
  await self.clients.claim();
 })());
});
self.addEventListener('message',event=>{
 if(event.data?.type==='SKIP_WAITING')self.skipWaiting();
});
async function cacheImage(request,response){
 if(!response.ok)return;
 const cache=await caches.open(IMAGE_CACHE);
 await cache.put(request,response.clone());
 const keys=await cache.keys();
 if(keys.length>MAX_IMAGES)await Promise.all(keys.slice(0,keys.length-MAX_IMAGES).map(key=>cache.delete(key)));
}
self.addEventListener('fetch',event=>{
 const request=event.request;
 if(request.method!=='GET')return;
 const url=new URL(request.url);
 if(url.origin!==self.location.origin)return;
 if(request.mode==='navigate'||url.pathname.endsWith('/index.html')){
  event.respondWith((async()=>{
   try{
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
 // Serve previously viewed images immediately. Refresh in the background
 // so asset updates can arrive without delaying display.
 if(url.pathname.toLowerCase().match(/\.(png|jpe?g|webp|gif|svg|avif)$/)){
  event.respondWith((async()=>{
   const cache=await caches.open(IMAGE_CACHE);
   const cached=await cache.match(request);
   if(cached){
    event.waitUntil(fetch(request).then(response=>cacheImage(request,response)).catch(()=>{}));
    return cached;
   }
   try{
    const response=await fetch(request);
    if(response.ok)event.waitUntil(cacheImage(request,response));
    return response;
   }catch(err){
    // Allow images from an earlier app-shell cache to survive migration.
    const old=await caches.match(request);
    if(old)return old;
    throw err;
   }
  })());
  return;
 }
 // Other assets stay network-first for prompt updates.
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
