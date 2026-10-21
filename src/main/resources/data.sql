-- 1. Training Types
MERGE INTO training_types (id, training_type_name) KEY(id) VALUES (1, 'FITNESS');
MERGE INTO training_types (id, training_type_name) KEY(id) VALUES (2, 'YOGA');
MERGE INTO training_types (id, training_type_name) KEY(id) VALUES (3, 'CARDIO');
MERGE INTO training_types (id, training_type_name) KEY(id) VALUES (4, 'STRENGTH');

-- 2. Users
MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY(id)
    VALUES (1, 'John', 'Doe', 'john.doe', 'Pass12345!', true);

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY(id)
    VALUES (2, 'Alice', 'Smith', 'alice.smith', 'Pass12345!', true);

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY(id)
    VALUES (3, 'Bob', 'Taylor', 'bob.taylor', 'Pass12345!', true);

MERGE INTO users (id, first_name, last_name, username, password, is_active) KEY(id)
    VALUES (4, 'Emma', 'Brown', 'emma.brown', 'Pass12345!', true);

-- 3. Trainers
MERGE INTO trainers (id, specialization_id, user_id) KEY(id) VALUES (1, 1, 1);
MERGE INTO trainers (id, specialization_id, user_id) KEY(id) VALUES (2, 2, 2);

-- 4. Trainees
MERGE INTO trainees (id, user_id, date_of_birth, address) KEY(id)
    VALUES (1, 3, '1995-04-12', '123 Main St, New York');

MERGE INTO trainees (id, user_id, date_of_birth, address) KEY(id)
    VALUES (2, 4, '2000-08-25', '456 Elm St, Boston');

-- 5. Trainee-Trainer Many-to-Many Relationship
MERGE INTO trainee_trainer (trainee_id, trainer_id) KEY(trainee_id, trainer_id) VALUES (1, 1);
MERGE INTO trainee_trainer (trainee_id, trainer_id) KEY(trainee_id, trainer_id) VALUES (2, 2);

-- 6. Trainings
MERGE INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) KEY(id)
    VALUES (1, 1, 1, 'Morning HIIT', 1, '2026-10-01', 60);

MERGE INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) KEY(id)
    VALUES (2, 1, 1, 'Core Stability', 1, '2026-10-05', 45);

MERGE INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) KEY(id)
    VALUES (3, 2, 2, 'Vinyasa Flow', 2, '2026-10-02', 90);