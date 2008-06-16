<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Import -->
<xsl:import href="../global/global.xsl"/>

<!-- parameters -->
<xsl:param name="current_command"/>
<xsl:param name="editOfferingPermissionsCommand"/>
<xsl:param name="editUserPermissionsCommand"/>
<xsl:param name="enrollCommand"/>
<xsl:param name="enrollViewCommand"/>
<xsl:param name="importCommand"/>
<xsl:param name="executeImportCommand"/>
<xsl:param name="offeringId"/>
<xsl:param name="resolveCommand">resolve</xsl:param>
<xsl:param name="roleId"/>
<xsl:param name="roleName"/>
<xsl:param name="searchCommand"/>
<xsl:param name="type"/><!-- May not be needed -->
<xsl:param name="unenrollCommand"/>
<xsl:param name="updateOfferingPermissionsCommand"/>
<xsl:param name="updateUserPermissionsCommand"/>
<xsl:param name="userId"/>
<xsl:param name="userIdParam"/>
<xsl:param name="viewMemberCommand"/>
<xsl:param name="confirmUnenrollCommand"/>
<xsl:param name="searchUserCommand"/>
<xsl:param name="displayCommand"/>
<xsl:param name="catChannel">roster</xsl:param> <!-- Catalog: for form id uniqueness and identifying which search inputs to insert - See Catalog templates in global.xsl -->
<!-- permissions -->
<xsl:param name="editOfferingPermissions"/>
<xsl:param name="editUserPermissions"/>
<xsl:param name="enrollUser"/>
<xsl:param name="unenrollUser"/>
<xsl:param name="viewUserInfo"/>
<xsl:param name="importRoster"/>
<!-- showViewAll:  Flag to make the Users/Members hyperlink available or not. -->
<xsl:param name="showViewAll"/>
<!-- emptyListMessage:  Text todisplay when the list is empty. -->
<xsl:param name="emptyListMessage">No users are currently enrolled in this offering.</xsl:param>
<!-- enrollment Model for Offering -->
<xsl:param name="enrollmentModel"></xsl:param><!-- needed to be able to filter commands in SIS offerings -->

    <xsl:template name="autoFormJS">
        <script language="JavaScript" type="text/javascript" src="javascript/RosterChannel/autoForm.js"></script>
    </xsl:template>
    <!-- Common -->
    <xsl:template name="links">
        <!-- Attempted fix for current_command not matching view (i.e. enroll or unenroll happens on both Main and Add) -->
        <!-- Count number of Unenrolled types in XML.  If greater than 0 then adding. Should have Java fix -->
        <xsl:variable name="numberUnenrolledMember" select="count(/roster/user/status[text() = 'Unenrolled'])"/>
        <form method="post" name="rosterPermForm" action="{$baseActionURL}">
            <input type="hidden" name="targetChannel" value="{$targetChannel}" />
            <input type="hidden" name="catPageSize" value="{$catPageSize}" />
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <td class="views-title">
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" 
                        alt="Icon of tool-tip indicating the channel options section" 
                        title="Icon of tool-tip indicating channel options section" 
                        align="absmiddle"/>
                    </td>
                    <td class="views" valign="middle" height="26" width="100%">
                        <xsl:choose>
                            <xsl:when test="$showViewAll = 'false'">
                                Users / Members
                                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" 
                                alt="Selected 'View' icon indicating all members of this offering are currently displayed" 
                                title="Selected 'View' icon indicating all members of this offering are currently displayed" 
                                align="absmiddle"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <a href="{$baseActionURL}?command={$pageCommand}&amp;catPageSize={$catPageSize}" 
                                title="To view all members in this offering" 
                                onmouseover="swapImage('rosterViewImage','channel_view_active.gif')"
                                onmouseout="swapImage('rosterViewImage','channel_view_base.gif')">
                                    Users / Members
                                    <img height="1" width="3" src="{$SPACER}" alt="" title="" 
                                    border="0"/><img border="0" 
                                    src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" 
                                    alt="'View' icon: display of all members in this offering" 
                                    title="'View' icon: display of all members in this offering" 
                                    align="absmiddle" name="rosterViewImage" id="rosterViewImage"/>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>
                        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                        <xsl:choose>
                            <xsl:when test="$enrollUser != 'Y' or $enrollmentModel = 'sis'">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif" 
                                alt="Inactive 'Add' icon indicating insufficient permissions to add member to this offering" 
                                title="Inactive 'Add' icon indication insufficient permissions to add member to this offering" 
                                align="absmiddle"/>
                            </xsl:when>
                            <!--<xsl:when test="($current_command = $searchCommand) or ($current_command = $enrollViewCommand) or ($numberUnenrolledMember &gt; 0)"> -->
                            <xsl:when test="($current_command = $searchCommand)">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" 
                                alt="Currently looking to add a member to this offering" 
                                title="Currently looking to add a member to this offering" 
                                align="absmiddle"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <a href="{$baseActionURL}?command={$searchCommand}&amp;catPageSize={$catPageSize}" 
                                title="To add a member to this offering" 
                                onmouseover="swapImage('rosterEnrollUsersImage','channel_add_active.gif')" 
                                onmouseout="swapImage('rosterEnrollUsersImage','channel_add_base.gif')">
                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" 
                                    alt="'Add' icon: add a member to this offering" 
                                    title="'Add' icon: add a member to this offering" 
                                    align="absmiddle" name="rosterEnrollUsersImage" id="rosterEnrollUsersImage"/>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>
                        |  Types <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                        <xsl:choose>
                            <xsl:when test="$editOfferingPermissions != 'Y'">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" 
                                alt="Inactive 'Edit' icon indicating insufficient permissions to edit roles in this offering" 
                                title="Inactive 'Edit' icon indication insufficient permissions to edit roles in this offering" 
                                align="absmiddle"/>
                            </xsl:when>
                            <xsl:when test="$current_command = $editOfferingPermissionsCommand">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif" 
                                alt="Selected 'Edit' icon: Currently editing a role's permissions for this offering" 
                                title="Selected 'Edit' icon: Currently editing a role's permissions for this offering" 
                                align="absmiddle"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <input type="hidden" name="command" value="{$editOfferingPermissionsCommand}"/>
                                <input type="hidden" name="ID" value="{$offeringId}"/>
                                <select name="roleId">
                                    <xsl:for-each select="/roster/role">
                                        <option value="{@id}">
                                            <xsl:value-of select="."/>
                                        </option>
                                    </xsl:for-each>
                                </select>
                                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                                <a href="javascript:document.rosterPermForm.submit()" 
                                title="To edit the selected role's permissions" 
                                onmouseover="swapImage('rosterEditRoleImage','channel_edit_active.gif')" 
                                onmouseout="swapImage('rosterEditRoleImage','channel_edit_base.gif')">
                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" 
                                    alt="'Edit' icon: Edit selected role's permissions" 
                                    title="'Edit' icon: Edit selected role's permissions" 
                                    align="absmiddle" name="rosterEditRoleImage" id="rosterEditRoleImage"/>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>
                        | <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                        <xsl:choose>
                            <xsl:when test="$importRoster != 'Y' or $enrollmentModel = 'sis'">
                                Import<img height="1" width="3" src="{$SPACER}" alt="" title="" 
                                border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_import_inactive.gif" 
                                alt="Inactive 'Import' icon indicating insufficient permissions to import roster" 
                                title="Inactive 'Import' icon indicating insufficient permissions to import roster" 
                                align="absmiddle"/>
                            </xsl:when>
                            <xsl:when test="$current_command = $importCommand">
                                Import<img height="1" width="3" src="{$SPACER}" alt="" title="" 
                                border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_import_selected.gif" 
                                alt="Selected 'Import' icon: Currently importing a roster" 
                                title="Selected 'Import' icon: Currently importing a roster" 
                                align="absmiddle"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <a href="{$baseActionURL}?command={$importCommand}&amp;catPageSize={$catPageSize}" 
                                title="Import roster to this offering" 
                                onmouseover="swapImage('rosterImportImage','channel_import_active.gif')" 
                                onmouseout="swapImage('rosterImportImage','channel_import_base.gif')">
                                    Import<img height="1" width="3" src="{$SPACER}" alt="" title="" 
                                    border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_import_base.gif" 
                                    alt="'Import' icon: Import roster to this offering" 
                                    title="'Import' icon: Import roster to this offering" 
                                    align="absmiddle" name="rosterImportImage" id="rosterImportImage"/>
                                </a>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- NOTIFICATION LINK - Freddy added  -->
                        | <a href="{$baseActionURL}?command=sendNotification&amp;ID={$offeringId}&amp;catPageSize={$catPageSize}"
                        title="To send a notification to a user of this offering" 
                        onmouseover="swapImage('rosterNoteImage','channel_email_active.gif')" 
                        onmouseout="swapImage('rosterNoteImage','channel_email_base.gif')">
                            Send Notification<img height="1" width="3" 
                            src="{$SPACER}" alt="" title="" 
                            border="0"/><img border="0" 
                            src="{$CONTROLS_IMAGE_PATH}/channel_email_base.gif" 
                            alt="'Send Notification' icon: Send a notification to a user of this offering" 
                            title="'Send Notification' icon: Send a notification to a user of this offering" 
                            align="absmiddle" name="rosterNoteImage" id="rosterNoteImage"/>
                        </a>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>

</xsl:stylesheet>
