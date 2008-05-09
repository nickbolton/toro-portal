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

     <xsl:variable name="valid-events" select="/calendar-system/calendar/entry[(not(todo) and ($window='event')) or (not(event) and ($window='todo')) ]" />
	<xsl:variable name="daily-mini-monthly" select="/calendar-system/mini-monthly" />
	<xsl:param name="condition">1</xsl:param>
	 
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->

<!-- detail-daily -->
<xsl:template name="detail-daily">
    <!--<textarea><xsl:copy-of select="/"></xsl:copy-of></textarea>-->
    
     	<div class="daily-container">
          <form method="post" action="{$baseActionURL}" name="calendarDailyChangeDateForm{$channelID}" id="calendarDailyChangeDateForm{$channelID}">
				
				<xsl:variable name="day">
					<xsl:value-of select="substring-before(substring-after($detail-daily/@title,'/'),'/')"/>
				</xsl:variable>
				
				<xsl:variable name="month-num">
					<xsl:value-of select="substring-before($detail-daily/@date,'/')"/>
				</xsl:variable>
				
				<xsl:variable name="month">
					<xsl:value-of select="substring-before($daily-mini-monthly/@title,' ')" />
				</xsl:variable>
				
				<xsl:variable name="year">
					<xsl:value-of select="substring-after($daily-mini-monthly/@title,' ')" />
				</xsl:variable>
				
				<xsl:variable name="dayofweek">
					<xsl:value-of select="substring-before($detail-daily/@title,',')"/>
				</xsl:variable>
				<input type="hidden" name="fixChangeDate" value="" />
				<input type="hidden" name="month" value="{$month-num}" />
				<input type="hidden" name="year" value="{$year}" />
				<input type="hidden" name="day" value="{$day}" />
				<input type="hidden" name="sid" value="{$sid}" />
				<input type="hidden" name="calid">
					  <xsl:attribute name="value">
						   <xsl:value-of select="/calendar-system/view/calendar/@calid" />
					  </xsl:attribute>
                </input>
                
				<div class="event-title">
					<xsl:value-of select="$dayofweek"/>,&#160;<xsl:value-of select="$month"/>&#160;<xsl:value-of select="$day"/>,&#160;<xsl:value-of select="$year"/>					
					
					<xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
						<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
						<a href="javascript:document.calendarDailyChangeDateForm{$channelID}.fixChangeDate.name='do~previousDate';javascript:document.calendarDailyChangeDateForm{$channelID}.submit()" title="Previous">
							<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
						</a>
						<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
						<a href="javascript:document.calendarDailyChangeDateForm{$channelID}.fixChangeDate.name='do~nextDate';javascript:document.calendarDailyChangeDateForm{$channelID}.submit()" title="Next">
							<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
						</a>			
					</xsl:if>
                </div>

				<div class="event-subtitle">
                    <xsl:value-of select="/calendar-system/calendar[@calid = /calendar-system/view/calendar/@calid]/@calname"/>
				</div>
				
			    <div class="daily-events-container">
			    	<table width="100%" cellpadding="2" cellspacing="1">
						<xsl:apply-templates select="$detail-daily/all-day" />
						<xsl:apply-templates select="$detail-daily/hour" />
					</table>
				</div>
          </form>
        </div>
     </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- Header of detail-daily -->
     <xsl:template name="daily-header">

		<form method="post" action="{$baseActionURL}" name="calendarNavigationForm{$channelID}" id="calendarNavigationForm{$channelID}">

			<div class="calendar-navigation-container">

				<xsl:variable name="m">
					<xsl:value-of select="substring-before($detail-daily/@date,'/')"/>
				</xsl:variable>
				<xsl:variable name="y">
					<xsl:value-of select="substring-after(substring-after(substring-before($detail-daily/@date,'_'),'/'),'/')" />
				</xsl:variable>
				<xsl:variable name="d">
					<xsl:value-of select="substring-before(substring-after(substring-before($detail-daily/@date,'_'),'/'),'/')" />
				</xsl:variable>

				<input type="hidden" name="year" value="20{$y}"/>
				<input type="hidden" name="month" value="{$m}"/>
				<input type="hidden" name="date" value="{$d}"/>

				<input type="hidden" name="fixChangeView" id="fixChangeView{$channelID}" value="" />
				<input type="hidden" name="op" id="op{$channelID}"/>

				<div class="calendar-tab-selected">
					Day
				</div>
				<div class="calendar-tab">
					<a class="whitetext" href="javascript:document.getElementById('fixChangeView{$channelID}').name='do~update'; document.getElementById('op{$channelID}').value='v~weekly'; document.getElementById('calendarNavigationForm{$channelID}').submit();" title="Weekly View">
						Week
					</a>
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
	
	<xsl:template name="tiny-monthly">
		<form method="post" action="{$baseActionURL}" id="changeMonthViewForm" name="changeMonthViewForm">
			<!--UniAcc: Layout Table -->
			<table cellspacing="0" cellpadding="0" width="100%" border="0">
				<tr>
					<td align="center">
						
						<xsl:variable name="m">
							<xsl:value-of select="substring-before($daily-mini-monthly/@date,'/')"/>
						</xsl:variable>
						<input type="hidden" name="month" value="{$m}"/>
						
						<!--
						<xsl:variable name="d">
							<xsl:value-of select="substring-before(substring-after(substring-before($daily-mini-monthly/@date,'_'),'/'),'/')" />
						</xsl:variable>
						<input type="text" name="day" value="{$d}"/>
						-->
						
						<xsl:variable name="y">
							<xsl:value-of select="substring-after(substring-after(substring-before($daily-mini-monthly/@date,'_'),'/'),'/')" />
						</xsl:variable>
						<input type="hidden" name="year" value="20{$y}"/>
						<input type="hidden" name="sid">
							<xsl:attribute name="value">
								<xsl:value-of select="$sid" />
							</xsl:attribute>
						</input>
						<input type="hidden" name="fixChangeMonthView" id="fixChangeMonthView" value="" />							

						<div>
							<table cellpadding="0" cellspacing="0">
								<tr>
								 	<td nowrap="nowrap" class="calendar-title">
										<xsl:if test="contains($targetChannel, 'CCalendarUnicon')">
											<a href="javascript:document.getElementById('fixChangeMonthView').name='do~previousDate'; document.getElementById('changeMonthViewForm').submit();" title="Previous">
												<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
											</a>						
											<img height="1" width="4" src="{$SPACER}" border="0" alt="" title="" />
										</xsl:if>

										<xsl:value-of select="$MONTH" /> 20<xsl:value-of select="$y"/>	

										<xsl:if test="contains($targetChannel, 'CCalendarUnicon')">
											<img height="1" width="4" src="{$SPACER}" border="0" alt="" title="" />
											<a href="javascript:document.getElementById('fixChangeMonthView').name='do~nextDate'; document.getElementById('changeMonthViewForm').submit();" title="Next">
												<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
											</a>		
										</xsl:if>
									</td>
								</tr>
							</table>
						</div>
						
						<xsl:call-template name="mini-monthly-table">
							<xsl:with-param name="mini-monthly-class">mini-monthly-20</xsl:with-param>
						</xsl:call-template>

					</td>
				</tr>
			</table>
		</form>	
	</xsl:template>
	
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- All Day session of detail-daily -->
     <xsl:template match="all-day">
          <xsl:variable name="right" select="key('access',$calid)" />
          <tr>
