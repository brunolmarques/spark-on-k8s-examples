package ch.cern.db

import java.nio.{ByteBuffer, ByteOrder}

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.streaming.{StreamingQuery, Trigger}

object StreamQuery {

  def main(args: Array[String]): Unit = {

    if (args.length < 1){
      println("Input should be in the form <topic name>")
      System.exit(1)
    }

    val topic = args(0)
    val servers = args(1)
    val truststoreFile = args(2)
    val truststorePass = args(3)

    val spark: SparkSession = SparkSession
      .builder
      .appName("StreamIngestion")
      .getOrCreate()

    import spark.implicits._

    // Dont overload with logs
    spark.sparkContext.setLogLevel("Warn")

    val inputDF = spark
      .readStream.format("kafka")
      .option("subscribe", topic)
      .option("kafka.bootstrap.servers", servers)
      .option("kafka.security.protocol", "SASL_SSL")
      .option("kafka.sasl.mechanism", "GSSAPI")
      .option("kafka.sasl.kerberos.service.name", "kafka")
      .option("kafka.ssl.truststore.location", truststoreFile)
      .option("kafka.ssl.truststore.password", truststorePass)
      .load()
      .select($"value".as("event"))

    val convertUDF = udf((record: Array[Byte]) => {

      try {
        val bb = ByteBuffer.wrap(record).order(ByteOrder.nativeOrder())
        val buffer = bb.getLong // get 8 bytes

        val HEAD        = ((buffer >> 62) & 0x3).toInt
        val FPGA        = ((buffer >> 58) & 0xF).toInt
        val TDC_CHANNEL = ((buffer >> 49) & 0x1FF).toInt
        val ORBIT_CNT   = ((buffer >> 17) & 0xFFFFFFFF).toInt
        val BX_COUNTER  = ((buffer >> 5) & 0xFFF).toInt
        val TDC_MEANS   = ((buffer >> 0) & 0x1F).toInt

        Array(HEAD, FPGA, TDC_CHANNEL, ORBIT_CNT, BX_COUNTER, TDC_MEANS)
      } catch {
        case e: Exception =>
          Array(0, 0, 0, 0, 0, 0)
      }
    })

    val convertedDF = inputDF
      .withColumn("converted", convertUDF(inputDF("event")))
      .select(
        $"converted"(0).as("HEAD"),
        $"converted"(1).as("FPGA"),
        $"converted"(2).as("TDC_CHANNEL"),
        $"converted"(3).as("ORBIT_CNT"),
        $"converted"(4).as("BX_COUNTER"),
        $"converted"(5).as("TDC_MEANS")
      )

    // Run a live query to count number of msg for each FPGA
    val occupancyDF = convertedDF
      .groupBy("FPGA")
      .count()
    val query = occupancyDF.writeStream
      .outputMode("complete")
      .format("console")
      .start()
    query.awaitTermination()

    spark.sparkContext.stop()
  }

}