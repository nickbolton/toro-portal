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

<!-- ############################################### -->
<!-- ESCAPE-CHARACTERS -->
<xsl:template name="escape-characters">
	<xsl:param name="string" />
	<xsl:variable name = "backslash-escaped"><xsl:call-template name="replace-string">
		<xsl:with-param name="string" select="$string" />
			<xsl:with-param name="replace" select='"\"' />
			<xsl:with-param name="with" select='"\\"' />
		</xsl:call-template></xsl:variable>
	<xsl:call-template name="replace-string">
		<xsl:with-param name="string" select="$backslash-escaped" />
		<xsl:with-param name="replace" select='"&apos;"' />
		<xsl:with-param name="with" select='"\&apos;"' />
	</xsl:call-template>
</xsl:template>

<!-- REPLACE STRING -->
<xsl:template name="replace-string">
   <xsl:param name="string" />
   <xsl:param name="replace" />
   <xsl:param name="with" />
   <xsl:choose>
      <!-- If the string contains the replace string -->
      <xsl:when test='contains($string, $replace)'>
         <!-- Then output the value before the replace string -->
         <xsl:value-of select="substring-before($string, $replace)" />
         <!-- Output what it will be replaced with (i.e. with string) -->
         <xsl:value-of select="$with" />
         <!-- Call this template recursively passing in the remainder of the string -->
         <xsl:call-template name="replace-string">
            <xsl:with-param name="string" select="substring-after($string, $replace)" />
            <xsl:with-param name="replace" select="$replace" />
            <xsl:with-param name="with" select="$with" />
         </xsl:call-template>
      </xsl:when>
      <!-- Else -->
      <xsl:otherwise>
         <!-- Simply output the string, as is -->
         <xsl:value-of select="$string" />
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- STRING-TO-ELEMENT -->
<xsl:template name="string-to-element">
   <xsl:param name="string" />
   <xsl:param name="replace" />
   <xsl:param name="with-element" />
   <xsl:choose>
      <!-- If the string contains the replace string -->
      <xsl:when test='contains($string, $replace)'>
         <!-- Then output the value before the replace string -->
         <xsl:value-of select="substring-before($string, $replace)" />
         
         <!-- Output what it will be replaced with (i.e. element) -->
         <xsl:element name="{$with-element}"></xsl:element>

         <!-- Call this template recursively passing in the remainder of the string -->
         <xsl:call-template name="string-to-element">
            <xsl:with-param name="string" select="substring-after($string, $replace)" />
            <xsl:with-param name="replace" select="$replace" />
            <xsl:with-param name="with-element" select="$with-element" />
         </xsl:call-template>
      </xsl:when>
      <!-- Else -->
      <xsl:otherwise>
         <!-- Simply output the string, as is -->
         <xsl:value-of select="$string" />
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- ############################################### -->
<!-- LOWERCASE -->
<!-- Function that returns the argument string as all lowercase -->
<xsl:template name="to-lowercase">
	<xsl:param name="string" />
	<xsl:value-of select="translate($string,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')" />
</xsl:template>

<!-- ############################################### -->
<!-- ESCAPE-FOR-ATTRIBUTE -->
<!-- Function that escapes characters for attributes -->
<xsl:template name="escape-for-attribute">
	<xsl:param name="string" />
    <xsl:call-template name="replace-string">
       <xsl:with-param name="string" select="$string" />
       <xsl:with-param name="replace">"</xsl:with-param>
       <xsl:with-param name="with" select="'&#034;'" />
    </xsl:call-template>
</xsl:template>

<!-- ############################################### -->
<!-- ESCAPE-FOR-JS-VAR -->
<!-- Function that escapes characters for JavaScript in attributes -->
<xsl:template name="escape-for-js-var">
	<xsl:param name="string" />
    <xsl:call-template name="replace-string">
       <xsl:with-param name="string" select="$string" />
       <xsl:with-param name="replace">'</xsl:with-param>
       <xsl:with-param name="with">\&#039;</xsl:with-param>
    </xsl:call-template>
</xsl:template>

<!-- ############################################### -->
<!-- ESCAPE-FOR-JS-VAR-ATTR -->
<!-- Function that escapes characters for JavaScript in attributes -->
<xsl:template name="escape-for-js-var-attr">
	<xsl:param name="string" />
    <xsl:variable name = "double-quote-escaped"><xsl:call-template name="escape-for-attribute">
       <xsl:with-param name="string" select="$string" />
    </xsl:call-template></xsl:variable>
    <xsl:call-template name="escape-for-js-var">
       <xsl:with-param name="string" select="$double-quote-escaped" />
    </xsl:call-template>
</xsl:template>

<!-- ############################################### -->
<!-- ESCAPE-FOR-ID-ATTR -->
<!-- Function that escapes characters for id attribute use -->
<xsl:template name="escape-for-id-attr">
	<xsl:param name="string" />
   <xsl:value-of select="translate($string,'+= /@$','______')" />
</xsl:template>

<!-- ############################################### -->
<!-- WRITE-ALL-HIDDEN-FORMS -->
<!-- Function that calls all descendant nodes that need to create hidden forms to keep from nesting forms -->
<xsl:template name="write-all-hidden-forms">
    <xsl:apply-templates select = "descendant::link//sequence">
		<xsl:with-param name="formOnly">true</xsl:with-param>
	</xsl:apply-templates>
</xsl:template>

<!-- ############################################### -->

</xsl:stylesheet>
