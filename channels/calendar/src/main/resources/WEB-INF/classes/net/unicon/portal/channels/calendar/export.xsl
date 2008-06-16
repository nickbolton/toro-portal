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
    <xsl:param name="window">export</xsl:param>
    <xsl:include href="common.xsl" />
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="access-detail.xsl" />
    <xsl:include href="date-widget.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="main" />
    <xsl:param name="date">default</xsl:param>
<!-- used only for navigation-bar -->
    <xsl:template match="/">
<!-- Navigation bar -->
        <xsl:call-template name="links" />
        <div class="bounding-box2">
			<form action="{$resourceURL}" method="post" name="exportForm" target="hidden_download">
				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<input type="hidden" name="sid" value="{$sid}" />
					<input type="hidden" name="targetChannel" value="{$targetChannel}" />
					<tr>
						<td class="table-light-left" align="right">
							<label for="CEx-CalenderS1">Calendar</label>
						</td>
						<td class="table-content-right" align="left">
		<!-- Combo Calendars -->
							<select name="calid" size="1" class="text" id="CEx-CalenderS1">
		<!--<option value='composite-cal'>Composite Calendar</option>-->
								<xsl:for-each select="//calendar-system/preference/calendar-group">
									<option value="composite_{@id}">
									<xsl:value-of select="@name" />
									&#160;composite view</option>
								</xsl:for-each>
		<!-- ////////////////////////////////////////////// -->
		<!-- own calendars -->
								<xsl:apply-templates select="//calendar[@owner=$cur_user]" mode="owner">
									<xsl:with-param name="curCalid" select="//calendar-system/view/calendar/@calid" />
									<xsl:sort select="@calname" />
								</xsl:apply-templates>
		<!-- ////////////////////////////////////////////// -->
		<!-- shared calendars -->
								<xsl:apply-templates select="//calendar[@owner!=$cur_user]" mode="shared">
									<xsl:with-param name="curCalid" select="//calendar-system/view/calendar/@calid" />
									<xsl:sort select="@owner" />
								</xsl:apply-templates>
							</select>
							&#160;
							<input type='radio' name="format" value="EVENT" checked="checked" id="CEx-EventsR1"/>
							<label for="CEx-EventsR1">Events (tab delimited)&#160;</label>
							<input type='radio' name="format" value="TODO" id="CEx-TodosR1"/>
							<label for="CEx-TodosR1">Tasks (tab delimited) &#160;</label>
							<input type='radio' name="format" value="ical" id="CEx-icalR1"/>
							<label for="CEx-icalR1">ICalendar(iCal) for Palm (only single calendar)</label>
						</td>
					</tr>
					<tr>
						<td class="table-light-left" align="right">
							<label for="CEx-StartS1">Start Date</label>
						</td>
						<td class="table-content-right" align="left">
							<select name="month" class="text" id="CEx-StartS1">
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
									<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($date,'_') ,'/') ,'/')"/>
								</xsl:call-template>
							</select>
						</td>
					</tr>
					<tr>
						<td class="table-light-left" align="right">
							<label for="CEx-EndS1">End Date</label>
						</td>
						<td class="table-content-right" align="left">
							<select name="endmonth" class="text" id="CEx-EndS1">
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
							<select name="endday" class="text">
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
							<select name="endyear" class="text">
								<xsl:call-template name="full-year-options">
									<xsl:with-param name="selected-date" select="substring-after( substring-after( substring-before($date,'_') ,'/') ,'/')"/>
								</xsl:call-template>
							</select>
						</td>
					</tr>
					<tr>
						<td valign="top" width="40%" colspan="2" class="table-nav" style="text-align:center">
							<input type="hidden" name="from" value="" />
							<input type="hidden" name="to" value="" />
							<input type="submit" name="Export" value="Export" class="uportal-button" />
                        	<input type="button" name="Cancel" value="Cancel" class="uportal-button" onclick="location.href='{$baseActionURL}?uP_root=root'" />							
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>
</xsl:stylesheet>

