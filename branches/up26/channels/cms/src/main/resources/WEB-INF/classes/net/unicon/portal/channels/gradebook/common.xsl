<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- Include -->
	<xsl:import href="../global/global.xsl"/>
	<xsl:output method="html" indent="yes"/>
	<!-- Non-permission Parameters -->
	<xsl:param name="baseActionURL">no parameter passed</xsl:param>
	<xsl:param name="workerActionURL">no parameter passed</xsl:param>
	<xsl:param name="targetChannel">no parameter passed</xsl:param>
	<xsl:param name="current_command">main</xsl:param>
	<xsl:param name="gradebookItemID"/>
	<xsl:param name="activation_id"/>
	<xsl:param name="positionID">no parameter passed</xsl:param>
	<xsl:param name="username"></xsl:param>
	<xsl:param name="currentMonth">8</xsl:param>
	<xsl:param name="currentDay">6</xsl:param>
	<xsl:param name="currentYear">2002</xsl:param>
	<!-- Permissions Parameters -->
	<xsl:param name="addItem">N</xsl:param>
	<xsl:param name="editItem">N</xsl:param>
	<xsl:param name="deleteItem">N</xsl:param>
	<xsl:param name="viewAll">N</xsl:param>
	<xsl:param name="editItemScores">N</xsl:param>
	<xsl:param name="editAllItemScores">N</xsl:param>
	<xsl:param name="editWeighting">N</xsl:param>
	<xsl:param name="accessDetails">no parameter passed</xsl:param>
	<xsl:param name="viewSubmissionDetails">no parameter passed</xsl:param>
	<xsl:param name="viewFeedbackDetails">no parameter passed</xsl:param>
	<xsl:param name="editSubmissionDetails">no parameter passed</xsl:param>
	<xsl:param name="editFeedbackDetails">no parameter passed</xsl:param>
	<xsl:param name="addActivation">Y</xsl:param>
	<xsl:param name="deleteActivation">Y</xsl:param>
	<xsl:param name="viewAllUserActivations">Y</xsl:param>
	<xsl:param name="viewActivationDetails">Y</xsl:param>
	<xsl:param name="accessHiddenFeedback">Y</xsl:param>
	<!-- Common -->
