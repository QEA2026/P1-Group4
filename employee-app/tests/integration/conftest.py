import sqlite3
import pytest


@pytest.fixture
def temp_database(tmp_path, mocker):

    database_path = tmp_path / "test_expense_manager.db"

    connection = sqlite3.connect(database_path)

    connection.execute("PRAGMA foreign_keys = ON")

    # Create the table to test

    connection.execute("""
        CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            role TEXT NOT NULL
        )
    """)

    connection.execute("""
        CREATE TABLE expenses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
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

    connection.execute("""
        CREATE TABLE approvals (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
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


    connection.execute(
        """
        INSERT INTO users (
            id,
            username,
            password,
            role
        )
        VALUES (?, ?, ?, ?)
        """,
        (
            1,
            "testname1",
            "newpassword",
            "employee"
        )
    )

    connection.execute(
        """
        INSERT INTO users (
            id,
            username,
            password,
            role
        )
        VALUES (?, ?, ?, ?)
        """,
        (
            2,
            "testname2",
            "newpassword",
            "employee"
        )
    )


    connection.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (?, ?, ?, ?, ?, ?)
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
    connection.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (?, ?, ?, ?, ?, ?)
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

    connection.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (?, ?, ?, ?, ?, ?)
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

    connection.execute(
        """
        INSERT INTO expenses (
            id,
            user_id,
            amount,
            description,
            date,
            category
        )
        VALUES (?, ?, ?, ?, ?, ?)
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

    connection.execute(
    """
    INSERT INTO approvals (
        id,
        expense_id,
        status,
        reviewer,
        comment,
        review_date
    )
    VALUES (?, ?, ?, ?, ?, ?)
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

    connection.execute(
        """
        INSERT INTO approvals (
            id,
            expense_id,
            status,
            reviewer,
            comment,
            review_date
        )
        VALUES (?, ?, ?, ?, ?, ?)
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

    connection.execute(
        """
        INSERT INTO approvals (
            id,
            expense_id,
            status,
            reviewer,
            comment,
            review_date
        )
        VALUES (?, ?, ?, ?, ?, ?)
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

    connection.execute(
        """
        INSERT INTO approvals (
            id,
            expense_id,
            status,
            reviewer,
            comment,
            review_date
        )
        VALUES (?, ?, ?, ?, ?, ?)
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

    connection.commit()
    connection.close()


    def get_test_connection():
        test_connection = sqlite3.connect(database_path)
        test_connection.execute("PRAGMA foreign_keys = ON")
        return test_connection

    # Patch both get_connection methods in expense and user dao
    mocker.patch(
        "dao.user_dao.get_connection",
        side_effect=get_test_connection
    )

    mocker.patch(
        "dao.expense_dao.get_connection",
        side_effect=get_test_connection
    )

    return database_path