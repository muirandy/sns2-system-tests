FROM confluentinc/cp-kafka-connect:5.3.0

RUN mkdir -p /opt/kafka-connect/jars
RUN apt-get -y install curl
RUN curl -k -S "http://central.maven.org/maven2/com/h2database/h2/1.4.197/h2-1.4.197.jar" -o /opt/kafka-connect/jars/h2-1.4.197.jar
RUN ln -s /opt/kafka-connect/jars/h2-1.4.197.jar /usr/share/java/kafka-connect-jdbc/
RUN curl -k -S "http://central.maven.org/maven2/no/sysco/middleware/kafka/kafka-interceptor-zipkin/0.4.2/kafka-interceptor-zipkin-0.4.2.jar" -o /opt/kafka-connect/jars/kafka-interceptor-zipkin-0.4.2.jar
RUN ln -s /opt/kafka-connect/jars/kafka-interceptor-zipkin-0.4.2.jar /usr/share/java/monitoring-interceptors/

#Debezium connectors
#RUN for CONNECTOR in {oracle}; do \
#    SNAPSHOT_VERSION=$(curl -fSL $MAVEN_OSS_SNAPSHOT/io/debezium/debezium-connector-$CONNECTOR/$DEBEZIUM_VERSION/maven-metadata.xml | awk -F'<[^>]+>' '/<extension>tar.gz<\/extension>/ {getline; print $2}'); \
#    curl -fSL -o /tmp/plugin.tar.gz \
#                 $MAVEN_OSS_SNAPSHOT/io/debezium/debezium-connector-$CONNECTOR/$DEBEZIUM_VERSION/debezium-connector-$CONNECTOR-$SNAPSHOT_VERSION-plugin.tar.gz &&\
#    tar -xzf /tmp/plugin.tar.gz -C $KAFKA_CONNECT_PLUGINS_DIR && \
#    rm -f /tmp/plugin.tar.gz; \
#    done