<?xml version="1.0" encoding="utf-8"?>
<!--

   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.
  
   This software is the confidential and proprietary information of 
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.
  
   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.

-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>
    <xsl:output method='html' />
	<xsl:include href="../../global/toolbar.xsl" />
	<xsl:include href="common.xsl" />
	
    <xsl:template match="survey-system">
     
		<div class="portlet-toolbar-container">
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Refresh</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?channel_command=refresh</xsl:with-param>
				<xsl:with-param name="imagePath">channel_refresh_active</xsl:with-param>
			</xsl:call-template>
		</div>   
			
		<div class="page-title">View Poll</div>			
			
		<div class="bounding-box1">
            <table cellpadding='0' cellspacing='0' width='100%' border='0'>
                <tr>
                    <td nowrap="nowrap" class="table-content" align="center">
						There is no current poll.
					</td>
                </tr>
            </table>
        </div>
        
    </xsl:template>
</xsl:stylesheet>

