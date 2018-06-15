# Manual creation of spark on kubernetes

### Prerequisites

- kubectl > v1.10.1
- kubernetes > v1.10 (with Initializers enabled) 

For more details look [here](https://github.com/cerndb/spark-on-k8s-operator/blob/master/opsparkctl/openstack_client.py)

### Manual installation

Get spark-on-k8s-operator

```bash
git clone https://github.com/cerndb/spark-on-k8s-operator
```

Install **spark operator prerequisites** on the cluster

```bash
kubectl apply -f spark-on-k8s-operator/manifest/spark-operator-base
```

Edit and install **spark operator config** on the cluster

```bash
vi spark-on-k8s-operator/manifest/spark-operator/spark-config.yaml
kubectl apply -f spark-on-k8s-operator/manifest/spark-operator/spark-config.yaml
```

Install **spark operator** on the cluster

```bash
kubectl apply -f spark-on-k8s-operator/manifest/spark-operator/spark-operator.yaml
```