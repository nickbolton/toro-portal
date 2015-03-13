# Overview #

WebAdvisor is a web application offered by Datatel presenting certain student information system information and services through a web browser.

The Gateway SSO Portlet is a powerful, flexible part of the Toro open source technology that allows portal administrators to accomplish single sign-on for embedded or externally linked access to applications like WebAdvisor from within the portal.

# Configuration #

This section describes the steps required to configure a new Toro Gateway SSO Portlet for WebAdvisor.

## Obligatory Backup Warning ##

Warning: You should make a backup of all files you edit before editing them.  Your portal environment should have a rigorous and thorough backup regimen and you should be in a position to restore your entire environment from backup if needed.  You should have a development or test instance of your portal in which you verify new configuration and other changes before applying these in production.

All administrative and systems activities carry some level of risk.

## Reference to KBA providing instructions ##

This KBA provides instructions.  The purpose of this page is to reference those instructions and provide an example of the resulting configuration.

## Example configuration ##

Here's a resulting example configuration, generously [shared](http://www.nabble.com/RE%3A-WebAdvisor-SSO-Question-p17151163.html) on [uportal-user@](http://www.ja-sig.org/wiki/display/JSG/uportal-user) by Tim Rudolph of the [Art Center College of Design](http://artcenter.edu/).

```
<!--
    gateway-recstudent.xml

    This file is an implementation of the Gateway Portlet for SSO into Academus
    WCM.  Refer to gateway-portlet.xml for configuration documentation.
-->
<gateway peephole="gateway_main">

    <!--
        The title to display when showing a list of available SSO entries. If
        only a single iframe entry is specified, the sso-entry's label will be
        displayed in its place.
    -->

    <title></title>

    <ajax-callback-url>/portal/ssoCallback</ajax-callback-url>

    <sso-entry handle="student" class="largeRecords">
        <label>Student</label>
        <description>Webadvisor</description>  
                
        <target handle="log">
            <url>https://inside.artcenter.edu/AcademusApps/rendering/html/sso/bridgeA.html?SSODynamicFormId=WEBADVISOR&amp;url=https%3A//secure.artcenter.edu/recstudent/bridgeB.html</url>
            <method>POST</method>
            <parameter name="SSODynamicFormUrl"><value>https://secure.artcenter.edu/recstudent/WebAdvisor?&amp;SS=LGRQ</value></parameter>
            <parameter name="SSODynamicFormButtonName"><value>SUBMIT</value></parameter>
            <parameter name="SSODynamicFormFollowLinkText"><value>Log In</value></parameter>
            <parameter name="USER.NAME"><value>{user.login.id}</value></parameter>
            <parameter name="CURR.PWD"><value>{password}</value></parameter>
        </target>

        <sequence type="login">
            <target handle="log" />
        </sequence>

        <window type="iframe">
            <name>WebAdvisor</name>
            <style>width: 740px; height: 400px; border: 0px;</style>
        </window>

    </sso-entry>

    <!-- Nothing below this line should require modification in normal usage. -->

</gateway>
```