<!-- All Day title -->
               <td align="right" width="5%" nowrap="nowrap" class="table-light-left">&#160; 
               <xsl:if test="$addEvents = 'Y'">
                    <xsl:choose>
                         <xsl:when test="$window='event'">
<!-- Plus sign for creating all-day event -->
	                         <xsl:choose>
	                              <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
	                                   <a href="{$mgoURL}=Event&amp;date={$detail-daily/@date}&amp;isAllDay=1&amp;calid={$calid}" title="Add a new event" >
									   		All Day<img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" align="absmiddle" title="Add Event" /></a>
	                              </xsl:when>
	                              <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
	                                   <a href="{$mgoURL}=Event&amp;date={$detail-daily/@date}&amp;isAllDay=1&amp;calid={$logon-user}" title="Add a new event" >
									   		All Day<img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" align="absmiddle" title="Add Event" /></a>
	                              </xsl:when>
	                              <xsl:otherwise>
	                                   All Day
	                                   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" align="absmiddle" title="Add Event" />	                                   	                                   
	                              </xsl:otherwise>
	                         </xsl:choose>
                         &#160;
                         </xsl:when>
                         <xsl:when test="$window='todo'">
<!-- Plus sign for creating all-day Todo -->
	                         <xsl:choose>
	                              <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
	                                   <a href="{$mgoURL}=Todos&amp;date={$detail-daily/@date}&amp;isAllDay=1&amp;calid={$calid}" title="Add a new Task">
	                                        All Day<img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" align="absmiddle" title="Add Task" /></a>
	                              </xsl:when>
	                              <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
	                                   <a href="{$mgoURL}=Todos&amp;date={$detail-daily/@date}&amp;isAllDay=1&amp;calid={$logon-user}" title="Add a new Task">
	                                        All Day<img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" align="absmiddle" title="Add Task" /></a>
	                              </xsl:when>
	                              <xsl:otherwise>
	                                   All Day<img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" align="absmiddle" title="Add Task" />	                                   
	                              </xsl:otherwise>
	                         </xsl:choose>
                         &#160;
                         </xsl:when>
                    </xsl:choose>
               </xsl:if>
               </td>
