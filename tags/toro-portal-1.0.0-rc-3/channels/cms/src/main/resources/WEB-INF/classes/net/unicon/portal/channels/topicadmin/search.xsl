<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="common.xsl"/>

  <xsl:template match="topicAdmin">

    <xsl:call-template name="links"/>
    <h2 class="page-title">Search Topics</h2>
    
    <div class="bounding-box1">
		<form action="{$baseActionURL}?command=searchResults" method="post">
			<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
			<input type="hidden" name="command" value="{$catCurrentCommand}"/>
			<input type="hidden" name="catPageSize" value="{$catPageSize}"/>
		<table cellpadding="0" cellspacing="0" border="0" width="100%">
		<tr>
			<td class="table-light-left">Topic Name:</td>
			<td class="table-content-right">
				<input class="text" type="text" name="searchTopicName" size="35" maxlength="35" id="atast1"/>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="table-nav">
				<input class="uportal-button" name="submit" value="Submit" type="submit" title="Submit topic search"/>
				<input type="button" class="uportal-button" name="cancel" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel topic search"/>
			</td>
		</tr>              
		</table>
		</form>
	</div>
  </xsl:template>

</xsl:stylesheet>
