const API_BASE = '/api';

// DOM hjælpefunktioner
function $(sel) {
    return document.querySelector(sel);
}

function $$(sel) {
    return document.querySelectorAll(sel);
}

// DOM elementer
const toastEl = $('#toast');
const backBtn = $('#backBtn');
const nextBtn = $('#nextBtn');
const confirmBtn = $('#confirmBtn');

// Global data
let allMovies = [];
let allShows = [];
let allSeats = [];
let allBookings = [];
let allBookingSeats = [];

// Booking state
const state = {
    currentStep: 1,
    selectedMovie: null,
    selectedShow: null,
    selectedSeats: [],
    customerName: '',
    customerPhone: ''
};

// ==================== Utilities ====================
function showToast(msg) {
    toastEl.textContent = msg;
    toastEl.style.display = 'block';
    setTimeout(function() {
        toastEl.style.display = 'none';
    }, 3500);
}

async function api(path, options) {
    if (!options) {
        options = {};
    }

    const fetchOptions = {
        headers: { 'Content-Type': 'application/json' }
    };

    // Kopier alle properties fra options til fetchOptions
    for (const key in options) {
        fetchOptions[key] = options[key];
    }

    const res = await fetch(API_BASE + path, fetchOptions);
    const ct = res.headers.get('content-type');
    const contentType = ct ? ct : '';

    let body;
    if (contentType.includes('application/json')) {
        try {
            body = await res.json();
        } catch (e) {
            body = {};
        }
    } else {
        body = await res.text();
    }

    if (!res.ok) {
        let message;
        if (typeof body === 'string') {
            message = body;
        } else {
            message = body.message ? body.message : JSON.stringify(body);
        }

        if (!message) {
            message = 'HTTP ' + res.status;
        }

        const err = new Error(message);
        err.status = res.status;
        throw err;
    }
    return body;
}

function escapeHtml(s) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    };
    const str = s !== null && s !== undefined ? String(s) : '';
    return str.replace(/[&<>"']/g, function(c) {
        return map[c];
    });
}

