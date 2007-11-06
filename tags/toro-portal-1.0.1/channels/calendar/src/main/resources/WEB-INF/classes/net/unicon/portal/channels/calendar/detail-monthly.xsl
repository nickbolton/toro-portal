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
	 <xsl:param name="condition">1</xsl:param>
    <xsl:template name="detail-monthly">
        
        <xsl:call-template name="monthly-header"/>
        
        <form method="post" action="{$baseActionURL}" name="calendarMonthlyChangeDateForm{$channelID}">
        	<!--UniAcc: Layout Table -->
			
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
				<tr>
                    <th class="th-top" align="left">

						<div class="event-title">
							<xsl:value-of select="$detail-monthly/@title"/>
							<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
							<a href="javascript:document.calendarMonthlyChangeDateForm{$channelID}.fixChangeDate.name='do~previousDate';javascript:document.calendarMonthlyChangeDateForm{$channelID}.submit()" title="Previous">
								<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
							</a>
								<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
							<a href="javascript:document.calendarMonthlyChangeDateForm{$channelID}.fixChangeDate.name='do~nextDate';javascript:document.calendarMonthlyChangeDateForm{$channelID}.submit()" title="Next">
							<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
							</a>							
						</div>	  
						<div class="event-subtitle">
							<!--<xsl:choose>
								<xsl:when test="/calendar-system/view/calendar/@calid = 'all-calendars'">
									all calendars
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="/calendar-system/view/calendar/@calid"/> calendar
								</xsl:otherwise>
							</xsl:choose>
							-->
							<xsl:value-of select="/calendar-system/calendar[@calid = /calendar-system/view/calendar/@calid]/@calname"/>
						</div>                        
                        
                        <input type="hidden" name="calid">
							<xsl:attribute name="value">
								<xsl:value-of select="/calendar-system/view/calendar/@calid" />
							</xsl:attribute>
						</input>
						<input type="hidden" name="op" value="v~monthly"/>
						<input type="hidden" name="fixChangeView" value="" />
						<xsl:call-template name="mform" />
						
					</th>
					<td class="th-top">
						<div class="calendar-navigation-right">
							<select name="month" class="text" id="CDM-ActivityS1">
								<option value="1">
								<xsl:if test='starts-with($detail-monthly/@date,"1/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Jan</option>
								<option value="2">
								<xsl:if test='starts-with($detail-monthly/@date,"2/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Feb</option>
								<option value="3">
								<xsl:if test='starts-with($detail-monthly/@date,"3/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Mar</option>
								<option value="4">
								<xsl:if test='starts-with($detail-monthly/@date,"4/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Apr</option>
								<option value="5">
								<xsl:if test='starts-with($detail-monthly/@date,"5/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								May</option>
								<option value="6">
								<xsl:if test='starts-with($detail-monthly/@date,"6/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Jun</option>
								<option value="7">
								<xsl:if test='starts-with($detail-monthly/@date,"7/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Jul</option>
								<option value="8">
								<xsl:if test='starts-with($detail-monthly/@date,"8/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Aug</option>
								<option value="9">
								<xsl:if test='starts-with($detail-monthly/@date,"9/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Sep</option>
								<option value="10">
								<xsl:if test='starts-with($detail-monthly/@date,"10/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Oct</option>
								<option value="11">
								<xsl:if test='starts-with($detail-monthly/@date,"11/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Nov</option>
								<option value="12">
								<xsl:if test='starts-with($detail-monthly/@date,"12/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Dec</option>
							</select>
							<select name="year" class="text">
								<xsl:call-template name="year-options">
									<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($detail-monthly/@date,'_') ,'/') ,'/')"/>
								</xsl:call-template>							
							</select>
							<input type="hidden" name="fixChangeDate" value="" />
							<input type="button" onclick="document.calendarMonthlyChangeDateForm{$channelID}.fixChangeDate.name='do~changeDate'; document.calendarMonthlyChangeDateForm{$channelID}.submit()" value="GO" class="uportal-button"/>
						</div>
                    </td>
                </tr>                
                <!--
                <tr>
                	<td class="th-top">
				 		
				 		
						<xsl:variable name="m">
							<xsl:value-of select="substring-before($detail-monthly/@date,'/')"/>
						</xsl:variable>
						<xsl:variable name="y">
							<xsl:value-of select="substring-after(substring-after(substring-before($detail-monthly/@date,'_'),'/'),'/')" />
						</xsl:variable>

						<input type="text" name="fixChangeDate" id="fixChangeDate{$channelID}" value="" />
						<input type="text" name="year" value="20{$y}"/>
						<input type="text" name="month" value="{$m}"/>
						<input type="text" name="op" value="v~monthly"/>
						
						<div class="event-title">
							<xsl:value-of select="$detail-monthly/@title"/>
							
							<a href="javascript:document.calendarMonthlyChangeDateForm{$channelID}.fixChangeDate.name='do~previousDate';javascript:document.calendarMonthlyChangeDateForm{$channelID}.submit()" title="Previous">
								<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
							</a>
							<a href="javascript:document.calendarMonthlyChangeDateForm{$channelID}.fixChangeDate.name='do~nextDate';javascript:document.calendarMonthlyChangeDateForm{$channelID}.submit()" title="Next">
								<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
							</a>
							
						</div>
						<div class="event-subtitle">
							<xsl:choose>
								<xsl:when test="/calendar-system/view/calendar/@calid = 'all-calendars'">
									all calendars
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="/calendar-system/view/calendar/@calid"/> calendar
								</xsl:otherwise>
							</xsl:choose>
						</div>     
						<div class="calendar-navigation-right">
							<select name="month" class="text" id="CDM-ActivityS1">
								<option value="1">
								<xsl:if test='starts-with($detail-monthly/@date,"1/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Jan</option>
								<option value="2">
								<xsl:if test='starts-with($detail-monthly/@date,"2/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Feb</option>
								<option value="3">
								<xsl:if test='starts-with($detail-monthly/@date,"3/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Mar</option>
								<option value="4">
								<xsl:if test='starts-with($detail-monthly/@date,"4/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Apr</option>
								<option value="5">
								<xsl:if test='starts-with($detail-monthly/@date,"5/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								May</option>
								<option value="6">
								<xsl:if test='starts-with($detail-monthly/@date,"6/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Jun</option>
								<option value="7">
								<xsl:if test='starts-with($detail-monthly/@date,"7/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Jul</option>
								<option value="8">
								<xsl:if test='starts-with($detail-monthly/@date,"8/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Aug</option>
								<option value="9">
								<xsl:if test='starts-with($detail-monthly/@date,"9/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Sep</option>
								<option value="10">
								<xsl:if test='starts-with($detail-monthly/@date,"10/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Oct</option>
								<option value="11">
								<xsl:if test='starts-with($detail-monthly/@date,"11/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Nov</option>
								<option value="12">
								<xsl:if test='starts-with($detail-monthly/@date,"12/")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								Dec</option>
							</select>
							<select name="year" class="text">
								<option value="2004">
								<xsl:if test='contains($detail-monthly/@date,"/04")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								04</option>
								<option value="2005">
								<xsl:if test='contains($detail-monthly/@date,"/05")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								05</option>
								<option value="2006">
								<xsl:if test='contains($detail-monthly/@date,"/06")'>
									<xsl:attribute name="selected">true</xsl:attribute>
								</xsl:if>
								06</option>
							</select>
							<a href="javascript:document.getElementById('fixChangeDate{$channelID}').name='do~changeDate'; document.calendarMonthlyChangeDateForm{$channelID}.submit()" title="Change date">
								<img border="0" src="{$SPACER}" width="3" alt="" title="" />
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" align="absmiddle" name="calendarEventChangeDateImage{$channelID}" id="calendarEventChangeDateImage{$channelID}" alt="Change date" title="Change date" />
							</a>							
						</div>
					</td>
                </tr>
                -->
                <tr>
                    <td colspan="2">
                        <table width="100%" cellspacing="1" cellpadding="0" border="0">
                            <tr>
                                <th width="14%" class="weekview-title1" scope="col">Sunday</th>
                                <th width="14%" class="weekview-title" scope="col">Monday</th>
                                <th width="14%" class="weekview-title" scope="col">Tuesday</th>
                                <th width="14%" class="weekview-title" scope="col">Wednesday</th>
                                <th width="14%" class="weekview-title" scope="col">Thursday</th>
                                <th width="14%" class="weekview-title" scope="col">Friday</th>
                                <th width="14%" class="weekview-title7" scope="col">Saturday</th>
                            </tr>
                            <xsl:call-template name="detail-monthly-week">
                                <xsl:with-param name="dom" select="1 - $detail-monthly/@wdo1" />
                            </xsl:call-template>
                            <xsl:call-template name="detail-monthly-week">
                                <xsl:with-param name="dom" select="8 - $detail-monthly/@wdo1" />
                            </xsl:call-template>
                            <xsl:call-template name="detail-monthly-week">
                                <xsl:with-param name="dom" select="15 - $detail-monthly/@wdo1" />
                            </xsl:call-template>
                            <xsl:call-template name="detail-monthly-week">
                                <xsl:with-param name="dom" select="22 - $detail-monthly/@wdo1" />
                            </xsl:call-template>
                            <xsl:call-template name="detail-monthly-week">
                                <xsl:with-param name="dom" select="29 - $detail-monthly/@wdo1" />
                            </xsl:call-template>
                            <xsl:if test="($detail-monthly/@ldom &gt; 30 and $detail-monthly/@wdo1 &gt;= 5) or $detail-monthly/@wdo1 &gt;= 6">
                                <xsl:call-template name="detail-monthly-week">
                                    <xsl:with-param name="dom" select="36 - $detail-monthly/@wdo1" />
                                </xsl:call-template>
                            </xsl:if>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
    
