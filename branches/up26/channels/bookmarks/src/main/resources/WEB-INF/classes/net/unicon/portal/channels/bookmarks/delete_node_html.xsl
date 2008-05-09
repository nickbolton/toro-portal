<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:import href="bookmark_tree_html.xsl"/>
	<xsl:include href="common.xsl"/>
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="imagesURL">media/net/unicon/channels/bookmarks/</xsl:param>

    <!-- This parameter tells the stylesheet which nodes to put radio buttons next to -->
    <xsl:param name="EditMode">DeleteBookmark</xsl:param>

    <xsl:template match="/">
    
		<div class="portlet-toolbar-container">
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Add Bookmark</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=AddBookmark</xsl:with-param>
			</xsl:call-template>
			
			<xsl:choose>
				<xsl:when test="$EditMode='DeleteBookmark'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Remove Bookmark</xsl:with-param>
					</xsl:call-template>	
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Remove Bookmark</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=DeleteBookmark</xsl:with-param>
					</xsl:call-template>	
				</xsl:otherwise>
			</xsl:choose>

			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Add Folder</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=AddFolder</xsl:with-param>
			</xsl:call-template>
			
			<xsl:choose>
				<xsl:when test="$EditMode='DeleteFolder'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Remove Folder</xsl:with-param>
					</xsl:call-template>	
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Remove Folder</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=DeleteFolder</xsl:with-param>
					</xsl:call-template>				
				</xsl:otherwise>
			</xsl:choose>
		</div>
		
		<div class="page-title">
			<xsl:choose>
				<xsl:when test="$EditMode='DeleteFolder'">Remove Folders</xsl:when>
				<xsl:otherwise>Remove Bookmarks</xsl:otherwise>
			</xsl:choose>
		</div>
		
        <form action="{$baseActionURL}?command={$EditMode}" method="post">
            <!--UniAcc: Layout Table -->
                       
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <th class="th-top-single">&#160;</th>
                </tr>
            </table>
            <xsl:call-template name="BookmarkTree">
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$EditMode"/>
                </xsl:with-param>
            </xsl:call-template>
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <td class="table-content-single">
                        Please select the
                        <xsl:choose>
                            <xsl:when test="$EditMode='DeleteBookmark'">bookmarks</xsl:when>
                            <xsl:when test="$EditMode='DeleteFolder'">folders</xsl:when>
                        </xsl:choose>
                        you wish to delete.
                    </td>
                </tr>
                <tr>
                    <td class="table-light-single" style="text-align:center;">
                    	<!-- This form keys on SubmitButton instead of submitValue -->
						<input type="hidden" name="SubmitButton" value="Delete"/>
                        <input type="submit" name="submitValue" value="Delete" onclick="this.form.SubmitButton.value=this.value;" class="uportal-button"/>
                        <input type="submit" name="submitValue" value="Cancel" onclick="this.form.SubmitButton.value=this.value;" class="uportal-button"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
