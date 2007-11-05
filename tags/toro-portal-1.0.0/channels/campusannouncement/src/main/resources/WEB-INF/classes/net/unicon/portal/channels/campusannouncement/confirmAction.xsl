<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
<!--<xsl:variable name="SELECTED_GROUP_ID"><xsl:value-of select="/announcement-edit/announcement/@category-id"/></xsl:variable>


	<xsl:template match="/">
	    <textarea rows="4" cols="40">
	      <xsl:copy-of select = "*"/> 
	      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
	      <parameter name="pageChannel"><xsl:value-of select="$pageChannel" /></parameter>
	      <parameter name="channel_admin"><xsl:value-of select="$channel_admin" /></parameter>
	   	</textarea>
	
		<xsl:apply-templates />
	</xsl:template> -->
	
    <xsl:template match="/confirmation">
    	<h2 class="page-title"><xsl:value-of select="caption"/></h2>
    	
    	<div class="bounding-box1">
			<form action="{$baseActionURL}?command=admin-announcer" method="post">
				<input type="hidden" name="sub-command" value="confirm-action" />            
				<table cellpadding="0" cellspacing="0" border="0" width="100%">

				  <xsl:for-each select="item">
					<tr>                
					  <td class="uportal-channel-table-row-even" align="left" width="100%">
						<xsl:value-of select="@name" />
					  </td>
					</tr>
				  </xsl:for-each>
					<tr>
					  <td colspan="2" class="table-nav">                    
						<input name="Cancel" value="OK" type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}'" title="To cancel editing this announcement, returning to viewing the announcements"/>
					  </td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>

</xsl:stylesheet>
