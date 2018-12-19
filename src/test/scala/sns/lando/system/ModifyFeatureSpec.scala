package sns.lando.system

import java.time.Duration
import java.util.{Collections, Properties, UUID}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.scalatest.{FunSpec, GivenWhenThen}

import scala.collection.JavaConverters._

class ModifyFeatureSpec extends FunSpec with GivenWhenThen {

  private val lluStreamMessagesTopic = "incoming.op.msgs"
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
  props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass.getName)


  var expectedKnitwareInstruction =
    """
      |<?xml version="1.0" encoding="UTF-8"?>
      |<switchServiceModificationInstruction switchServiceId="16" netstreamCorrelationId="33269793">
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
        """
          |<?xml version="1.0" encoding="UTF-8"?>
          |<transaction receivedDate="2018-11-15T10:29:07" operatorId="sky" operatorTransactionId="op_trans_id_095025_228" operatorIssuedDate="2011-06-01T09:51:12">
          |  <instruction version="1" type="PlaceOrder">
          |    <order>
          |      <type>modify</type>
          |      <operatorOrderId>SogeaVoipModify_YHUORO</operatorOrderId>
          |      <operatorNotes>Test: notes</operatorNotes>
          |      <orderId>33269793</orderId>
          |    </order>
          |    <modifyFeaturesInstruction serviceId="31642339" operatorOrderId="SogeaVoipModify_YHUORO" operatorNotes="Test: addThenRemoveStaticIpToAnFttcService">
          |      <features>
          |          <feature code="CallerDisplay"/>
          |          <feature code="RingBack"/>
          |          <feature code="ChooseToRefuse"/>
          |      </features>
          |    </modifyFeaturesInstruction>
          |  </instruction>
          |</transaction>
        """.stripMargin
      val messageUuid = UUID.randomUUID().toString

      When("LLU-Stream writes a valid Modify Features Message")
      val consumer = new KafkaConsumer[String, String](props)
      consumer.subscribe(Collections.singletonList(switchModificationTopic))
      val immediately = Duration.ofSeconds(0)
      consumer.poll(immediately)
      consumer.seekToBeginning(consumer.assignment)

      val lluStreamMessagesTopicProducer = new KafkaProducer[String, String](props)
      lluStreamMessagesTopicProducer.send(new ProducerRecord(lluStreamMessagesTopic, messageUuid, messageValue)).get()

      lluStreamMessagesTopicProducer.flush()
      lluStreamMessagesTopicProducer.close()

      Then("Knitware will receive an instruction to modify features")

      val duration = Duration.ofSeconds(2)
      val recs = consumer.poll(duration).asScala
      val last = recs.last
      assert(last.key() === messageUuid)
      val knitwareInstruction = last.value()
      assert(knitwareInstruction === expectedKnitwareInstruction)
    }

    it("should update router (ACS)") {

    }
  }
}