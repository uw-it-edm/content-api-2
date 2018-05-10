FROM openjdk:8 as builder
WORKDIR .

ENV APP_HOME=/root/dev/app/
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

COPY build.gradle gradlew gradlew.bat $APP_HOME
COPY gradle $APP_HOME/gradle

# download dependencies
COPY .git .git
RUN ./gradlew build -x :bootJar -x test -x artifactoryDeploy -x artifactoryPublish --continue
COPY . .
RUN ./gradlew build

FROM openjdk:8 as app
WORKDIR /root/
COPY --from=builder /root/dev/app/build/libs/content-api-2*.jar .
RUN mv content-api-2*.jar content-api-2.jar
EXPOSE 8080

HEALTHCHECK --start-period=5s --timeout=3s --interval=15s \
    CMD curl http://localhost:8080/actuator/health || exit 1

CMD ["java","-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar", "content-api-2.jar"]
