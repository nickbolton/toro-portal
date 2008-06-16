<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Parameters -->
    <xsl:param name="targetChannel"/>
    <xsl:param name="baseActionURL"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
            <!-- 
            <textarea rows="4" cols="40">
                <xsl:copy-of select="*"/>
                <parameter name="nodeid"><xsl:value-of select="$nodeid" /></parameter>  
                <parameter name="nodetype"><xsl:value-of select="$nodetype" /></parameter>  
                <parameter name="nodeurl"><xsl:value-of select="$nodeurl" /></parameter>  
                <parameter name="nodeurl"><xsl:value-of select="$nodeurl" /></parameter>  
                <parameter name="nodeurl"><xsl:value-of select="$nodeurl" /></parameter>  
            </textarea>
             -->
        <xsl:call-template name="links"/>
        <xsl:call-template name="autoFormJS"/>
        <xsl:call-template name="editElement"/>
        <!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>   
    </textarea>
     -->
    </xsl:template>
    <xsl:template name="editElement">
        <form name="ResourcesForm" action="{$baseActionURL}" method="post" onsubmit="return validator.applyFormRules(this, new ResourcesRulesObject())">
            <input type="hidden" name="command" value="s-edit"/>
            <input type="hidden" name="oid" value="{$nodeid}"/>
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <!-- UniAcc: Data Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th-top-single" id="EditElement">
                        <!-- Choose the type of Resource Element title to display based on param 'nodetype' -->
                        <xsl:choose>
                            <xsl:when test="$nodetype='url-element'">
                                Edit URL
                            </xsl:when>
                            <xsl:when test="$nodetype='file'">
                                Edit File
                            </xsl:when>
                            <xsl:otherwise>
                                Edit Folder
                            </xsl:otherwise>
                        </xsl:choose>
                        
                    </th>
                </tr>
                <!-- If the element is a URL, display a row to edit the href -->
                <xsl:if test = "$nodetype='url-element'">
                    <tr>
                        <td class="table-light-left" style="text-align:right" valign="top" headers="EditElement" id="URL">
                            <label for="rrceut1">URL:</label>
                        </td>
                        <td class="table-content-right" headers="EditURL URL">
                            <input type="text" class="text" size="55" maxlength="254" value="{$nodeurl}" name="entryURL" id="rrceut1"/>
                        </td>
                    </tr>
                </xsl:if>
                <tr>
                    <td class="table-light-left" style="text-align:right" valign="top" headers="EditElement" id="Title">
                        <label for="rrcert1">Title:</label>
                    </td>
                    <td class="table-content-right" headers="EditElement Title">
                        <input type="text" class="text" size="55" maxlength="254" name="name" id="rrcert1">
                            <xsl:attribute name="value" ><xsl:value-of disable-output-escaping="yes" select="$nodename" /></xsl:attribute>
                        </input> 
                        <!-- Needed to output tag as plain text to avoid XSL escaping special characters -->
                        <!-- 
                        <xsl:text disable-output-escaping = "yes">&lt;input type="text" class="text" size="55" maxlength="254" name="name" id="rrcert1" value="</xsl:text><xsl:value-of disable-output-escaping="yes" select="$nodename" /><xsl:text disable-output-escaping = "yes">" /&gt;</xsl:text>
                        -->
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" style="text-align:right" valign="top" headers="EditElement" id="Desc">
                        <label for="rrcerta1">Description:</label> 
                    </td>
                    <td class="table-content-right" headers="EditElement Desc">
                        <textarea name="desc" cols="40" id="rrcerta1">
                            <xsl:value-of select="$nodedesc" />
                        </textarea>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav" style="text-align:center">
                        <input class="uportal-button" type="submit" value="Submit" title="To add this Folder and return to viewing all Related Resources."/>
                        &#032;&#032;&#032;&#032;
                        <input class="uportal-button"  type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel and return to viewing all Related Resources."/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
    
</xsl:stylesheet>
