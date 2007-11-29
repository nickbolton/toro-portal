<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="user-notes">
        <xsl:call-template name="links"/>
        <xsl:call-template name="autoFormJS"/>
        <!--<form action="{$baseActionURL}?command=insert" method="post" onsubmit="return(checkFormNC(this));"> -->
        <form action="{$baseActionURL}?command=insert&amp;sortby={$sortby}&amp;order={$order}" method="post" onsubmit="return validator.applyFormRules(this, new NotePadRulesObject())">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
            <!-- UniAcc: Data Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top" id="AddNote">Add Note</th>
                </tr>
                <!-- Need to fix to get current date from database to be consistent
                <tr>
                    <td class="table-light-left" nowrap="nowrap">Date</td>
                    <td class="table-content-right" align="left" width="100%">
                        <xsl:value-of select="@date" />
                    </td>
                </tr>
                -->
                <tr>
                    <td class="table-light-left" nowrap="nowrap" id="Title" headers="AddNote">
                        <label for="npcat1">Title</label>
                    </td>
                    <td class="table-content-right" width="100%" headers="AddNote Title">
                        <input type="text" class="text" name="title" id="npcat1"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" nowrap="nowrap" id="Note" headers="AddNote">
                        <label for="npcta1">Note</label>
                    </td>
                    <td class="table-content-right" width="100%" headers="AddNote Note">
                        <textarea name="message" class="text" cols="40" id="npcta1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" align="center">
                        <input name="submit" value="Submit" type="submit" class="uportal-button" title="Add this note"/>
                        <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?sortby={$sortby}&amp;order={$order}'" title="Cancel adding this note"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
