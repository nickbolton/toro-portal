<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- New stylesheet for Requirement OA 2.1 -->
	<!-- Include -->
	<xsl:include href="common.xsl"/>
	<xsl:template match="offering">
		<xsl:call-template name = "autoFormJS" />
		<xsl:call-template name="links"/>
		<form method="post" onSubmit="return validator.applyFormRules(this, new OfferingAdminRulesObject())" name="offeringAdminForm" action="{$baseActionURL}">
			<input type="hidden" name="command" value="{$copySubmitCommand}"/>
			<input type="hidden" name="ID" value="{@id}"/>
			<!-- UniAcc: Layout Table -->
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<th colspan="2" class="th-top" id="OACCopyOffering">Copy Offering</th>
				</tr>
				<!-- Topic Name -->
				<tr>
					<td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap" id="oacTopicName">
						Topic Name
					</td>
					<td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%" headers="oacTopicName">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:value-of select="topic/name"/>
						<!-- store the topic ID in the topicNameParam instead of the name -->
						<input name="{$topicNameParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="topic/@id"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Offering Name -->
				<tr>
					<td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap">
						<label for="{$offeringNameParam}_OffName">Offering Name</label>
					</td>
					<td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%">
						<input name="{$offeringNameParam}" type="text" class="text" id="{$offeringNameParam}_OffName">
							<xsl:attribute name="value"><xsl:value-of select="concat('Copy of ',name)"/></xsl:attribute>
						</input>
						<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
					</td>
				</tr>
				<!-- Offering Description -->
				<tr>
					<td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap" id="oacOfferingDesc">
						<label for="{$offeringDescParam}_text">Offering Description</label>
					</td>
					<td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:value-of select="description"/>
						<input name="{$offeringDescParam}" type="hidden" id="{$offeringDescParam}_text">
							<xsl:attribute name="value"><xsl:value-of select="description"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Enrollment Model -->
				<tr>
					<td class="table-light-left" style="text-align:right;vertical-align:top;" id="oacUserEnroll">
          				User Enrollment Model
					</td>
					<td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%" headers="oacUserEnroll">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:value-of select="enrollmentModel"/>
						<input name="{$userEnrollmentModelParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="enrollmentModel"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Default User Role -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="oacUserDefaultPerm">
          				Enrolled User Default Permissions
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="oacUserDefaultPerm">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:value-of select="role"/>
						<!-- store the role ID in the defaultRoleParam instead of the name -->
						<input name="{$defaultRoleParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="role/@id"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering ID -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="oacOfferingID">
						Offering ID
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="oacOfferingID">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:value-of select="optionalOfferingId"/>
						<input name="{$offeringIdParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingId"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering Term -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="oacOfferingTerm">
						Offering Term
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="oacOfferingTerm">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:value-of select="optionalOfferingTerm"/>
						<input name="{$offeringTermParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingTerm"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering Start Date -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="oacOfferingStartDate">
						Offering Start Date
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="oacOfferingStartDate">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:choose>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 1">
								January 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 2">
								February 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 3">
								March 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 4">
								April 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 5">
								May 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 6">
								June 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 7">
								July 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 8">
								August 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 9">
								September 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 10">
								October 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 11">
								November 
							</xsl:when>
							<xsl:when test="optionalOfferingStartDate/MonthStart = 12">
								December 
							</xsl:when>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="optionalOfferingStartDate/MonthStart &gt; 0">
								<xsl:value-of select="optionalOfferingStartDate/DayStart"/>
									, 
								<xsl:value-of select="optionalOfferingStartDate/YearStart"/>
							</xsl:when>
						</xsl:choose>
						<input name="{$offeringMonthStartParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingStartDate/MonthStart"/></xsl:attribute>
						</input>
						<input name="{$offeringDayStartParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingStartDate/DayStart"/></xsl:attribute>
						</input>
						<input name="{$offeringYearStartParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingStartDate/YearStart"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering End Date -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="oacOfferingEndDate">
						Offering End Date
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="oacOfferingEndDate">
						<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:choose>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 1">
								January 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 2">
								February 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 3">
								March 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 4">
								April 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 5">
								May 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 6">
								June 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 7">
								July 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 8">
								August 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 9">
								September 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 10">
								October 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 11">
								November 
							</xsl:when>
							<xsl:when test="optionalOfferingEndDate/MonthEnd = 12">
								December 
							</xsl:when>
						</xsl:choose>
						<xsl:choose>
							<xsl:when test="optionalOfferingEndDate/MonthEnd &gt; 0">
								<xsl:value-of select="optionalOfferingEndDate/DayEnd"/>
								, 
								<xsl:value-of select="optionalOfferingEndDate/YearEnd"/>
							</xsl:when>
						</xsl:choose>
						<input name="{$offeringMonthEndParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingEndDate/MonthEnd"/></xsl:attribute>
						</input>
						<input name="{$offeringDayEndParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingEndDate/DayEnd"/></xsl:attribute>
						</input>
						<input name="{$offeringYearEndParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingEndDate/YearEnd"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering Meeting Days -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="oacOfferingMeetings">
						Offering Meeting Days
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="oacOfferingMeetings">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsMonday = 1">
								<input type="checkbox" name="Monday" value="1" checked="checked" disabled="disabled" id="oacMonday">
									<label for="oacMonday">Monday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Monday" value="1" disabled="disabled" id="oacMonday">
									<label for="oacMonday">Monday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<br />
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsTuesday = 1">
								<input type="checkbox" name="Tuesday" value="1" checked="checked" disabled="disabled" id="oacTuesday">
									<label for="oacTuesday">Tuesday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Tuesday" value="1" disabled="disabled" id="oacTuesday">
									<label for="oacTuesday">Tuesday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<br />
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsWednesday = 1">
								<input type="checkbox" name="Wednesday" value="1" checked="checked" disabled="disabled" id="oacWednesday">
									<label for="oacWednesday">Wednesday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Wednesday" value="1" disabled="disabled" id="oacWednesday">
									<label for="oacWednesday">Wednesday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<br />
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsThursday = 1">
								<input type="checkbox" name="Thursday" value="1" checked="checked" disabled="disabled" id="oacThursday">
									<label for="oacThursday">Thursday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Thursday" value="1" disabled="disabled" id="oacThursday">
									<label for="oacThursday">Thursday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<br />
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsFriday = 1">
								<input type="checkbox" name="Friday" value="1" checked="checked" disabled="disabled" id="oacFriday">
									<label for="oacFriday">Friday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Friday" value="1" disabled="disabled" id="oacFriday">
									<label for="oacFriday">Friday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<br />
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsSaturday = 1">
								<input type="checkbox" name="Saturday" value="1" checked="checked" disabled="disabled" id="oacSaturday">
									<label for="oacSaturday">Saturday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Saturday" value="1" disabled="disabled" id="oacSaturday">
									<label for="oacSaturday">Saturday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<br />
						<xsl:choose>
							<xsl:when test="optionalOfferingMeetingDays/MeetsSunday = 1">
								<input type="checkbox" name="Sunday" value="1" checked="checked" disabled="disabled" id="oacSunday">
									<label for="oacSunday">Sunday</label>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="checkbox" name="Sunday" value="1" disabled="disabled" id="oacSunday">
									<label for="oacSunday">Sunday</label>
								</input>
							</xsl:otherwise>
						</xsl:choose>
						<input type="hidden" name="{$offeringMtgMonParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsMonday"/></xsl:attribute>
						</input>
						<input type="hidden" name="{$offeringMtgTueParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsTuesday"/></xsl:attribute>
						</input>
						<input type="hidden" name="{$offeringMtgWedParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsWednesday"/></xsl:attribute>
						</input>
						<input type="hidden" name="{$offeringMtgThuParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsThursday"/></xsl:attribute>
						</input>
						<input type="hidden" name="{$offeringMtgFriParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsFriday"/></xsl:attribute>
						</input>
						<input type="hidden" name="{$offeringMtgSatParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsSaturday"/></xsl:attribute>
						</input>
						<input type="hidden" name="{$offeringMtgSunParam}">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingMeetingDays/MeetsSunday"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering Start Time -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="OACOfferingStartTime">
						Offering Start Time
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="OACOfferingStartTime">
						<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
						<xsl:choose>
							<xsl:when test="optionalOfferingStartTime/HourStart &gt; 0">
								<xsl:value-of select="optionalOfferingStartTime/HourStart"/>
								:
								<xsl:choose>
									<xsl:when test="optionalOfferingStartTime/MinuteStart &lt; 10">
										<xsl:value-of select="concat('0', optionalOfferingStartTime/MinuteStart)"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="optionalOfferingStartTime/MinuteStart"/>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="optionalOfferingStartTime/AmPmStart = 1">
										AM
									</xsl:when>
									<xsl:otherwise>
										PM
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
						</xsl:choose>
						<input name="{$offeringHourStartParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingStartTime/HourStart"/></xsl:attribute>
						</input>
						<input name="{$offeringMinuteStartParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingStartTime/MinuteStart"/></xsl:attribute>
						</input>
						<input name="{$offeringAmPmStartParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingStartTime/AmPmStart"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering End Time -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="OACOfferingEndTime">
						Offering End Time
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="OACOfferingEndTime">
						<xsl:choose>
							<xsl:when test="optionalOfferingEndTime/HourEnd &gt; 0">
								<xsl:value-of select="optionalOfferingEndTime/HourEnd"/>
									:
								<xsl:choose>
									<xsl:when test="optionalOfferingEndTime/MinuteEnd &lt; 10">
										<xsl:value-of select="concat('0', optionalOfferingEndTime/MinuteEnd)"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="optionalOfferingEndTime/MinuteEnd"/>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:choose>
									<xsl:when test="optionalOfferingEndTime/AmPmEnd = 1">
										AM
									</xsl:when>
									<xsl:otherwise>
										PM
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
						</xsl:choose>
						<input name="{$offeringHourEndParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingEndTime/HourEnd"/></xsl:attribute>
						</input>
						<input name="{$offeringMinuteEndParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingEndTime/MinuteEnd"/></xsl:attribute>
						</input>
						<input name="{$offeringAmPmEndParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingEndTime/AmPmEnd"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- Optional Offering Location  -->
				<tr>
					<td class="table-light-left-bottom" style="text-align:right;vertical-align:top;" id="OACOfferingLocation">
						Offering Location
					</td>
					<td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%" headers="OACOfferingLocation">
						<xsl:value-of select="optionalOfferingLocation"/>
						<input name="{$offeringLocationParam}" type="hidden">
							<xsl:attribute name="value"><xsl:value-of select="optionalOfferingLocation"/></xsl:attribute>
						</input>
					</td>
				</tr>
				<!-- End Changes for Requirements OA 4.1 - 4.12 -->
				<tr>
					<td colspan="2" class="table-nav">
						<input type="submit" class="uportal-button" value="Submit" title="Create this new offering"/>
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel the creation of this offering"/>
					</td>
				</tr>
			</table>
		</form>
		<!--<xsl:call-template name="offeringSearch"/>
  </xsl:template>

  <xsl:template match="channel">
    <li><xsl:value-of select="."/></li>
  </xsl:template>

  <xsl:template match="role">
    <xsl:if test="@default = 'true'">
      <xsl:value-of select="."/>
    </xsl:if> -->
	</xsl:template>
</xsl:stylesheet>
