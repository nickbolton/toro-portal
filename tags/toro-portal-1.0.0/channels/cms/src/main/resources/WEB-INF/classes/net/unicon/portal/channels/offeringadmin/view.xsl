<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="offering">

    <xsl:call-template name="links"/>

    <form name="offeringAdminForm">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
      <th colspan="2" class="th-top">Offering Details</th>
    </tr>

    <tr>
      <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap">Topic Name</td>
      <td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%">
          <xsl:value-of select="topic/name"/>&#160;
      </td>
    </tr>

    <tr>
      <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap">Offering Name</td>
      <td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%">
        <xsl:choose>
        <xsl:when test="$editOffering = 'Y' and enrollmentModel != 'sis'">
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={@id}" title="Edit this offering"
            onmouseover="swapImage('offeringAdminEditImage','channel_edit_active.gif')"
            onmouseout="swapImage('offeringAdminEditImage','channel_edit_base.gif')">
            <xsl:value-of select="name"/>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <img border="0" src=
            "{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
            alt="'Edit' icon: edit offering details for '{name}'"
            title="'Edit' icon: edit offering details for '{name}'" align="absmiddle" name="offeringAdminEditImage" id="offeringAdminEditImage"/></a>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="name"/>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <img border="0" src=
            "{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
            alt="Inactive 'Edit' icon: edit offering details unavailable due to lack of permission"
            title="Inactive 'Edit' icon: edit offering details unavailable due to lack of permission" align="absmiddle"/>
        </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="$exportOffering = 'Y'">
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                <a href="{$workerActionURL}&amp;ID={@id}" title="Export this offering" target="hidden_download"
                onmouseover="swapImage('offeringAdminExportImage{@id}','channel_export_active.gif')"
                onmouseout="swapImage('offeringAdminExportImage{@id}','channel_export_base.gif')">
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_base.gif"
                alt="'Export' icon: export offering '{name}'"
                title="'Export' icon: export offering '{name}'"
                align="absmiddle" name="offeringAdminExportImage{@id}" id="offeringAdminExportImage{@id}"/></a>
            </xsl:when>
            <xsl:otherwise>
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_inactive.gif"
                alt="Inactive 'Export' icon: export offering unavailable due to lack of permission"
                title="Inactive 'Export' icon: export offering unavailable due to lack of permission" align="absmiddle"/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
          <xsl:when test="$copyOffering = 'Y' and enrollmentModel != 'sis'">
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$copyCommand}&amp;ID={@id}"
            title="Copy this offering"
            onmouseover="swapImage('offeringAdminCopyImage{@id}','channel_copy_active.gif')"
            onmouseout="swapImage('offeringAdminCopyImage{@id}','channel_copy_base.gif')">
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_copy_base.gif"
                alt="'Copy' icon: copy offering '{name}'"
                title="'Copy' icon: copy offering '{name}'"
                align="absmiddle"
                name="offeringAdminCopyImage{@id}" id="offeringAdminCopyImage{@id}"/>
            </a>
          </xsl:when>
          <xsl:otherwise>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_copy_inactive.gif"
                align="absmiddle"
                alt="Inactive 'Copy' icon: copying offerings unavailable due to lack of permission"
                title="Inactive 'Copy' icon: copying offerings unavailable due to lack of permission"/>
          </xsl:otherwise>
        </xsl:choose>

        <!-- CAN'T IMPLEMENT OFFERING DELETE / INACTIVE / ACTIVE OPTIONS UNLESS PARAMETER OR XML ELEMENT INDICATES WHAT CURRENT STATE IS
        <img border="0" src=
        "{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
        alt=""
        title="The permission to delete this offering is currently unavailable" align="absmiddle"/>
         -->
         &#160;
      </td>
    </tr>

    <tr>
      <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap">Offering Description</td>
      <td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%">
          <xsl:value-of select="description"/>&#160;
      </td>
    </tr>

    <tr>
      <td class="table-light-left" style="text-align:right;vertical-align:top;">User Enrollment Model</td>
      <td class="table-content-right" style="text-align:left;vertical-align:top;" width="100%">
          <xsl:value-of select="enrollmentModel"/>&#160;
      </td>
    </tr>

    <tr>
      <td class="table-light-left-bottom" style="text-align:right;vertical-align:top;">Enrolled User Default Permissions</td>
      <td class="table-content-right-bottom" style="text-align:left;vertical-align:top;" width="100%">
          <xsl:value-of select="role"/>&#160;
      </td>
    </tr>

    <!-- Build ensemble fields -->
    <xsl:apply-templates select="ensemble"/>

