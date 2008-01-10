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
    <xsl:output method="html" />
    <xsl:include href="common.xsl" />
<!-- //////////////////////////////////////////////////////// -->
    <xsl:param name="name" />
    <xsl:param name="id" />
    <xsl:template match="calendar-system">
<!-- Title of window -->
        <xsl:variable name="title">
            <xsl:if test="$id">Rename Composite View</xsl:if>
            <xsl:if test="not($id)">New Composite View</xsl:if>
        </xsl:variable>
<!-- Form  -->
<!--
    <form method="post" action="{$baseActionURL}?focusedChannel={$focusedChannel}">
    -->
        <form method="post" action="{$baseActionURL}">
            <input type="hidden" name="sid" value="{$sid}" />
            <input type="hidden" name="id" value="{$id}" />
            <xsl:call-template name="bform" />
            <input type="hidden" name="default" value="do~ok" />
            <!--UniAcc: Layout Table -->
            <table border="0" width="100%" cellpadding="0" cellspacing="0">
                <tr>
                    <th class="th" colspan="2">
                        <xsl:value-of select="$title" />
                    </th>
                </tr>
<!-- Text field Description -->
                <tr>
                    <td class="table-light-left" width="1%" nowrap="nowrap">
                    	<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                    	<label for="CGE-NameT1">Name</label>
                    	<img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />
                    </td>
                    <td class="table-content-right">
                        <input class="text" type="text" name="groupname" value="{$name}" size="37" id="CGE-NameT1"/>
                    </td>
                </tr>
<!-- Nav -->
                <tr>
                    <td class="table-nav" colspan="2">
                        <input class="uportal-button" type="hidden" name="do~update" value="OK" />
                        <input class="uportal-button" type="submit" name="do~update" value="OK" />
                        <input class="uportal-button" type="submit" name="go~Preference" value="Cancel" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>

