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
  2    Channels  1.1         4/26/2002 5:34:35 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:35:46 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">News_Main</xsl:param>

    <xsl:template match="News_Main">
    
    	<xsl:call-template name = "autoFormJS" />
        
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">View My Articles</h2>
        
		<form action="#">
			
			<xsl:if test = "subscribedlist/child::Topic">
				<div class="show-abstract-toggle">
					<label>
						<input onclick="toggleNewsDescriptions(this.checked,this.form)" value="yes" name="showAbstracts" id="showAbstracts_CampusNewsChannel" type="checkbox" /> Show Article Abstract
					</label>
					<noscript> (Requires JavaScript) </noscript>
				</div>
			</xsl:if>
			<div class="bounding-box1">
				<xsl:apply-templates select="subscribedlist"/>
			</div>
		</form>
    </xsl:template>
    
    <!-- Configure News was commented out for TeamTrack 4401 on 11/04 -->
    <!-- <xsl:template match="configIcon">
        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
        <a href="{$baseActionURL}?uP_root=me&amp;action=configure_news">
            <img src="{$imagedir}/setup_16.gif" border="0" alt="Configure News Settings" title="Configure News Settings"/>
            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
            Configure News
        </a>
    </xsl:template> -->

    <xsl:template match="error">
        <span class="uportal-channel-error">
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="subscribedlist">
        <xsl:choose>
        	<xsl:when test="Empty">
                <p class="uportal-channel-text empty-message-box" style="width:200px;"><xsl:value-of select="Empty" /></p>  
                <p><a href="{$baseActionURL}?uP_root=me&amp;action=subscribe" title="To select other topics of interest.">Click here to select topics of interest.</a></p>
        	</xsl:when>
        	<xsl:when test="not(child::Topic)">
                <p class="uportal-channel-text empty-message-box" style="width:400px;">The topics you have subscribed to contain no articles.</p>  
                <p><a href="{$baseActionURL}?uP_root=me&amp;action=subscribe" title="To select other topics of interest.">Click here to subscribe to other topics of interest.</a></p>
        	</xsl:when>
        	<xsl:otherwise>
		        <ul class="news-topic-list">
		        	<xsl:apply-templates select="Topic"></xsl:apply-templates>
		        </ul>
        	</xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="Topic">
        <li>
            <span><xsl:value-of select="@name"/></span>
	        
	        <xsl:if test = "Article">
		        <ul class="news-article-list">
		        <xsl:for-each select="Article">
	                <li>
		                <a href="{$baseActionURL}?uP_root=me&amp;action=view_full_article&amp;articleID={@articleID}" title="{@abstract}">
		                    <xsl:value-of select="@title"/>
		                </a>
		                <p class="hide-description"><xsl:value-of select="@abstract" /></p>
	                </li>
		        </xsl:for-each>
		        </ul>
	        </xsl:if>
        </li>

    </xsl:template>

    <!--<xsl:template match="Empty">
        <p class="uportal-channel-text">
            <xsl:apply-templates/>
        </p>
    </xsl:template> -->
</xsl:stylesheet>
