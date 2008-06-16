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
  3    Channels  1.2         8/6/2002 4:55:58 PM  Freddy Lopez    modified files
       as part of new version of classifieds channel
  2    Channels  1.1         12/20/2001 4:54:01 PMFreddy Lopez    Made correction
       on copyright; inserted StarTeam log symbol
  1    Channels  1.0         12/20/2001 12:05:37 PMFreddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="DeleteItem">
        <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" align="left" width="100%" class="table-content">
            <tr>
                <td class="table-content-single">
                    <b>Warning:</b>This operation will permanently delete the Classified Item(s) you have selected. Are you sure you want to proceed with the delete?
                </td>
            </tr>
            <tr>
                <td>
                    <!--UniAcc: Layout Table -->
                    <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
					   <xsl:apply-templates/>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="DeleteList">

        <tr>
            <td colspan="2" class="th-top-single"><b>Selected items:</b></td>
        </tr>

        <xsl:apply-templates/>

        <xsl:if test="child::itemID">
            <tr>
                <td colspan="2" class="table-light-bottom">
                    <form action="{$baseActionURL}?uP_root=me&amp;action=DeleteItemConfirmed" method="post" name="confirmdelete">
			    <!-- <input type="hidden" name="action" value="DeleteItemConfirmed"/> -->
			    <input type="submit" name="ok" value="Delete" class="uportal-button"/>
			    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
			    <input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                    </form>
                </td>
            </tr>
        </xsl:if>


        <xsl:if test="child::NoItemsChecked">
            <tr>
                <td colspan="2" class="table-light-bottom">
                    <form action="{$baseActionURL}?uP_root=me&amp;action=myClassifieds" method="post" name="confirmdelete">
			    <!-- <input type="hidden" name="action" value="myClassifieds"/> -->
 
			    <input type="submit" name="back" value="Back" class="uportal-button"/>
                    </form>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="itemID">
        <tr>
            <td colspan="2" class="table-content-single">
                <img src="{$imagedir}/minus.gif" border="0" alt="" title=""/>
                <xsl:value-of select="@content"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="NoItemsChecked">

        <tr>
            <td colspan="2" class="table-content-single">
                <font class="uportal-channel-text">
                    <xsl:apply-templates/>
                </font>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
