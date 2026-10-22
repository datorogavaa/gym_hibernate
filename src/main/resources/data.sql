-- 1. Training Types
INSERT INTO training_types (training_type_name) VALUES ('FITNESS');
INSERT INTO training_types (training_type_name) VALUES ('YOGA');
INSERT INTO training_types (training_type_name) VALUES ('CARDIO');
INSERT INTO training_types (training_type_name) VALUES ('STRENGTH');

-- 2. Users (ID 1: John, ID 2: Alice, ID 3: Bob, ID 4: Emma)
INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('John', 'Doe', 'john.doe', 'Pass12345!', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Alice', 'Smith', 'alice.smith', 'Pass12345!', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Bob', 'Taylor', 'bob.taylor', 'Pass12345!', true);

INSERT INTO users (first_name, last_name, username, password, is_active)
VALUES ('Emma', 'Brown', 'emma.brown', 'Pass12345!', true);

-- 3. Trainers (user_id 1 -> John, user_id 2 -> Alice)
INSERT INTO trainers (specialization_id, user_id) VALUES (1, 1);
INSERT INTO trainers (specialization_id, user_id) VALUES (2, 2);

-- 4. Trainees (user_id 3 -> Bob, user_id 4 -> Emma)
INSERT INTO trainees (user_id, date_of_birth, address)
VALUES (3, '1995-04-12', '123 Main St, New York');

INSERT INTO trainees (user_id, date_of_birth, address)
VALUES (4, '2000-08-25', '456 Elm St, Boston');

-- 5. Trainee-Trainer Many-to-Many Relationship
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (1, 1);
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (2, 2);

-- 6. Trainings
INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (1, 1, 'Morning HIIT', 1, '2026-10-01', 60);

INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (1, 1, 'Core Stability', 1, '2026-10-05', 45);

INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration)
VALUES (2, 2, 'Vinyasa Flow', 2, '2026-10-02', 90);