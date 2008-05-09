<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
<!-- for all screens -->
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="baseImagePath">media/net/unicon/portal/channels/rad</xsl:param>
    <xsl:param name="resourceURL">default</xsl:param>
    <xsl:param name="sid">default</xsl:param>
    <xsl:param name="targetChannel" />
    <xsl:param name="root">false</xsl:param>
    <xsl:param name="focusedChannel">
        <xsl:value-of select="$targetChannel" />
    </xsl:param>
    <xsl:param name="backRoot" />
<!-- root URL -->
    <xsl:param name="rgoURL" />
    <xsl:param name="rdoURL" />
<!-- me URL -->
    <xsl:param name="mgoURL" />
    <xsl:param name="mdoURL" />
<!-- unchanged URL -->
    <xsl:param name="goURL" />
    <xsl:param name="doURL" />
    <xsl:param name="isNew">false</xsl:param>
    <xsl:param name="isOpen">false</xsl:param>
<!-- permissions -->
    <xsl:param name="addEvents">Y</xsl:param>
    <xsl:param name="editEvents">Y</xsl:param>
    <xsl:param name="deleteEvents">Y</xsl:param>
    <xsl:param name="writeAccess">Y</xsl:param>    
<!-- current date -->
    <xsl:param name="cur-date">default</xsl:param>
    
<!-- parse channel ID from baseActionURL to make channel form names unique -->
	<!--<xsl:param name="channelID"><xsl:value-of select="substring-after(substring-before($baseActionURL,'.uP'),'.target.')" /></xsl:param> -->
    <xsl:param name="channelID"><xsl:value-of select="substring-before(substring-after($baseActionURL,'.target.'),'.')" /></xsl:param>
<!-- used only for navigation-bar -->
<!-- Set Variable Names for image links -->
    <xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
    <xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
    <xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
    <xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/flash/academus</xsl:variable>
    <xsl:variable name="SPACER">
    <xsl:value-of select="$SKIN_IMAGE_PATH" />/transparent.gif</xsl:variable>
    
    <xsl:variable name="LCAL" select="contains($targetChannel, 'CCalendarUnicon')" />
    
    <xsl:template name="autoFormJS">
		<script language="JavaScript" type="text/javascript" src="javascript/CalenderChannel/autoForm.js"></script>
    </xsl:template>
    
    <xsl:template name="bform">
        <input type="hidden" name="sid" value="{$sid}" />
    </xsl:template>
    
    <xsl:template name="rform">
        <input type="hidden" name="sid" value="{$sid}" />
    </xsl:template>
    
    <xsl:template name="mform">
        <input type="hidden" name="sid" value="{$sid}" />
    </xsl:template>
    
    <xsl:template name="calcombobox"></xsl:template>
	
<!-- Newly Added for URL Recognition in Calendar Notes -->
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

