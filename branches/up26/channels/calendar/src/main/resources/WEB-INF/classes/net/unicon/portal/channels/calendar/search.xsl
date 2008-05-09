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
    <xsl:param name="window">search</xsl:param>
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="access-detail.xsl" />
    <xsl:include href="utils.xsl" />
    <xsl:include href="date-widget.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:key name="ceid" match="/calendar-system/calendar/entry" use="@ceid" />
    <xsl:key name="access" match="/calendar-system/logon/access" use="@calid" />
    <xsl:param name="main" />
<!-- used only for navigation-bar -->
	
	
	
<!--variable/params-->
    <xsl:template match="calendar-system">
        <xsl:variable name="from" select="view/search-event/@start" />
        <xsl:variable name="to" select="view/search-event/@end" />
<!-- Navigation bar -->
<!--    <xsl:call-template name='calendar-navigation-bar'/>-->
<!-- Search input and output -->
<!--
    <form method="post" action="{$mdoURL}=find">
    -->
	    <xsl:call-template name="autoFormJS"/>
        <xsl:call-template name="links" />
        
        <div class="bounding-box2">
			<form name="calendarSearchForm" method="post" action="{$baseActionURL}" onSubmit="return validator.applyFormRules(this, new CalendarRulesObject());">
				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<!-- Uncomment to view XML data	-->		
	<!--	<textarea name="AllXML" rows="15" cols="120"><xsl:copy-of select="."/></textarea> -->
	<!-- End Uncomment to view XML data -->				
	<!-- Search input form -->
					<div align="center">
						<tr>
							<td align="right" class="table-light-left">
								<label for="CS-FindT1">Find</label>
							</td>
							<td align="left" nowrap="nowrap" class="table-content-right">
		<!-- Text Box for search string -->
								<font class='uportal-channel-text'>
								<input type="text" name="search-string" size="30" class="uportal-input-text" id="CS-FindT1">
									<xsl:attribute name="value">
										<xsl:value-of select="view/search-event/@search-str" />
									</xsl:attribute>
								</input>
								&#160;in</font>
		<!-- Checkbox Title -->
								<input type="checkbox" name="titles" id="CS-TitleC1">
									<xsl:if test='view/search-event/@title="true"'>
										<xsl:attribute name="checked" />
									</xsl:if>
								</input>
								<label for="CS-TitleC1">Title</label>
		<!-- Checkbox Places -->
								<input type="checkbox" name="places" id="CS-PlaceC1">
									<xsl:if test='view/search-event/@places="true"'>
										<xsl:attribute name="checked" />
									</xsl:if>
								</input>
								<label for="CS-PlaceC1">Place</label>
		<!-- Checkbox Notes -->
								<input type="checkbox" name="notes" id="CS-NotesC1">
									<xsl:if test='view/search-event/@notes="true"'>
										<xsl:attribute name="checked" />
									</xsl:if>
								</input>
								<label for="CS-NotesC1">Notes</label>
							</td>
						</tr>
						<tr>
	<!-- Combo Category -->
							<td align="right" class="table-light-left">
								<label for="CS-CategoryS1">Category</label>
							</td>
							<td align="left" class="table-content-right">
								<xsl:call-template name="category">
									<xsl:with-param name="category">
										<xsl:value-of select="view/search-event/@category" />
									</xsl:with-param>
								</xsl:call-template>
							</td>
						</tr>
						<tr>
	<!-- From and To TextFields -->
							<td align="right" class="table-light-left">
								<label for="CS-FromS1">From</label>
							</td>
							<td align="left" nowrap="nowrap" class="table-content-right">
								<select name="month1" class="uportal-input-text" id="CS-FromS1">
									<option value="1">
									<xsl:if test='starts-with($from,"1/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Jan</option>
									<option value="2">
									<xsl:if test='starts-with($from,"2/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Feb</option>
									<option value="3">
									<xsl:if test='starts-with($from,"3/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Mar</option>
									<option value="4">
									<xsl:if test='starts-with($from,"4/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Apr</option>
									<option value="5">
									<xsl:if test='starts-with($from,"5/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									May</option>
									<option value="6">
									<xsl:if test='starts-with($from,"6/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Jun</option>
									<option value="7">
									<xsl:if test='starts-with($from,"7/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Jul</option>
									<option value="8">
									<xsl:if test='starts-with($from,"8/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Aug</option>
									<option value="9">
									<xsl:if test='starts-with($from,"9/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Sep</option>
									<option value="10">
									<xsl:if test='starts-with($from,"10/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Oct</option>
									<option value="11">
									<xsl:if test='starts-with($from,"11/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Nov</option>
									<option value="12">
									<xsl:if test='starts-with($from,"12/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Dec</option>
								</select>
								<select name="day1" class="uportal-input-text">
									<option value="1">
									<xsl:if test='contains($from,"/1/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									1</option>
									<option value="2">
									<xsl:if test='contains($from,"/2/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									2</option>
									<option value="3">
									<xsl:if test='contains($from,"/3/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									3</option>
									<option value="4">
									<xsl:if test='contains($from,"/4/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									4</option>
									<option value="5">
									<xsl:if test='contains($from,"/5/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									5</option>
									<option value="6">
									<xsl:if test='contains($from,"/6/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									6</option>
									<option value="7">
									<xsl:if test='contains($from,"/7/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									7</option>
									<option value="8">
									<xsl:if test='contains($from,"/8/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									8</option>
									<option value="9">
									<xsl:if test='contains($from,"/9/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									9</option>
									<option value="10">
									<xsl:if test='contains($from,"/10/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									10</option>
									<option value="11">
									<xsl:if test='contains($from,"/11/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									11</option>
									<option value="12">
									<xsl:if test='contains($from,"/12/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									12</option>
									<option value="13">
									<xsl:if test='contains($from,"/13/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									13</option>
									<option value="14">
									<xsl:if test='contains($from,"/14/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									14</option>
									<option value="15">
									<xsl:if test='contains($from,"/15/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									15</option>
									<option value="16">
									<xsl:if test='contains($from,"/16/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									16</option>
									<option value="17">
									<xsl:if test='contains($from,"/17/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									17</option>
									<option value="18">
									<xsl:if test='contains($from,"/18/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									18</option>
									<option value="19">
									<xsl:if test='contains($from,"/19/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									19</option>
									<option value="20">
									<xsl:if test='contains($from,"/20/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									20</option>
									<option value="21">
									<xsl:if test='contains($from,"/21/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									21</option>
									<option value="22">
									<xsl:if test='contains($from,"/22/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									22</option>
									<option value="23">
									<xsl:if test='contains($from,"/23/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									23</option>
									<option value="24">
									<xsl:if test='contains($from,"/24/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									24</option>
									<option value="25">
									<xsl:if test='contains($from,"/25/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									25</option>
									<option value="26">
									<xsl:if test='contains($from,"/26/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									26</option>
									<option value="27">
									<xsl:if test='contains($from,"/27/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									27</option>
									<option value="28">
									<xsl:if test='contains($from,"/28/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									28</option>
									<option value="29">
									<xsl:if test='contains($from,"/29/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									29</option>
									<option value="30">
									<xsl:if test='contains($from,"/30/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									30</option>
									<option value="31">
									<xsl:if test='contains($from,"/31/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									31</option>
								</select>
								<select name="year1" class="uportal-input-text">
									<xsl:call-template name="full-year-options">
										<xsl:with-param name="selected-date" select="substring-after( substring-after( $from,'/') ,'/20')"/>
									</xsl:call-template>
								</select>
								<label for="CS-ToS1">to</label>
								<select name="month2" class="uportal-input-text" id="CS-ToS1">
									<option value="1">
									<xsl:if test='starts-with($to,"1/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Jan</option>
									<option value="2">
									<xsl:if test='starts-with($to,"2/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Feb</option>
									<option value="3">
									<xsl:if test='starts-with($to,"3/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Mar</option>
									<option value="4">
									<xsl:if test='starts-with($to,"4/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Apr</option>
									<option value="5">
									<xsl:if test='starts-with($to,"5/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									May</option>
									<option value="6">
									<xsl:if test='starts-with($to,"6/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Jun</option>
									<option value="7">
									<xsl:if test='starts-with($to,"7/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Jul</option>
									<option value="8">
									<xsl:if test='starts-with($to,"8/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Aug</option>
									<option value="9">
									<xsl:if test='starts-with($to,"9/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Sep</option>
									<option value="10">
									<xsl:if test='starts-with($to,"10/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Oct</option>
									<option value="11">
									<xsl:if test='starts-with($to,"11/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Nov</option>
									<option value="12">
									<xsl:if test='starts-with($to,"12/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									Dec</option>
								</select>
								<select name="day2" class="uportal-input-text">
									<option value="1">
									<xsl:if test='contains($to,"/1/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									1</option>
									<option value="2">
									<xsl:if test='contains($to,"/2/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									2</option>
									<option value="3">
									<xsl:if test='contains($to,"/3/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									3</option>
									<option value="4">
									<xsl:if test='contains($to,"/4/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									4</option>
									<option value="5">
									<xsl:if test='contains($to,"/5/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									5</option>
									<option value="6">
									<xsl:if test='contains($to,"/6/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									6</option>
									<option value="7">
									<xsl:if test='contains($to,"/7/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									7</option>
									<option value="8">
									<xsl:if test='contains($to,"/8/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									8</option>
									<option value="9">
									<xsl:if test='contains($to,"/9/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									9</option>
									<option value="10">
									<xsl:if test='contains($to,"/10/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									10</option>
									<option value="11">
									<xsl:if test='contains($to,"/11/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									11</option>
									<option value="12">
									<xsl:if test='contains($to,"/12/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									12</option>
									<option value="13">
									<xsl:if test='contains($to,"/13/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									13</option>
									<option value="14">
									<xsl:if test='contains($to,"/14/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									14</option>
									<option value="15">
									<xsl:if test='contains($to,"/15/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									15</option>
									<option value="16">
									<xsl:if test='contains($to,"/16/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									16</option>
									<option value="17">
									<xsl:if test='contains($to,"/17/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									17</option>
									<option value="18">
									<xsl:if test='contains($to,"/18/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									18</option>
									<option value="19">
									<xsl:if test='contains($to,"/19/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									19</option>
									<option value="20">
									<xsl:if test='contains($to,"/20/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									20</option>
									<option value="21">
									<xsl:if test='contains($to,"/21/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									21</option>
									<option value="22">
									<xsl:if test='contains($to,"/22/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									22</option>
									<option value="23">
									<xsl:if test='contains($to,"/23/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									23</option>
									<option value="24">
									<xsl:if test='contains($to,"/24/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									24</option>
									<option value="25">
									<xsl:if test='contains($to,"/25/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									25</option>
									<option value="26">
									<xsl:if test='contains($to,"/26/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									26</option>
									<option value="27">
									<xsl:if test='contains($to,"/27/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									27</option>
									<option value="28">
									<xsl:if test='contains($to,"/28/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									28</option>
									<option value="29">
									<xsl:if test='contains($to,"/29/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									29</option>
									<option value="30">
									<xsl:if test='contains($to,"/30/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									30</option>
									<option value="31">
									<xsl:if test='contains($to,"/31/")'>
										<xsl:attribute name="selected">true</xsl:attribute>
									</xsl:if>
									31</option>
								</select>
								<select name="year2" class="uportal-input-text">
									<xsl:call-template name="full-year-options">
										<xsl:with-param name="selected-date" select="substring-after( substring-after($to,'/') ,'/20')"/>
									</xsl:call-template>
								</select>

							</td>
						</tr>
						<tr>
	<!-- Combo Calendars -->
							<td align="right" class="table-light-left">
								<label for="CS-CalenderS1">Calendar</label>
							</td>
							<td align="left" class="table-content-right">
								<select size="1" name="calid" class="uportal-input-text" id="CS-CalenderS1">
									<xsl:if test="/calendar-system/back/@value = 'Main'">
										<option value="all-calendars">All calendars</option>
									</xsl:if>
	<!-- ////////////////////////////////////////////// -->
	<!-- own calendars -->
									<xsl:apply-templates select="/calendar-system/calendar[@owner=$cur_user]" mode="owner">
										<xsl:with-param name="curCalid" select="/calendar-system/view/calendar/@calid" />
										<xsl:sort select="@calname" />
									</xsl:apply-templates>
	<!-- ////////////////////////////////////////////// -->
	<!-- shared calendars -->
									<xsl:apply-templates select="/calendar-system/calendar[@owner!=$cur_user and not(key('calhidden', @calid))]" mode="shared">
										<xsl:with-param name="curCalid" select="/calendar-system/view/calendar/@calid" />
										<xsl:sort select="@owner" />
									</xsl:apply-templates>
								</select>
							</td>
						</tr>
	<!--Button Search and parameter fields -->
						<tr>
							<td align="center" nowrap="nowrap" colspan="2" class="table-content-single-bottom">
								<input type="hidden" name="sid" value="{$sid}" />
								<input type="hidden" name="do" value="find" />
								<input class="uportal-button" type="submit" name="search" value="Search" />
							</td>
						</tr>
					</div>
					<tr>
						<td colspan="2">
	<!-- Search Result -->
							<xsl:choose>
								<xsl:when test="view/@pages &gt; 0">
									<xsl:call-template name="search-result" />
								</xsl:when>
								<xsl:when test="view/@pages = 0">
									<hr/>
									<b style="padding-left:20px">No results found.</b>
									<hr/>
								</xsl:when>
							</xsl:choose>
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>
	
	
	
