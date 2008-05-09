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
  1    Channels  1.0         8/6/2002 4:54:02 PM  Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>
    
    <xsl:param name="baseActionURL">default</xsl:param>

    <xsl:template match="DeleteTopic">
            <!--UniAcc: Layout Table -->
        <table border="0" cellpadding="0" cellspacing="0" align="left" width="100%" class="table-content">
            <tr>
                <td class="table-content-iso">
                        <b>Warning:</b>This operation will permanently delete the Classified Topic(s) you have selected. Are you sure you want to proceed with the delete?
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

    <xsl:template match="DeleteItem">

        <tr>
            <td colspan="2">
                <font class="uportal-channel-text">
                    <b>Selected items:</b>
                </font>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                <font class="uportal-channel-text">
                    <xsl:apply-templates/>
                </font>
            </td>
        </tr>

        <tr class="uportal-background-light">
            <td colspan="2">

                <form action="{$baseActionURL}?uP_root=me&amp;action=DeleteTopicConfirmed" method="post" name="confirmdelete">
			<!-- <input type="hidden" name="action" value="DeleteTopicConfirmed"/> -->

			<input type="submit" name="ok" value="Delete" class="uportal-button"/>
			<img src="{$imagedir}/transparent.gif" border="0" height="10" width="10" alt="" title=""/>
			<input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
                </form>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="DeleteList">

        <tr>
            <td colspan="2" class="th-top-single"><b>Selected items:</b></td>
        </tr>

        <xsl:apply-templates/>

        <xsl:if test="child::topicID">
            <tr>
                <td colspan="2" class="table-light-bottom" style="text-align:center;">

                    <form action="{$baseActionURL}?uP_root=me&amp;action=DeleteTopicConfirmed" method="post" name="confirmdelete">
			    <!-- <input type="hidden" name="action" value="DeleteTopicConfirmed"/> -->

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

                    <form action="{$baseActionURL}?uP_root=me&amp;action=TopicSearch" method="post" name="confirmdelete">
			    <!-- <input type="hidden" name="action" value="TopicSearch"/> -->

			    <input type="submit" name="back" value="Back" class="uportal-button"/>
                    </form>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>

    <xsl:template match="topicID">
        <tr>
            <td colspan="2" class="table-content-single">
                <xsl:value-of select="@name"/>
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
