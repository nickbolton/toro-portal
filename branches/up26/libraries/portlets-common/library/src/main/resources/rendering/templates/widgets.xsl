<?xml version="1.0"?>
<!--
  Copyright (C) 2007 Unicon, Inc.

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this distribution.  It is also available here:
  http://www.fsf.org/licensing/licenses/gpl.html

  As a special exception to the terms and conditions of version 
  2 of the GPL, you may redistribute this Program in connection 
  with Free/Libre and Open Source Software ("FLOSS") applications 
  as described in the GPL FLOSS exception.  You should have received
  a copy of the text describing the FLOSS exception along with this
  distribution.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- ############################################### -->
<!-- MISCELLANEOUS WIDGETS -->

<!-- CONTAINER -->
<xsl:template match="layout">
		<!-- Call Function to write all hidden forms first to avoid wrapping forms inadvertently -->
		<xsl:call-template name = "write-all-hidden-forms" />

        <div class="portlet-font portlet {layout-context/@class}">
            <table cellpadding="0" cellspacing="0" border="0" class="portlet-content">
                <xsl:apply-templates />
            </table>
        </div>
</xsl:template>

<!-- HEADER -->
<xsl:template match="section[@type = 'header']">
        <tr>
            <th class="portlet-font header">
                <xsl:attribute name="colspan"><xsl:value-of select="@cols"/></xsl:attribute>
                <xsl:apply-templates />
            </th>
        </tr>
</xsl:template>

<!-- CONTENT -->
<xsl:template match="section[@type = 'content']">
        <tr>
            <xsl:apply-templates />
        </tr>
</xsl:template>

<!-- CONTENT2 -->
<xsl:template match="section[@type = 'content2']">
        <table cellpadding="0" cellspacing="0" border="0" class="content2">
            <tr>
                <xsl:apply-templates />
            </tr>
        </table>
</xsl:template>

<!-- SECONDARY COLUMN -->
<xsl:template match="section[@type = 'secondary']">
        <td>
            <xsl:attribute name="class">portlet-font secondary <xsl:value-of select="@class"/></xsl:attribute>
            <xsl:apply-templates />
        </td>
</xsl:template>

<!-- MAIN -->
<xsl:template match="section[@type = 'main']">
        <td>
            <xsl:attribute name="class">portlet-font main-<xsl:value-of select="@cols"/>col <xsl:value-of select="@class"/></xsl:attribute>
            <xsl:apply-templates />
        </td>
</xsl:template>

<!-- SECTION -->
<xsl:template match="section">
        <div class="{@type} {@class}"><xsl:apply-templates /></div>
</xsl:template>

<!-- PAGING-CONTROLS -->
<xsl:template match="paging">
        <div class="paging"><xsl:apply-templates /></div>
</xsl:template>

<xsl:template match="display-per-page">
        <p class="display-per-page"><xsl:apply-templates /></p>
</xsl:template>

<xsl:template name="paging-controls" match="paging-controls">
      <div class="paging-controls">
	      <span><strong><xsl:value-of select="@firstdisplayed" /> - <xsl:value-of select="@lastdisplayed" /></strong> of <xsl:value-of select="@totalitems" /></span>
	      
	      <xsl:choose>
	         <xsl:when test="@currentpage = 1"><xsl:value-of select="first/descendant::label" /> | <xsl:value-of select="prev/descendant::label" /> | </xsl:when>
	
	         <xsl:otherwise><xsl:apply-templates select="first" /> | <xsl:apply-templates select="prev" /> | </xsl:otherwise>
	      </xsl:choose>
	
	      <xsl:choose>
	         <xsl:when test="@currentpage = @totalpages"><xsl:value-of select="next/descendant::label" /> | <xsl:value-of select="last/descendant::label" /></xsl:when>
	
	         <xsl:otherwise><xsl:apply-templates select="next" /> | <xsl:apply-templates select="last" /></xsl:otherwise>
	      </xsl:choose>
      </div>
</xsl:template>

<!-- SEARCH -->
<xsl:template name="search" match="search">
	<div class="searchbox">
		<xsl:apply-templates />
	</div>
</xsl:template>

<xsl:template match="search/label">
	<xsl:variable name = "id"><xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="..//choice[1]/@handle" />_<xsl:value-of select="..//choice[1]//option[1]/@handle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
	    <xsl:call-template name="escape-for-id-attr">
	       <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$id)" />
	    </xsl:call-template>
    </xsl:variable>
	<label for="{$baseFilteredId}"><xsl:value-of select="." /></label>
</xsl:template>

<!-- LOCATION-NAV -->
<xsl:template name="location-nav" match="location-nav">
	<table><tr><xsl:apply-templates /></tr></table>
</xsl:template>


<xsl:template match="location-nav/select-one[@type='dropdown']">
	<td>
		<xsl:apply-templates />
	</td>
</xsl:template>

<xsl:template match="location-nav/button-group">
	<td><xsl:apply-templates /></td>
</xsl:template>

