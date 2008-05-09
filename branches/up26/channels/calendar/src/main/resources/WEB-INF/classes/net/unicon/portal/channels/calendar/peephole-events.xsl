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
    <xsl:include href="mini-event.xsl" />
    <xsl:variable name="cur_user" select="calendar-system/logon/@user" />
<!-- will need to add privileges-->
    <xsl:key name="calhidden" match="/calendar-system/preference/calendar-hidden" use="@calid" />
<!-- /////////////////////////////////////////////// -->
<!-- peephole Events -->
    <xsl:template name="Events">
        <xsl:call-template name="mini-monthly-view" />
        
        <xsl:call-template name="detail-event-date" />
        
        <xsl:call-template name="invitations" />
    </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
    <xsl:template name="mini-monthly-view">
    	<!--UniAcc: Layout Table -->
    	<div class="calendar-container">
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr align="left">
					<td align="center" valign="middle" nowrap="nowrap" class="calendar-text-sm">
						<form method="post" action="{$baseActionURL}">
							<span align="left">
								<b><label for="CPE-CalenderS1">View:&#160;</label></b>
								<br/>
								<select name="calid" size="1" class="calendar-text-sm" id="CPE-CalenderS1" onchange="submit();">
									<option value="all-calendars">All calendars</option>
									<xsl:for-each select="//calendar-system/preference/calendar-group">
										<option value="composite_{@id}">
											<xsl:variable name="gid">
												<xsl:value-of select="concat('composite_',@id)" />
											</xsl:variable>
											<xsl:if test="//calendar-system/view/calendar/@calid = $gid">
												<xsl:attribute name="selected" />
											</xsl:if>
											<xsl:value-of select="concat(@name,' ','composite view')" />
										</option>
									</xsl:for-each>
									<xsl:for-each select="calendar-system/calendar[@owner=$cur_user]">
										<xsl:sort select='@calname' />
										<xsl:choose>
											<xsl:when test="@calid=//calendar-system/view/calendar/@calid">
												<option selected="selected">
													<xsl:attribute name="value">
														<xsl:value-of select="@calid" />
													</xsl:attribute>
													<xsl:value-of select="@calname" />
												</option>
											</xsl:when>
											<xsl:otherwise>
												<option>
													<xsl:attribute name="value">
														<xsl:value-of select="@calid" />
													</xsl:attribute>
													<xsl:value-of select="@calname" />
												</option>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:for-each>
									<xsl:for-each select="calendar-system/calendar[@owner!=$cur_user and not(key('calhidden', @calid))]">
										<xsl:sort select="@owner" />
										<xsl:choose>
											<xsl:when test="@calid=//calendar-system/view/calendar/@calid">
												<option selected="selected">
													<xsl:attribute name="value">
														<xsl:value-of select="@calid" />
													</xsl:attribute>
													<xsl:value-of select="@calname" />
												</option>
											</xsl:when>
											<xsl:otherwise>
												<option>
													<xsl:attribute name="value">
														<xsl:value-of select="@calid" />
													</xsl:attribute>
													<xsl:value-of select="@calname" />
												</option>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:for-each>
								</select>
							</span>
							<input type="hidden" name="sid">
								<xsl:attribute name="value">
									<xsl:value-of select="$sid" />
								</xsl:attribute>
							</input>
							<input type="hidden" style="border-style: none; border-width: 0" name="do~update" id="calendarImageEvent" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" align="absmiddle" title="Change Calendar" onmouseover="swapImage('calendarImageEvent','channel_view_active.gif')" onmouseout="swapImage('calendarImageEvent','channel_view_base.gif')" />
						</form>
					</td>
				</tr>
				<tr>
					<xsl:call-template name="mform" />
					<td valign="middle" nowrap="nowrap">
						<!--
						<form method="post" action="{$baseActionURL}" name="calendarPeepholeEventsChangeDateForm{$channelID}">
						<xsl:choose>
							<xsl:when test="$calid">
								<a href="{$mdoURL}=update&amp;op=d~{$mini-daily/@date}&amp;calid={$calid}&amp;window=event" onmouseover="swapImage('calendarEventsViewImage','channel_view_active.gif')" onmouseout="swapImage('calendarEventsViewImage','channel_view_base.gif')" title="Display">
									Events
									<img border="0" src="{$SPACER}" width="3" alt="" title="" />
									<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="calendarEventsViewImage" id="calendarEventsViewImage" align="absmiddle" alt="of this day" title="of this day" />
								</a>
							</xsl:when>
							<xsl:otherwise>
								<a href="{$mdoURL}=update&amp;op=d~{$mini-daily/@date}&amp;calid={$logon-user}&amp;window=event" onmouseover="swapImage('calendarEventsViewImage{generate-id()}','channel_view_active.gif')" onmouseout="swapImage('calendarEventsViewImage{generate-id()}','channel_view_base.gif')" title="Display">Events
								<img border="0" src="{$SPACER}" width="3" alt="" title="" />
								<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="calendarEventsViewImage{generate-id()}" id="calendarEventsViewImage{generate-id()}" align="absmiddle" alt="of this day" title="of this day" />
								</a>
							</xsl:otherwise>
						</xsl:choose>
						| <label for="CPE-DayS1">Day:</label>
						<xsl:call-template name="date" />
						| 
						<a href="{$mgoURL}=Invitations" onmouseover="swapImage('calendarInvitesViewImage{generate-id()}','channel_view_active.gif')" onmouseout="swapImage('calendarInvitesViewImage{generate-id()}','channel_view_base.gif')" title="Display">Invitations
							<img border="0" src="{$SPACER}" width="3" alt="" title="" />
							<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" name="calendarInvitesViewImage{generate-id()}" id="calendarInvitesViewImage{generate-id()}" align="absmiddle" alt="" title="" />
						</a>
						</form>
						-->
						<xsl:call-template name="mini-monthly"/>
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>
<!-- date combobox-->
    <xsl:template name="date">
        <select name="month" class="text" id="CPE-DayS1">
            <option value="1">
            <xsl:if test='starts-with($mini-daily/@date,"1/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Jan</option>
            <option value="2">
            <xsl:if test='starts-with($mini-daily/@date,"2/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Feb</option>
            <option value="3">
            <xsl:if test='starts-with($mini-daily/@date,"3/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Mar</option>
            <option value="4">
            <xsl:if test='starts-with($mini-daily/@date,"4/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Apr</option>
            <option value="5">
            <xsl:if test='starts-with($mini-daily/@date,"5/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            May</option>
            <option value="6">
            <xsl:if test='starts-with($mini-daily/@date,"6/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Jun</option>
            <option value="7">
            <xsl:if test='starts-with($mini-daily/@date,"7/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Jul</option>
            <option value="8">
            <xsl:if test='starts-with($mini-daily/@date,"8/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Aug</option>
            <option value="9">
            <xsl:if test='starts-with($mini-daily/@date,"9/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Sep</option>
            <option value="10">
            <xsl:if test='starts-with($mini-daily/@date,"10/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Oct</option>
            <option value="11">
            <xsl:if test='starts-with($mini-daily/@date,"11/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Nov</option>
            <option value="12">
            <xsl:if test='starts-with($mini-daily/@date,"12/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            Dec</option>
        </select>
        <select name="day" class="text">
            <option value="1">
            <xsl:if test='contains($mini-daily/@date,"/1/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            1</option>
            <option value="2">
            <xsl:if test='contains($mini-daily/@date,"/2/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            2</option>
            <option value="3">
            <xsl:if test='contains($mini-daily/@date,"/3/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            3</option>
            <option value="4">
            <xsl:if test='contains($mini-daily/@date,"/4/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            4</option>
            <option value="5">
            <xsl:if test='contains($mini-daily/@date,"/5/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            5</option>
            <option value="6">
            <xsl:if test='contains($mini-daily/@date,"/6/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            6</option>
            <option value="7">
            <xsl:if test='contains($mini-daily/@date,"/7/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            7</option>
            <option value="8">
            <xsl:if test='contains($mini-daily/@date,"/8/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            8</option>
            <option value="9">
            <xsl:if test='contains($mini-daily/@date,"/9/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            9</option>
            <option value="10">
            <xsl:if test='contains($mini-daily/@date,"/10/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            10</option>
            <option value="11">
            <xsl:if test='contains($mini-daily/@date,"/11/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            11</option>
            <option value="12">
            <xsl:if test='contains($mini-daily/@date,"/12/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            12</option>
            <option value="13">
            <xsl:if test='contains($mini-daily/@date,"/13/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            13</option>
            <option value="14">
            <xsl:if test='contains($mini-daily/@date,"/14/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            14</option>
            <option value="15">
            <xsl:if test='contains($mini-daily/@date,"/15/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            15</option>
            <option value="16">
            <xsl:if test='contains($mini-daily/@date,"/16/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            16</option>
            <option value="17">
            <xsl:if test='contains($mini-daily/@date,"/17/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            17</option>
            <option value="18">
            <xsl:if test='contains($mini-daily/@date,"/18/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            18</option>
            <option value="19">
            <xsl:if test='contains($mini-daily/@date,"/19/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            19</option>
            <option value="20">
            <xsl:if test='contains($mini-daily/@date,"/20/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            20</option>
            <option value="21">
            <xsl:if test='contains($mini-daily/@date,"/21/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            21</option>
            <option value="22">
            <xsl:if test='contains($mini-daily/@date,"/22/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            22</option>
            <option value="23">
            <xsl:if test='contains($mini-daily/@date,"/23/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            23</option>
            <option value="24">
            <xsl:if test='contains($mini-daily/@date,"/24/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            24</option>
            <option value="25">
            <xsl:if test='contains($mini-daily/@date,"/25/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            25</option>
            <option value="26">
            <xsl:if test='contains($mini-daily/@date,"/26/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            26</option>
            <option value="27">
            <xsl:if test='contains($mini-daily/@date,"/27/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            27</option>
            <option value="28">
            <xsl:if test='contains($mini-daily/@date,"/28/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            28</option>
            <option value="29">
            <xsl:if test='contains($mini-daily/@date,"/29/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            29</option>
            <option value="30">
            <xsl:if test='contains($mini-daily/@date,"/30/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            30</option>
            <option value="31">
            <xsl:if test='contains($mini-daily/@date,"/31/")'>
                <xsl:attribute name="selected">true</xsl:attribute>
            </xsl:if>
            31</option>
        </select>
        <select name="year" class="text">
			<xsl:call-template name="year-options">
				<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($mini-daily/@date,'_') ,'/') ,'/')"/>
			</xsl:call-template>
        </select>
        <input type="hidden" name="sid">
            <xsl:attribute name="value">
                <xsl:value-of select="$sid" />
            </xsl:attribute>
        </input>
        <input type="hidden" name="fixChangeDate" value="" />
        <a href="javascript:document.calendarPeepholeEventsChangeDateForm{$channelID}.fixChangeDate.name='do~changeDate';javascript:document.calendarPeepholeEventsChangeDateForm{$channelID}.submit()" title="Change" onmouseover="swapImage('calendarEventChangeDateImage{generate-id()}','channel_view_active.gif')" onmouseout="swapImage('calendarEventChangeDateImage{generate-id()}','channel_view_base.gif')">
            <img border="0" src="{$SPACER}" width="3" alt="" title="" />
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" align="absmiddle" name="calendarEventChangeDateImage{generate-id()}" id="calendarEventChangeDateImage{generate-id()}" alt="date" title="date" />
        </a>
    </xsl:template>
    
    
    <xsl:template name="detail-event-date">