<!-- Template for display result of search, context node is <calendar-system>-->
    <xsl:template name="search-result">
    	<!--UniAcc: Data Table -->
        <table border="0" cellpadding="2" cellspacing="0" width="100%" align="left">
<!-- Header of results -->
            <tr>
                <td align="center" class="th-left" id="CS-Title">&#160;Title</td>
                <td align="center" nowrap="nowrap" class="th" id="CS-DateTime">Date &amp; Time</td>
                <td align="center" nowrap="nowrap" class="th" id="CS-Type">Type</td>
                <td align="center" nowrap="nowrap" class="th" id="CS-Category">Category</td>
                <td align="center" class="th" id="CS-Place">Place</td>
                <td align="center" class="th-right" id="CS-Notes">Notes</td>
            </tr>
<!-- Entries of result -->
            <xsl:for-each select="view/calendar/entry">
                <xsl:apply-templates select='key("ceid", @ceid)' />
            </xsl:for-each>
            <tr>
                <td colspan="5" align="right" height="22" class="table-content-single-bottom">
<!-- Previous -->
                    <xsl:choose>
                        <xsl:when test="view/@prev='true'">
                            <a href="{$mdoURL}=previous&amp;p={view/@page}" title="Display">
                                <img src="{$baseImagePath}/prev_12.gif" border="0" align="middle" alt="Display Previous Page" title="Display Previous Page" />
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <img border="0" src="{$baseImagePath}/prev_disabled_12.gif" align="middle" alt="" title="" />
                        </xsl:otherwise>
                    </xsl:choose>
