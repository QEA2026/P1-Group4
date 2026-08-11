from dao.expense_dao import (
    submit_new_expense_dao,
    get_expenses_dao,
    get_expense_by_status,
    get_expense_history_dao,
    edit_expense_dao,
    delete_expense_dao
)
import datetime

class MockDate(datetime.date):
    @classmethod
    def today(cls):
        return cls(2026, 7, 26)


# submit_new_expense_dao

def test_submit_new_expense_returns_expense_id(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = (5,)

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    mocker.patch(
    "dao.expense_dao.datetime.date",
    MockDate
    )

    result = submit_new_expense_dao(
        user_id=1,
        amount=75.50,
        description="Team breakfast",
        category="Food"
    )

    assert result == 5

    mock_connection.cursor.assert_called_once()

    assert mock_cursor.execute.call_count == 2

    expense_insert_call = mock_cursor.execute.call_args_list[0]

    assert "INSERT INTO expenses" in expense_insert_call.args[0]
    assert "RETURNING id" in expense_insert_call.args[0]

    assert expense_insert_call.args[1] == (
        1,
        75.50,
        "Team breakfast",
        "2026-07-26",
        "Food"
    )

    mock_cursor.execute.assert_any_call(
        " INSERT INTO approvals (expense_id, status) values (%s,%s)",
        (
            5,
            "pending"
        )
    )

    mock_connection.commit.assert_called_once()
    mock_connection.close.assert_called_once()


def test_submit_new_expense_returns_none_when_insert_fails(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = submit_new_expense_dao(
        user_id=1,
        amount=75.50,
        description="Team breakfast",
        category="Food"
    )

    assert result is None

    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()


# get_expenses_dao

def test_get_expenses_returns_expenses(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    expected_result = [
        (
            2,
            200.0,
            "Plane tickets",
            "2026-07-23",
            "approved",
            "Travel",
            "Approved for client travel"
        ),
        (
            1,
            20.0,
            "Lunch",
            "2026-07-24",
            "pending",
            "Food",
            None
        )
    ]

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchall.return_value = expected_result

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expenses_dao(1)

    assert result == expected_result

    mock_connection.cursor.assert_called_once()

    mock_cursor.execute.assert_called_once_with(
        """
            SELECT expenses.id,
                   expenses.amount,
                   expenses.description,
                   expenses.date,
                   approvals.status,
                   expenses.category,
                   approvals.comment
            FROM expenses
            JOIN approvals ON approvals.expense_id = expenses.id
            WHERE expenses.user_id = %s
            ORDER BY expenses.date
        """,
        (1,)
    )

    mock_cursor.fetchall.assert_called_once()
    mock_connection.close.assert_called_once()


def test_get_expenses_returns_empty_list(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchall.return_value = []

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expenses_dao(999)

    assert result == []

    mock_cursor.fetchall.assert_called_once()
    mock_connection.close.assert_called_once()


def test_get_expenses_returns_none_when_database_error_occurs(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expenses_dao(1)

    assert result is None

    mock_cursor.fetchall.assert_not_called()
    mock_connection.close.assert_called_once()


# get_expense_by_status

def test_get_expense_by_status_returns_matching_expenses(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    expected_result = [
        (
            1,
            20.0,
            "Lunch",
            "2026-07-24",
            "pending",
            "Food",
            None
        )
    ]

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchall.return_value = expected_result

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expense_by_status(1, "pending")

    assert result == expected_result

    mock_cursor.execute.assert_called_once_with(
        """
            SELECT expenses.id,
                   expenses.amount,
                   expenses.description,
                   expenses.date,
                   approvals.status,
                   expenses.category,
                   approvals.comment
            FROM expenses
            JOIN approvals ON approvals.expense_id = expenses.id
            WHERE expenses.user_id = %s
            AND approvals.status = %s
            ORDER BY expenses.date
        """,
        (
            1,
            "pending"
        )
    )

    mock_cursor.fetchall.assert_called_once()
    mock_connection.close.assert_called_once()


def test_get_expense_by_status_returns_empty_list(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchall.return_value = []

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expense_by_status(1, "denied")

    assert result == []

    mock_cursor.fetchall.assert_called_once()
    mock_connection.close.assert_called_once()


def test_get_expense_by_status_returns_none_when_database_error_occurs(
    mocker
):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expense_by_status(1, "pending")

    assert result is None

    mock_cursor.fetchall.assert_not_called()
    mock_connection.close.assert_called_once()


# get_expense_history_dao

def test_get_expense_history_returns_completed_expenses(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    expected_result = [
        (
            2,
            200.0,
            "Plane tickets",
            "2026-07-23",
            "approved",
            "Travel",
            "Approved for client travel"
        ),
        (
            3,
            20.0,
            "Uber",
            "2026-07-24",
            "denied",
            "Transportation",
            "Please use company shuttle"
        )
    ]

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchall.return_value = expected_result

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expense_history_dao(1)

    assert result == expected_result

    mock_cursor.execute.assert_called_once_with(
        """
            SELECT expenses.id,
                   expenses.amount,
                   expenses.description,
                   expenses.date,
                   approvals.status,
                   expenses.category,
                   approvals.comment
            FROM expenses
            JOIN approvals ON approvals.expense_id = expenses.id
            WHERE expenses.user_id = %s
            AND approvals.status IN ('approved', 'denied')
            ORDER BY expenses.date
        """,
        (1,)
    )

    mock_cursor.fetchall.assert_called_once()
    mock_connection.close.assert_called_once()


def test_get_expense_history_returns_empty_list(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchall.return_value = []

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expense_history_dao(999)

    assert result == []

    mock_cursor.fetchall.assert_called_once()
    mock_connection.close.assert_called_once()


def test_get_expense_history_returns_none_when_database_error_occurs(
    mocker
):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = get_expense_history_dao(1)

    assert result is None

    mock_cursor.fetchall.assert_not_called()
    mock_connection.close.assert_called_once()


# edit_expense_dao

def test_edit_expense_updates_pending_expense(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = ("pending",)

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = edit_expense_dao(
        expense_id=1,
        user_id=1,
        new_amount=35.00,
        new_description="Team lunch"
    )

    assert result is True

    assert mock_cursor.execute.call_count == 2

    mock_cursor.execute.assert_any_call(
        """
            SELECT approvals.status
            FROM approvals
            JOIN expenses ON approvals.expense_id = expenses.id
            WHERE expenses.id = %s
            AND expenses.user_id = %s
        """,
        (
            1,
            1
        )
    )

    mock_cursor.execute.assert_any_call(
        """
            UPDATE expenses
            SET amount = %s, description = %s
            WHERE id = %s
            AND user_id = %s
        """,
        (
            35.00,
            "Team lunch",
            1,
            1
        )
    )

    mock_cursor.fetchone.assert_called_once()
    mock_connection.commit.assert_called_once()
    mock_connection.close.assert_called_once()


def test_edit_expense_returns_false_when_expense_not_found(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = None

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = edit_expense_dao(
        expense_id=999,
        user_id=1,
        new_amount=50,
        new_description="Missing expense"
    )

    assert result is False

    assert mock_cursor.execute.call_count == 1
    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()


def test_edit_expense_returns_false_when_expense_not_pending(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = ("approved",)

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = edit_expense_dao(
        expense_id=2,
        user_id=1,
        new_amount=500,
        new_description="Changed plane tickets"
    )

    assert result is False

    assert mock_cursor.execute.call_count == 1
    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()


def test_edit_expense_returns_none_when_database_error_occurs(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = edit_expense_dao(
        expense_id=1,
        user_id=1,
        new_amount=35.00,
        new_description="Team lunch"
    )

    assert result is None

    mock_cursor.fetchone.assert_not_called()
    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()


# delete_expense_dao

def test_delete_expense_deletes_pending_expense(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = ("pending",)

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = delete_expense_dao(
        user_id=1,
        expense_id=1
    )

    assert result is True

    assert mock_cursor.execute.call_count == 3

    mock_cursor.execute.assert_any_call(
        """
            SELECT approvals.status
            FROM approvals
            JOIN expenses ON approvals.expense_id = expenses.id
            WHERE expenses.id = %s
            AND expenses.user_id = %s
        """,
        (
            1,
            1
        )
    )

    mock_cursor.execute.assert_any_call(
        "DELETE from approvals WHERE expense_id = %s",
        (1,)
    )

    mock_cursor.execute.assert_any_call(
        "DELETE FROM expenses WHERE user_id = %s AND id = %s",
        (
            1,
            1
        )
    )

    mock_cursor.fetchone.assert_called_once()
    mock_connection.commit.assert_called_once()
    mock_connection.close.assert_called_once()


def test_delete_expense_returns_false_when_expense_not_found(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = None

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = delete_expense_dao(
        user_id=1,
        expense_id=999
    )

    assert result is False

    assert mock_cursor.execute.call_count == 1
    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()


def test_delete_expense_returns_false_when_expense_not_pending(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.fetchone.return_value = ("approved",)

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = delete_expense_dao(
        user_id=1,
        expense_id=2
    )

    assert result is False

    assert mock_cursor.execute.call_count == 1
    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()


def test_delete_expense_returns_none_when_database_error_occurs(mocker):
    mock_connection = mocker.Mock()
    mock_cursor = mocker.Mock()

    mock_connection.cursor.return_value = mock_cursor
    mock_cursor.execute.side_effect = Exception("Database error")

    mocker.patch(
        "dao.expense_dao.get_connection",
        return_value=mock_connection
    )

    result = delete_expense_dao(
        user_id=1,
        expense_id=1
    )

    assert result is None

    mock_cursor.fetchone.assert_not_called()
    mock_connection.commit.assert_not_called()
    mock_connection.close.assert_called_once()

