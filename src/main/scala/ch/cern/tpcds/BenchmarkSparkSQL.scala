package ch.cern.tpcds

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions.col

import scala.util.Try

object BenchmarkSparkSQL {
  def main(args: Array[String]) {
    val rootDir = args(0)
    val dsdgenDir = args(1)
    val scaleFactor = args(2)
    val iterations = args(3).toInt
    val skipCreate = Try(args(4).toBoolean).getOrElse(false)
    val optimizeQueries = Try(args(5).toBoolean).getOrElse(false)
    val genPartitions = Try(args(6).toInt).getOrElse(100)

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
      .appName(s"TPCDS Benchmark $scaleFactor GB")
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
        numPartitions = genPartitions)
    }

    if (optimizeQueries) {
      Try {
        spark.sql(s"create database $databaseName")
      }
      tables.createExternalTables(tpcdsDir, "parquet", databaseName, overwrite = true, discoverPartitions = true)
      tables.analyzeTables(databaseName, analyzeColumns = true)
      spark.conf.set("spark.sql.cbo.enabled", "true")
    } else {
      tables.createTemporaryTables(tpcdsDir, "parquet")
    }

    val tpcds = new TPCDS(spark.sqlContext)
    val queries = tpcds.tpcds2_4Queries

    val experiment = tpcds.runExperiment(
      queries,
      iterations = iterations,
      resultLocation = resultLocation,
      forkThread = true)

    experiment.waitForFinish(timeout)

    val resultPath = experiment.resultPath
    println(s"Reading result at $resultPath")
    val specificResultTable = spark.read.json(resultPath)
    specificResultTable.show()

    val result = specificResultTable.withColumn("result", explode(col("results"))).withColumn("executionSeconds", col("result.executionTime")/1000).withColumn("queryName", col("result.name"))
    result.select("iteration", "queryName", "executionSeconds").show()

    println(s"Final results at $resultPath")
    val aggResults = result.groupBy("queryName").agg(callUDF("percentile", col("executionSeconds").cast("long"), lit(0.5)).as('medianRuntimeSeconds)).orderBy(col("queryName"))
    aggResults.repartition(1).write.csv(s"$resultPath/csv")
    aggResults.show(105)
  }
}