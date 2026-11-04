DROP TABLE IF EXISTS trainings CASCADE;
DROP TABLE IF EXISTS trainee_trainer CASCADE;
DROP TABLE IF EXISTS trainees CASCADE;
DROP TABLE IF EXISTS trainers CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS training_types CASCADE;

CREATE TABLE training_types (
                                id SERIAL PRIMARY KEY,
                                training_type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       is_active BOOLEAN NOT NULL DEFAULT TRUE,
                       role VARCHAR(50) NOT NULL DEFAULT 'USER',
                       failed_attempts INT NOT NULL DEFAULT 0,
                       account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
                       lock_time TIMESTAMP
);

CREATE TABLE trainers (
                          id SERIAL PRIMARY KEY,
                          specialization_id INT NOT NULL REFERENCES training_types(id),
                          user_id INT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE trainees (
                          id SERIAL PRIMARY KEY,
                          user_id INT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                          date_of_birth DATE,
                          address VARCHAR(255)
);

CREATE TABLE trainee_trainer (
                                 trainee_id INT NOT NULL REFERENCES trainees(id) ON DELETE CASCADE,
                                 trainer_id INT NOT NULL REFERENCES trainers(id) ON DELETE CASCADE,
                                 PRIMARY KEY (trainee_id, trainer_id)
);

CREATE TABLE trainings (
                           id SERIAL PRIMARY KEY,
                           trainee_id INT NOT NULL REFERENCES trainees(id) ON DELETE CASCADE,
                           trainer_id INT NOT NULL REFERENCES trainers(id) ON DELETE CASCADE,
                           training_name VARCHAR(100) NOT NULL,
                           training_type_id INT NOT NULL REFERENCES training_types(id),
                           training_date DATE NOT NULL,
                           training_duration INT NOT NULL
);

INSERT INTO training_types (id, training_type_name) VALUES
                                                        (1, 'FITNESS'),
                                                        (2, 'YOGA'),
                                                        (3, 'CARDIO'),
                                                        (4, 'STRENGTH');

INSERT INTO users (id, first_name, last_name, username, password, is_active, role)
VALUES
    (1, 'John', 'Doe', 'john.doe', '$argon2id$v=19$m=16384,t=2,p=1$kHfZs/kQ8ZetnLN0A+DrHg$5RIgfhadhaaRxWhprRybBW8iK/69KhDOAmIE1WWULW0', true, 'TRAINER'),
    (2, 'Alice', 'Smith', 'alice.smith', '$argon2id$v=19$m=16384,t=2,p=1$kHfZs/kQ8ZetnLN0A+DrHg$5RIgfhadhaaRxWhprRybBW8iK/69KhDOAmIE1WWULW0', true, 'TRAINER'),
    (3, 'Bob', 'Taylor', 'bob.taylor', '$argon2id$v=19$m=16384,t=2,p=1$kHfZs/kQ8ZetnLN0A+DrHg$5RIgfhadhaaRxWhprRybBW8iK/69KhDOAmIE1WWULW0', true, 'TRAINEE'),
    (4, 'Emma', 'Brown', 'emma.brown', '$argon2id$v=19$m=16384,t=2,p=1$kHfZs/kQ8ZetnLN0A+DrHg$5RIgfhadhaaRxWhprRybBW8iK/69KhDOAmIE1WWULW0', true, 'TRAINEE');

INSERT INTO trainers (id, specialization_id, user_id) VALUES
                                                          (1, 1, 1),
                                                          (2, 2, 2);

INSERT INTO trainees (id, user_id, date_of_birth, address) VALUES
                                                               (1, 3, '1995-04-12', '123 Main St, New York'),
                                                               (2, 4, '2000-08-25', '456 Elm St, Boston');

INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES
                                                         (1, 1),
                                                         (2, 2);

INSERT INTO trainings
(id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES
    (1, 1, 1, 'Morning HIIT', 1, '2026-10-01', 60),
    (2, 1, 1, 'Core Stability', 1, '2026-10-05', 45),
    (3, 2, 2, 'Vinyasa Flow', 2, '2026-10-02', 90);

SELECT setval(pg_get_serial_sequence('users', 'id'), 100, false);
SELECT setval(pg_get_serial_sequence('trainers', 'id'), 100, false);
SELECT setval(pg_get_serial_sequence('trainees', 'id'), 100, false);
SELECT setval(pg_get_serial_sequence('trainings', 'id'), 100, false);
SELECT setval(pg_get_serial_sequence('training_types', 'id'), 100, false);