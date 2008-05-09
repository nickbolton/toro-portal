<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<!-- Non-permission Parameters -->
	<xsl:template match="/">
		<xsl:call-template name="links"/>
		<!-- Start JavaScript -->
		<script type="text/javascript" language="JavaScript">
    		initializeGradebookChannel = function()
    		{
    		    rows = parseInt('<xsl:value-of select="count(./gradebooks/gradebook-item)"/>'); 
    		    cols = parseInt('<xsl:value-of select="./gradebooks/gradebook-item[@id = $gradebookItemID]/@position"/>');
    		    min = parseInt('<xsl:value-of select="./gradebooks/gradebook-item[@id = $gradebookItemID]/@min_score"/>');
    		    max = parseInt('<xsl:value-of select="./gradebooks/gradebook-item[@id = $gradebookItemID]/@max_score"/>');
    		}
        </script>
		<xsl:call-template name="mainJS"/>
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="positionID"><xsl:value-of select="$positionID" /></parameter> 
    </textarea> -->
		<!-- End Javascript -->
		<!-- Gradebook -->
		<form onSubmit="return validator.applyFormRules(this, new GradebookScoresRulesObject(this))" name="gradebookForm" method="post" action="{$baseActionURL}">
			<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
			<!-- UniAcc: Data Table -->
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<!-- Empty Header -->
					<th class="gradebook-empty-left-top">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					</th>
					<!-- Column Header -->
					<th class="table-content-single-top" align="center" valign="bottom" scope="col">
						<span class="ADA-invisible">Gradebook header titled '<xsl:value-of select="title"/>'</span>
						<object tabindex="-1" align="" id="gradebook_column_header" height="144" width="50" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000">
							<PARAM NAME="quality" VALUE="high"/>
							<PARAM NAME="bgcolor" VALUE="#FFFFFF"/>
							<xsl:if test="./gradebooks/gradebook-item[@id = $gradebookItemID]/@type = 2">
								<param value="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={./gradebooks/gradebook-item[@id = $gradebookItemID]/title}&amp;hdrUrl={./gradebooks/gradebook-item[@id = $gradebookItemID]/@uri}&amp;target=_new" name="movie"/>
								<embed pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" align="" name="gradebook_column_header" height="144" width="50" bgcolor="#FFFFFF" quality="high" src="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={./gradebooks/gradebook-item[@id = $gradebookItemID]/title}&amp;hdrUrl={./gradebooks/gradebook-item[@id = $gradebookItemID]/@uri}&amp;target=_new"/>
							</xsl:if>
							<xsl:if test="./gradebooks/gradebook-item[@id = $gradebookItemID]/@type = 1">
								<param value="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={./gradebooks/gradebook-item[@id = $gradebookItemID]/title}" name="movie"/>
								<embed pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" align="" name="gradebook_column_header" height="144" width="50" bgcolor="#FFFFFF" quality="high" src="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={./gradebooks/gradebook-item[@id = $gradebookItemID]/title}"/>
							</xsl:if>
						</object>
					</th>
					<th class="gradebook-empty-right-top" width="100%">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					</th>
				</tr>
				<!-- main gradebook -->
				<xsl:call-template name="gradeBookCol">
					<xsl:with-param name="GRADE_BOOK" select="./gradebooks"/>
					<xsl:with-param name="CURR_POS" select="1"/>
				</xsl:call-template>
				<!-- Mean -->
				<tr>
					<td align="center" class="table-light-left" style="font-weight:bold" scope="row">Mean</td>
					<td align="center" class="gradebook-data-right">
						<input tabindex="-1" type="text" class="text-disabled" size="3" value="{./gradebooks/gradebook-item[@id = $gradebookItemID]/@mean}" name="Mean_{$gradebookItemID}" readonly="readonly" title="This is the calculated mean value for the scores in this column."/>
					</td>
					<td class="gradebook-empty-right" width="100%">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					</td>
				</tr>
				<!-- Median -->
				<tr>
					<td align="center" class="table-light-left" style="font-weight:bold" scope="row">Median</td>
					<td align="center" class="gradebook-data-right">
						<input tabindex="-1" type="text" class="text-disabled" size="3" value="{./gradebooks/gradebook-item[@id = $gradebookItemID]/@median}" name="Median_{$gradebookItemID}" readonly="readonly" title="This is the calculated median value for the scores in this column.">
                        </input>
					</td>
					<td class="gradebook-empty-right" width="100%">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					</td>
				</tr>
				<!-- Submit button -->
				<tr>
					<td colspan="2" class="table-nav-gradebook" nowrap="nowrap">
						<input type="submit" class="uportal-button" value="Update" title="To store these changes and return to the main view of the gradebook" onclick="document.gradebookForm.command.value = 'update_column'"/>
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel these changes and return to the main view of the gradebook"/>
					</td>
					<td class="gradebook-empty-right-bottom">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					</td>
				</tr>
				<xsl:call-template name="sublinks">
					<xsl:with-param name="commandDefault">update_column</xsl:with-param>
				</xsl:call-template>
			</table>
			<!-- Change log for data changes -->
			<input type="hidden" id="changeLog" name="changeLog" value=""/>
		</form>
	</xsl:template>
	<xsl:template name="gradeBookCol">
		<xsl:param name="GRADE_BOOK"/>
		<xsl:param name="CURR_POS"/>
		<xsl:param name="USER_COUNT" select="count($GRADE_BOOK/gradebook-item[@id = $gradebookItemID]/gradebook-score)"/>
		<!-- Not needed for now -->
		<!--

    <xsl:param name="INDEX" select="$GRADE_BOOK/gradebook-item[@id = $gradebookItemID]/@position"/>

    -->
		<xsl:if test="$GRADE_BOOK/gradebook-item/gradebook-score[$CURR_POS]/@hidden != 'true'">
			<tr>
				<xsl:for-each select="$GRADE_BOOK/gradebook-item[@id = $gradebookItemID]">
					<!-- User Titles -->
					<xsl:if test="(position() = 1)">
						<td class="gradebook-user-left" align="center" nowrap="nowrap" scope="row">
							<label for="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}">
								<xsl:value-of select="./gradebook-score[$CURR_POS]/user/last_name"/>, <xsl:text/>
								<xsl:value-of select="./gradebook-score[$CURR_POS]/user/first_name"/>
							</label>
						</td>
					</xsl:if>
					<!--  name="SCORE_{USER_NAME}_{gbItemID}" -->
					<td class="table-content-right" align="center">
						<input class="text" type="text" title="Enter score here." size="3" maxlength="3" onchange="recordOnChange(this);" name="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{./gradebook-score[$CURR_POS]/@gradebook-itemid}">
							<xsl:attribute  name = "value" >
						        <xsl:choose>
							  	   <!-- If the score is not defined, then show as blank -->
						           <xsl:when test="./gradebook-score[$CURR_POS]/@score = -1"></xsl:when>
						
						           <xsl:otherwise><xsl:value-of select="./gradebook-score[$CURR_POS]/@score" /></xsl:otherwise>
						        </xsl:choose>
							</xsl:attribute>
						</input>
						
						
						<!--<xsl:choose>
							<xsl:when test="@type = 2">
								<xsl:choose>
									<xsl:when test="./gradebook-score[$CURR_POS]/@score = -1">
										<input class="text" type="text" size="3" maxlength="3" value="" onchange="recordOnChange(this);" title="Enter score here." name="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}" id="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}">
						        </input>
									</xsl:when>
									<xsl:otherwise>
										<input class="text" type="text" size="3" maxlength="3" value="{./gradebook-score[$CURR_POS]/@score}" onchange="recordOnChange(this);" title="Enter score here." name="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}" id="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}">
					            </input>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:choose>
									<xsl:when test="./gradebook-score[$CURR_POS]/@score = -1">
										<input tabindex="-1" class="text-disabled" readonly="readonly" type="text" size="3" maxlength="3" value="" onchange="recordOnChange(this);" title="There is no recorded score yet for this online assessment. The student must complete the assessment online for a score to be recorded." name="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}">
						        </input>
									</xsl:when>
									<xsl:otherwise>
										<input tabindex="-1" class="text-disabled" readonly="readonly" type="text" size="3" maxlength="3" value="{./gradebook-score[$CURR_POS]/@score}" onchange="recordOnChange(this);" title="This score is from an online assessment.  It can't be directly edited.  A modifier can be added in the assignment details view." name="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{$gradebookItemID}">
					            </input>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose> -->
						
						
					</td>
				</xsl:for-each>
				<td class="gradebook-empty-right" width="100%">
					<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
				</td>
			</tr>
		</xsl:if>
		<!-- Recursive Call -->
		<xsl:if test="$CURR_POS &lt; $USER_COUNT">
			<xsl:call-template name="gradeBookCol">
				<xsl:with-param name="GRADE_BOOK" select="$GRADE_BOOK"/>
				<xsl:with-param name="CURR_POS" select="$CURR_POS + 1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
