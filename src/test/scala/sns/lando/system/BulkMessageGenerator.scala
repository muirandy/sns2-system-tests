package sns.lando.system

import java.time.Duration
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.util.Random

object BulkMessageGenerator extends App {
  private val lluStreamMessagesTopic = "incoming.op.msgs"
  private val KafkaDeserializer = "org.apache.kafka.common.serialization.StringDeserializer"
  private val KafkaSerializer = "org.apache.kafka.common.serialization.StringSerializer"


  private val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put("acks", "all")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaSerializer)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaSerializer)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass.getName)


  val operatorIds = Seq("sky", "nowTv")
  val random = new Random


  val lluStreamMessagesTopicProducer = new KafkaProducer[String, String](props)


  while (true) {
    val orderId = generateOrderId
    lluStreamMessagesTopicProducer.send(new ProducerRecord(lluStreamMessagesTopic, orderId, messageValue(orderId))).get()
    Thread.sleep(1000)
  }


  sys.ShutdownHookThread {
    lluStreamMessagesTopicProducer.flush()
    lluStreamMessagesTopicProducer.close()
  }

  def generateOrderId = Random.nextLong().abs.toString
  def opereratorOrderId = Random.nextLong().abs.toString

  def getRandomElement(list: Seq[String]): String =
    list(random.nextInt(list.length))

  def messageValue(orderId: String) = {
    val id = opereratorOrderId
    s"""|<?xml version="1.0" encoding="UTF-8"?>
        |<transaction receivedDate="2018-11-15T10:29:07" operatorId="${getRandomElement(operatorIds)}" operatorTransactionId="op_trans_id_095025_228" operatorIssuedDate="2011-06-01T09:51:12">
        |  <instruction version="1" type="PlaceOrder">
        |    <order>
        |      <type>modify</type>
        |      <operatorOrderId>SogeaVoipModify_$id</operatorOrderId>
        |      <operatorNotes>Test: notes</operatorNotes>
        |      <orderId>$orderId</orderId>
        |    </order>
        |    <modifyFeaturesInstruction serviceId="31642339" operatorOrderId="SogeaVoipModify_$id" operatorNotes="Test: addThenRemoveStaticIpToAnFttcService">
        |      <features>
        |          <feature code="CallerDisplay"/>
        |          <feature code="RingBack"/>
        |          <feature code="ChooseToRefuse"/>
        |      </features>
        |    </modifyFeaturesInstruction>
        |  </instruction>
        |</transaction>
        """.stripMargin
  }
}