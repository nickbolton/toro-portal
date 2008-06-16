<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- parameters -->
    <xsl:param name="skin" select="'academus'"/>
    <xsl:param name="baseActionURL"/>
    <xsl:param name="targetChannel"/>
    <xsl:param name="pageCommand"/>

    <!-- Set Variable Names for image links -->
	<!-- Design Note: with the proposed future use of multiple themes as well as styles might necessitate the creation of a "theme" parameter that will be pass into the following variables as <xsl:value-of select="$theme"/> to replace "nested-tables" in the path. -->
    <xsl:variable name="SKIN_IMAGE_PATH">media/net/unicon/academusTheme/<xsl:value-of select="$skin"/>/images</xsl:variable>
    <xsl:variable name="CONTROLS_IMAGE_PATH">media/net/unicon/academusTheme/<xsl:value-of select="$skin"/>/images/icons</xsl:variable>
    <xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/academusTheme/<xsl:value-of select="$skin"/>/images/icons</xsl:variable>
    <xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/academusTheme/<xsl:value-of select="$skin"/>/media</xsl:variable>
    <xsl:variable name="SPACER"><xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>
	
</xsl:stylesheet>
