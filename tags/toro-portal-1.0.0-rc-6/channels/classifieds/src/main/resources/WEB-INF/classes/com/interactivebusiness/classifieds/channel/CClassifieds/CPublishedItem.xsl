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
  5    Channels  1.4         8/6/2002 4:56:01 PM  Freddy Lopez    modified files
       as part of new version of classifieds channel
  4    Channels  1.3         2/7/2002 5:58:24 PM  Jing Chai       
  3    Channels  1.2         2/5/2002 6:05:30 PM  Jing Chai       updated the
       Classified_Topic table, remove smallicon,bigicon and active coloumns. edit
       my published items to my classified status.
  2    Channels  1.1         12/20/2001 4:54:20 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:42 PMFreddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="Classifieds_List">
        <form action="{$baseActionURL}?action=DeleteItem" method="post">
            <!--UniAcc: Layout Table -->
            <table border="0" cellspacing="0" cellpadding="3" width="100%">
                <tr>
                    <th class="th-top-single">myClassifieds</th>
                </tr>
                <tr>
                    <td class="table-content-single">Total articles found: <xsl:value-of select="@total"/></td>
                </tr>
                <xsl:if test="child::Item">
                    <tr>
                        <td class="table-content-single">
                            <font class="uportal-channel-error">
                                <a href="{$baseActionURL}?action=myClassifieds&amp;checkbox=all">
                                    <u>Select All</u>
                                </a>| <a href="{$baseActionURL}?action=myClassifieds&amp;checkbox=none">
                                    <u>Unselect All</u></a></font>
                            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                            <input type="image" src="{$imagedir}/delete_12.gif" border="0" title="Delete All Checked" id="CClass-PublishedDeleteI1"/>
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            <font class="uportal-channel-error">
                                <label for="CClass-PublishedDeleteI1">Delete Selection</label>
                            </font>
                        </td>
                    </tr>
                </xsl:if>
                <tr>
                    <td class="table-content-single">
                        <!--UniAcc: Data Table -->
                        <table border="0" cellspacing="0" cellpadding="2" width="100%" class="table-content-iso">
                            <tr>
                                <th width="5%" class="th-top-left">
                                    <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                </th>
                                <th width="35%" class="th-top" scope="col">
                                    Ad Text
                                </th>
                                <th width="10%" class="th-top" scope="col">
                                    Date
                                </th>
                                <th width="10%" class="th-top" scope="col">
                                    Status
                                </th>
                                <th width="15%" class="th-top" scope="col">
                                    Other
                                </th>
                                <th width="10%" class="th-top" scope="col">
                                    Contact
                                </th>
                                <th width="15%" class="th-top" scope="col">
                                    Price
                                </th>
                            </tr>
                            <xsl:apply-templates/>

                            <xsl:if test="not(child::Item)">
                                <tr>
                                    <td colspan="7" class="table-content-single">
                                        <!--UniAcc: Layout Table -->
                                        <table border="1" cellspacing="0" cellpadding="2" width="100%" class="uportal-background-light">
                                            <tr>
                                                <td>
                                                    <font class="uportal-channel-text">You have no classifieds published.</font>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </xsl:if>
                        </table>
                    </td>
                </tr>
                <xsl:if test="child::Item">
                    <tr>
                        <td class="table-content-single">
                            <font class="uportal-channel-error">
                                <a href="{$baseActionURL}?action=myClassifieds&amp;checkbox=all">
                                    <u>Select All</u>
                                </a>| <a href="{$baseActionURL}?action=myClassifieds&amp;checkbox=none">
                                    <u>Unselect All</u></a></font>
                            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                            <input type="image" src="{$imagedir}/delete_12.gif" border="0" title="Delete All Checked" id="CClass-PublishedDeleteI2"/>
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            <font class="uportal-channel-error">
                                <label for="CClass-PublishedDeleteI2">Delete Selection</label>
                            </font>
                        </td>
                    </tr>
                </xsl:if>

                <tr>
                    <td class="table-content-single">Total articles found: <xsl:value-of select="@total"/></td>
                </tr>
                <tr>
                	<td align="center">
                		<input type="button" class="uportal-button" name="return" value="Return to Classifieds" onclick="location.href='{$baseActionURL}?uP_root=me&amp;action=main'"/>
                	</td>
                </tr>
            </table>
        </form>
    </xsl:template>



    <xsl:template match="Item">
        <tr class="uportal-background-light">
            <xsl:attribute  name = "class" >
                <xsl:choose>
                    <xsl:when test="position() mod 2 = 1">table-light</xsl:when>
                    <xsl:otherwise>table-content</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            
            <td>
                <xsl:if test="@checked = 'checked'">
                    <input type="checkbox" name="{@itemID}" value="checked" checked="checked" id="{@itemID}"/>
                </xsl:if>

                <xsl:if test="@checked = 'no'">
                    <input type="checkbox" name="{@itemID}" value="checked" id="{@itemID}"/>
                </xsl:if>
            </td>
            <td>
                <font class="uportal-channel-text">
                    <label for="{@itemID}">
                        <xsl:value-of select="@content"/>
                    </label>
                </font>
            </td>
            <td>
                <font class="uportal-channel-text">
                    <xsl:value-of select="@date"/>
                </font>
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="@type = 'A'">
                        <font class="uportal-channel-text">Approved</font>
                        <br/>
                        <font class="uportal-channel-warning">Expires on <xsl:value-of select="@daysleft"/></font>
                        <br/>
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
            <td align="center">
                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                <xsl:if test="@image='yes'">
                    <img src="{$imagedir}/photo.gif" border="0" alt="classified contains Image" title="classified contains Image"/>
                    <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                </xsl:if>

                <xsl:if test="@messagetoauthor='yes'">
                    <img src="{$imagedir}/mail.gif" border="0" alt="admin has sent you a message" title="admin has sent you a message"/>
                </xsl:if>
            </td>
            <td align="center">
                <font class="uportal-channel-text">
                    <a href="{$baseActionURL}?action=details&amp;itemID={@itemID}&amp;name={@topic}&amp;type={@type}" title="Display Contact Information">
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