<!-- Header of detail-daily -->
     <xsl:template name="monthly-header">

		<form method="post" action="{$baseActionURL}" name="calendarNavigationForm{$channelID}" id="calendarNavigationForm{$channelID}">

			<div class="calendar-navigation-container">

				<xsl:variable name="m">
					<xsl:value-of select="substring-before($detail-monthly/@date,'/')"/>
				</xsl:variable>
				<xsl:variable name="y">
					<xsl:value-of select="substring-after(substring-after(substring-before($detail-monthly/@date,'_'),'/'),'/')" />
				</xsl:variable>
				<xsl:variable name="d">
					<xsl:value-of select="substring-before(substring-after(substring-before($detail-monthly/@date,'_'),'/'),'/')" />
				</xsl:variable>

				<input type="hidden" name="year" value="20{$y}"/>
				<input type="hidden" name="month" value="{$m}"/>
				<input type="hidden" name="date" value="{$d}"/>

				<input type="hidden" name="fixChangeView" id="fixChangeView{$channelID}" value="" />
				<input type="hidden" name="op" id="op{$channelID}"/>

				<div class="calendar-tab">
					<a class="whitetext" href="javascript:document.getElementById('fixChangeView{$channelID}').name='do~update'; document.getElementById('op{$channelID}').value='v~daily'; document.getElementById('calendarNavigationForm{$channelID}').submit();" title="Weekly View">
						Day
					</a>
				</div>
				<div class="calendar-tab">
					<a class="whitetext" href="javascript:document.getElementById('fixChangeView{$channelID}').name='do~update'; document.getElementById('op{$channelID}').value='v~weekly'; document.getElementById('calendarNavigationForm{$channelID}').submit();" title="Monthly View">
						Week
					</a>
				</div>
				<div class="calendar-tab-selected">
					Month
				</div>

				<!--
					 <select name="op" class="text" id="CDD-ViewS1">
						  <option value="v~monthly">Monthly</option>
						  <option value="v~weekly">Weekly</option>
						  <option value="v~daily">
						  <xsl:attribute name="selected">true</xsl:attribute>
						  Daily</option>
					 </select>


				-->
				<xsl:call-template name="mform" />
				<div class="calendar-navigation-right">
					
					<xsl:choose>
						<xsl:when test="contains($targetChannel, 'CCalendarUnicon')">
							 <input type="hidden" name="calid">
								  <xsl:attribute name="value">
									   <xsl:value-of select="/calendar-system/view/calendar/@calid" />
								  </xsl:attribute>
							 </input>
						</xsl:when>
						<xsl:otherwise>
							<span class="uportal-text"><label for="CDDS1-Calender">View:</label></span>

							<select name="calid" size="1" class="text" id="CDDS1-Calender" onchange="javascript:document.getElementById('cal_{$channelID}').name='do~update'; document.getElementById('calendarNavigationForm{$channelID}').submit()">
								<option value="all-calendars">All calendars</option>
								<xsl:for-each select="//calendar-system/preference/calendar-group">
								   <option value="composite_{@id}">
								   <xsl:variable name="gid">composite_<xsl:value-of select="@id" /></xsl:variable>
								   <xsl:if test="//calendar-system/view/calendar/@calid = $gid">
										<xsl:attribute name="selected" />
								   </xsl:if>
								   <xsl:value-of select="@name" />
								   &#160;composite view</option>
								</xsl:for-each>
								<!-- ////////////////////////////////////////////// -->
								<!-- own calendars -->
								<xsl:apply-templates select="calendar-system/calendar[@owner=$cur_user]" mode="owner">
								   <xsl:with-param name="curCalid" select="//calendar-system/view/calendar/@calid" />
								   <xsl:sort select="@calname" />
								</xsl:apply-templates>
								<!-- ////////////////////////////////////////////// -->
								<!-- shared calendars -->
								<xsl:apply-templates select="calendar-system/calendar[@owner!=$cur_user and not(key('calhidden', @calid))]" mode="shared">
								   <xsl:with-param name="curCalid" select="//calendar-system/view/calendar/@calid" />
								   <xsl:sort select="@owner" />
								</xsl:apply-templates>
							</select>

						</xsl:otherwise>
					</xsl:choose>
					<input type="hidden" name="sid" value="{$sid}" />
					<input type='hidden' name='cal_{$channelID}' id="cal_{$channelID}" value="" />

				</div>
			</div>	

		</form>
			
	</xsl:template>    
    
    <xsl:template name="detail-monthly-week">
        <xsl:param name="dom" />
        <tr align="left" class="uportal-background-content">
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="$dom" />
                <xsl:with-param name="dow" select="1" />
            </xsl:call-template>
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="1 + $dom" />
                <xsl:with-param name="dow" select="2" />
            </xsl:call-template>
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="2 + $dom" />
                <xsl:with-param name="dow" select="3" />
            </xsl:call-template>
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="3 + $dom" />
                <xsl:with-param name="dow" select="4" />
            </xsl:call-template>
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="4 + $dom" />
                <xsl:with-param name="dow" select="5" />
            </xsl:call-template>
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="5 + $dom" />
                <xsl:with-param name="dow" select="6" />
            </xsl:call-template>
            <xsl:call-template name="detail-monthly-day">
                <xsl:with-param name="dom" select="6 + $dom" />
                <xsl:with-param name="dow" select="7" />
            </xsl:call-template>
        </tr>
    </xsl:template>
