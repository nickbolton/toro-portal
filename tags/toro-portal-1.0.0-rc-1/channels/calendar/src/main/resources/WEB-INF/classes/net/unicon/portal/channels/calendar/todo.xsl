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
    <xsl:include href="view-todo.xsl" />
    <xsl:include href="utils.xsl" />
    <xsl:include href="date-widget.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="back">default</xsl:param>
<!-- variable-->
    <xsl:variable name="cur_user" select="/calendar-system/logon/@user" />
    <xsl:variable name="calid" select="/calendar-system/view/calendar/@calid" />
    <xsl:variable name="cal" select="/calendar-system/calendar[@calid=$calid]" />
    <xsl:variable name="notDelete" select="/calendar-system/view/entry" />
<!--body-->
    <xsl:template match="calendar-system">
        <xsl:choose>
            <xsl:when test="$notDelete">
                <xsl:apply-templates select="view/entry" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="todo-deleted" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:key name="ceid" match="/calendar-system/calendar/entry" use="@ceid" />
    <xsl:key name="access" match="/calendar-system/logon/access" use="@calid" />
    <xsl:template match="view/entry">
        <xsl:variable name="edit" select="@ceid" />
        <xsl:variable name="entry" select='key("ceid", @ceid)' />
        <xsl:variable name="title" select="normalize-space($entry/todo/text())" />
        <xsl:variable name="calright" select="key('access',$calid)" />
        <xsl:variable name="date">
            <xsl:choose>
                <xsl:when test="$edit">
                    <xsl:value-of select='substring-before($entry/duration/@start,"_")' />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@date" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="start">
            <xsl:choose>
                <xsl:when test="$edit">
                    <xsl:value-of select='substring-after($entry/duration/@start,"_")' />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select='substring-after(@date,"_")' />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="place" select="$entry/location/text()" />
        <xsl:variable name="share" select="$entry/@share" />
        <xsl:variable name="priority" select="$entry/todo/@priority" />
        <xsl:variable name="notes" select='$entry/todo/description/text()[$entry/todo/description/text()!="&lt;none&gt;"]' />
        <xsl:variable name="alarm" select="$entry/alarm" />
        <xsl:variable name="repeat" select="$entry/recurrence" />
        <xsl:variable name="isReccurent" select="contains($entry/@ceid,'.')" />
        <xsl:variable name="attendee" select="$entry/attendee" />
        <xsl:variable name="until">
            <xsl:choose>
                <xsl:when test="$edit and $repeat/@until">
                    <xsl:value-of select="$repeat/@until" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$date" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
<!--lam-->
        <xsl:call-template name="autoFormJS" />
        <xsl:choose>
            <xsl:when test="$calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true' or contains($calright/@rights,'W')">
                <form name="todoInfo" method="post" action="{$baseActionURL}" onsubmit="return(checkFormCalC(this));">
                    <input type='hidden' name="sid" value="{$sid}" />
                    <input type='hidden' name='back' value='{$back}' />
                    <!--UniAcc: Layout Table -->
                    <table cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <th class="th-top">
<!-- Title -->
                                <xsl:choose>
                                    <xsl:when test="$edit">
                                        <xsl:if test="$calid=$logon-user or substring-before($calid,':')=$logon-user or $user-share-write='true' or $everyone-share-write='true'">
											Edit Task
                                        </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="not($edit)">
										New Task
                                    </xsl:when>
                                </xsl:choose>
<!--END TITLE-->
                            </th>
                            <th colspan="2" class="th">Event Recurrence</th>
                        </tr>
                        <tr>
<!-- detail todo -->
                            <td class="uportal-background-light" valign="top">
                            	<!--UniAcc: Layout Table -->
                                <table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-channel-text">
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CT-CalenderS1">Calendar</label>
                                        </td>
                                        <td class="table-content-right" align="left">
