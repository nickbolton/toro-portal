<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:include href="common.xsl"/>

<xsl:output method="html" indent="yes" />
    
    <xsl:template match="//roster">
            <!--<textarea rows="4" cols="40"><xsl:copy-of select = "*"/></textarea>-->
        <!--<textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/> 
            <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>   
            <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
            <parameter name="editOfferingPermissions"><xsl:value-of select="$editOfferingPermissions" /></parameter>   
            <parameter name="editOfferingPermissionsCommand"><xsl:value-of select="$editOfferingPermissionsCommand" /></parameter>   
            <parameter name="editUserPermissions"><xsl:value-of select="$editUserPermissions" /></parameter>   
            <parameter name="editUserPermissionsCommand"><xsl:value-of select="$editUserPermissionsCommand" /></parameter>   
            <parameter name="enrollCommand"><xsl:value-of select="$enrollCommand" /></parameter>   
            <parameter name="enrollUser"><xsl:value-of select="$enrollUser" /></parameter>   
            <parameter name="enrollViewCommand"><xsl:value-of select="$enrollViewCommand" /></parameter>   
            <parameter name="importCommand"><xsl:value-of select="$importCommand" /></parameter>   
            <parameter name="offeringId"><xsl:value-of select="$offeringId" /></parameter>   
            <parameter name="pageCommand"><xsl:value-of select="$pageCommand" /></parameter>   
            <parameter name="roleId"><xsl:value-of select="$roleId" /></parameter>   
            <parameter name="roleName"><xsl:value-of select="$roleName" /></parameter>   
            <parameter name="searchCommand"><xsl:value-of select="$searchCommand" /></parameter>   
            <parameter name="type"><xsl:value-of select="$type" /></parameter>   
            <parameter name="unenrollCommand"><xsl:value-of select="$unenrollCommand" /></parameter>   
            <parameter name="unenrollUser"><xsl:value-of select="$unenrollUser" /></parameter>   
            <parameter name="updateOfferingPermissionsCommand"><xsl:value-of select="$updateOfferingPermissionsCommand" /></parameter>   
            <parameter name="updateUserPermissionsCommand"><xsl:value-of select="$updateUserPermissionsCommand" /></parameter>   
            <parameter name="userId"><xsl:value-of select="$userId" /></parameter>   
            <parameter name="userIdParam"><xsl:value-of select="$userIdParam" /></parameter>   
            <parameter name="viewMemberCommand"><xsl:value-of select="$viewMemberCommand" /></parameter>   
            <parameter name="viewUserInfo"><xsl:value-of select="$viewUserInfo" /></parameter>   
        </textarea> -->
        <xsl:call-template name="links"/>
        <form action="{$baseActionURL}" method="post" name="rosterForm">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
            <input type="hidden" name="catPageSize" value="{$catPageSize}" />
            <input type="hidden" name="command" value="{$updateUserPermissionsCommand}"></input>
            <input type="hidden" name="type" value="{$type}"></input>
            <input type="hidden" name="uid" value="{$userId}"></input>
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th" colspan="2" id="EditRole">Edit Role</th>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="EditRole" id="ID">
                        Student ID
                    </td>
                    <td class="table-content-right" style="text-align:left;" width="100%" headers="EditRole ID">
                        <xsl:value-of select="$userId"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right; vertical-align:top;" nowrap="nowrap" headers="EditRole" id="List">
                        List of Channels
                    </td>
                    <td class="table-content-right" style="text-align:left;" width="100%" headers="EditRole List">
                        <xsl:apply-templates select="*[name()='channel' or name()='application']">
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
                <input name="{../../@handle}-{@handle}" type="checkbox" class="radio" checked="true" id="{@handle}"/>
            </xsl:if>
            <xsl:if test="@allowed != 'Y'">
                <input name="{../../@handle}-{@handle}" class="radio" type="checkbox" id="{@handle}"/>
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
