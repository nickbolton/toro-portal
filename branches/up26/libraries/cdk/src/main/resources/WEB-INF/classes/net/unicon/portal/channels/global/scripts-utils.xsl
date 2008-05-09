<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- rollover JS -->
    <xsl:template name="commonJS">
        <script language="JavaScript1.2" type="text/javascript">
        window.focus(); // seems to be necessary to get Mozilla to display image title attributes
        function swapImage(imageTarget,imageSrc)
        {
            if (document.getElementById &amp;&amp; document.getElementById(imageTarget))
            {
                document.getElementById(imageTarget).src = '<xsl:value-of select="$CONTROLS_IMAGE_PATH"/>/'+imageSrc;
            }
        }
        </script>
    </xsl:template>


    <!-- Function to escape single and double quotes for
        given text string.  Requires argument named "textvalue".
    -->
    <xsl:template name="translateQuotes">
        <xsl:param name="TEXTVALUE"/>
        <xsl:variable name="SINGLE_RESULT">
            <xsl:call-template name="replaceString">
                <xsl:with-param name="TEXTVALUE" select="$TEXTVALUE"/>
                <xsl:with-param name="TEST">
                    <xsl:text/>'<xsl:text/>
                </xsl:with-param>
                <xsl:with-param name="REPLACEMENT">
                    <xsl:text/>`<xsl:text/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <xsl:text/><xsl:call-template name="replaceString">
            <xsl:with-param name="TEXTVALUE" select="$SINGLE_RESULT"/>
            <xsl:with-param name="TEST">
                <xsl:text/>"<xsl:text/>
            </xsl:with-param>
            <xsl:with-param name="REPLACEMENT">
                <xsl:text/>``<xsl:text/>
            </xsl:with-param>
        </xsl:call-template><xsl:text/>
    </xsl:template>


    <!-- Function to replace a substring for a given text string.
        Requires arguments named "textvalue", "replacement" and "test".
    -->
    <xsl:template name="replaceString">
        <xsl:param name="TEXTVALUE"/>
        <xsl:param name="TEST"/>
        <xsl:param name="REPLACEMENT"/>
        <xsl:choose>
            <xsl:when test="contains($TEXTVALUE, $TEST)">
                <xsl:text/><xsl:value-of
                    select="substring-before($TEXTVALUE, $TEST)"/><xsl:text/>
                <xsl:text/><xsl:value-of
                    disable-output-escaping="yes"
                    select="$REPLACEMENT"/><xsl:text/>
                <xsl:call-template name="replaceString">
                    <xsl:with-param name="TEXTVALUE"
                        select="substring-after($TEXTVALUE, $TEST)"/>
                    <xsl:with-param name="TEST" select="$TEST"/>
                    <xsl:with-param name="REPLACEMENT"
                        select="$REPLACEMENT"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of disable-output-escaping="yes" select="$TEXTVALUE"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- Function to escape a certain character for a given text string.
        Requires arguments named "textvalue" and "char".
    -->
    <xsl:template name="escapeChar">
        <xsl:param name="TEXTVALUE"/>
        <xsl:param name="CHAR"/>
        <xsl:choose>
            <xsl:when test="contains($TEXTVALUE, $CHAR)
                and not(contains($TEXTVALUE, '\'))">
                <xsl:text/><xsl:value-of
                    select="substring-before($TEXTVALUE, $CHAR)"/><xsl:text/>
                <xsl:text disable-output-escaping="yes">\</xsl:text><xsl:value-of disable-output-escaping="yes" select="$CHAR"/><xsl:text/>
                <xsl:call-template name="escapeChar">
                    <xsl:with-param name="TEXTVALUE" select="substring-after($TEXTVALUE, $CHAR)"/>
                    <xsl:with-param name="CHAR" select="$CHAR"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of disable-output-escaping="yes" select="$TEXTVALUE"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

