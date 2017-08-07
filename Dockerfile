FROM oberthur/docker-generic-app:jre8_8.131.11_2


MAINTAINER Blazej Pazera <b.pazera@oberthur.com>
WORKDIR /opt/app

RUN apt-get update && apt-get install -y  nano  wget  curl && apt-get -y autoremove && apt-get clean && rm -rf /var/lib/apt/lists/* 
ADD ./target/scala-2.12/ /opt/app/     

ENTRYPOINT ["java", "-Dconfig.file=./conf/application.conf", "-jar", "pillar-assembly-4.1.0.jar"]
