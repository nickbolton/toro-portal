<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:variable name="ICON_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
	
	<!-- *******   GENERIC   *********  
	<xsl:template name="channel-link-generic">
		<xsl:param name="title"/>
		<xsl:param name="URL"/>
		<xsl:param name="imagePath"/>
		<xsl:param name="target"/>

		<xsl:choose>
			<xsl:when test="$URL">
				<div class="tool">
					<a href="{$URL}" title="{$title}">
						<xsl:if test="$target">
							<xsl:attribute name="target">
								<xsl:value-of select="$target"/>
							</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="$title"/>
					</a>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="tool selected-tool">
					<span>
						<xsl:value-of select="$title"/>
					</span>
				</div>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	-->

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
	
	<!-- *******   GENERIC 2 - Page Links  *********  -->
	<xsl:template name="channel-link-generic2">
		<xsl:param name="title"/>
		<xsl:param name="URL"/>
		<xsl:param name="imagePath"/>

		<xsl:choose>
			<xsl:when test="$URL">
				<div class="tool2">
					<a href="{$URL}" title="{$title}">
						<!--
						<img src="{$ICON_PATH}/{$imagePath}.gif" class="toolbar-img" width="16" height="16"  border="0" alt="{$title}" title="{$title}"/>						
						-->
						<xsl:value-of select="$title"/>
					</a>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="tool2 selected-tool2">
					<span>
						<!-- 
						<img src="{$ICON_PATH}/{$imagePath}.gif" class="toolbar-img" width="16" height="16" alt="{$title}" title="{$title}"/>
						-->
						<xsl:value-of select="$title"/>
					</span>
				</div>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>	

	<!-- *******   Calendar - Top Links  *********  -->
	<xsl:template name="channel-link-calendar">
		<xsl:param name="title"/>
		<xsl:param name="URL"/>
		
		<div class="calendar-tool">
			<table class="tool-table" cellpadding="0" cellspacing="0">
				<tr>
					<xsl:choose>
						<xsl:when test="$URL">
							<td>&#160;</td>
							<td class="calendar-tool-body">								
								<div>
									<a href="{$URL}" title="{$title}" class="calendar-top-link">
										<xsl:value-of select="$title"/>
									</a>
								</div>
							</td>
							<td>&#160;</td>
						</xsl:when>
						<xsl:otherwise>
							<td class="calendar-tool-left calendar-selected-tool">&#160;</td>
							<td class="calendar-tool-body calendar-selected-tool">								
								<div>
									<xsl:value-of select="$title"/>
								</div>
							</td>
							<td class="calendar-tool-right calendar-selected-tool">&#160;</td>						
						</xsl:otherwise>
					</xsl:choose>
				</tr>
			</table>
		</div>
		
	</xsl:template>

	<!-- *******   SEARCH BOX   *********  -->
	<xsl:template name="channel-link-search-box">
		<xsl:param name="title"/>
		<xsl:param name="submitURL"/>
		<xsl:param name="value">search</xsl:param>

		<input align="absmiddle" class="uportal-input-text" maxlength="80" name="search" value="{$value}" size="10" type="text"/>
		<input type="button" class="uportal-button" onclick="{$submitURL}" value="{$title}"/>
		
	</xsl:template>
	
	<!-- **** Example Text *******
	
      	<div class="portlet-toolbar-container">
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title"></xsl:with-param>
				<xsl:with-param name="imagePath">channel_admin_base</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title"></xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$goURL"/></xsl:with-param>
				<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
			</xsl:call-template>
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title"></xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$goURL"/></xsl:with-param>
				<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>
			</xsl:call-template>
	  	</div>
	  	
	***************************** -->	  	
	
</xsl:stylesheet>
