FROM openjdk:8-jdk-alpine
MAINTAINER HuiJun
CMD apk add tzdata
CMD cp /usr/share/zoneinfo/America/Los_Angeles /etc/localtime
CMD echo "America/Los_Angeles" > /etc/timezone
CMD apk del tzdata
COPY target/bifrost-*-jar-with-dependencies.jar /opt/bifrost.jar
CMD ["java","-jar","/opt/bifrost.jar"]