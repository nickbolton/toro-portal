<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">

<xsl:call-template name="addEditJs"/>
<xsl:call-template name="links"/>

<h2 class="page-title">Edit Email Address</h2>
<div class="bounding-box1">
	<form onSubmit="return validator.applyFormRules(this, new UserAdminRulesObject())" name="userAdminForm{$instanceId}" action="{$baseActionURL}?command=updateEmail" method="post">
	<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
	<table cellpadding="0" cellspacing="0" border="0" width="100%">
	<!-- User Name -->
	<tr>
		<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAceChangeEmail">
			User Name
		</td>
		<td class="table-content-right" style="text-align:left;" width="100%">
			<xsl:value-of select="$user_name"/>
			<input type="hidden" name="user_name" value="{$user_name}"></input>
		</td>
	</tr>

	<!-- Current Password -->
	<tr>
		<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAceChangeEmail" id="UAcePass">
			Password
		</td>
		<td class="table-content-right" style="text-align:left;" width="100%" headers="UAceChangeEmail UAcePass">
			<label for="ueccpt1">Current Password</label>
			<input type="password" class="text" name="currentPassword" size="35" maxlength="35" id="ueccpt1" />
		</td>
	</tr>

	<!-- First Name -->
	<tr>
		<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAceChangeEmail" id="UAceFirstName">
			First Name
		</td>
		<td class="table-content-right" style="text-align:left;" width="100%" headers="UAceChangeEmail UAceFirstName">
			<xsl:value-of select="$first_name"/>
			<input type="hidden" name="first_name" value="{$first_name}"></input>   
		</td>
	</tr>

	<!-- Last Name -->
	<tr>
		<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAceChangeEmail" id="UAceLastName">
			Last Name
		</td>
		<td class="table-content-right" style="text-align:left;" width="100%" headers="UAceChangeEmail UAceLastName">
			<xsl:value-of select="$last_name"/>
			<input type="hidden" name="last_name" value="{$last_name}"></input>
		</td>
	</tr>

	<!-- Email -->
	<tr>
		<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAceChangeEmail" id="UAceEmail">
			<label for="uetemail">Email</label>
		</td>
		<td class="table-content-right" style="text-align:left;" width="100%" headers="UAceChangeEmail UAceEmail">
			<input class="text" type="text" name="email" size="35" id="uetemail" 
				   maxlength="60" value="{$email}"></input><br/>
			<span class="uportal-text-small">(60 char max)</span>   
		</td>
	</tr>
	<!--	<tr>
		<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAceChangeEmail" id="UAcePermissions">
			Permissions
		</td>
		<td class="table-content-right" style="text-align:left;" width="100%" headers="UAceChangeEmail UAcePermissions">
			<xsl:value-of select="$roleLabel"/>
		</td>
	</tr> -->	<tr>
		<td colspan="2" class="table-nav">
			<input class="uportal-button" name="submit" value="Submit" type="submit" title="Submit Email change"/>
			<input type="button" class="uportal-button" name="cancel" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel Email change"/>
		</td>
	</tr>
	</table>
	</form>
</div>
    
</xsl:template>
</xsl:stylesheet>