<!-- detail day -->
    <xsl:template name="detail-monthly-day">
        <xsl:param name="dom" />
        <xsl:param name="dow" />
        <xsl:variable name="m" select='substring-before($detail-monthly/@date,"/")' />
        <xsl:variable name="d" select='substring-before(substring-after($detail-monthly/@date,"/"),"/")' />
        <xsl:variable name="y" select='substring-after(substring-after($detail-monthly/@date,"/"),"/")' />
        <xsl:variable name="yy" select='substring-before(substring-after(substring-after($detail-monthly/@date,"/"),"/"),"_")' />
        <xsl:variable name="right" select='key("access",$calid)' />
        <xsl:variable name="br_color">
            <xsl:choose>
                <xsl:when test="$calid ='all-calendars' or contains($calid,'composite')">
                    <xsl:for-each select='calendar-system/calendar/entry[contains(duration/@start,concat($m,"/",$dom,"/",$yy))]'>
                        <xsl:choose>
                            <xsl:when test="((event and ($window ='event')) or (todo and ($window ='todo')) )">white</xsl:when>
                            <xsl:otherwise>uportal-background-content</xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
            </xsl:choose>
<!--in case of not all-calendar -->
            <xsl:for-each select="calendar-system/calendar[@calid=$calid and $calid!='all-calendars' and not(contains($calid,'composite'))]">
                <xsl:for-each select='entry[contains(duration/@start,concat($m,"/",$dom,"/",$yy))]'>
                    <xsl:choose>
                        <xsl:when test="((event and ($window ='event')) or (todo and ($window ='todo')))">white</xsl:when>
                        <xsl:otherwise>uportal-background-content</xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <td valign="top" style="padding:5px;">
            <xsl:choose>
                <xsl:when test="(0 &gt;= $dom) or ($dom &gt; $detail-monthly/@ldom)">
                    <xsl:choose>
                        <xsl:when test="$dow = 1">
                            <xsl:attribute name="class">not-a-day</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$dow = 7">
                            <xsl:attribute name="class">not-a-day</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="class">not-a-day</xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$dow = 1">
                            <xsl:attribute name="class">weekend</xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$dow = 7">
                            <xsl:attribute name="class">weekend</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="class">weekday</xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="0 &gt;= $dom">
                	<!--UniAcc: Layout Table -->
                    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                        <tr>
                            <td valign="top">
                                <xsl:value-of select="$dom + $detail-monthly/@ldopm" />
                                <br />
                                <img src="{$SPACER}" border="0" height="50" alt="" title="" />
                            </td>
                        </tr>
                    </table>
                </xsl:when>
                <xsl:when test="$dom &gt; 0 and $detail-monthly/@ldom &gt;= $dom">
                	<!--UniAcc: Layout Table -->
                    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
