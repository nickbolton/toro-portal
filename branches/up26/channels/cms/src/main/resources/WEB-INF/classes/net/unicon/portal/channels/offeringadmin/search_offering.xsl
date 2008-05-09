<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="offeringAdmin">
<xsl:call-template name="autoFormJS"/>
<xsl:call-template name="links"/>
<form name="offeringAdminForm" action="{$baseActionURL}" method="post" onsubmit="return validator.applyFormRules(this, new OfferingAdminRulesObject())">
<input type="hidden" name="targetChannel" value="{$targetChannel}" />
<input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
<input type="hidden" name="command" id="command" value="{$catCurrentCommand}" />
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<tr>
    <th class="th" colspan="2">
        Search for Offering
    </th>
</tr>

<!-- Topic Name -->
<tr>
    <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
        <label for="offeringAdminFormTopicName">Topic Name</label>
    </td>
    <td class="table-content-right" style="text-align:left;" width="100%">
        <input class="text" type="text" name="topicName" size="15" maxlength="15" id="offeringAdminFormTopicName" />
    </td>
</tr>
                    
<!-- Offering Name -->
<tr>
    <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
        <label for="offeringAdminFormOfferingName">Offering Name</label>
    </td>
    <td class="table-content-right" style="text-align:left;" width="100%">
        <input class="text" type="text" name="offName" size="15" maxlength="15" id="offeringAdminFormOfferingName"/>  
    </td>
</tr>

<!-- Offering ID -->
<tr>
    <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
        <label for="offeringAdminFormOfferingID">Offering ID</label>
    </td>
    <td class="table-content-right" style="text-align:left;" width="100%">
        <input class="text" type="text" name="optId" size="15" maxlength="15" id="offeringAdminFormOfferingID"/> 
    </td>
</tr>
                    
<tr>
    <td colspan="2" class="table-nav" style="text-align:center">
        <input class="uportal-button" name="submit" value="Submit" type="submit" 
        title="Submit offering search" />
        <input type="button" class="uportal-button" name="cancel" value="Cancel" 
        onclick="window.locationhref='{$baseActionURL}'" title="Cancel offering search" />    
    </td>
</tr>
</table>
</form>
</xsl:template>
</xsl:stylesheet>
