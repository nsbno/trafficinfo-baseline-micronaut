#!/bin/sh
set -e

# The module to start.
APP_JAR="application/application.jar"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

# Print some debug info about the JVM and Heap
exec su-exec "$USER:$GROUP" "$JAVA_HOME/bin/java" \
  -XX:MaxRAMPercentage=80 \
  -XX:+PrintFlagsFinal \
  -version | grep Heap

# Start the application with APM agent from ElasticCloud if enabled.
# The configuration of the APM agent is set as environment variables.
if [ "$APM_ENABLED" == "1" ]; then
  exec su-exec "$USER:$GROUP" "$JAVA_HOME/bin/java" \
    -javaagent:elastic-apm-agent-1.33.0.jar \
    -Delastic.apm.service_name=$APM_SERVICE_NAME \
    -Delastic.apm.server_urls=$APM_SERVER_URL \
    -Delastic.apm.secret_token=$APM_SECRET_TOKEN \
    -Delastic.apm.environment=$APM_ENVIRONMENT \
    -Delastic.apm.application_packages=no.vy.trafficinfo \
    -XX:MaxRAMPercentage=80 \
    -Dlogback.configurationFile=logback-cloud.xml \
    -Djava.util.concurrent.ForkJoinPool.common.parallelism=4 \
    -jar "$APP_JAR"
else
  exec su-exec "$USER:$GROUP" "$JAVA_HOME/bin/java" \
    -XX:MaxRAMPercentage=80 \
    -Dlogback.configurationFile=logback-cloud.xml \
    -Djava.util.concurrent.ForkJoinPool.common.parallelism=4 \
    -jar "$APP_JAR"
fi