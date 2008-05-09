<?xml version="1.0"?>
<!--
   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.

   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.

   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" />
    <xsl:template name="view-todo">
        <xsl:param name="entry" />
        <xsl:param name="title" />
        <xsl:param name="date" />
        <xsl:param name="start" />
        <xsl:param name="place" />
        <xsl:param name="share" />
        <xsl:param name="priority" />
        <xsl:param name="notes" />
        <xsl:param name="alarm" />
        <xsl:param name="repeat" />
        <xsl:param name="attendee" />
        <xsl:param name="until" />
        <xsl:param name="isReccurent" />
        <!--UniAcc: Layout Table -->
        <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-background-content">
            <tr>
                <th colspan="3" class="th">
<!-- Show Title -->
                View Task</th>
            </tr>
            <tr>
<!-- line -->
                <td colspan="3" class="uportal-background-light">
                    <img src="{$SPACER}" border="0" height="2" alt="" title="" />
                </td>
            </tr>
            <tr>
<!-- detail todo -->
                <td width="35%" class="uportal-channel-text" valign="top">
                	<!--UniAcc: Layout Table -->
                    <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-channel-text">
                        <tr>
                            <td colspan="2" class="uportal-background-content">
                                <img src="{$SPACER}" border="0" height="6" alt="" title="" />
                            </td>
                        </tr>
                        <tr>
                            <td>&#160;Calendar&#160;</td>
                            <td class="uportal-channel-emphasis">
<!-- Combo Calendars -->
                                <xsl:for-each select="//calendar">
                                    <xsl:if test="@calid=//calendar-system/view/calendar/@calid">
                                        <xsl:value-of select="@calname" />
                                    </xsl:if>
                                </xsl:for-each>
                            </td>
                        </tr>
                        <tr>
                            <td>&#160;Title</td>
                            <td>
                                <font class="uportal-channel-emphasis">
                                    <xsl:value-of select="$title" />
                                </font>
                            </td>
                        </tr>
                        <tr class="uportal-channel-text">
                            <td>&#160;Due</td>
                            <td class="uportal-channel-emphasis">
                                <xsl:value-of select="$date" />
                            </td>
                        </tr>
                        <tr>
                            <td>&#160;At</td>
                            <td class="uportal-channel-emphasis">
                                <xsl:call-template name="t24to12">
                                    <xsl:with-param name="hour" select='$start' />
                                </xsl:call-template>
                            </td>
                        </tr>
                        <tr>
                            <td>&#160;Priority</td>
                            <td class="uportal-channel-emphasis">
                                <xsl:if test='contains($priority,"0")'>Very Low</xsl:if>
                                <xsl:if test='contains($priority,"3")'>Low</xsl:if>
                                <xsl:if test='contains($priority,"5")'>Normal</xsl:if>
                                <xsl:if test='contains($priority,"7")'>High</xsl:if>
                                <xsl:if test='contains($priority,"9")'>Very High</xsl:if>
                            </td>
                        </tr>
                        <tr>
<!-- notes -->
                            <td valign="top">&#160;Notes</td>
                            <td class="uportal-channel-emphasis">
                                <xsl:value-of select="string($notes)" />
                            </td>
                        </tr>
                        <tr>
<!--Completion -->
                            <td>
 								Completed
                            </td>
                            <td class="uportal-channel-emphasis">
                                <xsl:choose>
                                    <xsl:when test="$entry/todo/completion/@completed">Yes</xsl:when>
                                    <xsl:otherwise>No</xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" class="uportal-background-content">
                                <img src="{$SPACER}" border="0" height="6" alt="" title="" />
                            </td>
                        </tr>
                    </table>
                </td>
<!-- col line -->
                <td class="uportal-background-light">
                    <img src="{$SPACER}" border="0" width="2" alt="" title="" />
                </td>
                <td width="65%" class="uportal-background-content" valign="top">
                	<!--UniAcc: Layout Table -->
                    <table width="30%" cellpadding="0" cellspacing="0" border="0" class="uportal-channel-text">
                        <tr>
                            <td colspan="4" class="uportal-background-content">
                                <img src="{$SPACER}" border="0" height="6" alt="" title="" />
                            </td>
                        </tr>
<!--view todo repeating -->
                        <tr>
                            <td colspan="4" class="uportal-channel-emphasis">&#160;&#160;Repeating</td>
                        </tr>
                        <xsl:if test="$isReccurent and /calendar-system/view/@pages != '0'">
<!--view  repeating -->
                            <xsl:for-each select="/calendar-system/view/calendar/entry">
                                <tr>
                                    <td colspan="4">
                                        <xsl:apply-templates select='key("ceid", @ceid)'>
                                            <xsl:with-param name="view-todo">yes</xsl:with-param>
                                        </xsl:apply-templates>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            <tr>
                                <td colspan="4">
<!-- Previous -->
                                    <xsl:choose>
                                        <xsl:when test="/calendar-system/view/@prev='true'">
                                            <a href="{$mdoURL}=previous&amp;p={/calendar-system/view/@page}" title="Display">
                                                <img src="{$baseImagePath}/prev_12.gif" border="0" align="middle" alt="Previous" title="Previous" />
                                            </a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <img border="0" src="{$baseImagePath}/prev_disabled_12.gif" align="middle" alt="" title="" />
                                        </xsl:otherwise>
                                    </xsl:choose>
<!-- current page number -->
                                    <font class="uportal-channel-text">
                                    <xsl:value-of select="/calendar-system/view/@page + 1" />
                                    /
                                    <xsl:value-of select="/calendar-system/view/@pages" />
                                    </font>
<!-- Next -->
                                    <xsl:choose>
                                        <xsl:when test="/calendar-system/view/@next='true'">
                                            <a href="{$mdoURL}=next&amp;p={/calendar-system/view/@page}" title="Display">
                                                <img src="{$baseImagePath}/next_12.gif" border="0" align="middle" alt="Next" title="Next" />
                                            </a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <img border="0" src="{$baseImagePath}/next_disabled_12.gif" align="middle" alt="" title="" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
<!-- End Repeating -->
                        </xsl:if>
                        <tr>
                            <td colspan="4" class="uportal-background-content">
                                <img src="{$SPACER}" border="0" height="6" alt="" title="" />
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
<!-- submit bottom -->
                <td colspan="3" align="left" class="table-nav" height="22">
                    <input type="hidden" name="sid">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$sid" />
                        </xsl:attribute>
                    </input>
                    <img src="{$SPACER}" border="0" width="1" alt="" title="" />
                    <input class="uportal-button" type="submit" name="go~{$back}&amp;uP_root=me" value="Close" />
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>

