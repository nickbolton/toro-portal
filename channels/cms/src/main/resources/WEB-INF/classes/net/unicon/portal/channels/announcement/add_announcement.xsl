<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
    <xsl:template match="/">
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/> 
            <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
            <parameter name="insert"><xsl:value-of select="$insert" /></parameter>   
            <parameter name="submit"><xsl:value-of select="$submit" /></parameter>   
            <parameter name="delete"><xsl:value-of select="$delete" /></parameter>   
        </textarea> -->
        <xsl:call-template name="autoFormJS" />
        <xsl:call-template name="links"/>

        <form action="{$baseActionURL}?command=insert" method="post" onsubmit="return validator.applyFormRules(this, announceRules)">
		<input type="hidden" name="targetChannel" value="{$targetChannel}" />
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top">Add Announcement</th>
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
                    <td class="table-light-left" nowrap="nowrap"><label for="ancan1">Announcement</label></td>
                    <td class="table-content-right" width="100%">
                        <textarea name="message" cols="40" id="ancan1" />
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" align="center">
                    <input name="submit" value="Submit" type="submit" class="uportal-button" title="Click to add announcement and return to viewing all Announcements" />
                    <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel and return to viewing all Announcements" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
    
</xsl:stylesheet>
