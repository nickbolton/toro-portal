<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">
    <xsl:call-template name="links"/>
    <table cellspacing="0" cellpadding="0" border="0" width="100%">
    <tr>
        <th colspan="2" class="th">Curriculum</th>
    </tr>
        <td class="table-light-left-bottom" nowrap="nowrap">Curriculum :</td>
        <td class="table-content-right-bottom" width="100%">Currently, there is no curriculum assigned to this offering.</td>
    </table>
</xsl:template>
</xsl:stylesheet>
