<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="user-notes">
        <xsl:call-template name="links"/>
        <!-- UniAcc: Data Table -->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <th colspan="2" class="th-top" id="ViewNote">View Note</th>
            </tr>
            <xsl:apply-templates select="note"/>
        </table>
    </xsl:template>
    <xsl:template match="note">
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap" id="Date" headers="ViewNote">Date</td>
            <td class="table-content-right" align="left" width="100%" headers="ViewNote Date">
                <xsl:value-of select="@date"/>
            </td>
        </tr>
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap" id="Title" headers="ViewNote">Title</td>
            <td class="table-content-right" align="left" width="100%" headers="ViewNote Title">
                <a href="{$baseActionURL}?command=edit&amp;ID={@id}&amp;sortby={$sortby}&amp;order={$order}" title="Edit '{note-title}' note" onmouseover="swapImage('noteEditImage{@id}','channel_edit_active.gif')" onmouseout="swapImage('noteEditImage{@id}','channel_edit_base.gif')">
                    <xsl:value-of select="note-title"/>
                    <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon: edit view for '{note-title}' note" title="'Edit' icon: edit view for '{note-title}' note" align="absmiddle" name="noteEditImage{@id}" id="noteEditImage{@id}"/>
                </a>
                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                <a href="{$baseActionURL}?command=delete&amp;ID={@id}&amp;sortby={$sortby}&amp;order={$order}" title="Delete '{note-title}' note" onmouseover="swapImage('noteDeleteImage{@id}','channel_delete_active.gif')" onmouseout="swapImage('noteDeleteImage{@id}','channel_delete_base.gif')">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Delete' icon: delete view for '{note-title}' note" title="'Delete' icon: delete view for '{note-title}' note" align="absmiddle" name="noteDeleteImage{@id}" id="noteDeleteImage{@id}"/>
                </a>
            </td>
        </tr>
        <tr>
            <td class="table-light-left-bottom" align="right" nowrap="nowrap" id="Note" headers="ViewNote">Note</td>
            <td class="table-content-right-bottom" align="left" width="100%" headers="ViewNote Note">
                <xsl:copy-of select="note-body"/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
