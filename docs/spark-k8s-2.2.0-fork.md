# Spark Applications Examples for EOS ROOT (legacy cern-spark-service)

Collection of stable application's examples for spark on kubernetes service 

### Prerequisites

Running spark-submit over spark-on-kubernetes
- pip > 8.1
- python > 2.7.5

### Install cern-spark-service package (requirement for spark-submit)

On MacOS and Linux, tools can be installed via [Python PIP](https://pip.pypa.io/en/stable/installing)

* In case you don't have sudo access (lxplus7, lxplus-cloud)
```
$ pip install --user --upgrade cern-spark-service
```

* In case you have sudo access
```
$ pip install --upgrade cern-spark-service
```

* For more info, please refer to [installation instructions of cern-spark-service](https://pypi.org/project/cern-spark-service)

### Run spark-submit job over Spark on Kubernetes

Before running spark-submit, make sure you have your configuration of Spark on Kubernetes cluster
locally e.g. using

```
cern-spark-service fetch --openstack-config
```

The above will ask you to select your Openstack project, then Kubernetes cluster in the project, 
and fetch the configuration for you. Check everything with

```
cern-spark-service check --cluster <your-cluster-name>
```

* For more info, please refer to [cern-spark-service documentation](https://pypi.org/project/cern-spark-service)

- **SparkPi**

```
cern-spark-service spark-submit \
--cluster <your-cluster-name> \
--class org.sparkservice.sparkrootapplications.examples.SparkPi \
<path-to-examples>/spark-service-examples/spark-service-examples_2.11-0.0.1.jar
```

- **EventsSelect**

```
cern-spark-service spark-submit \
--cluster <your-cluster-name> \
--conf spark.executor.instances=20 \
--class org.sparkservice.sparkrootapplications.examples.EventsSelect \
--jars \
http://central.maven.org/maven2/org/diana-hep/root4j/0.1.6/root4j-0.1.6.jar,\
http://central.maven.org/maven2/org/apache/bcel/bcel/5.2/bcel-5.2.jar,\
http://central.maven.org/maven2/org/diana-hep/spark-root_2.11/0.1.16/spark-root_2.11-0.1.16.jar \
<path-to-examples>/spark-service-examples/spark-service-examples_2.11-0.0.1.jar \
root://eospublic.cern.ch://eos/opendata/cms/Run2010B/MuOnia/AOD/Apr21ReReco-v1/0000/FEF1B99B-BF77-E011-B0A0-E41F1318174C.root
```

- **EventsSelect with Kerberos ticket for EOS**

```
$ export USER=<your-user>
$ kinit -c /tmp/krb5cc_$USER $USER
$ cern-spark-service spark-submit \
--cluster <your-cluster-name> \
--conf spark.executor.instances=20 \
--class org.sparkservice.sparkrootapplications.examples.EventsSelect \
--jars \
http://central.maven.org/maven2/org/diana-hep/root4j/0.1.6/root4j-0.1.6.jar,\
http://central.maven.org/maven2/org/apache/bcel/bcel/5.2/bcel-5.2.jar,\
http://central.maven.org/maven2/org/diana-hep/spark-root_2.11/0.1.16/spark-root_2.11-0.1.16.jar \
--files /tmp/krb5cc_$USER \
--conf spark.kubernetes.driverEnv.KRB5CCNAME=./krb5cc_$USER \
--conf spark.executorEnv.KRB5CCNAME=./krb5cc_$USER \
<path-to-examples>/spark-service/spark-service-examples/target/scala-2.11/spark-service-examples_2.11-0.0.1.jar \
root://eosuser.cern.ch/eos/user/<first-letter-your-user>/<your-user>/<some-rootfile-name>
```

- **DimuonReductionAOD**

```
cern-spark-service spark-submit \
--cluster <your-cluster-name> \
--conf spark.executor.instances=20 \
--class org.sparkservice.sparkrootapplications.examples.DimuonReductionAOD \
--jars \
http://central.maven.org/maven2/org/diana-hep/spark-root_2.11/0.1.16/spark-root_2.11-0.1.16.jar,\
http://central.maven.org/maven2/org/diana-hep/histogrammar-sparksql_2.11/1.0.3/histogrammar-sparksql_2.11-1.0.3.jar,\
http://central.maven.org/maven2/org/diana-hep/root4j/0.1.6/root4j-0.1.6.jar,\
http://central.maven.org/maven2/org/diana-hep/histogrammar_2.11/1.0.3/histogrammar_2.11-1.0.3.jar,\
http://central.maven.org/maven2/org/apache/bcel/bcel/5.2/bcel-5.2.jar,\
http://central.maven.org/maven2/org/tukaani/xz/1.2/xz-1.2.jar,\
http://central.maven.org/maven2/jakarta-regexp/jakarta-regexp/1.4/jakarta-regexp-1.4.jar \
<path-to-examples>/spark-service-examples/spark-service-examples_2.11-0.0.1.jar \
root://eospublic.cern.ch//eos/opendata/cms/MonteCarlo2012/Summer12_DR53X/DYJetsToLL_M-50_TuneZ2Star_8TeV-madgraph-tarball/AODSIM/PU_RD1_START53_V7N-v1/20000/DCF94DC3-42CE-E211-867A-001E67398011.root
```

- **For more refer here** https://github.com/olivito/spark-root-applications
