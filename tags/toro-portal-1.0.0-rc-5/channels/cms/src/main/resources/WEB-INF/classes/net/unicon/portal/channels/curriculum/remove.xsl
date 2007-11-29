<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes" />
  <!-- Include Files -->
  <xsl:include href="common.xsl"/>
  <xsl:param name="id" />

<xsl:template match="/">
    <!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
    </textarea>
     -->
    
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="course-list">
    <xsl:call-template name="links"/>

<form name="curriculumForm" action="{$baseActionURL}?command=delete" method="post">
<input type="hidden" name="id" value="{$id}"></input>	
	<!-- UniAcc: Layout Table -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th colspan="2" class="th-top-single">
                Remove Curriculum
            </th>
        </tr>
        <tr>
            <td class="table-content-single" style="text-align:center">
                <span class="uportal-channel-warning">Are you sure you want to remove the curriculum from this offering?</span>
            </td>
        </tr>
        <tr>
            <td class="table-content-single" style="text-align:center;">
                <input type="radio" class="radio" name="commandButton" value="confirm" id="ccrr1"/>
				<label for="ccrr1">&#032;Yes</label>
                <img height="1" width="15" src="{$SPACER}" alt=""/>
                <input type="radio" class="radio" name="commandButton" value="no" checked="checked" id="ccrr2"/>
				<label for="ccrr2">&#032;No</label>
				<br />
            </td>
        </tr>
        <tr>
            <td class="table-content-single-bottom" style="text-align:center">
               <input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to viewing the list of curriculum"/>
				&#032;&#032;&#032;&#032;
               <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to viewing the list of curriculum without deleting this item"/>
            </td>
        </tr>        
    </table>
</form>

</xsl:template>
</xsl:stylesheet>
