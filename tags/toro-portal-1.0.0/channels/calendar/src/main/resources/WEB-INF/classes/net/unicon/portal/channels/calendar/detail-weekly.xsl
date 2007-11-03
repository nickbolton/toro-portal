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
    <xsl:include href="utils.xsl" />
	 <xsl:param name="condition">1</xsl:param>
    
    
    <xsl:template name="detail-weekly">
        <xsl:call-template name="weekly-header"/>
        
        <form method="post" action="{$baseActionURL}" name="calendarWeeklyChangeDateForm{$channelID}">
        	<!--UniAcc: Layout Table -->
        	
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
					<xsl:call-template name="mform" />
					<xsl:if test="contains($targetChannel, 'CCalendarUnicon')">
						<input type="hidden" name="calid">
							<xsl:attribute name="value">
								<xsl:value-of select="/calendar-system/view/calendar/@calid" />
							</xsl:attribute>
						</input>
					</xsl:if>
					<th colspan="3" class="th-top" align="left">
						<table cellspacing="0" cellpadding="0" border="0" width="100%">
							<tr>
								<td colspan="2">
									<input type="hidden" name="fixChangeDate" value="" />

									<xsl:variable name="m">
										<xsl:value-of select="substring-before($detail-weekly/@date,'/')"/>
									</xsl:variable>
									<input type="hidden" name="month" value="{$m}"/>

									<xsl:variable name="y">
										<xsl:value-of select="substring-after(substring-after(substring-before($detail-weekly/@date,'_'),'/'),'/')" />
									</xsl:variable>
									<input type="hidden" name="year" value="20{$y}"/>

									<input type="hidden" name="week" value="{$detail-weekly/@week}"/>

									<input type="hidden" name="sid">
										<xsl:attribute name="value">
											<xsl:value-of select="$sid" />
										</xsl:attribute>
									</input>

									<div class="event-title">
										<xsl:value-of select="$detail-weekly/@title"/>
										<img height="1" width="10" src="{$SPACER}" border="0" alt="" title="" />
										<a href="javascript:document.calendarWeeklyChangeDateForm{$channelID}.fixChangeDate.name='do~previousDate';javascript:document.calendarWeeklyChangeDateForm{$channelID}.submit()" title="Previous">
											<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
										</a>
										<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
										<a href="javascript:document.calendarWeeklyChangeDateForm{$channelID}.fixChangeDate.name='do~nextDate';javascript:document.calendarWeeklyChangeDateForm{$channelID}.submit()" title="Next">
											<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
										</a>
									</div>
									<div class="event-subtitle">
										<xsl:value-of select="/calendar-system/calendar[@calid = /calendar-system/view/calendar/@calid]/@calname"/>
									</div>		
								</td>
							</tr>
						</table>
					</th>
				</tr>
                <tr>
<!-- DETAIL WEEKLY EVENT -->
                    <td colspan="3">
                    	<!--UniAcc: Layout Table -->
                        <table width="100%" cellspacing="0" cellpadding="0" border="0">
                            <xsl:variable name="mm" select='substring-before($detail-weekly/@date,"/")' />
                            <xsl:variable name="dd" select='substring-before(substring-after($detail-weekly/@date,"/"),"/")' />
                            <xsl:variable name="yy" select='substring-before(substring-after(substring-after($detail-weekly/@date,"/"),"/"),"_")' />
                            <xsl:variable name="hm" select='substring-after($detail-weekly/@date,"_")' />
                            <tr class="event-background">
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Sunday</xsl:with-param>
                                        <xsl:with-param name="dd" select="0 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr>
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Monday</xsl:with-param>
                                        <xsl:with-param name="dd" select="1 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr class="event-background">
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Tuesday</xsl:with-param>
                                        <xsl:with-param name="dd" select="2 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr>
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Wednesday</xsl:with-param>
                                        <xsl:with-param name="dd" select="3 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr class="event-background">
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Thursday</xsl:with-param>
                                        <xsl:with-param name="dd" select="4 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr>
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Friday</xsl:with-param>
                                        <xsl:with-param name="dd" select="5 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr class="event-background">
                                <td class="weekly-event-row">
                                    <xsl:call-template name="detail-weekly-day">
                                        <xsl:with-param name="DD">Saturday</xsl:with-param>
                                        <xsl:with-param name="dd" select="6 + $dd" />
                                        <xsl:with-param name="mm" select="$mm" />
                                        <xsl:with-param name="yy" select="$yy" />
                                        <xsl:with-param name="hm" select="$hm" />
                                    </xsl:call-template>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
    
