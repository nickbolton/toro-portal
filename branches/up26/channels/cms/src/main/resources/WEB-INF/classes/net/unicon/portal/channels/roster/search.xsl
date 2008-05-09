<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>
    <xsl:output method="html" indent="yes"/>
    <xsl:param name="baseActionURL"/>
    <!-- Activities -->
    <xsl:template match="roster">
        <xsl:call-template name="links"/>
        <form method="post" action="{$baseActionURL}">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="catPageSize" value="{$catPageSize}" />
            <input type="hidden" name="command" value="{$enrollViewCommand}"/>
            <!-- UniAcc: Layout Table -->
            <table cellpadding="2" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top" scope="col">User Search</th>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right">
                        <label for="rcst1">First Name</label>
                    </td>
                    <td class="table-content-right" style="text-align:left">
                        <input name="firstName" type="text" class="text" size="10" value="" id="rcst1"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right">
                        <label for="rcst2">Last Name</label>
                    </td>
                    <td class="table-content-right" style="text-align:left">
                        <input name="lastName" type="text" class="text" size="10" value="" id="rcst2"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right">
                        <label for="rcst3">User ID</label>
                    </td>
                    <td class="table-content-right" style="text-align:left">
                        <input name="userID" type="text" class="text" size="10" value="" id="rcst3"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" align="center" class="table-option-single-bottom">
                        <input name="submit" value="Submit" type="submit" class="uportal-button" title="To search for members based on the preceding criteria"/>
                        &#032;&#032;&#032;&#032;
                        <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=page&amp;catPageSize={$catPageSize}'" title="To cancel adding a new member and to the main view of the roster"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>

</xsl:stylesheet>
