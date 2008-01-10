<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include -->
    <xsl:import href="../global/global.xsl"/>
    <!-- parameters -->
    <xsl:param name="workerActionURL"/>
    <xsl:param name="current_command"/>
    <xsl:param name="topicNameParam"/>
    <xsl:param name="offeringNameParam"/>
    <xsl:param name="offeringDescParam"/>
    <xsl:param name="offeringNameSearchParam"/>
    <xsl:param name="userEnrollmentModelParam"/>
    <xsl:param name="selfRoleParam">selfDefaultType</xsl:param>
    <xsl:param name="enrollSelfParam">enrollSelf</xsl:param>
    <xsl:param name="enrollSelfCheckedParam">false</xsl:param>
    <xsl:param name="defaultRoleParam"/>
    <xsl:param name="channelParam"/>
    <xsl:param name="addCommand"/>
    <xsl:param name="addSubmitCommand"/>
    <xsl:param name="editCommand"/>
    <xsl:param name="editSubmitCommand"/>
    <xsl:param name="deleteCommand"/>
    <xsl:param name="deleteSubmitCommand"/>
    <xsl:param name="viewAllCommand"/>
    <xsl:param name="viewCommand"/>
    <xsl:param name="viewInactiveCommand"/>
    <xsl:param name="confirmInactivateCommand"/>
    <xsl:param name="confirmParam"/>
    <xsl:param name="importCommand"/>
    <xsl:param name="exportCommand"/>
    <xsl:param name="searchCommand"/>
    <xsl:param name="inactivateCommand"/>
    <xsl:param name="activateCommand"/>
    <xsl:param name="currentTopicId"/>
    <xsl:param name="currentTopicName"/>
	<xsl:param name="navigateRemoveMessage"/>
	<xsl:param name="enrolledUsers"/> <!-- Holds the number of users enrolled in the selected offering. It is displayed on the delete confirmation page -->
<xsl:param name="optId"/> <!-- Catalog: sticky offering search data of offering id input -->
<xsl:param name="offName"/> <!-- Catalog: sticky offering search data of offering name input -->
<xsl:param name="offDesc"/> <!-- Catalog: sticky offering search data of offering description input -->
<xsl:param name="topicName"/> <!-- Catalog: sticky user offering data of topic name input -->
<xsl:param name="catChannel">offAdmin</xsl:param> <!-- Catalog: for form id uniqueness and identifying which search inputs to insert - See Catalog templates in global.xsl -->

    <!-- Start Additions for Requirements OA 4.1-4.12 -->
    <xsl:param name="offeringIdParam"/>
    <xsl:param name="offeringTermParam"/>
    <xsl:param name="offeringMonthStartParam"/>
    <xsl:param name="offeringDayStartParam"/>
    <xsl:param name="offeringYearStartParam"/>
    <xsl:param name="offeringMonthEndParam"/>
    <xsl:param name="offeringDayEndParam"/>
    <xsl:param name="offeringYearEndParam"/>
    <xsl:param name="offeringMtgMonParam"/>
    <xsl:param name="offeringMtgTueParam"/>
    <xsl:param name="offeringMtgWedParam"/>
    <xsl:param name="offeringMtgThuParam"/>
    <xsl:param name="offeringMtgFriParam"/>
    <xsl:param name="offeringMtgSatParam"/>
    <xsl:param name="offeringMtgSunParam"/>
    <xsl:param name="offeringHourStartParam"/>
    <xsl:param name="offeringMinuteStartParam"/>
    <xsl:param name="offeringAmPmStartParam"/>
    <xsl:param name="offeringHourEndParam"/>
    <xsl:param name="offeringMinuteEndParam"/>
    <xsl:param name="offeringAmPmEndParam"/>
    <xsl:param name="offeringLocationParam"/>
    <xsl:param name="currentMonth"/>
    <xsl:param name="currentDay"/>
    <xsl:param name="currentYear"/>
    <!-- End Additions for Requirements OA 4.1-4.12 -->
    <!-- Start Additions for Requirements OA 2.1 -->
    <xsl:param name="copyCommand"/>
    <xsl:param name="copySubmitCommand"/>
    <!-- End Additions for Requirements OA 2.1 -->
    <!-- activities -->
    <xsl:param name="addOffering"/>
    <xsl:param name="editOffering"/>
    <xsl:param name="deleteOffering"/>
    <xsl:param name="importOffering"/>
    <xsl:param name="exportOffering"/>
    <xsl:param name="inactivateOffering"/>
    <xsl:param name="activateOffering"/>
    <xsl:param name="searchOffering"/>
    <!-- Start New activities for Requirement OA 2.1 -->
    <xsl:param name="copyOffering"/>
    <!-- End New activities for Requirement OA 2.1 -->
    <!-- Create variable to hold enrollment model nodes -->
	<xsl:variable name = "enrollmentModelOptions" select = "descendant::enrollmentModel" />
    <xsl:template name="autoFormJS">
        <script language="JavaScript" type="text/javascript" src="javascript/admin/OfferingAdminChannel/autoForm.js"/>
    </xsl:template>
    <!-- Common -->
    <!--<xsl:template name="javascript">
