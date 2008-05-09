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
    <xsl:include href="common.xsl" />
    <xsl:include href="access-detail.xsl" />
    <xsl:include href="view-event-invitation.xsl" />
    <xsl:include href="utils.xsl" />
    <xsl:include href="date-widget.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="back">default</xsl:param>
<!-- variable/params-->
    <xsl:param name="notification">default</xsl:param>
    <xsl:variable name="cur_user" select="/calendar-system/logon/@user" />
    <xsl:variable name="calid" select="/calendar-system/view/calendar/@calid" />
    <xsl:variable name="notDelete" select="/calendar-system/view/entry" />
    <xsl:variable name="page" select="/calendar-system/view/@page" />
<!-- body -->
    <xsl:template match="calendar-system">
        <xsl:choose>
            <xsl:when test="$notDelete">
                <xsl:apply-templates select="view/entry" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="event-deleted" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:key name="access" match="/calendar-system/logon/access" use="@calid" />
<!--xsl:key name="ceid" match="/calendar-system/calendar/entry" use="@ceid"/-->
<!--tempate view entry->show-->
    <xsl:template match="view/entry">
        <xsl:variable name="edit" select="@ceid" />
        <xsl:variable name="ceid" select="@ceid" />
        <xsl:variable name="right" select="key('access',$calid)" />
        <xsl:variable name="cal" select="/calendar-system/calendar[@calid=$calid]" />
<!--return a node-set containg the single element <entry ceid=@ceid>-->
        <xsl:choose>
            <xsl:when test="$cal/entry[@ceid=$ceid]">
                <xsl:variable name="edit-entry" select="/calendar-system/view/entry" />
                <xsl:call-template name="show">
                    <xsl:with-param name="cal" select="$cal" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="entry" select="$edit-entry" />
                    <xsl:with-param name="right" select="$right" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="new-entry" select="/calendar-system/view/entry" />
                <xsl:call-template name="show">
                    <xsl:with-param name="cal" select="$cal" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="entry" select="$new-entry" />
                    <xsl:with-param name="right" select="$right" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
<!--/tempate view entry->show-->
<!--template show-->
    <xsl:template name="show">
        <xsl:param name="right" />
        <xsl:param name="cal" />
        <xsl:param name="edit" />
        <xsl:param name="entry" />
        <xsl:variable name="title" select="normalize-space($entry/event/text())" />
        <xsl:variable name="date" select='substring-before($entry/duration/@start,"_")' />
        <xsl:variable name="start" select='substring-after($entry/duration/@start,"_")' />
        <xsl:variable name="end" select='substring-after($entry/duration/@end,"_")' />
        <xsl:variable name="length" select="$entry/duration/@length" />
        <xsl:variable name="place" select='$entry/location/text()[$entry/location/text()!="&lt;none&gt;"]' />
        <xsl:variable name="share" select="$entry/@share" />
        <xsl:variable name="priority" select="$entry/event/@priority" />
        <xsl:variable name="category" select="$entry/event/category/text()" />
        <xsl:variable name="notes" select='$entry/event/description/text()[$entry/event/description/text()!="&lt;none&gt;"]' />
        <xsl:variable name="alarm" select="$entry/alarm" />
        <xsl:variable name="repeat" select="$entry/recurrence" />
        <xsl:variable name="isReccurent" select="contains($entry/@ceid,'.')" />
        <xsl:variable name="attendee" select="$entry/attendee" />
        <xsl:variable name="related" select="$entry/relatedTos" />
        <xsl:variable name="is-invitation" select="$cal/@owner=$cal/@calid and $attendee[@cuid=$cal/@owner]/@status='ACCEPTED' and $entry/organizer/text() !=$cal/@calid" />
        <xsl:variable name="until">
            <xsl:choose>
                <xsl:when test="$repeat/@until">
                    <xsl:value-of select="$repeat/@until" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$date" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="vsend" select="/calendar-system/view/send" />
        <xsl:variable name="week" select="/calendar-system/view/week" />
<!--//////// view event or invitation //////////////////-->
        <xsl:call-template name="autoFormJS" />
        <xsl:choose>
            <xsl:when test="($edit and $editEvents='N') or ($is-invitation) or ($calid!=$logon-user and substring-before($calid,':')!=$logon-user and $user-share-write!='true' and $everyone-share-write!='true' and not(contains($right/@rights,'W')))">
                <xsl:call-template name="view">
                    <xsl:with-param name="cal" select="$cal" />
                    <xsl:with-param name="entry" select="$entry" />
                    <xsl:with-param name="is-invitation" select="$is-invitation" />
                    <xsl:with-param name="title" select="$title" />
                    <xsl:with-param name="date" select="$date" />
                    <xsl:with-param name="end" select="$end" />
                    <xsl:with-param name="start" select="$start" />
                    <xsl:with-param name="length" select="$length" />
                    <xsl:with-param name="place" select="$place" />
                    <xsl:with-param name="share" select="$share" />
                    <xsl:with-param name="category" select="$category" />
                    <xsl:with-param name="priority" select="$priority" />
                    <xsl:with-param name="notes" select="$notes" />
                    <xsl:with-param name="repeat" select="$repeat" />
                    <xsl:with-param name="isReccurent" select="$isReccurent" />
                    <xsl:with-param name="alarm" select="$alarm" />
                    <xsl:with-param name="until" select="$until" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <form name="eventInfo" method="post" action="{$baseActionURL}" onsubmit="return(checkFormCalC(this));">
                	<!--UniAcc: Layout Table -->
                    <table cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <th class="th-top">
<!-- Event Title -->
                                <xsl:choose>
                                    <xsl:when test="$edit">Edit Event</xsl:when>
                                    <xsl:when test="not($edit)">New Event</xsl:when>
                                </xsl:choose>
<!--Attendees TITLE-->
                            </th>
                            <th class="th-top">
<!-- Title -->
                            Event Attendees</th>
                        </tr>
                        <tr>
<!-- detail event -->
                            <td width="50%" valign="top" height="100%">
                            	<!--UniAcc: Layout Table -->
                                <table width="100%" cellpadding="0" cellspacing="0" border="0" height="100%">
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvent-CalenderS1">Calendar:</label>
                                        </td>
                                        <td class="table-content-right" align="left">
