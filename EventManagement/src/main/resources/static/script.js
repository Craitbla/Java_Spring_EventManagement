// Простой фронт на чистом JS для работы со всеми REST-контроллерами.
// Базовый URL — тот же домен, где запущен Spring Boot.
const API_BASE = "";

function handleResponse(response, outputEl) {
    return response.text().then(text => {
        let data = null;
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (e) {
                // текстовый ответ, оставим как есть
            }
        }

        if (!response.ok) {
            let message = `HTTP ${response.status} ${response.statusText}`;
            if (data && typeof data === "object" && "error" in data && "message" in data) {
                // формат GlobalExceptionHandler.ErrorResponse
                message = `${data.error}: ${data.message}`;
            } else if (text) {
                message += "\n" + text;
            }
            outputEl.textContent = message;
            outputEl.classList.add("error");
            outputEl.classList.remove("success");
        } else {
            if (data == null || data === "") {
                outputEl.textContent = "Успешно. Тело ответа пустое.";
            } else {
                outputEl.textContent = JSON.stringify(data, null, 2);
            }
            outputEl.classList.add("success");
            outputEl.classList.remove("error");
        }
    }).catch(err => {
        outputEl.textContent = "Ошибка при разборе ответа: " + err.message;
        outputEl.classList.add("error");
        outputEl.classList.remove("success");
    });
}

function apiRequest(method, url, body, outputEl) {
    const options = { method };
    if (body !== undefined && body !== null) {
        options.headers = { "Content-Type": "application/json" };
        options.body = JSON.stringify(body);
    }

    outputEl.textContent = "Выполняем запрос...";
    outputEl.classList.remove("error", "success");

    fetch(API_BASE + url, options)
        .then(res => handleResponse(res, outputEl))
        .catch(err => {
            outputEl.textContent = "Ошибка сети: " + err.message;
            outputEl.classList.add("error");
            outputEl.classList.remove("success");
        });
}

document.addEventListener("DOMContentLoaded", () => {
    setupClientForms();
    setupEventForms();
    setupReservationForms();
});

// =============== КЛИЕНТЫ ===============

function setupClientForms() {
    const output = document.getElementById("clients-output");

    // Создать клиента
    const clientCreateForm = document.getElementById("client-create-form");
    const clientCreateFullName = document.getElementById("client-create-fullName");
    const clientCreatePhone = document.getElementById("client-create-phone");
    const clientCreateEmail = document.getElementById("client-create-email");
    const clientCreatePassportSeries = document.getElementById("client-create-passport-series");
    const clientCreatePassportNumber = document.getElementById("client-create-passport-number");

    clientCreateForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!clientCreateForm.reportValidity()) return;

        const payload = {
            fullName: clientCreateFullName.value.trim(),
            phoneNumber: clientCreatePhone.value.trim(),
            email: clientCreateEmail.value.trim() || null,
            passport: {
                series: clientCreatePassportSeries.value.trim(),
                number: clientCreatePassportNumber.value.trim()
            }
        };

        apiRequest("POST", "/api/clients", payload, output);
    });

    // Получить клиента по ID
    const clientGetForm = document.getElementById("client-get-form");
    const clientGetId = document.getElementById("client-get-id");

    clientGetForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!clientGetForm.reportValidity()) return;

        const id = Number(clientGetId.value);
        apiRequest("GET", `/api/clients/${id}`, null, output);
    });

    // Поиск клиентов
    const clientSearchForm = document.getElementById("client-search-form");
    const clientSearchTerm = document.getElementById("client-search-term");

    clientSearchForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!clientSearchForm.reportValidity()) return;

        const term = clientSearchTerm.value.trim();
        apiRequest("GET", `/api/clients/search?searchTerm=${encodeURIComponent(term)}`, null, output);
    });

    // Все клиенты
    const clientAllForm = document.getElementById("client-all-form");
    clientAllForm.addEventListener("submit", (e) => {
        e.preventDefault();
        apiRequest("GET", "/api/clients", null, output);
    });

    // Обновить основные данные клиента
    const clientUpdateForm = document.getElementById("client-update-form");
    const clientUpdateId = document.getElementById("client-update-id");
    const clientUpdateFullName = document.getElementById("client-update-fullName");
    const clientUpdatePhone = document.getElementById("client-update-phone");
    const clientUpdateEmail = document.getElementById("client-update-email");

    clientUpdateForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!clientUpdateForm.reportValidity()) return;

        const id = Number(clientUpdateId.value);
        const payload = {
            fullName: clientUpdateFullName.value.trim(),
            phoneNumber: clientUpdatePhone.value.trim(),
            email: clientUpdateEmail.value.trim() || null
        };

        apiRequest("PUT", `/api/clients/${id}`, payload, output);
    });

    // Заменить паспорт клиента
    const clientPassportForm = document.getElementById("client-passport-form");
    const clientPassportId = document.getElementById("client-passport-id");
    const clientPassportSeries = document.getElementById("client-passport-series");
    const clientPassportNumber = document.getElementById("client-passport-number");

    clientPassportForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!clientPassportForm.reportValidity()) return;

        const id = Number(clientPassportId.value);
        const payload = {
            series: clientPassportSeries.value.trim(),
            number: clientPassportNumber.value.trim()
        };

        apiRequest("PUT", `/api/clients/${id}/passport`, payload, output);
    });

    // Удалить клиента
    const clientDeleteForm = document.getElementById("client-delete-form");
    const clientDeleteId = document.getElementById("client-delete-id");

    clientDeleteForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!clientDeleteForm.reportValidity()) return;

        const id = Number(clientDeleteId.value);
        apiRequest("DELETE", `/api/clients/${id}`, null, output);
    });
}

