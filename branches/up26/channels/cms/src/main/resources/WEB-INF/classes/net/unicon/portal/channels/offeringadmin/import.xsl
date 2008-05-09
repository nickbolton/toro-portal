<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="/">
    <xsl:call-template name="links"/>
    <xsl:call-template name="autoFormJS"/>
    
    <form name="offeringAdminForm" action="{$baseActionURL}" method="post" onsubmit="return validator.applyFormRules(this, new offeringAdminRulesObject())">
    <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <th class="th" colspan="2">
            Import Offering
        </th>
    </tr>
    <tr>
        <td class="table-light-left" style="text-align:right">
            Import from: <input type="radio" name="import-type" checked="checked" />Exported offering (file)
        </td>
        <td class="table-content-right" style="text-align:left">
            <input name="import-file" type="file" style="text-align: left;" size="50" maxlength="50" />
        </td>
    </tr>
    <tr>
        <td class="table-light-single" style="text-align:center" colspan="2">
            <strong>- OR -</strong>
        </td>
    </tr>
    <tr>
        <td class="table-light-left" style="text-align:left;vertical-align:top;" rowspan="2">
            <input type="radio" name="import-type" />Active offering
        </td>
        <td class="table-content-right" style="text-align:center">
            Parent Topic:
            <xsl:choose>
                <xsl:when test="count(topic) = 0">
                      Currently, there are no topics.
                  </xsl:when>
                  <xsl:otherwise>
                    <select name="topicId" onchange="" size="1">
                        <xsl:apply-templates select="topic">
                             <xsl:sort select="name"/>
                        </xsl:apply-templates>
                    </select>
                    <input type="submit" class="uportal-button" value="Display"/>
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </tr>
    <tr>
        <xsl:choose>
        <xsl:when test="count(offering) = 0">
        <td class="table-content-right">
                Currently, there are no offerings.
        </td>
        </xsl:when>
        <xsl:otherwise>
        <td class="table-content-right" style="padding-top:0px; padding-bottom:0px; padding-left:0px; padding-right:0px;">
                        
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <th class="th">Select</th>
                <th class="th">Name</th>
                <th class="th" width="100%">Description</th>
            </tr>
            <tr>
                <td class="table-light" style="text-align:center;">
                    <input type="checkbox" name="import-select{@id}" id="import-select{@id}" />
                </td>
                <td class="table-content" style="text-align:center;" width="120">
                    <xsl:value-of select="name"/>
                </td>
                <td class="table-content" style="text-align:center;" width="100%">
                    <xsl:value-of select="description"/>
                </td>
            </tr>    
            </table>
                        
        </td>
        </xsl:otherwise>
        </xsl:choose>        
    </tr>
    <tr>
        <td class="table-nav" colspan="2">
            <input type="submit" class="uportal-button" value="Submit" title="Submit import offering"/>
            <input type="submit" class="uportal-button" value="Cancel" title="Cancel import offering"/>
        </td>
    </tr>
    </table>
    
    </form>
    
  </xsl:template>
  
  <xsl:template match="topic">
    <xsl:choose>
      <xsl:when test="@id = $currentTopicId">
        <option value="{@id}" selected="selected"><xsl:value-of select="name"/></option>
      </xsl:when>
      <xsl:otherwise>
        <option value="{@id}"><xsl:value-of select="name"/></option>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>











