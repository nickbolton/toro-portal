<?xml version="1.0"?>
<!--
 
 Copyright (c) 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 This software is the confidential and proprietary information of
 Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 You shall not disclose such Confidential Information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with IBS.
 
 IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 THIS SOFTWARE OR ITS DERIVATIVES.

 $Log: 
  2    Channels  1.1         4/26/2002 5:34:37 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:35:48 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>
    <xsl:param name="pageName">View_Full_Article</xsl:param>

    <xsl:template match="View_Full">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">View Article</h2>
        
        <div class="bounding-box1">
        
			<!--UniAcc: Layout Table -->
			<table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
				<tr valign="middle">
					<xsl:apply-templates select="nextprevbar"/>
				</tr>
				<xsl:apply-templates select="fullarticle"/>
			</table>
		</div>
    </xsl:template>


    <xsl:template match="fullarticle">
        <tr>
            <td class="table-light-single">
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
                    <tr>
                        <td align="center" colspan="2" class="table-content-single-top">
                            <span class="uportal-channel-subtitle">
                                <xsl:value-of select="title"/>
                            </span>
                        </td>
                    </tr>
                    <tr>
                        <td align="left" colspan="2" class="table-content-single">
							<xsl:value-of select="begindate"/>
                        </td>
                    </tr>
                    <xsl:choose>
                        <xsl:when test="@layout = '1'">
                            <!-- layout 1 has image on right -->
                            <tr>
                                <td colspan="2" class="table-content-single-bottom">
                                    <!--imagefile/@imageTitle Does not exist yet, but ADA compliance requires some descriptor of the image from xml/java -->
                                    <xsl:apply-templates select="story">
                                        <xsl:with-param name="imagefile" select="imagefile"/>                                                    
                                        <xsl:with-param name="imagefile" select="imagefile/@imageTitle"/>
                                        <xsl:with-param name="layout" select="@layout"/>
                                    </xsl:apply-templates>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="@layout = '2'">
                            <!-- layout 2 has image on left -->
                            <tr>
                                <td colspan="2" class="table-content-single-bottom">
                                    <!--imagefile/@imageTitle Does not exist yet, but ADA compliance requires some descriptor of the image from xml/java -->
                                    <xsl:apply-templates select="story">
                                        <xsl:with-param name="imagefile" select="imagefile"/>
                                        <xsl:with-param name="imagefile" select="imagefile/@imageTitle"/>
                                        <xsl:with-param name="layout" select="@layout"/>
                                    </xsl:apply-templates>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- layout 3 no image -->
                            <tr>
                                <td colspan="2" class="table-content-single-bottom">
                                    <xsl:apply-templates select="story"/>
                                </td>
                            </tr>
                        </xsl:otherwise>
                    </xsl:choose>
                </table>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="nextprevbar">

        <td align="left" valign="middle" class="table-content-single">
            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
            <xsl:apply-templates select="previous"/>
            <xsl:apply-templates select="previous_disabled"/>
            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
            <font class="uportal-channel-text">
                <xsl:value-of select="viewing/@numberviewing"/>/<xsl:value-of select="viewing/@total"/>
            </font>
            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
            <xsl:apply-templates select="next"/>
            <xsl:apply-templates select="next_disabled"/>
        </td>
    </xsl:template>


    <xsl:template match="previous">
        <a href="{$baseActionURL}?action=view_full_article&amp;articleID={@firstArticle}&amp;view=nextprev"
		onmouseover="swapImage('CNewsFirstPageImage','channel_page_first_active.gif')"
		onmouseout="swapImage('CNewsFirstPageImage','channel_page_first_base.gif')">
            <img src="{$CONTROLS_IMAGE_PATH}/channel_page_first_base.gif"
			id="CNewsFirstPageImage" name="CNewsFirstPageImage" 
			border="0" alt="Go to First Article" title="Go to First Article"/>
        </a>

        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>

        <a href="{$baseActionURL}?action=view_full_article&amp;articleID={@previousArticle}&amp;view=nextprev"
		onmouseover="swapImage('CNewsPreviousPageImage','channel_page_prev_active.gif')"
		onmouseout="swapImage('CNewsPreviousPageImage','channel_page_prev_base.gif')">
            <img src="{$CONTROLS_IMAGE_PATH}/channel_page_prev_base.gif"
			id="CNewsPreviousPageImage" name="CNewsPreviousPageImage" 
			border="0" alt="Go to Previous Article" title="Go to Previous Article"/>
        </a>
    </xsl:template>

    <xsl:template match="next">
        <a href="{$baseActionURL}?action=view_full_article&amp;articleID={@nextArticle}&amp;view=nextprev"
		onmouseover="swapImage('CNewsNextPageImage','channel_page_next_active.gif')"
		onmouseout="swapImage('CNewsNextPageImage','channel_page_next_base.gif')">
            <img src="{$CONTROLS_IMAGE_PATH}/channel_page_next_base.gif"
			id="CNewsNextPageImage" name="CNewsNextPageImage"
			border="0" alt="Go to Next Article" title="Go to Next Article"/>
        </a>
		
        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
		
        <a href="{$baseActionURL}?action=view_full_article&amp;articleID={@lastArticle}&amp;view=nextprev"
		onmouseover="swapImage('CNewsLastPageImage','channel_page_last_active.gif')"
		onmouseout="swapImage('CNewsLastPageImage','channel_page_last_base.gif')">
            <img src="{$CONTROLS_IMAGE_PATH}/channel_page_last_base.gif"
			id="CNewsLastPageImage" name="CNewsLastPageImage"
			border="0" alt="Go to Last Article" title="Go to Last Article"/>
        </a>
    </xsl:template>

    <xsl:template match="previous_disabled">
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_first_inactive.gif" border="0" alt="First article currently displayed" title="First article currently displayed"/>
        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_prev_inactive.gif" border="0" alt="First article currently displayed" title="First article currently displayed"/>
    </xsl:template>

    <xsl:template match="next_disabled">
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_next_inactive.gif" border="0" alt="Last article currently displayed" title="Last article currently displayed"/>
        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_last_inactive.gif" border="0" alt="Last article currently displayed" title="Last article currently displayed"/>
    </xsl:template>

    <xsl:template match="story">
        <xsl:param name="imagefile"/>
        <xsl:param name="imageTitle"/>
        <xsl:param name="layout"/>

        <div class="uportal-channel-text">
			<xsl:choose>
            <xsl:when test="$layout = '1'">
			<!-- layout 1 has image on right -->
				<!--$imageTitle Does not exist yet, but ADA compliance requires some descriptor of the image from xml/java -->
				<img src="{$resourceURL}?fileName={$imagefile}" border="0" alt="{$imageTitle}" title="{$imageTitle}" class="img-float-right" />
            </xsl:when>
            <xsl:when test="$layout = '2'">
			<!-- layout 2 has image on left -->
				<!--$imageTitle Does not exist yet, but ADA compliance requires some descriptor of the image from xml/java -->
				<img src="{$resourceURL}?fileName={$imagefile}" border="0" alt="{$imageTitle}" title="{$imageTitle}" class="img-float-left" />
            </xsl:when>
			</xsl:choose>

			<xsl:for-each select="paragraph">
				<p><xsl:apply-templates/></p>
			</xsl:for-each>
        </div>
    </xsl:template>
	
</xsl:stylesheet>
