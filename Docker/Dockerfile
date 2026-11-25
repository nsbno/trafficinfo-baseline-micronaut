FROM eclipse-temurin:25-jre-alpine
ENV USER=appuser
ENV GROUP=$USER \
    HOME=/home/$USER \
    CONTEXT_PATH=/baseline-micronaut \
    ELASTIC_APM_AGENT_VERSION=1.57.1

# set timezone to Oslo, because otherwise it is set
# to the location of the datacenter running fargate.
ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# create app user.
RUN apk add --no-cache su-exec && \
    addgroup -S $GROUP && \
    adduser -S $USER -G $GROUP && \
    chown -R $USER $HOME

WORKDIR $HOME

EXPOSE 8080

# copy application files
COPY --chown=$USER Docker/runapp.sh $HOME/
RUN chmod 755 $HOME/*.sh

# The layers are created by `./gradlew build` (Micronaut Gradle plugin). The manifest Class-Path
# of application.jar references every dependency as libs/<jar>, but the plugin splits them into
# separate folders:
#   libs/          release dependencies
#   project_libs/  dependencies on other Gradle projects in this build
#   snapshot_libs/ -SNAPSHOT dependencies
# All three must be merged into application/libs, or the app fails at runtime with
# ClassNotFoundException for classes from snapshot/project dependencies.
# The plugin always creates all three folders, also when they are empty, so they are copied
# unconditionally. Ordered from least to most frequently changing for better layer caching.
COPY --chown=$USER build/docker/main/layers/libs $HOME/application/libs
COPY --chown=$USER build/docker/main/layers/project_libs $HOME/application/libs
COPY --chown=$USER build/docker/main/layers/snapshot_libs $HOME/application/libs
COPY --chown=$USER build/docker/main/layers/resources $HOME/application/resources
COPY --chown=$USER build/docker/main/layers/app/application.jar $HOME/application/application.jar

# Download elastic cloud apm agent
RUN wget https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/$ELASTIC_APM_AGENT_VERSION/elastic-apm-agent-$ELASTIC_APM_AGENT_VERSION.jar -O $HOME/elastic-apm-agent.jar

CMD [ "./runapp.sh" ]
