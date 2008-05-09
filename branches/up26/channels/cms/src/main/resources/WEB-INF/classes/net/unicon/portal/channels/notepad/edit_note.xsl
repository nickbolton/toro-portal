<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
<!--<xsl:template match="/">
    <textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/> 
   	</textarea>

    <xsl:apply-templates />
</xsl:template>  -->

    <xsl:template match="user-notes">
        <xsl:call-template name="autoFormJS"/>
        <xsl:call-template name="links"/>

        <form action="{$baseActionURL}?command=submit&amp;ID={note/@id}&amp;sortby={$sortby}&amp;order={$order}" method="post" onsubmit="return validator.applyFormRules(this, new NotePadRulesObject())">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top" id="EditNote">Edit Note</th>
                </tr>
                <xsl:apply-templates select="note"/>
            </table>
       </form>
    </xsl:template>

    <xsl:template match="note">
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap" id="Date" headers="EditNote">
				Date
			</td>
            <td class="table-content-right" align="left" width="100%" headers="EditNote Date">
                <xsl:value-of select="@date"/>
            </td>
        </tr>
        
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap" id="Title" headers="EditNote">
				<label for="npct1">Title</label>
				</td>
            <td class="table-content-right" align="left" width="100%" headers="EditNote Title">
                <xsl:variable name="notetitle">
                <xsl:value-of select="note-title"/>
            </xsl:variable>
                <input type="text" class="text" name="title" value="{$notetitle}" id="npct1">
                </input>
				</td>
			</tr>
			<tr>
				<td class="table-light-left" align="right" nowrap="nowrap" id="Note" headers="EditNote">
					<label for="npcta1">Note</label>
				</td>
				<td class="table-content-right" align="left" width="100%" headers="EditNote Note">
					<textarea name="message" cols="40" id="npcta1">
							<xsl:for-each select="note-body/node()">
								<xsl:value-of select="."/>
							</xsl:for-each>
					</textarea>
				</td>
			</tr>
			<tr>
				<td colspan="2" class="table-nav" headers="EditNote">
					<input name="submit" value="Submit" type="submit" class="uportal-button" title="Submit changes to this note"/>
            		<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?sortby={$sortby}&amp;order={$order}'" title="Cancel changes to this note"/>
				</td>
			</tr>
	</xsl:template>
</xsl:stylesheet>
