FROM openjdk:11-slim

ARG JAR_FILE=census-rm-notify-processor*.jar
COPY target/$JAR_FILE /opt/census-rm-notify-processor.jar

COPY healthcheck.sh /opt/healthcheck.sh
RUN chmod +x /opt/healthcheck.sh

CMD exec /usr/local/openjdk-11/bin/java $JAVA_OPTS -jar /opt/census-rm-notify-processor.jar