<!-- Combo Calendars -->
                                            <select name="calid" size="1" class="text" id="CEvent-CalenderS1">
                                                <xsl:if test="$edit">
                                                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                                                </xsl:if>
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
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvent-TitleT1">Title:</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <input type="text" name="event" size="27" class="text" id="CEvent-TitleT1">
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="$title" />
                                                </xsl:attribute>
                                            </input>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvent-DateS1">Date:</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <select name="month" class="text" id="CEvent-DateS1">
                                                <option value="1">
                                                <xsl:if test='starts-with($date,"1/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Jan</option>
                                                <option value="2">
                                                <xsl:if test='starts-with($date,"2/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Feb</option>
                                                <option value="3">
                                                <xsl:if test='starts-with($date,"3/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Mar</option>
                                                <option value="4">
                                                <xsl:if test='starts-with($date,"4/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Apr</option>
                                                <option value="5">
                                                <xsl:if test='starts-with($date,"5/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                May</option>
                                                <option value="6">
                                                <xsl:if test='starts-with($date,"6/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Jun</option>
                                                <option value="7">
                                                <xsl:if test='starts-with($date,"7/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Jul</option>
                                                <option value="8">
                                                <xsl:if test='starts-with($date,"8/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Aug</option>
                                                <option value="9">
                                                <xsl:if test='starts-with($date,"9/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Sep</option>
                                                <option value="10">
                                                <xsl:if test='starts-with($date,"10/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Oct</option>
                                                <option value="11">
                                                <xsl:if test='starts-with($date,"11/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Nov</option>
                                                <option value="12">
                                                <xsl:if test='starts-with($date,"12/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Dec</option>
                                            </select>
                                            <select name="day" class="text">
                                                <option value="1">
                                                <xsl:if test='contains($date,"/1/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                1</option>
                                                <option value="2">
                                                <xsl:if test='contains($date,"/2/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                2</option>
                                                <option value="3">
                                                <xsl:if test='contains($date,"/3/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                3</option>
                                                <option value="4">
                                                <xsl:if test='contains($date,"/4/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                4</option>
                                                <option value="5">
                                                <xsl:if test='contains($date,"/5/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                5</option>
                                                <option value="6">
                                                <xsl:if test='contains($date,"/6/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                6</option>
                                                <option value="7">
                                                <xsl:if test='contains($date,"/7/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                7</option>
                                                <option value="8">
                                                <xsl:if test='contains($date,"/8/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                8</option>
                                                <option value="9">
                                                <xsl:if test='contains($date,"/9/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                9</option>
                                                <option value="10">
                                                <xsl:if test='contains($date,"/10/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                10</option>
                                                <option value="11">
                                                <xsl:if test='contains($date,"/11/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                11</option>
                                                <option value="12">
                                                <xsl:if test='contains($date,"/12/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                12</option>
                                                <option value="13">
                                                <xsl:if test='contains($date,"/13/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                13</option>
                                                <option value="14">
                                                <xsl:if test='contains($date,"/14/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                14</option>
                                                <option value="15">
                                                <xsl:if test='contains($date,"/15/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                15</option>
                                                <option value="16">
                                                <xsl:if test='contains($date,"/16/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                16</option>
                                                <option value="17">
                                                <xsl:if test='contains($date,"/17/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                17</option>
                                                <option value="18">
                                                <xsl:if test='contains($date,"/18/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                18</option>
                                                <option value="19">
                                                <xsl:if test='contains($date,"/19/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                19</option>
                                                <option value="20">
                                                <xsl:if test='contains($date,"/20/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                20</option>
                                                <option value="21">
                                                <xsl:if test='contains($date,"/21/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                21</option>
                                                <option value="22">
                                                <xsl:if test='contains($date,"/22/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                22</option>
                                                <option value="23">
                                                <xsl:if test='contains($date,"/23/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                23</option>
                                                <option value="24">
                                                <xsl:if test='contains($date,"/24/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                24</option>
                                                <option value="25">
                                                <xsl:if test='contains($date,"/25/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                25</option>
                                                <option value="26">
                                                <xsl:if test='contains($date,"/26/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                26</option>
                                                <option value="27">
                                                <xsl:if test='contains($date,"/27/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                27</option>
                                                <option value="28">
                                                <xsl:if test='contains($date,"/28/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                28</option>
                                                <option value="29">
                                                <xsl:if test='contains($date,"/29/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                29</option>
                                                <option value="30">
                                                <xsl:if test='contains($date,"/30/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                30</option>
                                                <option value="31">
                                                <xsl:if test='contains($date,"/31/")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                31</option>
                                            </select>
                                            <select name="year" class="text">
       											<xsl:call-template name="full-year-options">
       												<xsl:with-param name="selected-date" select="substring-after( substring-after( $date,'/') ,'/')"/>
       											</xsl:call-template>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right" valign="top">Time:</td>
                                        <td class="table-content-right" align="left">
                                        <input type="radio" class="radio" name="all-day" value="1" id="CEvent-AllDayR1">
                                            <xsl:if test='$length="all-day"'>
                                                <xsl:attribute name="checked">true</xsl:attribute>
                                            </xsl:if>
                                            <label for="CEvent-AllDayR1">
												All-Day
											</label>
                                        </input>
                                        <br />
                                        <input type="radio" class="radio" name="all-day" value="0" id="CEvent-StartR1">
                                            <xsl:if test='$length!="all-day"'>
                                                <xsl:attribute name="checked">true</xsl:attribute>
                                            </xsl:if>
                                            <label for="CEvent-StartR1">
											 Start 
											</label>
                                        </input>
                                        <select name="hour" class="text">
                                            <option value="0">
                                            <xsl:if test='starts-with($start,"0:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            12 am</option>
                                            <option value="1">
                                            <xsl:if test='starts-with($start,"1:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            1 am</option>
                                            <option value="2">
                                            <xsl:if test='starts-with($start,"2:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            2 am</option>
                                            <option value="3">
                                            <xsl:if test='starts-with($start,"3:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            3 am</option>
                                            <option value="4">
                                            <xsl:if test='starts-with($start,"4:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            4 am</option>
                                            <option value="5">
                                            <xsl:if test='starts-with($start,"5:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            5 am</option>
                                            <option value="6">
                                            <xsl:if test='starts-with($start,"6:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            6 am</option>
                                            <option value="7">
                                            <xsl:if test='starts-with($start,"7:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            7 am</option>
                                            <option value="8">
                                            <xsl:if test='$length="all-day" or starts-with($start,"8:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            8 am</option>
                                            <option value="9">
                                            <xsl:if test='starts-with($start,"9:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            9 am</option>
                                            <option value="10">
                                            <xsl:if test='starts-with($start,"10:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            10 am</option>
                                            <option value="11">
                                            <xsl:if test='starts-with($start,"11:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            11 am</option>
                                            <option value="12">
                                            <xsl:if test='starts-with($start,"12:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            12 pm</option>
                                            <option value="13">
                                            <xsl:if test='starts-with($start,"13:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            1 pm</option>
                                            <option value="14">
                                            <xsl:if test='starts-with($start,"14:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            2 pm</option>
                                            <option value="15">
                                            <xsl:if test='starts-with($start,"15:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            3 pm</option>
                                            <option value="16">
                                            <xsl:if test='starts-with($start,"16:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            4 pm</option>
                                            <option value="17">
                                            <xsl:if test='starts-with($start,"17:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            5 pm</option>
                                            <option value="18">
                                            <xsl:if test='starts-with($start,"18:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            6 pm</option>
                                            <option value="19">
                                            <xsl:if test='starts-with($start,"19:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            7 pm</option>
                                            <option value="20">
                                            <xsl:if test='starts-with($start,"20:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            8 pm</option>
                                            <option value="21">
                                            <xsl:if test='starts-with($start,"21:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            9 pm</option>
                                            <option value="22">
                                            <xsl:if test='starts-with($start,"22:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            10 pm</option>
                                            <option value="23">
                                            <xsl:if test='starts-with($start,"23:") and $length!="all-day"'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            11 pm</option>
                                        </select>
                                        <select name="minute" class="text">
                                            <option value="0">
                                            <xsl:if test='contains($start,":00")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :00</option>
                <option value="5">
                                            <xsl:if test='contains($start,":05")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :05</option>
                                            <option value="10">
                                            <xsl:if test='contains($start,":10")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :10</option>
                                            <option value="15">
                                            <xsl:if test='contains($start,":15")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :15</option>
                                            <option value="20">
                                            <xsl:if test='contains($start,":20")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :20</option>
                                            <option value="25">
                                            <xsl:if test='contains($start,":25")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :25</option>
                                            <option value="30">
                                            <xsl:if test='contains($start,":30")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :30</option>
                                            <option value="35">
                                            <xsl:if test='contains($start,":35")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :35</option>
                                            <option value="40">
                                            <xsl:if test='contains($start,":40")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :40</option>
                                            <option value="45">
                                            <xsl:if test='contains($start,":45")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :45</option>
                                            <option value="50">
                                            <xsl:if test='contains($start,":50")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :50</option>
                                            <option value="55">
                                            <xsl:if test='contains($start,":55")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :55</option>
                                        </select>
                                        <br />
                                        <img src="{$SPACER}" border="0" height="10" alt="" title="" />
                                        <br />
                                        <img src="{$SPACER}" border="0" width="20" alt="" title="" />
                                        <label for="CEvent-LengthS1">Length</label>
                                        <select name="hours" class="text" id="CEvent-LengthS1">
                                            <option value="0">
                                            <xsl:if test='starts-with($length,"0:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            0</option>
                                            <option value="1">
                                            <xsl:if test='(starts-with($length,"1:") or $length="all-day") or not($edit)'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            1</option>
                                            <option value="2">
                                            <xsl:if test='starts-with($length,"2:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            2</option>
                                            <option value="3">
                                            <xsl:if test='starts-with($length,"3:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            3</option>
                                            <option value="4">
                                            <xsl:if test='starts-with($length,"4:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            4</option>
                                            <option value="5">
                                            <xsl:if test='starts-with($length,"5:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            5</option>
                                            <option value="6">
                                            <xsl:if test='starts-with($length,"6:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            6</option>
                                            <option value="7">
                                            <xsl:if test='starts-with($length,"7:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            7</option>
                                            <option value="8">
                                            <xsl:if test='starts-with($length,"8:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            8</option>
                                            <option value="9">
                                            <xsl:if test='starts-with($length,"9:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            9</option>
                                            <option value="10">
                                            <xsl:if test='starts-with($length,"10:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            10</option>
                                            <option value="11">
                                            <xsl:if test='starts-with($length,"11:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            11</option>
                                            <option value="12">
                                            <xsl:if test='starts-with($length,"12:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            12</option>
                                            <option value="13">
                                            <xsl:if test='starts-with($length,"13:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            13</option>
                                            <option value="14">
                                            <xsl:if test='starts-with($length,"14:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            14</option>
                                            <option value="15">
                                            <xsl:if test='starts-with($length,"15:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            15</option>
                                            <option value="16">
                                            <xsl:if test='starts-with($length,"16:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            16</option>
                                            <option value="17">
                                            <xsl:if test='starts-with($length,"17:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            17</option>
                                            <option value="18">
                                            <xsl:if test='starts-with($length,"18:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            18</option>
                                            <option value="19">
                                            <xsl:if test='starts-with($length,"19:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            19</option>
                                            <option value="20">
                                            <xsl:if test='starts-with($length,"20:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            20</option>
                                            <option value="21">
                                            <xsl:if test='starts-with($length,"21:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            21</option>
                                            <option value="22">
                                            <xsl:if test='starts-with($length,"22:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            22</option>
                                            <option value="23">
                                            <xsl:if test='starts-with($length,"23:")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            23</option>
                                        </select>

                                        <select name="minutes" class="text">
                                            <option value="0">
                                            <xsl:if test='contains($length,":00")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :00</option>
                                            <option value="10">
                                            <xsl:if test='contains($length,":10")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                             :10</option>
                                            <option value="15">
                                            <xsl:if test='contains($length,":15")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :15</option>
                                            <option value="20">
                                            <xsl:if test='contains($length,":20")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :20</option>
                                            <option value="25">
                                            <xsl:if test='contains($length,":25")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :25</option>
                                             <option value="30">
                                            <xsl:if test='contains($length,":30")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :30</option>
                                            <option value="35">
                                            <xsl:if test='contains($length,":35")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :35</option>
                                            <option value="40">
                                            <xsl:if test='contains($length,":40")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :40</option>
                                             <option value="45">
                                            <xsl:if test='contains($length,":45")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :45</option>
                                            <option value="50">
                                            <xsl:if test='contains($length,":50")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :50</option>
                                            <option value="55">
                                            <xsl:if test='contains($length,":55")'>
                                                <xsl:attribute name="selected">true</xsl:attribute>
                                            </xsl:if>
                                            :55</option>
                                        </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvent-PlaceTA1">Place:</label>
                                        </td>
                                        <td class="table-content-right" align="left">
											<input type="text" name="place" size="27" maxlength="500" class="text" id="CEvent-PlaceTA1">
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="string($place)" />
                                                </xsl:attribute>
                                            </input>
                                            <!-- <textarea name="place" class="text" wrap="virtual" rows="3" cols="25" id="CEvent-PlaceTA1">
                                                <xsl:value-of select="string($place)" />
                                            </textarea> -->
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvent-CategoryS1">Category</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <select name="category" class="text" id="CEvent-CategoryS1">
                                                <option value="None">None</option>
                                                <option value="Assessment">
                                                <xsl:if test='contains($category,"Assessment")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Assessment</option>
                                                <option value="Assignment">
                                                <xsl:if test='contains($category,"Assignment")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Assignment</option>
                                                <option value="Appointment">
                                                <xsl:if test='contains($category,"Appointment")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Appointment</option>
                                                <option value="Call">
                                                <xsl:if test='contains($category,"Call")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Call</option>
                                                <option value="Holiday">
                                                <xsl:if test='contains($category,"Holiday")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Holiday</option>
                                                <option value="Interview">
                                                <xsl:if test='contains($category,"Interview")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Interview</option>
                                                <option value="Meeting">
                                                <xsl:if test='contains($category,"Meeting")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Meeting</option>
                                                <option value="Party">
                                                <xsl:if test='contains($category,"Party")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Party</option>
                                                <option value="Travel">
                                                <xsl:if test='contains($category,"Travel")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Travel</option>
                                                <option value="Vacation">
                                                <xsl:if test='contains($category,"Vacation")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Vacation</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvent-PriorityS1">Priority:</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <select name="priority" class="text" id="CEvent-PriorityS1">
                                                <option value="0">
                                                <xsl:if test='contains($priority,"0")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Very Low</option>
                                                <option value="3">
                                                <xsl:if test='contains($priority,"3")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Low</option>
                                                <option value="5">
                                                <xsl:if test='contains($priority,"5") or (not($priority) and not($edit))'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Normal</option>
                                                <option value="7">
                                                <xsl:if test='contains($priority,"7")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                High</option>
                                                <option value="9">
                                                <xsl:if test='contains($priority,"9")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Very High</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CEvents-NotesTA1">Notes:</label>
                                        </td>
                                        <td class="table-content-right" align="left">
											<!--input type="text" name="description" size="27" maxlength="500" class="text" id="CEvents-NotesTA1">
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="string($notes)" />
                                                </xsl:attribute>
                                            </input-->

                                            <textarea name="description" class="text" wrap="virtual" rows="3" cols="25" id="CEvents-NotesTA1">
                                                <xsl:value-of select="string($notes)" />
                                            </textarea>
                                        </td>
                                    </tr>
                                </table>
