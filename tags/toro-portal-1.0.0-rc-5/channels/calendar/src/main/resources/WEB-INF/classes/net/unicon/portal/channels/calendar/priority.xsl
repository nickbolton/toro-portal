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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
<!-- Context node is calendar/entry -->
    <xsl:template name="priority">
        <xsl:param name="cur_node" />
        <xsl:variable name="p">
            <xsl:choose>
                <xsl:when test="$cur_node='event' or $cur_node='todo'">
                    <xsl:value-of select="@priority" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="todo/@priority | event/@priority" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$p='0' or $p='1' or $p='2'">
                <img align="absmiddle" src="{$baseImagePath}/unicon_priority_vlow.gif" border="0" alt="Priority level:Very Low" title="Priority level:Very Low" /><img border="0" src="{$SPACER}" width="3" alt="" title="" />
            </xsl:when>
            <xsl:when test="$p='3' or $p='4'">
                <img align="absmiddle" src="{$baseImagePath}/unicon_priority_low.gif" border="0" alt="Priority level:Low" title="Priority level:Low" /><img border="0" src="{$SPACER}" width="3" alt="" title="" />
            </xsl:when>
            <xsl:when test="$p='5' or $p='6'">
                <img align="absmiddle" src="{$baseImagePath}/unicon_priority_normal.gif" border="0" alt="Priority level:Normal" title="Priority level:Normal" /><img border="0" src="{$SPACER}" width="3" alt="" title="" />
            </xsl:when>
            <xsl:when test="$p='7' or $p='8'">
                <img align="absmiddle" src="{$baseImagePath}/unicon_priority_high.gif" border="0" alt="Priority level:High" title="Priority level:High" /><img border="0" src="{$SPACER}" width="3" alt="" title="" />
            </xsl:when>
            <xsl:otherwise>
                <img align="absmiddle" src="{$baseImagePath}/unicon_priority_vhigh.gif" border="0" alt="Priority level:Very High" title="Priority level:Very High" /><img border="0" src="{$SPACER}" width="3" alt="" title="" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

