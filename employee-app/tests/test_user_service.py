from unittest.mock import patch
from service.user_service import login


# The DB row shape find_user_by_username returns:
#   (id, username, hashed_password, role)
def fake_user(role="employee", pw_hash="hashed"):
    return (1, "vanessa", pw_hash, role)


# ── happy path ─────────────────────────────────────

# patch the DAO + bcrypt where user_service sees them, NOT dao.user_dao / bcrypt
@patch('service.user_service.bcrypt')
@patch('service.user_service.find_user_by_username')
def test_login_happy_path(mock_find, mock_bcrypt):
    user = fake_user()
    mock_find.return_value = user
    mock_bcrypt.checkpw.return_value = True   # password matches

    result = login("vanessa", "correct-password")

    assert result == user
    mock_find.assert_called_once_with("vanessa")
    mock_bcrypt.checkpw.assert_called_once()


# ── sad path: user does not exist ──────────────────

@patch('service.user_service.bcrypt')
@patch('service.user_service.find_user_by_username')
def test_login_user_not_found_returns_none(mock_find, mock_bcrypt):
    mock_find.return_value = None   # username doesnt exist

    result = login("ghost", "whatever")

    assert result is None
    # never reach the password check if the user doesn't exist
    mock_bcrypt.checkpw.assert_not_called()


# ── sad path: manager cannot log in on the employee side ──

@patch('service.user_service.bcrypt')
@patch('service.user_service.find_user_by_username')
def test_login_manager_rejected(mock_find, mock_bcrypt):
    mock_find.return_value = fake_user(role="manager")

    result = login("vanessa", "correct-password")

    assert result is None
    # role check happens before the password check, so bcrypt is never called
    mock_bcrypt.checkpw.assert_not_called()


# ── sad path: wrong password ───────────────────────

@patch('service.user_service.bcrypt')
@patch('service.user_service.find_user_by_username')
def test_login_wrong_password_returns_none(mock_find, mock_bcrypt):
    mock_find.return_value = fake_user()
    mock_bcrypt.checkpw.return_value = False   # when the password doesnt match

    result = login("vanessa", "wrong-password")

    assert result is None
    mock_bcrypt.checkpw.assert_called_once()


# ── edge case: bad input raises inside the try -> returns None ──

@patch('service.user_service.bcrypt')
@patch('service.user_service.find_user_by_username')
def test_login_none_password_returns_none(mock_find, mock_bcrypt):
    # password.encode() on None raises AttributeError -> caught -> None
    mock_find.return_value = fake_user()

    result = login("vanessa", None)

    assert result is None
    mock_bcrypt.checkpw.assert_not_called()


# ── edge case: DAO blows up -> caught -> returns None ──

@patch('service.user_service.find_user_by_username')
def test_login_dao_exception_returns_none(mock_find):
    mock_find.side_effect = Exception("db is down")

    result = login("vanessa", "correct-password")

    assert result is None