<!--
                <table width='100%' height='100%' class="table-content-right">
                   <tr><td height='100%'></td></tr>
                </table>
                -->
                            </td>
                            <td width="50%" valign="top" height="100%">
<!--START ATTENDEES-->
								<!--UniAcc: Layout Table -->
                                <table width="100%" cellpadding="0" cellspacing="0" border="0">
<!--                                    <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))"> -->
                                        <tr>
                                            <td class="table-light-left" align="right" nowrap="nowrap" valign="top">Invited Attendees:</td>
                                            <td class="table-content-right" align="left" width="100%">
                                                <xsl:if test="count($entry/attendee) = 0">No current attendees.
                                                </xsl:if>
<!--START CURRENT ATTENDEES-->
												<!--UniAcc: Layout Table -->
                                                <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-text">
                                                    <xsl:if test="$edit">
                                                        <xsl:for-each select="$entry/attendee">
                                                            <xsl:sort select="text()" />
                                                            <xsl:if test="@ientity != 'o'">
                                                            <tr>
                                                                <xsl:choose>
                                                                    <xsl:when test="@cuid='@'">
                                                                        <td>
																			<xsl:text>
																			  
																			</xsl:text>
                                                                            <img border="0" src="{$baseImagePath}/persons_16.gif" alt="Group" title="Group" />
                                                                        </td>
                                                                        <td colspan="2">
																			<xsl:text>
																			Everyone
																			</xsl:text>
                                                                        </td>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        <td>
																			<xsl:text>
																			  
																			</xsl:text>
                                                                            <xsl:choose>
                                                                                <xsl:when test="@itype='G'">
                                                                                    <img border="0" src="{$baseImagePath}/persons_16.gif" alt="Group" title="Group" />
                                                                                </xsl:when>
                                                                                <xsl:otherwise>
                                                                                    <img border="0" src="{$baseImagePath}/person_16.gif" alt="Individual" title="Individual" />
                                                                                </xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </td>
                                                                        <td colspan="2" nowrap="no">
                                                                            <xsl:value-of select="text()" />
                                                                        </td>
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                                <td align="left">
                                                                    <xsl:choose>
                                                                        <xsl:when test='@status="ACCEPTED"'>&#160;Accepted</xsl:when>
                                                                        <xsl:when test='@status="NEEDS-ACTION"'>&#160;Pending</xsl:when>
                                                                        <xsl:otherwise>Declined</xsl:otherwise>
                                                                    </xsl:choose>
                                                                </td>
                                                            </tr>
                                                          </xsl:if>
                                                        </xsl:for-each>
                                                    </xsl:if>
                                                </table>
