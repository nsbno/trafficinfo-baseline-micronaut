#!/bin/sh
set -e

# The module to start.
# Replace this with your own modulename (from module-info)
APP_JAR="baseline.jar"
JAVA_PARAMS="-XshowSettings:vm"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

/sbin/su-exec "$USER:1000" "$JAVA_HOME/bin/java" "$JAVA_PARAMS $JAVA_PARAMS_OVERRIDE" -jar "$APP_JAR"
