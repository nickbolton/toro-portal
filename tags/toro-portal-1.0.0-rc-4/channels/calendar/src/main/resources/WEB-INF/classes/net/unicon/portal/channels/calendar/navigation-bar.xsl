<?xml version="1.0" encoding="utf-8"?>
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
    <xsl:include href="../global/toolbar.xsl"/>
    <xsl:key match="/calendar-system/logon/access" name="access" use="@calid" />
    <xsl:key name="calhidden" match="/calendar-system/preference/calendar-hidden" use="@calid" />
    <xsl:variable name="cur_user" select="calendar-system/logon/@user" />
    <xsl:param name="calid">
        <xsl:choose>
            <xsl:when test="//calendar-system/view/calendar/@calid">
                <xsl:value-of select="//calendar-system/view/calendar/@calid" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="//calendar-system/logon/@user" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    
    
    <xsl:template name="links">
        <xsl:variable name="right" select="key(&quot;access&quot;,$calid)" />
        <xsl:variable name="date">
            <xsl:choose>
                <xsl:when test="$main='daily'">
                    <xsl:value-of select="/calendar-system/detail-daily/@date" />
                </xsl:when>
                <xsl:when test="$main='weekly'">
                    <xsl:value-of select="/calendar-system/detail-weekly/@date" />
                </xsl:when>
                <xsl:when test="$main='monthly'">
                    <xsl:value-of select="/calendar-system/detail-monthly/@date" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$cur-date" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <div class="calendar-toolbar-container">
			<xsl:choose>
				<xsl:when test="$window = 'event'"> 
					<xsl:call-template name="channel-link-calendar">
						<xsl:with-param name="title">Calendar</xsl:with-param>
					</xsl:call-template>				
				</xsl:when>
				<xsl:when test="$window = 'todo'">
					<xsl:call-template name="channel-link-calendar">
						<xsl:with-param name="title">Calendar</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$mdoURL"/>=changeView&amp;op=v~<xsl:value-of select="$main"/>&amp;window=event</xsl:with-param>
					</xsl:call-template>				
				</xsl:when>
				<xsl:when test="not(contains($targetChannel, 'CCalendarUnicon'))">
					<xsl:call-template name="channel-link-calendar">
						<xsl:with-param name="title">Calendar</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$mgoURL"/>=Main&amp;init=1&amp;window=event&amp;calid=<xsl:value-of select="$calid"/></xsl:with-param>
					</xsl:call-template>				
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-calendar">
						<xsl:with-param name="title">Calendar</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$mgoURL"/>=MainLMS&amp;init=1&amp;window=event&amp;calid=<xsl:value-of select="$calid"/></xsl:with-param>
					</xsl:call-template>				
				</xsl:otherwise>
			</xsl:choose>        

			<xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
				<xsl:choose>
                    <xsl:when test="$window = 'todo'">
						<xsl:call-template name="channel-link-calendar">
							<xsl:with-param name="title">Tasks</xsl:with-param>
						</xsl:call-template>                    
                    </xsl:when>
                    <xsl:when test="$window = 'event'">
						<xsl:call-template name="channel-link-calendar">
							<xsl:with-param name="title">Tasks</xsl:with-param>
							<xsl:with-param name="URL"><xsl:value-of select="$mdoURL"/>=changeView&amp;op=v~<xsl:value-of select="$main"/>&amp;window=todo</xsl:with-param>
						</xsl:call-template>                    
                    </xsl:when>
                    <xsl:otherwise>
						<xsl:call-template name="channel-link-calendar">
							<xsl:with-param name="title">Tasks</xsl:with-param>
							<xsl:with-param name="URL"><xsl:value-of select="$mgoURL"/>=Main&amp;init=1&amp;window=todo&amp;calid=<xsl:value-of select="$calid"/></xsl:with-param>
						</xsl:call-template>                    
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>

            <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
                <xsl:choose>
                    <xsl:when test="$window = 'invitation'">
						<xsl:call-template name="channel-link-calendar">
							<xsl:with-param name="title">My Invites</xsl:with-param>
						</xsl:call-template>                    
                    </xsl:when>
                    <xsl:otherwise>
						<xsl:call-template name="channel-link-calendar">
							<xsl:with-param name="title">My Invites</xsl:with-param>
							<xsl:with-param name="URL"><xsl:value-of select="$mgoURL"/>=Invitations&amp;init=1&amp;calid=<xsl:value-of select="$calid"/></xsl:with-param>
						</xsl:call-template>                     
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
            
			<xsl:choose>
				<xsl:when test="$window = 'search'">
					<xsl:call-template name="channel-link-calendar">
						<xsl:with-param name="title">Search</xsl:with-param>
					</xsl:call-template>  				
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-calendar">
						<xsl:with-param name="title">Search</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$mgoURL"/>=Search&amp;init=1&amp;calid=<xsl:value-of select="$calid"/></xsl:with-param>
					</xsl:call-template>				
				</xsl:otherwise>
            </xsl:choose>
         
        	<div class="calendar-navigation-right"> 

				<xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
					<xsl:choose>
						<xsl:when test="$window = 'setup'">Setup</xsl:when>
						<xsl:otherwise>
							<a href="{$mgoURL}=Setup&amp;init=1&amp;calid={$calid}" title="Modify Setup">Setup</a>
						</xsl:otherwise>
					</xsl:choose>
                </xsl:if>
                <span class="calendar-spacer"></span>
                <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
					<xsl:choose>
						<xsl:when test="$window = 'preference'">Preferences</xsl:when>
						<xsl:otherwise>
							<a href="{$mgoURL}=Preference&amp;init=1&amp;calid={$calid}" title="Modify Preferences">Preferences</a>
						</xsl:otherwise>
					</xsl:choose>
                </xsl:if>
                <span class="calendar-spacer"></span>
                <xsl:if test="not(contains($targetChannel, 'CCalendarUnicon'))">
					<xsl:choose>
						<xsl:when test="$window = 'import'">Import</xsl:when>
						<xsl:otherwise>
							<a href="{$mgoURL}=Import&amp;init=1&amp;calid={$calid}&amp;format={$window}" title="Import">Import</a>
						</xsl:otherwise>
					</xsl:choose>
                	<span class="calendar-spacer"></span>
                	<xsl:choose>
						<xsl:when test="$window = 'export'">Export</xsl:when>
						<xsl:otherwise>
							<a href="{$mgoURL}=Export&amp;init=1&amp;calid={$calid}&amp;format={$window}" title="Export">Export</a>
						</xsl:otherwise>
					</xsl:choose>
                </xsl:if>
        	
        	</div>
        </div>
        
    </xsl:template>
