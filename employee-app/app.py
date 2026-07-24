"""
API Layer: Flask routes for the employee side

* Exposes the service layer as JSON endpoints so the API can be tested
* No business logic here - just request parsing and responses
  (same role as ui/menu.py but over HTTP instead of the terminal)

Run with: python app.py  (serves on http://localhost:5001)
"""
from flask import Flask, request, jsonify, make_response

from api.auth import require_employee_auth, get_current_user
from service.user_service import login, generate_jwt_token, get_user_by_token
from service.expense_service import (
    submit_new_expense,
    get_my_expenses,
    edit_expense,
    delete_expense,
    get_expense_history,
)

app = Flask(__name__)

# turns the (id, amount, description, date, status, category) tuples into dicts for JSON
def expense_to_dict(row):
    return {
        "id": row[0],
        "amount": row[1],
        "description": row[2],
        "date": row[3],
        "status": row[4],
        "category": row[5],
    }


@app.route("/api/login", methods=["POST"])
def login_route():
    data = request.get_json(silent=True) or {}
    username = data.get("username")
    password = data.get("password")

    if not username or not password:
        return jsonify({"error": "username and password are required"}), 400

    user = login(username, password)
    if user is None:
        return jsonify({"error": "Invalid credentials"}), 401

    token = generate_jwt_token(user)
    response = make_response(jsonify({
        "message": "Login successful",
        "user": {"id": user[0], "username": user[1], "role": user[3]}
    }), 200)
    response.set_cookie(
        "jwt_token",
        token,
        httponly=True,
        secure=False,
        samesite="Lax",
        max_age=24 * 60 * 60
    )
    return response


@app.route("/api/logout", methods=["POST"])
def logout_route():
    response = make_response(jsonify({"message": "Logout successful"}), 200)
    response.set_cookie(
        "jwt_token",
        "",
        httponly=True,
        secure=False,
        samesite="Lax",
        expires=0
    )
    return response


@app.route("/api/status", methods=["GET"])
def status_route():
    token = request.cookies.get("jwt_token")
    if not token:
        return jsonify({"authenticated": False}), 200

    user = get_user_by_token(token)
    if user is None or str(user[3]).lower() != "employee":
        return jsonify({"authenticated": False}), 200

    return jsonify({
        "authenticated": True,
        "user": {"id": user[0], "username": user[1], "role": user[3]}
    }), 200


@app.route("/api/expenses", methods=["POST"])
@require_employee_auth
def submit_expense_route():
    data = request.get_json(silent=True) or {}
    amount = data.get("amount")
    description = data.get("description")
    category = data.get("category", "")

    if amount is None or description is None:
        return jsonify({"error": "amount and description are required"}), 400

    user_id = get_current_user()[0]
    result = submit_new_expense(user_id, amount, description, category)
    if result is True:
        return jsonify({"message": "Expense submitted successfully"}), 201
    elif result is False:
        return jsonify({"error": "Invalid amount or description"}), 400
    else:
        return jsonify({"error": "An error occurred while submitting the expense"}), 500


@app.route("/api/expenses", methods=["GET"])
@require_employee_auth
def get_expenses_route():
    status = request.args.get("status")
    user_id = get_current_user()[0]
    expenses = get_my_expenses(user_id, status)

    if expenses is None:
        return jsonify({"error": "An error occurred while retrieving expenses"}), 500

    return jsonify([expense_to_dict(e) for e in expenses]), 200


@app.route("/api/expenses/<int:expense_id>", methods=["PUT"])
@require_employee_auth
def edit_expense_route(expense_id):
    data = request.get_json(silent=True) or {}
    new_amount = data.get("amount")
    new_description = data.get("description")

    if new_amount is None or new_description is None:
        return jsonify({"error": "amount and description are required"}), 400

    user_id = get_current_user()[0]
    result = edit_expense(expense_id, user_id, new_amount, new_description)
    if result is True:
        return jsonify({"message": "Expense updated successfully"}), 200
    elif result is False:
        return jsonify({"error": "Expense not found, not yours, or no longer pending"}), 400
    else:
        return jsonify({"error": "Invalid amount"}), 400


@app.route("/api/expenses/<int:expense_id>", methods=["DELETE"])
@require_employee_auth
def delete_expense_route(expense_id):
    user_id = get_current_user()[0]
    result = delete_expense(user_id, expense_id)
    if result is True:
        return jsonify({"message": "Expense deleted successfully"}), 200
    elif result is False:
        return jsonify({"error": "Expense not found, not yours, or no longer pending"}), 400
    else:
        return jsonify({"error": "An error occurred while deleting the expense"}), 500


@app.route("/api/expenses/history", methods=["GET"])
@require_employee_auth
def expense_history_route():
    user_id = get_current_user()[0]
    expenses = get_expense_history(user_id)

    if expenses is None:
        return jsonify({"error": "An error occurred while retrieving history"}), 500

    return jsonify([expense_to_dict(e) for e in expenses]), 200


if __name__ == "__main__":
    host = "127.0.0.1"
    port = 5001
    print(f"Employee API running at http://{host}:{port}", flush=True)
    app.run(host=host, port=port, debug=True)
