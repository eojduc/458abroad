

delete from admins;
delete from students;
delete from programs;
delete from applications;

INSERT INTO admins (username, password, email, display_name)
VALUES
    ('admin', 'password123', 'admin@example.com', 'Administrator');

-- Populate students table
INSERT INTO students (username, password, email, display_name)
VALUES
    ('student1', 'password789', 'student1@example.com', 'Student One'),
('student2', 'password101', 'student2@example.com', 'Student Two');

-- Populate programs table
INSERT INTO programs (id, title, year, semester, application_open, application_close, start_date, end_date, faculty_lead, description)
VALUES
    (1, 'Software Engineering Abroad', 2025, 'FALL', '2025-01-01T08:00:00Z', '2025-04-30T23:59:59Z', '2025-09-01', '2025-12-15', 'Dr. Smith', 'A program focusing on software engineering practices abroad.'),
(2, 'Cultural Studies in Europe', 2025, 'SPRING', '2024-08-01T08:00:00Z', '2024-11-30T23:59:59Z', '2025-01-15', '2025-05-15', 'Dr. Johnson', 'Exploring European culture through immersive experiences.');

-- Populate applications table
INSERT INTO applications (id, student, program_id, date_of_birth, gpa, major, answer1, answer2, answer3, answer4, answer5, status)
VALUES
    ('1_student1', 'student1', 1, '2000-05-14', 3.8, 'Computer Science', 'Answer to question 1', 'Answer to question 2', 'Answer to question 3', 'Answer to question 4', 'Answer to question 5', 'APPLIED'),
('2_student2', 'student2', 2, '2001-09-21', 3.5, 'History', 'Answer to question 1', 'Answer to question 2', 'Answer to question 3', 'Answer to question 4', 'Answer to question 5', 'ENROLLED');
