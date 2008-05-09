<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="yes" />
<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">
    <!-- <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>     
    </textarea> -->
    <!--<xsl:call-template name = "commonJS" /> -->
<xsl:call-template name="links"/>
<form name="announcementForm" action="{$baseActionURL}" method="post">
<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
<input type="hidden" name="ID" value="{$ID}"></input>
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<tr>
    <th class="th-top-single">
        Delete Announcement
    </th>
</tr>
<tr>
    <td class="table-content-single-top" style="text-align:center">
        <span class="uportal-channel-warning">Are you sure you want to delete this announcement?</span>
        <br />
        <img height="10" width="1" src="{$SPACER}" alt="" title=""/>
        <br />
		<span class="text">
			<strong>Message:</strong>
			   "<xsl:call-template name='smarttext'>                    <xsl:with-param name="body" select="/class-announcements/announcement[@id = $ID]/announcement-body" />                </xsl:call-template>"
		</span>
        <br />
        <img height="10" width="1" src="{$SPACER}" alt="" title=""/>
        <br />
    </td>
</tr>
<tr>
    <td class="table-content-single" style="text-align:center;">
        <input type="radio" class="radio" name="command" value="delete" id="afdr1"/>
        <label for="afdr1">&#160;Yes</label>
        <img height="1" width="15" src="{$SPACER}" alt="" title=""/>
        <input type="radio" class="radio" name="command" value="no" checked="checked" id="afdr2"/>
        <label for="afdr2">&#160;No</label>
        <br />
    </td>
</tr>
<tr>
    <td class="table-content-single-bottom" style="text-align:center">
       <input type="submit" class="uportal-button" value="Submit" title="To submit your response and return to the main view of the annoucements"/>
       <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the announcements without deleting this announcement"/>
    </td>
</tr>

</table>
</form>

</xsl:template>

</xsl:stylesheet>




