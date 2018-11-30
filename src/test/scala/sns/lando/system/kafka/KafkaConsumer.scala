package sns.lando.system.kafka

import java.util.Properties

import kafka.utils.Logging
import org.apache.kafka.clients.consumer.ConsumerConfig

class KafkaConsumer(val brokers: String,
                    val groupId: String,
                    val topic: String) extends Logging {

  private val KafkaEnableAutoCommit = "true"
  private val AutoCommitIntervalMillis = "1000"
  private val SessionTimeoutMillisValue = "30000"
  private val KafkaDeserializer = "org.apache.kafka.common.serialization.StringDeserializer"

  private val props = createConsumerConfig(brokers, groupId)


  private def createConsumerConfig(brokers: String, groupId: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, KafkaEnableAutoCommit)
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, AutoCommitIntervalMillis)
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SessionTimeoutMillisValue)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
    props
  }
}
