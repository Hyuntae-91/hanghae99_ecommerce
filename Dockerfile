FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
RUN apt-get update && apt-get install -y git

COPY . /app

RUN chmod +x /app/gradlew
RUN cd /app && ./gradlew clean build -x test --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]