<!-- ENTITY -->
<!-- Hack to pass entity strings through Warlock -->
<xsl:template name="entity" match="entity">
	<xsl:choose>
		<xsl:when test="@value='lt'">&lt;</xsl:when>
        <xsl:when test="@value='quot'">&#034;</xsl:when>
		<xsl:otherwise></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- REMOVABLE -->
<!-- Removable widget for Messages that can be closed -->
<xsl:template name="removable" match="removable">
	<style>.remove{display:none;}</style>
	<div id="removable_{generate-id(.)}" onclick="this.className='remove'">
		<xsl:apply-templates />
	</div>
</xsl:template>

<!-- DISABLED -->
<xsl:template match="disabled">
    <span class="form-field-disabled"><xsl:apply-templates /></span>
</xsl:template>

<xsl:template name="select-toggle" match="select-toggle">
	<a>
		<xsl:if test = "@targetname">
			<xsl:attribute  name = "href" >javascript:toggleAllCheckBoxes('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="generate-id(ancestor::choice-collection)" />_form','<xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="@targetname" />');void(null);</xsl:attribute>
			
			<xsl:if test = "@title">
				<xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(@title)" /></xsl:attribute>
			</xsl:if>
		</xsl:if>
        <xsl:attribute  name = "class" ><xsl:value-of select="@class" /></xsl:attribute>
		<span><xsl:apply-templates /></span>
	</a>
</xsl:template>

<xsl:template match="select-toggle[text() = 'Select']">
	<input type="checkbox" title="{normalize-space(@title)}" class="select-toggle {@class}">
		<xsl:if test = "@disabled and @disabled != 'false' and @disabled != 'no'">
            <xsl:attribute  name = "disabled" >disabled</xsl:attribute>
			<xsl:attribute  name = "readonly" >readonly</xsl:attribute>
		</xsl:if>
		<xsl:if test = "@checked and @checked != 'false' and @checked != 'no'">
			<xsl:attribute  name = "checked" >checked</xsl:attribute>
		</xsl:if>
		<xsl:attribute  name = "onclick" >checkBoxToggleAll(this,'<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="generate-id(ancestor::choice-collection)" />_form','<xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="@targetname" />');</xsl:attribute>
	</input>
</xsl:template>

<!-- ############################################### -->
<!-- IMAGE -->
<xsl:template match="image" name="image">
	<img>
		<xsl:copy-of select = "@*[name(.) != 'src']"/>
		<xsl:attribute  name = "src" ><xsl:value-of select="$appsRoot" />/rendering/<xsl:value-of select="@src" /></xsl:attribute>
	</img>
</xsl:template>

<!-- ############################################### -->
<!-- LABEL-TEXT -->
<xsl:template match="label-text">
	<h4 class="portlet-font label-text"><xsl:apply-templates /></h4>
</xsl:template>

<!-- ############################################### -->
<!-- TOOLBAR -->
<xsl:template match="toolbar">
        <div>
            <xsl:attribute name="class">toolbar <xsl:value-of select="@class"/></xsl:attribute>
            <xsl:apply-templates />
        </div>
</xsl:template>

<!-- BUTTON-GROUP (within TOOLBAR) -->
<xsl:template match="toolbar/button-group">
        <xsl:choose>
        	<xsl:when test="../view-filter">
		        <div class="button-group-left">
		            <xsl:apply-templates />
		        </div>
        	</xsl:when>
        	<xsl:otherwise>
        		<xsl:apply-templates />
        	</xsl:otherwise>
        </xsl:choose>
</xsl:template>

<!-- TOOLBAR -->
<xsl:template match="toolbar/view-filter">
        <div class="view-filter">
            <xsl:apply-templates />
        </div>
</xsl:template>

<!-- ACTION (within TOOLBAR)-->
<xsl:template match="toolbar/action | toolbar/button-group/action">
        <span class="toolbar_button"><xsl:call-template name = "action" /></span>
</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- NAVBAR -->
<xsl:template match="navbar">
        <div class="navbar"><xsl:apply-templates select="*" /></div>
</xsl:template>

<!-- BUTTON-GROUP -->
<xsl:template match="navbar/button-group">
		<xsl:if test = "position() &gt; 1">|</xsl:if>
        <span class="button_group"><xsl:apply-templates /></span>
</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- RANK (useful with both select-tree and navtree) -->
<xsl:template match="rank">
	<xsl:if test = "number(@level) &gt; 0">
		<span>
			<xsl:call-template name = "repeat" >
				<xsl:with-param name="count" select="'1'" />
				<xsl:with-param name="count-stop" select="@level" />
				<xsl:with-param name="string">. . . . </xsl:with-param>
			</xsl:call-template>
		</span>
	</xsl:if>
	<xsl:apply-templates />
