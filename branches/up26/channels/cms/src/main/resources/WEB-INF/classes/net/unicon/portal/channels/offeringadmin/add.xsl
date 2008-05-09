<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

<xsl:template match="offeringAdmin">
    <!--<textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/>
    </textarea> -->

    <xsl:call-template name="autoFormJS"/>
    <xsl:call-template name="links"/>
     <form method="post" onSubmit="return validator.applyFormRules(this, new OfferingAdminRulesObject())" name="offeringAdminForm" action="{$baseActionURL}">
         <input type="hidden" name="command" value="{$addSubmitCommand}" />
         <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
         <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <th colspan="2" class="th-top">
                    Add Offering
                </th>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:right" nowrap="nowrap">
                    <label for="offeringAdminFormTopicName">Topic Name</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                    <select name="topicNameParam" id="offeringAdminFormTopicName">
                        <xsl:apply-templates select="topic">
                            <xsl:sort select="name"/>
                        </xsl:apply-templates>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormOfferingName">Offering Name</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                     <input name="offeringName" type="text" class="text" id="offeringAdminFormOfferingName" size="70" maxlength="80" />
                </td>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:center;vertical-align:top;" nowrap="nowrap">
                    <label for="offeringAdminFormOfferingDescription">Offering Description</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                    <textarea name="offeringDescription" rows="4" id="offeringAdminFormOfferingDescription"></textarea>
                </td>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormUserEnrollmentModel">User Enrollment Model</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                    <select name="{$userEnrollmentModelParam}" id="offeringAdminFormUserEnrollmentModel">
                        <xsl:apply-templates select="enrollmentModel">
                            <xsl:with-param name="forAction">add</xsl:with-param>
                        </xsl:apply-templates>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    Enroll Self in Offering
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                     <input type="checkbox" class="radio" name="{$enrollSelfParam}" value="true"
                     id="offeringAdminFormEnrollSelf">
                         <xsl:if test="$enrollSelfCheckedParam = 'true'">
		             <xsl:attribute name="checked">checked</xsl:attribute>
                         </xsl:if>
                     </input>
                     &#160;
                     <label for="offeringAdminFormEnrollSelf">Enroll</label>
                </td>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormSelfDefaultType">Self's Default Type (if enrolled)</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                    <select name="{$selfRoleParam}" id="offeringAdminFormSelfDefaultType">
                        <xsl:apply-templates select="role">
                            <xsl:with-param name="useDefaultType">false</xsl:with-param>
                        </xsl:apply-templates>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormEnrolledUserDefaultType">Enrolled User Default Type</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                    <select name="{$defaultRoleParam}" id="offeringAdminFormEnrolledUserDefaultType">
                        <xsl:apply-templates select="role"/>
                    </select>
                </td>
            </tr>

            <!-- Build ensemble fields -->
            <xsl:apply-templates select="ensemble"/>

            <!-- Start New functionality for Requirements OA 4.1-4.12 -->
            <!-- Offering ID -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormOfferingID">Offering ID</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                    <input name="offeringIdParam" type="text" size="15" maxlength="15"
                    class="text" id="offeringAdminFormOfferingID"/>
                </td>
            </tr>
            <!-- Offering Term -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormOfferingTerm">Offering Term</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                     <input name="{$offeringTermParam}" type="text" size="25" maxlength="25"
                    class="text" id="offeringAdminFormOfferingTerm" />
                </td>
            </tr>
            <!-- Offering Start Date -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap" id="offeringAdminFormOfferingStartDate">
                    <label for="offeringAdminFormOfferingStartMonth">Offering Start Date</label>
                </td>
                <td class="table-content-right" headers="offeringAdminFormOfferingStartDate">
                    <select name="offeringMonthStartParam" id="offeringAdminFormOfferingStartMonth" title="Offering start month">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="monthOptions" />
                    </select>
                    <select name="offeringDayStartParam" title="Offering start day">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="dayOptions" />
                    </select>
                    <select name="offeringYearStartParam" title="Offering start year">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="yearOptions" />
                    </select><br/>
                </td>
            </tr>
            <!-- Offering End Date -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap" id="offeringAdminFormOfferingEndDate">
                    <label for="offeringAdminFormOfferingEndMonth">Offering End Date</label>
                </td>
                <td class="table-content-right" headers="offeringAdminFormOfferingEndDate">
                    <select name="offeringMonthEndParam" id="offeringAdminFormOfferingEndMonth" title="Offering end month">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="monthOptions" />
                    </select>
                    <select name="offeringDayEndParam" title="Offering end day">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="dayOptions" />
                    </select>
                    <select name="offeringYearEndParam" title="Offering end year">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="yearOptions" />
                    </select><br/>
                </td>
            </tr>
            <!-- Offering Meeting Days -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap" id="offeringAdminFormOfferingMeetingDays">
                    Offering Meeting Days
                </td>
                <td class="table-content-right" style="text-align:left" width="100%" headers="offeringAdminFormOfferingMeetingDays">
                     <input type="checkbox" class="radio" name="{$offeringMtgMonParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysMonday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysMonday">Monday</label></input>&#160;
                     <br /><input type="checkbox" class="radio" name="{$offeringMtgTueParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysTuesday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysTuesday">Tuesday</label></input>&#160;
                     <br /><input type="checkbox" class="radio" name="{$offeringMtgWedParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysWednesday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysWednesday">Wednesday</label></input>&#160;
                     <br /><input type="checkbox" class="radio" name="{$offeringMtgThuParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysThursday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysThursday">Thursday</label></input>&#160;
                     <br /><input type="checkbox" class="radio" name="{$offeringMtgFriParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysFriday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysFriday">Friday</label></input>&#160;
                     <br /><input type="checkbox" class="radio" name="{$offeringMtgSatParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysSaturday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysSaturday">Saturday</label></input>&#160;
                     <br /><input type="checkbox" class="radio" name="{$offeringMtgSunParam}" value="1"
                     id="offeringAdminFormOfferingMeetingDaysSunday">&#160;
                     <label for="offeringAdminFormOfferingMeetingDaysSunday">Sunday</label></input>&#160;
                </td>
            </tr>
            <!-- Offering Start Time -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap" id="offeringAdminFormOfferingStartTime">
                    <label for="offeringAdminFormOfferingStartTimeHour">Offering Start Time</label>
                </td>
                <td class="table-content-right" headers="offeringAdminFormOfferingStartTime">
                    <select name="{$offeringHourStartParam}" id="offeringAdminFormOfferingStartTimeHour" title="Offering start hour">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="hourOptions" />
                    </select><b> : </b>
                    <select name="{$offeringMinuteStartParam}" title="Offering start minute">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="minuteOptions" />
                    </select>
                    <select name="{$offeringAmPmStartParam}" title="Offering start time of day">
                        <option value="1" selected="selected">AM</option>
                        <option value="2">PM</option>
                    </select>
                </td>
            </tr>
            <!-- Offering End Time -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap" id="offeringAdminFormOfferingEndTime">
                    <label for="offeringAdminFormOfferingEndTimeHour">Offering End Time</label>
                </td>
                <td class="table-content-right" headers="offeringAdminFormOfferingEndTime">
                    <select name="{$offeringHourEndParam}" id="offeringAdminFormOfferingEndTimeHour" title="Offering end hour">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="hourOptions" />
                    </select><b> : </b>
                    <select name="{$offeringMinuteEndParam}" title="Offering end minute">
                        <!-- Template located in common.xsl -->
                        <xsl:call-template name="minuteOptions" />
                    </select>
                    <select name="{$offeringAmPmEndParam}" title="Offering end time of day">
                        <option value="1" selected="selected">AM</option>
                        <option value="2">PM</option>
                    </select>
                </td>
            </tr>
            <!-- Offering Location -->
            <tr>
                <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                    <label for="offeringAdminFormOfferingLocation">Offering Location</label>
                </td>
                <td class="table-content-right" style="text-align:left" width="100%">
                     <input name="{$offeringLocationParam}" type="text" size="25" maxlength="25" class="text"
                     id="offeringAdminFormOfferingLocation" />
                </td>
            </tr>
            <tr>
                <td colspan="2" class="table-nav">
                    <xsl:choose>
                    <xsl:when test="$navigateRemoveMessage = 'true'">
                    <span class="uportal-channel-warning">NOTE:</span> The new Offering will not be added to the Navigation Channel until the next time it is refreshed (to refresh the navigation channel, click the Learning tab).
                    </xsl:when>
                    </xsl:choose>
                    <nobr>
                         <input type="submit" class="uportal-button" value="Submit" title="Submit new offering"/>
                         <input type="button" class="uportal-button" value="Cancel"
                        onclick="window.locationhref='{$baseActionURL}'"
                        title="Cancel new offering"/>
                    </nobr>
                </td>
            </tr>
            <!-- End New functionality for Requirements OA 4.1-4.12 -->

            <!-- functionality not supported yet
            <tr>
            <td class="uportal-background-light" style="vertical-align:top;">List of Channels</td>
            <td class="uportal-background-light">
            <xsl:apply-templates select="channel"/>
            </td>
            </tr>
            -->
         </table>
     </form>

    </xsl:template>

    <xsl:template match="ensemble">
        <tr>
            <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                Course Catalog settings
            </td>
            <td class="table-content-right" style="text-align:left" width="100%">
                    <input type="checkbox" class="radio" name="published" value="false"
                    id="published">
                        <xsl:if test="@published = 'true'">
                            <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                        <label for="published">Published</label>
                    </input>
                     &#160;
                    <input type="checkbox" class="radio" name="buy_now" value="false"
                    id="buy_now">
                        <xsl:if test="@buyNowEnabled = 'true'">
                            <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                        <label for="buy_now">"Buy Now" Enabled</label>
                    </input>
                    &#160;
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
