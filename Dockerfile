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

RUN curl https://cafiles.cern.ch/cafiles/certificates/CERN%20Root%20Certification%20Authority%202.crt -o CERNRootCertificationAuthority2.crt && \
    curl https://cafiles.cern.ch/cafiles/certificates/CERN%20Grid%20Certification%20Authority.crt -o CERNGridCertificationAuthority.crt && \
    keytool -importcert -file CERNRootCertificationAuthority2.crt -keystore kafka.jks -alias "cernroot" -storepass password -noprompt && \
    keytool -importcert -file CERNGridCertificationAuthority.crt -keystore kafka.jks -alias "cerngrid" -storepass password -noprompt

RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.14.0/bin/linux/amd64/kubectl && \
    chmod +x ./kubectl && \
    mv ./kubectl /usr/local/bin/kubectl

COPY --from=examples-builder /tmp/tpcds-kit/tools /opt/tpcds-kit/tools
COPY ./data/* ./
COPY ./libs/* ./

COPY ./target/scala-2.11/*jar ${SPARK_HOME}/examples/jars/

LABEL \
  org.label-schema.version="0.1" \
  org.label-schema.vcs-url="https://gitlab.cern.ch/db/spark-service/docker-registry.git" \
  org.label-schema.name="Spark Examples Docker - CERN Spark on Kubernetes" \
  org.label-schema.vendor="CERN" \
  org.label-schema.schema-version="1.0"