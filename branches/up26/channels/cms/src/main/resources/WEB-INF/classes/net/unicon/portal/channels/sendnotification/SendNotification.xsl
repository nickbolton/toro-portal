<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="catPageSize" />
<xsl:variable name="imagedir">media/net/unicon/portal/channels/rad/</xsl:variable>
  
	<xsl:template match="display_form">
		<script language="JavaScript" type="text/javascript" src="javascript/SendNotification/autoForm.js"></script>
		
		<form method="post" action="{$baseActionURL}">
		<input type="hidden" name="command" value="sendNotification" />
		<input type="hidden" name="catPageSize" value="{$catPageSize}" />
		<!--UniAcc: Layout Table -->
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
		
			<xsl:apply-templates select="incomplete"/>
		
			<tr>
				<th class="th" colspan="2">Send Notification</th>
			</tr>
			<tr>
				<td class="table-light-left" align="right" valign="top"><label for="rcsn1">Recipients:</label></td>
				<td class="table-content-right">
					<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="uportal-text">
							<input type="checkbox" name="checked" value="o:{@id}:{@offering}" id="rcsn1">
								<!--xsl:if test="@selected">
									<xsl:attribute name="checked">checked</xsl:attribute>
								</xsl:if-->
								<xsl:if test="@expand = 'yes'">
									<xsl:attribute name="disabled">disabled</xsl:attribute>
								</xsl:if>
								<xsl:if test="@expand != 'yes'">
									<xsl:attribute name="checked">checked</xsl:attribute>
								</xsl:if>
							</input>
						</td>
						<td class="uportal-text" width="100%">
							<img border="0" width="5" src="{$imagedir}transparent.gif"/>
							<xsl:if test="@expand = 'yes'">
								<input type="image" border="0" name="action=close" src="{$imagedir}tree_minus_16.gif" title="Close Tree"/>
								<input type="image" border="0" name="action=close" src="{$imagedir}persons_16.gif" title="Close Tree"/>
								<img border="0" width="2" src="{$imagedir}transparent.gif"/>
								<xsl:value-of select="@topic"/> - <xsl:value-of select="@offering"/>
							</xsl:if>
							<xsl:if test="@expand = 'no'">
								<input type="image" border="0" name="action=expand" src="{$imagedir}tree_plus_16.gif" title="Open Tree"/>
								<input type="image" border="0" name="action=expand" src="{$imagedir}persons_16.gif" title="Open Tree"/>
								<img border="0" width="2" src="{$imagedir}transparent.gif"/>
								<xsl:value-of select="@topic"/> - <xsl:value-of select="@offering"/>
							</xsl:if>
						</td>
					</tr>
					<xsl:apply-templates select="members"/>
					</table>
				</td>
			</tr>
			<tr>
				<td class="table-light-left" align="right" valign="top"><label for="rcsn2">Message:</label></td>
				<td class="table-content-right">
					<textarea  name="message" row="25" cols="35" class="uportal-input-text" id="rcsn2">
						<xsl:value-of select="@message"/>
					</textarea> 
				</td>
			</tr>
			<tr>
				<td class="table-light-left" align="right" valign="top"><label for="rcsn3">Send email:</label></td>
				<td class="table-content-right">
					<input type="checkbox" name="email" value="true" id="rcsn3"/>Yes
					<!-- <input type="radio" name="email" value="true" id="rcsn3"/>Yes
					<img border="0" width="10" src="{$imagedir}transparent.gif"/>
					<input type="radio" name="email" value="false" checked="checked" id="rcsn2"/>No -->
				</td>
			</tr>
			<tr>
				<td class="table-nav" colspan="2">
					<input type="submit" name="send" value="Send" class="uportal-button" onclick="return validator.applyFormRules(this.form, new SendNotificationRulesObject())" title="Send this notification" />
					<img border="0" width="4" src="{$imagedir}transparent.gif"/>
					<input type="button" name="cancel" value="Cancel" class="uportal-button" onclick="window.locationhref='{$baseActionURL}?command=page&amp;catPageSize={$catPageSize}'" title="Cancel this notification and return to the Roster main view" />
				</td>
			</tr>
		</table>
		</form>
	</xsl:template>

	<xsl:template match="incomplete">
		<span class="uportal-channel-error">
			** Input fields are incomplete, please select user(s) and enter a message.
		</span>
	</xsl:template>
  
	<xsl:template match="members">
		<xsl:for-each select="member">
		<xsl:sort select="@name"/>
		<tr>
			<td class="uportal-text">
				<input type="checkbox" name="checked" value="m:{@id}:{@name}:{@email}">
					<xsl:if test="@selected = 'yes'">
						<xsl:attribute name="checked">checked</xsl:attribute>
					</xsl:if>
				</input>
			</td>
			<td class="uportal-text">
				<img border="0" width="26" height="1" src="{$imagedir}transparent.gif"/><img src="{$imagedir}person_16.gif" border="0" alt="'User' Icon" title="'User' Icon"/><img border="0" width="2" src="{$imagedir}transparent.gif"/><xsl:value-of select="@name"/>
			</td>
		</tr>
		</xsl:for-each>
		<!-- if unable to retrieve members, display error -->
		<xsl:apply-templates select="error"/>
		<xsl:if test="child::member">
			<tr>
				<td class="uportal-text" align="right" colspan="2">
					<xsl:if test="@total = '1'">
						<img src="{$imagedir}back_12.gif" border="0" alt="Inactive 'Previous' arrow indicating that there are no previous pages of members to display" title="Inactive 'Back' arrow indicating that there are no previous pages of members to display"/>
					</xsl:if>
					
					<xsl:if test="not(@total = '1') and not(@page='1')">
						<input type="image" border="0" name="go=back" src="{$imagedir}back_12.gif" title="Display the previous page of members"/>
					</xsl:if>
					
					<img border="0" width="2" src="{$imagedir}transparent.gif"/>
					Page <xsl:value-of select="@page"/> of <xsl:value-of select="@total"/>
					<img border="0" width="2" src="{$imagedir}transparent.gif"/>
					
					<xsl:if test="@total = '1'">
						<img src="{$imagedir}go_12.gif" border="0" alt="Inactive 'Next' arrow indicating that there are no following pages of members to display" title="Inactive 'Next' arrow indicating that there are no following pages of members to display"/>
					</xsl:if>
					
					<xsl:if test="not(@total = '1') and not(@total = @page)">
						<input type="image" border="0" name="go=next" src="{$imagedir}go_12.gif" title="Display the next page of members"/>
					</xsl:if>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
  
	<xsl:template match="error">
	<tr>
		<td class="uportal-channel-error" colspan="2"><xsl:value-of select="."/></td>
	</tr>
	</xsl:template>
  
	<xsl:template match="warning">
		<p class="uportal-channel-error" style="margin-left:5px;"><xsl:value-of select="."/></p>
	</xsl:template>
  
	<xsl:template match="sent">
		<form name="sendNotificationSendAnotherForm" method="post" action="{$baseActionURL}">
		<input type="hidden" name="command" value="sendNotification" />
		<xsl:apply-templates />
		<table border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td class="table-content-single"><strong>Your notification has been sent!</strong></td>
			</tr>
			<tr>
				<td class="table-nav">
					<input type="submit" name="done" value="Send Another" class="uportal-button" title="Send another notification"/>
					<img border="0" width="4" src="{$imagedir}transparent.gif"/>
					<input onClick="document.sendNotificationSendAnotherForm.command.value='page'" type="submit" name="cancel" value="Return to Roster" class="uportal-button" title="Return to the Roster main view"/>
				</td>
			</tr>
		</table>
		</form>     
	</xsl:template>
	 
</xsl:stylesheet>