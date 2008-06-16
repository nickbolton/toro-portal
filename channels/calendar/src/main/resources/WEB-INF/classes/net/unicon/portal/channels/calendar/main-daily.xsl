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
  <xsl:include href="detail-daily.xsl"/>
  <xsl:include href="priority.xsl"/>
  <xsl:include href="access-detail.xsl"/>
  <xsl:include href="navigation-bar.xsl"/>
  <xsl:include href="mini-event.xsl"/>
  <xsl:include href="mini-todo.xsl"/>
  <xsl:include href="date-widget.xsl" />
  <!--variable/params-->
  <xsl:param name="window">event</xsl:param>
  <xsl:param name="main">monthly</xsl:param>
  <xsl:key name="access" match="/calendar-system/logon/access" use="@calid"/>
  <xsl:variable name="detail-daily" select="/calendar-system/detail-daily"/>
 <xsl:variable name="mini-daily" select="/calendar-system/mini-daily"/>


  <xsl:template match="/">
	<xsl:call-template name="links"/>
    <xsl:call-template name="daily-header" />	
	
	<!--UniAcc:Layout Table -->
    <table cellspacing="2" border="0" cellpadding="0" width='100%'>
      <tr>
         <xsl:choose>
           <xsl:when test="contains($targetChannel, 'CCalendarUnicon')">
              	<td valign="top" width='160'>
					<xsl:call-template name="tiny-monthly"/>
				</td>
		        <!-- Main weekly -->
				<td valign="top">
		          	<xsl:call-template name="detail-daily"/>
				</td>
           </xsl:when>
            <xsl:otherwise>
                 <td width="160" valign="top">
                 	<xsl:call-template name="tiny-monthly"/>
                 </td>
                 <td>
                   <xsl:call-template name="detail-daily"/>
                </td>
            </xsl:otherwise>
         </xsl:choose>
      </tr>

    </table>
  </xsl:template>
</xsl:stylesheet>
