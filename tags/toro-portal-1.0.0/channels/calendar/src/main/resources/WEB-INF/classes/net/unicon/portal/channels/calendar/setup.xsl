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
    <xsl:param name="window">setup</xsl:param>
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="access-detail.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="main" />
<!-- used only for navigation-bar -->
    <xsl:template match="/">
    	<!--<textarea rows="4" cols="40">    		
    		<xsl:copy-of select = "*"/>
    	</textarea> -->
<!-- Navigation bar -->
        <xsl:call-template name="links" />
        <!--UniAcc: Layout Table -->
        <div class="bounding-box2">
			<table border="0" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<td valign="top" width="40%" class="uportal-background-light">
	<!-- left screen: calendar and list calendar -->
						<form action="{$baseActionURL}" method="post">
							<xsl:call-template name="mform" />
							<!--UniAcc: Layout Table -->
							<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<!-- Link for create new calendar -->
								<tr>
									<td nowrap="nowrap" class="th">
										<xsl:choose>
											<xsl:when test="$curCalendar/@calid">Available Calendars</xsl:when>
											<xsl:otherwise>Available Calendars</xsl:otherwise>
										</xsl:choose>
										<input type="submit" class="uportal-button" name="do~createCalendar" title="Create New Calendar" value="Add" />
									</td>
								</tr>
	<!-- Draw left-hand side: list of calendar -->
								<xsl:for-each select="//calendar-system/calendar[@owner = @calid]">
									<xsl:if test="@owner=$cur-user">
										<tr>
											<td class="table-content-single">  
												<xsl:choose>
													<xsl:when test="@calid=$curCalendar/@calid">
														<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" align="absmiddle" alt="Currently displaying setup for this calendar" title="Currently displaying setup for this calendar" /> 
														<span class="uportal-background-highlight">
															<xsl:value-of select="@calname" />
														</span>
													</xsl:when>
													<xsl:otherwise>
														<a href="{$mdoURL}=gotoCalendar&amp;calid={@calid}" title="Display" onmouseover="swapImage('calendarDisplayImage{@calid}{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarDisplayImage{@calid}{$channelID}','channel_view_base.gif')">
															<xsl:value-of select="@calname" />
															<img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
															<img src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" border="0" align="absmiddle" name="calendarDisplayImage{@calid}{$channelID}" id="calendarDisplayImage{@calid}{$channelID}" alt="this calendar" title="this calendar" />
														</a>
													</xsl:otherwise>
												</xsl:choose>
											</td>
										</tr>
									</xsl:if>
								</xsl:for-each>
								<xsl:for-each select="//calendar-system/calendar[@owner != @calid]">
									<xsl:sort select="@calname" />
									<xsl:if test="@owner=$cur-user">
										<tr>
											<td class="table-content-single">

												<xsl:choose>
													<xsl:when test="@calid=$curCalendar/@calid">
														<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" align="absmiddle" alt="Currently displaying setup for this calendar" title="Currently displaying setup for this calendar" />

														<span class="uportal-background-highlight">
															<xsl:value-of select="@calname" />
														</span>
													</xsl:when>
													<xsl:otherwise>
														<a href="{$mdoURL}=gotoCalendar&amp;calid={@calid}" title="Display" onmouseover="swapImage('calendarDisplayImage{@calid}{$channelID}','channel_view_active.gif')" onmouseout="swapImage('calendarDisplayImage{@calid}{$channelID}','channel_view_base.gif')">
															<xsl:value-of select="@calname" />
															<img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
															<img src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" border="0" align="absmiddle" name="calendarDisplayImage{@calid}{$channelID}" id="calendarDisplayImage{@calid}{$channelID}" alt="this calendar" title="this calendar" />
														</a>
													</xsl:otherwise>
												</xsl:choose>
	<!-- Personal calendar -->
												<xsl:if test=" @owner != @calid">
													<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
	<!-- user-created calendars -->
													<a href="{$mdoURL}=deleteCalendar&amp;calid={@calid}" title="Delete" onmouseover="swapImage('calendarDeleteImage{@calid}{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('calendarDeleteImage{@calid}{$channelID}','channel_delete_base.gif')">
														<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" border="0" align="absmiddle" name="calendarDeleteImage{@calid}{$channelID}" id="calendarDeleteImage{@calid}{$channelID}" alt="this calendar" title="this calendar" />
													</a>
												</xsl:if>
											</td>
										</tr>
									</xsl:if>
								</xsl:for-each>
							</table>
						</form>
					</td>
					<td class="table-light-single" style="border:0px;">
						<img border="0" src="{$SPACER}" alt="" title="" />
					</td>
	<!-- Detail of Calendar -->
					<td valign="top" style="padding:0px;" width="60%" class="uportal-background-light">
						<xsl:call-template name="calendar-detail" />
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>
    <xsl:template name="calendar-detail">
<!-- List of shared users -->
        <form method="post" action="{$baseActionURL}">
            <input type="hidden" name="sid">
                <xsl:attribute name="value">
                    <xsl:value-of select="$sid" />
                </xsl:attribute>
            </input>
<!-- SETUP -->
			<!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <td>