<!-- Header of detail-daily -->
     <xsl:template name="weekly-header">

		<form method="post" action="{$baseActionURL}" name="calendarNavigationForm{$channelID}" id="calendarNavigationForm{$channelID}">

			<div class="calendar-navigation-container">

				<xsl:variable name="m">
					<xsl:value-of select="substring-before($detail-weekly/@date,'/')"/>
				</xsl:variable>
				<xsl:variable name="y">
					<xsl:value-of select="substring-after(substring-after(substring-before($detail-weekly/@date,'_'),'/'),'/')" />
				</xsl:variable>
				<xsl:variable name="d">
					<xsl:value-of select="substring-before(substring-after(substring-before($detail-weekly/@date,'_'),'/'),'/')" />
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
				<div class="calendar-tab-selected">
					Week
				</div>
				<div class="calendar-tab">
					<a class="whitetext" href="javascript:document.getElementById('fixChangeView{$channelID}').name='do~update'; document.getElementById('op{$channelID}').value='v~monthly'; document.getElementById('calendarNavigationForm{$channelID}').submit();" title="Monthly View">
						Month
					</a>
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
	    
    
    
    <xsl:template name="detail-weekly-day">
        <xsl:param name="DD" />
        <xsl:param name="dd" />
        <xsl:param name="mm" />
        <xsl:param name="yy" />
        <xsl:param name="hm" />
        <xsl:variable name="right" select="key('access',$calid)" />
        <xsl:variable name="br_color">
            <xsl:choose>
                <xsl:when test="$calid ='all-calendars' or contains($calid,'composite')">
                    <xsl:for-each select='calendar-system/calendar/entry[contains(duration/@start,concat($mm,"/",$dd,"/",$yy))]'>
                        <xsl:choose>
                            <xsl:when test="((event and $window = 'event') or (todo and $window='todo'))">white</xsl:when>
                            <xsl:otherwise>uportal-background-content</xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
            </xsl:choose>
<!--in case of not all-calendar -->
            <xsl:for-each select="calendar-system/calendar[@calid=$calid and $calid!='all-calendars' and not(contains($calid,'composite'))]">
                <xsl:for-each select='entry[contains(duration/@start,concat($mm,"/",$dd,"/",$yy))]'>
                    <xsl:choose>
                        <xsl:when test="((event and $window = 'event') or (todo and $window='todo'))">white</xsl:when>
                        <xsl:otherwise>uportal-background-content</xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$mm != 12 and $dd &gt; $detail-weekly/@ldom">
                <xsl:call-template name="detail-weekly-day">
                    <xsl:with-param name="DD" select="$DD" />
                    <xsl:with-param name="dd" select="$dd - $detail-weekly/@ldom" />
                    <xsl:with-param name="mm" select="$mm + 1" />
                    <xsl:with-param name="yy" select="$yy" />
                    <xsl:with-param name="hm" select="$hm" />
                </xsl:call-template>
            </xsl:when>
<!-- Truong 1/8/02 -->
            <xsl:when test="$mm = 12 and $dd &gt; $detail-weekly/@ldom">
                <xsl:call-template name="detail-weekly-day">
                    <xsl:with-param name="DD" select="$DD" />
                    <xsl:with-param name="dd" select="$dd - $detail-weekly/@ldom" />
                    <xsl:with-param name="mm" select="$mm - 12 + 1" />
                    <xsl:with-param name="yy">
                        <xsl:choose>
                            <xsl:when test="string-length($yy + 1) = 1">
                                <xsl:value-of select="concat('0',$yy + 1)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$yy + 1" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="hm" select="$hm" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$mm &gt; 12">
                <xsl:call-template name="detail-weekly-day">
                    <xsl:with-param name="DD" select="$DD" />
                    <xsl:with-param name="dd" select="$dd" />
                    <xsl:with-param name="mm" select="$mm - 12" />
                    <xsl:with-param name="yy" select="$yy + 1" />
                    <xsl:with-param name="hm" select="$hm" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
            	<!--UniAcc: Layout Table -->
                <table width="100%" cellpadding="0" cellspacing="0" border="0">
                    <xsl:choose>
                        <xsl:when test="contains($br_color,'white')">
                            <xsl:attribute name="class">uportal-text</xsl:attribute>
