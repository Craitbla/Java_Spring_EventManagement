const API_BASE = 'http://localhost:8080/api';

// Утилиты для работы с API
async function apiCall(url, options = {}) {
    try {
        const response = await fetch(`${API_BASE}${url}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        showMessage(`Ошибка: ${error.message}`);
        throw error;
    }
}

function showMessage(message) {
    document.getElementById('modalMessage').textContent = message;
    new bootstrap.Modal(document.getElementById('messageModal')).show();
}

// Клиенты
document.getElementById('clientForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const clientData = {
        fullName: document.getElementById('fullName').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        email: document.getElementById('email').value,
        passport: {
            series: document.getElementById('passportSeries').value,
            number: document.getElementById('passportNumber').value
        }
    };

    try {
        await apiCall('/clients', {
            method: 'POST',
            body: JSON.stringify(clientData)
        });

        showMessage('Клиент успешно создан!');
        document.getElementById('clientForm').reset();
        loadClients();
    } catch (error) {
        // Ошибка уже обработана в apiCall
    }
});

async function loadClients() {
    try {
        const clients = await apiCall('/clients');
        const container = document.getElementById('clientsList');

        container.innerHTML = clients.map(client => `
            <div class="list-group-item">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">${client.fullName}</h6>
                        <small class="text-muted">📞 ${client.phoneNumber} | ✉️ ${client.email}</small>
                    </div>
                    <div>
                        <button class="btn btn-danger btn-sm btn-action" onclick="deleteClient(${client.id})">Удалить</button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        // Ошибка уже обработана
    }
}

async function deleteClient(id) {
    if (confirm('Вы уверены, что хотите удалить клиента?')) {
        try {
            await apiCall(`/clients/${id}`, { method: 'DELETE' });
            showMessage('Клиент удален!');
            loadClients();
        } catch (error) {
            // Ошибка уже обработана
        }
    }
}

// Поиск клиентов
document.getElementById('searchClient').addEventListener('input', async (e) => {
    const searchTerm = e.target.value;
    if (searchTerm.length > 2) {
        try {
            const clients = await apiCall(`/clients/search?searchTerm=${encodeURIComponent(searchTerm)}`);
            const container = document.getElementById('clientsList');

            container.innerHTML = clients.map(client => `
                <div class="list-group-item">
                    <h6 class="mb-1">${client.fullName}</h6>
                    <small class="text-muted">📞 ${client.phoneNumber} | ✉️ ${client.email}</small>
                </div>
            `).join('');
        } catch (error) {
            // Ошибка уже обработана
        }
    } else if (searchTerm.length === 0) {
        loadClients();
    }
});

// Мероприятия
document.getElementById('eventForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const eventData = {
        name: document.getElementById('eventName').value,
        date: document.getElementById('eventDate').value,
        numberOfSeats: parseInt(document.getElementById('eventSeats').value),
        ticketPrice: parseFloat(document.getElementById('eventPrice').value),
        description: document.getElementById('eventDescription').value,
        status: 'PLANNED'
    };

    try {
        await apiCall('/events', {
            method: 'POST',
            body: JSON.stringify(eventData)
        });

        showMessage('Мероприятие успешно создано!');
        document.getElementById('eventForm').reset();
        loadEvents();
        loadEventsForReservation(); // Обновляем список для бронирований
    } catch (error) {
        // Ошибка уже обработана
    }
});

