<?xml version="1.0" encoding="UTF-8"?>
<!--     Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.     This software is the confidential and proprietary information of    Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not    disclose such Confidential Information and shall use it only in    accordance with the terms of the license agreement you entered into    with IBS-DP or its authorized distributors.     IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY    OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT    LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A    PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE    FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING    OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:import href="lib.xsl"/>
  <xsl:include href="common.xsl"/>
  <xsl:output method="html"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="sid">default</xsl:param>
  <xsl:param name="goURL"/>
  <xsl:param name="doURL"/>
  <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>
<!--/////////////////////////////////////////////////////////////////////////////-->
  <xsl:param name="nPages">10</xsl:param>
  <xsl:param name="curPage">2</xsl:param>
  <xsl:param name="start">0</xsl:param>
  <xsl:param name="end">0</xsl:param>
  <xsl:param name="total">0</xsl:param>
  <xsl:param name="maxResult">50</xsl:param>
  <xsl:param name="name"/>
  <xsl:param name="title"/>
  <xsl:param name="department"/>
  <xsl:param name="email"/>
  <xsl:param name="is-personal-check"/>
  <xsl:param name="is-portal-check"/>
  <xsl:param name="is-campus-check"/>
  <xsl:template match="/">
    <form method="post" action="{$baseActionURL}" name="addressBookForm" id="addressBookForm">
      <xsl:call-template name="links"/>
      <div class="page-title">Search Address Book</div>
    </form>
    <xsl:apply-templates/>
  </xsl:template>
<!--////////////////////////////////////////////////// -->
  <xsl:template match="addressbook-system">
    <script language="JavaScript1.2" type="text/javascript">
        // This script prevents an open search by checking to see if the input is empty. It is called from the onsubmit of the form below.
        OpenSearchCheck = function() {
            if (window.validateForm &amp;&amp; document.getElementById("addressbook-Search").name.value == "" &amp;&amp; document.getElementById("addressbook-Search").title.value == "" &amp;&amp; document.getElementById("addressbook-Search").department.value == "" &amp;&amp; document.getElementById("addressbook-Search").email.value == "") {
                alert("The search fields are empty; you must provide search criteria");
                return false;
            } else {
                return true;
            }
        }
        // Every below was added to support the copying of functions from one window to
        // another so that client-side optimizations would work
        AddressbookFunctions = ["initializeAddressbook", "OpenSearchCheck"];
        // if channelFunctionsArray is already defined, just add to it
        if (window.channelFunctionsArray)
        {
            channelFunctionsArray[channelFunctionsArray.length] = AddressbookFunctions;
        }
        // else create channelFunctionsArray with this as first entry
        else
        {
            channelFunctionsArray = [AddressbookFunctions]; // create 2-D array
        }
        // create initialize method so that it can be initialized outside of page load
        initializeAddressbook = function()
        {
        }
    </script>
    <form method="post" action="{$baseActionURL}" name="addressbook-Search" id="addressbook-Search" onsubmit="return OpenSearchCheck();">
      <input type="hidden" name="sid" value="{$sid}"/>
      <input type="hidden" name="Personal" value="Personal"/>
      <input type="hidden" name="Portal" value="Portal"/>
      <input type="hidden" name="Campus" value="Campus"/>
      <table border="0" cellPadding="0" cellSpacing="0" width="100%">
        <tr>
<!-- Left: Search criteria -->
          <td width="1%" valign="top">
            <xsl:call-template name="criteria"/>
          </td>
<!-- Right: Search results -->
          <td width="99%" valign="top">
            <xsl:call-template name="results"/>
          </td>
        </tr>
        <xsl:if test="number($nPages) &gt; 0">
          <tr class="table-nav">
            <td colspan="2">
              <xsl:call-template name="navigation"/>
            </td>
          </tr>
        </xsl:if>
      </table>
    </form>
  </xsl:template>
<!--////////////////////////////////////////////////// -->
<!-- criteria template -->
  <xsl:template name="criteria">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td class="table-light-left" style="text-align:right" nowrap="nowrap">
          <label for="addressbook-SearchNameT1">Name</label>
        </td>
        <td class="table-content-right" width="100%">
          <input class="text" type="text" name="name" value="{$name}" id="addressbook-SearchNameT1"/>
        </td>
      </tr>
      <tr>
        <td class="table-light-left" style="text-align:right" nowrap="nowrap">
          <label for="addressbook-SearchTitleT1">Title</label>
        </td>
        <td class="table-content-right" width="100%">
          <input class="text" type="text" name="title" value="{$title}" id="addressbook-SearchTitleT1"/>
        </td>
      </tr>
      <tr>
        <td class="table-light-left" style="text-align:right" nowrap="nowrap">
          <label for="addressbook-SearchDeptT1">Department</label>
        </td>
        <td class="table-content-right" width="100%">
          <input class="text" type="text" name="department" value="{$department}" id="addressbook-SearchDeptT1"/>
        </td>
      </tr>
      <tr><td class="table-light-left" style="text-align:right" nowrap="nowrap"><label for="addressbook-SearchEmailT1">Email</label></td>
          <td class="table-content-right" width="100%"><input class="text" type="text" name="email" value="{$email}" id="addressbook-SearchEmailT1"/></td></tr>
      <tr>
        <td class="table-light-left" style="text-align:right" nowrap="nowrap">
          <label for="addressbook-SearchMaxResultsS1">Max Results</label>
        </td>
        <td class="table-content-right" width="100%">
          <select name="max-results" id="addressbook-SearchMaxResultsS1">
            <option value="10"><xsl:if test="$maxResult='10'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>10</option>
            <option value="20"><xsl:if test="$maxResult='20'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>20</option>
            <option value="50"><xsl:if test="$maxResult='50'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>50</option>
            <option value="100"><xsl:if test="$maxResult='100'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>100</option>
            <option value="150"><xsl:if test="$maxResult='150'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>150</option>
          </select>
        </td>
      </tr>
      <tr>
        <td colspan="2" class="table-nav" style="text-align:center">
          <input type="submit" class="uportal-button" value="Search" name="do~search" title="To submit this information and return to the view of the address book" onclick="window.validateForm = true" />
        </td>
      </tr>
    </table>
  </xsl:template>
