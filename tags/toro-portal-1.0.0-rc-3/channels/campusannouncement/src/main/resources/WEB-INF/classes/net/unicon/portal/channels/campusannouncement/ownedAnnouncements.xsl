<?xml version="1.0"?>
		<xsl:call-template name="links">
			<xsl:with-param name="curPage">edit</xsl:with-param>
		</xsl:call-template>
		
        <h2 class="page-title">Manage Announcements</h2>
        <div class="bounding-box1">
					  <td colspan="4">
						<input class="uportal-button" type="button" value="Select All">
							<xsl:attribute  name = "onclick" >for(var i=0; i&lt; this.form.announcementIDs.length; i++){this.form.announcementIDs[i].checked=true}</xsl:attribute>
						</input>
						<xsl:text> </xsl:text>
						<input class="uportal-button" type="button" value="Deselect All">
							<xsl:attribute  name = "onclick" >for(var i=0; i&lt; this.form.announcementIDs.length; i++){this.form.announcementIDs[i].checked=false}</xsl:attribute>
						</input>
					  </td>
					</tr> 
					  </td>
    </xsl:template>