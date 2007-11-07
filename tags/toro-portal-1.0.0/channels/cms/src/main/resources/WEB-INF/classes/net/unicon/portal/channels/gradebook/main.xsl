<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:import href = "common_main.xsl" />
 
<xsl:output method="html" indent="yes" />

<xsl:param name="subLinkParameter"></xsl:param>
<xsl:param name="showGradeColumn">Y</xsl:param>
<xsl:param name="ONSUBMIT"></xsl:param>

<!-- ######################################################################## -->

<xsl:template match="/">
	<!-- Start JavaScript -->
	<script type="text/javascript" language="JavaScript1.2">
		// create initialize method so that it can be initialized outside of page load
		initializeGradebookChannel = function()
		{
		    // no initialization needed
		}
	</script>
	<!-- End Javascript -->

    <!-- Apply Common Main templates -->
    <xsl:apply-imports />
    <!-- Apply catalog paging and searching -->
    <xsl:call-template name="catalog">
        <xsl:with-param name="catSearchFlag" select="'Y'"/>
    </xsl:call-template>

</xsl:template>


</xsl:stylesheet>
