##
# BUILDING STEPS - examples-builder
##
FROM gitlab-registry.cern.ch/linuxsupport/cc7-base:latest as examples-builder

RUN mkdir -p /tmp

ENV TPCDS_KIT_VERSION "master"

RUN yum group install -y "Development Tools" && \
    git clone https://github.com/databricks/tpcds-kit.git -b ${TPCDS_KIT_VERSION} /tmp/tpcds-kit && \
    cd /tmp/tpcds-kit/tools && \
    make OS=LINUX

##
# OUTPUT DOCKER IMAGE
##
FROM gitlab-registry.cern.ch/db/spark-service/docker-registry/spark:v2.4.1-hadoop2-0.8
MAINTAINER Piotr Mrowczynski <piotr.mrowczynski@cern.ch>

RUN yum install -y \
     # Install configs to be able to connect to cern hadoop clusters
    "cern-hadoop-config" \
     # Required for cvmfs
    HEP_OSlibs \
     # Install xrootd-client
    xrootd-client \
    xrootd-client-libs

COPY --from=examples-builder /tmp/tpcds-kit/tools /opt/tpcds-kit/tools
COPY ./libs/*jar ${SPARK_HOME}/examples/jars/
COPY ./target/scala-2.11/*jar ${SPARK_HOME}/examples/jars/

LABEL \
  org.label-schema.version="0.1" \
  org.label-schema.vcs-url="https://gitlab.cern.ch/db/spark-service/docker-registry.git" \
  org.label-schema.name="Spark Examples Docker - CERN Spark on Kubernetes" \
  org.label-schema.vendor="CERN" \
  org.label-schema.schema-version="1.0"