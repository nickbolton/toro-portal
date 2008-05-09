<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
    <xsl:template match="/">

        <xsl:call-template name="autoFormJS"/>
        <xsl:call-template name="links"/>

        <form action="{$baseActionURL}?command=submit&amp;ID={./class-announcements/announcement/@id}" method="post" onsubmit="return validator.applyFormRules(this, announceRules)">
            <input type="hidden" name="targetChannel" value="{$targetChannel}" />
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top">Edit Announcement</th>
                </tr>
                <xsl:apply-templates select="./class-announcements/announcement"/>
            </table>
        </form>
    </xsl:template>

    <xsl:template match="announcement">
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap">Date</td>
            <td class="table-content-right" align="left" width="100%">
                <xsl:value-of select="@date" />
            </td>
        </tr>
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap"><label for="ancean1">Announcement</label></td>
            <td class="table-content-right" align="left" width="100%">
                <textarea name="message" cols="40" id="ancean1">
                	<xsl:value-of select="announcement-body" />
                </textarea>
            </td>
        </tr>
        <tr>
            <td colspan="2" class="table-nav">
            <input name="submit" value="Submit" type="submit" class="uportal-button" title="Click to submit announcement edits and return to viewing all Announcements"/>
            <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel editing this announcement and return to viewing all announcements"/>
            </td>
       </tr>
       <!-- Leave out dependent navigation for now
        <tr>
            <td colspan="2">
        
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <td class="views-title" nowrap="nowrap">Other Options:</td>
                    <td class="views-dependent" valign="middle" height="26" width="100%">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif" alt="Currently editing this announcement" align="absmiddle" />
                    <img height="1" width="3" src="{$SPACER}"
        alt="" border="0" /><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif" alt="Not permitted to delete this announcement" align="absmiddle" />
        </td>
                </tr>
            </table>
        
            </td>
        </tr>
        
        <tr>
            <td colspan="2" class="uportal-background-dark">
        <img height="3" width="1" src="{$SPACER}"
        alt="" border="0" /></td>
        
        </tr>
         -->
  </xsl:template>
  
</xsl:stylesheet>
