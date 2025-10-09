// Fetch movies and render simple grid with CTA to booking.html
const API = '/api/movies'; // we only need basic fields; /summaries not required
const grid = document.getElementById('grid');
const empty = document.getElementById('empty');
const retryEmpty = document.getElementById('retryEmpty');
const toast = document.getElementById('toast');

function showToast(msg){
    toast.textContent = msg; toast.style.display='block';
    setTimeout(()=> toast.style.display='none', 3500);
}

const FALLBACK_SVG = 'data:image/svg+xml;utf8,' + encodeURIComponent(`
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 300">
    <defs>
      <linearGradient id="g" x1="0" x2="0" y1="0" y2="1">
        <stop offset="0" stop-color="#e9e9e9"/><stop offset="1" stop-color="#f6f6f6"/>
      </linearGradient>
    </defs>
    <rect width="100%" height="100%" fill="url(#g)"/>
    <g fill="#b3b3b3">
      <rect x="30" y="40" width="140" height="20" rx="4"/>
      <rect x="30" y="70" width="100" height="14" rx="3"/>
      <circle cx="100" cy="180" r="38"/>
    </g>
  </svg>`);

function escapeHtml(s){
    return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c]));
}

function cardTpl(m){
    const poster = (m.posterUrl && m.posterUrl.trim()) ? m.posterUrl : FALLBACK_SVG;
    // CTA goes to booking.html with movieId in querystring (so booking can preselect movie later)
    const href = `/booking.html?movieId=${encodeURIComponent(m.movieId)}`;
    return `
    <article class="card">
      <img class="poster" src="${poster}" alt="${escapeHtml(m.title || 'Filmplakat')}"
           onerror="this.onerror=null;this.src='${FALLBACK_SVG}'"/>
      <div class="card-body">
        <h3 class="title">${escapeHtml(m.title)}</h3>
        <div class="meta">
          <span>${escapeHtml(m.category)}</span>
          <span>• ${m.durationMinutes} min</span>
          <span>• ${m.ageLimit}+</span>
        </div>
        <div class="actions">
          <a class="btn primary" href="${href}">Køb billet</a>
        </div>
      </div>
    </article>`;
}

async function load(){
    grid.setAttribute('aria-busy','true');
    try{
        const res = await fetch(API);
        if(!res.ok) throw new Error('HTTP '+res.status);
        const list = await res.json();

        grid.innerHTML = '';

        if(!list.length){
            empty.hidden = false;
            grid.classList.add('hidden');
            return;
        }

        empty.hidden = true;
        grid.classList.remove('hidden');

        // sort by title for a nicer UX
        list.sort((a,b)=>(a.title||'').localeCompare(b.title||''));
        list.forEach(m => grid.insertAdjacentHTML('beforeend', cardTpl(m)));
    }catch(e){
        showToast('Kunne ikke hente film. Prøv igen.');
        if(!grid.children.length){
            empty.hidden = false;
            grid.classList.add('hidden');
        }
    }finally{
        grid.removeAttribute('aria-busy');
    }
}

retryEmpty?.addEventListener('click', load);
load();
