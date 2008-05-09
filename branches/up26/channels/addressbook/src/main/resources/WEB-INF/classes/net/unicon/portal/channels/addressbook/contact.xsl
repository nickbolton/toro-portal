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

    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:param name="sid"/>

    <xsl:param name="goURL"/>

    <xsl:param name="doURL"/>

    <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>

    <!--//////////////////////////////////////////////////////////////////////////////////-->

    <!--<xsl:param name="isNew">false</xsl:param> -->

    <!--//////////////////////////////////////////////////////////////////////////////////-->





    <xsl:template match="/">

        <form method="post" action="{$baseActionURL}" name="addressBookForm" id="addressBookForm">

            <xsl:call-template name="links"/>

        </form>



        <xsl:apply-templates/>

    </xsl:template>





    <xsl:template match="addressbook-system">

        <xsl:variable name="name" select="contact/name"/>

        <xsl:variable name="email" select="contact/email"/>

        <xsl:variable name="mobile" select="contact/cell-phone"/>

        <xsl:variable name="title" select="contact/title"/>

        <xsl:variable name="company" select="contact/company"/>

        <xsl:variable name="department" select="contact/department"/>

        <xsl:variable name="business-phone" select="contact/business-phone"/>

        <xsl:variable name="fax" select="contact/fax"/>

        <xsl:variable name="office-address" select="contact/office-address"/>

        <xsl:variable name="home-phone" select="contact/home-phone"/>

        <xsl:variable name="notes" select="contact/notes"/>

        <xsl:call-template name="autoFormJS"/>              

        <form method="post" onSubmit="if (this.buttonpress != 'Cancel') return validator.applyFormRules(this, new AddressbookRulesObject())" action="{$baseActionURL}" name="addressBookBodyForm" id="addressBookBodyForm">

            <!--UniAcc: Layout Table -->

            <table width="100%" border="0" cellpadding="0" cellspacing="0">

                <tr>



                    <td colspan="2">
                    	<div class="page-title">

                        <xsl:choose>

                            <xsl:when test="$isNew='true'">Add Contact</xsl:when>

                            <xsl:otherwise>Edit Contact</xsl:otherwise>

                        </xsl:choose>
						</div>
                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactNameT1">Name</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="name" value="{$name}" size="35" id="addressbook-ContactNameT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactEmailT1">Email</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="email" value="{$email}" size="35" id="addressbook-ContactEmailT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactMobileT1">Mobile</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="mobile" value="{$mobile}" size="35" id="addressbook-ContactMobileT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactTitleT1">Title</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="title" value="{$title}" size="35" id="addressbook-ContactTitleT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactCompanyT1">Company</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="company" value="{$company}" size="35" id="addressbook-ContactCompanyT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactDepartmentT1">Department</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="department" value="{$department}" size="35" id="addressbook-ContactDepartmentT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactBusPhoneT1">Business Phone</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="business-phone" value="{$business-phone}" size="35" id="addressbook-ContactBusPhoneT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactFaxT1">Fax</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="fax" value="{$fax}" size="35" id="addressbook-ContactFaxT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactOffAddressTA1">Office Address</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <textarea class="text" rows="2" cols="35" name="office-address" id="addressbook-ContactOffAddressTA1">

                            <xsl:value-of select="contact/office-address"/>

                        </textarea>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactHomePhoneT1">Home Phone</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <input class="text" type="text" name="home-phone" value="{$home-phone}" size="35" id="addressbook-ContactHomePhoneT1"/>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactHomeAddressTA1">Home Address</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <textarea class="text" rows="2" cols="35" name="home-address" id="addressbook-ContactHomeAddressTA1">

                            <xsl:value-of select="contact/home-address"/>

                        </textarea>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">

                        <label for="addressbook-ContactNotesTA1">Notes</label>

                    </td>

                    <td class="table-content-right" width="100%">

                        <textarea class="text" rows="2" cols="35" name="notes" id="addressbook-ContactNotesTA1">

                            <xsl:value-of select="contact/notes"/>

                        </textarea>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">Groups In</td>

                    <td class="table-content-right" width="100%">

                        <input type="hidden" name="blank" value=""/>

                        <input type="hidden" name="folderid" value=""/>

                        <xsl:choose>

                            <xsl:when test="count(selected-folder/folder) &gt; 0">

                                <ul>

                                    <xsl:for-each select="selected-folder/folder">

                                        <xsl:variable name="del-group-name" select="text()"/>

                                        <li>

                                            <a href="javascript:document.addressBookBodyForm.submit();" onclick="document.addressBookBodyForm.blank.name='do~deleteFolder&amp;folderid={@id}';" title="" onmouseover="swapImage('addressBookGroupDeleteImage{position()}','channel_delete_active.gif')" onmouseout="swapImage('addressBookGroupDeleteImage{position()}','channel_delete_base.gif')">
                                                <xsl:value-of select="text()"/>

                                                <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>

                                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"  align="absmiddle" name="addressBookGroupDeleteImage{position()}" id="addressBookGroupDeleteImage{position()}" alt="Disassociate user with {text()} group" title="Disassociate user with {text()} group"/>

                                            </a>

                                        </li>

                                    </xsl:for-each>

                                </ul>

                            </xsl:when>

                            <xsl:otherwise>

                                <ul>

                                    <li>None</li>

                                </ul>

                            </xsl:otherwise>

                        </xsl:choose>

                    </td>

                </tr>

                <tr>

                    <td class="table-light-left" style="text-align:right" nowrap="nowrap">Groups Not In</td>

                    <td class="table-content-right" width="100%">

                        <xsl:choose>

                            <xsl:when test="count(folder) &gt; 0">

                                <ul>

                                    <xsl:for-each select="folder">

                                        <li>

                                            <a href="javascript:document.addressBookBodyForm.submit();" onclick="document.addressBookBodyForm.folderid.value='{@id}';document.addressBookBodyForm.blank.value='Add';document.addressBookBodyForm.blank.name='do~add';" title="" onmouseover="swapImage('addressBookGroupAddImage{position()}','channel_add_active.gif')" onmouseout="swapImage('addressBookGroupAddImage{position()}','channel_add_base.gif')">

                                                <xsl:value-of select="text()"/>

                                                <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>

                                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"  align="absmiddle" name="addressBookGroupAddImage{position()}" id="addressBookGroupAddImage{position()}" alt="Associate user with {text()} group" title="Associate user with {text()} group"/>

                                            </a>

                                        </li>

                                    </xsl:for-each>

                                </ul>

                            </xsl:when>

                            <xsl:otherwise>

                                <ul>

                                    <li>None</li>

                                </ul>

                            </xsl:otherwise>

                        </xsl:choose>

                    </td>

                </tr>

                <tr>

                    <td colspan="2" class="table-nav" style="text-align:center">

                        <input type="hidden" name="sid" value="{$sid}"/>

                        <input type="hidden" name="default" value="do~ok"/>

                        <input type="submit" class="uportal-button" value="OK" name="do~ok" title="To submit this information and return to the view of the address book"/>

                        <input type="submit" class="uportal-button" value="Cancel" name="do~cancel" title="To cancel this and return to the view of the address book" onclick="if (this.form) this.form.buttonpress = 'Cancel';"/>

                    </td>

                </tr>

            </table>

        </form>

    </xsl:template>

</xsl:stylesheet>


