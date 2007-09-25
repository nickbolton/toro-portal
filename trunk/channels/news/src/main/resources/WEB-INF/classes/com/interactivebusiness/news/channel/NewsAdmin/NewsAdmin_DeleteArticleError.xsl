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
  1    Channels  1.0         4/1/2002 10:36:04 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_DeleteArticleError</xsl:param>

    <!-- Start of XSL Code -->
    <xsl:template match="DeleteError">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">Delete Error</h2>
        <!--UniAcc: Layout Table -->
        <div class="bounding-box1">
			<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
				<tr>
					<td class="table-content-right">
						<span class="uportal-channel-warning">An error occured:</span> Your Article was not deleted.
					</td>
				</tr>

				<tr>
					<td class="table-content-single">
						Reason: <xsl:apply-templates select="error"/>
					</td>
				</tr>
				<tr>
					<td class="table-nav">
						<form action="{$baseActionURL}?action=ArticleSearch&amp;admin=yes" method="post">
							<input type="submit" name="back" value="Back" class="uportal-button"/>
						</form>
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>
</xsl:stylesheet>
