<?xml version="1.0"?>

<!--
 
 Copyright (c) 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 This software is the confidential and proprietary information of
 Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 You shall not disclose such Confidential Information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with IBS.
 
 IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 THIS SOFTWARE OR ITS DERIVATIVES.

 $Log: 
  1    Channels  1.0         2/6/2002 4:38:46 PM  Jing Chai       
 $
 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href = "common.xsl"/>

<xsl:param name="baseActionURL">default</xsl:param>


<xsl:template match="channel">

<xsl:variable name="id" select="topicid"/>
  <center>
  <form action="{$baseActionURL}" method="post" >  
  <table border="0" cellspacing="0" cellpadding="5" width="60%">    
   
    <tr align="left" valign="middle" >
      <td  class="uportal-channel-title" colspan="2">
        Status
      </td>
      <td  align="right" class="uportal-text">
        <a href="{$baseActionURL}?action=main">Back to Topics</a>
      </td>
    </tr>   

    <tr align="left" valign="middle">
      <td align="center" colspan="3">
        <hr  />
      </td>
    </tr>
               

     
    <xsl:call-template name="empty"/>        


    
    <tr align="left" valign="middle">
      <td align="center" colspan="3">
        <hr  />
      </td>
    </tr>
  </table>
  </form>
  <br/>  
  </center>
</xsl:template>

<xsl:template name ="empty">
  <xsl:variable name="isempty"><xsl:value-of select="isempty"/></xsl:variable>
  <xsl:choose>
    <xsl:when test="$isempty='true'">
      <tr >
        <td colspan='3' align="center" class="uportal-channel-warning">
          <xsl:value-of select="emptyMessage"/>
        </td>
      </tr>
    </xsl:when>
    <xsl:otherwise><xsl:call-template name="statusinfo"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="statusinfo">
    <xsl:for-each select="status">
    <xsl:variable name="actionvalue"><xsl:value-of select="action"/></xsl:variable>
    <tr align="center" valign="middle" >
      <td align="right" class="uportal-text">
        <xsl:text></xsl:text>
      </td>
  
      <td align="left" class="uportal-text">
        <a href="{$baseActionURL}?action={$actionvalue}">
        <xsl:value-of select="name"/> Items
        </a>
      </td>     
      <td  class="uportal-text">
        <xsl:value-of select="total"/> entries
      </td>
    </tr>          
    </xsl:for-each> 
</xsl:template>
 
</xsl:stylesheet> 

