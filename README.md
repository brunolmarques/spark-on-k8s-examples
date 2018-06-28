# Spark on Kubernetes documentation

Collection of stable application's examples for spark on kubernetes service 
(including managing the cluster, fetching local configuration and submitting Spark jobs) 

### Table of Contents

- [Prerequisites](#prerequisites)
- [Create and manage Spark on Kubernetes cluster](https://github.com/cerndb/spark-on-k8s-operator/tree/master/opsparkctl)
- [Submitting Spark applications]()
- [Building examples jars](#building-examples-jars)
- [Legacy: Spark cluster management and submitting applications using legacy cern-spark-service](docs/spark-k8s-2.2.0-fork.md)

### Prerequisites

1. **Install spark-on-k8s on Kubernetes cluster with Initializers enabled**

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

```bash
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

```bash
$ vi ./examples/spark-pi.yaml
```

The most important sections of your SparkApplication are:

- Application name
    ```bash
    metadata:
      name: spark-pi
    ```
- Application file
    ```bash
    spec:
      mainApplicationFile: "local:///opt/spark/examples/jars/spark-service-examples.jar"
    ```
- Application main class
    ```bash
    spec:
      mainClass: ch.cern.sparkrootapplications.examples.SparkPi
    ```

To submit application

```bash
$ ./sparkctl create ./examples/spark-pi.yaml
```

To check application status

```bash
$ ./sparkctl status spark-pi
```

Alternatively, to check application status (or check created pods and their status)

```
$ kubectl describe sparkapplication spark-pi
or
$ kubectl get pods --watch -n default
or
$ kubectl describe pod spark-pi-1528991055721-driver
or
$ kubectl logs spark-pi-1528991055721-driver
```

To get application logs

```bash
$ ./sparkctl log spark-pi
```

To delete application

```bash
$ ./sparkctl delete spark-pi
```

For more details regarding `sparkctl`, and more detailed user guide, 
please visit [sparkctl user-guide](https://github.com/cerndb/spark-on-k8s-operator/tree/master/sparkctl)

**Creating application with local dependencies**

In order to submit application with local dependencies, and have your spark-job fully-resilient to failures, 
they need to be staged at e.g. S3.

You would need to create an authentication file with cretentials on your filesystem:

```
$ vi ~/.aws/credentials

[default]
aws_access_key_id = <redacted>
aws_secret_access_key = <redacted>
``` 

After that, you will be able to submit applications with local dependencies, as shown in example `examples/spark-pi-deps.yaml` 

```bash
$ ./sparkctl create ./examples/spark-pi-deps.yaml \
--upload-to s3a://<your-cluster-name> \
--override \
--endpoint-url "https://cs3.cern.ch"
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
$ export HADOOP_CONF_DIR=~/hadoop-conf-dir
$ ./sparkctl create ./examples/secure-eos-events-select.yaml
```

**Scalability tests example**

```
Create hadoop config dir and put your kerberos cache there
$ kinit -c ~/hadoop-conf-dir/krb5cc_0 <your-user>
```
```
Edit your scalability-test dataset
- 2,5GB: 101,SingleMu_Run2012C,1,root://eospublic.cern.ch/eos/opendata/cms/Run2012C/SingleMu/AOD/22Jan2013-v1/30010
- 20TB: 101,SingleMu_Run2012C,1,root://eospublic.cern.ch/eos/opendata/cms/Run2012C/SingleMu/AOD/22Jan2013-v1/*0*/
$ vi ./examples/scalability-test-eos-datasets.csv
```
```
Submit your application with custom hadoop config directory to authenticate EOS
$ export HADOOP_CONF_DIR=~/hadoop-conf-dir
$ ./sparkctl create ./examples/scalability-test-eos.yaml --upload-to s3a://<your-cluster-name> --override --endpoint-url "https://cs3.cern.ch"
```

**TPCDS example**

```
Submit your TPCDS jobs (this will submit examples from target dir, and from libs folder
$ ./sparkctl create ./examples/tpcds.yaml --upload-to s3a://<your-cluster-name> --override --endpoint-url "https://cs3.cern.ch"
```

```
To perform complex analysis
$ spark-shell --jars /data/libs/scala-logging_2.11-3.9.0.jar,/data/libs/spark-sql-perf_2.11-0.5.0-SNAPSHOT.jar
$
$ sc.hadoopConfiguration.set("fs.s3a.access.key", "redacted")
$ sc.hadoopConfiguration.set("fs.s3a.secret.key", "redacted")
$ sc.hadoopConfiguration.set("fs.s3a.endpoint", "cs3.cern.ch")
$ val resultPath = "s3a://spark-on-k8s-cluster/TPCDS/tpcds_result/timestamp=1530032577241"
$ val specificResultTable = spark.read.json(resultPath)
$ specificResultTable.show()
$ ...more hints at https://github.com/databricks/spark-sql-perf/blob/master/src/main/notebooks/performance.dashboard.scala
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
