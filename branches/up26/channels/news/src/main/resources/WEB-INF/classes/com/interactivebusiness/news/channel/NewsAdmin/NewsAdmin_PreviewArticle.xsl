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
  2    Channels  1.1         4/26/2002 5:34:31 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:36:05 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_PreviewArticle</xsl:param>

    <xsl:variable name="imagepath">media/com/interactivebusiness/news/</xsl:variable>

    <!-- Start of XSL Code -->

    <xsl:template match="PreviewArticle">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
    
        <h2 class="page-title">Create/Edit Article</h2>

        <div style="margin:20px 0px;"><xsl:call-template name = "article-workflow" /></div>
        
        <div class="bounding-box1">
			<!--UniAcc: Layout Table -->
			<table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
				<tr>
					<td align="center" class="table-content-single">
						<img src="{$imagepath}news_banner.gif" border="0" alt="" title=""/>
					</td>
				</tr>            
				<xsl:apply-templates/>
				<tr>
					<td>
						<!--UniAcc: Layout Table -->
						<form action="{$baseActionURL}?action=SaveArticle&amp;admin=yes" method="post" name="previewItem">
						<table border="0" cellpadding="2" cellspacing="0" align="center" width="100%">
							<tr>
								<td class="table-light-single" style="text-align:center;">
									<input type="submit" name="back" value="Back" class="uportal-button"/>
									<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
									<input type="submit" name="next" value="Next" class="uportal-button"/>
									<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
									<input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=topic'" title="To cancel create/editing this article and return to Manage Articles" />
								</td>
							</tr>
						</table>
						</form>
					</td>
				</tr>
			</table>
		</div>
		
    </xsl:template>


    <xsl:template match="Article">
        <tr>
            <td>
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="3" cellspacing="0" align="center" width="100%">
                    <tr>
                        <td colspan="2" class="table-content-single">
                            <span class="uportal-channel-subtitle">
                                <xsl:value-of select="title"/>
                            </span>
                        </td>
                    </tr>

                    <xsl:choose>
						<xsl:when test="@layout = '1'">
                            <!-- layout 1 has image on right -->
                            <tr>
                                <td colspan="2" class="table-content-single">
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
                                <td colspan="2" class="table-content-single">
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
                                <td colspan="2" class="table-content-single">
                                    <xsl:apply-templates select="story"/>
                                </td>
                            </tr>
                        </xsl:otherwise>
                    </xsl:choose>
                    <tr>
                        <td colspan="2" class="table-content-single">
                            <span class="uportal-channel-error">This article will run from
                                <xsl:value-of select="begin_date"/> to <xsl:value-of select="end_date"/>
                            </span>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
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
