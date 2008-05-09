<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">
    <!--<textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/>
      <parameter name="workerActionURL"><xsl:value-of select="$workerActionURL" /></parameter>
      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
      <parameter name="topicNameParam"><xsl:value-of select="$topicNameParam" /></parameter>
      <parameter name="offeringNameParam"><xsl:value-of select="$offeringNameParam" /></parameter>
      <parameter name="offeringDescParam"><xsl:value-of select="$offeringDescParam" /></parameter>
      <parameter name="offeringNameSearchParam"><xsl:value-of select="$offeringNameSearchParam" /></parameter>
      <parameter name="userEnrollmentModelParam"><xsl:value-of select="$userEnrollmentModelParam" /></parameter>
      <parameter name="defaultRoleParam"><xsl:value-of select="$defaultRoleParam" /></parameter>
      <parameter name="channelParam"><xsl:value-of select="$channelParam" /></parameter>
      <parameter name="addCommand"><xsl:value-of select="$addCommand" /></parameter>
      <parameter name="addSubmitCommand"><xsl:value-of select="$addSubmitCommand" /></parameter>
      <parameter name="editCommand"><xsl:value-of select="$editCommand" /></parameter>
      <parameter name="editSubmitCommand"><xsl:value-of select="$editSubmitCommand" /></parameter>
      <parameter name="deleteCommand"><xsl:value-of select="$deleteCommand" /></parameter>
      <parameter name="deleteSubmitCommand"><xsl:value-of select="$deleteSubmitCommand" /></parameter>
      <parameter name="viewAllCommand"><xsl:value-of select="$viewAllCommand" /></parameter>
      <parameter name="viewCommand"><xsl:value-of select="$viewCommand" /></parameter>
      <parameter name="viewInactiveCommand"><xsl:value-of select="$viewInactiveCommand" /></parameter>
      <parameter name="confirmInactivateCommand"><xsl:value-of select="$confirmInactivateCommand" /></parameter>
      <parameter name="confirmParam"><xsl:value-of select="$confirmParam" /></parameter>
      <parameter name="importCommand"><xsl:value-of select="$importCommand" /></parameter>
      <parameter name="exportCommand"><xsl:value-of select="$exportCommand" /></parameter>
      <parameter name="searchCommand"><xsl:value-of select="$searchCommand" /></parameter>
      <parameter name="inactivateCommand"><xsl:value-of select="$inactivateCommand" /></parameter>
      <parameter name="activateCommand"><xsl:value-of select="$activateCommand" /></parameter>
      <parameter name="currentTopicId"><xsl:value-of select="$currentTopicId" /></parameter>
      <parameter name="currentTopicName"><xsl:value-of select="$currentTopicName" /></parameter>
      <parameter name="optId"><xsl:value-of select="$optId" /></parameter>
      <parameter name="offName"><xsl:value-of select="$offName" /></parameter>
      <parameter name="offDesc"><xsl:value-of select="$offDesc" /></parameter>
      <parameter name="topicName"><xsl:value-of select="$topicName" /></parameter>
      <parameter name="catChannel"><xsl:value-of select="$catChannel" /></parameter>

      <parameter name="offeringIdParam"><xsl:value-of select="$offeringIdParam" /></parameter>
      <parameter name="offeringTermParam"><xsl:value-of select="$offeringTermParam" /></parameter>
      <parameter name="offeringMonthStartParam"><xsl:value-of select="$offeringMonthStartParam" /></parameter>
      <parameter name="offeringDayStartParam"><xsl:value-of select="$offeringDayStartParam" /></parameter>
      <parameter name="offeringYearStartParam"><xsl:value-of select="$offeringYearStartParam" /></parameter>
      <parameter name="offeringMonthEndParam"><xsl:value-of select="$offeringMonthEndParam" /></parameter>
      <parameter name="offeringDayEndParam"><xsl:value-of select="$offeringDayEndParam" /></parameter>
      <parameter name="offeringYearEndParam"><xsl:value-of select="$offeringYearEndParam" /></parameter>
      <parameter name="offeringMtgMonParam"><xsl:value-of select="$offeringMtgMonParam" /></parameter>
      <parameter name="offeringMtgTueParam"><xsl:value-of select="$offeringMtgTueParam" /></parameter>
      <parameter name="offeringMtgWedParam"><xsl:value-of select="$offeringMtgWedParam" /></parameter>
      <parameter name="offeringMtgThuParam"><xsl:value-of select="$offeringMtgThuParam" /></parameter>
      <parameter name="offeringMtgFriParam"><xsl:value-of select="$offeringMtgFriParam" /></parameter>
      <parameter name="offeringMtgSatParam"><xsl:value-of select="$offeringMtgSatParam" /></parameter>
      <parameter name="offeringMtgSunParam"><xsl:value-of select="$offeringMtgSunParam" /></parameter>
      <parameter name="offeringHourStartParam"><xsl:value-of select="$offeringHourStartParam" /></parameter>
      <parameter name="offeringMinuteStartParam"><xsl:value-of select="$offeringMinuteStartParam" /></parameter>
      <parameter name="offeringAmPmStartParam"><xsl:value-of select="$offeringAmPmStartParam" /></parameter>
      <parameter name="offeringHourEndParam"><xsl:value-of select="$offeringHourEndParam" /></parameter>
      <parameter name="offeringMinuteEndParam"><xsl:value-of select="$offeringMinuteEndParam" /></parameter>
      <parameter name="offeringAmPmEndParam"><xsl:value-of select="$offeringAmPmEndParam" /></parameter>
      <parameter name="offeringLocationParam"><xsl:value-of select="$offeringLocationParam" /></parameter>
      <parameter name="currentMonth"><xsl:value-of select="$currentMonth" /></parameter>
      <parameter name="currentDay"><xsl:value-of select="$currentDay" /></parameter>
      <parameter name="currentYear"><xsl:value-of select="$currentYear" /></parameter>

      <parameter name="copyCommand"><xsl:value-of select="$copyCommand" /></parameter>
      <parameter name="copySubmitCommand"><xsl:value-of select="$copySubmitCommand" /></parameter>

      <parameter name="addOffering"><xsl:value-of select="$addOffering" /></parameter>
      <parameter name="editOffering"><xsl:value-of select="$editOffering" /></parameter>
      <parameter name="deleteOffering"><xsl:value-of select="$deleteOffering" /></parameter>
      <parameter name="importOffering"><xsl:value-of select="$importOffering" /></parameter>
      <parameter name="exportOffering"><xsl:value-of select="$exportOffering" /></parameter>
      <parameter name="inactivateOffering"><xsl:value-of select="$inactivateOffering" /></parameter>
      <parameter name="activateOffering"><xsl:value-of select="$activateOffering" /></parameter>
      <parameter name="searchOffering"><xsl:value-of select="$searchOffering" /></parameter>

      <parameter name="copyOffering"><xsl:value-of select="$copyOffering" /></parameter>
    </textarea> -->

    <xsl:apply-templates />
