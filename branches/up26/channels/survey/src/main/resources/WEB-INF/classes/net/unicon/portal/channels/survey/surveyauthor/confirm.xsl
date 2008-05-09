<?xml version='1.0'?>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:output method='html' />
	<xsl:include href="common.xsl" />

	<!-- //////////////////////////////////////////////////////// -->
	<xsl:template match='confirm'>
		<form action="{$baseActionURL}" method="post">
			<table width="100%" cellpadding="0" cellspacing="0" border="0">
				<tr>
					<th nowrap="nowrap" valign="middle" class="th-top" 
						colspan="2"> <img height="1" width="3" src="{$SPACER}" 
						border="0" alt="" title="" /> Confirm Cancel </th>
				</tr>
				<tr>
					<td class="table-content-single-top" 
						style="text-align:center">
						<span class="uportal-channel-warning">
							<xsl:copy-of select="text" />
						</span>
					</td>
				</tr>
				<tr>
					<td class="table-content-single-top" 
						style="text-align:center">
						<span class="uportal-channel-warning">
							<xsl:copy-of select="instructions" />
						</span>
					</td>
				</tr>
				<tr>
					<td class="table-content-single-bottom" 
						style="text-align:center">
						<input type="hidden" name="sid" value="{$sid}" />
						<input type="submit" class="uportal-button" value="OK" 
							name="do~clickOK" title="Confirm Cancel" />
						<input type="submit" class="uportal-button" 
							value="Cancel" name="do~cancel" 
							title="To Return to the New Survey Form" />
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
</xsl:stylesheet>