<!-- Combo Calendars -->
                                            <select name="calid" size="1" class="text" id="CT-CalenderS1">
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
                                                            <xsl:variable name="right" select="key('access',@calid)" />
                                                            <xsl:if test="contains($right/@rights,'W')">
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
                                        	<label for="CT-TitleT1">Title</label>
                                        </td>
                                        <td class="table-content-right" align="left" width="100%">
                                            <input type="text" name="todo" size="27" class="text" id="CT-TitleT1">
                                                <xsl:if test="$edit">
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="$title" />
                                                    </xsl:attribute>
                                                </xsl:if>
                                            </input>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CT-DueS1">Due</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <select name="month" class="text" id="CT-DueS1">
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
                                            <select name="year" class="input-text">
                                                <!-- $date contains a '_' when creating a new todo, but does not when editting one. -->
                                                <xsl:choose>
                                                    <xsl:when test="contains($date, '_')">
                                                        <xsl:call-template name="full-year-options">
                                                            <xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($date, '_') ,'/') ,'/')"/>
                                                        </xsl:call-template>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:call-template name="full-year-options">
                                                            <xsl:with-param name="selected-date" select="substring-after( substring-after($date,'/') ,'/')"/>
                                                        </xsl:call-template>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CT-AtS1">At</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <select name="hour" class="text" id="CT-AtS1">
                                                <option value="0">
                                                <xsl:if test='starts-with($start,"0:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                12 am</option>
                                                <option value="1">
                                                <xsl:if test='starts-with($start,"1:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                1 am</option>
                                                <option value="2">
                                                <xsl:if test='starts-with($start,"2:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                2 am</option>
                                                <option value="3">
                                                <xsl:if test='starts-with($start,"3:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                3 am</option>
                                                <option value="4">
                                                <xsl:if test='starts-with($start,"4:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                4 am</option>
                                                <option value="5">
                                                <xsl:if test='starts-with($start,"5:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                5 am</option>
                                                <option value="6">
                                                <xsl:if test='starts-with($start,"6:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                6 am</option>
                                                <option value="7">
                                                <xsl:if test='starts-with($start,"7:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                7 am</option>
                                                <option value="8">
                                                <xsl:if test='starts-with($start,"8:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                8 am</option>
                                                <option value="9">
                                                <xsl:if test='starts-with($start,"9:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                9 am</option>
                                                <option value="10">
                                                <xsl:if test='starts-with($start,"10:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                10 am</option>
                                                <option value="11">
                                                <xsl:if test='starts-with($start,"11:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                11 am</option>
                                                <option value="12">
                                                <xsl:if test='starts-with($start,"12:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                12 pm</option>
                                                <option value="13">
                                                <xsl:if test='starts-with($start,"13:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                1 pm</option>
                                                <option value="14">
                                                <xsl:if test='starts-with($start,"14:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                2 pm</option>
                                                <option value="15">
                                                <xsl:if test='starts-with($start,"15:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                3 pm</option>
                                                <option value="16">
                                                <xsl:if test='starts-with($start,"16:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                4 pm</option>
                                                <option value="17">
                                                <xsl:if test='starts-with($start,"17:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                5 pm</option>
                                                <option value="18">
                                                <xsl:if test='starts-with($start,"18:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                6 pm</option>
                                                <option value="19">
                                                <xsl:if test='starts-with($start,"19:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                7 pm</option>
                                                <option value="20">
                                                <xsl:if test='starts-with($start,"20:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                8 pm</option>
                                                <option value="21">
                                                <xsl:if test='starts-with($start,"21:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                9 pm</option>
                                                <option value="22">
                                                <xsl:if test='starts-with($start,"22:")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                10 pm</option>
                                                <option value="23">
                                                <xsl:if test='starts-with($start,"23:")'>
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
                                                <option value="15">
                                                <xsl:if test='contains($start,":15")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                :15</option>
                                                <option value="30">
                                                <xsl:if test='contains($start,":30")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                :30</option>
                                                <option value="45">
                                                <xsl:if test='contains($start,":45")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                :45</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="table-light-left" align="right">
                                        	<label for="CT-PriorityS1">Priority</label>
                                        </td>
                                        <td class="table-content-right" align="left">
                                            <select name="priority" class="text" id="CT-PriorityS1">
                                                <option value="0">
                                                <xsl:if test='$edit and contains($priority,"0")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Very Low</option>
                                                <option value="3">
                                                <xsl:if test='$edit and contains($priority,"3")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Low</option>
                                                <option value="5">
                                                <xsl:if test='$edit and contains($priority,"5") or (not($priority) and not($edit))'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Normal</option>
                                                <option value="7">
                                                <xsl:if test='$edit and contains($priority,"7")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                High</option>
                                                <option value="9">
                                                <xsl:if test='$edit and contains($priority,"9")'>
                                                    <xsl:attribute name="selected">true</xsl:attribute>
                                                </xsl:if>
                                                Very High</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
