from unittest.mock import patch
from service.expense_service import (
    submit_new_expense,
    get_my_expenses,
    edit_expense,
    delete_expense,
    get_expense_history,
)


# ── submit_new_expense ─────────────────────────────

# patch the DAO name where expense_service sees it, NOT dao.expense_dao
# submit_new_expense(id,amount,description, category)
@patch('service.expense_service.submit_new_expense_dao')
def test_submit_happy_path(mock_submit):
    mock_submit.return_value = True

    result = submit_new_expense(1, "150", "flight", "travel") # should convert the amount to a float

    assert result is True
    mock_submit.assert_called_once_with(1, "150", "flight", "travel")


# sad path
@patch('service.expense_service.submit_new_expense_dao')
def test_submit_zero_amount_rejected(mock_submit):
    result = submit_new_expense(1, "0", "flight", "travel")

    assert result is False
    mock_submit.assert_not_called()   # guard clause stopped before it reached the DB


@patch('service.expense_service.submit_new_expense_dao')
def test_submit_negative_amount_rejected(mock_submit):
    result = submit_new_expense(1, "-50", "flight", "travel")

    assert result is False
    mock_submit.assert_not_called()


@patch('service.expense_service.submit_new_expense_dao')
def test_submit_empty_description_rejected(mock_submit):
    result = submit_new_expense(1, "150", "   ", "travel")

    assert result is False
    mock_submit.assert_not_called()


@patch('service.expense_service.submit_new_expense_dao')
def test_submit_non_numeric_amount_returns_none(mock_submit):
    # float("abc") raises ValueError -> caught -> returns None
    result = submit_new_expense(1, "abc", "flight", "travel")

    assert result is None
    mock_submit.assert_not_called()


@patch('service.expense_service.submit_new_expense_dao')
def test_submit_dao_failure_returns_false(mock_submit):
    mock_submit.return_value = False   # valid input, but DB reports failure

    result = submit_new_expense(1, "150", "flight", "travel")

    assert result is False
    mock_submit.assert_called_once()


# ── get_my_expenses (branch coverage) ──────────────

# Happy Path
@patch('service.expense_service.get_expense_by_status')
@patch('service.expense_service.get_expenses_dao')
def test_get_my_expenses_no_status(mock_get_all, mock_by_status):
    mock_get_all.return_value = [(1, 100.0, 'x', '2026-06-01', 'pending', 'travel')]

    result = get_my_expenses(1)

    assert result == mock_get_all.return_value
    mock_get_all.assert_called_once_with(1)
    mock_by_status.assert_not_called()


@patch('service.expense_service.get_expense_by_status')
@patch('service.expense_service.get_expenses_dao')
def test_get_my_expenses_with_status(mock_get_all, mock_by_status):
    mock_by_status.return_value = []

    result = get_my_expenses(1, "pending")

    assert result == mock_by_status.return_value
    mock_by_status.assert_called_once_with(1, "pending")
    mock_get_all.assert_not_called()


# ── edit_expense (note: uses < 0, so 0 is VALID) ───

@patch('service.expense_service.edit_expense_dao')
def test_edit_zero_amount_is_valid(mock_edit):
    mock_edit.return_value = True

    result = edit_expense(1, 1, "0", "updated desc")

    assert result is True
    mock_edit.assert_called_once()   # 0 passes because check is < 0, not <= 0


@patch('service.expense_service.edit_expense_dao')
def test_edit_negative_amount_rejected(mock_edit):
    result = edit_expense(1, 1, "-5", "updated desc")

    assert result is False
    mock_edit.assert_not_called()


@patch('service.expense_service.edit_expense_dao')
def test_edit_empty_description_rejected(mock_edit):
    result = edit_expense(1, 1, "50", "   ")

    assert result is False
    mock_edit.assert_not_called()


@patch('service.expense_service.edit_expense_dao')
def test_edit_dao_failure_returns_false(mock_edit):
    mock_edit.return_value = False   # valid input, but not pending / doesn't exist

    result = edit_expense(1, 1, "50", "updated desc")

    assert result is False
    mock_edit.assert_called_once()


@patch('service.expense_service.edit_expense_dao')
def test_edit_non_numeric_amount_returns_none(mock_edit):
    # float("abc") raises ValueError -> caught -> returns None
    result = edit_expense(1, 1, "abc", "updated desc")

    assert result is None
    mock_edit.assert_not_called()


# ── delete_expense / get_expense_history (just passed through)

@patch('service.expense_service.delete_expense_dao')
def test_delete_success(mock_delete):
    mock_delete.return_value = True
    assert delete_expense(1, 5) is True


@patch('service.expense_service.delete_expense_dao')
def test_delete_failure_returns_false(mock_delete):
    mock_delete.return_value = False   # not pending / doesn't exist
    assert delete_expense(1, 5) is False


@patch('service.expense_service.get_expense_history_dao')
def test_get_history_passthrough(mock_history):
    mock_history.return_value = [(1, 100.0, 'x', '2026-06-01', 'approved', 'travel')]
    result = get_expense_history(1)
    assert len(result) == 1