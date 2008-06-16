<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="../global/global.xsl"/>

<!-- Parameters -->


<xsl:param name="current_command"/>
<xsl:param name="numberPerPage">5</xsl:param>
   
<!-- Permissions -->
<xsl:param name="canImport"/>
<xsl:template name="autoFormJS">
<script language="JavaScript" type="text/javascript" src="javascript/admin/ImportChannel/autoForm.js"></script>
</xsl:template>
<!-- Common -->
<xsl:template name="links">
	<!-- UniAcc: Layout Table -->
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
	    <tr>
	        <td class="views-title">
				<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif"
	    		alt="Icon of tool-tip indicating channel options section" 
	    		title="Icon of tool-tip indicating channel options section" 
				align="absmiddle" />
	        </td>
	        <td class="views" valign="middle" height="26" width="100%">
	            <xsl:choose>
	                <xsl:when test="$current_command = 'main'">
	                    Import<img height="1" width="3" src="{$SPACER}"
	                    alt="" title="" border="0" /><img border="0" 
						src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif"
	                    alt="Inactive 'View' icon: currently viewing import selection" 
	                    title="Inactive 'View' icon: currently viewing import selection" 
	                    align="absmiddle" />
	                </xsl:when>
	              
	                <xsl:otherwise>
	                    <a href="{$baseActionURL}" title="View import selection" 
	                    onmouseover="swapImage('importViewImage','channel_view_active.gif')" 
	                    onmouseout="swapImage('importViewImage','channel_view_base.gif')">
							Import<img 
		                    height="1" width="3" src="{$SPACER}"
		                    alt="" title="" border="0" /><img border="0" 
							src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
		                    alt="'View' icon: import selection" 
		                    title="'View' icon: import selection" 
		                    align="absmiddle" name="importViewImage" id="importViewImage"/>
						</a>
	                </xsl:otherwise>
	            </xsl:choose>
	            
	        </td>
	    </tr>
	</table>
</xsl:template>

</xsl:stylesheet>
