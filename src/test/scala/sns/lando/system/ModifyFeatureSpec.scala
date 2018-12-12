package sns.lando.system

import java.lang.Thread.sleep
import java.time.Duration
import java.util.{Collections, Properties, UUID}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.scalatest.{FunSpec, GivenWhenThen}

import scala.collection.JavaConverters._

class ModifyFeatureSpec extends FunSpec with GivenWhenThen {

  private val operatorMessagesTopic = "incoming.op.msgs"
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
    """

  describe("SNS") {
    it("should update switch (Knitware)") {
      Given("A valid operator message")
      val messageValue = "Hello Kafka - UUID is: ${UUID.randomUUID().toString}"
      val uuid = UUID.randomUUID().toString

      When("a valid operator message is received by SNS")
      val operatorMessagesTopicProducer = new KafkaProducer[String, String](props)
      operatorMessagesTopicProducer.send(new ProducerRecord(operatorMessagesTopic, uuid, messageValue)).get()

      operatorMessagesTopicProducer.flush()
      operatorMessagesTopicProducer.close()

      Then("Knitware will receive an instruction")
      val consumer = new KafkaConsumer[String, String](props)
      consumer.subscribe(Collections.singletonList(switchModificationTopic))

      sleep(2000)
      val duration = Duration.ofSeconds(1)
      val recs = consumer.poll(duration).asScala
      val last = recs.last
      assert(last.key() === uuid)
      val knitwareInstruction = last.value()
      assert(knitwareInstruction === expectedKnitwareInstruction)
    }

    it("should update router (ACS)") {

    }
  }
}