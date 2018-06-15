package ch.cern.sparkrootapplications.examples

import org.apache.spark.sql.SparkSession
import org.dianahep.sparkroot._

object EventsSelect {
  def main(args: Array[String]) {
    val inputPath = args(0)
    val spark = SparkSession.builder()
      .appName("AOD Public DS Example")
      .getOrCreate()

    // get the Dataset[Row] = Dataframe (from 2.0)
    val df = spark.sqlContext.read.option("tree", "Events").root(inputPath)

    // at this point at least...
    import spark.implicits._

    // select only AOD Collection of Muons. Now you have Dataset[Event].
    val dsMuons = df.select("recoMuons_muons__RECO_.recoMuons_muons__RECO_obj.reco::RecoCandidate.reco::LeafCandidate").toDF("muons")

    dsMuons.show(10, false)

    // stop the session/context
    spark.stop
  }
}