</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- REPEAT -->
<!-- Function that repeats a string a number of specified times -->
<xsl:template name="repeat">
	<xsl:param name="count" />
	<xsl:param name="count-stop" />
	<xsl:param name="string" />
	<xsl:value-of select="$string" />
	<xsl:if test = "$count &lt; $count-stop">
		<xsl:call-template name = "repeat" >
			<xsl:with-param name="count" select="$count+1" />
			<xsl:with-param name="count-stop" select="$count-stop" />
			<xsl:with-param name="string" select="$string" />
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- NAVTREE -->
<xsl:template match="navtree">
        <div class="navtree"><xsl:apply-templates select="*" /></div>
</xsl:template>

<!-- LINK (within NAVTREE) -->
<xsl:template match="link[ancestor::navtree]">
      <!--<xsl:choose>
        	<xsl:when test="../ul/descendant::link">- <xsl:apply-templates select="*" /></xsl:when>
          
        	<xsl:otherwise>+ <xsl:apply-templates select="*" /></xsl:otherwise>
      </xsl:choose> -->
      <xsl:apply-templates select="*" />
</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- SELECT-TREE -->
<xsl:template match="select-tree">
        <div class="select-tree"><xsl:apply-templates select="*" /></div>
</xsl:template>

<!-- LABEL (within SELECT-TREE) -->
<xsl:template match="label[ancestor::select-tree]">
	<xsl:variable name = "selecttree" select = "ancestor::select-tree" />
	<xsl:copy-of select = "@*[name(.)!='opened-class' and name(.)!='closed-class']"/>
	<xsl:if test = "not(@class)">
		<xsl:choose>
        	<xsl:when test="@opened-class">
        		<xsl:choose>
        			<xsl:when test="boolean(../../ul/descendant::li) or number(ancestor::rank/@children) &gt; 0">
        				<xsl:attribute  name = "class" ><xsl:value-of select="@opened-class" /> select-tree</xsl:attribute>
        			</xsl:when>

        			<xsl:otherwise>
        			 	<xsl:attribute  name = "class" ><xsl:value-of select="@closed-class" /> select-tree</xsl:attribute>
        			</xsl:otherwise>
        		</xsl:choose>
        	</xsl:when>
        	<xsl:when test="$selecttree/@opened-class">
        		<xsl:choose>
        			<xsl:when test="boolean(../../ul/descendant::li) or number(ancestor::rank/@children) &gt; 0">
        				<xsl:attribute  name = "class" ><xsl:value-of select="$selecttree/@opened-class" /> select-tree</xsl:attribute>
        			</xsl:when>
        		  
        			<xsl:otherwise>
        			 	<xsl:attribute  name = "class" ><xsl:value-of select="$selecttree/@closed-class" /> select-tree</xsl:attribute>
        			</xsl:otherwise>
        		</xsl:choose>
        	</xsl:when>
        </xsl:choose>
	</xsl:if>
	<xsl:apply-templates />
</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- TABS -->
<xsl:template name="tabs" match="tabs">
      <ul class="portlet-menu tabs">
         <xsl:apply-templates select="tab" mode="header" />
      </ul>
	<div class="tab_body"><xsl:apply-templates select="tab[@selected='selected']/tab-body/*" /></div>
</xsl:template>

<!-- TAB -->
<xsl:template name="tab" match="tab" mode="header">
	<li>
		<!-- <xsl:if test = "@removable='removable'">
			<xsl:attribute  name = "class" >removable</xsl:attribute>
		</xsl:if> -->
		<xsl:apply-templates select = "tab-label" />
	</li>
</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- HBOX -->
<xsl:template name="hbox" match="hbox">
<table class="hbox"><!--  style="padding:0px; margin:0px;" -->
	<xsl:copy-of select = "@*"/>
	<xsl:variable name = "totalshares" select = "count(*[not(@width) and @shares!='no'])" />
	<xsl:variable name = "hbox" select = "." />
	<!--<xsl:variable name = "width" select = "100/count(*)" /> -->
	<!--|<xsl:value-of select="sum(*)" />| -->
	<tr>
	<xsl:for-each select = "*">
		<xsl:variable name = "width">
		</xsl:variable>
		<td>
			<xsl:choose>
				<xsl:when test="@width"><xsl:attribute  name = "width" ><xsl:value-of select="@width" /></xsl:attribute></xsl:when>
				<xsl:when test="@shares and $totalshares &gt; 0"><xsl:attribute  name = "width" ><xsl:value-of select="round(100 * number(@shares) div $totalshares)" />%</xsl:attribute></xsl:when>
				<xsl:when test="$totalshares &gt; 0"><xsl:attribute  name = "width" ><xsl:value-of select="round(100 div $totalshares)" />%</xsl:attribute></xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
			
			<xsl:if test = "@nowrap"><xsl:copy-of select = "@nowrap"/></xsl:if>
            <xsl:if test = "$hbox/@valign"><xsl:copy-of select = "$hbox/@valign"/></xsl:if>
			<xsl:apply-templates select = "." />
		</td>
	</xsl:for-each>
	</tr>
</table>
</xsl:template>

<!-- VBOX -->
<xsl:template name="vbox" match="vbox">
<div class="vbox">
	<xsl:copy-of select = "@*"/>
	<xsl:for-each select = "*">
		<div class="vbox-element"><xsl:apply-templates select = "." /></div>
	</xsl:for-each>
