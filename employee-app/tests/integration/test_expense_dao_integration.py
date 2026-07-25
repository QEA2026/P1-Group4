from dao.expense_dao import (
    submit_new_expense_dao,
    get_expenses_dao,
    get_expense_by_status,
    get_expense_history_dao,
    edit_expense_dao,
    delete_expense_dao
)

# Test get expense by status

def test_get_expense_by_status_returns_pending_expenses(temp_database):

    result = get_expense_by_status(1, "pending")

    assert result == [
        (
            1,
            20.0,
            "Lunch",
            "2026-07-24",
            "pending",
            "Food"
        )
    ]


def test_get_expense_by_status_returns_approved_expenses(temp_database):

    result = get_expense_by_status(1, "approved")

    assert result == [
        (
            2,
            200.0,
            "Plane tickets",
            "2026-07-23",
            "approved",
            "Travel"
        )
    ]


def test_get_expense_by_status_returns_denied_expenses(temp_database):

    result = get_expense_by_status(2, "denied")

    assert result == [
        (
            3,
            20.0,
            "Uber",
            "2026-07-24",
            "denied",
            "transportation"
        )
    ]


def test_get_expense_by_status_returns_empty_list_when_no_match(temp_database):

    result = get_expense_by_status(1, "denied")

    assert result == []



# Test submit new expense

def test_submit_new_expense_creates_expense_and_approval(temp_database):

    result = submit_new_expense_dao(
        1,
        75.50,
        "team's breakfast",
        "Food"
    )

    assert result is True

    expenses = get_expense_by_status(1, "pending")

    matching_expenses = [
        expense
        for expense in expenses
        if expense[1] == 75.50
        and expense[2] == "team's breakfast"
        and expense[4] == "pending"
        and expense[5] == "Food"
    ]

    assert len(matching_expenses) == 1


def test_submit_new_expense_returns_none_when_user_does_not_exist(temp_database):

    result = submit_new_expense_dao(
        999,
        50,
        "Invalid user expense",
        "Other"
    )

    assert result is None


# Test get expenses by user id

def test_get_expenses_returns_all_expenses_for_user(temp_database):

    result = get_expenses_dao(1)

    assert result == [
        (
            2,
            200.0,
            "Plane tickets",
            "2026-07-23",
            "approved",
            "Travel"
        ),
        (
            1,
            20.0,
            "Lunch",
            "2026-07-24",
            "pending",
            "Food"
        )
    ]


def test_get_expenses_returns_empty_list_when_user_has_no_expenses(temp_database):
    result = get_expenses_dao(999)

    assert result == []

# Test get expense history by user id 
def test_get_expense_history_returns_approved_and_denied_expenses(temp_database):

    result = get_expense_history_dao(1)

    assert result == [
        (
            2,
            200.0,
            "Plane tickets",
            "2026-07-23",
            "approved",
            "Travel"
        )
    ]




def test_get_expense_history_returns_empty_list_when_no_history_exists(temp_database):

    result = get_expense_history_dao(999)

    assert result == []



# Test edit expense
def test_edit_expense_updates_pending_expense(temp_database):

    result = edit_expense_dao(
        expense_id=1,
        user_id=1,
        new_amount=35.00,
        new_description="Team lunch"
    )

    assert result is True

    expenses = get_expenses_dao(1)

    updated_expense = next(
        expense
        for expense in expenses
        if expense[0] == 1
    )

    assert updated_expense == (
        1,
        35.0,
        "Team lunch",
        "2026-07-24",
        "pending",
        "Food"
    )


def test_edit_expense_returns_false_when_expense_is_approved(temp_database):

    result = edit_expense_dao(
        expense_id=2,
        user_id=1,
        new_amount=500,
        new_description="Changed plane tickets"
    )

    assert result is False

    expenses = get_expenses_dao(1)

    approved_expense = next(
        expense
        for expense in expenses
        if expense[0] == 2
    )

    assert approved_expense[1] == 200.0
    assert approved_expense[2] == "Plane tickets"


def test_edit_expense_returns_false_when_expense_does_not_exist(temp_database):

    result = edit_expense_dao(
        expense_id=999,
        user_id=1,
        new_amount=50,
        new_description="non-existent expense"
    )

    assert result is False


def test_edit_expense_returns_false_when_expense_belongs_to_another_user(temp_database):

    result = edit_expense_dao(
        expense_id=1,
        user_id=2,
        new_amount=50,
        new_description="Not my expense"
    )

    assert result is False


# Test Delete Expense
def test_delete_expense_deletes_pending_expense(temp_database):

    result = delete_expense_dao(
        user_id=1,
        expense_id=1
    )

    assert result is True

    expenses = get_expenses_dao(1)

    expense_ids = [
        expense[0]
        for expense in expenses
    ]

    assert 1 not in expense_ids


def test_delete_expense_returns_false_when_expense_is_approved(temp_database):

    result = delete_expense_dao(
        user_id=1,
        expense_id=2
    )

    assert result is False

    expenses = get_expenses_dao(1)

    expense_ids = [
        expense[0]
        for expense in expenses
    ]

    assert 2 in expense_ids


def test_delete_expense_returns_false_when_expense_is_denied(temp_database):

    result = delete_expense_dao(
        user_id=2,
        expense_id=3
    )

    assert result is False

    expenses = get_expenses_dao(2)

    expense_ids = [
        expense[0]
        for expense in expenses
    ]

    assert 3 in expense_ids


def test_delete_expense_returns_false_when_expense_does_not_exist(temp_database):

    result = delete_expense_dao(
        user_id=1,
        expense_id=999
    )

    assert result is False


def test_delete_expense_returns_false_when_expense_belongs_to_another_user(temp_database):
    
    result = delete_expense_dao(
        user_id=2,
        expense_id=1
    )

    assert result is False