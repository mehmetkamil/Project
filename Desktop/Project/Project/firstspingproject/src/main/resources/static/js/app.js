// Global variables
let currentEvent = null;
let authModal, eventModal, profileModal, cartModal;
let selectedCategory = ''; // Track selected category from pills
let cart = []; // Shopping cart

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Bootstrap modals
    authModal = new bootstrap.Modal(document.getElementById('authModal'));
    eventModal = new bootstrap.Modal(document.getElementById('eventModal'));
    profileModal = new bootstrap.Modal(document.getElementById('profileModal'));
    cartModal = new bootstrap.Modal(document.getElementById('cartModal'));
    
    // Load cart from localStorage
    loadCartFromStorage();
    
    loadEvents();
    checkUserSession();
    setupEventListeners();
});

// Load cart from localStorage
function loadCartFromStorage() {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
        cart = JSON.parse(savedCart);
        updateCartBadge();
    }
}

// Save cart to localStorage
function saveCartToStorage() {
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartBadge();
}

// Update cart badge
function updateCartBadge() {
    const badge = document.getElementById('cartBadge');
    const cartNavItem = document.getElementById('cartNavItem');
    const user = JSON.parse(localStorage.getItem('currentUser'));
    
    if (user) {
        cartNavItem.style.display = 'block';
        if (cart.length > 0) {
            badge.style.display = 'inline';
            badge.textContent = cart.length;
        } else {
            badge.style.display = 'none';
        }
    } else {
        cartNavItem.style.display = 'none';
    }
}

// Filter by category
function filterByCategory(category) {
    // Update active button
    document.querySelectorAll('.category-pills .btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Find and activate the clicked button
    document.querySelectorAll('.category-pills .btn').forEach(btn => {
        const btnCategory = btn.getAttribute('onclick').match(/filterByCategory\('([^']*)'\)/)?.[1] || '';
        if (btnCategory === category) {
            btn.classList.add('active');
        }
    });
    
    // Set selected category and load
    selectedCategory = category;
    console.log('📂 Category selected:', category || 'ALL');
    loadEvents();
}

// Check if user is logged in
function checkUserSession() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    
    if (user) {
        // Show user welcome and hide auth button
        document.getElementById('userWelcome').classList.remove('d-none');
        document.getElementById('currentUsername').textContent = user.username;
        document.getElementById('authBtn').innerHTML = '<i class="bi bi-person-check me-1"></i>Profilim';
        document.getElementById('authBtn').onclick = () => showProfile(user);
        document.getElementById('userMenuNav').style.display = 'block';
        document.getElementById('cartNavItem').style.display = 'block';
        updateCartBadge();
    } else {
        // Hide user welcome and show auth button
        document.getElementById('userWelcome').classList.add('d-none');
        document.getElementById('authBtn').innerHTML = '<i class="bi bi-box-arrow-in-right me-1"></i>Giriş Yap';
        document.getElementById('authBtn').onclick = openAuthModal;
        document.getElementById('userMenuNav').style.display = 'none';
        document.getElementById('cartNavItem').style.display = 'none';
    }
}

// Setup event listeners
function setupEventListeners() {
    // Login form
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;
        
        const user = await loginUser(username, password);
        if (user) {
            authModal.hide();
            checkUserSession();
            document.getElementById('loginForm').reset();
        }
    });

    // Register form
    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('registerUsername').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        
        const user = await registerUser(username, email, password, ['USER']);
        if (user) {
            authModal.hide();
            checkUserSession();
            document.getElementById('registerForm').reset();
        }
    });
}

