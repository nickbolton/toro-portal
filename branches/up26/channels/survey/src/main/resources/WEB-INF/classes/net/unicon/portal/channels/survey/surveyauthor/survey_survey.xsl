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
<xsl:param name="CurPage">1</xsl:param>
<xsl:param name="LogonUserName">default</xsl:param>
<xsl:template match="survey-system">
    <xsl:variable name="MaxPage" select="count(Form/XMLForm/page)" />

	<div class="portlet-toolbar-container">
		<div class="tool">
			<a href="{$baseActionURL}?channel_command=refresh" title="Refresh Surveys">
				<img src="{$CONTROLS_IMAGE_PATH}/channel_refresh_active.gif" class="toolbar-img" width="16" height="16"  border="0" alt="Refresh" title="Refresh"/>
				Refresh
			</a>
		</div>
	</div>
	
    <h2 class="page-title">
		<xsl:value-of select="Form/@Title" />,&#160; 
		<xsl:value-of select="substring-before(Form/Survey/@Sent,'_')" />&#160; 
		<xsl:call-template name="t24to12">
			<xsl:with-param name="hour" select="substring-after(Form/Survey/@Sent,'_')" />
		</xsl:call-template>
    </h2>   
    
    <div class="bounding-box1">
        <form action="{$baseActionURL}" method="post">
            <input type="hidden" name="sid" value="{$sid}" />
            <input type="hidden" name="MaxPage" value="{$MaxPage}" />
            <input type="hidden" name="page-id" value="{Form/XMLForm/page[position()=$CurPage]/@page-id}" />
            <!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" width="100%" border="0">
                <tr>
                    <td>
                    	<!--UniAcc: Layout Table -->
                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                            <tr>
                                <td class="th-top" nowrap="nowrap">
   
                                </td>
                            </tr>
                            <tr>
                                <td class="table-content" nowrap="nowrap">Sent by :&#160; 
                                <xsl:value-of select="Form/Survey/@UserName" />
                                <br />
                                <xsl:choose>
                                    <xsl:when test="Form/Survey/@Type='Named'">Replied by :&#160; 
                                    <xsl:value-of select="$LogonUserName" />
                                    </xsl:when>
                                    <xsl:otherwise>Replied by:&#160;Anonymous</xsl:otherwise>
                                </xsl:choose>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="table-content">
                        <xsl:apply-templates select="Form/XMLForm/page[position()=$CurPage]">
                            <xsl:with-param name="response" select="view" />
                        </xsl:apply-templates>
                    </td>
                </tr>
                <tr>
                    <td class="table-content">
                    	<!--UniAcc: Layout Table -->
                        <table cellpadding="0" cellspacing="0" width="100%" border="0">
                            <tr>
                                <td nowrap="nowrap">
                                    <xsl:if test="$CurPage != 1">
                                        <input type="submit" class="uportal-button" name="do" value="Previous" />
                                    </xsl:if>
                                    <xsl:if test="$CurPage != $MaxPage">
                                        <input type="submit" class="uportal-button" name="do" value="Next" />
                                    </xsl:if>
                                    <xsl:if test="$CurPage = $MaxPage">
                                        <input type="submit" class="uportal-button" name="do" value="Submit" />
                                    </xsl:if>
                                    <input type="submit" class="uportal-button" name="do" value="Cancel" />
                                </td>
                                <td align="right" nowrap="nowrap" class="uportal-text">
                                <xsl:value-of select="$CurPage" />
                                / 
                                <xsl:value-of select="$MaxPage" />
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</xsl:template>
<xsl:template name="t24to12">
    <xsl:param name="hour" />
    <xsl:variable name="h" select='substring-before($hour,":")' />
    <xsl:variable name="m" select='substring-after($hour,":")' />
    <xsl:choose>
        <xsl:when test="$h &gt; 12">
            <xsl:value-of select='concat($h - 12,":",$m," pm")' />
        </xsl:when>
        <xsl:when test="$h = 12">
            <xsl:value-of select='concat($h,":",$m," pm")' />
        </xsl:when>
        <xsl:when test="$h = 0">
            <xsl:value-of select='concat("12",":",$m," am")' />
        </xsl:when>
        <xsl:when test="12 &gt; $h &gt; 0">
            <xsl:value-of select='concat($h,":",$m," am")' />
        </xsl:when>
    </xsl:choose>
</xsl:template>
</xsl:stylesheet>

