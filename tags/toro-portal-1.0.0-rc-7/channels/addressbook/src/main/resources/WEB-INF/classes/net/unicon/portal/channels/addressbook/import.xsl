<?xml version="1.0" encoding="UTF-8"?><!--   i Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.     This software is the confidential and proprietary information of    Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not    disclose such Confidential Information and shall use it only in    accordance with the terms of the license agreement you entered into    with IBS-DP or its authorized distributors.     IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY    OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT    LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A    PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE    FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING    OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.  --><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">  <xsl:output method="html"/>  <xsl:include href="common.xsl"/>  <xsl:param name="baseActionURL">default</xsl:param>  <xsl:param name="sid">default</xsl:param>  <xsl:param name="goURL"/>  <xsl:param name="doURL"/>  <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>  <!--/////////////////////////////////////////////////////////////////////////////// -->  <xsl:param name="first-name"/>  <xsl:param name="last-name"/>  <xsl:param name="middle-name"/>  <xsl:param name="email"/>  <xsl:param name="mobile"/>  <xsl:param name="title"/>  <xsl:param name="company"/>  <xsl:param name="department"/>  <xsl:param name="business-phone"/>  <xsl:param name="fax"/>  <xsl:param name="office-address"/>  <xsl:param name="home-phone"/>  <xsl:param name="home-address"/>  <xsl:param name="notes"/>    <xsl:template match="/">    <form method="post" action="{$baseActionURL}" name="addressBookForm" id="addressBookForm">      <xsl:call-template name="links"/>      <div class="page-title">Import Contacts</div>      <div class="page-toolbar-container">      	<xsl:call-template name="page-links"/>      </div>    </form>    <xsl:apply-templates/>  </xsl:template>    <xsl:template match="addressbook-system">	<xsl:call-template name="autoFormJS"/>    <form action="{$baseActionURL}" method="post" value="*.*" enctype="multipart/form-data" onSubmit="return validator.applyFormRules(this, new AddressbookRulesObject());">      <input type="hidden" name="sid" value="{$sid}"/>      <!--UniAcc: Layout Table -->      <table width="100%" border="0" cellpadding="0" cellspacing="0">        <tr>          <td class="table-light-left" style="text-align:right">            <label for="addressbook-ImportFileNameF1">File Name</label>          </td>          <td class="table-content-right">            <input type="file" style="text-align: left" name="file" enctype="multipart/form-data" size="50" maxlength="300" value="Click 'Browse' to select a file" id="addressbook-ImportFileNameF1" class="text"/>            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>            <input type="submit" class="uportal-button" value="Import" name="do~go" title="To load tab-delimited file into the following form"/>          </td>        </tr>        <xsl:if test="$isOpen='true'">          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportFirstNameS1">First Name</label>            </td>            <td class="table-content-right" width="100%">              <select name="first-name" id="addressbook-ImportFirstNameS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$first-name"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportMidNameS1">Middle Name</label>            </td>            <td class="table-content-right" width="100%">              <select name="middle-name" id="addressbook-ImportMidNameS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$middle-name"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportLastNameS1">Last Name</label>            </td>            <td class="table-content-right" width="100%">              <select name="last-name" id="addressbook-ImportLastNameS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$last-name"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportEmailS1">Email</label>            </td>            <td class="table-content-right" width="100%">              <select name="email" id="addressbook-ImportEmailS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$email"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportMobileS1">Mobile</label>            </td>            <td class="table-content-right" width="100%">              <select name="mobile" id="addressbook-ImportMobileS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$mobile"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportMobileS1">Title</label>            </td>            <td class="table-content-right" width="100%">              <select name="title" id="addressbook-ImportMobileS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$title"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportCompanyS1">Company</label>            </td>            <td class="table-content-right" width="100%">              <select name="company" id="addressbook-ImportCompanyS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$company"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportDepartmentS1">Department</label>            </td>            <td class="table-content-right" width="100%">              <select name="department" id="addressbook-ImportDepartmentS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$department"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportBusPhoneS1">Business Phone</label>            </td>            <td class="table-content-right" width="100%">              <select name="business-phone" id="addressbook-ImportBusPhoneS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$business-phone"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportFaxS1">Fax</label>            </td>            <td class="table-content-right" width="100%">              <select name="fax" id="addressbook-ImportFaxS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$fax"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportOffAddressS1">Office Address</label>            </td>            <td class="table-content-right" width="100%">              <select name="office-address" id="addressbook-ImportOffAddressS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$office-address"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportHomePhoneS1">Home Phone</label>            </td>            <td class="table-content-right" width="100%">              <select name="home-phone" id="addressbook-ImportHomePhoneS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$home-phone"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportHomeAddressS1">Home Address</label>            </td>            <td class="table-content-right" width="100%">              <select name="home-address" id="addressbook-ImportHomeAddressS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$home-address"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td class="table-light-left" style="text-align:right" nowrap="nowrap">              <label for="addressbook-ImportNotesS1">Notes</label>            </td>            <td class="table-content-right" width="100%">              <select name="notes" id="addressbook-ImportNotesS1">                <xsl:call-template name="load-cbo">                  <xsl:with-param name="selected" select="$notes"/>                </xsl:call-template>              </select>            </td>          </tr>          <tr>            <td colspan="2" class="table-nav" style="text-align:center">              <input type="hidden" name="default" value="do~ok"/>              <input type="submit" class="uportal-button" value="OK" name="do~ok" title="To submit this information and return to the view of the address book"/>              <input type="submit" class="uportal-button" value="Cancel" name="do~cancel" title="To cancel this and return to the view of the address book"/>            </td>          </tr>        </xsl:if>      </table>    </form>  </xsl:template>  <!-- ///////////////////////////////////////// -->  <xsl:template name="load-cbo">    <xsl:param name="selected"/>    <option value="0">&lt;-- Select one --&gt;</option>    <xsl:for-each select="fields/field">      <xsl:choose>        <xsl:when test="@name=$selected">          <option value="{@name}" selected="selected">            <xsl:value-of select="@name"/>          </option>        </xsl:when>        <xsl:otherwise>          <option value="{@name}">            <xsl:value-of select="@name"/>          </option>        </xsl:otherwise>      </xsl:choose>    </xsl:for-each>  </xsl:template></xsl:stylesheet>