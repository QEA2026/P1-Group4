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

# overrides ConnectionUtil's default CWD-relative db lookup;
# docker-compose mounts the real db at this path
ENTRYPOINT ["java", "-Dexpense.db.path=/app/database/expense_manager.db", "-jar", "app.jar"]
