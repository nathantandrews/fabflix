FROM maven:3.8.5-openjdk-11-slim AS builder
WORKDIR /app
COPY . .
RUN mvn clean package
FROM tomcat:10-jdk11
WORKDIR /app
COPY --from=builder /app/webapp/target/fabflix.war /usr/local/tomcat/webapps/fabflix.war
COPY mysql-connector-j-8.0.32.jar /usr/local/tomcat/lib
EXPOSE 8080
CMD ["catalina.sh", "run"]