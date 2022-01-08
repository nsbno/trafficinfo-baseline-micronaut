FROM eclipse-temurin:17.0.1_12-jre-alpine
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
