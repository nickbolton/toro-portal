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
    <xsl:param name="pageName">NewsAdmin_CreateStory</xsl:param>

    <!-- Start of XSL Code -->

    <xsl:template match="CreateStory">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
    
        <h2 class="page-title">Create/Edit Article</h2>

        <div style="margin:20px 0px;"><xsl:call-template name = "article-workflow" /></div>
        
        <div class="bounding-box1">
			<xsl:variable name="newsID">
				<xsl:text/><xsl:apply-templates select="newsID"/><xsl:text/>
			</xsl:variable>

			<xsl:call-template name="autoFormJS"/>

			<form action="{$baseActionURL}?action=CheckInputSecond&amp;admin=yes&amp;newsID={$newsID}" method="post" name="createItem" 
				  enctype="multipart/form-data" 
				  onSubmit="return(checkNewsAdminFormSubmit(this));">

				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="0" cellspacing="0" align="left" width="100%">
					<tr>
						<th class="th-top-single">News Article Story</th>
					</tr>                
					<xsl:apply-templates select="error"/>
					<tr>
						<td class="table-content-single">    
							<xsl:apply-templates select="story"/>
						</td>
					</tr>
					<tr>
						<td class="table-content-single">
							<span class="uportal-channel-strong">
								<label for="NewsAdmin-{name()}ImageF1">Image:</label>
							</span><img src="{$SPACER}" border="0" height="5" width="5" alt="" title=""/>
							<xsl:if test="child::imagefile">
								<span class="uportal-channel-warning"><xsl:apply-templates select="imagefile"/></span> is currently selected
							</xsl:if>
						</td>
					</tr>
					<tr>
						<td class="table-content-single">
							<xsl:if test="/CreateStory/story/@error='error3'">
								<span class="uportal-channel-warning">*</span>
								<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							</xsl:if>
							<input type="file" name="imagefile" enctype="multipart/form-data" id="NewsAdmin-{name()}ImageF1">
								<xsl:attribute name="value">
									<xsl:value-of select="imagefile"/>
								</xsl:attribute>
							</input><br/>
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/><br/>
						</td>
					</tr>
					<tr>
						<td class="table-content-single">
							<span class="uportal-channel-strong">Upload Story:</span><br/>
							<img src="{$SPACER}" border="0" height="3" width="3" alt="" title=""/><br/>
							<label for="NewsAdmin-{name()}FileF1">Upload file for news story (.txt file only)</label><br/>
							<img src="{$SPACER}" border="0" height="3" width="3" alt="" title=""/><br/>
							<xsl:if test="/CreateStory/story/@error='error2'">
								<span class="uportal-channel-warning">*</span>
								<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/><br/>
							</xsl:if>
							<input type="file" name="storyfile" value="browse" enctype="multipart/form-data" id="NewsAdmin-{name()}FileF1" style="text-align: left;"/>
							<input type="submit" name="load" value="Load Story" class="uportal-button" onclick="this.form.submitValue.value=this.value;" /><br/>
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/><br/>
						</td>
					</tr>
					<tr>
						<td>
							<!--UniAcc: Layout Table -->
							<table border="0" cellpadding="0" cellspacing="0" align="center" width="100%" class="table-content-single">
								<tr>
									<td colspan="3">
										<span class="uportal-channel-strong">News Article Layout:</span>
									</td>
								</tr>
								<xsl:apply-templates select="layouttype"/>
								<tr>
									<td>
										<img src="{$imagedir}/NewsArticle1.gif" border="0" alt="Layout with image on left" title="Layout with image on left"/>
									</td>
									<td>
										<img src="{$imagedir}/NewsArticle2.gif" border="0" alt="Layout with image on right" title="Layout with image on right"/>
									</td>
									<td>
										<img src="{$imagedir}/NewsArticle7.gif" border="0" alt="Layout with no image" title="Layout with no image"/>
									</td>
								</tr>                            
							</table>
						</td>
					</tr>
					<tr>
						<td class="table-nav">
							<input type="hidden" name="submitValue" value="Next"/>
							<input type="submit" name="back" value="Back" class="uportal-button" onclick="this.form.submitValue.value=this.value;" />
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<input type="submit" name="next" value="Next" class="uportal-button" onclick="this.form.submitValue.value=this.value;" />
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
                <span class="uportal-channel-warning">ERROR ENCOUNTERED: Please fix field items with red (*)
                    <xsl:if test="/CreateStory/story/@error='error'">- Please input then news story</xsl:if>
                    <xsl:if test="/CreateStory/story/@error='error1'">- Please limit the news story within 2,500 words</xsl:if>
                    <xsl:if test="/CreateStory/story/@error='error2'"> - Only text file can be uploaded as news story</xsl:if>
                    <xsl:if test="/CreateStory/story/@error='error3'"> - Only image file can be uploaded as news image</xsl:if>
                </span>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="newsID">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="story">
        <xsl:variable name="error">
            <xsl:value-of select="@error"/>
        </xsl:variable>
        <xsl:if test="($error='error') or ($error='error1')">
            <span class="uportal-channel-warning">*</span>
            <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
        </xsl:if>
        <label for="NewsAdmin{name()}TA1"><span class="uportal-channel-strong">News Story:</span> (2,500 words max)</label><br/>
        <textarea name="content" rows="15" maxrows="25" cols="50" class="uportal-input-text" id="NewsAdmin{name()}TA1">
            <xsl:apply-templates/>
        </textarea>
    </xsl:template>

    <xsl:template match="layouttype">
        <tr>
            <xsl:for-each select="layout">                
                <xsl:variable name = "position">
                    <xsl:choose>
						<xsl:when test="@id='1'">Layout with image on right</xsl:when>
						<xsl:when test="@id='2'">Layout with image on left</xsl:when>
                        <xsl:otherwise>Layout with no image</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <td>
                    <xsl:choose>
                        <xsl:when test="@selected = 'yes'">
                            <input type="radio" class="uportal-input-text" name="layout" value="{@id}" checked="checked" id="{@id}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <input type="radio" class="uportal-input-text" name="layout" value="{@id}" id="{@id}"/>
                        </xsl:otherwise>
                    </xsl:choose>&#160;            
                    <label for="{@id}">
                        <span class="message"><xsl:value-of select="$position" /></span>
                    </label>
                </td>
            </xsl:for-each>
        </tr>
    </xsl:template>
</xsl:stylesheet>
