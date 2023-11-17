FROM eclipse-temurin:21.0.1_12-jre-alpine
ENV USER=appuser

# <placeholder> Replace context path for your own application
ENV GROUP=$USER \
    HOME=/home/$USER \
    CONTEXT_PATH=/baseline-micronaut \
    ELASTIC_APM_AGENT_VERSION=1.38.0

# set timezone to Oslo, because otherwise it is set
# to the location of the datacenter running fargate.
ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# <placeholder> Add additional packages for the docker container here
RUN apk add --no-cache su-exec jattach

# create app user. \
RUN addgroup -S $GROUP && \
    adduser -S $USER -G $GROUP && \
    chown -R $USER $HOME

WORKDIR $HOME

EXPOSE 8080

# copy application files
COPY --chown=$USER Docker/runapp.sh $HOME/
RUN chmod 755 $HOME/*.sh

# <placeholder> Replace jar-file name with name of jar file from Gradle job building a jar
COPY --chown=$USER build/docker/main/layers/application.jar $HOME/application/application.jar

# Download elastic cloud apm agent
RUN wget https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/$ELASTIC_APM_AGENT_VERSION/elastic-apm-agent-$ELASTIC_APM_AGENT_VERSION.jar -O $HOME/elastic-apm-agent.jar

CMD [ "./runapp.sh" ]