<!--Entry title -->
               <td class="table-content-right" colspan="{@col}">
					<xsl:if test="entry">
						<xsl:attribute name="style">
							<xsl:text>background-color:#E6EFF8;border: 1px solid #6692BC;</xsl:text>
						</xsl:attribute>
					</xsl:if>
                    <xsl:for-each select="entry">
                        <xsl:variable name="ceid" select="@ceid"/>
                        <xsl:apply-templates select="$valid-events[@ceid=$ceid]" />
                    </xsl:for-each>
               </td>
          </tr>
     </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- Other entries (non-allday) of detail-daily -->
     <xsl:template match="hour">
          <xsl:variable name="right" select="key('access',$calid)" />
          <tr>
          	<xsl:if test="position() mod 2 = 1">
          		<xsl:attribute name="style">background-color: #efefef;</xsl:attribute>
          	</xsl:if>          		
          		
<!--Hour of day -->
               <td width="5%" nowrap="nowrap" align="right" class="table-light-left" scope="row">&#160;

               <xsl:if test="$addEvents = 'Y'">
                    <xsl:choose>
                         <xsl:when test="$window='event'">
<!-- Plus sign for creating new event -->
                         <xsl:choose>
<!-- in case of not all-calendars -->
                              <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' ) or contains($right/@rights,'W')">
                                   <a href="{$mgoURL}=Event&amp;date={$detail-daily/@date}&amp;hour={@hid}&amp;calid={$calid}" title="Add">
									   <xsl:call-template name="x24to12">
											<xsl:with-param name="hour" select="@hid" />
									   </xsl:call-template>  
									   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" title="Add Event" />									   
                                   </a>
                              </xsl:when>
<!-- in case of all-calendars -->
                              <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                                   <a href="{$mgoURL}=Event&amp;date={$detail-daily/@date}&amp;hour={@hid}&amp;calid={$logon-user}" title="Add">
									   <xsl:call-template name="x24to12">
											<xsl:with-param name="hour" select="@hid" />
									   </xsl:call-template>                                                                                
									   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" title="Add Event" />									   									   
                                   </a>
                              </xsl:when>
                              <xsl:otherwise>
									   <xsl:call-template name="x24to12">
											<xsl:with-param name="hour" select="@hid" />
									   </xsl:call-template>                                                                           
									   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add_gray.gif" title="Add Event" />									   									   
                              </xsl:otherwise>
                         </xsl:choose>
                         &#160;</xsl:when>
                         <xsl:when test="$window='todo'">
<!-- Plus sign for creating new todo -->
                         <xsl:choose>
<!-- in case of not all-calendars -->
                              <xsl:when test="$calid!='all-calendars' and not(contains($calid,'composite')) and ($calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' or contains($right/@rights,'W'))">
                                   <a href="{$mgoURL}=Todo&amp;date={$detail-daily/@date}&amp;hour={@hid}&amp;calid={$calid}" title="Add a new Task">
									   <xsl:call-template name="x24to12">
											<xsl:with-param name="hour" select="@hid" />
									   </xsl:call-template> 
									   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" title="Add Task" />									   
                                   </a>
                              </xsl:when>
<!-- in case of all-calendars -->
                              <xsl:when test="$calid='all-calendars' or contains($calid,'composite')">
                                   <a href="{$mgoURL}=Todo&amp;date={$detail-daily/@date}&amp;hour={@hid}&amp;calid={$logon-user}" title="Add a new Task">
									   <xsl:call-template name="x24to12">
											<xsl:with-param name="hour" select="@hid" />
									   </xsl:call-template>                                                                                
									   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" title="Add Task" />									   									   
                                   </a>
                              </xsl:when>
                              <xsl:otherwise>
									   <xsl:call-template name="x24to12">
											<xsl:with-param name="hour" select="@hid" />
									   </xsl:call-template>                                        
									   <img border="0" class="add-event-img" src="{$CONTROLS_IMAGE_PATH}/calendar_add.gif" title="Add Task" />									   
                              </xsl:otherwise>
                         </xsl:choose>
                         &#160;</xsl:when>
                    </xsl:choose>
               </xsl:if>
               </td>
<!--Slot of Entries -->
               <xsl:apply-templates select="slot" />
          </tr>
     </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- Slot of detail-daily -->
     <xsl:template match="slot">
