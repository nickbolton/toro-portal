<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="offering">

    <!--<textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/>
    </textarea> -->

    <xsl:call-template name="autoFormJS"/>

    <xsl:call-template name="links"/>

    <form method="post" onSubmit="return validator.applyFormRules(this, new OfferingAdminRulesObject())" name="offeringAdminForm" action="{$baseActionURL}">
      <input type="hidden" name="command" value="{$editSubmitCommand}" />
      <input type="hidden" name="ID" value="{@id}" />
      <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
      <input type="hidden" name="optId" value="{$optId}" />
      <input type="hidden" name="topicName" value="{$topicName}" />
      <input type="hidden" name="offName" value="{$offName}" />
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
          <th colspan="2" class="th-top">Edit Offering</th>
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
          <td class="table-light-left" style="text-align:right" nowrap="nowrap">
              <label for="offeringAdminFormOfferingName">Offering Name</label>
          </td>
          <td class="table-content-right" style="text-align:left" width="100%">
            <input name="offeringName" type="text" class="text" id="offeringAdminFormOfferingName" size="70" maxlength="80">
              <xsl:attribute name="value">
                <xsl:value-of select="name"/>
              </xsl:attribute>
            </input>
            <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}" title="View details for this offering"
            onmouseover="swapImage('offeringAdminDetailsImage','channel_view_active.gif')"
            onmouseout="swapImage('offeringAdminDetailsImage','channel_view_base.gif')">
            <img border="0" src=
            "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
             alt="'View' icon: offering details for '{name}'"
             title="'View' icon: offering details for '{name}'"
             align="absmiddle" name="offeringAdminDetailsImage" id="offeringAdminDetailsImage"/></a>

            <xsl:choose>
                <xsl:when test="$exportOffering = 'Y'">
                    <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                    <a href="{$workerActionURL}&amp;ID={@id}" title="Export this offering"
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
              <xsl:when test="$copyOffering = 'Y'">
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
          </td>
        </tr>

        <tr>
          <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap">
              <label for="offeringAdminFormOfferingDescription">Offering Description</label>
          </td>
          <td class="table-content-right" style="text-align:left" width="100%">
              <textarea name="offeringDescription" rows="4" id="offeringAdminFormOfferingDescription"><xsl:value-of select="description"/></textarea></td>
        </tr>

    <tr>
        <td class="table-light-left" style="text-align:center" nowrap="nowrap">
            <label for="offeringAdminFormUserEnrollmentModel">User Enrollment Model</label>
        </td>
        <td class="table-content-right" style="text-align:left" width="100%">
            <select name="{$userEnrollmentModelParam}" id="offeringAdminFormUserEnrollmentModel">
                  <xsl:apply-templates select="enrollmentModel"/>
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
          </select></td>
        </tr>

        <!-- Build ensemble fields -->
        <xsl:apply-templates select="ensemble"/>

<!-- Start of new functionality for requirements OA 4.1-4.12 -->
<!-- Offering ID -->
<tr>
 <td class="table-light-left" style="text-align:center"
    nowrap="nowrap"><label for="offeringAdminFormOfferingID">Offering ID</label></td>
 <td class="table-content-right" style="text-align:left" width="100%">
  <input name="offeringIdParam" type="text" size="15" maxlength="15"
       class="text" id="offeringAdminFormOfferingID">
              <xsl:attribute name="value">
                <xsl:value-of select="optionalOfferingId"/>
              </xsl:attribute>
  </input></td>
</tr>

<!-- Offering Term -->
<tr>
  <td class="table-light-left" style="text-align:center"
      nowrap="nowrap"><label for="offeringAdminFormOfferingTerm">Offering Term</label></td>
  <td class="table-content-right" style="text-align:left" width="100%">
     <input name="{$offeringTermParam}" type="text" size="25"
            maxlength="25" class="text" id="offeringAdminFormOfferingTerm">
              <xsl:attribute name="value">
                <xsl:value-of select="optionalOfferingTerm"/>
              </xsl:attribute>
     </input></td>
</tr>