<!-- All calendars of logon user -->
		<!--UniAcc: Data Table -->
		
        <div class="mini-events-title">
          	Calendar Events
        </div>
        <div class="mini-events-today">
        	<xsl:value-of select="$mini-daily/@title"/>
        </div>
        <div class="calendar-text-sm" style="text-align: center">
        	<strong>Today's Events</strong>
        </div>
		<xsl:choose>
			<xsl:when test="count(/calendar-system/view/event[@length='all-day']) = 0 and count(/calendar-system/view/event[not(@length)]) = 0">
				<div class="event-box calendar-text">
					There are no events for this day.
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="event-box">
					<xsl:for-each select="/calendar-system/view/event[@length='all-day']">
						<xsl:sort select="@title" />
			<!-- entries All-day of calendar -->
						<table width="100%" cellspacing="0" cellpadding="0" border="0">
							<xsl:call-template name="entry-event-allday">
								<xsl:with-param name="calid" select="@calid" />
							</xsl:call-template>
						</table>
					</xsl:for-each>
					<xsl:for-each select="/calendar-system/view/event[not(@length)]">
			<!-- entries hour of calendar -->
						<table width="100%" cellspacing="0" cellpadding="0" border="0">
							<xsl:call-template name="entry-event-daily">
								<xsl:with-param name="calid" select="@calid" />
							</xsl:call-template>
						</table>
					</xsl:for-each>  
				</div>  
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>
    
    
<!-- Daily entries of calendar, context node is "/calendar-system/calendar" -->
    <xsl:template name="entry-event-allday">
        <xsl:param name="calid" />
        <tr>
