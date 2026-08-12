pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        // Linux tools are installed on PATH
        PYTHON = '/usr/bin/python3.11'
        MAVEN = '/usr/bin/mvn'
        DOCKER = '/usr/bin/docker'
        NEWMAN = '/usr/bin/newman'

        // Selenium tests use localhost:5500 on this Jenkins host
        FRONTEND_HOST = 'http://localhost:5500'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/QEA2026/P1-Group4.git'
            }
        }

        stage('Prepare Environment') {
            steps {
                sh 'cp -f .env.example .env'
            }
        }

        stage('Python Setup') {
            steps {
                dir('employee-app') {
                    sh '"$PYTHON" -m venv .venv'
                    sh '.venv/bin/python -m pip install --upgrade pip'
                    sh '.venv/bin/python -m pip install -r requirements.txt'
                }
            }
        }

        stage('Python Unit Tests') {
            steps {
                dir('employee-app') {
                    sh '.venv/bin/python -m pytest tests/dao tests/service -v'
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
                sh 'docker compose -p p1-group4 up -d postgres'

                sh '''
                    ready=false

                    for i in $(seq 1 30); do
                        if docker exec expense-postgres \
                            pg_isready -U "$POSTGRES_USER" -d postgres >/dev/null 2>&1
                        then
                            ready=true
                            break
                        fi

                        sleep 2
                    done

                    if [ "$ready" != "true" ]; then
                        echo "PostgreSQL did not become ready"
                        exit 1
                    fi
                '''

                sh '''
                    docker exec \
                        -e PGPASSWORD="$POSTGRES_PASSWORD" \
                        expense-postgres \
                        dropdb \
                        --if-exists \
                        --force \
                        -U "$POSTGRES_USER" \
                        "$DB_NAME"
                '''

                sh '''
                    docker exec \
                        -e PGPASSWORD="$POSTGRES_PASSWORD" \
                        expense-postgres \
                        createdb \
                        -U "$POSTGRES_USER" \
                        "$DB_NAME"
                '''

                dir('employee-app') {
                    sh '.venv/bin/python -m pytest tests/integration -v'
                }
            }

            post {
                always {
                    sh '''
                        docker exec \
                            -e PGPASSWORD="$POSTGRES_PASSWORD" \
                            expense-postgres \
                            dropdb \
                            --if-exists \
                            --force \
                            -U "$POSTGRES_USER" \
                            "$DB_NAME" || true
                    '''
                }
            }
        }

        stage('Java Unit Tests') {
            steps {
                dir('manager-app') {
                    sh '''
                        mvn \
                        "-Dtest=ApprovalDAOUnitTest,ExpenseDAOUnitTest,UserDAOUnitTest,ApprovalTest,ExpenseTest,UserTest,ApprovalServiceUnitTest,ExpenseServiceTest,UserServiceTest" \
                        test
                    '''
                }
            }
        }

        stage('Java Integration Tests') {
            steps {
                dir('manager-app') {
                    sh '''
                        mvn \
                        "-Dtest=ApprovalDAOIT,ExpenseDAOIT,UserDAOIT,ApprovalServiceIntegrationTest" \
                        test
                    '''
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker build -t p1-group4-employee-app ./employee-app'
                sh 'docker build -t p1-group4-manager-app ./manager-app'
            }
        }

        stage('Deploy Containers') {
            steps {
                sh 'docker compose -p p1-group4 down'
                sh 'docker compose -p p1-group4 up -d'
                sh 'docker compose -p p1-group4 ps'
            }
        }

        stage('Start Manager Frontend') {
            steps {
                dir('manager-app') {
                    sh '''
                        nohup python3 -m http.server 5500 \
                            --directory src/main/resources \
                            --bind 0.0.0.0 \
                            > "$WORKSPACE/manager-frontend.log" 2>&1 &

                        echo $! > "$WORKSPACE/manager-frontend.pid"

                        echo "Manager frontend PID: $(cat "$WORKSPACE/manager-frontend.pid")"
                    '''
                }
            }
        }

        stage('Health Checks') {
            steps {
                sh '''
                    check_port() {
                        port=$1
                        name=$2

                        echo "Waiting for $name on port $port..."

                        for i in $(seq 1 30); do
                            if bash -c "echo > /dev/tcp/127.0.0.1/$port" 2>/dev/null; then
                                echo "$name is reachable on port $port"
                                return 0
                            fi

                            sleep 2
                        done

                        echo "$name failed health check on port $port"
                        return 1
                    }

                    check_port 5001 "Employee app"
                    check_port 8080 "Manager API"
                    check_port 5500 "Manager frontend"
                '''
            }
        }

        stage('Python API Tests - Newman') {
            steps {
                dir('employee-app/tests/postman') {
                    sh '''
                        mkdir -p reports

                        newman run "employee-app api tests.postman_collection.json" \
                            -r cli,htmlextra \
                            --reporter-htmlextra-export reports/employee-api-report.html
                    '''
                }
            }
        }

        stage('Java API Tests') {
            steps {
                dir('manager-app') {
                    sh '''
                        mvn \
                        "-Dtest=ApprovalApiTest,ExpenseReportsApiTest,LoginApiTest" \
                        test
                    '''
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
            }

            steps {
                dir('manager-app') {
                    sh '''
                        mvn clean test \
                            -Pselenium \
                            "-Dcucumber.features=classpath:features" \
                            "-Dcucumber.plugin=pretty"
                    '''
                }
            }
        }

        stage('Python E2E Tests - Selenium/Behave') {
            steps {
                dir('employee-app') {
                    sh '.venv/bin/python -m behave features'
                }
            }
        }
    }

    post {
        always {
            sh '''
                if [ -f "$WORKSPACE/manager-frontend.pid" ]; then
                    FRONTEND_PID=$(cat "$WORKSPACE/manager-frontend.pid")

                    echo "Stopping manager frontend PID $FRONTEND_PID"

                    kill "$FRONTEND_PID" 2>/dev/null || true

                    rm -f "$WORKSPACE/manager-frontend.pid"
                else
                    echo "No manager frontend PID file found"
                fi
            '''
        }
    }
}