<!--END CURRENT ATTENDEES-->
                                            </td>
                                        </tr>
                                     <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
                                        <tr>
                                            <td class="table-light-left" align="right" valign="top" nowrap="nowrap">New Invites:
                                            </td>
                                            <td class="table-content-right" align="left">
                                                <xsl:if test="count(/calendar-system/user[@selected='true']) = 0">No new invites.
                                                </xsl:if>
                                            	<!--UniAcc: Layout Table -->
                                                <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-text">
                                                    <xsl:for-each select="/calendar-system/user[@selected='true']">
                                                        <xsl:sort select="text()" />
                                                            <tr>
                                                                <xsl:choose>
                                                                    <xsl:when test="@cuid='@'">
                                                                        <td align="left">
																<xsl:text>
																  
																</xsl:text>
                                                                            <a href="{$mgoURL}=gotogroup&amp;calid={@calid}"  title="Display">
                                                                                <img border="0" src="{$baseImagePath}/persons_16.gif" alt="group" title="group" />
                                                                            </a>
                                                                        </td>
                                                                        <td colspan="3">
																<xsl:text>
																Everyone
																</xsl:text>
                                                                            <a href="{$mdoURL}=delete&amp;user={@cuid}" title="">
                                                                                <img src="{$baseImagePath}/delete_12.gif" border="0" alt="Delete" title="Delete" />
                                                                            </a>
                                                                        </td>
                                                                    </xsl:when>
                                                                    <xsl:otherwise>
                                                                        <td align="left">
																<xsl:text>
																  
																</xsl:text>
                                                                            <xsl:choose>
                                                                                <xsl:when test="@itype='G'">
                                                                                    <img border="0" src="{$baseImagePath}/persons_16.gif" alt="Group" title="Group" />
                                                                                </xsl:when>
                                                                                <xsl:otherwise>
                                                                                    <img border="0" src="{$baseImagePath}/person_16.gif" alt="Individual" title="Individual" />
                                                                                </xsl:otherwise>
                                                                            </xsl:choose>
                                                                        </td>
                                                                        <td colspan="3">
																<xsl:text>
																 
																</xsl:text>
                                                                            <xsl:value-of select="text()" />
																<xsl:text>
																 
																</xsl:text>
                                                                            <a href="{$mdoURL}=delete&amp;user={@cuid}" title="Remove" onmouseover="swapImage('calendarUserDeleteImage{@cuid}{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('calendarUserDeleteImage{@cuid}{$channelID}','channel_delete_base.gif')">
                                                                                <img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" border="0" align="absmiddle" name="calendarUserDeleteImage{@cuid}{$channelID}" id="calendarUserDeleteImage{@cuid}{$channelID}" alt="this attendee" title="this attendee" />
                                                                            </a>
                                                                        </td>
                                                                    </xsl:otherwise>
                                                                </xsl:choose>
                                                            </tr>
                                                    </xsl:for-each>
                                                </table>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="table-light-left" align="right" valign="top" nowrap="nowrap">Invite New Attendees:</td>
                                            <td class="table-content-right" align="left">
                                                <input type="button" class="uportal-button" style="margin-left:3px;" name="do~addAttendee" title="Invite New Attendees" value="Invite" id="CEvent-AddressBookT1" onclick="document.getElementById('calendarDefaultAction').value='do~addAttendee';this.form.submitValue.value=this.value;if(this.form.onsubmit())this.form.submit();" /><br/>
                                            	<div style="margin-top:15px;">
                                                    <label for="CEvent-EmailsT1">By email address:</label><br/>
                                                    <input type="text" style="margin-bottom:5px;" name="emails" size="25" id="CEvent-EmailsT1"/><br/>
                                                    <span class="uportal-text-small">Separate multiple emails with a semicolon
                                                    <br/>(jdoe@domain.com; jsmith@domain.com)</span>
                                                </div>
                                            </td>
                                        </tr>
                                    </xsl:if>
                                    <tr>
                                        <td class="table-light-left-bottom" valign="top" align="right" nowrap="nowrap">Notify Attendees:</td>
                                        <td class="table-content-right-bottom" valign="top" align="left" nowrap="nowrap">
                                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                                        <input type="checkbox" class="radio" name="notification" id="CEvent-NotificationC1">
                                            <xsl:if test="$vsend/@notification='true'">
                                                <xsl:attribute name="checked">true</xsl:attribute>
                                            </xsl:if>
                                            <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                                        </input>
                                        <label for="CEvent-NotificationC1">Notification</label>
                                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                                        <input type="checkbox" class="radio" name="email" id="CEvent-EmailC1">
                                            <xsl:if test="$vsend/@email='true'">
                                                <xsl:attribute name="checked">true</xsl:attribute>
                                            </xsl:if>
                                            <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                                        </input>
                                        <label for="CEvent-EmailC1">Email</label>
                                        </td>
                                    </tr>
