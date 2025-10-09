const API_BASE = '/api';
const $ = (sel) => document.querySelector(sel);

const moviesDiv = $('#movies');
const form = $('#movieForm');
const toastEl = $('#toast');
const posterInput = $('#posterUrl');
const posterImg = $('#posterPreview');
const noPoster = $('#noPoster');

function showToast(msg){
    toastEl.textContent = msg;
    toastEl.style.display = 'block';
    setTimeout(()=> toastEl.style.display = 'none', 3500);
}

async function api(path, options = {}){
    const res = await fetch(API_BASE + path, {
        headers: { 'Content-Type': 'application/json' }, ...options
    });
    const ct = res.headers.get('content-type') || '';
    const body = ct.includes('application/json')
        ? await res.json().catch(()=> ({}))
        : await res.text();
    if(!res.ok){
        const message = typeof body === 'string' ? body : (body.message || JSON.stringify(body));
        const err = new Error(message || `HTTP ${res.status}`); err.status = res.status; throw err;
    }
    return body;
}

function formToMovie(){
    return {
        movieId: $('#movieId').value ? Number($('#movieId').value) : null,
        title: $('#title').value.trim(),
        posterUrl: $('#posterUrl').value.trim() || null,
        category: $('#category').value.trim(),
        ageLimit: $('#ageLimit').value ? Number($('#ageLimit').value) : null,
        durationMinutes: $('#duration').value ? Number($('#duration').value) : null,
        actors: $('#actors').value.trim(),
        description: $('#description').value.trim()
    };
}

function validateClient(dto){
    if(!dto.title) return 'Title is required';
    if(!dto.category) return 'Category is required';
    if(![0,7,11,15,18].includes(dto.ageLimit ?? -1)) return 'Age limit must be 0, 7, 11, 15 or 18';
    if(!dto.durationMinutes || dto.durationMinutes <= 0) return 'Duration must be greater than 0';
    return null;
}

function setPosterPreview(url){
    if(url){
        posterImg.src = url;
        posterImg.onload = () => { posterImg.style.display = 'block'; noPoster.style.display = 'none'; }
        posterImg.onerror = () => { posterImg.style.display = 'none'; noPoster.style.display = 'grid'; }
    } else {
        posterImg.style.display = 'none'; noPoster.style.display = 'grid';
    }
}
if (posterInput) posterInput.addEventListener('input', ()=> setPosterPreview(posterInput.value.trim()));

async function loadMovies(){
    const list = await api('/movies');
    list.sort((a,b)=> (a.title||'').localeCompare(b.title||''));
    moviesDiv.innerHTML = '';
    list.forEach(m=>{
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
      <div style="display:flex; gap:12px; align-items:center;">
        <div style="width:50px;height:75px;border:1px solid #ddd;border-radius:6px;overflow:hidden;background:#f3f3f3;display:grid;place-items:center;">
          ${m.posterUrl ? `<img src="${m.posterUrl}" alt="" style="max-width:100%;max-height:100%;object-fit:cover;">` : `<span style="color:#aaa;font-weight:700;">${(m.title||'?').slice(0,2).toUpperCase()}</span>`}
        </div>
        <div style="flex:1">
          <strong>${escapeHtml(m.title)}</strong>
          <span class="muted"> — ${escapeHtml(m.category)} · ${m.durationMinutes} min · ${m.ageLimit}+</span>
        </div>
        <div class="actions">
          <button class="edit" data-id="${m.movieId}">Edit</button>
          <button class="danger delete" data-id="${m.movieId}">Delete</button>
        </div>
      </div>
      ${m.actors ? `<div class="muted" style="margin-top:6px">${escapeHtml(m.actors)}</div>` : ''}
      ${m.description ? `<div style="margin-top:6px">${escapeHtml(m.description)}</div>` : ''}
    `;
        moviesDiv.appendChild(card);
    });
    moviesDiv.querySelectorAll('.edit').forEach(btn => btn.onclick = () => editMovie(btn.dataset.id));
    moviesDiv.querySelectorAll('.delete').forEach(btn => btn.onclick = () => delMovie(btn.dataset.id));
}

function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])) }

async function editMovie(id){
    try{
        const m = await api(`/movies/${id}`);
        $('#movieId').value = m.movieId ?? '';
        $('#title').value = m.title ?? '';
        $('#posterUrl').value = m.posterUrl ?? '';
        setPosterPreview(m.posterUrl ?? '');
        $('#category').value = m.category ?? '';
        $('#ageLimit').value = String(m.ageLimit ?? '');
        $('#duration').value = m.durationMinutes ?? '';
        $('#actors').value = m.actors ?? '';
        $('#description').value = m.description ?? '';
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }catch(e){ showToast(`Load failed: ${e.message}`); }
}

async function delMovie(id){
    if(!confirm('Delete this movie?\n(Will fail if there are active scheduled shows)')) return;
    try{
        await api(`/movies/${id}`, { method:'DELETE' });
        showToast('Deleted');
        await loadMovies();
        if($('#movieId').value === String(id)) form.reset();
        setPosterPreview('');
    }catch(e){
        if(e.status===409) showToast('Cannot delete — active scheduled shows exist.');
        else showToast(`Delete failed: ${e.message}`);
    }
}

form.addEventListener('submit', async (e)=>{
    e.preventDefault();
    const dto = formToMovie();
    const err = validateClient(dto);
    if(err){ showToast(err); return; }
    try{
        if(dto.movieId){
            await api(`/movies/${dto.movieId}`, { method:'PUT', body: JSON.stringify(dto) });
            showToast('Updated');
        }else{
            const created = await api('/movies', { method:'POST', body: JSON.stringify(dto) });
            $('#movieId').value = created.movieId;
            showToast('Created');
        }
        form.reset(); setPosterPreview('');
        await loadMovies();
    }catch(e){ showToast(e.message || 'Save failed'); }
});

$('#resetBtn').onclick = ()=> { form.reset(); $('#movieId').value=''; setPosterPreview(''); };

loadMovies().catch(err=> showToast(`Initial load failed: ${err.message}`));
