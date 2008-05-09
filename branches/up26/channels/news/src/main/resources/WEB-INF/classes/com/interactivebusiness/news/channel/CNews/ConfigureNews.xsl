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
  2    Channels  1.1         4/26/2002 5:34:25 PM Freddy Lopez    fixed UI and
       fixed bugs found during testing phase, this is a release point
  1    Channels  1.0         4/1/2002 11:35:45 AM Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="../common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="pageName">ConfigureNews</xsl:param>

    <xsl:template match="ConfigureNews">
		<xsl:call-template name="channel-toolbar-generic"></xsl:call-template>
		
    	<h2 class="page-title">Customize News Channel</h2>
    	
    	<div class="bounding-box1">
			<form action="{$baseActionURL}?action=save_configuration" method="post" name="subscribe">
				<!--UniAcc: Layout Table -->
				<table border="0" cellpadding="2" cellspacing="0" align="left" width="100%" class="uportal-background-content">
				<tr>
					<xsl:choose>
						<xsl:when test="@layout='2'">
							<td align="center" valign="middle" class="table-content-left">
								 <img src="{$imagedir}/logoright.gif" border="0" alt="" title=""/>
								 <br/>                        
								 <input type="radio" name="layout" value="1" id="News-CNewsConfigRightR1"/>&#160;                        
								 <label for="News-CNewsConfigRightR1">Layout with image on the Right</label>
							</td>
							<td align="center" valign="middle" class="table-content-right">
								 <img src="{$imagedir}/logoleft.gif" border="0" alt="" title=""/>
								 <br/>
								 <input type="radio" name="layout" value="2" checked="checked" id="News-CNewsConfigLeftR1"/>&#160;
								 <label for="News-CNewsConfigLeftR1">Layout with image on the left</label>
							</td>
						</xsl:when>
						<xsl:otherwise>
							<td align="center" valign="middle" class="table-content-left">
								<img src="{$imagedir}/logoright.gif" border="0" alt="" title=""/>
								<br/>                        
								<input type="radio" name="layout" value="1" checked="checked" id="News-CNewsConfigRightR1"/>&#160;                        
								<label for="News-CNewsConfigRightR1">Layout with image on the Right</label>
							</td>
							<td align="center" valign="middle" class="table-content-right">
								<img src="{$imagedir}/logoleft.gif" border="0" alt="" title=""/>
								<br/>
								<input type="radio" name="layout" value="2" id="News-CNewsConfigLeftR1"/>&#160;
								<label for="News-CNewsConfigLeftR1">Layout with image on the left</label>
							</td>
						</xsl:otherwise>
					</xsl:choose>                    
					</tr>
					<tr>
						<td colspan="2" align="left" class="table-content-single">
							<label for="News-CNewsConfigArticlesS1">How many News Articles per Topic Name would you like to display?</label>
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<select name="ItemsPerTopic" class="uportal-input-text" id="News-CNewsConfigArticlesS1">
								<option value="" selected="selected"></option>
								<xsl:choose>
								  <xsl:when test="@itemsPerTopic='3'">
									<option value="3" selected="selected">3</option>
								  </xsl:when>
								  <xsl:otherwise>
									<option value="3">3</option>
								  </xsl:otherwise>
								</xsl:choose>

								<xsl:choose>
					  <xsl:when test="@itemsPerTopic='4'">
						<option value="4" selected="selected">4</option>
					  </xsl:when>
					  <xsl:otherwise>
						<option value="4">4</option>
					  </xsl:otherwise>
								</xsl:choose>

								<xsl:choose>
					  <xsl:when test="@itemsPerTopic='5'">
						<option value="5" selected="selected">5</option>
					  </xsl:when>
					  <xsl:otherwise>
						<option value="5">5</option>
					  </xsl:otherwise>
								</xsl:choose>

								<xsl:choose>
					  <xsl:when test="@itemsPerTopic='6'">
						<option value="6" selected="selected">6</option>
					  </xsl:when>
					  <xsl:otherwise>
						<option value="6">6</option>
					  </xsl:otherwise>
								</xsl:choose>
							</select>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="table-light-single-bottom" style="text-align:center;">
							<input type="submit" name="save" value="Save" class="uportal-button"/>
							<img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
							<input type="submit" name="cancel" value="Cancel" class="uportal-button"/>
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>
</xsl:stylesheet>
