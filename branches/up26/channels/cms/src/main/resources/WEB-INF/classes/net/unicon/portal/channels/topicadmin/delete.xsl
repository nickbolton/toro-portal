<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- Include -->
  <xsl:include href="common.xsl"/>

  <xsl:template match="topic">
    <xsl:call-template name="links"/>
	<h2 class="page-title">Delete Topic</h2>
	
	<div class="bounding-box1">
		<form action="{$baseActionURL}" method="post">
			<input type="hidden" name="targetChannel" value="{$targetChannel}" />
			<input type="hidden" name="ID" value="{@id}" />
			<input type="hidden" name="command" value="{$confirmDeleteCommand}" />
			<input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
			<input type="hidden" name="searchTopicName" id="searchTopicName" value="{$searchTopicName}" />
			<table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td class="table-content-single-top" style="text-align:center">
					<span class="uportal-channel-warning">This action will delete this and ALL sub-topics that exist. Are you sure you want to delete the <span class="uportal-channel-strong">"<xsl:value-of select="name"/>"</span> topic?</span>
				</td>
			</tr>
			<tr>
				<td class="table-content-single" style="text-align:center">
					<input type="radio" class="radio" name="{$deleteConfirmationParam}" value="yes" id="topicAdminDeleteConfirm"/>&#160;
					<label for="topicAdminDeleteConfirm">Yes</label>
					<img height="1" width="15" src="{$SPACER}" alt="" title="" />
					<input checked="checked" type="radio" class="radio" name="{$deleteConfirmationParam}" value="no" id="topicAdminDeleteDeny"/>&#160;
					<label for="topicAdminDeleteDeny">No</label><br />
				</td>
			</tr>
			<tr>
				<td class="table-content-single-bottom" style="text-align:center">
					<input type="submit" class="uportal-button" value="Submit" title="Submit deletion response for '{name}'"/>
					<input type="button" class="uportal-button" value="Cancel" title="Cancel and return to the topic search view without deleting '{name}'"
					onclick="window.locationhref='{$baseActionURL}?catPageSize={$catPageSize}&amp;searchTopicName={$searchTopicName}'"/>
				</td>
			</tr>
			</table>
		</form>
	</div>

  </xsl:template>

</xsl:stylesheet>











