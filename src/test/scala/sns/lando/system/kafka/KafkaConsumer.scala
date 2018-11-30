package sns.lando.system.kafka

import kafka.utils.Logging

class KafkaConsumer(val brokers: String,
                    val groupId: String,
                    val topic: String) extends Logging {

}