<!-- Start Changes for Requirements OA 4.1 - 4.12 -->

<!-- Optional Offering ID -->
    <tr>
      <td class="table-light-left-bottom" style="text-align:right;vertical-align:top;">Offering ID</td>
      <td class="table-content-right-bottom"
          style="text-align:left;vertical-align:top;" width="100%">
           <xsl:value-of select="optionalOfferingId"/>&#160;
      </td>
    </tr>

<!-- Optional Offering Term -->
    <tr>
      <td class="table-light-left-bottom"
          style="text-align:right;vertical-align:top;">Offering Term</td>
      <td class="table-content-right-bottom"
          style="text-align:left;vertical-align:top;" width="100%">
           <xsl:value-of select="optionalOfferingTerm"/>&#160;
      </td>
    </tr>

<!-- Optional Offering Start Date -->
    <tr>
     <td class="table-light-left-bottom"
         style="text-align:right;vertical-align:top;">Offering Start Date</td>
     <td class="table-content-right-bottom"
         style="text-align:left;vertical-align:top;" width="100%">
      <xsl:choose>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 1">
             January </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 2">
             February </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 3">
             March </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 4">
             April </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 5">
             May </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 6">
             June </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 7">
             July </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 8">
             August </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 9">
             September </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 10">
             October </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 11">
             November </xsl:when>
        <xsl:when test="optionalOfferingStartDate/MonthStart = 12">
             December </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="optionalOfferingStartDate/MonthStart &gt; 0">
         <xsl:value-of select="optionalOfferingStartDate/DayStart"/>
         ,
         <xsl:value-of select="optionalOfferingStartDate/YearStart"/>
        </xsl:when>
      </xsl:choose>
      &#160;
     </td>
    </tr>

<!-- Optional Offering End Date -->
    <tr>
     <td class="table-light-left-bottom"
         style="text-align:right;vertical-align:top;">Offering End Date</td>
     <td class="table-content-right-bottom"
         style="text-align:left;vertical-align:top;" width="100%">
      <xsl:choose>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 1">
             January </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 2">
             February </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 3">
             March </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 4">
             April </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 5">
             May </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 6">
             June </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 7">
             July </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 8">
             August </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 9">
             September </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 10">
             October </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 11">
             November </xsl:when>
        <xsl:when test="optionalOfferingEndDate/MonthEnd = 12">
             December </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="optionalOfferingEndDate/MonthEnd &gt; 0">
         <xsl:value-of select="optionalOfferingEndDate/DayEnd"/>
         ,
         <xsl:value-of select="optionalOfferingEndDate/YearEnd"/>
        </xsl:when>
      </xsl:choose>
      &#160;
     </td>
    </tr>

