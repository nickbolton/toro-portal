# You will need to manually install these dependencies into your local repository. Just execute the following maven commands.

mvn install:install-file -DgroupId=uportal -DartifactId=uportal \
    -Dversion=2.6.0-GA -Dpackaging=jar -Dfile=uPortal-2.6.0-GA.jar
mvn install:install-file -DgroupId=uportal -DartifactId=uportal \
    -Dversion=2.6.0-GA -Dpackaging=war -Dfile=uPortal-2.6.0-GA.war
mvn install:install-file -DgroupId=xsltc -DartifactId=xsltc \
    -Dversion=2.7.0 -Dpackaging=jar -Dfile=xsltc-2.7.0.jar
mvn install:install-file -DgroupId=serializer -DartifactId=serializer \
    -Dversion=2.7.0 -Dpackaging=jar -Dfile=serializer-2.7.0.jar
mvn install:install-file -DgroupId=javax.transaction -DartifactId=jta \
          -Dversion=1.0.1B -Dpackaging=jar -Dfile=jta-1.0.1B.jar
mvn install:install-file -DgroupId=jetspeed -DartifactId=jetspeed \
          -Dversion=1.5 -Dpackaging=jar -Dfile=jetspeed-1.5.jar
mvn install:install-file -DgroupId=jargs -DartifactId=jargs \
          -Dversion=1.0 -Dpackaging=jar -Dfile=jargs-1.0.jar
mvn install:install-file -DgroupId=cryptowallet-mod -DartifactId=cryptowallet-mod \
          -Dversion=1.0 -Dpackaging=jar -Dfile=cryptowallet-mod-1.0.jar
mvn install:install-file -DgroupId=cos -DartifactId=cos \
          -Dversion=05Nov2002 -Dpackaging=jar -Dfile=cos-05Nov2002.jar
mvn install:install-file -DgroupId=jtds -DartifactId=jtds \
          -Dversion=1.2 -Dpackaging=jar -Dfile=jtds-1.2.jar
