<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- Include -->
	<xsl:import href="../global/global.xsl"/>
	<xsl:include href="../global/toolbar.xsl"/>

	<xsl:output method="html" indent="yes"/>

	<!-- parameters -->

	<xsl:param name="current_command"/>
	<xsl:param name="ID"/>
	<xsl:param name="addCommand"/>
	<xsl:param name="editCommand"/>
	<xsl:param name="deleteCommand"/>
	<xsl:param name="deleteConfirmationParam"/>

	<!-- permissions -->
	<xsl:template name="autoFormJS">
		<script language="JavaScript" type="text/javascript" src="javascript/NotesChannel/autoForm.js"></script>
	</xsl:template>

	<!-- Common -->
	<xsl:template name="links">
		<!--UniAcc: Layout Table -->
		<div class="portlet-toolbar-container">			
			<xsl:choose>
				<xsl:when test="$current_command = 'general'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">View Notes</xsl:with-param>
					</xsl:call-template>						
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">View Notes</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=general</xsl:with-param>
					</xsl:call-template>						
				</xsl:otherwise>
			</xsl:choose>		
			
			<xsl:choose>
				<xsl:when test="$current_command = 'add'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Add Note</xsl:with-param>
					</xsl:call-template>				
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Add Note</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=add</xsl:with-param>
					</xsl:call-template>				
				</xsl:otherwise>
			</xsl:choose>					
		</div>
		
	</xsl:template>
</xsl:stylesheet>