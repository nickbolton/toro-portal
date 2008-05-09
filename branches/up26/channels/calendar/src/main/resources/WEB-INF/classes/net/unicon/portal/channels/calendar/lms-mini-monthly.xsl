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
  <xsl:output method="html"/>
  
  <xsl:template name="lms-mini-monthly">
      <!--UniAcc: Layout Table -->
  <form method="post" action="{$baseActionURL}" id="changeMonthViewFormLms" name="changeMonthViewFormLms">
   <xsl:call-template name="mform"/>
    <table cellspacing="0" cellpadding="0" border="0" class="uportal-background-light" width="1" height="1">
        <tr>
          <td align="left" valign="middle" nowrap="nowrap">
          <br/>
            <xsl:choose>
              <xsl:when test="$calid">
                <a href="{$mdoURL}=update&amp;op=m~{$mini-monthly/@date}&amp;calid={$calid}&amp;window=event" title="Display">
                 <img border="0" src="{$SPACER}" alt="" title=""/>
                 <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_weekview.gif" alt="monthly layout" title="monthly layout"/>
                 <img border="0" src="{$SPACER}" alt="" title=""/>
                 </a> 
            </xsl:when>
              <xsl:otherwise>
                <a href="{$mdoURL}=update&amp;op=m~{$mini-monthly/@date}&amp;calid={$logon-user}&amp;window=event" title="Display">
                 <img border="0" src="{$SPACER}" alt="" title=""/>
                 <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_weekview.gif"   alt="monthly layout" title="monthly layout"/>
                 <img border="0" src="{$SPACER}" alt="" title=""/>
                 </a> 
            </xsl:otherwise>
            </xsl:choose>
            <select name="month" class="text">
              <option value="1">
                <xsl:if test='starts-with($mini-monthly/@date,"1/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Jan</option>
              <option value="2">
                <xsl:if test='starts-with($mini-monthly/@date,"2/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Feb</option>
              <option value="3">
                <xsl:if test='starts-with($mini-monthly/@date,"3/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Mar</option>
              <option value="4">
                <xsl:if test='starts-with($mini-monthly/@date,"4/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Apr</option>
              <option value="5">
                <xsl:if test='starts-with($mini-monthly/@date,"5/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>May</option>
              <option value="6">
                <xsl:if test='starts-with($mini-monthly/@date,"6/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Jun</option>
              <option value="7">
                <xsl:if test='starts-with($mini-monthly/@date,"7/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Jul</option>
              <option value="8">
                <xsl:if test='starts-with($mini-monthly/@date,"8/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Aug</option>
              <option value="9">
                <xsl:if test='starts-with($mini-monthly/@date,"9/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Sep</option>
              <option value="10">
                <xsl:if test='starts-with($mini-monthly/@date,"10/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Oct</option>
              <option value="11">
                <xsl:if test='starts-with($mini-monthly/@date,"11/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Nov</option>
              <option value="12">
                <xsl:if test='starts-with($mini-monthly/@date,"12/")'>
                  <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>Dec</option>
            </select>
            <select name="year" class="text">
				<xsl:call-template name="year-options">
					<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($mini-monthly/@date,'_') ,'/') ,'/')"/>
				</xsl:call-template>
            </select>
            <xsl:text>&#160;</xsl:text>
            <!-- change month/year -->
            <input type="hidden" name="calid" value='{$calid}'/>
            <input type="hidden" name="fixChangeMonthViewLms" value=""/>
            <a href="javascript:document.changeMonthViewFormLms.fixChangeMonthViewLms.name='do~changeDate';javascript:document.changeMonthViewFormLms.submit()" title="Change to" onmouseover="swapImage('calendarEventChangeDateImage{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarEventChangeDateImage{$channelID}','channel_view_base.gif')">
                 <img border="0" src="{$SPACER}" width="3" alt="" title=""/>
                 <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"  align="absmiddle" name="calendarEventChangeDateImage{$channelID}" id="calendarEventChangeDateImage{$channelID}" alt="display selected month and year" title="display selected month and year"/>
             </a>
			 <a href="javascript:document.changeMonthViewFormLms.fixChangeMonthViewLms.name='do~previousDate';javascript:document.changeMonthViewFormLms.submit()" title="Previous">
				<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
	         </a>
				<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
	         <a href="javascript:document.changeMonthViewFormLms.fixChangeMonthViewLms.name='do~nextDate';javascript:document.changeMonthViewFormLms.submit()" title="Next">
	            <img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
	         </a>
         </td>
        </tr>
        <!-- Brief Day title -->
        <tr align="center">
          <td>
              <!--UniAcc: Data Table -->
            <table cellspacing="0" cellpadding="0" border="0" width="100%">
                <caption><xsl:value-of select="$MONTH" />&#160;Calendar</caption>
                <tr>
                    <td colspan="8" class="th-calendar">
                        <img border="0" src="{$SPACER}" height="8" alt="" title=""/>
                     </td>
                 </tr>
                <tr>
                    <th class="th-calendar">
                        <img border="0" src="{$SPACER}" alt="" title=""/>
                     </th>
                     <th class="weekend-title1" align="right" scope="col">Su<img border="0" width="1" height="1" src="{$SPACER}" alt="Sunday" title="Sunday"/></th>
                    <th class="weekday-title" align="right" scope="col">Mo<img border="0" width="1" height="1" src="{$SPACER}" alt="Monday" title="Monday"/></th>
                    <th class="weekday-title" align="right" scope="col">Tu<img border="0" width="1" height="1" src="{$SPACER}" alt="Tuesday" title="Tuesday"/></th>
                    <th class="weekday-title" align="right" scope="col">We<img border="0" width="1" height="1" src="{$SPACER}" alt="Wednesday" title="Wednesday"/></th>
                    <th class="weekday-title" align="right" scope="col">Th<img border="0" width="1" height="1" src="{$SPACER}" alt="Thursday" title="Thursday"/></th>
                    <th class="weekday-title" align="right" scope="col">Fr<img border="0" width="1" height="1" src="{$SPACER}" alt="Friday" title="Friday"/></th>
                    <th class="weekend-title2" align="right" scope="col">Sa<img border="0" width="1" height="1" src="{$SPACER}" alt="Saturday" title="Saturday"/></th>
                  </tr>
                  <!-- Fill days of month -->
                  <xsl:call-template name="lms-mini-monthly-week">
                    <xsl:with-param name="dom" select="1 - $mini-monthly/@wdo1"/>
                  </xsl:call-template>
                  <xsl:call-template name="lms-mini-monthly-week">
                    <xsl:with-param name="dom" select="8 - $mini-monthly/@wdo1"/>
                  </xsl:call-template>
                  <xsl:call-template name="lms-mini-monthly-week">
                    <xsl:with-param name="dom" select="15 - $mini-monthly/@wdo1"/>
                  </xsl:call-template>
                  <xsl:call-template name="lms-mini-monthly-week">
                    <xsl:with-param name="dom" select="22 - $mini-monthly/@wdo1"/>
                  </xsl:call-template>
                  <xsl:call-template name="lms-mini-monthly-week">
                    <xsl:with-param name="dom" select="29 - $mini-monthly/@wdo1"/>
                  </xsl:call-template>
                  <xsl:if test="($mini-monthly/@ldom > 30 and $mini-monthly/@wdo1 >= 5) or $mini-monthly/@wdo1 >= 6">
                    <xsl:call-template name="lms-mini-monthly-week">
                      <xsl:with-param name="dom" select="36 - $mini-monthly/@wdo1"/>
                    </xsl:call-template>
                  </xsl:if>
                  <tr>
                    <td colspan="8" class="th-calendar">                      
                        <img border="0" src="{$SPACER}" height="4" alt="" title=""/>
                     </td> 
                   </tr>
            </table>
          </td>
        </tr>
    </table>
  </form>
  </xsl:template>
 <!-- Fill days of week -->
  <xsl:template name="lms-mini-monthly-week">
    <xsl:param name="dom"/>
    <xsl:variable name="m" select='substring-before($mini-monthly/@date,"/")'/>
    <xsl:variable name="d" select='substring-before(substring-after($mini-monthly/@date,"/"),"/")'/>
    <xsl:variable name="y" select='substring-after(substring-after($mini-monthly/@date,"/"),"/")'/>
    <tr align="center">
      <td align="left" class="th-calendar">
        <!-- first day of week -->
        <xsl:variable name="fdow">
          <xsl:choose>
            <xsl:when test="$dom > 0 and $mini-monthly/@ldom >= $dom">
              <xsl:value-of select='concat($m,"/",$dom,"/",$y)'/>
            </xsl:when>
            <xsl:when test="1 + $dom > 0 and $mini-monthly/@ldom >= 1+ $dom">
              <xsl:value-of select='concat($m,"/", 1 + $dom,"/",$y)'/>
            </xsl:when>
            <xsl:when test="2 + $dom > 0 and $mini-monthly/@ldom >= 2+ $dom">
              <xsl:value-of select='concat($m,"/", 2 + $dom,"/",$y)'/>
            </xsl:when>
            <xsl:when test="3 + $dom > 0 and $mini-monthly/@ldom >= 3+ $dom">
              <xsl:value-of select='concat($m,"/", 3 + $dom,"/",$y)'/>
            </xsl:when>
            <xsl:when test="4 + $dom > 0 and $mini-monthly/@ldom >= 4+ $dom">
              <xsl:value-of select='concat($m,"/", 4 + $dom,"/",$y)'/>
            </xsl:when>
            <xsl:when test="5 + $dom > 0 and $mini-monthly/@ldom >= 5+ $dom">
              <xsl:value-of select='concat($m,"/", 5 + $dom,"/",$y)'/>
            </xsl:when>
            <xsl:when test="6 + $dom > 0 and $mini-monthly/@ldom >= 6+ $dom">
              <xsl:value-of select='concat($m,"/", 6 + $dom,"/",$y)'/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name = "STARTWEEK" select="substring-before($fdow,'_')"/>
        <xsl:choose>
          <xsl:when test="$calid">
            <a href="{$mdoURL}=update&amp;op=w~{$fdow}&amp;calid={$calid}&amp;window=event" title="Display">
             <img border="0" src="{$SPACER}" alt="" title=""/>
             <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_weekview.gif"   alt="week starting {$STARTWEEK}" title="week starting {$STARTWEEK}"/>
             <img border="0" src="{$SPACER}" alt="" title=""/>
             </a> 
        </xsl:when>
          <xsl:otherwise>
            <a href="{$mdoURL}=update&amp;op=w~{$fdow}&amp;calid={$logon-user}&amp;window=event" title="Display">
             <img border="0" src="{$SPACER}" alt="" title=""/>
             <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_weekview.gif"   alt="week starting {$STARTWEEK}" title="week starting {$STARTWEEK}"/>
             <img border="0" src="{$SPACER}" alt="" title=""/>
             </a> 
        </xsl:otherwise>
        </xsl:choose>
      </td>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="$dom"/>
        <xsl:with-param name="dow">1</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="1 + $dom"/>
      </xsl:call-template>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="2 + $dom"/>
      </xsl:call-template>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="3 + $dom"/>
      </xsl:call-template>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="4 + $dom"/>
      </xsl:call-template>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="5 + $dom"/>
      </xsl:call-template>
      <xsl:call-template name="lms-mini-monthly-day">
        <xsl:with-param name="dom" select="6 + $dom"/>
        <xsl:with-param name="dow" select="7"/>
      </xsl:call-template>
    </tr>
  </xsl:template>
  <!-- Day -->
  <xsl:template name="lms-mini-monthly-day">
    <xsl:param name="dom"/>
    <xsl:param name="dow"/>
    <xsl:variable name="m" select='substring-before($mini-monthly/@date,"/")'/>
    <xsl:variable name="d" select='substring-before(substring-after($mini-monthly/@date,"/"),"/")'/>
    <xsl:variable name="y" select='substring-after(substring-after($mini-monthly/@date,"/"),"/")'/>
    <xsl:variable name="yy" select='substring-before(substring-after(substring-after($mini-monthly/@date,"/"),"/"),"_")'/>
    <xsl:variable name="cur_m" select='substring-before($mini-daily/@date,"/")'/>
    <xsl:variable name="cur_d" select='substring-before(substring-after($mini-daily/@date,"/"),"/")'/>
    <xsl:variable name="cur_yy" select='substring-before(substring-after(substring-after($mini-daily/@date,"/"),"/"),"_")'/>
    <xsl:variable name="hightlight">
      <xsl:choose>
        <xsl:when test="$dom > 0 and $mini-monthly/@ldom >= $dom">
          <xsl:choose>
            <xsl:when test="$cur_yy=$yy and $cur_m=$m and $cur_d=$dom">
              <xsl:choose>
                <xsl:when test="$dow = 1">
                  <xsl:text>current-day1</xsl:text>
                </xsl:when>
                <xsl:when test="$dow = 7">
                  <xsl:text>current-day7</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>current-day</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:when test="$dow = 1">
              <xsl:text>weekend1</xsl:text>
            </xsl:when>
            <xsl:when test="$dow = 7">
              <xsl:text>weekend7</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>weekday</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$dow = 1">
              <xsl:text>not-a-day1</xsl:text>
            </xsl:when>
            <xsl:when test="$dow = 7">
              <xsl:text>not-a-day7</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>not-a-day</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <td align="center">
      <xsl:attribute name="class"><xsl:value-of select="$hightlight"/></xsl:attribute>
      <xsl:choose>
        <!--    <xsl:when test="0 >= $dom">
            <xsl:value-of select="$dom + $mini-monthly/@ldopm"/>
        </xsl:when>-->
        <xsl:when test="$dom > 0 and $mini-monthly/@ldom >= $dom">
          <xsl:choose>
            <xsl:when test="$calid">
              <xsl:choose>
                <xsl:when test="$cur_yy=$yy and $cur_m=$m and $cur_d=$dom">
                  <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$calid}&amp;window=event" class="calendar-event" title="Display events for day">
                     <xsl:value-of select="$dom"/> 
                   </a>
                </xsl:when>
                <xsl:otherwise>
                  <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$calid}&amp;window=event" class="calendar" title="Display events for day">
                         <xsl:value-of select="$dom"/>
                   </a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="$cur_yy=$yy and $cur_m=$m and $cur_d=$dom">
                  <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$logon-user}&amp;window=event" class="calendar-event" title="Display events for day">
                     <xsl:value-of select="$dom"/> 
                 </a>
                </xsl:when>
                <xsl:otherwise>
                  <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$logon-user}&amp;window=event" class="calendar" title="Display events for day">
                     <xsl:value-of select="$dom"/>
                 </a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
            <img border="0" src="{$SPACER}" alt="" title=""/>
         </xsl:otherwise>
      </xsl:choose>
    </td>
  </xsl:template>

</xsl:stylesheet>
