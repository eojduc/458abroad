-- Create the responses table
CREATE TABLE responses (
                           application_id VARCHAR(255) NOT NULL,
                           question VARCHAR(255) NOT NULL,
                           response VARCHAR(10000) NOT NULL,
                           PRIMARY KEY (application_id, question),
                           FOREIGN KEY (application_id) REFERENCES applications(id)
);

-- Migrate existing answers to the responses table
INSERT INTO responses (application_id, question, response)
SELECT id, 'WHY_THIS_PROGRAM', answer1 FROM applications WHERE answer1 IS NOT NULL;

INSERT INTO responses (application_id, question, response)
SELECT id, 'ALIGN_WITH_CAREER', answer2 FROM applications WHERE answer2 IS NOT NULL;

INSERT INTO responses (application_id, question, response)
SELECT id, 'ANTICIPATED_CHALLENGES', answer3 FROM applications WHERE answer3 IS NOT NULL;

INSERT INTO responses (application_id, question, response)
SELECT id, 'ADAPTED_TO_ENVIRONMENT', answer4 FROM applications WHERE answer4 IS NOT NULL;

INSERT INTO responses (application_id, question, response)
SELECT id, 'UNIQUE_PERSPECTIVE', answer5 FROM applications WHERE answer5 IS NOT NULL;

-- Drop the old columns
ALTER TABLE applications
    DROP COLUMN answer1,
    DROP COLUMN answer2,
    DROP COLUMN answer3,
    DROP COLUMN answer4,
    DROP COLUMN answer5;