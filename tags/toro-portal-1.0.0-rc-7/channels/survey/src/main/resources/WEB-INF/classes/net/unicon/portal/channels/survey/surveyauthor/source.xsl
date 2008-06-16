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
    <xsl:include href="common.xsl" />
    <xsl:param name="return">default</xsl:param>
	    <xsl:template match="survey-system">
        <center>
            <form action="{$baseActionURL}" method='post'>
                <input type='hidden' name='sid' value='{$sid}' />
				<input type='hidden' name='return' value='{$return}' />
                <!--UniAcc: Layout Table -->
                <table cellpadding='0' cellspacing='0' width='100%' border='0'>
                    <tr>
                        <td class="th-top" nowrap='nowrap'>Source</td>
                    </tr>
                    <tr>
                        <td class="table-content-right">
                        	<!--UniAcc: Layout Table -->
                            <table cellpadding='0' cellspacing='0' width='100%' border='0'>
                                <tr>
                                    <td width='100%' align='left'>
										<pre>
											<xsl:value-of select="Source" />										
										</pre>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="table-nav">
                            <input type='submit' class='uportal-button' name='do' value="Close" />
                        </td>
                    </tr>
                </table>
            </form>
        </center>
    </xsl:template>
</xsl:stylesheet>

