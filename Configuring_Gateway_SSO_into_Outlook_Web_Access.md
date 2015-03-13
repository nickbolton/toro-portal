# Overview #

Outlook Web Access (OWA) is a component of Microsoft Exchange that enables users to access their e-mail, calendar, contacts and other Exchange/Outlook personal information through a web browser.

The Gateway SSO Portlet is a powerful, flexible part of the Toro open source technology that allows portal administrators to accomplish single sign-on for embedded or externally linked access to applications like OWA from within the Portal.

# Configuration #

This section describes the steps required to configure a new Toro Gateway SSO Portlet for OWA.

## Obligatory Backup Warning ##

Warning: You should make a backup of all files you edit before editing them.  Your portal environment should have a rigorous and thorough backup regimen and you should be in a position to restore your entire environment from backup if needed.  You should have a development or test instance of your portal in which you verify new configuration and other changes before applying these in production.

## Placeholder tokens used in these instructions ##

Your local name for the portlet web application containing the Toro Gateway SSO Portlet (here placeheld as "{TORO\_WEBAPP}") will vary.  For instance, Academus instances used the name "AcademusApps".  New deployments using the Toro Installer will by default name this web application "toro-gateway-portlet".  In any case this token in these instructions should be replaced with the name of the web application containing the Toro portlets.

Your local path to the Tomcat Java servlet container application (here placeheld as "{TOMCAT}") will vary.  In Academus deployments, this path was frequently somewhere like .../unicon/academus/portal-tomcata/ .

## Creating a configuration file for the new portlet ##

The following steps will configure OWA to open in an iframe (inline frame) with a height of 600 pixels. The desirable height for your environment may vary.


**Create a file** at {TOMCAT}/{TORO\_WEBAPP}/WEB-INF/classes/config/ named something like "gateway-OutlookWebAccessSso.xml" .  Whatever you name this configuration file, make note of that name as you'll need to refer to it later in these instructions ("the name of the configuration file for the Outlook Web Access instance of the Gateway SSO Portlet").

Seed this file with the following configuration:

```
<gateway peephole="gateway_main">
  <title>Outlook Web Access</title>
  <sso-entry handle="outlookwebaccess" class="noImage">
    <label>Outlook Web Access</label>
    <description>Sign in to Outlook Web Access</description>
    <target handle="log">
      <url>https://deploy1.unicon.net/exchweb/bin/auth/owaauth.dll</url>
      <method>POST</method>
      <parameter name="username">
        <value>uniconsystest\{user.login.id}</value>
      </parameter>
      <parameter name="password">
        <value>{password}</value>
      </parameter>
      <parameter name="destination">
        <value>https://deploy1.unicon.net/exchange/<value>
      </parameter>
      <parameter name="flags">
        <value>0</value>
      </parameter>
    </target>
    <sequence type="login">
      <target handle="log"/>
    </sequence>
    <sequence type="relog">
      <target handle="log"/>
    </sequence>
    <window type="iframe">
      <title>Outlook Web Access</title>
      <name>Outlook Web Access</name>
      <style>width: 100%; height: 600px; border: 0px;</style>
    </window>
  </sso-entry>
  <jndi-ref>java:comp/env/jdbc/PortalDb</jndi-ref>
</gateway>
```

Note: the hostname of the value of the '

&lt;url&gt;

' element and the value of the 'parameter' element named "destination" must be set to the values for your OWA server.  This will be specific to your environment.  The prefix in the value of the 'parameter' tag named "username" should be replaced with the name of the Windows Active Directory Domain the Exchange server is using.

## Adding the new portlet to the application's web.xml file ##

**Place a new servlet entry and servlet mapping** in the web.xml file for the portlet applications.  This file will be located somewhere like this:

```
{TOMCAT}/webapps/{TORO_WEBAPP}/WEB-INF/web.xml
```

The new '

&lt;servlet&gt;

' element will need to look like this:

```
<servlet>
  <servlet-name>OutlookWebAccessPortlet</servlet-name>
  <display-name>Outlook Web Access Wrapper</display-name>
  <description>Servlet layer binding enabling Pluto to operate the corresponding Portlet instance</description>
  <servlet-class>org.apache.pluto.code.PortletServlet</servlet-class>
  <init-param>
    <param-name>portlet-class</param-name>
    <param-value>net.unicon.academus.apps.gateway.GatewayPortlet</param-value>
  </init-param>
  <init-param>
    <param-name>portlet-guid</portlet-name>
    <param-value>toro-gateway-portlet.OutlookWebAccessPortlet</param-value>
  </init-param>
</servlet>
```

The new '<servlet-mapping' element will need to look like this:

```
<servlet-mapping>
  <servlet-name>OutlookWebAccessPortlet</servlet-name>
  <url-pattern>/OutlookWebAccessPortlet</url-pattern>
</servlet-mapping>
```

## Adding the new portlet to portlet.xml ##

**Add a new '**

&lt;portlet&gt;

' element to the portlet.xml file**.**

This file will be located in the same directory as web.xml, at a path something like this:

```
{TOMCAT}/webapps/{TORO_WEBAPP}/WEB-INF/portlet.xml
```

The new '<portlet' element needs to look like this:

```
<portlet>
  <portlet-name>OutlookWebAccessPortlet</portlet-name>
  <portlet-class>net.unicon.academus.apps.gateway.GatewayPortlet</portlet-class>
  <init-param>
    <name>id</name>
    <value>outlookwebaccess</value>
  </init-param>
  <init-param>
    <name>configPath</name>
    <value>/WEB-INF/classes/config/gateway-OutlookWebAccessSoo.xml</value>
  </init-param>
  <expiration-cache>0</expiration-cache>
  <supported-locale>en-US</supported-locale>
  <portlet-info>
    <title>Outlook Web Access</title>
    <short-title>Outlook Web Access</short-title>
    <keywords>Outlook,Webmail,email</keywords>
  </portlet-info>
</portlet>
```

## Restart the portal ##

A restart of the portal will be required to effect these changes.

## Publishing the portlet ##

Publish the portlet adapter channel via the uPortal Channel Manager to present the deployed Gateway SSO Portlet instance in the portal.

| **Channel publication parameter** | **Value** | **Notes** |
|:----------------------------------|:----------|:----------|
| Channel Type | Portlet |  |
| Channel Title | Outlook Web Access | _(any other channel title you prefer will work)_ |
| Channel Name | Outlook Web Access | _(any other channel title you prefer will work)_ |
| Channel Functional Name | OutlookWebAccess | _(any valid functional name you prefer will work)_ |
| Channel Description | This is a portlet that will broker authentication to Outlook Web Access. | _(Any channel description will work.)_ |
| Channel Timeout | 5000 milliseconds | _(any reasonable timeout will work.  5 seconds (as configured here) is suggested)_ |
| Channel Secure |  | _(check or uncheck the box depending on your preference.  Unicon highly recommends that uPortal instances making use of the Gateway SSO Portlet use SSL for all situations involving this portlet)_ |
| Portlet definition ID | {TORO\_WEBAPP}.OutlookWebAccessPortlet |  |
| Channel controls |  | _None of Editable, Has Help, and Has About should be checked._ |
| Selected Categories |  | _A suitable category should be selected._ |
| Selected Groups and/or People |  | _The group of people desired to be able to accomplish single sign-on should be selected._ |