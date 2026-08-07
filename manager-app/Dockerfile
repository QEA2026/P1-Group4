# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# cache dependencies separately so code-only changes don't re-download them
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /build/target/manager-app.jar ./app.jar

EXPOSE 8080

# DB_HOST/PORT/NAME/USER/PASSWORD (Postgres) come from docker-compose's
# environment; expense.db.path is only ever set by tests, not here
ENTRYPOINT ["java", "-jar", "app.jar"]