<!--RECURRENCE-->
                                    <tr>
                                        <th colspan="2" class="th" style="padding-top:25px;">Event Recurrence</th>
                                    </tr>
                                    <xsl:choose>
<!-- new Event -->
                                        <xsl:when test="not($edit)">
                                            <tr>
                                                <td class="table-light-left" align="right" valign="top">Recurrence Pattern:</td>
                                                <td class="table-content-right" height="100%">
<!-- RECURRENCE PATTERN INPUTS -->
													<!--UniAcc: Layout Table -->
                                                    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="uportal-text">
                                                        <tr>
                                                            <td valign="abstop" height="0" nowrap="nowrap">
																&#160;
                                                                <input type="radio" class="radio" name="freq" value="0" id="CEvent-RecurrNoneR1">
                                                                    <xsl:if test="not($repeat)">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td valign="top" height="0">
																<label for="CEvent-RecurrNoneR1">
																 None
																</label>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap="nowrap">
																&#160;
                                                                <input type="radio" class="radio" name="freq" value="1" onclick="document.eventInfo.days.focus()" id="CEvent-RecurrDayR1">
                                                                    <xsl:if test="$repeat/@frequency = 'DAILY'">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td nowrap="nowrap">
																<label for="CEvent-RecurrDayR1">
																 Every
																</label>
                                                                <input type="text" size="2" name="days" class="text" maxlength="2">
                                                                    <xsl:choose>
                                                                        <xsl:when test="$repeat/@frequency = 'DAILY'">
                                                                            <xsl:attribute name="value">
                                                                                <xsl:value-of select="$repeat/@interval" />
                                                                            </xsl:attribute>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            <xsl:attribute name="value">1</xsl:attribute>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </input>
																<xsl:text>
																 day(s)
																</xsl:text>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap="nowrap">
																&#160;
                                                                <input type="radio" class="radio" name="freq" value="2" onclick="document.eventInfo.weeks.focus()" id="CEvent-RecurrWeekR1">
                                                                    <xsl:if test="$repeat/@frequency = 'WEEKLY'">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td nowrap="nowrap">
																<label for="CEvent-RecurrWeekR1">
																	Every
																</label>
                                                                <input type="text" size="2" name="weeks" class="text" maxlength="2">
                                                                    <xsl:choose>
                                                                        <xsl:when test="$repeat/@frequency = 'WEEKLY'">
                                                                            <xsl:attribute name="value">
                                                                                <xsl:value-of select="$repeat/@interval" />
                                                                            </xsl:attribute>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            <xsl:attribute name="value">1</xsl:attribute>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </input>
																week(s) on:
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td />
                                                            <td class="table-content-single">
