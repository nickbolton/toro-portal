<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>

	<!-- Include Files -->
	<xsl:include href="common.xsl"/>

	<!--<xsl:template match="/">
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>     
        <parameter name="ID"><xsl:value-of select="$ID" /></parameter>     
        <parameter name="addCommand"><xsl:value-of select="$addCommand" /></parameter>     
        <parameter name="editCommand"><xsl:value-of select="$editCommand" /></parameter>     
        <parameter name="deleteCommand"><xsl:value-of select="$deleteCommand" /></parameter>     
        <parameter name="deleteConfirmationParam"><xsl:value-of select="$deleteConfirmationParam" /></parameter>     
    </textarea>

    <xsl:apply-templates />

</xsl:template>
 -->
	<xsl:template match="user-notes">

		<xsl:call-template name="links"/>

		<form name="notesChannelForm" action="{$baseActionURL}" method="post">
			<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
			<input type="hidden" name="ID" value="{$ID}"></input>
			<!--UniAcc: Layout Table -->
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<td class="table-content-single-top">
						<span class="uportal-channel-warning">Are you sure you want to delete this note?</span>
						<br/>
						<img height="10" width="1" src="{$SPACER}" alt="" title=""/>
						<br/>

						<span class="uportal-channel-copyright">"<xsl:value-of select="note[@id = $ID]/note-body"/>"</span>
						<br/>
						<img height="10" width="1" src="{$SPACER}" alt="" title=""/>
						<br/>
					</td>
				</tr>
				<tr>
					<td class="table-content-single" style="text-align:center;">
						<input type="radio" class="radio" name="command" value="deleteConfirm" id="afdr1"/>
						<label for="afdr1">&#160;Yes</label>
						<img height="1" width="15" src="{$SPACER}" alt="" title=""/>
						<input type="radio" class="radio" name="command" value="no" checked="checked" id="afdr2"/>
						<label for="afdr2">&#160;No</label>
						<br/>
					</td>
				</tr>
				<tr>
					<td class="table-content-single-bottom" style="text-align:center">
						<input type="submit" class="uportal-button" value="Submit" title="Confirm deletion of this note"/>&#x20;&#x20;&#x20;&#x20;
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=general'" title="Cancel deletion of this note"/>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
</xsl:stylesheet>
