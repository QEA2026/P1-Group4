
# Revature Expense Manager — Phase 2

A web-based expense tracking system with two applications sharing one SQLite database:

- **Employee Web App** (Python) — submit and manage personal expense reports
- **Manager App** (Java REST API) — review, approve, and deny submitted expenses

Both applications read from and write to the same `database/expense_manager.db`, despite being written in different languages.

---

## Tech Stack

| Area | Technology |
|---|---|
| Backend | Python (Employee Web App), Java + Javalin (Manager API) |
| Database | SQLite (shared) |
| Auth | bcrypt password hashing, JWT sessions (employee app) |
| Unit Testing | pytest (Python), JUnit 5 + Mockito (Java) |
| Coverage | coverage.py / pytest-cov (Python), JaCoCo (Java) |
| E2E Testing | Behave + Selenium (Python) |
| API Testing | Postman |
| Performance | JMeter |
| Tracking | Jira, Git |

---

## Prerequisites

- Python 3.13+
- Java 17+ and Maven
- Google Chrome (required for Selenium E2E tests)
- Allure CLI (optional, for test reports): `brew install allure`

---

## Database Setup

From the repo root, create and seed the shared database:

```bash
python3 setup_db.py
```

To reset it completely:

```bash
rm database/expense_manager.db
python3 setup_db.py
```

Seeded users (all with password `password123`):

| Username | Role |
|---|---|
| marco | employee |
| bob | employee |
| vanessa | manager |

---

## Running the Applications

### Employee Web App (Python)

```bash
cd employee-app
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py
```

Runs on **http://localhost:5001**. Open `http://localhost:5001/login` in a browser.

### Manager App (Java REST API)

```bash
cd manager-app
mvn clean install
mvn exec:java -Dexec.mainClass="com.revature.Main"
```

Runs on **http://localhost:8080**. This is a REST API — test it with Postman or curl (see the API section below).

---

## Running the Tests

### Python — Unit Tests (pytest)

```bash
cd employee-app
source venv/bin/activate
pytest tests/ -v
```

With coverage:

```bash
pytest tests/ --cov=service --cov-report=term-missing
```

### Python — E2E Tests (Behave + Selenium)

The web app **must be running in a separate terminal first** (`python app.py`), since Selenium drives a real browser against the live site. Then:

```bash
cd employee-app
source venv/bin/activate
behave
```

Run a single feature:

```bash
behave features/login.feature
```

### Java - E2E Tests (Cucumber + Selenium)
The app must be running in two separate terminals first. Run it from the 'manager-app' folder:

**Run this in one terminal**: python -m http.server 5500 --directory src\main\resources  
**Run this in another terminal**: mvn clean compile exec:java "-Dexec.mainClass=com.revature.Main"

**Cucumber**
**Run this in a 3rd terminal**: mvn test "-Dcucumber.filter.tags=@reports or @expense or @login" 

**Selenium**
**Run this to only run the ManagereDashBoardTests since they take a while**: mvn "-Dtest=ManagerDashboardTests" test
If you want to run the other files (GenerateReportsTests or LoginPageTests) replace ManagerDashboardTests with that file name

### Java — Unit Tests (JUnit 5 + Mockito)

```bash
cd manager-app
mvn test
```

### Java — Code Coverage (JaCoCo)

```bash
cd manager-app
mvn clean test
open target/site/jacoco/index.html
```

### Allure Report (optional — Python unit tests)

```bash
cd employee-app
source venv/bin/activate
pytest tests/ --alluredir=allure-results
allure serve allure-results
```

---

## API Endpoints (Manager App)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/login` | Manager login (bcrypt verified, role checked) |
| GET | `/expenses/pending` | All expenses awaiting review |
| GET | `/reports/employee/{userId}` | All expenses for one employee |
| GET | `/reports/category/{category}` | All expenses in one category |
| GET | `/reports/date/{date}` | All expenses on one date |
| GET | `/reports/expense/{expenseId}` | A single expense by id |
| PUT | `/expenses/{id}/review` | Approve or deny an expense |

### Example — Login

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username": "vanessa", "password": "password123"}'
```

### Example — Approve an expense

```bash
curl -X PUT http://localhost:8080/expenses/1/review \
  -H "Content-Type: application/json" \
  -d '{"status": "approved", "reviewer": 3, "comment": "Approved for reimbursement."}'
```

---

## Performance Testing (JMeter)

<!-- TODO: fill in the actual .jmx file location and which endpoints were load-tested -->
JMeter test plans measure response times and throughput under concurrent load against the Manager API (port 8080). Test plans are located in `<add path to .jmx files>`.

---

## End-to-End Testing Notes

- **Python (Behave + Selenium):** implemented — see `employee-app/features/`. Covers login (success + rejection) and expense submission (valid, negative amount, empty description).
- **Java (Cucumber + Selenium):** <!-- TODO: update if implemented --> the Manager app is currently a REST API tested via Postman; browser-based E2E is not yet implemented on the Java side.

---

## Project Structure

```
P1-Group4/
├── database/
│   └── expense_manager.db
├── setup_db.py
├── employee-app/              # Python employee web app
│   ├── app.py                 # entry point (port 5001)
│   ├── api/  dao/  service/  ui/  db/
│   ├── tests/                 # pytest unit tests
│   ├── features/              # Behave E2E tests
│   │   ├── login.feature
│   │   ├── submit_expense.feature
│   │   ├── environment.py
│   │   └── steps/
│   └── requirements.txt
└── manager-app/               # Java manager REST API
    └── src/
        ├── main/java/com/revature/
        │   ├── Main.java       # entry point (port 8080)
        │   ├── controllers/  services/  DAOs/  models/  exceptions/  utils/
        └── test/java/com/revature/   # JUnit + Mockito tests
```

---

## Testing Approach

The suite validates behavior at multiple layers:

- **Unit tests** isolate business logic in the service layer using mocks, so no real database is touched. Both happy-path and sad-path (invalid input, not-found, edge cases) scenarios are covered.
- **API tests** (Postman) verify each endpoint returns correct status codes and enforces authentication and authorization.
- **E2E tests** (Behave + Selenium) drive a real browser through complete user workflows, covering both successful flows and failure scenarios (invalid form input, unauthorized access).
- **Performance tests** (JMeter) measure the API under concurrent load.

Client-side validation (HTML5 `required` / `min`) and server-side validation (service-layer checks) are tested as separate layers, since client validation can be bypassed and the server is the real enforcement point.
