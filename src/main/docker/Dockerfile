FROM openjdk:8-jdk-alpine
RUN apk update && apk upgrade && apk add netcat-openbsd
RUN mkdir -p /usr/local/in-analysis
ADD in-analysis-1.0.0.jar /usr/local/in-analysis/

CMD ["java", "-jar", "/usr/local/in-analysis/in-analysis-1.0.0.jar"]