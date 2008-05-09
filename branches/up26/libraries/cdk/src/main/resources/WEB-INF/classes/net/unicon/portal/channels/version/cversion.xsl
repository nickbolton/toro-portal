<?xml version="1.0" encoding="utf-8"?>

<!-- ========== LEGAL, NOTES, AND INSTRUCTIONS ========== -->
<!--
Copyright (c) 2006 Unicon, Inc.  All rights reserved.
Any reproduction or re-use of this theme is governed by the End User License Agreement between Unicon, Inc. and the purchasing institution.

Author: Gary Thompson, gary@unicon.net
Version $LastChangedRevision$

Description: Stylesheet for version number reporting channel.
-->
<!-- ========== END LEGAL, NOTES, AND INSTRUCTIONS ========== -->


<!-- ========== STYLESHEET ========== -->
<!-- Defines this document as an XSL stylesheet, conforming to the XSL 1.0 specification and XSL namespace as defined in the reference http://www.w3.org/1999/XSL/Transform. Final output will be HTML (as specified in the output:method attribute) and the indent attribute set to "yes" simply attempts to keep the HTML hierarchical structure. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


	<!-- ========== VARAIBLES AND PARAMETERS ========== -->
	<!--
	The only parameter used by this stylesheet is the version string.
	 -->
    <xsl:param name="version" select="unknown"/>


	<!-- ========== END VARAIBLES AND PARAMETERS ========== -->


	<!-- ========== DEFINE OUTPUT TYPE (XML/HTML) ========== -->
	<!-- Using XML as output type for XHTML compliance -->
	<xsl:output method = "xml"  version="1.0" encoding="utf-8" omit-xml-declaration="yes" indent="yes"  />
	<!-- ========== END DEFINE OUTPUT TYPE (XML/HTML) ========== -->


<!-- ========== TEMPLATES ========== -->
<!--
XSL templates defined in this document for the purpose of generating output. Templates defined below may reference other templates defined in this document as well as templates defined in imported documents (refer to the above section, IMPORTED TEMPLATES).
 -->

    <!-- ========== TEMPLATE: ROOT ========== -->
	<!-- This template was created to refine a few problematic places in the workflow. What it does is load a blank html page (defined in the following template) and use javascript to auto-submit a url that is passed along in a param. Conditions for utilizing this redirect are defined below in the choose statement. -->
    <xsl:template match="/">
        <span><xsl:value-of select="$version"/></span>
    </xsl:template>
	<!-- ========== END TEMPLATE: ROOT ========== -->


<!-- ========== END TEMPLATES ========== -->

</xsl:stylesheet>
<!-- ========== END STYLESHEET ========== -->
