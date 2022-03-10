FROM maven:3.6.3-openjdk-17-slim as maven_builder
WORKDIR /app
ADD . .
RUN mvn package -Dmaven.test.skip=true

FROM debian:10
RUN apt-get update && \
    apt-get install -y gnupg wget curl unzip --no-install-recommends && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list && \
    apt-get update -y && \
    apt-get install -y google-chrome-stable default-jre default-jdk && \
    CHROMEVER=$(google-chrome --product-version | grep -o "[^\.]*\.[^\.]*\.[^\.]*") && \
    DRIVERVER=$(curl -s "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROMEVER") && \
    wget -q --continue -P /chromedriver "http://chromedriver.storage.googleapis.com/$DRIVERVER/chromedriver_linux64.zip" && \
    unzip /chromedriver/chromedriver* -d /chromedriver
COPY --from=maven_builder /app/target/kairos-booker-spring-1.0.jar kairos-booker-spring-1.0.jar
ENTRYPOINT ["java","-jar","/kairos-booker-spring-1.0.jar"]
