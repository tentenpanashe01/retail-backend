# ===== Stage 1: Build the JAR =====
# ===== Stage 1: Build the JAR =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven config first (for dependency caching)
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build the project
RUN mvn -q -e -DskipTests clean package

# ===== Stage 2: Run the JAR =====
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default port
EXPOSE 8080

# Use environment variables for DB config and port
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""
ENV PORT=8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
