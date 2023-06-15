FROM openjdk:17-jdk-alpine
WORKDIR /app
ARG JAR_FILE
CMD ["ECHO", "${JAR_FILE}"]
COPY ${JAR_FILE} /app/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]