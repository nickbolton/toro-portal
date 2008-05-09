<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Import -->
<xsl:import href="../global/global.xsl"/>

<!-- parameters -->
<xsl:param name="channelParam"/>
<xsl:param name="topicNameParam"/> <!-- Made obsolete by new naming convention in catalog -->
<xsl:param name="offeringNameParam"/> <!-- Made obsolete by new naming convention in catalog -->
<xsl:param name="offeringDescParam"/> <!-- Made obsolete by new naming convention in catalog -->
<xsl:param name="offeringNameSearchParam"/>
<xsl:param name="searchCommand"/>
<xsl:param name="userEnrollmentModelParam"/>
<xsl:param name="showAvailableCommand"/> <!-- Probably obselete -->
<xsl:param name="currentTopicId"/>
<xsl:param name="currentTopicName"/>
<xsl:param name="currentTopicDesc"/>
<xsl:param name="currentOfferingId"/>
<xsl:param name="current_command"/>
<xsl:param name="subscribe"/>
<xsl:param name="unsubscribe"/>
<xsl:param name="optId"/> <!-- Catalog: sticky offering search data of offering id input -->
<xsl:param name="offName"/> <!-- Catalog: sticky offering search data of offering name input -->
<xsl:param name="offDesc"/> <!-- Catalog: sticky offering search data of offering description input -->
<xsl:param name="topicName"/> <!-- Catalog: sticky user offering data of topic name input -->
<xsl:param name="catChannel">sub</xsl:param> <!-- Catalog: for form id uniqueness and identifying which search inputs to insert - See Catalog templates in global.xsl -->
<!-- Parameters passed from available.xsl -->
<xsl:param name="enrollmentStatus"/>
<xsl:param name="offeringId"/>
<xsl:param name="topicId"/>
<xsl:param name="catSelectPage"/>

<!-- permissions -->
<!-- None -->

<!-- Common -->
<xsl:template name="links">
<table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <td class="views-title">
            <img border="0" src=
            "{$CONTROLS_IMAGE_PATH}/channel_options.gif"
            alt="" align="absmiddle" />
        </td>
        <td class="views" valign="middle" height="26" width="100%">

            <!-- Made obsolete with usability changes, reference requirement OS 1.1
			<xsl:choose>
                <xsl:when test="$current_command = 'showAvailable'">
                    Offering<img height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0" /><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif"
                    alt="Currently viewing offering subscriptions" align="absmiddle" title="Currently viewing offering subscriptions"/>
                </xsl:when>
                <xsl:otherwise>
                    <a href="{$baseActionURL}?catPageSize={$catPageSize}" title="View offering subscriptions" 
                    onmouseover="swapImage('subscriptionViewImage','channel_view_active.gif')" 
                    onmouseout="swapImage('subscriptionViewImage','channel_view_base.gif')">Offering<img 
                    height="1" width="3" src="{$SPACER}"
                    alt="" border="0"/><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
                    alt="View offering subscriptions" title="View offering subscriptions" align="absmiddle" name="subscriptionViewImage" id="subscriptionViewImage"/></a>
                </xsl:otherwise>
            </xsl:choose> -->
            
            <xsl:choose>
                <xsl:when test="$current_command = 'search'">
                    Search<img height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0"/><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_view_selected.gif"
                    alt="" align="absmiddle" title="Currently viewing offering search"/><img height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0"/>
                </xsl:when>
                <xsl:otherwise>
                    <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$searchCommand}&amp;catPageSize={$catPageSize}" title="Search offerings" 
                    onmouseover="swapImage('subscriptionSearchImage','channel_view_active.gif')" 
                    onmouseout="swapImage('subscriptionSearchImage','channel_view_base.gif')">Search<img 
                    height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0"/><img border="0" src=
                    "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
                    alt="" align="absmiddle" name="subscriptionSearchImage" id="subscriptionSearchImage"/></a><img 
                    height="1" width="3" src=
                    "{$SPACER}"
                    alt="" border="0"/>
                </xsl:otherwise>
            </xsl:choose>
            
        </td>
    </tr>
</table>
</xsl:template>

  <xsl:template name="offeringSearch">
<!-- functionality not supported
    <form method="post" action="{$baseActionURL}">
      <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
      <input type="hidden" name="command" value="{$searchCommand}"></input>
      <br />
      <table>
        <tr>
          <th class="uportal-background-med">Offering Search</th>
        </tr>
        <tr>
          <td class="uportal-background-light">Name: <input name="{$offeringNameParam}" type="text" size="10" value=""/></td>
        </tr>
        <tr>
          <td class="uportal-background-light">
            <input name="{$offeringNameSearchParam}" type="radio" value="contains" checked="checked"/>Contains<br />
            <input name="{$offeringNameSearchParam}" type="radio" value="begins"/>Begins with<br />
            <input name="{$offeringNameSearchParam}" type="radio" value="ends"/>Ends with
          </td>
        </tr>
        <tr>
          <td style="text-align:center"><input type="submit" value="Search"/></td>
        </tr>
      </table>
    </form>
-->
  </xsl:template>

  <xsl:template match="enrollmentModel">
    <input name="{$userEnrollmentModelParam}" type="radio">
      <xsl:if test="@default='true'">
        <xsl:attribute name="checked">checked</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="value">
        <xsl:value-of select="."/>
      </xsl:attribute>
      <xsl:value-of select="."/>
      <br/>
    </input>
  </xsl:template>

  <xsl:template match="role">
    <xsl:choose>
      <xsl:when test="@default='true'">
        <option selected="selected"><xsl:value-of select="."/></option>
      </xsl:when>
      <xsl:otherwise>
        <option><xsl:value-of select="."/></option>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="channel">
    <xsl:variable name="handle"><xsl:value-of select="@id"/></xsl:variable>
    <input type="checkbox" name="{$channelParam}" checked="checked" value="{$handle}"><xsl:value-of select="."/><br/></input>
  </xsl:template>
    
</xsl:stylesheet>