<!-- current page number -->
 
                    <font class='uportal-channel-text'>
                    <xsl:value-of select="view/@page + 1" />
                    /
                    <xsl:value-of select="view/@pages" />
                    </font>
 
<!-- Next -->
                    <xsl:choose>
                        <xsl:when test="view/@next='true'">
                            <a href="{$mdoURL}=next&amp;p={view/@page}" title="Display">
                                <img src="{$baseImagePath}/next_12.gif" border="0" align="middle" alt="Display Next Page" title="Display Next Page" />
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <img border="0" src="{$baseImagePath}/next_disabled_12.gif" align="middle" alt="" title="" />
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template>
	
	
	
<!-- View entry -->
    <xsl:template match="entry">
        <xsl:variable name="is-invitation" select="../@owner=../@calid and attendee[@cuid=../../@owner]/@status='ACCEPTED' and organizer/text() !=../@calid" />
        <xsl:variable name="title">
            <xsl:choose>
                <xsl:when test="event/text()">
                    <xsl:value-of select="normalize-space(event/text())" />
                </xsl:when>
                <xsl:when test="todo/text()">
                    <xsl:value-of select="normalize-space(todo/text())" />
                </xsl:when>
                <xsl:otherwise>Untitled</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <tr valign="top" class="uportal-background-content">

