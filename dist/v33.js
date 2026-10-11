(function(){
const dayIndex=n=>Math.floor(new Date().setHours(0,0,0,0)/86400000)%n;
let hadisler=[];
const hadisPromise=fetch('/riyazus.json').then(response=>response.ok?response.json():[]).then(items=>hadisler=Array.isArray(items)?items:[]).catch(()=>[]);
function showDailyHadith(){
 const panel=document.getElementById('daily-content');
 if(!panel)return;
 const paint=()=>{
  const full=hadisler.length?hadisler:(window.bigHadiths||[]),dailyPool=full.filter(item=>item.text?.length>=80&&item.text.length<=700),source=dailyPool.length?dailyPool:full,hadith=source[dayIndex(source.length)];
  if(!hadith)return;
  panel.replaceChildren();
  const label=document.createElement('b'),topic=document.createElement('h3'),text=document.createElement('p'),reference=document.createElement('p');
  label.textContent='Günün hadisi';topic.textContent=hadith.topic||hadith.category||'Hadis';
  text.className='daily-hadith-text';text.textContent=hadith.text;
  reference.className='daily-hadith-source';reference.textContent=`Riyâzü’s-Sâlihîn · Cilt ${hadith.volume||1} · Hadis ${hadith.number||''} · ${hadith.source||''}`;
  panel.append(label,topic,text,reference);
 };
 hadisler.length?paint():hadisPromise.then(paint);
}
function install(){
 const button=document.querySelector('[data-day="hadis"]');
 if(!button||button.dataset.riyazDaily==='1')return false;
 button.dataset.riyazDaily='1';button.addEventListener('click',()=>setTimeout(showDailyHadith,0));return true;
}
let attempts=0;const timer=setInterval(()=>{attempts++;if(install()||attempts>120)clearInterval(timer)},100);
})();

