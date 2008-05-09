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
    
    <xsl:template match="/results">
    	<h2 class="page-title">
			<xsl:choose>
				<xsl:when test="./announcement-edit/announcement/@announcement-id = '-1'">Add Announcement</xsl:when>

				<xsl:otherwise>Edit Announcement</xsl:otherwise>
			</xsl:choose>    	
		</h2>
		
		<div class="bounding-box1">
			<form action="{$baseActionURL}?command=edit-announcement" method="post">
				<input type="hidden" name="sub-command" value="save-announcement" />
				<input type="hidden" name="announcement-id" value="{./announcement-edit/announcement/@announcement-id}" />
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
					  <td class="table-light-left" align="right" nowrap="nowrap">Date</td>
					  <td class="table-content-right" align="left" width="100%">
						<xsl:value-of select="./announcement-edit/announcement/@date" />
						<input type="hidden" name="date">
						  <xsl:attribute name="value">
							<xsl:value-of select="./announcement-edit/announcement/@date" />
						  </xsl:attribute>
						</input>
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
					  <td class="table-light-left" align="right" nowrap="nowrap">Group</td>
					  <td class="table-content-right">
						<xsl:for-each select="./groups/group">
							<xsl:value-of select="@name"/><xsl:if test="count(./groups/group) &gt; 1">;</xsl:if>
							<input type="hidden" name="group_key">
								<xsl:attribute name="value">
								<xsl:value-of select="@key"/>
								</xsl:attribute>
							</input>
							<input type="hidden" name="group_type">
								<xsl:attribute name="value">
								<xsl:value-of select="@type"/>
								</xsl:attribute>
							</input>
						</xsl:for-each>
					  </td>
					</tr>
					<tr>
					  <td class="table-light-left" align="right" nowrap="nowrap">Announcement</td>
					  <td class="table-content-right" align="left" width="100%">
						<xsl:call-template name='smarttext'>
							<xsl:with-param name="body" select="./announcement-edit/announcement/announcement-body" />
						</xsl:call-template>
						 <!-- <xsl:apply-templates select="./announcement-edit/announcement/announcement-body" /> -->
						  <input type="hidden" name="message">
						  <xsl:attribute name="value">
							  <xsl:apply-templates select="./announcement-edit/announcement/announcement-body"/>
						  </xsl:attribute>
						  </input>
					  </td>
					</tr>
					<tr>
					  <td colspan="2" class="table-nav">
						<input name="Submit" value="Submit" type="submit" class="uportal-button" title="To finish editing this announcement, saving the changes"/>
						<input name="Cancel" value="Cancel" type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}'" title="To cancel editing this announcement, returning to viewing the announcements"/>
					  </td>
					</tr>
				</table>
			</form>
        </div>
    </xsl:template>

    <xsl:template match="announcement-body">
        <xsl:apply-templates />
    </xsl:template>

<!--
    <xsl:template match="group">
      <xsl:variable name="GROUP_ID"><xsl:value-of select="@group-id"/></xsl:variable>
      <xsl:variable name="GROUP_NAME"><xsl:value-of select="@name"/></xsl:variable>
      <option value="{$GROUP_ID}"><xsl:if test="$SELECTED_GROUP_ID = $GROUP_ID"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if><xsl:value-of select="@name"/></option>
    </xsl:template>
-->
</xsl:stylesheet>
