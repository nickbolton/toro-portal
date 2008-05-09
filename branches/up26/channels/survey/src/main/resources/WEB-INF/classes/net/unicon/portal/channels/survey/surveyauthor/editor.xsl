<?xml version="1.0" encoding="utf-8"?>
<!--

   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.

   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.

   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.

-->
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    <xsl:output method='html' />
    <xsl:include href="common.xsl" />
    <xsl:param name="xml-form" select="survey-system/source"></xsl:param>
    <xsl:param name="currentPage">1</xsl:param>
    <xsl:param name="currentQuestion">1</xsl:param>
    <xsl:param name="pageSize">
        <xsl:value-of select="count($xml-form/page)" />
    </xsl:param>
    <xsl:param name="questionSize">
        <xsl:value-of select="count($xml-form/page[@page-id=$currentPage]/question)" />
    </xsl:param>
    <xsl:param name="subQuestionSize">
        <xsl:value-of select="count($xml-form/page[@page-id=$currentPage]/question[@question-id=$currentQuestion]/input)" />
    </xsl:param>
    <xsl:param name="typeEditor">new</xsl:param>
    <xsl:template match="survey-system">
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>   
    </textarea>-->
            <form method='post' action='{$baseActionURL}' name="SurveyEditor" id="SurveyEditor">
                <input type='hidden' name='sid' value='{$sid}' />
                <input type='hidden' name='submitForm' id="SurveySubmitForm" />
                <xsl:apply-templates select="$xml-form/page" />
            </form>
    </xsl:template>
    
    <xsl:template match='page'>
        <xsl:if test="@page-id=$currentPage">
        	<!--UniAcc: Layout Table -->
        	
        	<xsl:choose>
        		<xsl:when test="$typeEditor='new'">
					<div class="gradient-page-title">
						Create Survey - Step 2 of 3	
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="gradient-page-title">
						Edit Survey - <xsl:value-of select="@title"/>
					</div>
				</xsl:otherwise>
			</xsl:choose>
        	
        	<h2 class="survey-page-title">Survey Page&#160;<xsl:value-of select="@page-id" /></h2>
        	
        	<div class="survey-toolbar-container">
        		<xsl:apply-templates select="." mode="navigator" />
        	</div>
        	
        	<div>
				<table cellpadding='0' cellspacing='0' width='100%' border='0' class="edit-survey-table">
					<tr><td colspan="2"><img height="10" width="1" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
					<tr>
						<td nowrap="nowrap" class="td-left">
							<label for="SSAEdit-TitleT1">Page&#160;<xsl:value-of select="@page-id" />&#160;Title:</label>
						</td>
						<td class="table-content-right">
							<input name='pageText' type='text' size='40' class="text" value="{@title}" id="SSAEdit-TitleT1" />
							<img height="1" width="15" src="{$SPACER}" border="0" alt="" title="" />
							<xsl:if test="$pageSize &gt; 1">
								<a href="javascript:document.getElementById('SurveySubmitForm').name='do~deletePage';document.getElementById('SurveyEditor').submit()" onmouseover="swapImage('SurveyDeletePage','channel_delete_active.gif')" onmouseout="swapImage('SurveyDeletePage','channel_delete_base.gif')" title="">
									<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif" border="0" align="absmiddle" name="SurveyDeletePage" id="SurveyDeletePage" alt="" title="" />
									<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Delete Page 								
								</a>
							</xsl:if>							
						</td>
					</tr>
					<tr><td colspan="2"><img height="15" width="1" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
					<xsl:apply-templates select="question[@question-id=$currentQuestion]" />
					<tr>
						<td colspan="2" align="center">
							<div class="top-bottom-space">
								<input type='submit' name='do~preview' value="Preview" />
								<xsl:choose>
									<xsl:when test="$typeEditor = 'new'">
										<input type='submit' name='do~reset' value="Reset" />
									</xsl:when>
									<xsl:otherwise>
										<input type='submit' name='do~reset' value="reset all pages" />
									</xsl:otherwise>
								</xsl:choose>							
							</div>
							
							<div class="submit-container">	
								<div class="submit-comments">
									<strong>Reminder:</strong> Click "Continue" to store your information
								</div>
								<input type='submit' name='do~save' value="Continue" />
								<input type='submit' name='do~close' value="Cancel" />
							</div>
						</td>
					</tr>
				</table>
			</div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="question">
        <!--<tr>
            <th class="th-top" colspan="2">Page&#160; 
            <xsl:value-of select="$currentPage" />&#160;Question&#160;<xsl:value-of select="@question-id" />
            </th>
        </tr>-->
        <tr>
        	<td colspan="2">
            <!--UniAcc: Layout Table -->
				<div class="survey-toolbar-container">
					<xsl:apply-templates select="." mode="navigator" />
				</div>
			</td>
		</tr>
		<xsl:choose>
			<xsl:when test="($subQuestionSize &lt; 2) and (string-length(normalize-space(input/text())) &lt; 1)">
				<tr>
					<td colspan="2">
						<div class="survey-editor-subtitle">
							Question Format
						</div>
					</td>
				</tr>
				<tr>
					<td class="td-left">
						<label for="SSAEdit-QuestionTypeS1">
							Question Type:
						</label>
					</td>
					<td class="td-right">
						<select name="dataType{input/@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={input/@input-id}';document.getElementById('SurveyEditor').submit();" id="SSAEdit-QuestionTypeS1">
							<option value="Check">
							<xsl:if test="input/data/@type='Check'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Multiple Choice</option>
							<option value="Choice">
							<xsl:if test="input/data/@type='Choice'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Single Choice</option>
							<option value="Text">
							<xsl:if test="input/data/@type='Text'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Text</option>
						</select>
					</td>
				</tr>						
				<xsl:choose>
					<xsl:when test="(input/data/@type='Choice') or (input/data/@type='Check')">
						<tr>
							<td class="td-left"></td>
							<td class="td-right">
								<input type="text" size="1" maxlength="1" name="responseSize" id="SSAEdit-ResponsesT1">
									<xsl:attribute name="value">
										<xsl:value-of select="count(input/data/entry)" />
									</xsl:attribute>
								</input>
								<label for="SSAEdit-ResponsesT1">
									Responses
								</label>
							</td>
						</tr>																
					</xsl:when>
					<xsl:otherwise>
						<tr><td colspan="2"><input type="hidden" size="1" name="responseSize" value="3"/></td></tr>
					</xsl:otherwise>
				</xsl:choose>
					
				<!--
				<tr>
					<td class="td-left" nowrap="nowrap">
						<label for="SSAEdit-SubQuestionsT1">
							Sub-Questions:
						</label>
					</td>
					<td>
						<input type="text" size="1" maxlength="1" name="subQuestionSize" value="{($subQuestionSize - 1)}" id="SSAEdit-SubQuestionsT1">
						</input>

						<input type='submit' class='uportal-button' name='do~updateQuestionType' value="Update" />
					</td>
				</tr>
				-->
				<tr>
					<td class="td-left">
						<label for="dataForm{input/@input-id}">
							Layout Option:
						</label>
					</td>
					<td class="td-right">
						<xsl:choose>
							<xsl:when test="(input/data/@type='Choice') or (input/data/@type='Check')">
								<select name="dataForm{input/@input-id}" id="dataForm{input/@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={input/@input-id}';document.getElementById('SurveyEditor').submit();">
									<option value="Column">
									<xsl:if test="input/data/@form='Column'">
										<xsl:attribute name="selected">selected</xsl:attribute>
									</xsl:if>
									Column</option>
									<option value="Row">
									<xsl:if test="input/data/@form='Row'">
										<xsl:attribute name="selected">selected</xsl:attribute>
									</xsl:if>
									Row</option>
								</select>
							</xsl:when>	
							<xsl:when test="(input/data/@type='Text')">
								<select name="dataForm{input/@input-id}" id="dataForm{input/@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={input/@input-id}';document.getElementById('SurveyEditor').submit();">
									<option value="Single">
									<xsl:if test="input/data/@form='Single'">
										<xsl:attribute name="selected">selected</xsl:attribute>
									</xsl:if>
									Single</option>
									<option value="Multiple">
									<xsl:if test="input/data/@form='Multiple'">
										<xsl:attribute name="selected">selected</xsl:attribute>
									</xsl:if>
									Multiple</option>
								</select>
							</xsl:when>
						</xsl:choose>
					</td>
				</tr>
				<tr>
					<td></td>
					<td class="td-right">
						<input type='submit' name='do~updateQuestionType' value="Update" />
					</td>
				</tr>


				<tr><td colspan="2"><img height="10" width="1" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
				<tr>
					<td colspan="2">
						<div class="survey-editor-subtitle">
							Question Details
						</div>
					</td>
				</tr>
				<tr>
					<td class="td-left" valign="top">
						<label for="SSAEdit-QuestionTextTA1">
							Question&#160;<xsl:value-of select="@question-id" /> Text:
						</label>
					</td>
					<td nowrap="nowrap" class="td-right" valign="top">
						<textarea rows="2" cols="80" class="text" name="questionText" id="SSAEdit-QuestionTextTA1">
							<xsl:value-of select="text()" />
						</textarea>
					</td>
				</tr>
				<tr><td colspan="2"><img height="10" width="1" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
				
				<xsl:choose>
					<xsl:when test="(input/data/@type='Choice') or (input/data/@type='Check')">
						<xsl:apply-templates select="input/data/entry">
							<xsl:with-param name="sub-question" select="input" />
						</xsl:apply-templates>
					</xsl:when>
					<xsl:when test="(input/data/@type='Text')">
						<tr>
							<td class="td-left" nowrap="nowrap" valign="top">Response:</td>
							<td class="td-right" nowrap="nowrap">&#160;Text input</td>
						</tr>
					</xsl:when>
				</xsl:choose>
				<tr><td colspan="2"><img height="20" width="1" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
			</xsl:when>
			<xsl:otherwise>
				<tr><td colspan="2"><img height="10" width="3" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
			
				<tr>
					<td class="td-left" width="1%" valign="top">
						<label for="SSAEdit-QuestionTextTA1">
						Question&#160;<xsl:value-of select="@question-id" /> Text:
						</label>
					</td>
					<td class="td-right" nowrap="nowrap" valign="top">
						<textarea rows="2" cols="80" class="text" name="questionText" id="SSAEdit-QuestionTextTA1">
							<xsl:value-of select="text()" />
						</textarea>
					</td>
				</tr>
				<tr><td colspan="2"><img height="10" width="3" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
				<!--<xsl:if test="$questionSize &gt; 1">
					<tr>
						<td class="table-nav" colspan="2">
						<input type='submit' class="uportal-button" name="do~deleteQuestion" value="delete question"/>
							</td>
					</tr>
				</xsl:if>-->
				<xsl:for-each select="input">
						<xsl:apply-templates select="." mode="sub-question"></xsl:apply-templates>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>
    
