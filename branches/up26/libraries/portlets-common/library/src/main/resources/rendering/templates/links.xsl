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
<!-- PARAMETERS used by links, choices and options -->
<xsl:param name="decisionCollections" select="/descendant::decision-collection" />
<xsl:param name="actionUrl" select="'http://shawn.unicon.net:8088/jsp-examples/result.jsp'" />
<!-- ############################################### -->


<!-- ############################################### -->
<!-- CHOICE-COLLECTION -->
<xsl:template match="choice-collection">
    <xsl:param name="id" select="generate-id(.)" />
    
    <xsl:variable name = "handle" select="./@handle"/>
    <!--<form name="{$filteredNameSpace}_{$id}_form" id="{$filteredNameSpace}_{$id}_form" action="http://localhost:8088/jsp-examples/result.jsp?actionUrl={$actionUrl}" method="post" style="display:inline;" onsubmit="return modValidate(this)"> -->
    <form name="{$filteredNameSpace}_{$id}_form" id="{$filteredNameSpace}_{$id}_form" action="{$actionUrl}" method="post" style="display:inline;" onsubmit="return modValidate(this)">
        <xsl:if test = "descendant::option[contains(@complement-type,'TypeFileUpload')]">
            <xsl:attribute  name = "enctype" >multipart/form-data</xsl:attribute>
        </xsl:if>
        <input type="hidden" id="{$filteredNameSpace}_{$id}_form_defaultAction" name="defaultAction" value=""></input>
        <xsl:apply-templates></xsl:apply-templates>
    </form>
</xsl:template>
<!-- ############################################### -->


<!-- ############################################### -->
<!-- CHOICE -->
<xsl:template match="choice">
        <xsl:apply-templates />
</xsl:template>

<!-- CHOICE (within SELECT-ONE [DROPDOWN])  -->
<xsl:template match="select-one[@type='dropdown']/choice">
    <xsl:variable name = "name"><xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="@handle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$name)" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name = "handle" select="./@handle"/>
    <xsl:variable name = "cc" select="ancestor::choice-collection"/>
    <xsl:variable name = "actionHandle" select="action/@handle"/>
    <xsl:variable name = "selectOne" select = "ancestor::select-one" />
    <xsl:if test = "label">
        <label for="{$baseFilteredId}"><xsl:value-of select="label" /></label>
    </xsl:if>
    <select name="{$name}" id="{$baseFilteredId}" class="{$selectOne/@class}">
        <xsl:if test = "action">
            <xsl:variable name = "ccid" select = "generate-id($cc)" />
            <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form_defaultAction')</xsl:variable>
            <xsl:attribute  name = "onchange" ><xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$actionHandle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$actionHandle" />';document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form').submit();</xsl:attribute>
        </xsl:if>
        <xsl:if test = "$selectOne/@title">
            <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space($selectOne/@title)" /></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="*[name(.)!='action']"></xsl:apply-templates>
    </select>
</xsl:template>

<!-- CHOICE (within SELECT-ONE [LINK or BUTTON]) -->
<xsl:template match="select-one[@type='link' or @type='button']/choice">
    <xsl:variable name = "choiceHandle" select = "@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "currentlySelected"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle)" />
        </xsl:call-template>
    </xsl:variable>
    <input type="hidden" id="{$baseFilteredId}" name="{$choiceCollectionHandle}_{$choiceHandle}" value="{$currentlySelected}"></input>
    <xsl:apply-templates />
</xsl:template>

<!-- CHOICE (within SELECT-ONE [TABS])  -->
<xsl:template match="select-one[@type='tabs']/choice">
    <xsl:variable name = "choiceHandle" select = "@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "currentlySelected"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle)" />
        </xsl:call-template>
    </xsl:variable>
    <input type="hidden" id="{$baseFilteredId}" name="{$choiceCollectionHandle}_{$choiceHandle}" value="{$currentlySelected}"></input>
    <ul class="portlet-menu tabs">
        <xsl:apply-templates select="*[name(.)!='action']"></xsl:apply-templates>
    </ul>
</xsl:template>

<!-- CHOICE (within SELECT-MANY [LISTBOX])  -->
<xsl:template match="select-many[@type='listbox']/choice">
    <xsl:variable name = "name"><xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="@handle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$name)" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name = "handle" select="./@handle"/>
    <xsl:variable name = "cc" select="ancestor::choice-collection"/>
    <xsl:variable name = "actionHandle" select="action/@handle"/>
    <xsl:variable name = "selectMany" select = "ancestor::select-many" />
    <xsl:if test = "label">
        <label for="{$baseFilteredId}"><xsl:value-of select="label" /></label>
    </xsl:if>
    <select name="{$name}" id="{$baseFilteredId}" class="{$selectMany/@class}" multiple="multiple">
        <xsl:attribute  name = "size" >
            <xsl:choose>
                <xsl:when test="$selectMany/@size"><xsl:value-of select="$selectMany/@size" /></xsl:when>
                <xsl:otherwise>5</xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
        <xsl:if test = "action">
            <xsl:variable name = "ccid" select = "generate-id($cc)" />
            <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form_defaultAction')</xsl:variable>
            <xsl:attribute  name = "onchange" ><xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$actionHandle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$actionHandle" />';document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form').submit();</xsl:attribute>
        </xsl:if>
        <xsl:if test = "$selectMany/@title">
            <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space($selectMany/@title)" /></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="*[name(.)!='action']"></xsl:apply-templates>
    </select>
</xsl:template>
<!-- ############################################### -->