function formatDateTime(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleString('da-DK', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatPrice(price) {
    return `${Number(price).toFixed(2)} DKK`;
}

// ==================== Data Loading ====================
async function loadAllData() {
    try {
        const results = await Promise.all([
            api('/movies'),
            api('/shows'),
            api('/seats'),
            api('/bookings'),
            api('/booking-seats')
        ]);
        allMovies = results[0];
        allShows = results[1];
        allSeats = results[2];
        allBookings = results[3];
        allBookingSeats = results[4];
        console.log('Data indlæst:', { allMovies, allShows, allSeats, allBookings, allBookingSeats });
    } catch (e) {
        showToast('Kunne ikke indlæse data: ' + e.message);
        throw e;
    }
}

// ==================== Step Navigation ====================
function goToStep(stepNum) {
    if (stepNum < 1 || stepNum > 4) return;

    // Opdater trin indhold synlighed
    $$('.step-content').forEach(function(el) {
        el.classList.remove('active');
    });
    $(`#step${stepNum}`).classList.add('active');

    // Opdater fremskridtsindikator
    $$('.progress-steps .step').forEach(function(step, idx) {
        const num = idx + 1;
        step.classList.remove('active', 'completed');
        if (num < stepNum) step.classList.add('completed');
        if (num === stepNum) step.classList.add('active');
    });

    state.currentStep = stepNum;

    // Opdater knap synlighed
    if (stepNum > 1) {
        backBtn.style.display = 'block';
    } else {
        backBtn.style.display = 'none';
    }

    if (stepNum < 4) {
        nextBtn.style.display = 'block';
    } else {
        nextBtn.style.display = 'none';
    }

    if (stepNum === 4) {
        confirmBtn.style.display = 'block';
    } else {
        confirmBtn.style.display = 'none';
    }

    // Render trin indhold
    if (stepNum === 1) renderMovies();
    else if (stepNum === 2) renderShows();
    else if (stepNum === 3) renderSeats();
    else if (stepNum === 4) renderSummary();
}

function validateStep() {
    const currentStep = state.currentStep;
    const selectedMovie = state.selectedMovie;
    const selectedShow = state.selectedShow;
    const selectedSeats = state.selectedSeats;
    const customerName = state.customerName;
    const customerPhone = state.customerPhone;

    if (currentStep === 1 && !selectedMovie) {
        showToast('Vælg venligst en film');
        return false;
    }
    if (currentStep === 2 && !selectedShow) {
        showToast('Vælg venligst en forestilling');
        return false;
    }
    if (currentStep === 3 && selectedSeats.length === 0) {
        showToast('Vælg venligst mindst én plads');
        return false;
    }
    if (currentStep === 4) {
        if (!customerName.trim()) {
            showToast('Indtast venligst dit navn');
            return false;
        }
        if (!customerPhone.trim()) {
            showToast('Indtast venligst dit telefonnummer');
            return false;
        }
    }
    return true;
}

backBtn.onclick = function() {
    if (state.currentStep > 1) {
        goToStep(state.currentStep - 1);
    }
};

nextBtn.onclick = function() {
    if (validateStep() && state.currentStep < 4) {
        goToStep(state.currentStep + 1);
    }
};

// ==================== Step 1: Movie Selection ====================
function renderMovies() {
    const movieList = $('#movieList');
    movieList.innerHTML = '';

    if (allMovies.length === 0) {
        movieList.innerHTML = '<p class="muted">Ingen film tilgængelige</p>';
        return;
    }

    allMovies.forEach(function(movie) {
        const card = document.createElement('div');
        card.className = 'movie-card';
        if (state.selectedMovie && state.selectedMovie.movieId === movie.movieId) {
            card.classList.add('selected');
        }

        let descriptionHtml = '';
        if (movie.description) {
            descriptionHtml = '<div class="movie-description">' + escapeHtml(movie.description) + '</div>';
        }

        let actorsHtml = '';
        if (movie.actors) {
            actorsHtml = '<div class="movie-meta" style="margin-top: 8px;">Starring: ' + escapeHtml(movie.actors) + '</div>';
        }

        card.innerHTML = `
            <div class="movie-title">${escapeHtml(movie.title)}</div>
            <div class="movie-meta">
                ${escapeHtml(movie.category)} • ${movie.durationMinutes} min • ${movie.ageLimit}+
            </div>
            ${descriptionHtml}
            ${actorsHtml}
        `;

        card.onclick = function() {
            state.selectedMovie = movie;
            state.selectedShow = null;
            state.selectedSeats = [];
            renderMovies();
        };

        movieList.appendChild(card);
    });
}

// ==================== Step 2: Show Selection ====================
function renderShows() {
    const selectedMovieInfo = $('#selectedMovieInfo');
    const showList = $('#showList');

    if (!state.selectedMovie) {
        showList.innerHTML = '<p class="muted">Ingen film valgt</p>';
        return;
    }

    selectedMovieInfo.innerHTML = `
        <strong>${escapeHtml(state.selectedMovie.title)}</strong> —
        ${escapeHtml(state.selectedMovie.category)} •
        ${state.selectedMovie.durationMinutes} min
    `;

    const movieShows = allShows.filter(function(s) {
        return s.movieId === state.selectedMovie.movieId && s.status === 'scheduled';
    });

    if (movieShows.length === 0) {
        showList.innerHTML = '<p class="muted">Ingen forestillinger tilgængelige for denne film</p>';
        return;
    }

    // Sortér efter dato og tid
    movieShows.sort(function(a, b) {
        return new Date(a.showDatetime) - new Date(b.showDatetime);
    });

    showList.innerHTML = '';
    movieShows.forEach(function(show) {
        const card = document.createElement('div');
        card.className = 'show-card';
        if (state.selectedShow && state.selectedShow.showId === show.showId) {
            card.classList.add('selected');
        }

        card.innerHTML = `
            <div class="show-info">
                <div class="show-datetime">${formatDateTime(show.showDatetime)}</div>
                <div class="show-details">Theater ${show.theaterId}</div>
            </div>
            <div class="show-price">${formatPrice(show.price)}</div>
        `;

        card.onclick = function() {
            state.selectedShow = show;
            state.selectedSeats = [];
            renderShows();
        };

        showList.appendChild(card);
    });
}

// ==================== Step 3: Seat Selection ====================
function getBookedSeatsForShow(showId) {
    // Hent alle bookinger for denne forestilling
    const showBookings = allBookings.filter(function(b) {
        return b.showId === showId;
    });
    const bookingIds = showBookings.map(function(b) {
        return b.bookingId;
    });

    // Hent alle sæde-ID'er der er booket
    const bookedSeatIds = allBookingSeats
        .filter(function(bs) {
            return bookingIds.includes(bs.bookingId);
        })
        .map(function(bs) {
            return bs.seatId;
        });

    return new Set(bookedSeatIds);
}

function renderSeats() {
    const selectedShowInfo = $('#selectedShowInfo');
    const seatMap = $('#seatMap');
    const selectedSeatsInfo = $('#selectedSeatsInfo');

    if (!state.selectedShow) {
        seatMap.innerHTML = '<p class="muted">Ingen forestilling valgt</p>';
        return;
    }

    selectedShowInfo.innerHTML = `
        <strong>${escapeHtml(state.selectedMovie.title)}</strong><br>
        ${formatDateTime(state.selectedShow.showDatetime)} — Theater ${state.selectedShow.theaterId}<br>
        Pris per plads: ${formatPrice(state.selectedShow.price)}
    `;

    // Hent pladser for denne sal
    const theaterSeats = allSeats.filter(function(s) {
        return s.theaterId === state.selectedShow.theaterId;
    });

    if (theaterSeats.length === 0) {
        seatMap.innerHTML = '<p class="muted">Ingen pladser tilgængelige for denne sal</p>';
        return;
    }

    // Hent bookede pladser
    const bookedSeatIds = getBookedSeatsForShow(state.selectedShow.showId);

    // Organiser pladser efter række
    const seatsByRow = {};
    let maxRow = 0;
    let maxSeatInRow = 0;

    theaterSeats.forEach(function(seat) {
        if (!seatsByRow[seat.rowNumber]) {
            seatsByRow[seat.rowNumber] = [];
        }
        seatsByRow[seat.rowNumber].push(seat);
        maxRow = Math.max(maxRow, seat.rowNumber);
        maxSeatInRow = Math.max(maxSeatInRow, seat.seatNumber);
    });

    // Render sæde-kort
    seatMap.innerHTML = '';
    for (let row = 1; row <= maxRow; row++) {
        const rowDiv = document.createElement('div');
        rowDiv.className = 'seat-row';

        // Række-label
        const label = document.createElement('div');
        label.className = 'row-label';
        label.textContent = `Række ${row}`;
        rowDiv.appendChild(label);

        // Pladser i denne række
        const rowSeats = seatsByRow[row] || [];
        rowSeats.sort(function(a, b) {
            return a.seatNumber - b.seatNumber;
        });

        rowSeats.forEach(function(seat) {
            const seatBtn = document.createElement('button');
            seatBtn.className = 'seat';
            seatBtn.textContent = seat.seatNumber;
            seatBtn.type = 'button';

            const isBooked = bookedSeatIds.has(seat.seatId);
            const isSelected = state.selectedSeats.some(function(s) {
                return s.seatId === seat.seatId;
            });

            if (isBooked) {
                seatBtn.classList.add('booked');
                seatBtn.disabled = true;
            } else if (isSelected) {
                seatBtn.classList.add('selected');
            } else {
                seatBtn.classList.add('available');
            }

            seatBtn.onclick = function() {
                if (!isBooked) {
                    if (isSelected) {
                        // Fravælg
                        state.selectedSeats = state.selectedSeats.filter(function(s) {
                            return s.seatId !== seat.seatId;
                        });
                    } else {
                        // Vælg
                        state.selectedSeats.push(seat);
                    }
                    renderSeats();
                }
            };

            rowDiv.appendChild(seatBtn);
        });

        seatMap.appendChild(rowDiv);
    }

    // Opdater valgte pladser information
    if (state.selectedSeats.length === 0) {
        selectedSeatsInfo.innerHTML = 'Ingen pladser valgt';
    } else {
        const seatList = state.selectedSeats
            .map(function(s) {
                return 'Række ' + s.rowNumber + ', Plads ' + s.seatNumber;
            })
            .join(', ');
        const total = state.selectedSeats.length * Number(state.selectedShow.price);
        selectedSeatsInfo.innerHTML = `
            Valgt: ${seatList}<br>
            Total: ${formatPrice(total)}
        `;
    }
}

// ==================== Step 4: Summary & Confirmation ====================
function renderSummary() {
    const bookingSummary = $('#bookingSummary');
    const totalAmount = state.selectedSeats.length * Number(state.selectedShow.price);

    const seatList = state.selectedSeats
        .map(function(s) {
            return 'Række ' + s.rowNumber + ', Plads ' + s.seatNumber;
        })
        .join(', ');

    bookingSummary.innerHTML = `
        <div class="summary-section">
            <div class="summary-label">Film</div>
            <div class="summary-value">${escapeHtml(state.selectedMovie.title)}</div>
        </div>
        <div class="summary-section">
            <div class="summary-label">Forestilling</div>
            <div class="summary-value">
                ${formatDateTime(state.selectedShow.showDatetime)}<br>
                Theater ${state.selectedShow.theaterId}
            </div>
        </div>
        <div class="summary-section">
            <div class="summary-label">Pladser (${state.selectedSeats.length})</div>
            <div class="summary-value">${seatList}</div>
        </div>
        <div class="summary-section">
            <div class="summary-label">Total Beløb</div>
            <div class="summary-total">${formatPrice(totalAmount)}</div>
        </div>
    `;
}

// ==================== Booking Submission ====================
confirmBtn.onclick = async function() {
    // Indhent kunde information
    state.customerName = $('#customerName').value.trim();
    state.customerPhone = $('#customerPhone').value.trim();

    if (!validateStep()) return;

    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Behandler...';

    try {
        const totalAmount = state.selectedSeats.length * Number(state.selectedShow.price);

        // 1. Opret bookingen
        const booking = {
            showId: state.selectedShow.showId,
            customerName: state.customerName,
            customerPhone: state.customerPhone,
            totalAmount: totalAmount,
            soldById: 1 // Placeholder - TODO: brug faktisk bruger-ID når auth er implementeret
        };

        const createdBooking = await api('/bookings', {
            method: 'POST',
            body: JSON.stringify(booking)
        });

        console.log('Booking oprettet:', createdBooking);

        // 2. Opret booking-sæde entries for hver valgte plads
        for (let i = 0; i < state.selectedSeats.length; i++) {
            const seat = state.selectedSeats[i];
            const bookingSeat = {
                bookingId: createdBooking.bookingId,
                seatId: seat.seatId
            };
            await api('/booking-seats', {
                method: 'POST',
                body: JSON.stringify(bookingSeat)
            });
        }

        showToast('Booking gennemført!');

        // Vis bekræftelse og nulstil
        setTimeout(function() {
            alert('Booking bekræftet!\n\nBooking ID: ' + createdBooking.bookingId + '\nFilm: ' + state.selectedMovie.title + '\nForestilling: ' + formatDateTime(state.selectedShow.showDatetime) + '\nPladser: ' + state.selectedSeats.length + '\nTotal: ' + formatPrice(totalAmount) + '\n\nTak, ' + state.customerName + '!');

            // Nulstil og start forfra
            state.selectedMovie = null;
            state.selectedShow = null;
            state.selectedSeats = [];
            state.customerName = '';
            state.customerPhone = '';
            $('#customerForm').reset();

            // Genindlæs data for at få opdaterede bookinger
            loadAllData().then(function() {
                goToStep(1);
            });
        }, 500);

    } catch (e) {
        showToast('Booking fejlede: ' + e.message);
        console.error('Booking fejl:', e);
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'Bekræft Booking';
    }
};

// ==================== Initialize ====================
(async function init() {
    try {
        await loadAllData();
        goToStep(1);
    } catch (e) {
        showToast('Kunne ikke initialisere booking system');
        console.error('Initialiseringsfejl:', e);
    }
})();
