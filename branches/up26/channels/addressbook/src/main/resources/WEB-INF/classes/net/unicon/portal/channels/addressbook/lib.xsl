<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <!-- 
    convert list of string into one string 
    Ex : a b c d is string -> a, b, c.
  -->
    <xsl:template name="esc-strings">
        <xsl:param name="title"/>
        <xsl:param name="department"/>
        <xsl:param name="company"/>
        <xsl:param name="cell-phone"/>
        <xsl:param name="business-phone"/>
        <xsl:param name="home-phone"/>
        <xsl:param name="email"/>
        <xsl:variable name="s">
            <xsl:if test="boolean($title)">
                <xsl:value-of select="$title"/>
                <xsl:text>,&#160;</xsl:text>
            </xsl:if>
            <xsl:if test="boolean($department)">
                <xsl:value-of select="$department"/>
                <xsl:text>,&#160;</xsl:text>
            </xsl:if>
            <xsl:if test="boolean($company)">
                <xsl:value-of select="$company"/>
                <xsl:text>,&#160;</xsl:text>
            </xsl:if>
            <xsl:if test="boolean($cell-phone)">
                <xsl:value-of select="$cell-phone"/>
                <xsl:text>&#160;(M),&#160;</xsl:text>
            </xsl:if>
            <xsl:if test="boolean($business-phone)">
                <xsl:value-of select="$business-phone"/>
                <xsl:text>&#160;(O),&#160;</xsl:text>
            </xsl:if>
            <xsl:if test="boolean($home-phone)">
                <xsl:value-of select="$home-phone"/>
                <xsl:text>&#160;(H),&#160;</xsl:text>
            </xsl:if>
            <xsl:if test="boolean($email)">
                <xsl:value-of select="$email"/>
                <xsl:text>,&#160;</xsl:text>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="result">
            <xsl:if test="boolean($s=' ')=false">
                <xsl:variable name="len">
                    <xsl:value-of select="number(string-length($s)) - 2"/>
                </xsl:variable>
                <xsl:value-of select="substring($s , 1 , $len)"/>
            </xsl:if>
        </xsl:variable>
        <xsl:value-of select="$result"/>
    </xsl:template>
</xsl:stylesheet>