<!-- ############################################### -->
<!-- OPTION -->
<xsl:template match="option">
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />

    <xsl:variable name = "name"><xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="ancestor::choice/@handle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$name,'_',@handle)" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test="ancestor::choice/@max-selections &gt; 1 or ancestor::choice/@max-selections=0">
            <input type="checkbox" name="{$name}" id="{$baseFilteredId}" value="{@handle}" title="{normalize-space(label/text())}">
                <xsl:if test = "ancestor::disabled">
                    <xsl:attribute  name = "disabled" >disabled</xsl:attribute>
                    <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
                </xsl:if>
                <xsl:if test = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]">
                    <xsl:attribute  name = "checked" >checked</xsl:attribute>
                </xsl:if>
            </input>
        </xsl:when>

        <xsl:otherwise>
            <input type="radio" name="{$name}" id="{$baseFilteredId}" value="{@handle}" title="{normalize-space(label/text())}">
                <xsl:if test = "ancestor::disabled">
                    <xsl:attribute  name = "disabled" >disabled</xsl:attribute>
                    <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
                </xsl:if>
                <xsl:if test = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]">
                    <xsl:attribute  name = "checked" >checked</xsl:attribute>
                </xsl:if>
            </input>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- OPTION [TypeFileUpload] -->
<xsl:template match="option[contains(@complement-type,'TypeFileUpload')]">
    <xsl:variable name = "name"><xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="ancestor::choice/@handle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$name,'_',@handle)" />
        </xsl:call-template>
    </xsl:variable>
    <input type="checkbox" name="{$name}" id="{$baseFilteredId}_hidden" value="{@handle}">
        <xsl:choose>
            <xsl:when test="number(ancestor::choice/@min-selections) &gt; 0 and count(ancestor::choice//option) &gt; 1">
                <xsl:attribute  name = "class" >implied minimum_<xsl:value-of select="ancestor::choice/@min-selections" /></xsl:attribute>
            </xsl:when>
          
            <xsl:otherwise>
                <xsl:attribute  name = "class" >implied</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </input>
    <input type="file" name="{$name}_{@handle}" id="{$baseFilteredId}">
        <xsl:if test = "ancestor::disabled">
            <!--<xsl:attribute  name = "disabled" >disabled</xsl:attribute>-->
            <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
        </xsl:if>
        <xsl:if test = "ancestor::choice/@min-selections = '1' and count(ancestor::choice//option) = 1">
            <xsl:attribute  name = "class" >required format_TypeFileUpload</xsl:attribute>
        </xsl:if>
    </input>
</xsl:template>