</div>
</xsl:template>

<!-- BOX -->
<xsl:template name="box" match="box">
	<xsl:apply-templates />
</xsl:template>

<!-- SPACER -->
<xsl:template name="spacer" match="spacer">
	<xsl:text>&#160;</xsl:text>
</xsl:template>


<!-- ############################################### -->

<!-- ############################################### -->
<!-- DUPLICATE -->
<xsl:template name="duplicate" match="duplicate">
	<xsl:if test = "@times &gt; 0">
		<xsl:call-template name = "repeatIt" >
			<xsl:with-param name="times" select="@times" />
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<xsl:template name="repeatIt">
	<xsl:param name="count" select="1" />
	<xsl:param name="times" select="0" />

	<xsl:if test = "$times &gt; 0">
		<xsl:choose>
			<!-- If repeating an attribute then do value of -->
			<xsl:when test="count(.|../@*)=count(../@*)"><xsl:value-of select="." /></xsl:when>
		  	<!-- If not an attribute, apply-templates on it -->
			<xsl:otherwise><xsl:apply-templates /></xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test = "$count &lt; $times">
			<xsl:call-template name = "repeatIt" >
				<xsl:with-param name="count" select="$count+1" />
				<xsl:with-param name="times" select="$times" />
			</xsl:call-template>
		</xsl:if>
	</xsl:if>
	
