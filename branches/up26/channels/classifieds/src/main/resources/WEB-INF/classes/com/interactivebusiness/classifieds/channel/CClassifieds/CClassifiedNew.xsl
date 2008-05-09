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
  4    Channels  1.3         8/6/2002 4:55:59 PM  Freddy Lopez    modified files
       as part of new version of classifieds channel
  3    Channels  1.2         5/29/2002 12:16:02 PMFreddy Lopez    fixing
       classifieds, adding images for classifieds, groups, toolbar
  2    Channels  1.1         12/20/2001 4:54:02 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:38 PMFreddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="NewItem">
    
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "/*"/>
    </textarea> -->
		<xsl:call-template name="autoFormJS"/>

        <form action="{$baseActionURL}?action=previewNew" method="post" enctype="multipart/form-data" 
        	  onSubmit="if (!CClassifiedsRulesObject.prototype.isCancel) return validator.applyFormRules(this, new CClassifiedsRulesObject())">
            <!--UniAcc: Data Table -->
            <table border="0" cellspacing="0" cellpadding="0" width="100%">
                <tr>
                    <th class="th-top-single" colspan="2">New Classified</th>
                </tr>
                <xsl:choose> 
                    <xsl:when test="(//message/@error = 'noMsg')">
                        <tr>
                            <td colspan="2" class="table-content-single">
                                <span class="uportal-channel-warning">ERROR: A classified message is required.</span>
                            </td>
                        </tr>
                    </xsl:when> 
                    <xsl:when test="(//phone/@error = 'error')"> 
                        <tr>
                            <td colspan="2" class="table-content-single">
                                <span class="uportal-channel-warning">ERROR: A phone number is required.</span>
                            </td>
                        </tr>
                    </xsl:when> 
                    <xsl:when test="(//message/@error = 'oversize')"> 
                        <tr> 
                            <td colspan="2" class="table-content-single"> 
                                <span class="uportal-channel-warning">ERROR: Classified message length exceeds 2500 character limit.</span> 
                            </td> 
                        </tr> 
                    </xsl:when> 
					<xsl:when test="(//image/@error = 'filetoobig')"> 
                        <tr> 
                            <td colspan="2" class="table-content-single"> 
                                <span class="uportal-channel-warning">ERROR: Classified Image Size exceeds the 100K limit.</span> 
                            </td> 
                        </tr> 
                    </xsl:when> 
                </xsl:choose> 
                <tr>
                    <td align="right" class="table-content-left" scope="row" nowrap="nowrap">
                        <label for="CClass-NewClassTopicS1">Select Topic:</label>
                    </td>
                    <td align="left" class="table-content-right">
                        <select name="topics" class="uportal-input-text" id="CClass-NewClassTopicS1">
                            <xsl:apply-templates select="topic_list">
                                <xsl:sort order="descending" data-type="text" select="."/>
                            </xsl:apply-templates>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td align="right" valign="top" class="table-content-left" scope="row" nowrap="nowrap">
                        <label for="CClass-NewClassMessageTA1">
                            <xsl:if test="//message/@error">
                                <span class="uportal-channel-warning">*</span>
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            </xsl:if>Message:
                        </label><br/>
                        <span class="uportal-text-small">(2500 character limit)</span>
                    </td>
                    <td align="left" class="table-content-right" width="100%">
                        <textarea name="message" row="10" cols="50" class="uportal-input-text" id="CClass-NewClassMessageTA1">
                            <xsl:value-of select="message"/>
                        </textarea>
                    </td>
                </tr>

                <tr>
                    <td align="right" class="table-content-left" scope="row">
                        <label for="CClass-NewClassCostT1">Cost:</label><br/>
                        <span class="uportal-text-small">(optional)</span>
                    </td>
                    <td align="left" class="table-content-right">
                        <xsl:variable name="cost">
                            <xsl:value-of select="cost"/>
                        </xsl:variable>
                        <input type="text" name="cost" size="15" maxlength="15" value="{$cost}" class="uportal-input-text" id="CClass-NewClassCostT1"/>
                    </td>
                </tr>

                <tr>
                    <td align="right" class="table-content-left" scope="row">
                        <label for="CClass-NewClassImageF1">Image:</label><br />
                        <span class="uportal-text-small">(optional)</span>
                    </td>
                    <td align="left" class="table-content-right">
                        <input type="file" name="classified_image" size="15" maxlength="50" enctype="multipart/form-data" class="uportal-input-text" id="CClass-NewClassImageF1" style="text-align: left;"/><br/>
                        <span class="uportal-channel-warning">100k limit; .gif, .jpg, .jpeg, .png, or .bmp format</span>
                    </td>
                </tr>

                <xsl:if test="child::image">
                    <tr>
                        <td colspan="2" class="table-content-single">
                            Image <strong><xsl:value-of select="image"/></strong> has been loaded.
                        </td>
                    </tr>
                </xsl:if>

                <tr>
                    <th colspan="2" class="th" style="padding-top:20px;" id="CClass-NewContactInfo">Contact Information</th>
                </tr>

                <tr>
                    <td align="right" class="table-content-left" headers="CClass-NewContactInfo" id="CClass-NewContactName">
                        <label for="CClass-NewClassContactNameT1">Name:</label><br />
                        <span class="uportal-text-small">(optional)</span>
                    </td>
                    <td align="left" class="table-content-right" headers="CClass-NewContactInfo CClass-NewContactName" >
                        <xsl:variable name="name">
                            <xsl:value-of select="name"/>
                        </xsl:variable>
                        <input type="text" name="contact_name" size="25" maxlength="50" value="{$name}" class="uportal-input-text" id="CClass-NewClassContactNameT1"/>
                    </td>
                </tr>

                <tr>
                    <td align="right" class="table-content-left" headers="CClass-NewContactInfo" id="CClass-NewContactPhone">
                        <label for="CClass-NewClassContactPhoneT1">
                            <xsl:if test="//phone/@error">
                                <span class="uportal-channel-warning">*</span>
                                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                            </xsl:if>Phone:
                        </label>
                    </td>
                    <td align="left" class="table-content-right" headers="CClass-NewContactInfo CClass-NewContactPhone">
                        <xsl:variable name="phone">
                            <xsl:value-of select="phone"/>
                        </xsl:variable>
                        <input type="text" name="phone" size="25" maxlength="20" value="{$phone}" class="uportal-input-text" id="CClass-NewClassContactPhoneT1"/>
                    </td>
                </tr>

                <tr>
                    <td align="right" class="table-content-left" headers="CClass-NewContactInfo" id="CClass-NewContactEmail" nowrap="nowrap">
                        <label for="CClass-NewClassContactEmailT1">or Email:</label>
                    </td>
                    <td align="left" class="table-content-right" headers="CClass-NewContactInfo CClass-NewContactEmail">
                        <xsl:variable name="email">
                            <xsl:value-of select="email"/>
                        </xsl:variable>
                        <input type="text" name="email" size="25" maxlength="50" value="{$email}" class="uportal-input-text" id="CClass-NewClassContactEmailT1"/>
                    </td>
                </tr>

                <tr>
                    <td colspan="2" class="table-content-single" style="padding-top:20px;"><span class="uportal-channel-warning">Note:</span> Your classified must be approved. A message will be sent to you regarding the approval process.</td>
                </tr>
                <tr>
                    <td colspan="2" class="table-light-bottom" style="text-align:center;">
                        <input type="submit" name="preview" value="Preview" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button" onclick="CClassifiedsRulesObject.prototype.isCancel=true;"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>

    <xsl:template name="topics">
        <xsl:for-each select="topic">
            <xsl:sort order="ascending"/>
            <xsl:variable name="value">
                <xsl:value-of select="select"/>
            </xsl:variable>
            <option class="uportal-text">
                <xsl:attribute name="value">
                    <xsl:value-of select="id"/>
                </xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$value='true'">
                        <xsl:attribute name="selected">
                            <xsl:value-of select="value"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text></xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="name"/>
            </option>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="option">

        <xsl:variable name="value">
            <xsl:value-of select="@value"/>
        </xsl:variable>
        <xsl:variable name="selected">
            <xsl:value-of select="@selected"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$selected = 'yes'">
                <option value="{$value}" selected="selected">
                    <xsl:apply-templates/>
                </option>
            </xsl:when>

            <xsl:otherwise>
                <option value="{$value}">
                    <xsl:apply-templates/>
                </option>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
