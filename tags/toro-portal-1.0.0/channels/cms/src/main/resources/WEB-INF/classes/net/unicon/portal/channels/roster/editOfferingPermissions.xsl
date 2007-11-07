<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>
    <xsl:output method="html" indent="yes"/>
    <xsl:template match="/">
        <xsl:call-template name="links"/>
         <form action="{$baseActionURL}" method="post" name="rosterForm">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
            <input type="hidden" name="catPageSize" value="{$catPageSize}" />
            <input type="hidden" name="command" value="{$updateOfferingPermissionsCommand}"></input>
            <input type="hidden" name="roleId" value="{$roleId}"></input>
            <!-- UniAcc:  Data Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th-top" colspan="2" id="EditRole">Edit Role</th>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="EditRole" id="Title">Title</td>
                    <td class="table-content-right" style="text-align:left;" width="100%" headers="EditRole Title">
                        <xsl:value-of select="$roleName"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right; vertical-align:top;" nowrap="nowrap" headers="EditRole" id="List">
                        List of Channels
                    </td>
                    <td class="table-content-right" style="text-align:left;" width="100%" headers="EditRole List">
                        <xsl:apply-templates select="manifest/*[name()='channel' or name()='application']">
                            <xsl:sort select="./label"/>
                        </xsl:apply-templates>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav" colspan="2" style="text-align:center;">
                        <input class="uportal-button" name="submit" value="Update" type="submit" title="To save these changes and return to the main view of the roster"/>
	                    <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=page&amp;catPageSize={$catPageSize}'" title="To cancel these changes and return to the main view of the roster"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
    <xsl:template match="channel">
        <li>
            <xsl:value-of select="./label"/>
            <ul>
                <xsl:apply-templates select="permissions/activity">
                    <xsl:sort select="./label"/>
                </xsl:apply-templates>
            </ul>
        </li>
    </xsl:template>
    <xsl:template match="application">
        <li><xsl:value-of select="./label"/>
            <ul>
                <xsl:apply-templates select="permissions/activity">
                    <xsl:sort select="./label"/>
                </xsl:apply-templates>
            </ul>
        </li>
    </xsl:template>
    <xsl:template match="activity">
        <li>
            <xsl:if test="@allowed = 'Y'">
                <input id="{@handle}" name="{../../@handle}-{@handle}" type="checkbox" class="radio" checked="true"/>
            </xsl:if>
            <xsl:if test="@allowed != 'Y'">
                <input id="{@handle}" name="{../../@handle}-{@handle}" class="radio" type="checkbox"/>
            </xsl:if>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <label for="{@handle}"><xsl:value-of select="label"/></label>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <a href="javascript:alert('{description}');void(null);" title="{description}" onmouseover="swapImage('rosterActivityHelp{@handle}Image','channel_help_active.gif')" onmouseout="swapImage('rosterActivityHelp{@handle}Image','channel_help_base.gif')">
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif" alt="'Help' icon linking to an alert box explaining: {description}" title="'Help' icon linking to an alert box explaining: {description}" align="absmiddle" name="rosterActivityHelp{@handle}Image" id="rosterActivityHelp{@handle}Image"/>
            </a>
        </li>
    </xsl:template>
</xsl:stylesheet>
