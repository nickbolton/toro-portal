<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:param name="maximumAge"/>
<xsl:param name="pageSize"/>

<xsl:template match="/">
  <xsl:call-template name="links"/>

  <form name="editPreferencesForm" action="{$baseActionURL}" method="post">
    <input type="hidden" name="command" value="main"></input>
    <input type="hidden" name="sub-command" value="submit-preferences"></input>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <th class="th-left" nowrap="nowrap">Property</th>
        <th class="th-right" width="100%">Value</th>
      </tr>
      <tr>
        <td class="table-light-left" style="vertical-align:top;" nowrap="nowrap">Page Size</td>
        <td class="table-content-right">
            <select name="selectPageSize">
                <option value="5"><xsl:if test="$pageSize = '5'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>5</option>
                <option value="10"><xsl:if test="$pageSize = '10'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>10</option>
                <option value="15"><xsl:if test="$pageSize = '15'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>15</option>
                <option value="25"><xsl:if test="$pageSize = '25'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>25</option>
                <option value="40"><xsl:if test="$pageSize = '40'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>40</option>
            </select>
        </td>
      </tr>
      <tr>
        <td class="table-light-left" style="vertical-align:top;" nowrap="nowrap">Maximum Announcement<br />Age (In Days)</td>
        <td class="table-content-right">
            <select name="selectMaximumAge">
                <option value="7"><xsl:if test="$maximumAge = '7'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>7</option>
                <option value="14"><xsl:if test="$maximumAge = '14'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>14</option>
                <option value="30"><xsl:if test="$maximumAge = '30'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>30</option>
                <option value="60"><xsl:if test="$maximumAge = '60'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>60</option>
            </select>
        </td>
      </tr>
      <tr>
        <td class="table-light-left" style="vertical-align:top;" nowrap="nowrap">Available<br />Announcement Groups</td>
        <td class="table-content-right">
          <ul>
            <xsl:apply-templates select="./campus-announcement-preferences/categories/category" />
          </ul>
        </td>
      </tr>
      <tr>
        <td colspan="2" class="table-nav">
			<nobr>
				<input type="submit" class="uportal-button" name="Submit" value="Submit" />
				<input type="submit" class="uportal-button" name="Submit" value="Cancel" />
			</nobr>
		</td>
      </tr>
    </table>
  </form>
</xsl:template>

<xsl:template match="category">
  <xsl:variable name="SELECTED"><xsl:value-of select="@selected"/></xsl:variable>
  <xsl:variable name="VALUE"><xsl:value-of select="@value"/></xsl:variable>

  <xsl:if test="$SELECTED = 'true'"><li><input name="selectedCategories" value="{$VALUE}" type="checkbox" class="radio" checked="checked" /><xsl:value-of select="." /></li></xsl:if>
  <xsl:if test="$SELECTED = 'false'"><li><input name="selectedCategories" value="{$VALUE}" type="checkbox" class="radio" /><xsl:value-of select="." /></li></xsl:if>
</xsl:template>

</xsl:stylesheet>
