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


    <!-- Start of XSL Code -->

    <xsl:template match="PreviewTopic">
    
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "/*"/>
        </textarea> -->
        
        <form action="{$baseActionURL}?action=SaveTopic" method="post" name="previewItem">
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="2" cellspacing="0" align="center" width="100%" class="table-content">
                <tr>
                    <th class="th-top-single">Topic Preview</th>
                </tr>
                <tr>
                    <td class="table-content-single">                        
                        <span class="uportal-channel-error">(This is how it will appear when in the classifieds list)</span><br/>
                        <xsl:variable name="icon_image">
                            <xsl:value-of select="image"/>
                        </xsl:variable>
                        <xsl:variable name="icon_mime_type">
                            <xsl:value-of select="mime_type"/>
                        </xsl:variable>
                        <!--{image/@title} Does not exist yet, but ADA compliance necessitates some descriptor - B.L. -->
                        <img src="{$resourceURL}?icon=icon&amp;image={$icon_image}&amp;mime_type={$icon_mime_type}" border="0" alt="{image/@title}" title="{image/@title}"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <strong>
                            <xsl:value-of select="topic"/>
                        </strong>
                        <img src="{$imagedir}/minus.gif" border="0" alt="" title=""/>
                        <font class="uportal-channel-text">
                            <xsl:value-of select="description"/>
                        </font>
                    </td>
                </tr>
                <tr>
                    <xsl:apply-templates select="GroupsList"/>
                </tr>
                <tr>
                    <td colspan="4"  class="table-light-single-bottom" style="text-align:center;">
                        <!-- <input type="hidden" name="action" value="SaveTopic"/> -->
                        <input type="submit" name="back" value="Back" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="next" value="Next" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <!-- if cancel go back to createtopic -->
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>



    <xsl:template match="topicID">

        <xsl:variable name="topicID">
            <xsl:apply-templates/>
        </xsl:variable>
        <input type="hidden" name="topicID" value="{$topicID}"/>
        <input type="submit" name="next" value="Next" class="uportal-button"/>
    </xsl:template>

    <xsl:template match="notopicID">

        <input type="submit" name="next" value="Next" class="uportal-button"/>
    </xsl:template>

    <xsl:template match="GroupsList">
        <td align="left" class="table-content-single">
            (Group/Person who can create classifieds for this topic)<br/>
            <xsl:for-each select="group">
                <xsl:sort select="@entity"/>

                <xsl:choose>
                    <xsl:when test="@entity = '3'">
                        <img src="{$imagedir}/folder_closed_16.gif" border="0" alt="" title=""/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    </xsl:when>

                    <xsl:otherwise>
                        <img src="{$imagedir}/person_16.gif" border="0" alt="" title=""/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    </xsl:otherwise>
                </xsl:choose>
                <b>
                    <xsl:value-of select="@name"/>
                </b>
                <br/>
            </xsl:for-each>
        </td>
    </xsl:template>
</xsl:stylesheet>
