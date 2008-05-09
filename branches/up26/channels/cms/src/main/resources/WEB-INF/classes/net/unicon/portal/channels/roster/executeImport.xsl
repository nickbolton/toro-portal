<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="roster">
        <xsl:call-template name="links"/>
        <xsl:apply-templates select="results"/>
    </xsl:template>
    <xsl:template match="results">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="th-top" align="center">
                    Import Results
                </td>
            </tr>
            <tr>
                <td class="table-content-single-bottom">
                    <xsl:apply-templates select="message"/>
                    <xsl:apply-templates select="success"/>
                    <xsl:apply-templates select="failure"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template match="message">
        <p><xsl:value-of select="." /></p>
    </xsl:template>
    
    <xsl:template match="success">
        <xsl:if test="count(entry) > 0">
            The following users were successfully imported:
            <ul>
                <xsl:apply-templates select="entry" />
            </ul>
        </xsl:if>
    </xsl:template>

    <xsl:template match="failure">
        <xsl:if test="count(reason) > 0">
            These users were not imported for the following reasons:
            <ul>
                <xsl:for-each select="reason">
                    <li>
                        <span class="uportal-channel-warning">
                            <xsl:value-of select="@msg" />
                        </span>
                        <ul>
                            <xsl:apply-templates select="entry" />
                        </ul>
                    </li>
                </xsl:for-each>
            </ul>
        </xsl:if>
    </xsl:template>

    <xsl:template match="entry">
        <li><xsl:value-of select="."/></li>
    </xsl:template>

</xsl:stylesheet>
