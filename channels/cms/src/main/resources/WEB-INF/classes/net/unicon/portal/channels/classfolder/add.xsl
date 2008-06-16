<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
        <xsl:call-template name="links"/>
        <xsl:call-template name="autoFormJS"/>
        <xsl:call-template name="addElement"/>
        <!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
    </textarea>
     -->
    </xsl:template>
    <!--############ Add Element ############ -->
    <xsl:template name="addElement">
<form name="ResourcesForm" action="{$baseActionURL}?command=s-add&amp;nodeid={$nodeid}" enctype="multipart/form-data" method="post" onsubmit="return validator.applyFormRules(this, new ResourcesRulesObject())">
            <!-- UniAcc: Data Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <!--command s-add redirects to main -->
                <!--
                <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
                <input type="hidden" name="command" value="s-add"/>
                <input type="hidden" name="nodeid" value="{$nodeid}"/>
                -->
                <tr>
                    <th colspan="2" class="th-top-single" id="AddElement">
                        Add Element
                    </th>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right">Location:</td>
                    <td class="table-content-right">
                        <xsl:choose>
							<xsl:when test="$nodename != ''">
								<xsl:value-of select="$nodename"/>
							</xsl:when>
							<xsl:otherwise>
								root level
							</xsl:otherwise>
						</xsl:choose>
                    </td>
                </tr>
                <!-- Upload of a file that was too large failed.  
                    Therefore, indicate this and let them try again -->
                <xsl:if test="$upload_status = 'FAILURE'">
                    <tr>
                        <td colspan="2" class="th-top-single" align="center">
                           The previous file exceeded the file size limit of 
                           <xsl:value-of select="$max_file_size"/>.<br/>
                           Please, choose a file that is below this size limit. 
                        </td>
                    </tr>
                </xsl:if>
                <tr>
                    <!-- the match folders(above) cycles through each folder...the select statement (below) retrieves the name from the folder with the 
            matching oid -This can be accomplished using the parameter nodename, but this current way functions so long as there is the full xml 
            returned to the add.xsl-->
                    <td class="table-light-left" style="text-align:right" valign="top" headers="AddElement" id="new" width="25%">
                        Type:
                    </td>
                    <td class="table-content-right" headers="AddElement" id="elements" width="75%">
                        <!-- UniAcc: Data Table -->
                        <table>
                            <tr>
                                <td headers="AddElement new element">
                                    <input type="radio" name="type" checked="checked" value="folder" id="rrcar1"/>
                                </td>
                                <td headers="AddElement new element">
                                    <span class="uportal-text">
                                        <label for="rrcar1">Folder</label>
                                    </span>
                                </td>
                                <td></td>
                            </tr>
                            <tr>
                                <td colspan="3"><br/></td>
                            </tr>
                            <tr>
                                <td  headers="AddElement new element">
                                    <input type="radio" name="type" value="url" onclick="document.ResourcesForm.hyperlink.focus();" id="rrcar2"/>
                                </td>
                                <td  headers="AddElement new element">
                                    <span class="uportal-text">
                                        <label for="rrcar2">URL</label>
                                    </span>
                                </td>
                                <td headers="AddElement new element">
                                    <input type="text" class="text" size="40" value="http://" name="hyperlink" onchange="document.ResourcesForm.type[1].checked=true;" id="rrcat1"/>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3"><br/></td>
                            </tr>
                            <tr>
                                <td headers="AddElement new element">
                                    <input type="radio" name="type" value="file" onclick="document.ResourcesForm.uploadedFile.focus();" id="rrcar3"/>
                                </td>
                                <td headers="AddElement new element">
                                    <span class="uportal-text">
                                        <label for="rrcar3">File</label>
                                    </span>
                                </td>
                                <td headers="AddElement new element">
                                    <input type="file" style="text-align: left;" name="uploadedFile" onchange="document.ResourcesForm.type[2].checked=true;" id="rrcaf1"/><br/>
                                    <span class="uportal-channel-warning"><xsl:value-of select="$max_file_size"/> maximum file size</span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3"><br/></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right" valign="top" headers="AddElement" id="title" width="25%">
                        <label for="rrcat2">Title:</label>
                    </td>
                    <td class="table-content-right" headers="AddElement title" width="75%">
                        <input type="text" class="text" size="55" maxlength="254" name="name" id="rrcat2"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right" valign="top" headers="AddElement" id="desc" width="25%">
                        <label for="rrcata1">Description:</label>
                    </td>
                    <td class="table-content-right" headers="AddElement desc" width="75%">
                        <textarea name="description" cols="40" id="rrcata1"></textarea>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" style="text-align:center">
                        <input class="uportal-button" type="submit" value="Submit" title="Click to add element and return to viewing all Related Resources."/>
                        &#032;&#032;&#032;&#032;
                        <input class="uportal-button"  type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel and return to viewing all Related Resources."/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
