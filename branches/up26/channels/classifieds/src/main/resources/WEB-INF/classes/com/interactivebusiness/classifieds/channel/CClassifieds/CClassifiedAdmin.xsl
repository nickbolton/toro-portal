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
  4    Channels  1.3         5/29/2002 12:16:01 PMFreddy Lopez    fixing
       classifieds, adding images for classifieds, groups, toolbar
  3    Channels  1.2         2/5/2002 6:05:18 PM  Jing Chai       updated the
       Classified_Topic table, remove smallicon,bigicon and active coloumns. edit
       my published items to my classified status.
  2    Channels  1.1         12/20/2001 4:53:59 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:37 PMFreddy Lopez    
 $
 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href = "common.xsl"/>

<xsl:param name="baseActionURL">default</xsl:param>


<xsl:template match="Main">

<form action="{$baseActionURL}?action=admin" method="post">

<table cellpadding="2" cellspacing="0" border="1" width="75%" class="uportal-background-content">
<tr>
    <td>
      <font class="uportal-channel-title">Classifieds Topic Administration</font>
    </td>
  </tr>
  <xsl:call-template name="warning"/>
<tr>
<td>
<hr size="1" width="100%"/>
</td>
</tr>
<tr>
    <td>
  
<table cellpadding="3" cellspacing="0" border="1" width="100%">
      <tr>
        <td align="left" colspan="2">
             <font class="uportal-channel-text"> Create Topic </font>
        </td>
      </tr>

            <tr>
                <td>
                  <font class="uportal-channel-text">Topic Name:</font>
                </td>
                <td>
                    <input type="text" size="20" name="newTopicName" class="uportal-input-text"/>
                    <input type="submit" name="create" value="Create New Topic" class="uportal-button"/>
                </td>
            </tr>
            <tr>
              <td>
                <font class="uportal-channel-text">Description<br/>
                (100 words max.)</font>
              </td>
              <td>
                <textarea name="description" row="2" cols="50" class="uportal-input-text" ></textarea>                     
              </td>
            </tr>
      <tr>
        <td colspan="2">
   <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
        </td>
      </tr>
      <tr>
          <td align="left" colspan="2">           
                <font class="uportal-channel-text">Edit a Topic</font>
          </td>
      </tr>
 
              <tr>
                <td> 
                   <font class="uportal-channel-text"> Select a Folder:</font>
                </td>
                <td>
                      <select name="topic" class="uportal-input-text">
                        <xsl:call-template name="topics" />
                      </select>
                </td>
              </tr>
              <tr>
                <td>
                   <font class="uportal-channel-text">New name:</font>
                </td>
                <td>
                   <input type="text" size="20" name="newname" class="uportal-input-text"/>
                   
                    <input type="submit" name="rename" value="Rename Topic" class="uportal-button"/><br/>
                     <input type="submit" name="delete" value="Delete Topic" class="uportal-button"/>


          </td>
        </tr>
      </table>
 </td>
 </tr>
 
 <tr>
 <td>
                  <input type="submit" name="finish" value="Finished" class="uportal-button"/>
    </td>
  </tr>
</table>
</form>




</xsl:template>

<xsl:template name="topics">
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
    </option>
    <xsl:value-of select="name"/>
  </xsl:for-each>
</xsl:template>


<xsl:template name="warning">
  <xsl:variable name="value"><xsl:value-of select="needWarning"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="$value='true'">
        <tr>
          <td class="uportal-channel-warning">
                <xsl:value-of select="warning"/>

          </td>
        </tr>
      </xsl:when>
      <xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
    </xsl:choose>
</xsl:template>


</xsl:stylesheet> 