<!-- EVENT -->
                        <xsl:choose>
                            <xsl:when test="$window = 'event'">
                                <tr>
                                    <td>
                                    	<!--UniAcc: Layout Table -->
                                        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                                            <tr>
                                                <td class="table-content-single">
                                                    <!--
                                                    <xsl:attribute name="class">
                                                        <xsl:choose>
                                                            <xsl:when test="$dow = 1 or $dow = 7">calendar-weekview-dom2</xsl:when>
                                                            <xsl:otherwise>calendar-weekview-dom</xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:attribute>
                                                    -->
                                                    <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={calendar-system/view/calendar/@calid}" class="calendar" style="font-size:10pt; font-weight:bold;" title="View" onmouseover="swapImage('calendarDayViewImage{$dom}{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarDayViewImage{$dom}{$channelID}','channel_view_base.gif')">
                                                        <xsl:value-of select="$dom" />
                                                    </a>
                                                    <img src="{$SPACER}" border="0" width="8" alt="" title="" />
                                                    <xsl:if test="$addEvents = 'Y'">
                                                        <xsl:choose>
                                                            <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
                                                                <a href="{$mgoURL}=Event&amp;date={concat($m,'/',$dom,'/',$y)}&amp;hour=8&amp;calid={$calid}" title="Add">
                                                                    <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$dom}{$channelID}" id="calendarEventAddImage{$dom}{$channelID}" alt="Add a new event" title="Add a new event" />
                                                                </a>
                                                            </xsl:when>
                                                            <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                                                                <a href="{$mgoURL}=Event&amp;date={concat($m,'/',$dom,'/',$y)}&amp;hour=8&amp;calid={$logon-user}" title="Add">
                                                                    <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$dom}{$channelID}" id="calendarEventAddImage{$dom}{$channelID}" alt="Add a new event" title="Add a new event" />
                                                                </a>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:if>
                                                </td>
                                            </tr>
                                        </table>
                                        <img src="{$SPACER}" border="0" height="5" width="1" alt="" title="" />
                                        <br />
