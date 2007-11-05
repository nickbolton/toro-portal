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
    <xsl:param name="goURL"/>
    <xsl:param name="doURL"/>
    <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>
    <!--////////////////////////////////////////////// -->
    <xsl:param name="groupsOnly">false</xsl:param>
    <xsl:param name="openGroup">0</xsl:param>
    <xsl:param name="nPages">10</xsl:param>
    <xsl:param name="curPage">2</xsl:param>
    <!--////////////////////////////////////////////// -->
    <xsl:key name="expand" match="expanded" use="."/>
    <!--////////////////////////////////////////////// -->

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="addressbook-system">
        <!-- Caption -->
        <table border="0" cellPadding="0" cellSpacing="0" width="100%">
            <tr>
                <td class="uportal-channel-strong" noWrap="nowrap" width="99%" height="20">Portal</td>
                <td class="uportal-background-light">
                    <img border="0" src="{$baseImagePath}/rad/transparent.gif" width="2" alt="" title=""/>
                </td>
                <td class="uportal-channel-strong" noWrap="nowrap">
                    <a class="uportal-navigation-channel" href="{$goURL}=Personal&amp;where=Portal&amp;uP_root=me">&#160;Personal&#160;</a>
                </td>
                <td class="uportal-background-light">
                    <img border="0" src="{$baseImagePath}/rad/transparent.gif" width="2" alt="" title=""/>
                </td>
                <td class="uportal-channel-strong" noWrap="nowrap">
                    <a class="uportal-navigation-channel" href="{$goURL}=Search&amp;uP_root=me&amp;where=Portal">&#160;Search</a>
                </td>
            </tr>
            <tr>
                <td colspan="5" class="uportal-background-light" width="100%">
                    <img border="0" height="2" src="{$baseImagePath}/rad/transparent.gif" alt="" title=""/>
                </td>
            </tr>
        </table>
        <!-- Body -->
        <table border="0" cellPadding="0" cellSpacing="0">
            <tr>
                <td>
                    <img border="0" height="6" src="{$baseImagePath}/rad/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img border="0" class="uportal-background-light" height="6" src="{$baseImagePath}/rad/transparent.gif" width="2" alt="" title=""/>
                </td>
                <td>
                    <img border="0" height="6" src="{$baseImagePath}/rad/transparent.gif" alt="" title=""/>
                </td>
            </tr>
            <tr>
                <!-- Edit Left Start-->
                <td vAlign="top">
                    <table border="0" cellpadding="0" cellspacing="0">
                        <xsl:apply-templates select="group">
                            <xsl:with-param name="prefix"/>
                        </xsl:apply-templates>
                    </table>
                </td>
                <!-- Edit Left End-->
                <td class="uportal-background-light">
                    <img border="0" src="{$baseImagePath}/rad/transparent.gif" width="2" alt="" title=""/>
                </td>
                <!-- Edit Right Start-->
                <td vAlign="top" width="99%">
                    <table border="0" cellPadding="0" cellSpacing="0">
                        <!-- groups -->
                        <!--<xsl:for-each select="groups/group">
                    <tr>
                      <td width="1%">&#160;<img src="{$baseImagePath}/rad/folder_closed_16.gif" border="0" alt="" title=""/></td>
                      <td width="99%" class="uportal-channel-text" nowrap="nowrap">&#160;<xsl:value-of select="@iname"/></td>
                    </tr>
                  </xsl:for-each>-->
                        <!-- entities -->
                        <xsl:for-each select="entities/entity">
                            <tr>
                                <td width="1%">&#160;<img src="{$baseImagePath}/rad/person_16.gif" border="0" alt="" title=""/></td>
                                <td width="99%" class="uportal-channel-text" nowrap="nowrap">
                                    <xsl:text>&#160;</xsl:text>
                                    <a href="{$doURL}=copyContact&amp;uP_root=me&amp;contactid={@iid}">
                                        <xsl:value-of select="@iname"/>
                                        <xsl:text>&#160;</xsl:text>
                                        <img src="{$baseImagePath}/rad/copy_12.gif" border="0"   alt="Copy Contact" title="Copy Contact"/>
                                    </a>
                                </td>
                            </tr>
                            <!-- added information -->
                            <!--<tr>
                  <td colspan="2" nowrap="nowrap" class="uportal-channel-text">&#160;
                    <img border="0" width="16" src="{$baseImagePath}/rad/transparent.gif" alt="" title=""/>&#160;
                    <xsl:value-of select="home-phone"/>
                    </td>
                </tr>-->
                        </xsl:for-each>
                    </table>
                </td>
                <!-- Edit Right End-->
            </tr>
            <tr>
                <td>
                    <img border="0" height="6" src="{$baseImagePath}/rad/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img border="0" class="uportal-background-light" height="6" src="{$baseImagePath}/rad/transparent.gif" width="2" alt="" title=""/>
                </td>
                <td>
                    <img border="0" height="6" src="{$baseImagePath}/rad/transparent.gif" alt="" title=""/>
                </td>
            </tr>
        </table>
        <!-- Tool -->
        <table border="0" cellPadding="0" cellSpacing="0" width="100%">

            <tr class="uportal-channel-text">
                <!-- Process Number -->
                <td class="uportal-background-light" height="22" colspan="3" align="right" width="100%">
                    <xsl:if test="number($nPages) &gt; 0">
                        <!-- Previous -->
                        <xsl:if test="number($curPage) &gt; 0">
                            <a href="{$doURL}=prev&amp;uP_root=me">
                                <img align="absmiddle" border="0" src="{$baseImagePath}/rad/prev_12.gif"   alt="Previous" title="Previous"/>
                            </a>
                            <!--<input type="image" align="absmiddle" name="do~prev" border="0" src="{$baseImagePath}/rad/prev_12.gif" title='Previous'/>-->
                        </xsl:if>
                        <xsl:if test="number($curPage) &lt; 1">
                            <img src="{$baseImagePath}/rad/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/>
                        </xsl:if>
                        <!-- curPage/nPages -->
                        <xsl:text>&#160;</xsl:text>
                        <xsl:value-of select="number($curPage)+1"/>/<xsl:value-of select="$nPages"/><xsl:text>&#160;</xsl:text>
                        <!-- Next -->
                        <xsl:if test="number($curPage) &lt; number($nPages) - 1">
                            <a href="{$doURL}=next&amp;uP_root=me">
                                <img align="absmiddle" border="0" src="{$baseImagePath}/rad/next_12.gif"   alt="Next" title="Next"/>
                            </a>
                            <!--<input type="image" align="absmiddle" border="0" name="do~next" src="{$baseImagePath}/rad/next_12.gif" title='Next'/>-->
                        </xsl:if>
                        <xsl:if test="number($curPage) &gt; number($nPages) - 2">
                            <img src="{$baseImagePath}/rad/next_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/>
                        </xsl:if>
                        <img src="{$baseImagePath}/rad/transparent.gif" border="0" width="1" alt="" title=""/>
                    </xsl:if>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- Content of recursive function -->
    <!-- ///////////////////////////////////////// -->
    <!-- Template for group  -->
    <xsl:template match="group">
        <xsl:param name="prefix"/>
        <!-- ///////////////////////////////////////// -->
        <xsl:variable name="expand" select="key('expand',@iid)"/>
        <tr>
            <td nowrap="true" style="border-style: solid; border-width: 0">
                <!-- Prefix: + | - | empty -->
                <xsl:copy-of select="$prefix"/>
                <xsl:if test="@groups='0'">
                    <img src="{$baseImagePath}/rad/tree_empty_16.gif" border="0" align="absmiddle" alt="" title=""/>
                </xsl:if>
                <xsl:if test="@groups!='0'">
                    <xsl:if test="$expand">
                        <a href="{$doURL}=collapse&amp;key={@iid}">
                            <img src="{$baseImagePath}/rad/tree_minus_16.gif" align="absmiddle" border="0" alt="" title=""/>
                        </a>
                    </xsl:if>
                    <xsl:if test="not($expand)">
                        <a href="{$doURL}=expand&amp;key={@iid}">
                            <img src="{$baseImagePath}/rad/tree_plus_16.gif" align="absmiddle" border="0" alt="" title=""/>
                        </a>
                    </xsl:if>
                </xsl:if>
                <xsl:text>&#160;</xsl:text>
                <!-- Display name of group -->
                <xsl:choose>
                    <xsl:when test="@iid=$openGroup">
                        <img src="{$baseImagePath}/rad/folder_open_16.gif" align="absmiddle" border="0" alt="" title=""/>
                        <xsl:text>&#160;</xsl:text>
                        <a class="uportal-navigation-channel">
                            <xsl:value-of select="@iname"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a class="uportal-navigation-channel" href="{$doURL}=open&amp;key={@iid}">
                            <img src="{$baseImagePath}/rad/folder_closed_16.gif" align="absmiddle" border="0" alt="" title=""/>
                            <xsl:text>&#160;</xsl:text>
                            <xsl:value-of select="@iname"/>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>&#160;</xsl:text>
            </td>
        </tr>
        <!-- Children -->
        <xsl:if test="$expand">
            <xsl:apply-templates select="group">
                <xsl:with-param name="prefix">
                    <xsl:copy-of select="$prefix"/>
                    <img src="{$baseImagePath}/rad/tree_space_16.gif" align="absmiddle" border="0" alt="" title=""/>
                </xsl:with-param>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    <!-- ///////////////////////////////////////// -->
</xsl:stylesheet><!-- Stylus Studio meta-information - (c)1998-2003 Copyright Sonic Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperInfo srcSchemaPath="" srcSchemaRoot="" srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
</metaInformation>
-->
