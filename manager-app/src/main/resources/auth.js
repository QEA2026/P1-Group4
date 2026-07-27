// Authentication JavaScript for the existing manager API.

const API_BASE_URL = "http://127.0.0.1:8080";

class AuthManager {
    constructor() {
        document.getElementById("login-form").addEventListener("submit", (event) => {
            event.preventDefault();
            this.login();
        });

        const currentUser = this.getCurrentUser();
        if (currentUser && currentUser.role.toLowerCase() === "manager") {
            window.location.href = "manager.html";
        }
    }

    getCurrentUser() {
        try {
            return JSON.parse(sessionStorage.getItem("managerUser"));
        } catch {
            sessionStorage.removeItem("managerUser");
            return null;
        }
    }

    async login() {
        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value;

        try {
            const response = await fetch(`${API_BASE_URL}/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const message = await response.text();
                this.showMessage(message || "Invalid manager credentials", "error");
                return;
            }

            const user = await response.json();
            if (!user.role || user.role.toLowerCase() !== "manager") {
                this.showMessage("Access denied - this is the manager portal", "error");
                return;
            }

            sessionStorage.setItem("managerUser", JSON.stringify(user));
            this.showMessage("Login successful! Redirecting...", "success");
            setTimeout(() => {
                window.location.href = "manager.html";
            }, 700);
        } catch (error) {
            console.error("Login error:", error);
            this.showMessage(
                "Could not reach the manager API at http://127.0.0.1:8080.",
                "error"
            );
        }
    }

    showMessage(message, type) {
        const element = document.getElementById("login-message");
        element.textContent = message;
        element.style.color = type === "error" ? "red" : "green";
        element.style.fontWeight = "bold";
    }
}

new AuthManager();
