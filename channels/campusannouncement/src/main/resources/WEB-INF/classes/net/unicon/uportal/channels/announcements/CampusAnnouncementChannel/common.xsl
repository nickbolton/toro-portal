<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- Import -->
<xsl:import href="../../global/global.xsl"/>

<xsl:output method="html" indent="yes" />

<!-- parameters -->

<xsl:param name="current_command">main</xsl:param>
<xsl:param name="pageChannel">campusAnnouncements</xsl:param>
<xsl:param name="channel_admin"/>

<xsl:template name="submissionJS">
	<script language="JavaScript" type="text/javascript" src="javascript/AnnouncementChannel/textCount.js"></script>
</xsl:template>

<!-- Common -->
<xsl:template name="links">

<!-- <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>     
    </textarea> -->

<!-- Put in an HTML comment I can look for -->

<table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="views-title">
            <img border="0" 
            src="{$CONTROLS_IMAGE_PATH}/channel_options.gif"
            alt="" title="channel options" align="absmiddle" />
        </td>
        <td class="views" valign="middle" height="26" width="100%">

            <xsl:choose>
                <xsl:when test="$current_command = 'main'">
                    Announcements<img height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0" /><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif"
                    alt="" title="Currently viewing campus announcements" align="absmiddle"/><img 
                    height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0"/>
                </xsl:when>
              
                <xsl:otherwise>
                    <a href="{$baseActionURL}" title="To view campus announcements" 
                    onmouseover="swapImage('announcementViewImage','channel_view_active.gif')" 
                    onmouseout="swapImage('announcementViewImage','channel_view_base.gif')">Announcements<img 
                    height="1" width="3" src="{$SPACER}"
                    alt="" border="0" /><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
                    alt="" title="View announcements" align="absmiddle" name="announcementViewImage" id="announcementViewImage"/></a><img 
                    height="1" width="3" src="{$SPACER}"
                    alt="" border="0"/>
                </xsl:otherwise>
            </xsl:choose>

          <xsl:if test="$channel_admin = 'true'">
            <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
            <xsl:choose>
                <xsl:when test="$current_command = 'add-announcement'">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif"
                     alt="Currently adding an announcement" title="Currently adding an announcement" align="absmiddle"/>
                </xsl:when>
                <xsl:otherwise>
                    <a href="{$baseActionURL}?command=edit-announcement&amp;announcement-id=-1" title="To add an announcement"
                    onmouseover="swapImage('announcementAddImage','channel_add_active.gif')" 
                    onmouseout="swapImage('announcementAddImage','channel_add_base.gif')"><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
                    alt="" title="Add an announcement" align="absmiddle" name="announcementAddImage" id="announcementAddImage"/></a>
                </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
		  
		    |  
          <xsl:choose>
            <xsl:when test="$current_command = 'edit-preferences'">
              Preferences<img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" src=
              "{$CONTROLS_IMAGE_PATH}/channel_edit_selected.gif"
              alt="Currently editing preferences for this channel" title="Currently editing preferences for this channel" align="absmiddle"/>
            </xsl:when>
            <xsl:otherwise>
              	<a href="{$baseActionURL}?command=edit-preferences" title="To edit preferences for this channel"
              	onmouseover="swapImage('editPreferencesImage','channel_edit_active.gif')" 
              	onmouseout="swapImage('editPreferencesImage','channel_edit_base.gif')">
	              Preferences<img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" src=
	              "{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
	              alt="To edit preferences for this channel" 
				  title="To edit preferences for this channel" 
				  align="absmiddle" name="editPreferencesImage" id="editPreferencesImage"/>
				</a>
            </xsl:otherwise>
          </xsl:choose>
		  
        </td>
    </tr>
</table>
</xsl:template>

</xsl:stylesheet>
