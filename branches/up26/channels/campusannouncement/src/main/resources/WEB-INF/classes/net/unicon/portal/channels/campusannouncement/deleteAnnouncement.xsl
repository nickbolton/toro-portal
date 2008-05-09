<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include Files -->
<xsl:include href="common.xsl"/>

  <xsl:template match="/">
  	<h2 class="page-title">Delete Announcement</h2>
  
  	<div class="bounding-box1">
		<form name="announcementForm" action="{$baseActionURL}?command=main" method="post">
		  <input type="hidden" name="announcement-id" value="{./announcement/@announcement-id}" />
		  <input type="hidden" name="sub-command" value="submit-delete"/>

		  <table cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
			  <td class="table-content-single-top">
				<span class="uportal-channel-warning">Are you sure you want to delete this announcement?</span><br />
				<img height="10" width="1" src="{$SPACER}" alt=""/><br />

				<span class="uportal-channel-copyright">
				  <xsl:apply-templates select="./announcement/announcement-body" />
				</span><br /><img height="10" width="1" src="{$SPACER}" alt=""/><br />
			  </td>
			</tr>
			<tr>
			  <td class="table-nav">
				  <nobr>
					<input type="submit" class="uportal-button" name="Submit" value="Submit" 
						title="To submit your response and return to the edit view of the announcements" />
					<input type="submit" class="uportal-button" name="Submit" value="Cancel" 
						title="To return to the main view of the announcements without deleting this announcement" />
				  </nobr>
			  </td>
			</tr>
		  </table>
		</form>
	</div>

  </xsl:template>

  <xsl:template match="announcement-body">
      <xsl:apply-templates />
  </xsl:template>

</xsl:stylesheet>