<!-- DAY OF WEEK RECURRENCE -->
																<!--UniAcc: Layout Table -->
                                                                <table cellpadding="0" cellspacing="0" border="0" class="uportal-text">
                                                                    <tr>
                                                                        <td>
                                                                            <input type="checkbox" class="radio" name="mon" id="CEvent-RecurrMonC1">
                                                                                <xsl:if test="$week/@mon = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrMonC1">
																			 Mon<img src="{$SPACER}" border="0" width="5" alt="monday" title="monday" />
																			</label>
                                                                        </td>
                                                                        <td>
                                                                            <input type="checkbox" class="radio" name="tue" id="CEvent-RecurrTueC1">
                                                                                <xsl:if test="$week/@tue = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrTueC1">
																			 Tue<img src="{$SPACER}" border="0" width="5" alt="tuesday" title="tuesday" />   
																			</label>
                                                                        </td>
                                                                        <td>
                                                                            <input type="checkbox" class="radio" name="wed" id="CEvent-RecurrWedC1">
                                                                                <xsl:if test="$week/@wed = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrWedC1">
																			 Wed<img src="{$SPACER}" border="0" width="5" alt="wednesday" title="wednesday" />
																			</label>
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td>
                                                                            <input type="checkbox" class="radio" name="thu" id="CEvent-RecurrThuC1">
                                                                                <xsl:if test="$week/@thu = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrThuC1">
																			 Thu<img src="{$SPACER}" border="0" width="5" alt="thursday" title="thursday" />   
																			</label>
                                                                        </td>
                                                                        <td>
                                                                            <input type="checkbox" class="radio" name="fri" id="CEvent-RecurrFriC1">
                                                                                <xsl:if test="$week/@fri = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrFriC1">
																			 Fri<img src="{$SPACER}" border="0" width="5" alt="friday" title="friday" />   
																			</label>
                                                                        </td>
                                                                        <td>
                                                                            <input type="checkbox" class="radio" name="sat" id="CEvent-RecurrSatC1">
                                                                                <xsl:if test="$week/@sat = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrSatC1">
																			 Sat<img src="{$SPACER}" border="0" width="5" alt="saturday" title="saturday" />
																			</label>
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <td colspan="3">
                                                                            <input type="checkbox" class="radio" name="sun" id="CEvent-RecurrSunC1">
                                                                                <xsl:if test="$week/@sun = 'yes' and $repeat/@frequency = 'WEEKLY'">
                                                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                                                </xsl:if>
                                                                            </input>
																			<label for="CEvent-RecurrSunC1">
																			 Sun<img src="{$SPACER}" border="0" width="5" alt="sunday" title="sunday" />
																			</label>
                                                                        </td>
                                                                    </tr>
                                                                </table>
<!-- END DAY OF WEEK RECURRENCE -->
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td>
                                                                <img src="{$SPACER}" border="0" width="10" alt="" title="" />
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap="nowrap">
																<xsl:text>&#160;</xsl:text>
                                                                <input type="radio" class="radio" name="freq" value="3" id="CE-RecurrMonthR1" onclick="document.eventInfo.months.focus()">
                                                                    <xsl:if test="$repeat/@frequency = 'MONTHLY'">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td nowrap="nowrap">
                                                            	<label for="CE-RecurrMonthR1" id="CE-RecurrMonthR1">
																 Every 
																</label>
                                                                <input type="text" size="2" name="months" class="text" maxlength="2">
                                                                    <xsl:choose>
                                                                        <xsl:when test="$repeat/@frequency = 'MONTHLY'">
                                                                            <xsl:attribute name="value">
                                                                                <xsl:value-of select="$repeat/@interval" />
                                                                            </xsl:attribute>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            <xsl:attribute name="value">1</xsl:attribute>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </input>
																<xsl:text>
																 month(s)
																</xsl:text>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td nowrap="nowrap">
																<xsl:text>&#160;</xsl:text>
                                                                <input type="radio" class="radio" name="freq" value="4" id="CE-RecurrYearR1">
                                                                    <xsl:if test="$repeat/@frequency = 'YEARLY'">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td nowrap="nowrap">
                                                            	<label for="CE-RecurrYearR1">
																 Every year
																</label>
                                                            </td>
                                                        </tr>
                                                    </table>
<!-- END RECURRENCE PATTERN INPUTS -->
                                                </td>
                                            </tr>
