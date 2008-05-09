<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!--  -->

<xsl:template match="//theme-set">
    <script language="JavaScript">
        var initializeThemeStyle = function() {}
        var loadStyles = function(formName) {

            // Clear the styles select.
            var optns = document.getElementById(formName).style.options;
            while (optns.length > 0) optns[0] = null;

            // Load new values.
        <xsl:for-each select="theme">
            if (document.getElementById(formName).theme.selectedIndex == <xsl:value-of select="position()" />) {
            <xsl:for-each select="style">
                optns[optns.length] = new Option("<xsl:value-of select="@handle" />", "<xsl:value-of select="@handle" />");
            </xsl:for-each>
            }
        </xsl:for-each>

            // Load the "Don't Override" option if we're retuning to default.
            if (document.getElementById(formName).theme.selectedIndex == 0) {
                optns[0] = new Option("Default [Don't Override]", "");
            }

        }
        var resolveLink = function(formName, linkUrl, windowName, delimiter) {

            // Get out if we're using the default.
            if (document.getElementById(formName).theme.selectedIndex == 0) {
                window.open(linkUrl, windowName);
                return;
            }

            var themes = document.getElementById(formName).theme.options;
            var styles = document.getElementById(formName).style.options;

            var tLabel = "Theme=";
            var sLabel = "Style=";

            var tName = themes[themes.selectedIndex].value;
            var sName = styles[styles.selectedIndex].value;

            var tokens = linkUrl.split(delimiter);
            for (var i=0; i &lt; tokens.length; i++) {
                if (tokens[i].substring(0, tLabel.length).toUpperCase() == tLabel.toUpperCase()) {
                    tokens[i] = tokens[i].substring(0, tLabel.length) + tName;
                }
                if (tokens[i].substring(0, sLabel.length).toUpperCase() == sLabel.toUpperCase()) {
                    tokens[i] = tokens[i].substring(0, sLabel.length) + sName;
                }
            }

            var rslt = tokens[0];
            for (var i=1; i &lt; tokens.length; i++) {
                rslt = rslt + delimiter + tokens[i];
            }

            window.open(rslt, windowName);

        }
        var themeStyleFunctions = ["initializeThemeStyle", "loadStyles", "resolveLink"];
        if (window.channelFunctionsArray) {
            channelFunctionsArray[channelFunctionsArray.length] = themeStyleFunctions;
        } else {
            // else create channelFunctionsArray with this as first entry
            channelFunctionsArray = [themeStyleFunctions]; // create 2-D array
        }
    </script>
</xsl:template>

<xsl:template name="theme-selection">
    <xsl:param name="FORMNAME"/>
    <select name="theme" onchange="loadStyles('{$FORMNAME}')">
        <option value="">Default [Don't Override]</option>
        <xsl:for-each select="//theme-set/theme">
            <xsl:call-template name="theme" />
        </xsl:for-each>
    </select>
</xsl:template>

<xsl:template name="theme">
    <option value="{@handle}"><xsl:value-of select="@handle" /></option>
</xsl:template>

<xsl:template name="style-selection">
    <select name="style">
        <option value="">Default [Don't Override]</option>
    </select>
</xsl:template>

</xsl:stylesheet>