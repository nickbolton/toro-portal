<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes"/>
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<xsl:template match="/">
		<xsl:call-template name="links"/>
		<xsl:call-template name="deleteElement"/>
		<!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
    </textarea>
     -->
	</xsl:template>
	<xsl:template name="deleteElement">
		<xsl:choose>
			<xsl:when test="$nodetype='url-element'">
				<!--############ Delete URL ############ -->
				<form name="ResourcesForm" action="{$baseActionURL}" method="post">
					<input type="hidden" name="command" value="s-delete"/>
					<input type="hidden" name="oid" value="{$nodeid}"/>
					<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
					<!-- UniAcc: Layout Table -->
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<tr>
							<th colspan="2" class="th-top-single">
                        		Delete URL
                    		</th>
						</tr>
						<tr>
							<td class="table-content-single" style="text-align:center">
								<span class="uportal-channel-warning">Are you sure you want to delete the URL, 
                        			<span class="uportal-channel-strong">
										&#034;<xsl:value-of select="$nodename"/>&#034;
									</span>
									, from Resources?
								</span>
							</td>
						</tr>
						<tr>
							<td class="table-content-single" style="text-align:center;">
								<input type="radio" class="radio" name="commandButton" value="confirm" id="rrcdr1"/>
								<label for="rrcdr1">&#160;Yes</label>
                        		<img height="1" width="15" src="{$SPACER}" alt="" title=""/>
								<input type="radio" checked="checked" class="radio" name="commandButton" value="no" id="rrcdr2"/>
								<label for="rrcdr2">&#160;No</label>
								<br/>
							</td>
						</tr>
						<tr>
							<td class="table-content-single-bottom" style="text-align:center">
		                       <input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to the main view of the resources"/>
		                       <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the resources without deleting this item"/>
							</td>
						</tr>
					</table>
				</form>
			</xsl:when>
			<xsl:when test="$nodetype='file'">
				<!--############ Delete File ############ -->
				<form name="ResourcesForm" action="{$baseActionURL}" method="post">
					<input type="hidden" name="command" value="s-delete"/>
					<input type="hidden" name="oid" value="{$nodeid}"/>
					<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
					<!-- UniAcc: Layout Table -->
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<tr>
							<th colspan="2" class="th-top-single">
		                        Delete File
		                    </th>
						</tr>
						<tr>
							<td class="table-content-single" style="text-align:center">
								<span class="uportal-channel-warning">
									Are you sure you want to delete the File, 
                        			<span class="uportal-channel-strong">
										&#034;<xsl:value-of select="$nodename"/>&#034;
									</span>
									, from Resources?
								</span>
							</td>
						</tr>
						<tr>
							<td class="table-content-single" style="text-align:center;">
								<input type="radio" class="radio" name="commandButton" value="confirm" id="rrcdr1"/>
								<label for="rrcdr1">&#160;Yes</label>
                        		<img height="1" width="15" src="{$SPACER}" alt="" title=""/>
								<input type="radio" checked="checked" class="radio" name="commandButton" value="no" id="rrcdr2"/>
								<label for="rrcdr2">&#160;No</label>
								<br/>
							</td>
						</tr>
						<tr>
							<td class="table-content-single-bottom" style="text-align:center">
		                       <input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to the main view of the resources"/>
		                       <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the resources without deleting this item"/>
							</td>
						</tr>
					</table>
				</form>
			</xsl:when>
			<xsl:otherwise>
				<!--############ Delete Folder ############ -->
				<form name="ResourcesForm" action="{$baseActionURL}" method="post">
					<input type="hidden" name="command" value="s-delete"/>
					<input type="hidden" name="oid" value="{$nodeid}"/>
					<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
					<!-- UniAcc: Layout Table -->
					<table cellpadding="0" cellspacing="0" border="0" width="100%">
						<tr>
							<th colspan="2" class="th-top-single">
		                        Delete Folder
		                    </th>
						</tr>
						<tr>
							<td class="table-content-single" style="text-align:center">
								<span class="uportal-channel-strong">
									Deleting a folder will delete all the contents of that folder.<br/>
								</span>
								<span class="uportal-channel-warning">
									Are you sure you want to delete the file, 
                        			<span class="uportal-channel-strong">
										&#034;<xsl:value-of select="$nodename"/>&#034;
									</span>
									, from Resources?
								</span>
							</td>
						</tr>
						<tr>
							<td class="table-content-single" style="text-align:center;">
								<input type="radio" class="radio" name="commandButton" value="confirm" id="rrcdr1"/>
								<label for="rrcdr1">&#160;Yes</label>
                        		<img height="1" width="15" src="{$SPACER}" alt="" title=""/>
								<input type="radio" checked="checked" class="radio" name="commandButton" value="no" id="rrcdr2"/>
								<label for="rrcdr2">&#160;No</label>
								<br/>
							</td>
						</tr>
						<tr>
							<td class="table-content-single-bottom" style="text-align:center">
								<input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to the main view of the resources"/>					&#032;&#032;&#032;&#032;
								<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the resources without deleting this item"/>
							</td>
						</tr>
					</table>
				</form>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
