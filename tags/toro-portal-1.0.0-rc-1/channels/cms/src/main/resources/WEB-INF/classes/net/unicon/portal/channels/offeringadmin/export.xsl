<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="export">
    <xsl:call-template name="links"/>
    <!-- UniAcc: Layout Table -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <td class="table-content-right-bottom" width="100%">Click <a href="{@file}">here</a> to download the archive.</td>
        </tr>
    </table>
  </xsl:template>

</xsl:stylesheet>











