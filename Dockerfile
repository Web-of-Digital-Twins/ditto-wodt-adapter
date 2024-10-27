FROM alpine:3.20
COPY ./ .
RUN apk add openjdk17
RUN ./gradlew compileJava
ENTRYPOINT ./gradlew run