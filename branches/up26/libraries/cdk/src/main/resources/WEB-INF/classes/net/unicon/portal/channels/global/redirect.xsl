<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<xsl:param name="redirect"/>


<xsl:template match="/">
    <script language="JavaScript" type="text/javascript">
        location.replace("<xsl:value-of select="$redirect" />");
    </script>
</xsl:template>

</xsl:stylesheet>
