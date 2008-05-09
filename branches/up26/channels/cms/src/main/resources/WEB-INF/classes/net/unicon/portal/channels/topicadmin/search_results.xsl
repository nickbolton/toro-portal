<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="topicAdmin">

    <xsl:call-template name="links"/>

    <h2 class="page-title">Topic Search Results</h2>
    
    <div class="bounding-box1">
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<th class="th-top-left">Topic Name</th>
				<th class="th-top-right">Topic Description</th>
			</tr>
			<xsl:choose>
			<xsl:when test="count(topic) = 0">
				<tr>
					<th class="table-content-single" colspan="4">No topics found.</th>
				</tr>                    
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="topic">
					<xsl:sort select="name"/>
				</xsl:apply-templates>
			</xsl:otherwise>
			</xsl:choose>
		</table>
	
    	<xsl:call-template name="catalog"/> <!-- Catalog for paging and search called from global.xsl -->
    </div>

  </xsl:template>

  <xsl:template match="topic">
  	<xsl:variable name = "bottomStyle"><xsl:if test = "position() = last()">-bottom</xsl:if></xsl:variable>
    <tr>
        <td class="table-light-left{$bottomStyle}" style="text-align:right;vertical-align:top;" width="30%">
            <xsl:value-of select="name" />
        </td>
        <td class="table-content-right{$bottomStyle}" style="text-align:left;vertical-align:top;" width="70%">
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}" 
			title="View topic details" onmouseover="swapImage('topicAdminDetailsImage{position()}','channel_view_active.gif')" onmouseout="swapImage('topicAdminDetailsImage{position()}','channel_view_base.gif')"><xsl:value-of select="description" /><img 
			height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/><img src=
            "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" 
            alt="'View' icon: topic details for '{name}'"   
            title="'View' icon: topic details for '{name}'"
            border="0" align="absmiddle" name="topicAdminDetailsImage{position()}" id="topicAdminDetailsImage{position()}"/></a><img 
            height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <xsl:choose>
              <xsl:when test="$editTopic = 'Y'">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}" title="Edit topic"
				onmouseover="swapImage('topicAdminEditImage{position()}','channel_edit_active.gif')" 
				onmouseout="swapImage('topicAdminEditImage{position()}','channel_edit_base.gif')"><img border="0" src= 
                "{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                alt="'Edit' icon: edit topic details for '{name}'" 
                title="'Edit' icon: edit topic details for '{name}'"
                align="absmiddle" name="topicAdminEditImage{position()}" id="topicAdminEditImage{position()}"/></a>
              </xsl:when>
              <xsl:otherwise>
                <img border="0" src=
                "{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
                alt="Inactive 'Edit' icon: edit topic details unavailable due to lack of permission" 
                title="Inactive 'Edit' icon: edit topic details unavailable due to lack of permission" align="absmiddle" />
              </xsl:otherwise>
            </xsl:choose>
            <img height="1" width="3" src=
            "{$SPACER}"
           alt="" border="0"/>
            <xsl:choose>
              <xsl:when test="$deleteTopic = 'Y'">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$deleteCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}" title="Delete topic"
				onmouseover="swapImage('topicAdminDeleteImage{position()}','channel_delete_active.gif')" 
				onmouseout="swapImage('topicAdminDeleteImage{position()}','channel_delete_base.gif')"><img 
				src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" 
                alt="'Delete' icon: delete '{name}' topic" 
                title="'Delete' icon: delete '{name}' topic" 
                border="0" align="absmiddle" name="topicAdminDeleteImage{position()}" id="topicAdminDeleteImage{position()}"/></a>
              </xsl:when>
              <xsl:otherwise>
                <img src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif" 
                alt="Inactive 'Delete' icon: delete topic is unavailable due to lack of permission" 
                title="Inactive 'Delete' icon: delete topic is unavailable due to lack of permission" 
                border="0" align="absmiddle" />
              </xsl:otherwise>
            </xsl:choose>
      </td>
    </tr>
  </xsl:template>
  
</xsl:stylesheet>
