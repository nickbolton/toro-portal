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
  1    Channels  1.0         8/6/2002 4:54:06 PM  Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="SelectGroupTopic">
    
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> --> 
        
        <form action="{$baseActionURL}?action=PreviewTopic&amp;admin=yes" method="post" name="selectgroup">
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
                <tr>
                    <th class="th-top-single">Current approvers for classifieds topic</th>
                </tr>
                <tr>
                    <td class="table-content-single-top" style="text-align:center;">
                        <input type="submit" name="back" value="Back" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="next" value="Add Approver" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="skip" value="Continue" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                    </td>
                </tr>
                
                <xsl:apply-templates select="error"/>
                
                <tr>
                    <td class="table-content-single-top">
                        Classifieds Topic <b><xsl:value-of select="topicname"/></b>:
                    </td>
                </tr>

                <xsl:apply-templates select="previousSelection"/>
<!--
	*** JK - NUKE THIS PART, AS WE'RE ONLY USING THIS TO SHOW AND ALLOW DELETION OF CURRENT SELECTIONS ***

                <tr>
                    <td class="table-content-single">
                        <label for="CClass-SelAppFindT1">Find user:</label>
                        <input type="text" name="person_name" size="12" maxsize="20" class="uportal-input-text" id="CClass-SelAppFindT1"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="go" value="Go" class="uportal-button"/>
                    </td>
                </tr>
                
                <xsl:apply-templates select="name_search"/>
                
                <tr>
                    <td>
-->
                        <!--UniaAcc: Layout Table -->
<!--
                        <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
                            <xsl:apply-templates select="groups_list"/>
                        </table>
                    </td>
                </tr>
-->
                <tr>
                    <td class="table-content-single-bottom" style="text-align:center;">
                        <!-- <input type="hidden" name="action" value="PreviewTopic"/> -->
                        <!-- <input type="hidden" name="admin" value="yes"/> -->
                        <input type="submit" name="back" value="Back" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="next" value="Add Approver" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="skip" value="Continue" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>


    <xsl:template match="error">
        <tr>
            <td align="left">
                <font class="uportal-channel-warning">***ERROR ENCOUNTERED: No Group/Person was selected</font>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="previousSelection">
        <tr>
            <td>
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <!-- If inside this template, then user is editing a folder -->
                    <input type="hidden" name="newfolder" value="false"/>

                    <xsl:for-each select="groupName">
                        <xsl:sort select="@entity"/>
                        <tr>
                            <td width="5%">
                              <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
				<!-- 
                                <input type="checkbox" name="{@ID}-{@entity}" value="checked" checked="checked" id="CClass{@ID}-{@entity}" onclick="this.form[1].focus();"/>
				-->

                                <xsl:if test="@entity = '2'">
                                    <img src="{$imagedir}/person_16.gif" border="0" alt="Person" title="Person"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                </xsl:if>
                                <xsl:if test="@entity = '3'">
                                    <img src="{$imagedir}/folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                </xsl:if>
                                <label for="CClass{@ID}-{@entity}">
                                    <font class="uportal-channel-text">
                                        <xsl:value-of select="@name"/>
                                    </font>
                                </label>

                              <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
					<a href="{$baseActionURL}?action=DeleteApprover&amp;approverID={@ID}-{@entity}" style="text-decoration:none">
                			  <img src="{$imagedir}/delete_12.gif" border="0" alt="Delete Approver" title="Delete Approver"/>
            			</a>

                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="name_search">
        <tr>
            <td class="table-content-single">
                <font class="uportal-channel-text">Search Results:</font>
            </td>
        </tr>
        <tr>
            <td class="table-content-single">
                <font class="uportal-channel-error">(NOTE: select person to keep user and continue searching)</font>
            </td>
        </tr>
        <tr>
            <td class="table-content-single">
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="1" cellspacing="0" align="center" width="100%" class="uportal-background-content">
                    <xsl:apply-templates select="nameNotFound"/>
                    <xsl:apply-templates select="personName"/>
                </table>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="nameNotFound">
        <tr>
            <td class="table-content-single">
                <img src="{$imagedir}/shadow_16.gif" border="0" alt="Missing Person" title="Missing Person"/>
                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                <font class="uportal-channel-text">user "<xsl:value-of select="@name"/>" not found!</font>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="personName[position() mod 2 = 1]">
        <tr class="table-light">
            <td width="5%">
                <xsl:choose>
                    <xsl:when test="@selected = 'yes'">
                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" checked="checked" id="CClass{@ID}-{@entity}" onclick="this.form[1].focus();"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="CClass{@ID}-{@entity}" onclick="this.form[1].focus();"/>
                    </xsl:otherwise>
                </xsl:choose>
                <label for="CClass{@ID}-{@entity}">
                    <img src="{$imagedir}/person_16.gif" border="0" alt="Person" title="Person"/>
                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    <font class="uportal-channel-text">
                        <xsl:value-of select="@name"/>
                    </font>
                </label>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="personName[position() mod 2 = 0]">
        <tr class="table-content">
            <td width="5%">
                <xsl:choose>
                    <xsl:when test="@selected = 'yes'">
                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" checked="checked" id="CClass{@ID}-{@entity}" onclick="this.form[1].focus();"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="CClass{@ID}-{@entity}" onclick="this.form[1].focus();"/>
                    </xsl:otherwise>
                </xsl:choose>
                <label for="CClass{@ID}-{@entity}">
                    <img src="{$imagedir}/person_16.gif" border="0" alt="Person" title="Person"/>
                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    <font class="uportal-channel-text">
                        <xsl:value-of select="@name"/>
                    </font>
                </label>
            </td>
        </tr>
    </xsl:template>

