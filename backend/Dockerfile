# Build stage — Maven downloads Node/npm via frontend-maven-plugin and builds React,
# then packages everything into a single fat JAR.
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy the entire repo layout so the plugin can reach ../frontend
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src
COPY frontend/package.json ./frontend/package.json
COPY frontend/public ./frontend/public
COPY frontend/src ./frontend/src

RUN apk add --no-cache maven curl && \
    cd backend && mvn clean package -DskipTests

# Runtime stage — just the JRE + fat JAR
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/target/indian-stock-analyzer-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
