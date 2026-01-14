// API Configuration
const API_BASE = 'http://localhost:7777/api';

// Helper function to show notifications
function showNotification(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.insertBefore(alertDiv, document.body.firstChild);
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Helper function to get authorization headers
function getAuthHeaders() {
    const token = localStorage.getItem('jwtToken');
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

// API Calls

// User API
async function registerUser(username, email, password, roles = ['USER']) {
    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password, roles })
        });
        
        if (response.ok) {
            const data = await response.json();
            // Save JWT token
            localStorage.setItem('jwtToken', data.token);
            localStorage.setItem('currentUser', JSON.stringify({ 
                id: data.id,
                username: data.username, 
                email: data.email, 
                roles: data.roles 
            }));
            showNotification('Kayıt başarılı!', 'success');
            return data;
        } else {
            const error = await response.text();
            showNotification('Kayıt başarısız: ' + error, 'danger');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showNotification('Bağlantı hatası. Lütfen tekrar deneyin.', 'danger');
    }
}

async function loginUser(username, password) {
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            // Save JWT token
            localStorage.setItem('jwtToken', data.token);
            localStorage.setItem('currentUser', JSON.stringify({ 
                id: data.id,
                username: data.username, 
                email: data.email, 
                roles: data.roles 
            }));
            showNotification(`Hoşgeldiniz, ${data.username}!`, 'success');
            return data;
        } else {
            showNotification('Kullanıcı adı veya şifre hatalı.', 'danger');
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification('Bağlantı hatası. Lütfen tekrar deneyin.', 'danger');
    }
}

async function getUserById(userId) {
    try {
        const response = await fetch(`${API_BASE}/users/${userId}`);
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Get user error:', error);
    }
}

// Event API
async function getAllEvents() {
    try {
        console.log('📡 Fetching events from:', `${API_BASE}/v1/events`);
        const response = await fetch(`${API_BASE}/v1/events`);
        console.log('📥 Events response status:', response.status);
        
        if (response.ok) {
            const data = await response.json();
            console.log('✅ Events loaded:', data.length, 'events');
            return data;
        } else {
            console.error('❌ Failed to get events:', response.status);
            return [];
        }
    } catch (error) {
        console.error('💥 Get events error:', error);
        return [];
    }
}

async function getEventsByCategory(category) {
    try {
        const response = await fetch(`${API_BASE}/v1/events/category/${category}`);
        if (response.ok) {
            return await response.json();
        } else {
            console.error('Failed to get events by category:', response.status);
            return [];
        }
    } catch (error) {
        console.error('Get events by category error:', error);
        return [];
    }
}

async function getEventsByLocation(location) {
    try {
        const response = await fetch(`${API_BASE}/v1/events/location/${location}`);
        if (response.ok) {
            return await response.json();
        } else {
            console.error('Failed to get events by location:', response.status);
            return [];
        }
    } catch (error) {
        console.error('Get events by location error:', error);
        return [];
    }
}

async function getEventById(eventId) {
    try {
        const response = await fetch(`${API_BASE}/v1/events/${eventId}`);
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Get event error:', error);
    }
}

// Ticket API
async function bookTicket(eventId, buyerId) {
    try {
        const response = await fetch(`${API_BASE}/v1/tickets/book?eventId=${eventId}&buyerId=${buyerId}`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            const ticket = await response.json();
            showNotification('✅ Bilet başarıyla rezerve edildi!', 'success');
            return ticket;
        } else if (response.status === 400) {
            showNotification('❌ Yeterli bilet yok veya etkinlik geçmiş.', 'danger');
        } else {
            showNotification('Bilet rezervasyonu başarısız.', 'danger');
        }
    } catch (error) {
        console.error('Book ticket error:', error);
        showNotification('Bağlantı hatası. Lütfen tekrar deneyin.', 'danger');
    }
}

async function getUserTickets(userId) {
    try {
        const response = await fetch(`${API_BASE}/v1/tickets/user/${userId}`, {
            headers: getAuthHeaders()
        });
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Get user tickets error:', error);
        return [];
    }
}

async function getUserActiveTickets(userId) {
    try {
        const response = await fetch(`${API_BASE}/v1/tickets/user/${userId}/active`, {
            headers: getAuthHeaders()
        });
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Get user active tickets error:', error);
        return [];
    }
}

async function cancelTicket(ticketId) {
    try {
        const response = await fetch(`${API_BASE}/v1/tickets/${ticketId}/cancel`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showNotification('✅ Bilet iptal edildi!', 'success');
            return true;
        } else {
            showNotification('Bilet iptali başarısız.', 'danger');
        }
    } catch (error) {
        console.error('Cancel ticket error:', error);
        showNotification('Bağlantı hatası. Lütfen tekrar deneyin.', 'danger');
    }
}

async function getTicketById(ticketId) {
    try {
        const response = await fetch(`${API_BASE}/v1/tickets/${ticketId}`, {
            headers: getAuthHeaders()
        });
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Get ticket error:', error);
    }
}