</xsl:template>

  <xsl:template match="offeringAdmin">

  <xsl:call-template name="links"/>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th class="th-top" id="offeringAdminTopicName">Topic Name</th>
            <th class="th-top" id="offeringAdminOfferingName">Offering Name</th>
        </tr>

    <xsl:choose>
    <xsl:when test="count(offering) = 0">
        <tr>
            <td colspan="2" class="table-content-single-bottom">
            <xsl:choose>
            <xsl:when test="$current_command = 'viewInactive'">
                Currently, there are no inactive offerings.
            </xsl:when>
            <xsl:otherwise>
                Currently, there are no offerings.
            </xsl:otherwise>
            </xsl:choose>
            </td>
        </tr>
    </xsl:when>
    <xsl:otherwise>
        <xsl:apply-templates select="offering">
            <xsl:sort select="topic/name"/>
            <xsl:sort select="name"/>
        </xsl:apply-templates>
    </xsl:otherwise>
    </xsl:choose>

    </table>

    <xsl:call-template name="catalog"/> <!-- Catalog for paging and search called from global.xsl -->

  </xsl:template>

  <xsl:template match="offering">
  <xsl:variable name = "bottomStyle"><xsl:if test = "position() = last()">-bottom</xsl:if></xsl:variable>
  <tr>
      <td class="table-light-left{$bottomStyle}" style="text-align:right;vertical-align:top;" width="40%" headers="offeringAdminTopicName">
          <xsl:value-of select="topic/name" />
      </td>
      <td class="table-content-right{$bottomStyle}" style="text-align:left;vertical-align:top;" width="60%" headers="offeringAdminOfferingName">

        <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}" title="View details for this offering"
        onmouseover="swapImage('offeringAdminDetailsImage{@id}','channel_view_active.gif')"
        onmouseout="swapImage('offeringAdminDetailsImage{@id}','channel_view_base.gif')">
        <xsl:value-of select="name" />
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
        alt="'View' icon: offering details for '{name}'"
        title="'View' icon: offering details for '{name}'"
        align="absmiddle"
        name="offeringAdminDetailsImage{@id}" id="offeringAdminDetailsImage{@id}"/></a>

<xsl:choose>
    <xsl:when test="$editOffering = 'Y' and enrollmentModel != 'sis' ">
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
        <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={@id}&amp;optId={$optId}&amp;topicName={$topicName}&amp;offName={$offName}" title="Edit this offering"
        onmouseover="swapImage('offeringAdminEditImage{@id}','channel_edit_active.gif')"
        onmouseout="swapImage('offeringAdminEditImage{@id}','channel_edit_base.gif')">
        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
        alt="'Edit' icon: edit offering details for '{name}'"
        title="'Edit' icon: edit offering details for '{name}'"
        align="absmiddle" name="offeringAdminEditImage{@id}" id="offeringAdminEditImage{@id}"/></a>
    </xsl:when>
    <xsl:otherwise>
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
        alt="Inactive 'Edit' icon: edit offering details unavailable due to lack of permission"
        title="Inactive 'Edit' icon: edit offering details unavailable due to lack of permission" align="absmiddle" />
    </xsl:otherwise>
