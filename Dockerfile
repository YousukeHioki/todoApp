FROM openjdk:21-jdk-slim-bullseye

COPY ./backend/build/libs/todoapp-0.0.1-SNAPSHOT.jar ./

CMD ["java", "-jar", "todoapp-0.0.1-SNAPSHOT.jar"]
