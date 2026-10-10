(function(){
const $id=id=>document.getElementById(id),verses=$id('verses');
function paintSaved(){const p=state.position;verses?.querySelectorAll('[data-save]').forEach(button=>{const selected=!!p&&button.dataset.save===`${p.surah}:${p.ayah}`,mark=selected?'✓':'⌑';button.classList.toggle('is-saved',selected);button.setAttribute('aria-pressed',String(selected));if(button.textContent!==mark)button.textContent=mark;button.title=selected?'Kaldığınız yer olarak işaretlendi':'Burayı kaldığım yer olarak kaydet'})}
new MutationObserver(paintSaved).observe(verses,{childList:true,subtree:true});
verses?.addEventListener('click',e=>{if(e.target.closest('[data-save]'))setTimeout(paintSaved,0)});
paintSaved();
const resume=$id('resume');if(resume)resume.onclick=()=>{const p=state.position;if(!p)return;window.selectSurah?.(p.surah,p.ayah);requestAnimationFrame(()=>requestAnimationFrame(()=>window.playFullSurahFromAyah?.(p.ayah)))};
})();