<script language="JavaScript">
<xsl:comment>
function stripWhitespace (s) { 
    return s.replace(/[ \t\n\r]+/,'');
}

function checkOfferingSubmit() {

    var form = document.offeringAdminForm;

    if (form.<xsl:value-of select="$offeringNameParam"/>.value == null || stripWhitespace(form.<xsl:value-of select="$offeringNameParam"/>.value) == '') {
        alert('Please enter a name for this offering.');
        return false;
    }

    if (form.<xsl:value-of select="$offeringDescParam"/>.value == null || stripWhitespace(form.<xsl:value-of select="$offeringDescParam"/>.value) == '') {
        alert('Please enter a description for this offering.');
        return false;
    }

    return true;
}

//</xsl:comment>

</script>
</xsl:template>
 -->
    <xsl:template name="links">
        <!-- UniAcc: Layout Table -->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="views-title">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" alt="Icon of tool-tip indicating channel options section" title="Icon of tool-tip indicating channel options section" align="absmiddle"/>
                </td>
                <td class="views" valign="middle" height="26" width="100%">
                    <!-- Made obsolete by the paging/usability requirements
    <xsl:choose>
      <xsl:when test="$current_command = 'viewAll'">
        My Offerings<img height="1" width="3" src=
        "{$SPACER}"
        alt="" border="0"/><img border="0" src=
        "{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="" align="absmiddle" 
        title="Currently viewing all active offerings"/>
      </xsl:when>
      <xsl:otherwise>
        <a href="{$baseActionURL}?catPageSize={$catPageSize}" title="View offering administration"
        onmouseover="swapImage('offeringAdminViewImage','channel_view_active.gif')" 
        onmouseout="swapImage('offeringAdminViewImage','channel_view_base.gif')">My Offerings<img 
        height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/><img border="0" src=
        "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="" align="absmiddle" 
        name="offeringAdminViewImage" id="offeringAdminViewImage"/></a>
      </xsl:otherwise>
    </xsl:choose> -->
                    <xsl:choose>
                        <xsl:when test="$current_command = 'search'">
                            Search
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="Selected 'Search' icon indicating you are currently searching offerings" title="Selected 'Search' icon indicating you are currently searching offerings"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=mainSearch&amp;servant_command=cancel" title="To search offerings" onmouseover="swapImage('offeringAdminSearchImage','channel_view_active.gif')" onmouseout="swapImage('offeringAdminSearchImage','channel_view_base.gif')">
                                Search
                                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                                <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'Search' icon to search offerings" title="'Search' icon to search offerings" name="offeringAdminSearchImage" id="offeringAdminSearchImage"/>
                            </a>
                        </xsl:otherwise>
						<!-- Searching offerings does not require permission. This code is saved for reference only.
						<xsl:when test="$searchOffering = 'Y'">
                            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=mainSearch" title="To search offerings" onmouseover="swapImage('offeringAdminSearchImage','channel_view_active.gif')" onmouseout="swapImage('offeringAdminSearchImage','channel_view_base.gif')">
                                Search
                                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                                <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'Search' icon to search offerings" title="'Search' icon to search offerings" name="offeringAdminSearchImage" id="offeringAdminSearchImage"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            Search
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif" align="absmiddle" alt="Inactive 'Search' icon indicating issuficient permission to search offerings" title="Inactive 'Search' icon indicating issuficient permission to search offerings"/>
                        </xsl:otherwise> -->
                    </xsl:choose>
                    <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                    <xsl:choose>
                        <xsl:when test="$current_command = 'add'">
                              | Add
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif" alt="Selected 'Add' icon indicating that you are currently on the add offering view" title="Selected 'Add' icon indicating that you are currently on the add offering view"/>
                        </xsl:when>
                        <xsl:when test="$addOffering = 'Y'">
                              | 
                            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$addCommand}&amp;servant_command=cancel" title="To add a new offering" onmouseover="swapImage('offeringAdminAddImage','channel_add_active.gif')" onmouseout="swapImage('offeringAdminAddImage','channel_add_base.gif')">
                            Add
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Add' icon to add a new offering" title="'Add' icon to add a new offering" name="offeringAdminAddImage" id="offeringAdminAddImage"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                              | Add
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif" alt="Inactive 'Add' icon indicating insufficient permission to add offerings" title="Inactive 'Add' icon indicating insufficient permission to add offerings"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                    <xsl:choose>
                        <xsl:when test="$current_command = 'viewInactive'">
                            | Inactive
                            <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif" alt="Selected 'View' icon indicating you are currently viewing inactive offerings" title="Selected 'View' icon indicating you are currently viewing inactive offerings"/>
                        </xsl:when>
                        <xsl:when test="$activateOffering = 'Y'">
                            | 
                            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewInactiveCommand}" title="To view inactive offerings" onmouseover="swapImage('offeringAdminInactiveImage','channel_view_active.gif')" onmouseout="swapImage('offeringAdminInactiveImage','channel_view_base.gif')">
                                  Inactive
                                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" align="absmiddle" alt="'View' icon to view a listing of inactive offerings" title="'View' icon to view a listing of inactive offerings" name="offeringAdminInactiveImage" id="offeringAdminInactiveImage"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            Inactive
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif" alt="Inactive 'View' icon indicating insufficient permission to view inactive offerings" title="Inactive 'View' icon indicating insufficient permission to view inactive offerings"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
    </xsl:template>
    <xsl:template match="topic">
        <xsl:choose>
            <xsl:when test="@selected = 'true'">
                <option selected="selected" value="{@id}">
                    <xsl:value-of select="name"/>
                </option>
            </xsl:when>
            <xsl:otherwise>
                <option value="{@id}">
                    <xsl:value-of select="name"/>
                </option>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="enrollmentModel">
    	<xsl:param name="forAction">edit</xsl:param>
        <xsl:choose>
        	<!-- Add XML has SIS set as the default type, so ignore SIS listing for Add -->
        	<xsl:when test="$forAction != 'add' and $enrollmentModelOptions[@default='true'] = 'SIS'">
        		<xsl:if test = "text() = 'SIS'">
	                <option selected="selected">
	                    <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
	                    <xsl:value-of select="."/>
	                </option>
        		</xsl:if>
        	</xsl:when>
          
        	<xsl:otherwise>
	        	<xsl:if test = "text() != 'SIS'">
		        	<xsl:choose>
		        		<!-- Add XML has SIS set as the default type, so if add then set all as selected which will mean last in list is default -->
			            <xsl:when test="$forAction = 'add' or @default='true'">
			                <option selected="selected">
			                    <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			                    <xsl:value-of select="."/>
			                </option>
			            </xsl:when>
			            <xsl:otherwise>
			                <option>
			                    <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
			                    <xsl:value-of select="."/>
			                </option>
			            </xsl:otherwise>
			        </xsl:choose>
	        	</xsl:if>
        	</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="role">
    	<xsl:param name="useDefaultType">true</xsl:param>
        <xsl:choose>
            <xsl:when test="((@default = 'true') and ($useDefaultType = 'true')) or (($useDefaultType = 'false') and (@id = '5'))">
                <option selected="selected">
                    <xsl:attribute name="value"><xsl:value-of select="@id"/></xsl:attribute>
                    <xsl:value-of select="."/>
                </option>
            </xsl:when>
            <xsl:otherwise>
                <option>
                    <xsl:attribute name="value"><xsl:value-of select="@id"/></xsl:attribute>
                    <xsl:value-of select="."/>
                </option>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="channel">
    <xsl:variable name="handle"><xsl:value-of select="@id"/></xsl:variable>
    <input type="checkbox" class="radio" name="{$channelParam}" checked="checked" value="{$handle}"><xsl:value-of select="."/><br/></input>
  </xsl:template>

    <xsl:template name="yearOptions">
        <OPTION value="2002">
            <xsl:if test="$currentYear = 2002">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2002
        </OPTION>
        <OPTION value="2003">
            <xsl:if test="$currentYear = 2003">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2003
        </OPTION>
        <OPTION value="2004">
            <xsl:if test="$currentYear = 2004">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2004
        </OPTION>
        <OPTION value="2005">
            <xsl:if test="$currentYear = 2005">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2005
        </OPTION>
        <OPTION value="2006">
            <xsl:if test="$currentYear = 2006">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2006
        </OPTION>
        <OPTION value="2007">
            <xsl:if test="$currentYear = 2007">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2007
        </OPTION>
        <OPTION value="2008">
            <xsl:if test="$currentYear = 2008">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2008
        </OPTION>
        <OPTION value="2009">
            <xsl:if test="$currentYear = 2009">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2009
        </OPTION>
        <OPTION value="2010">
            <xsl:if test="$currentYear = 2010">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>2010
        </OPTION>
    </xsl:template>
    
    <xsl:template name="monthOptions">
        <option value="1">
          <xsl:if test="$currentMonth = 1">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Jan.
        </option>
        <option value="2">
          <xsl:if test="$currentMonth = 2">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Feb.
        </option>
        <option value="3">
          <xsl:if test="$currentMonth = 3">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Mar.
        </option>
        <option value="4">
          <xsl:if test="$currentMonth = 4">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Apr.
        </option>
        <option value="5">
          <xsl:if test="$currentMonth = 5">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         May.
        </option>
        <option value="6">
          <xsl:if test="$currentMonth = 6">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Jun.
        </option>
        <option value="7">
          <xsl:if test="$currentMonth = 7">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Jul.
        </option>
        <option value="8">
          <xsl:if test="$currentMonth = 8">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Aug.
        </option>
        <option value="9">
          <xsl:if test="$currentMonth = 9">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Sep.
        </option>
        <option value="10">
          <xsl:if test="$currentMonth = 10">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Oct.
        </option>
        <option value="11">
          <xsl:if test="$currentMonth = 11">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Nov.
        </option>
        <option value="12">
          <xsl:if test="$currentMonth = 12">
             <xsl:attribute  name="selected">selected</xsl:attribute>
          </xsl:if>
         Dec.
        </option>
    </xsl:template>
    
    <xsl:template name="dayOptions">
        <option value="1">
            <xsl:if test="$currentDay = 1">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            1
        </option>
        <option value="2">
            <xsl:if test="$currentDay = 2">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            2
        </option>
        <option value="3">
            <xsl:if test="$currentDay = 3">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            3
        </option>
        <option value="4">
            <xsl:if test="$currentDay = 4">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            4
        </option>
        <option value="5">
            <xsl:if test="$currentDay = 5">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            5
        </option>
        <option value="6">
            <xsl:if test="$currentDay = 6">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            6
        </option>
        <option value="7">
            <xsl:if test="$currentDay = 7">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            7
        </option>
        <option value="8">
            <xsl:if test="$currentDay = 8">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            8
        </option>
        <option value="9">
            <xsl:if test="$currentDay = 9">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            9
        </option>
        <option value="10">
            <xsl:if test="$currentDay = 10">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            10
        </option>
        <option value="11">
            <xsl:if test="$currentDay = 11">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            11
        </option>
        <option value="12">
            <xsl:if test="$currentDay = 12">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            12
        </option>
        <option value="13">
            <xsl:if test="$currentDay = 13">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            13
        </option>
        <option value="14">
            <xsl:if test="$currentDay = 14">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            14
        </option>
        <option value="15">
            <xsl:if test="$currentDay = 15">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            15
        </option>
        <option value="16">
            <xsl:if test="$currentDay = 16">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            16
        </option>
        <option value="17">
            <xsl:if test="$currentDay = 17">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            17
        </option>
        <option value="18">
            <xsl:if test="$currentDay = 18">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            18
        </option>
        <option value="19">
            <xsl:if test="$currentDay = 19">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            19
        </option>
        <option value="20">
            <xsl:if test="$currentDay = 20">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            20
        </option>
        <option value="21">
            <xsl:if test="$currentDay = 21">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            21
        </option>
        <option value="22">
            <xsl:if test="$currentDay = 22">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            22
        </option>
        <option value="23">
            <xsl:if test="$currentDay = 23">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            23
        </option>
        <option value="24">
            <xsl:if test="$currentDay = 24">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            24
        </option>
        <option value="25">
            <xsl:if test="$currentDay = 25">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            25
        </option>
        <option value="26">
            <xsl:if test="$currentDay = 26">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            26
        </option>
        <option value="27">
            <xsl:if test="$currentDay = 27">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            27
        </option>
        <option value="28">
            <xsl:if test="$currentDay = 28">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            28
        </option>
        <option value="29">
            <xsl:if test="$currentDay = 29">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            29
        </option>
        <option value="30">
            <xsl:if test="$currentDay = 30">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            30
        </option>
        <option value="31">
            <xsl:if test="$currentDay = 31">
              <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            31
        </option>
    </xsl:template>
    
    <xsl:template name="hourOptions">
        <OPTION value="1">1</OPTION>
        <OPTION value="2">2</OPTION>
        <OPTION value="3">3</OPTION>
        <OPTION value="4">4</OPTION>
        <OPTION value="5">5</OPTION>
        <OPTION value="6">6</OPTION>
        <OPTION value="7">7</OPTION>
        <OPTION value="8">8</OPTION>
        <OPTION value="9" selected="selected">9</OPTION>
        <OPTION value="10">10</OPTION>
        <OPTION value="11">11</OPTION>
        <OPTION value="12">12</OPTION>
    </xsl:template>
    
    <xsl:template name="minuteOptions">
        <OPTION value="00" selected="selected">00</OPTION>
        <OPTION value="05">05</OPTION>
        <OPTION value="10">10</OPTION>
        <OPTION value="15">15</OPTION>
        <OPTION value="20">20</OPTION>
        <OPTION value="25">25</OPTION>
        <OPTION value="30">30</OPTION>
        <OPTION value="35">35</OPTION>
        <OPTION value="40">40</OPTION>
        <OPTION value="45">45</OPTION>
        <OPTION value="50">50</OPTION>
        <OPTION value="55">55</OPTION>
    </xsl:template>
</xsl:stylesheet>
