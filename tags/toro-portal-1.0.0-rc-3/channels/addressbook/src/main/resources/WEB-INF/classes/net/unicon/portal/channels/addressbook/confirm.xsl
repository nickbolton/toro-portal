<?xml version="1.0" encoding="utf-8"?>

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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html"/>
  <xsl:include href="common.xsl"/>
  <xsl:param name="backRoot">default</xsl:param>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="sid">default</xsl:param>
  <xsl:param name="goURL" select="concat($baseActionURL,'?back=',$sid,'&amp;go')"/>
  <xsl:param name="doURL" select="concat($baseActionURL,'?sid=',$sid,'&amp;do')"/>
  <xsl:param name="baseMediaPath">media/net/unicon/portal/channels/rad</xsl:param>
<!-- //////////////////////////////////////////////////////// -->
    <xsl:template match="confirm">
        <form method="post" action="{$baseActionURL}">
            <!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top-single">Confirm</th>
                </tr>
                <tr>
                    <td class="table-content-right" width="100%">
                      Some of imported contacts are existing in your address book. What do you wish to do?
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" style="text-align:center">
                        <input type="hidden" name="sid" value="{$sid}"/>
                        <input type="hidden" name="default" value="do~replace"/>
                        <input type="submit" class="uportal-button" value="Replace" name="do~replace" title="Overwrite those contact"/>
                        <input type="submit" class="uportal-button" value="Add" name="do~add" title="Add those contact"/>
                        <input type="submit" class="uportal-button" value="Skip" name="do~skip" title="Skip those contacts"/>
                        <input type="submit" class="uportal-button" value="Cancel" name="do~cancel" title="Cancel"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>




