<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
<xsl:variable name="SELECTED_GROUP_ID"><xsl:value-of select="/announcement-edit/announcement/@category-id"/></xsl:variable>


	<!--<xsl:template match="/">
	    <textarea rows="4" cols="40">
	      <xsl:copy-of select = "*"/> 
	      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
	      <parameter name="pageChannel"><xsl:value-of select="$pageChannel" /></parameter>
	      <parameter name="channel_admin"><xsl:value-of select="$channel_admin" /></parameter>
	   	</textarea>
	
		<xsl:apply-templates />
	</xsl:template> -->
	
    <xsl:template match="/announcement-edit">

        <xsl:call-template name="submissionJS"/>
        <xsl:call-template name="links"/>

        <form action="{$baseActionURL}?command=main" method="post" onsubmit="return validator.applyFormRules(this, new AnnouncementRulesObject())">
            <input type="hidden" name="sub-command" value="submit-announcement" />
            <input type="hidden" name="announcement-id" value="{./announcement/@announcement-id}" />
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top">
                    <xsl:choose>
                    	<xsl:when test="./announcement/@announcement-id = '-1'">Add Announcement</xsl:when>
                      
                    	<xsl:otherwise>Edit Announcement</xsl:otherwise>
                    </xsl:choose>
                    </th>
                </tr>
                <tr>
                  <td class="table-light-left" align="right" nowrap="nowrap">Date</td>
                  <td class="table-content-right" align="left" width="100%">
                    <xsl:value-of select="./announcement/@date" />
                  </td>
                </tr>
                <tr>
                  <td class="table-light-left" align="right" nowrap="nowrap">Group</td>
                  <td class="table-content-right" align="left" width="100%">
                    <select name="category-id">
                      <xsl:apply-templates select="./group" />
                    </select>
                  </td>
                </tr>
                <tr>
                  <td class="table-light-left" align="right" nowrap="nowrap">Announcement</td>
                  <td class="table-content-right" align="left" width="100%">
                    <textarea name="message" cols="40">
                      <xsl:value-of select="./announcement/announcement-body" />
                    </textarea>
                  </td>
                </tr>
                <tr>
                  <td colspan="2" class="table-nav">
                    <input name="Submit" value="Submit" type="submit" class="uportal-button" title="To finish editing this announcement, saving the changes"/>
                    <input name="Cancel" value="Cancel" type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}'" title="To cancel editing this announcement, returning to viewing the announcements"/>
                  </td>
                </tr>
            </table>
        </form>
    </xsl:template>

    <xsl:template match="group">
      <xsl:variable name="GROUP_ID"><xsl:value-of select="@group-id"/></xsl:variable>
      <xsl:variable name="GROUP_NAME"><xsl:value-of select="@name"/></xsl:variable>
      <option value="{$GROUP_ID}"><xsl:if test="$SELECTED_GROUP_ID = $GROUP_ID"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if><xsl:value-of select="@name"/></option>
    </xsl:template>

</xsl:stylesheet>
