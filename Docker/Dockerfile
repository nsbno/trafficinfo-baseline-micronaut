FROM adoptopenjdk/openjdk15:jdk-15.0.1_9-alpine
ENV USER=appuser
ENV JAVA_HOME=/opt/java/openjdk \
    HOME=/home/$USER \
    CONTEXT_PATH=/baseline-micronaut

RUN adduser -S $USER
RUN apk add --no-cache su-exec

# Replace baseline.jar with your applications JAR file (defined in build.gradle.kts)
COPY Docker/runapp.sh build/libs/baseline.jar $HOME/
RUN chmod 755 $HOME/*.sh && \
    chown -R $USER $HOME

EXPOSE 8080

WORKDIR /home/$USER
CMD [ "./runapp.sh"]
