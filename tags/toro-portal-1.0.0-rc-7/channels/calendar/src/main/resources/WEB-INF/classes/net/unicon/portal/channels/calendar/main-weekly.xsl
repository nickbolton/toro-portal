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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
    <xsl:include href="common.xsl" />
    <xsl:include href="peephole-calendar.xsl" />
    <xsl:include href="detail-weekly.xsl" />
    <xsl:include href="priority.xsl" />
    <xsl:include href="access-detail.xsl" />
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="mini-event.xsl" />
    <xsl:include href="mini-todo.xsl" />
    <xsl:include href="date-widget.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="main">monthly</xsl:param>
    <xsl:param name="window">event</xsl:param>
    <xsl:key name="access" match="/calendar-system/logon/access" use="@calid" />
    <xsl:variable name="detail-weekly" select="/calendar-system/detail-weekly" />
    <xsl:variable name="mini-daily" select="/calendar-system/mini-daily" />
    <xsl:template match="/">
        <xsl:call-template name="links" />
        <!--UniAcc: Layout Table -->
        <table cellspacing="0" border="0" cellpadding="0" width='100%'>
            <tr>
                <xsl:choose>
                    <xsl:when test="contains($targetChannel, 'CCalendarUnicon')">
<!-- Main weekly -->
                        <td valign="top">
                            <xsl:call-template name="detail-weekly" />
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td>
                            <xsl:call-template name="detail-weekly" />
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
<!-- /////Hidden
      <tr>
         -->
<!-- Mini event -->
<!--
        <td>
          <xsl:variable name="right" select="key('access',$calid)"/>
           -->
<!-- in case of not all-calendars -->
<!--
          <xsl:if test="$calid='all-calendars' or ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
          <xsl:choose>
            <xsl:when test="$window='event'">
              <xsl:call-template name="mini-event">
                <xsl:with-param name="mini-daily" select="$detail-weekly"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="$window='todo'">
              <xsl:call-template name="mini-todo">
                <xsl:with-param name="mini-daily" select="$detail-weekly"/>
              </xsl:call-template>
            </xsl:when>
          </xsl:choose>
        </xsl:if>
        </td>
      </tr>
      ///// -->
        </table>
    </xsl:template>
</xsl:stylesheet>
