@echo off
java -Djava.awt.headless=false -Xmx1024m -XX:MaxPermSize=256m -jar toro-installer-1.0.0-SNAPSHOT.jar %1
