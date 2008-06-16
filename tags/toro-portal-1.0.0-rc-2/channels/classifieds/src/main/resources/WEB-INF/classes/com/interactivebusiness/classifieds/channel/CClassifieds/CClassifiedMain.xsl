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
  5    Channels  1.4         8/6/2002 4:55:58 PM  Freddy Lopez    modified files
       as part of new version of classifieds channel
  4    Channels  1.3         5/29/2002 12:16:02 PMFreddy Lopez    fixing
       classifieds, adding images for classifieds, groups, toolbar
  3    Channels  1.2         2/5/2002 6:05:25 PM  Jing Chai       updated the
       Classified_Topic table, remove smallicon,bigicon and active coloumns. edit
       my published items to my classified status.
  2    Channels  1.1         12/20/2001 4:54:01 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:37 PMFreddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>

    <xsl:template match="Main">
    
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->


        <form action="{$baseActionURL}" method="post">
            <!--UniAcc: Layout Table -->
            <table border="0" cellspacing="0" cellpadding="0" width="100%">
                <tr>
                    <td colspan="2">
                        <!--UniAcc: Layout Table -->
                        <table border="0" cellspacing="0" cellpadding="0" width="100%">
                            <tr>
                                <xsl:apply-templates select="toolbar"/>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <th colspan="2" class="th-top-single">
                        <img src="{$imagedir}/classified.gif" border="0" alt="Classifieds Icon" title="Classifieds Icon"/>
                        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        <font class="uportal-channel-text">
                            <b>Classified Topics</b>
                        </font>
                    </th>
                </tr>

                <tr>
                    <td colspan="2">
                        <!--UniAcc: Layout Table -->
                        <table border="0" cellspacing="0" cellpadding="1" width="100%" class="table-content">
                            <xsl:apply-templates select="topic_list"/>
                            <xsl:apply-templates select="Empty"/>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>

    <xsl:template match="toolbar">
        <td colspan="2" height="35" class="portlet-toolbar-container">
            <div class="tool-tab-body">
            	<xsl:apply-templates/>
            </div>
        </td>
    </xsl:template>

    <xsl:template match="topicIcon">
        <a href="{$baseActionURL}?uP_root=me&amp;action=manageTopics">Manage Topics</a>
        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
    </xsl:template>

    <xsl:template match="approverIcon">
        <a href="{$baseActionURL}?uP_root=me&amp;action=approver">Approve Classifieds</a>
        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
    </xsl:template>
    
    <xsl:template match="newClassifedIcon">
        <xsl:choose>
			<xsl:when test="../../topic_list">
				<a href="{$baseActionURL}?uP_root=me&amp;action=new" title="Create">Add Classified</a>
				<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
			</xsl:when>
			<xsl:otherwise>
				Add Classified
				<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
			</xsl:otherwise>
		</xsl:choose>        
    </xsl:template>

    <xsl:template match="statusIcon">
        <a href="{$baseActionURL}?uP_root=me&amp;action=myClassifieds" title="Display">View My Classifieds</a>
    </xsl:template>

    <xsl:template match="Empty">
        <tr>
            <td colspan="2">
                <table border="1" cellpadding="1" cellspacing="0" width="100%">
                    <tr>
                        <td>
                            <font class="uportal-channel-text">No classified articles found.</font>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>


    <xsl:template name="appMessage">
        <xsl:variable name="value">
            <xsl:value-of select="hasApvdMsg"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$value='true'">
                <a href="{$baseActionURL}?uP_root=me&amp;action=hasMsg" title="Display">
                    <img src="{$imagedir}/mail.gif" width="16" height="16" border="0" alt="Mail from Administrator" title="Mail from Administrator"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text></xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="status">
        <xsl:variable name="value">
            <xsl:value-of select="hasStatus"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$value='true'">
                <a href="{$baseActionURL}?uP_root=me&amp;action=status" title="Display">
                    <img src="{$imagedir}/mystuff.gif" width="16" height="16" border="0" alt="My Classifieds Status" title="My Classifieds Status"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text></xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="administration">
        <xsl:variable name="value">
            <xsl:value-of select="isAdministration"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$value='true'">
                <a href="{$baseActionURL}?uP_root=me&amp;action=admin" title="Access">
                    <img src="{$imagedir}/admin.gif" width="16" height="16" border="0" alt="Administration" title="Administration"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text></xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="approvals">
        <xsl:variable name="value">
            <xsl:value-of select="isApproval"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$value='true'">
                <a href="{$baseActionURL}?uP_root=me&amp;action=needApproval">
                    <img src="{$imagedir}/approve.gif" width="16" height="16" border="0" alt="Approval" title="Approval"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text></xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="topic">
        <xsl:variable name = "styleAppend">
            <xsl:if test = "position()=last()">
                <xsl:text>-bottom</xsl:text>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name = "styleAlternate">
        <xsl:choose>
            <xsl:when test="position() mod 2=1">-content</xsl:when>
              <xsl:otherwise>-light</xsl:otherwise>
        </xsl:choose>
        </xsl:variable>
        <tr>
            <td width="10%" align="center" class="table{$styleAlternate}-left{styleAppend}">
                <!--@imageTitle does not currently exist, but ADA compliance necessitates some descriptor from xml/java -->
                <img src="{$resourceURL}?icon=icon&amp;mime_type={@mime_type}&amp;image={@image}" border="0" alt="{@imageTitle}" title="{@imageTitle}"/>
                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
            </td>
            <td width="25%" align="left" class="table{$styleAlternate}{styleAppend}"> 
                <a href="{$baseActionURL}?uP_root=me&amp;action=detail&amp;topicID={@id}" title="Display Topic:">
                    <xsl:value-of select="@name"/>
                </a>
            </td>
            <td align="left" class="table{$styleAlternate}-right{styleAppend}">
                <xsl:value-of select="@count"/> classified(s)
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