<!-- Offering Start Date -->
<tr>
  <td class="table-light-left" style="text-align:center" id="offeringAdminFormOfferingStartDate"
      nowrap="nowrap"><label for="offeringAdminFormOfferingStartMonth">Offering Start Date</label></td>
  <td class="table-content-right" headers="offeringAdminFormOfferingStartDate">
    <select name="offeringMonthStartParam" id="offeringAdminFormOfferingStartMonth" title="Offering start month">
      <xsl:choose>
        <xsl:when test="optionalOfferingStartDate/MonthStart &gt; 0">
          <option value="1">
            <xsl:if test="optionOfferingStartDate/MonthStart = 1">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Jan.
          </option>
          <option value="2">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 2">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Feb.
          </option>
          <option value="3">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 3">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Mar.
          </option>
          <option value="4">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 4">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Apr.
          </option>
          <option value="5">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 5">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           May.
          </option>
          <option value="6">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 6">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Jun.
          </option>
          <option value="7">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 7">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Jul.
          </option>
          <option value="8">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 8">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Aug.
          </option>
          <option value="9">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 9">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Sep.
          </option>
          <option value="10">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 10">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Oct.
          </option>
          <option value="11">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 11">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Nov.
          </option>
          <option value="12">
            <xsl:if test="optionalOfferingStartDate/MonthStart = 12">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Dec.
          </option>
        </xsl:when>
        <xsl:otherwise>
              <!-- Template located in common.xsl -->
            <xsl:call-template name="monthOptions" />
        </xsl:otherwise>
      </xsl:choose>
    </select>
    <select name="offeringDayStartParam" title="Offering start day">
      <xsl:choose>
        <xsl:when test="optionalOfferingStartDate/MonthStart &gt; 0">
          <option value="1">
              <xsl:if test="optionalOfferingStartDate/DayStart = 1">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              1
          </option>
          <option value="2">
              <xsl:if test="optionalOfferingStartDate/DayStart = 2">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              2
          </option>
          <option value="3">
              <xsl:if test="optionalOfferingStartDate/DayStart = 3">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              3
          </option>
          <option value="4">
              <xsl:if test="optionalOfferingStartDate/DayStart = 4">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              4
          </option>
          <option value="5">
              <xsl:if test="optionalOfferingStartDate/DayStart = 5">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              5
          </option>
          <option value="6">
              <xsl:if test="optionalOfferingStartDate/DayStart = 6">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              6
          </option>
          <option value="7">
              <xsl:if test="optionalOfferingStartDate/DayStart = 7">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              7
          </option>
          <option value="8">
              <xsl:if test="optionalOfferingStartDate/DayStart = 8">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              8
          </option>
          <option value="9">
              <xsl:if test="optionalOfferingStartDate/DayStart = 9">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              9
          </option>
          <option value="10">
              <xsl:if test="optionalOfferingStartDate/DayStart = 10">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              10
          </option>
          <option value="11">
              <xsl:if test="optionalOfferingStartDate/DayStart = 11">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              11
          </option>
          <option value="12">
              <xsl:if test="optionalOfferingStartDate/DayStart = 12">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              12
          </option>
          <option value="13">
              <xsl:if test="optionalOfferingStartDate/DayStart = 13">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              13
          </option>
          <option value="14">
              <xsl:if test="optionalOfferingStartDate/DayStart = 14">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              14
          </option>
          <option value="15">
              <xsl:if test="optionalOfferingStartDate/DayStart = 15">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              15
          </option>
          <option value="16">
              <xsl:if test="optionalOfferingStartDate/DayStart = 16">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              16
          </option>
          <option value="17">
              <xsl:if test="optionalOfferingStartDate/DayStart = 17">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              17
          </option>
          <option value="18">
              <xsl:if test="optionalOfferingStartDate/DayStart = 18">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              18
          </option>
          <option value="19">
              <xsl:if test="optionalOfferingStartDate/DayStart = 19">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              19
          </option>
          <option value="20">
              <xsl:if test="optionalOfferingStartDate/DayStart = 20">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              20
          </option>
          <option value="21">
              <xsl:if test="optionalOfferingStartDate/DayStart = 21">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              21
          </option>
          <option value="22">
              <xsl:if test="optionalOfferingStartDate/DayStart = 22">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              22
          </option>
          <option value="23">
              <xsl:if test="optionalOfferingStartDate/DayStart = 23">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              23
          </option>
          <option value="24">
              <xsl:if test="optionalOfferingStartDate/DayStart = 24">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              24
          </option>
          <option value="25">
              <xsl:if test="optionalOfferingStartDate/DayStart = 25">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              25
          </option>
          <option value="26">
              <xsl:if test="optionalOfferingStartDate/DayStart = 26">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              26
          </option>
          <option value="27">
              <xsl:if test="optionalOfferingStartDate/DayStart = 27">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              27
          </option>
          <option value="28">
              <xsl:if test="optionalOfferingStartDate/DayStart = 28">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              28
          </option>
          <option value="29">
              <xsl:if test="optionalOfferingStartDate/DayStart = 29">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              29
          </option>
          <option value="30">
              <xsl:if test="optionalOfferingStartDate/DayStart = 30">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              30
          </option>
          <option value="31">
              <xsl:if test="optionalOfferingStartDate/DayStart = 31">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              31
          </option>
        </xsl:when>
        <xsl:otherwise>
              <!-- Template located in common.xsl -->
            <xsl:call-template name="dayOptions" />
        </xsl:otherwise>
      </xsl:choose>
    </select>
    <select name="offeringYearStartParam" title="Offering start year">
      <xsl:choose>
        <xsl:when test="optionalOfferingStartDate/MonthStart &gt; 0">
          <option value="2002">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2002">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            2002
          </option>
          <option value="2003">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2003">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            2003
          </option>
          <option value="2004">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2004">
              <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2004
          </option>
          <option value="2005">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2005">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2005
          </option>
          <option value="2006">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2006">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2006
          </option>
          <option value="2007">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2007">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2007
          </option>
          <option value="2008">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2008">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2008
          </option>
          <option value="2009">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2009">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2009
          </option>
          <option value="2010">
            <xsl:if test="optionalOfferingStartDate/YearStart = 2010">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2010
          </option>
        </xsl:when>
        <xsl:otherwise>
              <!-- Template located in common.xsl -->
            <xsl:call-template name="yearOptions" />
        </xsl:otherwise>
      </xsl:choose>
    </select><br/>
  </td>
