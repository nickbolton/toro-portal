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
  1    Channels  1.0         4/26/2002 5:35:16 PM Freddy Lopez
 $

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_SelectGroup</xsl:param>

    <xsl:variable name="imagepath">media/com/interactivebusiness/news/</xsl:variable>

    <xsl:template match="SelectGroupTopic">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <script language="JavaScript1.2" type="text/javascript">
            // This script prevents an open search by checking to see if the input is empty. It is called from the onsubmit of the form below.
            OpenSearchCheck = function() {
                if (window.validateForm &amp;&amp; document.getElementById("selectgroup").person_name.value == "") {
                    alert("The search field is empty; you must provide search criteria");
                    return false;
                } else {
                    return true;
                }
            }
            // Every below was added to support the copying of functions from one window to
            // another so that client-side optimizations would work
            NewsAdminFunctions = ["initializeNewsAdmin", "OpenSearchCheck"];
            // if channelFunctionsArray is already defined, just add to it
            if (window.channelFunctionsArray)
            {
                channelFunctionsArray[channelFunctionsArray.length] = NewsAdminFunctions;
            }
            // else create channelFunctionsArray with this as first entry
            else
            {
                channelFunctionsArray = [NewsAdminFunctions]; // create 2-D array
            }
            // create initialize method so that it can be initialized outside of page load
            initializeNewsAdmin = function()
            {
            }
            
            function checkForEnter(myInput,e) {
            	var keycode;
            	if(window.event) {
            		keycode = window.event.keyCode; }
            	else if (e) {
            		keycode = e.which; }
            	else	
            		return true;
            		
            	if( keycode == 13 ) {
            		window.validateForm = true;
            		document.getElementById('go').name = 'go';
            		myInput.form.submit();
            		return false;
            	}
            	else {
            		return true; }
            }
        </script>
        
        <h2 class="page-title">Create/Edit Topics</h2>
        
        <xsl:call-template name = "topic-workflow" />
        
        <div class="bounding-box1">
        
			<form action="{$baseActionURL}?action=PreviewTopic&amp;admin=yes" method="post" name="selectgroup" id="selectgroup" onsubmit="return OpenSearchCheck();">
				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="2" cellspacing="0" align="left" width="100%">
					<tr>
						<th align="left">Select all persons and groups who can create news articles for topic &quot;<xsl:value-of select="topicname"/>&quot;</th>
					</tr>
					<xsl:apply-templates select="error"/>
					<xsl:apply-templates select="previousSelection"/>
					<tr>
						<td class="table-content-single">
							<div class="uportal-channel-strong">Select Persons by search:</div>
							<img src="{$SPACER}" border="0" height="5" width="1" alt="" title=""/><br/>
							<label for="NewsAdmin-SelectGroupFindUserT1">Find user:</label>
							<input type="text" onkeypress="checkForEnter(this,event);" name="person_name" size="16" maxsize="20" class="uportal-input-text" id="NewsAdmin-SelectGroupFindUserT1"/>
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<input type="hidden" name="default" id="go" value="go" />
							<input type="submit" name="go" value="go" class="uportal-button" onclick="window.validateForm = true"/><br/>
							<img src="{$SPACER}" border="0" height="3" width="1" alt="" title=""/><br/>
							<div class="uportal-text-small">(search by user, first or last name)</div>
							<br/>
							<xsl:apply-templates select="name_search"/>
						</td>
					</tr>
					<tr>
						<td class="table-content-single">
							<!--UniAcc: Layout Table -->
							<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%" class="table-content">
								<xsl:apply-templates select="groups_list"/>
							</table>
						</td>
					</tr>
					<tr>
						<td class="table-light-single" style="text-align:center;">
							<input type="submit" name="back" value="Back" class="uportal-button" onclick="window.validateForm = false" title="To return to Enter Topic Details" />
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<input type="submit" name="next" value="Next" class="uportal-button" onclick="window.validateForm = false" title="To go to Preview Topic Properties" />
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=TopicSearch&amp;admin=yes'" title="To cancel create/editing this article topic and return to Manage Article Topics" />
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>


    <xsl:template match="error">
        <tr>
            <td align="center">
                <div class="uportal-channel-error">***ERROR ENCOUNTERED: No Group/Person was selected to share this topic. Use either the person search or group hierarchy to select Persons or Groups to share this topic.</div>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="previousSelection">
        <tr>
            <td class="table-content">
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <!-- If inside this template, then user is editing a folder -->
                    <input type="hidden" name="newfolder" value="false"/>

                    <xsl:for-each select="groupName">
                        <xsl:sort select="@entity"/>
                        <tr>
                            <td width="5%">
                                <input type="checkbox" name="{@ID}-{@entity}" value="checked" checked="checked" id="{@ID}-{@entity}"/>
                            </td>

                            <td width="5%">
                                <xsl:if test="@entity = '2'">
                                    <img src="{$imagepath}person_16.gif" border="0" alt="Person" title="Person"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                </xsl:if>
                                <xsl:if test="@entity = '3'">
                                    <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                </xsl:if>
                            </td>
                            <td width="70%">
                                <label for="{@ID}-{@entity}"><xsl:value-of select="@name"/></label>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="name_search">
        <div class="uportal-channel-strong">Search Results:</div>
        <img src="{$SPACER}" border="0" height="3" width="1" alt="" title=""/><br/>
        <div class="uportal-channel-small">Select checkboxes of desired persons to share the topic. Once checked, those persons will remain selected to share the topic. You may then continue searching.</div>
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
            <xsl:apply-templates select="nameNotFound"/>
            <xsl:apply-templates select="personName"/>
        </table>
    </xsl:template>

    <xsl:template match="nameNotFound">
        <tr>
            <td colspan="3" class="table-content-single">
                <img src="{$imagepath}shadow_16.gif" border="0" alt="Missing Person" title="Missing Person"/>
                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                <span class="uportal-channel-warning">User "<xsl:value-of select="@name"/>" was not found. Please try another search or use different search criteria.</span>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="personName">
        <tr class="uportal-background-light">
            <xsl:attribute  name = "class" >
                <xsl:choose>
                    <xsl:when test="position() mod 2 = 1">table-light</xsl:when>
                    <xsl:otherwise>table-content</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <td width="5%">
                <xsl:choose>
                    <xsl:when test="@selected = 'yes'">
                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" checked="checked" id="{@ID}-{@entity}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="{@ID}-{@entity}"/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td width="5%">
                <img src="{$imagepath}person_16.gif" border="0" alt="Person" title="Person"/>
                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
            </td>
            <td width="70%">
                <label for="{@ID}-{@entity}"><xsl:value-of select="@name"/></label>
            </td>
        </tr>
    </xsl:template>



    <xsl:template match="groups_list">
        <tr>
            <td>
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
                    <tr>
                        <td colspan="2">
                            <div class="uportal-channel-strong">Select Groups by hierarchy:</div>
                            <img src="{$SPACER}" border="0" height="5" width="1" alt="" title=""/><br/>
                        </td>
                    </tr>

                    <xsl:apply-templates select = "group">
                        <xsl:with-param name="depth">0</xsl:with-param>
                    </xsl:apply-templates>

                </table>
            </td>
        </tr>
    </xsl:template>

    <!-- template to handle rendering group node -->
    <xsl:template match="group">
        <xsl:param name="depth" />
        <xsl:param name="ancestorOpenPath"></xsl:param>
        <xsl:variable name = "islast">
            <xsl:choose>
                <xsl:when test="position()=last()">1</xsl:when>

                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <tr>
            <td width="5%">
                <input type="checkbox" name="{@ID}-{@entity}" value="checked" id="{@ID}-{@entity}"/>
            </td>
            <td>
                <xsl:if test = "$depth &gt; 0">
                    <xsl:call-template name = "indent" >
                        <xsl:with-param name="count" select="1" />
                        <xsl:with-param name="maxcount" select="$depth" />
                        <xsl:with-param name="ancestorOpenPath" select="$ancestorOpenPath" />
                    </xsl:call-template>
                </xsl:if>

                <xsl:choose>
                    <xsl:when test = "@ID='local.0' or @ID='0'">
                        <a href="{$baseActionURL}?action=SelectGroupTopic&amp;admin=yes&amp;selected={@ID}&amp;expand=yes" title="Display">
                            <xsl:if test="@expand = 'yes'">
                                <img src="{$imagepath}menu_root_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                                <img src="{$imagepath}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>
                            </xsl:if>
                            <xsl:if test="@expand = 'no'">
                                <img src="{$imagepath}menu_root_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                            </xsl:if>
                        </a>
                    </xsl:when>
                    <xsl:when test="@isLast='yes' or $islast='1'">
                        <xsl:if test="@expand = 'yes'">
                            <a href="{$baseActionURL}?action=SelectGroupTopic&amp;admin=yes&amp;selected={@ID}&amp;expand=yes" title="Display">
                                <img src="{$imagepath}menu_corner_minus.gif" border="0" alt="Last Node Open" title="Last Node Open"/>
                                <img src="{$imagepath}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>
                            </a>
                        </xsl:if>
                        <xsl:if test="@expand = 'no'">
                            <xsl:if test="@leafnode = 'no'">
                                <a href="{$baseActionURL}?action=SelectGroupTopic&amp;admin=yes&amp;selected={@ID}&amp;expand=yes" title="Display">
                                    <img src="{$imagepath}menu_corner_plus.gif" border="0" alt="Last Node Closed" title="Last Node Closed"/>
                                    <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                                </a>
                            </xsl:if>
                            <xsl:if test="@leafnode = 'yes'">
                                <img src="{$imagepath}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                            </xsl:if>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="@expand = 'yes'">
                            <a href="{$baseActionURL}?action=SelectGroupTopic&amp;admin=yes&amp;selected={@ID}&amp;expand=yes" title="Display">
                                <img src="{$imagepath}menu_tree_minus.gif" border="0" alt="Open Node" title="Open Node"/>
                                <img src="{$imagepath}folder_open_16.gif" border="0" alt="Open Folder" title="Open Folder"/>
                            </a>
                        </xsl:if>
                        <xsl:if test="@expand = 'no'">
                            <xsl:if test="@leafnode = 'no'">
                                <a href="{$baseActionURL}?action=SelectGroupTopic&amp;admin=yes&amp;selected={@ID}&amp;expand=yes" title="Display">
                                    <img src="{$imagepath}menu_tree_plus.gif" border="0" alt="Closed Node" title="Closed Node"/>
                                    <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                                </a>
                            </xsl:if>
                            <xsl:if test="(@leafnode = 'yes') and (@isLast = 'no' or  $islast='0')">
                                <img src="{$imagepath}menu_tree.gif" border="0" alt="Node" title="Node"/>
                                <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                            </xsl:if>
                            <xsl:if test="(@leafnode = 'yes') and (@isLast = 'yes' or $islast='1')">
                                <img src="{$imagepath}menu_corner.gif" border="0" alt="Last Node" title="Last Node"/>
                                <img src="{$imagepath}folder_closed_16.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                            </xsl:if>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
                <label for="{@ID}-{@entity}">&#160;<xsl:value-of select="@name"/></label>
                </td>
            </tr>
            <xsl:apply-templates select = "group">
                <xsl:with-param name="depth" select="$depth+1" />
                <xsl:with-param name="ancestorOpenPath" select="concat($ancestorOpenPath,$islast)" />
            </xsl:apply-templates>
    </xsl:template>

    <!-- template to create proper indentation of tree nodes -->
    <xsl:template name="indent">
        <xsl:param name="count" /><!-- current count within depth -->
        <xsl:param name="maxcount" /><!-- depth of element -->
        <xsl:param name="ancestorOpenPath" /><!-- a string comprised of 1's and 0's (i.e. a crude array) to know whether to display a blank image or a line image at each count within the depth -->
        <!-- For example, to represent the following tree:

            f1
            ()  f2
            ()  |   f3
            ()  |   ()  f4
            ()  f5

            where () is a blank, and | is a vertical line image

            the ancestorOpenPath for f3 is "10", the ancestorOpenPath for f4 is "101", while ancestorOpenPath for f2 and f5 is "1"
         -->
        <xsl:choose>
            <xsl:when test="substring($ancestorOpenPath,1,1)='0'">
                <img src="{$imagepath}menu_bar.gif" border="0" alt="" title=""/>
            </xsl:when>

            <xsl:otherwise>
                <img src="{$imagepath}transparent.gif" border="0" height="18" width="18" alt="" title=""/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test = "$count &lt; $maxcount">
            <xsl:call-template name = "indent" >
                <xsl:with-param name="count" select="$count+1" />
                <xsl:with-param name="maxcount" select="$maxcount" />
                <xsl:with-param name="ancestorOpenPath" select="substring($ancestorOpenPath,2,(number($maxcount)-number($count)))" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>


</xsl:stylesheet>
