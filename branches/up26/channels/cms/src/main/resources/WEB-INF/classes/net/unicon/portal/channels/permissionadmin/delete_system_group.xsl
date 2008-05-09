<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="/">
    <!--<textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/> 
      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
      <parameter name="type"><xsl:value-of select="$type" /></parameter>
      <parameter name="role_name"><xsl:value-of select="$role_name" /></parameter>
      <parameter name="role_id"><xsl:value-of select="$role_id" /></parameter>
      <parameter name="addAdminPermissions"><xsl:value-of select="$addAdminPermissions" /></parameter>
      <parameter name="deleteAdminPermissions"><xsl:value-of select="$deleteAdminPermissions" /></parameter>
      <parameter name="editAdminPermissions"><xsl:value-of select="$editAdminPermissions" /></parameter>
      <parameter name="addOfferingPermissions"><xsl:value-of select="$addOfferingPermissions" /></parameter>
      <parameter name="deleteOfferingPermissions"><xsl:value-of select="$deleteOfferingPermissions" /></parameter>
      <parameter name="editOfferingPermissions"><xsl:value-of select="$editOfferingPermissions" /></parameter>
   	</textarea> -->

  
    <xsl:call-template name="links"/>
    
    <h2 class="page-title">Delete System Role Group</h2>
    
    <div class="bounding-box1">
		<form name="permissionsForm" action="{$baseActionURL}" method="post">
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td class="table-content-single-top" style="text-align:center">
					<span class="uportal-channel-warning">Are you sure you want to delete the <span class="uportal-channel-strong">"<xsl:value-of select="$group_name"/>"</span> system role group?</span>
					<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
					<input type="hidden" name="command" value="confirm_delete_system_group"></input>
					<input type="hidden" name="group_key" value="{$group_key}"></input>
				</td>
			</tr>
			<tr>
				<td class="table-content-single" style="text-align:center">
					<input type="radio" name="confirm_delete_system_group" value="yes" id="roleAdminDeleteConfirm"/><label for="roleAdminDeleteConfirm">&#160;Yes</label>
					<img height="1" width="15" src="{$SPACER}" alt="" title=""/>
					<input checked="checked" type="radio" name="confirm_delete_system_group" value="no" id="roleAdminDeleteDeny"/><label for="roleAdminDeleteDeny">&#160;No</label><br />
				</td>
			</tr>
			<tr>
				<td class="table-content-single-bottom" style="text-align:center">
					<input type="submit" class="uportal-button" value="Submit" title="Submit deletion for the '{$group_name}' system role group"/>
					<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel deletion for the '{$group_name}' system role group and return to the main view"/>
				</td>
			</tr>
			 </table>
		</form>
	</div>
  </xsl:template>

</xsl:stylesheet>