</tr>

<!-- Offering End Date -->
<tr>
  <td class="table-light-left" style="text-align:center"
      nowrap="nowrap" id="offeringAdminFormOfferingEndDate"><label for="offeringAdminFormOfferingEndMonth">Offering End Date</label></td>
  <td class="table-content-right" headers="offeringAdminFormOfferingEndDate">
    <select name="offeringMonthEndParam" id="offeringAdminFormOfferingEndMonth" title="Offering end month">
      <xsl:choose>
        <xsl:when test="optionalOfferingEndDate/MonthEnd &gt; 0">
          <option value="1">
            <xsl:if test="optionOfferingEndDate/MonthEnd = 1">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Jan.
          </option>
          <option value="2">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 2">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Feb.
          </option>
          <option value="3">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 3">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Mar.
          </option>
          <option value="4">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 4">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Apr.
          </option>
          <option value="5">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 5">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           May.
          </option>
          <option value="6">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 6">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Jun.
          </option>
          <option value="7">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 7">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Jul.
          </option>
          <option value="8">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 8">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Aug.
          </option>
          <option value="9">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 9">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Sep.
          </option>
          <option value="10">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 10">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Oct.
          </option>
          <option value="11">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 11">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Nov.
          </option>
          <option value="12">
            <xsl:if test="optionalOfferingEndDate/MonthEnd = 12">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
           Dec.
          </option>
        </xsl:when>
        <xsl:otherwise>
              <!-- Template located in common.xsl -->
            <xsl:call-template name="monthOptions" />
        </xsl:otherwise>
      </xsl:choose>
    </select>
    <select name="offeringDayEndParam" title="Offering end day">
      <xsl:choose>
        <xsl:when test="optionalOfferingEndDate/MonthEnd &gt; 0">
          <option value="1">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 1">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              1
          </option>
          <option value="2">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 2">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              2
          </option>
          <option value="3">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 3">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              3
          </option>
          <option value="4">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 4">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              4
          </option>
          <option value="5">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 5">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              5
          </option>
          <option value="6">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 6">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              6
          </option>
          <option value="7">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 7">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              7
          </option>
          <option value="8">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 8">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              8
          </option>
          <option value="9">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 9">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              9
          </option>
          <option value="10">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 10">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              10
          </option>
          <option value="11">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 11">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              11
          </option>
          <option value="12">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 12">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              12
          </option>
          <option value="13">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 13">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              13
          </option>
          <option value="14">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 14">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              14
          </option>
          <option value="15">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 15">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              15
          </option>
          <option value="16">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 16">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              16
          </option>
          <option value="17">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 17">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              17
          </option>
          <option value="18">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 18">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              18
          </option>
          <option value="19">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 19">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              19
          </option>
          <option value="20">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 20">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              20
          </option>
          <option value="21">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 21">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              21
          </option>
          <option value="22">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 22">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              22
          </option>
          <option value="23">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 23">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              23
          </option>
          <option value="24">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 24">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              24
          </option>
          <option value="25">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 25">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              25
          </option>
          <option value="26">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 26">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              26
          </option>
          <option value="27">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 27">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              27
          </option>
          <option value="28">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 28">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              28
          </option>
          <option value="29">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 29">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              29
          </option>
          <option value="30">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 30">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              30
          </option>
          <option value="31">
              <xsl:if test="optionalOfferingEndDate/DayEnd = 31">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
              </xsl:if>
              31
          </option>
        </xsl:when>
        <xsl:otherwise>
              <!-- Template located in common.xsl -->
            <xsl:call-template name="dayOptions" />
        </xsl:otherwise>
      </xsl:choose>
    </select>
    <select name="offeringYearEndParam" title="Offering end year">
      <xsl:choose>
        <xsl:when test="optionalOfferingEndDate/MonthEnd &gt; 0">
          <option value="2002">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2002">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            2002
          </option>
          <option value="2003">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2003">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            2003
          </option>
          <option value="2004">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2004">
              <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2004
          </option>
          <option value="2005">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2005">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2005
          </option>
          <option value="2006">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2006">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2006
          </option>
          <option value="2007">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2007">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2007
          </option>
          <option value="2008">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2008">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2008
          </option>
          <option value="2009">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2009">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2009
          </option>
          <option value="2010">
            <xsl:if test="optionalOfferingEndDate/YearEnd = 2010">
               <xsl:attribute name="selected" >selected</xsl:attribute>
            </xsl:if>
            2010
          </option>
        </xsl:when>
        <xsl:otherwise>
              <!-- Template located in common.xsl -->
            <xsl:call-template name="yearOptions" />
        </xsl:otherwise>
      </xsl:choose>
    </select><br/>
  </td>
