FROM openjdk:8-jdk-alpine
# Refer to Maven build -> finalName
ARG JAR_FILE=target/play-master.jar

# cd /opt/app
WORKDIR /opt/app

RUN apk update && apk add --no-cache libc6-compat

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]