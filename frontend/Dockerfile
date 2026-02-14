# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Install Node.js (required by Vaadin build-frontend goal)
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Copy Maven wrapper and pom.xml first for dependency caching
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source and build (production mode)
COPY src src
RUN ./mvnw clean package -Pproduction -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
