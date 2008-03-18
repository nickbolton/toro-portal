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
    <xsl:template name="view">
        <xsl:param name="entry" />
        <xsl:param name="is-invitation" />
        <xsl:param name="cal" />
        <xsl:param name="title" />
        <xsl:param name="date" />
        <xsl:param name="start" />
        <xsl:param name="end" />
        <xsl:param name="length" />
        <xsl:param name="place" />
        <xsl:param name="share" />
        <xsl:param name="category" />
        <xsl:param name="priority" />
        <xsl:param name="notes" />
        <xsl:param name="repeat" />
        <xsl:param name="alarm" />
        <xsl:param name="until" />
        <xsl:param name="isReccurent" />
        <form method="post" action="{$baseActionURL}">
        	<!--UniAcc: Layout Table -->
            <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-background-content">
                <tr>
                    <th class="th">
<!-- Show Title -->
                        <xsl:choose>
                            <xsl:when test="$is-invitation">Accepted Invitation</xsl:when>
                            <xsl:otherwise>View Event</xsl:otherwise>
                        </xsl:choose>
                    </th>
<!--START ATTENDEES-->
					<th class="th">&#160;&#160;Attendees</th>
                </tr>
                <tr>
<!-- detail event -->
                    <td valign="top">
                    	<!--UniAcc: Layout Table -->
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-channel-text">
                            <tr>
                                <td class="table-light-left">&#160;Calendar&#160;</td>
                                <td class="table-content-right">
<!-- Calendars name-->
                                    <xsl:for-each select="//calendar">
                                        <xsl:if test="@calid=//calendar-system/view/calendar/@calid">
                                            <xsl:value-of select="@calname" />
                                        </xsl:if>
                                    </xsl:for-each>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-light-left">&#160;Title</td>
                                <td class="table-content-right">
                                    <xsl:value-of select="$title" />
                                    &#160;
                                </td>
                            </tr>
                            <tr>
                                <td class="table-light-left">&#160;Date</td>
                                <td class="table-content-right">
									<xsl:value-of select="$date" />
                                </td>
                            </tr>
                            <tr>
                                <td class="table-light-left">&#160;Time</td>
                                <td  class="table-content-right">
                                    <xsl:choose>
                                        <xsl:when test='$length="all-day"'>All Day</xsl:when>
                                        <xsl:otherwise>
                                        <xsl:call-template name="t24to12">
                                            <xsl:with-param name="hour" select='$start' />
                                        </xsl:call-template>
                                        &#160;-&#160;
                                        <xsl:call-template name="t24to12">
                                            <xsl:with-param name="hour" select='$end' />
                                        </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                            <tr>
                                <td valign="top" class="table-light-left">&#160;Place</td>
                                <td nowrap="wrap" class="table-content-right">
                                    <xsl:value-of select="string($place)" />
                                    &#160;
                                </td>
                            </tr>
                            <tr>
                                <td class="table-light-left">&#160;Category&#160;</td>
                                <td class="table-content-right">
                                    <xsl:choose>
                                        <xsl:when test="$category">
                                            <xsl:value-of select="$category" />
                                        </xsl:when>
                                        <xsl:otherwise>None</xsl:otherwise>
                                    </xsl:choose>
                                </td>
                            </tr>
                            <tr>
                                <td class="table-light-left">&#160;Priority</td>
                                <td class="table-content-right">
                                    <xsl:if test='contains($priority,"0")'>Very Low</xsl:if>
                                    <xsl:if test='contains($priority,"3")'>Low</xsl:if>
                                    <xsl:if test='(contains($priority,"5"))'>Normal</xsl:if>
                                    <xsl:if test='contains($priority,"7")'>High</xsl:if>
                                    <xsl:if test='contains($priority,"9")'>Very High</xsl:if>
                                </td>
                            </tr>
                            <tr>
                                <td valign="top" class="table-light-left">&#160;Notes</td>
                                <td class="table-content-right">
									<xsl:call-template name='smarttext'>
										<xsl:with-param name="body" select="string($notes)" />
									</xsl:call-template>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td class="uportal-background-light" valign="top">
