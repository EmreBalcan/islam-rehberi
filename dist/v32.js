(function(){
const mealNames={'tr.yazir':'Elmalılı Hamdi Yazır','tr.ates':'Süleyman Ateş','tr.bulac':'Ali Bulaç','tr.yildirim':'Suat Yıldırım'};
const native=()=>window.Capacitor?.Plugins?.IslamNative;
function selectedEdition(){return localStorage.getItem('quran-meal-edition')||localStorage.getItem('widgetMealEdition')||state?.corpus?.edition?.turkish||'tr.yazir'}
function hourlyAyah(){
 if(!state?.corpus?.surahs?.length)return null;
 const hour=Math.floor(Date.now()/3600000),global=Number((BigInt(hour)*2654435761n)%6236n)+1;
 for(const surah of state.corpus.surahs){const ayah=surah.ayahs.find(item=>item.global===global);if(ayah)return{hour,global,surah,ayah}}
 return null;
}
async function paintHourly(edition){
 const current=hourlyAyah();if(!current)return;
 const {hour,global,surah,ayah}=current,cacheKey=`hourly-meal-${edition}-${global}`;
 let translation=localStorage.getItem(cacheKey);
 if(!translation){
  try{const response=await fetch(`https://api.alquran.cloud/v1/ayah/${global}/${edition}`);if(!response.ok)throw Error();translation=(await response.json())?.data?.text;if(translation)localStorage.setItem(cacheKey,translation)}catch(_){translation=ayah.tr}
 }
 const reference=`${surah.name} Suresi, ${ayah.number}. ayet`;
 const ref=document.getElementById('hourly-ref'),arabic=document.getElementById('hourly-arabic'),turkish=document.getElementById('hourly-tr');
 if(ref)ref.textContent=reference;if(arabic)arabic.textContent=ayah.ar;if(turkish)turkish.textContent=translation||ayah.tr;
 native()?.updateHourlyVerse({hourKey:String(hour),globalNumber:global,reference,arabic:ayah.ar,turkish:translation||ayah.tr}).catch(()=>{});
}
function syncNative(edition){
 const showArabic=document.getElementById('widget-arabic')?.checked!==false,showTafsir=document.getElementById('widget-tafsir')?.checked===true;
 native()?.setWidgetPreferences({showArabic,showTafsir,mealEdition:edition}).catch(()=>{});
}
function applySingleEdition(edition,source){
 if(!mealNames[edition])edition='tr.yazir';
 localStorage.setItem('quran-meal-edition',edition);localStorage.setItem('widgetMealEdition',edition);
 const app=document.getElementById('meal-edition'),widget=document.getElementById('widget-meal');
 if(app&&app.value!==edition)app.value=edition;if(widget&&widget.value!==edition)widget.value=edition;
 syncNative(edition);paintHourly(edition);
 if(source==='widget'&&typeof changeMeal==='function'&&state?.corpus?.edition?.turkish!==edition)changeMeal(edition);
}
function install(){
 const app=document.getElementById('meal-edition'),widget=document.getElementById('widget-meal');
 if(!app||!widget||!state?.corpus?.surahs?.length)return false;
 const edition=selectedEdition();applySingleEdition(edition,'startup');
 if(state.corpus.edition?.turkish!==edition&&typeof changeMeal==='function')changeMeal(edition);
 if(app.dataset.unifiedMeal!=='1'){app.dataset.unifiedMeal='1';app.addEventListener('change',()=>applySingleEdition(app.value,'app'))}
 if(widget.dataset.unifiedMeal!=='1'){widget.dataset.unifiedMeal='1';widget.addEventListener('change',()=>applySingleEdition(widget.value,'widget'))}
 return true;
}
let attempts=0;const timer=setInterval(()=>{attempts++;if(install()||attempts>240)clearInterval(timer)},250);
document.addEventListener('visibilitychange',()=>{if(!document.hidden){install();paintHourly(selectedEdition())}});
window.addEventListener('pageshow',()=>setTimeout(()=>{install();paintHourly(selectedEdition())},0));
setInterval(()=>paintHourly(selectedEdition()),60000);
})();

