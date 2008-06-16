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
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="sid">default</xsl:param>
  <xsl:param name="goURL">default</xsl:param>
  <xsl:param name="doURL">default</xsl:param>
  <xsl:param name="baseImagePath">media/net/unicon/portal/channels/rad</xsl:param>
  <!--////////////////////////////////////////////// -->
  <xsl:param name="groupsOnly">false</xsl:param>
  <xsl:param name="openGroup">0</xsl:param>
  <xsl:param name="nPages">10</xsl:param>
  <xsl:param name="curPage">2</xsl:param>
  <!--////////////////////////////////////////////// -->
  <xsl:key name="expand" match="expanded" use="."/>
  <xsl:key name="gselect" match='selected[@itype="G"]' use="@iid"/>
  <xsl:key name="eselect" match='selected[@itype="E"]' use="@iid"/>
  <!--////////////////////////////////////////////// -->
  <!-- Root template  -->
  <xsl:template match="groups-system">
    <form method="post" action="{$baseActionURL}">
      <table border="0" cellPadding="0" cellSpacing="0" width="100%" class="uportal-background-content">
        <tr>
          <td height="20" nowrap="nowrap" class="uportal-channel-strong">Select</td>
        </tr>
        <tr>
          <td class="uportal-background-light">
            <img border="0" height="2" src="{$baseImagePath}/transparent.gif"/>
          </td>
        </tr>
        <tr>
          <td width="100%">
            <table border="0" cellPadding="0" cellSpacing="0" width="100%">
              <xsl:if test="$groupsOnly='true'">
                <xsl:call-template name="groups"/>
              </xsl:if>
              <xsl:if test="$groupsOnly!='true'">
                <xsl:call-template name="all"/>
              </xsl:if>
            </table>
          </td>
        </tr>
      </table>
    </form>
  </xsl:template>
  <!-- ///////////////////////////////////////// -->
  <!-- Template for groups option ( groups only)  -->
  <xsl:template name="groups">
    <tr>
      <td>
        <img src="{$baseImagePath}/transparent.gif" border="0" height="6"/>
      </td>
    </tr>
    <!--///////////////////////////////////////////// -->
    <!-- Start content here -->
    <tr>
      <td valign="top">
        <table border="0" width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse">
          <xsl:apply-templates select="group">
            <xsl:with-param name="prefix"/>
          </xsl:apply-templates>
        </table>
      </td>
    </tr>
    <!-- End content here -->
    <!--///////////////////////////////////////////// -->
    <tr>
      <td>
        <img src="{$baseImagePath}/transparent.gif" border="0" height="6"/>
      </td>
    </tr>
    <tr>
      <td colspan="3" class="uportal-background-light" height="22">
        <img src="{$baseImagePath}/transparent.gif" border="0" width="1"/>
        <input type="hidden" name="sid" value="{$sid}"/>
        <input class="uportal-button" type="submit" name="do~ok" value="OK"/>
        <input class="uportal-button" type="submit" name="do~cancel" value="Cancel"/>
      </td>
    </tr>
  </xsl:template>
  <!-- ///////////////////////////////////////// -->
  <!-- Template for all option ( groups + entites)  -->
  <xsl:template name="all">
    <tr>
      <td>
        <img border="0" height="6" src="{$baseImagePath}/transparent.gif"/>
      </td>
      <td class="uportal-background-light">
        <img border="0" height="6" src="{$baseImagePath}/transparent.gif" width="2"/>
      </td>
      <td>
        <img border="0" height="6" src="{$baseImagePath}/transparent.gif"/>
      </td>
    </tr>
    <!--///////////////////////////////////////////// -->
    <!-- Start content here -->
    <tr>
      <!-- Groups -->
      <td valign="top">
        <table border="0" width="100%" cellpadding="0" cellspacing="0" style="border-collapse: collapse">
          <xsl:apply-templates select="group">
            <xsl:with-param name="prefix"/>
          </xsl:apply-templates>
        </table>
      </td>
      <td class="uportal-background-light">
        <img border="0" src="{$baseImagePath}/transparent.gif" width="2"/>
      </td>
      <!-- Entities -->
      <td valign="top" width="99%">
        <table border="0" cellPadding="0" cellSpacing="0" width="100%">
          <xsl:apply-templates select="entities/entity[../@group=$openGroup]"/>
        </table>
      </td>
      <!-- Edit Right End-->
    </tr>
    <!-- End content here -->
    <!--///////////////////////////////////////////// -->
    <tr>
      <td>
        <img border="0" height="6" src="{$baseImagePath}/transparent.gif"/>
      </td>
      <td>
        <img border="0" class="uportal-background-light" height="6" src="{$baseImagePath}/transparent.gif" width="2"/>
      </td>
      <td>
        <img border="0" height="6" src="{$baseImagePath}/transparent.gif"/>
      </td>
    </tr>
    <tr>
      <td class="uportal-background-light" height="22" colspan="3" width="100%">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <tr class="uportal-channel-text">
            <td width="99%">
              <img src="{$baseImagePath}/transparent.gif" border="0" width="1"/>
              <input type="hidden" name="sid" value="{$sid}"/>
              <input class="uportal-button" type="submit" name="do~ok" value="OK"/>
              <input class="uportal-button" type="submit" name="do~cancel" value="Cancel"/>
            </td>
            <td width="1%" align="right" nowrap="nowrap">
              <xsl:if test="number($nPages) &gt; 0">
                <!-- Previous -->
                <xsl:if test="number($curPage) &gt; 0">
                  <input type="image" align="absmiddle" name="do~prev" border="0" src="{$baseImagePath}/prev_12.gif" title='Previous'/>
                </xsl:if>
                <xsl:if test="number($curPage) &lt; 1">
                  <img src='{$baseImagePath}/prev_disabled_12.gif' border='0' align='absmiddle'/>
                </xsl:if>
                <!-- curPage/nPages -->
                &#160;<xsl:value-of select="number($curPage)+1"/>/<xsl:value-of select="$nPages"/>&#160;
                <!-- Next -->
                <xsl:if test="number($curPage) &lt; number($nPages) - 1">
                  <input type="image" align="absmiddle" border="0" name="do~next" src="{$baseImagePath}/next_12.gif" title='Next'/>
                </xsl:if>
                <xsl:if test="number($curPage) &gt; number($nPages) - 2">
                  <img src='{$baseImagePath}/next_disabled_12.gif' border='0' align='absmiddle'/>
                </xsl:if>
                <img src='{$baseImagePath}/transparent.gif' border='0' width="1"/>
		    </xsl:if>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>
  <!-- ///////////////////////////////////////// -->
  <!-- Template for group  -->
  <xsl:template match="group">
    <xsl:param name="prefix"/>
    <!-- ///////////////////////////////////////// -->
    <xsl:variable name="expand" select="key('expand',@iid)"/>
    <xsl:variable name="displayName">
      <xsl:if test="@iname and @iname != ''"><xsl:value-of select="@iname"/></xsl:if>
      <xsl:if test="not(@iname) or @iname = ''"><xsl:value-of select="@iid"/></xsl:if>
    </xsl:variable>
    <tr class="uportal-channel-text">
      <td nowrap="true" style="border-style: solid; border-width: 0">
        <!-- Prefix: + | - | empty -->
        <xsl:copy-of select="$prefix"/>
        <xsl:if test="@groups='0'">
          <img src="{$baseImagePath}/tree_empty_16.gif" border="0" align="absmiddle"/>
        </xsl:if>
        <xsl:if test="@groups!='0'">
          <xsl:if test="$expand">
            <input type="image" name="do~collapse&amp;key={@iid}" src="{$baseImagePath}/tree_minus_16.gif" align="absmiddle" title='Collapse'/>
          </xsl:if>
          <xsl:if test="not($expand)">
            <input type="image" name="do~expand&amp;key={@iid}" src="{$baseImagePath}/tree_plus_16.gif" align="absmiddle" title='Expand'/>
          </xsl:if>
        </xsl:if>
        <xsl:text>&#160;</xsl:text>
        <!-- Display name of group -->
        <xsl:choose>
          <xsl:when test="@iid=$openGroup">
		<img src="{$baseImagePath}/folder_open_16.gif" border="0" align="absmiddle"/>
		<xsl:text>&#160;</xsl:text>
      	<a class="uportal-navigation-channel">
            <xsl:value-of select="$displayName"/>
		</a>
          </xsl:when>
          <xsl:otherwise>
            <input type="image" name="do~open&amp;key={@iid}" src="{$baseImagePath}/folder_closed_16.gif" align="absmiddle" title='Open'/>
            <xsl:text>&#160;</xsl:text>
            <xsl:value-of select="$displayName"/>
          </xsl:otherwise>
        </xsl:choose>
        <!-- Selected state -->
        <input type="checkbox" name="group{@iid}" value="{$displayName}">
          <xsl:if test="key('gselect', @iid)">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
        </input>
      </td>
    </tr>
    <!-- Children -->
    <xsl:if test="$expand">
      <xsl:apply-templates select="group">
        <xsl:with-param name="prefix">
          <xsl:copy-of select="$prefix"/>
          <img src="{$baseImagePath}/tree_space_16.gif" align="absmiddle" border="0"/>
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  <!-- ///////////////////////////////////////// -->
  <!-- Template for entity  -->
  <xsl:template match="entity">
    <xsl:variable name="displayName">
      <xsl:if test="@iname and @iname != ''"><xsl:value-of select="@iname"/></xsl:if>
      <xsl:if test="not(@iname) or @iname = ''">
        <xsl:if test="@ialias and @ialias != ''"><xsl:value-of select="@ialias"/></xsl:if>
        <xsl:if test="not(@ialias) or @ialias = ''"><xsl:value-of select="@iid"/></xsl:if>
      </xsl:if>
    </xsl:variable>
    <tr>
      <td colspan="2" class="uportal-channel-text" nowrap="nowrap">
        <img src="{$baseImagePath}/person_16.gif" border="0"/>&#160;<xsl:value-of select="$displayName"/>
        <xsl:text>&#160;</xsl:text>
        <input type="checkbox" name="entity{@iid}" value="{$displayName}">
          <xsl:if test="key('eselect',@iid)">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
        </input>&#160;
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
