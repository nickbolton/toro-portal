<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	
	<!-- This stylesheet is used as a principal stylesheet of several stylesheet modules, reuseable components that are called and referenced from multiple channels.

	Parameters and variables need to be set before including the other XSL templates so that they can render properly as they make use of these declared params and varaibles. -->
	
	<!-- Imports -->
	<xsl:import href="params-vars.xsl"/><!-- Parameters and varaibles -->
	<xsl:import href="scripts-utils.xsl"/><!-- JavaScript and utilities -->
	<xsl:import href="choice-decision.xsl"/><!-- Choice and decision collection transforms (Penelope) -->
	<xsl:import href="catalog.xsl"/><!-- Paging and page-turning -->
	<xsl:import href="theme-style.xsl"/><!-- Virtuoso theme and style selection -->
	
	<xsl:output method="html" indent="yes"/>
	<xsl:strip-space elements="*"/>
	
</xsl:stylesheet>