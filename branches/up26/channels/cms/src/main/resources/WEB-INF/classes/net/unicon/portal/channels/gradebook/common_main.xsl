<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<!-- ######################################################################## -->
	<xsl:template match="/">
	<!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>
		<parameter name="subLinkParameter"><xsl:value-of select="$subLinkParameter" /></parameter>
		<parameter name="showGradeColumn"><xsl:value-of select="$showGradeColumn" /></parameter>
    </textarea> -->
		<xsl:call-template name="links"/>
		<!-- Making sure there are rows before we start processsing the gradeboook -->
		<xsl:if test="count(./gradebooks/gradebook-item) &gt; 0">
			<!-- Gradebook -->
			<script type="text/javascript" language="JavaScript" src="javascript/GradebookChannel/main.js"/>
			<form name="gradebookForm" method="post" action="{$baseActionURL}" onsubmit="{$ONSUBMIT}">
				<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
				<!-- UniAcc: DataTable -->
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<xsl:call-template name="buildHeaders">
						<xsl:with-param name="GRADEBOOK" select="./gradebooks"/>
					</xsl:call-template>
					<xsl:choose>
						<xsl:when test="not($editItemScores = 'N' and $editItem = 'N' and $deleteItem = 'N' and $viewSubmissionDetails = 'N' and $addActivation = 'N')">
							<xsl:call-template name="sublinks">
								<xsl:with-param name="commandDefault">
									<xsl:value-of select="$subLinkParameter"/>
								</xsl:with-param>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<tr>
								<td colspan="100" class="uportal-background-med">
									<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								</td>
							</tr>
						</xsl:otherwise>
					</xsl:choose>
				</table>
				<!-- Change log for data changes -->
				<input type="hidden" id="changeLog" name="changeLog" value="">
            </input>
			</form>
		</xsl:if>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="buildHeaders">
		<xsl:param name="GRADEBOOK"/>
		<tr>
			<!-- Empty Header now, fill with sort criteria when supported -->
			<th valign="bottom" align="right" class="table-content-left-top">
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
				<xsl:call-template name="emptyHeaderField"/>
			</th>
			<xsl:for-each select="$GRADEBOOK/gradebook-item">
				<xsl:variable name = "encodedTitle"><xsl:call-template name = "urlEncode" >
						<xsl:with-param name="remainingString"><xsl:value-of select="title" /></xsl:with-param></xsl:call-template>
				</xsl:variable>
				<!-- Column Header -->
				<th valign="bottom" align="center" class="table-content-top" id="{title}">
					<span class="ADA-invisible">Gradebook header titled '<xsl:value-of select="title"/>'</span>
					<object tabindex="-1" align="" id="gradebook_column_header" height="144" width="50" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000">
						<PARAM NAME="quality" VALUE="high"></PARAM>
						<PARAM NAME="bgcolor" VALUE="#FFFFFF"></PARAM>
						<xsl:if test="@type = 2">
							<param value="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={$encodedTitle}&amp;hdrUrl={@uri}&amp;target=_new" name="movie"/>
							<embed pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" align="" name="gradebook_column_header" height="144" width="50" bgcolor="#FFFFFF" quality="high" src="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={$encodedTitle}&amp;hdrUrl={@uri}&amp;target=_new"/>
						</xsl:if>
						<xsl:if test="@type = 1">
							<param value="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={$encodedTitle}" name="movie"/>
							<embed pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" align="" name="gradebook_column_header" height="144" width="50" bgcolor="#FFFFFF" quality="high" src="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName={$encodedTitle}">
							</embed>
						</xsl:if>
					</object>
					<xsl:call-template name="entryHeaderField"/>
				</th>
			</xsl:for-each>
			<!-- Grade Column -->
			<xsl:if test="$showGradeColumn = 'Y'">
				<span class="ADA-invisible">Grade Column</span>
				<th valign="bottom" align="center" class="table-content-right-top" id="{title}">
					<object tabindex="-1" align="" id="gradebook_column_header" height="144" width="50" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000">
						<PARAM NAME="quality" VALUE="high"></PARAM>
						<PARAM NAME="bgcolor" VALUE="#FFFFFF"></PARAM>
						<param value="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName=Grade" name="movie"/>
						<embed pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" align="" name="gradebook_column_header" height="144" width="50" bgcolor="#FFFFFF" quality="high" src="{$GRADEBOOK_HEADER_PATH}/tblHdr.swf?hdrName=Grade">
						</embed>
					</object>
					<xsl:call-template name="gradeHeaderField"/>
				</th>
			</xsl:if>
			<th class="gradebook-empty-right-top" width="100%">
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
			</th>
		</tr>
		<!-- main gradebook -->
		<xsl:call-template name="gradeBookCol">
			<xsl:with-param name="GRADE_BOOK" select="$GRADEBOOK"/>
			<xsl:with-param name="CURR_POS" select="1"/>
		</xsl:call-template>
		<!-- Mean -->
		<tr>
			<td align="center" class="table-light-left" style="font-weight:bold" id="GBMean">
				Mean 
         		<xsl:call-template name="meanHeaderField"/>
			</td>
			<xsl:for-each select="$GRADEBOOK/gradebook-item">
				<td align="center" class="gradebook-data" headers="GBMean {title}">
					<xsl:call-template name="meanDataField"/>
				</td>
			</xsl:for-each>
			<!-- Grade column -->
			<xsl:if test="$showGradeColumn = 'Y'">
				<td class="table-light-right">
					<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					<xsl:call-template name="meanGradeField"/>
				</td>
			</xsl:if>
			<!-- buffer -->
			<td class="gradebook-empty-right" width="100%">
				<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
			</td>
		</tr>
		<!-- Median -->
		<tr>
			<td align="center" class="table-light-left" style="font-weight:bold" id="GBMedian">
				Median 
         		<xsl:call-template name="medianHeaderField"/>
			</td>
			<xsl:for-each select="$GRADEBOOK/gradebook-item">
				<td align="center" class="gradebook-data" headers="GBMedian {title}">
					<xsl:call-template name="medianDataField"/>
				</td>
			</xsl:for-each>
			<!-- Grade column -->
			<xsl:if test="$showGradeColumn = 'Y'">
				<td class="table-light-right">
					<!--<xsl:attribute name="style">border-right: 4px solid #8F8870;</xsl:attribute> -->
					<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
					<xsl:call-template name="medianGradeField"/>
				</td>
			</xsl:if>
			<!-- buffer -->
			<td class="gradebook-empty-right">
				<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
			</td>
		</tr>
		<!-- Submit button -->
		<xsl:call-template name="submitRow">
			<xsl:with-param name="GRADEBOOK" select="$GRADEBOOK"/>
		</xsl:call-template>
		<!-- footer select column -->
		<xsl:call-template name="columnSelectFooter">
			<xsl:with-param name="GRADEBOOK" select="$GRADEBOOK"/>
		</xsl:call-template>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="gradeBookCol">
		<xsl:param name="GRADE_BOOK"/>
		<xsl:param name="CURR_POS"/>
		<xsl:param name="USER_COUNT" select="count($GRADE_BOOK/gradebook-item[1]/gradebook-score)"/>
		<xsl:if test="$GRADE_BOOK/gradebook-item/gradebook-score[$CURR_POS]/@hidden != 'true'">
			<tr>
				<xsl:for-each select="$GRADE_BOOK/gradebook-item">
					<!-- User Titles -->
					<xsl:if test="(position() = 1)">
						<!-- Initializing Score Variables-->
						<td align="center" class="gradebook-user-left" nowrap="nowrap" headers="{title}" id="{./gradebook-score[$CURR_POS]/user/last_name},{./gradebook-score[$CURR_POS]/user/first_name}">
							<xsl:value-of select="./gradebook-score[$CURR_POS]/user/last_name"/>

                  , 
                  <xsl:value-of select="./gradebook-score[$CURR_POS]/user/first_name"/>
							<xsl:call-template name="learnerNameField">
								<xsl:with-param name="CURR_POS" select="$CURR_POS"/>
							</xsl:call-template>
						</td>
					</xsl:if>
					<!-- Scores -->
					<td align="center" class="table-content" headers="{title} {./gradebook-score[$CURR_POS]/user/last_name},{./gradebook-score[$CURR_POS]/user/first_name}">
						<xsl:call-template name="gradeBookDataField">
							<xsl:with-param name="CURR_POS" select="$CURR_POS"/>
						</xsl:call-template>
					</td>
				</xsl:for-each>
				<!-- Grade column -->
				<xsl:if test="$showGradeColumn = 'Y'">
					<td class="gradebook-user-right" style="text-align:center" headers="Grade {$GRADE_BOOK/gradebook-item/gradebook-score[$CURR_POS]/user/last_name},{$GRADE_BOOK/gradebook-item/gradebook-score[$CURR_POS]/user/first_name}">
						<xsl:variable name="TOTAL_SCORE_VALUE">
							<xsl:call-template name="totalScoreField">
								<xsl:with-param name="CURR_POS" select="$CURR_POS"/>
								<xsl:with-param name="LIST" select="$GRADE_BOOK/gradebook-item"/>
								<xsl:with-param name="LIST_POS" select="1"/>
								<xsl:with-param name="TOTAL_SCORE" select="0"/>
								<xsl:with-param name="MAX_SCORE" select="0"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:call-template name="totalScoreDataField">
							<xsl:with-param name="TOTAL_SCORE_VALUE" select="$TOTAL_SCORE_VALUE"/>
							<xsl:with-param name="CURR_POS" select="$CURR_POS"/>
						</xsl:call-template>
					</td>
				</xsl:if>
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
	<!-- ######################################################################## -->
	<xsl:template name="emptyHeaderField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="entryHeaderField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="gradeHeaderField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="meanHeaderField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="meanDataField">
		<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
		<xsl:choose>
			<xsl:when test="not((@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
				<xsl:value-of select="@mean"/>
			</xsl:when>
			<xsl:otherwise>0</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="meanGradeField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="medianHeaderField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="medianDataField">
		<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
		<xsl:choose>
			<xsl:when test="not((@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
				<xsl:value-of select="@median"/>
			</xsl:when>
			<xsl:otherwise>0</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="medianGradeField">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="submitRow">
   </xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="columnSelectFooter">
		<xsl:param name="GRADEBOOK"/>
		<xsl:if test="not($editItemScores = 'N' and $editItem = 'N' and $deleteItem = 'N' and $viewSubmissionDetails = 'N' and $addActivation = 'N')">
			<tr>
				<td align="center" class="table-light-left" id="GBSelectCol">
					<span class="stamp">Select Column</span>
				</td>
				<xsl:for-each select="$GRADEBOOK/gradebook-item">
					<td align="center" class="gradebook-data" headers="GBSelectCol {title}">
						<xsl:choose>
							<xsl:when test="@id = $gradebookItemID">
								<input type="radio" class="radio" name="position" value="{@position}" onclick="gradebookForm.gradebook_itemID_selected.options[this.value].selected = true" checked="checked"></input>
							</xsl:when>
							<xsl:when test="(position() = 1) and ($gradebookItemID = '')">
								<input type="radio" class="radio" name="position" value="{@position}" onclick="gradebookForm.gradebook_itemID_selected.options[this.value].selected = true" checked="checked"></input>
							</xsl:when>
							<xsl:otherwise>
								<input type="radio" class="radio" name="position" value="{@position}" onclick="gradebookForm.gradebook_itemID_selected.options[this.value].selected = true"></input>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</xsl:for-each>
				<!-- Grade column -->
				<td class="table-light-right">
					<!--<xsl:attribute name="style">border-right: 4px solid #8F8870;</xsl:attribute> -->
					<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
				</td>
				<!-- buffer -->
				<td class="gradebook-empty-right-bottom" width="100%">
					<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="learnerNameField">
		<xsl:param name="CURR_POS"/>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="gradeBookDataField">
		<xsl:param name="CURR_POS"/>
		<xsl:choose>
			<!-- If permitted to access details make link -->
			<xsl:when test="$accessDetails = 'Y'">
				<a href="{$baseActionURL}?command=details&amp;gradebook_itemID={@id}&amp;username={./gradebook-score[$CURR_POS]/@username}" title="To submit assignments and feedback, and view details">
					<xsl:choose>
						<xsl:when test="(./gradebook-score[$CURR_POS]/@score = -1)">--</xsl:when>
						<xsl:when test="((@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
							--
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="./gradebook-score[$CURR_POS]/@score"/>
						</xsl:otherwise>
					</xsl:choose>
				</a>
			</xsl:when>
			<!-- If not permitted to access details -->
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="(./gradebook-score[$CURR_POS]/@score = -1)">--</xsl:when>
					<xsl:when test="((@feedback = 'no') or ($accessHiddenFeedback = 'N'))">
						<xsl:value-of select="./gradebook-score[$CURR_POS]/@score"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="./gradebook-score[$CURR_POS]/@score"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ######################################################################## -->
	<!-- ######## This template is common to main, export, and edit all ######### -->
	<!-- ######################################################################## -->
	<xsl:template name="totalScoreField">
		<xsl:param name="CURR_POS"/>
		<xsl:param name="LIST"/>
		<xsl:param name="LIST_POS"/>
		<xsl:param name="TOTAL_SCORE"/>
		<xsl:param name="MAX_SCORE"/>
		<!--<xsl:param name="LOG" /> -->
		<xsl:variable name = "COLUMN" select="$LIST[$LIST_POS]" />
		<xsl:variable name = "COLUMN_SCORE" select="$COLUMN/gradebook-score[$CURR_POS]" />
		<xsl:choose>
			<xsl:when test="$LIST_POS != (count($LIST) + 1)">
				<xsl:choose>
					<!-- Test to make sure a score of -1 is not included in the final grade -->
					<xsl:when test="($COLUMN_SCORE/@score != -1) and ($COLUMN/@max_score != '') and ($COLUMN/@max_score != '0') and (($COLUMN/@feedback != 'no') or ($accessHiddenFeedback = 'Y'))">
						<xsl:call-template name="totalScoreField">
							<xsl:with-param name="CURR_POS" select="$CURR_POS"/>
							<xsl:with-param name="LIST" select="$LIST"/>
							<xsl:with-param name="LIST_POS" select="$LIST_POS+1"/>
							<xsl:with-param name="TOTAL_SCORE">
								<!-- Add to Total Score, score / max_score * weight (i.e. percentage * weight) -->
								<xsl:value-of select="$TOTAL_SCORE + ((($COLUMN_SCORE/@score) div ($COLUMN/@max_score)) * ($COLUMN/@weight))"/>
							</xsl:with-param>
							<!-- Add to Max Score, weight (i.e. 100% * weight) -->
							<xsl:with-param name="MAX_SCORE" select="$MAX_SCORE + ($COLUMN/@weight)"/>
							<!--<xsl:with-param name="LOG"><xsl:value-of select="concat($LOG,':',((($LIST[$LIST_POS]/gradebook-score[$CURR_POS]/@score) * ($LIST[$LIST_POS]/@weight))))" /></xsl:with-param> -->
						</xsl:call-template>
					</xsl:when>
					<!-- If -1 then value is skipped in calculation -->
					<xsl:otherwise>
						<xsl:call-template name="totalScoreField">
							<xsl:with-param name="CURR_POS" select="$CURR_POS"/>
							<xsl:with-param name="LIST" select="$LIST"/>
							<xsl:with-param name="LIST_POS" select="$LIST_POS+1"/>
							<xsl:with-param name="TOTAL_SCORE" select="$TOTAL_SCORE"/>
							<xsl:with-param name="MAX_SCORE" select="$MAX_SCORE"/>
							<!--<xsl:with-param name="LOG" select="$LOG" /> -->
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<!-- Test to avoid division by 0 -->
					<xsl:when test="$MAX_SCORE = 0">--</xsl:when>
					<!-- Provide % to 1 decimal place (i.e. 66.6%) -->
					<xsl:otherwise>
						<!-- Total Percentage is Total Score / Max Score -->
						<xsl:value-of select="round(($TOTAL_SCORE div $MAX_SCORE) * 1000) div 10"/>
						<!--LOG<xsl:value-of select="$LOG" />|<xsl:value-of select="$TOTAL_SCORE" />/<xsl:value-of select="$MAX_SCORE" />|=<xsl:value-of select="round(($TOTAL_SCORE div $MAX_SCORE) * 1000) div 10" />|-->
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="totalScoreDataField">
		<xsl:param name="TOTAL_SCORE_VALUE"/>
		<xsl:param name="CURR_POS"/>
		<xsl:value-of select="$TOTAL_SCORE_VALUE"/>
	</xsl:template>
	<!-- ######################################################################## -->
	<xsl:template name="urlEncode">
		<xsl:param name="remainingString" />
		<!-- Create variable for character to be encoded -->
		<xsl:variable name = "character"><xsl:value-of select="substring($remainingString,1,1)" /></xsl:variable>
		<!-- Create variable to hold apostrophe, in order to be able to test it (i.e. single quotes string text won't work) -->
		<xsl:variable name = "apostrophe">'</xsl:variable>
		<!-- Check for special characters to encode, else output character as normal -->
		<xsl:choose>
			<xsl:when test="$character=' '">%20</xsl:when>
			<xsl:when test="$character='~'">%7E</xsl:when>
			<xsl:when test="$character='!'">%21</xsl:when>
			<xsl:when test="$character='#'">%23</xsl:when>
			<xsl:when test="$character='$'">%24</xsl:when>
			<xsl:when test="$character='%'">%25</xsl:when>
			<xsl:when test="$character='^'">%5E</xsl:when>
			<xsl:when test="$character='('">%28</xsl:when>
			<xsl:when test="$character=')'">%29</xsl:when>
			<xsl:when test="$character='='">%3D</xsl:when>
			<xsl:when test="$character='`'">%60</xsl:when>
			<xsl:when test="$character='&amp;'">%26</xsl:when>
			<xsl:when test="$character='{'">%7B</xsl:when>
			<xsl:when test="$character='}'">%7D</xsl:when>
			<xsl:when test="$character='|'">%7C</xsl:when>
			<xsl:when test="$character=':'">%3A</xsl:when>
			<xsl:when test="$character='&#034;'">%22</xsl:when>
			<xsl:when test="$character='&lt;'">%3C</xsl:when>
			<xsl:when test="$character='&gt;'">%3E</xsl:when>
			<xsl:when test="$character='?'">%3F</xsl:when>
			<xsl:when test="$character=','">%2C</xsl:when>
			<xsl:when test="$character=$apostrophe">%27</xsl:when>
			<xsl:when test="$character=';'">%3B</xsl:when>
			<xsl:when test="$character='\'">%5C</xsl:when>
			<xsl:when test="$character=']'">%5D</xsl:when>
			<xsl:when test="$character='['">%5B</xsl:when>
			<xsl:otherwise><xsl:value-of select="$character" /></xsl:otherwise>
		</xsl:choose>
		<!-- If there is more characters to encode, call function again with the remaining string -->
		<xsl:if test = "string-length($remainingString) &gt; 1">
			<xsl:call-template name = "urlEncode" >
				<xsl:with-param name="remainingString" select="substring($remainingString,2)" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!-- ######################################################################## -->
</xsl:stylesheet>