<!-- OPTION [TypeText or TypePassword] -->
<xsl:template match="option[contains(@complement-type,'TypeText') or contains(@complement-type,'TypePassword') or contains(@complement-type,'TypeHTMLFilter') or contains(bean/@class,'TypeTextConfigurableLimit')]">
    <xsl:variable name = "complementType"><xsl:choose>
            <xsl:when test="contains(@complement-type,'TypeText')">TypeText</xsl:when>
            <xsl:when test="contains(bean/@class,'TypeTextConfigurableLimit')">TypeText</xsl:when>
            <xsl:when test="contains(@complement-type,'TypePassword')">TypePassword</xsl:when>
            <xsl:when test="contains(@complement-type,'TypeHTMLFilter')">TypeHTMLFilter</xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose></xsl:variable>
    <xsl:variable name = "defaultAction" select = "ancestor::default-action" />
    <!--<xsl:variable name = "textSize" select = "number(substring-after(@complement-type,'TypeText'))" /> -->
    <xsl:variable name = "textSize"><xsl:choose>
                                        <xsl:when test="contains(bean/@class,'TypeTextConfigurableLimit')"><xsl:value-of select="number(bean/constructor-arg/value)" /></xsl:when>
                                        <xsl:when test="$complementType = 'TypeText'"><xsl:value-of select="number(substring-after(@complement-type,'TypeText'))" /></xsl:when>
                                        <xsl:when test="$complementType = 'TypePassword'"><xsl:value-of select="number(substring-after(@complement-type,'TypePassword'))" /></xsl:when>
                                        <xsl:otherwise></xsl:otherwise>
                                    </xsl:choose></xsl:variable>
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "previousValue"><xsl:value-of select = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]" /></xsl:variable>
    <xsl:variable name = "name"><xsl:value-of select="ancestor::choice-collection/@handle" />_<xsl:value-of select="ancestor::choice/@handle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$name,'_',@handle)" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test="($complementType = 'TypeText' and $textSize &lt; 256) or $complementType = 'TypePassword'">
            <input type="checkbox" name="{$name}" id="{$baseFilteredId}_hidden" value="{@handle}">
                <xsl:if test = "$previousValue != ''"><xsl:attribute  name = "checked" >checked</xsl:attribute></xsl:if>
                <xsl:choose>
                    <xsl:when test="number(ancestor::choice/@min-selections) &gt; 0 and count(ancestor::choice//option) &gt; 1">
                        <xsl:attribute  name = "class" >implied minimum_<xsl:value-of select="ancestor::choice/@min-selections" /></xsl:attribute>
                    </xsl:when>
                  
                    <xsl:otherwise>
                        <xsl:attribute  name = "class" >implied</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </input>
            <input name="{$name}_{@handle}" id="{$baseFilteredId}" maxlength="{$textSize}" size="20">
                <xsl:attribute  name = "type" ><xsl:choose>
                    <xsl:when test="$complementType = 'TypePassword'">password</xsl:when>
                    <xsl:otherwise>text</xsl:otherwise>
               </xsl:choose></xsl:attribute>
                <xsl:if test = "ancestor::disabled">
                    <!--<xsl:attribute  name = "disabled" >disabled</xsl:attribute>-->
                    <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
                </xsl:if>
                <xsl:if test = "ancestor::choice/@min-selections = '1' and count(ancestor::choice//option) = 1">
                    <xsl:attribute  name = "class" >required <xsl:choose>
                        <xsl:when test="$complementType = 'TypeText'">format_TypeText</xsl:when>
                        <xsl:otherwise>format_TypePassword</xsl:otherwise>
                    </xsl:choose> maxsize_<xsl:value-of select="$textSize" /></xsl:attribute>
                </xsl:if>
                <xsl:attribute  name = "value" ><xsl:value-of select="$previousValue" disable-output-escaping="yes" /></xsl:attribute>
                <xsl:choose>
                    <xsl:when test="ancestor::disabled/@title">
                        <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(ancestor::disabled/@title)" /></xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(label/text())" /></xsl:attribute>
                    </xsl:otherwise>
               </xsl:choose>

                <xsl:if test = "$defaultAction">
                    <xsl:variable name = "actionRef" select = "/descendant::action[@handle = $defaultAction/@ref-handle]" />
                    
                    <xsl:if test = "$actionRef">
                        <xsl:variable name = "actionId">
                            <xsl:call-template name="escape-for-id-attr">
                               <xsl:with-param name="string" select="concat($filteredNameSpace,'_',generate-id($actionRef),'_button')" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:attribute  name = "onfocus" >window.buttonpress=document.getElementById('<xsl:value-of select="$actionId" />')</xsl:attribute>
                    </xsl:if>
                </xsl:if>
            </input>
        </xsl:when>
        <xsl:otherwise>
            <input type="checkbox" name="{$name}" id="{$baseFilteredId}_hidden" value="{@handle}">
                <xsl:choose>
                    <xsl:when test="number(ancestor::choice/@min-selections) &gt; 0 and count(ancestor::choice//option) &gt; 1">
                        <xsl:attribute  name = "class" >implied minimum_<xsl:value-of select="ancestor::choice/@min-selections" /></xsl:attribute>
                    </xsl:when>
                  
                    <xsl:otherwise>
                        <xsl:attribute  name = "class" >implied</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </input>
            <xsl:variable name = "required"><xsl:choose>
                    <xsl:when test="ancestor::choice/@min-selections = '1' and count(ancestor::choice//option) = 1">required</xsl:when>
                    <xsl:when test = "ancestor::choice/@min-selections = '0' and count(ancestor::choice//option) = 1">optional</xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose></xsl:variable>
            <textarea name="{$name}_{@handle}" id="{$baseFilteredId}">
                <!--<xsl:if test = "ancestor::text-input/@format='rte'">
                    <xsl:attribute  name = "mce_editable" >true</xsl:attribute>
                </xsl:if> -->
                <xsl:if test = "ancestor::text-input/@cols">
                    <xsl:attribute  name = "cols" ><xsl:value-of select="ancestor::text-input/@cols" /></xsl:attribute>
                </xsl:if>
                <xsl:if test = "ancestor::text-input/@rows">
                    <xsl:attribute  name = "rows" ><xsl:value-of select="ancestor::text-input/@rows" /></xsl:attribute>
                </xsl:if>
                <xsl:if test = "ancestor::text-input/@style">
                    <xsl:attribute  name = "style" ><xsl:value-of select="ancestor::text-input/@style" /></xsl:attribute>
                </xsl:if>
                <xsl:if test = "ancestor::disabled">
                    <!--<xsl:attribute  name = "disabled" >disabled</xsl:attribute>-->
                    <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
                </xsl:if>
                <xsl:attribute  name = "class" >
                    <xsl:choose>
                        <xsl:when test="$complementType = 'TypeText'"><xsl:value-of select="$required" /> format_TypeText maxsize_<xsl:value-of select="$textSize" /></xsl:when>
                        <xsl:when test="$complementType = 'TypeHTMLFilter'"><xsl:value-of select="$required" /> format_TypeHTMLFilter</xsl:when>
                        <xsl:otherwise><xsl:value-of select="$required" /></xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test = "ancestor::text-input/@format='rte'"> mceEditor</xsl:if><!-- Class to identify for TinyMCE -->
                </xsl:attribute> 
                <xsl:choose>
                    <xsl:when test="ancestor::disabled/@title">
                        <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(ancestor::disabled/@title)" /></xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(label/text())" /></xsl:attribute>
                    </xsl:otherwise>
               </xsl:choose>

                <xsl:if test = "$defaultAction">
                    <xsl:variable name = "actionRef" select = "/descendant::action[@handle = $defaultAction/@ref-handle]" />
                
                    <xsl:if test = "$actionRef">
                        <xsl:variable name = "actionId">
                            <xsl:call-template name="escape-for-id-attr">
                               <xsl:with-param name="string" select="concat($filteredNameSpace,'_',generate-id($actionRef),'_button')" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:attribute  name = "onfocus" >window.buttonpress=document.getElementById('<xsl:value-of select="$actionId" />')</xsl:attribute>
                    </xsl:if>
                </xsl:if>

                <xsl:choose>
                    <xsl:when test = "$previousValue = ''">
                        <xsl:text> </xsl:text><!-- Add space to keep XHTML from collapsing to <textarea /> which gets interpreted as <textarea> with no closing tag -->
                    </xsl:when>
                    <xsl:when test="$complementType = 'TypeHTMLFilter'">
                        <xsl:value-of disable-output-escaping="yes" select="$previousValue" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$previousValue" />
                    </xsl:otherwise>
                </xsl:choose>
            </textarea>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- OPTION [TypeGrantDenyDefer] -->
