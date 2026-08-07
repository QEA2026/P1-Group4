from datetime import datetime, timedelta, timezone
from unittest.mock import patch
import jwt
import pytest
from service.user_service import (
    login,
    generate_jwt_token,
    validate_jwt_token,
    get_user_by_token,
    JWT_SECRET_KEY,
    JWT_ALGORITHM,
)


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

    mock_find.return_value = fake_user()

    result = login("vanessa", None)

    assert result is None
    mock_bcrypt.checkpw.assert_not_called()



@patch('service.user_service.find_user_by_username')
def test_login_dao_exception_returns_none(mock_find):
    mock_find.side_effect = Exception("db is down")

    result = login("vanessa", "correct-password")

    assert result is None


# ── generate_jwt_token ──────────────────────────────

def test_generate_jwt_token_contains_expected_claims():
    user = fake_user()

    token = generate_jwt_token(user)
    payload = jwt.decode(token, JWT_SECRET_KEY, algorithms=[JWT_ALGORITHM])

    assert payload["user_id"] == user[0]
    assert payload["username"] == user[1]
    assert payload["role"] == user[3]
    assert "iat" in payload
    assert "exp" in payload


# ── validate_jwt_token ──────────────────────────────

def test_validate_jwt_token_valid_token_returns_payload():
    token = generate_jwt_token(fake_user())

    payload = validate_jwt_token(token)

    assert payload is not None
    assert payload["username"] == "vanessa"


def test_validate_jwt_token_expired_returns_none():
    now = datetime.now(timezone.utc)
    expired_payload = {
        "user_id": 1,
        "username": "vanessa",
        "role": "employee",
        "iat": now - timedelta(hours=25),
        "exp": now - timedelta(hours=1),
    }
    expired_token = jwt.encode(expired_payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)

    result = validate_jwt_token(expired_token)

    assert result is None


def test_validate_jwt_token_invalid_token_returns_none():
    result = validate_jwt_token("notatoken")

    assert result is None


def test_validate_jwt_token_missing_required_claim_returns_none():
    now = datetime.now(timezone.utc)
    incomplete_payload = {
        "user_id": 1,
        "iat": now,
        "exp": now + timedelta(hours=1),
    }
    token = jwt.encode(incomplete_payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)

    result = validate_jwt_token(token)

    assert result is None


# ── get_user_by_token ───────────────────────────────

@patch('service.user_service.find_user_by_id')
@patch('service.user_service.validate_jwt_token')
def test_get_user_by_token_valid_token_returns_user(mock_validate, mock_find_by_id):
    mock_validate.return_value = {"user_id": 1, "username": "vanessa", "role": "employee"}
    mock_find_by_id.return_value = fake_user()

    result = get_user_by_token("some-token")

    assert result == fake_user()
    mock_find_by_id.assert_called_once_with(1)


@patch('service.user_service.find_user_by_id')
@patch('service.user_service.validate_jwt_token')
def test_get_user_by_token_invalid_token_returns_none(mock_validate, mock_find_by_id):
    mock_validate.return_value = None

    result = get_user_by_token("bad-token")

    assert result is None
    mock_find_by_id.assert_not_called()
