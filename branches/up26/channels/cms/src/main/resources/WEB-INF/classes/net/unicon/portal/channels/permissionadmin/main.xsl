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
    <h2 class="page-title">Manage Roles</h2>
    
    <div class="bounding-box1">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th class="th-top-left" id="roleAdminRoleType">Role Type</th>
            <th class="th-top-right" id="roleAdminRole">Role</th>
        </tr>
        <tr>
            <td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="roleAdminRoleType">
                <label for="roleAdminSysPerm">System Roles</label>
            </td>
            <td class="table-content-right" nowrap="nowrap" headers="roleAdminRole">
	        <form name="permissionsForm" action="{$baseActionURL}" method="post">
	            <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
	            <input type="hidden" name="command" value=""></input>
	            <input type="hidden" name="type" value="system"></input>
	
                <xsl:apply-templates select="./administration/system-permissions"/>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$addAdminPermissions = 'Y'">
                        <a href="{$baseActionURL}?command=add_role&amp;type=system" title="Add a new system-level role"
                        onmouseover="swapImage('roleAdminSystemAddImage','channel_add_active.gif')" 
                        onmouseout="swapImage('roleAdminSystemAddImage','channel_add_base.gif')"><img border="0" src=
                        "{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
                        alt="'Add' icon: add system-level role"
                        title="'Add' icon: add system-level role" 
                        align="absmiddle" name="roleAdminSystemAddImage" id="roleAdminSystemAddImage"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif"
                        alt="Inactive 'Add' icon: add system-level role unavailable due to lack of permission" 
                        title="Inactive 'Add' icon: add system-level role unavailable due to lack of permission" 
                        align="absmiddle"/>
                    </xsl:otherwise>
                </xsl:choose>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$editAdminPermissions = 'Y'">
                        <a href="javascript:void(document.permissionsForm.command.value='edit_system_group',document.permissionsForm.submit());" title=
                        "Edit this system-level role"
                        onmouseover="swapImage('roleAdminSystemEditImage','channel_edit_active.gif')" 
                        onmouseout="swapImage('roleAdminSystemEditImage','channel_edit_base.gif')">
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                        alt="'Edit' icon: edit selected system-level role" 
                        title="'Edit' icon: edit selected system-level role" 
                        align="absmiddle" name="roleAdminSystemEditImage" id="roleAdminSystemEditImage"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
                        alt="Inactive 'Edit' icon: edit system-level role unavailable due to lack of permission" 
                        title="Inactive 'Edit' icon: edit system-level role unavailable due to lack of permission" 
                        align="absmiddle"/>
                    </xsl:otherwise>
                </xsl:choose>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$deleteAdminPermissions = 'Y'">
                        <a href="javascript:void(document.permissionsForm.command.value='delete_system_group',document.permissionsForm.submit());" title=
                        "Delete this system-level role"
                        onmouseover="swapImage('roleAdminSystemDeleteImage','channel_delete_active.gif')" 
                        onmouseout="swapImage('roleAdminSystemDeleteImage','channel_delete_base.gif')">
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                        alt="'Delete' icon: delete selected system-level role" 
                        title="'Delete' icon: delete selected system-level role" 
                        align="absmiddle" name="roleAdminSystemDeleteImage" id="roleAdminSystemDeleteImage"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
                        alt="Inactive 'Delete' icon: deleting system-level role unavailable due to lack of permission"
                        title="Inactive 'Delete' icon: deleting system-level role unavailable due to lack of permission"
                        align="absmiddle"/>
                    </xsl:otherwise>
                </xsl:choose>
	        </form>
            </td>
        </tr>
        <tr>
            <td class="table-light-left-bottom" style="text-align:right;" nowrap="nowrap" headers="roleAdminRoleType">
                <label for="roleAdminOffPerm">Offering Roles</label>
            </td>
            <td class="table-content-right-bottom" headers="roleAdminRole">
	        <form name="permissionsOfferingForm" action="{$baseActionURL}" method="post">
	            <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
	            <input type="hidden" name="command" value=""></input>
	            <input type="hidden" name="type" value="offering"></input>

                <xsl:apply-templates select="./administration/offering-permissions"/>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$addOfferingPermissions = 'Y'">
                        <a href="{$baseActionURL}?command=add_role&amp;type=offering" title="Add a new offering-level role"
                        onmouseover="swapImage('roleAdminOfferingAddImage','channel_add_active.gif')" 
                        onmouseout="swapImage('roleAdminOfferingAddImage','channel_add_base.gif')">
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
                        alt="'Add' icon: add offering-level role" 
                        title="'Add' icon: add offering-level role"
                        align="absmiddle" name="roleAdminOfferingAddImage" id="roleAdminOfferingAddImage"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif"
                        alt="Inactive 'Add' icon: add offering-level role unavailable due to lack of permission" 
                        title="Inactive 'Add' icon: add offering-level role unavailable due to lack of permission" 
                        align="absmiddle" />
                    </xsl:otherwise>
                </xsl:choose>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$editOfferingPermissions = 'Y'">
                        <a href="javascript:document.permissionsOfferingForm.command.value='edit_role';document.permissionsOfferingForm.submit()" title=
                        "Edit this offering-level role"
                        onmouseover="swapImage('roleAdminOfferingEditImage','channel_edit_active.gif')" 
                        onmouseout="swapImage('roleAdminOfferingEditImage','channel_edit_base.gif')">
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                        alt="'Edit' icon: edit selected offering-level role" 
                        title="'Edit' icon: edit selected offering-level role" 
                        align="absmiddle" name="roleAdminOfferingEditImage" id="roleAdminOfferingEditImage"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
                        alt="Inactive 'Edit' icon: edit offering-level role unavailable due to lack of permission" 
                        title="Inactive 'Edit' icon: edit offering-level role unavailable due to lack of permission"
                        align="absmiddle" />
                    </xsl:otherwise>
                </xsl:choose>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$deleteOfferingPermissions = 'Y'">
                        <a href="javascript:document.permissionsOfferingForm.command.value='delete_role';document.permissionsOfferingForm.submit()" title=
                        "Delete this offering-level role"
                        onmouseover="swapImage('roleAdminOfferingDeleteImage','channel_delete_active.gif')" 
                        onmouseout="swapImage('roleAdminOfferingDeleteImage','channel_delete_base.gif')">
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                        alt="'Delete' icon: delete selected offering-level role" 
                        title="'Delete' icon: delete selected offering-level role" 
                        align="absmiddle" name="roleAdminOfferingDeleteImage" id="roleAdminOfferingDeleteImage"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
                        alt="Inactive 'Delete' icon: deleting offering-level role unavailable due to lack of permission"  
                        title="Inactive 'Delete' icon: deleting offering-level role unavailable due to lack of permission"
                        align="absmiddle" />
                    </xsl:otherwise>
                </xsl:choose>
            </form>
            </td>
        </tr>
    </table>
   </div>
</xsl:template>

    <xsl:template match="system-permissions">
        <select name="group_key" id="roleAdminSysPerm">
            <xsl:apply-templates select="group"/>
        </select>
    </xsl:template>
    
    <xsl:template match="offering-permissions">
        <select name="role_id_and_name" id="roleAdminOffPerm">
            <xsl:apply-templates select="role"/>
        </select>
    </xsl:template>

    <xsl:template match="role">
        <option value="{@id}.{label}"><xsl:value-of select="label"/></option>
    </xsl:template>

    <xsl:template match="group">
        <option value="{@key}"><xsl:value-of select="name"/></option>
    </xsl:template>
</xsl:stylesheet>
