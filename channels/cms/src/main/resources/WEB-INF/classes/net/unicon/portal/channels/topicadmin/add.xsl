<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">

    <!-- <textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/>
    </textarea> -->
	<xsl:call-template name="links"/>
	<h2 class="page-title">Add Topic</h2>
	<div class="bounding-box1">
    	<xsl:apply-templates select="topicAdmin"/>
    </div>
</xsl:template>

<xsl:template match="path">
    <xsl:if test="position()!=1">
        <br/>
    </xsl:if>
    <a href="javascript:void(document.topicAdminForm.next_command.value='{$subsequentAddCommand}', document.topicAdminForm.command.value='{$selectParentCommand}', document.topicAdminForm.submit());" title="Edit the parent topic"><xsl:value-of select="."/></a>
</xsl:template>


<xsl:template match="topicAdmin">
      <xsl:call-template name="autoFormJS"/>

        <form method="post" onSubmit="return checkTopicAdminFormSubmit(this,'{$selectParentCommand}');" name="topicAdminForm" action="{$baseActionURL}">
        <input type="hidden" name="targetChannel" value="{$targetChannel}" />
        <input type="hidden" name="command" value="{$addSubmitCommand}" />
        <input type="hidden" name="next_command" value="" />
        <input type="hidden" name="searchTopicName" id="searchTopicName" value="{$searchTopicName}" />
        <input type="hidden" name="catPageSize" id="catPageSize" value="{$catPageSize}" />
        <table cellpadding="0" cellspacing="0" border="0" width="100%">

            <!-- Selecting a group for the parent group invokes the group selec
tion servant (currently from the addressbook - select.xsl). The channel is take
n over by this servant for the selection of a group, then returns to this add f
orm. Initially, there is no parent group selected. The user will have to go and
 select a group before finishing the addition process. Once a group has been se
lected, the option should be available to re-enter the groups selection servant
 and pick a different group. -->
            <tr>
                 <td class="table-light-left" style="text-align:right;vertical-a
lign:top;" nowrap="nowrap">
                    <label for="topicAdminFormParentGroup">Parent Group</label>
                </td>
            <xsl:choose>
                <xsl:when test="not(topic/parentGroup)">
                    <input type="hidden" name="parentGroupId" value="" />
                    <td class="table-content-right" style="text-align:left" width="100%">
                        <input class="uportal-button" type="submit" value="Select"
                            title="Select a parent group for this topic"
                            onClick="document.topicAdminForm.command.value='{$selectParentCommand}'; document.topicAdminForm.next_command.value='{$subsequentAddCommand}';"/>
                    </td>
                </xsl:when>
                <xsl:otherwise>
                    <td class="table-content-right" style="text-align:left" width="100%">
                        <input type="hidden" name="parentGroupId" value="{topic/parentGroup/@id}" />
                        <xsl:apply-templates select="topic/parentGroup/path"/>
                    </td>
                </xsl:otherwise>
            </xsl:choose>
            </tr>

            <tr>
                <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="topicAdminFormTopicName">Topic Name</label></td>
                <td class="table-content-right" style="text-align:left" width="100%"><input name="{$topicNameParam}" type="text" class="text" value="{topic/name}" id="topicAdminFormTopicName" size="70" maxlength="80"/></td>
            </tr>

            <tr>
                <td class="table-light-left" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="topicAdminFormTopicDescription">Topic Description</label></td>
                <td class="table-content-right" width="100%"><textarea name="{$topicDescParam}" rows="4" cols="62" id="topicAdminFormTopicDescription"><xsl:value-of select="topic/description"/></textarea></td>
            </tr>

            <xsl:for-each select="//adjunct/choice-collection">
                <xsl:call-template name="adjunct-data" />
            </xsl:for-each>

            <tr>
                <td colspan="2" class="table-nav" style="text-align:center">
                    <input class="uportal-button" type="submit" value="Submit"
                    title="Submit to add this new topic"/>
                    <input type="button" class="uportal-button" value="Cancel"
                    title="Cancel to return to the topics search without adding a new topic"
                    onclick="window.locationhref='{$baseActionURL}?catPageSize={$catPageSize}&amp;searchTopicName=';"/></td>
            </tr>

        </table>
        </form>

        <!-- <form method="post" onSubmit="return checkTopicSubmit()" name="topicAdminForm" action="{$baseActionURL}">
        <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
        <input type="hidden" name="searchTopicName" id="searchTopicName" value="{$searchTopicName}" />
        <input type="hidden" name="command" value="{$addSubmitCommand}"></input>
        <table>
        <tr>
        <th class="uportal-background-med">Property</th>
        <th class="uportal-background-med">Value</th>
        </tr>

        <tr>
        <td class="uportal-background-light">Topic Name</td>
        <td class="uportal-background-light" style="text-align:center"><input name="{$topicNameParam}" type="text" /></td>
        </tr>

        <tr>
        <td class="uportal-background-light">Topic Description</td>
        <td class="uportal-background-light" style="text-align:center"><textarea name="{$topicDescParam}" rows="4"></textarea></td>
        </tr>

        <tr>
        <td colspan="2" style="text-align:center"><nobr><input type="submit" value="Submit"/><input type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?catPageSize={$catPageSize}&amp;searchTopicName='"/></nobr></td>
        </tr>

        </table>
        </form> -->

</xsl:template>

<xsl:template name="adjunct-data">

    <!-- The context node is <choice-collection> -->

    <tr>
        <th colspan="2" class="th-top"><xsl:value-of select="label" /></th>
    </tr>

    <tr>
        <td class="table-light-left" style="text-align:right;vertical-align:top;"><img height="1" width="1" src="{$SPACER}" alt="" title="" border="0" /></td>
        <td class="table-content-right" width="100%" valign="top">
            <xsl:choose>
                <xsl:when test="count(choice[position() = 1]/option) = 0">
                    None available
                </xsl:when>

                <xsl:otherwise>
                    <!-- The choice-decision collection rendering is done through a XSL module initiated in the global.xsl file -->
                    <xsl:apply-templates select="."/>
                </xsl:otherwise>
            </xsl:choose>

        </td>
    </tr>

</xsl:template>

<xsl:template match="group">
  <option value="{@id}"><xsl:value-of select="name"/></option>
  <xsl:apply-templates select="group"/>
</xsl:template>


</xsl:stylesheet>