// Load all events
async function loadEvents() {
    const searchText = document.getElementById('filterCategory').value.trim().toLowerCase();
    const locationText = document.getElementById('filterLocation').value.trim().toLowerCase();
    
    console.log('🔄 Loading events... selectedCategory:', selectedCategory);
    
    // Show loading state
    const eventsList = document.getElementById('eventsList');
    eventsList.innerHTML = `
        <div class="col-12 text-center py-5">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Yükleniyor...</span>
            </div>
            <p class="mt-3 text-muted">Etkinlikler yükleniyor...</p>
        </div>
    `;
    
    try {
        let events = [];
        
        // First, get events based on category pill selection
        if (selectedCategory) {
            console.log('📂 Fetching by category:', selectedCategory);
            events = await getEventsByCategory(selectedCategory);
        } else {
            console.log('📂 Fetching all events');
            events = await getAllEvents();
        }
        
        console.log('📊 Events received:', events);
        
        // Ensure events is an array
        if (!Array.isArray(events)) {
            console.warn('⚠️ Events is not an array, converting...');
            events = [];
        }
        
        // Then filter by search text if provided
        if (searchText) {
            events = events.filter(e => 
                e.title.toLowerCase().includes(searchText) ||
                e.description.toLowerCase().includes(searchText) ||
                e.category.toLowerCase().includes(searchText)
            );
        }
        
        // Filter by location if provided
        if (locationText) {
            events = events.filter(e => 
                e.location.toLowerCase().includes(locationText)
            );
        }
        
        console.log('✅ Final events to display:', events.length);
        displayEvents(events);
    } catch (error) {
        console.error('Error loading events:', error);
        eventsList.innerHTML = `
            <div class="col-12 text-center py-5">
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    Etkinlikler yüklenirken bir hata oluştu. Lütfen sayfayı yenileyiniz.
                </div>
            </div>
        `;
    }
}

