<?xml version="1.0" encoding="utf-8"?>
<!--     Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.     This software is the confidential and proprietary information of    Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not    disclose such Confidential Information and shall use it only in    accordance with the terms of the license agreement you entered into    with IBS-DP or its authorized distributors.     IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY    OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT    LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A    PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE    FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING    OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" />
<!-- for all screens -->
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="resourceURL">default</xsl:param>
<xsl:param name="sid">default</xsl:param>
<xsl:param name="targetChannel">default</xsl:param>
<xsl:param name="root">false</xsl:param>
<xsl:param name="focusedChannel">
    <xsl:value-of select="$targetChannel" />
</xsl:param>
<xsl:param name="baseImagePath">media/net/unicon/portal/channels/rad</xsl:param>
<xsl:param name="backRoot" />
<xsl:param name="back">default</xsl:param>
<xsl:param name="goURL">default</xsl:param>
<xsl:param name="doURL">default</xsl:param>
<!-- current date -->
<xsl:param name="cur-date">default</xsl:param>
<!-- used only for navigation-bar -->
<!-- Set Variable Names for image links -->
<xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
<xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
<xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
<xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/flash/academus</xsl:variable>
<xsl:variable name = "SPACER"><xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>
insertinclude
<xsl:param name="CurPage">1</xsl:param>
<xsl:param name="FormId">default</xsl:param>
<xsl:param name="SurveyId">default</xsl:param>
<xsl:param name="FlagForm">False</xsl:param>
<xsl:template match="survey-system">
    <xsl:variable name="MaxPage" select="count(Form[(Survey/@SurveyId)=$SurveyId]/XMLForm/page)" />
    <center>
        <form action="{$baseActionURL}" method="post" name="pollForm">
            <input type="hidden" name="sid" value="{$sid}" />
            <input name="submitPoll" type="hidden" value="" />
            <input type="hidden" name="MaxPage" value="{$MaxPage}" />
            <input type="hidden" name="page-id" value="{Form[(Survey/@SurveyId)=$SurveyId]/XMLForm/page[position()=$CurPage]/@page-id}" />
            <!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" width="100%" border="0" class="uportal-background-content">
                <tr>
                    <td nowrap="nowrap" class="th-top">
                    <xsl:choose>
	            			<xsl:when test="Form/Survey[@SurveyId=$SurveyId]/@DistributionTitle != ''">
	 							<xsl:value-of select="Form/Survey[@SurveyId=$SurveyId]/@DistributionTitle" /> 
	 						</xsl:when>	
	 						<xsl:otherwise>
		 						<xsl:value-of select="Form[@FormId=$FormId]/@Title" /> - 
 								<xsl:value-of select="substring-before(Form/Survey[@SurveyId=$SurveyId]/@Sent,'_')" />
			                    &#160;
			                    <xsl:call-template name="t24to12">
			                    	<xsl:with-param name="hour" select="substring-after(Form/Survey[@SurveyId=$SurveyId]/@Sent,'_')" />
			                    </xsl:call-template>
	 						</xsl:otherwise>
 						</xsl:choose>                            
                    </td>
                </tr>
                <tr>
                    <td class="table-content">
                        <xsl:apply-templates select="Form[(Survey/@SurveyId)=$SurveyId]/XMLForm/page[position()=$CurPage]">
                            <xsl:with-param name="response" select="view" />
                        </xsl:apply-templates>
                    </td>
                </tr>
                <tr>
                    <td class="table-content">
                    	<!--UniAcc: Layout Table -->
                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                            <tr>
                                <td valign="middle" width="1%" nowrap="nowrap" class="uportal-text">
                                    <xsl:if test="$CurPage != 1">
                                        <input type="submit" class="uportal-button" name="do" value="Previous" />
                                    </xsl:if>
                                    <xsl:if test="$CurPage != $MaxPage">
                                        <input type="submit" class="uportal-button" name="do" value="Next" />
                                    </xsl:if>
                                    <xsl:if test="$CurPage = $MaxPage">
                                        <input type="submit" class="uportal-button" name="do" value="Submit" />
                                    </xsl:if>
                                    <input type="submit" class="uportal-button" name="do" value="Results" />
                                    <xsl:if test="$FlagForm='False'">
                                    <label for="SSAPP-OthersS1">&#160;Others</label>
                                    <select name="Polls" size="1" class="text" id="SSAPP-OthersS1">
                                        <xsl:for-each select="Form">
                                            <xsl:choose>
                                                <xsl:when test="(Survey/@SurveyId=$SurveyId)">
                                                    <option id="{Survey/@SurveyId}" value="{Survey/@SurveyId}" selected="selected">
                                                        <xsl:choose>
									            			<xsl:when test="Survey/@DistributionTitle != ''">
	 															<xsl:value-of select="Survey/@DistributionTitle" /> 
	 														</xsl:when>	
	 														<xsl:otherwise>
		 														<xsl:value-of select="@Title" /> - 
		 														<xsl:value-of select="substring-before(Survey/@Sent,'_')" />
					                                            &#160;
					                                            <xsl:call-template name="t24to12">
					                                                <xsl:with-param name="hour" select="substring-after(Survey/@Sent,'_')" />
					                                            </xsl:call-template>
	 														</xsl:otherwise>
 														</xsl:choose>   
                                                    </option>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <option id="{Survey/@SurveyId}" value="{Survey/@SurveyId}">
                                                        <xsl:choose>
									            			<xsl:when test="Survey/@DistributionTitle != ''">
	 															<xsl:value-of select="Survey/@DistributionTitle" /> 
	 														</xsl:when>	
	 														<xsl:otherwise>
		 														<xsl:value-of select="@Title" /> - 
					                                            <xsl:value-of select="substring-before(Survey/@Sent,'_')" />
					                                            &#160;
					                                            <xsl:call-template name="t24to12">
					                                                <xsl:with-param name="hour" select="substring-after(Survey/@Sent,'_')" />
					                                            </xsl:call-template>
	 														</xsl:otherwise>
 														</xsl:choose>  
                                                    </option>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                    </select>
                                    &#160; 
                                    <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="do~selectPoll" id="SelectPoll" title="Enter" align="absmiddle" onmouseover="swapImage('SelectPoll','channel_view_active.gif')" onmouseout="swapImage('SelectPoll','channel_view_base.gif')" />
									<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
									<a href="{$baseActionURL}?channel_command=refresh" 
										onmouseover="swapImage('refreshPolls','channel_refresh_active.gif');" 
										onmouseout="swapImage('refreshPolls','channel_refresh_base.gif');"
										title="To refresh the poll list">
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_refresh_base.gif" 
									    	alt="'Refresh' icon to refresh the poll list" 
									    	title="'Refresh' icon to refresh the poll list" 
									    	align="absmiddle" name="refreshPolls" id="refreshPolls"/>
									</a>
                                    </xsl:if>
                                </td>
                                <td align="right" nowrap="nowrap" class="uportal-text">
                                <xsl:value-of select="$CurPage" />
                                / 
                                <xsl:value-of select="$MaxPage" />
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </center>
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

