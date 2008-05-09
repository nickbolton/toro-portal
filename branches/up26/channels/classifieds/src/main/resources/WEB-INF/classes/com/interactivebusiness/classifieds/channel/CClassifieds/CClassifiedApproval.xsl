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
  3    Channels  1.2         2/5/2002 6:05:18 PM  Jing Chai       updated the
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

    <xsl:template match="Item">
    
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "/*"/>
    </textarea> -->
    
        <form action="{$baseActionURL}?action=approval" method="post">
            <!--UniAcc: Layout Table -->
            <table border="0" cellspacing="0" cellpadding="3" width="100%">
                <tr>
                    <th class="th-top-single">Approvals >> <xsl:value-of select="@topic"/></th>
                </tr>
                <xsl:if test="@error">
                    <tr>
                        <td class="table-content-single">ERROR ENCOUNTERED: Please fix field items with red (*)</td>
                    </tr>
                </xsl:if>
                <tr>
                    <td>
                        <!--UniAcc: Data Table -->
                        <table border="0" cellspacing="0" cellpadding="2" width="100%" class="table-content">
                            <tr>
                                <td class="table-content-left" scope="row">Topic:</td>
                                <td class="table-content-right">
                                    <xsl:value-of select="@topic"/>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-content-left" scope="row">Message:</td>
                                <td class="table-content-right">
                                    <xsl:value-of select="@content"/>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-content-left" scope="row">Cost:</td>
                                <td class="table-content-right">
                                        <xsl:if test="starts-with(@cost, '$')">
                                            <xsl:value-of select="@cost"/>
                                        </xsl:if>
                                        <xsl:if test="not (starts-with(@cost, '$'))">$ <xsl:value-of select="@cost"/></xsl:if>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-content-left" scope="row">Photo:</td>
                                <td class="table-content-right">
                                    <!--@imageTitle does not exist, but ADA compliance necessitates some descriptor TTrack to be started for Java changes -->
                                    <xsl:if test="@image = 'yes'">
                                        <img src="{$resourceURL}?itemID={@itemID}" border="0" alt="{@imageTitle}" title="{@imageTitle}"/>
                                    </xsl:if>
                                    <xsl:if test="@image = 'no'">
                                        <img src="{$imagedir}/nophoto.gif" border="0" alt="No associated image" title="No associated image"/>
                                    </xsl:if>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" class="th-top-single" id="CClass-ContactInfo">Contact Information</td>
                            </tr>

                            <tr>
                                <td class="table-content-left" headers="CClass-ContactInfo" id="CClass-ContactPhone">
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                    Phone:
                                </td>
                                <td class="table-content-right" headers="CClass-ContactInfo CClass-ContactPhone">
                                    <xsl:value-of select="@phone"/>
                                </td>
                            </tr>

                            <tr>
                                <td class="table-content-left" headers="CClass-ContactInfo" id="CClass-ContactEmail">
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                    Email:
                                </td>
                                <td class="table-content-right" headers="CClass-ContactInfo CClass-ContactEmail">
                                    <xsl:value-of select="@email"/>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-content-left" headers="CClass-ContactInfo" id="CClass-ContactName">
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>Name:
                                </td>
                                <td class="table-content-right" headers="CClass-ContactInfo CClass-ContactName">
                                    <xsl:value-of select="@contact"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td>
                        <!--UniAcc: Layout Table -->
                        <table border="0" cellspacing="0" cellpadding="2" width="100%">
                            <tr>
                                <td width="15%">
                                    <xsl:if test="@error">
                                        <font class="uportal-channel-warning">*</font>
                                        <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                    </xsl:if>
                                    <input type="radio" name="choice" value="approved" id="CClass-ApprovalApprovedR1"/>
                                    <font class="uportal-channel-text">
                                        <label for="CClass-ApprovalApprovedR1">Approved</label>
                                    </font>
                                </td>
                                <td>
                                    <input type="radio" name="choice" value="denied" id="CClass-ApprovalDeniedR1"/>
                                    <font class="uportal-channel-text">
                                        <label for="CClass-ApprovalDeniedR1">Denied</label>
                                    </font>
                                </td>
                            </tr>

                            <xsl:call-template name="warning"/>

                            <tr>
                                <td colspan="2">
                                    <font class="uportal-channel-text">
                                        <label for="CClass-ApprovealMessage">Message to Author (200 character limit)</label>
                                    </font>
                                    <br/>
									<input type="text" name="messageToAuth"  size="100" maxlength="200" class="uportal-input-text" id="CClass-ApprovealMessage" />
                                    <!-- Changed to text input so that the character input could be limited to 100.
									<textarea name="messageToAuth" rows="5" cols="50" class="uportal-input-text" id="CClass-ApprovealMessage">
                                    </textarea> -->
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr class="table-light-single" style="text-align:center;">
                    <td>
                        <input type="submit" name="finished" value="Finished" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <xsl:if test="@hasnext = 'yes'">
                            <input type="submit" name="next" value="Next" class="uportal-button"/>
                            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        </xsl:if>
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>


    <xsl:template name="topics">
        <xsl:for-each select="topic">
            <xsl:variable name="value">
                <xsl:value-of select="select"/>
            </xsl:variable>
            <option class="uportal-text">
                <xsl:attribute name="value">
                    <xsl:value-of select="id"/>
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$value='true'">
                        <xsl:attribute name="selected">
                            <xsl:value-of select="value"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text></xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="name"/>
            </option>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="warning">
        <xsl:variable name="value">
            <xsl:value-of select="needWarning"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$value='true'">
                <tr>
                    <td class="uportal-channel-warning" colspan="2">
                        <xsl:value-of select="warning"/>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text></xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
