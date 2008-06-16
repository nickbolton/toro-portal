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
<xsl:variable name = "XHTMLTags">
|a|abbr|acronym|address|applet|area|b|basefont|bdo|big|blockquote|br|button|caption|center|cite|code|col|colgroup|dd|del|dir|div|dfn|dl|dt|em|fieldset|font|form|h1|h2|h3|h4|h5|h6|hr|i|iframe|img|input|ins|kbd|label|legend|li|map|menu|noscript|object|ol|optgroup|option|p|param|pre|q|s|samp|script|select|small|span|strike|strong|style|sub|sup|table|tbody|td|textarea|tfoot|th|thead|title|tr|tt|u|ul|var|
</xsl:variable>

<!-- ############################################### -->
<!-- HTML Elements -->
<xsl:template match="a | abbr | acronym | address | applet | area | b | basefont | bdo | big | blockquote | br | button | caption | center | cite | code | col | colgroup | dd | del | dir | div | dfn | dl | dt | em | fieldset | font | form | h1 | h2 | h3 | h4 | h5 | h6 | hr | i | iframe | img | input | ins | kbd | label | legend | li | map | menu | noscript | object | ol | optgroup | option | p | param | pre | q | s | samp | script | select | small | span | strike | strong | style | sub | sup | table | tbody | td | textarea | tfoot | th | thead | title | tr | tt | u | ul | var">
	<xsl:copy> 
		<xsl:copy-of select = "@*"/>
		<xsl:apply-templates />
	</xsl:copy>
</xsl:template>

<!-- Uppercase HTML Elements (convert to lowercase for XHTML) -->
<xsl:template match="A | ABBR | ACRONYM | ADDRESS | APPLET | AREA | B | BASEFONT | BDO | BIG | BLOCKQUOTE | BR | BUTTON | CAPTION | CENTER | CITE | CODE | COL | COLGROUP | DD | DEL | DIR | DIV | DFN | DL | DT | EM | FIELDSET | FONT | FORM | H1 | H2 | H3 | H4 | H5 | H6 | HR | I | IFRAME | IMG | INPUT | INS | KBD | LABEL | LEGEND | LI | MAP | MENU | META | NOFRAMES | NOSCRIPT | OBJECT | OL | OPTGROUP | OPTION | P | PARAM | PRE | Q | S | SAMP | SCRIPT | SELECT | SMALL | SPAN | STRIKE | STRONG | STYLE | SUB | SUP | TABLE | TBODY | TD | TEXTAREA | TFOOT | TH | THEAD | TITLE | TR | TT | U | UL | VAR">
	<xsl:variable name = "lowercaseName"><xsl:call-template name = "to-lowercase" ><xsl:with-param name="string" select="name()"></xsl:with-param></xsl:call-template></xsl:variable>
	<xsl:element name = "{$lowercaseName}">
		<xsl:apply-templates select = "@*" mode="attribute-to-lowercase" />
		<xsl:apply-templates />
	</xsl:element>
</xsl:template>

<!-- Convert Attribute Name to Lowercase (for XHTML) -->
<xsl:template match="@*" mode="attribute-to-lowercase">
	<xsl:variable name = "lowercaseName"><xsl:call-template name = "to-lowercase" ><xsl:with-param name="string" select="name()"></xsl:with-param></xsl:call-template></xsl:variable>
	<xsl:attribute name = "{$lowercaseName}" ><xsl:value-of select="." /></xsl:attribute>
</xsl:template>
<!-- ############################################### -->


<xsl:template match="textarea">
	<xsl:copy> 
		<xsl:copy-of select = "@*"/>
		<xsl:copy-of select = "*"/>
	</xsl:copy>
</xsl:template>

<!-- ############################################### -->
<!-- TABLE -->
<!-- Exclude additional attributes: even-row-class, odd-row-class -->
<xsl:template name="table" match="table">
	<xsl:copy> 
		<xsl:copy-of select = "@*[name(.)!='even-row-class' and name(.)!='odd-row-class' and name(.)!='row-class' and name(.)!='selected-row-class' and name(.)!='highlight-row-class' and name(.)!='selected-row-id']"/>
		<xsl:apply-templates />
	</xsl:copy>
</xsl:template>

<!-- TR -->
<!-- Add support for defining even-row-class and odd-row-class attributes on table -->
<xsl:template name="tr" match="tr">
	<xsl:variable name = "table" select = "ancestor::table" />
	<xsl:copy> 
		<xsl:copy-of select = "@*"/>
		<xsl:if test = "not(@class) and ($table/@even-row-class or $table/@odd-row-class)">
			<xsl:choose>
				<xsl:when test="($table/@even-row-class) and (position() mod 2 = 0)"><xsl:attribute  name = "class" ><xsl:value-of select="$table/@even-row-class" /></xsl:attribute></xsl:when>
				<xsl:when test="($table/@odd-row-class) and (position() mod 2 = 1)"><xsl:attribute  name = "class" ><xsl:value-of select="$table/@odd-row-class" /></xsl:attribute></xsl:when>
			</xsl:choose>
        </xsl:if>
		<xsl:apply-templates />
	</xsl:copy>
</xsl:template>

<!-- Catch 1st pass on col. If first col then for each row wrap in <tr/> and call other columns to writeRows, else ignore -->
<xsl:template name="col" match="col">
	<xsl:param name="row">1</xsl:param>
	<xsl:variable name = "columns" select = "../col" />
	<xsl:variable name = "table" select = "ancestor::table" />
	<xsl:if test = "generate-id(.) = generate-id($columns[position()=1])">
		<xsl:for-each select = "descendant::*[name(.)='td' or name(.)='th']">
		<xsl:variable name = "rowpos" select = "position()" />
		<tr>
			<xsl:if test = "$table/@even-row-class or $table/@odd-row-class">
				<xsl:choose>
					<xsl:when test="($table/@even-row-class) and ($rowpos mod 2 = 0)"><xsl:attribute  name = "class" ><xsl:value-of select="$table/@even-row-class" /></xsl:attribute></xsl:when>
					<xsl:when test="($table/@odd-row-class) and ($rowpos mod 2 = 1)"><xsl:attribute  name = "class" ><xsl:value-of select="$table/@odd-row-class" /></xsl:attribute></xsl:when>
				</xsl:choose>
			</xsl:if>
	        <xsl:if test="($table/@selected-row-class) and ($table/@selected-row-id) and (descendant::node()[@id = $table/@selected-row-id])">
	        	<xsl:attribute  name = "class" ><xsl:value-of select="$table/@selected-row-class" /></xsl:attribute>
	        </xsl:if>
			<xsl:apply-templates select = "$columns" mode="writeRows">
				<xsl:with-param name="row" select="$rowpos" />
			</xsl:apply-templates>
		</tr>
		</xsl:for-each>
	</xsl:if>
</xsl:template>

<xsl:template name="writeRows" match="col" mode="writeRows">
	<xsl:param name="row">1</xsl:param>
	<xsl:variable name = "td_n_th" select="descendant::*[name(.)='td' or name(.)='th']" />

	<xsl:apply-templates select = "$td_n_th[position()=$row]"></xsl:apply-templates>
	
</xsl:template>
<!-- ############################################### -->

</xsl:stylesheet>
