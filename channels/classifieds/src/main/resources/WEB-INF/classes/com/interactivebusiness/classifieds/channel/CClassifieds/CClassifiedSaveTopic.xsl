<?xml version="1.0"?>
<!--
 
 Copyright (c) 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 This software is the confidential and proprietary information of
 Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 You shall not disclose such Confidential Information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with IBS.
 
 IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 THIS SOFTWARE OR ITS DERIVATIVES.

 $Log: 
  1    Channels  1.0         8/6/2002 4:54:05 PM  Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <!-- Start of XSL Code -->
    <xsl:template match="TopicSaved">

        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="2" cellspacing="0" align="left" width="100%" class="table-content">
            <tr>
                <td colspan="2" class="table-content-single">
                    <xsl:if test="child::saved">Thank you, Your Topic <b><xsl:apply-templates select="//@topic_name"/></b> has been saved.</xsl:if>
                    <xsl:if test="child::notsaved">
                            <img src="{$imagedir}/error.gif" border="0" alt="Error" title="Error"/>
                            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                            ERROR OCCURRED!! Your Topic <b><xsl:apply-templates select="//@topic_name"/></b> was not saved.
                    </xsl:if>
                </td>
            </tr>
            <xsl:choose>
                <xsl:when test="child::notsaved">
                    <tr>
                        <td align="left" class="table-content-single">
                            <font class="uportal-channel-error">Reason: <xsl:apply-templates select="//@error"/></font>
                        </td>
                    </tr>
					<tr>
						<td class="table-light-single-bottom" style="text-align:center;">
							<form action="{$baseActionURL}?action=SaveTopicError" method="post">
								<!-- <input type="hidden" name="action" value="SaveTopicError"/> -->
								<input type="submit" name="back" value="Back" class="uportal-button"/>
								<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
								<input type="submit" name="createanother" value="Create Another" class="uportal-button"/>
								<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
								<input type="submit" name="exit" value="Exit" class="uportal-button"/>
							</form>
						</td>
					</tr>
                </xsl:when>
                <xsl:otherwise>
					<tr class="uportal-background-light">
						<td class="table-light-single-bottom" style="text-align:center;">
							<form action="{$baseActionURL}?action=SaveTopicError" method="post">
                                <!-- <input type="hidden" name="action" value="SaveTopicError"/> -->
                                <input type="submit" name="createanother" value="Create Another" class="uportal-button"/>
                                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                                <input type="submit" name="exit" value="Exit" class="uportal-button"/>
							</form>
						</td>
					</tr>
                </xsl:otherwise>
            </xsl:choose>
        </table>
    </xsl:template>
</xsl:stylesheet>
