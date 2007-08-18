#!/bin/sh

java -Djava.awt.headless=true -Xmx1024m -XX:MaxPermSize=256m -jar /Volumes/Deuce/Users/deuce/.m2/repository/net/unicon/toro/toro-installer/1.0.0-SNAPSHOT/toro-installer-1.0.0-SNAPSHOT.jar $*
