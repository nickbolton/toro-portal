# Introduction #

[Replicon](http://www.replicon.com/) [WebTimesheet](http://www.replicon.com/timesheet.aspx) is a proprietary closed-source software application for time tracking.  This wiki page presents an example of configuring Gateway SSO to accomplish single sign-on into this application.

Gateway SSO is capable of presenting hyperlinks to accomplish single sign on into applications presented in new windows.  It is also capable of presenting applications within the portlet in inline frames.

# Opening Web Timesheet in a new window #

This configuration implements the approach of providing a logo'ed link to open the application, with the user conveniently authenticated to it,


## Screenshot ##

This is what the Gateway SSO Portlet can look like in presenting the opportunity to log into the timesheet application from the portal (here shown with an ugly custom icon -- your icon can be better!)

![http://toro-portal.googlecode.com/svn/wiki/images/replicon_web_timesheet_sso.png](http://toro-portal.googlecode.com/svn/wiki/images/replicon_web_timesheet_sso.png)

## Details ##

Gateway SSO configuration file for accomplishing this integration:

```
<gateway peephole="gateway_main">

  <!--
   The title to display when showing a list of available SSO entries.
   In this example, this is a list of just one SSO entry, but it could have
   additional entries, and then having a title for the list of multiple entries would
   be more interesting.
  -->
  <title>Timesheet</title>
    <!--
     Single Sign-On entry for an external system.
     The attribute 'handle' is required, and must be unique among other
     sso-entry elements.
    -->
    <sso-entry handle="Timesheet" class="largeTimesheet">
      <!-- Label and description to identify the external site. -->
      <label>Timesheet</label>
      <description>Launch My Timesheet</description>
      <target handle="submit">
        <url>https://webtimesheet.yourorganization.com/CGI/rt.exe</url>
        <method>POST</method>
        <parameter name="module"><value>login</value></parameter>
        <parameter name="package"><value></value></parameter>
        <parameter name="startpage"><value></value></parameter>
        <parameter name="startpageuserid"><value></value></parameter>
        <parameter name="startpageobjectid"><value></value></parameter>
        <parameter name="UserId">
          <value>{user.login.id}</value>
        </parameter>
        <parameter name="UserPass"><value>{password}</value></parameter>
      </target>
        <!--
         A sequence defines one or more targets that must be submitted in sequence.
         The only two expected sequence types are "login" and "refresh". The
         "login" sequence type must always exist; the "refresh" sequence may
         be specified if you wish for a difference sequence to be executed
         after the user's first login has occurred. All other sequences are
         available via buttons in the user interface.
        -->
      <sequence type="login">
        <target handle="submit" />
      </sequence>

        <!--
         Window definition. Type can be either 'popup' or 'iframe'.
         <title> and <name> can be declared for both.
         If using type 'iframe', the element <style> can be 
         defined to contain CSS information.

         If using type 'popup', the element <properties> can be 
         defined to specify window parameters.
        -->
      <window type="popup">
        <title>Timesheet</title>
        <name>Timesheet</name>
        <properties></properties>
      </window>
    </sso-entry>

    <!-- Nothing below this line should require modification in normal usage. -->
    <!-- JNDI Reference to the database. -->
    <jndi-ref>java:comp/env/jdbc/PortalDb</jndi-ref>
</gateway>

```

# Presenting Web Timesheet within the Gateway SSO Portlet in an inline frame #

Instead of the portlet presenting a labeled, potentially icon-adorned, hyperlink inviting the user to single sign-in to the Web Timesheet application and opening the application in a new window when the user clicks the link, Gateway SSO can accomplish the single sign-on immediately and present the signed-in user experience within an inline frame presented by the portlet.  This configuration implements that approach.

## Configuration ##

```
<gateway peephole="gateway_main">
    <!--
     This title is not displayed when the portlet is configured to present an inline
     frame, as it is here.
    -->
    <title>TimesheetIframe</title>

    <sso-entry handle="Timesheet" class="largeTimesheet">
        <!-- Label and description to identify the external site. -->
        <label>Timesheet</label>
        <description>Launch My Timesheet</description>
          <target handle="submit">
            <url>https://webtimesheet.yourorganization.com/CGI/rt.exe</url>
            <method>POST</method>
            <parameter name="module"><value>login</value></parameter>
            <parameter name="package"><value></value></parameter>
            <parameter name="startpage"><value></value></parameter>
            <parameter name="startpageuserid"><value></value></parameter>			
            <parameter name="startpageobjectid"><value></value></parameter>

            <parameter name="UserId">
              <value>{user.login.id}</value>
            </parameter>

            <parameter name="UserPass"><value>{password}</value></parameter>
	  </target>


        <!--
            A sequence defines one or more targets that must be submitted in sequence.
            The only two expected sequence types are "login" and "refresh". The
            "login" sequence type must always exist; the "refresh" sequence may
            be specified if you wish for a difference sequence to be executed
            after the user's first login has occurred. Other sequences if present 
            would be available via buttons in the user interface.
  Other sequences aren't
            present here, so they won't be buttons in the user interface.
        -->

        <sequence type="login">
          <target handle="submit" />
        </sequence>

        <!--
          Window definition. Type can be either 'popup' or 'iframe'.
          <title> and <name> can be declared for both.

          If using type 'iframe', the element <style> 
          can be defined to contain CSS information.

          If using type 'popup', the element <properties> 
          can be defined to specify window parameters.
        -->

        <window type="iframe">
            <title>Timesheet</title>
            <name>Timesheet</name>
            <properties></properties>
        </window>

    </sso-entry>


    <!-- Nothing below this line should require modification in normal usage. -->

    <!-- JNDI Reference to the database. -->
    <jndi-ref>java:comp/env/jdbc/PortalDb</jndi-ref>
</gateway>

```