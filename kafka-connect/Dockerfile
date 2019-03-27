FROM confluentinc/cp-kafka-connect:5.1.2

RUN mkdir -p /opt/kafka-connect/jars
RUN apt-get -y install curl
RUN curl -k -S "http://central.maven.org/maven2/com/h2database/h2/1.4.197/h2-1.4.197.jar" -o /opt/kafka-connect/jars/h2-1.4.197.jar
RUN ln -s /opt/kafka-connect/jars/h2-1.4.197.jar /usr/share/java/kafka-connect-jdbc/
RUN curl -k -S "http://central.maven.org/maven2/no/sysco/middleware/kafka/kafka-interceptor-zipkin/0.4.2/kafka-interceptor-zipkin-0.4.2.jar" -o /opt/kafka-connect/jars/kafka-interceptor-zipkin-0.4.2.jar
RUN ln -s /opt/kafka-connect/jars/kafka-interceptor-zipkin-0.4.2.jar /usr/share/java/monitoring-interceptors/