FROM adoptopenjdk/openjdk12:jdk-12.0.2_10-alpine
ENV USER=appuser
ENV JAVA_HOME=/opt/java/openjdk \
    HOME=/home/$USER

RUN adduser -S $USER
RUN apk add --no-cache su-exec

COPY Docker/runapp.sh target/app.jar $HOME/
RUN chmod 755 $HOME/*.sh && \
    chown -R $USER $HOME

EXPOSE 8080

WORKDIR /home/$USER
CMD [ "./runapp.sh"]