<!--<xsl:attribute name="bgcolor">#ffffff</xsl:attribute>-->
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="class">uportal-text</xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:variable name="date" select='concat($mm,"/",$dd,"/",$yy)' />
<!-- EVENT OR TODO -->
                    <xsl:choose>
<!-- VIEW EVENT -->
                        <xsl:when test="$window = 'event'">
                            <tr>
            					<td nowrap="nowrap" colspan="2" valign="middle" class="table-light-single">
                                    <xsl:if test="$addEvents = 'Y'">
										<xsl:choose>
											<xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' )or contains($right/@rights,'W')">
												<a href="{$mgoURL}=Event&amp;date={concat($date,'_',$hm)}&amp;hour=8&amp;calid={$calid}" title="Add">
													<img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$DD}{$channelID}" id="calendarEventAddImage{$DD}{$channelID}" alt="Add a new event" title="Add a new event" />
												</a>
											</xsl:when>
											<xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
												<a href="{$mgoURL}=Event&amp;date={concat($date,'_',$hm)}&amp;hour=8&amp;calid={$logon-user}" title="Add">
													<img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$DD}{$channelID}" id="calendarEventAddImage{$DD}{$channelID}" alt="Add a new event" title="Add a new event" />
												</a>
											</xsl:when>
											<xsl:otherwise>
												<img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage" id="calendarEventAddImage" alt="The permission to add events is currently unavailable" title="The permission to add events is currently unavailable" />
											</xsl:otherwise>
										</xsl:choose>
                                    </xsl:if>
                                    <img border="0" src="{$SPACER}" width="5" alt="" title="" />
                                    <a href="{$mdoURL}=update&amp;op=d~{concat($date,'_',$hm)}&amp;calid={calendar-system/view/calendar/@calid}" title="View">
                                        <xsl:value-of select='concat($DD,", ",$mm,"/",$dd)' />
                                    </a>
                                </td>
                            </tr>
                            <xsl:choose>
<!-- in case of all-calendars -->
                                <xsl:when test="$calid ='all-calendars' or contains($calid,'composite')">
                                    <xsl:for-each select="calendar-system/calendar/entry[starts-with(duration/@start,$date) and not(todo)]">
                                        <xsl:sort select='substring-before( substring-after(duration/@start, "_"),":" )' data-type="number" order="ascending" />
                                        <xsl:apply-templates select="." />
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
<!-- in case of not all-calendars -->
                                    <xsl:for-each select="calendar-system/calendar">
                                        <xsl:if test="@calid=$calid and $calid!='all-calendars' and not(contains($calid,'composite'))">
                                            <xsl:apply-templates select="entry[starts-with(duration/@start,$date) and not(todo)]" />
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
<!-- END VIEW EVENT -->
<!-- VIEW TODO -->
                        <xsl:when test="$window = 'todo'">
                            <tr>
								<xsl:if test="position() mod 2 = 1">
									<xsl:attribute name="style">background-color: #efefef;</xsl:attribute>
          						</xsl:if>
          						
                                <td nowrap="nowrap" colspan="2" valign="middle" class="table-light-left">
                                    <a href="{$mdoURL}=update&amp;op=d~{concat($date,'_',$hm)}&amp;calid={calendar-system/view/calendar/@calid}" title="View" onmouseover="swapImage('calendarTodoViewImage{$DD}{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarTodoViewImage{$DD}{$channelID}','channel_view_base.gif')">
                                        <xsl:value-of select='concat($DD,", ",$mm,"/",$dd)' />
                                        <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                                        <img src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" border="0" align="absmiddle" name="calendarTodoViewImage{$DD}{$channelID}" id="calendarTodoViewImage{$DD}{$channelID}" alt="View Task" title="View Task" />
                                    </a>
                                    <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                                    <xsl:if test="$addEvents = 'Y'">
                                        <xsl:choose>
                                            <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
                                                <a href="{$mgoURL}=Todo&amp;date={concat($date,'_',$hm)}&amp;hour=8&amp;calid={$calid}" title="Add Task">
                                                    <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$DD}{$channelID}" id="calendarEventAddImage{$DD}{$channelID}" alt="Add a new Task" title="Add a new Task" />
                                                </a>
                                            </xsl:when>
                                            <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                                                <a href="{$mgoURL}=Todo&amp;date={concat($date,'_',$hm)}&amp;hour=8&amp;calid={$logon-user}" title="Add Task">
                                                    <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage{$DD}{$channelID}" id="calendarEventAddImage{$DD}{$channelID}" alt="Add a new Task" title="Add a new Task" />
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <img src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" border="0" align="absmiddle" name="calendarEventAddImage" id="calendarEventAddImage" alt="The permission to add Tasks is currently unavailable" title="The permission to add Tasks is currently unavailable" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:if>
                                </td>
                            </tr>
                            <xsl:choose>
