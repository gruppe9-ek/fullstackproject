const API_BASE = '/api';
const $ = (sel) => document.querySelector(sel);

const showsDiv = $('#shows');
const form = $('#showForm');
const toastEl = $('#toast');
const movieSelect = $('#movieId');
const theaterSelect = $('#theaterId');

let movies = [];
let theaters = [];

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

function formToShow(){
    const date = $('#showDate').value;
    const time = $('#showTime').value;
    const showDatetime = date && time ? `${date}T${time}:00` : null;

    return {
        showId: $('#showId').value ? Number($('#showId').value) : null,
        movieId: $('#movieId').value ? Number($('#movieId').value) : null,
        theaterId: $('#theaterId').value ? Number($('#theaterId').value) : null,
        showDatetime: showDatetime,
        price: $('#price').value ? Number($('#price').value) : null,
        status: $('#status').value || 'scheduled'
    };
}

function validateClient(dto){
    if(!dto.movieId) return 'Film er påkrævet';
    if(!dto.theaterId) return 'Sal er påkrævet';
    if(!dto.showDatetime) return 'Dato og tid er påkrævet';
    if(!dto.price || dto.price < 0) return 'Pris skal være >= 0';
    if(!dto.status) return 'Status er påkrævet';
    return null;
}

async function loadMovies(){
    try {
        movies = await api('/movies');
        movies.sort((a,b)=> (a.title||'').localeCompare(b.title||''));
        movieSelect.innerHTML = '<option value="">Vælg film...</option>';
        movies.forEach(m => {
            const opt = document.createElement('option');
            opt.value = m.movieId;
            opt.textContent = m.title;
            movieSelect.appendChild(opt);
        });
    } catch(e) {
        showToast(`Kunne ikke indlæse film: ${e.message}`);
    }
}

async function loadTheaters(){
    try {
        theaters = await api('/theaters');
        theaters.sort((a,b)=> (a.theaterName||'').localeCompare(b.theaterName||''));
        theaterSelect.innerHTML = '<option value="">Vælg sal...</option>';
        theaters.forEach(t => {
            const opt = document.createElement('option');
            opt.value = t.theaterId;
            opt.textContent = `${t.theaterName} (${t.totalRows}x${t.seatsPerRow})`;
            theaterSelect.appendChild(opt);
        });
    } catch(e) {
        showToast(`Kunne ikke indlæse sale: ${e.message}`);
    }
}

async function loadShows(){
    try {
        const list = await api('/shows');
        list.sort((a,b)=> (a.showDatetime||'').localeCompare(b.showDatetime||''));
        showsDiv.innerHTML = '';

        if(list.length === 0) {
            showsDiv.innerHTML = '<div class="muted">Ingen forestillinger endnu</div>';
            return;
        }

        list.forEach(s=>{
            const movie = movies.find(m => m.movieId === s.movieId);
            const theater = theaters.find(t => t.theaterId === s.theaterId);
            const movieTitle = movie ? movie.title : `Film #${s.movieId}`;
            const theaterName = theater ? theater.theaterName : `Sal #${s.theaterId}`;

            const datetime = new Date(s.showDatetime);
            const dateStr = datetime.toLocaleDateString('da-DK', {
                year: 'numeric', month: 'short', day: 'numeric'
            });
            const timeStr = datetime.toLocaleTimeString('da-DK', {
                hour: '2-digit', minute: '2-digit'
            });

            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
          <div style="display:flex; gap:12px; align-items:center; justify-content:space-between;">
            <div style="flex:1">
              <div style="display:flex; gap:8px; align-items:baseline;">
                <strong style="font-size:1.1em;">${escapeHtml(movieTitle)}</strong>
                <span class="badge badge-${s.status === 'scheduled' ? 'success' : 'danger'}">${s.status}</span>
              </div>
              <div class="muted" style="margin-top:4px">
                🗓 ${dateStr} kl. ${timeStr} · 🏛 ${escapeHtml(theaterName)} · 💰 ${s.price} kr.
              </div>
            </div>
            <div class="actions">
              <button class="edit" data-id="${s.showId}">Rediger</button>
              <button class="danger delete" data-id="${s.showId}">Slet</button>
            </div>
          </div>
        `;
            showsDiv.appendChild(card);
        });

        showsDiv.querySelectorAll('.edit').forEach(btn => btn.onclick = () => editShow(btn.dataset.id));
        showsDiv.querySelectorAll('.delete').forEach(btn => btn.onclick = () => delShow(btn.dataset.id));
    } catch(e) {
        showToast(`Kunne ikke indlæse forestillinger: ${e.message}`);
    }
}

function escapeHtml(s){ return String(s ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])) }

async function editShow(id){
    try{
        const s = await api(`/shows/${id}`);
        $('#showId').value = s.showId ?? '';
        $('#movieId').value = s.movieId ?? '';
        $('#theaterId').value = s.theaterId ?? '';

        // Split showDatetime into date and time
        if(s.showDatetime) {
            const dt = new Date(s.showDatetime);
            const dateStr = dt.toISOString().split('T')[0];
            const timeStr = dt.toTimeString().split(' ')[0].substring(0, 5);
            $('#showDate').value = dateStr;
            $('#showTime').value = timeStr;
        }

        $('#price').value = s.price ?? '';
        $('#status').value = s.status ?? 'scheduled';

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }catch(e){ showToast(`Indlæsning fejlede: ${e.message}`); }
}

async function delShow(id){
    if(!confirm('Slet denne forestilling?\n(Vil fejle hvis der er bookinger)')) return;
    try{
        await api(`/shows/${id}`, { method:'DELETE' });
        showToast('Slettet');
        await loadShows();
        if($('#showId').value === String(id)) form.reset();
    }catch(e){
        if(e.status===409) showToast('Kan ikke slette — der eksisterer bookinger.');
        else showToast(`Sletning fejlede: ${e.message}`);
    }
}

form.addEventListener('submit', async (e)=>{
    e.preventDefault();
    const dto = formToShow();
    const err = validateClient(dto);
    if(err){ showToast(err); return; }
    try{
        if(dto.showId){
            await api(`/shows/${dto.showId}`, { method:'PUT', body: JSON.stringify(dto) });
            showToast('Opdateret');
        }else{
            const created = await api('/shows', { method:'POST', body: JSON.stringify(dto) });
            $('#showId').value = created.showId;
            showToast('Oprettet');
        }
        form.reset();
        await loadShows();
    }catch(e){
        if(e.status === 409) showToast('Konflikt: Salen er optaget på dette tidspunkt');
        else showToast(e.message || 'Gem fejlede');
    }
});

$('#resetBtn').onclick = ()=> {
    form.reset();
    $('#showId').value='';
};

// Initialize
(async () => {
    await Promise.all([loadMovies(), loadTheaters()]);
    await loadShows();
})().catch(err=> showToast(`Initialisering fejlede: ${err.message}`));