<!--////////////////////////////////////////////// -->
<!-- Search results template -->
  <xsl:template name="results">
   <!--UniAcc: Layout Table-->
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <xsl:if test="//contact">
            <tr>
                <th class="th">Search Results</th>
            </tr>
        </xsl:if>
        <xsl:apply-templates select="entity | group | contact | campus | folder" mode="search"/>
    </table>
  </xsl:template>
<!-- ///////////////////////////////////////// -->
<!-- Template for contact (non-portal users) -->
  <xsl:template match="contact" mode="search">
    <tr>
<!-- Contact name -->
      <td width="100%" class="table-content-right" nowrap="nowrap" valign="middle">
        <img src="{$baseImagePath}/rad/card_16.gif" border="0" align="absmiddle" alt="Contact:" title="Contact:"/>
        <xsl:text> </xsl:text>
        <a href="{$doURL}=editContact&amp;uP_root=me&amp;ord-id={number($start) + position() - 1}" title="Edit this contact" onmouseover="swapImage('addressBookContactEditImage{position()}','channel_edit_active.gif')" onmouseout="swapImage('addressBookContactEditImage{position()}','channel_edit_base.gif')">
          <xsl:value-of select="name"/>
          <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
          <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" align="absmiddle" name="addressBookContactEditImage{position()}" id="addressBookContactEditImage{position()}" alt="Edit this contact" title="Edit this contact"/>
        </a>
        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
        <a href="{$doURL}=confirmDelContact&amp;uP_root=me&amp;ord-id={number($start) + position() - 1}" title="Delete this contact" onmouseover="swapImage('addressBookContactDeleteImage{position()}','channel_delete_active.gif')" onmouseout="swapImage('addressBookContactDeleteImage{position()}','channel_delete_base.gif')">
          <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" align="absmiddle" name="addressBookContactDeleteImage{position()}" id="addressBookContactDeleteImage{position()}" alt="Delete this contact" title="Delete this contact"/>
        </a>
<!-- Other informations -->
<!-- Title Department Company-->
        <xsl:variable name="line11">
          <xsl:call-template name="esc-strings">
            <xsl:with-param name="title" select="title/text()"/>
            <xsl:with-param name="department" select="department/text()"/>
            <xsl:with-param name="company" select="company/text()"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="boolean($line11=' ')=false and boolean($line11='')=false"><br/>  <xsl:value-of select="$line11"/></xsl:if>
<!-- cell-phone, office-phone, home-phone, email -->
        <xsl:variable name="line12">
          <xsl:call-template name="esc-strings">
            <xsl:with-param name="cell-phone" select="cell-phone/text()"/>
            <xsl:with-param name="business-phone" select="business-phone/text()"/>
            <xsl:with-param name="home-phone" select="home-phone/text()"/>
            <xsl:with-param name="email" select="email/text()"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="boolean($line12=' ')=false and boolean($line12='')=false"><br/>  <xsl:value-of select="$line12"/></xsl:if>
      </td>
    </tr>
  </xsl:template>
<!-- ///////////////////////////////////////// -->
<!-- Template for campus -->
  <xsl:template match="campus" mode="search">
    <tr>
<!-- name -->
      <td width="100%" class="table-content-right" nowrap="nowrap" valign="middle">
        <img src="{$baseImagePath}/rad/card_16.gif" border="0" align="absmiddle" alt="Missing Person" title="Missing Person"/>
        <xsl:text> </xsl:text>
        <a href="{$doURL}=copyContact&amp;uP_root=me&amp;ord-id={number($start) + position() - 1}" title="Copy">
          <xsl:value-of select="name"/>
          <xsl:text> </xsl:text>
          <img src="{$baseImagePath}/rad/copy_12.gif" border="0" alt="Copy Contact" title="Copy Contact"/>
        </a>
        <xsl:text> </xsl:text>
