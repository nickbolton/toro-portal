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
  1    Channels  1.0         4/1/2002 11:35:47 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">Subscribe</xsl:param>

    <xsl:template match="Subscribe">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">Select Topics of Interest</h2>
        
		
		<div class="bounding-box1" style="margin:20px 0px;">    
			<form action="{$baseActionURL}?action=save_subscribed" method="post" name="subscribe">
				<!--UniAcc: Data Table -->
				<table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
					<tr>
						<td class="th-top-left">
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
						</td>
						<td width="30%" class="th-top" scope="col">Topic Name</td>
						<td width="60%" class="th-top" scope="col">Topic Description</td>
						<td width="10%" class="th-top-right" scope="col">Articles</td>
					</tr>
					<xsl:apply-templates/>
					<xsl:if test="//child::topic">
						<tr>
							<td align="left" colspan="4" class="table-content-single">
								<a href="{$baseActionURL}?action=subscribe&amp;checkbox=all" title="To select all displayed topics.">Select All</a> | <a href="{$baseActionURL}?action=subscribe&amp;checkbox=none" title="To deselect all displayed topics.">Deselect All</a>
								<br/>
							</td>
						</tr>
					</xsl:if>
					<tr>
						<td class="table-light-single" style="text-align:center;" colspan="4">
							<input type="submit" name="subscribe" value="Submit" class="uportal-button" title="To save your topic selections." />
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>


    <xsl:template match="topic">
    	<xsl:variable name = "rowStyle"><xsl:choose>
    			<xsl:when test="position() mod 2 = 0">alt-row-even</xsl:when>
    			<xsl:otherwise>alt-row-odd</xsl:otherwise>
		</xsl:choose></xsl:variable>
        <xsl:variable name="value">
            <xsl:value-of select="topic_name/@value"/>
        </xsl:variable>
        <xsl:variable name="checked">
            <xsl:value-of select="topic_name/@checked"/>
        </xsl:variable>
        <tr>
	        <td width="5%" class="table-content-left {$rowStyle}" style="vertical-align:top;">
	            <xsl:if test="$checked='checked'">
	                <input type="checkbox" name="{$value}" value="check" checked="checked" id="CNews-Subscribe{$value}C1"/>
	            </xsl:if>
	            <xsl:if test="$checked='no'">
	                <input type="checkbox" name="{$value}" value="check" id="CNews-Subscribe{$value}C1"/>
	            </xsl:if>
	        </td>
	        <td class="table-content {$rowStyle}" style="vertical-align:top;">
	            <label for="CNews-Subscribe{$value}C1"><xsl:value-of select="topic_name" /></label>
	        </td>
            <td class="table-content {$rowStyle}" style="vertical-align:top;">
                <xsl:value-of select="description"/>
            </td>

            <td class="table-content-right {$rowStyle}" style="text-align:center;vertical-align:top;">
                <span class="uportal-channel-error"><xsl:value-of select="article_count"/></span>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="Empty">
        <tr>
            <td align="center" colspan="4" class="table-content-single">
                <!--UniAcc: Layout Table -->
                <table border="1" width="100%" cellpadding="1" cellspacing="0">
                    <tr>
                        <td>
                            <font class="uportal-channel-text">
                                <xsl:apply-templates/>
                            </font>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
