#!/bin/sh
set -e

# The module to start.
# Replace this with your own modulename (from module-info)
APP_JAR="application/application.jar"
JAVA_PARAMS="-XshowSettings:vm"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

exec su-exec "$USER:1000" "$JAVA_HOME/bin/java" "$JAVA_PARAMS $JAVA_PARAMS_OVERRIDE" -Dlogback.configurationFile=logback-cloud.xml -jar "$APP_JAR"
