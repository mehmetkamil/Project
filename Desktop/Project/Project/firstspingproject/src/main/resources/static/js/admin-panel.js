// Auto-login for admin user
let authToken = null;
let currentUser = null;

// Initialize admin panel
document.addEventListener('DOMContentLoaded', async () => {
    // Auto-login with admin credentials
    await autoLogin();
    
    // Setup navigation
    setupNavigation();
    
    // Load dashboard data
    loadDashboard();
    
    // Setup search functionality
    setupSearchFilters();
    
    // Auto-refresh data every 10 seconds
    setInterval(() => {
        const activeSection = document.querySelector('.content-section.active');
        if (activeSection) {
            const sectionId = activeSection.id.replace('-section', '');
            loadSectionData(sectionId);
        }
    }, 10000); // 10 saniyede bir yenile
});

// Auto-login function
async function autoLogin() {
    try {
        console.log('🔐 Attempting auto-login...');
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userName: 'admin',
                password: 'admin123'
            })
        });

        console.log('📥 Login response status:', response.status);

        if (response.ok) {
            const data = await response.json();
            console.log('📦 Login response data:', data);
            
            // Try different token field names
            authToken = data.accessToken || data.token || data.jwtToken;
            currentUser = data.userName || data.username || 'admin';
            
            console.log('🔑 Auth token:', authToken ? 'SET ✓' : 'MISSING ✗');
            console.log('👤 Current user:', currentUser);
            
            document.getElementById('admin-username').textContent = currentUser;
            console.log('✅ Auto-login successful');
            
            // Load dashboard immediately after login
            loadDashboard();
        } else {
            const errorData = await response.json();
            console.error('❌ Login failed:', errorData);
            showError('Auto-login failed: ' + (errorData.message || 'Please login manually'));
        }
    } catch (error) {
        console.error('💥 Auto-login error:', error);
        showError('Failed to connect to server: ' + error.message);
    }
}

// Setup navigation
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Update active nav item
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
            
            // Show corresponding section
            const section = item.getAttribute('data-section');
            showSection(section);
        });
    });
}

function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Show selected section
    const section = document.getElementById(`${sectionName}-section`);
    if (section) {
        section.classList.add('active');
    }
    
    // Update page title
    const titles = {
        'dashboard': 'Kontrol Paneli',
        'users': 'Kullanıcı Yönetimi',
        'events': 'Etkinlik Yönetimi',
        'tickets': 'Bilet Yönetimi',
        'payments': 'Ödeme Yönetimi',
        'security': 'Güvenlik Kayıtları',
        'settings': 'Sistem Ayarları'
    };
    document.getElementById('page-title').textContent = titles[sectionName] || 'Yönetim Paneli';
    
    // Load section data
    loadSectionData(sectionName);
}

function loadSectionData(section) {
    switch(section) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'users':
            loadUsers();
            break;
        case 'events':
            loadEvents();
            break;
        case 'tickets':
            loadTickets();
            break;
        case 'payments':
            loadPayments();
            break;
        case 'security':
            loadSecurityLogs();
            break;
        case 'settings':
            loadSettings();
            break;
    }
}

