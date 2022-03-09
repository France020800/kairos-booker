FROM maven:3.6.3-openjdk-17-slim as maven_builder
WORKDIR /app
ADD . .
RUN mvn package -Dmaven.test.skip=true

FROM local-seleniarm/standalone-chromium
COPY --from=maven_builder /app/target/kairos-booker-spring-1.0.jar kairos-booker-spring-1.0.jar
ENTRYPOINT ["java","-jar","/kairos-booker-spring-1.0.jar"]
