<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Choice-Decision
The purpose of this set of templates is to provide agnostic translation of a decision process. A sample xml structure follows:

<choice-collection handle="example">
    <label>Penelope Examples</label>
    <choice handle="addition" min-selections="1" max-selections="1">
        <label>What is 2 + 2?</label>
        <option handle="3" complement-type="net.unicon.penelope.complement.TypeNone">
            <label>3</label>
        </option>
        <option handle="4" complement-type="net.unicon.penelope.complement.TypeNone">
            <label>4</label>
        </option>
        <option handle="5" complement-type="net.unicon.penelope.complement.TypeNone">
            <label>5</label>
        </option>
    </choice>
    <choice handle="name" min-selections="1" max-selections="1">
        <label>What is your name?</label>
        <option handle="answer" complement-type="net.unicon.penelope.complement.TypeText64"></option>
    </choice>
</choice-collection>
<decision-collection choice-collection="example">
    <decision choice="addition">
        <selection option="4">null</selection>
    </decision>
    <decision choice="name">
        <selection option="answer">Andrew</selection>
    </decision>
</decision-collection>


The parent template matches the choice-collection node of the XML. Since it does a match, it will be the starting point for future references in the node structure, essentailly all relative. This was done purposefully as this template will be called into multiple situations where it will not know an absolute path to its starting place in the node structure.

In order to detect and match corresponding decision-collections, variables holding unique identifiers (@handle) are set in each of the templates and passed down through child templates via xsl:with-param. 
-->

<xsl:template match="choice-collection"><!--Parent template -->

	<xsl:param name="INPUT_TYPE">default</xsl:param><!--Receive input type param, if not being passed, default value is set -->
	<xsl:param name="WRAPPER">default</xsl:param><!--This param can be set when the choice-collection template is applied. Gives an option to what tags will wrap around the inputs -->
	<xsl:variable name="DECISION_COLLECTION" select="@handle" /><!--Set decision-collection identifier to be passed into child templates -->

	<!--print choice-collection label
	<h2>
		<xsl:attribute name="name">
			<xsl:value-of select="@handle" />
		</xsl:attribute>
		<xsl:value-of select="label" />
	</h2> -->
	
	<xsl:apply-templates select="choice"><!--match choice nodes -->
		<!--Pass identifiers into child template -->
		<xsl:with-param name="INPUT_TYPE">
		    <xsl:value-of select="$INPUT_TYPE" />
		</xsl:with-param>
		<xsl:with-param name="WRAPPER">
		    <xsl:value-of select="$WRAPPER" />
		</xsl:with-param>
		<xsl:with-param name="DECISION_COLLECTION">
			<xsl:value-of select="$DECISION_COLLECTION" />
		</xsl:with-param>
	</xsl:apply-templates>
</xsl:template>

<xsl:template match="choice">

	<xsl:param name="INPUT_TYPE" /><!--Receive input type param -->
	<xsl:param name="WRAPPER" /><!--Receive wrapper -->
	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:variable name="DECISION" select="@handle" /><!--Set decision identifier to be passed into child templates -->
	
	<xsl:choose>
		<xsl:when test="$INPUT_TYPE='selection'">
		    <!-- Test $WRAPPER to determine the tag that will wrap the inputs -->
		    <xsl:choose>
		        <xsl:when test="$WRAPPER='td'">
		            <td>
               	        <xsl:call-template name="print-choiceLabel" />
                    	<xsl:call-template name="print-choiceSelect">
                    		<xsl:with-param name="DECISION_COLLECTION">
                    			<xsl:value-of select="$DECISION_COLLECTION" />
                    		</xsl:with-param>
                    	</xsl:call-template>
                	</td>
		        </xsl:when>
		        <xsl:otherwise>
                	<div>
                		<xsl:attribute name="name">
                			<xsl:value-of select="@handle" />
                		</xsl:attribute>

                    	<xsl:call-template name="print-choiceLabel" /><br />
                    	<xsl:call-template name="print-choiceSelect">
                    		<xsl:with-param name="DECISION_COLLECTION">
                    			<xsl:value-of select="$DECISION_COLLECTION" />
                    		</xsl:with-param>
                    	</xsl:call-template>
                	</div>
                </xsl:otherwise>
        	</xsl:choose>
		</xsl:when>
		<xsl:otherwise>
            <div><!--generate a containing div for the choice and correpsonding options -->
        		<xsl:attribute name="name">
        			<xsl:value-of select="@handle" />
        		</xsl:attribute>
        		
        	    <xsl:call-template name= "print-choiceLabel" /><br />
        	    <xsl:call-template name="print-choice">
        		<xsl:with-param name="DECISION_COLLECTION">
        			<xsl:value-of select="$DECISION_COLLECTION" />
        		</xsl:with-param>
        	</xsl:call-template>
        	</div>
		</xsl:otherwise>
	</xsl:choose>

</xsl:template>