// Dashboard functions
async function loadDashboard() {
    try {
        // Load statistics
        await Promise.all([
            loadStatistics(),
            loadHealthStatus()
        ]);
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

async function loadStatistics() {
    try {
        console.log('📊 Loading statistics...');
        console.log('🔑 Using token:', authToken ? 'YES' : 'NO');
        
        // Load users count
        console.log('👥 Fetching users...');
        const usersResponse = await apiRequest('/api/admin-panel/users');
        console.log('👥 Users response:', usersResponse);
        if (usersResponse) {
            document.getElementById('total-users').textContent = usersResponse.length || 0;
        }
        
        // Load events count
        console.log('📅 Fetching events...');
        const eventsResponse = await apiRequest('/api/admin-panel/events/all');
        console.log('📅 Events response:', eventsResponse);
        if (eventsResponse) {
            document.getElementById('total-events').textContent = eventsResponse.length || 0;
        }
        
        // Load tickets count
        console.log('🎫 Fetching tickets...');
        const ticketsResponse = await apiRequest('/api/admin-panel/tickets');
        console.log('🎫 Tickets response:', ticketsResponse);
        if (ticketsResponse) {
            document.getElementById('total-tickets').textContent = ticketsResponse.length || 0;
        }
        
        // Calculate revenue
        console.log('💰 Fetching payments...');
        const paymentsResponse = await apiRequest('/api/admin-panel/payments');
        console.log('💰 Payments response:', paymentsResponse);
        if (paymentsResponse) {
            const revenue = paymentsResponse.reduce((sum, payment) => sum + (payment.amount || 0), 0);
            document.getElementById('total-revenue').textContent = `₺${revenue.toFixed(2)}`;
        }
        
        console.log('✅ Statistics loaded successfully');
    } catch (error) {
        console.error('❌ Error loading statistics:', error);
    }
}

async function loadHealthStatus() {
    try {
        const health = await apiRequest('/actuator/health');
        
        if (health) {
            document.getElementById('api-status').textContent = health.status === 'UP' ? '✅ Sağlıklı' : '❌ Çevrimdışı';
            document.getElementById('api-status').style.color = health.status === 'UP' ? 'var(--success-color)' : 'var(--danger-color)';
        }
        
        // Database status
        if (health && health.components && health.components.db) {
            const dbStatus = health.components.db.status === 'UP' ? '✅ Bağlı' : '❌ Bağlantı Yok';
            document.getElementById('db-status').textContent = dbStatus;
            document.getElementById('db-status').style.color = health.components.db.status === 'UP' ? 'var(--success-color)' : 'var(--danger-color)';
        }
        
        // Memory usage (mock data for now)
        document.getElementById('memory-usage').textContent = '45% (2.5GB / 8GB)';
    } catch (error) {
        console.error('Error loading health status:', error);
        document.getElementById('api-status').textContent = '❌ Hata';
    }
}

// Users management
async function loadUsers() {
    try {
        const users = await apiRequest('/api/admin-panel/users');
        const tbody = document.getElementById('users-table-body');
        
        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="loading">Kullanıcı bulunamadı</td></tr>';
            return;
        }
        
        tbody.innerHTML = users.map(user => `
            <tr>
                <td>${user.id}</td>
                <td>${user.userName || user.username}</td>
                <td>${user.email}</td>
                <td>${(user.roles || []).map(r => `<span class="status-badge status-active">${r.name}</span>`).join(' ')}</td>
                <td>
                    <button class="btn-primary" onclick="viewUser(${user.id})">View</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading users:', error);
        document.getElementById('users-table-body').innerHTML = 
            '<tr><td colspan="5" class="loading">Error loading users</td></tr>';
    }
}

// Events management
async function loadEvents() {
    try {
        const events = await apiRequest('/api/admin-panel/events/all');
        const tbody = document.getElementById('events-table-body');
        
        if (!events || events.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="loading">No events found</td></tr>';
            return;
        }
        
        tbody.innerHTML = events.map(event => `
            <tr>
                <td>${event.id}</td>
                <td>${event.title}</td>
                <td>${event.location}</td>
                <td>${new Date(event.startDateTime).toLocaleDateString()}</td>
                <td>${event.capacity}</td>
                <td>${event.availableSeats}</td>
                <td>₺${event.price}</td>
                <td><span class="status-badge status-active">Aktif</span></td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading events:', error);
        document.getElementById('events-table-body').innerHTML = 
            '<tr><td colspan="8" class="loading">Error loading events</td></tr>';
    }
}

// Tickets management
async function loadTickets() {
    try {
        const tickets = await apiRequest('/api/admin-panel/tickets');
        const tbody = document.getElementById('tickets-table-body');
        
        if (!tickets || tickets.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="loading">Bilet bulunamadı</td></tr>';
            return;
        }
        
        tbody.innerHTML = tickets.map(ticket => `
            <tr>
                <td>${ticket.ticketNumber}</td>
                <td>${ticket.eventTitle || 'N/A'}</td>
                <td>${ticket.buyerName || 'N/A'}</td>
                <td>${new Date(ticket.purchaseDate).toLocaleDateString()}</td>
                <td>$${ticket.price}</td>
                <td><span class="status-badge status-${ticket.status.toLowerCase()}">${ticket.status}</span></td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading tickets:', error);
        document.getElementById('tickets-table-body').innerHTML = 
            '<tr><td colspan="6" class="loading">Error loading tickets</td></tr>';
    }
}

// Payments management
async function loadPayments() {
    try {
        const payments = await apiRequest('/api/admin-panel/payments');
        const tbody = document.getElementById('payments-table-body');
        
        if (!payments || payments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="loading">No payments found</td></tr>';
            return;
        }
        
        tbody.innerHTML = payments.map(payment => `
            <tr>
                <td>${payment.transactionId}</td>
                <td>${payment.userName || 'N/A'}</td>
                <td>$${payment.amount}</td>
                <td>${payment.paymentMethod}</td>
                <td>${new Date(payment.paymentDate).toLocaleDateString()}</td>
                <td><span class="status-badge status-${payment.status.toLowerCase()}">${payment.status}</span></td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading payments:', error);
        document.getElementById('payments-table-body').innerHTML = 
            '<tr><td colspan="6" class="loading">Error loading payments</td></tr>';
    }
}

// Security logs
async function loadSecurityLogs() {
    try {
        const logs = await apiRequest('/api/admin-panel/security/logs');
        const tbody = document.getElementById('security-table-body');
        
        if (!logs || logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="loading">No security logs found</td></tr>';
            return;
        }
        
        tbody.innerHTML = logs.slice(0, 50).map(log => `
            <tr>
                <td>${new Date(log.timestamp).toLocaleString()}</td>
                <td>${log.eventType}</td>
                <td>${log.username || 'Anonymous'}</td>
                <td>${log.ipAddress}</td>
                <td><span class="status-badge ${log.successful ? 'status-active' : 'status-cancelled'}">${log.successful ? 'Success' : 'Failed'}</span></td>
                <td>${log.details || 'N/A'}</td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Error loading security logs:', error);
        document.getElementById('security-table-body').innerHTML = 
            '<tr><td colspan="6" class="loading">Error loading security logs</td></tr>';
    }
}

// Settings
function loadSettings() {
    // Load from environment or default values
    document.getElementById('active-profile').textContent = 'default';
    document.getElementById('server-port').textContent = '7777';
    document.getElementById('db-type').textContent = 'H2 (Development)';
    document.getElementById('db-connection-status').textContent = 'Connected';
}

// API request helper
async function apiRequest(url, options = {}) {
    try {
        console.log(`📡 API Request: ${url}`);
        
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (authToken) {
            headers['Authorization'] = `Bearer ${authToken}`;
            console.log('✓ Token added to request');
        } else {
            console.warn('⚠️ No auth token available!');
        }
        
        const response = await fetch(url, {
            ...options,
            headers
        });
        
        console.log(`📥 Response status: ${response.status}`);
        
        if (response.status === 401) {
            console.error('🔒 Unauthorized! Session expired.');
            showError('Session expired. Please login again.');
            window.location.href = '/';
            return null;
        }
        
        if (response.status === 403) {
            console.error('🚫 Forbidden! Access denied.');
            showError('Access denied. Admin role required.');
            return null;
        }
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error(`❌ HTTP error! status: ${response.status}, body:`, errorText);
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('✅ Data received:', data);
        return data;
    } catch (error) {
        console.error('💥 API Request failed:', error);
        showError('Failed to load data: ' + error.message);
        return null;
    }
}

// Search filters
function setupSearchFilters() {
    document.getElementById('user-search')?.addEventListener('input', (e) => {
        filterTable('users-table-body', e.target.value);
    });
    
    document.getElementById('event-search')?.addEventListener('input', (e) => {
        filterTable('events-table-body', e.target.value);
    });
}

function filterTable(tableId, searchTerm) {
    const tbody = document.getElementById(tableId);
    const rows = tbody.getElementsByTagName('tr');
    
    for (let row of rows) {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm.toLowerCase()) ? '' : 'none';
    }
}

// Utility functions
function refreshData() {
    const activeSection = document.querySelector('.nav-item.active');
    if (activeSection) {
        const section = activeSection.getAttribute('data-section');
        loadSectionData(section);
    }
}

function viewUser(userId) {
    alert(`View user details for ID: ${userId}`);
    // Implement user details modal
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        authToken = null;
        currentUser = null;
        window.location.href = '/';
    }
}

function showError(message) {
    alert(message);
    // Implement better error notification
}

// Export functions for HTML onclick handlers
window.loadUsers = loadUsers;
window.loadEvents = loadEvents;
window.loadTickets = loadTickets;
window.loadPayments = loadPayments;
window.loadSecurityLogs = loadSecurityLogs;
window.refreshData = refreshData;
window.viewUser = viewUser;
window.logout = logout;
