const CACHE='islam-rehberi-v37';const FILES=['/','/index.html','/style-v32.css','/app.js','/expanded.js','/fixes.js','/v11.js','/v12.js','/v13.js','/v14.js','/v16.js','/v21.js','/v22.js','/v24.js','/v25.js','/v26.js','/v27.js','/v28.js','/v30.js','/v31.js','/v32.js','/v33.js','/v34.js','/menu-quran.jpg','/menu-qibla.jpg','/menu-hadith.jpg','/app-cover.png','/riyazus.json','/tefsir.json','/icon.svg','/manifest.webmanifest'];
self.addEventListener('install',event=>event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(FILES)).then(()=>self.skipWaiting())));
self.addEventListener('activate',event=>event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',event=>{const url=new URL(event.request.url);if(url.origin!==self.location.origin)return;if(event.request.mode==='navigate'){event.respondWith(fetch(event.request).then(response=>{const copy=response.clone();caches.open(CACHE).then(cache=>cache.put('/index.html',copy));return response;}).catch(()=>caches.match('/index.html')));return;}event.respondWith(caches.match(event.request).then(hit=>hit||fetch(event.request)));});







