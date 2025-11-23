CREATE TABLE passports
(
    id SERIAL PRIMARY KEY,
    series      VARCHAR(4) NOT NULL,
    number      VARCHAR(6) NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    UNIQUE (series, number)
);

CREATE TABLE clients
(
    id    SERIAL PRIMARY KEY,
    full_name    TEXT           NOT NULL,
    phone_number VARCHAR(12)    NOT NULL CHECK (phone_number LIKE '+7%'),
    email        TEXT           ,
    passport_id  INTEGER UNIQUE NOT NULL REFERENCES passports (id),
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE (phone_number),
    UNIQUE (email)
);

CREATE TABLE events
(
    id     SERIAL PRIMARY KEY,
    name         TEXT NOT NULL,
    date         DATE NOT NULL                                                                 DEFAULT CURRENT_DATE,
    number_of_seats INTEGER NOT NULL DEFAULT 1 CHECK (number_of_seats > 0),
    ticket_price NUMERIC NOT NULL                                                                       DEFAULT 0 CHECK (ticket_price >= 0),
    status       TEXT CHECK (status IN ('запланировано', 'проходит', 'отменено', 'завершено')) DEFAULT 'запланировано',
    description  TEXT,
    created_at   TIMESTAMP                                                                     DEFAULT NOW(),
    updated_at   TIMESTAMP                                                                     DEFAULT NOW(),
    UNIQUE (name, date)
);

CREATE TABLE ticket_reservations
(
    id    SERIAL PRIMARY KEY,
    client_id         INTEGER REFERENCES clients (id),
    event_id          INTEGER REFERENCES events (id),
    number_of_tickets INTEGER   DEFAULT 1 CHECK (number_of_tickets > 0),
    booking_status    TEXT CHECK (booking_status IN ('подтверждено', 'отменено', 'ожидает подтверждения'))  DEFAULT 'ожидает подтверждения',
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

--для тестов
-- CREATE INDEX idx_reservations_client_event ON ticket_reservations(client_id, event_id);
-- CREATE INDEX idx_events_date_status ON events(date, status);
-- CREATE INDEX idx_clients_phone ON clients(phone_number);