<!-- EVENT ENTRY DETAIL-->
										<!--UniAcc: Layout Table -->
                                        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                                            <tr>
                                                <td>
                                                    <img src="{$SPACER}" border="0" height="50" width="1" alt="" title="" />
                                                </td>
                                                <td width="100%" valign="top">
                                                	<!--UniAcc: Layout Table -->
                                                    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                                                        <xsl:choose>
<!--in case of all-calendar -->
                                                            <xsl:when test="$calid ='all-calendars' or contains($calid,'composite')">
                                                                <xsl:for-each select="calendar-system/calendar">
                                                                    <xsl:apply-templates select='entry[contains(duration/@start,concat($m,"/",$dom,"/",$yy)) and not(todo)]' />
                                                                </xsl:for-each>
                                                            </xsl:when>
                                                            <xsl:otherwise>
<!--in case of not all-calendar -->
                                                                <xsl:for-each select="calendar-system/calendar">
                                                                    <xsl:if test="@calid=$calid and $calid!='all-calendars' and not(contains($calid,'composite'))">
                                                                        <xsl:apply-templates select='entry[contains(duration/@start,concat($m,"/",$dom,"/",$yy)) and not(todo)]' />
                                                                    </xsl:if>
                                                                </xsl:for-each>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
<!-- END EVENT ENTRY DETAIL-->
                                    </td>
                                </tr>
                            </xsl:when>
