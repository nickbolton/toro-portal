<?xml version="1.0" encoding="utf-8"?>
<!--   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.   This software is the confidential and proprietary information of   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not   disclose such Confidential Information and shall use it only in   accordance with the terms of the license agreement you entered into   with IBS-DP or its authorized distributors.   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
    <xsl:include href="common.xsl" />
    <xsl:include href="../../global/toolbar.xsl" />
    <xsl:param name="Type">default</xsl:param>
    <xsl:param name="Title">default</xsl:param>
    <xsl:param name="FormID">default</xsl:param>
    <xsl:template match="survey-system">
    	<!--<textarea><xsl:copy-of select="*"/></textarea>-->
       
        <xsl:choose>
        	<xsl:when test="not(Form/@Title)">
				<div class="gradient-page-title">
					Create Survey - Step 1 of 3
				</div>
				<form action="{$baseActionURL}" method="post" name="SurveyForm">
					<input type="hidden" name="sid" value="{$sid}" />
					<input type="hidden" name="Type" value="{$Type}" />
					<input type="hidden" size="30" name="Title" value="{$Title}"/>
					<input type="hidden" name="xsl">
						<xsl:if test="File/@FileShow">
							<xsl:attribute name="value">
								<xsl:value-of select="File/@FileShow"/>
							</xsl:attribute>
						</xsl:if>
					</input>				

					<div align="center">
						<table cellpadding="0" cellspacing="0" border="0">
							<tr>
								<td colspan="99" class="survey-step1-subtitle">
									Please select a method to create a survey:
								</td>
							</tr>
							<tr>
								<td>
									<img src="{$surveyImagePath}/create_survey_left_bg.gif" border="0" alt="" title=""/>
								</td>
								<td style="background-image: url({$surveyImagePath}/create_survey_middle_bg.gif);" align="center">
									<img src="{$surveyImagePath}/create_survey3.gif" border="0" alt="" title=""/>
									<br/><br/>
									<input type="image" class="image" name="do~editXML" src="{$surveyImagePath}/new_survey_text.gif" border="0" alt="" title=""/>
								</td>
								<td>
									<img src="{$surveyImagePath}/create_survey_right_bg.gif" border="0" alt="" title=""/>
								</td>
								<td>
									<strong style="padding:10px;">
										OR
									</strong>
								</td>
								<td>
									<img src="{$surveyImagePath}/create_survey_left_bg.gif" border="0" alt="" title=""/>
								</td>
								<td style="background-image: url({$surveyImagePath}/create_survey_middle_bg.gif);" align="center">
									<img src="{$surveyImagePath}/import_survey2.gif" border="0" alt="" title=""/>
									<br/><br/>
									<input type="image" class="image" name="do~uploadXML" src="{$surveyImagePath}/import_survey_text.gif" border="0" alt="" title=""/>
								</td>
								<td>
									<img src="{$surveyImagePath}/create_survey_right_bg.gif" border="0" alt="" title=""/>
								</td>        			
							</tr>
						</table>       				
					</div>
					<div align="center">
						<div  class="submit-container">
							<input type="submit" value="Back to Survey List" name="do~Cancel" />
						</div>
					</div>
				</form>
        	
			</xsl:when>

			<xsl:when test="not(Form/@FormId)">
				<div class="gradient-page-title">
					Create Survey - Step 3 of 3
				</div>
				<div class="bounding-box7">
					<form action="{$baseActionURL}" method="post" name="SurveyForm" id="SurveyForm">
						<input type="hidden" name="sid" value="{$sid}" />
						<input type="hidden" name="Type" value="{$Type}" />
						<input type="hidden" name="survey_temp" id="survey_temp" />
						
						<!--UniAcc: Layout Table -->
						<table cellpadding="0" cellspacing="0" border="0" align="center">
							<tr>
								<td nowrap="nowrap" valign="middle" colspan="2">
									<a href="javascript:document.getElementById('survey_temp').name='do~editXML';document.getElementById('SurveyForm').submit();" title="Edit Survey">
										Go back and edit survey
									</a>
									<div class="top-bottom-space">
										Enter the name of your survey and select a presentation style:
									</div>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" nowrap="nowrap">
									<label for="SSAF-TitleT1">Survey Title:</label>
								</td>
								<td nowrap="nowrap" width="99%">
									<xsl:choose>
										<xsl:when test="$Title != 'default'">
											<input type="text" size="30" name="Title" maxlength="80" value="{$Title}" class="uportal-input-text" id="SSAF-TitleT1"/>
										</xsl:when>
										<xsl:otherwise>
											<input type="text" size="30" name="Title" maxlength="80" class="uportal-input-text" id="SSAF-TitleT1"/>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>    
							<tr>
								<td class="table-light-left" nowrap="nowrap">
									<label for="SSAF-StyleDescS1">Select Style:</label>
								</td>
								<td nowrap="nowrap" width="99%" valign="middle">
									<select name="xsl" size="1" class="text" id="SSAF-StyleDescS1">
										<xsl:for-each select="File">
											<xsl:choose>
												<xsl:when test="@FileShow=../Form/XSLForm/@FileShow">
													<option selected="selected">
														<xsl:attribute name="value">
															<xsl:value-of select="@FileName" />
														</xsl:attribute>
														<xsl:value-of select="@FileShow" />
													</option>
												</xsl:when>
												<xsl:otherwise>
													<option>
														<xsl:attribute name="value">
															<xsl:value-of select="@FileName" />
														</xsl:attribute>
														<xsl:value-of select="@FileShow" />
													</option>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each>
									</select>
									<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
									<input type="submit" class="uportal-button" name="do~preview" value="Preview" />
									<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
									<input type="hidden" name="return" value="Form" />
									<input type="image" class="nopad" src="{$CONTROLS_IMAGE_PATH}/channel_view_active.gif" value="Preview XSL" alt="View Source XSL" name="do~textXSL" />
									View XSL
								</td>
							</tr>                    
							<!--
							<tr>
								<td></td>
								<td class="table-content-right" nowrap="nowrap" width="99%">
									<input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="do~textXML" id="ViewSourceXML" title="View Source XML" align="absmiddle" onmouseover="swapImage('ViewSourceXML','channel_view_active.gif')" onmouseout="swapImage('ViewSourceXML','channel_view_base.gif')" alt="View Source XML"/>
									View XML
								</td>
							</tr> 
							<tr>
								<td></td>
								<td class="table-content-right" nowrap="nowrap" width="99%">
									<input type="submit" class="uportal-button" name="do~preview" value="Preview" />
								</td>
							</tr>
							-->
							<tr>
								<td colspan="2" align="center">
									<div class="submit-container">
										<input type="hidden" name="default" value="do~OK" />
										<input type="submit" name="do~OK" value="Save" />
									</div>
								</td>
							</tr>
						</table>
					</form>
				</div>
			</xsl:when>
			<xsl:otherwise> 
				<div class="gradient-page-title">
					Edit Survey - <xsl:value-of select="$Title"/>
				</div>
				<div class="bounding-box7">
					<form action="{$baseActionURL}" method="post" name="SurveyForm" id="SurveyForm">
						<input type="hidden" name="sid" value="{$sid}" />
						<input type="hidden" name="Type" value="{$Type}" />
						<input type="hidden" name="survey_temp" id="survey_temp" />
						
						<!--UniAcc: Layout Table -->
						<table cellpadding="0" cellspacing="0" border="0" align="center">
							<tr>
								<td class="table-light-left" nowrap="nowrap">
									<label for="SSAF-TitleT1">Survey Title:</label>
								</td>
								<td nowrap="nowrap" width="99%">
									<xsl:choose>
										<xsl:when test="$Title != 'default'">
											<input type="text" size="30" name="Title" maxlength="80" value="{$Title}" class="uportal-input-text" id="SSAF-TitleT1"/>
										</xsl:when>
										<xsl:otherwise>
											<input type="text" size="30" name="Title" maxlength="80" class="uportal-input-text" id="SSAF-TitleT1"/>
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>    
							<tr>
								<td class="table-light-left" nowrap="nowrap">
									<label for="SSAF-StyleDescS1">Select Style:</label>
								</td>
								<td nowrap="nowrap" width="99%" valign="middle">
									<select name="xsl" size="1" class="text" id="SSAF-StyleDescS1">
										<xsl:for-each select="File">
											<xsl:choose>
												<xsl:when test="@FileShow=../Form/XSLForm/@FileShow">
													<option selected="selected">
														<xsl:attribute name="value">
															<xsl:value-of select="@FileName" />
														</xsl:attribute>
														<xsl:value-of select="@FileShow" />
													</option>
												</xsl:when>
												<xsl:otherwise>
													<option>
														<xsl:attribute name="value">
															<xsl:value-of select="@FileName" />
														</xsl:attribute>
														<xsl:value-of select="@FileShow" />
													</option>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each>
									</select>
									<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
									<input type="submit" class="uportal-button" name="do~preview" value="Preview" />
									<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
									<input type="image" class="nopad" src="{$CONTROLS_IMAGE_PATH}/channel_view_active.gif" value="Preview XSL" alt="View Source XSL" name="do~textXSL" />
									View XSL
									<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
									<input type="hidden" name="return" value="Form" />
									<a href="{$goURL}=Source&amp;FormId={$FormID}&amp;return=Form"><img src="{$CONTROLS_IMAGE_PATH}/channel_view_active.gif" border="0" /></a>
									View XML
									</td>
							</tr>   
							<tr>
								<td></td>
								<td nowrap="nowrap" valign="middle">
									<br/><br/>
									<input type="submit" name="do~editXML" value="Edit Survey" class="uportal-button" title="edit survey"/>
								</td>
							</tr>							
							<!--
							<tr>
								<td></td>
								<td class="table-content-right" nowrap="nowrap" width="99%">
									<input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="do~textXML" id="ViewSourceXML" title="View Source XML" align="absmiddle" onmouseover="swapImage('ViewSourceXML','channel_view_active.gif')" onmouseout="swapImage('ViewSourceXML','channel_view_base.gif')" alt="View Source XML"/>
									View XML
								</td>
							</tr> 
							<tr>
								<td></td>
								<td class="table-content-right" nowrap="nowrap" width="99%">
									<input type="submit" class="uportal-button" name="do~preview" value="Preview" />
								</td>
							</tr>
							-->
							<tr>
								<td colspan="2" align="center">
									<div class="submit-container">
										<div class="submit-comments">
											<strong>Warning:</strong> Clicking "Cancel" will delete any changes made this session
										</div>
										<input type="hidden" name="default" value="do~OK" />
										<input type="submit" name="do~OK" value="Save" />
										<input type="submit" name="do~Cancel" value="Cancel" />
									</div>
								</td>
							</tr>
						</table>
					</form>
				</div>
			</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

