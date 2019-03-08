FROM confluentinc/cp-kafka-connect

RUN mkdir -p /opt/kafka-connect/jars
RUN apt-get -y update && apt-get -y install curl && apt-get install -y unzip
RUN curl -k -S "http://www.h2database.com/h2-2018-03-18.zip" -o /tmp/h2-2019-02-22.zip \
&&  unzip /tmp/h2-2019-02-22.zip h2/bin/h2-1.4.197.jar -d /opt/kafka-connect/jars h2/bin/h2-1.4.197.jar
RUN ln -s /opt/kafka-connect/jars/h2/bin/h2-1.4.197.jar /usr/share/java/kafka-connect-jdbc/