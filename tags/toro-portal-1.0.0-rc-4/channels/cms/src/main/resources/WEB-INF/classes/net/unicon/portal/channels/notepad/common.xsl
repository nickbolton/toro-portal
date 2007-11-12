<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include -->
    <xsl:include href="../global/global.xsl"/>
    <!-- parameters -->

    <xsl:param name="current_command"/>

    <xsl:param name="ID"/>
    <xsl:param name="addCommand"/>
    <xsl:param name="editCommand"/>
    <xsl:param name="deleteCommand"/>
    <xsl:param name="deleteConfirmationParam"/>
    <xsl:param name="sortby"/>
    <xsl:param name="order"/>
    <!-- permissions -->
    <xsl:template name="autoFormJS">
        <script language="JavaScript" type="text/javascript" src="javascript/NotepadChannel/autoForm.js"></script>
    </xsl:template>
    <!-- Common -->
    <xsl:template name="links">
        <!-- UniAcc: Layout Table -->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="views-title">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" align="absmiddle" alt="Icon of tool-tip indicating the channel options section" title="Icon of tool-tip indicating channel options section"/>
                </td>
                <td class="views" valign="middle" height="26" width="100%">
                    <xsl:choose>
                        <xsl:when test="$current_command = 'main'">
                            Note
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="Selected 'View' icon indicating that all notes in this offering are currently displayed" title="Inactive 'View' icon indicating that all notes in this offering are currently displayed" align="absmiddle"/>
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{$baseActionURL}?sortby={$sortby}&amp;order={$order}" title="To view notes" onmouseover="swapImage('noteViewImage','channel_view_active.gif')" onmouseout="swapImage('noteViewImage','channel_view_base.gif')">
                                Note
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to display of all notes in this offering" title="'View' icon linking to display of all notes in this offering" align="absmiddle" name="noteViewImage" id="noteViewImage"/>
                            </a>
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="$current_command = 'add'">
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" alt="Inactive 'Add' icon indicating that the add option is currently in view" title="Inactive 'Add' icon indicating that the add option is currently in view" align="absmiddle"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{$baseActionURL}?command=add&amp;sortby={$sortby}&amp;order={$order}" title="To add an note" onmouseover="swapImage('noteAddImage','channel_add_active.gif')" onmouseout="swapImage('noteAddImage','channel_add_base.gif')">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' icon linking to the 'Add Note' view" title="'Add' icon linking to the 'Add Note' view" align="absmiddle" name="noteAddImage" id="noteAddImage"/>
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
