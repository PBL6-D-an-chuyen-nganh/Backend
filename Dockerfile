# --- STAGE 1: Build ứng dụng từ Source Code ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy file cấu hình maven trước để tận dụng cache layer
COPY pom.xml .
# Tải toàn bộ thư viện về (bước này tốn thời gian nhất nên tách riêng)
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code mới nhất vào
COPY src ./src

# Compile và đóng gói thành file JAR (bỏ qua test để build nhanh hơn)
RUN mvn clean package -DskipTests

# --- STAGE 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file JAR đã build từ Stage 1 sang Stage 2
COPY --from=build /app/target/*.jar app.jar

# Cấu hình biến môi trường để ép log hiển thị
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=prod

# Mở cổng 7860 (Cổng mặc định của Hugging Face)
EXPOSE 7860

# Lệnh chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]