<!-- Title -->
		<xsl:choose>
	        <xsl:when test="event/text()">
				<xsl:call-template name="display-title">
                    <xsl:with-param name="type" select="'Event'" />
                    <xsl:with-param name="is-invitation" select="$is-invitation" />
                    <xsl:with-param name="title" select="$title" />
				</xsl:call-template>
            </xsl:when>
            <xsl:when test="todo/text()">
				<xsl:call-template name="display-title">
                    <xsl:with-param name="type" select="'Todo'" />
                    <xsl:with-param name="is-invitation" select="$is-invitation" />
                    <xsl:with-param name="title" select="$title" />
				</xsl:call-template>
            </xsl:when>
        </xsl:choose>
        
<!-- Date & Time -->
            <td nowrap="nowrap" align="left" class="table-light" headers="CS-DateTime">
				<xsl:choose>
			        <xsl:when test="event/text()">
		                <xsl:choose>
		                    <xsl:when test="duration/@length='all-day'">
		                    <xsl:value-of select='substring-before(duration/@start,"_")' />
		                    &#160; All day</xsl:when>
		                    <xsl:otherwise>
		                    <xsl:value-of select='substring-before(duration/@start,"_")' />
		                    &#160; 
		                    <xsl:call-template name="t24to12">
		                        <xsl:with-param name="hour" select='substring-after(duration/@start,"_")' />
		                    </xsl:call-template>
		 					- 
		                    <xsl:if test="substring-before(duration/@start,'_') != substring-before(duration/@end,'_')">
		                    <xsl:value-of select='substring-before(duration/@end,"_")' />
		                    &#160;</xsl:if>
		                    <xsl:call-template name="t24to12">
		                        <xsl:with-param name="hour" select='substring-after(duration/@end,"_")' />
		                    </xsl:call-template>
		                    </xsl:otherwise>
		                </xsl:choose>
					</xsl:when>
		
			        <xsl:when test="todo/text()">
		                    Due on
		                    <xsl:value-of select='substring-before(duration/@start,"_")' />
		                    &#160; 
		                    <xsl:call-template name="t24to12">
		                        <xsl:with-param name="hour" select='substring-after(duration/@start,"_")' />
		                    </xsl:call-template>
					</xsl:when>
		
				</xsl:choose>
            </td>