<!-- Validation --> 
    <xsl:template name="autoFormJS"> 
       <script language="JavaScript" type="text/javascript" src="javascript/GradebookChannel/autoForm.js"></script> 
       </xsl:template> 
	<xsl:template name="mainJS">
		<script language="JavaScript1.2" type="text/javascript" src="javascript/GradebookChannel/main.js"></script>
	</xsl:template>
	<xsl:template name="addEditItemJs">
		<script language="JavaScript1.2" type="text/javascript" src="javascript/GradebookChannel/addEditItem.js"></script>
	</xsl:template>
	<xsl:template name="links">
		<!-- UniAcc: Layout Table -->
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td class="views-title">
					<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" alt="Icon of tool-tip indicating the channel options section" title="Icon of tool-tip indicating the channel options section" align="absmiddle"/>
				</td>
				<td class="views" valign="middle" height="26" width="100%">
					<xsl:choose>
						<xsl:when test="$current_command='main' and $catCurrentCommand='page'">
            				Scores
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="Selected 'View' icon indicating that all gradebook scores are currently being displayed" title="Selected 'View' icon indicating that all gradebook scores are currently being displayed" align="absmiddle"/>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						</xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}" title="To view all gradebook scores" onmouseover="swapImage('gbViewAllScoresImage','channel_view_active.gif')" onmouseout="swapImage('gbViewAllScoresImage','channel_view_base.gif')">
								Scores
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to view all gradebook scores" title="'View' icon linking to view all gradebook scores" name="gbViewAllScoresImage" id="gbViewAllScoresImage" align="absmiddle"/>
							</a>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="$current_command = 'editAll'">
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif" alt="Selected 'Edit' icon: Currently editing all gradebook scores" title="Selected 'Edit' icon: Currently editing all gradebook scores" align="absmiddle"/>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						</xsl:when>
						<xsl:when test="$editAllItemScores = 'N'">
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit' icon indicating insufficient permissions to edit all gradebook scores" title="Inactive 'Edit' icon indicating insufficient permissions to edit all gradebook scores" align="absmiddle"/>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						</xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}?command=editscores" title="To edit all gradebook scores" onmouseover="swapImage('gbEditAllScoresImage','channel_edit_active.gif')" onmouseout="swapImage('gbEditAllScoresImage','channel_edit_base.gif')">
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon linking to all gradebook scores" title="'Edit' icon linking to all gradebook scores" align="absmiddle" name="gbEditAllScoresImage" id="gbEditAllScoresImage"/>
							</a>
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="$current_command = 'export'">
							<a href="javascript:generateExportFile();void(null);" title="To export data from the gradebook as a delimited file">
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_selected.gif" alt="'Export' icon: Export data from the gradebook as a delimited file" title="'Export' icon: Export data from the gradebook as a delimited file" align="absmiddle"/>
							</a> | 
    					</xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}?command=export" title="To export data from the gradebook as a delimited file" onmouseover="swapImage('gbExportAllScoresImage','channel_export_active.gif')" onmouseout="swapImage('gbExportAllScoresImage','channel_export_base.gif')">
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_base.gif" alt="'Export' icon: Export data from the gradebook as a delimited file" title="'Export' icon: Export data from the gradebook as a delimited file" align="absmiddle" name="gbExportAllScoresImage" id="gbExportAllScoresImage"/>
							</a> | 
    					</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="$current_command = 'all_activations'">
            				All Activations
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="Selected 'View' icon indicating that all activations for this offering are currently in view" title="Selected 'View' icon indicating that all activations for this offering are currently in view" align="absmiddle"/> | 
    </xsl:when>
						<!--      
      <xsl:when test = "$addItem = 'N'">
            All Activations<img height="1" width="3" src=
    "{$SPACER}"
     alt="" border="0"/><img border="0" src=
    "{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif"
     alt="" align="absmiddle"/> | 
      </xsl:when>
   -->
						<xsl:otherwise>
							<a href="{$baseActionURL}?command=all_activations" title="To view all activations for this offering" onmouseover="swapImage('gbViewAllActivationsImage','channel_view_active.gif')" onmouseout="swapImage('gbViewAllActivationsImage','channel_view_base.gif')">
								Activations
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to view of all activations for this offering" title="'View' icon linking to view of all activations for this offering" align="absmiddle" name="gbViewAllActivationsImage" id="gbViewAllActivationsImage"/>
							</a> | 
    </xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="$current_command = 'add'">
            				Column
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" alt="Selected 'Add' icon: Currently adding a gradebook column" title="Selected 'Add' icon: Currently adding a gradebook column" align="absmiddle"/> | 
    </xsl:when>
						<xsl:when test="$addItem = 'N'">
            				Column
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif" alt="Inactive 'Add' icon indicating insufficient permissions to add a gradebook column" title="Inactive 'Add' icon indicating insufficient permissions to add a gradebook column" align="absmiddle"/> | 
      </xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}?command=add" title="To add a gradebook column" onmouseover="swapImage('gbAddColumnImage','channel_add_active.gif')" onmouseout="swapImage('gbAddColumnImage','channel_add_base.gif')">
								Column
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' icon: Add a gradebook column to this offering" title="'Add' icon: Add a gradebook column to this offering" align="absmiddle" name="gbAddColumnImage" id="gbAddColumnImage"/>
							</a> | 
    					</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
						<xsl:when test="$current_command = 'weighting'">
            				Weighting
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif" alt="Selected 'Edit' icon: Currently editing all column weights" title="Selected 'Edit' icon: Currently editing all column weights" align="absmiddle"/> |
    </xsl:when>
						<xsl:when test="$editWeighting = 'N'">
				            Weighting
							<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit' icon indicating insufficient permissions to edit column weight" title="Inactive 'Edit' icon indicating insufficient permissions to edit column weight" align="absmiddle"/> |
    					</xsl:when>
						<xsl:otherwise>
							<a href="{$baseActionURL}?command=weighting" title="To edit all column weights" onmouseover="swapImage('gbEditWeightingImage','channel_edit_active.gif')" onmouseout="swapImage('gbEditWeightingImage','channel_edit_base.gif')">
								Weighting
								<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon links to edit all column weights" title="'Edit' icon links to edit all column weights" align="absmiddle" name="gbEditWeightingImage" id="gbEditWeightingImage"/>
							</a> | 
    					</xsl:otherwise>
					</xsl:choose>
					<!-- <a href="{$baseActionURL}"> -->
					Grading
					<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
					<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit' icon indicating that the view to edit grading criteria is currently unavailable" title="Inactive 'Edit' icon indicating that the view to edit grading criteria is currently unavailable" align="absmiddle"/>
					<!-- </a> -->
				</td>
			</tr>
		</table>
	</xsl:template>
	<!-- Dependent Navigational links -->
	<xsl:template name="sublinks">
		<xsl:param name="commandDefault"/>

	<xsl:choose>
		<xsl:when test="count(gradebooks/gradebook-item) &gt; 0">
				<tr>
				<td colspan="100">
					<!-- UniAcc: Layout Table -->
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<tr>
							<td class="table-content-left-bottom">
								<input type="hidden" name="command" value="{$commandDefault}"/>
								<input type="hidden" name="gradebook_itemID" value="{$gradebookItemID}"/> 
	     							For
									<select name="gradebook_itemID_selected" title="Select gradebook column">
									<xsl:if test="($current_command = 'export') or ($current_command = 'main')">
										<xsl:attribute name="onchange">
							                gradebookForm.position[this.selectedIndex].checked = true
							            </xsl:attribute>
									</xsl:if>
									<xsl:for-each select="gradebooks/gradebook-item">
										<option value="{@id}">
											<xsl:if test="@id = $gradebookItemID">
												<xsl:attribute name="selected">
							                        selected
							                    </xsl:attribute>
											</xsl:if>
											<xsl:value-of select="title"/>
										</option>
									</xsl:for-each>
								</select>
							</td>
							<td class="table-content-right-bottom" valign="middle" height="26">
								<xsl:choose>
									<xsl:when test="$viewSubmissionDetails = 'Y'">
										<a href="javascript:document.gradebookForm.command.value='all_question_details';document.gradebookForm.gradebook_itemID.value=document.gradebookForm.gradebook_itemID_selected.options[document.gradebookForm.gradebook_itemID_selected.selectedIndex].value;document.gradebookForm.submit();" title="To view the assignment or assessment submissions for the selected column" onmouseover="swapImage('gbViewAllSubmissionsImage','channel_view_active.gif')" onmouseout="swapImage('gbViewAllSubmissionsImage','channel_view_base.gif')">
											Submissions
											<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
											<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to edit the scores for the selected column" title="'View' icon linking to edit the scores for the selected column" align="absmiddle" name="gbViewAllSubmissionsImage" id="gbViewAllSubmissionsImage"/>
										</a> | 
	        						</xsl:when>
									<xsl:otherwise>
							            Submissions
							            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif" alt="Inactive 'View' icon indicating insufficient permissions to edit the scores for a column" title="Inactive 'View' icon indicating insufficient permissions to edit the scores for a column" align="absmiddle"/> | 
	        						</xsl:otherwise>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="$editItemScores = 'Y'">
										<a href="javascript:document.gradebookForm.command.value='show only';document.gradebookForm.gradebook_itemID.value=document.gradebookForm.gradebook_itemID_selected.options[document.gradebookForm.gradebook_itemID_selected.selectedIndex].value;document.gradebookForm.submit();" title="To edit the scores for the selected column" onmouseover="swapImage('gbEditColumnScoresImage','channel_edit_active.gif')" onmouseout="swapImage('gbEditColumnScoresImage','channel_edit_base.gif')">
											Column Scores
											<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
											<!--<xsl:choose>
	                <xsl:when test="$current_command = 'show_only'">
	                        <img border="0" src=
	                    "{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif"
	                    alt="To edit the scores for the selected column" title="To edit the scores for the selected column" align="absmiddle"
	                     name="gbEditColumnScoresImage" id="gbEditColumnScoresImage"/>
	                </xsl:when>
	                <xsl:otherwise>
	                        <img border="0" src=
	                    "{$CONTROLS_IMAGE_PATH}/channel_edit_active.gif"
	                    alt="To edit the scores for the selected column" title="To edit the scores for the selected column" align="absmiddle"/>
	                </xsl:otherwise>
	            </xsl:choose> -->
											<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon linking to edit the scores for the selected column" title="'Edit' icon linking to edit the scores for the selected column" align="absmiddle" name="gbEditColumnScoresImage" id="gbEditColumnScoresImage"/>
										</a> | 
	        						</xsl:when>
									<xsl:otherwise>
							            Column Scores
							            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit' icon indicating insufficient permissions to edit the scores for a column" title="Inactive 'Edit' icon indicating insufficient permissions to edit the scores for a column" align="absmiddle"/> | 
	        						</xsl:otherwise>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="$addActivation = 'Y'">
										<a href="javascript:document.gradebookForm.command.value='activation';document.gradebookForm.gradebook_itemID.value=document.gradebookForm.gradebook_itemID_selected.options[document.gradebookForm.gradebook_itemID_selected.selectedIndex].value;document.gradebookForm.submit();" title="To add an activation for the selected column" onmouseover="swapImage('gbAddActivationImage','channel_add_active.gif')" onmouseout="swapImage('gbAddActivationImage','channel_add_base.gif')">
											Activation
							               <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
											<!--<xsl:choose>
	                <xsl:when test="$current_command = 'show_only'">
	                        <img border="0" src=
	                    "{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif"
	                    alt="To add an activation for the selected column" title="To add an activation for the selected column" align="absmiddle"/>
	                </xsl:when>
	                <xsl:otherwise>
	                        <img border="0" src=
	                    "{$CONTROLS_IMAGE_PATH}/channel_add_active.gif"
	                    alt="To add an activation for the selected column" title="To add an activation for the selected column" align="absmiddle"/>
	                </xsl:otherwise>
	            </xsl:choose> -->
											<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' icon linking to add an activation for the selected column" title="'Add' icon linking to add an activation for the selected column" align="absmiddle" name="gbAddActivationImage" id="gbAddActivationImage"/>
										</a> | 
	        						</xsl:when>
									<xsl:otherwise>
							            Activation
							            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif" alt="Inactive 'Add' icon indicating insufficient permissions to add assignment activations" title="Not permitted to add assignment activations" align="absmiddle"/> | 
	        						</xsl:otherwise>
								</xsl:choose>
								<!-- <img height="1" width="3" src=
	    "{$SPACER}"
	    alt="" border="0"/>
	
	    <a href="javascript:document.gradebookForm.command.value = 'details'; document.gradebookForm.submit();" title="To submit assignments and feedback, and view details">
	    <img border="0" src=
	    "{$CONTROLS_IMAGE_PATH}/channel_view_active.gif"
	    alt="To submit assignments and feedback, and view details" align="absmiddle"/>
	    </a> -->
								<xsl:choose>
									<xsl:when test="$editItem = 'Y'">
										<a href="javascript:document.gradebookForm.command.value='edit';document.gradebookForm.gradebook_itemID.value=document.gradebookForm.gradebook_itemID_selected.options[document.gradebookForm.gradebook_itemID_selected.selectedIndex].value;document.gradebookForm.submit();" title="To edit the information for the selected column item" onmouseover="swapImage('gbEditColumnImage','channel_edit_active.gif')" onmouseout="swapImage('gbEditColumnImage','channel_edit_base.gif')">
	            							Column
											<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
											<!--<xsl:choose>
	                <xsl:when test="$current_command = 'edit_gradebookitem'">
	                        <img border="0" src=
	            "{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif"
	            alt="To edit the information for the selected column item" title="To edit the information for the selected column item" align="absmiddle"/>
	                </xsl:when>
	                <xsl:otherwise>
	                        <img border="0" src=
	            "{$CONTROLS_IMAGE_PATH}/channel_edit_active.gif"
	            alt="To edit the information for the selected column item" title="To edit the information for the selected column item" align="absmiddle"/>
	                </xsl:otherwise>
	            </xsl:choose> -->
											<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon linking to edit the information for the selected column item" title="'Edit' icon linking to edit the information for the selected column item" align="absmiddle" name="gbEditColumnImage" id="gbEditColumnImage"/>
										</a>
									</xsl:when>
									<xsl:otherwise>
	            						Column
										<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="Inactive 'Edit' icon indicating insufficient permissions to edit the information for a column item" title="Inactive 'Edit' icon indicating insufficient permissions to edit the information for a column item" align="absmiddle"/>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="$deleteItem = 'Y'">
										<a href="javascript:document.gradebookForm.command.value='delete';document.gradebookForm.gradebook_itemID.value=document.gradebookForm.gradebook_itemID_selected.options[document.gradebookForm.gradebook_itemID_selected.selectedIndex].value;document.gradebookForm.submit();" title="To delete the selected column item" onmouseover="swapImage('gbDeleteColumnImage','channel_delete_active.gif')" onmouseout="swapImage('gbDeleteColumnImage','channel_delete_base.gif')">
											<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
											<!--<xsl:choose>
	                <xsl:when test="$current_command = 'delete_gradebookitem'">
	                        <img border="0" src=
	            "{$CONTROLS_IMAGE_PATH}/channel_delete_selected.gif"
	            alt="To delete the selected column item" title="To delete the selected column item" align="absmiddle"/>
	                </xsl:when>
	                <xsl:otherwise>
	                        <img border="0" src=
	            "{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif"
	            alt="To delete the selected column item" title="To delete the selected column item" align="absmiddle"/>
	                </xsl:otherwise>
	            </xsl:choose> -->
											<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Delete' icon linking to delete the selected column item" title="'Delete' icon linking to delete the selected column item" align="absmiddle" name="gbDeleteColumnImage" id="gbDeleteColumnImage"/>
										</a>
									</xsl:when>
									<xsl:otherwise>
										<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif" alt="Inactive 'Delete' icon indicating insufficient permissions to delete a column item" title="Not permitted to delete a column item" align="absmiddle"/>
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</xsl:when>
	  
		<xsl:otherwise>
				<tr>
				<td colspan="100">
					<input type="hidden" name="command" value="{$commandDefault}"/>
					<input type="hidden" name="gradebook_itemID" value="{$gradebookItemID}"/> 
				</td>
				</tr>
		 <!-- Don't show the bottom options -->
		</xsl:otherwise>
	</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