<xsl:template match="groups_list">
        <tr>
            <td>

                <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%" class="uportal-background-content">

                    <tr>
                        <td colspan="2">
                            <font class="uportal-channel-text">
                                <strong>Group/Person</strong>
                            </font>
                        </td>
                    </tr>
                    <xsl:for-each select="group[@ID=0]">
                        <xsl:if test="(@ID=0)">
                            <tr>
                                <td width="5%">
                                    <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="CBrief{@ID}-{@entity}"/>
                                </td>

                                <td>

                                    <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">

                                        <xsl:if test="@expand = 'yes'">
                                            <img src="{$imagedir}menu_root_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                                            <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;</xsl:if>
                                        <xsl:if test="@expand = 'no'">
                                            <img src="{$imagedir}menu_root_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;</xsl:if>

                                                                
                                        <label for="CBrief{@ID}-{@entity}">
                                            <font class="uportal-channel-text">
                                                <xsl:value-of select="@name"/>
                                            </font>
                                        </label>
                                    </a>
                                </td>
                            </tr>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:for-each select="descendant::*[not(@ID=0)]">
                        <xsl:choose>
                            <xsl:when test="(@selected = 'yes')">

                                <tr>
                                    <td width="5%">
                                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="CBrief{@ID}-{@entity}"/>
                                    </td>

                                    <td>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '3'">&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '4'">&#160;
