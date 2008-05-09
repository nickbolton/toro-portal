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
  1    Channels  1.0         8/6/2002 4:54:03 PM  Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>
    
    <!-- Start of XSL Code -->

    <xsl:template match="Icon_Library">
    
        <!-- <textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->
		<xsl:call-template name="autoFormJS"/>

        <form action="{$baseActionURL}?action=chooseIcon" method="post" enctype="multipart/form-data" 
        	  onSubmit="if (!CClassifiedsRulesObject.prototype.isCancel) return validator.applyFormRules(this, new CClassifiedsRulesObject())">
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
                <tr>
                    <th class="th-top-single">Icon Library</th>
                </tr>
                <tr>
                    <td>
                        <!--UniAcc: Layout Table -->
                        <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
                            <tr>
                                <td colspan="2" class="table-content-single">Please select an icon to assign to the Topic: <b><xsl:value-of select="@topic_name"/></b>
                                </td>
                            </tr>
                            <xsl:if test="@error='error'">
                                <tr>
                                    <td colspan="2" class="table-content-single">
                                        <span class="uportal-channel-warning">ERROR ENCOUNTERED: Please choose an Icon or select 'default'</span>
                                    </td>
                                </tr>
                            </xsl:if>
                            <xsl:if test="@error='error1'"> 
                               <tr> 
                                    <td colspan="2" class="table-content-single"> 
                                        <font class="uportal-channel-warning">ERROR ENCOUNTERED: The size of the icon image can not be over 40KB.  Please upload another one.</font> 
                                    </td> 
                               </tr> 
                           </xsl:if> 

                            <tr>
                                <td align="left" width="5%" class="table-content-left">
                                    <input type="radio" name="icon" value="topic.gif:image/gif" checked="checked" id="CClass-IconDefault"/>
                                </td>
                                <td class="table-content-right">
                                    <img src="{$resourceURL}?icon=icon&amp;image=topic.gif&amp;mime_type=image/gif" border="0" alt="" title=""/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                    <label for="CClass-IconDefault">(default)</label>
                                </td>
                            </tr>

                            <xsl:for-each select="icon">
                                <tr>
                                    <td align="left" width="5%" class="table-content-left">
                                        <xsl:if test="@selected='yes'">
                                            <input type="radio" name="icon" value="{@name}:{@mime_type}" checked="checked" id="{@id}"/>
                                        </xsl:if>
                                        <xsl:if test="@selected='no'">
                                            <input type="radio" name="icon" value="{@name}:{@mime_type}" id="{@id}"/>
                                        </xsl:if>
                                    </td>
                                    <td class="table-content-right">
                                        <!--Currently there is no @title for icon, but ADA compliance necessitates some descriptor for the icon -->
                                        <img src="{$resourceURL}?icon=icon&amp;image={@name}&amp;mime_type={@mime_type}" border="0" alt="{@name}" title="{@name}"/>
                                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                        <label for="{@id}">&#160;</label>
                                        <a href="{$baseActionURL}?action=chooseIcon&amp;remove={@id}">
                                            <img src="{$imagedir}/delete_12.gif" border="0" alt="Delete Icon" title="Delete Icon"/>
                                        </a>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            <tr>
                                <td colspan="2" class="table-content-single">
                                    <label for="CClass-IconChooseAddF1:">Add Icon (40k max):</label>
                                    <br/>
                                    <input type="file" name="icon_image" size="15" maxlength="50" enctype="multipart/form-data" class="uportal-input-text" style="text-align: left;"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                    <input type="submit" name="add" value="Add Icon" class="uportal-button"/>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="4" class="table-light-single-bottom" style="text-align:center;">
                                    <!-- <input type="hidden" name="action" value="chooseIcon"/> -->

                                    <input type="submit" name="back" value="Back" class="uportal-button"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                    <input type="submit" name="next" value="Next" class="uportal-button"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                    <!-- if cancel go back to createtopic -->
                                    <input type="submit" name="cancel" value="Cancel" class="uportal-button" onclick="CClassifiedsRulesObject.prototype.isCancel=true;"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
