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
  1    Channels  1.0         8/6/2002 4:54:04 PM  Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>

    <xsl:template match="Preview">
    
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->
        
        <form action="{$baseActionURL}?action=saveNew" method="post">
            <!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top-single">New Classified Preview</th>
                </tr>
                <xsl:apply-templates/>
                <tr class="table-light-bottom" style="text-align:center;">
                    <td>
                        <input type="submit" name="back" value="Back" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="next" value="Next" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>

    <xsl:template match="NewItem">
        <tr>
            <td>
                <!--UniAcc: Layout Table -->
                <table border="0" cellspacing="0" cellpadding="0" width="100%" class="table-content">
                    <tr>
                        <td class="table-content-single">
                            <xsl:value-of select="@message"/>
                            <br/>
                        </td>
                    </tr>
                    <tr>
                        <td class="table-content-single">
                            <b>Price:
                                <xsl:if test="starts-with(@cost, '$')">
                                    <xsl:value-of select="@cost"/>
                                </xsl:if>
                                <xsl:if test="not (starts-with(@cost, '$'))">$ <xsl:value-of select="@cost"/></xsl:if>
                            </b>
                        </td>
                    </tr>
                    <tr>                        
                        <td class="table-content-single">
                            <!--{@imageTitle} does not exist yet, but ADA compliance requires some descriptor from xml/java -->
                            <xsl:if test="@image">
                                <img src="{$resourceURL}?itemID={@itemID}" border="0" alt="{@imageTitle}" title="{@imageTitle}"/>
                            </xsl:if>
                            <xsl:if test="not (@image)">
                                <img src="{$imagedir}/nophoto.gif" border="0" alt="No Associated Image" title="No Associated Image"/>
                            </xsl:if>
                        </td>
                    </tr>
                    <tr>
                        <td class="table-content-single-bottom">
                            <b>Contact Information:</b>
                            <br/>
                            <xsl:value-of select="@contact"/>
                            <br/>
                            <xsl:value-of select="@phone"/>
                            <br/>
                            <xsl:value-of select="@email"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
