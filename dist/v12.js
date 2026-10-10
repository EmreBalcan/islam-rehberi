(function(){
// APK/QR alanını ana sayfadan çekmeceye taşı.
const download=document.querySelector('.download-panel');
const drawer=document.querySelector('.app-drawer');
if(drawer){const head=drawer.querySelector('.drawer-head strong');if(head){const img=document.createElement('img');img.src='/app-cover.png';img.alt='İslam Rehberi';img.className='app-cover-mini';head.prepend(img);}}
if(download&&drawer){download.classList.add('drawer-download');drawer.append(download);}

// Saatlik ayeti küçük, açılır kapanır bir kart yap.
const hourly=document.querySelector('.hourly-verse');
if(hourly){
 hourly.classList.add('collapsed');
 const head=hourly.firstElementChild;
 const toggle=document.createElement('button');toggle.className='hourly-toggle';toggle.type='button';toggle.setAttribute('aria-expanded','false');toggle.textContent='Aç';head.append(toggle);
 [...hourly.children].slice(1).forEach(x=>x.classList.add('hourly-body'));
 toggle.onclick=()=>{const open=hourly.classList.toggle('open');hourly.classList.toggle('collapsed',!open);toggle.textContent=open?'Kapat':'Aç';toggle.setAttribute('aria-expanded',String(open));};
}

// Uygulama ve widget aynı saat anahtarını ve aynı ayeti kullanır.
let lastWidgetHour='';
function syncHourlyWidget(){
 if(!state.corpus?.surahs?.length)return;
 const hour=Math.floor(Date.now()/3600000),key=String(hour),global=Number((BigInt(hour)*2654435761n)%6236n)+1;
 let ayah,surah;for(const s of state.corpus.surahs){ayah=s.ayahs.find(a=>a.global===global);if(ayah){surah=s;break}}
 if(!ayah||!surah)return;
 const ref=`${surah.name} Suresi, ${ayah.number}. ayet`;
 const refEl=document.getElementById('hourly-ref'),arEl=document.getElementById('hourly-arabic'),trEl=document.getElementById('hourly-tr');
 if(refEl)refEl.textContent=ref;if(arEl)arEl.textContent=ayah.ar;if(trEl)trEl.textContent=ayah.tr;
 if(lastWidgetHour!==key)lastWidgetHour=key;
}
setTimeout(syncHourlyWidget,900);setInterval(syncHourlyWidget,15000);document.addEventListener('visibilitychange',()=>{if(!document.hidden)syncHourlyWidget()});

// Doğrulanmış ses sürümleri. Yâsir ed-Dûserî tam sure kaynağından çalınır.
const picker=document.getElementById('arabic-reciter');
if(picker){
 const abdul=picker.querySelector('option[value="ar.abdulbasit"]')||picker.querySelector('option[value="ar.abdulbasitmurattal"]');
 if(abdul){abdul.value='ar.abdulbasitmurattal';abdul.textContent='Abdülbâsıt Abdüssamed';}
 if(!picker.querySelector('option[value="ar.abdurrahmaansudais"]'))picker.add(new Option('Abdurrahman es-Sudeys','ar.abdurrahmaansudais'));
 if(!picker.querySelector('option[value="yasser-surah"]'))picker.add(new Option('Yâsir ed-Dûserî','yasser-surah'));
}
const audioConfig={
 'ar.alafasy':{kind:'ayah',bitrate:128},'ar.abdulbasitmurattal':{kind:'ayah',bitrate:192},
 'ar.husary':{kind:'ayah',bitrate:128},'ar.minshawi':{kind:'ayah',bitrate:128},
 'ar.abdurrahmaansudais':{kind:'ayah',bitrate:192},'yasser-surah':{kind:'surah'}
};
const pad=n=>String(n).padStart(3,'0');
function surahAudio(s){return `https://download.quranicaudio.com/quran/yasser_ad-dussary/${pad(s.number)}.mp3`;}
function playChosen(){const s=selectedSurah();if(!s)return;const id=picker?.value||'ar.alafasy',cfg=audioConfig[id]||audioConfig['ar.alafasy'],p=$('quran-audio');p.hidden=false;if(cfg.kind==='surah'){p.onended=null;p.onerror=null;p.src=surahAudio(s);p.load();p.play().catch(()=>setStatus('Yâsir ed-Dûserî kaydı başlatılamadı.'));setStatus(`${s.name} suresi Yâsir ed-Dûserî’den oynatılıyor.`);return;}let i=0;const next=()=>{if(i>=s.ayahs.length){setStatus(`${s.name} suresinin Arapça okuması tamamlandı.`);return;}p.src=`https://cdn.islamic.network/quran/audio/${cfg.bitrate}/${id}/${s.ayahs[i++].global}.mp3`;p.load();p.play().catch(()=>setStatus('Ses başlatılamadı. Bağlantıyı denetleyin.'));};p.onended=next;p.onerror=()=>setTimeout(next,250);next();setStatus(`${s.name} suresi kesintisiz oynatılıyor.`);}
const playAr=$('play-arabic');if(playAr)playAr.onclick=playChosen;
const downAr=$('download-arabic');if(downAr)downAr.onclick=async()=>{const s=selectedSurah();if(!s)return;const id=picker?.value||'ar.alafasy',cfg=audioConfig[id]||audioConfig['ar.alafasy'];downAr.disabled=true;try{const cache=await caches.open(`arabic-audio-${id}`);if(cfg.kind==='surah'){await cache.add(surahAudio(s));downAr.textContent='Kaydedildi';}else for(let i=0;i<s.ayahs.length;i++){const u=`https://cdn.islamic.network/quran/audio/${cfg.bitrate}/${id}/${s.ayahs[i].global}.mp3`;if(!await cache.match(u))await cache.add(u);downAr.textContent=`İndiriliyor ${i+1}/${s.ayahs.length}`;}setStatus(`${s.name} suresinin seçili kârî kaydı çevrimdışı kullanım için kaydedildi.`);}catch(_){setStatus('Ses indirilemedi. İnternet bağlantısını denetleyin.');}finally{downAr.disabled=false;setTimeout(()=>downAr.textContent='↓ Arapça indir',800);}};

// Pusulayı tek sensör akışıyla, ekran dönüşünü hesaba katarak çalıştır.
let compassOn=false,smoothedHeading=null;
function screenAngle(){return screen.orientation?.angle||window.orientation||0;}
function fixedOrientation(e){let h;if(typeof e.webkitCompassHeading==='number'&&e.webkitCompassAccuracy!==-1)h=e.webkitCompassHeading;else if(typeof e.alpha==='number'&&e.absolute)h=(360-e.alpha+screenAngle()+360)%360;if(typeof h!=='number'||Number.isNaN(h))return;if(smoothedHeading==null)smoothedHeading=h;else{const delta=((h-smoothedHeading+540)%360)-180;smoothedHeading=(smoothedHeading+delta*.22+360)%360;}appState.heading=smoothedHeading;updateQiblaView();const dial=document.querySelector('.compass-dial');if(dial)dial.style.transform=`rotate(${-smoothedHeading}deg)`;}
function stopCompass(){window.removeEventListener('deviceorientationabsolute',fixedOrientation,true);window.removeEventListener('deviceorientation',fixedOrientation,true);window.removeEventListener('deviceorientationabsolute',orientationHandler,true);window.removeEventListener('deviceorientation',orientationHandler,true);compassOn=false;smoothedHeading=null;appState.heading=null;const b=$('start-compass');b.textContent='Pusulayı başlat';b.classList.remove('danger-button');$('qibla-instruction').textContent='Pusula kapalı. Başlatmak için düğmeye dokunun.';const dial=document.querySelector('.compass-dial');if(dial)dial.style.transform='rotate(0deg)';updateQiblaView();}
async function toggleCompass(){if(compassOn){stopCompass();return;}try{if(typeof DeviceOrientationEvent!=='undefined'&&typeof DeviceOrientationEvent.requestPermission==='function'){const result=await DeviceOrientationEvent.requestPermission();if(result!=='granted')throw new Error('Pusula izni verilmedi');}window.removeEventListener('deviceorientationabsolute',orientationHandler,true);window.removeEventListener('deviceorientation',orientationHandler,true);window.addEventListener('deviceorientationabsolute',fixedOrientation,true);window.addEventListener('deviceorientation',fixedOrientation,true);compassOn=true;const b=$('start-compass');b.textContent='Pusulayı kapat';b.classList.add('danger-button');$('qibla-instruction').textContent='Telefonu düz tutup yavaşça döndürün.';}catch(e){$('qibla-instruction').textContent=e.message||'Pusula başlatılamadı.';}}
const oldButton=$('start-compass');if(oldButton){const fresh=oldButton.cloneNode(true);oldButton.replaceWith(fresh);fresh.onclick=toggleCompass;}
const compass=$('compass');if(compass&&!compass.querySelector('.compass-dial')){const dial=document.createElement('div');dial.className='compass-dial';dial.innerHTML='<span class="n">K</span><span class="e">D</span><span class="s">G</span><span class="w">B</span>';compass.prepend(dial);compass.querySelector('.compass-north')?.remove();}
})();
