package com.revature;

import io.javalin.Javalin;
import com.revature.controllers.AuthController;
import com.revature.controllers.ExpenseController;
import com.revature.controllers.ApprovalController;

public class Main {

    // Extracted so tests can boot the real app (routes, controllers, services,
    // DAOs) against a disposable database instead of duplicating route wiring.
    public static Javalin createApp() {
        AuthController authController = new AuthController();
        ExpenseController expenseController = new ExpenseController();
        ApprovalController approvalController = new ApprovalController();

        return Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.allowHost(
                            "http://127.0.0.1:5500",
                            "http://localhost:5500",
                            "http://127.0.0.1:5501",
                            "http://localhost:5501",
                            "http://18.188.107.94:5500"
                    );
                });
            });

            // Keep manager-side route setup together so the API entry points stay easy to find.
            config.routes.post("/login", authController.loginHandler);
            config.routes.get("/expenses/pending", expenseController.getPendingExpensesHandler);
            config.routes.put("/expenses/{id}/review", approvalController.reviewExpenseHandler); // to approve
            config.routes.get("/reports/employee/{userId}", expenseController.getExpensesByEmployeeHandler);
            config.routes.get("/reports/category/{category}", expenseController.getExpensesByCategoryHandler);
            config.routes.get("/reports/date/{date}", expenseController.getExpenseByDateHandler);
            config.routes.get("/reports/expense/{expenseId}", expenseController.getExpenseByIdHandler);
        });
    }

    public static void main(String[] args) {
        createApp().start(8080);
    }
}
