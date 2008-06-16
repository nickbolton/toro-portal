<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="offering">

    <xsl:call-template name="links"/>

    <form name="offeringAdminForm" action="{$baseActionURL}" method="post">
    <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
    <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
        <input type="hidden" name="optId" value="{$optId}" />
        <input type="hidden" name="topicName" value="{$topicName}" />
        <input type="hidden" name="offName" value="{$offName}" />
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="table-content-single-top" style="text-align:center">
            <span class="uportal-channel-warning">Are you sure you want to inactivate the <xsl:value-of select="topicNameParam"/> class <span class="uportal-channel-strong">&quot;<xsl:value-of select="name"/>&quot;</span>?</span>
        </td>
    </tr>
    <tr>
        <td class="table-content-single" style="text-align:center">
            <input type="hidden" name="command" value="{$confirmInactivateCommand}"></input>
            <input type="hidden" name="ID" value="{@id}"></input>
            <input type="radio" class="radio" name="{$confirmParam}" value="yes"
            id="offeringAdminFormInactivateConfirm"/>&#160;<label for="offeringAdminFormInactivateConfirm">Yes</label>
            <img height="1" width="15" src="{$SPACER}" alt="" title="" border="0"/>
            <input checked="checked" type="radio" class="radio" name="{$confirmParam}" value="no"
            id="offeringAdminFormInactivateDeny"/>&#160;<label for="offeringAdminFormInactivateDeny">No</label><br />
        </td>
    </tr>
    <tr>
        <td class="table-content-single-bottom" style="text-align:center">
            <input type="submit" class="uportal-button" value="Submit" title="Submit choice for the inactivation of '{name}' offering" />
            <input type="button" class="uportal-button" value="Cancel" title="Cancel inactivation for the '{name}' offering and return to the main view"
            onclick="window.locationhref='{$baseActionURL}?command={$viewAllCommand}'"/>
        </td>
    </tr>

    </table>

    </form>

  </xsl:template>

</xsl:stylesheet>











