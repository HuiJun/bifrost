FROM openjdk:8-jdk-alpine
MAINTAINER HuiJun
RUN apk add tzdata
RUN cp /usr/share/zoneinfo/America/Los_Angeles /etc/localtime
RUN echo "America/Los_Angeles" > /etc/timezone
RUN apk del tzdata
COPY target/bifrost-*-jar-with-dependencies.jar /opt/bifrost.jar
CMD ["java","-jar","/opt/bifrost.jar"]