<!-- notes -->
                                        <td class="table-light-left" align="right">
                                        	<label for="CT-NotesTA1">Notes</label>
                                        </td>
                                        <td class="table-content-right" align="left">
											<input type="text" name="description" size="27" maxlength="500" class="text" id="CEvents-NotesTA1">
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="string($notes)" />
                                                </xsl:attribute>
                                            </input>
                                            <!-- <textarea name="description" wrap="virtual" rows="3" cols="25" class="uportal-input-text" id="CT-NotesTA1">
                                                <xsl:value-of select="string($notes)" />
                                            </textarea> -->
                                        </td>
                                    </tr>
                                    <tr>
<!--Completion -->
                                        <td class="table-light-left-bottom" align="right">
                                        	<label for="CT-CompletedC1">Completed</label>
                                        </td>
                                        <td class="table-content-right-bottom" align="left">
                                            <input id="CT-CompletedC1">
                                                <xsl:attribute name="type">checkbox</xsl:attribute>
                                                <xsl:attribute name="name">complete</xsl:attribute>
                                                <xsl:if test="$entry/todo/completion/@completed">
                                                    <xsl:attribute name="checked">true</xsl:attribute>
                                                </xsl:if>
                                            </input>
                                        </td>
                                    </tr>
                                </table>
                            </td>
<!-- Repeating Options -->
                            <td valign="top" class="uportal-background-light">
                            	<!--UniAcc: Layout Table -->
                                <table cellpadding="0" cellspacing="0" border="0" class="uportal-text" width="100%">
<!--repeating-->
                                    <xsl:choose>
<!-- new todo -->
                                        <xsl:when test="not($edit)">                       
                                            <tr>
                                            	<td class="table-light-left" align="right" valign="top">
                                            		Recurrence<br />Pattern
                                            	</td>
                                                <td class="table-content-right" height="100%">
