<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
        <xsl:call-template name="addEditJs"/>
        <xsl:call-template name="links"/>
        <h2 class="page-title">Add User</h2>
        
        <div class="bounding-box1">
			<form onSubmit="if (document.userAdminForm{$instanceId}.command.value == 'selectGroups') return true; else return validator.applyFormRules(this, new UserAdminRulesObject());" name="userAdminForm{$instanceId}" action="{$baseActionURL}" method="post">
				<input type="hidden" name="command" value="insert"/>
				<input type="hidden" name="servant_command" value="selectGroups"/>
				<input type="hidden" name="next_command" value="updateGroups"/>
				<input type="hidden" name="next_command_arg" value="username"/>
				<input type="hidden" name="next_command_username" value=""/>
				<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
				<!-- UniAcc: Data Table -->
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<!-- User Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAauAddUser">
							<label for="uaaut4">User Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="user_name" size="35" maxlength="35" value="{$user_name}" id="uaaut4"/>
							<br/>
							<span class="uportal-text-small">(35 character maximum)</span>
						</td>
					</tr>
					<!-- Password -->
					<tr>
						<td rowspan="2" class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAauAddUser" id="UAauPass">
							Password
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAauAddUser UAauPass">
							<input type="radio" class="radio" name="entry_type" value="1" checked="true" id="uaaur1">
							<label for="uaaur1">Automatic Password <span class="uportal-text-small">(Same as username)</span></label>
							</input>
						</td>
					</tr>
					<tr>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAauAddUser">
							<input type="radio" class="radio" name="entry_type" value="2" id="uaaur2">
								<label for="uaaur2">Manually Entered</label>
							</input>
							<br/>
							<table width="100%">
								<tr>
									<td>
										<input type="text" class="text" name="password" size="35" maxlength="35" title="Enter Password"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<!-- First Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAauAddUser">
							<label for="uaaut1">First Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="first_name" size="15" maxlength="15" value="{$first_name}" id="uaaut1"/>
							<span class="uportal-text-small"> (15 char max)</span>
						</td>
					</tr>
					<!-- Last Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAauAddUser">
							<label for="uaaut2">Last Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="last_name" size="15" maxlength="15" value="{$last_name}" id="uaaut2"/>
							<span class="uportal-text-small"> (15 char max)</span>
						</td>
					</tr>
					<!-- Email -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAauAddUser">
							<label for="uaaut3">Email</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="email" size="35" maxlength="60" value="{$email}" id="uaaut3"/>
							<br/>
							<span class="uportal-text-small">(60 char max)</span>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="table-nav">
							<center>
								<input onClick="document.userAdminForm{$instanceId}.next_command_username.value=document.userAdminForm{$instanceId}.user_name.value" class="uportal-button" name="submit" value="Next" type="submit" title="Submit new user and select group membership"/>
								<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel adding new user"/>
							</center>
						</td>
					</tr>
				</table>
				<xsl:if test="($in_use = 'true')">
					<P class="uportal-channel-warning">Warning user name <span class="uportal-channel-error">
							<xsl:value-of select="$user_name"/>
						</span> is already in use. Please select another username and re-submit.</P>
				</xsl:if>
			</form>
		</div>
    </xsl:template>
</xsl:stylesheet>
