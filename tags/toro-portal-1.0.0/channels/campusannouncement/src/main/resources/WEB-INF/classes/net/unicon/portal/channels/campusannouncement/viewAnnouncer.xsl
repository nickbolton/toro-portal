<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
<!--<xsl:variable name="SELECTED_GROUP_ID"><xsl:value-of select="/announcement-edit/announcement/@category-id"/></xsl:variable>


	<xsl:template match="/">
	    <textarea rows="4" cols="40">
	      <xsl:copy-of select = "*"/> 
	      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
	      <parameter name="pageChannel"><xsl:value-of select="$pageChannel" /></parameter>
	      <parameter name="channel_admin"><xsl:value-of select="$channel_admin" /></parameter>
	   	</textarea>
	
		<xsl:apply-templates />
	</xsl:template> -->
	
    <xsl:template match="/announcers">
		<xsl:call-template name="links"/>

    	<h2 class="page-title">Current Announcement Administrators</h2>
    	
    	<div class="bounding-box1">
			<form action="{$baseActionURL}?command=admin-announcer" method="post">
				<input type="hidden" name="sub-command" value="submit-announcer" />
				<input type="hidden" name="announcement-id" value="{./announcement/@announcement-id}" />
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
				  	<tr>
				  		<th class="th-left">User</th>
				  		<th class="th-right">Permissions</th>
				  	</tr>
				  <xsl:for-each select="announcer">
					<xsl:sort select="@permissionType"/>
					<tr>                
					  <td class="table-content-left" align="left" width="100%">
						<xsl:value-of select="@name" />
						<xsl:choose>
						  <xsl:when test="@type='2'">(Person)</xsl:when>
						  <xsl:when test="@type='3'">(Group)</xsl:when>
						</xsl:choose>
					  </td>
					  <td class="table-content-right">
						<xsl:choose>
				  <xsl:when test="@permissionType='GRANT'">Granted</xsl:when>
				  <xsl:when test="@permissionType='DENY'">Denied</xsl:when>
						</xsl:choose>
					  </td>
					</tr>
				  </xsl:for-each>
				  <xsl:if test="count(announcer)=0">
					<tr>
					  <td class="uportal-channel-table-row-even" align="left" width="100%" colspan="2">
						No Groups/Persons have been assigned as administrator.
					  </td>
					</tr>
				  </xsl:if>
					<tr>
					  <td colspan="2" class="table-nav">
						<input name="Submit" value="Change" type="submit" class="uportal-button" title="To select other person/group as announcer"/>
						<input name="OK" value="OK" type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}'" title="To cancel editing this announcement, returning to viewing the announcements"/>
					  </td>
					</tr>
				</table>
			</form>
        </div>
    </xsl:template>

</xsl:stylesheet>
