// Manager dashboard connected to the existing Javalin API.

const API_BASE_URL = `http://${window.location.hostname}:8080`;

class ManagerDashboard {
    constructor() {
        this.currentUser = this.getCurrentUser();
        this.currentExpenseId = null;

        if (!this.currentUser || this.currentUser.role.toLowerCase() !== "manager") {
            window.location.href = "login.html";
            return;
        }

        this.initializeApp();
    }

    getCurrentUser() {
        try {
            return JSON.parse(sessionStorage.getItem("managerUser"));
        } catch {
            sessionStorage.removeItem("managerUser");
            return null;
        }
    }

    initializeApp() {
        document.getElementById("loading-section").style.display = "none";
        document.getElementById("username-display").textContent =
            this.currentUser.username;
        this.setupEventListeners();
        this.showPendingExpensesSection();
    }

    setupEventListeners() {
        document.getElementById("logout-btn").addEventListener("click", () => {
            sessionStorage.removeItem("managerUser");
            window.location.href = "login.html";
        });

        document.getElementById("show-pending").addEventListener("click", () => {
            this.showPendingExpensesSection();
        });

        document.getElementById("show-reports").addEventListener("click", () => {
            this.showSection("reports-section");
        });

        document.getElementById("refresh-pending").addEventListener("click", () => {
            this.loadPendingExpenses();
        });

        document.getElementById("generate-employee-report").addEventListener("click", () => {
            const id = document.getElementById("employee-report-id").value;
            if (id) {
                this.loadReport(`/reports/employee/${id}`, `Expenses for employee ${id}`);
            } else {
                this.showReportMessage("Please enter an employee ID", "error");
            }
        });

        document.getElementById("generate-category-report").addEventListener("click", () => {
            const category = document.getElementById("category-report").value.trim();
            if (category) {
                this.loadReport(
                    `/reports/category/${encodeURIComponent(category)}`,
                    `Expenses in category ${category}`
                );
            } else {
                this.showReportMessage("Please enter a category", "error");
            }
        });

        document.getElementById("generate-date-report").addEventListener("click", () => {
            const date = document.getElementById("report-date").value;
            if (date) {
                this.loadReport(`/reports/date/${date}`, `Expenses for ${date}`);
            } else {
                this.showReportMessage("Please select a date", "error");
            }
        });

        document.getElementById("approve-expense").addEventListener("click", () => {
            this.reviewCurrentExpense("approved");
        });

        document.getElementById("deny-expense").addEventListener("click", () => {
            this.reviewCurrentExpense("denied");
        });

        document.getElementById("cancel-review").addEventListener("click", () => {
            this.closeReviewModal();
        });
    }

    async request(path, options = {}) {
        const response = await fetch(`${API_BASE_URL}${path}`, {
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...options.headers
            }
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || `Request failed with status ${response.status}`);
        }

        return response;
    }

    async loadPendingExpenses() {
        try {
            const response = await this.request("/expenses/pending");
            const expenses = await response.json();
            this.displayPendingExpenses(expenses);
        } catch (error) {
            this.showMessage("pending-expenses-list", error.message, "error");
        }
    }

    displayPendingExpenses(expenses) {
        const container = document.getElementById("pending-expenses-list");

        if (!Array.isArray(expenses) || expenses.length === 0) {
            container.innerHTML = "<p>No pending expenses found.</p>";
            return;
        }

        container.innerHTML = "";
        const table = this.createExpenseTable(expenses, true);
        container.appendChild(table);
    }

    createExpenseTable(expenses, includeActions = false) {
        const table = document.createElement("table");
        table.border = "1";
        table.cellPadding = "8";
        table.cellSpacing = "0";
        table.width = "100%";

        const header = table.insertRow();
        ["Employee ID", "Date", "Amount", "Category", "Description"].forEach((label) => {
            const cell = document.createElement("th");
            cell.textContent = label;
            header.appendChild(cell);
        });
        if (includeActions) {
            const cell = document.createElement("th");
            cell.textContent = "Actions";
            header.appendChild(cell);
        }

        expenses.forEach((expense) => {
            const row = table.insertRow();
            [
                expense.userId,
                expense.date,
                `$${Number(expense.amount).toFixed(2)}`,
                expense.category || "-",
                expense.description
            ].forEach((value) => {
                const cell = row.insertCell();
                cell.textContent = value;
            });

            if (includeActions) {
                const cell = row.insertCell();
                const button = document.createElement("button");
                button.textContent = "Review";
                button.addEventListener("click", () => this.openReviewModal(expense));
                cell.appendChild(button);
            }
        });

        return table;
    }

    openReviewModal(expense) {
        this.currentExpenseId = expense.id;
        const details = document.getElementById("expense-details");
        details.textContent =
            `Employee ID: ${expense.userId} | Date: ${expense.date} | ` +
            `Amount: $${Number(expense.amount).toFixed(2)} | ` +
            `Description: ${expense.description}`;
        document.getElementById("review-comment").value = "";
        document.getElementById("review-message").textContent = "";
        document.getElementById("review-modal").style.display = "block";
    }

    async reviewCurrentExpense(status) {
        const comment = document.getElementById("review-comment").value.trim();

        try {
            const response = await this.request(
                `/expenses/${this.currentExpenseId}/review`,
                {
                    method: "PUT",
                    body: JSON.stringify({
                        status,
                        reviewer: this.currentUser.id,
                        comment
                    })
                }
            );

            const message = await response.text();
            this.showMessage("review-message", message, "success");
            setTimeout(() => {
                this.closeReviewModal();
                this.loadPendingExpenses();
            }, 900);
        } catch (error) {
            this.showMessage("review-message", error.message, "error");
        }
    }

    async loadReport(path, title) {
        try {
            const response = await this.request(path);
            const expenses = await response.json();
            const container = document.getElementById("report-results");
            container.innerHTML = "";

            const heading = document.createElement("h4");
            heading.textContent = title;
            container.appendChild(heading);

            if (!Array.isArray(expenses) || expenses.length === 0) {
                const message = document.createElement("p");
                message.textContent = "No expenses found.";
                container.appendChild(message);
            } else {
                container.appendChild(this.createExpenseTable(expenses));
            }
            this.showReportMessage("Report loaded successfully", "success");
        } catch (error) {
            this.showReportMessage(error.message, "error");
        }
    }

    showPendingExpensesSection() {
        this.showSection("pending-expenses-section");
        this.loadPendingExpenses();
    }

    showSection(sectionId) {
        ["pending-expenses-section", "reports-section"].forEach((id) => {
            document.getElementById(id).style.display =
                id === sectionId ? "block" : "none";
        });
    }

    closeReviewModal() {
        document.getElementById("review-modal").style.display = "none";
        this.currentExpenseId = null;
    }

    showMessage(elementId, message, type) {
        const element = document.getElementById(elementId);
        element.textContent = message;
        element.style.color = type === "error" ? "red" : "green";
        element.style.fontWeight = "bold";
    }

    showReportMessage(message, type) {
        this.showMessage("report-message", message, type);
    }
}

const managerDashboard = new ManagerDashboard();
