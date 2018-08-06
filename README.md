# Spark on Kubernetes documentation

Collection of stable application's examples for spark on kubernetes service 
(including managing the cluster, fetching local configuration and submitting Spark jobs) 

### Table of Contents

- [Prerequisites](#prerequisites)
- [Create and manage Spark on Kubernetes cluster](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl)
- [Submitting Spark applications](#submitting-spark-applications)
- [Building examples jars](#building-examples-jars)

### Prerequisites

1. **Install spark-on-k8s on Kubernetes cluster with MutatingAdmissionWebhook enabled**

    NOTE: if you already have cluster created, skip
    
    For Openstack you can use dedicated python tool [opsparkctl](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl).
    However, if you have on-premise kubernetes cluster, check [manual spark-on-k8s installation for on-premise](docs/spark-k8s-cluster.md)

2. **Fetch cluster configuration to be able to use sparkctl/kubectl**

    For Openstack, you can use [opsparkctl create local-kube-config](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl). 
    However, if you have on-premise kubernetes cluster, check [manual kubectl config](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters)

    After successfull configuration, such a files should be found in `~/.kube` folder locally:
    ```
    config    ca.pem    cert.pem    key.pem
    ```

3. **If you use Openstack, check your cluster status**
    
    You can check cluster status by [opsparkctl status](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl). 
    The following output should be displayed:
    
    ```
    [Kubernetes client configuration..]
    Kubernetes config used: /Users/pmrowczy/.kube/config
    Kubernetes cluster name: spark-cluster-test
    Kubernetes master: https://137.138.157.26:6443
    
    [Spark status..]
    SPARK HISTORY: http://137.138.157.31:30968
    ```

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
$ kubectl get pods --watch -n default
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
Submit your TPCDS jobs (this will submit examples from target dir, and from libs folder
$ ./sparkctl create ./jobs/tpcds.yaml
```

**Creating application with local dependencies**

Dependencies can be stage building a custom Docker file e.g. [Examples Dockerfile](Dockerfile),
or via staging dependencies in high-availability storage as S3 or GCS. 

In order to submit application with local dependencies to S3, 
access key, secret and endpoint have to be specified:
```
$ export AWS_ACCESS_KEY_ID=<redacted>
$ export AWS_SECRET_ACCESS_KEY=<redacted>
$ ./sparkctl create ./jobs/spark-pi-deps.yaml \
--upload-to s3a://<your-cluster-name> \
--override \
--upload-to-endpoint "https://cs3.cern.ch"
```

NOTE: Command `opsparkctl create spark` will automatically create S3 bucket for the cluster.
If you wish to use other staging S3 bucket than `s3a://<your-cluster-name>`, you need to use 
config as specified in `examples/spark-pi-custom-s3.yaml` 

**Creating complex spark applications**

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

### Building examples jars

Being in root folder of this repository, run:

```
sbt package
```

Find your jar in 

```
<path-to-examples/spark-service-examples/target/scala-X.Y/spark-service-examples_-X.Y-Z.jar
```