</tr>

<!-- Offering Meeting Days -->
<tr>
  <td class="table-light-left" style="text-align:center"
      nowrap="nowrap" id="offeringAdminFormOfferingMeetingDays">Offering Meeting Days</td>
  <td class="table-content-right" style="text-align:left" width="100%" headers="offeringAdminFormOfferingMeetingDays">
    <input type="checkbox" name="{$offeringMtgMonParam}" value="1" id="offeringAdminFormOfferingMeetingDaysMonday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsMonday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysMonday">Monday</label>
    </input>&#160;
    <br />
    <input type="checkbox" name="{$offeringMtgTueParam}" value="1" id="offeringAdminFormOfferingMeetingDaysTuesday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsTuesday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysTuesday">Tuesday</label>
    </input>&#160;
    <br />
    <input type="checkbox" name="{$offeringMtgWedParam}" value="1" id="offeringAdminFormOfferingMeetingDaysWednesday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsWednesday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysWednesday">Wednesday</label>
    </input>&#160;
    <br />
    <input type="checkbox" name="{$offeringMtgThuParam}" value="1" id="offeringAdminFormOfferingMeetingDaysThursday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsThursday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysThursday">Thursday</label>
    </input>&#160;
    <br />
    <input type="checkbox" name="{$offeringMtgFriParam}" value="1" id="offeringAdminFormOfferingMeetingDaysFriday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsFriday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysFriday">Friday</label>
    </input>&#160;
    <br />
    <input type="checkbox" name="{$offeringMtgSatParam}" value="1" id="offeringAdminFormOfferingMeetingDaysSaturday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsSaturday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysSaturday">Saturday</label>
    </input>&#160;
    <br />
    <input type="checkbox" name="{$offeringMtgSunParam}" value="1" id="offeringAdminFormOfferingMeetingDaysSunday">
       <xsl:if test="optionalOfferingMeetingDays/MeetsSunday = 1">
          <xsl:attribute name="checked">true</xsl:attribute>
       </xsl:if>
       &#160;<label for="offeringAdminFormOfferingMeetingDaysSunday">Sunday</label>
    </input>&#160;
  </td>
