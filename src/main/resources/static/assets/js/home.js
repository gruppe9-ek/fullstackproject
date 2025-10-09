// /js/home.js
const API = {
    moviesCurrent: "/api/movies",
    showsFor: (id) => `/shows.html?movieId=${id}`,
    movieInfo: (id) => `/movies/${id}`
};

const qs = (s, el=document)=> el.querySelector(s);
const qsa = (s, el=document)=> [...el.querySelectorAll(s)];

async function fetchMovies(){
    const res = await fetch(API.moviesCurrent, { headers:{Accept:'application/json'} });
    if(!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
}

function renderHero(){
    const hero = qs('#hero');
    const heroBg = qs('#heroBg');
    const title = qs('#heroTitle');
    const sub = qs('#heroSub');

    hero.classList.add('hero--placeholder');
    if (heroBg) heroBg.style.backgroundImage = ''; // lad CSS vise /assets/hero-default.jpg
    title.textContent = 'Velkommen i biografen';
    sub.textContent = 'Se programmet og køb billetter';
}

function cardTemplate(m){
    const age = (m.ageLimit != null) ? `${m.ageLimit} år` : 'Tilladt for alle';
    const showsHref = API.showsFor(m.movieId);
    const infoHref = API.movieInfo(m.movieId);
    const poster = m.posterUrl
        ? `<img src="${m.posterUrl}" alt="${escapeHtml(m.title || 'Plakat')}" loading="lazy">`
        : '';
    return `
  <article class="movie-card" data-title="${escapeAttr(m.title || '')}">
    <div class="movie-card__poster">${poster}</div>
    <div class="movie-card__body">
      <h3 class="movie-card__title">${escapeHtml(m.title || 'Filmtitel')}</h3>
      <p class="movie-card__meta">
        <span>${escapeHtml(m.category || 'Kategori')}</span>
        <span class="dot">•</span>
        <span>${escapeHtml(age)}</span>
      </p>
      <div class="movie-card__actions">
        <a class="btn btn-primary btn-sm" href="${showsHref}">Find billetter</a>
        <a class="btn btn-ghost btn-sm" href="${infoHref}">Mere info</a>
      </div>
    </div>
  </article>`;
}

function renderMovies(movies){
    const grid = qs('#movieGrid'); const empty = qs('#emptyState');
    grid.innerHTML = movies.map(cardTemplate).join('');
    empty.hidden = movies.length > 0;
}

function setupSearch(){
    const input = qs('#searchInput'); if(!input) return;
    input.addEventListener('input', ()=>{
        const val = input.value.trim().toLowerCase();
        let any = false;
        for(const c of qsa('.movie-card')){
            const t = (c.dataset.title || '').toLowerCase();
            const show = !val || t.includes(val);
            c.style.display = show ? '' : 'none';
            any = any || show;
        }
        qs('#emptyState').hidden = any;
    });
}

function escapeHtml(s){ return String(s).replace(/[&<>\"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;"}[c])) }
function escapeAttr(s){ return escapeHtml(s).replace(/\"/g,'&quot;'); }

(function init(){
    const yearEl = qs('#year'); if(yearEl) yearEl.textContent = new Date().getFullYear();
    renderHero(); // <-- vis velkomst-hero uanset film
    fetchMovies()
        .then(ms => { renderMovies(ms); setupSearch(); })
        .catch(()=> { const e = qs('#errorBox'); if(e) e.hidden = false; });
})();