<!-- END EVENT -->
<!-- TO-DO -->
                            <xsl:when test="$window='todo'">
                                <tr>
                                    <td>
                                    	<!--UniAcc: Layout Table -->
                                        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                                            <tr>
                                                <td class="table-content-single">
                                                    <!--<xsl:attribute name="class">
                                                        <xsl:choose>
                                                            <xsl:when test="$dow = 1 or $dow = 7">calendar-weekview-dom2</xsl:when>
                                                            <xsl:otherwise>calendar-weekview-dom</xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:attribute>
                                                    -->
                                                    <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={calendar-system/view/calendar/@calid}" class="calendar" style="font-size:10pt; font-weight:bold;" title="View day" onmouseover="swapImage('calendarDayViewImage{$dom}{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarDayViewImage{$dom}{$channelID}','channel_view_base.gif')">
                                                        <xsl:value-of select="$dom" />
                                                    </a>
                                                    <img src="{$SPACER}" border="0" width="8" alt="" title="" />
                                                    <xsl:if test="$addEvents = 'Y'">
                                                        <xsl:choose>
                                                            <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
                                                                <a href="{$mgoURL}=Todo&amp;date={concat($m,'/',$dom,'/',$y)}&amp;hour=8&amp;calid={$calid}" title="Add Task">
                                                                    <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$dom}{$channelID}" id="calendarEventAddImage{$dom}{$channelID}" alt="Add a new Task" title="Add a new Task" />
                                                                </a>
                                                            </xsl:when>
                                                            <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                                                                <a href="{$mgoURL}=Todo&amp;date={concat($m,'/',$dom,'/',$y)}&amp;hour=8&amp;calid={$logon-user}" title="Add">
                                                                    <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$dom}{$channelID}" id="calendarEventAddImage{$dom}{$channelID}" alt="Add a new Task" title="Add a new Task" />
                                                                </a>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </xsl:if>
                                                </td>
                                            </tr>
                                        </table>
                                        <img src="{$SPACER}" border="0" height="5" width="1" alt="" title="" />
                                        <br />
<!-- TO-DO ENTRY DETAIL-->
										<!--UniAcc: Layout Table -->
                                        <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                                            <tr>
                                                <td>
                                                    <img src="{$SPACER}" border="0" height="50" width="1" alt="" title="" />
                                                </td>
                                                <td width="100%" valign="top">
                                                	<!--UniAcc: Layout Table -->
                                                    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                                                        <xsl:choose>
<!--in case of all-calendar -->
                                                            <xsl:when test="$calid ='all-calendars' or contains($calid,'composite')">
                                                                <xsl:for-each select="calendar-system/calendar">
                                                                    <xsl:apply-templates select='entry[contains(duration/@start,concat($m,"/",$dom,"/",$yy)) and not(event)]' />
                                                                </xsl:for-each>
                                                            </xsl:when>
                                                            <xsl:otherwise>
