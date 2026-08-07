DROP TABLE IF EXISTS approvals CASCADE;
DROP TABLE IF EXISTS expenses CASCADE;
DROP TABLE IF EXISTS users CASCADE;


CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username TEXT NOT NULL UNIQUE,
                       password TEXT NOT NULL,
                       role TEXT NOT NULL
);


CREATE TABLE expenses (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER NOT NULL,
                          amount REAL NOT NULL,
                          description TEXT NOT NULL,
                          date TEXT NOT NULL,
                          category TEXT,

                          FOREIGN KEY(user_id)
                              REFERENCES users(id)
                              ON DELETE CASCADE
);


CREATE TABLE approvals (
                           id SERIAL PRIMARY KEY,
                           expense_id INTEGER NOT NULL,
                           status TEXT NOT NULL,
                           reviewer INTEGER,
                           comment TEXT,
                           review_date TEXT,

                           FOREIGN KEY(expense_id)
                               REFERENCES expenses(id)
                               ON DELETE CASCADE
);

-- ==========================================
-- USER SEED DATA
-- Replace password values with your bcrypt hashes
-- ==========================================

INSERT INTO users (username, password, role)
VALUES
    ('marco', '$2b$12$60sjao5luPt.RgwhoIKSceq8dBqOsZkJd3rm3di/7nGHqQynGzt7m', 'employee'),
    ('bob', '$2b$12$MreaDe.pg57Q1OXHaG/ah.a.8wbXBPqg1ITHF0dDQKgbdcICUWd4C', 'employee'),
    ('vanessa', '$2b$12$8yGwrjrDogBQBr4akdl1zenxvj4plVQ6ssuXmsnv8Dy8s23rAGLW2', 'manager'),
    ('testmanager', '$2b$12$if4fWe4RhH4.msdbLDf9eeSlCIUR1J.dDYhaiTPQJOHnyFejRkkyS', 'manager'),
    ('manager', '$2b$12$Qw.KcwMSmQKi0ZrROUdiwe3tMfNLRPSJ74T8PX/HA64rJA6K0BO/a', 'manager')
    ON CONFLICT (username) DO NOTHING;


-- ==========================================
-- INITIAL EXPENSE DATA
-- ==========================================

INSERT INTO expenses (user_id, amount, category, description, date)
VALUES
    (
        (SELECT id FROM users WHERE username = 'marco'),
        135.42,
        'travel',
        'Airport rideshare to client site',
        '2026-06-01'
    ),
    (
        (SELECT id FROM users WHERE username = 'marco'),
        82.19,
        'meals',
        'Team lunch during sprint planning',
        '2026-06-03'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        46.77,
        'office',
        'Replacement keyboard for workstation',
        '2026-06-02'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        312.50,
        'lodging',
        'Hotel for training travel',
        '2026-06-04'
    );


-- ==========================================
-- INITIAL APPROVAL DATA
-- ==========================================

INSERT INTO approvals (expense_id, status, reviewer, comment, review_date)
VALUES
    (
        (SELECT id FROM expenses WHERE description = 'Airport rideshare to client site'),
        'approved',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Approved for client travel reimbursement.',
        '2026-06-02'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Team lunch during sprint planning'),
        'pending',
        NULL,
        NULL,
        NULL
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Replacement keyboard for workstation'),
        'denied',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Please attach the original approval request before resubmitting.',
        '2026-06-03'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Hotel for training travel'),
        'pending',
        NULL,
        NULL,
        NULL
    );


-- ==========================================
-- ADDITIONAL EXPENSE TEST DATA
-- ==========================================

INSERT INTO expenses (user_id, amount, category, description, date)
VALUES
    (
        (SELECT id FROM users WHERE username = 'marco'),
        19.99,
        'office',
        'USB-C cable for docking station',
        '2026-06-05'
    ),
    (
        (SELECT id FROM users WHERE username = 'marco'),
        240.00,
        'travel',
        'Round-trip train ticket to regional office',
        '2026-06-07'
    ),
    (
        (SELECT id FROM users WHERE username = 'marco'),
        58.30,
        'meals',
        'Client dinner after demo',
        '2026-06-10'
    ),
    (
        (SELECT id FROM users WHERE username = 'marco'),
        1200.00,
        'lodging',
        'Conference hotel stay (3 nights)',
        '2026-06-12'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        75.00,
        'travel',
        'Parking at downtown client garage',
        '2026-06-06'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        14.25,
        'meals',
        'Coffee run for onboarding session',
        '2026-06-08'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        499.99,
        'office',
        'Standing desk converter',
        '2026-06-09'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        6500.00,
        'travel',
        'International flight for vendor summit',
        '2026-06-11'
    ),
    (
        (SELECT id FROM users WHERE username = 'marco'),
        33.10,
        'meals',
        'Working lunch during code review',
        '2026-06-13'
    ),
    (
        (SELECT id FROM users WHERE username = 'bob'),
        128.40,
        'office',
        'Ergonomic chair cushion set',
        '2026-06-14'
    );


-- ==========================================
-- ADDITIONAL APPROVAL DATA
-- ==========================================

INSERT INTO approvals (expense_id, status, reviewer, comment, review_date)
VALUES
    (
        (SELECT id FROM expenses WHERE description = 'USB-C cable for docking station'),
        'approved',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Minor office supply, approved.',
        '2026-06-06'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Round-trip train ticket to regional office'),
        'approved',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Approved for regional office visit.',
        '2026-06-08'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Client dinner after demo'),
        'pending',
        NULL,
        NULL,
        NULL
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Conference hotel stay (3 nights)'),
        'denied',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Exceeds lodging cap; please rebook within policy.',
        '2026-06-13'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Parking at downtown client garage'),
        'approved',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Approved, client visit confirmed.',
        '2026-06-07'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Coffee run for onboarding session'),
        'pending',
        NULL,
        NULL,
        NULL
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Standing desk converter'),
        'pending',
        NULL,
        NULL,
        NULL
    ),
    (
        (SELECT id FROM expenses WHERE description = 'International flight for vendor summit'),
        'denied',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Needs VP sign-off before booking international travel.',
        '2026-06-12'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Working lunch during code review'),
        'approved',
        (SELECT id FROM users WHERE username = 'vanessa'),
        'Approved.',
        '2026-06-14'
    ),
    (
        (SELECT id FROM expenses WHERE description = 'Ergonomic chair cushion set'),
        'pending',
        NULL,
        NULL,
        NULL
    );