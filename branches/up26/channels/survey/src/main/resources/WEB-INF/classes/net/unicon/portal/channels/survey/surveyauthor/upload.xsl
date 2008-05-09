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
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    <xsl:output method='html' />
    <xsl:include href="common.xsl" />
    <xsl:template match='survey-system'>

        <div class="gradient-page-title">Upload File</div>
        
        <div class="bounding-box1">
            <form method='post' action='{$baseActionURL}' enctype="multipart/form-data">
                <input type='hidden' name='sid' value='{$sid}' />
                <!--UniAcc: Layout Table -->
                <table cellpadding='0' cellspacing='0' width='100%' border='0'>
                    <tr>
                        <td class="td-left" nowrap="nowrap">
                        	<label for="SSA-AttachFileF1">Attach File</label>
                        </td>
                        <td class="td-right">
                            <input name='file' type='file' size='40' class="text" id="SSA-AttachFileF1"></input>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="center">
                        	<div class="submit-container">
								<input type='submit' name='do' value="upload" />
								<input type='submit' name='do' value="close" />
							</div>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </xsl:template>
</xsl:stylesheet>


