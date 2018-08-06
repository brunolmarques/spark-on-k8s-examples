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
FROM gitlab-registry.cern.ch/db/spark-service/docker-registry/spark:v2.4.0-hadoop3.1
MAINTAINER Piotr Mrowczynski <piotr.mrowczynski@cern.ch>

ARG BUILD_DATE
ARG VCS_REF

ENV SPARK_EXAMPLES_VERSION "2.11-0.3.0"
ENV SCALA_LOGGING_VERSION "2.11-3.9.0"
ENV SPARK_MEASURE_VERSION "2.11-0.11"
ENV SPARK_SQL_PERF_VERSION "2.11-0.5.0-SNAPSHOT"

COPY --from=examples-builder /tmp/tpcds-kit/tools /opt/tpcds-kit/tools

COPY ./libs/spark-service-examples_${SPARK_EXAMPLES_VERSION}.jar ${SPARK_HOME}/examples/jars/
COPY ./libs//scala-logging_${SCALA_LOGGING_VERSION}.jar ${SPARK_HOME}/examples/jars/
COPY ./libs/spark-measure_${SPARK_MEASURE_VERSION}.jar ${SPARK_HOME}/examples/jars/
COPY ./libs/spark-sql-perf_${SPARK_SQL_PERF_VERSION}.jar ${SPARK_HOME}/examples/jars/

COPY ./libs/scalability-test-eos-datasets.csv ${SPARK_HOME}/examples/

LABEL \
  org.label-schema.version="0.1" \
  org.label-schema.build-date=$BUILD_DATE \
  org.label-schema.vcs-url="https://gitlab.cern.ch/db/spark-service/docker-registry.git" \
  org.label-schema.name="Spark Examples Docker - CERN Spark on Kubernetes" \
  org.label-schema.vendor="CERN" \
  org.label-schema.schema-version="1.0"