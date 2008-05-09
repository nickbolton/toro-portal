<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:import href="common_main.xsl"/>
	<xsl:output method="html" indent="yes"/>
    <xsl:param name="subLinkParameter">update_gb</xsl:param>
	<xsl:param name="showGradeColumn">Y</xsl:param>
	<xsl:param name="ONSUBMIT">return validator.applyFormRules(this, new GradebookScoresRulesObject(this))</xsl:param>
	<!-- ######################################################################## -->
   <xsl:template match="/">
<!-- <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
    </textarea>  -->
		<!-- Start JavaScript -->
		<script type="text/javascript" language="JavaScript">
    	    // create initialize method so that it can be initialized outside of page load
    	    initializeGradebookChannel = function()
    	    {
    				    rows = <xsl:value-of select="count(./gradebooks/gradebook-item)"/>; 
    	    }
		</script>
		<!-- End Javascript -->
		<!-- Apply Common Main templates -->
		<xsl:apply-imports/>
   </xsl:template>
	<!-- ######################################################################## -->
   <xsl:template name="meanDataField">
      <xsl:if test="position() = last()">
<!-- This should contain the class info: i.e. gradebook-data-right -->
<!--<xsl:attribute name="style">border-right: 4px solid #8F8870;</xsl:attribute> -->
      </xsl:if>

      <input tabindex="-1" type="text" class="text-disabled" size="3" value="{@mean}" name="Mean_{@id}" readonly="readonly" title="This is the calculated mean value for the scores in this column." />
   </xsl:template>

<!-- ######################################################################## -->
   <xsl:template name="medianDataField">
      <xsl:if test="position() = last()">
<!-- This should contain the class info: i.e. gradebook-data-right -->
<!--<xsl:attribute name="style">border-right: 4px solid #8F8870;</xsl:attribute> -->
      </xsl:if>

      <input tabindex="-1" type="text" class="text-disabled" size="3" value="{@median}" name="Median_{@id}" readonly="readonly" title="This is the calculated median value for the scores in this column.">
      </input>
   </xsl:template>



<!-- ######################################################################## -->
   <xsl:template name="submitRow">


      <xsl:param name="GRADEBOOK" />

      <xsl:variable name="GRADE_COL_COUNT">
         <xsl:choose>
            <xsl:when test="$showGradeColumn = 'Y'">1</xsl:when>

            <xsl:otherwise>0</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <tr>
         <td colspan="{count($GRADEBOOK/gradebook-item) + $GRADE_COL_COUNT + 1}" class="table-nav">


            <input type="submit" class="uportal-button" value="Update" 
            	title="To store these changes and return to the main view of the gradebook" onclick="document.gradebookForm.command.value = 'update_gb'"/>

            <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" 
            	title="To cancel these changes and return to the main view of the gradebook" />
         </td>

         <td class="gradebook-empty-right-bottom" width="100%">
            <img height="1" width="1" src="{$SPACER}" alt="" title="" border="0" />
         </td>
      </tr>
   </xsl:template>




<!-- ######################################################################## -->
   <xsl:template name="columnSelectFooter">
      <xsl:param name="GRADEBOOK" />
   </xsl:template>

<!-- ######################################################################## -->
   <xsl:template name="gradeBookDataField">
      <xsl:param name="CURR_POS" />
      <input class="text" type="text" title="Enter score here." size="3" maxlength="3" onchange="recordOnChange(this);" name="SCORE_{./gradebook-score[$CURR_POS]/user/@username}_{./gradebook-score[$CURR_POS]/@gradebook-itemid}">
	 		<xsl:attribute  name = "value" >
	            <xsl:choose>
			  	   <!-- If the score is not defined, then show as blank -->
	               <xsl:when test="./gradebook-score[$CURR_POS]/@score = -1"></xsl:when>
	
	               <xsl:otherwise><xsl:value-of select="./gradebook-score[$CURR_POS]/@score" /></xsl:otherwise>
	            </xsl:choose>
	 		</xsl:attribute>
      </input>
   </xsl:template>

<!-- ######################################################################## -->
<!--<xsl:template name="totalScoreDataField">
    <xsl:param name="TOTAL_SCORE_VALUE" />
    <xsl:param name="CURR_POS" />
    <input type="text" class="text-disabled" size="3" value="{$TOTAL_SCORE_VALUE}" name="Grade_{./gradebook-score[$CURR_POS]/user/@username}" readonly="readonly">
    </input>
   </xsl:template> -->
<!-- ######################################################################## -->
</xsl:stylesheet>



