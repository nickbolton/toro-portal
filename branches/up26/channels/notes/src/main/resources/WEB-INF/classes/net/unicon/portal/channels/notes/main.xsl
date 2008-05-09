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
		<!--UniAcc: Layout Table -->
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<th class="th-left" scope="col">Date</th>
				<th class="th-right" scope="col">Note</th>
			</tr>
			<xsl:apply-templates select="note"/>			
		</table>
		<xsl:if test="count(note) = 0">
			<div class="bounding-box3">
				You Currenly have no notes.
			</div>
			<br/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="note">
		<xsl:variable name="bottomStyle">
			<xsl:if test="position() = last()">-bottom</xsl:if>
		</xsl:variable>
		<tr>
			<td class="table-light-left{$bottomStyle}" align="left" valign="top" nowrap="nowrap">
				<xsl:value-of select="@date"/>
			</td>

			<td class="table-content-right{$bottomStyle}">
				<a href="{$baseActionURL}?command=edit&amp;ID={@id}" title="Edit this note" onmouseover="swapImage('noteEditImage{@id}','channel_edit_active.gif')" onmouseout="swapImage('noteEditImage{@id}','channel_edit_base.gif')">
                    Note
                    <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"  align="absmiddle" name="noteEditImage{@id}" id="noteEditImage{@id}" alt="Edit this note" title="Edit this note"/>
                </a>
				<img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
				<a href="{$baseActionURL}?command=delete&amp;ID={@id}" title="Delete this note" onmouseover="swapImage('noteDeleteImage{@id}','channel_delete_active.gif')" onmouseout="swapImage('noteDeleteImage{@id}','channel_delete_base.gif')">
					<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"  align="absmiddle" name="noteDeleteImage{@id}" id="noteDeleteImage{@id}" alt="Delete this note" title="Delete this note"/>
				</a>
				<img height="5" width="1" src="{$SPACER}" border="0" alt="" title=""/>
				<br/>
				<xsl:value-of select="note-body"/>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>