# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service.

### Application examples

- [Spark Streaming Job](examples/kafka-streaming.yaml)
- [HDFS/YARN ETL Job](examples/yarn-hdfs-job.yaml)
- [Data generation for TPCDS with S3](examples/s3-tpcds-datagen.yaml)
- [TPCDS SQL Benchmark with S3](examples/s3-tpcds.yaml)
- [Distributed Events Select with ROOT/EOS](examples/public-eos-events-select.yaml)
- [Spark Streaming Data generator](examples/stream-generator.yaml)
- [Spark Structured Streaming Querying](examples/stream-query.yaml)

### Prerequisites

- Initialize the Kubernetes cluster with services needed to run Spark by following [Spark Kubernetes Cluster Admin Guide](http://spark-user-guide.web.cern.ch)

- Get kubernetes token allowing to submit spark applications to kubernetes cluster by following [Spark Kubernetes User Guide](http://spark-user-guide.web.cern.ch)

### Documentation

#### 1. Build docker image or use existing one

`gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples:v1.1-070519`

##### 1a. Building examples jars

Being in root folder of this repository, run:

```
$ sbt assembly
```

##### 1b. Building examples docker image (with assemled jar in target/ and deps jars in libs/)

Being in root folder of this repository, run:

```
$ IMAGE_VERSION=v1.1-$(date +'%d%m%y')
$ docker build -t gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples:$IMAGE_VERSION .
$ docker push gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples:$IMAGE_VERSION
```

#### 2. Test on Kubernetes cluster

##### Example applications

Full documentation on how to customize and work with Kubernetes Operator is available in [Spark Kubernetes Guide](http://spark-user-guide.web.cern.ch)

##### Spark Streaming JOB/CRONJOB

Submit job

```bash
$ kubectl delete job kafka
$ kubectl create -f ./examples/kafka-streaming-job.yaml
$ kubectl logs -l "app=kafka"
```

Find driver ui

```bash
$ kubectl get pod -l "app=kafka" -o yaml | grep host
http://<hostIP>:4040
```

##### HDFS ETL JOB/CRONJOB

Authenticate with e.g. krbcache or keytab

```bash
$ kubectl delete secret krb5
$ kubectl create secret generic krb5 --from-file=krb5cc="$(klist|grep FILE|cut -f3 -d":")"
```

Submit job

```bash
$ kubectl delete job hdfs-etl
$ kubectl create -f ./examples/yarn-hdfs-job.yaml
```

##### Structured Streaming with Operator

Authenticate with e.g. krbcache

```bash
$ kubectl delete secret krb5
$ kubectl create secret generic krb5 --from-file=krb5cc="$(klist|grep FILE|cut -f3 -d":")"
```

Submit generator

```bash
$ kubectl create -f ./examples/stream-generator.yaml
```

Submit reader

```bash
$ kubectl create -f ./examples/stream-queries.yaml
```

Check pod logs

```bash
$ kubectl logs -f stream-queries-driver
```

##### Events Selection with Operator

```
$ kubectl apply -f ./examples/eos-events-select.yaml
```

##### TPCDS Spark SQL with Operator

TPCDS is an example of complex application that will run Query 23a and save output to known location. First, dataset needs to be created [as described here](examples/tpcds-datagen.yaml). Fill `access` and `secret` below to be able to read/write to S3. When the creation of the test dataset is finished, continue with an actual [TPCDS Spark SQL job](examples/tpcds.yaml). 
