FROM selenium/standalone-chrome
COPY target/kairos-booker-spring-1.0.jar kairos-booker-spring-1.0.jar
ENTRYPOINT ["java","-jar","/kairos-booker-spring-1.0.jar"]
