<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
<!-- for all screens -->
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>
    <xsl:param name="sid">default</xsl:param>
    <xsl:param name="targetChannel">default</xsl:param>
    <xsl:param name="root">false</xsl:param>
    <xsl:param name="focusedChannel">
        <xsl:value-of select="$targetChannel" />
    </xsl:param>
    <xsl:param name="baseImagePath">media/net/unicon/portal/channels/rad</xsl:param>
    <xsl:param name="surveyImagePath">media/net/unicon/portal/channels/survey</xsl:param>
    <xsl:param name="backRoot" />
    <xsl:param name="back">default</xsl:param>
    <xsl:param name="goURL">default</xsl:param>
    <xsl:param name="doURL">default</xsl:param>
<!-- current date -->
    <xsl:param name="cur-date">default</xsl:param>
<!-- used only for navigation-bar -->
<!-- Set Variable Names for image links -->
    <xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
    <xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
    <xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
    <xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/flash/academus</xsl:variable>
    <xsl:variable name = "SPACER"><xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>

<xsl:template name="autoFormJS">
    <script language="JavaScript1.2" type="text/javascript" src="javascript/SurveyAuthor/autoForm.js"></script>
</xsl:template>

</xsl:stylesheet>

