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
  2    Channels  1.1         4/26/2002 5:34:29 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:36:02 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_CreateArticle</xsl:param>

    <!-- Start of XSL Code -->

    <xsl:template match="CreateArticle">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>

        <h2 class="page-title">Create/Edit Article</h2>

        <div style="margin:20px 0px;"><xsl:call-template name = "article-workflow" /></div>
        
        <div class="bounding-box1">        
        
			<xsl:call-template name="autoFormJS"/>

			<xsl:variable name="newsID">
				<xsl:text/><xsl:apply-templates select="newsID"/><xsl:text/>
			</xsl:variable>

			<form action="{$baseActionURL}?action=CheckInputFirst&amp;admin=yes&amp;newsID={$newsID}" method="post" name="createItem" 
				  onSubmit="return(checkNewsAdminFormSubmit(this));">
				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<xsl:apply-templates/>
					<tr>
						<td class="table-nav" colspan="2">
							<input type="hidden" name="submitValue" value="Next"/>
							<input type="submit" name="next" value="Next" class="uportal-button" />
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref = '{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=topic'" title="To cancel create/editing this article and return to Manage Articles" />
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>

    <xsl:template match="error">
        <tr>
            <td class="table-content-single">
                <span class="uportal-channel-warning">ERROR ENCOUNTERED: Please fix field items with a red (*)</span>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="newsID">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="newArticle">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="topic_list">
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>
		<tr>
			<td class="table-light-left" align="right" nowrap="nowrap">
	            <xsl:if test="$error='error'">
	                <span class="uportal-channel-warning">*</span>
	                <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
	            </xsl:if>
	            <label for="NewsAdmin-CreateArticleSelTopicS1">Select Topic:</label>
			</td>
	        <td class="table-content-right" width="100%">
	            <xsl:choose>
	                <xsl:when test="child::Empty">
	                    No Topics Found.  You do not have permission to create News Articles.
	                </xsl:when>
	                <xsl:otherwise>
	                    <select name="topicID" size="1" class="uportal-input-text" id="NewsAdmin-CreateArticleSelTopicS1">
	                        <xsl:apply-templates/>
	                    </select>
	                </xsl:otherwise>
	            </xsl:choose>
			</td>
		</tr>
    </xsl:template>

    <xsl:template match="option">
        <xsl:variable name="value">
            <xsl:value-of select="@value"/>
        </xsl:variable>
        <xsl:variable name="selected">
            <xsl:value-of select="@selected"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$selected = 'yes' or position()=1">
                <option value="{$value}" selected="selected">
                    <xsl:apply-templates/>
                </option>
            </xsl:when>
            <xsl:otherwise>
                <option value="{$value}">
                    <xsl:apply-templates/>
                </option>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="begin_date">
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap">
                <label for="NewsAdmin-CreateArt{name()}">Start Date:</label>
			</td>
			<td class="table-content-right" width="100%">
                <xsl:apply-templates/>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template match="end_date">
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>
        <tr>
            <td class="table-light-left" align="right" nowrap="nowrap">
                <xsl:if test="$error='error'">
                    <span class="uportal-channel-warning">*</span>
                    <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                </xsl:if>
                <label for="NewsAdmin-CreateArt{name()}">End Date:</label>
            </td>
			<td class="table-content-right" width="100%">
                <xsl:apply-templates/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="month">
        <xsl:variable name="type">
            <xsl:value-of select="@type"/>
        </xsl:variable>
            <select name="{$type}_month" class="uportal-input-text" id="NewsAdmin-CreateArt{name(..)}">
                <xsl:apply-templates/>
            </select>
    </xsl:template>


    <xsl:template match="day">
        <xsl:variable name="type">
            <xsl:value-of select="@type"/>
        </xsl:variable>
            <select name="{$type}_date" class="uportal-input-text">
                <xsl:apply-templates/>
            </select>
            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
    </xsl:template>

    <xsl:template match="year">
        <xsl:variable name="type">
            <xsl:value-of select="@type"/>
        </xsl:variable>
            <select name="{$type}_year" class="uportal-input-text">
                <xsl:apply-templates/>
            </select>
            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
    </xsl:template>

    <xsl:template match="title">
        <xsl:variable name="title">
            <xsl:apply-templates/>
        </xsl:variable>
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>
        <tr>
            <td class="table-content-single" colspan="2">
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
                    <tr>
                        <td>
                            <label for="NewsAdmin_CreateArt{name()}">
                                <xsl:if test="$error='error'">
                                    <span class="uportal-channel-warning">*</span>
                                    <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                                </xsl:if>
                                Title (100 characters max):
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" name="title" size="50" maxlength="100" value="{$title}" class="uportal-input-text" id="NewsAdmin_CreateArt{name()}"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>


    <xsl:template match="abstract">
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>
        <tr>
            <td class="table-content-single" colspan="2">
                <!--UniAcc: Layout Table -->
                <table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
                    <tr>
                        <td align="left" width="20%">
                            <label for="NewsAdmin_CreateArt{name()}">
                                <xsl:if test="$error='error'"> 
                                    <span class="uportal-channel-warning">*</span> 
                                    <img src="{$imagedir}/transparent.gif" border="0" height="10" width="5"/> 
                                </xsl:if> 
                                Abstract (500 characters max):
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <textarea name="abstract" rows="2" cols="50" class="uportal-input-text" id="NewsAdmin_CreateArt{name()}">
                                <xsl:apply-templates/>
                            </textarea>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
