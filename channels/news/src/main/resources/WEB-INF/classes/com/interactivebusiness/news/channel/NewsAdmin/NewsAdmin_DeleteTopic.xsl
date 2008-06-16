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
  1    Channels  1.0         4/1/2002 11:36:04 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_DeleteTopic</xsl:param>

    <xsl:template match="DeleteTopic">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
        
        <!--UniAcc: Layout Table -->
        <h2 class="page-title">Delete Topic</h2>
		<div class="bounding-box1">
			<table border="0" cellpadding="2" cellspacing="0" align="left" width="100%">
				<tr>
					<td class="table-content-single">
						<span class="uportal-channel-warning">Warning:</span>This operation will permanently delete the News Topic(s) you have selected. Are you sure you want to proceed with the delete?
					</td>
				</tr>            
				<xsl:apply-templates/>
			</table>
		</div>
    </xsl:template>


    <xsl:template match="DeleteItem">
        <tr>
            <td colspan="2" class="table-content-single">
				<strong>Selected items:</strong>
            </td>
        </tr>
        <tr>
            <td colspan="2" class="table-content-single">
				<xsl:apply-templates/>
            </td>
        </tr>

        <tr>
            <td colspan="2" class="table-nav">
				<form action="{$baseActionURL}?action=DeleteTopicConfirmed&amp;admin=yes" method="post" name="confirmdelete">
					<input type="submit" name="ok" value="Delete" class="uportal-button" title="To delete the selected items and return to Manage Article Topics."/>
					<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
					<input type="submit" name="cancel" value="Cancel" class="uportal-button" title="To return to Manage Article Topics without deleting the selected items."/>
				</form>
			</td>
        </tr>
    </xsl:template>

    <xsl:template match="DeleteList">
        <tr>
            <td colspan="2" class="table-content-single">
                <strong>Selected items:</strong>
            </td>
        </tr>
        <tr>
            <td class="table-content-single">
                <table>
                    <xsl:apply-templates/>
                </table>
            </td>
        </tr>        
        <xsl:if test="child::topicID">
            <tr>
                <td colspan="2" class="table-nav">
					<form action="{$baseActionURL}?action=DeleteTopicConfirmed&amp;admin=yes" method="post" name="confirmdelete">
						<input type="submit" name="ok" value="Delete" class="uportal-button"/>
						<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
						<input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
					</form>
                </td>
            </tr>
        </xsl:if>
        <xsl:if test="child::NoItemsChecked">
            <tr>
                <td colspan="2" class="table-nav">
					<form action="{$baseActionURL}?action=TopicSearch&amp;admin=yes" method="post" name="confirmdelete">
						<input type="submit" name="back" value="Back" class="uportal-button"/>
					</form>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="topicID">
        <tr>
            <td colspan="2" class="table-content-single">
				<xsl:value-of select="@name"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="NoItemsChecked">
        <tr>
            <td colspan="2" class="table-content-single">
				<xsl:apply-templates/>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
