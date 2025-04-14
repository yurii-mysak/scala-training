CREATE TABLE IF NOT EXISTS cars (
                                    id UUID PRIMARY KEY,
                                    make VARCHAR NOT NULL,
                                    model VARCHAR NOT NULL,
                                    year INTEGER NOT NULL,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS maintenances (
                                            id UUID PRIMARY KEY,
                                            car_id UUID NOT NULL REFERENCES cars(id),
    description TEXT NOT NULL,
    maintenance_types UUID[] NOT NULL,
    scheduled_date TIMESTAMP,
    status VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
    );