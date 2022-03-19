FROM maven:3.6.3-openjdk-11-slim as maven_builder
WORKDIR /app
ADD . .
RUN mvn package -Dmaven.test.skip=true

FROM openjdk:11
#RUN sudo apt update
#RUN sudo apt install -y default-jre
COPY --from=maven_builder /app/target/kairos-booker-spring-1.0.jar kairos-booker-spring-1.0.jar
ENTRYPOINT ["java","-jar","/kairos-booker-spring-1.0.jar"]
