FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD build/libs/backend-1.0.0.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
