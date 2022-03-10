FROM maven:3.6.3-openjdk-17-slim as maven_builder
WORKDIR /app
ADD . .
RUN mvn package -Dmaven.test.skip=true

FROM debian:10
RUN apt-get update
RUN apt-get install -y curl unzip xvfb libxi6 libgconf-2-4 openjdk-11-jdk openjdk-11-jre
RUN curl -sS -o - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN echo "deb [arch=amd64]  http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list
RUN apt-get -y update
RUN apt-get -y install google-chrome-stable
RUN wget https://chromedriver.storage.googleapis.com/99.0.4844.51/chromedriver_linux64.zip
RUN unzip chromedriver_linux64.zip
RUN mv chromedriver /usr/bin/chromedriver
RUN chmod +x /usr/bin/chromedriver
COPY --from=maven_builder /app/target/kairos-booker-spring-1.0.jar kairos-booker-spring-1.0.jar
ENTRYPOINT ["java","-jar","/kairos-booker-spring-1.0.jar"]
