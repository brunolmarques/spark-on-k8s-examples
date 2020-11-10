# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service.

### Access CVMFS and EOS

CVMFS and EOS are the typical resources required for ROOT analysis with spark

```
# Obtain KUBECONFIG from SWAN (select 'cloud containers') or contact it-hadoop-support@cern.ch
# from SWAN terminal /spark/k8s-user.config
export KUBECONFIG=/path/to/kubeconfig

# propagate krb cache with kubernetes token mechansim
export KRB5CCNAME=/tmp/krb5cc_$USER
kubectl create secret generic krb5ccname --from-file=$KRB5CCNAME -n $USER

# source /cvmfs/sft-nightlies.cern.ch/lcg/views/dev3python3/latest/x86_64-centos7-gcc10-opt/setup.sh
import os
from pyspark import SparkConf, SparkContext

config = {
    "spark.master": "k8s://https://188.184.157.211:6443",
    "spark.executor.instances": 3,
    "spark.kubernetes.authenticate.driver.serviceAccountName": "spark",
    "spark.kubernetes.container.image": "gitlab-registry.cern.ch/db/spark-service/docker-registry/swan:v3",
    "spark.kubernetes.container.image.pullPolicy": "IfNotPresent",
    "spark.kubernetes.executor.secrets.krb5ccname": "/tokens",
    "spark.executorEnv.KRB5CCNAME": "/tokens/krb5cc_0",
    "spark.kubernetes.executor.volumes.persistentVolumeClaim.cvmfs-sft.mount.path": "/cvmfs/sft.cern.ch",
    "spark.kubernetes.executor.volumes.persistentVolumeClaim.cvmfs-sft.mount.readOnly": "true",
    "spark.kubernetes.executor.volumes.persistentVolumeClaim.cvmfs-sft.options.claimName": "cvmfs-sft-cern-ch-pvc",
    "spark.kubernetes.executor.volumes.persistentVolumeClaim.sft-nightlies-cern-ch.mount.path": "/cvmfs/sft-nightlies.cern.ch",
    "spark.kubernetes.executor.volumes.persistentVolumeClaim.sft-nightlies-cern-ch.mount.readOnly": "true",
    "spark.kubernetes.executor.volumes.persistentVolumeClaim.sft-nightlies-cern-ch.options.claimName": "cvmfs-sft-nightlies-cern-ch-pvc",
    "spark.kubernetes.namespace": os.environ.get('SPARK_USER'),
    "spark.executorEnv.PYTHONPATH": os.environ.get('PYTHONPATH'),
    "spark.executorEnv.JAVA_HOME": os.environ.get('JAVA_HOME'),
    "spark.executorEnv.SPARK_HOME": os.environ.get('SPARK_HOME'),
    "spark.executorEnv.SPARK_EXTRA_CLASSPATH": os.environ.get('SPARK_DIST_CLASSPATH'),
    "spark.executorEnv.LD_LIBRARY_PATH": os.environ.get('LD_LIBRARY_PATH'),
    "spark.network.timeout": 60,
    "spark.task.maxDirectResultSize": 10000000000,
}

sparkConf = SparkConf().setAll(config.items())

sc = SparkContext.getOrCreate(sparkConf)

def ROOT_version(range):
    import ROOT
    return ROOT.__version__

def reduce_ROOT_version(v1, v2):
    return ",".join([v1,v2])

print(sc.parallelize(range(3)).map(ROOT_version).reduce(reduce_ROOT_version))
```

### Access S3

Additional parameters required for accessing S3, you may also need S3 jars if you are not using CVMFS

```
--conf spark.hadoop.fs.s3a.impl="org.apache.hadoop.fs.s3a.S3AFileSystem"
--conf spark.hadoop.fs.s3a.endpoint="s3.cern.ch"
--conf spark.hadoop.fs.s3a.access.key="XXXXXXXXXXXXX"
--conf spark.hadoop.fs.s3a.secret.key="xxxxxxxxxxxxxxxxxx"
```

### Access HDFS

In addition to krb conf

```
export HADOOP_CONF_DIR=/cvmfs/sft.cern.ch/lcg/etc/hadoop-confext/conf/etc/analytix/hadoop.analytix
--conf spark.executorEnv.HADOOP_CONF_DIR=/cvmfs/sft.cern.ch/lcg/etc/hadoop-confext/conf/etc/analytix/hadoop.analytix
```

### Archived Application examples based on spark operator (no longer recommended)

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
