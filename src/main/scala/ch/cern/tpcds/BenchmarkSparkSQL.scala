package ch.cern.tpcds

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.col
import org.apache.log4j.{Level, LogManager}
import scala.util.Try

object BenchmarkSparkSQL {
  def main(args: Array[String]) {
    val rootDir = args(0)
    val dsdgenDir = args(1)
    val scaleFactor = Try(args(2).toString).getOrElse("1")
    val iterations = args(3).toInt
    val optimizeQueries = Try(args(4).toBoolean).getOrElse(false)
    val filterQueries = Try(args(5).toString).getOrElse("")
    val onlyWarn = Try(args(6).toBoolean).getOrElse(false)

    val tpcdsDir = s"$rootDir/tpcds"
    val resultLocation = s"$rootDir/tpcds_result"
    val databaseName = "tpcds_db"
    val format = "parquet"
    val timeout = 24*60*60

    println(s"ROOT DIR is $rootDir")

    val spark = SparkSession
      .builder
      .appName(s"TPCDS SQL Benchmark $scaleFactor GB")
      .getOrCreate()

    if (onlyWarn) {
      println(s"Only WARN")
      LogManager.getLogger("org").setLevel(Level.WARN)
    }

    var query_filter : Seq[String] = Seq()
    if (!filterQueries.isEmpty) {
      println(s"Running only queries: $filterQueries")
      query_filter = filterQueries.split(",").toSeq
    }

    val tables = new TPCDSTables(spark.sqlContext,
      dsdgenDir = dsdgenDir,
      scaleFactor = scaleFactor,
      useDoubleForDecimal = false,
      useStringForDate = false)

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

    val filtered_queries = query_filter match {
      case Seq() => tpcds.tpcds2_4Queries
      case _ => tpcds.tpcds2_4Queries.filter(q => query_filter.contains(q.name))
    }

    val experiment = tpcds.runExperiment(
      filtered_queries,
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