<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:include href="common.xsl"/>

<xsl:template match="/">

    <!--<textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/> 
   	</textarea> -->
	<xsl:call-template name="links"/>
	<br/>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr>
        <th colspan="2" class="th-top">
            Campus Announcements
        </th>
    </tr>
    <tr>
        <td class="table-light-left-bottom" nowrap="nowrap">
          Announcement:
        </td>

        <td class="table-content-right-bottom" width="100%">
              Currently there are no announcements matching your preferences.
        </td>
    </tr>
    </table>
</xsl:template>
</xsl:stylesheet>
