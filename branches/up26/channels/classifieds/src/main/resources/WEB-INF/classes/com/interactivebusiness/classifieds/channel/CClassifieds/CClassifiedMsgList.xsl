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
  3    Channels  1.2         2/7/2002 4:58:23 PM  Jing Chai       
  2    Channels  1.1         12/20/2001 3:54:02 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 11:05:38 AMFreddy Lopez    
 $
 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href = "common.xsl"/>

<xsl:param name="baseActionURL">default</xsl:param>


<xsl:template match="channel">
  <center>
  <form action="{$baseActionURL}?action=hasMsg" method="post">  

<table cellpadding="0" cellspacing="2" border="0" width="80%">
  <tr>
          <td valign="bottom" align="right" class="uportal-text">
              <a href="{$baseActionURL}?action=main"> Back to Topics</a>

    </td>
  </tr>
  <tr class="uportal-background-dark" >
    <td>
      <table cellpadding="2" cellspacing="0" border="0" width="100%">
        <tr>
          <td align="left"><xsl:text></xsl:text>
          </td>
          <td align="right">
            <select name="topics">
              <xsl:call-template name="topicList" />
            </select>
            <input type="submit" name="view" value="View" class="uportal-button"/>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td width="100%">
      <table cellpadding="2" cellspacing="0" border="0" width="100%">
        <tr class="uportal-background-med">

          <td class="uportal-channel-table-header">
                &#x20;Message from Approver
          </td>
          <td class="uportal-channel-table-header">
            
                &#x20;Content

          </td>
          <td class="uportal-channel-table-header" >
          
                &#x20;Topic              

          </td>
          <td class="uportal-channel-table-header" >

<!--                <a href="{$baseActionURL}?action=sortByExpiredDate">
                  <xsl:call-template name="sort"/>
                </a>        -->
                Expire Date

          </td>
        </tr>
        
        <xsl:call-template name="empty"/>
        

      </table>
    </td>
  </tr>
  <tr class="uportal-background-dark" >
    <td>
    <br/>
    </td>
  </tr>

</table>
</form>
</center>

</xsl:template>

<xsl:template name="topicList">
  <xsl:for-each select="topic">
  <xsl:variable name="value"><xsl:value-of select="select"/></xsl:variable>
    <option class="uportal-text">
      <xsl:attribute name="value"><xsl:value-of select="id"/></xsl:attribute>
        <xsl:choose>
        <xsl:when test="$value='true'">
          <xsl:attribute name="selected"><xsl:value-of select="value"/></xsl:attribute>
        </xsl:when>
        <xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
        </xsl:choose>
      <xsl:value-of select="name"/>
    </option>
  </xsl:for-each>
</xsl:template>


<xsl:template name ="empty">
  <xsl:variable name="isempty"><xsl:value-of select="isempty"/></xsl:variable>
  <xsl:choose>
    <xsl:when test="$isempty='true'">
      <tr class="uportal-background-content">
        <td colspan='4' align="center" class="uportal-channel-warning">
          <xsl:value-of select="emptyTopic"/>
        </td>
      </tr>
    </xsl:when>
    <xsl:otherwise><xsl:call-template name="iteminfo"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
   
    
<xsl:template name="iteminfo">    
  <xsl:for-each select="item">
    <xsl:variable name="i"><xsl:value-of select="position()"/></xsl:variable>
    <xsl:variable name="cssclass">
    <xsl:choose>
      <xsl:when test="position() mod 2=0">
        <xsl:text></xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>uportal-background-light</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:variable>
    <tr class="{$cssclass}">     
      <td class="uportal-text"><xsl:value-of select="msgToAuth"/></td>
      <td class="uportal-text"><xsl:value-of select="content"/></td>
      <td class="uportal-text" >
        <xsl:value-of select="topicname"/>
      </td>
      <td class="uportal-text" >
        <xsl:value-of select="expireDate"/>
      </td>
    </tr>
        
  </xsl:for-each>    
</xsl:template>

<xsl:template name="sort">
  <xsl:variable name="sortorder"><xsl:value-of select="sortorder"/></xsl:variable>
  <xsl:choose>
    <xsl:when test="$sortorder='increase'">
      <img src="{$imagedir}/up.gif" width="13" height="13" border="0" alt="" title=""/>
    </xsl:when>
    <xsl:otherwise><img src="{$imagedir}/down.gif" width="13" height="13" border="0" alt="" title=""/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
    
</xsl:stylesheet> 