// =============== МЕРОПРИЯТИЯ ===============

function setupEventForms() {
    const output = document.getElementById("events-output");

    // Создать мероприятие
    const eventCreateForm = document.getElementById("event-create-form");
    const eventCreateName = document.getElementById("event-create-name");
    const eventCreateDate = document.getElementById("event-create-date");
    const eventCreateSeats = document.getElementById("event-create-seats");
    const eventCreatePrice = document.getElementById("event-create-price");
    const eventCreateDescription = document.getElementById("event-create-description");

    eventCreateForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!eventCreateForm.reportValidity()) return;

        const payload = {
            name: eventCreateName.value.trim(),
            date: eventCreateDate.value, // yyyy-MM-dd (HTML date)
            numberOfSeats: Number(eventCreateSeats.value),
            ticketPrice: eventCreatePrice.value, // BigDecimal — строка тоже ок
            description: eventCreateDescription.value.trim() || null
        };

        apiRequest("POST", "/api/events", payload, output);
    });

    // Получить мероприятие по ID
    const eventGetForm = document.getElementById("event-get-form");
    const eventGetId = document.getElementById("event-get-id");

    eventGetForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!eventGetForm.reportValidity()) return;

        const id = Number(eventGetId.value);
        apiRequest("GET", `/api/events/${id}`, null, output);
    });

    // Все мероприятия
    const eventAllForm = document.getElementById("event-all-form");
    eventAllForm.addEventListener("submit", (e) => {
        e.preventDefault();
        apiRequest("GET", "/api/events", null, output);
    });

    // Обновить статус
    const eventStatusForm = document.getElementById("event-status-form");
    const eventStatusId = document.getElementById("event-status-id");
    const eventStatusSelect = document.getElementById("event-status-select");

    eventStatusForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!eventStatusForm.reportValidity()) return;

        const id = Number(eventStatusId.value);
        const status = eventStatusSelect.value;
        if (!status) {
            output.textContent = "Выберите статус мероприятия.";
            output.classList.add("error");
            output.classList.remove("success");
            return;
        }

        // Тело: строка Enum с русским значением ("запланировано" и т.д.)
        apiRequest("PUT", `/api/events/${id}/status`, status, output);
    });

    // Статистика по мероприятию
    const eventStatForm = document.getElementById("event-stat-form");
    const eventStatId = document.getElementById("event-stat-id");

    eventStatForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!eventStatForm.reportValidity()) return;

        const id = Number(eventStatId.value);
        apiRequest("GET", `/api/events/${id}/statistics`, null, output);
    });

    // Удалить мероприятие
    const eventDeleteForm = document.getElementById("event-delete-form");
    const eventDeleteId = document.getElementById("event-delete-id");

    eventDeleteForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!eventDeleteForm.reportValidity()) return;

        const id = Number(eventDeleteId.value);
        apiRequest("DELETE", `/api/events/${id}`, null, output);
    });
}

// =============== БРОНИРОВАНИЯ ===============

function setupReservationForms() {
    const output = document.getElementById("reservations-output");

    // Создать бронирование
    const reservationCreateForm = document.getElementById("reservation-create-form");
    const reservationCreateClientId = document.getElementById("reservation-create-clientId");
    const reservationCreateEventId = document.getElementById("reservation-create-eventId");
    const reservationCreateNum = document.getElementById("reservation-create-num");
    const reservationCreateStatus = document.getElementById("reservation-create-status");

    reservationCreateForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!reservationCreateForm.reportValidity()) return;

        const payload = {
            clientId: Number(reservationCreateClientId.value),
            eventId: Number(reservationCreateEventId.value),
            numberOfTickets: Number(reservationCreateNum.value)
        };

        const statusVal = reservationCreateStatus.value;
        if (statusVal) {
            payload.bookingStatus = statusVal; // строковое значение Enum ("подтверждено" и т.д.)
        }

        apiRequest("POST", "/api/ticketReservations", payload, output);
    });

    // Получить бронирование по ID
    const reservationGetForm = document.getElementById("reservation-get-form");
    const reservationGetId = document.getElementById("reservation-get-id");

    reservationGetForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!reservationGetForm.reportValidity()) return;

        const id = Number(reservationGetId.value);
        apiRequest("GET", `/api/ticketReservations/${id}`, null, output);
    });

    // Все бронирования
    const reservationAllForm = document.getElementById("reservation-all-form");
    reservationAllForm.addEventListener("submit", (e) => {
        e.preventDefault();
        apiRequest("GET", "/api/ticketReservations", null, output);
    });

    // Подтвердить бронирование
    const reservationConfirmForm = document.getElementById("reservation-confirm-form");
    const reservationConfirmId = document.getElementById("reservation-confirm-id");

    reservationConfirmForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!reservationConfirmForm.reportValidity()) return;

        const id = Number(reservationConfirmId.value);
        apiRequest("PUT", `/api/ticketReservations/${id}/confirm`, null, output);
    });

    // Отменить бронирование
    const reservationCancelForm = document.getElementById("reservation-cancel-form");
    const reservationCancelId = document.getElementById("reservation-cancel-id");

    reservationCancelForm.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!reservationCancelForm.reportValidity()) return;

        const id = Number(reservationCancelId.value);
        apiRequest("PUT", `/api/ticketReservations/${id}/cancel`, null, output);
    });

    // Очистка старых отмененных
    const reservationCleanupForm = document.getElementById("reservation-cleanup-form");
    reservationCleanupForm.addEventListener("submit", (e) => {
        e.preventDefault();
        apiRequest("POST", "/api/ticketReservations/cleanup/canceled-reservations", null, output);
    });
}