<!-- Type -->
            <td nowrap="nowrap" align="left" class="table-light" headers="CS-Type">
				<xsl:choose>
			        <xsl:when test="event/text()">
						Event
		            </xsl:when>
		            <xsl:when test="todo/text()">
						Task
		            </xsl:when>
		            <xsl:otherwise>
		            	N/a
		            </xsl:otherwise>
		        </xsl:choose>
        	</td>

<!-- Category -->
            <td nowrap="nowrap" class="table-light" headers="CS-Category">&#160;
            <xsl:value-of select="event/category" />
            <xsl:if test="not(event/category) or string-length(event/category)=0">None</xsl:if>
            </td>

<!-- Place -->
            <td class="table-light" headers="CS-Place">
                <xsl:call-template name="break-line">
                    <xsl:with-param name="st" select="location/text()" />
                    <xsl:with-param name="chunk-len" select="60" />
                </xsl:call-template>
                <xsl:if test="not(location) or string-length(location)=0">&#160;</xsl:if>
            </td>

<!-- Notes -->
            <td class="table-light-left" headers="CS-Notes">
			  <xsl:call-template name='smarttext'>
                    <xsl:with-param name="body" select="event/description/text()" />
                </xsl:call-template>
                <xsl:if test="not(event/description) or string-length(event/description)=0">&#160;</xsl:if>
            </td>
        </tr>
    </xsl:template>
	
	
	
	