<!--PAGE NAVIGATION TEMPLATE -->    
    <xsl:template match="page" mode="navigator">
		<div class="tool3">
			<strong>
				<label for="SSAEdit-PageNavigationS1">
					<xsl:text>Page&#160;</xsl:text>
					<xsl:value-of select="$currentPage"/>
				</label>
			</strong>

			<img height="1" width="15" src="{$SPACER}" border="0" alt="" title="" />
			
			<a href="javascript:document.getElementById('SurveySubmitForm').name='do~addPage';document.getElementById('SurveyEditor').submit()" title="add page">
				<img src="{$CONTROLS_IMAGE_PATH}/channel_add_active.gif" border="0" align="absmiddle" name="SurveyAddPage" id="SurveyAddPage" alt="" title="" />
				<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Add New Page 
			</a>
	    
		</div>
		<div class="tool-right">
			<strong>
				<label for="SSAEdit-PageNavigationS1">
					<xsl:text>Page&#160;</xsl:text>
				</label>
			</strong>
			<select class="nopad" name="selectPage" id="SSAEdit-PageNavigationS1" onchange="document.getElementById('SurveySubmitForm').name='do~gotoPage';document.getElementById('SurveyEditor').submit();">
				<xsl:for-each select="$xml-form/page">
					<xsl:if test="(@page-id=$currentPage)">
						<option selected="selected" value="{@page-id}">
							<xsl:value-of select="@page-id" />
						</option>
					</xsl:if>
					<xsl:if test="not(@page-id=$currentPage)">
						<option value="{@page-id}">
							<xsl:value-of select="@page-id" />
						</option>
					</xsl:if>
				</xsl:for-each>
			</select>
			of&#160; 
			<xsl:value-of select="$pageSize" />
		</div>
    </xsl:template>
    
