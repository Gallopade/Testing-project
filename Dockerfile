# the first stage of our build will extract the layers
FROM openjdk:21-jdk as builder
WORKDIR report.scheduler

# Add Maintainer Info
LABEL maintainer="adeel770.gp@gmail.com"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make 9004 available to the world outside this container
EXPOSE 9090

# The application's jar file
ARG JAR_FILE=/target/report.scheduler.jar

COPY ${JAR_FILE} report.scheduler.jar

RUN java -Djarmode=layertools -jar report.scheduler.jar extract


# the second stage of our build will copy the extracted layers
FROM openjdk:21-jdk
WORKDIR report.scheduler
COPY --from=builder report.scheduler/dependencies/ ./
COPY --from=builder report.scheduler/spring-boot-loader/ ./
COPY --from=builder report.scheduler/snapshot-dependencies/ ./
COPY --from=builder report.scheduler/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]