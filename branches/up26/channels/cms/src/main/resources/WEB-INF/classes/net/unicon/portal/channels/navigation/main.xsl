<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<!-- Include -->
<xsl:include href="common.xsl"/>

 <xsl:template match="/">
<!--      <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="offeringID"><xsl:value-of select="$offeringID" /></parameter>   
        <parameter name="adminMode"><xsl:value-of select="$adminMode" /></parameter>   
        <parameter name="subscriptionMode"><xsl:value-of select="$subscriptionMode" /></parameter>   
        <parameter name="offeringMode"><xsl:value-of select="$offeringMode" /></parameter>   
        <parameter name="currentMode"><xsl:value-of select="$currentMode" /></parameter>   
        <parameter name="optId"><xsl:value-of select="$optId" /></parameter>   
        <parameter name="offName"><xsl:value-of select="$offName" /></parameter>   
        <parameter name="topicName"><xsl:value-of select="$topicName" /></parameter>   
        <parameter name="catChannel"><xsl:value-of select="$catChannel" /></parameter>   
    </textarea> -->

    <xsl:apply-templates />
</xsl:template> 
    <!-- UniACC: layout table -->
  <xsl:template match="navigation">
    <table cellspacing="0" cellpadding="0" border="0" width="100%">
      <tr>
        <td class="table-nav-channel">
          <xsl:call-template name="administation"/>
          <xsl:call-template name="subscription"/>
		  <xsl:apply-templates select="academics"/>
          <xsl:apply-templates select="coi"/>
        </td>
      </tr>
    </table>
	<xsl:if test="count(academics/offering) &gt; 0 or $catCurrentCommand = 'search'"> <!-- Check if there are any offerings or in search mode -->
		<xsl:call-template name="catalog" /> <!-- Catalog/paging system called from the imported global.xsl (from common.xsl) -->
	</xsl:if>
    
  </xsl:template>
  
 <!--Below is the display of the navigation elements which get highlighted when in focus.  Currently this is being tested by a check against params
 sent in, but a more efficient way may be to have these major nodes be sent in through xml - outer node of 'navigation' with child nodes of 'Administration', 'Offering Subscription', and 'Academics'. -->



  <xsl:template name="administation">
      <!--Determine if Administration is the focus and style accordingly -->
      <xsl:choose>
          <xsl:when test="$currentMode = $adminMode">
                <!-- UniACC: layout table -->
              <table cellspacing="0" cellpadding="0" border="0" width="100%">
                <tr class="table-row-nav-channel">
                      <td class="nav-folder-administration" valign="middle" align="left">
                        <img height="16" width="0" src="{$SPACER}" alt="" title="" />
                        <a class="nav-folder-selected" href="{$baseActionURL}?mode={$adminMode}&amp;navigating=true&amp;catPageSize={$catPageSize}">
                            <img class="nav-folder-selected" alt="*" title="*" border="0" src="{$NAV_IMAGE_PATH}/folder-noChildOn.gif"/>
                            <img height="16" width="4" border="0" src="{$SPACER}" alt="" title=""/>
                            Administration
                        </a>
                    </td>
                </tr>
              </table>         
          </xsl:when>
        
          <xsl:otherwise>
              <table cellspacing="0" cellpadding="0" border="0" width="100%">
                <tr class="table-row-nav-channel" valign="middle" align="left" style="padding:0px">
                      <td class="nav-folder-administration">
                        <img height="16" width="0" src="{$SPACER}" alt="" title=""/>
                        <a class="nav-folder" href="{$baseActionURL}?mode={$adminMode}&amp;navigating=true&amp;catPageSize={$catPageSize}">
                            <img alt="*" title="*" border="0" src="{$NAV_IMAGE_PATH}/folder-noChildOff.gif"/>
                            <img height="16" width="4" border="0" src="{$SPACER}" alt="" title=""/>
                            Administration
                        </a>
                    </td>
                </tr>
              </table>
          </xsl:otherwise>
      </xsl:choose>
      
  </xsl:template>

  <xsl:template name="subscription">
  
       <!--Determine if Offering Subscription is the focus and style accordingly -->
  
      <xsl:choose>
          <xsl:when test="$currentMode = $subscriptionMode">
                <!-- UniACC: layout table -->
              <table cellspacing="0" cellpadding="0" border="0" width="100%">
                <tr class="table-row-nav-channel">
                      <td class="nav-folder-subscription" valign="middle" align="left">
                        <img height="16" width="0" src="{$SPACER}" alt="" title=""/>
                        <a class="nav-folder-selected" href="{$baseActionURL}?mode={$subscriptionMode}&amp;navigating=true&amp;catPageSize={$catPageSize}">
                            <img class="nav-folder-selected" alt="*" title="*" border="0" src="{$NAV_IMAGE_PATH}/folder-noChildOn.gif"/>
                            <img height="16" width="4" border="0" src="{$SPACER}" alt="" title=""/>
                            Offering Subscription
                        </a>
                    </td>
                </tr>
              </table>         
          </xsl:when>
        
          <xsl:otherwise>
              <table cellspacing="0" cellpadding="0" border="0" width="100%">
                <tr class="table-row-nav-channel">
                      <td class="nav-folder-subscription" valign="middle" align="left">
                        <img height="16" width="0" src="{$SPACER}" alt="" title=""/>
                        <a class="nav-folder" href="{$baseActionURL}?mode={$subscriptionMode}&amp;navigating=true&amp;catPageSize={$catPageSize}">
                            <img alt="*" title="*" border="0" src="{$NAV_IMAGE_PATH}/folder-noChildOff.gif"/>
                            <img height="16" width="4" border="0" src="{$SPACER}" alt="" title=""/>
                            Offering Subscription
                        </a>
                    </td>
                </tr>
              </table>
          </xsl:otherwise>
      </xsl:choose>
  
  </xsl:template>

  <xsl:template match="academics">
  
       <!--Determine if Academics is the focus and style accordingly -->
  
      <table cellspacing="0" cellpadding="0" border="0" width="100%">
      <xsl:choose>
          <xsl:when test="$currentMode = $offeringMode">
                <!-- UniACC: Data table -->
                <tr class="table-row-nav-channel">
                      <td id="Academics" class="nav-folder-academics" valign="middle" align="left">
                        <img height="16" width="0" src="{$SPACER}" alt="" title=""/>
                        <a class="nav-folder-selected" href="{$baseActionURL}?mode={$offeringMode}&amp;navigating=true&amp;catPageSize={$catPageSize}">
                            <img class="nav-folder-selected" alt="*" title="*" border="0" src="{$NAV_IMAGE_PATH}/folder-openOn.gif"/>
                            <img height="16" width="4" border="0" src="{$SPACER}" alt="" title=""/>
                            My Offerings
                        </a>
                    </td>
                </tr>

	            <xsl:if test="count(offering) &gt; 0 or $catCurrentCommand = 'search'"> <!-- Check if there are any offerings or in search mode -->
                    <xsl:apply-templates select="offering">
                        <xsl:sort select="topic/name"/>
                        <xsl:sort select="name"/>
                    </xsl:apply-templates>
                </xsl:if>
          </xsl:when>
        
          <xsl:otherwise>
	          <xsl:if test="count(offering) &gt; 0 or $catCurrentCommand = 'search'"> <!-- Check if there are any offerings or in search mode -->
                <tr class="table-row-nav-channel">
                      <td id="Academics" class="nav-folder-academics">
                        <img height="16" width="0" src="{$SPACER}" alt="" title=""/>
                        <a class="nav-folder" href="{$baseActionURL}?mode={$offeringMode}&amp;navigating=true&amp;catPageSize={$catPageSize}">
                            <img alt="*" title="*" border="0" src="{$NAV_IMAGE_PATH}/folder-openOff.gif"/>
                            <img height="16" width="4" border="0" src="{$SPACER}" alt="" title=""/>
                            My Offerings
                        </a>
                    </td>
                </tr>

                <xsl:apply-templates select="offering" />
            </xsl:if>
          </xsl:otherwise>

      </xsl:choose>
      <xsl:if test="count(offering) = 0 and $catCurrentCommand = 'search'"> <!-- if search returned no offerings show message -->
        <tr class="table-row-nav-channel">
          <td class="nav-offering">
            There are no matches to your search criteria.
          </td>
        </tr> 
      </xsl:if>
      </table>
                    
  </xsl:template>
  
  <xsl:template match="offering">
    <tr class="table-row-nav-channel">
      <td headers="academics" class="nav-offering">
            <img height="1" width="20" src="{$SPACER}" alt="" title=""/>
            <!-- Determine if the offering is highlighted or not -->
            <xsl:choose>
            <xsl:when test="(@id = $offeringID) and ($currentMode = $offeringMode)">
                <a class="nav-offering-selected">
                    <xsl:attribute name = "title" >&#160;You&#160;are&#160;currenly&#160;viewing&#160;this&#160;offering</xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$baseActionURL"/>?mode=<xsl:value-of select="$offeringMode"/>&amp;offeringId=<xsl:value-of select="@id"/>&amp;navigating=true&amp;catPageSize=<xsl:value-of select="$catPageSize" />&amp;catSelectPage=<xsl:value-of select="$catCurrentPage" />&amp;optId=<xsl:value-of select="$optId" />&amp;offName=<xsl:value-of select="$offName" />&amp;topicName=<xsl:value-of select="$topicName" />
                    </xsl:attribute>
                    <xsl:call-template name="offeringName"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <a class="nav-offering">
                    <xsl:attribute name = "title" >&#160;&#160;View&#160;this&#160;offering</xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$baseActionURL"/>?mode=<xsl:value-of select="$offeringMode"/>&amp;offeringId=<xsl:value-of select="@id"/>&amp;navigating=true&amp;catPageSize=<xsl:value-of select="$catPageSize" />&amp;catSelectPage=<xsl:value-of select="$catCurrentPage" />&amp;optId=<xsl:value-of select="$optId" />&amp;offName=<xsl:value-of select="$offName" />&amp;topicName=<xsl:value-of select="$topicName" />
                    </xsl:attribute>
                    <xsl:call-template name="offeringName"/>
                </a>
            </xsl:otherwise>
            </xsl:choose>
        </td>
    </tr>
  </xsl:template>

  <xsl:template name="offeringName">
    <xsl:choose>
    <xsl:when test="@status = 2">
        <span class="nav-offering-inactive">
            <xsl:value-of select="topic/name"/> - <xsl:value-of select="name"/>
        </span>
    </xsl:when>
    <xsl:otherwise>
        <xsl:value-of select="topic/name"/> - <xsl:value-of select="name"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
