# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service 

### Table of Contents

- [Prerequisites](#prerequisites)
- [Create and manage Spark on Kubernetes cluster](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl)
- [Submitting Spark applications](#submitting-spark-applications)
    - [Managing simple application](#managing-simple-application)
    - [Using Webhooks example](#using-webhooks-(customize-driver/executor))
    - [Local Dependencies example](#local-dependencies-example)
    - [Python examples](#python-examples)
    - [Application examples](#application-examples)
- [Building examples jars](#building-examples-jars)
- [Building docker image with examples](#building-examples-docker-image)

### Prerequisites

- Install Kubernetes cluster and deploy Spark K8S Operator, 
instruction at [http://cern.ch/spark-user-guide](http://spark-user-guide.web.cern.ch/spark-user-guide/spark-k8s/k8s_overview.html)  

- Install `sparkctl` tool to interact with your kubernetes cluster. 

    ```
    MAC:
    $ wget https://s3.cern.ch/binaries/sparkctl/mac/latest/sparkctl
    LINUX:
    $ wget https://s3.cern.ch/binaries/sparkctl/linux/latest/sparkctl
    ```
    ```
    $ chmod +x sparkctl
    $ ./sparkctl --help
    ```

- Test that sparkctl can access Spark K8S Operator
    ```
    $ export PATH=[path-to-sparkctl-dir]:$PATH
    $ sparkctl list 
    ```

### Submitting Spark applications

##### Managing simple application

The most important sections of your SparkApplication are:

- Application name
    ```
    metadata:
      name: spark-pi
    ```
- Application file
    ```
    spec:
      mainApplicationFile: "local:///opt/spark/examples/jars/spark-service-examples.jar"
    ```
- Application main class
    ```
    spec:
      mainClass: ch.cern.sparkrootapplications.examples.SparkPi
    ```

To submit application

```
$ cat <<EOF >>spark-pi.yaml
apiVersion: "sparkoperator.k8s.io/v1alpha1"
kind: SparkApplication
metadata:
  name: spark-pi
  namespace: default
spec:
  type: Scala
  mode: cluster
  image: gitlab-registry.cern.ch/db/spark-service/docker-registry/spark:v2.4.0-hadoop3.1-examples
  imagePullPolicy: Always
  mainClass: ch.cern.sparkrootapplications.examples.SparkPi
  mainApplicationFile: local:///opt/spark/examples/jars/spark-service-examples_2.11-0.3.0.jar
  mode: cluster
  driver:
    cores: 1
    coreLimit: "1000m"
    memory: "1024m"
    serviceAccount: spark
  executor:
    instances: 1
    cores: 1
    memory: "1024m"
  restartPolicy: Never
EOF
 
$ sparkctl create spark-pi.yaml
```

To delete application

```
$ sparkctl delete spark-pi
```

Check if your driver/executors are correctly created

```
$ sparkctl event spark-pi
```

To get application logs

```
$ sparkctl log spark-pi
```

To check application status

```
$ sparkctl status spark-pi
```

To access driver UI (forwarded to localhost:4040 from where sparctl is executed)

```
$ sparkctl forward spark-pi
```

Alternatively, to check application status (or check created pods and their status)

```
$ kubectl describe sparkapplication spark-pi
or
$ kubectl get pods -n default
or
$ kubectl describe pod spark-pi-1528991055721-driver
or
$ kubectl logs spark-pi-1528991055721-driver
```

For more details regarding `sparkctl`, and more detailed user guide, 
please visit [sparkctl user-guide](https://github.com/cerndb/spark-on-k8s-operator/tree/master/sparkctl)

##### Using webhooks (customize driver/executor)

Webhooks are used to customize driver/executor pod for using CephFS, Hadoop config, CVMFS, pod affinity etc.

Example of customizing driver/executors with custom hadoop config is shown below. 

```
$ mkdir ~/hadoop-conf-dir/
$ cat <<EOF >>~/hadoop-conf-dir/core-site.xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
    <property>
        <name>fs.s3a.endpoint</name>
        <value>{{ endpoint }}</value>
    </property>

    <property>
        <name>fs.s3a.bucket.{{ bucket }}.access.key</name>
        <value>{{ access }}</value>
    </property>

    <property>
        <name>fs.s3a.bucket.{{ bucket }}.secret.key</name>
        <value>{{ secret }}</value>
    </property>

    <property>
        <name>fs.s3a.impl</name>
        <value>org.apache.hadoop.fs.s3a.S3AFileSystem</value>
    </property>
</configuration>
EOF
 
$ HADOOP_CONF_DIR=~/hadoop-conf-dir sparkctl create spark-pi.yaml
```

The above will create `spark-pi` application, and mount to each driver and executor config map 
with contents of local `HADOOP_CONF_DIR`, and in order to intercept `spark-submit` webhooks are used.

##### Local Dependencies example

Dependencies can be stage building a custom Docker file e.g. [Examples Dockerfile](Dockerfile),
or via staging dependencies in high-availability storage as S3 or GCS. 

- [Dockerfile example ](Dockerfile)
- [Stage to s3 manualy](examples/basics/spark-pi-deps-s3.yaml)
- [Stage to s3 using sparkctl](examples/basics/spark-pi-deps.yaml)
- [Stage to http (via s3) using sparkctl](examples/basics/spark-pi-deps-public.yaml)

##### Python examples

- [Spark PyFiles](examples/basics/spark-pyfiles.yaml)
- [Spark Python Zip Dependencies](examples/applications/py-wordcount.yaml)

##### Application examples

- [Data generation for TPCDS](examples/applications/tpcds-datagen.yaml)
- [TPCDS SQL Benchmark](examples/applications/tpcds.yaml)
- [EOS Public Events Select](examples/applications/public-eos-events-select.yaml)
- [EOS Authentication Events Select (requires webhooks enabled)](examples/applications/secure-eos-events-select.yaml)
- [Data Reduction EOS (requires webhooks enabled)](examples/applications/data-reduction-eos.yaml)

### Building examples docker image

Being in root folder of this repository, run:

```
./build-docker.sh
docker push [registry]:[tag]
```

### Building examples jars

Being in root folder of this repository, run:

```
sbt package
```

Find your jar in 

```
<path-to-examples/spark-service-examples/target/scala-X.Y/spark-service-examples_-X.Y-Z.jar
```
