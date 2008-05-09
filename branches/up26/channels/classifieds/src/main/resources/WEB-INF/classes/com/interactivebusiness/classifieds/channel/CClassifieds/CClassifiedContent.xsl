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
  4    Channels  1.3         8/6/2002 4:55:57 PM  Freddy Lopez    modified files
       as part of new version of classifieds channel
  3    Channels  1.2         2/5/2002 6:05:19 PM  Jing Chai       updated the
       Classified_Topic table, remove smallicon,bigicon and active coloumns. edit
       my published items to my classified status.
  2    Channels  1.1         12/20/2001 4:54:00 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:37 PMFreddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>


    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>


    <xsl:template match="topic_items">
    
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->

        <!--UniAcc: Layout Table -->
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
            <tr>
                <th class="th-top-single">
                    <a href="{$baseActionURL}?action=main" title="Display parent category:">
                        <img src="{$imagedir}/classified.gif" border="0" alt="Classifieds" title="Classifieds"/>
                        Topics
                    </a>
                    <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                    &gt;
                    <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                    <xsl:value-of select="@name"/>
                </th>
            </tr>
            <tr>
                <td>
                    <font class="uportal-channel-text">Total articles found: <xsl:value-of select="@total"/></font>
                </td>
            </tr>
            <tr>
                <td>
                    <!--UniAcc: Data Table -->
                    <table border="0" cellspacing="0" cellpadding="2" width="100%" class="table-content">
                        <tr>
                            <td class="th-top-left" scope="col">Date</td>
                            <td class="th-top" scope="col">Ad Text</td>
                            <td class="th-top" scope="col">Other</td>
                            <td class="th-top" scope="col">Contact</td>
                            <td class="th-top-right" scope="col">Price</td>
                        </tr>
                        <xsl:apply-templates/>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                   <xsl:if test="@total &lt; 1">
			     <!--UniAcc: Layout Table -->
			     <table border="1" cellspacing="0" cellpadding="2" width="100%">
				 <tr>
				     <td>
					 <font class="uportal-channel-text">No Classifieds currently exist for the selected topic.</font>
				     </td>
				 </tr>
			     </table>
		   </xsl:if>
		   <xsl:if test="@total &gt; 0">
                    <font class="uportal-channel-text">Total articles found: <xsl:value-of select="@total"/></font>
                    </xsl:if>
                </td>
            </tr>
        </table>
    </xsl:template>
    
    <xsl:template match="item">
        <tr>
            <xsl:attribute  name = "class" >
                <xsl:choose>
                    <xsl:when test="position() mod 2 = 1">table-light</xsl:when>
                    <xsl:otherwise>table-content</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <td width="15%">
                <font class="uportal-channel-text">
                    <xsl:value-of select="@date"/>
                </font>
            </td>
            <td width="45%">
                <font class="uportal-channel-text">
                    <xsl:value-of select="@content"/>
                </font>
            </td>
            <td width="10%">
                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                <xsl:if test="@image='yes'">
                    <img src="{$imagedir}/photo.gif" border="0" alt="Associated Image" title="Associated Image"/>
                </xsl:if>
            </td>
            <td width="5%">
                <font class="uportal-channel-text">
                    <a href="{$baseActionURL}?action=vdetail&amp;itemID={@itemID}&amp;name={//@name}" title="Display Contact Information">
                        click
                    </a>
                </font>
            </td>
            <td>
                <font class="uportal-channel-text">
                    <xsl:if test="starts-with(@cost, '$')">
                        <xsl:value-of select="@cost"/>
                    </xsl:if>
                    <xsl:if test="not (starts-with(@cost, '$'))">$ <xsl:value-of select="@cost"/></xsl:if>
                </font>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
