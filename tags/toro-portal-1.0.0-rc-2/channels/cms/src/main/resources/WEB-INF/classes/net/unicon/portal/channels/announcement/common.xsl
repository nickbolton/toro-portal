<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="../global/global.xsl"/>

<!-- parameters -->

<xsl:param name="view"/>
<xsl:param name="current_command" select="$view"/>

<xsl:param name="numberPerPage">5</xsl:param>
<xsl:param name="ID"/>

<!-- permissions -->
<xsl:param name="insert"/>
<xsl:param name="submit"/>
<xsl:param name="delete"/>


<xsl:template name="autoFormJS">
<script language="JavaScript" type="text/javascript" src="javascript/AnnouncementChannel/autoForm.js"></script>
</xsl:template>

<!-- Common -->
<xsl:template name="links">
<!-- <textarea rows="4" cols="40">
            <xsl:copy-of select="*"/>     
</textarea> -->

<!-- UniAcc: Layout Table -->
<table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="views-title">
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" align="absmiddle" alt="Icon of tool-tip indicating the channel options section" title="Icon of tool-tip indicating channel options section"/>
        </td>
        <td class="views" valign="middle" height="26" width="100%">

            <xsl:choose>
                <xsl:when test="$current_command = 'main'">
                    Announcement<img height="1" width="3" src="{$SPACER}"
                    alt="" border="0" /><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif"
                    alt="Selected 'View' icon indicating Announcements View is currently displayed" title="Selected 'View' icon indicating Announcements View is currently displayed" align="absmiddle"/><img 
                    height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                </xsl:when>
              
                <xsl:otherwise>
                    <a href="{$baseActionURL}" title="View announcements" 
                    onmouseover="swapImage('announcementViewImage','channel_view_active.gif')" 
                    onmouseout="swapImage('announcementViewImage','channel_view_base.gif')">
						Announcement<img height="1" width="3" src="{$SPACER}" alt="" 
						border="0" /><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
						name="announcementViewImage" id="announcementViewImage" alt="'View' icon linking to display of announcements" title="'View' icon linking to display of announcements" align="absmiddle" />
					</a>
					<img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="$current_command = 'add'">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_selected.gif"
                     alt="Selected 'Add' icon indicating Add Announcement View is currently displayed" title="Selected 'Add' icon indicating Add Announcement View is currently displayed" align="absmiddle"/>
                </xsl:when>
              
                <xsl:when test="$insert = 'Y'">
                    <a href="{$baseActionURL}?command=add" title="Add announcement"
                    onmouseover="swapImage('announcementAddImage','channel_add_active.gif')" 
                    onmouseout="swapImage('announcementAddImage','channel_add_base.gif')">
						<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
						name="announcementAddImage" id="announcementAddImage" alt="'Add' icon linking to add announcement view" title="'Add' icon linking to add announcement view" align="absmiddle" />
					</a>
                </xsl:when>
              
                <xsl:otherwise>
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_inactive.gif"
                    alt="Inactive 'Add' icon indicating the permission to add an announcement is currently unavailable" title="Inactive 'Add' icon indicating the permission to add an announcement is currently unavailable" align="absmiddle"/>
                </xsl:otherwise>
            </xsl:choose>
            
        </td>
    </tr>
</table>
</xsl:template>