<xsl:template match="option">

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	

	<!--Determine the type of choice/input. Determination is made by a logical test on the @min/max-selections and/or the @complement-type. Default type is radio button. -->
	<xsl:choose>
	
		<!-- Checkbox -->
		<xsl:when test="../@max-selections > 1 or ../@max-selections = 00 and @complement-type = 'net.unicon.penelope.complement.TypeNone'">
			<xsl:call-template name="checkbox"><!--Use a template (below) to generate the checkbox input -->
			<!--Pass identifiers into child template -->
		
				<xsl:with-param name="DECISION_COLLECTION">
					<xsl:value-of select="$DECISION_COLLECTION" />
				</xsl:with-param>
		
				<xsl:with-param name="DECISION">
					<xsl:value-of select="$DECISION" />
				</xsl:with-param>
			</xsl:call-template>
		
		</xsl:when>
		
		<!--text field -->
		<!--Note: probably need to be able to account for multiple text fields (or text field/select combo) that account for a single answer (like a date for instance). Support for such combinations likely would be done by testing the complement-type. This is currently not supported in this solution. -->
		<xsl:when test="count(../option) = 1">
		
			<xsl:call-template name="text"><!--Use a template (below) to generate the text input -->
			<!--Pass identifiers into child template -->
		
				<xsl:with-param name="DECISION_COLLECTION">
					<xsl:value-of select="$DECISION_COLLECTION" />
				</xsl:with-param>
		
				<xsl:with-param name="DECISION">
					<xsl:value-of select="$DECISION" />
				</xsl:with-param>
			</xsl:call-template>
		
		</xsl:when>
		
		<!--textarea -->
		<xsl:when test="count(../option) = 1 and @complement-type = 'net.unicon.penelope.complement.Textarea'">
		
			<xsl:call-template name="textarea"><!--Use a template (below) to generate the textarea input -->
			<!--Pass identifiers into child template -->
		
				<xsl:with-param name="DECISION_COLLECTION">
					<xsl:value-of select="$DECISION_COLLECTION" />
				</xsl:with-param>
		
				<xsl:with-param name="DECISION">
					<xsl:value-of select="$DECISION" />
				</xsl:with-param>
			</xsl:call-template>
		
		</xsl:when>
	  	
	  	<!--radio button -->
		<xsl:otherwise>
		
			<xsl:call-template name="radio"><!--Use a template (below) to generate the radio button input -->
			<!--Pass identifiers into child template -->
		
				<xsl:with-param name="DECISION_COLLECTION">
					<xsl:value-of select="$DECISION_COLLECTION" />
				</xsl:with-param>
		
				<xsl:with-param name="DECISION">
					<xsl:value-of select="$DECISION" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
	
</xsl:template>

<xsl:template match="option" mode="selection">

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	
			<xsl:choose>
				<!--Multiple selection -->
				<xsl:when test="../@max-selections > 1 or ../@max-selections = 00">
					<xsl:call-template name="select"><!--Use a template (below) to generate the select options -->
						<!--Pass identifiers into child template -->
						
						<xsl:with-param name="MULTIPLE"><!--Param to set selection to multiple -->
							<xsl:value-of select="true" />
						</xsl:with-param>
		
						<xsl:with-param name="DECISION_COLLECTION">
							<xsl:value-of select="$DECISION_COLLECTION" />
						</xsl:with-param>
		
						<xsl:with-param name="DECISION">
							<xsl:value-of select="$DECISION" />
						</xsl:with-param>
					</xsl:call-template>	
				</xsl:when>
				
			  	<!--Single selection -->
				<xsl:otherwise>
				 	<xsl:call-template name="select"><!--Use a template (below) to generate the select options -->
						<!--Pass identifiers into child template -->
						
						<xsl:with-param name="MULTIPLE"><!--Param to set selection to single -->
							<xsl:value-of select="false" />
						</xsl:with-param>
		
						<xsl:with-param name="DECISION_COLLECTION">
							<xsl:value-of select="$DECISION_COLLECTION" />
						</xsl:with-param>
		
						<xsl:with-param name="DECISION">
							<xsl:value-of select="$DECISION" />
						</xsl:with-param>
						
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>

</xsl:template>

<xsl:template name="checkbox">

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	
	<input type="checkbox">
		
		<!--give the input a name based on the choice collection, choice, and option handles -->
		<xsl:attribute name="name">
			<xsl:value-of select="concat(../../@handle,../@handle,@handle)" />
		</xsl:attribute>
		
		<!--test to see if the checkbox is checked -->
		<xsl:if test = "../../../decision-collection[@choice-collection = $DECISION_COLLECTION]/decision[@choice = $DECISION]/selection/@option = ./@handle">
			<xsl:attribute name="checked">
				<xsl:value-of select="checked" />
			</xsl:attribute>
		</xsl:if>
		
		<xsl:value-of select="label" /><!--print the input label -->
	</input><br/>
</xsl:template>


<xsl:template name="select">

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	<xsl:param name="MULTIPLE" /><!--Receive decision identifier -->
	<option>
		<xsl:attribute name="value">
			<xsl:value-of select="@handle" />
		</xsl:attribute>
		<xsl:value-of select="label" />
	</option>
	
