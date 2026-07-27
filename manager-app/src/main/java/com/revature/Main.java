package com.revature;

import io.javalin.Javalin;
import com.revature.controllers.AuthController;
import com.revature.controllers.ExpenseController;
import com.revature.controllers.ApprovalController;

public class Main {
    public static void main(String[] args) {
        AuthController authController = new AuthController();
        ExpenseController expenseController = new ExpenseController();
        ApprovalController approvalController = new ApprovalController();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.allowHost(
                            "http://127.0.0.1:5500",
                            "http://localhost:5500",
                            "http://127.0.0.1:5501",
                            "http://localhost:5501"
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

        app.start(8080);
    }
}
