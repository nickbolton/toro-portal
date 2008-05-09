<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="user-notes">
        <!-- <textarea  rows="4" cols="40">
            <xsl:copy-of select="*" />
        </textarea> -->
        <xsl:call-template name="links"/>
        <xsl:apply-templates select="note"/>
    </xsl:template>
        
    <xsl:template match="note">
        <form name="notepadChannelForm" action="{$baseActionURL}?ID={@id}&amp;sortby={$sortby}&amp;order={$order}" method="post">
        <input type="hidden" name="command" value=""/>
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top-single">
                        Delete Note
                    </th>
                </tr>
                <tr>
                    <td class="table-content-single-top" style="text-align:center;">
                        <span class="uportal-channel-warning">Are you sure you want to delete this note?</span>
                        <br/>
                        <img height="10" width="1" src="{$SPACER}" alt="" title=""/>
                        <br/>
                        <span class="text">
                            <strong>Title:</strong>
                                "<xsl:value-of select="note-title"/>"
                            <br/>
                            <strong>Note:</strong>
                            "<xsl:value-of select="note-body"/>"
                        </span>
                        <br/>
                        <img height="10" width="1" src="{$SPACER}" alt="" title=""/>
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td class="table-content-single" style="text-align:center;">
                        <input type="radio" class="radio" name="commandButton" value="confirm" onclick="document.notepadChannelForm.command.value = 'confirm'" id="npcdr1"/>
                        <label for="npcdr1">&#160;Yes</label>
                        <img height="1" width="15" src="{$SPACER}" alt="" title=""/>
                        <input type="radio" class="radio" name="commandButton" value="no" checked="true" onclick="document.notepadChannelForm.command.value = 'no'" id="npcdr2"/>
                        <label for="npcdr2">&#160;No</label>
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td class="table-content-single-bottom" style="text-align:center">
                        <input type="submit" class="uportal-button" value="Submit" title="Confirm deletion of this note"/>
                           <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?sortby={$sortby}&amp;order={$order}'" title="Cancel deletion of this note"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
