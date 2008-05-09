<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:import href="bookmark_tree_html.xsl" />
	<xsl:include href="common.xsl"/>
    <xsl:output method="xml" indent="no" />

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="mediaPath">media/net/unicon/channels/bookmarks</xsl:param>

    <xsl:template match="/">
    	

		<div class="portlet-toolbar-container">
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Add Bookmark</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=AddBookmark</xsl:with-param>
			</xsl:call-template>	

			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Remove Bookmark</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=DeleteBookmark</xsl:with-param>
			</xsl:call-template>		

			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Add Folder</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=AddFolder</xsl:with-param>
			</xsl:call-template>		

			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Remove Folder</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL" />?command=DeleteFolder</xsl:with-param>
			</xsl:call-template>	
		</div>
        <div class="page-title">
        	View Bookmarks
        </div>
        
        <xsl:call-template name="BookmarkTree" />
        <!--UniAcc: Layout Table -->

    </xsl:template>
    
    <xsl:template name="portlet-toolbar">
	    <xsl:param name="toolbar-link-href" />
	    <xsl:param name="toolbar-link-name" />
	    <xsl:param name="toolbar-link-image" />
	    
	    	<a href="{$toolbar-link-href}">
            	<xsl:value-of select="$toolbar-link-name" /><img src="{$toolbar-link-image}" border="0" alt="{$toolbar-link-name}" title="{$toolbar-link-name}" />
            </a>
	    
	</xsl:template>
    
</xsl:stylesheet>