&#160;&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '5'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '6'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>


                                        <xsl:choose>
                                            <xsl:when test="@isLast='yes'">
                                                <xsl:if test="@expand = 'yes'">
                                                    <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                        <img src="{$imagedir}menu_corner_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                                                        <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                                                        
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </a>
                                                </xsl:if>
                                                <xsl:if test="@expand = 'no'">

                                                    <xsl:if test="@leafnode = 'no'">
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                            <img src="{$imagedir}menu_corner_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                                <font class="uportal-channel-text">
                                                                    <xsl:value-of select="@name"/>
                                                                </font>
                                                            </label>
                                                        </a>
                                                    </xsl:if>
                                                    <xsl:if test="@leafnode = 'yes'">
                                                        <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                        <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </xsl:if>
                                                </xsl:if>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:if test="@expand = 'yes'">
                                                    <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                        <img src="{$imagedir}menu_tree_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                                                        <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </a>
                                                </xsl:if>
                                                <xsl:if test="@expand = 'no'">
                                                    <xsl:if test="@leafnode = 'no'">
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                            <img src="{$imagedir}menu_tree_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                        </a>
                                                    </xsl:if>
                                                    <xsl:if test="(@leafnode = 'yes') and (@isLast = 'no')">
                                                        <img src="{$imagedir}menu_tree.gif" border="0"   alt="Node" title="Node"/>
                                                        <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </xsl:if>
                                                    <xsl:if test="(@leafnode = 'yes') and (@isLast = 'yes')">
                                                        <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                        <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </xsl:if>
                                                </xsl:if>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                                <xsl:for-each select="descendant::group/group">
                                    <xsl:call-template name="displayChildren">
                                        <xsl:with-param name="name" select="@name"/>
                                        <xsl:with-param name="ID" select="@ID"/>
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <tr>
                                    <td width="5%">
                                        <xsl:choose>
                                            <xsl:when test="@isPerson = 'no'">
                                                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="{@ID}-{@entity}"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '3'">&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '4'">&#160;
