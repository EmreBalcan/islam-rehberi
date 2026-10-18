(function(){
const dayIndex=n=>Math.floor(new Date().setHours(0,0,0,0)/86400000)%n;
let hadisler=[];
const hadisPromise=fetch('/riyazus.json?v=40').then(response=>response.ok?response.json():[]).then(items=>hadisler=Array.isArray(items)?items:[]).catch(()=>[]);
function showDailyHadith(){
 const panel=document.getElementById('daily-content');
 if(!panel)return;
 const paint=()=>{
  const source=hadisler.length?hadisler:(window.bigHadiths||[]),hadith=source[dayIndex(source.length)];
  if(!hadith)return;
  panel.replaceChildren();
  const label=document.createElement('b'),topic=document.createElement('h3'),text=document.createElement('p'),reference=document.createElement('p');
  label.textContent='Günün hadisi';topic.textContent=hadith.topic||hadith.category||'Hadis';
  text.className='daily-hadith-text';text.textContent=hadith.text;
  reference.className='daily-hadith-source';reference.textContent=`Riyâzü’s-Sâlihîn · Cilt ${hadith.volume||1} · Hadis ${hadith.number||''} · ${hadith.source||''}`;
  panel.append(label,topic,text);
  if(hadith.text.length>420){
   text.classList.add('is-collapsed');
   const more=document.createElement('button');more.type='button';more.className='daily-hadith-more';more.textContent='Devamını oku';
   more.addEventListener('click',()=>{const open=text.classList.toggle('is-expanded');text.classList.toggle('is-collapsed',!open);more.textContent=open?'Daralt':'Devamını oku'});
   panel.append(more);
  }
  panel.append(reference);
 };
 hadisler.length?paint():hadisPromise.then(paint);
}
function install(){
 const button=document.querySelector('[data-day="hadis"]');
 if(!button||button.dataset.riyazDaily==='1')return false;
 button.dataset.riyazDaily='1';button.addEventListener('click',()=>setTimeout(showDailyHadith,0));return true;
}
let attempts=0;const timer=setInterval(()=>{attempts++;if(install()||attempts>120)clearInterval(timer)},100);
const style=document.createElement('style');style.textContent='.daily-hadith-text{white-space:pre-line;line-height:1.65}.daily-hadith-text.is-collapsed{display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:6;overflow:hidden}.daily-hadith-more{margin:.35rem 0 .7rem;border:1px solid currentColor;border-radius:999px;background:transparent;color:inherit;padding:.55rem .9rem;font:inherit;font-weight:700}.daily-hadith-source{opacity:.78;font-size:.92em}';document.head.append(style);
})();

