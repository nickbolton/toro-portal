<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="/">

    <!-- <textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/>
    </textarea> -->

    <xsl:apply-templates select="topicAdmin/topic"/>
  </xsl:template>


<xsl:template match="path">
    <xsl:if test="position()!=1">
        <br/>
    </xsl:if>
    <a href="javascript:void(document.topicAdminForm.next_command.value='{$subsequentEditCommand}', document.topicAdminForm.command.value='{$selectParentCommand}', document.topicAdminForm.submit());" title="Edit the parent topic"><xsl:value-of select="."/></a>
</xsl:template>

  <xsl:template match="topic">

    <xsl:call-template name="autoFormJS"/>
    <xsl:call-template name="links"/>
	
	<h2 class="page-title"> Edit Topic</h2>
	
	<div class="bounding-box1">
		<!-- <form method="post" onSubmit="return checkTopicSubmit()" name="topicAdminForm" action="{$baseActionURL}"> -->
		<form method="post" onSubmit="return validator.applyFormRules(this, new TopicAdminRulesObject())" name="topicAdminForm" action="{$baseActionURL}">
		  <input type="hidden" name="targetChannel" value="{$targetChannel}" />
		  <input type="hidden" name="command" value="{$editSubmitCommand}" />
		  <input type="hidden" name="ID" value="{@id}" />
		  <input type="hidden" name="searchTopicName" id="searchTopicName" value="{$searchTopicName}" />
		  <input type="hidden" name="next_command" value="" />
		  <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
		  <input type="hidden" name="parentGroupId" value="{parentGroup/@id}"/>
		<table cellpadding="0" cellspacing="0" border="0" width="100%">

		<tr>
			<td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="topicAdminFormParentGroup">Parent Group</label></td>
			<td class="table-content-right" style="text-align:left" width="100%">
				<xsl:apply-templates select="parentGroup/path"/>
		  </td>
		</tr>

		<tr>
			<td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="topicAdminFormTopicName">Topic Name</label></td>
			<td class="table-content-right" style="text-align:left" width="100%">
				<input name="{$topicNameParam}" type="text" class="text" id="topicAdminFormTopicName" size="70" maxlength="80">
				<xsl:attribute name="value">
					<xsl:value-of select="name"/>
				</xsl:attribute>
				</input>
				<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/><a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}"
				title="View details of this topic"
				onmouseover="swapImage('topicAdminDetailsImage','channel_view_active.gif')"
				onmouseout="swapImage('topicAdminDetailsImage','channel_view_base.gif')"><img border="0" src=
				"{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
				alt="'View' icon: topic details for '{name}'"
				title="'View' icon: topic details for '{name}'"
				align="absmiddle" name="topicAdminDetailsImage" id="topicAdminDetailsImage"/></a><img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
				<xsl:choose>
					<xsl:when test="$deleteTopic = 'Y'">
					<a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$deleteCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}" title="Delete this topic"
					onmouseover="swapImage('topicAdminDeleteImage','channel_delete_active.gif')"
					onmouseout="swapImage('topicAdminDeleteImage','channel_delete_base.gif')">
					<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
					alt="'Delete' icon: delete '{name}' topic"
					title="'Delete' icon: delete '{name}' topic"
					border="0" align="absmiddle" name="topicAdminDeleteImage" id="topicAdminDeleteImage"/></a>
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

		<tr>
			<td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="topicAdminFormTopicDescription">Topic Description</label></td>
			<td class="table-content-right" width="100%"><textarea name="{$topicDescParam}" rows="4" cols="62"
			id="topicAdminFormTopicDescription"><xsl:value-of select="description"/></textarea></td>
		</tr>

		<xsl:for-each select="//adjunct/choice-collection">
			<xsl:call-template name="adjunct-data" />
		</xsl:for-each>

		<tr>
			<td colspan="2" class="table-nav" style="text-align:center">
				<input type="submit" class="uportal-button" value="Submit" title="Submit to update this topic's details"/>
				<input type="button" class="uportal-button" value="Cancel" title="Cancel to return to the topics search without updating this topic"
				onclick="window.locationhref='{$baseActionURL}?command={$cancelCommand}&amp;catPageSize={$catPageSize}&amp;searchTopicName={$searchTopicName}';"/></td>
		</tr>

		</table>
		</form>
	</div>

    <!-- <form method="post" onSubmit="return checkTopicSubmit()" name="topicAdminForm" action="{$baseActionURL}">
      <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
      <input type="hidden" name="command" value="{$editSubmitCommand}"></input>
      <input type="hidden" name="ID" value="{@id}"></input>
      <input type="hidden" name="searchTopicName" id="searchTopicName" value="{$searchTopicName}" />
      <table>
        <tr>
          <th class="uportal-background-med">Property</th>
          <th class="uportal-background-med">Value</th>
        </tr>

        <tr>
          <td class="uportal-background-light">Topic Name</td>
          <td class="uportal-background-light" style="text-align:center">
            <input name="{$topicNameParam}" type="text">
              <xsl:attribute name="value">
                <xsl:value-of select="name"/>
              </xsl:attribute>
            </input>
          </td>
        </tr>

        <tr>
          <td class="uportal-background-light">Topic Description</td>
          <td class="uportal-background-light" style="text-align:center"><textarea name="{$topicDescParam}" rows="4"><xsl:value-of select="description"/></textarea></td>
        </tr>

        <tr>
          <td colspan="2" style="text-align:center"><nobr><input type="submit" value="Submit"/><input type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?catPageSize={$catPageSize}&amp;searchTopicName='"/></nobr></td>
        </tr>

      </table>
    </form> -->
    <!--<xsl:call-template name="topicSearch"/> -->

  </xsl:template>

<xsl:template name="adjunct-data">

    <!-- The context node is <choice-collection> -->

    <tr>
        <th colspan="2" class="th-top"><xsl:value-of select="label" /></th>
    </tr>

    <tr>
        <td class="table-light-left" style="text-align:right;vertical-align:top;"><img height="1" width="1" src="{$SPACER}" alt="" title="" border="0" /></td>
        <td class="table-content-right" width="100%" valign="top">
            <xsl:choose>
                <xsl:when test="count(choice[position() = 1]/option) = 0">
                    None available
                </xsl:when>

                <xsl:otherwise>
                    <!-- The choice-decision collection rendering is done through a XSL module initiated in the global.xsl file -->
                    <xsl:apply-templates select="."/>
                </xsl:otherwise>
            </xsl:choose>

        </td>
    </tr>

</xsl:template>

<xsl:template match="group">
  <xsl:param name="parentGroupId"/>
  <xsl:choose>
    <xsl:when test="@id = $parentGroupId">
      <option selected="selected" value="{@id}"><xsl:value-of select="name"/></option>
    </xsl:when>
    <xsl:otherwise>
      <option value="{@id}"><xsl:value-of select="name"/></option>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:apply-templates select="group">
    <xsl:with-param name="parentGroupId" select="$parentGroupId"/>
  </xsl:apply-templates>
</xsl:template>
</xsl:stylesheet>
