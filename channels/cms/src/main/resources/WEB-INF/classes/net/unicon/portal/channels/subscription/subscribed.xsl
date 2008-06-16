<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:include href="common.xsl"/>

<xsl:param name="addCommand"/>
<xsl:param name="viewCommand"/>
<xsl:param name="editCommand"/>
<xsl:param name="deleteCommand"/>

  <xsl:template match="offeringAdmin">

    <p class="uportal-channel-text">Offering Administration allows an authorized user to add, edit, or import Offering information in the system. </p>

    <xsl:call-template name="links"/>

    <table>
        <tr>
            <th class="uportal-background-med">Offering Name</th>
            <th colspan="3" class="uportal-background-med">Options</th>
        </tr>
        <xsl:apply-templates select="offering"> 
          <xsl:sort select="name"/>
        </xsl:apply-templates>
    </table>

    <xsl:call-template name="offeringSearch"/>

    <xsl:call-template name="bottom-links"/>
  </xsl:template>

  <xsl:template name="bottom-links">
    <p class="uportal-channel-text">Options:
        <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$addCommand}">Add</a>
    </p>
  </xsl:template>

  <xsl:template match="offering">
  <tr>
      <td class="uportal-background-light">
          <xsl:value-of select="name" />
      </td>
      <td class="uportal-background-light"><a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}">View</a></td>
      <td class="uportal-background-light"><a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={@id}">Edit</a></td>
      <td class="uportal-background-light"><a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$deleteCommand}&amp;ID={@id}">Delete</a></td>
  </tr>
  </xsl:template>
</xsl:stylesheet>