<xsl:template match="option[contains(@complement-type,'TypeGrantDenyDefer')]">
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "optionName"><xsl:value-of select="$choiceCollectionHandle" />_<xsl:value-of select="$choiceHandle" />_<xsl:value-of select="$optionHandle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$optionName)" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name = "previousValue"><xsl:value-of select = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]" /></xsl:variable>

    <input type="hidden" name="{$choiceCollectionHandle}_{$choiceHandle}" value="{$optionHandle}"></input>
    <ul class="no-bullet" style="margin:0px; padding:0px;text-align:left;">
        <li>
            <input type="radio" id="{$baseFilteredId}_true" name="{$optionName}" value="grant" title="{normalize-space(label/text())}">
                <xsl:if test = "$previousValue = 'grant'">
                    <xsl:attribute  name = "checked" >checked</xsl:attribute>
                </xsl:if>
            </input> 
            <label for="{$baseFilteredId}_true" title="{normalize-space(description/text())}" style="color:green;"><xsl:value-of select="grant-label" /> <xsl:text disable-output-escaping = "yes"> &amp;#10004;</xsl:text></label>
        </li>
        <li>
            <input type="radio" id="{$baseFilteredId}_false" name="{$optionName}" value="deny" title="{normalize-space(label/text())}">
                <xsl:if test = "$previousValue = 'deny'">
                    <xsl:attribute  name = "checked" >checked</xsl:attribute>
                </xsl:if>
            </input> 
            <label for="{$baseFilteredId}_false" title="{normalize-space(description/text())}" style="color:red;"><xsl:value-of select="deny-label" /> <xsl:text disable-output-escaping = "yes"> &amp;#10008;</xsl:text></label>
        </li>
        <li>
            <input type="radio" id="{$baseFilteredId}_nonspecified" name="{$optionName}" value="defer" title="{normalize-space(label/text())}">
                <xsl:if test = "not($previousValue = 'grant') and not($previousValue = 'deny') ">
                    <xsl:attribute  name = "checked" >checked</xsl:attribute>
                </xsl:if>
            </input> 
            <label for="{$baseFilteredId}_nonspecified" title="{normalize-space(description/text())}"><xsl:value-of select="defer-label" /></label>
        </li>
    </ul>
</xsl:template>

<!-- OPTION [of TypeGrantDenyDefer and within option-display/@type='dropdown' ] -->
<xsl:template match="option-display[@type='dropdown']/option[contains(@complement-type,'TypeGrantDenyDefer')]">
    <xsl:variable name = "optionDisplay" select = "parent::option-display" />
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "optionName"><xsl:value-of select="$choiceCollectionHandle" />_<xsl:value-of select="$choiceHandle" />_<xsl:value-of select="$optionHandle" /></xsl:variable>
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$optionName)" />
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name = "previousValue"><xsl:value-of select = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]" /></xsl:variable>
    <xsl:if test = "label">
        <label for="{$baseFilteredId}"><xsl:value-of select="label" /></label>
    </xsl:if>
    <input type="hidden" name="{$choiceCollectionHandle}_{$choiceHandle}" value="{$optionHandle}"></input>
    <select name="{$optionName}" id="{$baseFilteredId}">
        <xsl:if test = "$optionDisplay/@class-prefix">
            <xsl:attribute  name = "class" >
                <xsl:choose>
                    <xsl:when test="$previousValue = 'grant'"><xsl:value-of select="$optionDisplay/@class-prefix" />grant</xsl:when>
                    <xsl:when test="$previousValue = 'deny'"><xsl:value-of select="$optionDisplay/@class-prefix" />deny</xsl:when>
                    <xsl:otherwise><xsl:value-of select="$optionDisplay/@class-prefix" />defer</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute  name = "onchange" >this.className = this.options[this.selectedIndex].className;</xsl:attribute>
        </xsl:if>
        <xsl:if test = "description">
            <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
        </xsl:if>
        <option value="grant">
            <xsl:if test = "$optionDisplay/@class-prefix">
                <xsl:attribute  name = "class" ><xsl:value-of select="$optionDisplay/@class-prefix" />grant</xsl:attribute>
            </xsl:if>
            <xsl:if test = "$previousValue = 'grant'">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="grant-label" />
        </option>
        <option value="deny">
            <xsl:if test = "$optionDisplay/@class-prefix">
                <xsl:attribute  name = "class" ><xsl:value-of select="$optionDisplay/@class-prefix" />deny</xsl:attribute>
            </xsl:if>
            <xsl:if test = "$previousValue = 'deny'">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="deny-label" />
        </option>
        <option value="defer">
            <xsl:if test = "$optionDisplay/@class-prefix">
                <xsl:attribute  name = "class" ><xsl:value-of select="$optionDisplay/@class-prefix" />defer</xsl:attribute>
            </xsl:if>
            <xsl:if test = "not($previousValue = 'grant') and not($previousValue = 'deny')">
                <xsl:attribute  name = "selected" >selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="defer-label" />
        </option>
    </select>
</xsl:template>


<!-- OPTION [within hidden-input of type 'TypeNone'] -->
<xsl:template match="option[contains(@complement-type,'TypeNone') and boolean(ancestor::hidden-input)]">
    <input type="hidden" name="{ancestor::choice-collection/@handle}_{ancestor::choice/@handle}" value="{@handle}"></input>
</xsl:template>

<!-- OPTION [within hidden-input of type 'TypeText'] -->
<xsl:template match="option[contains(@complement-type,'TypeText') and boolean(ancestor::hidden-input)]">
    <input type="hidden" name="{ancestor::choice-collection/@handle}_{ancestor::choice/@handle}" value="{@handle}"></input>
    <input type="hidden" id="{$filteredNameSpace}_{ancestor::choice-collection/@handle}_{ancestor::choice/@handle}_{@handle}" name="{ancestor::choice-collection/@handle}_{ancestor::choice/@handle}_{@handle}" value=""></input>
</xsl:template>