async function loadEvents() {
    try {
        const events = await apiCall('/events');
        const container = document.getElementById('eventsList');

        container.innerHTML = events.map(event => `
            <div class="list-group-item status-${event.status.toLowerCase()}">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">${event.name}</h6>
                        <small class="text-muted">
                            📅 ${new Date(event.date).toLocaleDateString()} | 
                            💺 ${event.numberOfSeats} мест | 
                            💰 ${event.ticketPrice} руб. |
                            📊 ${event.status}
                        </small>
                        ${event.description ? `<p class="mb-1 small">${event.description}</p>` : ''}
                    </div>
                    <div>
                        <button class="btn btn-danger btn-sm btn-action" onclick="deleteEvent(${event.id})">Удалить</button>
                        <button class="btn btn-info btn-sm btn-action" onclick="loadEventStatistics(${event.id})">Статистика</button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        // Ошибка уже обработана
    }
}

async function deleteEvent(id) {
    if (confirm('Вы уверены, что хотите удалить мероприятие?')) {
        try {
            await apiCall(`/events/${id}`, { method: 'DELETE' });
            showMessage('Мероприятие удалено!');
            loadEvents();
            loadEventsForReservation();
        } catch (error) {
            // Ошибка уже обработана
        }
    }
}

async function loadEventStatistics(eventId) {
    try {
        const stats = await apiCall(`/events/${eventId}/statistics`);
        showMessage(`
            Статистика мероприятия "${stats.name}":
            Подтвержденные билеты: ${stats.confirmedTickets}
            Общая выручка: ${stats.totalRevenue} руб.
            Статус: ${stats.status}
        `);
    } catch (error) {
        // Ошибка уже обработана
    }
}

// Бронирования
async function loadClientsForReservation() {
    try {
        const clients = await apiCall('/clients');
        const select = document.getElementById('reservationClient');

        select.innerHTML = '<option value="">Выберите клиента</option>' +
            clients.map(client => `<option value="${client.id}">${client.fullName} (${client.email})</option>`).join('');
    } catch (error) {
        // Ошибка уже обработана
    }
}

async function loadEventsForReservation() {
    try {
        const events = await apiCall('/events');
        const select = document.getElementById('reservationEvent');

        select.innerHTML = '<option value="">Выберите мероприятие</option>' +
            events.map(event => `<option value="${event.id}">${event.name} (${new Date(event.date).toLocaleDateString()})</option>`).join('');
    } catch (error) {
        // Ошибка уже обработана
    }
}

document.getElementById('reservationForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const reservationData = {
        clientId: parseInt(document.getElementById('reservationClient').value),
        eventId: parseInt(document.getElementById('reservationEvent').value),
        numberOfTickets: parseInt(document.getElementById('reservationTickets').value)
    };

    try {
        await apiCall('/ticketReservations', {
            method: 'POST',
            body: JSON.stringify(reservationData)
        });

        showMessage('Бронирование успешно создано!');
        document.getElementById('reservationForm').reset();
        loadReservations();
    } catch (error) {
        // Ошибка уже обработана
    }
});

async function loadReservations() {
    try {
        const reservations = await apiCall('/ticketReservations');
        const container = document.getElementById('reservationsList');

        container.innerHTML = reservations.map(reservation => `
            <div class="list-group-item booking-${reservation.bookingStatus.toLowerCase()}">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">Бронирование #${reservation.id}</h6>
                        <small class="text-muted">
                            👤 Клиент ID: ${reservation.client.id} | 
                            🎭 Мероприятие ID: ${reservation.event.id} | 
                            🎫 Билетов: ${reservation.numberOfTickets} |
                            📊 Статус: ${reservation.bookingStatus}
                        </small>
                    </div>
                    <div>
                        ${reservation.bookingStatus === 'PENDING_CONFIRMATION' ? `
                            <button class="btn btn-success btn-sm btn-action" onclick="confirmReservation(${reservation.id})">Подтвердить</button>
                            <button class="btn btn-warning btn-sm btn-action" onclick="cancelReservation(${reservation.id})">Отменить</button>
                        ` : ''}
                        ${reservation.bookingStatus === 'CONFIRMED' ? `
                            <button class="btn btn-warning btn-sm btn-action" onclick="cancelReservation(${reservation.id})">Отменить</button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        // Ошибка уже обработана
    }
}

async function confirmReservation(id) {
    try {
        await apiCall(`/ticketReservations/${id}/confirm`, { method: 'PUT' });
        showMessage('Бронирование подтверждено!');
        loadReservations();
    } catch (error) {
        // Ошибка уже обработана
    }
}

async function cancelReservation(id) {
    if (confirm('Вы уверены, что хотите отменить бронирование?')) {
        try {
            await apiCall(`/ticketReservations/${id}/cancel`, { method: 'PUT' });
            showMessage('Бронирование отменено!');
            loadReservations();
        } catch (error) {
            // Ошибка уже обработана
        }
    }
}

// Администрирование
async function cleanupReservations() {
    try {
        const result = await apiCall('/admin/cleanup/canceled-reservations', { method: 'POST' });
        document.getElementById('cleanupResult').innerHTML = `
            <div class="alert alert-info">
                ${result.message}
            </div>
        `;
    } catch (error) {
        // Ошибка уже обработана
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    loadClients();
    loadEvents();
    loadClientsForReservation();
    loadEventsForReservation();
    loadReservations();

    // Обновляем списки при переключении вкладок
    document.querySelectorAll('[data-bs-toggle="tab"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', function() {
            if (this.id === 'clients-tab') loadClients();
            if (this.id === 'events-tab') loadEvents();
            if (this.id === 'reservations-tab') loadReservations();
        });
    });
});