</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- Convert Element and Attribute elements back to original XML state -->
	<xsl:template match="element">
		<xsl:element  name = "{@name}">
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	<xsl:template match="attribute">
		<xsl:attribute  name = "{@name}" ><xsl:value-of select="@value" /></xsl:attribute>
	</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- NO-ESCAPE to prevent output escaping -->

	<xsl:template match="no-escape">
		<xsl:value-of select="." disable-output-escaping="yes" />
	</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- URL-REWRITE to convert <a href="" /> and <form action="" /> to portlet friendly URLs -->

	<xsl:template match="url-rewrite/pattern">
	</xsl:template>

	<!-- APPLY PATTERNs -->
	<!-- Recursive function for applying url-rewrite patterns to strings -->
	<xsl:template name="apply-patterns">
		<xsl:param name="string" />
		<xsl:param name="pos" />
		<xsl:param name="patterns" />
		<xsl:choose>
			<xsl:when test="$pos &gt; count($patterns)">
				<xsl:value-of select="$string" />			
			</xsl:when>
		  
			<xsl:otherwise>
				<xsl:variable name = "currentPattern" select = "$patterns[$pos]" />
				<xsl:variable name = "newString">
					<xsl:choose>
						<xsl:when test="$currentPattern/@match-type = 'starts-with' and starts-with($string,$currentPattern/match/text())"><xsl:value-of select="$currentPattern/replace-with" /><xsl:value-of select="substring-after($string,$currentPattern/match/text())" /></xsl:when>
						<xsl:otherwise><xsl:value-of select="$string" /></xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:call-template name = "apply-patterns" >
					<xsl:with-param name="string" select="$newString" />
					<xsl:with-param name="pos" select="$pos+1" />
					<xsl:with-param name="patterns" select="$patterns" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="url-rewrite/body">
		<!-- Write Hidden Forms for any <a/> tags that need rewriting (in case they are within a form already) -->
		<!--<xsl:apply-templates select="descendant::a" mode="write-forms" /> -->
		<!-- Render Body with replacements -->
		<xsl:apply-templates />
	</xsl:template>

	<!-- URL-REWRITE//... -->
	<!-- Re-write urls for img, bgsound, embed, input [@type=image], and script tags -->
	<xsl:template match="url-rewrite//img | url-rewrite//IMG | url-rewrite//bgsound | url-rewrite//BGSOUND | url-rewrite//embed | url-rewrite//EMBED | url-rewrite//input[@type='image'] | url-rewrite//INPUT[@type='image'] | url-rewrite//script | url-rewrite//SCRIPT">
		<xsl:variable name = "src"><xsl:choose>
        	<xsl:when test="@src"><xsl:value-of select="@src" /></xsl:when>
        	<xsl:when test="@SRC"><xsl:value-of select="@SRC" /></xsl:when>
        	<xsl:otherwise></xsl:otherwise>
        </xsl:choose></xsl:variable>
        
		<xsl:variable name = "url">
			<xsl:call-template name = "apply-patterns" >
				<xsl:with-param name="string" select="$src" />
				<xsl:with-param name="pos" select="1" />
				<xsl:with-param name="patterns" select="ancestor::url-rewrite/pattern" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name = "elementName">
			<xsl:call-template name = "to-lowercase" >
				<xsl:with-param name="string" select="name(.)" />
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:element name = "{$elementName}">
			<xsl:attribute  name = "src" ><xsl:value-of select="$url" /></xsl:attribute>
			<xsl:apply-templates select = "@*[name(.) != 'src' and name(.) != 'SRC']" mode="attribute-to-lowercase" />
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

	<!-- URL-REWRITE//A -->
	<xsl:template match="url-rewrite//a | url-rewrite//A">
		<xsl:variable name = "id" select = "generate-id(.)" />
		<xsl:variable name = "url-rewrite" select = "ancestor::url-rewrite" />
		<xsl:variable name = "cc" select = "//choice-collection[@handle=$url-rewrite/@cc-handle]" />
		<xsl:variable name = "formId" select = "concat($filteredNameSpace,'_',generate-id($cc),'_form')" />
		<xsl:variable name = "urlInputId" select = "concat($filteredNameSpace,'_',$url-rewrite/@cc-handle,'_',$url-rewrite/@choice-handle,'_',$url-rewrite/@option-handle)" />
		<!-- Account for case where href attribute is uppercase or lowercase -->
		<xsl:variable name = "ahref"><xsl:choose>
        	<xsl:when test="@href"><xsl:value-of select="@href" /></xsl:when>
        	<xsl:when test="@HREF"><xsl:value-of select="@HREF" /></xsl:when>
        	<xsl:otherwise></xsl:otherwise>
        </xsl:choose></xsl:variable>
		<xsl:variable name = "url">
			<xsl:call-template name = "apply-patterns" >
				<xsl:with-param name="string" select="$ahref" />
				<xsl:with-param name="pos" select="1" />
				<xsl:with-param name="patterns" select="ancestor::url-rewrite/pattern" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name = "isExternalLink" select="string(contains($url,'http:') or contains($url,'ftp:') or contains($url,'mailto:') or contains($url,'news:') or contains($url,'gopher:') or contains($url,'telnet:') or starts-with($url,'#'))" />
		
		<xsl:variable name = "hrefLessSearchAndAnchor">
			<xsl:call-template name = "removeSearchAndAnchorFromURL" >
				<xsl:with-param name="string" select="$url" />
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:variable name = "href">
			<xsl:choose>
				<xsl:when test="$isExternalLink='false'">javascript:document.getElementById('<xsl:value-of select="$urlInputId" />').value='<xsl:value-of select="$hrefLessSearchAndAnchor" />';document.getElementById('<xsl:value-of select="$formId" />').submit();</xsl:when>
				<xsl:otherwise><xsl:value-of select="$url" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:element name = "a">
			<!-- href -->
            <xsl:attribute  name = "href" ><xsl:value-of select="$href" /></xsl:attribute>
			<xsl:apply-templates select = "@*[name(.) != 'href' and name(.) != 'HREF' and name(.) != 'target' and name(.) != 'TARGET']" mode="attribute-to-lowercase" />
			<!-- target -->
			<xsl:choose>
                <xsl:when test="$isExternalLink = 'true' and starts-with($href,'#')"><!-- no target needed --></xsl:when>
				<xsl:when test="$isExternalLink = 'true' and (not(@target) or @target='_self' or @target='_parent' or @target='_top')">
					<xsl:attribute  name = "target" >_blank</xsl:attribute>
				</xsl:when>
				<xsl:when test = "$isExternalLink = 'true' and @target"><xsl:attribute  name = "target" ><xsl:value-of select="@target" /></xsl:attribute></xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>
	
	<!-- URL-REWRITE//FORM -->
	<xsl:template match="url-rewrite//form | url-rewrite//FORM">
		<!-- Account for case where target is uppercase or lowercase -->
		<xsl:variable name = "target"><xsl:choose>
        	<xsl:when test="@target"><xsl:value-of select="@target" /></xsl:when>
        	<xsl:when test="@TARGET"><xsl:value-of select="@TARGET" /></xsl:when>
        	<xsl:otherwise></xsl:otherwise>
        </xsl:choose></xsl:variable>
		<xsl:variable name = "action"><xsl:choose>
        	<xsl:when test="@action"><xsl:value-of select="@action" /></xsl:when>
        	<xsl:when test="@ACTION"><xsl:value-of select="@ACTION" /></xsl:when>
        	<xsl:otherwise></xsl:otherwise>
        </xsl:choose></xsl:variable>
		<xsl:variable name = "url">
			<xsl:call-template name = "apply-patterns" >
				<xsl:with-param name="string" select="$action" />
				<xsl:with-param name="pos" select="1" />
				<xsl:with-param name="patterns" select="ancestor::url-rewrite/pattern" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:element name = "form">
			<xsl:attribute  name = "action" ><xsl:value-of select="$url" /></xsl:attribute>
			<xsl:apply-templates select = "@*[name(.) != 'target' and name(.) != 'TARGET' and name(.) != 'action' and name(.) != 'ACTION']" mode="attribute-to-lowercase" />
			<!-- target -->
			<xsl:choose>
				<xsl:when test="$target = '' or $target='_self' or $target='_parent' or $target='_top' or $target='_SELF' or $target='_PARENT' or $target='_TOP'">
					<xsl:attribute  name = "target" >_blank</xsl:attribute>
				</xsl:when>
				<xsl:when test = "$target != ''"><xsl:attribute  name = "target" ><xsl:value-of select="$target" /></xsl:attribute></xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates />
		</xsl:element>
	</xsl:template>

	<!-- Remove Search and Anchor from URL -->
	<xsl:template name="removeSearchAndAnchorFromURL">
		<xsl:param name="string" />

		<xsl:variable name = "stringLessSearch">
			<xsl:choose>
				<xsl:when test="contains($string,'?')"><xsl:value-of select="substring-before($string,'?')" /></xsl:when>
			  
				<xsl:otherwise><xsl:value-of select="$string" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="contains($stringLessSearch,'#')"><xsl:value-of select="substring-before($stringLessSearch,'#')" /></xsl:when>
		  
			<xsl:otherwise><xsl:value-of select="$stringLessSearch" /></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- TEXT-TO-MARKUP to convert \n to <br/>, etc -->

  	<xsl:template match="text-to-markup">
   		<xsl:call-template name="string-to-element">
  			<xsl:with-param name="string" select="text()" />
   			<xsl:with-param name="replace" select="'&#010;'" />
   			<xsl:with-param name="with-element" select="'br'" />
   		</xsl:call-template>
   	</xsl:template>

	<!-- If we want to use text-to-markup to string tags -->
	<!--<xsl:template match="text()[ancestor::text-to-markup]">
		<xsl:call-template name="string-to-element">
			<xsl:with-param name="string" select="." />
			<xsl:with-param name="replace" select="'&#010;'" />
			<xsl:with-param name="with-element" select="'br'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="node()[ancestor::text-to-markup]">
		<xsl:apply-templates />
	</xsl:template> -->

