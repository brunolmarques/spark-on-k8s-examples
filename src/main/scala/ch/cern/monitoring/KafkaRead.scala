package ch.cern.monitoring

// Spark stuff
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

// JSON serialization
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * An example class which shows how data can be read from Kafka with
  * Spark Streaming.
  * In this example, I will query Kafka directly, instead of via Zookeeper,
  * which I used in the Python examples.
  * For differences between the two, see these articles:
  * - https://spark.apache.org/docs/1.5.0/streaming-kafka-integration.html
  * - https://databricks.com/blog/2015/03/30/improvements-to-kafka-integration-of-spark-streaming.html
  *
  * This example just showcases that both ways are possible in our cluster.
  * Created by Daniel Zolnai on 07/03/16.
  */
object KafkaRead {

  private val BUCKET_SIZE = 15

  // These case classes represent the structure of the message,
  // to deserialize the JSON messages into
  case class BodyFileMetadata(filesize: Option[Long])

  case class MessageData(fileMetadata: Option[BodyFileMetadata],
                         srcHostname: String,
                         dstHostname: String)

  case class FtsRawCompleteMessage(data: MessageData)

  // Used for converting between String, Date, Long, etc. and JSON
  implicit val formats = DefaultFormats


  def main(args: Array[String]) {
    // Create context with the predefined BUCKET_SIZE second batch interval
    val topic = args(0)
    val brokerUrl = args(1)
    val sparkConf = new SparkConf().setAppName("KafkaRead")
    val ssc = new StreamingContext(sparkConf, Seconds(BUCKET_SIZE))

    // Create direct kafka stream with brokers and topics
    val topicsSet = Array(topic)
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokerUrl,
      "group.id" -> "kafka_read_spark_examples",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer])

    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topicsSet, kafkaParams))

    val transfersRdd = messages.map(_.value)
      .map((msgBody: String) => parse(msgBody)
        .transformField {
          case ("t__error_message", x) => ("t_error_message", x) // camelizeKeys crashes on the double underscore
        }
        .camelizeKeys // Is needed because "src_hostname" should be converted to "srcHostname"
        .extract[FtsRawCompleteMessage].data)
      .map(getSimpleStats)

    transfersRdd.print()

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }

  def getSimpleStats(messageData: MessageData): String = {
    "From: " + messageData.srcHostname +
      "; To: " + messageData.dstHostname +
      "; Size: " + getSize(messageData)
  }

  def getSize(messageData: MessageData): Long = {
    if (messageData.fileMetadata.isDefined && messageData.fileMetadata.get.filesize.isDefined) {
      return messageData.fileMetadata.get.filesize.get
    }
    0
  }
}
