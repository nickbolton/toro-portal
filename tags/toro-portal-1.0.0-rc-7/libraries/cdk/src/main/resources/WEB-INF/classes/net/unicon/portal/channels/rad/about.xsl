<?xml version="1.0"?>
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
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="sid">default</xsl:param>
  <xsl:param name="skin">rad</xsl:param>
  <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>
  <!-- ////////////////// -->
  <xsl:template match="about">
    <form action="{$baseActionURL}" method="post">
      <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="uportal-channel-emphasis" colspan="2" height="20">
            <i>About</i>
          </td>
        </tr>
        <tr>
          <td class="uportal-background-light" colspan="2">
            <img src="{$baseImagePath}/rad/transparent.gif" border="0" height="2"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <img src="{$baseImagePath}/rad/transparent.gif" border="0" height="6"/>
          </td>
        </tr>
        <tr class="uportal-background-content" valign="middle">
          <td width="1%" align="center">
            <img border="0" src="{$baseImagePath}/rad/{logo}"/>
            <xsl:text>&#160;</xsl:text>
          </td>
          <td class="uportal-channel-text">
            <table>
              <tr>
                <td class="uportal-channel-text">Version <xsl:value-of select="version"/>
                </td>
              </tr>
              <tr>
                <td class="uportal-channel-text">
                  <xsl:value-of select="copyright"/>
                </td>
              </tr>
              <xsl:if test="product-desc">
                <tr>
                  <td class="uportal-channel-text">
                    <xsl:value-of select="product-desc"/>
                  </td>
                </tr>
              </xsl:if>
            </table>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <img src="{$baseImagePath}/rad/transparent.gif" border="0" height="6"/>
          </td>
        </tr>
        <xsl:if test="$skin='rad'">
          <tr class="uportal-background-light" height="22">
            <td colspan="2">
              <img src="{$baseImagePath}/rad/transparent.gif" border="0" width="1"/>
              <input type="hidden" name="sid" value="{$sid}"/>
              <input class="uportal-button" type="submit" value="OK" name="do~ok"/>
            </td>
          </tr>
	   </xsl:if>
	   <xsl:if test="$skin!='rad'">
          <tr>
            <td colspan="2">
              <img src="{$baseImagePath}/rad/transparent.gif" border="0" width="1"/>
              <input type="hidden" name="sid" value="{$sid}"/>
              <input type="image" name="do~ok" src="{$baseImagePath}/{$skin}/button_ok_17.gif" title="OK"/>
            </td>
          </tr>
	   </xsl:if>
      </table>
    </form>
  </xsl:template>
</xsl:stylesheet>
