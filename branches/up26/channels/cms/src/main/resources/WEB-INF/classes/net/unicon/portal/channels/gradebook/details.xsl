<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>
	
	<!-- INCLUDE FILES -->
	<xsl:include href="common.xsl"/>
	
	<!-- VARIABLES -->
	<xsl:variable name="GRADEBOOK-ITEM-POINTER" select="/gradebooks/gradebook-item[@id = $gradebookItemID]"/>
	<xsl:variable name="USER-GRADEBOOK-SCORE" select="$USER[@username = $username]"/>
	<xsl:variable name = "ORIGINAL_SCORE" select = "$USER-GRADEBOOK-SCORE/../@original_score" />
	<xsl:variable name = "SCORE" select = "$USER-GRADEBOOK-SCORE/../@score" />
	<xsl:variable name = "TYPE" select = "$GRADEBOOK-ITEM-POINTER/@type" />
	<xsl:variable name = "USER" select = "$GRADEBOOK-ITEM-POINTER/gradebook-score/user" />
	<xsl:variable name = "FEEDBACK_HIDDEN" select = "boolean(($GRADEBOOK-ITEM-POINTER/@feedback = 'no') and ($accessHiddenFeedback = 'N'))" />
	<xsl:variable name = "MODIFIER">
		<xsl:choose>
			<xsl:when test = "not(boolean($ORIGINAL_SCORE))"></xsl:when>
			<xsl:when test="($SCORE = '-1') or ($SCORE = '') or ($ORIGINAL_SCORE = '-1') or ($ORIGINAL_SCORE = '')"></xsl:when>
			<xsl:otherwise><xsl:value-of select="number($SCORE) - number($ORIGINAL_SCORE)" /></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name = "LABEL">
		<xsl:choose>
			<xsl:when test="($TYPE = '1')">Assessment</xsl:when>
			<xsl:otherwise>Assignment</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name = "VIEWSCORING" />
	<xsl:variable name = "VIEWPFEEDBACK" />
	
	
	<!-- PARENT TEMPLATE -->
	<xsl:template match="/">
		<xsl:call-template name="links"/>
		
		<!--|<xsl:value-of select="$ORIGINAL_SCORE" />|<xsl:value-of select="$MODIFIER" />|<xsl:value-of select="$SCORE" /><br/> -->
		<!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
        <parameter name="username"><xsl:value-of select="$username" /></parameter>   
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>   
        <parameter name="accessDetails"><xsl:value-of select="$accessDetails" /></parameter>   
        <parameter name="viewSubmissionDetails"><xsl:value-of select="$viewSubmissionDetails" /></parameter>   
        <parameter name="viewFeedbackDetails"><xsl:value-of select="$viewFeedbackDetails" /></parameter>   
        <parameter name="editSubmissionDetails"><xsl:value-of select="$editSubmissionDetails" /></parameter>   
        <parameter name="editFeedbackDetails"><xsl:value-of select="$editFeedbackDetails" /></parameter>   
    	</textarea> -->
		
		<!-- Start JavaScript -->
		<script type="text/javascript" language="JavaScript1.2">
		initializeGradebookChannel = function()
		{
			// nothing to initialize
		}
    	</script>
		<xsl:call-template name="mainJS"/>
		
			<form onSubmit="return validator.applyFormRules(this, new GradebookScoresRulesObject(this))"
				  name="gradebookForm" action="{$baseActionURL}" enctype="multipart/form-data" method="post">
			<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
			<!--<input type="hidden" name="command" value="submit_details"></input> -->
			<!-- UniAcc: Layout Table -->
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<!-- === TABLE HEADER === -->
				<tr>
					<th class="th" colspan="2"><xsl:value-of select="$LABEL"/> Details</th>
				</tr>
				<!-- === GRADEBOOK ENTRY/COLUMN === -->
				<tr>
					<td class="table-light-left" style="text-align:right" nowrap="nowrap">
                		<label for="gbds1"><xsl:value-of select="$LABEL"/></label>
					</td>
					<td class="table-content-right" width="100%">
						<!--<select name="assignmentLink"> -->
						<select name="current_gradebook_itemID" id="gbds1">
							<xsl:attribute name="onchange">document.gradebookForm.command.value = 'details'; document.gradebookForm.gradebook_itemID.value = document.gradebookForm.current_gradebook_itemID.options[document.gradebookForm.current_gradebook_itemID.selectedIndex].value; document.gradebookForm.submit();</xsl:attribute>
							<xsl:apply-templates select="gradebooks/gradebook-item"/>
						</select>
					</td>
				</tr>
				<!-- === MEMBER === -->
				<tr>
					<td class="table-light-left" style="text-align:right" nowrap="nowrap">
						<label for="gbds2">Member</label>
					</td>
					<td class="table-content-right" width="100%">
						<xsl:choose>
							<!-- If more than 1 member then display a dropdown box -->
							<xsl:when test="($viewAll = 'Y') and (count($USER) &gt; 1)">
								<select name="username" id="gbds2">
									<xsl:attribute name="onchange">document.gradebookForm.command.value = 'details'; document.gradebookForm.submit();</xsl:attribute>
									<xsl:apply-templates select="$USER"/>
								</select>
							</xsl:when>
							<!-- Otherwise, display in text field -->
							<xsl:otherwise>
								<img height="1" width="7" src="{$SPACER}" alt="" title="" border="0"/>
								<xsl:value-of select="$USER[1]/last_name"/>, <xsl:value-of select="$USER[1]/first_name"/>
								<input type="hidden" value="{$username}" name="username"/>
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
				<!-- === SCORE === -->
				<tr>
					<td class="table-light-left" style="text-align:right" nowrap="nowrap" id="GBScore">
						<xsl:choose>
							<xsl:when test="$TYPE = '1'">
								<label for="gbdt1">Original Score</label>
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<a href="javascript:alert(unescape('The original score is generated by the assessment system, when the assessment is graded.'));void(null);" 
									title="The original score is generated by the assessment system, when the assessment is graded." 
									onmouseover="swapImage('gbScoreHelpImage','channel_help_active.gif')" onmouseout="swapImage('gbScoreHelpImage','channel_help_base.gif')">
									<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif" 
										alt="'Help' icon linking to alert box stating: The original score is generated by the assessment system, when the assessment is graded." 
										title="'Help' icon linking to alert box stating: The original score is generated by the assessment system, when the assessment is graded." 
										name="gbScoreHelpImage" id="gbScoreHelpImage" align="absmiddle"/>
								</a>
							</xsl:when>
							<xsl:otherwise>
								<label for="gbdt1">Score</label>
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<a href="javascript:alert(unescape('The score is a rating assigned to the submitted assignment by the grader.'));void(null);" title="The score is a rating assigned to the submitted assignment by the grader." onmouseover="swapImage('gbScoreHelpImage','channel_help_active.gif')" onmouseout="swapImage('gbScoreHelpImage','channel_help_base.gif')">
									<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif" alt="'Help' icon linking to alert box stating: The score is a rating assigned to the submitted assignment by the grader." title="'Help' icon linking to alert box stating: The score is a rating assigned to the submitted assignment by the grader." name="gbScoreHelpImage" id="gbScoreHelpImage" align="absmiddle"/>
								</a>
							</xsl:otherwise>
						</xsl:choose>
					</td>
					<td class="table-content-right" headers="GBScore">
						<xsl:choose>
							<!-- Show original score in non-editable text field if online assessment and permitted to edit score -->
							<xsl:when test="$editItemScores = 'Y' and $TYPE = '1'">
								<input maxlength="3" id="gbdt1" type="text" size="3" name="original_score" class="text-disabled" readonly="readonly">
									<xsl:attribute name="value"><xsl:choose><xsl:when test="$ORIGINAL_SCORE = -1"/><xsl:otherwise><xsl:value-of select="$ORIGINAL_SCORE"/></xsl:otherwise></xsl:choose></xsl:attribute>
								</input>
							</xsl:when>
							<!-- Show score in editable text field if not online assessment and permitted to edit score  -->
							<xsl:when test="$editItemScores = 'Y' and $TYPE = '2'">
								<input maxlength="3" id="gbdt1" type="text" size="3" onchange="recordOnChange(this);" name="score">
									<xsl:attribute name="value">
										<xsl:choose>
											<xsl:when test="$SCORE = -1"/>
											<xsl:otherwise>
												<xsl:value-of select="$SCORE"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:attribute>
								</input>
							</xsl:when>
							<!-- Otherwise, show score as simple text -->
							<xsl:otherwise>
								<xsl:choose>
									<!-- If no score, or feedback set to hidden and user is unable to access hidden feedback then show blank -->
									<xsl:when test="($SCORE = -1) or ($FEEDBACK_HIDDEN)"> &#xA0;</xsl:when>
									<xsl:otherwise>
										<img height="1" width="7" src="{$SPACER}" alt="" title="" border="0"/>
										<xsl:choose>
											<xsl:when test="$TYPE = '1'">
												<xsl:value-of select="$ORIGINAL_SCORE"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="$SCORE"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
							
						</xsl:choose>
						<!-- SCORING BREAKDOWN LINK -->
						<xsl:if test="$VIEWSCORING = 'Y'">
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/><a href="{$baseActionURL}" title="View the scoring breakdown for this assessment" onmouseover="swapImage('gbViewScoringBreakdown','channel_view_active.gif')" onmouseout="swapImage('gbViewScoringBreakdown','channel_view_base.gif')">
								Scoring Breakdown
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to the scoring breakdown for this assessment" title="'View' icon linking to the scoring breakdown for this assessment" name="gbViewScoringBreakdown" id="gbViewScoringBreakdown" align="absmiddle"/>
							</a>
						</xsl:if>
					</td>
				</tr>
				<!-- === ASSESSMENT DETAILS === -->
				<!-- Online Assessment Details information -->
				<xsl:if test="$TYPE = '1'">
					<tr>
						<td class="table-light-left" style="text-align:right" nowrap="nowrap" id="GBMod">
							<label for="gbdt2">Modifier</label>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<a href="javascript:alert(unescape('The modifier is a value that the instructor can add to the generated assessment score as an adjustment.'));void(null);" title="The modifier is a value that the instructor can add to the generated assessment score as an adjustment." onmouseover="swapImage('gbModifierHelpImage','channel_help_active.gif')" onmouseout="swapImage('gbModifierHelpImage','channel_help_base.gif')">
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif" alt="'Help' icon linking to alert box stating: The modifier is a value that the instructor can add to the generated assessment score as an adjustment." title="'Help' icon linking to alert box stating: The modifier is a value that the instructor can add to the generated assessment score as an adjustment." name="gbModifierHelpImage" id="gbModifierHelpImage" align="absmiddle"/>
							</a>
						</td>
						<td class="table-content-right" headers="GBMod">
							<xsl:choose>
								<xsl:when test="$editItemScores = 'Y'">
									<input maxlength="3" id="gbdt2" type="text" size="3" name="modifier" class="text" value="{$MODIFIER}" onchange="changeScoreModifier(this);" />
								</xsl:when>
								<!-- Otherwise, show score as simple text -->
								<xsl:otherwise>
									<xsl:choose>
										<!-- If no score, or feedback set to hidden and user is unable to access hidden feedback then show blank -->
										<xsl:when test="($FEEDBACK_HIDDEN)"> &#xA0;</xsl:when>
										<!-- Otherwise, display as simple text -->
										<xsl:otherwise>
											<img height="1" width="7" src="{$SPACER}" alt="" title="" border="0"/>
											<xsl:value-of select="$MODIFIER" />
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
							
						</td>
					</tr>
					<tr>
						<td class="table-light-left" style="text-align:right" nowrap="nowrap" id="GBModScore">
							<label for="gbdt3">Total Score</label>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<a href="javascript:alert(unescape('The total score is the sum of the generated assessment score and the instructor\'s modifier, and will be what is reflected in the student\'s total grade.'));void(null);" 
								title="The total score is the sum of the generated assessment score and the instructor's modifier, and will be what is reflected in the student's total grade." 
								onmouseover="swapImage('gbModifiedScoreHelpImage','channel_help_active.gif')" onmouseout="swapImage('gbModifiedScoreHelpImage','channel_help_base.gif')">
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif" 
									alt="'Help' icon linking to alert box stating: The total score is the sum of the generated assessment score and the instructor's modifier, and will be what is reflected in the student's total grade." 
									title="'Help' icon linking to alert box stating: The total score is the sum of the generated assessment score and the instructor's modifier, and will be what is reflected in the student's total grade." 
									name="gbModifiedScoreHelpImage" id="gbModifiedScoreHelpImage" align="absmiddle"/>
							</a>
						</td>
						<td class="table-content-right" headers="GBModScore">
							<xsl:choose>
								<xsl:when test="$editItemScores = 'Y'">
									<input maxlength="3" id="gbdt3" type="text" size="3" name="score" class="text" onchange="changeTotalScore(this);">
										<xsl:attribute name="value"><xsl:choose><xsl:when test="$SCORE = -1"/><xsl:otherwise><xsl:value-of select="$SCORE"/></xsl:otherwise></xsl:choose></xsl:attribute>
									</input>
								</xsl:when>
								<!-- Otherwise, show score as simple text -->
								<xsl:otherwise>
									<xsl:choose>
										<!-- If no score, or feedback set to hidden and user is unable to access hidden feedback then show blank -->
										<xsl:when test="($SCORE = -1) or ($FEEDBACK_HIDDEN)"> &#xA0;</xsl:when>
										<xsl:otherwise>
											<img height="1" width="7" src="{$SPACER}" alt="" title="" border="0"/>
											<xsl:value-of select="$SCORE"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</td>
					</tr>
				</xsl:if>
				<!-- === SUBMISSION === -->
				<!-- Show Submission section only if user has permissions to view it -->
				<xsl:if test="$viewSubmissionDetails = 'Y'">
					<tr>
						<td class="table-light-left" style="text-align:right" nowrap="nowrap" id="GBSub">
							Submission
						</td>
						<td class="table-content-right" id="GBSub">
							<xsl:call-template name="GradebookSubmission">
								<xsl:with-param name="SUBMISSION_NODE" select="$USER-GRADEBOOK-SCORE/../submission"/>
								<xsl:with-param name="TYPE" select="$TYPE"/>
							</xsl:call-template>
						</td>
					</tr>
				</xsl:if>
				<!-- === FEEDBACK === -->
				<!-- Show Feedback only if user has permissions to view it -->
				<xsl:if test="$viewFeedbackDetails = 'Y'">
					<tr>
						<td class="table-light-left" style="text-align:right" nowrap="nowrap" id="GBFeedback">
							Personalized Feedback
						</td>
						<td class="table-content-right" headers="GBFeedback">
							<xsl:call-template name="GradebookFeedback">
								<xsl:with-param name="FEEDBACK_NODE" select="$USER-GRADEBOOK-SCORE/../feedback"/>
								<xsl:with-param name="TYPE" select="$TYPE"/>
							</xsl:call-template>
						</td>
					</tr>
				</xsl:if>
				<!-- === FORM SUBMISSION BUTTONS === -->
				<tr>
					<td colspan="2" class="table-nav" style="text-align:center">
						<!-- Show Submit button only if they have something to submit (i.e. score, assignment, or feedback) -->
						<xsl:if test="($editItemScores = 'Y') or ($editSubmissionDetails = 'Y') or ($editFeedbackDetails = 'Y')">
							<input type="submit" class="uportal-button" value="Submit" title="To submit this information and return to the main view of the gradebook"/>&#032;&#032;&#032;&#032;
                    </xsl:if>
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel these changes and return to the main view of the gradebook"/>
					</td>
				</tr>
				<!-- === FOOTER NAVIGATION === -->
				<xsl:choose>
					<xsl:when test="not($editItemScores = 'N' and $editItem = 'N' and $deleteItem = 'N')">
						<xsl:call-template name="sublinks">
							<xsl:with-param name="commandDefault">submit_details</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<tr>
							<td colspan="100" class="uportal-background-med">
								<input type="hidden" name="command" value="submit_details"/>
								<input type="hidden" name="gradebook_itemID" value="{$gradebookItemID}"/>
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							</td>
						</tr>
					</xsl:otherwise>
				</xsl:choose>
			</table>
		</form>
	</xsl:template>
	<!-- END PARENT TEMPLATE -->
	
