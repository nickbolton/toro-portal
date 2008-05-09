<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">

    <!-- <textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/>
      <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
      <parameter name="addCommand"><xsl:value-of select="$addCommand" /></parameter>
      <parameter name="selectGroupCommand"><xsl:value-of select="$selectGroupCommand" /></parameter>
      <parameter name="searchCommand"><xsl:value-of select="$searchCommand" /></parameter>
      <parameter name="viewCommand"><xsl:value-of select="$viewCommand" /></parameter>
      <parameter name="editCommand"><xsl:value-of select="$editCommand" /></parameter>
      <parameter name="deleteCommand"><xsl:value-of select="$deleteCommand" /></parameter>
      <parameter name="addSubmitCommand"><xsl:value-of select="$addSubmitCommand" /></parameter>
      <parameter name="editSubmitCommand"><xsl:value-of select="$editSubmitCommand" /></parameter>
      <parameter name="topicNameParam"><xsl:value-of select="$topicNameParam" /></parameter>
      <parameter name="topicDescParam"><xsl:value-of select="$topicDescParam" /></parameter>
      <parameter name="defaultRoleParam"><xsl:value-of select="$defaultRoleParam" /></parameter>
      <parameter name="confirmDeleteCommand"><xsl:value-of select="$confirmDeleteCommand" /></parameter>
      <parameter name="deleteConfirmationParam"><xsl:value-of select="$deleteConfirmationParam" /></parameter>
      <parameter name="searchTopicName"><xsl:value-of select="$searchTopicName" /></parameter>
      <parameter name="topicName"><xsl:value-of select="$topicName" /></parameter>
      <parameter name="catChannel"><xsl:value-of select="$catChannel" /></parameter>

      <parameter name="addTopic"><xsl:value-of select="$addTopic" /></parameter>
      <parameter name="editTopic"><xsl:value-of select="$editTopic" /></parameter>
      <parameter name="deleteTopic"><xsl:value-of select="$deleteTopic" /></parameter>
    </textarea> -->


    <xsl:apply-templates />
</xsl:template>

<xsl:template match="path">
    <xsl:if test="position()!=1">
        <br/>
    </xsl:if>
    <xsl:value-of select="." />
</xsl:template>

  <xsl:template match="topic">

    <xsl:call-template name="links"/>

    <table cellpadding="0" cellspacing="0" border="0" width="100%">

    <tr>
        <th colspan="2" class="th-top">Topic Details</th>
    </tr>

    <tr>
        <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="topicAdminFormParentGroup">Parent Group</label></td>
        <td class="table-content-right" style="text-align:left" width="100%">
            <xsl:apply-templates select="parentGroup/path"/>
        </td>
    </tr>

    <tr>
        <td class="table-light-left-top" style="text-align:right" id="topicAdminTopicName">
            Topic Name
        </td>
        <td class="table-content-right" style="text-align:left" width="40%" headers="topicAdminTopicName">
        <xsl:choose>
            <xsl:when test="$editTopic = 'Y'">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}" title="Edit this topic"
                onmouseover="swapImage('topicAdminEditImage','channel_edit_active.gif')"
                onmouseout="swapImage('topicAdminEditImage','channel_edit_base.gif')"><xsl:value-of select="name"/><img border="0" src=
                "{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                alt="'Edit' icon: edit topic details for '{name}'"
                title="'Edit' icon: edit topic details for '{name}'"
                align="absmiddle" name="topicAdminEditImage" id="topicAdminEditImage"/></a>
            </xsl:when>
            <xsl:otherwise>
                  <xsl:value-of select="name"/>
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
                alt="Inactive 'Edit' icon: edit topic details unavailable due to lack of permission"
                title="Inactive 'Edit' icon: edit topic details unavailable due to lack of permission" align="absmiddle"/>
            </xsl:otherwise>
        </xsl:choose>
            <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
        <xsl:choose>
            <xsl:when test="$deleteTopic = 'Y'">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$deleteCommand}&amp;ID={@id}&amp;searchTopicName={$searchTopicName}" title="Delete this topic"
                onmouseover="swapImage('topicAdminDeleteImage','channel_delete_active.gif')"
                onmouseout="swapImage('topicAdminDeleteImage','channel_delete_base.gif')">
                <img src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                alt="'Delete' icon: delete '{name}' topic"
                title="'Delete' icon: delete '{name}' topic"
                border="0" align="absmiddle" name="topicAdminDeleteImage" id="topicAdminDeleteImage"/></a>
            </xsl:when>
            <xsl:otherwise>
                <img src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
                border="0" align="absmiddle"
                alt="Inactive 'Delete' icon: delete topic is unavailable due to lack of permission"
                title="Inactive 'Delete' icon: delete topic is unavailable due to lack of permission"/>
            </xsl:otherwise>
        </xsl:choose>
        </td>
    </tr>

    <tr>
        <td class="table-light-left-bottom" style="text-align:right" id="topicAdminTopicDescription" valign="top">
            Topic Description
        </td>
        <td class="table-content-right-bottom" style="text-align:left" width="60%" headers="topicAdminTopicDescription" valign="top">
            <xsl:value-of select="description"/>
        </td>
    </tr>

    <xsl:for-each select="//adjunct">
        <xsl:call-template name="adjunct-data" />
    </xsl:for-each>

    </table>

  </xsl:template>

<xsl:template name="adjunct-data">

    <!-- The context node is <adjunct> -->

    <tr>
        <th colspan="2" class="th-top">
            <xsl:value-of select="choice-collection[position() = 1]/label" />
            <xsl:if test="$editTopic = 'Y'">
                <span style="font-weight:normal;">
                <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>|<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/><a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={//topic/@id}&amp;searchTopicName={$searchTopicName}" title="Edit settings for this topic"
                onmouseover="swapImage('topicAdminVirtuosoEditImage','channel_edit_active.gif')"
                onmouseout="swapImage('topicAdminVirtuosoEditImage','channel_edit_base.gif')">Edit<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/><img border="0" src=
                "{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                alt="'Edit' icon: edit topic details for '{name}'"
                title="'Edit' icon: edit topic details for '{name}'"
                align="absmiddle" name="topicAdminVirtuosoEditImage" id="topicAdminVirtuosoEditImage"/></a>
                </span>
            </xsl:if>
        </th>
    </tr>

    <tr>
        <td class="table-light-left-bottom" style="text-align:right" id="topicAdminVirtuosoContent" valign="top">
            <xsl:value-of select="decision-collection[position() = 1]/decision[position() = 1]/label" />
        </td>
        <td class="table-content-right-bottom" style="text-align:left" width="60%" headers="topicAdminVirtuosoContent" valign="top">
            <xsl:if test="count(decision-collection/decision/selection) = 0">
                None
            </xsl:if>
            <xsl:for-each select="decision-collection/decision/selection">
                <xsl:value-of select="@option"/><br/>
            </xsl:for-each>
        </td>
    </tr>

</xsl:template>

</xsl:stylesheet>











