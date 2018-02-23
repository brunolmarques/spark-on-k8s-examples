# Spark Service Example Applications

Collection of stable application's examples for spark on kubernetes service 

NOTE: please mind that work is in progress and dependencies will change

### Prerequisites: prepare your local edge environment

- Install sbt to be able to build your jobs - if you want to build jobs

https://www.scala-sbt.org/1.0/docs/Installing-sbt-on-Linux.html

- Install docker-ce and add user to docker group (for ease of use)

https://docs.docker.com/install/linux/docker-ce/ubuntu/#install-docker-ce-1

```
sudo usermod -aG docker <your-user>
```

Remember to log out and back in for this to take effect!

- Get `spark-submit` script

Clone the Spark service repository, which contains spark-submit

```
kinit <your-username>
git clone https://:@gitlab.cern.ch:8443/db/spark-service/tools.git
```

### Running spark-submit jobs


Source the cluster configuration from `env.sh` file in your cluster config folder 

```
source /path/to/cluster-config/env.sh 
```

For more details on how to setup your spark on kubernetes cluster and get cluster config
visit [https://gitlab.cern.ch/db/spark-service/tools](https://gitlab.cern.ch/db/spark-service/tools)


- **SparkPi**

This is simple job, which requires only build jar. Please find yours at 
`/path/to/spark-service-examples/target/scala-2.11/spark-service-examples_2.11-0.0.1.jar`

```
./spark-submit \
--conf spark.executor.instances=5 \
--class org.sparkservice.sparkrootapplications.examples.SparkPi \
/home/mrow4a/Projects/spark-service-examples/target/scala-2.11/spark-service-examples_2.11-0.0.1.jar
```

- **EventsSelect**

This is more complex job, which requires not only build jar 
e.g. `/path/to/spark-service-examples/target/scala-2.11/spark-service-examples_2.11-0.0.1.jar`
but also `--jars` maven dependencies, which you can dynamically add. 
In this example, we use input file which is embedded inside the driver and executor image at `file://` location

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