<!--in case of not all-calendar -->
                                                                <xsl:for-each select="calendar-system/calendar">
                                                                    <xsl:if test="@calid=$calid and $calid != 'all-calendars' and not(contains($calid,'composite'))">
                                                                        <xsl:apply-templates select='entry[contains(duration/@start,concat($m,"/",$dom,"/",$yy)) and not(event)]' />
                                                                    </xsl:if>
                                                                </xsl:for-each>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
<!-- END TO-DO ENTRY DETAIL -->
                                    </td>
                                </tr>
                            </xsl:when>
<!-- END TO-DO -->
                        </xsl:choose>
                    </table>
                </xsl:when>
                <xsl:when test="$dom &gt; $detail-monthly/@ldom">
                	<!--UniAcc: Layout Table -->
                    <table width="100%" cellpadding="0" cellspacing="0" class="uportal-channel-text">
                        <tr>
                            <td valign="top">
                                <xsl:value-of select="$dom - $detail-monthly/@ldom" />
                                <br />
                                <img src="{$SPACER}" border="0" height="50" alt="" title="" />
                            </td>
                        </tr>
                    </table>
                </xsl:when>
            </xsl:choose>
        </td>
    </xsl:template>
<!-- /detail day -->
    <xsl:template match="entry">
        <xsl:variable name="is-invitation" select="../@owner=../@calid and attendee[@cuid=../../@owner]/@status='ACCEPTED' and organizer/text() !=../@calid" />
        <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="$window='event'">
                    <xsl:choose>
                        <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                        <xsl:value-of select="../@calname" />
                        :
                        <xsl:value-of select="event/text()" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="event/text()" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="$window='todo'">
                    <xsl:choose>
                        <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                        <xsl:value-of select="../@calname" />
                        :
                        <xsl:value-of select="todo/text()" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="todo/text()" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>Untitled</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <tr>
            <td class="uportal-channel-text" width="100%" valign="top">
                <xsl:choose>
                    <xsl:when test="$window = 'event'">
<!-- Event title-->
                        <xsl:call-template name="priority" />
                        <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                        <xsl:choose>
                            <xsl:when test="$editEvents">
                                <xsl:choose>
                                    <xsl:when test="$is-invitation">
                                        <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit">
                                            <xsl:value-of select="$title" /></a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit">
                                            <xsl:value-of select="$title" /></a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$title" />
                            </xsl:otherwise>
                        </xsl:choose>
<!-- delete-link for Event -->
                        <xsl:if test="$deleteEvents = 'Y'">
                            <xsl:call-template name="access">
                                <xsl:with-param name="type-link">
                                    <xsl:choose>
                                        <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite'))">delete</xsl:when>
                                        <xsl:otherwise>delete-all</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:with-param>
                                <xsl:with-param name="window" select="$window" />
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="$window ='todo'">
<!-- Todo title-->
                        <xsl:call-template name="priority" />
                        <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                        <xsl:choose>
                            <xsl:when test="$editEvents">
                                <a href="{$mgoURL}=Todo&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit Task">
                                    <xsl:value-of select="$title" /></a>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$title" />
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:choose>
<!-- Completion of Todo-->
                            <xsl:when test="todo/completion/@completed">
                                <xsl:call-template name="access">
                                    <xsl:with-param name="type-link">un-complete</xsl:with-param>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="access">
                                    <xsl:with-param name="type-link">complete</xsl:with-param>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
<!-- delete-link for Todo -->
                        <xsl:if test="$deleteEvents = 'Y'">
                            <xsl:call-template name="access">
                                <xsl:with-param name="type-link">delete</xsl:with-param>
                                <xsl:with-param name="window" select="$window" />
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:when>
                </xsl:choose>
            </td>
        </tr>
<!-- </xsl:if>-->
    </xsl:template>
</xsl:stylesheet>

