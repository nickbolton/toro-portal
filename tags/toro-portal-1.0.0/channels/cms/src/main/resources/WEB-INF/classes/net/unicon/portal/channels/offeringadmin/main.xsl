<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="offeringAdmin">

  <xsl:call-template name="links"/>
	<div class="bounding-box2">
        <!-- UniAcc: Layout Table -->
      	<table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="table-content-single-bottom">
    				Select an offering administration option.
                </td>
            </tr>
        </table>
    </div>
   
  </xsl:template>
  
</xsl:stylesheet>



