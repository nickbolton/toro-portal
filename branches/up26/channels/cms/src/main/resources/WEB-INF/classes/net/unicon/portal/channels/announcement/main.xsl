<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
  
<!-- Include Files -->
<xsl:include href="common.xsl"/>

  <xsl:template match="/">

    <xsl:call-template name="links"/>

    <xsl:if test="count(./class-announcements/announcement) &gt; 0">
    <!-- UniAcc: Data Table -->
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
            <th id="date" class="th-left">
                Date
            </th>
            <th id="announcement" class="th-right">
                Announcement
            </th>
          </tr>
        <xsl:apply-templates select="./class-announcements/announcement" />
      </table>
    </xsl:if>
    
  </xsl:template>

  <xsl:template match="announcement">
  <xsl:variable name="bottomStyle"><xsl:if test="position() = last()">-bottom</xsl:if></xsl:variable>
    <tr>
      <td headers="date" class="table-light-left{$bottomStyle}" align="center" nowrap="nowrap">
          <xsl:value-of select="@date" />
      </td>

      <td headers="announcement" class="table-content-right{$bottomStyle}" width="100%">
        <xsl:choose>
            <xsl:when test="$submit = 'Y'">
			    <xsl:call-template name='smarttext'>                   <xsl:with-param name="body" select="announcement-body" />                </xsl:call-template>                <a href="{$baseActionURL}?command=edit&amp;ID={@id}" title="Edit this announcement" onmouseover="swapImage('announcementMessageEditImage{@id}','channel_edit_active.gif')" onmouseout="swapImage('announcementMessageEditImage{@id}','channel_edit_base.gif')">
                        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0" />
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="Edit this announcement" title="Edit this announcement" align="absmiddle" name="announcementMessageEditImage{@id}" id="announcementMessageEditImage{@id}"/>
                </a>
            </xsl:when>
            <xsl:otherwise>			    <xsl:call-template name='smarttext'>                    <xsl:with-param name="body" select="announcement-body" />                </xsl:call-template>
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0" />
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif" alt="The permission to edit announcements is currently unavailable" title="The permission to edit announcements is currently unavailable" align="absmiddle" />       
            </xsl:otherwise>
        </xsl:choose>
        <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0" />
        <xsl:choose>
            <xsl:when test="$delete = 'Y'">
                <a href="{$baseActionURL}?command=delete_announcement&amp;ID={@id}" title="Delete this announcement" onmouseover="swapImage('announcementMessageDeleteImage{@id}','channel_delete_active.gif')" onmouseout="swapImage('announcementMessageDeleteImage{@id}','channel_delete_base.gif')">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="To delete this announcement" title="Delete this announcement" align="absmiddle" name="announcementMessageDeleteImage{@id}" id="announcementMessageDeleteImage{@id}"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif" alt="The permission to delete announcements is currently unavailable" title="The permission to delete announcements is currently unavailable" align="absmiddle" />         
            </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>   
  </xsl:template>
  
</xsl:stylesheet>