<!-- ############################################### -->

<!-- ############################################### -->
<!-- MARKUP-TO-TEXT to strip markup and convert \n to <br/>, etc -->

	<xsl:template match="text()[ancestor::markup-to-text]">
		<xsl:call-template name="string-to-element">
			<xsl:with-param name="string" select="." />
			<xsl:with-param name="replace" select="'&#010;'" />
			<xsl:with-param name="with-element" select="'br'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="node()[ancestor::markup-to-text]">
		<xsl:apply-templates />
	</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- PERMITTED-MARKUP to pass through xhtml markup -->

	<xsl:template match="*[ancestor::permitted-markup]">
		<xsl:choose>
			<!-- If current node is listed in permitted-markup/@allowed, or permitted-markup/@allowed is a wildcard then copy  -->
			<xsl:when test="(ancestor::permitted-markup/@allowed = '*' and contains($XHTMLTags, concat('|',local-name(),'|'))) or contains(ancestor::permitted-markup/@allowed, concat('|',local-name(),'|'))">
				<xsl:copy> 
					<xsl:copy-of select = "@*"/>
					<xsl:apply-templates />
				</xsl:copy>
			</xsl:when>
		    <!-- Else, just apply templates to children -->
			<xsl:otherwise>
				<xsl:apply-templates />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- AJAX-CALLBACK-URL ignored by normal rendering flow -->
	<xsl:template match="sso-context">
		<xsl:variable name = "sequences" select = "//sequence" />
		<xsl:if test = "$sequences">
			<xsl:variable name = "sequenceIDs">"<xsl:value-of select="concat('sequencer_',$filteredNameSpace,'_',generate-id($sequences[1]))" />"<xsl:for-each select = "$sequences[position() &gt; 1]">,"<xsl:value-of select="concat('sequencer_',$filteredNameSpace,'_',generate-id(.))" />"</xsl:for-each></xsl:variable>
            <script language="JavaScript" type="text/javascript">
                 UniconURLSequencer.setFormParameters([<xsl:value-of select="$sequenceIDs" />]<xsl:if test = "ajax-callback-url">,"ajax","<xsl:value-of select="ajax-callback-url" />"</xsl:if>);
            </script>
		</xsl:if>
	</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- SEQUENCE -->
	<xsl:template match="sequence" name="sequence">
	    <xsl:param name="id" select="generate-id(.)" />
	    <xsl:param name="formOnly" select="'false'" />
	    <xsl:variable name = "target">
	    	<xsl:choose>
	    		<!-- Create unique window name, so more than 1 can be displayed within portal if needed for some reason -->
	    		<xsl:when test="../iframe"><xsl:value-of select="../iframe/@handle" /></xsl:when>
	    		<xsl:when test="ancestor::link/@target"><xsl:value-of select="ancestor::link/@target" /></xsl:when>
	    	</xsl:choose>
	    </xsl:variable>

	    <xsl:variable name = "JSSequencerObject" select = "concat('sequencer_',$filteredNameSpace,'_',$id)" />
	    
	    <!-- write form if formOnly (needed to prewrite forms to keep from nesting) or if sibling is an iframe (no prewriting done) -->
	    <xsl:if test = "$formOnly = 'true' or ../iframe">
			<form action="{$appsRoot}/rendering/html/sso/resetPage.html" id="{concat($JSSequencerObject,'_resetForm')}" target="{$target}" method="get" enctype="application/x-www-form-urlencoded"><input type="hidden" name="guid" id="{concat($JSSequencerObject,'_resetForm_guid')}" value="" /></form>
			<xsl:for-each select = "send">
			    <xsl:variable name = "formName"><xsl:value-of select="concat($filteredNameSpace,'_',generate-id(.),'_form')" /></xsl:variable>
				<form name="{$formName}" id="{$formName}" action="{@action}" method="{@method}" target="{$target}" style="display:inline;">
					<xsl:apply-templates select="parameter/value" mode="hidden-fields" />
					<noscript>
						<input type="submit" value="Step {position()}:{normalize-space(../../label/text())}" title="{normalize-space(../../description/text())}"></input>
					</noscript>
				</form>
			</xsl:for-each>
	    </xsl:if>
	    
	    <!-- if not formOnly then write rest of code for sequence -->
	    <xsl:if test = "not($formOnly = 'true')">
            <script language="JavaScript" type="text/javascript">
                UniconSSOSequences["<xsl:value-of select="$JSSequencerObject" />"] = new UniconURLSequencer("<xsl:value-of select="$JSSequencerObject" />");

	            /* Begin repeatable section */
	            <xsl:for-each select = "send">
				    <xsl:variable name = "formName"><xsl:value-of select="concat($filteredNameSpace,'_',generate-id(.),'_form')" /></xsl:variable>
	            	UniconSSOSequences["<xsl:value-of select="$JSSequencerObject" />"].add("<xsl:value-of select="$formName" />"<xsl:if test = "@id">,"<xsl:value-of select="@id" />"</xsl:if>);
	            </xsl:for-each>
	            
	            /* End repeatable section */
	            <xsl:if test = "@current = 'true' and ancestor::auto-submit">
		            if (window.addEventListener)
		            {
		            	/* Submit onload to avoid Safari bug which only would load first iframe */
		                window.addEventListener("load", UniconSSOSequences["<xsl:value-of select="$JSSequencerObject" />"].start, false);
		            }
		            else
		            {
	            		UniconSSOSequences["<xsl:value-of select="$JSSequencerObject" />"].start();
		            }
	            </xsl:if>
			</script>
			<xsl:apply-templates />
	    </xsl:if>
    	
	</xsl:template>
	
	
