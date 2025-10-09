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
let allProducts = [];
let allProductSales = [];

// Booking state
const state = {
    currentStep: 1,
    selectedMovie: null,
    selectedShow: null,
    selectedSeats: [],
    selectedProducts: [], // [{product: Product, quantity: number}]
    customerName: '',
    customerPhone: '',
    bookingReceipt: null // Stores completed booking data for receipt
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
            api('/booking-seats'),
            api('/products'),
            api('/product-sales')
        ]);
        allMovies = results[0];
        allShows = results[1];
        allSeats = results[2];
        allBookings = results[3];
        allBookingSeats = results[4];
        allProducts = results[5];
        allProductSales = results[6];
        console.log('Data indlæst:', { allMovies, allShows, allSeats, allBookings, allBookingSeats, allProducts, allProductSales });
    } catch (e) {
        showToast('Kunne ikke indlæse data: ' + e.message);
        throw e;
    }
}

// ==================== Step Navigation ====================
function goToStep(stepNum) {
    if (stepNum < 1 || stepNum > 5) return;

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

    if (stepNum < 5) {
        nextBtn.style.display = 'block';
    } else {
        nextBtn.style.display = 'none';
    }

    if (stepNum === 5) {
        confirmBtn.style.display = 'block';
    } else {
        confirmBtn.style.display = 'none';
    }

    // Render trin indhold
    if (stepNum === 1) renderMovies();
    else if (stepNum === 2) renderShows();
    else if (stepNum === 3) renderSeats();
    else if (stepNum === 4) renderProducts();
    else if (stepNum === 5) renderSummary();
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
    // Step 4 (products) is optional - no validation required
    if (currentStep === 5) {
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
    if (validateStep() && state.currentStep < 5) {
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
            state.selectedProducts = [];
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
            state.selectedProducts = [];
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

// ==================== Step 4: Product Selection ====================
function renderProducts() {
    const selectedSeatsInfoProducts = $('#selectedSeatsInfoProducts');
    const productList = $('#productList');
    const selectedProductsInfo = $('#selectedProductsInfo');

    if (!state.selectedShow) {
        productList.innerHTML = '<p class="muted">Ingen forestilling valgt</p>';
        return;
    }

    // Vis info om valgte billetter
    selectedSeatsInfoProducts.innerHTML = `
        <strong>${escapeHtml(state.selectedMovie.title)}</strong><br>
        ${formatDateTime(state.selectedShow.showDatetime)}<br>
        ${state.selectedSeats.length} billet(ter): ${formatPrice(state.selectedSeats.length * Number(state.selectedShow.price))}
    `;

    if (allProducts.length === 0) {
        productList.innerHTML = '<p class="muted">Ingen produkter tilgængelige. Du kan springe dette trin over.</p>';
        selectedProductsInfo.innerHTML = '';
        return;
    }

    // Organiser produkter efter kategori
    const categories = {
        candy: [],
        soda: [],
        popcorn: [],
        other: []
    };

    allProducts.forEach(function(product) {
        categories[product.category].push(product);
    });

    productList.innerHTML = '';

    // Render produkter efter kategori
    const categoryOrder = ['popcorn', 'soda', 'candy', 'other'];
    const categoryNames = {
        candy: 'Slik',
        soda: 'Drikkevarer',
        popcorn: 'Popcorn',
        other: 'Andet'
    };

    categoryOrder.forEach(function(category) {
        const productsInCategory = categories[category];
        if (productsInCategory.length === 0) return;

        // Kategori overskrift
        const categoryHeader = document.createElement('h3');
        categoryHeader.textContent = categoryNames[category];
        categoryHeader.style.gridColumn = '1 / -1';
        categoryHeader.style.marginTop = 'var(--space-3)';
        categoryHeader.style.marginBottom = 'var(--space-2)';
        productList.appendChild(categoryHeader);

        // Render produkter
        productsInCategory.forEach(function(product) {
            const card = document.createElement('div');
            card.className = 'product-card';

            // Find nuværende antal for dette produkt
            const existing = state.selectedProducts.find(function(sp) {
                return sp.product.productId === product.productId;
            });
            const currentQty = existing ? existing.quantity : 0;

            card.innerHTML = `
                <div class="product-header">
                    <div class="product-name">${escapeHtml(product.productName)}</div>
                    <span class="product-category ${escapeHtml(product.category)}">${escapeHtml(product.category)}</span>
                </div>
                <div class="product-price">${formatPrice(product.price)}</div>
                <div class="product-quantity">
                    <button type="button" class="quantity-btn minus" data-product-id="${product.productId}">−</button>
                    <span class="quantity-value">${currentQty}</span>
                    <button type="button" class="quantity-btn plus" data-product-id="${product.productId}">+</button>
                </div>
            `;

            productList.appendChild(card);

            // Event listeners for quantity buttons
            const minusBtn = card.querySelector('.quantity-btn.minus');
            const plusBtn = card.querySelector('.quantity-btn.plus');

            minusBtn.onclick = function() {
                updateProductQuantity(product, -1);
                renderProducts();
            };

            plusBtn.onclick = function() {
                updateProductQuantity(product, 1);
                renderProducts();
            };
        });
    });

    // Opdater valgte produkter information
    updateSelectedProductsInfo();
}

function updateProductQuantity(product, delta) {
    const existing = state.selectedProducts.find(function(sp) {
        return sp.product.productId === product.productId;
    });

    if (existing) {
        existing.quantity += delta;
        if (existing.quantity <= 0) {
            // Fjern fra listen
            state.selectedProducts = state.selectedProducts.filter(function(sp) {
                return sp.product.productId !== product.productId;
            });
        }
    } else if (delta > 0) {
        // Tilføj nyt produkt
        state.selectedProducts.push({
            product: product,
            quantity: delta
        });
    }
}

function updateSelectedProductsInfo() {
    const selectedProductsInfo = $('#selectedProductsInfo');

    if (state.selectedProducts.length === 0) {
        selectedProductsInfo.innerHTML = '<p style="margin: 0;">Ingen produkter valgt. Du kan fortsætte uden at vælge produkter.</p>';
        return;
    }

    let html = '<div style="font-weight: 600; margin-bottom: var(--space-2);">Valgte Produkter:</div>';

    let productsTotal = 0;
    state.selectedProducts.forEach(function(sp) {
        const itemTotal = sp.quantity * Number(sp.product.price);
        productsTotal += itemTotal;
        html += `
            <div class="product-list-item">
                <span>${sp.quantity}x ${escapeHtml(sp.product.productName)}</span>
                <span>${formatPrice(itemTotal)}</span>
            </div>
        `;
    });

    html += `
        <div class="product-subtotal">
            <span>Produkter Total:</span>
            <span>${formatPrice(productsTotal)}</span>
        </div>
    `;

    selectedProductsInfo.innerHTML = html;
}

// ==================== Step 5: Summary & Confirmation ====================
function renderSummary() {
    const bookingSummary = $('#bookingSummary');
    const ticketsAmount = state.selectedSeats.length * Number(state.selectedShow.price);

    // Beregn produkter total
    let productsAmount = 0;
    state.selectedProducts.forEach(function(sp) {
        productsAmount += sp.quantity * Number(sp.product.price);
    });

    const totalAmount = ticketsAmount + productsAmount;

    const seatList = state.selectedSeats
        .map(function(s) {
            return 'Række ' + s.rowNumber + ', Plads ' + s.seatNumber;
        })
        .join(', ');

    let productsSectionHtml = '';
    if (state.selectedProducts.length > 0) {
        const productsList = state.selectedProducts
            .map(function(sp) {
                return sp.quantity + 'x ' + escapeHtml(sp.product.productName) + ' (' + formatPrice(sp.quantity * Number(sp.product.price)) + ')';
            })
            .join('<br>');

        productsSectionHtml = `
            <div class="summary-section">
                <div class="summary-label">Produkter (${state.selectedProducts.length})</div>
                <div class="summary-value">${productsList}</div>
            </div>
            <div class="summary-section">
                <div class="summary-label">Produkter Subtotal</div>
                <div class="summary-value">${formatPrice(productsAmount)}</div>
            </div>
        `;
    }

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
            <div class="summary-label">Billetter (${state.selectedSeats.length})</div>
            <div class="summary-value">${seatList}</div>
        </div>
        <div class="summary-section">
            <div class="summary-label">Billetter Subtotal</div>
            <div class="summary-value">${formatPrice(ticketsAmount)}</div>
        </div>
        ${productsSectionHtml}
        <div class="summary-section">
            <div class="summary-label">Total Beløb</div>
            <div class="summary-total">${formatPrice(totalAmount)}</div>
        </div>
    `;
}

// ==================== Receipt Rendering ====================
function renderReceipt(bookingData) {
    const receiptContent = $('#receiptContent');
    const currentDate = new Date().toLocaleString('da-DK', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    // Build seats list
    const seatsList = bookingData.seats
        .map(function(s) {
            return 'Række ' + s.rowNumber + ', Plads ' + s.seatNumber;
        })
        .join(', ');

    // Build products section if any
    let productsHtml = '';
    if (bookingData.products.length > 0) {
        productsHtml = '<div class="receipt-section-title">PRODUKTER</div>';
        bookingData.products.forEach(function(sp) {
            const itemTotal = sp.quantity * Number(sp.product.price);
            productsHtml += `
                <div class="receipt-row indent">
                    <span>${sp.quantity}x ${escapeHtml(sp.product.productName)}</span>
                    <span>${formatPrice(itemTotal)}</span>
                </div>
            `;
        });
        productsHtml += `
            <div class="receipt-row bold" style="margin-top: 8px;">
                <span>Produkter Subtotal:</span>
                <span>${formatPrice(bookingData.productsAmount)}</span>
            </div>
        `;
    }

    receiptContent.innerHTML = `
        <div class="receipt-cinema-header">
            <div class="receipt-cinema-name">KINO</div>
            <div>Biografbilletter</div>
            <div>${currentDate}</div>
        </div>

        <div class="receipt-section">
            <div class="receipt-row bold">Booking ID: ${bookingData.bookingId}</div>
            <div class="receipt-row">Kunde: ${escapeHtml(bookingData.customerName)}</div>
            <div class="receipt-row">Telefon: ${escapeHtml(bookingData.customerPhone)}</div>
        </div>

        <div class="receipt-section">
            <div class="receipt-section-title">FILM & FORESTILLING</div>
            <div class="receipt-row">
                <span>Film:</span>
                <span>${escapeHtml(bookingData.movie.title)}</span>
            </div>
            <div class="receipt-row">
                <span>Kategori:</span>
                <span>${escapeHtml(bookingData.movie.category)}</span>
            </div>
            <div class="receipt-row">
                <span>Varighed:</span>
                <span>${bookingData.movie.durationMinutes} min</span>
            </div>
            <div class="receipt-row">
                <span>Dato & Tid:</span>
                <span>${formatDateTime(bookingData.show.showDatetime)}</span>
            </div>
            <div class="receipt-row">
                <span>Teater:</span>
                <span>${bookingData.show.theaterId}</span>
            </div>
        </div>

        <div class="receipt-section">
            <div class="receipt-section-title">BILLETTER</div>
            <div class="receipt-row indent">
                <span>${bookingData.seats.length}x Billet @ ${formatPrice(bookingData.show.price)}</span>
                <span>${formatPrice(bookingData.ticketsAmount)}</span>
            </div>
            <div class="receipt-row indent" style="font-size: 0.85em; color: #666;">
                <span colspan="2">${seatsList}</span>
            </div>
            <div class="receipt-row bold" style="margin-top: 8px;">
                <span>Billetter Subtotal:</span>
                <span>${formatPrice(bookingData.ticketsAmount)}</span>
            </div>
        </div>

        ${productsHtml ? '<div class="receipt-section">' + productsHtml + '</div>' : ''}

        <div class="receipt-section">
            <div class="receipt-row total">
                <span>TOTAL:</span>
                <span>${formatPrice(bookingData.totalAmount)}</span>
            </div>
        </div>

        <div class="receipt-barcode">
            <div class="receipt-barcode-text">${bookingData.bookingId.toString().padStart(8, '0')}</div>
        </div>

        <div class="receipt-footer">
            Tak for dit besøg!<br>
            Gem denne kvittering som dokumentation<br>
            Medbring booking ID ved afhentning
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
        // Beregn totaler
        const ticketsAmount = state.selectedSeats.length * Number(state.selectedShow.price);
        let productsAmount = 0;
        state.selectedProducts.forEach(function(sp) {
            productsAmount += sp.quantity * Number(sp.product.price);
        });
        const totalAmount = ticketsAmount + productsAmount;

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

        // 3. Opret product sales for hvert valgt produkt
        for (let i = 0; i < state.selectedProducts.length; i++) {
            const sp = state.selectedProducts[i];
            const productSale = {
                productId: sp.product.productId,
                quantity: sp.quantity,
                totalPrice: sp.quantity * Number(sp.product.price),
                soldById: 1, // Placeholder
                bookingId: createdBooking.bookingId
            };
            await api('/product-sales', {
                method: 'POST',
                body: JSON.stringify(productSale)
            });
        }

        showToast('Booking gennemført!');

        // Store receipt data
        state.bookingReceipt = {
            bookingId: createdBooking.bookingId,
            customerName: state.customerName,
            customerPhone: state.customerPhone,
            movie: state.selectedMovie,
            show: state.selectedShow,
            seats: state.selectedSeats,
            products: state.selectedProducts,
            ticketsAmount: ticketsAmount,
            productsAmount: productsAmount,
            totalAmount: totalAmount,
            bookingDate: new Date()
        };

        // Switch to receipt view
        setTimeout(function() {
            $('#confirmationView').style.display = 'none';
            $('#receiptView').style.display = 'block';

            // Hide navigation buttons
            backBtn.style.display = 'none';
            confirmBtn.style.display = 'none';

            // Render the receipt
            renderReceipt(state.bookingReceipt);

            // Scroll to top
            window.scrollTo(0, 0);

            // Genindlæs data i baggrunden
            loadAllData();
        }, 500);

    } catch (e) {
        showToast('Booking fejlede: ' + e.message);
        console.error('Booking fejl:', e);
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'Bekræft Booking';
    }
};

// ==================== Receipt Actions ====================
// Download Receipt
$('#downloadReceiptBtn').onclick = function() {
    // Trigger browser print dialog (user can save as PDF)
    window.print();
};

// New Booking
$('#newBookingBtn').onclick = function() {
    // Reset all state
    state.selectedMovie = null;
    state.selectedShow = null;
    state.selectedSeats = [];
    state.selectedProducts = [];
    state.customerName = '';
    state.customerPhone = '';
    state.bookingReceipt = null;
    $('#customerForm').reset();

    // Show confirmation view again, hide receipt
    $('#confirmationView').style.display = 'block';
    $('#receiptView').style.display = 'none';

    // Go back to step 1
    goToStep(1);
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
