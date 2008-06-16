<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" /><!-- Include Files -->
<xsl:include href="common.xsl"/>
<xsl:param name="associationID">2</xsl:param>
<xsl:param name="associationTitle" select="/gradebooks/assessment[@id=$associationID]/title" />
<xsl:param name="onlineAssessmentAvailable" select="false"/>
<xsl:variable name = "associatedAssessment" select = "/gradebooks/assessment" />
<xsl:template match="/">
    <xsl:call-template name="addEditItemJs"/>
    <xsl:call-template name = "links" />
   
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>   
        <parameter name="workerActionURL"><xsl:value-of select="$workerActionURL" /></parameter>   
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>   
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>   
        <parameter name="associationID"><xsl:value-of select="$associationID" /></parameter>   
        <parameter name="positionID"><xsl:value-of select="$positionID" /></parameter>   
        <parameter name="username"><xsl:value-of select="$username" /></parameter>   
        <parameter name="currentMonth"><xsl:value-of select="$currentMonth" /></parameter>   
        <parameter name="currentDay"><xsl:value-of select="$currentDay" /></parameter>   
        <parameter name="currentYear"><xsl:value-of select="$currentYear" /></parameter>   
        <parameter name="addItem"><xsl:value-of select="$addItem" /></parameter>   
        <parameter name="editItem"><xsl:value-of select="$editItem" /></parameter>   
        <parameter name="deleteItem"><xsl:value-of select="$deleteItem" /></parameter>   
        <parameter name="viewAll"><xsl:value-of select="$viewAll" /></parameter>   
        <parameter name="editItemScores"><xsl:value-of select="$editItemScores" /></parameter>   
        <parameter name="editAllItemScores"><xsl:value-of select="$editAllItemScores" /></parameter>   
        <parameter name="editWeighting"><xsl:value-of select="$editWeighting" /></parameter>   
        <parameter name="accessDetails"><xsl:value-of select="$accessDetails" /></parameter>   
        <parameter name="viewSubmissionDetails"><xsl:value-of select="$viewSubmissionDetails" /></parameter>   
        <parameter name="viewFeedbackDetails"><xsl:value-of select="$viewFeedbackDetails" /></parameter>   
        <parameter name="editSubmissionDetails"><xsl:value-of select="$editSubmissionDetails" /></parameter>   
        <parameter name="editFeedbackDetails"><xsl:value-of select="$editFeedbackDetails" /></parameter>   
        <parameter name="addActivation"><xsl:value-of select="$addActivation" /></parameter>   
        <parameter name="deleteActivation"><xsl:value-of select="$deleteActivation" /></parameter>   
        <parameter name="viewAllUserActivations"><xsl:value-of select="$viewAllUserActivations" /></parameter>   
        <parameter name="viewActivationDetails"><xsl:value-of select="$viewActivationDetails" /></parameter>   
        <parameter name="accessHiddenFeedback"><xsl:value-of select="$accessHiddenFeedback" /></parameter>   
        <parameter name="associationTitle"><xsl:value-of select="$associationTitle" /></parameter>   
        <parameter name="onlineAssessmentAvailable"><xsl:value-of select="$onlineAssessmentAvailable" /></parameter>   
    </textarea> -->
    <form onSubmit="return checkSubmit()" name="gradebookForm" action="{$baseActionURL}" method="post">
        <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
        <input type="hidden" name="item_type" value="{$associationID}"/>
        <table cellpadding="0" cellspacing="0" border="0" width="100%">            
            <tr>                
                <th class="th-top-single" colspan="2" id="GBAddCol">Add Column</th>
            </tr>
     
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap" id="AssessAssoc">
                    <xsl:choose>
                        <xsl:when test="$onlineAssessmentAvailable = 'true' or $current_command = 'setAssociation'">
                            <a href="javascript:document.gradebookForm.submit();" title="To change this columns assessment association (i.e. None, or specific online assessments)" 
                            onmouseover="swapImage('gbEditThisAssocImage','channel_edit_active.gif')" 
                            onmouseout="swapImage('gbEditThisAssocImage','channel_edit_base.gif')" 
                            onclick="document.gradebookForm.command.value='associate'">
                            Assessment<br/>
                            Association
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img height="16" width="16" border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" 
                                alt="'Edit' icon: to change this columns assessment association (i.e. None, or specific online assessments)" 
                                title="'Edit' icon: to change this columns assessment association (i.e. None, or specific online assessments)" align="absmiddle" 
                                name="gbEditThisAssocImage" id="gbEditThisAssocImage"/>
                            </a>
                        </xsl:when>
                      
                        <xsl:otherwise>Assessment<br/>Association</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td class="table-content-right" width="100%" headers="AssessAssoc">
                <!--<xsl:choose>
                    <xsl:when test="count(/gradebooks/available_assessments/assessment) &gt; 0">
                        <select name="item_type" id="gbagbis1">
                            <xsl:attribute  name = "onchange" >onlineAsmtAssociationCheck(this)</xsl:attribute>
                            <option value="2" selected="selected">None (or N/A)</option>
                            <xsl:for-each select = "/gradebooks/available_assessments/assessment">
                            <option value="{@id}"><xsl:value-of select="./title" /></option>
                            </xsl:for-each>
                        </select>
                    </xsl:when>
                  
                    <xsl:otherwise>
                    <a href="javascript:alert('No curriculum has been associated with this offering that has supported online assessments.');void(null);" title="No curriculum has been associated with this offering that has supported online assessments."
                    onmouseover="swapImage('gbAssessmentAssociationHelpImage','channel_help_active.gif')" 
                    onmouseout="swapImage('gbAssessmentAssociationHelpImage','channel_help_base.gif')">
                        None
                        <img height="1" width="3" src="{$SPACER}"
                        alt="" border="0"/>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif"
                        alt="No curriculum has been associated with this offering that has supported online assessments." align="absmiddle" 
                        title="No curriculum has been associated with this offering that has supported online assessments."
                        name="gbAssessmentAssociationHelpImage" id="gbAssessmentAssociationHelpImage"/>
                    </a>
                    <input type="hidden" name="item_type" value="2"/>
                    </xsl:otherwise>
                </xsl:choose> -->
                    <xsl:choose>
                        <xsl:when test="$associationID!='2'"><xsl:value-of select="$associationTitle" /></xsl:when>
                      
                        <xsl:otherwise>None (or N/A)</xsl:otherwise>
                    </xsl:choose>
                    
                    <input type="hidden" name="associationID">
                        <xsl:choose>
                            <xsl:when test="$associationID='2'">
                                <xsl:attribute  name = "value" >null</xsl:attribute>
                            </xsl:when>
                          
                            <xsl:otherwise>
                                <xsl:attribute  name = "value" ><xsl:value-of select="$associationID" /></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </input>
                </td>
            </tr>

            <tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                    <label for="gbagbit1">Column Name</label>
                </td>
                <td class="table-content-right" width="100%">
                    <input type="text" class="text" size="55" maxlength="254" name="item_name" id="gbagbit1">
                        <xsl:if test = "$associationTitle!=''">
                            <xsl:attribute  name = "value" ><xsl:value-of select="$associationTitle" /></xsl:attribute>
                        </xsl:if>
                    </input>
                </td>
            </tr>
            
            <!--<tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">Column Type</td>
                <td class="table-content-right" width="100%"><select name="item_type" onchange="">
                <option value="1">Online Assessment</option><option selected="selected" value="2">Other (Instructor Graded Assignments, Exams, Quizes, etc.)</option>
                </select></td>
            </tr> -->
            
            <tr>
                <td class="table-light-left" style="text-align=right" width="90" id="makeVis">Make Personalized Feedback Visible</td>
                <td class="table-content-right" headers="makeVis">
                    <input type="radio" class="radio" value="yes" name="feedback" checked="checked" id="gbagbir1"/><label for="gbagbir1">&#160;Yes</label>&#160;
                    <input type="radio" class="radio" value="no" name="feedback" id="gbagbir2"/><label for="gbagbir2">&#160;No</label>
                </td>
            </tr>
            
            <!--<tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">Link Type</td>
                <td class="table-content-right" width="100%"><select name="linkType" onchange="">
                <option selected="selected">Link to URL</option></select></td>
            </tr> -->
            
            <!--<tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">Link to URL</td>
                <td class="table-content-right" width="100%"><input class="text" type="text" name="uri" size="45" maxlength="255"/></td>
            </tr> -->
            
            <!--<tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                    <label for="gbagbit2">Minimum Score</label>
                </td>
                <td class="table-content-right" width="100%"><input type="text" class="text" name="min_score" size="3" maxlength="3" id="gbagbit2"/></td>
            </tr> -->
            
            <!--<tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                    <label for="gbagbit3">Maximum Score</label>
                </td>
                <td class="table-content-right" width="100%">
                    <input type="hidden" name="min_score" value="0"/>
                    <input type="text" class="text" name="max_score" size="3" maxlength="3" id="gbagbit3"/>
                </td>
            </tr> -->
            <tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                    <label for="gbagbit3">Maximum Score</label>
                </td>
                <td class="table-content-right" width="100%">
                    <input type="hidden" name="min_score" value="0"/>
                    <xsl:choose>
                        <xsl:when test="$associationID != 2">
                            <input type="text" class="text-disabled" name="max_score" size="3" maxlength="3" id="gbagbit3" readonly="readonly">
                                <xsl:if test = "$associatedAssessment">
                                    <xsl:attribute  name = "value" >
                                        <xsl:value-of select="$associatedAssessment/form/attributes/attribute[@name='maxScore']/value" />
                                    </xsl:attribute>
                                </xsl:if>
                               </input>
                        </xsl:when>
                      
                        <xsl:otherwise>
                    <input type="text" class="text" name="max_score" size="3" maxlength="3" value="" id="gbagbit3"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>

            <tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                    <label for="gbagbit4">Column Weight</label>
                </td>
                <td class="table-content-right" width="100%"><input type="text" class="text" name="weight" size="3" maxlength="3" value="0" id="gbagbit4"/></td>
            </tr>            <tr>
                <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                    <label for="gbagbis2">Position</label>
                </td>
                <td class="table-content-right" width="100%">
                <select name="position" id="gbagbis2">
                       <option value="0">
                       <xsl:if test="count(//gradebooks/gradebook-item) = 0" >
						   <xsl:attribute name="selected">selected</xsl:attribute>
				       </xsl:if>
				       <xsl:text>At beginning</xsl:text>
                       </option>
                    <xsl:apply-templates select="./gradebooks/gradebook-item"/>                                       
                </select>
                </td>
            </tr>
                        <tr>
                <td colspan="2" class="table-nav-gradebook" style="text-align:center">
                    <table>
                        <tr>
                            <td><input type="submit" class="uportal-button" value="Submit" title="To add this column and return to the main view of the gradebook"/></td>
                            <xsl:if test = "$addActivation = 'Y'">
                                <td><input type="submit" class="uportal-button" value="Submit and Activate" 
                                            title="To add this column and go to the add activation view of the gradebook"
                                            onclick="document.gradebookForm.command.value = 'insert_activate';"/>
                                </td>
                            </xsl:if>
                            <td><input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel adding this column and return to the main view of the gradebook"/></td>
                        </tr>
                    </table>
                   </td>
            </tr>            
            <xsl:call-template name = "sublinks" >
                  <xsl:with-param name="commandDefault">insert</xsl:with-param>
            </xsl:call-template>
        </table>
    </form>
    </xsl:template>
    <xsl:template match="gradebook-item">
		<option value="{@position + 1}">
		<xsl:if test="@position + 1 = count(//gradebooks/gradebook-item)" >					
			<xsl:attribute name="selected">selected</xsl:attribute>
		</xsl:if>
		<xsl:text>After </xsl:text><xsl:value-of select="title"/>
		</option>
    </xsl:template>
</xsl:stylesheet>
