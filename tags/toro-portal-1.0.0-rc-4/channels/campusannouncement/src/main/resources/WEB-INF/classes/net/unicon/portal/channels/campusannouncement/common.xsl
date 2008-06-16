<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Import -->
<xsl:import href="../global/campus_announcements_global.xsl"/>
<xsl:include href="../global/toolbar.xsl"/>

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
	<xsl:param name="curPage"/>

	<div class="portlet-toolbar-container">
		
		<xsl:choose>
			<xsl:when test="$curPage = 'view'">
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">View Announcements</xsl:with-param>
					<xsl:with-param name="imagePath">channel_view_base</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">View Announcements</xsl:with-param>
					<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
					<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/></xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>

		<xsl:choose>
			<xsl:when test="$curPage = 'preferences'">
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">Edit Preferences</xsl:with-param>
					<xsl:with-param name="imagePath">channel_edit_base</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="channel-link-generic">
					<xsl:with-param name="title">Edit Preferences</xsl:with-param>
					<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>
					<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=edit-preferences</xsl:with-param>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>		
	</div>
</xsl:template>

<xsl:template name="main-links">
	<xsl:if test="campus-announcements/toolbar/icon[position()=2]='announcer'">
		<xsl:call-template name="channel-link-generic2">
			<xsl:with-param name="title">Add Announcement</xsl:with-param>
			<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=edit-announcement&amp;announcement-id=-1</xsl:with-param>
			<xsl:with-param name="imagePath">channel_add_active</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="channel-link-generic2">
			<xsl:with-param name="title">Manage Announcements</xsl:with-param>
			<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=owned-announcements</xsl:with-param>
			<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>
		</xsl:call-template>
	</xsl:if>
	
	<xsl:if test="campus-announcements/toolbar/icon[position()=1]='admin'">
		<xsl:call-template name="channel-link-generic2">
			<xsl:with-param name="title">Change Administrator</xsl:with-param>
			<xsl:with-param name="imagePath">channel_admin_active</xsl:with-param>
			<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=admin-announcer&amp;admin=yes</xsl:with-param>
		</xsl:call-template>                 
	</xsl:if>	
</xsl:template>



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
            <a href="{$link}" target="_blank" title="Display"><xsl:value-of select="$link"/></a>&#160;
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
            <a href="{$link}" target="_blank" title="Display"><xsl:value-of select="$link"/></a>&#160;
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
            <a href="http://{$link}" target="_blank" title="Display"><xsl:value-of select="$link"/></a>&#160;
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
            <a href="{$link}" target="_blank" title="Display"><xsl:value-of select="$link"/></a>&#160;
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
            <a href="ftp://{$link}" target="_blank" title="Display"><xsl:value-of select="$link"/></a>&#160;
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
            <a href="{$link}" target="_blank" title="Display"><xsl:value-of select="$link"/></a>&#160;
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
            <a href="mailto:{$firstmail}{$link}" target="_blank" title="Display"><xsl:value-of select="$firstmail"/><xsl:value-of select="$link"/></a>&#160;
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

<xsl:template match="br">
    <xsl:copy />
</xsl:template>

</xsl:stylesheet>

