# 1. Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and download deps
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/receiptprocessor-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 and run
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
