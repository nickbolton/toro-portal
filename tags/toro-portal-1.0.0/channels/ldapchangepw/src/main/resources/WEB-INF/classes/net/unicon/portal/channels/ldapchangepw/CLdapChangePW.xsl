<?xml version="1.0"?>

<!--
Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."

THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Author: Ken Weiner, kweiner@unicon.net
Version $LastChangedRevision$
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>

<xsl:template match="content">
  <xsl:choose>
    <xsl:when test="userNotFound">
        <div align="center" class="text">Sorry, your account was not found in the user directory.  Please contact your support desk.</div><br/>
    </xsl:when>
    <xsl:when test="wrongCurrentPassword">
        <div align="center" class="text">Sorry, the password you entered does not match your current password.</div><br/>
    </xsl:when>
    <xsl:when test="noConfirm">
	<div align="center" class="text">Sorry, the passwords you entered do not match.  Please try again.</div><br/>
    </xsl:when>
    <xsl:when test="Confirm">
        <div align="center" class="text">Your password has been changed.</div><br/>
    </xsl:when>
    <xsl:otherwise>
    <div align="center" class="text">You may use the fields below to change your password:</div><br/>
    </xsl:otherwise>
  </xsl:choose> 
    <form action="{$baseActionURL}" method="post">
      <input type="hidden" name="uP_root" value="root"/>
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <xsl:choose>
	<xsl:when test="askForCurrent">
	<tr><td><div align="right">
	Current Password:
	</div></td><td><div align="left">
        <input type="password" name="curPass" size="16" class="uportal-input-text"/>
	</div></td></tr>
	</xsl:when>
      </xsl:choose>
	<tr><td><div align="right">
        New Password: 
	</div></td><td><div align="left">
	<input type="password" name="newPass" size="16" class="uportal-input-text"/>
	</div></td></tr><tr><td><div align="right">
        Confirm Password:
	</div></td><td><div align="left">
	<input type="password" name="confirmPass" size="16" class="uportal-input-text"/>
	</div></td></tr></table><br/>
	<div align="center">
      <input type="submit" value="submit" class="uportal-button"/>
	</div>
    </form>
  
</xsl:template>

</xsl:stylesheet> 