<!-- OPTION (within SELECT-ONE [RADIO]) -->
<xsl:template match="select-one[@type='radio']/choice//option">
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle,'_',$optionHandle)" />
        </xsl:call-template>
    </xsl:variable>

    <input type="radio" name="{$choiceCollectionHandle}_{$choiceHandle}" value="{$optionHandle}" id="{$baseFilteredId}">
        <xsl:if test = "ancestor::disabled">
            <xsl:attribute  name = "disabled" >disabled</xsl:attribute>
            <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
        </xsl:if>
        <xsl:if test = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]">
            <xsl:attribute  name = "checked" >checked</xsl:attribute>
        </xsl:if>
    </input> 
    <xsl:if test = "label">
        <label for="{$baseFilteredId}">
            <xsl:apply-templates select = "label" />
        </label>
    </xsl:if>

</xsl:template>

<!-- OPTION (within SELECT-ONE [DROPDOWN]) -->
<xsl:template match="select-one[@type='dropdown']/choice/option | state-choice-options/option">
    <xsl:param name="id" select="generate-id(.)" />
    <xsl:variable name = "optionpos"><xsl:for-each select = "../option"><xsl:if test = "$id = generate-id(.)"><xsl:value-of select="position()" /></xsl:if></xsl:for-each>
    </xsl:variable>
    <!-- @handle hack to fix {} support limited to whole attribute.  So, check for <handle></handle>.  If it exists, use it instead of @handle -->
    <xsl:variable name = "optionHandle"><xsl:choose><xsl:when test="handle"><xsl:value-of select="handle" /></xsl:when><xsl:otherwise><xsl:value-of select="@handle" /></xsl:otherwise></xsl:choose></xsl:variable>
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <option value="{$optionHandle}">
        <xsl:if test = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]">
            <xsl:attribute  name = "selected" >selected</xsl:attribute>
        </xsl:if>
        <xsl:if test = "ancestor::select-one/@indent">
            <xsl:for-each select = "ancestor::select-one/@indent">
                <xsl:call-template name = "repeatIt" >
                    <xsl:with-param name="times" select="number($optionpos)-1" />
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
        <xsl:apply-templates>
        </xsl:apply-templates>
    </option>
</xsl:template>

<!-- OPTION (within SELECT-MANY [LISTBOX]) -->
<xsl:template match="select-many[@type='listbox']/choice/option">
    <xsl:param name="id" select="generate-id(.)" />
    <xsl:variable name = "optionHandle"><xsl:value-of select="@handle" /></xsl:variable>
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <option value="{$optionHandle}">
        <xsl:if test = "$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection[@option = $optionHandle]">
            <xsl:attribute  name = "selected" >selected</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates />
    </option>
</xsl:template>

<!-- OPTION (Ignore HANDLE child) -->
<xsl:template match="option/handle"></xsl:template>

<!-- OPTION (within SELECT-ONE [LINK]) -->
<xsl:template match="select-one[@type='link']/choice/option">
    <xsl:variable name = "action" select = "../../action" />
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "currentlySelected"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
    <xsl:variable name = "ccid" select = "generate-id(ancestor::choice-collection)" />
    <a><xsl:choose>
            <xsl:when test="$currentlySelected = $optionHandle">
                <xsl:attribute  name = "class" >selected</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name = "filteredChoiceId">
                    <xsl:call-template name="escape-for-id-attr">
                       <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle)" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form_defaultAction')</xsl:variable>
                <xsl:attribute  name = "href" >javascript:document.getElementById('<xsl:value-of select="$filteredChoiceId" />').value='<xsl:value-of select="$optionHandle" />';<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$action/@handle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$action/@handle" />';document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form').submit();</xsl:attribute>
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose><xsl:apply-templates select="label" /></a>
</xsl:template>

<!-- OPTION (within SELECT-ONE [BUTTON]) -->
<xsl:template match="select-one[@type='button']/choice/option">
    <xsl:variable name = "action" select = "../../action" />
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "currentlySelected"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
    <xsl:variable name = "ccid" select = "generate-id(ancestor::choice-collection)" />
    <input type="button" value="{label}"><xsl:choose>
            <xsl:when test="$currentlySelected = $optionHandle">
                <xsl:attribute  name = "disabled" >disabled</xsl:attribute>
                <xsl:attribute  name = "readonly" >readonly</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name = "filteredChoiceId">
                    <xsl:call-template name="escape-for-id-attr">
                       <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle)" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form_defaultAction')</xsl:variable>
                <xsl:attribute  name = "onclick" >javascript:document.getElementById('<xsl:value-of select="$filteredChoiceId" />').value='<xsl:value-of select="$optionHandle" />';<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$action/@handle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$action/@handle" />';document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form').submit();</xsl:attribute>
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose></input>
</xsl:template>

<!-- OPTION (within SELECT-ONE [TABS]) -->
<xsl:template match="select-one[@type='tabs']/choice/option">
    <xsl:variable name = "action" select = "../../action" />
    <xsl:variable name = "optionHandle" select = "@handle" />
    <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "currentlySelected"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
    <xsl:variable name = "ccid" select = "generate-id(ancestor::choice-collection)" />
    <li>
    <a><xsl:choose>
            <xsl:when test="$currentlySelected = $optionHandle">
                <xsl:attribute  name = "class" >portlet-menu-item-selected tab selected</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute  name = "class" >portlet-menu-item tab</xsl:attribute>
                <xsl:variable name = "filteredChoiceId">
                    <xsl:call-template name="escape-for-id-attr">
                       <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle)" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form_defaultAction')</xsl:variable>
                <xsl:attribute  name = "href" >javascript:document.getElementById('<xsl:value-of select="$filteredChoiceId" />').value='<xsl:value-of select="$optionHandle" />';<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$action/@handle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$action/@handle" />';document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form').submit();</xsl:attribute>
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose><xsl:apply-templates select="label" /></a>
    </li>
</xsl:template>
<!-- ############################################### -->


