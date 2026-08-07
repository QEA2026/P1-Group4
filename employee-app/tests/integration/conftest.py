import psycopg2
import pytest
import os


@pytest.fixture
def temp_database(mocker):

    connection = psycopg2.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=os.getenv("DB_PORT", "5432"),
        dbname=os.getenv("DB_NAME", "expense_manager"),
        user=os.getenv("DB_USER", "postgres"),
        password=os.getenv("DB_PASSWORD", "newPostgresqlUser26")
    )
    

    cursor = connection.cursor()

    cursor.execute("""
        DROP TABLE IF EXISTS approvals, expenses, users CASCADE
    """)

    # Create the table to test

    cursor.execute("""
        CREATE TABLE users (
            id SERIAL PRIMARY KEY,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            role TEXT NOT NULL
        )
    """)

    cursor.execute("""
        CREATE TABLE expenses (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL,
            amount REAL NOT NULL,
            description TEXT NOT NULL,
            date TEXT NOT NULL,
            category TEXT,

            FOREIGN KEY (user_id)
                REFERENCES users(id)
                ON DELETE CASCADE
        )
    """)

    cursor.execute("""
        CREATE TABLE approvals (
            id SERIAL PRIMARY KEY,
            expense_id INTEGER UNIQUE NOT NULL,
            status TEXT NOT NULL DEFAULT 'pending',
            reviewer INTEGER,
            comment TEXT,
            review_date TEXT,

            FOREIGN KEY (expense_id)
                REFERENCES expenses(id)
                ON DELETE CASCADE
        )
    """)


    cursor.execute(
        """
        INSERT INTO users (
            id,
            username,
            password,
            role
        )
        VALUES (%s, %s, %s, %s)
        """,
        (
            1,
            "testname1",
            "newpassword",
            "employee"
        )
    )

    cursor.execute(
        """
        INSERT INTO users (
            id,
            username,
            password,
            role
        )
        VALUES (%s, %s, %s, %s)
        """,
        (
            2,
            "testname2",
            "newpassword",
            "employee"
        )
    )


    cursor.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            1,
            1,
            20,
            "Lunch",
            "2026-07-24",
            "Food"
        )
    )
    cursor.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            2,
            1,
            200,
            "Plane tickets",
            "2026-07-23",
            "Travel"
        )
    )

    cursor.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            3,
            2,
            20,
            "Uber",
            "2026-07-24",
            "transportation"
        )
    )

    cursor.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            4,
            2,
            200,
            "Office supplies",
            "2026-07-23",
            "Supplies"
        )
    )

    cursor.execute(
    """
    INSERT INTO approvals (
        id,
        expense_id,
        status,
        reviewer,
        comment,
        review_date
    )
    VALUES (%s, %s, %s, %s, %s, %s)
    """,
    (
        1,
        1,
        "pending",
        None,
        None,
        None
    )
)

    cursor.execute(
        """
        INSERT INTO approvals (
            id,
            expense_id,
            status,
            reviewer,
            comment,
            review_date
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            2,
            2,
            "approved",
            10,
            "Approved for business travel",
            "2026-07-24"
        )
    )

    cursor.execute(
        """
        INSERT INTO approvals (
            id,
            expense_id,
            status,
            reviewer,
            comment,
            review_date
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            3,
            3,
            "denied",
            10,
            "Transportation was not eligible",
            "2026-07-24"
        )
    )

    cursor.execute(
        """
        INSERT INTO approvals (
            id,
            expense_id,
            status,
            reviewer,
            comment,
            review_date
        )
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            4,
            4,
            "pending",
            None,
            None,
            None
        )
    )
    cursor.execute("SELECT * FROM users")
    print(cursor.fetchall())

    cursor.execute("""
        SELECT setval('expenses_id_seq', (SELECT MAX(id) FROM expenses));
    """)

    cursor.execute("""
        SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
    """)

    cursor.execute("""
        SELECT setval('approvals_id_seq', (SELECT MAX(id) FROM approvals));
    """)

    connection.commit()
    connection.close()


    def get_test_connection():
        return psycopg2.connect(
            host=os.getenv("DB_HOST", "localhost"),
            port=os.getenv("DB_PORT", "5432"),
            dbname=os.getenv("DB_NAME", "expense_manager"),
            user=os.getenv("DB_USER", "postgres"),
            password=os.getenv("DB_PASSWORD", "newPostgresqlUser26")
        )

    # Patch both get_connection methods in expense and user dao
    mocker.patch(
        "dao.user_dao.get_connection",
        side_effect=get_test_connection
    )

    mocker.patch(
        "dao.expense_dao.get_connection",
        side_effect=get_test_connection
    )

    yield

    connection = get_test_connection()
    print(os.getenv("DB_NAME", "expense_manager"))
    cursor = connection.cursor()

    cursor.execute("""
        TRUNCATE TABLE approvals, expenses, users
        RESTART IDENTITY CASCADE
    """)

    connection.commit()
    cursor.close()
    connection.close()