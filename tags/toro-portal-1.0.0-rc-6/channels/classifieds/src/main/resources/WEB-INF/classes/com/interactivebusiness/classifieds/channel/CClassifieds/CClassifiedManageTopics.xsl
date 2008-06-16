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
  1    Channels  1.0         8/6/2002 4:54:03 PM  Freddy Lopez    
 $

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="ManageTopics">
    
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "/*"/>
    </textarea> -->
    
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="2" cellspacing="0" align="left" width="100%">
            <tr>
                <th class="th-top-single">Manage Classifieds Topics</th>
            </tr>
            <tr>
                <td width="100%" class="table-content-single">
                    <a href="{$baseActionURL}?action=addTopic">
                        <img src="{$imagedir}/add_classified.gif" border="0" alt="Add Topic" title="Add Topic"/>
                        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        Add Classifieds Topic
                    </a>
                </td>
            </tr>
            <tr>
                <td class="table-content-single">
					<form action="{$baseActionURL}?action=TopicSearch" method="post" name="topics">
						<table border="0" cellpadding="2" cellspacing="0" align="center" width="100%">
							<tr>                                                            
								<td>
									<img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
								</td>
								<td align="left" colspan="3">
									<font class="uportal-channel-error">(Please use * in search string i.e - Spor* )</font>
								</td>
							</tr>
							<tr valign="middle">
								<td valign="middle">
									<!-- <input type="hidden" name="action" value="TopicSearch"/> -->
									<span class="uportal-channel-text">
										<label for="CClass-ManTopicSearchT1">
											<b>Search&#160;for&#160;Topic:</b>
										</label>
									</span>
								</td>
								<td align="left" valign="middle">
									<input type="text" size="25" maxsize="30" name="topic_to_find" class="uportal-input-text" id="CClass-ManTopicSearchT1"/>
								</td>
								<td align="left" valign="middle" colspan="2">
									<input type="submit" value="Find" class="uportal-button"/>
								</td>                                
								<td width="100%">
									<img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
								</td>
							</tr>
						</table>
					</form>
                    <xsl:apply-templates/>
                </td>
            </tr>
            <xsl:if test="//search_results/child::topic">
                <tr>
                    <td class="table-content-single">
                        <img src="{$imagedir}/add_classified.gif" border="0" alt="Add Topic" title="Add Topic"/>
                        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        <font class="uportal-channel-text">
                            <a href="{$baseActionURL}?action=addTopic">Add Classifieds Topic</a>
                        </font>
                    </td>
                </tr>
            </xsl:if>
            <tr>
            	<td align="center">
            		<input type="button" class="uportal-button" name="return" value="Return to Classifieds" onclick="location.href='{$baseActionURL}?uP_root=me&amp;action=main'"/>
            	</td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="search_results">
        <table border="0" cellpadding="2" cellspacing="0" align="center" width="100%">
            <tr>
                <td colspan="4" class="th-top" width="100%" align="left">Search Results:</td>
            </tr>   
			<tr>
				<td colspan="4" class="table-content-iso">
					<!--UniAcc: Data Table -->
		            <form action="{$baseActionURL}?action=DeleteTopic" method="post">
						<table border="0" cellpadding="2" cellspacing="0" align="center" width="100%">
							<xsl:if test="child::topic">
								<tr>
									<td align="left" colspan="4">
										<font class="uportal-channel-error">
											<a href="{$baseActionURL}?action=TopicSearch&amp;checkbox=all">
												<u>Select All</u>
											</a>| <a href="{$baseActionURL}?action=TopicSearch&amp;checkbox=none">
												<u>Unselect All</u></a></font>
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
										<input type="image" src="{$imagedir}/delete_12.gif" border="0" title="Delete All Checked" alt="Delete All Checked" id="CClass-ManageDeleteI1"/>
										<img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
										<font class="uportal-channel-error">
											<label for="CClass-ManageDeleteI1">Delete Selection</label>
										</font>
									</td>
								</tr>
								<tr class="uportal-channel-table-header">
									<td colspan="2" width="40%" scope="col">Topic Name</td>
									<td width="45%" scope="col">Topic Description</td>
									<td align="right" scope="col">Items</td>
								</tr>
							</xsl:if>
								<tr>
									<td colspan="4">
										<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%" class="uportal-background-content">
											<xsl:apply-templates/>
										</table>
									</td>
								</tr>
							<xsl:if test="child::topic">
								<tr>
									<td align="left" colspan="4">
										<font class="uportal-channel-error">
											<a href="{$baseActionURL}?action=TopicSearch&amp;checkbox=all">
												<u>Select All</u>
											</a>| <a href="{$baseActionURL}?action=TopicSearch&amp;checkbox=none">
												<u>Unselect All</u></a></font>
										<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
										<input type="image" src="{$imagedir}/delete_12.gif" border="0" title="Delete All Checked" alt="Delete All Checked" id="CClass-ManageDeleteI2"/>
										<img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
										<font class="uportal-channel-error">
											<label for="CClass-ManageDeleteI2">Delete Selection</label>
										</font>
									</td>
								</tr>
							</xsl:if>
						</table>
					</form>
				</td>
			</tr>
        </table>
    </xsl:template>


    <xsl:template match="topic">

        <xsl:variable name="topicID">
            <xsl:value-of select="@value"/>
        </xsl:variable>

        <tr valign="middle" class="uportal-background-light">
            <xsl:attribute  name = "class" >
                <xsl:choose>
                    <xsl:when test="position() mod 2 = 0">uportal-background-light</xsl:when>
                    <xsl:otherwise>uportal-background-content</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <td valign="middle" align="center" width="5%">

                <xsl:if test="@checked = 'checked'">
                    <input type="checkbox" name="{$topicID}" value="checked" checked="checked" id="{$topicID}"/>
                </xsl:if>

                <xsl:if test="@checked = 'no'">
                    <input type="checkbox" name="{$topicID}" value="checked" id="{$topicID}"/>
                </xsl:if>
            </td>

            <xsl:apply-templates select="topic_name">
                <xsl:with-param name="topicID" select="$topicID"/>
            </xsl:apply-templates>

            <xsl:apply-templates select="description"/>

            <td align="right">
                <font class="uportal-channel-error">
                    <b>
                        <xsl:apply-templates select="numberofitems"/>
                    </b>
                </font>
                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="topic_name">
        <xsl:param name="topicID"/>

        <td width="35%" valign="middle">
            <font class="uportal-channel-text">
                <label for="{$topicID}">
                    <b>
                        <xsl:apply-templates/>
                    </b>
                </label>
            </font>
            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>

            <a href="{$baseActionURL}?action=EditTopic&amp;topicID={$topicID}" style="text-decoration:none">
                <img src="{$imagedir}/edit_16.gif" border="0" alt="Edit Topic" title="Edit Topic"/>
            </a>

            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>

            <a href="{$baseActionURL}?action=DeleteTopic&amp;topicID={$topicID}" style="text-decoration:none">
                <img src="{$imagedir}/delete_12.gif" border="0" alt="Delete Topic" title="Delete Topic"/>
            </a>
        </td>
    </xsl:template>

    <xsl:template match="description">
        <td width="45%" valign="middle">
            <font class="uportal-channel-text">
                <xsl:apply-templates/>
            </font>
        </td>
    </xsl:template>

    <xsl:template match="Empty">
        <tr>
            <td align="center" colspan="4">
                <font class="uportal-channel-text">
                    <b>
                        <xsl:apply-templates/>
                    </b>
                </font>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
