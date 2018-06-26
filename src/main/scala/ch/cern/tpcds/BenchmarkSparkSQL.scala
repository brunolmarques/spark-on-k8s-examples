package ch.cern.tpcds

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.sql.SparkSession

import scala.util.Try

object BenchmarkSparkSQL {
  def main(args: Array[String]) {
    val rootDir = args(0)
    val dsdgenDir = args(1)
    val scaleFactor = args(2)
    val iterations = args(3).toInt
    val skipCreate = Try(args(4).toBoolean).getOrElse(false)

    val tpcdsDir = s"$rootDir/tpcds"
    val resultLocation = s"$rootDir/tpcds_result"
    val databaseName = "tpcds_db"
    val format = "parquet"
    val timeout = 24*60*60

    println(s"ROOT DIR is $rootDir")
    println(s"Tools dsdgen executable located in $dsdgenDir")
    println(s"Scale factor is $scaleFactor GB")

    val spark = SparkSession
      .builder
      .appName("TPCDS Benchmark")
      .getOrCreate()

    val tables = new TPCDSTables(spark.sqlContext,
      dsdgenDir = dsdgenDir,
      scaleFactor = scaleFactor,
      useDoubleForDecimal = false,
      useStringForDate = false)

    if (!skipCreate) {
      println(s"Generating data")

      tables.genData(
        location = tpcdsDir,
        format = format,
        overwrite = true,
        partitionTables = true,
        clusterByPartitionColumns = true,
        filterOutNullPartitionValues = false,
        tableFilter = "",
        numPartitions = 100)
    }

    Try {
      spark.sql(s"create database $databaseName")
    }
    tables.createExternalTables(tpcdsDir, "parquet", databaseName, overwrite = true, discoverPartitions = true)
    tables.analyzeTables(databaseName, analyzeColumns = true)

    val tpcds = new TPCDS(spark.sqlContext)
    val queries = tpcds.tpcds2_4Queries

    val experiment = tpcds.runExperiment(
      queries,
      iterations = iterations,
      resultLocation = resultLocation,
      forkThread = true)

    experiment.waitForFinish(timeout)
    val specificResultTable = spark.read.json(experiment.resultPath)
    specificResultTable.show()
  }
}