<?xml version='1.0'?>
<!--
   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.
  
   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.
  
   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>
    <xsl:output method='html' />
    <xsl:template name='t24to12'>
        <xsl:param name='hour' />
        <xsl:variable name='h' select='substring-before($hour,":")' />
        <xsl:variable name='m' select='substring-after($hour,":")' />
        <xsl:choose>
            <xsl:when test='$h &gt; 12'>
                <xsl:value-of select='concat($h - 12,":",$m," pm")' />
            </xsl:when>
            <xsl:when test='$h = 12'>
                <xsl:value-of select='concat($h,":",$m," pm")' />
            </xsl:when>
            <xsl:when test='$h = 0'>
                <xsl:value-of select='concat("12",":",$m," am")' />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='concat($h,":",$m," am")' />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="category-none">
        <xsl:param name="category" />
        <select name='category' class="text">
            <option value='None'>None</option>
            <option value='Assignment'>
            <xsl:if test='contains($category,"Assignment")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Assignment</option>
            <option value='Appointment'>
            <xsl:if test='contains($category,"Appointment")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Appointment</option>
            <option value='Call'>
            <xsl:if test='contains($category,"Call")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Call</option>
            <option value='Holiday'>
            <xsl:if test='contains($category,"Holiday")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Holiday</option>
            <option value='Interview'>
            <xsl:if test='contains($category,"Interview")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Interview</option>
            <option value='Meeting'>
            <xsl:if test='contains($category,"Meeting")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Meeting</option>
            <option value='Party'>
            <xsl:if test='contains($category,"Party")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Party</option>
            <option value='Travel'>
            <xsl:if test='contains($category,"Travel")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Travel</option>
            <option value='Vacation'>
            <xsl:if test='contains($category,"Vacation")'>
                <xsl:attribute name='selected'>true</xsl:attribute>
            </xsl:if>
            Vacation</option>
        </select>
        <br />
    </xsl:template>
    <xsl:template name="break-line">
        <xsl:param name="st" />
        <xsl:param name="chunk-len" />
        <xsl:variable name="first" select='substring-before($st,"&#xA;")' />
        <xsl:variable name="rest" select='substring-after($st,"&#xA;")' />
        <xsl:choose>
            <xsl:when test="$first or $rest">
                <xsl:call-template name="break-line-by-chunk">
                    <xsl:with-param name="st" select='$first' />
                    <xsl:with-param name="chunk-len" select='$chunk-len' />
                </xsl:call-template>
                <xsl:if test="$rest">
                    <br />
                    <xsl:message>
                        <xsl:value-of select="$first" />
                    </xsl:message>
                    <xsl:call-template name="break-line">
                        <xsl:with-param name="st" select="$rest" />
                        <xsl:with-param name="chunk-len" select='$chunk-len' />
                    </xsl:call-template>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="break-line-by-chunk">
                    <xsl:with-param name="st" select='$st' />
                    <xsl:with-param name="chunk-len" select='$chunk-len' />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="break-line-by-chunk">
        <xsl:param name="st" />
        <xsl:param name="chunk-len" />
        <xsl:if test="contains($st,'&#160;')">
            <xsl:message>
                <xsl:value-of select="$st" />
            </xsl:message>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="(string-length($st) &gt; $chunk-len) and not(contains($st,'&#160;')) and not(contains($st,'&#x9;')) and not(contains($st,'&#x20;'))">
                <xsl:variable name="first" select='substring($st,0,$chunk-len)' />
                <xsl:variable name="rest" select='substring($st,$chunk-len+1)' />
                <xsl:choose>
                    <xsl:when test="$first or $rest">
                        <xsl:value-of select="$first" />
                        <xsl:if test="$rest">
                            <br />
                            <xsl:call-template name="break-line-by-chunk">
                                <xsl:with-param name="st" select="$rest" />
                                <xsl:with-param name="chunk-len" select='$chunk-len' />
                            </xsl:call-template>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$st" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$st" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

