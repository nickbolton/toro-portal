<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>
	<xsl:include href="common.xsl"/>
	<xsl:variable name="ACTIVATION" select="/gradebooks/gradebook-item/activation"/>
	<xsl:variable name="ATTRIBUTE" select="$ACTIVATION/attributes/attribute"/>
	<xsl:template match="/">
	
	<!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>  
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter> 
        <parameter name="workerActionURL"><xsl:value-of select="$workerActionURL" /></parameter>
        <parameter name="activation_id"><xsl:value-of select="$activation_id" /></parameter>
    </textarea> -->
		<!-- gradebookItemID not being passed as parameter yet.  Set gbID to get value from 1st and only gradebook-item -->
		<xsl:variable name="gbID" select="/gradebooks/gradebook-item[1]/@id"/>
		<xsl:call-template name="links"/>

		<xsl:choose>
			<xsl:when test="count($ACTIVATION) &gt; 0">
				<form method="post" name="gradebookForm">
					<!-- UniAcc: Data Table -->
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<tr>
							<th colspan="2" class="th" id="ViewActivation">
								<xsl:choose>
									<xsl:when test="$ACTIVATION/@type = 'ASSESSMENT'">
				                    	View Assessment Activation
				                    </xsl:when>
									<xsl:otherwise>
				                    	View Assignment Activation
				                    </xsl:otherwise>
								</xsl:choose>
							</th>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" headers="ViewActivation" id="AssessAssoc">
								Assessment Association
							</td>
							<td class="table-content-right" headers="ViewActivation AssessAssoc">
								<xsl:choose>
									<xsl:when test="$ATTRIBUTE[@name = 'assessment']/value != ''">
										<xsl:value-of select="$ATTRIBUTE[@name = 'assessment']/value"/>
									</xsl:when>
									<xsl:otherwise>
			                             None (or N/A)
			                        </xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right" headers="ViewActivation" id="colName">
								Column Name
							</td>
							<td class="table-content-right" headers="ViewActivation colName">
								<xsl:choose>
									<xsl:when test="$editItem = 'Y'">
										<a href="{$baseActionURL}?command=edit&amp;gradebook_itemID={$gbID}" 
											title="To edit the information for this column" 
											onmouseover="swapImage('gbEditThisColumnImage','channel_edit_active.gif')" 
											onmouseout="swapImage('gbEditThisColumnImage','channel_edit_base.gif')">
											<xsl:value-of select="/gradebooks/gradebook-item[@id = $gbID]/title"/>
											<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
											<img height="16" width="16" border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" 
												alt="'Edit' icon: edit the information for this column" 
												title="'Edit' icon: edit the information for this column" 
												align="absmiddle" name="gbEditThisColumnImage" id="gbEditThisColumnImage"/>
										</a>
									</xsl:when>
									<xsl:otherwise>
				                    	<xsl:value-of select="/gradebooks/gradebook-item[@id = $gbID]/title"/>
				                    </xsl:otherwise>
								</xsl:choose>
								
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right">
								Activation Link
							</td>
							<td class="table-content-right">
								<xsl:choose>
				                	<!-- ACTIVATION IS STILL IN THE FUTURE -->
				                	<xsl:when test = "$ACTIVATION/@time_status='future'">
				                		Not available yet
				                	</xsl:when>
				                	<!-- DIRECT TO PORTAL ASSESSMENT CHANNEL UNTIL ABLE TO FOCUS IT FROM GRADEBOOK -->
				                	<xsl:when test = "@type = 'ASSESSMENT' and reference-link/url='default'">
				                		Go to the <span class="uportal-channel-emphasis">Portal Assessment Channel</span>
				                	</xsl:when>
				                	<!-- CAN BE IMPLEMENTED WHEN THE GRADEBOOK IS ABLE TO FOCUS THE PORTAL ASSESSMENT CHANNEL -->
				                    <xsl:when test="@type = 'ASSESSMENT' and reference-link">
				                        <a title="To launch this assessment">
				                        	<!-- 
				                        			Build HREF depending on reference type:
				                        			1) 'default' link will be replaced by baseActionURL with parameters appended
				                        			2) otherwise it will use the link provided 
				                        	-->
				                        	<xsl:attribute  name = "href" >
				                        		<xsl:choose>
				                        			<!-- If 'default' build URL -->
				                        			<xsl:when test="reference-link/url = 'default'">
				                        				<!-- Use baseActionURL  -->
				                        				<xsl:value-of select="$baseActionURL" />
				                        				<xsl:choose>
				                        					<!-- If '?' in baseActionURL and parameters in XML then add &amp; for additional parameters -->
				                                        	<xsl:when test="contains($baseActionURL,'?') and count(reference-link/parameters/parameter) &gt; 0">&amp;</xsl:when>
				                                        	<!-- Otherwise, if parameters in XML then add '?' -->
				                                        	<xsl:when test="count(reference-link/parameters/parameter) &gt; 0">?</xsl:when>
				                                        	<!-- Otherwise add nothing -->
				                                        	<xsl:otherwise></xsl:otherwise>
				                                        </xsl:choose>
				                                        <!-- Loop thru all parameters -->
				                                        <xsl:for-each select = "reference-link/parameters/parameter">
				                                        	<!-- If last parameter, don't add &amp; at the end -->
				                                        	<xsl:choose>
				                                        		<xsl:when test="position()=last()"><xsl:value-of select="@name" />=<xsl:value-of select="value" /></xsl:when>
				                                        		<xsl:otherwise><xsl:value-of select="@name" />=<xsl:value-of select="value" />&amp;</xsl:otherwise>
				                                        	</xsl:choose>
				                                        </xsl:for-each>
				                                    </xsl:when>
				                        		  	<!-- If not 'default' then use URL provided -->
				                        			<xsl:otherwise><xsl:value-of select="reference-link/url" /></xsl:otherwise>
				                        		</xsl:choose>
				                        	</xsl:attribute>
				                        	<!-- Add target as specified in the XML: new=_blank, self=no target needed, otherwise use name specified -->
				                    		<xsl:choose>
				                    			<xsl:when test="reference-link/@target = 'new'">
						                        	<xsl:attribute  name = "target" >_blank</xsl:attribute>
				                    			</xsl:when>
				                    			<xsl:when test="reference-link/@target = 'self'"></xsl:when>
				                    			<xsl:otherwise>
						                        	<xsl:attribute  name = "target" ><xsl:value-of select="reference-link/@target" /></xsl:attribute>
				                    			</xsl:otherwise>
				                    		</xsl:choose>
				                        	To Assessment</a>
				                    </xsl:when>
				                    <!-- Old implementation still works -->
									<xsl:when test="@type = 'ASSESSMENT'">
										<a href="{$ATTRIBUTE[@name = 'link_uri']/value}" target="_blank" title="To launch this assessment">To Assessment</a>
									</xsl:when>
									<xsl:when test="$ATTRIBUTE[@name = 'link_type']/value = 'file'">
										<a href="{$workerActionURL}&amp;fileType=activation&amp;activationId={$ATTRIBUTE/../../@id}" target="_blank" title="To access/download this file">To Assignment File</a>
									</xsl:when>
									<xsl:when test="$ATTRIBUTE[@name = 'link_type']/value = 'url'">
										<a href="{$ATTRIBUTE[@name = 'link_uri']/value}" target="_blank" title="To launch this activation url">To Assignment URL</a>
									</xsl:when>
									<xsl:otherwise>&#160;</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
						<xsl:if test="$ACTIVATION/@type = 'ASSESSMENT' and $ATTRIBUTE[@name = 'form_name']/value">
							<tr>
								<td class="table-light-left" style="text-align:right">
									Form Selection
								</td>
								<td class="table-content-right">
									<xsl:value-of select="$ATTRIBUTE[@name = 'form_name']/value"/>
								</td>
							</tr>
						</xsl:if>
						<tr>
							<td class="table-light-left" style="text-align:right">
								Activation Period
							</td>
							<td class="table-content-right">
								<ul>
									<li>Start Time: <xsl:value-of select="$ACTIVATION/start_time"/>
										<br/>
                Start Date: <xsl:value-of select="$ACTIVATION/start_date"/>
										<br/>
										<br/>
									</li>
									<li>End Time:&#xA0; <xsl:value-of select="$ACTIVATION/end_time"/>
										<br/>
                End Date:&#xA0; <xsl:value-of select="$ACTIVATION/end_date"/>
									</li>
								</ul>
							</td>
						</tr>
						<tr>
							<td class="table-light-left" style="text-align:right">
								Activation For
							</td>
							<td class="table-content-right">
								<ul>
									<xsl:choose>
										<xsl:when test="$ACTIVATION/user-list/@allusers ='true'">
											<li>All Users</li>
										</xsl:when>
										<xsl:otherwise>
											<xsl:apply-templates select="$ACTIVATION/user-list/user"/>
										</xsl:otherwise>
									</xsl:choose>
								</ul>
							</td>
						</tr>
						<xsl:if test="$ACTIVATION/@type = 'ASSESSMENT'">
							<tr>
								<td class="table-light-left" style="text-align:right">
									Activation Comment
								</td>
								<td class="table-content-right">
									<xsl:value-of select="$ATTRIBUTE[@name = 'comment']/value"/>
								</td>
							</tr>

							<xsl:if test = "$ATTRIBUTE[@name = 'duration']/value">
								<tr>
									<td class="table-light-left" style="text-align:right">
										Duration to
										<br/>
										Complete (hrs)
									</td>
									<td class="table-content-right">
										<xsl:value-of select="$ATTRIBUTE[@name = 'duration']/value"/>
									</td>
								</tr>
							</xsl:if>
						</xsl:if>
						<tr>
							<td class="table-light-left" style="text-align:right">
								Attempts Permitted
							</td>
							<td class="table-content-right">
								<xsl:value-of select="$ATTRIBUTE[@name = 'attempts']/value"/>
							</td>
						</tr>
						<tr>
							<td colspan="2" class="table-nav" style="text-align:center">
											<input class="uportal-button" type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=all_activations'" title="To return to the view of all current activations."/>
							</td>
						</tr>
						<xsl:choose>
							<!-- Include dependent nav if user has permissions to use at least of the dependent nav options and there is at least 1 column to select -->
							<xsl:when test="not($editItemScores = 'N' and $editItem = 'N' and $deleteItem = 'N') and (count(/gradebooks/gradebook-item) &gt; 0)">
								<!--             <xsl:call-template name="sublinks">
                    <xsl:with-param name="commandDefault">view_activation</xsl:with-param>
                 </xsl:call-template>-->
				 </xsl:when>
							<!-- Otherwise need to include some expected hidden fields that the dependent links usually added -->
							<xsl:otherwise>
								<tr>
									<td colspan="100" class="med">
										<input type="hidden" name="command" value="all_activations"/>
										<input type="hidden" name="gradebook_itemID" value="{$gbID}"/>
										<img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
									</td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
					</table>
				</form>
			</xsl:when>
			<xsl:otherwise>
     			There are currently no assessment or assignment activations for this offering.<br/>
				<br/>
				Click <a href="{$baseActionURL}" title="To return to the main view of the gradebook.">here</a> to return to the main view of the gradebook.
		    </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="user">
		<li>
			<xsl:value-of select="last_name"/>, <xsl:value-of select="first_name"/>
		</li>
	</xsl:template>
</xsl:stylesheet>
