# Start with OL runtime.
FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi AS builder

ARG VERSION=1.0
ARG REVISION=SNAPSHOT
ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

LABEL \
  org.opencontainers.image.authors="Your Name" \
  org.opencontainers.image.vendor="IBM" \
  org.opencontainers.image.url="local" \
  org.opencontainers.image.source="https://github.com/OpenLiberty/guide-docker" \
  org.opencontainers.image.version="$VERSION" \
  org.opencontainers.image.revision="$REVISION" \
  vendor="Open Liberty" \
  name="system" \
  version="$VERSION-$REVISION" \
  summary="The system microservice from the Docker Guide" \
  description="This image contains the system microservice running with the Open Liberty runtime."

USER root

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
 && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
 && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
 && rm -f /tmp/apache-maven.tar.gz \
 && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

COPY src ./src
COPY pom.xml ./
RUN mvn package

FROM icr.io/appcafe/open-liberty:kernel-slim-java11-openj9-ubi

COPY --from=builder --chown=1001:0 ./src/resources/artists.json /config/

COPY --from=builder --chown=1001:0 ./src/main/liberty/config /config/
RUN features.sh

COPY --from=builder --chown=1001:0 ./target/*.war /config/apps/
RUN configure.sh

USER 1001