<!--QUESTION NAVIGATION TEMPLATE -->    
    <xsl:template match="question" mode="navigator">
    	<div class="tool3">
			<strong>
				<label for="SSAEdit-QuestionNavS1">
					<xsl:text>Question&#160;</xsl:text>
					<xsl:value-of select="$currentQuestion"/>
				</label>
			</strong>
			
			<img height="1" width="15" src="{$SPACER}" border="0" alt="" title="" />
			<a href="javascript:document.getElementById('SurveySubmitForm').name='do~addQuestion';document.getElementById('SurveyEditor').submit()" onmouseover="swapImage('SurveyAddQuestion','channel_add_active.gif')" onmouseout="swapImage('SurveyAddQuestion','channel_add_base.gif')" title="">
				<img src="{$CONTROLS_IMAGE_PATH}/channel_add_active.gif" border="0" align="absmiddle" name="SurveyAddQuestion" id="SurveyAddQuestion" alt="" title="" />
				<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Add New Question
			</a>

			<img height="1" width="15" src="{$SPACER}" border="0" alt="" title="" />
			<a href="javascript:document.getElementById('SurveySubmitForm').name='do~addSubQuestion';document.getElementById('SurveyEditor').submit()" title="">
				<img src="{$CONTROLS_IMAGE_PATH}/channel_add_active.gif" border="0" align="absmiddle" name="SurveyAddSubQuestion" id="SurveyAddSubQuestion" alt="" title="" />
				<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Add New Sub-Question
			</a>

			<xsl:if test="$questionSize &gt; 1">
				<img height="1" width="15" src="{$SPACER}" border="0" alt="" title="" />
				<a href="javascript:document.getElementById('SurveySubmitForm').name='do~deleteQuestion';document.getElementById('SurveyEditor').submit()" title="">
					<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif" border="0" align="absmiddle" name="SurveyAddSubQuestion" id="SurveyAddSubQuestion" alt="" title="" />
					<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Delete Question
				</a>
			</xsl:if>
							
		</div>
		
		<div class="tool-right">
			<strong>
				<label for="SSAEdit-QuestionNavS1">
					<xsl:text>Question&#160;</xsl:text>
				</label>
			</strong>
			<select name="selectQuestion" id="SSAEdit-QuestionNavS1" onchange="document.getElementById('SurveySubmitForm').name='do~gotoQuestion';document.getElementById('SurveyEditor').submit();">
				<xsl:for-each select="$xml-form/page[@page-id=$currentPage]/question">
					<xsl:if test="(@question-id=$currentQuestion)">
						<option selected="selected" value="{@question-id}">
							<xsl:value-of select="@question-id" />
						</option>
					</xsl:if>
					<xsl:if test="not(@question-id=$currentQuestion)">
						<option value="{@question-id}">
							<xsl:value-of select="@question-id" />
						</option>
					</xsl:if>
				</xsl:for-each>
			</select>
			of&#160; 
			<xsl:value-of select="$questionSize" />
		</div>
		
    </xsl:template>
    
    <xsl:template match="input" mode="sub-question">
        <tr>
            <td colspan="2">
            	<div class="survey-subquestion-container">
            		<div class="tool3">
						<strong>Sub Question&#160;<xsl:value-of select="@input-id" /></strong>
						<xsl:if test="$subQuestionSize &gt; 1">
							<img height="1" width="15" src="{$SPACER}" border="0" alt="" title="" />
							<a href="javascript:document.getElementById('SurveySubmitForm').name='do~deleteSubQuestion&amp;inputId={@input-id}';document.getElementById('SurveyEditor').submit()" title="">
								<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif" border="0" align="absmiddle" name="SurveyAddSubQuestion" id="SurveyAddSubQuestion" alt="" title="" />
								<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Delete Sub-Question
							</a>						
						</xsl:if>            		
					</div>
            	</div>
            </td>
        </tr>    
		<tr>
			<td colspan="2">
				<div class="survey-editor-subtitle">
					Question Format
				</div>
			</td>
		</tr>
		<tr>
			<td class="td-left">
				<label for="SSAEdit-QuestionTypeS1">
					Question Type:
				</label>
			</td>
			<td class="td-right">
				<select name="dataType{@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={@input-id}';document.getElementById('SurveyEditor').submit();" id="SA-SubQuestionSubS1{@input-id}">
					<option value="Check">
					<xsl:if test="data/@type='Check'">
						<xsl:attribute name="selected">selected</xsl:attribute>
					</xsl:if>
					Multiple Choice</option>
					<option value="Choice">
					<xsl:if test="data/@type='Choice'">
						<xsl:attribute name="selected">selected</xsl:attribute>
					</xsl:if>
					Single Choice</option>
					<option value="Text">
					<xsl:if test="data/@type='Text'">
						<xsl:attribute name="selected">selected</xsl:attribute>
					</xsl:if>
					Text</option>
				</select>
			</td>
		</tr>						
		<tr>
			<td class="td-left"></td>
			<td class="td-right">
				<xsl:choose>
					<xsl:when test="(data/@type='Choice') or (data/@type='Check')">
						<input type="text" size="1" maxlength="1" name="responseSize{@input-id}" id="responseSize{@input-id}">
							<xsl:attribute name="value">
								<xsl:value-of select="count(data/entry)" />
							</xsl:attribute>
						</input>
						<label for="SSAEdit-ResponsesT1">
							Responses
						</label>																
					</xsl:when>
					<xsl:otherwise>
						<input type="hidden" size="1" name="responseSize" value="3"/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</tr>    
		<!--
        <tr>
			<td>
				<xsl:choose>
					<xsl:when test="(data/@type='Choice') or (data/@type='Check')">
						<select name="dataType{@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={@input-id}';document.getElementById('SurveyEditor').submit();" id="SA-SubQuestionSubS1{@input-id}">
							<option value="Check">
							<xsl:if test="data/@type='Check'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Multiple Choice</option>
							<option value="Choice">
							<xsl:if test="data/@type='Choice'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Single Choice</option>
							<option value="Text">
							<xsl:if test="data/@type='Text'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Text</option>
						</select>
					</xsl:when>
					<xsl:when test="(data/@type='Text')">
						<select name="dataType{@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={@input-id}';document.getElementById('SurveyEditor').submit();" id="SA-SubQuestionSubS1{@input-id}">
							<option value="Check">
							<xsl:if test="data/@type='Check'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Multiple Choice</option>
							<option value="Choice">
							<xsl:if test="data/@type='Choice'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Single Choice</option>
							<option value="Text">
							<xsl:if test="data/@type='Text'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Text</option>
						</select>
					</xsl:when>
				</xsl:choose>
			</td>
		</tr>
		<tr>
			<td class="table-light-left" align="right" valign="top">
				<label for="responseSize{@input-id}">Responses:</label>
			</td>
			<td>
				<xsl:choose>
					<xsl:when test="(data/@type='Choice') or (data/@type='Check')">
					<input type="text" size="1" maxlength="1" name="responseSize{@input-id}" id="responseSize{@input-id}">
					<xsl:attribute name="value">
						<xsl:value-of select="count(data/entry)" />
					</xsl:attribute>
					</input>
					</xsl:when>
					<xsl:otherwise>
					  <input type="hidden" size="1" name="responseSize{@input-id}" value="3"/>
					</xsl:otherwise>
				</xsl:choose>
				<img height="1" width="6" src="{$SPACER}" border="0" alt="" title="" />
				<input type='submit' class='uportal-button' name='do~updateSubQuestionType&amp;inputId={@input-id}' value="Update" />
			</td>
		</tr>		
		-->
		<tr>
			<td class="td-left" valign="top">
				<label for="dataForm{@input-id}">Layout Option:</label>
			</td>
			<td class="td-right">
				<xsl:choose>
					<xsl:when test="(data/@type='Choice') or (data/@type='Check')">
						<select name="dataForm{@input-id}" id="dataForm{@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={@input-id}';document.getElementById('SurveyEditor').submit();">
							<option value="Column">
							<xsl:if test="data/@form='Column'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Column</option>
							<option value="Row">
							<xsl:if test="data/@form='Row'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Row</option>
						</select>
					</xsl:when>
					<xsl:when test="(data/@type='Text')">
						<select name="dataForm{@input-id}" id="dataForm{@input-id}" onchange="document.getElementById('SurveySubmitForm').name='do~dataTypeChanged&amp;inputId={@input-id}';document.getElementById('SurveyEditor').submit();">
							<option value="Single">
							<xsl:if test="data/@form='Single'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Single</option>
							<option value="Multiple">
							<xsl:if test="data/@form='Multiple'">
								<xsl:attribute name="selected">selected</xsl:attribute>
							</xsl:if>
							Multiple</option>
						</select>
					</xsl:when>
				</xsl:choose>
			</td>
		</tr>
		<tr>
			<td></td>
			<td class="td-right">
				<input type='submit' name='do~updateSubQuestionType&amp;inputId={@input-id}' value="Update" />
			</td>
		</tr>
		
		<tr>
			<td colspan="2"><img height="20" width="1" src="{$SPACER}" border="0" alt="" title="" /></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="survey-editor-subtitle">
					Question Details
				</div>
			</td>
		</tr>		
		<tr>
			<td class="td-left" nowrap="nowrap"  valign="top">
				<label for="SA-SubQuestionSubT1{@input-id}">
					Sub-Question&#160;<xsl:value-of select="@input-id" />&#160;Text:
				</label>
			</td>
			<td class="td-right">
				<textarea rows="2" cols="80" class="text" name="inputText{@input-id}" id="SA-SubQuestionSubT1{@input-id}">
					<xsl:value-of select="text()" />
				</textarea>
			</td>
		</tr>
		<tr>
			<td colspan="2"><img height="10" width="1" src="{$SPACER}" border="0" alt="" title="" /></td>
		</tr>						
		<xsl:choose>
			<xsl:when test="(data/@type='Choice') or (data/@type='Check')">
				<xsl:apply-templates select="./data/entry">
					<xsl:with-param name="sub-question" select="." />
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="(data/@type='Text')">
				<tr>
					<td class="td-left" nowrap="nowrap" valign="top">Response:</td>
					<td class="td-right" nowrap="nowrap">&#160;Text input</td>
				</tr>
			</xsl:when>
		</xsl:choose>
		<tr><td colspan="2"><img height="10" width="1" src="{$SPACER}" border="0" alt="" title="" /></td></tr>
    </xsl:template>
    
    <xsl:template match="entry">
        <xsl:param name="sub-question" />
        <tr>
            <td class="td-left" valign="top">
				<label for="SSAEdit-Response{$sub-question/@input-id}{@data-id}">
					Response&#160;<xsl:value-of select="@data-id" />:
				</label> 
			</td>
            <td class="td-right" valign="top">
            	<div>
					<input type="text" class="text" size="60" value="{text()}" name="entryText{$sub-question/@input-id}{@data-id}" id="SSAEdit-Response{$sub-question/@input-id}{@data-id}"/>
					<xsl:if test="(($sub-question/data/@type='Choice') and (count($sub-question/data/entry)) &gt; 2) or (($sub-question/data/@type='Check') and (count($sub-question/data/entry)) &gt; 1)">
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
						<a href="javascript:document.getElementById('SurveySubmitForm').name='do~deleteResponse&amp;inputId={$sub-question/@input-id}&amp;dataId={@data-id}';document.getElementById('SurveyEditor').submit()" onmouseover="swapImage('SurveyDeleteResponse{$sub-question/@input-id}{@data-id}','channel_delete_active.gif')" onmouseout="swapImage('SurveyDeleteResponse{$sub-question/@input-id}{@data-id}','channel_delete_base.gif')" title="">
							<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" border="0" align="absmiddle" name="SurveyDeleteResponse{$sub-question/@input-id}{@data-id}" id="SurveyDeleteResponse{$sub-question/@input-id}{@data-id}" alt="" title="" />
							<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />Delete Response
						</a>
					</xsl:if>							
				</div>
				<div class="small-pad-tb">
					<img height="1" width="4" src="{$SPACER}" border="0" alt="" title="" />
					<label for="SSAEdit-LinkResponseT{$sub-question/@input-id}{@data-id}">Link response to URL:</label>
					<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
					<input type="text" class="text" size="60" value="{@href}" name="entryHref{$sub-question/@input-id}{@data-id}" id="SSAEdit-LinkResponseT{$sub-question/@input-id}{@data-id}"/>
				</div>
				<div>
					<input type="checkbox" class="text" name="entryShuffle{$sub-question/@input-id}{@data-id}" value="y" id="SSAEdit-RandomizeT{$sub-question/@input-id}{@data-id}">
					<xsl:if test="@shuffle = 'y'">
						<xsl:attribute name="checked">checked</xsl:attribute>
					</xsl:if>
					</input>							
					<label for="SSAEdit-RandomizeT{$sub-question/@input-id}{@data-id}">Randomize response order</label>
				</div>
			</td>
		</tr>
    </xsl:template>
</xsl:stylesheet>

