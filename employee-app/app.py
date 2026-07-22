"""
API Layer: Flask routes for the employee side

* Exposes the service layer as JSON endpoints so the API can be tested
* No business logic here - just request parsing and responses
  (same role as ui/menu.py but over HTTP instead of the terminal)

Run with: python app.py  (serves on http://localhost:5001)
"""
from flask import Flask, request, jsonify

from service.user_service import login
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

    return jsonify({"id": user[0], "username": user[1], "role": user[3]}), 200


@app.route("/api/expenses", methods=["POST"])
def submit_expense_route():
    data = request.get_json(silent=True) or {}
    user_id = data.get("user_id")
    amount = data.get("amount")
    description = data.get("description")
    category = data.get("category", "")

    if user_id is None or amount is None or description is None:
        return jsonify({"error": "user_id, amount, and description are required"}), 400

    result = submit_new_expense(user_id, amount, description, category)
    if result is True:
        return jsonify({"message": "Expense submitted successfully"}), 201
    elif result is False:
        return jsonify({"error": "Invalid amount or description"}), 400
    else:
        return jsonify({"error": "An error occurred while submitting the expense"}), 500


@app.route("/api/expenses", methods=["GET"])
def get_expenses_route():
    # required ?user_id=  and optional ?status=pending filter, same as the CLI menu
    # type=int because SQLite won't match the string '1' against the integer user_id column
    user_id = request.args.get("user_id", type=int)
    status = request.args.get("status")

    if user_id is None:
        return jsonify({"error": "user_id query parameter is required"}), 400
    expenses = get_my_expenses(user_id, status)

    if expenses is None:
        return jsonify({"error": "An error occurred while retrieving expenses"}), 500

    return jsonify([expense_to_dict(e) for e in expenses]), 200


@app.route("/api/expenses/<int:expense_id>", methods=["PUT"])
def edit_expense_route(expense_id):
    data = request.get_json(silent=True) or {}
    user_id = data.get("user_id")
    new_amount = data.get("amount")
    new_description = data.get("description")

    if user_id is None or new_amount is None or new_description is None:
        return jsonify({"error": "user_id, amount, and description are required"}), 400

    result = edit_expense(expense_id, user_id, new_amount, new_description)
    if result is True:
        return jsonify({"message": "Expense updated successfully"}), 200
    elif result is False:
        return jsonify({"error": "Expense not found, not yours, or no longer pending"}), 400
    else:
        return jsonify({"error": "Invalid amount"}), 400


@app.route("/api/expenses/<int:expense_id>", methods=["DELETE"])
def delete_expense_route(expense_id):
    data = request.get_json(silent=True) or {}
    user_id = data.get("user_id")

    if user_id is None:
        return jsonify({"error": "user_id is required"}), 400

    result = delete_expense(user_id, expense_id)
    if result is True:
        return jsonify({"message": "Expense deleted successfully"}), 200
    elif result is False:
        return jsonify({"error": "Expense not found, not yours, or no longer pending"}), 400
    else:
        return jsonify({"error": "An error occurred while deleting the expense"}), 500


@app.route("/api/expenses/history", methods=["GET"])
def expense_history_route():
    user_id = request.args.get("user_id", type=int)

    if user_id is None:
        return jsonify({"error": "user_id query parameter is required"}), 400

    expenses = get_expense_history(user_id)

    if expenses is None:
        return jsonify({"error": "An error occurred while retrieving history"}), 500

    return jsonify([expense_to_dict(e) for e in expenses]), 200


if __name__ == "__main__":
    app.run(debug=True, port=5001)
