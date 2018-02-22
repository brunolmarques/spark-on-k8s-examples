# Spark Service Example Applications
Collection of stable application's examples for spark on kubernetes service 

```
./spark-submit \
--conf spark.executor.instances=5 \
--conf spark.driver.extraClassPath="/usr/lib/hadoop/EOSfs.jar" \
--class org.sparkservice.sparkrootapplications.examples.EventsSelect \
--jars \
http://central.maven.org/maven2/org/diana-hep/spark-root_2.11/0.1.11/spark-root_2.11-0.1.11.jar,\
http://central.maven.org/maven2/org/diana-hep/histogrammar-sparksql_2.11/1.0.3/histogrammar-sparksql_2.11-1.0.3.jar,\
http://central.maven.org/maven2/org/diana-hep/root4j/0.1.6/root4j-0.1.6.jar,\
http://central.maven.org/maven2/org/diana-hep/histogrammar_2.11/1.0.3/histogrammar_2.11-1.0.3.jar,\
http://central.maven.org/maven2/org/apache/bcel/bcel/5.2/bcel-5.2.jar,\
http://central.maven.org/maven2/org/tukaani/xz/1.2/xz-1.2.jar,\
http://central.maven.org/maven2/jakarta-regexp/jakarta-regexp/1.4/jakarta-regexp-1.4.jar \
/home/mrow4a/Projects/spark-service-examples/target/scala-2.11/spark-service-examples_2.11-0.0.1.jar \
file:///opt/spark/examples/test.root
```