#!/bin/sh

java -Djava.awt.headless=true -Xmx1024m -XX:MaxPermSize=256m -jar toro-installer-1.0.0-SNAPSHOT.jar $*
