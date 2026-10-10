(function(){
const player=document.getElementById('quran-audio'),verses=document.getElementById('verses'),native=()=>window.Capacitor?.Plugins?.IslamNative;
const mealNames={'tr.yazir':'Elmalılı Hamdi Yazır','tr.ates':'Süleyman Ateş','tr.bulac':'Ali Bulaç','tr.yildirim':'Suat Yıldırım'};
function installWidgetMeal(){const box=document.querySelector('.widget-settings');if(!box||document.getElementById('widget-meal'))return false;const label=document.createElement('label');label.className='widget-meal-picker';label.innerHTML='<span>Widget meali</span><select id="widget-meal" aria-label="Widget meal yazarı"></select>';const select=label.querySelector('select');for(const [id,name] of Object.entries(mealNames))select.add(new Option(name,id));select.value=localStorage.getItem('widgetMealEdition')||'tr.yazir';const note=box.querySelector('small');box.insertBefore(label,note);const sync=()=>{const ar=document.getElementById('widget-arabic')?.checked!==false,tf=document.getElementById('widget-tafsir')?.checked===true,meal=select.value;localStorage.setItem('widgetArabic',ar?'1':'0');localStorage.setItem('widgetTafsir',tf?'1':'0');localStorage.setItem('widgetMealEdition',meal);native()?.setWidgetPreferences({showArabic:ar,showTafsir:tf,mealEdition:meal}).catch(()=>{})};select.onchange=sync;document.getElementById('widget-arabic').onchange=sync;document.getElementById('widget-tafsir').onchange=sync;sync();return true}
installWidgetMeal()||setTimeout(installWidgetMeal,500);
function volumeIcon(value){return value===0?'🔇':value<.55?'🔉':'🔊'}
function syncVolumeControls(){if(!player)return;document.querySelectorAll('.mini-volume').forEach(wrap=>{const input=wrap.querySelector('input'),button=wrap.querySelector('.mini-tool'),icon=volumeIcon(player.volume);if(input&&Number(input.value)!==player.volume)input.value=String(player.volume);if(button&&button.textContent!==icon)button.textContent=icon})}
player?.addEventListener('volumechange',syncVolumeControls);
new MutationObserver(syncVolumeControls).observe(verses,{childList:true,subtree:true});
verses?.addEventListener('input',event=>{if(event.target.closest('.mini-volume'))requestAnimationFrame(syncVolumeControls)});
syncVolumeControls();
})();
