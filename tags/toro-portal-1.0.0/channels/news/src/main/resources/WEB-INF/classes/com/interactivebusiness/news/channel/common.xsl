<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>

    <!-- Permissions -->
    <xsl:param name="mayManageArticles" />
    <!--<xsl:variable name = "mayManageArticles" select = "'false'" /> -->
    <xsl:param name="mayManageTopics" />
    
    <!-- Set Variable Names for image links -->
    <xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
    <xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
    <xsl:variable name="SPACER"><xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>
    <!-- imagedir differs from other image paths in that it is specific to the news -->
    <xsl:variable name="imagedir">
        <xsl:text>media/com/interactivebusiness/news</xsl:text>
    </xsl:variable>

	<xsl:template name="autoFormJS">
	        <script language="JavaScript" type="text/javascript" src="javascript/NewsAdmin/autoForm.js"/>
	</xsl:template>

    <!-- Common -->
    <xsl:template name="links">
    </xsl:template>
    
    <!-- Debug -->
    <xsl:template name="debug">
    	<!-- Page name -->
		<h1><xsl:value-of select="$pageName" /></h1>
		<!-- XML dump per page -->
		<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea>
        <!-- Parameter Check -->
        <p>|mayManageArticles:<xsl:value-of select="$mayManageArticles" />|</p>
        <p>|mayManageTopics:<xsl:value-of select="$mayManageTopics" />|</p>
    </xsl:template>
    
    <!-- Default wrapper -->
    <xsl:template match="/">
    	<!-- Uncomment line below to get debug info -->
		<!--<xsl:call-template name = "debug" /> -->
		<div class="CampusNewsChannel CampusNewsChannel_{$pageName}">
			<xsl:apply-templates />
		</div>
    </xsl:template>
        
	<!-- *******   GENERIC   *********  -->
	<xsl:template name="channel-toolbar-generic">
		
        <xsl:choose>
        	<xsl:when test="$pageName = 'News_Main' or $pageName = 'Subscribe' or $pageName = 'Saved_Subscribed' or $pageName = 'View_Full_Article'">
		    	<div class="portlet-toolbar-container">
					<xsl:call-template name="tab1">
						<xsl:with-param name="selected" select="self::node()" />
					</xsl:call-template>
					<xsl:call-template name="tab2"></xsl:call-template>
		        </div>
		        
		        <xsl:call-template name="article-toolbar-generic"></xsl:call-template>
		        
        	</xsl:when>
          
        	<xsl:otherwise>
		    	<div class="portlet-toolbar-container">
					<xsl:call-template name="tab1"></xsl:call-template>
					<xsl:call-template name="tab2">
						<xsl:with-param name="selected" select="self::node()" />
					</xsl:call-template>
		        </div>

		        <xsl:call-template name="topic-toolbar-generic"></xsl:call-template>

        	</xsl:otherwise>
        </xsl:choose>        
	</xsl:template>
	
	<!-- Tab1 - Read Articles -->
	<xsl:template name="tab1">
		<xsl:param name="selected" />
		
		<xsl:call-template name="channel-link-generic">
			<xsl:with-param name="title">Read Articles</xsl:with-param>
			<xsl:with-param name="imagePath">folder_open_16</xsl:with-param>
			<xsl:with-param name="URL"></xsl:with-param>
			<xsl:with-param name="selected" select="$selected" />
		</xsl:call-template>
	</xsl:template>

	<!-- Tab2 - Post &amp; Edit Articles -->
	<xsl:template name="tab2">
		<xsl:param name="selected" />
		
		<xsl:if test = "$mayManageArticles = 'true' or $mayManageTopics = 'true'">
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Post &amp; Edit Articles</xsl:with-param>
				<xsl:with-param name="URL">action=ArticleSearch&amp;admin=yes&amp;searchstyle=dates</xsl:with-param>
				<xsl:with-param name="imagePath">add_file_16</xsl:with-param>
				<xsl:with-param name="selected" select="$selected" />
			</xsl:call-template>
		</xsl:if>			
	</xsl:template>
	
	<xsl:template name="channel-link-generic">
		<xsl:param name="title"/>
		<xsl:param name="URL"/>
		<xsl:param name="imagePath"/>
		<xsl:param name="target"/>
		<xsl:param name="selected" />
		
		<xsl:variable name = "focusChannelParameter"><xsl:choose>
				<xsl:when test="$pageName = 'News_Main'">uP_root=me&amp;</xsl:when>
				<xsl:otherwise></xsl:otherwise>
		</xsl:choose></xsl:variable>

		<div class="tool">
			<table class="tool-table" cellpadding="0" cellspacing="0">
				<tr>
					<xsl:choose>
						<xsl:when test="not($selected)">
							<td></td>
							<td class="tool-tab-body">								
								<div>
									<a href="{concat($baseActionURL,'?',$focusChannelParameter,$URL)}" title="{$title}">
										<xsl:if test="$target">
											<xsl:attribute name="target">
												<xsl:value-of select="$target"/>
											</xsl:attribute>
										</xsl:if>									
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
									<a href="{concat($baseActionURL,'?',$focusChannelParameter,$URL)}" title="{$title}" class="selected-tab-link">
										<xsl:if test="$target">
											<xsl:attribute name="target">
												<xsl:value-of select="$target"/>
											</xsl:attribute>
										</xsl:if>									
										<xsl:value-of select="$title"/>
									</a>
								</div>
							</td>
							<td class="tool-tab-right">&#160;</td>						
						</xsl:otherwise>
					</xsl:choose>
				</tr>
			</table>
		</div>
		
	</xsl:template>

	<xsl:template name="article-toolbar-generic">
		<xsl:param name="selected" />

        <div class="page-toolbar-container">
			<xsl:choose>
				<xsl:when test="$pageName = 'View_Full_Article'">
					<xsl:call-template name="channel-link-generic2">
						<xsl:with-param name="title">&lt;&lt; Back to View My Articles</xsl:with-param>
						<xsl:with-param name="URL"></xsl:with-param>
						<xsl:with-param name="imagePath"></xsl:with-param>
					</xsl:call-template>
				</xsl:when>
			  
				<xsl:when test="$pageName = 'Saved_Subscribed'">
					<xsl:call-template name="channel-link-generic2">
						<xsl:with-param name="title">&lt;&lt; Back to Select Topics of Interest</xsl:with-param>
						<xsl:with-param name="URL">action=subscribe</xsl:with-param>
						<xsl:with-param name="imagePath"></xsl:with-param>
					</xsl:call-template>
				</xsl:when>
			  
				<xsl:when test="$pageName = 'NewsAdmin_DeleteArticle' or $pageName = 'NewsAdmin_DeleteArticleError' or $pageName = 'NewsAdmin_CreateArticle' or $pageName = 'NewsAdmin_CreateStory' or $pageName = 'NewsAdmin_PreviewArticle' or $pageName = 'NewsAdmin_SaveArticle'">
					<xsl:call-template name="channel-link-generic2">
						<xsl:with-param name="title">&lt;&lt; Back to Manage Articles</xsl:with-param>
						<xsl:with-param name="URL">action=ArticleSearch&amp;admin=yes&amp;searchstyle=topic</xsl:with-param>
						<xsl:with-param name="imagePath"></xsl:with-param>
					</xsl:call-template>
				</xsl:when>
			  
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic2">
						<xsl:with-param name="title">View My Articles</xsl:with-param>
						<xsl:with-param name="URL"></xsl:with-param>
						<xsl:with-param name="imagePath">add_file_16</xsl:with-param>
						<xsl:with-param name="selected" select="self::node()[$pageName = 'News_Main']" />
					</xsl:call-template>

					<div class="tool2">&#160; | &#160;</div>
					<xsl:call-template name="channel-link-generic2">
						<xsl:with-param name="title">Select Topics of Interest</xsl:with-param>
						<xsl:with-param name="URL">action=subscribe</xsl:with-param>
						<xsl:with-param name="imagePath"></xsl:with-param>
						<xsl:with-param name="selected" select="self::node()[$pageName = 'Subscribe']" />
					</xsl:call-template>
					
				</xsl:otherwise>
			</xsl:choose>			
		</div>

	</xsl:template>

	<xsl:template name="topic-toolbar-generic">
		<xsl:param name="selected" />

		<xsl:choose>
			<xsl:when test = "$pageName = 'NewsAdmin_ArticlesMenu' and not(//search_results)">
				<!-- No link needed -->
			</xsl:when>
			<xsl:otherwise>
		        <div class="page-toolbar-container">
				<xsl:call-template name="channel-link-generic2">
					<xsl:with-param name="title">&lt;&lt; Back to Post &amp; Edit Articles</xsl:with-param>
					<xsl:with-param name="URL">action=ArticleSearch&amp;admin=yes&amp;searchstyle=dates</xsl:with-param>
					<xsl:with-param name="imagePath"></xsl:with-param>
				</xsl:call-template>
				</div>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	
	<!-- *******   GENERIC 2 - Page Links  *********  -->
	<xsl:template name="channel-link-generic2">
		<xsl:param name="title"/>
		<xsl:param name="URL"/>
		<xsl:param name="imagePath"/>
		<xsl:param name="selected" />

		<xsl:variable name = "focusChannelParameter"><xsl:choose>
				<xsl:when test="$pageName = 'News_Main'">uP_root=me&amp;</xsl:when>
				<xsl:otherwise></xsl:otherwise>
		</xsl:choose></xsl:variable>
		
		<xsl:choose>
			<xsl:when test="not($selected)">
				<div class="tool2">
					<a href="{concat($baseActionURL,'?',$focusChannelParameter,$URL)}" title="{$title}">
						<xsl:value-of select="$title"/>
					</a>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<div class="tool2 selected-tool2">
					<span>
						<xsl:value-of select="$title"/>
					</span>
				</div>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>	

    <xsl:template name="topic-workflow">
    	<table border="0" cellpadding="3" cellspacing="6">
    		<tr>        			
    			<xsl:choose>
    				<xsl:when test="$pageName = 'NewsAdmin_CreateTopic'">
        				<td><strong>Enter<br />Topic Details</strong></td>
    				</xsl:when>
    			  
    				<xsl:otherwise>
        				<td class="portlet-font-dim">Enter<br />Topic Details</td>
    				</xsl:otherwise>
    			</xsl:choose>
    			<td>&gt;</td>
    			<xsl:choose>
    				<xsl:when test="$pageName = 'NewsAdmin_SelectGroup'">
	        			<td><strong>Select<br />Topic Authors</strong></td>
    				</xsl:when>
    			  
    				<xsl:otherwise>
        				<td class="portlet-font-dim">Select<br />Topic Authors</td>
    				</xsl:otherwise>
    			</xsl:choose>
    			<td>&gt;</td>
    			<xsl:choose>
    				<xsl:when test="$pageName = 'NewsAdmin_PreviewTopic'">
	        			<td><strong>Preview<br />Topic Properties</strong></td>
    				</xsl:when>
    			  
    				<xsl:otherwise>
        				<td class="portlet-font-dim">Preview<br />Topic Properties</td>
    				</xsl:otherwise>
    			</xsl:choose>
    		</tr>
    	</table>
    </xsl:template>    
    
    <xsl:template name="article-workflow">
    	<table border="0" cellpadding="3" cellspacing="6">
    		<tr>        			
    			<xsl:choose>
    				<xsl:when test="$pageName = 'NewsAdmin_CreateArticle'">
        				<td><strong>Enter<br />Article Properties</strong></td>
    				</xsl:when>
    			  
    				<xsl:otherwise>
        				<td class="portlet-font-dim">Enter<br />Article Properties</td>
    				</xsl:otherwise>
    			</xsl:choose>
    			<td>&gt;</td>
    			<xsl:choose>
    				<xsl:when test="$pageName = 'NewsAdmin_CreateStory'">
	        			<td><strong>Enter<br />Article Content</strong></td>
    				</xsl:when>
    			  
    				<xsl:otherwise>
        				<td class="portlet-font-dim">Enter<br />Article Content</td>
    				</xsl:otherwise>
    			</xsl:choose>
    			<td>&gt;</td>
    			<xsl:choose>
    				<xsl:when test="$pageName = 'NewsAdmin_PreviewArticle'">
	        			<td><strong>Preview<br />Article</strong></td>
    				</xsl:when>
    			  
    				<xsl:otherwise>
        				<td class="portlet-font-dim">Preview<br />Article</td>
    				</xsl:otherwise>
    			</xsl:choose>
    		</tr>
    	</table>
    </xsl:template>    
</xsl:stylesheet>