<!-- ############################################### -->
<!-- ACTION -->
<xsl:template match="action" name="action">
    <xsl:param name="id" select="generate-id(.)" />
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$id,'_button')" />
        </xsl:call-template>
    </xsl:variable>

    <input type="button" title="{normalize-space(description/text())}" name="act_{@handle}" id="{$baseFilteredId}" value="{label}" onclick="window.buttonpress=this;if(document.getElementById('{$filteredNameSpace}_{generate-id(ancestor::choice-collection)}_form').onsubmit())document.getElementById('{$filteredNameSpace}_{generate-id(ancestor::choice-collection)}_form').submit();">
        <xsl:if test = "parent::disabled">
            <xsl:attribute  name = "disabled" >disabled</xsl:attribute>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="parent::disabled/@title">
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(parent::disabled/@title)" /></xsl:attribute>
            </xsl:when>
          
            <xsl:otherwise>
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="parent::disabled">
                <xsl:attribute  name = "class" >form-button-disabled</xsl:attribute>
            </xsl:when>
          
            <xsl:otherwise>
                <xsl:attribute  name = "class" >portlet-form-button <xsl:value-of select="@class" /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </input>
</xsl:template>

<!-- ACTION [within hidden-input] -->
<xsl:template match="action[ancestor::hidden-input]">
    <xsl:param name="id" select="generate-id(.)" />
    <xsl:variable name = "baseFilteredId">
        <xsl:call-template name="escape-for-id-attr">
           <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$id,'_button')" />
        </xsl:call-template>
    </xsl:variable>
    <input type="hidden" name="act_{@handle}" id="{$baseFilteredId}" value="{label}"></input>
</xsl:template>