<!-- RANGE OF RECURRENCE -->
                                            <tr>
                                                <td class="table-light-left" align="right" valign="top">Range of Recurrence:</td>
                                                <td class="table-content-right-bottom">
<!-- RANGE OF RECURRENCE INPUTS -->
													<!--UniAcc: Layout Table -->
                                                    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="uportal-text">
                                                        <tr>
                                                            <td nowrap="nowrap">
																<xsl:text>&#160;</xsl:text>
                                                                <input type="radio" class="radio" name="until" value="0" onclick="document.eventInfo.times.focus()" id="CEvent-EndAfterR1">
                                                                    <xsl:if test="$repeat/@count">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td nowrap="nowrap">
																<label for="CEvent-EndAfterR1">End After:</label>
                                                                <input type="text" size="2" name="times" class="text" maxlength="2">
                                                                    <xsl:choose>
                                                                        <xsl:when test="$repeat/@count">
                                                                            <xsl:attribute name="value">
                                                                                <xsl:value-of select="$repeat/@count" />
                                                                            </xsl:attribute>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            <xsl:attribute name="value">1</xsl:attribute>
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                </input>
																 occurrences
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td>
                                                                <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td>
                                                            	&#160;
                                                                <input type="radio" class="radio" name="until" value="1" id="CEvent-EndByR1">
                                                                    <xsl:if test="$repeat/@until or not($repeat)">
                                                                        <xsl:attribute name="checked">true</xsl:attribute>
                                                                    </xsl:if>
                                                                </input>
                                                            </td>
                                                            <td>
																<label for="CEvent-EndByR1">End By:</label>
                                                                <select name="month2" class="text">
                                                                    <option value="1">
                                                                    <xsl:if test='starts-with($until,"1/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Jan</option>
                                                                    <option value="2">
                                                                    <xsl:if test='starts-with($until,"2/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Feb</option>
                                                                    <option value="3">
                                                                    <xsl:if test='starts-with($until,"3/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Mar</option>
                                                                    <option value="4">
                                                                    <xsl:if test='starts-with($until,"4/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Apr</option>
                                                                    <option value="5">
                                                                    <xsl:if test='starts-with($until,"5/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    May</option>
                                                                    <option value="6">
                                                                    <xsl:if test='starts-with($until,"6/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Jun</option>
                                                                    <option value="7">
                                                                    <xsl:if test='starts-with($until,"7/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Jul</option>
                                                                    <option value="8">
                                                                    <xsl:if test='starts-with($until,"8/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Aug</option>
                                                                    <option value="9">
                                                                    <xsl:if test='starts-with($until,"9/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Sep</option>
                                                                    <option value="10">
                                                                    <xsl:if test='starts-with($until,"10/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Oct</option>
                                                                    <option value="11">
                                                                    <xsl:if test='starts-with($until,"11/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Nov</option>
                                                                    <option value="12">
                                                                    <xsl:if test='starts-with($until,"12/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    Dec</option>
                                                                </select>
                                                                <select name="day2" class="text">
                                                                    <option value="1">
                                                                    <xsl:if test='contains($until,"/1/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    1</option>
                                                                    <option value="2">
                                                                    <xsl:if test='contains($until,"/2/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    2</option>
                                                                    <option value="3">
                                                                    <xsl:if test='contains($until,"/3/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    3</option>
                                                                    <option value="4">
                                                                    <xsl:if test='contains($until,"/4/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    4</option>
                                                                    <option value="5">
                                                                    <xsl:if test='contains($until,"/5/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    5</option>
                                                                    <option value="6">
                                                                    <xsl:if test='contains($until,"/6/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    6</option>
                                                                    <option value="7">
                                                                    <xsl:if test='contains($until,"/7/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    7</option>
                                                                    <option value="8">
                                                                    <xsl:if test='contains($until,"/8/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    8</option>
                                                                    <option value="9">
                                                                    <xsl:if test='contains($until,"/9/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    9</option>
                                                                    <option value="10">
                                                                    <xsl:if test='contains($until,"/10/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    10</option>
                                                                    <option value="11">
                                                                    <xsl:if test='contains($until,"/11/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    11</option>
                                                                    <option value="12">
                                                                    <xsl:if test='contains($until,"/12/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    12</option>
                                                                    <option value="13">
                                                                    <xsl:if test='contains($until,"/13/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    13</option>
                                                                    <option value="14">
                                                                    <xsl:if test='contains($until,"/14/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    14</option>
                                                                    <option value="15">
                                                                    <xsl:if test='contains($until,"/15/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    15</option>
                                                                    <option value="16">
                                                                    <xsl:if test='contains($until,"/16/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    16</option>
                                                                    <option value="17">
                                                                    <xsl:if test='contains($until,"/17/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    17</option>
                                                                    <option value="18">
                                                                    <xsl:if test='contains($until,"/18/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    18</option>
                                                                    <option value="19">
                                                                    <xsl:if test='contains($until,"/19/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    19</option>
                                                                    <option value="20">
                                                                    <xsl:if test='contains($until,"/20/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    20</option>
                                                                    <option value="21">
                                                                    <xsl:if test='contains($until,"/21/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    21</option>
                                                                    <option value="22">
                                                                    <xsl:if test='contains($until,"/22/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    22</option>
                                                                    <option value="23">
                                                                    <xsl:if test='contains($until,"/23/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    23</option>
                                                                    <option value="24">
                                                                    <xsl:if test='contains($until,"/24/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    24</option>
                                                                    <option value="25">
                                                                    <xsl:if test='contains($until,"/25/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    25</option>
                                                                    <option value="26">
                                                                    <xsl:if test='contains($until,"/26/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    26</option>
                                                                    <option value="27">
                                                                    <xsl:if test='contains($until,"/27/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    27</option>
                                                                    <option value="28">
                                                                    <xsl:if test='contains($until,"/28/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    28</option>
                                                                    <option value="29">
                                                                    <xsl:if test='contains($until,"/29/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    29</option>
                                                                    <option value="30">
                                                                    <xsl:if test='contains($until,"/30/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    30</option>
                                                                    <option value="31">
                                                                    <xsl:if test='contains($until,"/31/")'>
                                                                        <xsl:attribute name="selected">true</xsl:attribute>
                                                                    </xsl:if>
                                                                    31</option>
                                                                </select>
                                                                <select name="year2" class="text">
																	<xsl:call-template name="full-year-options">
																		<xsl:with-param name="selected-date" select="substring-after( substring-after( $until,'/') ,'/')"/>
																	</xsl:call-template>
                                                                </select>
																<xsl:text>&#160;</xsl:text>
                                                            </td>
                                                        </tr>
                                                    </table>
