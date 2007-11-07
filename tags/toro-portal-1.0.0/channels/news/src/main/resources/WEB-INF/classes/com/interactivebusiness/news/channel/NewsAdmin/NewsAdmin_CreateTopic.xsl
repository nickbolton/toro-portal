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
  2    Channels  1.1         4/26/2002 5:34:30 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:36:03 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_CreateTopic</xsl:param>

    <xsl:variable name="imagepath">media/com/interactivebusiness/news/</xsl:variable>

    <xsl:template match="CreateTopic">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
        
        <h2 class="page-title">Create/Edit Topics</h2>
        
        <xsl:call-template name = "topic-workflow" />
        
        <div class="bounding-box1">
        
			<xsl:call-template name="autoFormJS"/>

			<!--UniAcc: Layout TableA -->
			<table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
				<tr>
					<td>
						<!--UniAcc: Layout Table -->
						<form action="{$baseActionURL}?action=CheckTopicInput&amp;admin=yes" method="post" name="createtopic" 
							  onSubmit="return(checkNewsAdminFormSubmit(this));">
							<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%" class="table-content">
								<xsl:apply-templates/>
								<tr>
									<td class="table-light-single" style="text-align:center;">
										<input type="hidden" name="submitValue" value="Next"/>
										<input type="submit" name="next" value="Next" class="uportal-button" onclick="this.form.submitValue.value=this.value;" title="To go to Select Topic Authors for this topic" />
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
										<input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=TopicSearch&amp;admin=yes'" title="To cancel create/editing this article topic and return to Manage Article Topics" />
									</td>
								</tr>
							</table>
						</form>
					</td>
				</tr>
			</table>
		</div>
		
    </xsl:template>


    <xsl:template match="topicID">

        <xsl:variable name="topicID">
            <xsl:apply-templates/>
        </xsl:variable>

        <input type="hidden" name="topicID" value="{$topicID}"/>
    </xsl:template>

    <xsl:template match="topic">

        <xsl:variable name="topic">
            <xsl:apply-templates/>
        </xsl:variable>
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>

        <xsl:if test="$error='error'">
            <tr>
                <td class="table-content-single">
                    <font class="uportal-channel-warning">ERROR ENCOUNTERED: Please fix field items with red (*)</font>
                </td>
            </tr>
        </xsl:if>

	  <xsl:if test="$error='descr'">
            <tr>
                <td class="table-content-single">
                    <font class="uportal-channel-warning">ERROR ENCOUNTERED: Topic Description exceeds maximum length (250 characters)</font>
                </td>
            </tr>
        </xsl:if>


        <tr>
            <td class="table-content-single">
                <xsl:if test="$error='error'">
                    <font class="uportal-channel-warning">*</font>
                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                </xsl:if>
                <label for="NewsAdmin-CreateTopicNameT1"><span class="uportal-channel-strong">Topic Name:</span></label>
                <br/>
                <input type="text" name="topic" size="20" maxlength="20" value="{$topic}" class="uportal-input-text" id="NewsAdmin-CreateTopicNameT1"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="description">

        <tr>
            <td class="table-content-single">
                <label for="NewsAdmin-CreateTopicDescTA1"><span class="uportal-channel-strong">Topic Description:</span><br/><span class="uportal-text-small">(250 characters max.)</span></label>
                <br/>
                <textarea name="topic_description" rows="2" cols="50" class="uportal-input-text" id="NewsAdmin-CreateTopicDescTA1">
                    <xsl:apply-templates/>
                </textarea>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