</xsl:template>


<xsl:template name="text">

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	
	<input type="text">
		
		<!--give the input a name based on the choice collection, choice, and option handles -->
		<xsl:attribute name="name">
			<xsl:value-of select="concat(../../@handle,../@handle,@handle)" />
		</xsl:attribute>
		
		<!--test to see if the text input has a value -->
		<xsl:if test = "../../../decision-collection[@choice-collection = $DECISION_COLLECTION]/decision[@choice = $DECISION]/selection/@option = ./@handle">
			<xsl:attribute name="value">
				<xsl:value-of select="../../../decision-collection[@choice-collection = $DECISION_COLLECTION]/decision[@choice = $DECISION]/selection" />
			</xsl:attribute>
		</xsl:if>
		
		<xsl:value-of select="label" /><!--print the input label -->
	</input><br/>
</xsl:template>


<xsl:template name="textarea">
<!--Note: may need to be able to set the "cols" and "rows" attributes of the textarea tag. This might be accomplished by utilizing the complement-type attribute of the XML. -->

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	
	<textarea>
		
		<!--give the input a name based on the choice collection, choice, and option handles -->
		<xsl:attribute name="name">
			<xsl:value-of select="concat(../../@handle,../@handle,@handle)" />
		</xsl:attribute>
		
		<!--test to see if the textarea has a value -->
		<xsl:if test = "../../../decision-collection[@choice-collection = $DECISION_COLLECTION]/decision[@choice = $DECISION]/selection/@option = ./@handle">
			<xsl:attribute name="value">
				<xsl:value-of select="../../../decision-collection[@choice-collection = $DECISION_COLLECTION]/decision[@choice = $DECISION]/selection" />
			</xsl:attribute>
		</xsl:if>
		
		<xsl:value-of select="label" /><!--print the input label -->
	</textarea>
</xsl:template>


<xsl:template name="radio">

	<xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:param name="DECISION" /><!--Receive decision identifier -->
	
	<input type="radio">
		
		<!--give the input a name based on the choice collection, and choice handles - this does not include the option handle as the radio buttons all need to have the same name to know they are included in the same set -->
		<xsl:attribute name="name">
			<xsl:value-of select="concat(../../@handle,../@handle)" />
		</xsl:attribute>
		
		<!--assign input value from the option handle-->
		<xsl:attribute name="value">
			<xsl:value-of select="@handle" />
		</xsl:attribute>
		
		<!--test to see if the radio button is checked -->
		<xsl:if test = "../../../decision-collection[@choice-collection = $DECISION_COLLECTION]/decision[@choice = $DECISION]/selection/@option = ./@handle">
			<xsl:attribute name="checked">
				<xsl:value-of select="checked" />
			</xsl:attribute>
		</xsl:if>
		
		<xsl:value-of select="label" /><!--print the input label -->
	</input><br/>
</xsl:template>


<xsl:template name="print-choiceLabel">
    
    <!--print choice label -->
	<xsl:attribute name="name">
		<xsl:value-of select="@handle" />
	</xsl:attribute>
	<xsl:value-of select="label" />

</xsl:template>


<xsl:template name="print-choiceSelect">
    <xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:variable name="DECISION" select="@handle" /><!--Set decision identifier to be passed into child templates -->
	
    <select>
    	<xsl:attribute name="name">
    		<xsl:value-of select="concat(../@handle,@handle)" />
    	</xsl:attribute>
    	
    	<xsl:apply-templates select="option" mode="selection"><!--match option nodes -->
    		<!--Pass identifiers into child template -->
    		
    		<xsl:with-param name="DECISION_COLLECTION">
    			<xsl:value-of select="$DECISION_COLLECTION" />
    		</xsl:with-param>
    		
    		<xsl:with-param name="DECISION">
    			<xsl:value-of select="$DECISION" />
    		</xsl:with-param>
    	</xsl:apply-templates>
    </select>
</xsl:template>


<xsl:template name="print-choice">
    <xsl:param name="DECISION_COLLECTION" /><!--Receive decision-collection identifier -->
	<xsl:variable name="DECISION" select="@handle" /><!--Set decision identifier to be passed into child templates -->
	
    <xsl:apply-templates select="option"><!--match option nodes -->
    	<!--Pass identifiers into child template -->
    	
    	<xsl:with-param name="DECISION_COLLECTION">
    		<xsl:value-of select="$DECISION_COLLECTION" />
    	</xsl:with-param>
    	
    	<xsl:with-param name="DECISION">
    		<xsl:value-of select="$DECISION" />
    	</xsl:with-param>
    </xsl:apply-templates>

</xsl:template>


<xsl:template match="text()">
<!--
Prevents text in unmatched elements from being written to the output. To implement new elements a new template just needs to be added, instead of restraining all <xsl:apply-templates /> calls to specific supported elements.
-->
</xsl:template>

</xsl:stylesheet>