<!-- RECURRENCE PATTERN INPUTS -->
													<!--UniAcc: Layout Table -->
                                            		<table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-text">
                                            			<tr>
			                                                <td>
			                                                    <input type="radio" class="radio" name="freq" value="0" checked="true" id="CT-RepeatNoneR1"/>
			                                                </td>
			                                                <td>
			                                                	<label for="CT-RepeatNoneR1">None</label>
			                                                </td>
                                                		</tr>
                                                		<tr>
			                                                <td>
			                                                    <input type="radio" class="radio" name="freq" value="1" id="CT-RepeatEveryR1" />
			                                                </td>
			                                                <td>
			                                                	<label for="CT-RepeatEveryR1">Every&#160;</label>
				                                                <input type="text" size="2" name="days" class="text" maxlength="2" value="1" id="CT-RepeatDaysT1"/>
			                                                	<label for="CT-RepeatDaysT1">&#160;day(s)</label>
			                                                </td>
			                                            </tr>
			                                            <tr>
			                                                <td nowrap='nowrap'>
			                                                    <input type="radio" class="radio" name="freq" value="2" id="CT-RecurrWeeksR1"/>
			                                                </td>
			                                                <td nowrap='nowrap'>
			                                                	<label for="CT-RecurrWeeksR1">Every&#160;</label>
				                                                <input type="text" size="2" name="weeks" class="text" maxlength="2" value="1" id="CT-RecurrWeeksT1"/>
				                                                <label for="CT-RecurrWeeksT1">&#160;week(s),&#160;on</label>
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
			                                                            <input type="checkbox" class="radio" name="mon" id="CT-RecurrMonC1" />
			                                                            	<label for="CT-RecurrMonC1">&#160;Mon<img src="{$SPACER}" border="0" width="5" alt="monday" title="monday" /></label>
			                                                            </td>
			                                                            <td>
			                                                            <input type="checkbox" class="radio" name="tue" id="CT-RecurrTueC1" />
			                                                            	<label for="CT-RecurrTueC1">&#160;Tue<img src="{$SPACER}" border="0" width="5" alt="tuesday" title="tuesday" /></label>
			                                                            </td>
			                                                            <td>
			                                                            <input type="checkbox" class="radio" name="wed" id="CT-RecurrWedC1" />
			                                                            	<label for="CT-RecurrWedC1">&#160;Wed<img src="{$SPACER}" border="0" width="5" alt="wednesday" title="wednesday" /></label>
			                                                            </td>
			                                                        </tr>
			                                                        <tr>
			                                                            <td>
			                                                            <input type="checkbox" class="radio" name="thu" id="CT-RecurrThuC1" />
			                                                            	<label for="CT-RecurrThuC1">&#160;Thu<img src="{$SPACER}" border="0" width="5" alt="thursday" title="thursday" /></label>
			                                                            </td>
			                                                            <td>
			                                                            <input type="checkbox" class="radio" name="fri" id="CT-RecurrFriC1" />
			                                                            	<label for="CT-RecurrFriC1">&#160;Fri<img src="{$SPACER}" border="0" width="5" alt="friday" title="friday" /></label>
			                                                            </td>
			                                                            <td>
			                                                            <input type="checkbox" class="radio" name="sat" id="CT-RecurrSatC1" />
			                                                            	<label for="CT-RecurrSatC1">&#160;Sat<img src="{$SPACER}" border="0" width="5" alt="saturday" title="saturday" /></label>
			                                                            </td>
			                                                        </tr>
			                                                        <tr>
			                                                            <td />
			                                                            <td colspan="3">
			                                                            <input type="checkbox" class="radio" name="sun" id="CT-RecurrSunC1" />
			                                                            	<label for="CT-RecurrSunC1">&#160;Sun<img src="{$SPACER}" border="0" width="5" alt="sunday" title="sunday" /></label>
			                                                            </td>
			                                                        </tr>
			                                                    </table>
			<!-- END DAY OF WEEK RECURRENCE -->
			                                                </td>
			                                            </tr>
			                                            <tr>
			                                                <td nowrap='nowrap'>
			                                                    <input type="radio" class="radio" name="freq" value="3" id="CT-RecurrMonthsR1"/>
			                                                </td>
			                                                <td nowrap='nowrap'>
			                                                	<label for="CT-RecurrMonthsR1">Every&#160;</label>
				                                                <input type="text" size="2" name="months" class="text" maxlength="2" value="1" id="CT-RecurrMonthsT1"/>
				                                                <label for="CT-RecurrMonthsT1">&#160;month(s)</label>
			                                                </td>
			                                            </tr>
			                                            <tr>
			                                                <td>
			                                                    <input type="radio" class="radio" name="freq" value="4" id="CT-RecurrYearR1"/>
			                                                </td>
			                                                <td>
			                                                	<label for="CT-RecurrYearR1">Every year</label>
			                                                </td>
			                                            </tr>
                                                	</table>
                                                </td>
                                            </tr>
                                            <tr>
                                            	<td class="table-light-left-bottom" align="right" valign="top">
                                            		Range of<br />Recurrence
                                            	</td>                                            	
                                                <td class="table-content-right-bottom">
                                                	<!--UniAcc: Layout Table -->
                                                    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="uportal-text">
                                                        <tr>
                                                        	<td nowrap='nowrap'>
			                                                    <input type="radio" class="radio" name="until" value="0" id="CT-RangeAfterR1">
			                                                        <xsl:if test="$repeat/@count">
			                                                            <xsl:attribute name="checked">true</xsl:attribute>
			                                                        </xsl:if>
			                                                    </input>
			                                                </td>
			                                                <td colspan="3" nowrap='nowrap'>
			                                                	<label for="CT-RangeAfterR1">After&#160;</label>
			                                                <input type="text" size="2" name="times" class="text" maxlength="2" value="1" id="CT-RangeAfterT1"/>
			                                                	<label for="CT-RangeAfterT1">&#160;time(s)</label>
			                                                </td>
			                                            </tr>
			                                            <tr>
			                                                <td>
			                                                    <input type="radio" class="radio" name="until" value="1" nowrap="nowrap" id="CT-RangeByR1">
			                                                        <xsl:if test="$repeat/@until or not($repeat)">
			                                                            <xsl:attribute name="checked">true</xsl:attribute>
			                                                        </xsl:if>
			                                                    </input>
			                                                </td>
			                                                <td colspan="3" nowrap='nowrap'>
			                                                	<label for="CT-RangeByR1">By&#160;</label>
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
																		<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($until,'_') ,'/') ,'/')"/>
																	</xsl:call-template>
				                                                </select>
			                                                </td>
                                                        </tr>
													</table>
                                                </td>
                                            </tr>
                                        </xsl:when>
<!-- edit Todo -->
                                        <xsl:when test="$edit">
                                            <xsl:if test="$isReccurent and /calendar-system/view/@pages != '0'">
                                                <tr>
                                                    <td colspan="2">&#160;&#160;Repeating</td>
                                                </tr>