<!-- Added for URL Recognition in Announcement Text -->
<!-- smartext processing -->
<xsl:template name="smarttext">
   <xsl:param name="body"/>
   <xsl:choose>
      <xsl:when test="contains($body, 'http://')">
         <xsl:variable name="first" select="substring-before($body, 'http://')"/>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
             <xsl:call-template name="findlink"><xsl:with-param name="tmp" select="$tmp"/></xsl:call-template>
         </xsl:variable>
         <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$first"/>
         </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('{$link}'));" title="Display"><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($body, 'https://')">
         <xsl:variable name="first" select="substring-before($body, 'https://')"/>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
             <xsl:call-template name="findlink"><xsl:with-param name="tmp" select="$tmp"/></xsl:call-template>
         </xsl:variable>
         <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$first"/>
         </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('{$link}'));" title="Display"><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($body, 'www.')">
         <xsl:variable name="first" select="substring-before($body, 'www.')"/>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
             <xsl:call-template name="findlink"><xsl:with-param name="tmp" select="$tmp"/></xsl:call-template>
         </xsl:variable>
         <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$first"/>
         </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('http://{$link}'));" title="Display"><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($body, 'ftp://')">
         <xsl:variable name="first" select="substring-before($body, 'ftp://')"/>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
             <xsl:call-template name="findlink"><xsl:with-param name="tmp" select="$tmp"/></xsl:call-template>
         </xsl:variable>
         <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$first"/>
         </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('{$link}'));" title="Display"><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($body, 'ftp.')">
         <xsl:variable name="first" select="substring-before($body, 'ftp.')"/>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
             <xsl:call-template name="findlink"><xsl:with-param name="tmp" select="$tmp"/></xsl:call-template>
         </xsl:variable>
         <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$first"/>
         </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('ftp://{$link}'));" title="Display"><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($body, 'mailto:')">
         <xsl:variable name="first" select="substring-before($body, 'mailto:')"/>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
             <xsl:call-template name="findlink"><xsl:with-param name="tmp" select="$tmp"/></xsl:call-template>
         </xsl:variable>
         <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$first"/>
         </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('{$link}'));" title="Display"><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($body, '@')">
         <xsl:variable name="first" select="substring-before($body, '@')"/>
         <xsl:variable name="firstmail">
           <xsl:call-template name="getfirst">
              <xsl:with-param name="first" select='$first'/>
           </xsl:call-template>
         </xsl:variable>
         <xsl:variable name="tmp" select="substring($body, string-length($first)+1)"/>
         <xsl:variable name="link">
           <xsl:choose>
               <xsl:when test="contains($tmp, '&#xA;')">
                   <xsl:choose>
                       <xsl:when test="substring-before(substring-before($tmp,'&#xA;'),'&#x20;')">
                           <xsl:value-of select="substring-before(substring-before($tmp,'&#xA;'),'&#x20;')"/>
                       </xsl:when>
                       <xsl:otherwise><xsl:value-of select="substring-before($tmp,'&#xA;')"/></xsl:otherwise>
                   </xsl:choose>
               </xsl:when>
               <xsl:otherwise>
                   <xsl:if test="contains($tmp, '&#x20;')">
                     <xsl:value-of select="substring-before($tmp,'&#x20;')"/>
                   </xsl:if>
                   <xsl:if test="not(contains($tmp, '&#x20;'))">
                       <xsl:value-of select="$tmp"/>
                   </xsl:if>
               </xsl:otherwise>
           </xsl:choose>
         </xsl:variable>
        <xsl:variable name="last" select="substring($tmp, string-length($link)+1)"/>
        <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="substring($first, 0 ,  string-length($first) - string-length($firstmail) +1 )"/>
        </xsl:call-template>
         <xsl:if test="$link">
            <a href="javascript:void(window.open('mailto:{$firstmail}{$link}'), return false);" title="Display"><xsl:value-of select="$firstmail"/><xsl:value-of select="$link"/></a>&#160;
         </xsl:if>
         <xsl:call-template name="smarttext">
            <xsl:with-param name="body" select="$last"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name='break-line-c'>
          <xsl:with-param name='st' select='$body'/>
        </xsl:call-template>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="findlink">
   <xsl:param name="tmp"/>
   <xsl:choose>
       <xsl:when test="contains($tmp, '&#xA;')">
           <xsl:choose>
               <xsl:when test="substring-before(substring-before($tmp,'&#xA;'),'&#x20;')">
                   <xsl:value-of select="substring-before(substring-before($tmp,'&#xA;'),'&#x20;')"/>
               </xsl:when>
               <xsl:otherwise><xsl:value-of select="substring-before($tmp,'&#xA;')"/></xsl:otherwise>
           </xsl:choose>
       </xsl:when>
       <xsl:otherwise>
           <xsl:if test="contains($tmp, '&#x20;')">
             <xsl:value-of select="substring-before($tmp,'&#x20;')"/>
           </xsl:if>
           <xsl:if test="not(contains($tmp, '&#x20;'))">
               <xsl:value-of select="$tmp"/>
           </xsl:if>
       </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name='getfirst'>
<!-- w/ space -->
  <xsl:param name="first"/>
  <xsl:variable name="tmp" select="substring-after($first, '&#x20;')"/>
  <xsl:choose>
      <xsl:when test="contains($tmp, '&#x20;')">
          <xsl:call-template name="getfirst">
              <xsl:with-param name="first" select="$tmp"/>
          </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
          <xsl:if test="$tmp">
              <xsl:call-template name="getfirstwbreak">
                  <xsl:with-param name="first" select="$tmp"/>
              </xsl:call-template>
          </xsl:if>
          <xsl:if test="not($tmp)">
              <xsl:call-template name="getfirstwbreak">
                  <xsl:with-param name="first" select="$first"/>
              </xsl:call-template>
          </xsl:if>
      </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name='getfirstwbreak'>
  <xsl:param name="first"/>
  <xsl:variable name="tmp" select="substring-after($first, '&#xA;')"/>
  <xsl:choose>
      <xsl:when test="contains($tmp, '&#xA;')">
          <xsl:call-template name="getfirstwbreak">
              <xsl:with-param name="first" select="$tmp"/>
          </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
          <xsl:if test="$tmp"><xsl:value-of select="$tmp"/></xsl:if>
          <xsl:if test="not($tmp)"><xsl:value-of select="$first"/></xsl:if>
      </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name='break-line-c'>
  <xsl:param name='st'/>
  <xsl:if test="$st != '&#xA;' ">
      <xsl:variable name='first' select='substring-before($st,"&#xA;")'/>
      <xsl:variable name='rest' select='substring-after($st,"&#xA;")'/>
      <xsl:choose>
        <xsl:when test='$first or $rest'>
          <xsl:value-of select='$first'/>
          <br/>
          <xsl:if test='$rest'>
            <xsl:call-template name='break-line-c'>
              <xsl:with-param name='st' select='$rest'/>
            </xsl:call-template>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='$st'/>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:if>
  <xsl:if test="$st = '&#xA;'"><br/></xsl:if>

</xsl:template>
</xsl:stylesheet>
