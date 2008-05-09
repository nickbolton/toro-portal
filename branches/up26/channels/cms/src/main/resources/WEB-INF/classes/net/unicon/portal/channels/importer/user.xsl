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
 -->	    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="import">
  
        <xsl:call-template name="links"/>
        <xsl:call-template name="autoFormJS"/>
      
      <form name="importChannelForm" action="{$baseActionURL}?command=import" 
	  enctype='multipart/form-data' method="post" 
	  onsubmit="return validator.applyFormRules(this, new ImportRulesObject())">
	      <input type="hidden" name="targetChannel" value="{$targetChannel}" />
	      <input type="hidden" name="import-type" value="users" />
      
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td class="table-light-left" colspan="2" align="center">
			Import Users
		</td>
      </tr>
      <tr>
        <td class="table-light-left">
			<label for="importChannelFormFile">File:</label>
		</td>
        <td class="table-content-right">
	        <!-- Inline style to fix bug with Netscape & Input type= "file" - apparently due to text align other than "left" -->
	        <input name="import-file" style="text-align: left;" type="file" 
			size="20" maxlength="100" id="importChannelFormFile" />
        </td>
      </tr>
      
      <tr>
		<td colspan="2" class="table-nav" align="center">
			<nobr>
			<input type="submit" class="uportal-button" value="Submit" 
			title="Submit selected file to import users" />
			<input type="button" class="uportal-button" value="Cancel" 
			onclick="window.locationhref='{$baseActionURL}'" 
			title="Return to import selection view without importing users" />
			</nobr>
		</td>
        <!--<td colspan="2" class="table-nav" align="center">
        	<input name="submit" value="Submit" type="submit" class="uportal-button" />
        </td> -->
      </tr>
      
      </table>
      </form>
      
  </xsl:template>
</xsl:stylesheet>


