<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<xsl:template match="/">
		<!-- <textarea rows="4" cols="100">
        <xsl:copy-of select = "*"/>
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>
        <parameter name="in_use"><xsl:value-of select="$in_use" /></parameter>
        <parameter name="first_name"><xsl:value-of select="$first_name" /></parameter>
        <parameter name="last_name"><xsl:value-of select="$last_name" /></parameter>
        <parameter name="email"><xsl:value-of select="$email" /></parameter>
        <parameter name="user_name"><xsl:value-of select="$user_name" /></parameter>
        <parameter name="deleteCommand"><xsl:value-of select="$deleteCommand" /></parameter>
        <parameter name="deleteConfirmationParam"><xsl:value-of select="$deleteConfirmationParam" /></parameter>
        <parameter name="addUser"><xsl:value-of select="$addUser" /></parameter>
        <parameter name="editUser"><xsl:value-of select="$editUser" /></parameter>
        <parameter name="deleteUser"><xsl:value-of select="$deleteUser" /></parameter>
        <parameter name="searchUser"><xsl:value-of select="$searchUser" /></parameter>
    	</textarea> -->
		<xsl:call-template name="links"/>
		<!-- UniAcc: Data Table -->
		<div class="bounding-box1">
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<th class="th" id="UAfuUsername">Username</th>
					<xsl:if test="$editUser = 'Y'">
						<th class="th" id="UAfuEditGroups">Groups</th>
					</xsl:if>
					<th class="th" id="UAfuFirstName">First Name</th>
					<th class="th" id="UAfuLastName">Last Name</th>
					<th class="th" width="100%" id="UAfuEmail">Email</th>
				</tr>
				<xsl:if test="count(user-admin/user) = 0">
					<tr>
						<th class="table-content-single-bottom" colspan="4">No users found.</th>
					</tr>
				</xsl:if>
				<xsl:apply-templates select="user-admin/user"/>
				<!-- <tr>
					<td class="table-nav" colspan="4">

						<form action="{$baseActionURL}" method="post">
							<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
							<input type="button" class="uportal-button" value="New Search" onclick="window.locationhref='{$baseActionURL}?command=search'" title="New user search"/>
							<input type="button" class="uportal-button" value="Cancel Search" onclick="window.locationhref='{$baseActionURL}'" title="Cancel user search and return to User Administration main page"/>
						</form>
					</td>
				</tr> -->
			</table>
	
		<xsl:call-template name="catalog"/> <!-- Catalog for paging and search called from global.xsl -->
		</div>
	</xsl:template>
	<xsl:template match="user">
		<tr>
			<td class="table-light-left" nowrap="nowrap" headers="UAfuUsername">
				<xsl:value-of select="@username"/>
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
			</td>
			<xsl:if test="$editUser = 'Y'">
			<td class="table-light-left" nowrap="nowrap" headers="UAfuEditGroups">
				<a href="{$baseActionURL}?servant_command=selectGroups&amp;next_command=updateGroups&amp;next_command_arg=username&amp;next_command_username={@username}" title="To edit this user's group memberships" onmouseover="swapImage('userAdminSelectGroupsImage{position()}{$instanceId}','channel_edit_active.gif')" onmouseout="swapImage('userAdminSelectGroupsImage{position()}{$instanceId}','channel_edit_base.gif')">
                Select
				<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
				<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Group Selection' icon linking to edit {first_name} {last_name}'s group memberships" title="'Group Selection' icon linking to edit {first_name} {last_name}'s group memberships" align="absmiddle" name="userAdminSelectGroupsImage{position()}{$instanceId}" id="userAdminSelectGroupsImage{position()}{$instanceId}"/>
				</a>
			</td>
            </xsl:if>
			<td class="table-content" nowrap="nowrap" headers="UAfuFirstName">
				<xsl:value-of select="first_name"/>
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
			</td>
			<td class="table-content" nowrap="nowrap" headers="UAfuLastName">
				<form action="{$baseActionURL}" method="post" name="editUser{position()}" id="editUser{position()}">
					<input type="hidden" name="command" value="edit"/>
					<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
					<input type="hidden" name="ID" value="{@id}"/>
					<input type="hidden" name="user_name" id="user_name" value="{@username}"/>
					<input type="hidden" name="first_name" id="first_name" value="{first_name}"/>
					<input type="hidden" name="last_name" id="last_name" value="{last_name}"/>
					<input type="hidden" name="roleID" id="roleID" value="{@role_id}"/>
					<input type="hidden" name="email" id="email" value="{email}"/>
				</form>
				<xsl:choose>
					<xsl:when test="$editUser = 'Y'">
						<a href="javascript:document.editUser{position()}.submit();" title="To edit this user's properties" onmouseover="swapImage('userAdminEditImage{position()}{$instanceId}','channel_edit_active.gif')" onmouseout="swapImage('userAdminEditImage{position()}{$instanceId}','channel_edit_base.gif')">
							<xsl:value-of select="last_name"/>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon linking to edit {first_name} {last_name}'s properties" title="'Edit' icon linking to edit {first_name} {last_name}'s properties" align="absmiddle" name="userAdminEditImage{position()}{$instanceId}" id="userAdminEditImage{position()}{$instanceId}"/>
						</a>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="last_name"/>
						<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit' icon indicating insufficient permisions to edit {first_name} {last_name}'s properties" title="Inactive 'Edit' icon indicating insufficient permisions to edit {first_name} {last_name}'s properties" align="absmiddle" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:choose>
					<xsl:when test="$deleteUser = 'Y'">
						<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						<!-- <a href="{$baseActionURL}?command={$deleteCommand}&amp;ID={@id}" title="Delete this user"
            onmouseover="swapImage('userAdminDeleteImage{position()}{$instanceId}','channel_delete_active.gif')" 
            onmouseout="swapImage('userAdminDeleteImage{position()}{$instanceId}','channel_delete_base.gif')"> -->
						<a href="javascript:document.editUser{position()}.command.value='delete'; document.editUser{position()}.submit();" title="To delete this user" onmouseover="swapImage('userAdminDeleteImage{position()}{$instanceId}','channel_delete_active.gif')" onmouseout="swapImage('userAdminDeleteImage{position()}{$instanceId}','channel_delete_base.gif')">
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Delete' icon linking to delete {first_name} {last_name}" title="'Delete' icon linking to delete {first_name} {last_name}" align="absmiddle" name="userAdminDeleteImage{position()}{$instanceId}" id="userAdminDeleteImage{position()}{$instanceId}"/>
						</a>
					</xsl:when>
					<xsl:otherwise>
						<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif" alt="Inactive 'Delete' icon indicating insufficient permissions to delete {first_name} {last_name}" title="Inactive 'Delete' icon indicating insufficient permissions to delete {first_name} {last_name}" align="absmiddle" />
					</xsl:otherwise>
				</xsl:choose>
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
			</td>
			<td class="table-content" nowrap="nowrap" headers="UAfuEmail">
				<xsl:value-of select="email"/>
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
