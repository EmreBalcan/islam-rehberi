(function(){
const selectId='v19-surah-select';
function populateSurahSelect(){
 const select=document.getElementById(selectId),surahs=state?.corpus?.surahs;
 if(!select||!Array.isArray(surahs)||surahs.length!==114)return false;
 const selected=Number(select.value||state.selected||0);
 if(select.options.length!==115){
  const fragment=document.createDocumentFragment();
  fragment.append(new Option('Bir sure seçin',''));
  for(const surah of surahs)fragment.append(new Option(`${surah.number}. ${surah.name} Suresi · ${surah.count} ayet`,String(surah.number)));
  select.replaceChildren(fragment);
 }
 select.value=selected?String(selected):'';
 return true;
}
function install(){
 const select=document.getElementById(selectId);
 if(!select)return false;
 populateSurahSelect();
 if(select.dataset.v31!=='1'){
  select.dataset.v31='1';
  select.addEventListener('focus',populateSurahSelect);
  select.addEventListener('pointerdown',populateSurahSelect);
  select.addEventListener('change',()=>{
   const number=Number(select.value);
   if(!number)return;
   if(typeof window.selectSurah==='function')window.selectSurah(number);
   else{state.selected=number;render();}
   select.value=String(number);
   document.querySelector('.quran-setup')?.classList.add('open');
  });
 }
 return true;
}
let attempts=0;
const timer=setInterval(()=>{
 attempts++;
 if((install()&&populateSurahSelect())||attempts>240)clearInterval(timer);
},250);
document.addEventListener('click',event=>{
 if(event.target.closest('[data-page-target="quran"],.quran-setup-head'))setTimeout(()=>{install();populateSurahSelect();},0);
});
window.addEventListener('pageshow',()=>setTimeout(()=>{install();populateSurahSelect();},0));
})();
