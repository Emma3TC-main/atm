# Imagen base ligera de Java
FROM eclipse-temurin:17-jre

# Crea el directorio del app
WORKDIR /app

# Copia tu JAR generado por sbt assembly
COPY target/scala-2.13/atm.jar app.jar

# Puerto donde corre http4s
EXPOSE 8080

# Comando de ejecución
CMD ["java", "-jar", "app.jar"]
