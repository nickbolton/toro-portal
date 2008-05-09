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

        <xsl:call-template name="autoFormJS"/>

        <xsl:call-template name="links"/>
        <h2 class="page-title">Edit System Role Group</h2>
        
        <div class="bounding-box1">
			<form action="{$baseActionURL}?command=update_system_group&amp;type={$type}" method="post" name="permissionsForm" onsubmit="return validator.applyFormRules(this, new RoleAdminRulesObject());">
				<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap">
							<label for="roleAdminTitle">Title</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="group_name" size="35" maxlength="35" value="{$group_name}" id="roleAdminTitle">
							</input><span class="uportal-text-small"> (35 char max)</span>
						</td>
					</tr>
					<tr>
						<td class="table-light-single" colspan="2">
							<h4>Channel Permissions</h4>
						</td>
					</tr>
					<tr>
						<td  class="table-content-single" colspan="2">
							<xsl:apply-templates select="manifest/*[name()='channel' or name()='application']">
								<xsl:sort select="./label"/>
							</xsl:apply-templates>
						</td>
					</tr>
					<tr>
						<td class="table-nav" colspan="2">
						<input class="uportal-button" name="submit" value="Update" type="submit" title="Submit permission changes for the '{$group_name}' system role group"></input>
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel permission changes for the '{$group_name}' system role group"/></td>
					</tr>
				</table>
				<input type="hidden" name="group_key" value="{$group_key}"></input>
			</form>
		</div>
    </xsl:template>

    <xsl:template match="channel">
        <li><xsl:value-of select="./label"/>
            <ul style="list-style: none;">
                <xsl:apply-templates select="permissions/activity">
                    <xsl:sort select="./label"/>
                </xsl:apply-templates>
            </ul>
        </li>
    </xsl:template>

    <xsl:template match="application">
        <li><xsl:value-of select="./label"/>
            <ul style="list-style: none;">
                <xsl:apply-templates select="permissions/activity">
                    <xsl:sort select="./label"/>
                </xsl:apply-templates>
            </ul>
        </li>
    </xsl:template>

    <xsl:template match="activity">
        <li>
            <xsl:if test="@allowed = 'Y'">
            <input name="{../../@handle}-{@handle}" type="checkbox" class="radio" checked="true" id="roleAdminPermission{label}" style="margin-right: 5px;"></input>
            </xsl:if>
            
            <xsl:if test="@allowed != 'Y'">
            <input name="{../../@handle}-{@handle}" class="radio" type="checkbox" id="roleAdminPermission{label}" style="margin-right: 5px;"></input>
            </xsl:if>
            
            <label for="roleAdminPermission{label}"><xsl:value-of select="label"/></label><xsl:text> </xsl:text>
            <a href="javascript:alert('{description}');void(null);" title="Help for this option"
            onmouseover="swapImage('roleAdminHelpImage{@handle}','channel_help_active.gif')" 
            onmouseout="swapImage('roleAdminHelpImage{@handle}','channel_help_base.gif')"><img border="0" src=
            "{$CONTROLS_IMAGE_PATH}/channel_help_base.gif"
            alt="'Help' icon: help for '{label}'"
            title="'Help' icon: help for '{label}'" 
            align="absmiddle" name="roleAdminHelpImage{@handle}" id="roleAdminHelpImage{@handle}"/> 
            </a>
        </li>
    </xsl:template>

</xsl:stylesheet>