<!-- info event in right screen-->
						<!--UniAcc: Layout Table -->
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-channel-text">
                            <xsl:for-each select="$entry/attendee">
                                <xsl:sort select='@status' />
                                <tr>
                                    <xsl:choose>
                                        <xsl:when test="@cuid='@'">
                                            <td class="table-light-left">
                                                <img border="0" src="{$baseImagePath}/persons_16.gif" alt="group" title="group" />
												&#160;Everyone
                                            </td>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <td class="table-light-left">
                                                <xsl:choose>
                                                    <xsl:when test="@itype='G'">
                                                        <img border="0" src="{$baseImagePath}/persons_16.gif" alt="group" title="group" />
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <img border="0" src="{$baseImagePath}/person_16.gif" alt="Individual" title="Individual" />
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                &#160;<xsl:value-of select="text()" />
                                            </td>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <td class="table-content-right">
                                        <xsl:choose>
                                            <xsl:when test='@status="ACCEPTED"'>&#160;Accepted</xsl:when>
                                            <xsl:when test='@status="NEEDS-ACTION"'>&#160;Pending</xsl:when>
                                            <xsl:otherwise>Declined</xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            <xsl:for-each select="/calendar-system/user">
                                <xsl:if test="@selected='true'">
                                    <tr>
                                        <xsl:choose>
                                            <xsl:when test="@cuid='@'">
                                                <td class="table-light-left">
                                                    <a href="{$mgoURL}=gotogroup&amp;calid={@calid}" title="View">
                                                        <img border="0" src="{$baseImagePath}/persons_16.gif" alt="group" title="group" />
                                                    </a>
                                                    &#160;Everyone
                                                    <a href="{$mdoURL}=delete&amp;user={@cuid}" title="Delete">
                                                        <img src="{$baseImagePath}/delete_12.gif" border="0" alt="" title="" />
                                                    </a>
                                                </td>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <td class="table-light-left">
                                                    <xsl:choose>
                                                        <xsl:when test="@itype='G'">
                                                            <img border="0" src="{$baseImagePath}/persons_16.gif" alt="group" title="group" />
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <img border="0" src="{$baseImagePath}/person_16.gif" alt="Individual" title="Individual" />
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                    &#160;<xsl:value-of select="text()" />
                                                    <a href="{$mdoURL}=delete&amp;user={@cuid}" title="Delete">
                                                        <img src="{$baseImagePath}/delete_12.gif" border="0" alt="" title="" />
                                                    </a>
                                                </td>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </tr>
                                </xsl:if>
                            </xsl:for-each>
						</table>
<!--view repeat event-->
						
						<!--UniAcc: Layout Table -->
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-channel-text">
                            <xsl:if test="$isReccurent and /calendar-system/view/@pages != '0'">
                                <tr>
                                    <th class="th">&#160;&#160;Repeating</th>
                                </tr>
<!--view  repeating -->
                                <xsl:for-each select="/calendar-system/view/calendar/entry">
                                    <tr>
                                        <td class="table-content-single">
                                            <xsl:variable name="ceid" select="@ceid" />
                                            <xsl:apply-templates select="/calendar-system/calendar[@calid=$calid]/entry[@ceid=$ceid]">
                                                <xsl:with-param name="view-event">yes</xsl:with-param>
                                            </xsl:apply-templates>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                                <tr>
                                    <td class="table-content-single">
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
                        </table>
                    </td>
                </tr>
                <tr>
<!-- submit bottom -->
                    <td colspan="2" align="left" class="table-nav">
                        <input type="hidden" name="sid" value="{$sid}" />
                        <xsl:call-template name="bform" />
                        <img src="{$SPACER}" border="0" width="1" alt="" title="" />
                        <input class="uportal-button" type="submit" name="go~{$back}" value="Close" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
<!--/template show-->
</xsl:stylesheet>

