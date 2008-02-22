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
    <xsl:variable name="curCalendar" select="calendar-system/view/calendar" />
    <xsl:variable name="cur-user">
        <xsl:value-of select="calendar-system/logon/@user" />
    </xsl:variable>
    <xsl:include href="common.xsl" />
    <xsl:param name="window">import</xsl:param>
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="access-detail.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="main" />
<!-- used only for navigation-bar -->
    <xsl:param name="format">default</xsl:param>
    <xsl:template match="/">
<!-- Navigation bar -->
        <xsl:call-template name="links" />
        <xsl:apply-templates />
    </xsl:template>
    <xsl:template match="calendar-system">
        <xsl:variable name="subject">Subject</xsl:variable>
        <xsl:variable name="start-date">Start Date</xsl:variable>
        <xsl:variable name="start-time">Start Time</xsl:variable>
        <xsl:variable name="end-date">End Date</xsl:variable>
        <xsl:variable name="end-time">End Time</xsl:variable>
        <xsl:variable name="all-day">All day event</xsl:variable>
        <xsl:variable name="organizer">Meeting Organizer</xsl:variable>
        <xsl:variable name="attendees">Required Attendees</xsl:variable>
        <xsl:variable name="categories">Categories</xsl:variable>
        <xsl:variable name="description">
            <xsl:if test="$format='EVENT'">Description</xsl:if>
            <xsl:if test="$format='TODO'">Notes</xsl:if>
        </xsl:variable>
        <xsl:variable name="location">Location</xsl:variable>
        <xsl:variable name="priority">Priority</xsl:variable>
        <xsl:variable name="due-date">Due Date</xsl:variable>
        <xsl:variable name="date-complete">Date Completed</xsl:variable>
        <xsl:variable name="percent-complete">% Complete</xsl:variable>
        
        <div class="bounding-box2">
			<form action="{$baseActionURL}" name="importForm" method="post" value="*.*" enctype="multipart/form-data">
				<xsl:call-template name="mform" />
				<!--UniAcc: Layout Table -->
				<table width="100%" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td class="table-light-left" style="text-align:right">Type</td>
						<td class="table-content-right">
							<xsl:choose>
								<xsl:when test="$format='EVENT'">&#160;
									<input type='radio' name="format" value="EVENT" checked='checked' id="CI-TypeEventsR1"/>
									<label for="CI-TypeEventsR1">Events (tab delimited file)&#160;</label>
									<input type='radio' name="format" value="TODO" id="CI-TypeTodosR1"/>
									<label for="CI-TypeTodosR1">Tasks (tab delimited file)</label>
								</xsl:when>
								<xsl:when test="$format='TODO'">&#160;
									<input type='radio' name="format" value="EVENT"  id="CI-TypeEventsR1"/>
									<label for="CI-TypeEventsR1">Events (tab delimited file)&#160;</label>
									<input type='radio' name="format" value="TODO" checked='checked' id="CI-TypeTodosR1"/>
									<label for="CI-TypeTodosR1">Tasks (tab delimited file)</label>
								</xsl:when>
								<xsl:otherwise>&#160;
									<input type='radio' name="format" value="EVENT" checked='checked'  id="CI-TypeEventsR1"/>
									<label for="CI-TypeEventsR1">Events (tab delimited file)&#160;</label>
									<input type='radio' name="format" value="TODO" id="CI-TypeTodosR1" />
									<label for="CI-TypeTodosR1">Tasks (tab delimited file)</label>
								</xsl:otherwise>
							</xsl:choose>
							&#160;
							<input type='radio' name="format" value="ical" id="CI-TypeVCalenderR1" />
							<label for="CI-TypeVCalenderR1">ICalendar (iCal from Palm)</label>
						</td>
					</tr>
					<tr>
						<td class="table-light-left" style="text-align:right">
							<label for="CI-FileNameF1">File Name</label>
						</td>
						<td class="table-content-right">
							<input type="file" style="text-align: left" name="file" src="img/button_browse" enctype="multipart/form-data" size="50" maxlength="300" value="Click 'Browse' to select a file" id="CI-FileNameF1"/>
							<img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
							<input type="submit" class="uportal-button" value="Import" name="do~go" alt="To load tab-delimited file into the following form" title="To load tab-delimited file into the following form" />
						</td>
					</tr>
					<xsl:if test="$isOpen='true'">
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-CalenderS1">Calendar</label>
							</td>
							<td class="table-content-right" width="100%">
	<!-- Combo Calendars -->
								<select name="calid" size="1" class="text" id="CI-CalenderS1">
	<!-- ////////////////////////////////////////////// -->
	<!-- own calendars -->
									<xsl:apply-templates select="//calendar[@owner=$cur_user]" mode="owner">
										<xsl:with-param name="curCalid" select="//calendar-system/view/calendar/@calid" />
										<xsl:sort select="@calname" />
									</xsl:apply-templates>
	<!-- ////////////////////////////////////////////// -->
	<!-- shared calendars -->
									<xsl:for-each select="//calendar[@owner!=$cur_user]">
										<xsl:sort select="@owner" />
										<xsl:choose>
	<!-- DPCS Server -->
											<xsl:when test="//calendar-system/logon/access">
												<xsl:variable name="calright" select="key('access',@calid)" />
												<xsl:if test="contains($calright/@rights,'W')">
	<!-- Display name of shared calendar -->
													<xsl:variable name="displayCal">
														<xsl:choose>
															<xsl:when test="@owner=@calname">
																<xsl:value-of select="@calname" />
															</xsl:when>
															<xsl:otherwise>
																<xsl:value-of select="concat(@owner, ':', @calname)" />
															</xsl:otherwise>
														</xsl:choose>
													</xsl:variable>
	<!-- Selected calendar -->
													<xsl:choose>
														<xsl:when test="@calid=//calendar-system/view/calendar/@calid">
															<option selected="selected" value="{@calid}">
																<xsl:value-of select="$displayCal" />
															</option>
														</xsl:when>
														<xsl:otherwise>
															<option value="{@calid}">
																<xsl:value-of select="$displayCal" />
															</option>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:if>
											</xsl:when>
	<!-- IPlannet server -->
											<xsl:otherwise>
												<xsl:for-each select="ace[$cur_user=@cuid]">
													<xsl:if test="@write='true'">
	<!-- Display name of shared calendar -->
														<xsl:variable name="displayCal">
															<xsl:choose>
																<xsl:when test="../@owner=../@calname">
																	<xsl:value-of select="../@calname" />
																</xsl:when>
																<xsl:otherwise>
																	<xsl:value-of select="concat(../@owner, ':', ../@calname)" />
																</xsl:otherwise>
															</xsl:choose>
														</xsl:variable>
	<!-- Selected calendar -->
														<xsl:choose>
															<xsl:when test="../@calid=//calendar-system/view/calendar/@calid">
																<option selected="selected" value="{../@calid}">
																	<xsl:value-of select="$displayCal" />
																</option>
															</xsl:when>
															<xsl:otherwise>
																<option value="{../@calid}">
																	<xsl:value-of select="$displayCal" />
																</option>
															</xsl:otherwise>
														</xsl:choose>
													</xsl:if>
												</xsl:for-each>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:for-each>
								</select>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-TitleS1">Title</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="subject" id="CI-TitleS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$subject' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-StartDateS1">Start Date</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="start-date" id="CI-StartDateS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$start-date' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
					
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-StartTimeS1">Start Time</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="start-time" id="CI-StartTimeS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$start-time' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
						<xsl:if test="$format='EVENT'">
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-EndDateS1">End Date</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="end-date" id="CI-EndDateS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$end-date' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-EndTimeS1">End Time</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="end-time" id="CI-EndTimeS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$end-time' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-AllDayS1">All Day</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="all-day" id="CI-AllDayS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$all-day' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-OrganizerS1">Organizer</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="organizer" id="CI-OrganizerS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$organizer' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-AttendeesS1">Attendees</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="attendees" id="CI-AttendeesS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$attendees' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
						</xsl:if>
						<xsl:if test="$format='TODO'">
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-DueDateS1">Due Date</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="due-date" id="CI-DueDateS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$due-date' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-DateCompleteS1">Date Completed</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="date-complete" id="CI-DateCompleteS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$date-complete' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
							<tr>
								<td class="table-light-left" style="text-align:right" nowrap="nowrap">
									<label for="CI-PercentCompleteS1">Percent Completed</label>
								</td>
								<td class="table-content-right" width="100%">
									<select name="percent-complete" id="CI-PercentCompleteS1">
										<xsl:call-template name="load-cbo">
											<xsl:with-param name="selected" select='$percent-complete' />
										</xsl:call-template>
									</select>
								</td>
							</tr>
						</xsl:if>
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-CategoriesS1">Categories</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="categories" id="CI-CategoriesS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$categories' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-PlaceS1">Place</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="location" id="CI-PlaceS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$location' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-PriorityS1">Priority</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="priority" id="CI-PriorityS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$priority' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" nowrap="nowrap">
								<label for="CI-NotesS1">Notes</label>
							</td>
							<td class="table-content-right" width="100%">
								<select name="description" id="CI-NotesS1">
									<xsl:call-template name="load-cbo">
										<xsl:with-param name="selected" select='$description' />
									</xsl:call-template>
								</select>
							</td>
						</tr>
						<tr>
							<td colspan="2" class="table-nav" style="text-align:center">
								<input type="hidden" name="default" value="do~ok" />
								<input type="submit" class="uportal-button" value="OK" name="do~ok" title="To submit this information and return to the view of the calendar" />
								<input type="submit" class="uportal-button" value="Cancel" name="do~cancel" title="To cancel this and return to the view of the calendar" />
							</td>
						</tr>
					</xsl:if>
				</table>
			</form>
		</div>
    </xsl:template>
    <xsl:template name="load-cbo">
        <xsl:param name="selected" />
        <option value="" selected="selected"></option>
        <xsl:for-each select="fields/field">
            <xsl:choose>
                <xsl:when test="@name=$selected">
                    <option value="{@name}" selected="selected">
                        <xsl:value-of select="@name" />
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option value="{@name}">
                        <xsl:value-of select="@name" />
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>

