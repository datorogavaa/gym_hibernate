
INSERT INTO training_types (id, training_type_name) VALUES (1, 'FITNESS');
INSERT INTO training_types (id, training_type_name) VALUES (2, 'YOGA');
INSERT INTO training_types (id, training_type_name) VALUES (3, 'CARDIO');
INSERT INTO training_types (id, training_type_name) VALUES (4, 'STRENGTH');

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (1, 'John', 'Doe', 'john.doe', 'Pass12345!', true);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (2, 'Alice', 'Smith', 'alice.smith', 'Pass12345!', true);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (3, 'Bob', 'Taylor', 'bob.taylor', 'Pass12345!', true);

INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (4, 'Emma', 'Brown', 'emma.brown', 'Pass12345!', true);

INSERT INTO trainers (id, specialization_id, user_id) VALUES (1, 1, 1);
INSERT INTO trainers (id, specialization_id, user_id) VALUES (2, 2, 2);

-- 4. Trainees (id, user_id, date_of_birth, address)
INSERT INTO trainees (id, user_id, date_of_birth, address)
VALUES (1, 3, '1995-04-12', '123 Main St, New York');

INSERT INTO trainees (id, user_id, date_of_birth, address)
VALUES (2, 4, '2000-08-25', '456 Elm St, Boston');

INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (1, 1);

INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (2, 2);

INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (1, 1, 1, 'Morning HIIT', 1, '2026-10-01', 60);

INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (2, 1, 1, 'Core Stability', 1, '2026-10-05', 45);

INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (3, 2, 2, 'Vinyasa Flow', 2, '2026-10-02', 90);