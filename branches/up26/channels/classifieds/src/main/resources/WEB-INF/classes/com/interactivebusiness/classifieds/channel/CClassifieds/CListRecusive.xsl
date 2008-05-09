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
  3    Channels  1.2         2/5/2002 5:05:30 PM  Jing Chai       updated the
       Classified_Topic table, remove smallicon,bigicon and active coloumns. edit
       my published items to my classified status.
  2    Channels  1.1         12/20/2001 3:54:07 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 11:05:39 AMFreddy Lopez    
 $
 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href = "common.xsl"/>

<xsl:param name="baseActionURL">default</xsl:param>


<xsl:template match="channel">

<xsl:variable name="beginIndex">
  <xsl:value-of select="beginIndex"/>
</xsl:variable>

<xsl:variable name="endIndex">
  <xsl:value-of select="endIndex"/>
</xsl:variable>

  <center>
  <form action="{$baseActionURL}" method="post" >
<table border="0" cellspacing="0" cellpadding="5" width="80%"> 
<tr>
  <td colspan="2" align="center">
  <table border="0" cellspacing="0" cellpadding="5" width="100%">    
    <tr align="left" valign="middle" >
      <td class="uportal-channel-title" colspan="3">
       
      Topics > <xsl:value-of select="categoryName"/> 
      </td>
    </tr>
    
    <tr align="left" valign="middle" >
      <td  class="uportal-text" align="center" colspan="3">
        <a href="{$baseActionURL}?action=previous&amp;beginIndex={$beginIndex}">Previous</a>
         | Listing <xsl:value-of select="beginIndex"/> - <xsl:value-of select="endIndex"/> | 
        <a href="{$baseActionURL}?action=next&amp;endIndex={$endIndex}">Next</a>
      </td>               
    </tr>
    
    <tr align="left" valign="middle" >
      <td colspan="3" align="center">
        <hr width="100%"/>
      </td>
    </tr>
  </table> 
</td>
</tr>



<tr>
<td width="50%" align="center">             
  <table border="0" cellspacing="0" cellpadding="5"  > 
  <xsl:variable name="t" select="$endIndex mod 10 "/>  
    <xsl:for-each select="message[position()&lt;6]">
    <xsl:call-template name="table"/>
    </xsl:for-each>
    <xsl:if test="$t&lt;5">
      <xsl:call-template name="recursion">
        <xsl:with-param name="i" select="$t+5" />
      </xsl:call-template>
    </xsl:if>
  </table>
</td>
<td width="50%" align="center">  
  <table border="0" cellspacing="0" cellpadding="5"  > 
    <xsl:for-each select="message[position()&gt;5]" > 
      <xsl:call-template name="table"/>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="$t&lt;5"> 
        <xsl:call-template name = "recursion">
          <xsl:with-param name="i" select="5" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name = "recursion">
          <xsl:with-param name="i" select="$endIndex mod 10" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    
  </table> <br/>     
</td>
</tr>

<tr>
<td colspan="2" align="center">

  <table border="0" cellspacing="0" cellpadding="5" width="100%" > 
    <tr align="left" valign="middle" >
      <td colspan="3" align="center">
        <hr width="100%"/>
      </td>
    </tr>

    <tr align="left" valign="middle" >
      <td  class="uportal-text" align="center" colspan="3">
        <a href="{$baseActionURL}?action=previous&amp;beginIndex={$beginIndex}">Previous</a>
         | Listing <xsl:value-of select="beginIndex"/> - <xsl:value-of select="endIndex"/> | 
        <a href="{$baseActionURL}?action=next&amp;endIndex={$endIndex}">Next</a>
      </td>               
    </tr>
           
  </table>
</td>
</tr>
</table>
  </form>
  <br/>  
  </center>
</xsl:template>

<xsl:template  name="table">
  <tr align="left" valign="middle">
    <td class="uportal-text">
  <xsl:value-of select="content"/>    
      <br/><hr/>
    </td>
  </tr>
</xsl:template>   


<xsl:template name="recursion">
  <xsl:param name="i" />
  <xsl:if test="$i&lt;10">
    <tr align="left" valign="middle">
    <td class="uportal-text">    
      <xsl:text>&#x20;&#x20;&#x20;&#x20;</xsl:text><br/><hr/>
    </td>
  </tr> 
    <xsl:call-template name="recursion">
        <xsl:with-param name="i" select="$i +1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

  
</xsl:stylesheet> 
