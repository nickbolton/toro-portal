@echo off
java -Djava.awt.headless=true -Xmx1024m -XX:MaxPermSize=384m -jar toro-installer-1.0.0-SNAPSHOT.jar %1
