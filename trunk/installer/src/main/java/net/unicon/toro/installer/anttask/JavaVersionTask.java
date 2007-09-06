package net.unicon.toro.installer.anttask;

import org.apache.tools.ant.*;

/**
 JavaVersionTask is an Ant task for testing if
 the installed Java version is greater than a 
 minimum required version.
 **/
public class JavaVersionTask extends Task {

  // Minimum required Java version.
  private String minVersion;
  // Installed Java version.
  private String installedVersion;
  // The name of the property that gets set when
  // the installed Java version is ok.
  private String propertyName;

  /**
   * Constructor of the JavaVersionTask class.
   **/
  public JavaVersionTask() {
    super();
    installedVersion = System.getProperty
                       ("java.version");
    /*
    String[] split = installedVersion.split("\\.");
    System.out.println("ZZZ split.length: " + split.length);
    if (split.length >= 3) {
        installedVersion = split[0] + '.' + split[1];
    }
    */
  }

  /**
   * Set the attribute minVersion.
   **/
  public void setMinVersion(String version) {
    minVersion = version;
  }

  /**
   Set the property name that the task sets when
   the installed Java version is ok.
   **/
  public void setProperty(String propName) {
    propertyName = propName;
  }

  /**
   * Execute the task.
   **/
  public void execute() throws BuildException {
    if (propertyName==null) {
      throw new BuildException("No property name set.");
    } else if (minVersion==null) {
      throw new BuildException("No minimum version set.");
    }

    if(installedVersion.compareTo(minVersion) >= 0) {
      getProject().setProperty(propertyName, "true");
    }
  }
}
