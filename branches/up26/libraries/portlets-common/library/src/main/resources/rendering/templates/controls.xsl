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
<!-- CONTROLS -->
<!-- IF -->
<xsl:template match="if">
    <xsl:choose>
        <xsl:when test="(@condition='eq' or @condition='equals') and (@test = @against)">
            <xsl:apply-templates select="true" />
        </xsl:when>
        <xsl:when test="(@condition='gt' or @condition='greater than') and (@test &gt; @against)">
            <xsl:apply-templates select="true" />
        </xsl:when>
        <xsl:when test="(@condition='lt' or @condition='less than') and (@test &lt; @against)">
            <xsl:apply-templates select="true" />
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates select="false" />
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- COPY -->
<!-- Element to copy another element to this location, whose copy-id matches this elements ref-id -->
<xsl:template name="copy" match="copy">
	<xsl:variable name = "ref-id" select = "@ref-id" />
	
	<xsl:apply-templates select = "/descendant::*[@copy-id=$ref-id]" />
	
</xsl:template>


<!-- ############################################### -->

<!-- ############################################### -->
<!-- IGNORED elements -->

<!-- Ignore any <state></state> elements as they are for sample data only -->
<xsl:template name="state" match="state"></xsl:template>

<!-- If decisions is called, output values in textarea for debugging -->
<xsl:template match="decisions">
	<form>
		<textarea>
			<xsl:copy-of select = "*"/>
		</textarea>
	</form>
</xsl:template>
<!-- ############################################### -->

<!-- VIRTUAL / REFERENCE -->
<!--<xsl:template name="virtual" match="virtual"></xsl:template>

<xsl:template name="reference" match="reference">

    <xsl:variable name = "virtual-ref" select = "/descendant::virtual" />
    <xsl:variable name = "choice-handle" select = "./@choice-handle" />
    <xsl:variable name = "option-handle" select = "./@option-handle" />
    <a title="{label}">
        <xsl:attribute  name = "href" ><xsl:value-of select="$actionUrl" />&amp;act_<xsl:value-of select="@action-handle" />=act_<xsl:value-of select="@action-handle" />&amp;<xsl:value-of select="@choice-handle" />=<xsl:value-of select="@option-handle" /></xsl:attribute>
        <xsl:apply-templates></xsl:apply-templates>
    </a>

</xsl:template> -->



</xsl:stylesheet>