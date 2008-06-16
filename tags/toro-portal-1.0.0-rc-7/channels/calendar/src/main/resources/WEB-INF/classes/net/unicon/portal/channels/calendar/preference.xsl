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
    <xsl:variable name="curCalendar" select="calendar-system/view/calendar" />
    <xsl:variable name="cur-user">
        <xsl:value-of select="calendar-system/logon/@user" />
    </xsl:variable>
    <xsl:include href="common.xsl" />
    <xsl:param name="window">preference</xsl:param>
    <xsl:include href="navigation-bar.xsl" />
    <xsl:include href="access-detail.xsl" />
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    <xsl:param name="main" />
    <xsl:key name="calhidden" match="/calendar-system/preference/calendar-hidden" use="@calid" />
	
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
					<td style="padding:0px;" valign="top" width="40%">
	<!-- left screen: calendar and list calendar -->
						<!--UniAcc: Layout Table -->
						<table border="0" cellpadding="0" cellspacing="0" width="100%">
							<tr>
								<td class="th" colspan="2" nowrap="nowrap">Composite View Configuration 
								<a href="{$goURL}=GroupEdit" title="Add">
									<img id="add{$channelID}" style="border-style: none; border-width: 0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" align="absmiddle" onmouseover="swapImage('add{$channelID}','channel_add_active.gif')" onmouseout="swapImage('add{$channelID}','channel_add_base.gif')" alt="Add a new view" title="Add a new view" />
								</a>
								</td>
							</tr>
							<tr>
								<xsl:if test="//preference/@type = 'composite'">
									<xsl:if test="//preference/@saved = 'true'">
										<td align="center"><b><font color="red">Preferences saved successfully.</font></b></td>
									</xsl:if>
								</xsl:if>
								<xsl:if test="//preference/@type = 'composite'">
									<xsl:if test="//preference/@saved = 'false'">
										<td align="center"><b><font color="red">An error occurred while attempting to save preferences.</font></b></td>
									</xsl:if>
								</xsl:if>
							</tr>
							<tr>
								<td>
									<xsl:for-each select="//preference/calendar-group">
										<xsl:variable name="idx" select="@id" />
										<form action="{$baseActionURL}" method="post">
											<!--UniAcc: Layout Table -->
											<table border="0" cellpadding="0" cellspacing="0" width="100%">
												<xsl:call-template name="mform" />
												<tr>
													<td class="table-light-left" colspan="2" nowrap="nowrap">&#160;
														<xsl:value-of select="@name" />
														<input type="hidden" name="id" value="{@id}" />
														<input type="hidden" name="name" value="{@name}" />
														<input type="image" style="border-style: none; border-width: 0" name='do~editGroup' id="{@id}{$channelID}" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" align="absmiddle"  onmouseover="swapImage('{@id}{$channelID}','channel_edit_active.gif')" onmouseout="swapImage('{@id}{$channelID}','channel_edit_base.gif')" title="Edit Calendar" alt="Edit Calendar"/>
														<input type="image" style="border-style: none; border-width: 0" name='do~deleteGroup' id="{@id}delete{$channelID}" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" align="absmiddle" onmouseover="swapImage('{@id}delete{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('{@id}delete{$channelID}','channel_delete_base.gif')" title="Delete Calendar" alt="Delete Calendar"/>
													</td>
												</tr>
												<xsl:for-each select="//calendar-system/calendar[not(key('calhidden', @calid))]">
													<tr>
														<td align="center" class="table-content-left" width="5%">
															<input name="calid_{@calid}" type="checkbox" value="ON" id="CP-ViewConfigC{@calid}{$idx}">
																<xsl:if test="//preference/calendar-group[@id=$idx]/calendar/@calid = @calid">
																	<xsl:attribute name="checked" />
																</xsl:if>
															</input>
														</td>
														<td class="table-content-right">
														<label for="CP-ViewConfigC{@calid}{$idx}">
															&#160;
															<xsl:choose>
																<xsl:when test="@calid=$cur-user">
																	<span class="uportal-background-highlight">
																		<xsl:value-of select="@calname" />
																	</span>
																</xsl:when>
																<xsl:when test="@owner!=$cur-user">
																	<xsl:choose>
																		<xsl:when test="@owner=@calname">
																			<xsl:value-of select="@calname" />
																		</xsl:when>
																		<xsl:otherwise>
																			<xsl:value-of select="concat(@owner, ':', @calname)" />
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:when>
																<xsl:otherwise>
																	<xsl:value-of select="@calname" />
																</xsl:otherwise>
															</xsl:choose>
														</label>
														</td>
													</tr>
												</xsl:for-each>
												<tr>
													<td align="center" class="table-content-single" colspan="2">
														<input name="default" type="hidden" value="do~save" />
														<input class="uportal-button" name="do~save" title="Save" type="submit" value="Save" />
													</td>
												</tr>
											</table>
										</form>
									</xsl:for-each>
								</td>
							</tr>
							<tr>
								<td>
									<form action="{$baseActionURL}" method="post">
										<!--UniAcc: Layout Table -->
										<table border="0" cellpadding="0" cellspacing="0" width="100%">
											<xsl:call-template name="mform" />
											<tr>
												<td class="th" colspan="2" nowrap="nowrap">Hide Shared Calendar</td>
											</tr>
											<tr>
												<xsl:if test="//preference/@type = 'hidden'">
													<xsl:if test="//preference/@saved = 'true'">
														<td align="center" colspan="2"><b><font color="red">Preferences saved successfully.</font></b></td>
													</xsl:if>
												</xsl:if>
												<xsl:if test="//preference/@type = 'hidden'">
													<xsl:if test="//preference/@saved = 'false'">
														<td align="center" colspan="2"><b><font color="red">An error occurred while attempting to save preferences.</font></b></td>
													</xsl:if>
												</xsl:if>										
											</tr>
											<xsl:for-each select="//calendar-system/calendar[@owner!=$cur-user]">
												<tr>
													<td align="center" class="table-content-left" width="5%">
														<input name="calid_hidden_{@calid}" type="checkbox" value="ON" id="CP-HideSharedCalC{@calname}">
															<xsl:if test="//preference/calendar-hidden/@calid = @calid">
																<xsl:attribute name="checked" />
															</xsl:if>
														</input>
													</td>
													<td class="table-content-right">
														<label for="CP-HideSharedCalC{@calname}">
														&#160;
														<xsl:value-of select="concat(@owner, ':', @calname)" />
														</label>
													</td>
												</tr>
											</xsl:for-each>
											<xsl:if test="//preference/@type != 'hidden' or not(//preference/@type)">
 											  <tr>
												<td align="center" class="table-content-single" colspan="2">
												  <input name="default" type="hidden" value="do~saveHidden" />
												  <input class="uportal-button" name="do~saveHidden" title="Save" type="submit" value="Save" />
												</td>
											  </tr>
											</xsl:if>
										</table>
									</form>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>
<xsl:template match="saved">
	<td><b><font color="red"><xsl:value-of select="saved" />&#160;testing</font></b></td>
</xsl:template>
</xsl:stylesheet>