<!-- in case of all-calendars -->
                                <xsl:when test="$calid ='all-calendars' or contains($calid,'composite')">
                                    <xsl:for-each select="calendar-system/calendar/entry[starts-with(duration/@start,$date) and not(event)]">
                                        <xsl:sort select='substring-before( substring-after(duration/@start, "_"),":" )' data-type="number" order="ascending" />
                                        <xsl:apply-templates select="." />
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
<!-- in case of not all-calendars -->
                                    <xsl:for-each select="calendar-system/calendar">
                                        <xsl:if test="@calid=$calid and $calid!='all-calendars' and not(contains($calid,'composite'))">
                                            <xsl:apply-templates select="entry[starts-with(duration/@start,$date) and not(event)]" />
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
<!-- END VIEW TODO -->
                    </xsl:choose>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
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
        <tr class="uportal-channel-text">
<!--Display duration of Entry -->
            <xsl:choose>
                <xsl:when test="$window = 'event'">
                    <xsl:choose>
                        <xsl:when test='duration/@length = "all-day"'>
                            <td class="table-content-left" nowrap="true">
                            	<img border="0" src="{$SPACER}" width="15" height="1" alt="" title="" />
                            	All Day
                            </td>
                        </xsl:when>
                        <xsl:otherwise>
                            <td class="table-content-left" nowrap="true">
                            	<img border="0" src="{$SPACER}" width="15" height="1" alt="" title="" />
								<xsl:call-template name="t24to12">
									<xsl:with-param name="hour" select='substring-after(duration/@start,"_")' />
								</xsl:call-template>
								&#160;-&#160;
								<xsl:call-template name="t24to12">
									<xsl:with-param name="hour" select='substring-after(duration/@end,"_")' />
								</xsl:call-template>
                            </td>
                        </xsl:otherwise>
                    </xsl:choose>
                    <td class="table-content-right" width="100%">
<!-- Title of Entry -->
                        <xsl:choose>
                            <xsl:when test="event">
                                <xsl:call-template name="priority" />
                                <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                                <xsl:choose>
                                    <xsl:when test="$editEvents">
                                        <xsl:choose>
                                            <xsl:when test="$is-invitation">
                                                <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit">
                                                    <xsl:value-of select="$title" />
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit">
                                                    <xsl:value-of select="$title" />
                                                </a>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$title" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                        </xsl:choose>
<!--delete link of entry-->
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
                    </td>
                </xsl:when>
                <xsl:when test="$window = 'todo'">
<!--Display duration of Entry -->
                    <xsl:choose>
                        <xsl:when test='duration/@length = "all-day"'>
                            <td class="table-content-left" nowrap="true">
                            	<img border="0" src="{$SPACER}" width="15" height="1" alt="" title="" />
                            	All Day
                            </td>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:if test="todo">
                                <td class="table-content-left" nowrap="true">
                                	<img border="0" src="{$SPACER}" width="15" height="1" alt="" title="" />
                                    <xsl:call-template name="t24to12">
                                        <xsl:with-param name="hour" select='substring-after(duration/@start,"_")' />
                                    </xsl:call-template>
                                </td>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                    <td class="table-content-right" width="100%">
<!-- Title of Entry -->
                        <xsl:if test="todo">
                            <xsl:call-template name="priority" />
                            <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                            <xsl:choose>
                                <xsl:when test="$editEvents">
                                    <a href="{$mgoURL}=Todo&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit Task">
                                        <xsl:value-of select="$title" />
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$title" />
                                </xsl:otherwise>
                            </xsl:choose>
<!-- Todo completion -->
                            <xsl:choose>
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
<!--delete link of entry-->
                            <xsl:if test="$deleteEvents = 'Y'">
                                <xsl:call-template name="access">
                                    <xsl:with-param name="type-link">delete</xsl:with-param>
                                    <xsl:with-param name="window" select="$window" />
                                </xsl:call-template>
                            </xsl:if>
                        </xsl:if>
                    </td>
                </xsl:when>
            </xsl:choose>
        </tr>
    </xsl:template>
    
</xsl:stylesheet>

