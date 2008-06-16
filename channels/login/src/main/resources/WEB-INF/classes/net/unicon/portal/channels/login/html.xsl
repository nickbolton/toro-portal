<?xml version='1.0' encoding='utf-8' ?>
<!-- ========== LEGAL, NOTES, AND INSTRUCTIONS ========== -->
<!--
Copyright (c) 2004 Unicon, Inc.  All rights reserved.
Any reproduction or re-use of this code is governed by the End User License Agreement between Unicon, Inc. and the purchasing institution.

Author: Gary Thompson, gary@unicon.net
Version $LastChangedRevision$
-->
<!-- ========== END LEGAL, NOTES, AND INSTRUCTIONS ========== -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- ========== VARAIBLES AND PARAMETERS ========== -->
	<xsl:output method="html" indent="yes"/>
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="unauthenticated">false</xsl:param>
    <!-- deprecated params for old notifications display
	<xsl:param name="message">N</xsl:param>
	<xsl:param name="notificationAlert">0</xsl:param>
	<xsl:param name="noti_id">n74</xsl:param>
      -->
	<xsl:variable name="mediaPath" select="'media/net/unicon/portal/channels/login/'"/>
	<!-- ========== END VARAIBLES AND PARAMETERS ========== -->
	
	
	<!-- ========== TEMPLATES ========== -->
	<!-- XSL templates defined in this document for the purpose of generating output. Templates defined below may reference other templates defined in this document. -->

	<!-- ========== TEMPLATE: LOGIN-STATUS ========== -->
	<!-- Builds the login form with user ID and password inputs if unathenticated. If authenticated, it checks for notifications. -->
	<xsl:template match="login-status">
		<xsl:choose>
			<xsl:when test="$unauthenticated='true'">
				<div id="loginContainer">
					<form id="login" action="Login" method="post">
						<input type="hidden" name="action" value="login" />
						<label for="userName">Username:</label>
						<input type="text" name="userName" size="20">
							<xsl:attribute name="value"><xsl:value-of select="/login-status/failure/@attemptedUserName"/></xsl:attribute>
						</input>
						<label for="password">Password:</label>
						<input type="password" name="password" size="20" />
						<input type="submit" value="Sign In" name="Login" class="uportal-button" id="loginSubmit" />
                        <xsl:apply-templates />
					</form>
				</div>
			</xsl:when>
			<xsl:otherwise>
                <xsl:value-of select="full-name"/>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	<!-- ========== END TEMPLATE: LOGIN-STATUS ========== -->
	
	
	<!-- ========== TEMPLATE: LOGIN FAILURE ========== -->
	<!-- Reports a failure in the login process, where the username/password combination is not valid. It would be good to further delineate if the username was valid apart from the password for better feedback to the user. I.e., "Your User ID is not valid. Please try again." If the user ID is valid, then it would check against the password and spit out something like "The password you entered for your User ID is not valid. Check to ensure that your 'caps lock' is off, and try again." As it stands right now, the user is left to guess if he messed up his user ID or his password. -->
	<xsl:template match="failure">
	    <div class="login-alert"><!-- <h3><span>Alert:</span></h3> -->Your sign in information is not valid. Please try again.</div>
	</xsl:template>
	<!-- ========== END TEMPLATE: LOGIN FAILURE ========== -->
	
	
	<!-- ========== TEMPLATE: LOGIN ERROR ========== -->
	<!-- Reports a failure in the login process, though I am unsure as to what conditions elicit this failure. The error message is not very helpful either. -->
	<xsl:template match="error">
	    <div class="login-alert"><!-- <h3><span>Alert:</span></h3> -->An error occured during authentication. The portal is unable to log you on at this time. Please try again later.</div>
	</xsl:template>
	<!-- ========== END TEMPLATE: LOGIN ERROR ========== -->

</xsl:stylesheet>