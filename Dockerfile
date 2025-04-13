FROM eclipse-temurin:17
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 88

ENV SERVER_PORT=88
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_DATASOURCE_URL=${spring.datasource.url}
ENV SPRING_DATASOURCE_USERNAME=${spring.datasource.username}
ENV SPRING_DATASOURCE_PASSWORD=${spring.datasource.password}
ENV JWT_SECRET_KEY=${JWT_SECRET_KEY}
ENV CLOUD_AWS_CREDENTIALS_ACCESS_KEY=${cloud.aws.credentials.access-key}
ENV CLOUD_AWS_CREDENTIALS_SECRET_KEY=${cloud.aws.credentials.secret-key}
ENV CLOUD_AWS_REGION_STATIC=${cloud.aws.region.static}
ENV CLOUD_AWS_S3_BUCKET=${cloud.aws.s3.bucket}
ENV SPRING_CLOUD_AWS_CREDENTIALS_INSTANCE_PROFILE=false
ENV SPRING_CLOUD_AWS_S3_ENABLED=true

ENTRYPOINT ["java", \
    "-Dserver.port=${SERVER_PORT}", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
    "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", \
    "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", \
    "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", \
    "-Djwt.secret=${JWT_SECRET_KEY}", \
    "-jar", "app.jar"] 