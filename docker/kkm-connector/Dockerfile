#
# Copyright © 2018 HAVELSAN Inc.
#

FROM openjdk:8-jre


RUN apt-get update -y
RUN apt-get install -y nmap
ADD run-application.sh /run-application.sh
ADD kkm-connector.jar /kkm-connector.jar
RUN chmod +x /run-application.sh

