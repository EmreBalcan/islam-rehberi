(function(){
const dayIndex=n=>Math.floor(new Date().setHours(0,0,0,0)/86400000)%n;
function showDailyPrayer(){
 const panel=document.getElementById('daily-content'),items=window.bigDuas||[];if(!panel||!items.length)return;
 const prayer=items[dayIndex(items.length)],ayah=prayer.surah?state?.corpus?.surahs?.[prayer.surah-1]?.ayahs?.[prayer.ayah-1]:null;
 const translation=ayah?.tr||prayer.text;
 if(!translation){setTimeout(showDailyPrayer,250);return}
 panel.replaceChildren();
 const label=document.createElement('b');label.textContent='Günün duası';panel.append(label);
 const arabic=prayer.arabic||ayah?.ar;if(arabic){const ar=document.createElement('div');ar.className='daily-arabic';ar.dir='rtl';ar.textContent=arabic;panel.append(ar)}
 const text=document.createElement('h3');text.className='daily-prayer-text';text.textContent=translation;panel.append(text);
 const source=document.createElement('p');source.textContent=prayer.source;panel.append(source);
}
function install(){const button=document.querySelector('[data-day="dua"]');if(!button||button.dataset.fullPrayer==='1')return false;button.dataset.fullPrayer='1';button.addEventListener('click',()=>setTimeout(showDailyPrayer,0));return true}
let attempts=0;const timer=setInterval(()=>{attempts++;if(install()||attempts>120)clearInterval(timer)},100);
})();

