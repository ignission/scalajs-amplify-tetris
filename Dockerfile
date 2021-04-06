FROM adoptopenjdk/openjdk8:latest

RUN set -xe \
  && apt-get update \
  && apt-get install -y curl git openssh-client npm \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN set -xe \
  && echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
  && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add \
  && apt-get update \
  && apt-get install -y sbt \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN set -xe \
  && npm i -g yarn
