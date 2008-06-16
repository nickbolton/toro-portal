<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- Import the bookmark tree stylesheet -->
    <xsl:import href="bookmark_tree_html.xsl"/>

    <xsl:import href="common.xsl"/>

    <!-- Make sure that XHTML is being output to facilitate caching -->
    <xsl:output method="xml" indent="yes"/>

    <!-- Take the baseActionURL and the location of the images as parameters -->
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="imagesURL">media/net/unicon/channels/bookmarks/</xsl:param>

    <!-- Find out whether you are adding a folder or a bookmark -->
    <xsl:param name="EditMode">AddBookmark</xsl:param>

    <xsl:template match="/">
        <xsl:call-template name="autoFormJS"/>

		<div class="portlet-toolbar-container">
			<xsl:choose>
				<xsl:when test="$EditMode='AddBookmark'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Add Bookmark</xsl:with-param>
					</xsl:call-template>	
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Add Bookmark</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=AddBookmark</xsl:with-param>
					</xsl:call-template>				
				</xsl:otherwise>
			</xsl:choose>

			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Remove Bookmark</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=DeleteBookmark</xsl:with-param>
			</xsl:call-template>
			
			<xsl:choose>
				<xsl:when test="$EditMode='AddFolder'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Add Folder</xsl:with-param>
					</xsl:call-template>	
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Add Folder</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=AddFolder</xsl:with-param>
					</xsl:call-template>				
				</xsl:otherwise>
			</xsl:choose>
		
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Remove Folder</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=DeleteFolder</xsl:with-param>
			</xsl:call-template>	
		</div>
		<div class="page-title">
			<xsl:choose>
				<xsl:when test="$EditMode='AddFolder'">Add Folder</xsl:when>
				<xsl:otherwise>Add Bookmark</xsl:otherwise>
			</xsl:choose>
		</div>
        <form action="{$baseActionURL}?command={$EditMode}" method="post" onsubmit="return(checkBookmarkFormSubmit(this));">
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <th class="th-top-single">1. Select Folder for Bookmark</th>
                </tr>
            </table>
            <xsl:call-template name="BookmarkTree">
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$EditMode"/>
                </xsl:with-param>
            </xsl:call-template>
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <th class="th-top-single">2. Add New <xsl:value-of select="substring-after($EditMode,'Add')"/></th>
                </tr>
            </table>
            <xsl:choose>
                <xsl:when test="$EditMode='AddFolder'">
                    <!--UniAcc: Layout Table -->
                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-AddFolderName">Folder Name:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <input type="text" name="FolderTitle" class="text" id="CBookmark-AddFolderName" maxlength="80"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-nav" colspan="2">
                            	<!-- This form keys on SubmitButton instead of submitValue -->
                                <input type="submit" name="SubmitButton" value="Add" onclick="this.form.isCancel=false;" class="uportal-button"/>
                                <input type="submit" name="SubmitButton" value="Cancel" onclick="this.form.isCancel=true;" class="uportal-button"/>
                            </td>
                        </tr>
                    </table>
                </xsl:when>
                <xsl:when test="$EditMode='AddBookmark'">
                    <!--UniAcc: Layout Table -->
                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-AddBookmarkTitleT1">Bookmark Title:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <input type="text" name="BookmarkTitle" class="text" id="CBookmark-AddBookmarkTitleT1" maxlength="80"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-AddBookmarkurlT1">URL:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <input type="text" name="BookmarkURL" class="text" value="http://" id="CBookmark-AddBookmarkurlT1"/>
                                <br/>
                                <span class="uportal-text-small">You must include &quot;http://&quot; for the URL to be valid.</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-AddBookmarkDescTA1">Description:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <textarea rows="5" cols="20" name="BookmarkDescription" class="text" id="CBookmark-AddBookmarkDescTA1"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-nav" colspan="2">
                            	<!-- This form seems to key on submitValue instead of SubmitButton -->
                                <input type="submit" name="SubmitButton" value="Add" onclick="this.form.isCancel=false;" class="uportal-button"></input>
                                <input type="submit" name="SubmitButton" value="Cancel" onclick="this.form.isCancel=true;" class="uportal-button"></input>
                            </td>
                        </tr>
                    </table>
                </xsl:when>
            </xsl:choose>
        </form>
    </xsl:template>
</xsl:stylesheet>
