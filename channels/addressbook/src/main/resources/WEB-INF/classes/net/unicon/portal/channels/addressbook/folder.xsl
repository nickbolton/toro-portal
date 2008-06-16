<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:include href="common.xsl"/>
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="sid">default</xsl:param>
    <xsl:param name="goURL"/>
    <xsl:param name="doURL"/>
    <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>
    <!--////////////////////////////////////////////////////////////////////-->
    <!--<xsl:param name="isNew">false</xsl:param> -->
    <!--////////////////////////////////////////////////////////////////////-->



    <xsl:template match="/">
        <form method="post" action="{$baseActionURL}" name="addressBookForm" id="addressBookForm">
            <xsl:call-template name="links"/>
        </form>

        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="addressbook-system">
        <xsl:variable name="folder-name" select="folder"/>
        <form method="post" action="{$baseActionURL}">
            <!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <td colspan="2">
                    	<div class="page-title">
							<xsl:choose>
								<xsl:when test="$isNew='true'">Add Group</xsl:when>
								<xsl:otherwise>Edit Group</xsl:otherwise>
							</xsl:choose>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">
                        <label for="addressbook-GroupNameT1">Group Name</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input class="text" type="text" name="foldername" value="{$folder-name}" size="35" id="addressbook-GroupNameT1"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" style="text-align:center">
                        <input type="hidden" name="sid" value="{$sid}"/>
                        <input type="hidden" name="default" value="do~ok"/>
                        <input type="submit" class="uportal-button" value="OK" name="do~ok" title="To submit this information and return to the view of the address book"/>
                        <input type="submit" class="uportal-button" value="Cancel" name="do~cancel" title="To cancel this and return to the view of the address book"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