<!-- SETUP OPTIONS -->
						<!--UniAcc: Layout Table -->
                        <table border="0" cellpadding="0" cellspacing="0" width="100%">
                            <tr>
                                <td class="table-light-left-bottom" nowrap="nowrap">
                                	<label for="CSet-CalendarNameT1">Calendar Name</label>
                                </td>
                                <td class="table-content-right-bottom" width="100%">
                                    <input type="text" name="calname" size="23" class="text" id="CSet-CalendarNameT1">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$curCalendar/@calname" />
                                        </xsl:attribute>
                                    </input>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
<!-- USERS -->
									<!--UniAcc: Layout Table -->
                                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                        <tr>
                                            <th class="th-top-right" nowrap="nowrap">Write Permission</th>
                                            <th class="th-top-left" width="100%">Calendar Users&#160;
                                            	<input type="submit" class="uportal-button" border="0" name="do~addUser" title="Add New Users" value="Add" />
                                            </th>
                                        </tr>
<!-- Share options -->
                                        <xsl:call-template name="cur-share" />
                                        <tr>
                                            <td class="table-nav" colspan="2">
<!-- Save -->
                                                <input type="hidden" name="default" value="do~save" />
                                                <input class="uportal-button" type="submit" name="do~save" value="Save" title="Save" />
                                            </td>
                                        </tr>
                                    </table>
<!-- END USERS -->
                                </td>
                            </tr>
                        </table>
<!-- END SETUP OPTIONS -->
                    </td>
                </tr>
            </table>
<!-- END SETUP -->
<!-- Hidden fields -->
            <xsl:if test="$curCalendar/@calid">
                <input type="hidden" name="calid">
                    <xsl:attribute name="value">
                        <xsl:value-of select="$curCalendar/@calid" />
                    </xsl:attribute>
                </input>
            </xsl:if>
        </form>
    </xsl:template>
<!--///// Select all user that curent calendar share //////-->
    <xsl:template name="cur-share">
        <xsl:for-each select="$curCalendar/ace[@cuid !='@@p' and @cuid !='@@o' and @read='true' and $cur_user = @cuid]">
            <tr>
<!-- Write check box -->
                <td class="table-content-left" align="right">
                	<img border="0" src="{$baseImagePath}/checked_disable_12.gif" alt="disabled checkbox" title="disabled checkbox" />
                	&#160;
                </td>
<!-- User id -->
                <td class="table-content-right"> 
                    <xsl:choose>
                        <xsl:when test="@cuid='@'">
                            <img border="0" src="{$baseImagePath}/persons_16.gif" alt="" title="" />
 								"Everyone"
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="@itype='G'">
                                    <img border="0" src="{$baseImagePath}/persons_16.gif" alt="" title="" />
                                </xsl:when>
                                <xsl:otherwise>
                                    <img border="0" src="{$baseImagePath}/person_16.gif" alt="" title="" />
                                </xsl:otherwise>
                            </xsl:choose>
 
                            <xsl:value-of select="text()" />
                        </xsl:otherwise>
                    </xsl:choose> 
                </td>
            </tr>
        </xsl:for-each>
        <xsl:for-each select="$curCalendar/ace[@cuid !='@@p' and @cuid !='@@o' and @read='true' and $cur_user != @cuid]">
            <xsl:sort select="text()" />
            <tr>
<!-- Write check box -->
                <td class="table-content-left" align="right">
	                <input type="checkbox" value="ON" id="CSet-CalUsers{generate-id()}">
	                    <xsl:attribute name="name">
	                        <xsl:value-of select="@cuid" />
	                    </xsl:attribute>
	                    <xsl:if test="@write='true'">
	                        <xsl:attribute name="checked" />
	                    </xsl:if>
	                </input>
	                &#160;
                </td>
<!-- User id -->
                <td class="table-content-right">
                	<label for="CSet-CalUsers{generate-id()}">
	                    <xsl:choose>
	                        <xsl:when test="@cuid='@'">
	                            <img border="0" src="{$baseImagePath}/persons_16.gif" alt="group" title="group" />
	 							"Everyone"
	                        </xsl:when>
	                        <xsl:otherwise>
	                            <xsl:choose>
	                                <xsl:when test="@itype='G'">
	                                    <img border="0" src="{$baseImagePath}/persons_16.gif" alt="individual" title="individual" />
	                                </xsl:when>
	                                <xsl:otherwise>
	                                    <img border="0" src="{$baseImagePath}/person_16.gif" alt="individual" title="individual" />
	                                </xsl:otherwise>
	                            </xsl:choose>
	 
	                            <xsl:value-of select="text()" />
	                        </xsl:otherwise>
	                    </xsl:choose>
 					</label>
                    <a href="{$mdoURL}=deleteUser&amp;user={@cuid}" title="Remove" onmouseover="swapImage('calendarUserDeleteImage{generate-id()}{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('calendarUserDeleteImage{generate-id()}{$channelID}','channel_delete_base.gif')">
                        <img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" border="0" align="absmiddle" name="calendarUserDeleteImage{generate-id()}{$channelID}" id="calendarUserDeleteImage{generate-id()}{$channelID}" alt="this calendar user" title="this calendar user" />
                    </a>
                </td>
            </tr>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>

