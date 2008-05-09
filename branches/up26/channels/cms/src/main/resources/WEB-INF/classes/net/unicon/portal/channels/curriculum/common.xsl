<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- Include -->
<xsl:import href="../global/global.xsl"/>

<xsl:output method="html" indent="yes" />

<!-- Parameters -->
<xsl:param name="workerActionURL"/>
<xsl:param name="current_command"/>
<xsl:param name="offeringName"/>
<xsl:param name="curriculumID"/>
<xsl:param name="onlineCurriculumAvailable">true</xsl:param>
<xsl:param name="title"/> <!-- Catalog: sticky search data of curriclum title input -->
<xsl:param name="description"/> <!-- Catalog: sticky search data of curriclum description input -->
<xsl:param name="catChannel">curr</xsl:param> <!-- Catalog: for form id uniqueness and identifying which search inputs to insert - See Catalog templates in global.xsl -->
<xsl:param name="catCurrentCommand">search</xsl:param> <!-- Catalog: identifies search command -->

<!-- Permissions -->
<xsl:param name="addCurriculum"/>
<xsl:param name="removeCurriculum"/>
<xsl:param name="editPermissions"/>
<xsl:param name="viewInstructorNotes"/>

<xsl:template name="autoFormJS">
<script language="JavaScript" type="text/javascript" src="javascript/CurriculumChannel/autoForm.js"></script>
</xsl:template>

<!-- Common -->
<xsl:template name="links">	
	<!-- UniAcc: Layout Table -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="views-title">
        	<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" alt="Icon of tool-tip indicating the channel options section" title="Icon of tool-tip indicating channel options section" align="absmiddle"/>
		</td>
        <td class="views" valign="middle" height="26" width="100%">
        <xsl:choose>
        <xsl:when test="$current_command = 'main'">
            Current Curriculum
			<img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
			<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="Selected 'View' icon indicating that all curriculum in this offering are currently displayed" title="Selected 'View' icon indicating that all curriculum in this offering are currently displayed" align="absmiddle" />
        </xsl:when>
        <xsl:otherwise>
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;servant_command=cancel" title="View all curriculum associated with this offering" onmouseover="swapImage('curriculumViewImage','channel_view_active.gif')"  onmouseout="swapImage('curriculumViewImage','channel_view_base.gif')">
				Current Curriculum
				<img border="0" height="1" width="3" src= "{$SPACER}" alt="" title="" />
				<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon: display of all curriculum in this offering" title="'View' icon: display of all curriculum in this offering" align="absmiddle" name="curriculumViewImage" id="curriculumViewImage"/>
			</a>
        </xsl:otherwise>
        </xsl:choose>
        
        <xsl:choose>
        <xsl:when test="$current_command = 'add'">
            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
			<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" alt="Selected 'Add' icon indicating that the add option is currently in view" title="Selected 'Add' icon indicating that the add option is currently in view" align="absmiddle" />
        </xsl:when>
        <xsl:otherwise>
            <xsl:choose>
            <xsl:when test="$addCurriculum = 'Y'">
                <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
				<a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=add&amp;servant_command=cancel" title="Add curriculum to the offering" onmouseover="swapImage('curriculumAddImage','channel_add_active.gif')" onmouseout="swapImage('curriculumAddImage','channel_add_base.gif')">
					<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' icon: Add curriculum to this offering" title="'Add' icon: Add curriculum to this offering" align="absmiddle" name="curriculumAddImage" id="curriculumAddImage"/>
			</a>
            </xsl:when>
            <xsl:otherwise>
                <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
				<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif" alt="" align="absmiddle" title="Inactive 'Add' icon due to permission status"/>
            </xsl:otherwise>
            </xsl:choose>
        </xsl:otherwise>
        </xsl:choose>

        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>

        <xsl:choose>
            <xsl:when test="$editPermissions = 'Y'">
                |
                <a href="{$baseActionURL}?command=editPermissions" title="To Edit Channel Permissions" onmouseover="swapImage('curriculumEditPermissionsImage','channel_edit_active.gif')" onmouseout="swapImage('curriculumEditPermissionsImage','channel_edit_base.gif')">
                    Permissions
                    <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit Permissions' icon linking to edit channel permissions" title="'Edit Permissions' icon linking to edit channel permissions" align="absmiddle" name="curriculumEditPermissionsImage" id="curriculumEditPermissionsImage"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                |  Permissions
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit Permissions' icon indicating insufficient permissions to edit channel permissions" title="Inactive 'Permissions Edit' icon indicating insufficient permissions to edit channel permissions" align="absmiddle"/>
            </xsl:otherwise>
        </xsl:choose>
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>

        
        </td>
    </tr>
    </table>

</xsl:template>

</xsl:stylesheet>
