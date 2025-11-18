# Imagen base ligera de Java 17
FROM eclipse-temurin:17-jre

# Crear directorio app
WORKDIR /app

# Copiar JAR del assembly
COPY target/scala-2.13/atm.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]
