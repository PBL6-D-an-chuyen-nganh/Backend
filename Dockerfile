# --- STAGE 1: Build ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Run ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# [SỬA ĐOẠN NÀY] Thay vì *.jar, hãy dùng tên cụ thể
# Nếu artifactId trong pom.xml của bạn là 'backend' và version là '0.0.1-SNAPSHOT'
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod
EXPOSE 7860
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]