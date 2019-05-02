package ch.cern.monitoring

// Spark stuff
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DataType, StringType, StructType}
import org.apache.spark.sql.functions.{col}

/**
  * An example class which shows how data can be extracted from HDFS,
  * transformed by appending string to each row and loaded (printed to screen)
  */
object HdfsETL {

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName(s"HdfsETL")
      .getOrCreate()

    val raw_data_path = "/project/itmon/archive/lemon/hadoop_ng/2018-03/"
    val schema_json= spark.read.json(raw_data_path).schema.json
    val newSchema=DataType.fromJson(schema_json).asInstanceOf[StructType]
    newSchema.printTreeString()


    import spark.implicits._
    val df = spark.read.schema(newSchema).json(raw_data_path)
    df.select(col("toplevel_hostgroup").cast(StringType))
      .map( row =>
        row.get(0) + "_cern"
      )
      .show(10)
    spark.stop()
  }
}
