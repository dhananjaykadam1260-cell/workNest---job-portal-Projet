# ================================
# Stage 1 - Build Spring Boot App
# ================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .

RUN mvn -B dependency:go-offline

# Copy source code
COPY src ./src

# Build JAR
RUN mvn -B clean package -DskipTests


# ================================
# Stage 2 - Run Spring Boot App
# ================================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]