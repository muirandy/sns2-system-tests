package sns.lando.system

import java.util.{Collections, Properties, UUID}

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.scalatest.{FunSpec, GivenWhenThen}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class FirstSpec extends FunSpec with GivenWhenThen {

  private val incomingTopic = "incoming.op.msgs"
  private val KafkaDeserializer = "org.apache.kafka.common.serialization.StringDeserializer"
  private val KafkaSerializer = "org.apache.kafka.common.serialization.StringSerializer"

  private val props = new Properties()
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
//  props.put("acks", "all")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaSerializer)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaSerializer)
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaDeserializer)
  props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass.getName)
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1")


  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(Collections.singletonList(incomingTopic))


  describe("SNS") {
    it("should accept valid operator messages") {
      Given("A valid operator message")

      val message = s"Hello Kafka, UUID: ${UUID.randomUUID().toString}"

      When("a valid operator message is received by SNS")

      val producer = new KafkaProducer[String, String](props)

      var future = Future {

        System.out.println("Started future execution")
        var records = consumer.poll(100).asScala
        System.out.println("Future: After first poll")
        consumer.commitSync()

        val deadline = 2.seconds.fromNow

        while (deadline.hasTimeLeft) {
          val r = consumer.poll(100).asScala
          records = records ++ r

          System.out.println("Attempt to read = " + r.size + " and = " + records.size)
          consumer.commitSync()
        }

        System.out.println("Done future execution with = " + records.size)

        consumer.close()

        records
      }

      producer.send(new ProducerRecord(incomingTopic, "operatorMessage", message)).get()
      producer.send(new ProducerRecord(incomingTopic, "operatorMessage", message)).get()

      System.out.println("Attempt to sent message")
      producer.flush()
      producer.close()

      //      val r = consumer.poll(500).asScala
      //      consumer.commitSync()
      //      System.out.println("Number of records = " + r.size)
      //      r.foreach(e =>
      //      System.out.println("key = " + e.key() + ", value = " + e.value())
      //      )



      Then("SNS accepts the valid message")
      Await.ready(future, 2200.millis)//3.seconds)
      future.map {
        records =>
          assert(records.size === 1)
          println(s"${records.size} records")
          assert(records.last.value() === message)
      }
      future onComplete {
        case Success(records) => {
          assert(records.size === 1)
          assert(records.last.value() === message)
        }
        case Failure(t) => fail(s"An error occured: ${t.getMessage}")
      }
    }
  }

}
