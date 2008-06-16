<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="baseActionURL">no parameter passed</xsl:param>
    <xsl:param name="mediaPath">media/net/unicon/channels/bookmarks</xsl:param>
    <xsl:param name="imagePath">media/net/unicon/portal/layout/AL_TabColumn/academusTheme/academus/controls</xsl:param>
	<xsl:param name="ParentNodeId"></xsl:param>

    <xsl:template match="xbel" name="BookmarkTree">
        <xsl:param name="TreeMode">View</xsl:param>
        <!--UniAcc: Layout Table -->
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <xsl:if test="$TreeMode='AddFolder' or $TreeMode='AddBookmark' or $TreeMode='EditBookmark'">
                <tr valign="top">
                    <td class="table-light-left">
						 <xsl:choose>
		                    <xsl:when test="$ParentNodeId=''">
								<input type="radio" name="FolderRadioButton" value="RootLevel" checked="true" class="radio" id="CBookmark-TreeNewTopLevelR1"/>
							</xsl:when>
							<xsl:otherwise>
                       			 <input type="radio" name="FolderRadioButton" value="RootLevel" class="radio" id="CBookmark-TreeNewTopLevelR1"/>
							</xsl:otherwise>
						</xsl:choose>				        
                    </td>
                    <td width="100%" class="table-content-right">
                        <label for="CBookmark-TreeNewTopLevelR1">
                            New Top Level 
                            <xsl:value-of select="substring-after($TreeMode,'Add')"/>
                        </label>
                    </td>
                </tr>
            </xsl:if>
            <xsl:apply-templates select="/xbel/folder">
                <xsl:sort select="title"/>
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$TreeMode"/>
                </xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates select="/xbel/bookmark">
                <xsl:sort select="title"/>
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$TreeMode"/>
                </xsl:with-param>
            </xsl:apply-templates>
        </table>
    </xsl:template>

    <xsl:template match="folder">
        <xsl:param name="TreeMode">View</xsl:param>
        <tr align="left" valign="top">
            <!-- Display a radio button or checkbox if in edit mode -->
            <td valign="top" class="table-light-left">
                <xsl:choose>
                    <xsl:when test="$TreeMode='AddFolder' or $TreeMode='AddBookmark' or $TreeMode='EditBookmark'">
                        <input type="radio" name="FolderRadioButton" value="{@id}" class="radio" id="CBookmark-AddFolder{@id}">
                        	<xsl:if test = "@id = $ParentNodeId">
                        		<xsl:attribute name = "checked" >checked</xsl:attribute>
                        	</xsl:if>
                        </input>
                    </xsl:when>
                    <xsl:when test="$TreeMode='DeleteFolder'">
                        <input type="checkbox" name="FolderCheckbox#{@id}" class="radio" id="CBookmark-FolderCheckbox#{@id}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <img src="{$mediaPath}/transparent.gif" border="0" width="1" height="1" alt="" title=""/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td valign="top" class="table-content-right">
                <!-- Indent the folder -->
                <img src="{$mediaPath}/transparent.gif" width="{(count(ancestor::*)-1) * 4 + (count(ancestor::*)-1) * 16}" height="16" alt="" title=""/>
                <!-- Display an open or closed folder icon and the folder title -->
                <xsl:choose>
                    <xsl:when test="@folded='yes'">
                        <a href="{$baseActionURL}?command=unfold&amp;ID={@id}">
                            <xsl:attribute name="title">
                                Click here to open&#160;&amp;quot;<xsl:value-of select="title"/>&amp;quot;&#160;folder.
                            </xsl:attribute>
                            <img src="{$mediaPath}/folded_yes.gif" border="0" alt="Closed Folder" title="Closed Folder"/>
                            <img src="{$mediaPath}/transparent.gif" border="0" width="4" height="16" alt="" title=""/>
                        </a>
                        <label>                        
                            <xsl:attribute  name = "for" >
                                <xsl:choose>
                                    <xsl:when test="$TreeMode='AddFolder' or $TreeMode='AddBookmark'">CBookmark-AddFolder<xsl:value-of select="@id" /></xsl:when>
                                    <xsl:when test="$TreeMode='DeleteFolder'">CBookmark-FolderCheckbox#<xsl:value-of select="@id" /></xsl:when>                                                              <xsl:otherwise></xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <strong class="uportal-channel-text">
                                <xsl:value-of select="title"/>
                            </strong>
                        </label>                        
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="{$baseActionURL}?command=fold&amp;ID={@id}">
                            <xsl:attribute name="title">Click here to close&#160;&amp;quot;<xsl:value-of select="title"/>&amp;quot;&#160;folder.</xsl:attribute>
                            <img src="{$mediaPath}/folded_no.gif" border="0" alt="Open Folder" title="Open Folder"/>
                            <img src="{$mediaPath}/transparent.gif" border="0" width="4" height="16" alt="" title=""/>
                        </a>
                        <label>                        
                            <xsl:attribute  name = "for" >
                                <xsl:choose>
                                    <xsl:when test="$TreeMode='AddFolder' or $TreeMode='AddBookmark'">CBookmark-AddFolder<xsl:value-of select="@id" /></xsl:when>
                                    <xsl:when test="$TreeMode='DeleteFolder'">CBookmark-FolderCheckbox#<xsl:value-of select="@id" /></xsl:when>                                                              <xsl:otherwise></xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <strong class="uportal-channel-text">
                                <xsl:value-of select="title"/>
                            </strong>
                        </label>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
        <xsl:if test="$TreeMode != 'View'">
        </xsl:if>
        <!-- Recurse through the subtrees if the folder is open -->
        <xsl:if test="@folded='no'">
            <xsl:apply-templates select="folder">
                <xsl:sort select="title"/>
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$TreeMode"/>
                </xsl:with-param>
            </xsl:apply-templates>
            <xsl:apply-templates select="bookmark">
                <xsl:sort select="title"/>
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$TreeMode"/>
                </xsl:with-param>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    <xsl:template match="bookmark">
        <xsl:param name="TreeMode">View</xsl:param>
        <tr align="left" valign="top">
            <!-- Display a checkbox if in edit mode -->
            <td valign="top" class="table-light-left">
                <xsl:choose>
                    <xsl:when test="$TreeMode='DeleteBookmark'">
                        <input type="checkbox" name="BookmarkCheckbox#{@id}" class="radio" id="BookmarkCheckbox#{@id}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <img src="{$mediaPath}/transparent.gif" border="0" width="1" height="1" alt="" title=""/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td valign="top" class="table-content-right">
                <!--UniAcc: Layout Table -->
                <table>
                    <tr>
                        <td rowspan="2">
                            <!-- Indent the bookmark -->
                            <img src="{$mediaPath}/transparent.gif" width="{(count(ancestor::*)-1) * 4 + (count(ancestor::*)-1) * 16}" height="16" alt="" title=""/>
                        </td>
                        <td class="uportal-text" valign="top">
                            <!-- Bookmark name -->
                                <a href="{@href}" target="Bookmark">
                                    <xsl:attribute name="title">Click here to view&#160;&quot;<xsl:value-of select="title"/>&quot;&#160;in a new browser.</xsl:attribute>
                                    <xsl:attribute name="target">
                                        <xsl:value-of select="title"/>
                                    </xsl:attribute>
                                    <img src="{$mediaPath}/bookmark.gif" border="0" alt="Bookmark:" title="Bookmark:"/>
                                    <img src="{$mediaPath}/transparent.gif" border="0" width="4" height="16" alt="" title=""/>
                                    <xsl:value-of select="title"/>
                                </a>
								<xsl:choose>
									<xsl:when test="$TreeMode='EditBookmark'"></xsl:when>
									<xsl:otherwise>
										<a href="{$baseActionURL}?command=EditBookmark&amp;ID={@id}" title="Edit">
											<img src="{$mediaPath}/transparent.gif" border="0" width="4" height="1" alt="" title=""/>
											<img border="0" src="{$imagePath}/channel_edit_base.gif" align="absmiddle" name="bookmarkEditImage{@id}" id="bookmarkEditImage{@id}" alt="Edit" title="Edit" />
										</a>								
									</xsl:otherwise>
								</xsl:choose>	
                        </td>
                    </tr>
                    <tr>
                        <td class="uportal-text-small">
                            <!-- Bookmark description -->
                            <label>
                                <xsl:attribute  name = "for" >
                                    <xsl:choose>
                                        <xsl:when test="$TreeMode='DeleteBookmark'">BookmarkCheckbox#<xsl:value-of select="@id" /></xsl:when>
                                          <xsl:otherwise></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:value-of select="desc"/>
                            </label>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <xsl:if test="$TreeMode != 'View'">
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