</tr>

<!-- Offering Start Time -->
<tr>
  <td class="table-light-left" style="text-align:center"
      nowrap="nowrap" id="offeringAdminFormOfferingStartTime"><label for="offeringAdminFormOfferingStartTimeHour">Offering Start Time</label></td>
  <td class="table-content-right" headers="offeringAdminFormOfferingStartTime">
    <xsl:choose>
      <xsl:when test="optionalOfferingStartTime/HourStart &gt; 0">
       <select name="{$offeringHourStartParam}" id="offeringAdminFormOfferingStartTimeHour" title="Offering start hour">
         <option value="1">
           <xsl:if test="optionalOfferingStartTime/HourStart = 1">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           1
         </option>
         <option value="2">
           <xsl:if test="optionalOfferingStartTime/HourStart = 2">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           2
         </option>
         <option value="3">
           <xsl:if test="optionalOfferingStartTime/HourStart = 3">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           3
         </option>
         <option value="4">
           <xsl:if test="optionalOfferingStartTime/HourStart = 4">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           4
         </option>
         <option value="5">
           <xsl:if test="optionalOfferingStartTime/HourStart = 5">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           5
         </option>
         <option value="6">
           <xsl:if test="optionalOfferingStartTime/HourStart = 6">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           6
         </option>
         <option value="7">
           <xsl:if test="optionalOfferingStartTime/HourStart = 7">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           7
         </option>
         <option value="8">
           <xsl:if test="optionalOfferingStartTime/HourStart = 8">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           8
         </option>
         <option value="9">
           <xsl:if test="optionalOfferingStartTime/HourStart = 9">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           9
         </option>
         <option value="10">
           <xsl:if test="optionalOfferingStartTime/HourStart = 10">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           10
         </option>
         <option value="11">
           <xsl:if test="optionalOfferingStartTime/HourStart = 11">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           11
         </option>
         <option value="12">
           <xsl:if test="optionalOfferingStartTime/HourStart = 12">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           12
         </option>
       </select> <b> : </b>
       <select name="{$offeringMinuteStartParam}" title="Offering start minute">
         <option value="00">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 0">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           00
         </option>
         <option value="05">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 5">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           05
         </option>
         <option value="10">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 10">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           10
         </option>
         <option value="15">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 15">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           15
         </option>
         <option value="20">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 20">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           20
         </option>
         <option value="25">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 25">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           25
         </option>
         <option value="30">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 30">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           30
         </option>
         <option value="35">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 35">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           35
         </option>
         <option value="40">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 40">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           40
         </option>
         <option value="45">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 45">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           45
         </option>
         <option value="50">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 50">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           50
         </option>
         <option value="55">
           <xsl:if test="optionalOfferingStartTime/MinuteStart = 55">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           55
         </option>
       </select>
       <select name="{$offeringAmPmStartParam}" title="Offering start time of day">
         <option value="1">
           <xsl:if test="optionalOfferingStartTime/AmPmStart = 1">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           AM
         </option>
         <option value="2">
           <xsl:if test="optionalOfferingStartTime/AmPmStart = 2">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           PM
         </option>
       </select>
      </xsl:when>
      <xsl:otherwise>
       <select name="{$offeringHourStartParam}" id="offeringAdminFormOfferingStartTimeHour" title="Offering start hour">
             <!-- Template located in common.xsl -->
            <xsl:call-template name="hourOptions" />
       </select> <b> : </b>
       <select name="{$offeringMinuteStartParam}" title="Offering start minute">
             <!-- Template located in common.xsl -->
            <xsl:call-template name="minuteOptions" />
       </select>
       <select name="{$offeringAmPmStartParam}" title="Offering start time of day">
         <option value="1" selected="selected">AM</option>
         <option value="2">PM</option>
       </select>
      </xsl:otherwise>
    </xsl:choose>
  </td>
</tr>