&#160;&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '5'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '6'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                                        <xsl:if test="(count(ancestor-or-self::*)) = '7'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                                        <xsl:choose>
                                            <xsl:when test="position()=last()">
                                                <xsl:if test="@expand = 'yes'">
                                                    <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                        <img src="{$imagedir}menu_corner_minus.gif" border="0" alt="Last Node Open" title="Last Node Open"/>
                                                        <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </a>
                                                </xsl:if>
                                                <xsl:if test="@expand = 'no'">
                                                    <xsl:if test="@leafnode = 'no'">
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                            <img src="{$imagedir}menu_corner_plus.gif" border="0" alt="Last Node Closed" title="Last Node Closed"/>
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                                <font class="uportal-channel-text">
                                                                    <xsl:value-of select="@name"/>
                                                                </font>
                                                            </label>
                                                        </a>
                                                    </xsl:if>
                                                    <xsl:if test="@leafnode = 'yes'">
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes&amp;leafnode=yes" title="Display">
                                                            <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                                <font class="uportal-channel-text">
                                                                    <xsl:value-of select="@name"/>
                                                                </font>
                                                            </label>
                                                        </a>
                                                    </xsl:if>
                                                </xsl:if>
                                                <xsl:if test="(@isPerson = 'yes') and (@isLast = 'no')">
                                                    <img src="{$imagedir}menu_tree.gif" border="0" alt="Node" title="Node"/>
                                                    <img src="{$imagedir}person_16.gif" border="0" alt="Person" title="Person"/>&#160;
                                                    <label for="CBrief{@ID}-{@entity}">
                                                        <font class="uportal-channel-text">
                                                            <xsl:value-of select="@name"/>
                                                        </font>
                                                    </label>
                                                </xsl:if>
                                                <xsl:if test="(@isPerson = 'yes') and (@isLast = 'yes')">
                                                    <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                    <img src="{$imagedir}person_16.gif" border="0" alt="Person" title="Person"/>&#160;
                                                    <label for="CBrief{@ID}-{@entity}">
                                                        <font class="uportal-channel-text">
                                                            <xsl:value-of select="@name"/>
                                                        </font>
                                                    </label>
                                                </xsl:if>
                                                <xsl:if test="@isPerson = 'no'">
                                                    <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                    <img src="{$imagedir}shadow_16.gif" border="0" alt="Missing Person" title="Missing Person"/>&#160;
                                                    <font class="uportal-channel-error">No people in Group</font>
                                                </xsl:if>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:if test="(@expand = 'yes') and (@isLast = 'no')">
                                                    <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                        <img src="{$imagedir}menu_tree_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                                                        <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </a>
                                                </xsl:if>
                                                <xsl:if test="(@expand = 'yes') and (@isLast = 'yes')">
                                                    <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                        <img src="{$imagedir}menu_corner_minus.gif" border="0" alt="Last Node Open" title="Last Node Open"/>
                                                        <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                                                        <label for="CBrief{@ID}-{@entity}">
                                                            <font class="uportal-channel-text">
                                                                <xsl:value-of select="@name"/>
                                                            </font>
                                                        </label>
                                                    </a>
                                                </xsl:if>
                                                <xsl:if test="@expand = 'no'">
                                                    <xsl:if test="@leafnode = 'no'">
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes" title="Display">
                                                            <img src="{$imagedir}menu_tree_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                                <font class="uportal-channel-text">
                                                                    <xsl:value-of select="@name"/>
                                                                </font>
                                                            </label>
                                                        </a>
                                                    </xsl:if>
                                                    <xsl:if test="(@leafnode = 'yes') and (@isLast = 'no')">

                                                        <img src="{$imagedir}menu_tree.gif" border="0"  alt="Node" title="Node"/>
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes&amp;leafnode=yes" title="Open">
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                                <font class="uportal-channel-text">
                                                                    <xsl:value-of select="@name"/>
                                                                </font>
                                                            </label>
                                                        </a>
                                                    </xsl:if>
                                                    <xsl:if test="(@leafnode = 'yes') and (@isLast = 'yes')">
                                                        <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                        <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={@ID}&amp;expand=yes&amp;leafnode=yes" title="Open">
                                                            <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                                            <label for="CBrief{@ID}-{@entity}">
                                                                <font class="uportal-channel-text">
                                                                    <xsl:value-of select="@name"/>
                                                                </font>
                                                            </label>
                                                        </a>
                                                    </xsl:if>
                                                </xsl:if>
                                                <xsl:if test="(@isPerson = 'yes') and (@isLast = 'no')">
                                                    <img src="{$imagedir}menu_tree.gif" border="0" alt="Node" title="Node"/>
                                                    <img src="{$imagedir}person_16.gif" border="0" alt="Person" title="Person"/>&#160;
                                                    <label for="CBrief{@ID}-{@entity}">
                                                        <font class="uportal-channel-text">
                                                            <xsl:value-of select="@name"/>
                                                        </font>
                                                    </label>
                                                </xsl:if>
                                                <xsl:if test="(@isPerson = 'yes') and (@isLast = 'yes')">
                                                    <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                    <img src="{$imagedir}person_16.gif" border="0" alt="Person" title="Person"/>&#160;
                                                    <label for="CBrief{@ID}-{@entity}">
                                                        <font class="uportal-channel-text">
                                                            <xsl:value-of select="@name"/>
                                                        </font>
                                                    </label>
                                                </xsl:if>

                                                <xsl:if test="@isPerson = 'no'">
                                                    <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                                    <img src="{$imagedir}shadow_16.gif" border="0"   alt="Missing Person" title="Missing Person"/>&#160;
                                                    <font class="uportal-channel-error">No people in Group</font>
                                                </xsl:if>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </table>
            </td>
        </tr>
    </xsl:template>

    <xsl:template name="displayChildren">

        <xsl:param name="ID"/>
        <xsl:param name="name"/>

        <tr>
            <td width="75%">
                <xsl:if test="(count(ancestor-or-self::*)) = '3'">&#160;</xsl:if>
                <xsl:if test="(count(ancestor-or-self::*)) = '4'">&#160;
&#160;&#160;</xsl:if>
                <xsl:if test="(count(ancestor-or-self::*)) = '5'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                <xsl:if test="(count(ancestor-or-self::*)) = '6'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                <xsl:if test="(count(ancestor-or-self::*)) = '7'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                <xsl:if test="(count(ancestor-or-self::*)) = '8'">&#160;
