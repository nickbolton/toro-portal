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
    <xsl:include href="common.xsl" />
    <xsl:include href="../../global/toolbar.xsl" />
<!-- //////////////////////////////////////////// -->
    <xsl:param name="Title">default</xsl:param>
    <xsl:param name="SDTitle">default</xsl:param>
    <xsl:param name="Anonymous">default</xsl:param>
    <xsl:param name="Type">default</xsl:param>
    <xsl:param name="Noti">default</xsl:param>
    <xsl:param name="Mail">default</xsl:param>
    <xsl:template match="survey-system">

            <xsl:call-template name="autoFormJS" />
            <form action="{$baseActionURL}" method="post" name="distributionForm">
                <input type="hidden" name="sid" value="{$sid}" />
                <!--UniAcc: Layout Table -->
               
                <div class="gradient-page-title">
                	Publish <xsl:value-of select="$Title" />
                </div>
                <div class="bounding-box1">

					<table cellpadding="0" cellspacing="2" width="100%" border="0">
						<tr>
							<td colspan="2"> 
								<span class="survey-title">
									Select a method to publish <xsl:value-of select="$Title"/>
								</span>
							</td>
						</tr>
						<tr>
							<td class="table-content-left" nowrap="nowrap" align="right" width="5%">
								<xsl:choose>
									<xsl:when test="$Type='Poll' or count(ViewTarget)=0">
										<input type="radio" name="Type" value="Poll" checked="checked" id="SD-PublicPollR1" onclick="ToggleDistribution('poll')"/>
									</xsl:when>
									<xsl:otherwise>
										<input type="radio" name="Type" value="Poll" id="SD-PublicPollR1" onclick="ToggleDistribution('poll')" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="table-content-right">
								<label for="SD-PublicPollR1"><strong>As a Public Poll</strong></label>
							</td>
						</tr>
						<tr>
							<td class="table-content-left" nowrap="nowrap" align="right">
								<xsl:choose>
									<xsl:when test="$Type='Recipient'">
										<input type="radio" name="Type" value="Recipient" checked="checked" id="SD-SelectedRecipR1" onclick="ToggleDistribution('recipient')" />
									</xsl:when>
									<xsl:otherwise>
										<input type="radio" name="Type" value="Recipient" id="SD-SelectedRecipR1" onclick="ToggleDistribution('recipient')" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="table-content-right" nowrap="nowrap" align="left">
								<label for="SD-SelectedRecipR1"><strong>As a Targeted Survey</strong></label>
							</td>
						</tr>
						<tr>
							<td></td>
							<td>
								<div class="bounding-box1">	
									<div id="recipientOptions">
										<xsl:attribute name="style">
											<xsl:choose>
												<xsl:when test="ViewTarget or $Type='Recipient'">
												</xsl:when>
												<xsl:otherwise>
													display:none;
												</xsl:otherwise>
											</xsl:choose>
										</xsl:attribute>
										
										<div class="survey-editor-subtitle">
											Survey Options
										</div>
										<div>
											<div>
												Notify recipients of this survey by:
											</div>
											<div class="indent">
												<xsl:choose>
													<xsl:when test="$Noti='default'">
														<input type="checkbox" name="Noti" value="Noti" id="SD-NotificationC1" />
														<label for="SD-NotificationC1">Notification</label>
													</xsl:when>
													<xsl:otherwise>
														<input type="checkbox" name="Noti" value="Noti" checked="checked" id="SD-NotificationC1" />
														<label for="SD-NotificationC1">Notification</label>
													</xsl:otherwise>
												</xsl:choose>
												<br/>
												<xsl:choose>
													<xsl:when test="$Mail='default'">
														<input type="checkbox" name="Mail" value="Mail" id="SD-EmailC1" />
														<label for="SD-EmailC1">&#160;Email</label>
													</xsl:when>
													<xsl:otherwise>
														<input type="checkbox" name="Mail" value="Mail" checked="checked" id="SD-EmailC1" />
														<label for="SD-EmailC1">&#160;Email</label>
													</xsl:otherwise>
												</xsl:choose>
											</div>
											<br/>
											<div>
												Survey responses will be stored:
											</div>
											<div class="indent">
												<xsl:choose>
													<xsl:when test="$Anonymous='Anonymous'">
														<input type="radio" name="Anonymous" value="Anonymous" checked="checked" id="SD-AnonymousR1"/>
														<label for="SD-AnonymousR1">Anonymously</label>
													</xsl:when>
													<xsl:otherwise>
														<input type="radio" name="Anonymous" value="Anonymous" id="SD-AnonymousR1"/>
														<label for="SD-AnonymousR1">Anonymously</label>
													</xsl:otherwise>
												</xsl:choose>
												<br/>
												<xsl:choose>
													<xsl:when test="$Anonymous='Named'">
														<input type="radio" name="Anonymous" value="Named" checked="checked" id="SD-NamedR1"/>
														<label for="SD-NamedR1">Named</label>
													</xsl:when>
													<xsl:otherwise>
														<input type="radio" name="Anonymous" value="Named" id="SD-NamedR1"/>
														<label for="SD-NamedR1">Named</label>
													</xsl:otherwise>
												</xsl:choose>
												<br/>
												<xsl:choose>
													<xsl:when test="$Anonymous='Election'">
														<input type="radio" name="Anonymous" value="Election" checked="checked" id="SD-ElectionR1"/>
														<label for="SD-ElectionR1">As an Election</label>
													</xsl:when>
													<xsl:otherwise>
														<input type="radio" name="Anonymous" value="Election" id="SD-ElectionR1"/>
														<label for="SD-ElectionR1">As an Election</label>
													</xsl:otherwise>
												</xsl:choose>
											</div>
											
											<div style="margin-top:10pt;">
												<!-- LIST CURRENT RECIPIENTS -->
												<div class="survey-editor-subtitle">
													Recipients
												</div>
												<xsl:choose>
													<xsl:when test="ViewTarget">
														<xsl:for-each select="ViewTarget">
															<div class="small-pad-tb">
																<xsl:if test="position() mod 2 = 1">
																	<xsl:attribute name="style">background-color:#EFEFEF;</xsl:attribute>
																</xsl:if>
																<xsl:choose>
																	<xsl:when test="@itype='G'">
																		<img src="{$baseImagePath}/persons_16.gif" align='absmiddle' border="0" alt="group named:" title="group named:" />
																	</xsl:when>
																	<xsl:otherwise>
																		<img src="{$baseImagePath}/person_16.gif" align='absmiddle' border="0" alt="individual named:" title="individual named:" />
																	</xsl:otherwise>
																</xsl:choose>
																<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
																<xsl:value-of select="@iname" />
																<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                                                                <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" name="do~deleteRecipient&amp;ientity={@ientity}&amp;iname={@iname}&amp;iid={@iid}&amp;itype={@itype}" id="SurveyDeleteRecipient_{generate-id(.)}" align="absmiddle" onmouseover="swapImage('SurveyDeleteRecipient_{generate-id(.)}','channel_delete_active.gif')" onmouseout="swapImage('SurveyDeleteRecipient_{generate-id(.)}','channel_delete_base.gif')">
																	<xsl:attribute name="title" >
																		<xsl:choose>
																			<xsl:when test="@itype='G'">Delete group:<xsl:value-of select="@iname" /></xsl:when>
																			<xsl:otherwise>Delete recipient:<xsl:value-of select="@iname" /></xsl:otherwise>
																		</xsl:choose>
																	 </xsl:attribute>
																	 <xsl:attribute name="alt">
																		<xsl:choose>
																			<xsl:when test="@itype='G'">Delete group:<xsl:value-of select="@iname" /></xsl:when>
																			<xsl:otherwise>Delete recipient:<xsl:value-of select="@iname" /></xsl:otherwise>
																		</xsl:choose>
																	 </xsl:attribute>
																</input>
															</div>
														</xsl:for-each>
													</xsl:when>
													<xsl:otherwise>
														<div style="margin:5pt;">No recipients have been selected.</div>
														
													</xsl:otherwise>
												</xsl:choose>
												<div class="small-pad-tb">
													<input type="submit" name="do~addTarget" value="Add Recipients" alt="Add a recepient or group of recipients" />
												</div>
											</div>
										</div>
									</div>
								</div>
							</td>
						</tr>
						<tr>
							<td nowrap="nowrap" align="left" colspan="2">
								<label for="SD-Description">Give this distribution of <xsl:value-of select="$Title"/> a title:</label>
							</td>
						</tr>
						<tr>
							<td></td>
							<td class="small-pad-tb">
								<xsl:choose>
									<xsl:when test="$SDTitle != 'default'">
										<input type="text" size="30" name="SDTitle" maxlength="80" value="{$SDTitle}" class="uportal-input-text" id="SD-Title"/>
									</xsl:when>
									<xsl:otherwise>
										<input type="text" size="30" name="SDTitle" maxlength="80" class="uportal-input-text" id="SD-Title"/>
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>						
						<tr>
							<td colspan="2" align='center'>
								<div class="submit-container">
									<input type="submit" name="do~OK" value="OK" onclick="return WarnUntitledDistribution(this.form);" />
									<input type="submit" name="do~Cancel" value="Cancel" />
								</div>
							</td>
						</tr>
					</table>
				</div>
            </form>
    </xsl:template>
</xsl:stylesheet>

