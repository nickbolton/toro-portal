<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="../global/global.xsl"/>

<!-- Parameters -->

<xsl:param name="workerActionURL"/>
<xsl:param name="current_command"/>
<xsl:param name="upload_status" select="'SUCCESS'"/>
<xsl:param name="max_file_size" select="0"/>
<xsl:param name="offeringName"/>
<xsl:param name="resourceID"/>
<xsl:param name="nodeid"/>
<xsl:param name="nodedesc"/>
<xsl:param name="nodeurl"/>
<xsl:param name="nodename"/>
<xsl:param name="nodetype"/>


<!-- Permissions -->
<xsl:param name="addResource"/>
<xsl:param name="deleteResource"/>
<xsl:param name="editResource"/>


<xsl:template name="autoFormJS">
<script language="JavaScript" type="text/javascript" src="javascript/ResourceChannel/autoForm.js"/>
</xsl:template>

<!-- Common -->
<xsl:template name="links">
    <!-- UniAcc: Layout Table -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <td class="views-title">
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" align="absmiddle" alt="Icon of tool-tip indicating the channel options section" title="Icon of tool-tip indicating channel options section"/></td>
            <td class="views" valign="middle" height="26" width="100%">
                <xsl:choose>
                    <xsl:when test="$current_command = 'add'">
                        <a href="{$baseActionURL}" title="View class folders" onmouseover="swapImage('resourceViewImage{generate-id()}','channel_view_active.gif');" onmouseout="swapImage('resourceViewImage{generate-id()}','channel_view_base.gif');">
                            Related Resources
                            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to display of class folders" title="'View' icon linking to display of class folders" align="absmiddle" name="resourceViewImage{generate-id()}" id="resourceViewImage{generate-id()}"/>
                        </a>
                        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" alt="Selected 'Add' icon indicating that Add Element is currently in view" title="Selected 'Add' icon indicating that Add Element is currently in view" align="absmiddle"/>                                            
                    </xsl:when>
                    <xsl:when test="$current_command = 'main'">
                        Related Resources
                        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" align="absmiddle" alt="Selected 'View' icon indicating Class Folders View is currently displayed" title="Selected 'View' icon indicating Class Folders View is currently displayed"/>
                        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                        <xsl:choose>
                             <xsl:when test="$addResource='Y'">
                                <a href="{$baseActionURL}?command=add"  title="Add an Element at the root level" onmouseover="swapImage('resourceAddElement{generate-id()}','channel_add_active.gif');" onmouseout="swapImage('resourceAddElement{generate-id()}','channel_add_base.gif');">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' icon linking to the Add Element view" title="'Add' icon linking to the Add Element view" align="absmiddle" name="resourceAddElement{generate-id()}" id="resourceAddElement{generate-id()}"/>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif"
                    alt="Inactive 'Add' icon indicating the permission to add an announcement is currently unavailable" title="Inactive 'Add' icon indicating the permission to add an announcement is currently unavailable" align="absmiddle"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="{$baseActionURL}" title="View class folders" onmouseover="swapImage('resourceViewImage{generate-id()}','channel_view_active.gif');" onmouseout="swapImage('resourceViewImage{generate-id()}','channel_view_base.gif');">
                            Related Resources
                            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to display of class folders" title="'View' icon linking to display of class folders" align="absmiddle" name="resourceViewImage{generate-id()}" id="resourceViewImage{generate-id()}"/>
                        </a>
                        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                        <xsl:choose>
                             <xsl:when test="$addResource='Y'">
                                <a href="{$baseActionURL}?command=add"  title="Add an Element at the root level" onmouseover="swapImage('resourceAddElement{generate-id()}','channel_add_active.gif');" onmouseout="swapImage('resourceAddElement{generate-id()}','channel_add_base.gif');">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' linking to the Add Element view" title="'Add' icon linking to the Add Element view" align="absmiddle" name="resourceAddElement{generate-id()}" id="resourceAddElement{generate-id()}"/>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" alt="Selected 'Add' icon indicating that Add Element is currently in view" title="Selected 'Add' icon indicating that Add Element is currently in view" align="absmiddle"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </table>

</xsl:template>
</xsl:stylesheet>
