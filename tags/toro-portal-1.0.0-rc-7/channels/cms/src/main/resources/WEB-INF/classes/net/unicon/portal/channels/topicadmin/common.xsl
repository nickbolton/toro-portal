<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:import href="../global/global.xsl"/>
<xsl:include href="../global/toolbar.xsl"/>

<!-- parameters -->
<xsl:param name="current_command"/>
<xsl:param name="searchCommand"/>
<xsl:param name="searchResultsCommand"/>
<xsl:param name="addCommand"/>
<xsl:param name="subsequentAddCommand"/>
<xsl:param name="cancelCommand"/>
<xsl:param name="selectParentCommand"/>
<xsl:param name="viewCommand"/>
<xsl:param name="editCommand"/>
<xsl:param name="subsequentEditCommand"/>
<xsl:param name="deleteCommand"/>
<xsl:param name="addSubmitCommand"/>
<xsl:param name="editSubmitCommand"/>
<xsl:param name="topicNameParam"/>
<xsl:param name="topicDescParam"/>
<xsl:param name="defaultRoleParam"/>
<xsl:param name="confirmDeleteCommand"/>
<xsl:param name="deleteConfirmationParam"/>
<xsl:param name="topicName"/> 
<xsl:param name="searchTopicName"/> <!-- Catalog: sticky user offering data of topic name input -->
<xsl:param name="catChannel">topicAdmin</xsl:param> <!-- Catalog: for form id uniqueness and identifying which search inputs to insert - See Catalog templates in global.xsl -->

<!-- activities -->
<xsl:param name="addTopic"/>
<xsl:param name="editTopic"/>
<xsl:param name="deleteTopic"/>

<!-- Common -->
<!-- The autoFormJS template should replace the following javascript template. That template has been commented out but not deleted. It should be considered deprecated. -->
<xsl:template name="autoFormJS">
        <script language="JavaScript" type="text/javascript" src="javascript/admin/TopicAdminChannel/autoForm.js"/>
</xsl:template>

<xsl:template name="links">


	<div class="portlet-toolbar-container">
		<xsl:choose>
			<xsl:when test="($current_command = 'search')">
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">Search Topics</xsl:with-param>
					<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">Search Topics</xsl:with-param>
					<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?catPageSize=<xsl:value-of select="$catPageSize"/>&amp;command=search&amp;searchTopicName=&amp;servant_command=cancel</xsl:with-param>
					<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
				</xsl:call-template>		
			</xsl:otherwise>
		</xsl:choose>

		<xsl:choose>
			<xsl:when test="$current_command = 'add'">
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">Add Topic</xsl:with-param>
					<xsl:with-param name="imagePath">channel_add_active</xsl:with-param>
				</xsl:call-template>		
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$addTopic = 'Y'">
						<xsl:call-template name="channel-link-generic">
							<xsl:with-param name="title">Add Topic</xsl:with-param>
							<xsl:with-param name="imagePath">channel_add_active</xsl:with-param>
							<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?catPageSize=<xsl:value-of select="$catPageSize"/>&amp;command=<xsl:value-of select="$addCommand"/>&amp;searchTopicName=&amp;servant_command=cancel</xsl:with-param>
						</xsl:call-template>			
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</div>
	<!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>     
    </textarea> -->

</xsl:template>

</xsl:stylesheet>
