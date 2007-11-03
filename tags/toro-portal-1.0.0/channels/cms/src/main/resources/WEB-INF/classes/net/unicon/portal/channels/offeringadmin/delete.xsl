<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="offering">

    <xsl:call-template name="links"/>

    <form  name="offeringAdminForm" action="{$baseActionURL}" method="post">
        <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
        <input type="hidden" name="optId" value="{$optId}" />
        <input type="hidden" name="topicName" value="{$topicName}" />
        <input type="hidden" name="offName" value="{$offName}" />
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="table-content-single-top" style="text-align:center">
            <span class="uportal-channel-warning">This will delete all offering data. Are you sure you want to permanently delete the <span class="uportal-channel-strong">&quot;<xsl:value-of select="topic/name"/>&#160;-&#160;<xsl:value-of select="name"/>&quot;</span> offering?</span>
            This offering currently has <span class="uportal-channel-strong"><xsl:value-of select="$enrolledUsers"/></span> enrolled users.
            <xsl:if test="$navigateRemoveMessage = 'true'">
                <p><span class="uportal-channel-warning">NOTE:</span> The deleted Offering will not be removed from the Navigation Channel until the next time it is refreshed (to refresh the navigation channel, click the Learning tab).</p>
            </xsl:if>
        </td>
    </tr>
    <tr>
        <td class="table-content-single" style="text-align:center">
            <input type="hidden" name="command" value="{$deleteSubmitCommand}"></input>
            <input type="hidden" name="ID" value="{@id}"></input>
            <input type="radio" class="radio" name="{$confirmParam}" value="yes"
            id="offeringAdminFormDeleteConfirm"/>&#160;<label for="offeringAdminFormDeleteConfirm">Yes</label>
            <img height="1" width="15" src="{$SPACER}" alt="" title="" border="0"/>
            <input checked="checked" type="radio" class="radio" name="{$confirmParam}" value="no"
            id="offeringAdminFormDeleteDeny"/>&#160;<label for="offeringAdminFormDeleteDeny">No</label><br />
        </td>
    </tr>
    <tr>
        <td class="table-content-single-bottom" style="text-align:center">
            <input type="submit" class="uportal-button" value="Submit" title="Submit deletion for the '{name}' offering" />
            <input type="button" class="uportal-button" value="Cancel" title="Cancel deletion for the '{name}' offering and return to the main view"
            onclick="window.locationhref='{$baseActionURL}'"/>
        </td>
    </tr>
    </table>
    </form>
  </xsl:template>

</xsl:stylesheet>











