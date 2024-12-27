FROM openjdk:22-jdk AS builder

ARG COMPILE_DIR=/compiledir
WORKDIR ${COMPILE_DIR}

#Copy build files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY src src
COPY .mvn .mvn

#Build app
RUN chmod a+x ./mvnw
RUN ./mvnw clean package -Dmaven.skip.tests=true

ENTRYPOINT ["java", "-jar", "target/ThemeParkQueueTime-0.0.1-SNAPSHOT.jar"]

# second stage Run time
FROM openjdk:22-jdk

ARG WORK_DIR=/app
WORKDIR ${WORK_DIR}

#Copy jar from builder
COPY --from=builder /compiledir/target/*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE ${SERVER_PORT}

ENTRYPOINT java -jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1
