# ============================
# 1. STAGE: Build JAR with sbt
# ============================
FROM hseeberger/scala-sbt:11.0.16_1.9.7_2.13.12 AS builder

WORKDIR /app
COPY . .

# Construye el JAR assembly
RUN sbt clean assembly

# ============================
# 2. STAGE: Runtime
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia el jar generado
COPY --from=builder /app/target/scala-2.13/*assembly*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