<!-- SEQUENCE / SEQUENCE-START-TRIGGER -->
	<xsl:template match="sequence/sequence-start-trigger"></xsl:template>
	
<!-- ############################################### -->

<!-- ############################################### -->
<!-- AUTO-SUBMIT / IFRAME -->
	<xsl:template match="auto-submit/iframe">
		<xsl:copy> 
			<xsl:copy-of select = "@*[name(.) != 'handle']"/>
			<!-- handle -->
			<xsl:attribute  name = "name" ><xsl:value-of select="@handle" /></xsl:attribute>
			<!--<xsl:attribute  name = "id" ><xsl:value-of select="@handle" /></xsl:attribute> -->
			<xsl:apply-templates />
		</xsl:copy> 
	</xsl:template>

	<xsl:template match="auto-submit/label|auto-submit/description"></xsl:template>


	
<!-- ############################################### -->

<!-- ############################################### -->
<!-- LINK / SEND -->

	<xsl:template match="link//send[position() = last()]" name="link_send">
		<xsl:param name="class" />
		<xsl:param name="isDuplicate" />
	    <xsl:param name="id" select="generate-id(.)" />
	    <xsl:param name="formOnly" select="'false'" />
	    <xsl:variable name = "link" select = "ancestor::link" />
	    <xsl:variable name = "JSSequencerObject" select = "concat('sequencer_',$filteredNameSpace,'_',generate-id(ancestor::sequence))" />
	    <xsl:variable name = "target">
	    	<xsl:choose>
	    		<xsl:when test="$link/@target"><xsl:value-of select="$link/@target" /></xsl:when>
	    		<xsl:otherwise>_blank</xsl:otherwise>
	    	</xsl:choose>
	    </xsl:variable>

		<!--<xsl:choose>
			<xsl:when test="$formOnly = 'true'">
		    	<form name="{$filteredNameSpace}_{$id}_form" id="{$filteredNameSpace}_{$id}_form" action="{@action}" method="{@method}" target="{$target}" style="display:inline;">
					<xsl:apply-templates select="parameter/value" />
					<noscript>
						<input type="submit" name="SubmitToIframe" value="{$link/label/text()}" title="{normalize-space($link/description/text())}"></input>
					</noscript>
				</form>
			</xsl:when>
		  
			<xsl:otherwise> -->
				<a title="{normalize-space($link/description/text())}">
					<!--<xsl:attribute  name = "href" >javascript:document.getElementById('<xsl:value-of select="concat($filteredNameSpace,'_',$id,'_form')" />').submit();</xsl:attribute> -->
                    <!--<xsl:attribute  name = "href" >javascript:<xsl:value-of select="$JSSequencerObject" />.start();</xsl:attribute>-->
					<xsl:attribute  name = "href" >javascript:UniconSSOSequences['<xsl:value-of select="$JSSequencerObject" />'].start();</xsl:attribute>
					<xsl:if test = "$link/properties">
						<xsl:attribute  name = "onclick" >window.open('','<xsl:value-of select="normalize-space($link/@target)" />','<xsl:value-of select="normalize-space($link/properties)" />')</xsl:attribute>
					</xsl:if>
					<xsl:choose>
						<xsl:when test="$class != ''">
							<xsl:attribute  name = "class" ><xsl:value-of select="$class" /></xsl:attribute>
							<xsl:apply-templates select = "$link/image" mode="display" />
							<span class="{$class}"><xsl:value-of select="$link/label/text()" /></span>
						</xsl:when>
						<xsl:when test="$link/@class">
							<xsl:attribute  name = "class" ><xsl:value-of select="$link/@class" /></xsl:attribute>
							<xsl:apply-templates select = "$link/image" mode="display" />
							<span class="{$link/@class}"><xsl:value-of select="$link/label/text()" /></span>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select = "$link/image" mode="display" />
							<xsl:value-of select="$link/label/text()" />
						</xsl:otherwise>
					</xsl:choose>
				</a>
			<!--</xsl:otherwise>
		</xsl:choose> -->
		
	</xsl:template>

