# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service.

Full documentation is available in [spark user guide](http://spark-user-guide.web.cern.ch/spark-user-guide/spark-k8s/k8s_overview.html) 

### Application examples

- [HDFS ETL](examples/yarn-hdfs-job.yaml)
- [Data generation for TPCDS with S3](examples/s3-tpcds-datagen.yaml)
- [TPCDS SQL Benchmark with S3](examples/s3-tpcds.yaml)
- [Distributed Events Select with ROOT/EOS](examples/public-eos-events-select.yaml)

##### 1. Build docker image or use existing one

`gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples:v1.0`

###### 1a. Building examples jars

Being in root folder of this repository, run:

```
$ sbt package
```

Find your jar and move to `/libs` folder

```
mv <path-to-examples>/spark-k8s-examples/target/scala-X.Y/spark-k8s-examples-assembly-X.Y.jar <path-to-examples>/spark-k8s-examples/libs
```

###### 1b. Building examples docker image

Being in root folder of this repository, run:

```
$ OUTPUT_IMAGE=gitlab-registry.cern.ch/db/spark-service/spark-k8s-examples:vX.Y
$ docker build --no-cache -t ${OUTPUT_IMAGE} .
$ docker push ${OUTPUT_IMAGE}
```

##### 2. Test on Kubernetes cluster

###### HDFS ETL JOB/CRONJOB

Authenticate with e.g. krbcache or keytab

```bash
$ kubectl delete secret krb5
$ kubectl create secret generic krb5 --from-file=krb5cc=$KRB5CCNAME
```

Submit job

```bash
$ kubectl create -f ./examples/yarn-hdfs-job.yaml
```

###### Events Selection

TODO: verify still works

```
$ kubectl apply -f ./examples/eos-events-select.yaml
```

###### TPCDS Spark SQL

TODO: verify still works

TPCDS is an example of complex application that will run Query 23a and save output to known location. First, dataset needs to be created [as described here](examples/tpcds-datagen.yaml). Fill `access` and `secret` below to be able to read/write to S3. When the creation of the test dataset is finished, continue with an actual [TPCDS Spark SQL job](examples/tpcds.yaml). 