<!-- Template: display-title -->
	<xsl:template name="display-title">
		<xsl:param name="type" />
		<xsl:param name="is-invitation" />
		<xsl:param name="title" />
	            <td class="table-light-left" headers="CS-Title">
 
                <a href="{$mgoURL}={$type}&amp;ceid={@ceid}&amp;calid={../@calid}" title="Display">
                    <xsl:choose>
                        <xsl:when test="$is-invitation">
                            <xsl:call-template name="break-line">
                                <xsl:with-param name="st" select="$title" />
                                <xsl:with-param name="chunk-len" select="60" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="break-line">
                                <xsl:with-param name="st" select="$title" />
                                <xsl:with-param name="chunk-len" select="60" />
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
                    <a href="{$mgoURL}={$type}&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit" onmouseover="swapImage('calendarEntryImage{@ceid}{$channelID}','channel_edit_active.gif')" onmouseout="swapImage('calendarEntryImage{@ceid}{$channelID}','channel_edit_base.gif')">
                        <img src="{$SPACER}" border="0" width="3" alt="" title="" />
                        <img src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" border="0" align="absmiddle" name="calendarEntryImage{@ceid}{$channelID}" id="calendarEntryImage{@ceid}{$channelID}" alt="Edit this event" title="Edit this event" />
                    </a>
                </xsl:if>
                <xsl:if test="contains($targetChannel, 'CCalendarUnicon') and $editEvents='Y'">
                    <a href="{$mgoURL}={$type}&amp;ceid={@ceid}&amp;calid={../@calid}" title="Edit" onmouseover="swapImage('calendarEntryImage{@ceid}{$channelID}','channel_edit_active.gif')" onmouseout="swapImage('calendarEntryImage{@ceid}{$channelID}','channel_edit_base.gif')">
                        <img src="{$SPACER}" border="0" width="3" alt="" title="" />
                        <img src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" border="0" align="absmiddle" name="calendarEntryImage{@ceid}{$channelID}" id="calendarEntryImage{@ceid}{$channelID}" alt="Edit this event" title="Edit this event" />
                    </a>
                </xsl:if>
                <xsl:if test="contains($targetChannel, 'CCalendarUnicon') and $editEvents='N'">
                    <a href="{$mgoURL}={$type}&amp;ceid={@ceid}&amp;calid={../@calid}" title="View" onmouseover="swapImage('calendarViewEntryImage{@ceid}{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarViewEntryImage{@ceid}{$channelID}','channel_view_base.gif')">
                        <img src="{$SPACER}" border="0" width="3" alt="" title="" />
                        <img src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" border="0" align="absmiddle" name="calendarViewEntryImage{@ceid}{$channelID}" id="calendarViewEntryImage{@ceid}{$channelID}" alt="View this event" title="View this event" />
                    </a>
                </xsl:if>
				<!--delete link of entry-->
                <xsl:call-template name="access">
                    <xsl:with-param name="type-link">delete</xsl:with-param>
                </xsl:call-template>
            </td>
	</xsl:template>
	
<!-- Combo category -->
    <xsl:template name="category">
        <xsl:param name="category" />
        <select name="category" class="uportal-input-text" id="CS-CategoryS1">
            <option value="all-categories">All categories</option>
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
        <br />
    </xsl:template>
</xsl:stylesheet>