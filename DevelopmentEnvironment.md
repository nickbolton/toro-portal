This page describes how to build and deploy the components in Toro Portal.

  1. Check out the desired version. For the trunk, svn checkout https://toro-portal.googlecode.com/svn/trunk/ toro-portal --username username
  1. Perform an initial maven install from the root directory. (mvn install)
  1. Use the installer to initially deploy all the components and initialize the database. (See http://www.unicon.net/node/821)
  1. Copy the ant.installer.properties file to the root directory as build.properties.
  1. Modify source code or resources, then deploy that component. The ant script at the root directory handles deploying all the subcomponents. Type 'ant' at the root directory to get a list of all the available targets.
  1. Restart tomcat

```
e.g.

toro-portal> vi channels/survey/src/main/java/net/unicon/portal/channels/survey/survey/CSurvey.java

toro-portal> ant survey
Buildfile: build.xml
Warning: Reference toro.repository has not been set at runtime, but was found during
build file parsing, attempting to resolve. Future versions of Ant may support
 referencing ids defined in non-executed targets.

set-resource-tokens:
     [echo] building build.properties.tokens from ./build.properties
     [echo] building empty ant.install.properties.tokens

set-db-properties:
     [echo] Setting postgres specific properties...

survey:
Warning: Reference toro.repository has not been set at runtime, but was found during
build file parsing, attempting to resolve. Future versions of Ant may support
 referencing ids defined in non-executed targets.
Trying to override old definition of task copyWithAllPropertiesFilter
Trying to override old definition of task moveWithAllPropertiesFilter

mvn:
     [exec] [INFO] Scanning for projects...
     [exec] [INFO] ----------------------------------------------------------------------------
     [exec] [INFO] Building Survey Channel
     [exec] [INFO]    task-segment: [install]
     [exec] [INFO] ----------------------------------------------------------------------------
     [exec] [INFO] [resources:resources]
     [exec] [INFO] Using default encoding to copy filtered resources.
     [exec] [INFO] [compiler:compile]
     [exec] [INFO] Compiling 1 source file to /common/dev/src/unicon/toro/src/channels/survey/target/classes
     [exec] [INFO] [resources:testResources]
     [exec] [INFO] Using default encoding to copy filtered resources.
     [exec] [INFO] [compiler:testCompile]
     [exec] [INFO] No sources to compile
     [exec] [INFO] [surefire:test]
     [exec] [INFO] No tests to run.
     [exec] [INFO] [jar:jar]
     [exec] [INFO] Building jar: /common/dev/src/unicon/toro/src/channels/survey/target/toro-survey-channel-1.0.0-rc-6-SNAPSHOT.jar
     [exec] [INFO] [install:install]
     [exec] [INFO] Installing /common/dev/src/unicon/toro/src/channels/survey/target/toro-survey-channel-1.0.0-rc-6-SNAPSHOT.jar to /Users/deuce/.m2/repository/net/unicon/toro/toro-survey-channel/1.0.0-rc-6-SNAPSHOT/toro-survey-channel-1.0.0-rc-6-SNAPSHOT.jar
     [exec] [INFO] ------------------------------------------------------------------------
     [exec] [INFO] BUILD SUCCESSFUL
     [exec] [INFO] ------------------------------------------------------------------------
     [exec] [INFO] Total time: 4 seconds
     [exec] [INFO] Finished at: Tue Jan 08 16:07:06 MST 2008
     [exec] [INFO] Final Memory: 8M/14M
     [exec] [INFO] ------------------------------------------------------------------------
     [copy] Copying 1 file to /common/dev/src/unicon/toro/tomcat/webapps/portal/WEB-INF/lib
Warning: Reference toro.repository has not been set at runtime, but was found during
build file parsing, attempting to resolve. Future versions of Ant may support
 referencing ids defined in non-executed targets.
Trying to override old definition of task copyWithAllPropertiesFilter
Trying to override old definition of task moveWithAllPropertiesFilter

copy-local-resources-filtered:
     [copy] Copying 18 files to /common/dev/src/unicon/toro/tomcat/webapps/portal
[copyWithAllPropertiesFilter] Copying 29 files to /common/dev/src/unicon/toro/tomcat/webapps/portal
Warning: Reference toro.repository has not been set at runtime, but was found during
build file parsing, attempting to resolve. Future versions of Ant may support
 referencing ids defined in non-executed targets.
Trying to override old definition of task copyWithAllPropertiesFilter
Trying to override old definition of task moveWithAllPropertiesFilter

merge-configuration:
     [echo] MergeConfiguration configfile=channels/survey/target/classes/uportal-configuration-merges/uportal-configuration-merges.xml targetdir=/common/dev/src/unicon/toro/tomcat/webapps/portal
     [java] Merging properties changes to /common/dev/src/unicon/toro/tomcat/webapps/portal/WEB-INF/classes/properties/worker.properties

BUILD SUCCESSFUL
Total time: 11 seconds
```