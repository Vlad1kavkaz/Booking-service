CREATE TABLE rooms (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       has_conditioner BOOLEAN NOT NULL,
                       has_projector BOOLEAN NOT NULL,
                       has_fridge BOOLEAN NOT NULL,
                       has_balcony BOOLEAN NOT NULL,
                       capacity INT NOT NULL CHECK (capacity > 0)
);
