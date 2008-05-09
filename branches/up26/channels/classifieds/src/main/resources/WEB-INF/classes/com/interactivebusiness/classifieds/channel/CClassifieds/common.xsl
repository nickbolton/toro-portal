<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>

    <xsl:param name="current_command"/>
    <xsl:param name="offeringName"/>
    <xsl:param name="curriculumID"/>
    <xsl:param name="criteria"></xsl:param>
    <xsl:param name="isNew">false</xsl:param>
    <!-- Permissions -->
    <xsl:param name="addCurriculum"/>
    <xsl:param name="removeCurriculum"/>

    <!-- Set Variable Names for image links -->
    <xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
    <xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
    <xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
    <xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/flash/academus</xsl:variable>
    <xsl:variable name="SPACER">
        <xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>
    <!-- imagedir differs from other image paths in that it is specific to the classifieds -->
    <xsl:variable name="imagedir">
        <xsl:text>media/com/interactivebusiness/classifieds</xsl:text>
    </xsl:variable>

	<xsl:template name="autoFormJS">
	        <script language="JavaScript" type="text/javascript" src="javascript/CClassifieds/autoForm.js"/>
	</xsl:template>

    <!-- Common -->
    <xsl:template name="links">
    </xsl:template>
    
	<!-- *******   GENERIC   *********  -->
	<xsl:template name="channel-link-generic">
		<xsl:param name="title"/>
		<xsl:param name="URL"/>
		<xsl:param name="imagePath"/>
		<xsl:param name="target"/>
		
		<div class="tool">
			<table class="tool-table" cellpadding="0" cellspacing="0">
				<tr>
					<xsl:choose>
						<xsl:when test="$URL">
							<td></td>
							<td class="tool-tab-body">								
								<div>
									<a href="{$URL}" title="{$title}">
										<xsl:if test="$target">
											<xsl:attribute name="target">
												<xsl:value-of select="$target"/>
											</xsl:attribute>
										</xsl:if>									
										<!--
										<img src="{$imagedir}/{$imagePath}.gif" class="toolbar-img" width="16" height="16"  border="0" alt="{$title}" title="{$title}"/>
										-->
										<xsl:value-of select="$title"/>
									</a>
								</div>
							</td>
							<td></td>
						</xsl:when>
						<xsl:otherwise>
							<td class="tool-tab-left">&#160;</td>
							<td class="tool-tab-body selected-tool">								
								<div>
									<xsl:value-of select="$title"/> 
								</div>
							</td>
							<td class="tool-tab-right">&#160;</td>						
						</xsl:otherwise>
					</xsl:choose>
				</tr>
			</table>
		</div>
		
	</xsl:template>
	
	
</xsl:stylesheet>
