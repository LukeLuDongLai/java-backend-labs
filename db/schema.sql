CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL
        CHECK (unit_price >= 0),
    capacity INTEGER NOT NULL
        CHECK (capacity >= 0),
    available_tickets INTEGER NOT NULL
        CHECK (
            available_tickets >= 0
            AND available_tickets <= capacity
        ),
    status VARCHAR(30) NOT NULL
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,

    event_id UUID NOT NULL
        REFERENCES events(id),

    customer_email VARCHAR(320) NOT NULL,

    quantity INTEGER NOT NULL
        CHECK (quantity > 0),

    total_price NUMERIC(12, 2) NOT NULL
        CHECK (total_price >= 0),

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_events_category
    ON events(category);

CREATE INDEX idx_events_starts_at
    ON events(starts_at);

CREATE INDEX idx_bookings_event_id
    ON bookings(event_id);