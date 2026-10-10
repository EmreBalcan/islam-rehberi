(function(){
const area=document.getElementById('verses');if(!area)return;
const basmala='بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ',meaning='Rahmân ve Rahîm olan Allah’ın adıyla.';
function apply(){const s=state.corpus?.surahs?.[state.selected-1];if(!s)return;const existing=area.querySelector('.basmala-card');if(s.number===1||s.number===9){existing?.remove();return}const first=area.querySelector('.verse'),ar=first?.querySelector('.arabic');if(!first||!ar)return;if(!existing){const card=document.createElement('section');card.className='basmala-card';card.dataset.surah=String(s.number);card.innerHTML=`<span>Besmele</span><p class="arabic">${basmala}</p><p class="translation">${meaning}</p>`;first.before(card)}const forms=['بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ','بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيمِ','بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ'];let text=ar.textContent.trim();for(const form of forms)if(text.startsWith(form)){text=text.slice(form.length).trim();break}ar.textContent=text}
let queued=false;new MutationObserver(()=>{if(queued)return;queued=true;queueMicrotask(()=>{queued=false;apply()})}).observe(area,{childList:true});setTimeout(apply,0);
})();
