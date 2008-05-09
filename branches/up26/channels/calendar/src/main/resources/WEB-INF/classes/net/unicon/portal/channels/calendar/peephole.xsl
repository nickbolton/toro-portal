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
  <xsl:output method="html"/>
  
  <xsl:include href="common.xsl"/>
  <xsl:include href="peephole-calendar.xsl"/>
  <xsl:include href="peephole-events.xsl"/>
  <xsl:include href="peephole-todos.xsl"/>
  <xsl:include href="priority.xsl"/>
  <xsl:include href="utils.xsl"/>
  <xsl:include href="date-widget.xsl" />
  <!-- //// RAD.xsl ///////////////////////////////////////// -->
  <xsl:param name="back">default</xsl:param>
  <!-- variable/params-->
  <xsl:param name="view">Calendar</xsl:param>
  <xsl:variable name="logon-user" select="calendar-system/logon/@user"/>
  <xsl:variable name="mini-daily" select="/calendar-system/mini-daily"/>
  <xsl:variable name="calid" select="/calendar-system/view/calendar/@calid"/>
  <xsl:key name="access" match="/calendar-system/logon/access" use="@calid"/>

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="calendar-system/error">
      	<!--UniAcc: Layout Table -->
        <table width="200" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td valign="absmiddle" class="uportal-channel-text">
				<img src="{$baseImagePath}/rad/error_32.gif" border="0" align="absmiddle" alt="Error!" title="Error!"/>
				&#160;&#160;&#160;<xsl:value-of select="calendar-system/error"/>
 			</td> 
 			</tr>
        </table>
      </xsl:when>
      <xsl:otherwise>
      
        <xsl:choose>
          <xsl:when test="$view = 'Calendar'">
            <xsl:call-template name="Calendar"/>
          </xsl:when>
          <xsl:when test="$view = 'Events'">
            <xsl:call-template name="Events"/>
          </xsl:when>
          <xsl:when test="$view = 'Todos'">
            <xsl:call-template name="Todos"/>
          </xsl:when>
        </xsl:choose>
        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