<!-- Optional Offering Meeting Days -->
   <tr>
     <td class="table-light-left-bottom"
       style="text-align:right;vertical-align:top;" id="offeringAdminFormOfferingMeetingDays">Offering Meeting Days</td>
     <td class="table-content-right-bottom"
         style="text-align:left;vertical-align:top;" width="100%" headers="offeringAdminFormOfferingMeetingDays">
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsMonday = 1">
              <input type="checkbox" name="Monday" value="1" id="offeringAdminFormOfferingMeetingDaysMonday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysMonday">Monday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Monday" value="1" id="offeringAdminFormOfferingMeetingDaysMonday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysMonday">Monday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
     <br />
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsTuesday = 1">
              <input type="checkbox" name="Tuesday" value="1" id="offeringAdminFormOfferingMeetingDaysTuesday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysTuesday">Tuesday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Tuesday" value="1" id="offeringAdminFormOfferingMeetingDaysTuesday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysTuesday">Tuesday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
     <br />
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsWednesday = 1">
              <input type="checkbox" name="Wednesday" value="1" id="offeringAdminFormOfferingMeetingDaysWednesday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysWednesday">Wednesday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Wednesday" value="1" id="offeringAdminFormOfferingMeetingDaysWednesday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysWednesday">Wednesday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
     <br />
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsThursday = 1">
              <input type="checkbox" name="Thursday" value="1" id="offeringAdminFormOfferingMeetingDaysThursday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysThursday">Thursday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Thursday" value="1" id="offeringAdminFormOfferingMeetingDaysThursday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysThursday">Thursday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
     <br />
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsFriday = 1">
              <input type="checkbox" name="Friday" value="1" id="offeringAdminFormOfferingMeetingDaysFriday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysFriday">Friday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Friday" value="1" id="offeringAdminFormOfferingMeetingDaysFriday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysFriday">Friday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
         <br />
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsSaturday = 1">
              <input type="checkbox" name="Saturday" value="1" id="offeringAdminFormOfferingMeetingDaysSaturday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysSaturday">Saturday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Saturday" value="1" id="offeringAdminFormOfferingMeetingDaysSaturday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysSaturday">Saturday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
     <br />
         <xsl:choose>
            <xsl:when test="optionalOfferingMeetingDays/MeetsSunday = 1">
              <input type="checkbox" name="Sunday" value="1" id="offeringAdminFormOfferingMeetingDaysSunday"
               checked="checked" disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysSunday">Sunday</label></input>&#160;
            </xsl:when>
            <xsl:otherwise>
              <input type="checkbox" name="Sunday" value="1" id="offeringAdminFormOfferingMeetingDaysSunday"
               disabled="disabled">&#160;<label for="offeringAdminFormOfferingMeetingDaysSunday">Sunday</label></input>&#160;
            </xsl:otherwise>
         </xsl:choose>
     </td>
   </tr>

<!-- Optional Offering Start Time -->
    <tr>
     <td class="table-light-left-bottom"
         style="text-align:right;vertical-align:top;">Offering Start Time</td>
     <td class="table-content-right-bottom"
         style="text-align:left;vertical-align:top;" width="100%">
       <xsl:choose>
         <xsl:when test="optionalOfferingStartTime/HourStart &gt; 0">
           <xsl:value-of select="optionalOfferingStartTime/HourStart"/>
            :
            <xsl:choose>
              <xsl:when test="optionalOfferingStartTime/MinuteStart &lt; 10">
                <xsl:value-of
                  select="concat('0', optionalOfferingStartTime/MinuteStart)"/>
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
       &#160;
     </td>
    </tr>

<!-- Optional Offering End Time -->
    <tr>
     <td class="table-light-left-bottom"
         style="text-align:right;vertical-align:top;">Offering End Time</td>
     <td class="table-content-right-bottom"
         style="text-align:left;vertical-align:top;" width="100%">
       <xsl:choose>
         <xsl:when test="optionalOfferingEndTime/HourEnd &gt; 0">
           <xsl:value-of select="optionalOfferingEndTime/HourEnd"/>
            :
            <xsl:choose>
              <xsl:when test="optionalOfferingEndTime/MinuteEnd &lt; 10">
                <xsl:value-of
                  select="concat('0', optionalOfferingEndTime/MinuteEnd)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="optionalOfferingEndTime/MinuteEnd" />
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
       &#160;
     </td>
    </tr>

<!-- Optional Offering Location  -->
    <tr>
     <td class="table-light-left-bottom"
         style="text-align:right;vertical-align:top;">Offering Location</td>
     <td class="table-content-right-bottom"
         style="text-align:left;vertical-align:top;" width="100%">
         <xsl:value-of select="optionalOfferingLocation"/>&#160;
     </td>
    </tr>


<!-- End Changes for Requirements OA 4.1 - 4.12 -->



<!--
    <tr>
      <td class="uportal-background-light" style="vertical-align:top;">List of Channels</td>
      <td class="uportal-background-light">
        <ul>
            <xsl:apply-templates select="channel"/>
        </ul>
      </td>
    </tr>
-->

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

      <xsl:template match="ensemble">
        <tr>
            <td class="table-light-left" style="text-align:center" nowrap="nowrap">
                Course Catalog settings
            </td>
            <td class="table-content-right" style="text-align:left" width="100%">
                    <input type="checkbox" class="radio" name="published" value="false" disabled="disabled"
                    id="published">
                        <xsl:if test="@published = 'true'">
                            <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                        <label for="published">Published</label>
                    </input>
                    &#160;
                    <input type="checkbox" class="radio" name="buy_now" value="false" disabled="disabled"
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











