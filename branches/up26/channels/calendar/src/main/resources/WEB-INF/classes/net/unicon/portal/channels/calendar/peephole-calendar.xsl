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

<!-- Include -->
   <xsl:include href="mini-monthly.xsl" />

   <xsl:include href="lms-mini-monthly.xsl" />

<!-- will need to add privileges-->
<!-- peephole Calendar -->
   <xsl:template name="Calendar">
      <xsl:variable name="mm">
         <xsl:if test='starts-with($mini-daily/@date,"1/")'>January</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"2/")'>February</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"3/")'>March</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"4/")'>April</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"5/")'>May</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"6/")'>June</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"7/")'>July</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"8/")'>August</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"9/")'>September</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"10/")'>October</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"11/")'>November</xsl:if>
         <xsl:if test='starts-with($mini-daily/@date,"12/")'>December</xsl:if>
      </xsl:variable>

      <xsl:variable name="dd">
         <xsl:value-of select="substring-before(substring-after($mini-daily/@title,'/'),'/')" />
      </xsl:variable>

      <xsl:variable name="yy">
         <xsl:if test='contains($mini-daily/@date,"/04")'>2004</xsl:if>
         <xsl:if test='contains($mini-daily/@date,"/05")'>2005</xsl:if>
         <xsl:if test='contains($mini-daily/@date,"/06")'>2006</xsl:if>
         <xsl:if test='contains($mini-daily/@date,"/07")'>2007</xsl:if>
      </xsl:variable>

<!-- Nav bar -->
      <!--
      <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
      	
         <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
               <td class="views-title">
                  <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" align="absmiddle" alt="channel options section" title="channel options section" />
               </td>

               <td class="views" valign="middle" height="26" width="100%">
                  <xsl:choose>
                     <xsl:when test="$calid">
                        <a href="{$mdoURL}=update&amp;op=d~{$mini-daily/@date}&amp;calid={$calid}&amp;window=event" title="View" onmouseover="swapImage('calendarEventDayViewImage{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarEventDayViewImage{$channelID}','channel_view_base.gif')">
                           <xsl:value-of select="concat(substring-before($mini-daily/@title,','),',','&#160;',$mm,'&#160;',$dd,',','&#160;',$yy)" />
                           <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                           <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" align="absmiddle" name="calendarEventDayViewImage{$channelID}" id="calendarEventDayViewImage{$channelID}" alt="View this day's events" title="View this day's events" />
                        </a>
                     </xsl:when>
                     <xsl:otherwise>
                        <a href="{$mdoURL}=update&amp;op=d~{$mini-daily/@date}&amp;calid={$logon-user}&amp;window=event" title="View" onmouseover="swapImage('calendarEventDayViewImage{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarEventDayViewImage{$channelID}','channel_view_base.gif')">
                           <xsl:value-of select="concat(substring-before($mini-daily/@title,','),',','&#160;',$mm,'&#160;',$dd,',','&#160;',$yy)" />
                           <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                           <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" align="absmiddle" name="calendarEventDayViewImage{$channelID}" id="calendarEventDayViewImage{$channelID}" alt="View this day's events" title="View this day's events" />
                        </a>
                     </xsl:otherwise>
                  </xsl:choose>
               </td>
            </tr>
         </table>
      </xsl:if>
      -->
      <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
         <xsl:call-template name="mini-monthly" />
      </xsl:if>
      <xsl:if test="contains($targetChannel, 'CCalendarUnicon')">
         <xsl:call-template name="mini-monthly" />
      </xsl:if>
   </xsl:template>

<!--/////////////////////////////////////////////////////////////////////-->
</xsl:stylesheet>

