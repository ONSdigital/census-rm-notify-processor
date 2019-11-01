FROM openjdk:11-jdk-slim
CMD ["/usr/local/openjdk-11/bin/java", "-jar", "/opt/census-rm-notify-processor.jar"]

COPY healthcheck.sh /opt/healthcheck.sh
RUN chmod +x /opt/healthcheck.sh

RUN groupadd --gid 999 notifyprocessor && \
    useradd --create-home --system --uid 999 --gid notifyprocessor notifyprocessor

USER notifyprocessor

ARG JAR_FILE=census-rm-notify-processor*.jar
COPY target/$JAR_FILE /opt/census-rm-notify-processor.jar