<!-- All day events -->
            <td nowrap="true" align="center" valign="top" style="padding:2px;">
	            <xsl:call-template name="priority">
	                <xsl:with-param name="cur_node">event</xsl:with-param>
	            </xsl:call-template>				
            </td>
<!-- Priority, title -->
            <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="@title">
                    <xsl:value-of select="@title" />
                </xsl:when>
                <xsl:otherwise>Untitled</xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <td width="100%" valign="top" class="calendar-text" style="padding-bottom: 5px;">
				<a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={$calid}" title="Edit"><xsl:value-of select="$title" /></a>
	            <xsl:call-template name="delete">
	                <xsl:with-param name="calid" select='$calid' />
	            </xsl:call-template>
	            <div>
	            	All Day
	            </div>
            </td>
        </tr>       		
    </xsl:template>
    
    
    <xsl:template name="entry-event-daily">
        <xsl:param name="calid" />
        <tr>
<!-- Start time -->
            <td nowrap="true" align="center" valign="top" style="padding:2px;">
                <xsl:call-template name="priority">
                    <xsl:with-param name="cur_node">event</xsl:with-param>
                </xsl:call-template>
            </td>
<!-- Priority, title -->
            <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="@title">
                    <xsl:value-of select="@title" />
                </xsl:when>
                <xsl:otherwise>Untitled</xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <td width="100%" valign="top" class="calendar-text" style="padding-bottom: 5px;">
                <a href="{$mgoURL}=Event&amp;ceid={@ceid}&amp;calid={$calid}" title="Edit"><xsl:value-of select="$title" /></a>
                <xsl:call-template name="delete">
                    <xsl:with-param name="calid" select='$calid' />
                </xsl:call-template>
                <div>
					<xsl:call-template name="t24to12">
						<xsl:with-param name="hour" select='substring-after(@start,"_")' />
					</xsl:call-template>
					&#160;-&#160;
					<xsl:call-template name="t24to12">
						<xsl:with-param name="hour" select='substring-after(@end,"_")' />
					</xsl:call-template> 
				</div>
            </td>
        </tr>
    </xsl:template>
    
    
    <xsl:template name="invitations">
    	<div class="event-box calendar-text">
			<xsl:choose>
				<xsl:when test="calendar-system/invitations/@count='0'">You have no invitations for the next&#160; 
					<xsl:value-of select="calendar-system/invitations/@days" />&#160;days.
				</xsl:when>
				<xsl:otherwise>
					<a href="{$mgoURL}=Invitations" title="">You have&#160; 
					<xsl:value-of select="calendar-system/invitations/@count" />
					&#160;invitations for the next&#160;
					<xsl:value-of select="calendar-system/invitations/@days" />
					&#160;days.</a>
				</xsl:otherwise>
			</xsl:choose>
		</div>
    </xsl:template>
    
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
    <xsl:template name="delete">
        <xsl:param name="calid" />
        <xsl:variable name="everyone-share-write">
            <xsl:for-each select="/calendar-system/calendar[@calid=$calid]">
                <xsl:for-each select="ace">
                    <xsl:choose>
                        <xsl:when test="@cuid='@'">
                            <xsl:value-of select="@write" />
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="user-share-write">
            <xsl:for-each select="/calendar-system/calendar[@calid=$calid]">
                <xsl:for-each select="ace">
                    <xsl:choose>
                        <xsl:when test="@cuid=$logon-user">
                            <xsl:value-of select="@write" />
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="owner">
            <xsl:for-each select="/calendar-system/calendar[@calid=$calid]">
                <xsl:choose>
                    <xsl:when test="@owner=$logon-user">true</xsl:when>
                    <xsl:otherwise>false</xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="right" select="key('access',$calid)" />
        <xsl:choose>
            <xsl:when test="$owner='true' or $user-share-write='true' or $everyone-share-write='true' or contains($right/@rights,'W')">
                <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                <a href="{$mgoURL}=DeleteAll&amp;ceid={@ceid}&amp;calid={$calid}&amp;window=event" title="Delete" onmouseover="swapImage('calendarEventDeleteImage{@ceid}{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('calendarEventDeleteImage{@ceid}{$channelID}','channel_delete_base.gif')">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" align="absmiddle" name="calendarEventDeleteImage{@ceid}{$channelID}" id="calendarEventDeleteImage{@ceid}{$channelID}" alt="this event" title="this event" />
                </a>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

