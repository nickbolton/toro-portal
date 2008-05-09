<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0">

<!-- Import -->
<xsl:import href="../global/global.xsl"/>

  <xsl:param name="errorMessageParam"/>
  

  <xsl:template match="errorPage">
  <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <td colspan="2" class="table-content-single" style="text-align:center;">
				<span class="uportal-channel-warning"><xsl:value-of select="message"/></span>
				
        </td>
        </tr>
        <tr>
            <td colspan="2" class="table-nav" style="text-align:center">
				Click <a href="{$baseActionURL}?command=cancel">here</a> to return.
			</td>
        </tr>
 		</table>
  </xsl:template>
  
</xsl:stylesheet>
