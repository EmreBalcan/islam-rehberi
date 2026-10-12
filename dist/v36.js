(function(){
const player=document.getElementById('quran-audio');if(!player)return;
function savedRate(){const n=Number(localStorage.getItem('quranPlaybackRate'));return [0.75,1,1.25,1.5,2].includes(n)?n:1}
function enforceRate(){const rate=savedRate();player.defaultPlaybackRate=rate;if(player.playbackRate!==rate)player.playbackRate=rate}
['loadstart','loadedmetadata','canplay','play','playing'].forEach(type=>player.addEventListener(type,enforceRate));
new MutationObserver(enforceRate).observe(player,{attributes:true,attributeFilter:['src']});
enforceRate();
})();
