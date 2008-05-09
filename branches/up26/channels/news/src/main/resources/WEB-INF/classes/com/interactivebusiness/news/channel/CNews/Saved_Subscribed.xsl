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
  2    Channels  1.1         4/26/2002 5:34:36 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:35:46 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">Saved_Subscribed</xsl:param>

    <xsl:template match="Saved">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">Selected News Topics</h2>
        
        <div class="bounding-box1">        
			<!--UniAcc: Layout Table -->
			<form action="{$baseActionURL}" method="post">
			<table border="0" cellpadding="2" cellspacing="0" width="100%" align="left" class="table-content">
				<tr>
					<td class="table-content-single">
						<xsl:apply-templates select="subscribed_list"/>
					</td>                        
				</tr>
				<tr>
					<td class="table-light-bottom" style="text-align:center;">
						<input type="submit" class="uportal-button" value="Finished" title="To return to View All Articles for these selected topics." />
					</td>
				</tr>
			</table>
			</form>
		</div>
    </xsl:template>

    <xsl:template match="subscribed_list">
        <xsl:for-each select="topic">
            <img src="{$imagedir}/minus.gif" border="0" height="10" width="10" alt="" title=""/>
            <xsl:apply-templates/>
            <br/>
        </xsl:for-each>
        <xsl:apply-templates select="Empty"/>
    </xsl:template>

    <xsl:template match="Empty">
        <xsl:apply-templates/>
    </xsl:template>
</xsl:stylesheet>
