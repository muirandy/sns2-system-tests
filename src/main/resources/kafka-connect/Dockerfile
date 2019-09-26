FROM confluentinc/cp-kafka-connect:5.3.0

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-activemq-sink:1.1.1

ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

RUN mkdir -p /opt/kafka-connect/jars
#RUN apt-get -y install curl

#Active MQ Sink Connector
#RUN curl -k -S "https://oss.sonatype.org/content/repositories/snapshots/com/aimyourtechnology/kafka-connect-amq-sink/0.1-SNAPSHOT/kafka-connect-amq-sink-0.1-20190903.082212-1-jar-with-dependencies.jar" -o /opt/kafka-connect/jars/kafka-connect-amq-sink-jar-with-dependencies.jar
#RUN mkdir -p /usr/share/java/kafka-connect-amq-sink
#RUN ln -s /opt/kafka-connect/jars/kafka-connect-amq-sink-jar-with-dependencies.jar /usr/share/java/kafka-connect-amq-sink/

#H2 DB driver
#RUN curl -k -S "http://central.maven.org/maven2/com/h2database/h2/1.4.197/h2-1.4.197.jar" -o /opt/kafka-connect/jars/h2-1.4.197.jar
#RUN ln -s /opt/kafka-connect/jars/h2-1.4.197.jar /usr/share/java/kafka-connect-jdbc/

#Zipkin
RUN curl -k -S "http://central.maven.org/maven2/no/sysco/middleware/kafka/kafka-interceptor-zipkin/0.4.2/kafka-interceptor-zipkin-0.4.2.jar" -o /opt/kafka-connect/jars/kafka-interceptor-zipkin-0.4.2.jar
RUN ln -s /opt/kafka-connect/jars/kafka-interceptor-zipkin-0.4.2.jar /usr/share/java/monitoring-interceptors/

#Oracle DB Driver
#RUN curl -k -S "http://repo.sns.sky.com:8081/artifactory/libs-releases/com/oracle/ojdbc6/11.2.0.4/ojdbc6-11.2.0.4.jar" -o /opt/kafka-connect/jars/ojdbc6-11.2.0.4.jar
#RUN ln -s /opt/kafka-connect/jars/ojdbc6-11.2.0.4.jar /usr/share/java/kafka-connect-jdbc/


#Debezium connectors
#SNAPSHOT_VERSION=$(curl -fSL $MAVEN_OSS_SNAPSHOT/io/debezium/debezium-connector-oracle/$DEBEZIUM_VERSION/maven-metadata.xml | awk -F'<[^>]+>' '/<extension>tar.gz<\/extension>/ {getline; print $2}'); \
#curl -fSL -o /tmp/plugin.tar.gz \
#             $MAVEN_OSS_SNAPSHOT/io/debezium/debezium-connector-oracle/$DEBEZIUM_VERSION/debezium-connector-oracle-$SNAPSHOT_VERSION-plugin.tar.gz &&\
#tar -xzf /tmp/plugin.tar.gz -C $KAFKA_CONNECT_PLUGINS_DIR && \
#rm -f /tmp/plugin.tar.gz; \
#done
