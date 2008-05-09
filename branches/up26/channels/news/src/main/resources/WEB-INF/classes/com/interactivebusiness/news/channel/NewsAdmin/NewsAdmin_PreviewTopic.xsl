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
    <xsl:param name="pageName">NewsAdmin_PreviewTopic</xsl:param>

    <!-- Start of XSL Code -->

    <xsl:template match="PreviewTopic">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
        
        <h2 class="page-title">Create/Edit Topics</h2>
        
        <xsl:call-template name = "topic-workflow" />
        
        <div class="bounding-box1">
        
			<form action="{$baseActionURL}?action=SaveTopic&amp;admin=yes" method="post" name="previewItem">
				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="0" cellspacing="0" align="left" width="100%" class="uportal-background-content">
					<tr>
						<td>
							<!--UniAcc: Layout Table -->
							<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
								<tr>
									<td align="left" colspan="4" class="table-content-single">
										<div class="uportal-channel-strong">This is how the topic will appear when in the subscription list:</div>
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/><br/>
									</td>
								</tr>
								<tr>
									<td colspan="4" class="table-content-single">
										<input type="checkbox" disabled="disabled" id="NewsAdmin-PreviewC1"/>&#160;
										<label for="NewsAdmin-PreviewC1"><xsl:apply-templates select="PreviewTopicXML"/></label>
									</td>
								</tr>
								<tr>
									<td align="left" colspan="4" class="table-content-single">
										<br/>
										<div class="uportal-channel-strong">Persons and Groups who share this topic (can create news articles for this topic):</div>
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/><br/>
									</td>
								</tr>                            
								<xsl:if test = "GroupsList/group">
									<tr>
										<xsl:apply-templates select="GroupsList"/>
									</tr>
								</xsl:if>
								<tr>
									<td colspan="4" class="table-nav">
										<input type="submit" name="back" value="Back" class="uportal-button" title="To return to Select Topic authors" />
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
										<xsl:apply-templates select="topicID"/>
										<xsl:apply-templates select="notopicID"/>
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
										<!-- if cancel go back to createtopic -->
										<input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=TopicSearch&amp;admin=yes'" title="To cancel create/editing this article topic and return to Manage Article Topics" />
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</form>
		</div>
		
    </xsl:template>

    <xsl:template match="PreviewTopicXML">
        <strong>
            <xsl:value-of select="topic"/>
        </strong>
        <img src="{$imagedir}/minus.gif" border="0" alt="" title=""/>
        <xsl:value-of select="description"/>
    </xsl:template>

    <xsl:template match="topicID">

        <xsl:variable name="topicID">
            <xsl:apply-templates/>
        </xsl:variable>
        <input type="hidden" name="topicID" value="{$topicID}"/>
        <input type="submit" name="next" value="Next" class="uportal-button"/>
    </xsl:template>

    <xsl:template match="notopicID">
        <input type="submit" name="next" value="Next" class="uportal-button" title="To submit this topic" />
    </xsl:template>

    <xsl:template match="GroupsList">
        <td align="left" colspan="4" class="table-content-single">
            <xsl:for-each select="group">
                <xsl:sort select="@entity"/>
                <xsl:choose>
                    <xsl:when test="@entity = '3'">
                        <img src="{$imagedir}/folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    </xsl:when>
                    <xsl:otherwise>
                        <img src="{$imagedir}/person_16.gif" border="0" alt="Person" title="Person"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    </xsl:otherwise>
                </xsl:choose>
                <b>
                    <xsl:value-of select="@name"/>
                </b>
                <br/>
            </xsl:for-each>
        </td>
    </xsl:template>
</xsl:stylesheet>