<!--  //////////End UNICON\\\\\\\\\\ -->

<!-- Navigation-bar for screen "Main Window "-->
    <xsl:template name="window-navigation-bar">
        <xsl:param name="window" />
        <xsl:param name="main" />
        <xsl:variable name="setup" select="/calendar-system/config/@setup" />
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-light" style="border-collapse: collapse" width="100%">
            <tr class="uportal-background-content">
                <form action="{$baseActionURL}" method="post">
                    <td align="left" valign="middle" width="99%">
                        <font class="uportal-channel-emphasis">
                        	<label for="CNB-CalendersS1">
	                            <xsl:choose>
	                                <xsl:when test="$window = 'event'">Events</xsl:when>
	                                <xsl:when test="$window='todo'">Tasks</xsl:when>
	                            </xsl:choose>
                            </label>
                        </font>
						&#194;&#160;
<!-- Combo Calendars -->
                        <select class="uportal-input-text" name="calid" size="1" id="CNB-CalendersS1">
                            <option value="all-calendars">All calendars</option>
<!-- calendars of owner -->
                            <xsl:for-each select="calendar-system/calendar[@owner=$cur_user]">
                                <xsl:sort select="@calname" />
                                <xsl:choose>
                                    <xsl:when test="@calid=//calendar-system/view/calendar/@calid">
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
                            </xsl:for-each>
<!-- shared calendars  -->
                            <xsl:for-each select="calendar-system/calendar[@owner!=$cur_user]">
                                <xsl:sort select="@owner" />
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
                            </xsl:for-each>
                        </select>
                        <input name="sid" type="hidden" value="{$sid}" />
						&#194;&#160;
                        <input align="bottom" border="0" name="do~update" src="{$baseImagePath}/enter_12.gif" title="Change Calendar" type="image" />
                    </td>
                </form>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <xsl:choose>
                        <xsl:when test="$window = 'event'">
                            <a href="{$mdoURL}=changeView&amp;op=v~{$main}&amp;window=todo" title="Display">
                                <font class="uportal-channel-emphasis">&#194;&#160;Tasks&#194;&#160;</font>
                            </a>
                        </xsl:when>
                        <xsl:when test="$window='todo'">
                            <a href="{$mdoURL}=changeView&amp;op=v~{$main}&amp;window=event" title="Display">
                                <font class="uportal-channel-emphasis">&#194;&#160;Events&#194;&#160;</font>
                            </a>
                        </xsl:when>
                    </xsl:choose>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Invitations&amp;init=1&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">&#194;&#160;Invitations&#194;&#160;</font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Search&amp;init=1&amp;calid={$calid}" title="">
                        <font class="uportal-channel-emphasis">&#194;&#160;Search&#194;&#160;</font>
                    </a>
                </td>
                <xsl:choose>
                    <xsl:when test="$setup='true'">
                        <td class="uportal-background-light" width="2">
                            <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                        </td>
                        <td align="center" nowrap="nowrap" valign="middle">
                            <a href="{$mgoURL}=Setup&amp;init=1&amp;calid={$calid}" title="Display">
                                <font class="uportal-channel-emphasis">&#194;&#160;Setup</font>
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td colspan="2" />
                    </xsl:otherwise>
                </xsl:choose>
                <td>
                    <img height="20" src="{$SPACER}" width="1" alt="" title="" />
                </td>
            </tr>
            <tr class="uportal-background-light">
                <td colspan="10">
                    <img border="0" height="2" src="{$SPACER}" alt="" title="" />
                </td>
            </tr>
        </table>
    </xsl:template>
