<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- NOTE: This XSL was made obsolete by the usability requirement OF 1.1, which made the default page a search form -->

<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">

    <xsl:call-template name="links"/>

    <form action="{$baseActionURL}?command=find" method="post">
    <input type="hidden" name="targetChannel" value="{$targetChannel}"/>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <th class="th" colspan="2">Search for Offering</th>
    </tr>

    <!-- Topic Name -->
    <tr>
        <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
            Topic Name
        </td>
        <td class="table-content-right" style="text-align:left;" width="100%">
            <input class="text" type="text" name="topic_name" size="35" maxlength="35"/>
            <span class="uportal-text-small"> (35 char max)</span>    
        </td>
    </tr>
                    
    <!-- Offering Name -->
    <tr>
        <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
            Offering Name
        </td>
        <td class="table-content-right" style="text-align:left;" width="100%">
            <input class="text" type="text" name="offering_name" size="15" maxlength="15"/>
            <span class="uportal-text-small"> (15 char max)</span>   
        </td>
    </tr>
                    
    <!-- Offering Description -->
    <tr>
        <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
            Offering Description
        </td>
        <td class="table-content-right" style="text-align:left;" width="100%">
            <input class="text" type="text" name="offering_description" size="60" maxlength="60"/>
            <span class="uportal-text-small"> (60 char max)</span>   
        </td>
    </tr>
                    
    <!-- Enrollment Type -->
    <tr>
        <td class="table-light-left" style="text-align:right;" nowrap="nowrap">
            Enrollment Type
        </td>
        <td class="table-content-right" style="text-align:left;" width="100%">
            <select name="enrollment" id="enrollment">
                <option value="1" SELECTED>All Types</option>
                <option value="2">Open</option>
                <option value="3">Request &amp; Approve</option>
                <option value="4">Invite Only</option>
            </select>  
        </td>
    </tr>
    
    <tr>
        <td colspan="2" class="table-nav">
            <input class="uportal-button" name="submit" value="Submit" type="submit" title=
                "Submit offering search"/>
            <input type="button" class="uportal-button" name="cancel" value="Cancel" onclick=
                "window.locationhref='{$baseActionURL}'" title="Cancel offering search"/>    
        </td>
    </tr>
    </table>
    </form>
    </xsl:template>
    
</xsl:stylesheet>