</xsl:choose>


<xsl:choose>
    <xsl:when test="$exportOffering = 'Y'">
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
        <a href="{$workerActionURL}&amp;ID={@id}" title="Export this offering" target="hidden_download"
        onmouseover="swapImage('offeringAdminExportImage{@id}','channel_export_active.gif')"
        onmouseout="swapImage('offeringAdminExportImage{@id}','channel_export_base.gif')">
        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_base.gif"
        alt="'Export' icon: export offering '{name}'"
        title="'Export' icon: export offering '{name}'"
        align="absmiddle" name="offeringAdminExportImage{@id}" id="offeringAdminExportImage{@id}"/></a>
    </xsl:when>
    <xsl:otherwise>
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_export_inactive.gif"
        alt="Inactive 'Export' icon: export offering unavailable due to lack of permission"
        title="Inactive 'Export' icon: export offering unavailable due to lack of permission" align="absmiddle"/>
    </xsl:otherwise>
</xsl:choose>


<xsl:choose>
    <xsl:when test="$current_command = 'viewAll'">
        <xsl:choose>
        <xsl:when test="$inactivateOffering = 'Y'">
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$inactivateCommand}&amp;ID={@id}&amp;optId={$optId}&amp;topicName={$topicName}&amp;offName={$offName}"
            title="Inactivate this offering"
            onmouseover="swapImage('offeringAdminDeleteImage{@id}','channel_delete_active.gif')"
            onmouseout="swapImage('offeringAdminDeleteImage{@id}','channel_delete_base.gif')">
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
            alt="'Delete' icon: make offering '{name}' inactive"
            title="'Delete' icon: make offering '{name}' inactive"
            align="absmiddle" name="offeringAdminDeleteImage{@id}" id="offeringAdminDeleteImage{@id}"/></a>
        </xsl:when>
        <xsl:otherwise>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
            alt="Inactive 'Delete' icon: making offering inactive is unavailable due to lack of permission"
            title="Inactive 'Delete' icon: making offering inactive is unavailable due to lack of permission"
            align="absmiddle" />
        </xsl:otherwise>
        </xsl:choose>
    </xsl:when>

    <xsl:when test="$current_command = 'viewInactive'">
        <xsl:choose>
        <xsl:when test="$activateOffering = 'Y'">
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$activateCommand}&amp;ID={@id}&amp;optId={$optId}&amp;topicName={$topicName}&amp;offName={$offName}"
            title="Activate this offering"
            onmouseover="swapImage('offeringAdminActivateImage{@id}','channel_add_active.gif')"
            onmouseout="swapImage('offeringAdminActivateImage{@id}','channel_add_base.gif')">
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
            alt="'Add' icon: make '{name}' an active offering"
            title="'Add' icon: make '{name}' an active offering"
            align="absmiddle" name="offeringAdminActivateImage{@id}" id="offeringAdminActivateImage{@id}"/></a>
        </xsl:when>
        <xsl:otherwise>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif"
            alt="Inactive 'Add' icon: making offering active is unavailable due to lack of permission"
            title="Inactive 'Add' icon: making offering active is unavailable due to lack of permission" align="absmiddle" />
        </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
        <xsl:when test="$deleteOffering = 'Y'">
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$deleteCommand}&amp;ID={@id}&amp;optId={$optId}&amp;topicName={$topicName}&amp;offName={$offName}"
            title="Delete this offering"
            onmouseover="swapImage('offeringAdminDeleteImage{@id}','channel_delete_active.gif')"
            onmouseout="swapImage('offeringAdminDeleteImage{@id}','channel_delete_base.gif')">
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
            alt="'Delete' icon: delete offering '{name}'"
            title="'Delete' icon: delete offering '{name}'"
            align="absmiddle" name="offeringAdminDeleteImage{@id}" id="offeringAdminDeleteImage{@id}"/></a>
        </xsl:when>
        <xsl:otherwise>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
            alt="Inactive 'Delete' icon: delete offerings unavailable due to lack of permission"
            title="Inactive 'Delete' icon: delete offerings unavailable due to lack of permission" align="absmiddle"/>
        </xsl:otherwise>
        </xsl:choose>

    </xsl:when>
</xsl:choose>


<!-- Start New XSL for requirement OA 2.1 -->
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
<!-- End New XSL for requirement OA 2.1 -->



    </td>
</tr>
</xsl:template>

</xsl:stylesheet>



