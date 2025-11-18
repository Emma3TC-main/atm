# ============================
# 1. Stage: Build JAR with sbt
# ============================
FROM hseeberger/scala-sbt:17.0.2_1.8.2_2.13.10 as builder

WORKDIR /app
COPY . .

# Compila y crea el JAR fat/assembly
RUN sbt clean assembly


# ============================
# 2. Stage: Runtime (JRE only)
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia el JAR generado
COPY --from=builder /app/target/scala-2.13/*assembly*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
