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
    <xsl:template match="ClassifiedSaved">
    
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->
        
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" align="left" class="table-content" width="100%">
            <tr>
                <xsl:if test="child::saved">
                    <td colspan="2" class="table-content-single">
                        <font class="uportal-channel-text">Thank you, Your Classified <b><xsl:apply-templates select="topicname"/></b> has been saved.</font>
                    </td>
                </xsl:if>
                <xsl:if test="child::notsaved">
                    <td class="table-content-left">
                        <img src="{$imagedir}/error.gif" border="0" alt="Error" title="Error"/>
                    </td>
                    <td align="left" class="table-content-right">
                        <font class="uportal-channel-text">ERROR OCCURRED!! Your Classified <b><xsl:apply-templates select="topicname"/></b> was not saved.</font>
                    </td>
                </xsl:if>
            </tr>
            <xsl:choose>
                <xsl:when test="child::notsaved">
                    <tr>
                        <td align="left" colspan="2" class="table-content-single">
                            <!--element error is not in the xml structure.  A TTrack issue will be started -->
                            <font class="uportal-channel-error">Reason: <xsl:apply-templates select="error"/></font>
                        </td>
                    </tr>
					<tr>
						<td colspan="2" class="table-light-single-bottom" style="text-align:center;">
							<form action="{$baseActionURL}?action=ClassifiedSaved" method="post">
                                <!-- <input type="hidden" name="action" value="ClassifiedSaved"/> -->
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
					<tr>
						<td class="table-light-single-bottom" style="text-align:center;">
							<form action="{$baseActionURL}?action=ClassifiedSaved" method="post">
                                <!-- <input type="hidden" name="action" value="ClassifiedSaved"/> -->
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
