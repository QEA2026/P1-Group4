pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        PYTHON = 'C:\\Users\\Abdulrahman\\AppData\\Local\\Programs\\Python\\Python311\\python.exe'
        MAVEN = 'C:\\apache-maven-3.9.9\\bin\\mvn.cmd'
        DOCKER = 'C:\\Users\\Abdulrahman\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
        COMPOSE = 'C:\\Users\\Abdulrahman\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker-compose.exe'
        NEWMAN = 'C:\\nvm4w\\nodejs\\newman.cmd'

        FRONTEND_HOST = 'http://18.188.107.94:5500/'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/QEA2026/P1-Group4.git'
            }
        }

        stage('Python Setup') {
            steps {
                dir('employee-app') {
                    bat '"%PYTHON%" -m venv .venv'
                    bat '.venv\\Scripts\\python.exe -m pip install -r requirements.txt'
                }
            }
        }

        stage('Python Unit Tests') {
            steps {
                dir('employee-app') {
                    bat '.venv\\Scripts\\python.exe -m pytest tests/dao tests/service -v'
                }
            }
        }

        stage('Python Integration Tests') {
            environment {
                POSTGRES_DB = 'expense_manager'
                POSTGRES_USER = 'postgres'
                POSTGRES_PASSWORD = 'changeme'

                DB_HOST = 'localhost'
                DB_PORT = '5434'
                DB_NAME = 'expense_manager_integration'
                DB_USER = 'postgres'
                DB_PASSWORD = 'changeme'
            }

            steps {
                bat '"%COMPOSE%" -p p1-group4 up -d postgres'

                bat 'powershell -NoProfile -Command "$ready = $false; for ($i = 0; $i -lt 30; $i++) { & $env:DOCKER exec expense-postgres pg_isready -U $env:POSTGRES_USER -d postgres; if ($LASTEXITCODE -eq 0) { $ready = $true; break }; Start-Sleep -Seconds 2 }; if (-not $ready) { exit 1 }"'

                bat '"%DOCKER%" exec -e PGPASSWORD=%POSTGRES_PASSWORD% expense-postgres dropdb --if-exists --force -U %POSTGRES_USER% expense_manager_integration'
                bat '"%DOCKER%" exec -e PGPASSWORD=%POSTGRES_PASSWORD% expense-postgres createdb -U %POSTGRES_USER% expense_manager_integration'

                dir('employee-app') {
                    bat '.venv\\Scripts\\python.exe -m pytest tests\\integration -v'
                }
            }

            post {
                always {
                    bat(
                        returnStatus: true,
                        script: '"%DOCKER%" exec -e PGPASSWORD=%POSTGRES_PASSWORD% expense-postgres dropdb --if-exists --force -U %POSTGRES_USER% expense_manager_integration'
                    )
                }
            }
        }



        stage('Java Unit Tests') {
            steps {
                dir('manager-app') {
                    bat '"%MAVEN%" "-Dtest=ApprovalDAOUnitTest,ExpenseDAOUnitTest,UserDAOUnitTest,ApprovalTest,ExpenseTest,UserTest,ApprovalServiceUnitTest,ExpenseServiceTest,UserServiceTest" test'
                }
            }
        }

        stage('Java Integration Tests') {
            steps {
                dir('manager-app') {
                    bat '"%MAVEN%" "-Dtest=ApprovalDAOIT,ExpenseDAOIT,UserDAOIT,ApprovalServiceIntegrationTest" test'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                bat '"%DOCKER%" build -t p1-group4-employee-app ./employee-app'
                bat '"%DOCKER%" build -t p1-group4-manager-app ./manager-app'
            }
        }

        stage('Prepare Environment') {
            steps {
                bat 'copy /Y .env.example .env'
            }
        }

        stage('Deploy Containers') {
            steps {
                bat '"%COMPOSE%" -p p1-group4 down'
                bat '"%COMPOSE%" -p p1-group4 up -d'
                bat '"%COMPOSE%" -p p1-group4 ps'
            }
        }

        stage('Start Manager Frontend') {
            steps {
                dir('manager-app') {
                    bat '''
                    start "" /B "%PYTHON%" -m http.server 5500 --directory src\\main\\resources
                    '''
                }
            }
        }

        stage('Health Checks') {
            steps {
                bat 'powershell -NoProfile -Command "if (-not (Test-NetConnection localhost -Port 5001 -InformationLevel Quiet)) { exit 1 }"'
                bat 'powershell -NoProfile -Command "if (-not (Test-NetConnection localhost -Port 8080 -InformationLevel Quiet)) { exit 1 }"'
                bat 'powershell -NoProfile -Command "if (-not (Test-NetConnection localhost -Port 5500 -InformationLevel Quiet)) { exit 1 }"'


                echo 'Employee app is reachable on port 5001'
                echo 'Manager app is reachable on port 8080'
                echo 'Manager frontend is reachable on port 5500'
            }
        }

        stage('Python API Tests - Newman') {
            steps {
                dir('employee-app\\tests\\postman') {
                    bat '"%NEWMAN%" run "employee-app api tests.postman_collection.json" -r cli,htmlextra --reporter-htmlextra-export reports\\employee-api-report.html'
                }
            }
        }

        stage('Java API Tests') {
            steps {
                dir('manager-app') {
                    bat '"%MAVEN%" "-Dtest=ApprovalApiTest,ExpenseReportsApiTest,LoginApiTest" test'
                }
            }
        }

        stage('E2E Tests - Selenium/Cucumber') {
            environment {
                DB_HOST = 'localhost'
                DB_PORT = '5434'
                DB_SSLMODE = 'disable'
                DB_NAME = 'expense_manager'
                DB_USER = 'postgres'
                DB_PASSWORD = 'changeme'

                JAVA_HOME = 'C:\\Program Files\\Java\\jdk-23'
            }

            steps {
                dir('manager-app') {
                    bat '"%MAVEN%" clean test -Pselenium "-Dcucumber.features=classpath:features" "-Dcucumber.plugin=pretty"'
                }
            }
        }

        stage('Python E2E Tests - Selenium/Behave') {
            steps {
                dir('employee-app') {
                    bat '.venv\\Scripts\\python.exe -m behave features'
                }
            }
        }

    }

    post {
        always {
            bat '''
            powershell -NoProfile -Command ^
            "$connection = Get-NetTCPConnection -LocalPort 5500 -State Listen -ErrorAction SilentlyContinue; ^
            if ($connection) { ^
                $frontendPid = $connection.OwningProcess; ^
                Write-Host ('Stopping manager frontend process PID ' + $frontendPid); ^
                Stop-Process -Id $frontendPid -Force -ErrorAction SilentlyContinue ^
            } else { ^
                Write-Host 'No manager frontend process is listening on port 5500' ^
            }"
            '''
        }
    }
}