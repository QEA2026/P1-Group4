from functools import wraps

from flask import jsonify, request

from service.user_service import get_user_by_token


def require_employee_auth(route):
    @wraps(route)
    def decorated_route(*args, **kwargs):
        token = request.cookies.get("jwt_token")

        if not token:
            return jsonify({"error": "Authentication required"}), 401

        user = get_user_by_token(token)
        if user is None:
            return jsonify({"error": "Invalid or expired token"}), 401

        if str(user[3]).lower() != "employee":
            return jsonify({"error": "Access denied"}), 403

        request.current_user = user
        return route(*args, **kwargs)

    return decorated_route


def get_current_user():
    return getattr(request, "current_user", None)