<!-- Other informations -->
<!-- Title Department Company-->
        <xsl:variable name="line21">
          <xsl:call-template name="esc-strings">
            <xsl:with-param name="title" select="title/text()"/>
            <xsl:with-param name="department" select="department/text()"/>
            <xsl:with-param name="company" select="company/text()"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="boolean($line21=' ')=false and boolean($line21='')=false"><br/>  <xsl:value-of select="$line21"/></xsl:if>
<!-- cell-phone, office-phone, home-phone, email -->
        <xsl:variable name="line22">
          <xsl:call-template name="esc-strings">
            <xsl:with-param name="cell-phone" select="cell-phone/text()"/>
            <xsl:with-param name="business-phone" select="business-phone/text()"/>
            <xsl:with-param name="home-phone" select="home-phone/text()"/>
            <xsl:with-param name="email" select="email/text()"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="boolean($line22=' ')=false and boolean($line22='')=false"><br/>  <xsl:value-of select="$line22"/></xsl:if>
      </td>
    </tr>
  </xsl:template>
<!-- ///////////////////////////////////////// -->
<!-- Template for group  -->
  <xsl:template match="group" mode="search">
    <tr>
<!-- name -->
      <td width="100%" class="table-content-right" nowrap="nowrap" valign="middle">
        <img src="{$baseImagePath}/rad/persons_16.gif" border="0" alt="Group named:" title="Group named:"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@iname"/>
        <xsl:text> </xsl:text>
      </td>
    </tr>
  </xsl:template>
<!-- ///////////////////////////////////////// -->
<!-- Template for entity  -->
  <xsl:template match="entity" mode="search">
    <tr>
<!-- name -->
      <td width="100%" class="table-content-right" nowrap="nowrap" valign="middle">
        <img src="{$baseImagePath}/rad/person_16.gif" border="0" alt="Individual named:" title="Individual named:"/>
        <xsl:text> </xsl:text>
        <a href="{$doURL}=copyContactPortal&amp;uP_root=me&amp;ord-id={number($start) + position() - 1}" title="Copy">
          <xsl:value-of select="@iname"/>
          <xsl:text> </xsl:text>
          <img src="{$baseImagePath}/rad/copy_12.gif" border="0" alt="Copy Contact" title="Copy Contact"/>
        </a>
        <xsl:text> </xsl:text>
<!-- Other informations: email -->
        <xsl:if test="@email/text()"><br/>  <xsl:value-of select="@email"/></xsl:if>
      </td>
    </tr>
  </xsl:template>
<!-- ///////////////////////////////////////// -->
<!-- Template for folder  -->
  <xsl:template match="folder" mode="search">
    <tr>
<!-- name -->
      <td width="100%" class="table-content-right" nowrap="nowrap" valign="middle">
        <img src="{$baseImagePath}/rad/folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
        <xsl:text> </xsl:text>
        <a href="{$doURL}=editFolder&amp;uP_root=me&amp;ord-id={number($start) + position() - 1}" title="Edit this group" onmouseover="swapImage('addressBookGroupEditImage{position()}','channel_edit_active.gif')" onmouseout="swapImage('addressBookGroupEditImage{position()}','channel_edit_base.gif')">
          <xsl:value-of select="text()"/>
          <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
          <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" align="absmiddle" name="addressBookGroupEditImage{position()}" id="addressBookGroupEditImage{position()}" alt="Edit this group" title="Edit this group"/>
        </a>
        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
        <a href="{$doURL}=confirmDelFolder&amp;uP_root=me&amp;ord-id={number($start) + position() - 1}" title="Delete this group" onmouseover="swapImage('addressBookGroupDeleteImage{position()}','channel_delete_active.gif')" onmouseout="swapImage('addressBookGroupDeleteImage{position()}','channel_delete_base.gif')">
          <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" align="absmiddle" name="addressBookGroupDeleteImage{position()}" id="addressBookGroupDeleteImage{position()}" alt="Delete this group" title="Delete this group"/>
        </a>
        <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
      </td>
    </tr>
  </xsl:template>
<!--////////////////////////////////////////////// -->
<!-- Search results page navigation template -->
  <xsl:template name="navigation"><!-- Previous --><xsl:if test="number($curPage) &gt; 0"><input type="image" align="absmiddle" name="do~prev&amp;p={$curPage}" border="0" src="{$baseImagePath}/rad/prev_12.gif" title="Previous"/></xsl:if><xsl:if test="number($curPage) &lt; 1"><img src="{$baseImagePath}/rad/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/></xsl:if><!-- curPage/nPages --><xsl:text> </xsl:text><xsl:value-of select="number($start)+1"/> - <xsl:value-of select="$end"/><xsl:text> of </xsl:text><xsl:value-of select="$total"/><xsl:text> </xsl:text><!-- Next --><xsl:if test="number($curPage) &lt; number($nPages) - 1"><input type="image" align="absmiddle" border="0" name="do~next&amp;p={$curPage}" src="{$baseImagePath}/rad/next_12.gif" title="Next"/></xsl:if><xsl:if test="number($curPage) &gt; number($nPages) - 2"><img src="{$baseImagePath}/rad/next_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/></xsl:if><img src="{$baseImagePath}/rad/transparent.gif" border="0" width="1" alt="" title=""/></xsl:template>
</xsl:stylesheet>
