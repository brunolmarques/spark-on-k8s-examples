#!/usr/bin/python
import importlib
import os
import sys
from pyspark.sql import SparkSession
from pyspark.files import SparkFiles

if __name__ == '__main__':
    spark = SparkSession \
        .builder \
        .appName("WordCount") \
        .getOrCreate()

    # dependency should be submitted with --py-files examples/libs/wordcount_python_job.zip
    wordcount_job_path = SparkFiles.get("./wordcount_python_job.zip")
    spark.sparkContext.addPyFile(wordcount_job_path)

    job_module = importlib.import_module('jobs.wordcount')

    job_module.analyze(spark)
