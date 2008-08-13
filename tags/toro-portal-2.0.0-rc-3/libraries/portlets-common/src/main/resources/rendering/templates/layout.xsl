<?xml version="1.0"?>
<!--
  Copyright (C) 2007 Unicon, Inc.

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this distribution.  It is also available here:
  http://www.fsf.org/licensing/licenses/gpl.html

  As a special exception to the terms and conditions of version 
  2 of the GPL, you may redistribute this Program in connection 
  with Free/Libre and Open Source Software ("FLOSS") applications 
  as described in the GPL FLOSS exception.  You should have received
  a copy of the text describing the FLOSS exception along with this
  distribution.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:import href = "functions.xsl" />
    <xsl:import href = "html.xsl" />
    <xsl:import href = "widgets.xsl" />
    <xsl:import href = "controls.xsl" />
    <xsl:import href = "links.xsl" />

    <xsl:output method="xml" omit-xml-declaration="yes"/>

	<xsl:param name="namespace" select="'Pluto_853E13074FBA0C5AADD99A0887887A64/2001_265_'" />
    <xsl:param name="screenID" select="/screen/@handle" />
    <xsl:param name="appsRoot">http://localhost:8088/toro-portlets-common</xsl:param>
	<!--<xsl:param name="appsRoot">../../../..</xsl:param> -->

	<xsl:variable name = "filteredNameSpace">
		<xsl:call-template name="replace-string">
			<xsl:with-param name="string" select="$namespace" />
			<xsl:with-param name="replace" select="'/'" />
			<xsl:with-param name="with" select="'_'" />
		</xsl:call-template>
	</xsl:variable>
	
    <xsl:template match="/">
        <xsl:comment>Comment to Fix Serializer Bug?</xsl:comment>
        <script src="{$appsRoot}/rendering/javascript/common.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
        <script src="{$appsRoot}/rendering/javascript/formValidation.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
        <xsl:if test = "//sequence">
	        <script src="{$appsRoot}/rendering/javascript/yui/yahoo.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
	        <script src="{$appsRoot}/rendering/javascript/yui/connection.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
	        <script src="{$appsRoot}/rendering/javascript/uniconURLSequencer.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
        </xsl:if>
        <xsl:if test="//attach-behaviors">
	        <script src="{$appsRoot}/rendering/javascript/yui/yahoo.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
	        <script src="{$appsRoot}/rendering/javascript/yui/connection.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
	        <script src="{$appsRoot}/rendering/javascript/yui/event.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
	        <script src="{$appsRoot}/rendering/javascript/yui/dom.js" language="JavaScript" type="text/javascript"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
        </xsl:if>
        <xsl:apply-templates select = "screen/layout" />
        <!--<xsl:apply-templates select = "screen/decisions" /> -->
    </xsl:template>

</xsl:stylesheet>