<!-- LINK / REFRESH -->
	<xsl:template match="link/refresh">
		<xsl:variable name = "class" select = "../@class" />
		<a title="{normalize-space(description/text())}" href="{$actionUrl}">
			<xsl:choose>
				<xsl:when test="$class != ''">
					<xsl:attribute  name = "class" ><xsl:value-of select="$class" /></xsl:attribute>
					<span class="{$class}"><xsl:value-of select="label/text()" /></span>
				</xsl:when>
			  
				<xsl:otherwise>
					<xsl:value-of select="label/text()" />
				</xsl:otherwise>
			</xsl:choose>
		</a>
	</xsl:template>
	
<!-- Don't copy <link/> element (i.e.xhtml) if it has a refresh or send descendant -->	
	<xsl:template match="link[descendant::refresh or descendant::send]">
		<xsl:apply-templates />
	</xsl:template>

<!-- Hide contents of link properties -->	
	<xsl:template match="link/label|link/description|link/properties|link/image"></xsl:template>

<!-- LINK / IMAGE -->
<!-- Display Image within link when explicitly called for -->	
	<xsl:template match="link/image" mode="display">
		<xsl:call-template name = "image" />
	</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- SEND / PARAMETER as hidden fields -->
	<xsl:template match="send/parameter/value" mode="hidden-fields">
		<input type="hidden" name="{../@name}" value="{text()}"></input>
	</xsl:template>

<!-- SEND / PARAMETER as a url -->
	<xsl:template match="send/parameter/value" mode="url"><xsl:value-of select="../@name" />=<xsl:value-of select="text()" /><xsl:if test = "position() != last()">&amp;</xsl:if></xsl:template>

<!-- otherwise ignore SEND / PARAMETER -->
	<xsl:template match="send/parameter/value"></xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- TEXT-INPUT to specify extra text options like TinyMCE rich text editing -->

	<xsl:template match="text-input[@format='rte']">
		<!-- tinyMCE -->
		<script language="javascript" type="text/javascript" src="{$appsRoot}/rendering/javascript/tiny_mce/tiny_mce.js"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
		<script language="javascript" type="text/javascript" src="{$appsRoot}/rendering/javascript/tinyMCEInitCode.js"><xsl:comment>//comment to keep closing script tag for XHTML to HTML support</xsl:comment></script>
		<!-- /tinyMCE -->	
		<xsl:apply-templates />
	</xsl:template>
<!-- ############################################### -->
<!-- HINT -->
	<xsl:template match="hint">
		<!--<xsl:variable name = "hintDescription" select = "normalize-space(description/text())" /> -->
		<xsl:variable name = "hintDescription"><xsl:call-template name = "escape-for-js-var-attr" >
				<xsl:with-param name="string" select="normalize-space(description/text())" />
			</xsl:call-template></xsl:variable>
		<a href="javascript:alert('{$hintDescription}');" class="hint" title="{$hintDescription}">
			<xsl:value-of select="normalize-space(label/text())" />
		</a>
	</xsl:template>

<!-- ############################################### -->

<!-- ############################################### -->
<!-- ATTACH-BEHAVIORS -->
	<xsl:template match="attach-behaviors">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="behavior">
        <script language="javascript" type="text/javascript">
            AcademusApps.<xsl:value-of select="@type" />("<xsl:value-of select="@ref-css" />");
        </script>
    </xsl:template>
<!-- ############################################### -->

<!-- ############################################### -->
<!-- SELECT-ROW -->
	<xsl:template match="select-row">
        <a name="hidden-anchor-{@id}" class="hidden-anchor"></a>
        <xsl:apply-templates />
    </xsl:template>

<!-- ############################################### -->
	
</xsl:stylesheet>

