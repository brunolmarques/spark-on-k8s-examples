# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service 

### Table of Contents

- [Prerequisites](#prerequisites)
- [Create and manage Spark on Kubernetes cluster](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl)
- [Submitting Spark applications](#submitting-spark-applications)

### Prerequisites

1. **Install Kubernetes cluster with MutatingAdmissionWebhook enabled**

    NOTE: if you already have cluster created, skip
    
2. **Fetch cluster configuration to be able to use helm/kubectl/sparkctl**

    After successfull configuration, such a files should be found in `~/.kube` folder locally:
    ```
    config    ca.pem    cert.pem    key.pem
    ```

3. **Create Spark on K8S Operator in the cluster**
    

### Submitting Spark applications

Install `sparkctl` tool to interact with your kubernetes cluster. 

```
MAC:
$ wget https://cs3.cern.ch/binaries/sparkctl/mac/latest/sparkctl
LINUX:
$ wget https://cs3.cern.ch/binaries/sparkctl/linux/latest/sparkctl
```
```
$ chmod +x sparkctl
$ ./sparkctl --help
```

**Managing simple application**

Edit yaml file with SparkApplication. 

```
$ cp ./examples/spark-pi.yaml ./jobs/spark-pi.yaml
$ vi ./jobs/spark-pi.yaml
```

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
$ ./sparkctl create ./jobs/spark-pi.yaml
```

To delete application

```
$ ./sparkctl delete spark-pi
```


Check if your driver/executors are correctly created

```
$ ./sparkctl event spark-pi
```

To get application logs

```
$ ./sparkctl log spark-pi
```

To check application status

```
$ ./sparkctl status spark-pi
```

To access driver UI (forwarded to localhost:4040 from where sparctl is executed)

```
$ ./sparkctl forward spark-pi
```

Alternatively, to check application status (or check created pods and their status)

```
$ kubectl get pods -n default
or
$ kubectl describe pod spark-pi-1528991055721-driver
or
$ kubectl logs spark-pi-1528991055721-driver
or
$ kubectl describe sparkapplication spark-pi
```

For more details regarding `sparkctl`, and more detailed user guide, 
please visit [sparkctl user-guide](https://github.com/cerndb/spark-on-k8s-operator/tree/master/sparkctl)

**Python example**

```
$ ./sparkctl create ./jobs/spark-pyfiles.yaml
```

**TPCDS example**

```
$ ./sparkctl create ./jobs/tpcds.yaml
```

**Local Dependencies example**

Dependencies can be stage building a custom Docker file e.g. [Examples Dockerfile](Dockerfile),
or via staging dependencies in high-availability storage as S3 or GCS. 

In order to submit application with local dependencies to S3, 
access key, secret and endpoint have to be specified (both during submission and in job specification):
```
$ export AWS_ACCESS_KEY_ID=<redacted>
$ export AWS_SECRET_ACCESS_KEY=<redacted>
$ ./sparkctl create ./jobs/spark-pi-deps.yaml \
--upload-to s3a://<bucket-name> \
--override \
--upload-to-endpoint "https://cs3.cern.ch"
```

In order to submit application with local dependencies to S3 so that they are downloaded using `http`, resources neet to be made public

```
$ export AWS_ACCESS_KEY_ID=<redacted>
$ export AWS_SECRET_ACCESS_KEY=<redacted>
$ ./sparkctl create ./jobs/spark-pi-deps-public.yaml \
--upload-to s3a://<bucket-name> \
--override \
--public \
--upload-to-endpoint "https://cs3.cern.ch"
```
**EOS Authentication example**

Please check [SparkApplication User Guide](https://github.com/cerndb/spark-on-k8s-operator/blob/master/docs/user-guide.md) for details
on how to create custom SparkApplication YAML files

Example of such comples application is Events Select over secure EOS:

```
Create hadoop config dir and put your kerberos cache there
$ mkdir ~/hadoop-conf-dir
$ kinit -c ~/hadoop-conf-dir/krb5cc_0 <your-user>
```
```
Submit your application with custom hadoop config directory to authenticate EOS
$ HADOOP_CONF_DIR=~/hadoop-conf-dir ./sparkctl create ./jobs/secure-eos-events-select.yaml
```

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
