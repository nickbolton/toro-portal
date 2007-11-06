<?xml version="1.0" encoding="utf-8"?>
<!--     Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.     This software is the confidential and proprietary information of    Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not    disclose such Confidential Information and shall use it only in    accordance with the terms of the license agreement you entered into    with IBS-DP or its authorized distributors.     IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY    OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT    LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A    PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE    FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING    OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" />
<!-- for all screens -->
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="resourceURL">default</xsl:param>
<xsl:param name="sid">default</xsl:param>
<xsl:param name="targetChannel">default</xsl:param>
<xsl:param name="root">false</xsl:param>
<xsl:param name="focusedChannel">
    <xsl:value-of select="$targetChannel" />
</xsl:param>
<xsl:param name="baseImagePath">media/net/unicon/portal/channels/rad</xsl:param>
<xsl:param name="backRoot" />
<xsl:param name="back">default</xsl:param>
<xsl:param name="goURL">default</xsl:param>
<xsl:param name="doURL">default</xsl:param>
<!-- current date -->
<xsl:param name="cur-date">default</xsl:param>
<!-- used only for navigation-bar -->
<!-- Set Variable Names for image links -->
<xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
<xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
<xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
<xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/flash/academus</xsl:variable>
<xsl:variable name = "SPACER"><xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>
insertinclude
<xsl:variable name="summary" />
<xsl:variable name="mode" />
<xsl:param name="CurPage">1</xsl:param>
<xsl:param name="Title">default</xsl:param>
<xsl:template match="survey-system">
    <xsl:variable name="MaxPage" select="count(Form/XMLForm/page)" />
    <center>
        <form method="post" action="{$baseActionURL}">
            <input type="hidden" name="sid" value="{$sid}" />
            <input type="hidden" name="page-id" value="{Form/XMLForm/page[position()=$CurPage]/@page-id}" />
            <!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" width="100%" border="0">
                <tr>
                    <td nowrap="nowrap" class="th-top">Survey Preview :&#160; 
                    <xsl:value-of select="$Title" />
                    <img border="0" src="{$SPACER}" height="20" align="absmiddle" alt="" title="" />
                    </td>
                </tr>
                <tr>
                    <td class="table-content">
                    	<!--UniAcc: Layout Table -->
                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                            <tr>
                                <td>
                                    <xsl:apply-templates select="Form/XMLForm/page[position()=$CurPage]">
                                        <xsl:with-param name="response" select="view/response" />
                                    </xsl:apply-templates>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav">
                    	<!--UniAcc: Layout Table -->
                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                            <tr>
                                <td>
                                    <input type="submit" class="uportal-button" name="do~close" value="Close Preview" />
                                </td>
                                <td align="right" nowrap="nowrap" class="uportal-text">
                                <xsl:choose>
                                    <xsl:when test="$CurPage != 1">
                                        <a href="{$doURL}=previous&amp;MaxPage={$MaxPage}&amp;CurPage={$CurPage}" title="Display">
                                            <img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <img src="{$baseImagePath}/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                    </xsl:otherwise>
                                </xsl:choose>
                                &#160; 
                                <xsl:value-of select="$CurPage" />
                                / 
                                <xsl:value-of select="$MaxPage" />
                                &#160; 
                                <xsl:choose>
                                    <xsl:when test="$CurPage != $MaxPage">
                                        <a href="{$doURL}=next&amp;MaxPage={$MaxPage}&amp;CurPage={$CurPage}" title="Display">
                                            <img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
                                        </a>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <img src="{$baseImagePath}/next_disabled_12.gif" border="0" align="absmiddle" alt="" title="" />
                                    </xsl:otherwise>
                                </xsl:choose>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </center>
</xsl:template>
</xsl:stylesheet>

