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
  2    Channels  1.1         4/26/2002 5:34:28 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:36:01 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>
    

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">NewsAdmin_ArticlesMenu</xsl:param>

	
	<xsl:template match="ArticlesMenu[boolean(search_results)]">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
        
        <h2 class="page-title">Edit Articles from Search Results</h2>
        
        <div class="bounding-box1">
			<form action="{$baseActionURL}?admin=yes&amp;action=DeleteArticle" name="DeleteArticle" method="post">
				<xsl:apply-templates select="search_results"/>
			</form>
		</div>
	</xsl:template>

    <xsl:template match="ArticlesMenu">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
        
        <h2 class="page-title">Post &amp; Edit Articles</h2>
        
        <div class="bounding-box1">
			<!--UniAcc: Layout Table -->
			<table border="0" cellpadding="2" cellspacing="0" align="left" width="100%">
				<tr>
					<td style="width:200px;vertical-align:top;">
						<div class="areabox areabox1">
							<a href="{$baseActionURL}?action=CreateArticle&amp;admin=yes" title="To post a new article.">Post a New Article</a><br/>
						</div>
						
						<xsl:if test = "$mayManageTopics = 'true'">
							<div class="areabox areabox2">
								<a href="{$baseActionURL}?action=TopicSearch&amp;admin=yes" title="To view, add, edit, or delete article topics." >Manage Article Topics</a><br/>
							</div>
						</xsl:if>
					</td>
					<td style="vertical-align:top;">
						<div class="areabox areabox3">
							<span class="uportal-label">Find articles to edit:</span><br/><br/>
							
							<xsl:choose>
								<xsl:when test="DateSearch">
									<span>By Date</span>
								</xsl:when>
							  
								<xsl:otherwise>
									<a href="{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=dates" title="To search for articles to edit by Date.">By Date</a>
								</xsl:otherwise>
							</xsl:choose>
							
							<xsl:text> | </xsl:text>

							<xsl:choose>
								<xsl:when test="topic_list">
									<span>By Article Topics</span>
								</xsl:when>
							  
								<xsl:otherwise>
									<a href="{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=topic" title="To search for articles to edit by Topic Name.">By Article Topics</a>
								</xsl:otherwise>
							</xsl:choose>

							<xsl:text> | </xsl:text>

							<a href="{$baseActionURL}?action=ArticleSearch&amp;admin=yes&amp;searchstyle=listall" title="To list all articles for editing.">Show All</a>
	
							<xsl:choose>
								<xsl:when test="topic_list">
									<form action="{$baseActionURL}?action=ArticleSearchResults&amp;admin=yes" method="post" name="search">
										<xsl:apply-templates select="topic_list"/>
									</form>
								</xsl:when>
								<xsl:when test="DateSearch">
									<form action="{$baseActionURL}?action=ArticleSearchResults&amp;admin=yes" method="post" name="search">
										<xsl:apply-templates select="DateSearch"/>
									</form>
								</xsl:when>
								<xsl:otherwise>
								</xsl:otherwise>
							</xsl:choose>
						</div>
					</td>
				</tr>
			</table>
		</div>
    </xsl:template>

    <xsl:template match="topic_list">
		<div style="margin:20px 0px;">
			<table border="0" cellpadding="0" cellspacing="10">
				<tr>
		            <td align="right">
		                <label for="CampusNewsChannel_{$pageName}_topicID">
		                    <span class="uportal-label">Topic:</span>
		                </label>
		                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
		            </td>
			        <td class="uportal-text">
			            <select name="topicID" id="CampusNewsChannel_{$pageName}_topicID" class="uportal-input-text">
			                <xsl:apply-templates/>
			            </select>
			        </td>
			    </tr>
			    <tr>
			    	<td>&#160;</td>
			        <td>
			            <input type="submit" value="Search" name="go" class="uportal-button" title="To return a list of articles for the selected topic."/>
			        </td>
			    </tr>
		    </table>
		</div>    
    </xsl:template>


    <xsl:template match="option">
        <xsl:variable name="value">
            <xsl:value-of select="@value"/>
        </xsl:variable>
        <xsl:variable name="selected">
            <xsl:value-of select="@selected"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$selected = 'yes'">
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

    <xsl:template match="DateSearch">
		<div style="margin:20px 0px;">
			<table border="0" cellpadding="0" cellspacing="10">
	            <xsl:apply-templates/>
			    <tr>
			    	<td>&#160;</td>
			        <td>
			            <input type="submit" value="Search" name="search" class="uportal-button" title="To return a list of articles for the selected date range."/>
			        </td>
			    </tr>
		    </table>
		</div>    
    </xsl:template>

    <xsl:template match="begin_date">
        <tr>
            <td width="" align="right">
                <label for="{name()}">
                    <span class="uportal-label">From:</span>
                </label>
                <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
            </td>
            <xsl:apply-templates/>
        </tr>
    </xsl:template>

    <xsl:template match="end_date">
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>
        <tr>
            <td width="" align="right">
                <label for="{name()}">
                    <xsl:if test="$error='error'">
                        <span class="uportal-channel-warning">*</span>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                    </xsl:if>
                    <span class="uportal-label">To:</span>
                    <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                </label>
            </td>
            <xsl:apply-templates/>
        </tr>
    </xsl:template>

    <xsl:template match="month">
        <xsl:variable name="type">
            <xsl:value-of select="@type"/>
        </xsl:variable>
        <td align="left" width="">
            <select name="{$type}_month" class="uportal-input-text" id="{name(..)}">
                <xsl:apply-templates/>
            </select>
        </td>
    </xsl:template>


    <xsl:template match="day">
        <xsl:variable name="type">
            <xsl:value-of select="@type"/>
        </xsl:variable>
        <td align="center" width="">
            <select name="{$type}_date" class="uportal-input-text">
                <xsl:apply-templates/>
            </select>
        </td>
    </xsl:template>

    <xsl:template match="year">

        <xsl:variable name="type">
            <xsl:value-of select="@type"/>
        </xsl:variable>

        <td align="left" width="">
            <select name="{$type}_year" class="uportal-input-text">
                <xsl:apply-templates/>
            </select>
        </td>
    </xsl:template>


    <xsl:template match="listall">
        <td width="50%" valign="middle">
			<xsl:apply-templates/>
        </td>
    </xsl:template>


    <xsl:template match="search_results">
		<!--UniAcc: Layout Table -->
		<div style="margin:20px 0px;">
			<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td class="table-content-iso">
					<!--UniAcc: Layout Table -->
					<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%">
						<xsl:if test="child::article">
							<tr>
								<td class="th-top-left">
									<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
								</td>
								<td width="45%" class="th-top" scope="col">Article Title</td>
								<td class="th-top-right" scope="col">Article Abstract</td>
							</tr>
						</xsl:if>
						<!-- Apply templates for each article in result set -->
						<xsl:apply-templates/>
						<xsl:if test="child::article">
							<tr>
								<td align="left" colspan="3" class="table-content-single-bottom">
									<a href="{$baseActionURL}?action=ArticleSearchResults&amp;checkbox=all&amp;admin=yes" title="To select all displayed articles for deletion." >Select All</a> | <a href="{$baseActionURL}?action=ArticleSearchResults&amp;checkbox=none&amp;admin=yes" title="To deselect all displayed articles.">Deselect All</a> 
								</td>
							</tr>
						</xsl:if>
					</table>
				</td>
			</tr>
			<xsl:if test="child::article">
			<tr>
				<td class="table-light-single" style="text-align:center;">
					<input type="submit" value="Delete" class="uportal-button"/>
				</td>
			</tr>
			</xsl:if>
			</table>
		</div>
    </xsl:template>

    <xsl:template match="article">
    	<xsl:variable name = "rowStyle"><xsl:choose>
    			<xsl:when test="position() mod 2 = 0">alt-row-even</xsl:when>
    			<xsl:otherwise>alt-row-odd</xsl:otherwise>
		</xsl:choose></xsl:variable>
		<tr>
            <td class="table-light-left {$rowStyle}" style="vertical-align:top;">
                <xsl:if test="@checked = 'checked'">
                    <input type="checkbox" name="{@ID}" value="checked" checked="checked" id="NewsAdmin-Articles{@ID}"/>
                </xsl:if>

                <xsl:if test="@checked = 'no'">
                    <input type="checkbox" name="{@ID}" value="checked" id="NewsAdmin-Articles{@ID}"/>
                </xsl:if>
            </td>
            <td class="table-content-left {$rowStyle}" style="vertical-align:top;">
                <label for="NewsAdmin-Articles{@ID}"><xsl:value-of select="@title"/></label>
				<img src="{$SPACER}" border="0" height="5" width="5" alt="" title=""/>
				<a href="{$baseActionURL}?action=EditArticle&amp;articleID={@ID}&amp;admin=yes"
				onmouseover="swapImage('NAdminEditArticleImage{@ID}','channel_edit_active.gif')"
				onmouseout="swapImage('NAdminEditArticleImage{@ID}','channel_edit_base.gif')">
					<img src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
					id="NAdminEditArticleImage{@ID}" name="NAdminEditArticleImage{@ID}" 
					border="0" alt="Edit This Article" title="Edit This Article" align="middle"/>
				</a>
				<img src="{$SPACER}" border="0" height="5" width="5" alt="" title=""/>
				<a href="{$baseActionURL}?action=DeleteArticle&amp;articleID={@ID}&amp;admin=yes"
				onmouseover="swapImage('NAdminDeleteArticleImage{@ID}','channel_delete_active.gif')"
				onmouseout="swapImage('NAdminDeleteArticleImage{@ID}','channel_delete_base.gif')">
					<img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
					id="NAdminDeleteArticleImage{@ID}" name="NAdminDeleteArticleImage{@ID}" 
					border="0" alt="Delete This Article" title="Delete This Article" align="middle"/>
				</a>
            </td>
            <td class="table-content-right {$rowStyle}" style="vertical-align:top;">
                <xsl:value-of select="@abstract"/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="Empty">
        <tr>
            <td align="center" colspan="3">
                <span class="uportal-channel-warning"><xsl:apply-templates/></span>
            </td>
        </tr>
    </xsl:template>

<!-- JK - added for TT03512 -->
    <xsl:template match="Dates">
        <tr>
            <td align="center" colspan="3">
                <span class="uportal-channel-text"><xsl:apply-templates/></span>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
