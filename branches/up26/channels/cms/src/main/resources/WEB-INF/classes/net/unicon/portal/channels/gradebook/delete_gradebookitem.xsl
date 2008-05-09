<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>
        </textarea> -->
        <xsl:call-template name="links"/>

        <form name="gradebookForm" action="{$baseActionURL}" method="post">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="position" value="{/gradebooks/gradebook-item[@id = $gradebookItemID]/@position}"/>
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top-single">
                        Delete Column Item
                    </th>
                </tr>
                <tr>
                    <td class="table-content-single" style="text-align:center;">
                        <span class="uportal-channel-warning">Are you sure you want to delete the item '<span class="uportal-channel-strong">
                                <xsl:value-of select="/gradebooks/gradebook-item[@id = $gradebookItemID]/title"/>
                            </span>' from this offering?</span>
                    </td>
                </tr>
                <tr>
                    <td class="table-content-single" style="text-align:center;">
                        <input type="radio" class="radio" name="commandButton" value="confirm" onclick="document.gradebookForm.command.value = 'confirm'" id="gbdgbir1"/>
                        <label for="gbdgbir1">&#160;Yes</label>
                        <img height="1" width="15" src="{$SPACER}" alt="" title="" border="0"/>
                        <input type="radio" class="radio" name="commandButton" value="no" onclick="document.gradebookForm.command.value = 'no'" id="gbdgbir2" checked="checked"/>
                        <label for="gbdgbir2">&#160;No<br/></label>
                    </td>
                </tr>
                <tr>
                    <td class="table-content-single-bottom" style="text-align:center">
                        <input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to the main view of the gradebook"/>
                        <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the gradebook without deleting this item"/>
                    </td>
                </tr>
                <xsl:call-template name="sublinks"/>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
