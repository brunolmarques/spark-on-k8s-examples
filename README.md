# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service.

Full documentation is available in [spark user guide](http://spark-user-guide.web.cern.ch/spark-user-guide/spark-k8s/k8s_overview.html) 

### Application examples

- [Data generation for TPCDS](examples/tpcds-datagen.yaml)
- [TPCDS SQL Benchmark](examples/tpcds.yaml)
- [Distributed XRootD Public Events Select](examples/public-eos-events-select.yaml)

##### 1. Building examples jars

Being in root folder of this repository, run:

```
$ sbt package
```

Find your jar and move to `/libs` folder

```
<path-to-examples/spark-service-examples/target/scala-X.Y/spark-service-examples_-X.Y-Z.jar
```

##### 2. Building examples docker image

Being in root folder of this repository, run:

```
$ OUTPUT_IMAGE=gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples:v2.4.0-hadoop3-0.7
$ docker build --no-cache -t ${OUTPUT_IMAGE} .
$ docker push ${OUTPUT_IMAGE}
```

##### 3. Test on Kubernetes cluster

###### Events Selection

```
$ ./sparkctl create ./examples/public-eos-events-select.yaml
$ ./sparkctl log -f public-eos-events-select
```

###### TPCDS Spark SQL

TPCDS is an example of complex application that will run Query 23a and save output to known location. First, dataset needs to be created [as described here](examples/tpcds-datagen.yaml). Fill `access` and `secret` below to be able to read/write to S3. When the creation of the test dataset is finished, continue with an actual [TPCDS Spark SQL job](examples/tpcds.yaml). 