<!--view  repeating -->
                                                <xsl:for-each select="/calendar-system/view/calendar/entry">
                                                    <tr>
                                                        <td colspan="2">
                                                            <xsl:apply-templates select='key("ceid", @ceid)'>
                                                                <xsl:with-param name="view-todo">yes</xsl:with-param>
                                                            </xsl:apply-templates>
                                                        </td>
                                                    </tr>
                                                </xsl:for-each>
                                                <tr>
                                                    <td colspan='2'>
<!-- Previous -->
                                                        <xsl:choose>
                                                            <xsl:when test="/calendar-system/view/@prev='true'">
                                                                <a href="{$mdoURL}=previous&amp;p={/calendar-system/view/@page}">
                                                                    <img src="{$baseImagePath}/prev_12.gif" border="0" align="middle" alt="Previous" title="Previous" />
                                                                </a>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <img border="0" src="{$baseImagePath}/prev_disabled_12.gif" align="middle" alt="" title="" />
                                                            </xsl:otherwise>
                                                        </xsl:choose>
<!-- current page number -->
                                                        <font class='uportal-channel-text'>
                                                        <xsl:value-of select="/calendar-system/view/@page + 1" />
                                                        /
                                                        <xsl:value-of select="/calendar-system/view/@pages" />
                                                        </font>
<!-- Next -->
                                                        <xsl:choose>
                                                            <xsl:when test="/calendar-system/view/@next='true'">
                                                                <a href="{$mdoURL}=next&amp;p={/calendar-system/view/@page}">
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
                                        </xsl:when>
                                    </xsl:choose>
                                </table>
                            </td>
                        </tr>
                        <tr>
<!-- submit bottom -->
                            <td colspan="3" align="left" class="table-nav" height="22">
                                <input type="hidden" name="default" value="do~ok" />
								<input type="hidden" name="submitValue" value="OK"/>
                                <input class="uportal-button" type="submit" name="do~ok" value="OK" onclick="this.form.submitValue.value=this.value;" />
                                <input class="uportal-button" type="submit" name="go~{$back}&amp;uP_root=me" value="Cancel" onclick="this.form.submitValue.value=this.value;" />
                            </td>
                        </tr>
                    </table>
                </form>
            </xsl:when>
            <xsl:otherwise>
                <form method="post" action="{$baseActionURL}">
                    <xsl:call-template name="view-todo">
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="entry" select="$entry" />
                        <xsl:with-param name="title" select="$title" />
                        <xsl:with-param name="date" select="$date" />
                        <xsl:with-param name="start" select="$start" />
                        <xsl:with-param name="place" select="$place" />
                        <xsl:with-param name="share" select="$share" />
                        <xsl:with-param name="priority" select="$priority" />
                        <xsl:with-param name="notes" select="$notes" />
                        <xsl:with-param name="alarm" select="$alarm" />
                        <xsl:with-param name="repeat" select="$repeat" />
                        <xsl:with-param name="isReccurent" select="$isReccurent" />
                        <xsl:with-param name="attendee" select="$attendee" />
                        <xsl:with-param name="until" select="$until" />
                    </xsl:call-template>
                </form>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
<!-- view repeat date-->
    <xsl:template match="entry">
        <xsl:param name="view-todo" />
        <xsl:if test="/calendar-system/view/entry/@ceid = @ceid">
            <font class="uportal-channel-subtitle">
                <xsl:value-of select="concat(duration/@dow,',&#160;',substring-before(duration/@start,'_'))" />
            </font>
        </xsl:if>
        <xsl:if test="/calendar-system/view/entry/@ceid != @ceid">
            <xsl:choose>
                <xsl:when test="$view-todo = 'no'">
                    <xsl:value-of select="concat(duration/@dow,',&#160;',substring-before(duration/@start,'_'))" />
                </xsl:when>
                <xsl:otherwise>
                    <a href="{$mdoURL}=viewRepeat&amp;back={$back}&amp;ceid={@ceid}&amp;calid={$calid}">
                        <xsl:value-of select="concat(duration/@dow,',&#160;',substring-before(duration/@start,'_'))" />
                    </a>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
<!-- //////////////////////////////-->
    <xsl:template name="todo-deleted">
        <form method="post" action="{$baseActionURL}">
        	<!--UniAcc: Layout Table -->
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <th class="th-top">Information</th>
                </tr>
                <tr>
                    <td class="table-content-single">This Task has been deleted:
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
                        <input class="uportal-button" type="submit" name="go~{$back}&amp;uP_root=me" value="Cancel" />
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

