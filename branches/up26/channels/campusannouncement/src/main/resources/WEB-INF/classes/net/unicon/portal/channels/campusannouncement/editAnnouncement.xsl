<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
    
<!--<xsl:variable name="SELECTED_GROUP_ID"><xsl:value-of select="/announcement-edit/announcement/@category-id"/></xsl:variable>


    <xsl:template match="/">
        <textarea rows="4" cols="40">
          <xsl:copy-of select = "*"/> 
          <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
          <parameter name="pageChannel"><xsl:value-of select="$pageChannel" /></parameter>
          <parameter name="channel_admin"><xsl:value-of select="$channel_admin" /></parameter>
           </textarea>
    
        <xsl:apply-templates />
    </xsl:template> -->
    
    <xsl:template match="/announcement-edit">
    
		<xsl:choose>
			<xsl:when test="./announcement/@announcement-id = '-1'">
				<xsl:call-template name="links">
					<xsl:with-param name="curPage">add</xsl:with-param>
				</xsl:call-template>			
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="links">
					<xsl:with-param name="curPage">edit</xsl:with-param>
				</xsl:call-template>			
			</xsl:otherwise>
		</xsl:choose>
	
		<h2 class="page-title">
			<xsl:choose>
				<xsl:when test="./announcement/@announcement-id = '-1'">Add Announcement</xsl:when>
				<xsl:otherwise>Edit Announcement</xsl:otherwise>
			</xsl:choose>		
		</h2>
		
		<div class="bounding-box1">
			<form action="{$baseActionURL}?command=edit-announcement" method="post">
				<input type="hidden" name="sub-command" value="select-groups" />
				<input type="hidden" name="announcement-id" value="{./announcement/@announcement-id}" />
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
					  <td class="table-light-left" align="right" nowrap="nowrap">Date</td>
					  <td class="table-content-right" align="left" width="100%">
						<xsl:value-of select="./announcement/@date" />
					  </td>
					</tr>
					<!--
					<tr>
					  <td class="table-light-left" align="right" nowrap="nowrap">Group</td>
					  <td class="table-content-right" align="left" width="100%">
						<select name="category-id">
						  <xsl:apply-templates select="./group" />
						</select>
					  </td>
					</tr>
					-->
					<tr>
					  <td class="table-light-left" valign="top" align="right" nowrap="nowrap"><label for="cacaea1">Announcement</label></td>
					  <td class="table-content-right" align="left" width="100%">
						<textarea name="message" cols="40" id="cacaea1">
						  <xsl:value-of select="./announcement/announcement-body" />
						</textarea>
					  </td>
					</tr>
					<tr>
					  <td></td>
					  <td class="table-content-right">
					  Click <b>next</b> button to select Groups/Persons who can view this announcement
					  </td>
					</tr>
					<tr>
					  <td colspan="2" class="table-nav">
						  <xsl:choose>
							<xsl:when test="./announcement/@announcement-id = '-1'">
								<input name="Submit" value="Next" type="submit" class="uportal-button" title="Click to add announcement and go to the next step"/>
								<input name="Cancel" value="Cancel" type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}'" title="Cancel adding this announcement and return to viewing all announcements"/>
							</xsl:when>
							<xsl:otherwise>
								<input name="Submit" value="Next" type="submit" class="uportal-button" title="Click to submit announcement edits and go to the next step"/>
								<input name="Cancel" value="Cancel" type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}'" title="Cancel editing this announcement and return to viewing all announcements"/>
							</xsl:otherwise>
						</xsl:choose>

					  </td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>
<!--
    <xsl:template match="group">
      <xsl:variable name="GROUP_ID"><xsl:value-of select="@group-id"/></xsl:variable>
      <xsl:variable name="GROUP_NAME"><xsl:value-of select="@name"/></xsl:variable>
      <option value="{$GROUP_ID}"><xsl:if test="$SELECTED_GROUP_ID = $GROUP_ID"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if><xsl:value-of select="@name"/></option>
    </xsl:template>
-->
</xsl:stylesheet>