<!-- Attribute of this slot -->
          <td>
              <xsl:variable name="ent" select="entry"/>
               <xsl:choose>
                   <xsl:when test="$ent and $valid-events[@ceid = $ent/@ceid]">
                         <xsl:attribute name="class">table-content-right</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                         <xsl:attribute name="class">table-light-right</xsl:attribute>
                    </xsl:otherwise>
               </xsl:choose>
               <xsl:attribute name="colspan">
                    <xsl:value-of select="@col" />
               </xsl:attribute>
               <xsl:attribute name="rowspan">
                    <xsl:value-of select="@row" />
               </xsl:attribute>
			   <xsl:if test="entry">
					<xsl:attribute name="style">
						<xsl:text>background-color:#E6EFF8;border: 1px solid #6692BC;</xsl:text>
					</xsl:attribute>
			   </xsl:if>
               <xsl:if test="not(entry)">&#160;</xsl:if>
<!-- Entries of this slot -->
               <font class="uportal-channel-text">
                    <xsl:for-each select="entry">
                        <xsl:variable name="ceid" select="@ceid"/>
                        <xsl:apply-templates select="$valid-events[@ceid=$ceid]" />
                    </xsl:for-each>
               </font>
          </td>
     </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- Entry of calendar (/calendar-system/calendar/entry) -->
     <xsl:template match="entry">
          <xsl:if test="$calid='all-calendars' or contains($calid,'composite') or (../@calid=$calid and $calid!='all-calendars' and not(contains($calid,'composite')))">
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
<!-- Event -->
<!-- Length of entry, for non-allday only -->
               <xsl:if test="duration/@length!='all-day'">&#160; 
               <xsl:call-template name="t24to12">
                    <xsl:with-param name="hour" select='substring-after(duration/@start,"_")' />
               </xsl:call-template>
               <xsl:if test="$window='event'">&#160;-&#160;
               <xsl:call-template name="t24to12">
                    <xsl:with-param name="hour" select='substring-after(duration/@end,"_")' />
               </xsl:call-template>
               </xsl:if>
               </xsl:if>
               <xsl:choose>
                    <xsl:when test="$window='event'">
<!-- Priority -->
                         <img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
                         <xsl:call-template name="priority" />
                         <img border="0" src="{$SPACER}" width="3" alt="" title="" />
<!-- Title -->
                         <xsl:choose>
                              <xsl:when test="$editEvents='Y'">
                                   <xsl:choose>
                                        <xsl:when test="$is-invitation">
                                             <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                             <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit this event">
                                                  <xsl:value-of select="$title" />
                                             </a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                             <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                             <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit this event">
                                                  <xsl:value-of select="$title" />
                                             </a>
                                        </xsl:otherwise>
                                   </xsl:choose>
                              </xsl:when>
                              <xsl:otherwise>
                                   <xsl:choose>
                                        <xsl:when test="$is-invitation">
<!--<strong>-->
                                             <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                             <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="View this event">
                                                  <xsl:value-of select="$title" />
                                             </a>
<!--</strong>-->
                                        </xsl:when>
                                        <xsl:otherwise>
                                             <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                             <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={../@calid}" title="View this event">
                                                  <xsl:value-of select="$title" />
                                             </a>
                                        </xsl:otherwise>
                                   </xsl:choose>
                              </xsl:otherwise>
                         </xsl:choose>
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
                    <xsl:when test="$window='todo'">
<!-- Priority -->
                         <img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
                         <xsl:call-template name="priority" />
                         <img border="0" src="{$SPACER}" width="3" alt="" title="" />
<!-- Title -->
<!--<em>-->
                         <a href="{$mgoURL}=Todo&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit this Task">
                              <xsl:value-of select="$title" />
                         </a>
                         <xsl:if test="$deleteEvents = 'Y'">
                              <xsl:call-template name="access">
                                   <xsl:with-param name="type-link">delete</xsl:with-param>
                                   <xsl:with-param name="window" select="$window" />
                              </xsl:call-template>
                         </xsl:if>
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
                    </xsl:when>
               </xsl:choose>
               <br />
          </xsl:if>
     </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- Utils -->
     <xsl:template name="x24to12">
          <xsl:param name="hour" />
          <xsl:choose>
               <xsl:when test="$hour &gt; 12">
               <xsl:value-of select="$hour - 12" />
               &#160;pm</xsl:when>
               <xsl:when test="$hour = 12">12 pm&#160;</xsl:when>
               <xsl:when test="$hour = 0">12 am&#160;</xsl:when>
               <xsl:otherwise>
               <xsl:value-of select="$hour" />
               &#160;am</xsl:otherwise>
          </xsl:choose>
     </xsl:template>
</xsl:stylesheet>

