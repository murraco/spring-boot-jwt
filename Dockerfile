FROM openjdk:8-jdk-alpine
MAINTAINER Ramazan Sakin <ramazansakin63@gmail.com>
ADD target/*.jar user-service-1.0-RELEASE.jar
EXPOSE 8080

ENTRYPOINT ["java","-Dspring.profiles.active=docker", "-jar", "/user-service-1.0-RELEASE.jar"]