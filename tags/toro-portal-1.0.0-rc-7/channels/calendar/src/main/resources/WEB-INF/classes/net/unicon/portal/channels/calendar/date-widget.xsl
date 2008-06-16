<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" />
	
	<!-- *******************************
		 Change this variable to adjust  
		 years on drop-down boxes 
		 ******************************* -->
	<xsl:variable name="start">05</xsl:variable>
	<!-- ******** END CHANGE *********** -->
	
	<xsl:template name="year-options">
		<xsl:param name="selected-date">05</xsl:param>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start)-1)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)-1)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start)-1">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)-1)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)-1)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start))&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start))" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start))&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start))" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start)+1)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+1)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start)+1">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+1)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+1)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start)+2)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+2)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+2)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+2)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+2)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start)+3)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+3)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+3)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+3)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+3)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start)+4)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+4)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+4)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+4)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+4)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:if test="(number($start)+5)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+5)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+5)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+5)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+5)" />
		</option>		
    </xsl:template>
	
	<xsl:template name="full-year-options">
		<xsl:param name="selected-date">05</xsl:param>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start)-1)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)-1)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start)-1">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)-1)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)-1)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start))&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start))" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start))&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start))" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start)+1)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+1)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start)+1">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+1)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+1)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start)+2)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+2)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+2)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+2)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+2)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start)+3)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+3)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+3)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+3)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+3)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start)+4)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+4)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+4)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+4)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+4)" />
		</option>
		<option>
			<xsl:attribute name="value">
				<xsl:text>20</xsl:text>
				<xsl:if test="(number($start)+5)&lt;=9">0</xsl:if>
				<xsl:value-of select="(number($start)+5)" />
			</xsl:attribute>
			<xsl:if test="number($selected-date) = number($start+5)">
				<xsl:attribute name="selected">selected</xsl:attribute>
			</xsl:if>
			<xsl:if test="(number($start)+5)&lt;=9">0</xsl:if>
			<xsl:value-of select="(number($start)+5)" />
		</option>		
    </xsl:template>
    
</xsl:stylesheet>	
   
   