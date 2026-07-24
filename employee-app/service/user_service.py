"""
Service layer: business logic only, no DB calls

* Does the password match?
Output should be: (1, "vanessa", "hashedpassword123", "employee")

"""
from dao.user_dao import find_user_by_username, find_user_by_id
from logger import get_logger
import bcrypt
import jwt
import os
from datetime import datetime, timedelta, timezone

logger = get_logger(__name__)

JWT_SECRET_KEY = os.getenv(
    "jwt_secret_key",
    "Secret-Json-Key-Group4"
)
JWT_ALGORITHM = "HS256"
TOKEN_EXPIRY_HOURS = 24

def login(username, password):
    try:
        user = find_user_by_username(username)
        
        if user is None:
            logger.warning(f"User with username {username} does not exist")
            return None

        if user[3] == "manager":
            logger.warning(f"Cannot login to employee side with manager credentials")
            return None
        
        if bcrypt.checkpw(password.encode(), user[2].encode()):
            logger.info(f"Successful login for user: {username}")
            return user
        else:
            logger.warning(f"Failed login attempt - incorrect password for user: {username}")
            return None
        
    except Exception as e:
        logger.error(f"Error during login for {username}: {e}")
        return None

def generate_jwt_token(user):
    now = datetime.now(timezone.utc)
    payload = {
        "user_id": user[0],
        "username": user[1],
        "role": user[3],
        "iat": now,
        "exp": now + timedelta(hours=TOKEN_EXPIRY_HOURS)
    }
    return jwt.encode(payload, JWT_SECRET_KEY, algorithm=JWT_ALGORITHM)

def validate_jwt_token(token):
    try:
        return jwt.decode(
            token,
            JWT_SECRET_KEY,
            algorithms=[JWT_ALGORITHM],
            options={"require": ["user_id", "username", "role", "iat", "exp"]}
        )
    except jwt.ExpiredSignatureError:
        logger.warning("Expired JWT token")
        return None
    except jwt.InvalidTokenError as e:
        logger.warning(f"Invalid JWT token: {e}")
        return None

def get_user_by_token(token):
    payload = validate_jwt_token(token)
    if payload is None:
        return None
    return find_user_by_id(payload["user_id"])