// Display events on page
function displayEvents(events) {
    const eventsList = document.getElementById('eventsList');
    
    // Update total events count
    const totalEventsEl = document.getElementById('totalEvents');
    if (totalEventsEl) {
        totalEventsEl.textContent = events.length;
    }
    
    if (!events || events.length === 0) {
        eventsList.innerHTML = `
            <div class="col-12">
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="bi bi-calendar-x"></i>
                    </div>
                    <h4>Etkinlik bulunamadı</h4>
                    <p class="text-muted">Filtreleri temizleyerek tüm etkinlikleri görüntüleyebilirsiniz.</p>
                    <button class="btn btn-primary" onclick="clearFilters()">
                        <i class="bi bi-arrow-repeat me-1"></i>Tüm Etkinlikleri Göster
                    </button>
                </div>
            </div>
        `;
        return;
    }
    
    // Category icons
    const categoryIcons = {
        'KONSER': 'bi-music-note-beamed',
        'TİYATRO': 'bi-mask',
        'SPOR': 'bi-trophy',
        'KONFERANS': 'bi-mic',
        'EĞİTİM': 'bi-book',
        'FESTİVAL': 'bi-stars',
        'STAND-UP': 'bi-emoji-laughing',
        'SERGİ': 'bi-palette',
        'ÇOCUK': 'bi-balloon'
    };
    
    eventsList.innerHTML = events.map((event, index) => `
        <div class="col-lg-4 col-md-6 mb-4" style="animation-delay: ${index * 0.1}s">
            <div class="card event-card h-100" onclick="showEventDetails(${event.id})">
                <div class="event-card-header">
                    <div class="event-category">
                        <i class="${categoryIcons[event.category] || 'bi-calendar-event'} me-1"></i>
                        ${event.category}
                    </div>
                    <div class="event-title">${event.title}</div>
                    <div class="event-meta">
                        <i class="bi bi-geo-alt me-1"></i>${event.location}
                    </div>
                </div>
                <div class="card-body">
                    <p class="card-text text-muted">${event.description.substring(0, 120)}${event.description.length > 120 ? '...' : ''}</p>
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div class="event-meta">
                            <i class="bi bi-calendar3 me-1"></i>
                            ${new Date(event.startDateTime).toLocaleDateString('tr-TR', {day: 'numeric', month: 'long', year: 'numeric'})}
                        </div>
                        <div class="event-meta">
                            <i class="bi bi-clock me-1"></i>
                            ${new Date(event.startDateTime).toLocaleTimeString('tr-TR', {hour: '2-digit', minute: '2-digit'})}
                        </div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="event-price">₺${event.price.toLocaleString('tr-TR')}</div>
                        <div class="event-available-seats ${event.availableSeats === 0 ? 'event-no-seats' : ''}">
                            ${event.availableSeats > 0 
                                ? `<i class="bi bi-ticket-perforated me-1"></i>${event.availableSeats.toLocaleString('tr-TR')} bilet` 
                                : '<i class="bi bi-x-circle me-1"></i>Tükendi'}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// Show event details in modal
async function showEventDetails(eventId) {
    try {
        const event = await getEventById(eventId);
        currentEvent = event;
        
        if (!event) {
            showNotification('Etkinlik yüklenemedi.', 'danger');
            return;
        }
        
        const startDate = new Date(event.startDateTime);
        const endDate = new Date(event.endDateTime);
        
        // Category icons
        const categoryIcons = {
            'KONSER': 'bi-music-note-beamed',
            'TİYATRO': 'bi-mask',
            'SPOR': 'bi-trophy',
            'KONFERANS': 'bi-mic',
            'EĞİTİM': 'bi-book',
            'FESTİVAL': 'bi-stars',
            'STAND-UP': 'bi-emoji-laughing',
            'SERGİ': 'bi-palette',
            'ÇOCUK': 'bi-balloon'
        };
        
        document.getElementById('eventTitle').innerHTML = `
            <i class="${categoryIcons[event.category] || 'bi-calendar-event'} me-2"></i>${event.title}
        `;
        
        document.getElementById('eventDetails').innerHTML = `
            <div class="event-detail-card">
                <div class="mb-4">
                    <span class="badge bg-primary fs-6 me-2">
                        <i class="${categoryIcons[event.category] || 'bi-calendar-event'} me-1"></i>
                        ${event.category}
                    </span>
                    <span class="badge ${event.availableSeats > 0 ? 'bg-success' : 'bg-danger'} fs-6">
                        ${event.availableSeats > 0 ? `${event.availableSeats.toLocaleString('tr-TR')} bilet mevcut` : 'Bilet Tükendi'}
                    </span>
                </div>
                
                <div class="mb-4">
                    <h6 class="text-muted mb-2"><i class="bi bi-card-text me-2"></i>Açıklama</h6>
                    <p class="fs-6">${event.description}</p>
                </div>
                
                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <div class="info-card p-3 bg-light rounded-3">
                            <h6 class="text-muted mb-2"><i class="bi bi-geo-alt me-2"></i>Konum</h6>
                            <p class="mb-0 fw-bold">${event.location}</p>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-card p-3 bg-light rounded-3">
                            <h6 class="text-muted mb-2"><i class="bi bi-cash-stack me-2"></i>Bilet Fiyatı</h6>
                            <p class="mb-0 fw-bold text-success fs-4">₺${event.price.toLocaleString('tr-TR')}</p>
                        </div>
                    </div>
                </div>
                
                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <div class="info-card p-3 bg-light rounded-3">
                            <h6 class="text-muted mb-2"><i class="bi bi-calendar-check me-2"></i>Başlangıç</h6>
                            <p class="mb-0 fw-bold">${startDate.toLocaleDateString('tr-TR', {weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'})}</p>
                            <p class="mb-0 text-primary">${startDate.toLocaleTimeString('tr-TR', {hour: '2-digit', minute: '2-digit'})}</p>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-card p-3 bg-light rounded-3">
                            <h6 class="text-muted mb-2"><i class="bi bi-calendar-x me-2"></i>Bitiş</h6>
                            <p class="mb-0 fw-bold">${endDate.toLocaleDateString('tr-TR', {weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'})}</p>
                            <p class="mb-0 text-primary">${endDate.toLocaleTimeString('tr-TR', {hour: '2-digit', minute: '2-digit'})}</p>
                        </div>
                    </div>
                </div>
                
                <div class="row g-4">
                    <div class="col-md-6">
                        <div class="info-card p-3 bg-light rounded-3">
                            <h6 class="text-muted mb-2"><i class="bi bi-people me-2"></i>Kapasite</h6>
                            <p class="mb-0 fw-bold">${event.capacity.toLocaleString('tr-TR')} kişi</p>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="info-card p-3 bg-light rounded-3">
                            <h6 class="text-muted mb-2"><i class="bi bi-ticket-perforated me-2"></i>Kalan Bilet</h6>
                            <p class="mb-0 fw-bold ${event.availableSeats > 0 ? 'text-success' : 'text-danger'}">
                                ${event.availableSeats.toLocaleString('tr-TR')} bilet
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Show booking section only if user is logged in and tickets are available
        const user = JSON.parse(localStorage.getItem('currentUser'));
        const bookingSection = document.getElementById('bookingSection');
        const bookBtn = document.getElementById('bookBtn');
        
        // Check if this event is already in cart
        const isInCart = cart.some(item => item.eventId === event.id);
        
        if (user && event.availableSeats > 0) {
            if (isInCart) {
                bookingSection.innerHTML = `
                    <hr>
                    <div class="alert alert-info mb-0">
                        <div class="d-flex justify-content-between align-items-center">
                            <span><i class="bi bi-cart-check me-2"></i>Bu etkinlik sepetinizde!</span>
                            <button class="btn btn-success" onclick="showCartModal(); eventModal.hide();">
                                <i class="bi bi-cart3 me-1"></i>Sepete Git
                            </button>
                        </div>
                    </div>
                `;
            } else {
                bookingSection.innerHTML = `
                    <hr>
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h5 class="mb-0"><i class="bi bi-ticket-perforated me-2"></i>Bilet Rezervasyonu</h5>
                            <small class="text-muted">Sepete ekleyerek alışverişe devam edin!</small>
                        </div>
                        <div class="d-flex gap-2">
                            <button class="btn btn-outline-success btn-lg px-4" onclick="addToCart(${event.id})">
                                <i class="bi bi-cart-plus me-2"></i>Sepete Ekle
                            </button>
                            <button class="btn btn-success btn-lg px-4" onclick="buyNow(${event.id})">
                                <i class="bi bi-lightning me-2"></i>Hemen Al
                            </button>
                        </div>
                    </div>
                `;
            }
            bookingSection.style.display = 'block';
        } else if (!user) {
            bookingSection.innerHTML = `
                <hr>
                <div class="alert alert-warning mb-0">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    Bilet rezervasyonu için lütfen <a href="#" onclick="authModal.show(); showAuthForm('login'); eventModal.hide(); return false;" class="fw-bold">giriş yapın</a> veya 
                    <a href="#" onclick="authModal.show(); showAuthForm('register'); eventModal.hide(); return false;" class="fw-bold">kayıt olun</a>.
                </div>
            `;
            bookingSection.style.display = 'block';
        } else {
            bookingSection.innerHTML = `
                <hr>
                <div class="alert alert-danger mb-0">
                    <i class="bi bi-x-circle me-2"></i>
                    Maalesef bu etkinliğin tüm biletleri tükenmiştir.
                </div>
            `;
            bookingSection.style.display = 'block';
        }
        
        eventModal.show();
    } catch (error) {
        console.error('Error loading event:', error);
        showNotification('Etkinlik açılırken hata oluştu.', 'danger');
    }
}

// Add to cart
function addToCart(eventId) {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    
    if (!user) {
        showNotification('Lütfen önce giriş yapın.', 'warning');
        return;
    }
    
    if (!currentEvent) {
        showNotification('Etkinlik bilgisi bulunamadı.', 'danger');
        return;
    }
    
    // Check if already in cart
    if (cart.some(item => item.eventId === eventId)) {
        showNotification('Bu etkinlik zaten sepetinizde!', 'warning');
        return;
    }
    
    // Add to cart
    cart.push({
        eventId: currentEvent.id,
        title: currentEvent.title,
        price: currentEvent.price,
        category: currentEvent.category,
        location: currentEvent.location,
        startDateTime: currentEvent.startDateTime,
        quantity: 1
    });
    
    saveCartToStorage();
    showNotification('✅ Etkinlik sepete eklendi!', 'success');
    eventModal.hide();
}

// Buy now (add to cart and go to checkout)
async function buyNow(eventId) {
    addToCart(eventId);
    eventModal.hide();
    setTimeout(() => {
        showCartModal();
    }, 300);
}

// Show cart modal
function showCartModal() {
    renderCart();
    cartModal.show();
}

// Render cart items
function renderCart() {
    const cartItems = document.getElementById('cartItems');
    const cartSummary = document.getElementById('cartSummary');
    const cartTotal = document.getElementById('cartTotal');
    
    if (cart.length === 0) {
        cartItems.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-cart-x text-muted" style="font-size: 4rem;"></i>
                <h5 class="mt-3">Sepetiniz Boş</h5>
                <p class="text-muted">Etkinlikleri keşfederek sepetinize bilet ekleyin!</p>
                <button class="btn btn-primary" onclick="cartModal.hide(); document.getElementById('eventsSection').scrollIntoView({behavior: 'smooth'})">
                    <i class="bi bi-search me-1"></i>Etkinlikleri Keşfet
                </button>
            </div>
        `;
        cartSummary.style.display = 'none';
        return;
    }
    
    // Category icons
    const categoryIcons = {
        'KONSER': 'bi-music-note-beamed',
        'TİYATRO': 'bi-mask',
        'SPOR': 'bi-trophy',
        'KONFERANS': 'bi-mic',
        'EĞİTİM': 'bi-book',
        'FESTİVAL': 'bi-stars',
        'STAND-UP': 'bi-emoji-laughing',
        'SERGİ': 'bi-palette',
        'ÇOCUK': 'bi-balloon'
    };
    
    let total = 0;
    cartItems.innerHTML = cart.map((item, index) => {
        total += item.price * item.quantity;
        const startDate = new Date(item.startDateTime);
        return `
            <div class="card mb-3">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <div class="d-flex align-items-center mb-2">
                                <span class="badge bg-primary me-2">
                                    <i class="${categoryIcons[item.category] || 'bi-calendar-event'} me-1"></i>${item.category}
                                </span>
                                <h6 class="mb-0">${item.title}</h6>
                            </div>
                            <small class="text-muted">
                                <i class="bi bi-geo-alt me-1"></i>${item.location} |
                                <i class="bi bi-calendar3 ms-2 me-1"></i>${startDate.toLocaleDateString('tr-TR')}
                            </small>
                        </div>
                        <div class="col-md-4 text-md-end mt-2 mt-md-0">
                            <div class="d-flex align-items-center justify-content-md-end gap-3">
                                <span class="fw-bold text-success fs-5">₺${item.price.toLocaleString('tr-TR')}</span>
                                <button class="btn btn-outline-danger btn-sm" onclick="removeFromCart(${index})">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
    
    cartTotal.textContent = '₺' + total.toLocaleString('tr-TR');
    cartSummary.style.display = 'block';
}

// Remove from cart
function removeFromCart(index) {
    cart.splice(index, 1);
    saveCartToStorage();
    renderCart();
    showNotification('Etkinlik sepetten kaldırıldı.', 'info');
}

// Clear cart
function clearCart() {
    cart = [];
    saveCartToStorage();
    renderCart();
    showNotification('Sepet temizlendi.', 'info');
}

// Complete checkout - purchase all items in cart
async function completeCheckout() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    
    if (!user) {
        showNotification('Lütfen önce giriş yapın.', 'warning');
        return;
    }
    
    if (cart.length === 0) {
        showNotification('Sepetiniz boş!', 'warning');
        return;
    }
    
    let successCount = 0;
    let failCount = 0;
    
    console.log('🛒 Sepet:', cart);
    console.log('👤 Kullanıcı:', user);
    
    // Get JWT token
    const token = localStorage.getItem('jwtToken');
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Process each item in cart
    for (const item of cart) {
        try {
            const url = `${API_BASE}/v1/tickets/book?eventId=${item.eventId}&buyerId=${user.id}`;
            console.log('📡 İstek gönderiliyor:', url);
            console.log('🔑 Token:', token ? 'Var ✓' : 'YOK ✗');
            
            const response = await fetch(url, {
                method: 'POST',
                headers: headers
            });
            
            console.log('📥 Response status:', response.status);
            
            if (response.ok) {
                const ticket = await response.json();
                console.log('✅ Bilet satın alındı:', ticket);
                successCount++;
            } else {
                const errorText = await response.text();
                console.error('❌ Bilet satın alma hatası:', errorText);
                console.error('📄 Hata detayı - Status:', response.status, 'Message:', errorText);
                failCount++;
            }
        } catch (error) {
            console.error('💥 Network hatası:', error);
            failCount++;
        }
    }
    
    // Clear cart after checkout
    cart = [];
    saveCartToStorage();
    
    cartModal.hide();
    
    if (successCount > 0 && failCount === 0) {
        showNotification(`✅ ${successCount} bilet başarıyla satın alındı!`, 'success');
    } else if (successCount > 0 && failCount > 0) {
        showNotification(`⚠️ ${successCount} bilet alındı, ${failCount} bilet alınamadı.`, 'warning');
    } else {
        showNotification('❌ Bilet satın alma başarısız oldu.', 'danger');
    }
    
    // Reload events and show profile
    setTimeout(() => {
        loadEvents();
        showProfile(user);
    }, 500);
}

// Book a ticket (legacy - kept for compatibility)
async function bookTicket() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    
    if (!user) {
        showNotification('Lütfen önce giriş yapın.', 'warning');
        return;
    }
    
    if (!currentEvent) {
        showNotification('Etkinlik bilgisi bulunamadı.', 'danger');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/v1/tickets/book?eventId=${currentEvent.id}&buyerId=${user.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        
        if (response.ok) {
            const ticket = await response.json();
            showNotification('✅ Bilet başarıyla rezerve edildi!', 'success');
            eventModal.hide();
            setTimeout(() => {
                loadEvents();
                showProfile(user);
            }, 500);
        } else {
            showNotification('❌ Bilet rezervasyonu başarısız.', 'danger');
        }
    } catch (error) {
        console.error('Error booking ticket:', error);
        showNotification('Bağlantı hatası. Lütfen tekrar deneyin.', 'danger');
    }
}

// Show user profile
async function showProfile(user) {
    document.getElementById('profileUsername').textContent = user.username;
    document.getElementById('profileEmail').textContent = user.email;
    
    const tickets = await getUserTickets(user.id);
    const myTicketsList = document.getElementById('myTicketsList');
    
    if (!tickets || tickets.length === 0) {
        myTicketsList.innerHTML = `
            <div class="text-center py-4">
                <i class="bi bi-ticket-perforated text-muted" style="font-size: 3rem;"></i>
                <p class="text-muted mt-3">Henüz bilet satın almadınız.</p>
                <button class="btn btn-primary" onclick="profileModal.hide(); document.getElementById('eventsSection').scrollIntoView({behavior: 'smooth'})">
                    <i class="bi bi-search me-1"></i>Etkinlikleri Keşfet
                </button>
            </div>
        `;
    } else {
        myTicketsList.innerHTML = tickets.map(ticket => `
            <div class="ticket-card">
                <div class="row align-items-center">
                    <div class="col-md-7">
                        <div class="mb-2">
                            <span class="ticket-number">
                                <i class="bi bi-upc-scan me-1"></i>${ticket.ticketNumber}
                            </span>
                        </div>
                        <div class="mb-2">
                            <strong><i class="bi bi-calendar-event me-2"></i>Etkinlik:</strong> 
                            ${ticket.event?.title || 'Bilgi yok'}
                        </div>
                        <div>
                            <strong><i class="bi bi-cash me-2"></i>Fiyat:</strong> 
                            <span class="text-success fw-bold">₺${ticket.price.toLocaleString('tr-TR')}</span>
                        </div>
                    </div>
                    <div class="col-md-5 text-md-end mt-3 mt-md-0">
                        <div class="mb-2">
                            <strong><i class="bi bi-calendar3 me-2"></i>Tarih:</strong> 
                            ${new Date(ticket.purchaseDate).toLocaleDateString('tr-TR')}
                        </div>
                        <div class="mb-3">
                            <span class="ticket-status ${ticket.status.toLowerCase()}">
                                ${ticket.status === 'ACTIVE' 
                                    ? '<i class="bi bi-check-circle me-1"></i>Aktif' 
                                    : ticket.status === 'USED' 
                                        ? '<i class="bi bi-check2-all me-1"></i>Kullanıldı' 
                                        : '<i class="bi bi-x-circle me-1"></i>İptal Edildi'}
                            </span>
                        </div>
                        ${ticket.status === 'ACTIVE' ? `
                            <button class="btn btn-sm btn-outline-danger" onclick="cancelAndReload(${ticket.id})">
                                <i class="bi bi-x-lg me-1"></i>İptal Et
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `).join('');
    }
    
    profileModal.show();
}

// Cancel ticket and reload
async function cancelAndReload(ticketId) {
    await cancelTicket(ticketId);
    const user = JSON.parse(localStorage.getItem('currentUser'));
    showProfile(user);
    setTimeout(() => loadEvents(), 1000);
}

// Open auth modal
function openAuthModal() {
    showAuthForm('login');
    authModal.show();
}

// Show auth form (login or register)
function showAuthForm(form) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const authTitle = document.getElementById('authTitle');
    
    if (form === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        authTitle.innerHTML = '<i class="bi bi-box-arrow-in-right me-2"></i>Giriş Yap';
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        authTitle.innerHTML = '<i class="bi bi-person-plus me-2"></i>Kayıt Ol';
    }
}

// Show profile (from navigation)
function showProfileFromNav() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (user) {
        showProfile(user);
    }
}

// Logout
function logout() {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('cart');
    cart = [];
    checkUserSession();
    loadEvents();
    showNotification('Başarıyla çıkış yaptınız.', 'info');
}

// Clear filters
function clearFilters() {
    document.getElementById('filterCategory').value = '';
    document.getElementById('filterLocation').value = '';
    selectedCategory = '';
    
    // Reset category buttons
    document.querySelectorAll('.category-pills .btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector('.category-pills .btn').classList.add('active'); // First button (Tümü)
    
    loadEvents();
}

// Show notification toast
function showNotification(message, type = 'info') {
    // Create notification container if it doesn't exist
    let container = document.getElementById('notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            max-width: 400px;
        `;
        document.body.appendChild(container);
    }
    
    // Create notification
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show`;
    notification.style.cssText = `
        margin-bottom: 10px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        animation: slideInRight 0.3s ease-out;
    `;
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    container.appendChild(notification);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 150);
    }, 5000);
}

// Handle profile nav click
document.addEventListener('DOMContentLoaded', () => {
    const profileLink = document.querySelector('a[href="#profile"]');
    if (profileLink) {
        profileLink.addEventListener('click', (e) => {
            e.preventDefault();
            showProfileFromNav();
        });
    }
});
