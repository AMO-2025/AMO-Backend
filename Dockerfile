FROM eclipse-temurin:17
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 88
ENTRYPOINT ["java", "-jar", "app.jar"] 