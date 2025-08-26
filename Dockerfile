# BUILD STAGE
FROM maven:3.9.9-amazoncorretto-21-alpine AS builder

ENV WDIR=/tmp

ADD pom.xml $WDIR/pom.xml
ADD src $WDIR/src

WORKDIR $WDIR
RUN mvn clean package assembly:single

# RELEASE STAGE
FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /tmp/target/mattermost-moderator-bot-*.jar /tmp/bot.jar
WORKDIR /tmp

ENTRYPOINT ["java", "-jar", "bot.jar", "/tmp/settings.props"]