<!-- ACTION (within LINK) -->
<xsl:template name="link_action" match="link/action" >
    <xsl:param name="context" select="/screen/state" />
    <xsl:param name = "class" />
    <xsl:param name = "isDuplicate">false</xsl:param>
    <xsl:variable name = "id" select = "generate-id(.)" />
    <xsl:variable name = "navtree" select = "ancestor::navtree" />
    <xsl:variable name = "link" select = "ancestor::link" />
    <xsl:variable name = "cc" select = "ancestor::choice-collection" />
    <xsl:variable  name = "formId" ><xsl:choose>
            <xsl:when test = "$cc"><xsl:value-of select="generate-id($cc)" ></xsl:value-of></xsl:when>
            <xsl:otherwise><xsl:value-of select="$id" ></xsl:value-of></xsl:otherwise>
        </xsl:choose></xsl:variable>
        <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$formId" />_form_defaultAction')</xsl:variable>
        
    <!-- LINK Submission logic -->
    <xsl:variable  name = "submitLogic" ><xsl:choose>
            <!-- Validate -->
            <xsl:when test = "$link/@validate = 'yes'">window.buttonpress=<xsl:value-of select="$defaultActionRef" />;if(document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$formId" />_form').onsubmit())document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$formId" />_form').submit();</xsl:when>
            <!-- Pass All Data -->
            <xsl:when test = "$link/@validate = 'pass'">modValidate(document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$formId" />_form'),true);document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$formId" />_form').submit();</xsl:when>
            <!-- No Pass or Validate -->
            <xsl:otherwise>document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$formId" />_form').submit();</xsl:otherwise>
        </xsl:choose></xsl:variable>
    
    <xsl:choose>
        <xsl:when test = "$isDuplicate = 'true'"><!-- No input field info is needed, as will be provided elsewhere --></xsl:when>
        <xsl:when test="$cc">
            <xsl:apply-templates select = "option" mode="inputfield" />
        </xsl:when>
        <xsl:otherwise>
            <!-- If not within Choice-Collection (i.e. form) create a form for submission -->
            <form name="{$filteredNameSpace}_{$formId}_form" id="{$filteredNameSpace}_{$formId}_form" action="{$actionUrl}" method="post" style="display:inline;">
                <input type="hidden" id="{$filteredNameSpace}_{$formId}_form_defaultAction" name="defaultAction" value=""></input>
                <xsl:apply-templates select = "option" mode="inputfield" />
            </form>
        </xsl:otherwise>
    </xsl:choose>
    <a title="{normalize-space(label/text())}">
        <xsl:if test = "$link/@style">
            <xsl:attribute  name = "style" ><xsl:value-of select="$link/@style" /></xsl:attribute>
        </xsl:if>
        <xsl:choose>
            <!-- When href is provided, use it -->
            <xsl:when test="@href != ''">
                <xsl:attribute  name = "href" ><xsl:value-of select="@href" /></xsl:attribute>
            </xsl:when>
            <!-- When within CHOICE-COLLECTION submit form -->
            <xsl:when test = "$cc">
                <xsl:attribute  name = "href" >javascript:<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="@handle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="@handle" />';<xsl:apply-templates select = "option" mode="setvalues" /><xsl:value-of select="$submitLogic" /></xsl:attribute>
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
            </xsl:when>
            <!-- Otherwise, build link -->
            <xsl:otherwise>
                <xsl:attribute  name = "href" >javascript:<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="@handle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="@handle" />';<xsl:apply-templates select = "option" mode="setvalues" /><xsl:value-of select="$submitLogic" /></xsl:attribute>
                <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(description/text())" /></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="$class!=''">
                <xsl:attribute  name = "class" ><xsl:value-of select="$class" /></xsl:attribute>
            </xsl:when>
            <xsl:when test="$link/@class">
                <xsl:attribute  name = "class" ><xsl:value-of select="$link/@class" /></xsl:attribute>
            </xsl:when>
            <xsl:when test="$link/@opened-class">
                <xsl:choose>
                    <xsl:when test="../../ul/descendant::link or number(ancestor::rank/@children) &gt; 0">
                        <xsl:attribute  name = "class" ><xsl:value-of select="$link/@opened-class" /></xsl:attribute>
                    </xsl:when>
                    <!-- If no subelements, check navtree to see if link is current folder.  If so, show as open -->
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$navtree/@current-id = $link/@id">
                                <xsl:attribute  name = "class" ><xsl:value-of select="$link/@opened-class" /></xsl:attribute>
                            </xsl:when>
                          
                            <xsl:otherwise>
                                <xsl:attribute  name = "class" ><xsl:value-of select="$link/@closed-class" /></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$navtree/@opened-class">
                <xsl:choose>
                    <xsl:when test="../../ul/descendant::link or number(ancestor::rank/@children) &gt; 0">
                        <xsl:attribute  name = "class" ><xsl:value-of select="$navtree/@opened-class" /></xsl:attribute>
                    </xsl:when>
                    <!-- If no subelements, check navtree to see if link is current folder.  If so, show as open -->
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$navtree/@current-id = $link/@id">
                                <xsl:attribute  name = "class" ><xsl:value-of select="$navtree/@opened-class" /></xsl:attribute>
                            </xsl:when>
                          
                            <xsl:otherwise>
                                <xsl:attribute  name = "class" ><xsl:value-of select="$navtree/@closed-class" /></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
        
        <xsl:choose>
            <!-- If class passed in (expanded-link), then wrap contents in <span/> to be able to hide text -->
            <xsl:when test="$class!=''">
                <span class="{$class}">
                    <xsl:apply-templates>
                        <xsl:with-param name="context" select="$context" />
                    </xsl:apply-templates>
                </span>
            </xsl:when>
            <!-- Else display contents normally -->
            <xsl:otherwise>
                <xsl:apply-templates>
                    <xsl:with-param name="context" select="$context" />
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </a>
</xsl:template>

<!-- OPTION (for rendering hidden input fields) -->
<xsl:template match="option" mode="inputfield">
    <xsl:variable name = "firstOptionId" select = "generate-id(ancestor::choice//option[1])" />
    <xsl:if test = "$firstOptionId = generate-id(.)">
        <xsl:variable name = "choiceHandle" select = "ancestor::choice/@handle" />
        <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
        <xsl:variable name = "selectedOption"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
        <xsl:variable name = "baseFilteredId">
            <xsl:call-template name="escape-for-id-attr">
               <xsl:with-param name="string" select="concat($filteredNameSpace,'_',$choiceCollectionHandle,'_',$choiceHandle)" />
            </xsl:call-template>
        </xsl:variable>
        <input type="hidden" id="{$baseFilteredId}" name="{$choiceCollectionHandle}_{$choiceHandle}" value="{$selectedOption}"></input>
    </xsl:if>
</xsl:template>

<!-- OPTION (for rendering Javascript commands to populate hidden input fields) -->
<xsl:template match="option" mode="setvalues"><xsl:param name="selectedOption" select="@handle" /><xsl:variable name = "selectedOptionEscaped"><xsl:call-template name = "escape-characters" ><xsl:with-param name="string" select="$selectedOption" /></xsl:call-template></xsl:variable><xsl:variable name = "filteredChoiceId"><xsl:call-template name="escape-for-id-attr"><xsl:with-param name="string" select="concat($filteredNameSpace,'_',ancestor::choice-collection/@handle,'_',ancestor::choice/@handle)" /></xsl:call-template></xsl:variable><xsl:if test = "position() = 1">document.getElementById('<xsl:value-of select="$filteredChoiceId" />').value='<xsl:value-of select="$selectedOptionEscaped" />';</xsl:if></xsl:template>

<!-- ACTION/OPTION -->
<xsl:template match="link/action/option"></xsl:template>

<!-- EXPANDED-LINK -->
<xsl:template match="expanded-link">
    <xsl:variable name = "class" select = "@class" />
    <div class="expanded-link">
        <!-- Call Link Action for Large Image -->
        <xsl:for-each select = "descendant::action">
            <xsl:call-template name = "link_action" >
                <xsl:with-param name="class" select="$class" />
                <xsl:with-param name="isDuplicate">true</xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>

        <!-- Call Link Send for Large Image -->
        <xsl:for-each select = "descendant::link//send[position() = last()]">
            <xsl:call-template name = "link_send" >
                <xsl:with-param name="class" select="$class" />
                <xsl:with-param name="isDuplicate">true</xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        
        <div>
            <xsl:apply-templates />
        </div>
    </div>
</xsl:template>

<!-- ############################################### -->


<!-- ############################################### -->
<!-- DESCRIPTION (within ACTION is to be ignored) -->
<xsl:template name="action_description" match="action/description"></xsl:template>

<!-- LABEL -->
<xsl:template match="label">
    <xsl:choose>
        <xsl:when test = "@handle='entryPointOther_label'">
            <!--Do Nothing. This is a fix for Toro-80 - http://www.ja-sig.org/issues/browse/TORO-80 - To fix #2 duplicate SSO title. The handle attribute is added to the label node within the entryPointWithNavLink template of gateway_main.xml to provide an unique identifier. Also, this test checks for a match of the label to the link/action template match above in this file, which is the link output. -->
        </xsl:when>
        <xsl:when test = "@for">
            <label for="{@for}"><xsl:apply-templates></xsl:apply-templates></label>
        </xsl:when>
        <xsl:when test = "@for-option">
            <xsl:variable name = "filteredForOptionId">
                <xsl:call-template name="escape-for-id-attr">
                   <xsl:with-param name="string" select="concat($filteredNameSpace,'_',@for-option)" />
                </xsl:call-template>
            </xsl:variable>
            <label for="{$filteredForOptionId}"><xsl:apply-templates></xsl:apply-templates></label>
        </xsl:when>
        <xsl:when test = "label-for">
            <label for="{label-for}">
                <xsl:apply-templates />
            </label>
        </xsl:when>
        <xsl:when test="ancestor::option">
            <xsl:apply-templates></xsl:apply-templates>
        </xsl:when>
        <xsl:when test="ancestor::choice and not(ancestor::select-one)">
            <xsl:variable name = "choice" select = "ancestor::choice" />
            <xsl:variable name = "filteredOptionId">
                <xsl:call-template name="escape-for-id-attr">
                   <xsl:with-param name="string" select="concat($filteredNameSpace, '_', ancestor::choice-collection/@handle, '_', $choice/@handle, '_', $choice/descendant::option[position()=1]/@handle)" />
                </xsl:call-template>
            </xsl:variable>
            <label for="{$filteredOptionId}"><xsl:apply-templates></xsl:apply-templates></label>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates></xsl:apply-templates>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- LABEL-FOR -->
<xsl:template name="label-for" match="label-for"></xsl:template>

<!-- LABEL (within ACTION) -->
<xsl:template match="action//label">
    <xsl:apply-templates></xsl:apply-templates>
</xsl:template>

<!-- LABEL (within SELECT-ONE/CHOICE) -->
<xsl:template match="select-one[@type='dropdown']/choice/label"><!-- BLANK --></xsl:template>
<!-- ############################################### -->


<!-- ############################################### -->
<!-- LINK WIDGETS -->

<!-- SELECT-ONE LINK/TABS -->
<xsl:template match="select-one[@type='link' or @type='tabs' or @type='button']">
    <xsl:apply-templates select = "*[name(.)!='action']" />
</xsl:template>

<!-- SORT-COLUMN -->
<xsl:template name="sort-column" match="sort-column">
    <xsl:variable name = "id" select = "generate-id(.)" />
    <xsl:variable name = "cc" select = "ancestor::choice-collection" />
    <xsl:variable name = "actionHandle" select = "descendant::action/@handle" />
    <xsl:variable name = "choiceHandle" select = "choice/@handle" />
    <xsl:variable name = "choiceCollectionHandle" select = "ancestor::choice-collection/@handle" />
    <xsl:variable name = "image" select = "descendant::action/image" />
    <xsl:variable name = "selectedOption"><xsl:value-of select="$decisionCollections[@choice-collection = $choiceCollectionHandle]/decision[@choice = $choiceHandle]/selection/@option" /></xsl:variable>
    <xsl:variable name = "nextOption">
        <xsl:choose>
            <xsl:when test="$selectedOption=''"><xsl:value-of select="descendant::option[1]/@handle" /></xsl:when>

            <xsl:otherwise><xsl:value-of select="descendant::option[@handle!=$selectedOption]/@handle" /></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:choose>
        <xsl:when test="$cc">
            <xsl:apply-templates select = "descendant::option" mode="inputfield" />
        </xsl:when>
        <xsl:otherwise>
            <!-- If not within Choice-Collection (i.e. form) create a form for submission -->
            <form name="{$filteredNameSpace}_{$id}_form" id="{$filteredNameSpace}_{$id}_form" action="{$actionUrl}" method="post" style="display:inline;">
                <input type="hidden" id="{$filteredNameSpace}_{$id}_form_defaultAction" name="defaultAction" value=""></input>
                <xsl:apply-templates select = "descendant::option" mode="inputfield" />
            </form>
        </xsl:otherwise>
    </xsl:choose>

    <a>
        <xsl:choose>
            <xsl:when test="$cc">
                <xsl:variable name = "ccid" select = "generate-id($cc)" />
                <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form_defaultAction')</xsl:variable>
                <xsl:attribute  name = "href" >javascript:<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$actionHandle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$actionHandle" />';<xsl:apply-templates select = "descendant::option" mode="setvalues"><xsl:with-param name="selectedOption" select="$nextOption" /></xsl:apply-templates>document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$ccid" />_form').submit();</xsl:attribute>
                
            </xsl:when>
          
            <xsl:otherwise>
                <xsl:variable name = "defaultActionRef">document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$id" />_form_defaultAction')</xsl:variable>
                <xsl:attribute  name = "href" >javascript:<xsl:value-of select="$defaultActionRef" />.name='act_<xsl:value-of select="$actionHandle" />';<xsl:value-of select="$defaultActionRef" />.value='act_<xsl:value-of select="$actionHandle" />';<xsl:apply-templates select = "descendant::option" mode="setvalues"><xsl:with-param name="selectedOption" select="$nextOption" /></xsl:apply-templates>document.getElementById('<xsl:value-of select="$filteredNameSpace" />_<xsl:value-of select="$id" />_form').submit();</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:attribute  name = "title" ><xsl:value-of select="normalize-space(descendant::option[@handle=$nextOption]/label/text())" /></xsl:attribute>
        <xsl:choose>
            <xsl:when test="$selectedOption='asc'"><xsl:attribute  name = "class" >sortedAscending</xsl:attribute></xsl:when>
            <xsl:when test="$selectedOption='des'"><xsl:attribute  name = "class" >sortedDescending</xsl:attribute></xsl:when>
            <xsl:otherwise><xsl:attribute  name = "class" >notSorted</xsl:attribute></xsl:otherwise>
        </xsl:choose>

        <xsl:if test = "$image">
            <!--<img>
                <xsl:copy-of select = "$image/@*[name(.) != 'src']"/>
                <xsl:attribute  name = "src" ><xsl:value-of select="$appsRoot" />/rendering/<xsl:value-of select="$image/@src" /></xsl:attribute>
            </img> -->
            <xsl:apply-templates select = "$image" />
        </xsl:if>
        <span><xsl:value-of select="action/label" /></span>

    </a>
</xsl:template>
<!-- ############################################### -->



</xsl:stylesheet>
