<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<xsl:template match="/">
		<script LANGUAGE="JavaScript" src="javascript/GradebookChannel/weighting.js"> </script>
		<xsl:call-template name="links"/>

		<form name="gradebookForm" method="post" action="{$baseActionURL}" onsubmit="return validator.applyFormRules(this, new GradebookWeightingRulesObject(this))">

			<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
			<!-- UniAcc: Data Table -->
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<th class="th" id="cn">Column Name</th>
					<th class="th" id="cw">Column Weight</th>
				</tr>
				<xsl:apply-templates select="./gradebooks/gradebook-item"/>
				<xsl:if test="count(./gradebooks/gradebook-item) &gt; 0">
					<tr>
						<td class="table-light-left" style="text-align:center;">
							<label for="gbcwtotal">Total</label>
						</td>
						<td class="table-content-right" style="text-align:center">
							<input tabindex="-1" type="text" class="text-disabled" maxlength="3" size="3" value="{sum(/gradebooks/gradebook-item/@weight)}" name="totalWeight" readonly="readonly" title="This is the calculated total weight for all of the gradebook columns." id="gbcwtotal"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="table-nav" align="center">
							<input class="uportal-button" type="submit" value="Submit" title="To save these changes and return to the main view of the gradebook"/>
							<input class="uportal-button" type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}';" title="To cancel these changes and return to the main view of the gradebook"></input>
						</td>
					</tr>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="not($editItemScores = 'N' and $editItem = 'N' and $deleteItem = 'N')">
						<xsl:call-template name="sublinks">
							<xsl:with-param name="commandDefault">update_weight</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<tr>
							<td colspan="100" class="light">
								<input type="hidden" name="command" value="update_weight"/>
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							</td>
						</tr>
					</xsl:otherwise>
				</xsl:choose>
			</table>
			<input TYPE="hidden" id="changeLog" NAME="changeLog" VALUE=""/>
		</form>
	</xsl:template>
	<xsl:template match="gradebook-item">
		<tr>
			<td class="table-light-left" style="text-align:center" headers="cn">
				<label for="gbcwt{@id}">
					<a href="{$baseActionURL}?command=edit&amp;gradebook_itemID={@id}" title="To edit this entry's properties." onmouseover="swapImage('gbEditColumnImage{position()}','channel_edit_active.gif')" onmouseout="swapImage('gbEditColumnImage{position()}','channel_edit_base.gif')">
						<xsl:value-of select="title"/>
						<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon: Click here to edit {title}'s properties" title="'Edit' icon: Click here to edit {title}'s properties" align="absmiddle" name="gbEditColumnImage{position()}" id="gbEditColumnImage{position()}"/>
					</a>
				</label>
			</td>
			<td class="table-content-right" style="text-align:center" headers="cw">
				<input type="text" class="text" maxlength="3" size="3" value="{@weight}" name="weight_{@id}" onchange="recordOnChange(this)" title="Enter weight here." id="gbcwt{@id}"/>

			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
