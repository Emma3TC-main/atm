# STAGE: Base runtime
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copiar JAR generado
COPY dist/atm.jar /app/app.jar

# Exponer puerto
EXPOSE 8080

# Comando para correr la app
CMD ["java", "-jar", "/app/app.jar"]
