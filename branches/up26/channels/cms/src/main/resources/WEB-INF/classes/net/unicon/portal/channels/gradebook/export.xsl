<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:import href="common_main.xsl" />

<xsl:output method="html" indent="yes" />

<xsl:param name="subLinkParameter"></xsl:param>
<xsl:param name="showGradeColumn">Y</xsl:param>
<xsl:param name="ONSUBMIT"></xsl:param>


<!-- ######################################################################## -->

<xsl:template match="/">

    <script LANGUAGE="JavaScript" src="javascript/GradebookChannel/main_export.js"></script>

    <script LANGUAGE="JavaScript">
    	gradeBookCols = <xsl:value-of select="count(./gradebooks/gradebook-item)+2" />;
		// create initialize method so that it can be initialized outside of page load
    	initializeGradebookChannel = function()
    	{
        	gradeBookCols = <xsl:value-of select="count(./gradebooks/gradebook-item)+2" />;
        	GBExportOnloadHandler();        	
        }
    </script>
    
    <!-- Apply Common Main templates -->
    <xsl:apply-imports />
   
</xsl:template>
   
<!-- ######################################################################## -->


<xsl:template name="emptyHeaderField">
    <input type="hidden" name="headerCol0" value="" />
</xsl:template>


<!-- ######################################################################## -->

<xsl:template name="entryHeaderField">
    <input type="hidden" name="{concat('headerCol',@id)}" value="{title}" />
</xsl:template>

<!-- ######################################################################## -->

<xsl:template name="gradeHeaderField">
    <input type="hidden" name="headerGradeCol" value="Grade" />
</xsl:template>

<!-- ######################################################################## -->

<xsl:template name="meanHeaderField">
    <input type="hidden" name="meanCol0" value="Mean" />
</xsl:template>


<!-- ######################################################################## -->

<xsl:template name="meanDataField">
    <img height="1" width="1" src="{$SPACER}" alt="" border="0" />
		<xsl:choose>
			<xsl:when test="not((@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
		      <xsl:value-of select="@mean" />
		      <input type="hidden" name="{concat('meanCol',@id)}" value="{@mean}" />
			</xsl:when>
		  
			<xsl:otherwise>
				0
				<input type="hidden" name="{concat('meanCol',@id)}" value="0" />
			</xsl:otherwise>
		</xsl:choose>
</xsl:template>

<!-- ######################################################################## -->

   <xsl:template name="meanGradeField">
    <input type="hidden" name="meanGradeCol" value="" />
   </xsl:template>


<!-- ######################################################################## -->

<xsl:template name="medianHeaderField">
    <input type="hidden" name="medianCol0" value="Median" />
</xsl:template>


<!-- ######################################################################## -->

<xsl:template name="medianDataField">
    <img height="1" width="3" src="{$SPACER}" alt="" border="0" />
		<xsl:choose>
			<xsl:when test="not((@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
				<xsl:value-of select="@median" />
				<input type="hidden" name="{concat('medianCol',@id)}" value="{@median}" />
			</xsl:when>
		  
			<xsl:otherwise>
				0
				<input type="hidden" name="{concat('medianCol',@id)}" value="0" />
			</xsl:otherwise>
		</xsl:choose>
</xsl:template>

<!-- ######################################################################## -->

   <xsl:template name="medianGradeField">
    <input type="hidden" name="medianGradeCol" value="" />
   </xsl:template>

<!-- ######################################################################## -->

<xsl:template name="learnerNameField">
    <xsl:param name="CURR_POS" />
    <input type="hidden" name="{concat('user',$CURR_POS,'Col0')}" value="{concat(./gradebook-score[$CURR_POS]/user/last_name,', ',./gradebook-score[$CURR_POS]/user/first_name)}" />
</xsl:template>

<!-- ######################################################################## -->

<xsl:template name="gradeBookDataField">
<xsl:param name="CURR_POS" />

    <xsl:choose>
        <!-- If permitted to access details make link -->
        <xsl:when test="$accessDetails = 'Y'">
          <a href="{$baseActionURL}?command=details&amp;gradebook_itemID={@id}&amp;username={./gradebook-score[$CURR_POS]/@username}" 
               onclick="document.gradebookForm.command.value = 'details';" title="To submit assignments and feedback, and view details">
          <xsl:choose>
             <xsl:when test="(./gradebook-score[$CURR_POS]/@score = -1) or ((@feedback = 'no') and ($accessHiddenFeedback = 'N'))">--<input 
             type="hidden" name="{concat('user',$CURR_POS,'Col',@id)}" value="" /></xsl:when>

    		 <xsl:when test = "./gradebook-score[$CURR_POS]/@modifier">
    		 	<xsl:value-of select="(./gradebook-score[$CURR_POS]/@score) + (./gradebook-score[$CURR_POS]/@modifier)" /><input 
    		 	type="hidden" name="{concat('user',$CURR_POS,'Col',@id)}" value="{(./gradebook-score[$CURR_POS]/@score) + (./gradebook-score[$CURR_POS]/@modifier)}" />
    		 </xsl:when>
                 
             <xsl:otherwise>
                <xsl:value-of select="./gradebook-score[$CURR_POS]/@score" /><input 
                type="hidden" name="{concat('user',$CURR_POS,'Col',@id)}" value="{./gradebook-score[$CURR_POS]/@score}" />
             </xsl:otherwise>
          </xsl:choose>
          </a>
        </xsl:when>
          <!-- If not permitted to access details -->
        <xsl:otherwise>
          <xsl:choose>
             <xsl:when test="./gradebook-score[$CURR_POS]/@score = -1">--</xsl:when>
    
             <xsl:otherwise>
                <xsl:value-of select="./gradebook-score[$CURR_POS]/@score" />
             </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
    </xsl:choose>


    <!--<xsl:choose>
        <xsl:when test="./gradebook-score[$CURR_POS]/@score = -1">
            <input type="hidden" name="{concat('user',$CURR_POS,'Col',@id)}" value="" />
        </xsl:when>
        
        <xsl:otherwise>
            <input type="hidden" name="{concat('user',$CURR_POS,'Col',@id)}" value="{./gradebook-score[$CURR_POS]/@score}" />
        </xsl:otherwise>
    </xsl:choose> -->

</xsl:template>

<!-- ######################################################################## -->
   <xsl:template name="totalScoreDataField">
    <xsl:param name="TOTAL_SCORE_VALUE" />
    <xsl:param name="CURR_POS" />
    <xsl:value-of select="$TOTAL_SCORE_VALUE" />
    <input type="hidden" value="{$TOTAL_SCORE_VALUE}" name="GradeCol_{./gradebook-score[$CURR_POS]/user/@username}">
    </input>
   </xsl:template>

<!-- ######################################################################## -->


</xsl:stylesheet>

