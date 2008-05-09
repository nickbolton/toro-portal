<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="../global/global.xsl"/>
<xsl:include href="../global/toolbar.xsl"/>

<!-- parameters -->

<xsl:param name="current_command"/>

<xsl:param name="type"/>
<xsl:param name="role_name"/>
<xsl:param name="role_id"/>
<xsl:param name="group_name"/>
<xsl:param name="group_key"/>

<!-- privleges -->
<xsl:param name="addAdminPermissions"/>
<xsl:param name="deleteAdminPermissions"/>
<xsl:param name="editAdminPermissions"/>
<xsl:param name="addOfferingPermissions"/>
<xsl:param name="deleteOfferingPermissions"/>
<xsl:param name="editOfferingPermissions"/>

<!-- Common -->
<xsl:template name="autoFormJS">
	<script language="JavaScript" type="text/javascript" src="javascript/PermissionsChannel/autoForm.js"></script>
</xsl:template>
    
<xsl:template name="links">

	<div class="portlet-toolbar-container">
		<xsl:choose>
			<xsl:when test="$current_command = 'general'">
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">View Roles</xsl:with-param>
					<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">View Roles</xsl:with-param>
					<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/></xsl:with-param>
					<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>	
	</div>
			
	<!-- UniAcc: Layout Table -->
    <!--
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="views-title">
			<img border="0" src=
			"{$CONTROLS_IMAGE_PATH}/channel_options.gif"
			alt="Icon of tool-tip indicating channel options section" 
			title="Icon of tool-tip indicating channel options section" align="absmiddle"/></td>
        <td class="views" valign="middle" height="26" width="100%">
		
			<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
		</td>
    </tr>
    </table>
    -->
</xsl:template>

</xsl:stylesheet>

