<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
    
<!-- Include Files -->
<xsl:include href="common.xsl"/>
   <xsl:template match="/owned-announcements"> 
   	 <h2 class="page-title">Delete Selected Announcements</h2>
   	 
   	 <div class="bounding-box1">
        <form action="{$baseActionURL}?command=main" method="post">
            
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                  <th class="th" colspan="2"></th>
                </tr>
                <tr>
                  <td class="table-content-single-top" colspan="2">
                  <span class="uportal-channel-warning">Are you sure you want to delete this announcement?</span><br />
                  <img height="10" width="1" src="{$SPACER}" alt=""/><br />                                                              
                  <span class="uportal-channel-copyright"/>
                  <input type="hidden" name="sub-command" value="submit-deletes"/> 
                 </td> 
                 </tr> 
                <xsl:for-each select="announcement">
                  <tr> 
                    <input type="hidden" name="announcementIDs" value="{@announcement-id}"/>
                    <td class="table-content-right" align="left" width="10%">
                    <xsl:value-of select="@date" />
                    </td>
                    <td class="table-content-right" align="left" width="90%">
            	      <span class="uportal-channel-copyright">
                      <xsl:apply-templates select="announcement-body" />
                      </span>
                    </td>
                  </tr>
                </xsl:for-each>
                
                <tr>
                  <td colspan="2" class="table-nav">
                    <input name="Submit" value="Submit" type="submit" class="uportal-button" title="To submit your response and return to the edit announcements page."/>
                    <input name="Submit" value="Cancel" type="submit" class="uportal-button" align="left" title="To return to the edit annoucements page."/>
                  </td>
                </tr>
            </table>
        </form>
      </div>
    </xsl:template>

    <xsl:template match="announcement-body">
        <xsl:apply-templates />
    </xsl:template>

</xsl:stylesheet>
