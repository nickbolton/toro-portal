<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes" />
  
  <!-- Include Files -->
  <xsl:include href="common.xsl"/>
  
  <xsl:template match="/">
<!--		<textarea rows="4" cols="40">
	        <xsl:copy-of select = "*"/> 
	        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
	        <parameter name="numberPerPage"><xsl:value-of select="$numberPerPage" /></parameter>   
	        <parameter name="canImport"><xsl:value-of select="$canImport" /></parameter>   
	    </textarea>
 -->	    
	    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="import">
  
      <xsl:call-template name="links"/>
      
	  <form action="{$baseActionURL}" name="importChannelForm" method="post">
	  <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
	  <!-- UniAcc: Layout Table -->
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td class="table-light-left-top" style="text-align:right;vertical-align:top;" nowrap="nowrap"><label for="importChannelFormSelect">Import Type:</label></td>
        <td class="table-content-right-top" width="100%" style="vertical-align:top;">
	      <nobr><select name="command" id="importChannelFormSelect">
            <option value="importUsers">Users</option>
            <option value="importOfferings">Offerings</option>
          </select>
            <a href="javascript:document.importChannelForm.submit();" title="Select type to import"
            onmouseover="swapImage('importImage','channel_import_active.gif')"
            onmouseout="swapImage('importImage','channel_import_base.gif')">
	            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_import_base.gif" 
	            alt="'Import' icon: import selected type" 
	            title="'Import' icon: import selected type" 
	            align="absmiddle" name="importImage" id="importImage"/>
			</a>
	      </nobr></td>
      </tr>
      </table>
      </form>
      <xsl:apply-templates select="message"/>
      
  </xsl:template>

  <xsl:template match="message">
	  <!-- UniAcc: Layout Table -->
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <th class="th">
            <xsl:value-of select="."/>
        </th>
      </tr>
            
      <tr>
        <td class="table-content-single-bottom">
            <xsl:apply-templates select="/import/result"/>
        </td>
      </tr>
      
      </table>
  </xsl:template>

  <xsl:template match="/import/result">
    <ul>
        <li><span class="uportal-channel-warning"><xsl:value-of select="@msg"/></span>
            <ul><xsl:apply-templates select="entry"/></ul>
        </li>
    </ul>
  </xsl:template>

  <xsl:template match="entry">
      <li><xsl:value-of select="."/></li>
  </xsl:template>

</xsl:stylesheet>


