FROM openjdk:8-jdk-alpine AS build-env

RUN apk update
#RUN apk upgrade
RUN apk add git

ADD https://api.github.com/repos/NathanNorth/vc-notification-bot/git/refs/heads/master version.json
RUN git clone https://github.com/NathanNorth/vc-notification-bot /code

WORKDIR /code
RUN chmod +x gradlew
RUN /code/gradlew -Dorg.gradle.java.home=/usr/lib/jvm/java-1.8-openjdk build && mv build/libs/vc-notification-bot-*.jar /code/bot.jar

FROM openjdk:8-jdk-slim
COPY --from=build-env /code/bot.jar /bot/bot.jar
COPY keys.txt /bot/keys.txt
WORKDIR /bot
CMD ["java", "-jar", "bot.jar"]