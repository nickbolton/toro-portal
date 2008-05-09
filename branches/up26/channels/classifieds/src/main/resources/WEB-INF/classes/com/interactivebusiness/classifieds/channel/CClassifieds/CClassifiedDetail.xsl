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
    <xsl:param name="resourceURL">default</xsl:param>

    <xsl:template match="item_details">
        
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->
        
        <!--UniAcc: Layout Table -->
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
            <xsl:if test="@type">
                <tr>
                    <td class="table-content-iso">
                        <a href="{$baseActionURL}?action=myClassifieds">
                            <img src="{$imagedir}/prev_12.gif" border="0" alt="Display Previous Article" title="Display Previous Article"/>
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            back
                        </a>
                    </td>
                </tr>
            </xsl:if>
            
            <xsl:if test="not(@type)">
                <tr>
                    <th class="th-top-single">
                        <a href="{$baseActionURL}?action=main">
                            <img src="{$imagedir}/classified.gif" border="0" alt="Classifieds Icon" title="Classifieds Icon"/>
                            Topics
                        </a>
                        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        &gt;
                        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        <a href="{$baseActionURL}?action=detail&amp;topicID={@topicID}&amp;name={@name}" title="Display parent topic:">
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            <xsl:value-of select="@name"/>
                        </a>
                    </th>
                </tr>
                <tr>
                    <td align="right" class="table-content-iso">
                        <xsl:if test="@prev">
                            <a href="{$baseActionURL}?action=vdetail&amp;prev=yes">
                                <img src="{$imagedir}/prev_12.gif" border="0" alt="Display Previous Article" title="Display Previous Article"/>
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                Previous
                            </a>
                        </xsl:if>
                        <xsl:if test="(@prev) and (@next)">
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            |
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        </xsl:if>
                        <xsl:if test="@next">
                            <a href="{$baseActionURL}?action=vdetail&amp;next=yes">
                                Next
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                <img src="{$imagedir}/next_12.gif" border="0" alt="Go to Next Article" title="Go to Next Article"/>
                            </a>
                        </xsl:if>
                    </td>
                </tr>
            </xsl:if>
            <tr>
                <td>
                    <!--UniAcc: Layout Table -->
                    <table border="0" cellspacing="0" cellpadding="0" width="100%" class="table-content">
                        <tr >
                            <th colspan="2" class="th-top-single">
                                Classified item posted on: <b><xsl:value-of select="@date"/></b>
                            </th>
                        </tr>
                        <tr>
                            <td width="50%" valign="top" class="table-content-single">
                                <font class="uportal-channel-text">
                                    <xsl:value-of select="@content"/>
                                    <br/>
                                </font>
                            </td>
                        </tr>                        
                        <xsl:if test = "@hasimage">
                            <tr>
                                <td class="table-content-single">
                                    <xsl:if test="@hasimage = 'yes'">
                                        <img src="{$resourceURL}?itemID={@itemID}" border="0" alt="" title=""/>
                                    </xsl:if>
                                    <xsl:if test="@hasimage = 'no'">
                                        <img src="{$imagedir}/nophoto.gif" border="0" alt="" title=""/>
                                    </xsl:if>
                                </td>
                            </tr>
                        </xsl:if>
                        <tr>
                            <td class="table-content-single">                                
                                <b>Price:
                                    <xsl:if test="starts-with(@cost, '$')">
                                        <xsl:value-of select="@cost"/>
                                    </xsl:if>
                                    <xsl:if test="not (starts-with(@cost, '$'))">$ <xsl:value-of select="@cost"/></xsl:if>
                                </b>
                            </td>
                        </tr>

                        <tr>
                            <td class="table-content-single">                                
                                <b>Contact Information:</b>
                                <br/>
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                <xsl:value-of select="@contact"/>
                                <br/>
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                <xsl:value-of select="@phone"/>
                                <br/>
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                <xsl:value-of select="@email"/>                                
                            </td>
                        </tr>
                        <xsl:if test="@type">
                            <tr>
                                <td class="table-content-single">
                                    <font class="uportal-channel-text">
                                        <b>Status:</b>
                                    </font>
                                    <br/>
                                    <xsl:choose>
                                        <xsl:when test="@type = 'A'">
                                            <font class="uportal-channel-text">Approved</font>
                                            <br/>
                                            <font class="uportal-channel-warning">Expires on <xsl:value-of select="@daysleft"/></font>
                                        </xsl:when>
                                        <xsl:when test="@type = 'P'">
                                            <font class="uportal-channel-text">Pending Approval</font>
                                            <br/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <font class="uportal-channel-text">Denied</font>
                                            <br/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-content-single">
                                    <font class="uportal-channel-text">
                                        <b>Message From Approver:</b>
                                        <br/>
                                        <xsl:value-of select="@messagetoauthor"/>
                                    </font>
                                </td>
                            </tr>
                        </xsl:if>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
