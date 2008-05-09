<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

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
		<xsl:call-template name="autoFormJS"/>

		<form action="{$baseActionURL}?command=submit&amp;ID={note/@id}" method="post" onsubmit="return validator.applyFormRules(this, new NotesRulesObject())">
			<!--UniAcc: Layout Table -->
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<th colspan="2" class="th-top">Edit Note</th>
				</tr>
				<xsl:apply-templates select="note"/>
			</table>
		</form>
	</xsl:template>

	<xsl:template match="note">
		<tr>
			<td class="table-light-left" align="right" nowrap="nowrap">Date</td>
			<td class="table-content-right" align="left" width="100%">
				<xsl:value-of select="@date"/>
			</td>
		</tr>

		<tr>
			<td class="table-light-left" align="right" nowrap="nowrap">
				<label for="note-EditNoteTA1">Note</label>
			</td>
			<td class="table-content-right" align="left" width="100%">
				<textarea name="message" cols="40" id="note-EditNoteTA1">
					<xsl:value-of select="note-body"/>
				</textarea>
			</td>
		</tr>

		<tr>
			<td colspan="2" class="table-nav">
				<input name="submit" value="Submit" type="submit" class="uportal-button" title="Submit changes to this note"/>
				<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=general'" title="Cancel changes to this note"/>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
