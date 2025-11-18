WORKDIR /app
COPY dist/atm.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
