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
  2    Channels  1.1         4/26/2002 5:34:32 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:36:06 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_SaveArticle</xsl:param>

    <!-- Start of XSL Code -->
    <xsl:template match="SaveArticle">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

		<h2 class="page-title">Save Confirmation</h2>

		<div class="bounding-box1">
			<!--UniAcc: Layout Table -->
			<table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
				<tr>
					<td>
						<!--UniAcc: Layout Table -->
						<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
							<tr>
								<td class="table-content-single">
									<xsl:if test="@saved = 'yes'">
										Thank you, Your news article <strong><xsl:apply-templates select="articlename"/></strong> has been saved.
									</xsl:if>
									<xsl:if test="@saved = 'no'">
										<span class="uportal-channel-warning">An error occured:</span> Your news article <strong><xsl:apply-templates select="articlename"/></strong> was not saved.
									</xsl:if>
								</td>
							</tr>
							<xsl:choose>
								<xsl:when test="@saved = 'no'">
									<tr>
										<td align="left" class="table-content-single">
											Reason: <xsl:apply-templates select="error"/>
										</td>
									</tr>
									<tr>
										<td class="table-nav">
											<form action="{$baseActionURL}?action=SaveArticleError&amp;admin=yes" method="post">
												<input type="submit" name="back" value="Back" class="uportal-button"/>
												<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
												<input type="submit" name="createanother" value="Create Another" class="uportal-button"/>
												<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
												<input type="button" name="exit" value="Exit" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=topic'" title="To exit the creating of articles and return to Manage Articles" />
											</form>
										</td>
									</tr>
								</xsl:when>
								<xsl:otherwise>
									<tr>
										<td class="table-nav">
											<form action="{$baseActionURL}?action=SaveArticleError&amp;admin=yes" method="post">
												<input type="submit" name="createanother" value="Create Another" class="uportal-button"/>
												<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
												<input type="button" name="exit" value="Exit" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=topic'" title="To exit the creating of articles and return to Manage Articles" />
											</form>
										</td>
									</tr>
								</xsl:otherwise>
							</xsl:choose>
						</table>
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>
</xsl:stylesheet>