<!-- END RANGE OF RECURRENCE INPUTS -->
                                                </td>
                                            </tr>
<!-- END RANGE OF RECURRENCE -->
<!-- END RECURRENCE -->
                                        </xsl:when>
<!-- EDIT EVENT -->
                                        <xsl:when test="$edit">
                                            <tr>
                                                <td class="table-light-left" align="right" valign="top" nowrap="nowrap">Recurrence Dates:</td>
                                                <xsl:choose>
                                                    <xsl:when test="$isReccurent and /calendar-system/view/@pages != '0'">
    <!--VIEW RECURRENCE -->
    
                                                        <td class="table-content-right">
                                                            <xsl:for-each select="/calendar-system/view/calendar/entry">
                                                                <xsl:variable name="ceid" select="@ceid" />
                                                                <xsl:apply-templates select="/calendar-system/calendar[@calid=$calid]/entry[@ceid=$ceid]">
                                                                    <xsl:with-param name="view-event">yes</xsl:with-param>
                                                                </xsl:apply-templates>
                                                                <br />
                                                            </xsl:for-each>
                                                            <br/>
    														<xsl:text>&#160;</xsl:text>
    <!-- PREVIOUS -->
                                                            <xsl:choose>
                                                                <xsl:when test="/calendar-system/view/@prev='true'">
                                                                    <a href="{$mdoURL}=previous&amp;p={$page}" title="Display">
                                                                        <img src="{$CONTROLS_IMAGE_PATH}/calendar_prev.gif" border="0" align="middle" alt="Previous" title="Previous" />
                                                                    </a>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_prev_disable.gif" align="middle" alt="disabled previous navigation" title="disabled previous navigation" />
                                                                </xsl:otherwise>
                                                            </xsl:choose>
    <!-- CURRENT PAGE NUMBER -->
    														<xsl:text>&#160;</xsl:text>
                                                            <font class="uportal-channel-text">
                                                            <xsl:value-of select="$page + 1" />
                                                            /
                                                            <xsl:value-of select="/calendar-system/view/@pages" />
                                                            </font>
    														<xsl:text>&#160;</xsl:text>
    <!-- NEXT -->
                                                            <xsl:choose>
                                                                <xsl:when test="/calendar-system/view/@next='true'">
                                                                    <a href="{$mdoURL}=next&amp;p={$page}" title="Display">
                                                                        <img src="{$CONTROLS_IMAGE_PATH}/calendar_next.gif" border="0" align="middle" alt="Next" title="Next" />
                                                                    </a>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_next_disable.gif" align="middle" alt="disabled next navigation" title="disabled next navigation" />
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </td>
    <!-- END RECURRENCE -->
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <td class="table-content-single">This event has no recurrence.</td>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </tr>
                                        </xsl:when>
                                    </xsl:choose>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" class="table-nav" height="22">
                                <input type="hidden" name="sid" value="{$sid}" />
                                <input type="hidden" name="back" value="{$back}" />
                                <input type="hidden" name="default" id="calendarDefaultAction" value="do~ok" />
								<input type="hidden" name="submitValue" value="OK"/>
                                <input class="uportal-button" type="submit" name="do~ok" value="OK" onclick="this.form.submitValue.value=this.value;" />
                                <input class="uportal-button" type="submit" name="go~{$back}" value="Cancel" onclick="this.form.submitValue.value=this.value;" />
                            </td>
                        </tr>
                    </table>
                </form>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
<!--template show-->
<!-- view repeat date-->
    <xsl:template match="entry">
        <xsl:param name="view-event" />
<xsl:text>
   
</xsl:text>
        <xsl:if test="/calendar-system/view/entry/@ceid = @ceid">
            <font class="uportal-channel-subtitle">
                <xsl:value-of select="concat(duration/@dow,',&#160;',substring-before(duration/@start,'_'))" />
            </font>
        </xsl:if>
        <xsl:if test="/calendar-system/view/entry/@ceid != @ceid">
            <xsl:choose>
                <xsl:when test="$view-event = 'no'">
                    <xsl:value-of select="concat(duration/@dow,',&#160;',substring-before(duration/@start,'_'))" />
                </xsl:when>
                <xsl:otherwise>
                    <a href="{$mdoURL}=viewRepeat&amp;back={$back}&amp;ceid={@ceid}&amp;calid={$calid}" title="">
                        <xsl:value-of select="concat(duration/@dow,',&#160;',substring-before(duration/@start,'_'))" />
                    </a>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
<!-- //////////////////////////////////-->
    <xsl:template name="event-deleted">
        <form method="post" action="{$baseActionURL}">
        	<!--UniAcc: Layout Table -->
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <th class="th-top">Information</th>
                </tr>
                <tr>
                    <td class="table-content-single">This event has been deleted:
                    <br />
                    <strong>
                        <em>
                            <xsl:value-of select="@title" />
                        </em>
                    </strong>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav">
                        <input type="hidden" name="sid" value="{$sid}" />
                        <input type="hidden" name="back" value="{$back}" />
                        <input class="uportal-button" type="submit" name="go~{$back}" value="Cancel" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
<!-- ////////////////////////////////////////////////////////// -->
    <xsl:template match="calendar" mode="owner">
        <xsl:param name="curCalid" />
        <xsl:choose>
            <xsl:when test="@calid=$curCalid">
                <option selected="selected" value="{@calid}">
                    <xsl:value-of select="@calname" />
                </option>
            </xsl:when>
            <xsl:otherwise>
                <option value="{@calid}">
                    <xsl:value-of select="@calname" />
                </option>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

