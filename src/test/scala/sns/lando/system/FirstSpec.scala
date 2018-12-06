package sns.lando.system

import java.lang.Thread.sleep
import java.time.Duration
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.scalatest.{FunSpec, GivenWhenThen}

import scala.collection.JavaConverters._


class FirstSpec extends FunSpec with GivenWhenThen {

  private val incomingTopic = "incoming.op.msgs"
  private val KafkaDeserializer = "org.apache.kafka.common.serialization.StringDeserializer"
  private val KafkaSserializer = "org.apache.kafka.common.serialization.StringSerializer"

  private val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  props.put("acks", "all")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaSserializer)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaSserializer)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
  props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass.getName)

  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(Collections.singletonList(incomingTopic))


  describe("SNS") {
    it("should accept valid operator messages") {
      Given("A valid operator message")

      val messageValue = "Hello Kafka - UUID is: ${UUID.randomUUID().toString}"

      When("a valid operator message is received by SNS")
      //      val future = {
      val producer = new KafkaProducer[String, String](props)

//      var future = Future {
//        val records = Seq()
//        val deadline = 2.seconds.fromNow
//
//        while (deadline.hasTimeLeft)
//          records ++ consumer.poll(100).asScala
//
//        consumer.commitSync()
//        records
//      }

      producer.send(new ProducerRecord(incomingTopic, "operatorMessage", messageValue)).get()

      producer.flush()
      producer.close()
//      future
      //      }

      Then("SNS accepts the valid message")
//      Await.ready(future, 2.seconds)
      sleep(2000)
      val duration = Duration.ofSeconds(1)
      val recs = consumer.poll(duration).asScala
      val last = recs.last
      assert(last.value() === messageValue)

//      assert(recs.count() == 99)
//      val rec = future.value.get
//      assert(rec ===
//      future onComplete {
//        case Success(records) => {
//          assert(records.length === 1)
//        }
//        case Failure(t) => fail(s"An error occured: ${t.getMessage}")
//      }
    }
  }

}
