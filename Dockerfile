# STAGE: Base runtime
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copiar JAR generado
COPY target/scala-2.13/atm.jar /app/app.jar

# Exponer puerto
EXPOSE 8080

# Comando para correr tu app
CMD ["java", "-jar", "/app/app.jar"]
