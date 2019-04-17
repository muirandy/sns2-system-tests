package sns.lando.system

import java.time.Duration
import java.util.{Collections, Properties, UUID}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.scalatest.{FunSpec, GivenWhenThen}

import scala.collection.JavaConverters._
import scala.util.Random

class ModifyFeatureSpec extends FunSpec with GivenWhenThen {

  private val lluStreamMessagesTopic = "INCOMING_OP_MSGS"
  private val switchModificationTopic = "switch.modification.instructions"
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

  var orderId = Random.nextLong().toString
  var opereratorOrderId = Random.nextLong().toString

  var expectedKnitwareInstruction =
    s"""|<?xml version="1.0" encoding="UTF-8"?>
      |<switchServiceModificationInstruction switchServiceId="1" netstreamCorrelationId="${orderId}">
      |  <features>
      |    <callerDisplay active="true"/>
      |    <ringBack active="true"/>
      |    <chooseToRefuse active="true"/>
      |  </features>
      |</switchServiceModificationInstruction>
    """.stripMargin

  describe("SNS") {
    it("should update switch (Knitware)") {
      Given("A valid LLU-Stream Modify Features Message")
      val messageValue =
        s"""|<?xml version="1.0" encoding="UTF-8"?>
          |<transaction receivedDate="2018-11-15T10:29:07" operatorId="sky" operatorTransactionId="op_trans_id_095025_228" operatorIssuedDate="2011-06-01T09:51:12">
          |  <instruction version="1" type="PlaceOrder">
          |    <order>
          |      <type>modify</type>
          |      <operatorOrderId>SogeaVoipModify_${opereratorOrderId}</operatorOrderId>
          |      <operatorNotes>Test: notes</operatorNotes>
          |      <orderId>${orderId}</orderId>
          |    </order>
          |    <modifyFeaturesInstruction serviceId="31642339" operatorOrderId="SogeaVoipModify_${opereratorOrderId}" operatorNotes="Test: addThenRemoveStaticIpToAnFttcService">
          |      <features>
          |          <feature code="CallerDisplay"/>
          |          <feature code="RingBack"/>
          |          <feature code="ChooseToRefuse"/>
          |      </features>
          |    </modifyFeaturesInstruction>
          |  </instruction>
          |</transaction>
        """.stripMargin


      When("LLU-Stream writes a valid Modify Features Message")
      val consumer = new KafkaConsumer[String, String](props)
      consumer.subscribe(Collections.singletonList(switchModificationTopic))
      val immediately = Duration.ofSeconds(0)
      consumer.poll(immediately)

      val lluStreamMessagesTopicProducer = new KafkaProducer[String, String](props)
      lluStreamMessagesTopicProducer.send(new ProducerRecord(lluStreamMessagesTopic, orderId, messageValue)).get()

      lluStreamMessagesTopicProducer.flush()
      lluStreamMessagesTopicProducer.close()

      Then("Knitware will receive an instruction to modify features")

      val duration = Duration.ofSeconds(4)
      val recs = consumer.poll(duration).asScala
      val last = recs.last
      assert(last.key() === orderId)
      val knitwareInstruction = last.value()
      assert(knitwareInstruction === expectedKnitwareInstruction)
    }

    it("should update router (ACS)") {

    }
  }
}