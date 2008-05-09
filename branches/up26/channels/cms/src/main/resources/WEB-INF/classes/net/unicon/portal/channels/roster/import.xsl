<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="roster">
        <xsl:call-template name="links"/>
        <xsl:call-template name="autoFormJS"/>
        <form action="{$baseActionURL}?command={$executeImportCommand}" enctype='multipart/form-data' method="post" onsubmit="return validator.applyFormRules(this, new RosterRulesObject())">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
            <input type="hidden" name="catPageSize" value="{$catPageSize}" />
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top-single" colspan="2" align="center">
                        Import Roster
                    </th>
                </tr>
                <tr>
                    <td class="table-light-left">
                        <label for="rcif1">File:</label>
                    </td>
                    <td class="table-content-right">
                        <!-- Inline style to fix bug with Netscape & Input type= "file" - apparently due to text align other than "left" -->
                        <input name="import-file" style="text-align: left;" type="file" size="20" maxlength="100" id="rcif1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" align="center">
                        <input name="submit" value="Submit" type="submit" class="uportal-button" />
                        &#032;&#032;&#032;&#032;
                        <input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref='{$baseActionURL}?command=page&amp;catPageSize={$catPageSize}'" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>


