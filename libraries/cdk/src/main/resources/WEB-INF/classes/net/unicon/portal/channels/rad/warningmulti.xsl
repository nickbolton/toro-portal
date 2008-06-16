<?xml version="1.0"?>
<!--

   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.

   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.

   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes"/>
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="sid">default</xsl:param>
	<xsl:param name="baseImagePath">media/net/unicon/portal/channels/rad</xsl:param>
	<xsl:template match="warning-multi">
		<form action="{$baseActionURL}" method="post">
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr>	<td class="uportal-channel-emphasis" height="20" width="100%" colspan="2"><i>Warning</i></td></tr>
				<tr>	<td class="uportal-background-light" width="100%" colspan="2"><img src="{$baseImagePath}/transparent.gif" border="0" height="2"/></td></tr>
				<tr>	<td width="100%" colspan="2"><img src="{$baseImagePath}/transparent.gif" border="0" height="6"/></td></tr>
				<tr>
					<td width="1%" align="center"><img border="0" src="{$baseImagePath}/error_32.gif"/></td>
					<td><table><xsl:apply-templates select="text"/></table></td>
				</tr>
				<tr>	<td width="100%" colspan="2"><img src="{$baseImagePath}/transparent.gif" border="0" height="6"/></td></tr>
				<tr class="uportal-background-light" height="22">
					<td width="100%" colspan="2">
						<img src="{$baseImagePath}/transparent.gif" border="0" width="1"/>
						<input type="hidden" name="sid" value="{$sid}"/>
						<input class="uportal-button" type="submit" value="OK" name="do~ok"/>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>

	<!-- ////////////////////////// -->
	<xsl:template match="text">
		<tr class="uportal-background-content"><td class="uportal-channel-text" width="100%"><xsl:text>&#160;&#160;</xsl:text><xsl:value-of select="."/></td></tr>
	</xsl:template>
</xsl:stylesheet>
