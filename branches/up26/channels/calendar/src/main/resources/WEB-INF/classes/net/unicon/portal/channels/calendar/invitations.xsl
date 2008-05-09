<?xml version="1.0"?>
<!-- edited with XML Spy v3.5 NT (http://www.xmlspy.com) by M-TRUYEN (IBS-DP) -->
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
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:variable name="user">
        <xsl:value-of select="calendar-system/logon/@user" />
    </xsl:variable>
    <xsl:include href="common.xsl" />
    <xsl:include href="access-detail.xsl" />
    <xsl:param name="window">invitation</xsl:param>
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="utils.xsl" />
    <xsl:key name="attendee" match="attendee" use="@cuid" />
    <xsl:param name="back">default</xsl:param>
    <xsl:param name="status-entry">none</xsl:param>
    <xsl:param name="notification">false</xsl:param>
    <xsl:param name="main" />
<!-- not used, set for navigation-bar -->
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$status-entry='removed'">
            	<div class="bounding-box2">
					<form method="post" action="{$baseActionURL}">
						<xsl:call-template name="mform" />
						<!--UniAcc: Layout Table -->
						<table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-background-content">
							<tr>
								<td colspan="2" height="22" class="uportal-channel-emphasis" align="left">Infomation</td>
							</tr>
							<tr>
								<td colspan="2" height="2" class="uportal-background-light">
									<img src="{$SPACER}" height="2" border="0" alt="" title="" />
								</td>
							</tr>
							<tr>
								<td colspan="2" height="15" class="uportal-background-content">
									<img src="{$SPACER}" height="15" border="0" alt="" title="" />
								</td>
							</tr>
							<tr>
								<td width="50" align="right">
									<img border="0" src="{$baseImagePath}/info_32.gif" alt="" title="" />
								</td>
								<td align="left" valign="middle" class="uportal-channel-text">
									There are no invitations.
								</td>
							</tr>
							<tr>
								<td colspan="2" height="15" class="uportal-background-content">
									<img src="{$SPACER}" height="15" border="0" alt="" title="" />
								</td>
							</tr>
							<tr class="uportal-background-light">
								<td colspan="2" align="left" height="22">
									<img src="{$SPACER}" alt="" title="" />
									<input type="submit" class="uportal-button" name="go~{$back}" value="OK" size="80" />
								</td>
							</tr>
						</table>
					</form>
				</div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$status-entry='none' and $notification='false'">
<!-- Navigation bar -->
                        <xsl:call-template name="links" />
                    </xsl:when>
                    <xsl:otherwise>
                    	<!--UniAcc: Layout Table -->
                        <table cellspacing="0" border="0" cellpadding="0" width="100%" class="uportal-background-light" style="border-collapse: collapse">
                            <tr class="uportal-background-content">
                                <td align="left" valign="middle">
                                    <font class="uportal-channel-emphasis">Invitations</font>
                                </td>
                            </tr>
                            <tr>
                                <td height="2" class="uportal-background-light">
                                    <img border="0" src="{$SPACER}" height="2" alt="" title="" />
                                </td>
                            </tr>
                        </table>
                    </xsl:otherwise>
                </xsl:choose>
<!-- Invatation entries -->
<!-- For border of Netscape -->
                <xsl:choose>
                    <xsl:when test="count(calendar-system/invitations/entry)&gt;0">
                    	<!--UniAcc: Layout Table -->
                    	<div class="bounding-box2">
							<table width="100%" cellpadding="0" cellspacing="0" border="0" class="uportal-background-content">
								<xsl:for-each select="calendar-system/invitations/entry">
									<xsl:variable name="invicalid" select='key("attendee", $user)' />
									<tr>
										<th class="th" colspan="2" height="6">Invitations</th>
									</tr>
									<tr>
	<!-- Event Title -->
										<td align="left" class="table-content-left">&#160;Event</td>
										<td align="left" width="90%" class="table-content-right">
											<xsl:choose>
												<xsl:when test="event/text()">
													<xsl:value-of select="event/text()" />
												</xsl:when>
												<xsl:otherwise>Untitled</xsl:otherwise>
											</xsl:choose>
										</td>
									</tr>
									<tr>
	<!-- Organizer -->
										<td align="left" class="table-content-left" nowrap="nowrap">&#160;Invited By&#160;&#160;</td>
										<td align="left" class="table-content-right">
											<xsl:value-of select="organizer/@cuid" />
										</td>
									</tr>
									<tr>
	<!-- Time -->
										<td align="left" class="table-content-left" nowrap="nowrap">&#160;Time&#160;&#160;</td>
										<td align="left" class="table-content-right">
											<xsl:choose>
												<xsl:when test="duration/@length='all-day'">
													All day
												</xsl:when>
												<xsl:otherwise>
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
										</td>
									</tr>
									<tr>
	<!-- Date -->
										<td align="left" class="table-content-left" nowrap="nowrap" valign='abstop'>
											&#160;Date&#160;&#160;
										</td>
										<td align="left" class="table-content-right">
											<xsl:value-of select="repeat/@rdate" />
										</td>
									</tr>
									<tr>
	<!-- Location -->
										<td align="left" class="table-content-left" valign='abstop'>&#160;Location</td>
										<td align="left" class="table-content-right">
											<xsl:call-template name="break-line">
												<xsl:with-param name="st" select="location" />
												<xsl:with-param name="chunk-len" select="60" />
											</xsl:call-template>
										</td>
									</tr>
									<tr>
	<!-- Attendee -->
										<td align="left" class="table-content-left" valign='abstop'>
											&#160;Attendees
										</td>
										<td align="left" class="table-content-right">
											<ul>
												<xsl:for-each select="attendee">
													<li>
														<xsl:value-of select="text()" />
														:&#160; 
														<xsl:choose>
															<xsl:when test='@status="ACCEPTED"'>accepted</xsl:when>
															<xsl:when test='@status="NEEDS-ACTION"'>pending</xsl:when>
															<xsl:otherwise>declined</xsl:otherwise>
														</xsl:choose>
													</li>
												</xsl:for-each>
											</ul>
										</td>
									</tr>
									<tr>
										<td class="table-nav" colspan="2">
											<form method="post" action="{$baseActionURL}">
												<!--UniAcc: Layout Table -->
												<table>
													<tr>
														<td align="left">
															<input type="hidden" name="sid">
																<xsl:attribute name="value">
																	<xsl:value-of select="$sid" />
																</xsl:attribute>
															</input>
															<input type="hidden" name="calid">
																<xsl:attribute name="value">
																	<xsl:value-of select="/calendar-system/calendar/@calid[/calendar-system/calendar/@calid=$user]" />
																</xsl:attribute>
															</input>
															<input type="hidden" name="ceid">
																<xsl:attribute name="value">
																	<xsl:value-of select="@ceid" />
																</xsl:attribute>
															</input>
															<input class="uportal-button" type="submit" name="do~reply" value="Accept" />
															<input class="uportal-button" type="submit" name="do~reply" value="Decline" />
														</td>
													</tr>
												</table>
											</form>
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</div>
						
                        <xsl:if test="$status-entry='found'">
                        	<!--UniAcc: Layout Table -->
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td align="left">
                                        <a href="{$rgoURL}={$back}" title="View">
                                            <img border="0" src="{$SPACER}" alt="" title="" />
                                            <img border="0" src="{$baseImagePath}/back_12.gif" alt="All Invitations" title="All Invitations" />
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                    	<!--UniAcc: Layout Table -->
                        <table cellpadding="0" cellspacing="0" border="0" width="100%">
                            <tr>
                                <td colspan="2" height="6">
                                    <img src="{$SPACER}" height="6" alt="" title="" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" align="left" valign="middle" class="uportal-channel-text">
									There are no invitations. 
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" height="6">
                                    <img src="{$SPACER}" height="6" alt="" title="" />
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" height="2" class="uportal-background-light">
                                    <img src="{$SPACER}" height="2" alt="" title="" />
                                </td>
                            </tr>
                        </table>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

