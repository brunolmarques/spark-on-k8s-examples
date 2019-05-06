package ch.cern.db

import java.util

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.config.{SaslConfigs, SslConfigs}
import org.apache.spark.sql.SparkSession

object StreamGenerator {

  def main(args: Array[String]): Unit = {

    if (args.length < 2){
      println("Input should be in the form <topic name> <file-name>")
      return
    }

    val file = args(0)
    val delay = args(1)
    val topic = args(2)
    val servers = args(3)
    val truststoreFile = args(4)
    val truststorePass = args(5)

    val spark= SparkSession.builder().appName("StreamGenerator").getOrCreate()
    val sc = spark.sparkContext

    // Read the binary file ad split it into records
    // recordLength = 8 bytes, 64 bits
    val rdd = sc.binaryRecords(file, 8).cache()

    // Count() to trigger cache()
    val numRecords = rdd.count()
    println("Number of records: "+numRecords.toString)

    while( true ) {
      val startTimer = System.currentTimeMillis()

      rdd.foreachPartition(partition => {

        val props = new util.HashMap[String, Object]()

        //Authentication
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers)
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
        props.put(SaslConfigs.SASL_MECHANISM, "GSSAPI")
        props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka")
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreFile)
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePass)

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

        val producer = new KafkaProducer[String, String](props)

        partition.foreach(record => {
          val msg = new ProducerRecord[String, String](topic, record.toString)
          producer.send(msg)
          Thread.sleep(delay.toInt)
        })
      })

      val stopTimer = System.currentTimeMillis()
      println("Elapsed time: %.1f s".format((stopTimer-startTimer).toFloat/1000))
      println("Sent %d msg/s".format((numRecords.toFloat/((stopTimer-startTimer).toFloat/1000)).toInt))
    }


    sc.stop()
  }

}
