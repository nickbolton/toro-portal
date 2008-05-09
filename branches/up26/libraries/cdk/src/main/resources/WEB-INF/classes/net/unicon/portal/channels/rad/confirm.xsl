<?xml version='1.0'?>

<!--

   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.

   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.

   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.

-->

<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method='html'/>
<xsl:param name='baseActionURL'>default</xsl:param>
<xsl:param name='sid'>default</xsl:param>
<xsl:param name='baseImagePath'>media/net/unicon/portal/channels/rad</xsl:param>
<!-- //////////////////////////////////////////////////////// -->
<xsl:template match='confirm'>
  <form action="{$baseActionURL}" method="post">
    <table width="100%" cellpadding="0" cellspacing="0" border="0">
    <tr>
      <td class="table-content-single-top">
        <span class="uportal-channel-warning"><xsl:copy-of select="text"/></span>
      </td>
    </tr>
    <tr>
      <td class="table-content-single-bottom" style="text-align:center">
        <input type="hidden" name="sid" value="{$sid}"/>
        <input type="submit" class="uportal-button" value="OK" name="do~clickOK"
        title="To submit your confirmation response"/>
        <input type="submit" class="uportal-button" value="Cancel" name="do~cancel"
        title="To return to the previous view without submitting a confirmation"/>
      </td>
    </tr>
    </table>
  </form>
</xsl:template>
</xsl:stylesheet>



