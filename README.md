# Spark on Kubernetes examples

Collection of stable application's examples for spark on kubernetes service 
(including managing the cluster, fetching local configuration and submitting Spark jobs) 

### Table of Contents

- Create and manage Spark on Kubernetes cluster
    - TODO
- Submitting Spark applications
    - TODO
- Legacy:
    - [Spark cluster management and submitting applications using legacy cern-spark-service](docs/spark-k8s-2.2.0-fork.md)

### Building jars

Being in root folder of this repository, run:

```
sbt package
```

Find your jar in 

```
<path-to-examples/spark-service-examples/target/scala-X.Y/spark-service-examples_-X.Y-Z.jar
```
