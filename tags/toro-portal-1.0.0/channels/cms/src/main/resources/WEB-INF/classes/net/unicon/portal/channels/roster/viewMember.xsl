<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:include href="common.xsl"/>
	<xsl:output method="html" indent="yes"/>
	<xsl:template match="/">
		<!-- <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
    </textarea> -->
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="user">
		<xsl:call-template name="links"/>
		<form method="post" name="rosterUserForm" action="{$baseActionURL}">
			<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
			<input type="hidden" name="catPageSize" value="{$catPageSize}"/>
			<!-- UniAcc: Data Table -->
			<table cellpadding="2" cellspacing="0" border="0" width="100%">
				<tr>
					<th class="th-top-left" width="30%" scope="col">Property</th>
					<th class="th-top-right">Value</th>
				</tr>
				<tr>
					<td class="table-light-left" style="text-align:right" scope="col">User ID</td>
					<td class="table-content-right">
						<xsl:value-of select="@id"/>
					</td>
				</tr>
				<tr>
					<td class="table-light-left" style="text-align:right" scope="row">First Name</td>
					<td class="table-content-right">
						<xsl:value-of select="firstname"/>
					</td>
				</tr>
				<tr>
					<td class="table-light-left" style="text-align:right" scope="row">Middle Initial</td>
					<td class="table-content-right">
					</td>
				</tr>
				<tr>
					<td class="table-light-left" style="text-align:right" scope="row">Last Name</td>
					<td class="table-content-right">
						<xsl:value-of select="lastname"/>
					</td>
				</tr>
				<tr>
					<td class="table-light-left" style="text-align:right" scope="row">User Type</td>
					<td class="table-content-right">
						<xsl:value-of select="type"/>
					</td>
				</tr>
				<tr>
					<td class="table-light-left" style="text-align:right" scope="row">User Status</td>
					<td class="table-content-right">
						<xsl:value-of select="status"/>
					</td>
				</tr>
				<tr>
					<td class="table-light-left-bottom" style="text-align:right" scope="row">Email Address</td>
					<td class="table-content-right-bottom">
						<xsl:value-of select="email"/>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
	<xsl:template match="role">
  </xsl:template>
</xsl:stylesheet>