<!-- ================================= CHILD TEMPLATES ================================= -->
	
	<!-- Fills the "Assignment" select box with available column entries from the gradebook -->
	<xsl:template match="gradebook-item">
		<option value="{@id}">
			<xsl:if test="@id = $gradebookItemID">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="title"/>
		</option>
	</xsl:template>
	
	<!-- Fills the "Member" select box with enrolled members of the offering -->
	<xsl:template match="user">
		<option value="{@username}">
			<xsl:if test="@username = $username">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="last_name"/>, <xsl:value-of select="first_name"/>
		</option>
	</xsl:template>
	
	<!-- Renders content of the "Submission" section that deals with submitted files/assignments -->
	<xsl:template name="GradebookSubmission">
		<xsl:param name="SUBMISSION_NODE"/>
		<xsl:param name="TYPE"/>
		<ul>
			<!-- Check to see if there is actually a submisison node, before displaying fields from it -->
			<xsl:if test="$SUBMISSION_NODE/../submission">
				<xsl:choose>
					<xsl:when test="$TYPE = '2' and ($SUBMISSION_NODE/@submission-count &gt; 0)">
						<li>File Name: 
							<a href="{$workerActionURL}&amp;filename={$SUBMISSION_NODE/@filename}&amp;fileType=submission&amp;gradebookScoreId={$SUBMISSION_NODE/@gradebook-scoreid}" title="To access or download the submitted file" target="_blank">
								<xsl:value-of select="$SUBMISSION_NODE/@filename"/>
							</a>
						</li>
						<li>File Size: <xsl:choose>
								<!-- Show in Bytes if less than 1024 bytes -->
								<xsl:when test="$SUBMISSION_NODE/@size &lt; 1024">
									<xsl:value-of select="$SUBMISSION_NODE/@size"/> Bytes
                               </xsl:when>
								<!-- Show in Kilobytes if greater than 1 kilobyte but less than 1 megabyte -->
								<xsl:when test="($SUBMISSION_NODE/@size &gt;= 1024) and ($SUBMISSION_NODE/@size &lt; 1048576)">
									<xsl:value-of select="round(($SUBMISSION_NODE/@size) div 1024)"/> KB
                               </xsl:when>
								<!-- Show in Megabytes if greater than 1 Megabyte -->
								<xsl:when test="$SUBMISSION_NODE/@size &gt;= 1048576">
									<xsl:value-of select="round(($SUBMISSION_NODE/@size) div 1048576)"/> MB
                               </xsl:when>
								<!-- Otherwise, show nothing -->
								<xsl:otherwise>
                               </xsl:otherwise>
							</xsl:choose>
						</li>
						<li>Upload Date: <xsl:value-of select="$SUBMISSION_NODE/@date"/>
						</li>
						<li>Timestamp: <xsl:value-of select="$SUBMISSION_NODE/@time"/>
						</li>
						<li>Submission Count: 
                        <xsl:choose>
								<xsl:when test="$SUBMISSION_NODE/@submission-count &gt; 0">
									<xsl:value-of select="$SUBMISSION_NODE/@submission-count"/>
								</xsl:when>
								<xsl:otherwise>0</xsl:otherwise>
							</xsl:choose>
						</li>
					</xsl:when>
					<xsl:when test="$TYPE = '1'">
						<li>
							<a href="{$baseActionURL}?command=question_details&amp;gradebook_itemID={$gradebookItemID}&amp;username={$username}" title="To view the details of this scored assessment">
								Assessment Details 
                    			<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_active.gif" alt="To view this assignment" align="absmiddle"/>
							</a>
						</li>
						<li>Date: <xsl:value-of select="$SUBMISSION_NODE/@date"/>
						</li>
						<li>Timestamp: <xsl:value-of select="$SUBMISSION_NODE/@time"/>
						</li>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:if>
			<!-- Show File Upload option only if permitted to submit assignment -->
			<xsl:if test="$editSubmissionDetails = 'Y' and $TYPE = '2'">
				<xsl:variable name="PERMITTED_ATTEMPTS">
					<xsl:call-template name="findPermittedAttempts">
						<xsl:with-param name="MIN_ATTEMPTS"/>
						<xsl:with-param name="POSITION">1</xsl:with-param>
					</xsl:call-template>
				</xsl:variable>
				<!-- Display fields only if the number of permitted attempts is greater than 0  -->
				<xsl:if test="$PERMITTED_ATTEMPTS &gt; 0">
					<li>Attempts Permitted: <xsl:value-of select="$PERMITTED_ATTEMPTS"/>
					</li>
					<xsl:choose>
						<xsl:when test="($SUBMISSION_NODE/../submission) and ($SUBMISSION_NODE/@submission-count)">
							<xsl:choose>
								<xsl:when test="$SUBMISSION_NODE/@submission-count &lt; $PERMITTED_ATTEMPTS">
									<!-- Inline style to fix bug with Netscape & Input type= "file" - apparently due to text align other than "left" -->
									<li><label for="gbdf1">Upload New: </label><input name="submission-file" type="file" style="text-align: left;" value="" id="gbdf1"/>
									</li>
								</xsl:when>
								<xsl:otherwise>
									<li>No more submission attempts are permitted</li>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<!-- If no <submission> then 1st upload -->
						<xsl:otherwise>
							<li><label for="gbdf2">Upload New: </label><input name="submission-file" type="file" style="text-align: left;" value="" id="gbdf2"/>
							</li>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:if>
			<!-- Assuming no comments from online submission (not possible anyway in Virtuoso) -->
			<xsl:if test = "$TYPE = '2'">
			<xsl:choose>
				<xsl:when test="$editSubmissionDetails = 'Y' or $SUBMISSION_NODE/../submission">
					<li><label for="gbdta1">Comment: </label>
                    <xsl:choose>
							<!-- If permitted to edit show comments in editable field -->
							<xsl:when test="$editSubmissionDetails = 'Y'">
								<textarea name="submission-comment" cols="40" id="gbdta1">
									<xsl:value-of select="$SUBMISSION_NODE/comment"/>
								</textarea>
							</xsl:when>
							<!-- Otherwise, show as simple text -->
							<xsl:otherwise>
								<xsl:value-of select="$SUBMISSION_NODE/comment"/>
							</xsl:otherwise>
						</xsl:choose>
					</li>
				</xsl:when>
				<xsl:otherwise>
					<li>No submission information to display</li>
				</xsl:otherwise>
			</xsl:choose>
			</xsl:if>
		</ul>
	</xsl:template>
	
	<!-- Renders content of the "Feedback" section that deals with submitted files/feedback -->
	<xsl:template name="GradebookFeedback">
		<xsl:param name="FEEDBACK_NODE"/>
		<xsl:param name="TYPE"/>
		<ul>
			<!-- Check to see if there is actually a feedback node, before displaying fields from it -->
			<xsl:if test="$FEEDBACK_NODE/../feedback and not($FEEDBACK_HIDDEN)">
				<xsl:choose>
					<xsl:when test="($TYPE = '2') and ($FEEDBACK_NODE/@filename != 'null')">
						<li>File Name: <a href="{$workerActionURL}&amp;filename={$FEEDBACK_NODE/@filename}&amp;fileType=feedback&amp;gradebookScoreId={$FEEDBACK_NODE/@gradebook-scoreid}" title="To access or download the feedback file" target="_blank">
								<xsl:value-of select="$FEEDBACK_NODE/@filename"/>
							</a>
						</li>
						<li>File Size: <xsl:choose>
								<!-- Show in Bytes if less than 1024 bytes -->
								<xsl:when test="$FEEDBACK_NODE/@size &lt; 1024">
									<xsl:value-of select="$FEEDBACK_NODE/@size"/> Bytes
                               </xsl:when>
								<!-- Show in Kilobytes if greater than 1 kilobyte but less than 1 megabyte -->
								<xsl:when test="($FEEDBACK_NODE/@size &gt;= 1024) and ($FEEDBACK_NODE/@size &lt; 1048576)">
									<xsl:value-of select="round(($FEEDBACK_NODE/@size) div 1024)"/> KB
                               </xsl:when>
								<!-- Show in Megabytes if greater than 1 Megabyte -->
								<xsl:when test="$FEEDBACK_NODE/@size &gt;= 1048576">
									<xsl:value-of select="round(($FEEDBACK_NODE/@size) div 1048576)"/> MB
                               </xsl:when>
								<!-- Otherwise, show nothing -->
								<xsl:otherwise>
                               </xsl:otherwise>
							</xsl:choose>
						</li>
						<li>Upload Date: <xsl:value-of select="$FEEDBACK_NODE/@date"/>
						</li>
						<li>Timestamp: <xsl:value-of select="$FEEDBACK_NODE/@time"/>
						</li>
						<!--        <li>Comment: <xsl:value-of select="comment" /></li> -->
					</xsl:when>
					<xsl:when test="$TYPE = '1' and $VIEWPFEEDBACK = 'Y'">
						<xsl:if test = "$FEEDBACK_NODE/@filename!='' and $FEEDBACK_NODE/@filename!='null'">
							<li>
								<!-- PERSONALIZED FEEDBACK LINK -->
								<a href="{$FEEDBACK_NODE/@filename}" title="View Personalized Feedback for this scored assessment." onmouseover="swapImage('gbViewPFeeback','channel_view_active.gif')" onmouseout="swapImage('gbViewPFeeback','channel_view_base.gif')" target="_blank">
									Personalized Feedback
									<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
									<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to view Personalized Feedback for this scored assessment" title="'View' icon linking to view Personalized Feedback for this scored assessment" name="gbViewPFeeback" id="gbViewPFeeback" align="absmiddle"/>
								</a>
							</li>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise/>
				</xsl:choose>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="$editFeedbackDetails = 'Y' or (($FEEDBACK_NODE/../feedback) and not($FEEDBACK_HIDDEN))">
					<li><label for="gbdta2">Comment: </label>
                    <xsl:choose>
							<!-- If permitted to edit show comments in editable field -->
							<xsl:when test="$editFeedbackDetails = 'Y'">
								<textarea name="feedback-comment" cols="40" id="gbdta2">
									<xsl:choose>
										<xsl:when test="$FEEDBACK_NODE/comment = 'null'"/>
										<xsl:otherwise>
											<xsl:value-of select="$FEEDBACK_NODE/comment"/>
										</xsl:otherwise>
									</xsl:choose>
								</textarea>
							</xsl:when>
							<!-- Otherwise, show as simple text -->
							<xsl:otherwise>
								<xsl:choose>
									<xsl:when test="$FEEDBACK_NODE/comment = 'null'"/>
									<xsl:otherwise>
										<xsl:value-of select="$FEEDBACK_NODE/comment"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:otherwise>
						</xsl:choose>
					</li>
				</xsl:when>
				<xsl:otherwise>
					<li>No feedback is available</li>
				</xsl:otherwise>
			</xsl:choose>
			<!-- Show File Upload option only if permitted to submit feedback -->
			<xsl:if test="$editFeedbackDetails = 'Y' and $TYPE = '2'">
				<!-- Inline style to fix bug with Netscape & Input type= "file" - apparently due to text align other than "left" -->
				<li><label for="gbdf3">Upload New: </label><input name="feedback-file" type="file" style="text-align: left;" value="" id="gbdf3"/>
				</li>
			</xsl:if>
		</ul>
	</xsl:template>
	
	<!-- Checks for number of submission attempts and how many submissions have already been made -->
	<xsl:template name="findPermittedAttempts">
		<xsl:param name="MIN_ATTEMPTS"/>
		<xsl:param name="POSITION"/>
		<xsl:choose>
			<xsl:when test="$POSITION &lt;= count($GRADEBOOK-ITEM-POINTER/activation)">
				<!--<xsl:variable name = "CURRENT_ACTIVATION_ATTEMPTS" 
                        select = "$GRADEBOOK-ITEM-POINTER/activation[number($POSITION)]/user-list/user[username = $username]/../../attributes/attribute[@name = 'attempts']/value" /> -->
				<xsl:variable name="CURRENT_ACTIVATION_ATTEMPTS">
					<xsl:choose>
						<xsl:when test="$GRADEBOOK-ITEM-POINTER/activation[number($POSITION)]/user-list/@allusers = 'true' or
                                    $GRADEBOOK-ITEM-POINTER/activation[number($POSITION)]/user-list/user/username = $username">
							<xsl:value-of select="$GRADEBOOK-ITEM-POINTER/activation[number($POSITION)]/attributes/attribute[@name = 'attempts']/value"/>
						</xsl:when>
						<xsl:otherwise/>
					</xsl:choose>
				</xsl:variable>
				<!--<xsl:value-of select="$GRADEBOOK-ITEM-POINTER/activation[$POSITION]/user-list/user[username = $username]/../../attributes/attribute[@name = 'attempts']/value" />
            <xsl:variable name = "CURRENT_ACTIVATION_ATTEMPTS" select = "1" /> -->
				<!--,<xsl:value-of select="$POSITION" />=<xsl:value-of select="$CURRENT_ACTIVATION_ATTEMPTS" />=<xsl:value-of select="$GRADEBOOK-ITEM-POINTER/activation[2]/user-list/user[username = $username]/../../attributes/attribute[@name = 'attempts']/value" />, -->
				<xsl:choose>
					<xsl:when test="($MIN_ATTEMPTS = '') or ($MIN_ATTEMPTS &gt; number($CURRENT_ACTIVATION_ATTEMPTS))">
						<xsl:call-template name="findPermittedAttempts">
							<xsl:with-param name="MIN_ATTEMPTS">
								<xsl:value-of select="$CURRENT_ACTIVATION_ATTEMPTS"/>
							</xsl:with-param>
							<xsl:with-param name="POSITION">
								<xsl:value-of select="$POSITION+1"/>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="findPermittedAttempts">
							<xsl:with-param name="MIN_ATTEMPTS">
								<xsl:value-of select="$MIN_ATTEMPTS"/>
							</xsl:with-param>
							<xsl:with-param name="POSITION">
								<xsl:value-of select="$POSITION+1"/>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$MIN_ATTEMPTS"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
</xsl:stylesheet>