<!-- Navigation-bar for screen "invitations"-->
    <xsl:template name="invitations-navigation-bar">
        <xsl:variable name="setup" select="/calendar-system/config/@setup" />
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-light" style="border-collapse: collapse" width="100%">
            <tr class="uportal-background-content">
                <td align="left" valign="middle" width="99%">
                    <font class="uportal-channel-emphasis">Invitations</font>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Main&amp;init=1&amp;window=event&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Events&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Main&amp;init=1&amp;window=todo&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Tasks&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Search&amp;init=1&amp;calid={$calid}" title="">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Search&#194;&#160;
                        </font>
                    </a>
                </td>
                <xsl:choose>
                    <xsl:when test="$setup='true'">
                        <td class="uportal-background-light" width="2">
                            <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                        </td>
                        <td align="center" nowrap="nowrap" valign="middle">
                            <a href="{$mgoURL}=Setup&amp;init=1&amp;calid={$calid}" title="Display">
                                <font class="uportal-channel-emphasis">
									&#194;&#160;Setup
                                </font>
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td colspan="2" />
                    </xsl:otherwise>
                </xsl:choose>
                <td>
                    <img height="20" src="{$SPACER}" width="1" alt="" title="" />
                </td>
            </tr>
            <tr>
                <td class="uportal-background-light" colspan="10" height="2">
                    <img border="0" height="2" src="{$SPACER}" alt="" title="" />
                </td>
            </tr>
        </table>
    </xsl:template>
<!-- Navigation-bar for screen "SearchEvents"-->
    <xsl:template name="search-navigation-bar">
        <xsl:variable name="setup" select="/calendar-system/config/@setup" />
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-light" style="border-collapse: collapse" width="100%">
            <tr class="uportal-background-content">
                <td align="left" valign="middle" width="99%">
                    <font class="uportal-channel-emphasis">Search</font>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Main&amp;init=1&amp;window=event&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Events&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Main&amp;init=1&amp;window=todo&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Tasks&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Invitations&amp;init=1&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Invitations&#194;&#160;
                        </font>
                    </a>
                </td>
                <xsl:choose>
                    <xsl:when test="$setup='true'">
                        <td class="uportal-background-light" width="2">
                            <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                        </td>
                        <td align="center" nowrap="nowrap" valign="middle">
                            <a href="{$mgoURL}=Setup&amp;init=1&amp;calid={$calid}" title="Display">
                                <font class="uportal-channel-emphasis">
									&#194;&#160;Setup
                                </font>
                            </a>
                        </td>
                    </xsl:when>
                    <xsl:otherwise>
                        <td colspan="2" />
                    </xsl:otherwise>
                </xsl:choose>
                <td>
                    <img height="20" src="{$SPACER}" width="1" alt="" title="" />
                </td>
            </tr>
            <tr>
                <td class="uportal-background-light" colspan="10" height="2">
                    <img border="0" height="2" src="{$SPACER}" alt="" title="" />
                </td>
            </tr>
        </table>
    </xsl:template>
<!-- Navigation-bar for screen "Setup"-->
    <xsl:template name="setup-navigation-bar">
    	<!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" class="uportal-background-light" style="border-collapse: collapse" width="100%">
            <tr class="uportal-background-content">
                <td align="left" valign="middle" width="99%">
                    <font class="uportal-channel-emphasis">Setup</font>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Main&amp;init=1&amp;window=event&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Events&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Main&amp;init=1&amp;window=todo&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Tasks&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Invitations&amp;init=1&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Invitations&#194;&#160;
                        </font>
                    </a>
                </td>
                <td class="uportal-background-light" width="2">
                    <img border="0" src="{$SPACER}" width="2" alt="" title="" />
                </td>
                <td align="center" nowrap="nowrap" valign="middle">
                    <a href="{$mgoURL}=Search&amp;init=1&amp;calid={$calid}" title="Display">
                        <font class="uportal-channel-emphasis">
							&#194;&#160;Search
                        </font>
                    </a>
                </td>
                <td>
                    <img height="20" src="{$SPACER}" width="1" alt="" title="" />
                </td>
            </tr>
            <tr>
                <td class="uportal-background-light" colspan="10" height="2">
                    <img border="0" height="2" src="{$SPACER}" alt="" title="" />
                </td>
            </tr>
        </table>
    </xsl:template>
<!-- ////////////////////////////////////////////////////////// -->
    <xsl:template match="calendar" mode="shared">
        <xsl:param name="curCalid" />
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
            <xsl:when test="@calid=$curCalid">
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

