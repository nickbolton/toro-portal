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
    <xsl:template name="mini-event">
        <xsl:param name="mini-daily" />
        <xsl:variable name="calid-add-event">
<!-- If all-calendars we add to personal calendar-->
            <xsl:choose>
                <xsl:when test="$calid='all-calendars' or not($calid)">
                    <xsl:value-of select="$logon-user" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$calid" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="date">
            <xsl:value-of select="$mini-daily/@date" />
        </xsl:variable>
        <xsl:variable name="hour">8</xsl:variable>
        <xsl:variable name="minute">0</xsl:variable>
        <xsl:variable name="hours">1</xsl:variable>
        <xsl:variable name="minutes">0</xsl:variable>
<!-- /////////////////////////////////////////////// -->
        <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <form method="post" action="{$baseActionURL}">
                <input type="hidden" name="sid">
                    <xsl:attribute name="value">
                        <xsl:value-of select="$sid" />
                    </xsl:attribute>
                </input>
                <input type="hidden" name="calid">
                    <xsl:attribute name="value">
                        <xsl:value-of select="$calid-add-event" />
                    </xsl:attribute>
                </input>
                <tr>
                    <th class="th-top" colspan="2">Quick Event Add</th>
                </tr>
                <tr>
                    <td class="table-light-left" align="right">Title</td>
                    <td class="table-content-right">
                        <input type="text" name="event" size="18" class="text" />
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" align="right" nowrap="nowrap">Date</td>
                    <td class="table-content-right" colspan="2" nowrap="nowrap">
                        <select name="month" class="text">
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
							<xsl:call-template name="year-options">
								<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($date,'_') ,'/') ,'/')"/>
							</xsl:call-template>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" align="right" width="43" nowrap="nowrap">Start</td>
                    <td class="table-content-right">
                        <select name="hour" class="text">
                            <option value="0">
                            <xsl:if test="$hour=0">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            12 am</option>
                            <option value="1">
                            <xsl:if test="$hour=1">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            1 am</option>
                            <option value="2">
                            <xsl:if test="$hour=2">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            2 am</option>
                            <option value="3">
                            <xsl:if test="$hour=3">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            3 am</option>
                            <option value="4">
                            <xsl:if test="$hour=4">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            4 am</option>
                            <option value="5">
                            <xsl:if test="$hour=5">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            5 am</option>
                            <option value="6">
                            <xsl:if test="$hour=6">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            6 am</option>
                            <option value="7">
                            <xsl:if test="$hour=7">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            7 am</option>
                            <option value="8">
                            <xsl:if test="$hour=8">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            8 am</option>
                            <option value="9">
                            <xsl:if test="$hour=9">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            9 am</option>
                            <option value="10">
                            <xsl:if test="$hour=10">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            10 am</option>
                            <option value="11">
                            <xsl:if test="$hour=11">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            11 am</option>
                            <option value="12">
                            <xsl:if test="$hour=12">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            12 pm</option>
                            <option value="13">
                            <xsl:if test="$hour=13">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            1 pm</option>
                            <option value="14">
                            <xsl:if test="$hour=14">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            2 pm</option>
                            <option value="15">
                            <xsl:if test="$hour=15">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            3 pm</option>
                            <option value="16">
                            <xsl:if test="$hour=16">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            4 pm</option>
                            <option value="17">
                            <xsl:if test="$hour=17">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            5 pm</option>
                            <option value="18">
                            <xsl:if test="$hour=18">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            6 pm</option>
                            <option value="19">
                            <xsl:if test="$hour=19">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            7 pm</option>
                            <option value="20">
                            <xsl:if test="$hour=20">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            8 pm</option>
                            <option value="21">
                            <xsl:if test="$hour=21">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            9 pm</option>
                            <option value="22">
                            <xsl:if test="$hour=22">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            10 pm</option>
                            <option value="23">
                            <xsl:if test="$hour=23">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            11 pm</option>
                        </select>
                        <select name="minute" class="text">
                            <option value="0">
                            <xsl:if test="$minute=0">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :00</option>
                            <option value="15">
                            <xsl:if test="$minute=15">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :15</option>
                            <option value="30">
                            <xsl:if test="$minute=30">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :30</option>
                            <option value="45">
                            <xsl:if test="$minute=45">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :45</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" align="right" width="43" nowrap="nowrap">Length</td>
                    <td class="table-content-right">
                        <select name="hours" class="text">
                            <option value="0">
                            <xsl:if test="$hours=0">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            0</option>
                            <option value="1">
                            <xsl:if test="$hours=1">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            1</option>
                            <option value="2">
                            <xsl:if test="$hours=2">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            2</option>
                            <option value="3">
                            <xsl:if test="$hours=3">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            3</option>
                            <option value="4">
                            <xsl:if test="$hours=4">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            4</option>
                            <option value="5">
                            <xsl:if test="$hours=5">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            5</option>
                            <option value="6">
                            <xsl:if test="$hours=6">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            6</option>
                            <option value="7">
                            <xsl:if test="$hours=7">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            7</option>
                            <option value="8">
                            <xsl:if test="$hours=8">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            8</option>
                            <option value="9">
                            <xsl:if test="$hours=9">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            9</option>
                            <option value="10">
                            <xsl:if test="$hours=10">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            10</option>
                            <option value="11">
                            <xsl:if test="$hours=11">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            11</option>
                            <option value="12">
                            <xsl:if test="$hours=12">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            12</option>
                        </select>
                        <select name="minutes" class="text">
                            <option value="0">
                            <xsl:if test="$minutes=0">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :00</option>
                            <option value="15">
                            <xsl:if test="$minutes=15">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :15</option>
                            <option value="30">
                            <xsl:if test="$minutes=30">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :30</option>
                            <option value="45">
                            <xsl:if test="$minutes=45">
                                <xsl:attribute name="selected">true</xsl:attribute>
                            </xsl:if>
                            :45</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav" colspan="2" align="left">
                        <input type="hidden" name="default" value="do~addEvent" />
                        <input class="uportal-button" type="submit" name="do~addEvent" value="Add" />
                    </td>
                </tr>
            </form>
        </table>
    </xsl:template>
</xsl:stylesheet>

