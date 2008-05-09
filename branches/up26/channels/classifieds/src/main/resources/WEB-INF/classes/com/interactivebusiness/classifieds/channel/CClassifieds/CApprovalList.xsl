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
  5    Channels  1.4         8/6/2002 4:55:56 PM  Freddy Lopez    modified files
       as part of new version of classifieds channel
  4    Channels  1.3         5/29/2002 12:16:00 PMFreddy Lopez    fixing
       classifieds, adding images for classifieds, groups, toolbar
  3    Channels  1.2         2/5/2002 6:05:17 PM  Jing Chai       updated the
       Classified_Topic table, remove smallicon,bigicon and active coloumns. edit
       my published items to my classified status.
  2    Channels  1.1         12/20/2001 4:53:52 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:35 PMFreddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href = "common.xsl"/>

    <xsl:param name="resourceURL">default</xsl:param>
    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="Approver">
        <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "/*"/>
        </textarea> -->

        <form action="{$baseActionURL}?action=assignApprover" method="post">
            <!--UniAcc: Layout Table -->
            <table border="0" cellspacing="0" cellpadding="2" width="100%">
                <tr>
                    <th colspan="2" class="th-top-single">Classifieds Pending Approvals</th>
                </tr>
                <tr>
                    <td colspan="2">
                        <b>
                            <font class="uportal-channel-text">Topic</font>
                        </b>
                    </td>
                </tr>
                <xsl:apply-templates/>
            </table>
        </form>
    </xsl:template>

    <xsl:template match="Empty">
        <tr>
            <td>
                <!--UniAcc: Layout Table -->
                <table border="1" cellspacing="0" cellpadding="2" width="100%">
                    <tr>
                        <td>
                            <font class="uportal-channel-text">No classified topics found with pending items for approval.</font>
                        </td>
                    </tr>
                </table>
                <div align="center">
                	<br/>
                	<input type="button" name="close" value="Return to Classifieds" class="uportal-button" onclick="location.href='{$baseActionURL}?uP_root=me&amp;action=main'"/>
                </div>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="topic_list">
        <tr>
            <td colspan="2">
                <!--UniAcc: Layout Table -->
                <table border="0" cellspacing="0" cellpadding="0" width="100%">
                    <xsl:apply-templates select="Empty"/>

                    <xsl:for-each select="option">
                        <xsl:variable name = "styleAppend">                        
                        <xsl:choose>
                            <xsl:when test="position()=1">-top</xsl:when>
                              <xsl:when test="position()=last()">-bottom</xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                        </xsl:variable>
                        <xsl:variable name = "styleAlternate">
                        <xsl:choose>
                            <xsl:when test="position() mod 2=1">-content</xsl:when>
                              <xsl:otherwise>-light</xsl:otherwise>
                        </xsl:choose>
                        </xsl:variable>
                        <tr>
                            <td align="center" class="table{$styleAlternate}-left{styleAppend}" width="10%">
                                <img src="{$resourceURL}?icon=icon&amp;mime_type={@mime_type}&amp;image={@image}" border="0" alt="" title=""/>
                                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                            </td>
                            <td class="table{$styleAlternate}{styleAppend}" width="20%">
                                <a href="{$baseActionURL}?action=approval&amp;topicID={@value}" title="Display first approval detail for topic">
                                    <xsl:value-of select="."/>
                                </a>
                            </td>
                            <td class="table{$styleAlternate}-right{styleAppend}" align="left">
                                <xsl:value-of select="@count"/>
                                <xsl:choose>
                                    <xsl:when test="number(@count)&gt;1"> entries</xsl:when>
                                    <xsl:otherwise> entry</xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
