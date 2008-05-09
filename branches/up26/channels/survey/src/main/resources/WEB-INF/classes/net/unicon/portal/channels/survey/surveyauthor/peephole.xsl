<?xml version="1.0" encoding="utf-8"?>
<!--     Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.     This software is the confidential and proprietary information of    Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not    disclose such Confidential Information and shall use it only in    accordance with the terms of the license agreement you entered into    with IBS-DP or its authorized distributors.     IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY    OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT    LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A    PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE    FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING    OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
    <xsl:include href="common.xsl" />
    <xsl:include href="../../global/toolbar.xsl"/>
    <xsl:param name="tarChannel">default</xsl:param>
    <xsl:param name="xmlURL">default</xsl:param>
    <xsl:param name="mode">all</xsl:param>
    <xsl:param name="publicExpand">default</xsl:param>
    <xsl:param name="privateExpand">default</xsl:param>
    <xsl:template match="survey-system">
        <!--
        <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>
        </textarea>         
		<textarea cols="100" rows="4">
			<xsl:copy-of select="." />
			****************************
			<xsl:value-of select="$privateExpand" />
			<xsl:value-of select="$publicExpand" />
			<xsl:value-of select="$mode" />
		</textarea>
		-->
		
	
		<xsl:choose>
			<xsl:when test="$publicExpand!=1 or $privateExpand!=1">
		
				<form action="{$baseActionURL}" method="post">
					<input type="hidden" name="sid" value="{$sid}" />
					
					
					<div class="survey-tab-container">
						<xsl:choose>
							<xsl:when test="$privateExpand!=1">
								<div class="survey-tab-selected">
									Personal Surveys
								</div>
							</xsl:when>
							<xsl:otherwise>
								<div class="survey-tab">
									<a href="{$doURL}=privateExpand&amp;uP_root=me" title="View Personal Surveys">Personal Surveys</a>
								</div>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$publicExpand!=1">
								<div class="survey-tab-selected">
									Shared Surveys
								</div>
							</xsl:when>
							<xsl:otherwise>
								<div class="survey-tab">
									<a href="{$doURL}=publicExpand&amp;uP_root=me" title="View Shared Surveys">Shared Surveys</a>
								</div>
							</xsl:otherwise>
						</xsl:choose>	
						<div style="float:right" align="right">
							<a href="{$baseActionURL}?channel_command=refresh" title="Refresh Survey List">Refresh</a>
						</div>
					</div>
					<div class="survey-tab-subtitle">
						<xsl:choose>
							<xsl:when test="$privateExpand=2">
								<a href="{$goURL}=Form&amp;Init=Yes&amp;Type=Private%20Survey&amp;uP_root=me" title="Build a Personal Survey">
									<img src="{$surveyImagePath}/create_personal_survey.gif" align="absmiddle" border="0" alt="Build a Personal Survey" title="Build a Personal Survey" />
								</a>
							</xsl:when>
							<xsl:otherwise>
								<a href="{$goURL}=Form&amp;Init=Yes&amp;Type=Public%20Survey&amp;uP_root=me" title="Build a Shared Survey">
									<img src="{$surveyImagePath}/create_shared_survey.gif" align="absmiddle" border="0" alt="Build a Shared Survey" title="Build a Shared Survey" />
								</a>
							</xsl:otherwise>
						</xsl:choose>
					</div>
					<div class="bounding-box6">
					
						<table width="100%" cellpadding="0" cellspacing="0" border="0">
							<xsl:choose>
								<xsl:when test="$privateExpand=2">
									<xsl:apply-templates select="Form[@Type='Private Survey']" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates select="Form[@Type='Public Survey']" />
								</xsl:otherwise>
							</xsl:choose>
						</table>
					</div>
				</form>
			</xsl:when>
			
			<xsl:otherwise>
				<div class="survey-peephole-container">
					<table cellpadding="0" cellspacing="0" border="0" align="center">
						<tr>
							<td class="personal-container" valign="top">
								<a href="{$doURL}=privateExpand&amp;uP_root=me" title="Personal Surveys">
									<img class="survey-icon" border="0" src="{$surveyImagePath}/survey_icon_personal.gif" title="Personal Surveys" />
									<div class="survey-spacer">View Personal Surveys</div>
								</a>
							</td>
							<td class="personal-container" valign="top">
								<a href="{$doURL}=publicExpand&amp;uP_root=me" title="Personal Surveys">
									<img class="survey-icon" border="0" src="{$surveyImagePath}/survey_icon_shared.gif" title="Personal Surveys" />
									<div class="survey-spacer">View Shared Surveys</div>
								</a>
							</td>
						</tr>
					</table>
				</div>
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>
    
    <xsl:template match="Form">
        <tr>
            <td colspan="2">
                <img height="15" width="1" src="{$SPACER}" border="0" alt="" title="" />
            </td>
        </tr>
        <tr>
            <td class="survey-form-td" align="left" valign="middle" nowrap="nowrap" colspan="2">
                <xsl:attribute  name = "id" >
                    <xsl:choose>
                        <xsl:when test="Survey">SSA-FormPersonal</xsl:when>
                        <xsl:otherwise>SSA-FormShared</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute  name = "headers" >
                    <xsl:choose>
                        <xsl:when test="Survey">SSA-Personal SSA-Type</xsl:when>
                        <xsl:otherwise>SSA-Shared SSA-Type</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                <xsl:choose>
                    <xsl:when test="Survey">
                    	<table cellpadding="0" cellspacing="0">
                    		<tr>
                    			<td valign="middle">
									<a href="{$goURL}=Sample&amp;FormId={@FormId}" title="View Sample">
										<img src="{$surveyImagePath}/survey_icon2.gif" border="0" align="left" alt="" title="" />                        	    
									</a>
                        		</td>
                        		<td class="whitelink" valign="middle">
									<a href="{$goURL}=Sample&amp;FormId={@FormId}" title="View Sample">
										<xsl:value-of select="@Title" />
									</a>
								</td>
							</tr>
						</table>
                    </xsl:when>
                    <xsl:otherwise>
                    	<table cellpadding="0" cellspacing="0">
                    		<tr>
                    			<td valign="middle">
									<a href="{$goURL}=Form&amp;Init=Yes&amp;FormId={@FormId}" title="Edit" >
										<img src="{$surveyImagePath}/survey_icon2.gif" border="0" align="left" alt="" title="" />
									</a>
                        		</td>
                        		<td class="whitelink" valign="middle">
									<a href="{$goURL}=Form&amp;Init=Yes&amp;FormId={@FormId}" title="Edit">
										<xsl:value-of select="@Title" />
									</a>
								</td>
							</tr>
						</table>  
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td class="survey-form-td" align="right" nowrap="nowrap" colspan="2">
            	<div class="survey-form-links whitelink">
					<xsl:if test="$mode = 'all' or $mode = 'author'">
						<a href="{$doURL}=Distribution&amp;Init=Yes&amp;FormId={@FormId}" title="To publish this survey">Publish Survey</a>
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />|
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
						<a href="{$goURL}=Sample&amp;FormId={@FormId}" title="To preview this survey as it will appear">Preview Survey</a>
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />|
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
						<a href="{$goURL}=Source&amp;FormId={@FormId}&amp;return=Peephole" title="To preview this survey as it will appear">View Survey XML</a>
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />|
						<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
						<a href="{$doURL}=confirmDeleteForm&amp;methodName=deleteForm&amp;FormId={@FormId}" title="To delete this survey">Delete Survey</a>
					</xsl:if>
				</div>
            </td>
        </tr>
        <xsl:apply-templates select="Survey" mode="detail">
            <xsl:with-param name="FormId" select="@FormId" />
        </xsl:apply-templates>
        <xsl:if test="not(Survey)">
            <tr>
                <td colspan="99" class="survey-survey-td-bottom">
                    No Surveys Published Yet
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Survey" mode="detail">
        <xsl:param name="FormId" />
        <tr>
        	<xsl:if test="position() mod 2 = 0">
                <xsl:attribute name="style">background-color: #D6E6F5;</xsl:attribute>
            </xsl:if>
            
            <td class="survey-survey-td-col1" nowrap="nowrap" valign="middle" >
            	<xsl:if test="position() = last()">
					<xsl:attribute name="style">border-bottom: 1px solid #477BAD;</xsl:attribute>
            	</xsl:if>
                
                <xsl:choose>
                    <xsl:when test="@Closed">
                        <xsl:choose>
                            <xsl:when test="$mode = 'all' or $mode = 'results'">
                                <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                                <a href="{$goURL}=Results&amp;Init=Yes&amp;FormId={../@FormId}&amp;SurveyId={@SurveyId}" title="Display results of">
                                    <xsl:choose>
                                        <xsl:when test="@DistributionTitle!=''">
                                            <xsl:value-of select="@DistributionTitle"/>
                                            &#160;-&#160;
                                            <xsl:value-of select="substring-before(@Closed,'_')" />
                                            &#160;
                                            <xsl:call-template name="t24to12">
                                                <xsl:with-param name="hour" select="substring-after(@Closed,'_')" />
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="substring-before(@Sent,'_')" />
                                            &#160;
                                            <xsl:call-template name="t24to12">
                                                <xsl:with-param name="hour" select="substring-after(@Sent,'_')" />
                                            </xsl:call-template>
                                            &#160;-&#160;
                                            <xsl:value-of select="substring-before(@Closed,'_')" />
                                            &#160;
                                            <xsl:call-template name="t24to12">
                                                <xsl:with-param name="hour" select="substring-after(@Closed,'_')" />
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="substring-before(@Sent,'_')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$mode = 'all' or $mode = 'results'">
                                <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                                <a href="{$goURL}=Results&amp;Init=Yes&amp;FormId={../@FormId}&amp;SurveyId={@SurveyId}" title="Display results of">
                                    <xsl:choose>
                                        <xsl:when test="@DistributionTitle!=''">
                                            <xsl:value-of select="@DistributionTitle"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="substring-before(@Sent,'_')" />
                                            &#160;
                                            <xsl:call-template name="t24to12">
                                                <xsl:with-param name="hour" select="substring-after(@Sent,'_')" />
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="substring-before(@Sent,'_')" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td class="survey-survey-td" nowrap="nowrap" valign="middle" >
            	<xsl:if test="position() = last()">
					<xsl:attribute name="style">border-bottom: 1px solid #477BAD;</xsl:attribute>
            	</xsl:if>
                          
            	<div>
            		Published on <xsl:value-of select="substring-before(@Sent,'_')"/>
            	</div>
            </td>
            <td class="survey-survey-td" nowrap="nowrap" valign="middle" >
            	<xsl:if test="position() = last()">
					<xsl:attribute name="style">border-bottom: 1px solid #477BAD;</xsl:attribute>
            	</xsl:if>
                          
                <div>
                    <xsl:choose>
                        <xsl:when test="@Type='Poll'">
                            <xsl:value-of select="format-number(@Replied,'#,###')" /> users have taken this poll
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="format-number(@Replied,'#,###')" /> users have taken this survey
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </td>
            <td class="survey-survey-td" nowrap="nowrap" valign="middle" align="right" >
            	<xsl:if test="position() = last()">
					<xsl:attribute name="style">border-bottom: 1px solid #477BAD;</xsl:attribute>
            	</xsl:if>
                          
                <xsl:choose>
                    <xsl:when test="@Closed">
                        <xsl:choose>
                            <xsl:when test="$mode = 'all' or $mode = 'results'">
                                <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                                <a href="{$goURL}=Export&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}&amp;Type={@Type}" title="">
                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_active.gif" align="absmiddle" name="ExportResults{@FormId}{@SurveyId}" alt="Export survey results" title="Export survey results" />Export
                                </a>
                                <img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
                                <a href="#" onclick="window.open('{$xmlURL}?tarChannel={$tarChannel}&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}','','location=no,status=no,scrollbars=yes,resizable=yes,menubar=yes');" title="">
                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_print_active.gif" align="absmiddle" name="PrintResults{@FormId}{@SurveyId}" alt="Format to print survey results" title="Format to print survey results" />Print
                                </a>
                                <img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="t24to12">
                                    <xsl:with-param name="hour" select="substring-after(@Closed,'_')" />
                                </xsl:call-template>
                                <img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="$mode = 'all' or $mode = 'author'">
                            <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                            <a href="{$doURL}=confirmDeleteDistribution&amp;methodName=deleteDistribution&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}" title="">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif" align="absmiddle" name="deleteMessage{@FormId}{@SurveyId}" alt="Delete survey" title="Delete survey" />Delete
                            </a>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$mode = 'all' or $mode = 'results'">
                                <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                                <a href="{$goURL}=Export&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}&amp;Type={@Type}" title="">
                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_active.gif" align="absmiddle" name="ExportResults{@FormId}{@SurveyId}" alt="Export survey results" title="Export survey results" />Export
                                </a>
                                <img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
                                <a href="#" onclick="window.open('{$xmlURL}?tarChannel={$tarChannel}&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}','','location=no,status=no,scrollbars=yes,resizable=yes,menubar=yes');" title="">
                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_print_active.gif" align="absmiddle" name="PrintResults{@FormId}{@SurveyId}" alt="Format to print survey results" title="Format to print survey results" />Print
                                </a>
                                <img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
                            </xsl:when>
                        </xsl:choose>
                        <xsl:if test="$mode = 'all' or $mode = 'author'">
                            <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                            <a href="{$doURL}=confirmCloseDistribution&amp;methodName=closeDistribution&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}" title="">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_xdistribute_active.gif" align="absmiddle" name="CloseDistribution{@FormId}{@SurveyId}" alt="Close distribution" title="Close distribution" />Close
                            </a>
                            <img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
                            <!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
                            <a href="{$doURL}=confirmDeleteDistribution&amp;methodName=deleteDistribution&amp;FormId={@FormId}&amp;SurveyId={@SurveyId}" title="">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif" align="absmiddle" name="deleteMessage{@FormId}{@SurveyId}" alt="Delete survey" title="Delete survey" />Delete
                            </a>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:template>


    <xsl:template name="t24to12">
        <xsl:param name="hour" />
        <xsl:variable name="h" select='substring-before($hour,":")' />
        <xsl:variable name="m" select='substring-after($hour,":")' />
        <xsl:choose>
            <xsl:when test="$h &gt; 12">
                <xsl:value-of select='concat($h - 12,":",$m," pm")' />
            </xsl:when>
            <xsl:when test="$h = 12">
                <xsl:value-of select='concat($h,":",$m," pm")' />
            </xsl:when>
            <xsl:when test="$h = 0">
                <xsl:value-of select='concat("12",":",$m," am")' />
            </xsl:when>
            <xsl:when test="12 &gt; $h &gt; 0">
                <xsl:value-of select='concat($h,":",$m," am")' />
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