<!-- Offering End Time -->
<tr>
  <td class="table-light-left" style="text-align:center" id="offeringAdminFormOfferingEndTime"
      nowrap="nowrap"><label for="offeringAdminFormOfferingEndTimeHour">Offering End Time</label></td>
  <td class="table-content-right" headers="offeringAdminFormOfferingEndTime">
    <xsl:choose>
      <xsl:when test="optionalOfferingEndTime/HourEnd &gt; 0">
       <select name="{$offeringHourEndParam}" id="offeringAdminFormOfferingEndTimeHour" title="Offering end hour">
         <option value="1">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 1">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           1
         </option>
         <option value="2">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 2">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           2
         </option>
         <option value="3">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 3">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           3
         </option>
         <option value="4">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 4">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           4
         </option>
         <option value="5">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 5">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           5
         </option>
         <option value="6">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 6">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           6
         </option>
         <option value="7">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 7">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           7
         </option>
         <option value="8">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 8">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           8
         </option>
         <option value="9">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 9">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           9
         </option>
         <option value="10">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 10">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           10
         </option>
         <option value="11">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 11">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           11
         </option>
         <option value="12">
           <xsl:if test="optionalOfferingEndTime/HourEnd = 12">
             <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           12
         </option>
       </select> <b> : </b>
       <select name="{$offeringMinuteEndParam}" title="Offering end minute">
         <option value="00">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 0">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           00
         </option>
         <option value="05">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 5">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           05
         </option>
         <option value="10">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 10">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           10
         </option>
         <option value="15">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 15">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           15
         </option>
         <option value="20">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 20">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           20
         </option>
         <option value="25">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 25">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           25
         </option>
         <option value="30">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 30">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           30
         </option>
         <option value="35">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 35">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           35
         </option>
         <option value="40">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 40">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           40
         </option>
         <option value="45">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 45">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           45
         </option>
         <option value="50">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 50">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           50
         </option>
         <option value="55">
           <xsl:if test="optionalOfferingEndTime/MinuteEnd = 55">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           55
         </option>
       </select>
       <select name="{$offeringAmPmEndParam}" title="Offering end time of day">
         <option value="1">
           <xsl:if test="optionalOfferingEndTime/AmPmEnd = 1">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           AM
         </option>
         <option value="2">
           <xsl:if test="optionalOfferingEndTime/AmPmEnd = 2">
              <xsl:attribute name="selected">selected</xsl:attribute>
           </xsl:if>
           PM
         </option>
       </select>
      </xsl:when>
      <xsl:otherwise>
       <select name="{$offeringHourEndParam}" id="offeringAdminFormOfferingEndTimeHour" title="Offering end hour">
             <!-- Template located in common.xsl -->
            <xsl:call-template name="hourOptions" />
       </select> <b> : </b>
       <select name="{$offeringMinuteEndParam}" title="Offering end minute">
             <!-- Template located in common.xsl -->
            <xsl:call-template name="minuteOptions" />
       </select>
       <select name="{$offeringAmPmEndParam}" title="Offering end time of day">
         <option value="1" selected="selected">AM</option>
         <option value="2">PM</option>
       </select>
      </xsl:otherwise>
    </xsl:choose>
  </td>
</tr>


<!-- Offering Location -->
<tr>
  <td class="table-light-left" style="text-align:center"
      nowrap="nowrap"><label for="offeringAdminFormOfferingLocation">Offering Location</label></td>
  <td class="table-content-right" style="text-align:left" width="100%">
    <input name="{$offeringLocationParam}" type="text" size="25" id="offeringAdminFormOfferingLocation"
           maxlength="25" class="text">
              <xsl:attribute name="value">
                <xsl:value-of select="optionalOfferingLocation"/>
              </xsl:attribute>
    </input>
  </td>
</tr>


<!-- End of new functionality for requirements OA 4.1-4.12 -->



<!--
        <tr>
          <td class="uportal-background-light" style="vertical-align:top;">List of Channels</td>
          <td class="uportal-background-light">
            <xsl:apply-templates select="channel"/>
          </td>
        </tr>
-->

        <tr>
          <td colspan="2" class="table-nav">
            <nobr>
              <input type="submit" class="uportal-button" value="Submit" title="Submit changes to this offering"/>
              <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel changes to this offering"/>
            </nobr>
          </td>
        </tr>

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
