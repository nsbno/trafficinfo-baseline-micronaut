FROM adoptopenjdk/openjdk16:jdk-16.0.2_7-alpine
ENV USER=appuser
ENV GROUP=$USER \
    HOME=/home/$USER \
    CONTEXT_PATH=/baseline-micronaut

RUN apk add --no-cache su-exec && \
    addgroup -S $GROUP && \
    adduser -S $USER -G $GROUP && \
    chown -R $USER $HOME

WORKDIR $HOME

EXPOSE 8080

COPY --chown=$USER Docker/runapp.sh $HOME/
RUN chmod 755 $HOME/*.sh

COPY --chown=$USER build/docker/layers/libs $HOME/application/libs
COPY --chown=$USER build/docker/layers/resources $HOME/application/resources
COPY --chown=$USER build/docker/layers/application.jar $HOME/application/application.jar

CMD [ "./runapp.sh" ]
