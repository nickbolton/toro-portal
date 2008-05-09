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
  2    Channels  1.1         4/26/2002 5:34:33 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:36:08 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_TopicsMenu</xsl:param>

    <xsl:variable name="imagepath">media/com/interactivebusiness/news/</xsl:variable>

    <xsl:template match="TopicsMenu">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">Manage Article Topics</h2>
        
        <div class="bounding-box1" style="margin-left:10px;margin-top:20px;">        
        
			<p><a href="{$baseActionURL}?action=CreateTopic&amp;admin=yes" title="To create a new article topic." >Add a New Topic</a></p>

			<!--<form action="{$baseActionURL}?action=TopicSearch&amp;admin=yes" method="post" name="topics">
				<p>
				<label for="News-AdminTopicSearchT1">Show Topics:</label>
				<input type="text" size="25" maxlength="30" name="topic_to_find" class="uportal-input-text" id="News-AdminTopicSearchT1"/>
				<input type="submit" value="Show" class="uportal-button"/>
				</p>
			</form> -->

			<xsl:apply-templates/>

		</div>
    </xsl:template>


    <xsl:template match="search_results">
		<form action="{$baseActionURL}?admin=yes&amp;action=DeleteTopic" name="DeleteTopic" method="post">
        <div style="margin:20px 0px;">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
        	<!-- Headings -->
			<xsl:if test="child::topic">
				<tr>
					<td class="th-top-left" width="5%">
						<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
					</td>
					<td class="th-top" scope="col" width="25%">Topic Name</td>
					<td class="th-top" scope="col" width="60%">Topic Description</td>
					<td class="th-top-right" scope="col" width="10%" style="text-align:center;">Articles</td>
				</tr>
			</xsl:if>
			<!-- Article Rows -->
			<xsl:apply-templates/>
			<!-- Select / Deselect Row -->
			<xsl:if test="child::topic">
				<tr>
					<td align="left" colspan="4"  class="table-content-single">
						<a href="{$baseActionURL}?action=TopicSearch&amp;checkbox=all&amp;admin=yes" title="To select all displayed topics for deletion.">Select All</a> | <a href="{$baseActionURL}?action=TopicSearch&amp;checkbox=none&amp;admin=yes" title="To deselect all displayed topics." >Deselect All</a> 
					</td>
				</tr>
				<!-- Delete button -->
				<tr>
					<td class="table-light-single" style="text-align:center;" colspan="4">
						<input type="submit" value="Delete" class="uportal-button"/>
					</td>
				</tr>
			</xsl:if>
        </table>
        </div>
		</form>
    </xsl:template>


    <xsl:template match="topic">
    	<xsl:variable name = "rowStyle"><xsl:choose>
    			<xsl:when test="position() mod 2 = 0">alt-row-even</xsl:when>
    			<xsl:otherwise>alt-row-odd</xsl:otherwise>
		</xsl:choose></xsl:variable>
        <xsl:variable name="topicID">
            <xsl:value-of select="@value"/>
        </xsl:variable>
        <tr>
            <td class="table-light-left {$rowStyle}" style="vertical-align:top;">
                <xsl:if test="@checked = 'checked'">
                    <input type="checkbox" name="{$topicID}" value="checked" checked="checked" id="CampusNewsChannelTopic_{$topicID}"/>
                </xsl:if>
                <xsl:if test="@checked = 'no'">
                    <input type="checkbox" name="{$topicID}" value="checked" id="CampusNewsChannelTopic_{$topicID}"/>
                </xsl:if>
            </td>
	         <td class="table-content {$rowStyle}" style="white-space:nowrap;vertical-align:top;">
	            <label for="CampusNewsChannelTopic_{$topicID}">
	            	<xsl:value-of select="topic_name" />
	            </label>
	            <img src="{$SPACER}" border="0" height="5" width="5" alt="" title=""/>
				<a href="{$baseActionURL}?action=EditTopic&amp;topicID={$topicID}&amp;admin=yes"
				onmouseover="swapImage('NAdminEditTopicImage{$topicID}','channel_edit_active.gif')"
				onmouseout="swapImage('NAdminEditTopicImage{$topicID}','channel_edit_base.gif')">
					<img src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
					id="NAdminEditTopicImage{$topicID}" name="NAdminEditTopicImage{$topicID}" 
					border="0" alt="Edit This Topic" title="Edit This Topic" align="middle"/>
				</a>
				<img src="{$SPACER}" border="0" height="5" width="5" alt="" title=""/>
				<a href="{$baseActionURL}?action=DeleteTopic&amp;topicID={$topicID}&amp;admin=yes"
				onmouseover="swapImage('NAdminDeleteTopicImage{$topicID}','channel_delete_active.gif')"
				onmouseout="swapImage('NAdminDeleteTopicImage{$topicID}','channel_delete_base.gif')">
					<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
					id="NAdminDeleteTopicImage{$topicID}" name="NAdminDeleteTopicImage{$topicID}" 
					border="0" alt="Delete This Topic" title="Delete This Topic" align="middle"/>
				</a>
	        </td>
            <td class="table-content {$rowStyle}" style="vertical-align:top;">
                <xsl:value-of select="description"/>
            </td>

            <td class="table-content-right {$rowStyle}" style="text-align:center;vertical-align:top;">
                <span class="uportal-channel-error"><xsl:value-of select="numberofarticles"/></span>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="Empty">
        <tr>
            <td colspan="4" class="table-content-single">
                <span class="uportal-channel-warning"><xsl:value-of select="." /></span>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