&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</xsl:if>
                <xsl:choose>
                    <xsl:when test="@isLast = 'yes'">
                        <xsl:if test="@expand = 'yes'">
                            <img src="{$imagedir}menu_corner_minus.gif" border="0" alt="Last Node Open" title="Last Node Open"/>
                            <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                            <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={$ID}&amp;expand=yes">
                                <label for="CBriefChild{@ID}-{@entity}">
                                    <font class="uportal-channel-text">
                                        <xsl:value-of select="concat('',$name,'')"/>
                                    </font>
                                </label>
                            </a>
                        </xsl:if>
                        <xsl:if test="@expand = 'no'">
                            <xsl:if test="@leafnode = 'no'">
                                <img src="{$imagedir}menu_corner_plus.gif" border="0" alt="Last Node Closed" title="Last Node Closed"/>
                                <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={$ID}&amp;expand=yes">
                                    <label for="CBriefChild{@ID}-{@entity}">
                                        <font class="uportal-channel-text">
                                            <xsl:value-of select="concat('',$name,'')"/>
                                        </font>
                                    </label>
                                </a>
                            </xsl:if>
                            <xsl:if test="@leafnode = 'yes'">
                                <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                <label for="CBriefChild{@ID}-{@entity}">
                                    <font class="uportal-channel-text">
                                        <xsl:value-of select="concat('',$name,'')"/>
                                    </font>
                                </label>
                            </xsl:if>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="@expand = 'yes'">
                            <img src="{$imagedir}menu_tree_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                            <img src="{$imagedir}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>&#160;
                            <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={$ID}&amp;expand=yes">
                                <label for="CBriefChild{@ID}-{@entity}">
                                    <font class="uportal-channel-text">
                                        <xsl:value-of select="concat('',$name,'')"/>
                                    </font>
                                </label>
                            </a>
                        </xsl:if>
                        <xsl:if test="@expand = 'no'">
                            <xsl:if test="@leafnode = 'no'">
                                <img src="{$imagedir}menu_tree_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                <a href="{$baseActionURL}?action=PreviewTopic&amp;selected={$ID}&amp;expand=yes">
                                    <label for="CBriefChild{@ID}-{@entity}">
                                        <font class="uportal-channel-text">
                                            <xsl:value-of select="concat('',$name,'')"/>
                                        </font>
                                    </label>
                                </a>
                            </xsl:if>
                            <xsl:if test="(@leafnode = 'yes') and (@isLast = 'no')">
                                <img src="{$imagedir}menu_tree.gif" border="0" alt="Node" title="Node"/>
                                <img src="{$imagedir}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>&#160;
                                <label for="CBriefChild{@ID}-{@entity}">
                                    <font class="uportal-channel-text">
                                        <xsl:value-of select="@name"/>
                                    </font>
                                </label>
                            </xsl:if>
                            <xsl:if test="(@leafnode = 'yes') and (@isLast = 'yes')">
                                <img src="{$imagedir}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                <img src="{$imagedir}folder_closed_16.gif" border="0"   alt="Closed Folder" title="Closed Folder"/>&#160;
                                <label for="CBriefChild{@ID}-{@entity}">
                                    <font class="uportal-channel-text">
                                        <xsl:value-of select="@name"/>
                                    </font>
                                </label>
                            </xsl:if>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>

                <font class="uportal-channel-text">(DisplayChildre::is Leaf Node = <xsl:value-of select="@leafnode"/> | isLast = <xsl:value-of select="@isLast"/>)</font>
            </td>
            <td align="left">
                <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="CBriefChild{@ID}-{@entity}"/>
            </td>
        </tr>


        <xsl:for-each select="descendant::group">
            <xsl:call-template name="displayChildren">
                <xsl:with-param name="name" select="@name"/>
                <xsl:with-param name="ID" select="@ID"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
