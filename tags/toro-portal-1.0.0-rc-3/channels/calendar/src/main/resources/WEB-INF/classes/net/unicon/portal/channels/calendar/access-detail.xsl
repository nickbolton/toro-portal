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
   <xsl:output method="html" />

   <xsl:variable name="logon-user" select="calendar-system/logon/@user" />

   <xsl:param name="everyone-share-write">
      <xsl:for-each select="calendar-system/calendar">
         <xsl:choose>
            <xsl:when test="@calid=$calid ">
               <xsl:for-each select="ace">
                  <xsl:choose>
                     <xsl:when test="@cuid='@' ">
                        <xsl:value-of select="@write" />
                     </xsl:when>
                  </xsl:choose>
               </xsl:for-each>
            </xsl:when>
         </xsl:choose>
      </xsl:for-each>
   </xsl:param>

   <xsl:param name="user-share-write">
      <xsl:for-each select="calendar-system/calendar">
         <xsl:choose>
            <xsl:when test="@calid=$calid ">
               <xsl:for-each select="ace">
                  <xsl:choose>
                     <xsl:when test="@cuid=$logon-user ">
                        <xsl:value-of select="@write" />
                     </xsl:when>
                  </xsl:choose>
               </xsl:for-each>
            </xsl:when>
         </xsl:choose>
      </xsl:for-each>
   </xsl:param>

   <xsl:template name="access">
      <xsl:param name="type-link" />

      <xsl:param name="window" />

      <xsl:param name="everyone-share-write-detail">
         <xsl:for-each select="../ace">
            <xsl:choose>
               <xsl:when test="@cuid='@' ">
                  <xsl:value-of select="@write" />
               </xsl:when>
            </xsl:choose>
         </xsl:for-each>
      </xsl:param>

      <xsl:param name="user-share-write-detail">
         <xsl:for-each select="../ace">
            <xsl:choose>
               <xsl:when test="@cuid=$logon-user ">
                  <xsl:value-of select="@write" />
               </xsl:when>
            </xsl:choose>
         </xsl:for-each>
      </xsl:param>

      <xsl:variable name="right" select="key('access',../@calid)" />

      <xsl:if test="$type-link='complete'">
         <xsl:choose>
            <xsl:when test="../@owner=$logon-user or $everyone-share-write-detail='true' or $user-share-write-detail='true' or contains($right/@rights,'W')">
               <a href="{$mdoURL}=complete&amp;ceid={@ceid}&amp;calid={../@calid}&amp;complete=yes" title="">
                  <img src="{$baseImagePath}/check_12.gif" border="0" align="bottom" alt="Complete" title="Complete" />
               </a>
            </xsl:when>

            <xsl:otherwise>
               <img src="{$baseImagePath}/check_disable_12.gif" border="0" align="bottom" alt="" title="" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>

      <xsl:if test="$type-link='un-complete' ">
         <xsl:choose>
            <xsl:when test="../@owner=$logon-user or $everyone-share-write-detail='true' or $user-share-write-detail='true' or contains($right/@rights,'W') ">
               <a href="{$mdoURL}=complete&amp;ceid={@ceid}&amp;calid={../@calid}&amp;uncomplete=yes" title="">
                  <img src="{$baseImagePath}/checked_12.gif" border="0" align="bottom" alt="Uncomplete" title="Uncomplete" />
               </a>
            </xsl:when>

            <xsl:otherwise>
               <img src="{$baseImagePath}/checked_disable_12.gif" border="0" align="bottom" alt="" title="" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>

      <xsl:if test="$type-link='delete' ">
         <xsl:choose>
            <xsl:when test="../@owner=$logon-user or $everyone-share-write-detail='true' or $user-share-write-detail='true' or contains($right/@rights,'W')">
               <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />

               <a href="{$mgoURL}=Delete&amp;window={$window}&amp;ceid={@ceid}&amp;calid={../@calid}" title="Delete this {$window}" onmouseover="swapImage('calendarEventDeleteImage{@ceid}{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('calendarEventDeleteImage{@ceid}{$channelID}','channel_delete_base.gif')">
                  <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" align="absmiddle" name="calendarEventDeleteImage{@ceid}{$channelID}" id="calendarEventDeleteImage{@ceid}{$channelID}" alt="Delete this {$window}" title="Delete this {$window}" />
               </a>
            </xsl:when>
         </xsl:choose>
      </xsl:if>

      <xsl:if test="$type-link='delete-all' ">
         <xsl:choose>
            <xsl:when test="../@owner=$logon-user or $everyone-share-write-detail='true' or $user-share-write-detail='true' or contains($right/@rights,'W')">
               <img height="1" width="3" src="{$SPACER}" border="0" alt="" title="" />

               <a href="{$mgoURL}=DeleteAll&amp;window={$window}&amp;ceid={@ceid}&amp;calid={../@calid}" title="" onmouseover="swapImage('calendarEventDeleteImage{@ceid}{$channelID}','channel_delete_active.gif')" onmouseout="swapImage('calendarEventDeleteImage{@ceid}{$channelID}','channel_delete_base.gif')">
                  <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" align="absmiddle" name="calendarEventDeleteImage{@ceid}{$channelID}" id="calendarEventDeleteImage{@ceid}{$channelID}" alt="Delete this {$window}" title="Delete this {$window}" />
               </a>
            </xsl:when>
         </xsl:choose>
      </xsl:if>
   </xsl:template>
</xsl:stylesheet>

