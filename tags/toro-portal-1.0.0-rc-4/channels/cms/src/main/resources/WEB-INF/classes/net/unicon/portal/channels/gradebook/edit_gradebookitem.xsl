<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:param name="associationID" select="/gradebooks/gradebook-item[@id = $gradebookItemID]/@association"/>
    <xsl:param name="associationTitle" select="/gradebooks/assessment[@id=$associationID]/title" />
    <!-- Non-permission Parameters -->
    <xsl:param name="onlineAssessmentAvailable" select="false"/>
    <!--<xsl:param name="gradebookItemID">no parameter passed</xsl:param> -->
    <xsl:variable name="GRADEBOOK-ITEM-POINTER" select="/gradebooks/gradebook-item[@id = $gradebookItemID]"/>
    <xsl:variable name="ASSOCIATED_EXAM_ID" select="$GRADEBOOK-ITEM-POINTER/@association"/>
    <xsl:template match="/">
    <!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>   
        <parameter name="workerActionURL"><xsl:value-of select="$workerActionURL" /></parameter>   
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>   
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>   
        <parameter name="activation_id"><xsl:value-of select="$activation_id" /></parameter>   
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
    </textarea> 
    -->
        <!--<xsl:apply-templates select = "gradebooks" /> -->
        <xsl:call-template name="addEditItemJs"/>
        <xsl:call-template name="links"/>

        <form onSubmit="return checkSubmit()" name="gradebookForm" action="{$baseActionURL}" method="post">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="item_type">
                <xsl:choose>
                    <xsl:when test="$ASSOCIATED_EXAM_ID='null'">
                        <xsl:attribute  name = "value" >2</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute  name = "value" ><xsl:value-of select="$ASSOCIATED_EXAM_ID" /></xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </input>
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top-single" colspan="2" id="GBEditCol">
                        Edit Column
                    </th>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                        <label for="gbegbit1">Column Name</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="text" class="text" size="55" maxlength="254" name="item_name" value="{$GRADEBOOK-ITEM-POINTER/title}" id="gbegbit1"/>
                    </td>
                </tr>
                <xsl:if test="$onlineAssessmentAvailable = 'true'">
                <tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap" id="AssessAssoc">
                        <label for="gbegbis1">Assessment<br/>Association</label>
                    </td>
                    <td class="table-content-right" width="100%" headers="AssessAssoc">
                        <!--
                        <a href="javascript:document.gradebookForm.submit();" title="To change this columns assessment association (i.e. None, or specific online assessments)" 
                            onmouseover="swapImage('gbEditThisAssocImage','channel_edit_active.gif')" 
                            onmouseout="swapImage('gbEditThisAssocImage','channel_edit_base.gif')" 
                            onclick="document.gradebookForm.command.value='associate'">
                            <xsl:choose>
                                <xsl:when test="$ASSOCIATED_EXAM_ID = 2">None (or N/A)</xsl:when>
                                <xsl:otherwise>
                                 <xsl:value-of select="/gradebooks/assessment[@id=$ASSOCIATED_EXAM_ID]/title" />
                                </xsl:otherwise>
                            </xsl:choose>
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img height="16" width="16" border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" 
                                alt="'Edit' icon: to change this columns assessment association (i.e. None, or specific online assessments)" 
                                title="'Edit' icon: to change this columns assessment association (i.e. None, or specific online assessments)" align="absmiddle" 
                                name="gbEditThisAssocImage" id="gbEditThisAssocImage"/>
                        </a> 
                        -->
                        <xsl:choose>
                            <xsl:when test="$associationTitle!=''"><xsl:value-of select="$associationTitle" /></xsl:when>
                          
                            <xsl:otherwise>None</xsl:otherwise>
                        </xsl:choose>
                        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                        <a href="javascript:alert('The assessment association may not be edited after a column has been created.  If necessary, please delete the existing column and add a new one with the desired association.');void(null);" 
                            title="The assessment association may not be edited after a column has been created.  If necessary, please delete the existing column and add a new one with the desired association." 
                            onmouseover="swapImage('gbAsmtAssocHelpImage','channel_help_active.gif')" 
                            onmouseout="swapImage('gbAsmtAssocHelpImage','channel_help_base.gif')">
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif" 
                                alt="'Help' icon linking to an alert box explaining: The assessment association may not be edited after a column has been created.  If necessary, please delete the existing column and add a new one with the desired association." 
                                title="'Help' icon linking to an alert box explaining: The assessment association may not be edited after a column has been created.  If necessary, please delete the existing column and add a new one with the desired association." 
                                align="absmiddle" name="gbAsmtAssocHelpImage" id="gbAsmtAssocHelpImage"/>
                        </a>
                    </td>
                </tr>
                </xsl:if>
                <tr>
                    <td class="table-light-left" style="text-align=right" width="90" id="makeVis">
                        Make Personalized Feedback Visible
                    </td>
                    <td class="table-content-right" headers="makeVis">
                        <input type="radio" class="radio" value="yes" name="feedback" id="gbegbir1">
                            <xsl:if test="$GRADEBOOK-ITEM-POINTER/@feedback != 'no'">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                            </xsl:if>
                        </input>
                        <label for="gbegbir1">&#160;Yes</label>&#160;
                    <input type="radio" class="radio" value="no" name="feedback" id="gbegbir2">
                            <xsl:if test="$GRADEBOOK-ITEM-POINTER/@feedback = 'no'">
                                <xsl:attribute name="checked">checked</xsl:attribute>
                            </xsl:if>
                        </input>
                        <label for="gbegbir2">&#160;No</label>
                    </td>
                </tr>
                <!--<tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                        <label for="gbegbit2">Minimum Score</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="text" class="text" name="min_score" size="3" maxlength="3" value="{$GRADEBOOK-ITEM-POINTER/@min_score}" id="gbegbit2"/>
                    </td>
                </tr> -->
                <!--<tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                        <label for="gbegbit3">Maximum Score</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="hidden" name="min_score" value="0"/>
                        <xsl:choose>
                            <xsl:when test="/gradebooks/assessment[@id = $ASSOCIATED_EXAM_ID]">
                                <input type="text" class="text-disabled" name="max_score" size="3" maxlength="3" value="{$GRADEBOOK-ITEM-POINTER/@max_score}" id="gbegbit3" onfocus="this.blur();"/>
                            </xsl:when>
                          
                            <xsl:otherwise>
                        <input type="text" class="text" name="max_score" size="3" maxlength="3" value="{$GRADEBOOK-ITEM-POINTER/@max_score}" id="gbegbit3"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr> -->
                <tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                        <label for="gbegbit3">Maximum Score</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="hidden" name="min_score" value="0"/>
                        <xsl:choose>
                            <xsl:when test="/gradebooks/assessment[@id = $ASSOCIATED_EXAM_ID]">
                                <input type="text" class="text-disabled" name="max_score" size="3" maxlength="3" value="{$GRADEBOOK-ITEM-POINTER/@max_score}" id="gbegbit3" readonly="readonly"/>
                            </xsl:when>
                          
                            <xsl:otherwise>
                        <input type="text" class="text" name="max_score" size="3" maxlength="3" value="{$GRADEBOOK-ITEM-POINTER/@max_score}" id="gbegbit3"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                        <label for="gbegbit4">Column Weight</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="text" class="text" name="weight" size="3" maxlength="3" value="{$GRADEBOOK-ITEM-POINTER/@weight}" id="gbegbit4"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align=right" nowrap="nowrap">
                        <label for="gbegbis2">Position</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <!-- <xsl:variable name = "current_position" select = "{gradebook-item[@id = $gradebookItemID]/@position}" /> -->
                        <input type="hidden" name="current_position" value="{$GRADEBOOK-ITEM-POINTER/@position}"/>
                        <select name="position" id="gbegbis2">
                            <option value="0" selected="selected">At beginning</option>
                            <xsl:apply-templates select="/gradebooks/gradebook-item">
                                <xsl:with-param name="IN_POSITION" select="$GRADEBOOK-ITEM-POINTER/@position"/>
                            </xsl:apply-templates>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" style="text-align:center">
                        <input type="submit" class="uportal-button" value="Submit" title="To save these changes and return to the main view of the gradebook"/>
                        <xsl:if test="$addActivation = 'Y'">
                            <input type="submit" class="uportal-button" value="Submit and Activate" 
                                   title="To save the changes for this column and go to the add activation view of the gradebook"
                                   onclick="document.gradebookForm.command.value = 'update'" />
                        </xsl:if>
                        <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel these changes and return to the main view of the gradebook"/>
                    </td>
                </tr>
                <xsl:call-template name="sublinks">
                    <xsl:with-param name="commandDefault">update</xsl:with-param>
                </xsl:call-template>
            </table>
        </form>
    </xsl:template>
    <xsl:template match="gradebook-item">
        <xsl:param name="IN_POSITION"/>
        <!-- Add option to dropdown if not the item being edited (can't position it based on itself) -->
        <xsl:if test="(@position != $IN_POSITION)">
            <option>
                <!-- set value attribute of option -->
                <xsl:choose>
                    <xsl:when test="@position &gt; $IN_POSITION">
                        <xsl:attribute name="value"><xsl:value-of select="@position"/></xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="value"><xsl:value-of select="@position + 1"/></xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <!-- set selected attribute of option  -->
                <xsl:if test="((@position + 1) = $IN_POSITION)">
                    <xsl:attribute name="selected">
                   selected
               </xsl:attribute>
                </xsl:if>
           
           After <xsl:value-of select="title"/>
            </option>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
