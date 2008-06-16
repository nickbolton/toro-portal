<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
  
<!-- Include Files -->
<xsl:include href="common.xsl"/>

  <xsl:template match="/">
    <!--<textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/> 
      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
      <parameter name="pageChannel"><xsl:value-of select="$pageChannel" /></parameter>
      <parameter name="channel_admin"><xsl:value-of select="$channel_admin" /></parameter>
   	</textarea> -->

	<xsl:call-template name="links"/>
	<br/>
    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="table-light">
      <tr>
        <th class="th-left">Date</th>
        <th class="th-right">Announcement</th>
      </tr>
      <xsl:apply-templates select="./campus-announcements/announcement" />
    </table>
    <xsl:call-template name="pageForm" />
  </xsl:template>

  <xsl:template match="announcement">
  <xsl:variable name = "bottomStyle"><xsl:if test = "position() = last()">-bottom</xsl:if></xsl:variable>
    <tr>
      <td class="table-light-left{$bottomStyle}" align="center" nowrap="nowrap">
          <xsl:value-of select="@date" />
      </td>

      <td class="table-content-right{$bottomStyle}" width="100%">
          <xsl:variable name="TYPE"><xsl:value-of select="@type"/></xsl:variable>
          <xsl:choose>
            <xsl:when test="$channel_admin = 'true' and $TYPE = 'CAMPUS'">
              <a href="{$baseActionURL}?command=edit-announcement&amp;announcement-id={@announcement-id}" title="Edit this announcement" onmouseover="swapImage('announcementMessageEditImage{@announcement-id}','channel_edit_active.gif')" onmouseout="swapImage('announcementMessageEditImage{@announcement-id}','channel_edit_base.gif')">
                <b><xsl:value-of select="@category-name" />: </b><xsl:value-of select="announcement-body" />
			    <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0" />
			    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="Edit this announcement" title="Edit this announcement" align="absmiddle" name="announcementMessageEditImage{@announcement-id}" id="announcementMessageEditImage{@announcement-id}"/>
			  </a>
              <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0" />
              <a href="{$baseActionURL}?command=delete-announcement&amp;announcement-id={@announcement-id}" title="To delete this announcement" onmouseover="swapImage('announcementMessageDeleteImage{@announcement-id}','channel_delete_active.gif')" onmouseout="swapImage('announcementMessageDeleteImage{@announcement-id}','channel_delete_base.gif')">
			    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="To delete this announcement" title="To delete this announcement" align="absmiddle" name="announcementMessageDeleteImage{@announcement-id}" id="announcementMessageDeleteImage{@announcement-id}"/>
			  </a>
            </xsl:when>
            <xsl:otherwise>
              <b><xsl:value-of select="@category-name" />: </b><xsl:value-of select="announcement-body" />
            </xsl:otherwise>
          </xsl:choose>
      </td>
    </tr>   
  </xsl:template>
  
</xsl:stylesheet>

