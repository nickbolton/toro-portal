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
<xsl:param name="Reply">default</xsl:param>
<xsl:param name="Replier">default</xsl:param>
<xsl:param name="ReplierId">default</xsl:param>
<xsl:param name="ReplierStep">default</xsl:param>
<xsl:param name="Step">default</xsl:param>
<xsl:variable name="summary" select="survey-system/Form/Survey/Summary" />
<xsl:param name="mode">summary</xsl:param>


<xsl:template match="survey-system">
    <xsl:variable name="MaxReplier" select="Form/Survey/@Replied" />
    <xsl:variable name="MaxReplierName" select="count(RecipientName/Identity)" />
    <xsl:variable name="MaxPage" select="count(Form/XMLForm/page)" />

		<h2 class="page-title">Results Summary</h2>			
		<div class="bounding-box1">     
    	<!--UniAcc: Layout Table -->
        <table cellpadding="0" cellspacing="0" width="100%" border="0">
            <xsl:if test="$MaxReplier != 0">
                <tr>
                    <td>
                        <xsl:choose>
                            <xsl:when test="$mode != 'summary'">
                            	
                            	<div class="page-toolbar-container">
                            		<div class="tool">
										<a href="{$doURL}=changeMode&amp;mode=summary" title="Display">
											<img src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" border="0" align="absmiddle" name="SurveyResultSummary" id="SurveyResultSummary" alt="" title="" />
											View Summary
										</a>
									</div>
									<div class="tool">
										<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" align="absmiddle" alt="Summary" title="Summary" />
										<xsl:text>View Individual</xsl:text>
									</div>
								</div>
                            	
                            </xsl:when>
                            <xsl:otherwise>
                            
                            	<div class="page-toolbar-container">
                            		<div class="tool">
										<img src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" border="0" align="absmiddle" name="SurveyResultSummary" id="SurveyResultSummary" alt="" title="" />
										View Summary
									</div>
									<div class="tool">
										<a href="{$doURL}=changeMode&amp;mode=individual" title="Display">
											<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" align="absmiddle" alt="Summary" title="Summary" />
											<xsl:text>View Individual</xsl:text>
										</a>
									</div>
								</div>                            

                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr>
            </xsl:if>
            <tr>
                <td nowrap="nowrap" class="th-top">Results :&#160; 
					<xsl:value-of select="Form/@Title" />
					,&#160; 
					<xsl:value-of select="substring-before(Form/Survey/@Sent,'_')" />
					&#160; 
					<xsl:call-template name="t24to12">
						<xsl:with-param name="hour" select="substring-after(Form/Survey/@Sent,'_')" />
					</xsl:call-template>
					<xsl:choose>
						<xsl:when test="Form/Survey/@Type='Poll'">, Sent :&#160;Poll,&#160;Replied :&#160; 
						<xsl:value-of select="format-number($MaxReplier,'#,###')" />
						</xsl:when>
						<xsl:otherwise>, Sent :&#160; 
						<xsl:value-of select="format-number(Form/Survey/@TargetSize,'#,###')" />
						,&#160;Replied :&#160; 
						<xsl:value-of select="format-number($MaxReplier,'#,###')" />
						</xsl:otherwise>
					</xsl:choose>
                </td>
            </tr>
            <tr>
                <td>
                    <xsl:choose>
                        <xsl:when test="$mode != 'summary'">
                        	<!--UniAcc: Layout Table -->
                            <table cellpadding="0" cellspacing="0" width="100%" border="0" class="channel">
                                <tr>
                                    <td valign="top">
                                    	<!--UniAcc: Layout Table -->
                                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                                            <xsl:if test="$MaxReplier != 0">
                                                <tr>
                                                    <td nowrap="nowrap" class="table-content-left">
                                                        <form action="{$baseActionURL}" method="post" name="SurveyFindForm" id="SurveyFindForm">
                                                            <input type="hidden" name="sid" value="{$sid}" />
                                                            <input type="hidden" name="MaxReplier" value="{$MaxReplier}" />
                                                            <input name="submitFind" type="hidden" value="" id="SurveySubmitFind" />
                                                        	<label for="SSAAR-FindReplyT1">Find reply&#160;</label>
	                                                        <input type="text" size="5" name="Reply" class="text" id="SSAAR-FindReplyT1">
	                                                            <xsl:if test="$Reply != 'default'">
	                                                                <xsl:attribute name="value">
	                                                                    <xsl:value-of select="$Reply" />
	                                                                </xsl:attribute>
	                                                            </xsl:if>
	                                                        </input>
	                                                        &#160; 
	                                                        <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="do~locateReply" id="SurveyResultFindReply" title="Find Reply" align="absmiddle" onmouseover="swapImage('SurveyResultFindReply','channel_view_active.gif')" onmouseout="swapImage('SurveyResultFindReply','channel_view_base.gif')" />
                                                        </form>
                                                     <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                                     <xsl:choose>
                                                         <xsl:when test="$Reply &gt; 1">
                                                                <a href="javascript:document.getElementById('SurveySubmitFind').name='do~prevReply';document.getElementById('SurveyFindForm').submit()" title="Decrement">
                                                                 <img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
                                                             </a>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                             <img src="{$baseImagePath}/prev_disabled_12.gif" border="0" align="absmiddle" alt="disabled" title="disabled" />
                                                         </xsl:otherwise>
                                                     </xsl:choose>
                                                     <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                                     <xsl:choose>
                                                         <xsl:when test="$Reply &lt; $MaxReplier and $Reply &gt; 0">
                                                             <a href="javascript:document.getElementById('SurveySubmitFind').name='do~nextReply';document.getElementById('SurveyFindForm').submit()" title="Increment">
                                                                 <img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
                                                             </a>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                             <img src="{$baseImagePath}/next_disabled_12.gif" border="0" align="absmiddle" alt="disabled" title="disabled" />
                                                         </xsl:otherwise>
                                                     </xsl:choose>
                                                    </td>
                                                </tr>
                                                <xsl:if test="Form/Survey/@Type='Named'">
                                                <tr>
                                                    <td nowrap="nowrap" class="table-content-left">
                                                        <form action="{$baseActionURL}" method="post" name="SurveyFindReplier" id="SurveyFindReplier">
                                                            <input type="hidden" name="sid" value="{$sid}" />
                                                            <input type="hidden" name="MaxReplier" value="{$MaxReplier}" />
                                                            <input name="submitFind" type="hidden" value="" id="SurveySubmitFind" />
                                                        	<label for="SSAAR-FindReplierT1">Find replier&#160;</label>
                                                            <input type="text" size="15" name="ReplierName" class="uportal-input-text" id="SSAAR-FindReplierT1"/>
                                                            &#160; 
                                                            <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="do~locateReplierByName" id="SurveyResultFindReplier" title="Find Replier" align="absmiddle" onmouseover="swapImage('SurveyResultFindReplier','channel_view_active.gif')" onmouseout="swapImage('SurveyResultFindReplier','channel_view_base.gif')" />
                                                        </form>
                                                     <xsl:apply-templates select="RecipientName" />
                                                    </td>
                                                </tr>
                                                </xsl:if>
                                            </xsl:if>
                                            <tr>
                                                <td class="table-content-left-bottom">
                                                	<!--UniAcc: Layout Table -->
                                                    <table cellpadding="0" cellspacing="0" width="100%" border="0">
                                                        <tr>
                                                            <td>
                                                                <form action="{$baseActionURL}" method="post">
                                                                    <input type="hidden" name="sid" value="{$sid}" />
                                                                    <input type="submit" class="uportal-button" name="do" value="Close" />
                                                                </form>
                                                            </td>
                                                            <xsl:if test="Form/Survey/@Type='Named' and $MaxReplierName != 0">
                                                                <td align="right" nowrap="nowrap" class="uportal-text">
                                                                <xsl:choose>
                                                                    <xsl:when test="$ReplierStep &gt; 1">
                                                                        <a href="{$doURL}=previousReplier&amp;MaxReplier={$MaxReplier}" title="Display">
                                                                            <img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
                                                                        </a>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        <img src="{$baseImagePath}/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                                &#160; 
                                                                <xsl:value-of select="$ReplierStep" />
                                                                / 
                                                                <xsl:value-of select="floor(($MaxReplierName div $Step)+0.5)" />
                                                                &#160; 
                                                                <xsl:choose>
                                                                    <xsl:when test="$ReplierStep &lt; floor(($MaxReplierName div $Step)+0.5) and $ReplierStep &gt; 0">
                                                                        <a href="{$doURL}=nextReplier&amp;MaxReplier={$MaxReplier}" title="Display">
                                                                            <img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
                                                                        </a>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        <img src="{$baseImagePath}/next_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                                </td>
                                                            </xsl:if>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td>
                                    	<!--UniAcc: Layout Table -->
                                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                                            <xsl:if test="Form/Survey/@Type='Named'">
                                                <tr>
                                                    <td class="table-content-right">Replied by:&#160; 
                                                    <span class="uportal-label"><xsl:value-of select="Form/Survey/Recipient[@current]/@UserName" /></span>
                                                    </td>
                                                </tr>
                                            </xsl:if>
                                            <tr>
                                                <td class="table-content-right">
                                                    <xsl:choose>
                                                        <xsl:when test="$ReplierId='default'">
                                                            <xsl:apply-templates select="Form/XMLForm/page[position()=$CurPage]">
                                                                <xsl:with-param name="response" select="Form/Survey/Recipient[@current]" />
                                                                <xsl:with-param name="disabled">disabled</xsl:with-param>
                                                            </xsl:apply-templates>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:apply-templates select="Form/XMLForm/page[position()=$CurPage]">
                                                                <xsl:with-param name="response" select="Form/Survey/Recipient[@UserId=$ReplierId]" />
                                                                <xsl:with-param name="disabled">disabled</xsl:with-param>
                                                            </xsl:apply-templates>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="table-content-right" align="right" nowrap="nowrap">
                                                <xsl:choose>
                                                    <xsl:when test="$CurPage != 1">
                                                        <a href="{$doURL}=previous&amp;MaxPage={$MaxPage}&amp;CurPage={$CurPage}" title="Display">
                                                            <img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
                                                        </a>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <img src="{$baseImagePath}/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                &#160; 
                                                <xsl:value-of select="$CurPage" />
                                                / 
                                                <xsl:value-of select="$MaxPage" />
                                                &#160; 
                                                <xsl:choose>
                                                    <xsl:when test="$CurPage != $MaxPage">
                                                        <a href="{$doURL}=next&amp;MaxPage={$MaxPage}&amp;CurPage={$CurPage}" title="Display">
                                                            <img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
                                                        </a>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <img src="{$baseImagePath}/next_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </xsl:when>
                        <xsl:otherwise>
                        	<!--UniAcc: Layout Table -->
                            <table cellpadding="0" cellspacing="0" width="100%" border="0">
                                <tr>
                                    <td class="table-content-single">
                                        <xsl:apply-templates select="Form/XMLForm/page[position()=$CurPage]">
                                            <xsl:with-param name="response" select="view" />
                                            <xsl:with-param name="disabled">disabled</xsl:with-param>
                                        </xsl:apply-templates>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="table-nav" nowrap="nowrap">
                                    	<!--UniAcc: Layout Table -->
                                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                                            <tr>
                                                <td>
                                                    <form action="{$baseActionURL}" method="post">
                                                        <input type="hidden" name="sid" value="{$sid}" />
                                                        <input type="submit" class="uportal-button" name="do" value="Close" />
                                                    </form>
                                                </td>
                                                <td align="right" nowrap="nowrap" class="uportal-text">
                                                <xsl:choose>
                                                    <xsl:when test="$CurPage != 1">
                                                        <a href="{$doURL}=previous&amp;MaxPage={$MaxPage}&amp;CurPage={$CurPage}" title="Display">
                                                            <img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
                                                        </a>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <img src="{$baseImagePath}/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                &#160; 
                                                <xsl:value-of select="$CurPage" />
                                                / 
                                                <xsl:value-of select="$MaxPage" />
                                                &#160; 
                                                <xsl:choose>
                                                    <xsl:when test="$CurPage != $MaxPage">
                                                        <a href="{$doURL}=next&amp;MaxPage={$MaxPage}&amp;CurPage={$CurPage}" title="Display">
                                                            <img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
                                                        </a>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <img src="{$baseImagePath}/next_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </div>
</xsl:template>

<xsl:template match="RecipientName">
    <xsl:for-each select="Identity[position() &gt; $Replier - 1 and position() &lt; $Replier + $Step]">
    <br />
    <img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
    <img src="{$baseImagePath}/dot_black_6.gif" align="middle" border="0" alt="" title="" />
    &#160; 
    <a href="{$doURL}=locateReplierById&amp;ReplierId={@UserId}" title="Display results for">
        <xsl:value-of select="@UserName" />
    </a>
    </xsl:for-each>
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


