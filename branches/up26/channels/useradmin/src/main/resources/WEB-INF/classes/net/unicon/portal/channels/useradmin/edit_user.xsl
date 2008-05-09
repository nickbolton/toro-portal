<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include Files -->
    <xsl:import href="common.xsl"/>
    <xsl:variable name = "USER" select="/user-admin/user[@username = $user_name]" />
    <xsl:variable name = "ATTR_LIST" select = "$USER/attributes/attribute" />
    <xsl:template match="/">
        <!--
        <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>
            <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>     
        </textarea> 
        -->
        <xsl:call-template name="addEditJs"/>
        <xsl:call-template name="links"/>
        <h2 class="page-title">Edit User</h2>
        <div class="bounding-box1">
			<form onSubmit="return validator.applyFormRules(this, new UserAdminRulesObject())" name="userAdminForm{$instanceId}" action="{$baseActionURL}" method="post">
				<input type="hidden" name="command" value="update"/>
				<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
				<!-- UniAcc: Data Table -->
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<!-- User Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuUsername">
							User Name
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAeuEditUser UAeuUsername">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							<xsl:value-of select="$USER/@username"/>
							<input type="hidden" name="user_name" value="{$USER/@username}"/>
						</td>
					</tr>
					<!-- Password -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuPassword">
							Password
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAeuEditUser UAeuPassword">
							<!-- UniAcc: Layout Table -->
							<table width="100%">
								<tr>
									<td class="table-content-iso">
										<input type="radio" class="radio" name="entry_type" value="1" checked="true" id="uaceur1">
											<label for="uaceur1">Keep Current Password</label>
										</input>
									</td>
								</tr>
								<tr>
									<td class="table-content-iso">
										<input type="radio" class="radio" name="entry_type" value="2" id="uaceur2">
											<label for="uaceur2">Change Current Password</label>
										</input>
										<br/>
										<input class="text" type="text" name="password" size="35" maxlength="35" title="New Password Field"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<!-- Prefix -->
					<!--<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuPrefixName">
							<label for="uaceut0">Prefix</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="name_prefix" 
								size="5" maxlength="15" value="{$ATTR_LIST[@name='name_prefix']/value}" id="uaceut0"/>
						</td>
					</tr> -->
					<!-- First Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuFirstName">
							<label for="uaceut1">First Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="first_name" size="15" maxlength="15" value="{$USER/first_name}" id="uaceut1"/>
							<span class="uportal-text-small"> (15 char max)</span>
						</td>
					</tr>
					<!-- Last Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuLastName">
							<label for="uaceut2">Last Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="last_name" size="15" maxlength="15" value="{$USER/last_name}" id="uaceut2"/>
							<span class="uportal-text-small"> (15 char max)</span>
						</td>
					</tr>
					<!-- Suffix -->
					<!--<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuSuffixName">
							<label for="uaceut3">Suffix</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="name_suffix" 
								size="5" maxlength="15" value="{$ATTR_LIST[@name='name_suffix']/value}" id="uaceut3"/>
						</td>
					</tr> -->
					<!-- Address -->
					<!--<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuAddress">
							Address
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<label for="uaceut4">Line 1:</label> <input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="address_line1" 
								size="20" maxlength="20" value="{$ATTR_LIST[@name='address_line1']/value}" id="uaceut4"/><br/>
							<label for="uaceut5">Line 2:</label> <input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="address_line2" 
								size="20" maxlength="20" value="{$ATTR_LIST[@name='address_line2']/value}" id="uaceut5"/><br/>
							<label for="uaceut6">City:</label> <input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="city" 
								size="10" maxlength="15" value="{$ATTR_LIST[@name='city']/value}" id="uaceut6"/>&#160;
							<label for="uaceut7">State:</label> <input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="state" 
								size="5" maxlength="15" value="{$ATTR_LIST[@name='state']/value}" id="uaceut7"/>&#160;
							<label for="uaceut8">Zip:</label> <input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="zip" 
								size="5" maxlength="15" value="{$ATTR_LIST[@name='zip']/value}" id="uaceut8"/>
						</td>
					</tr> -->
					<!-- Phone -->
					<!--<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuPhone">
							<label for="uaceut9">Phone</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input tabindex="-1" class="text-disabled" readonly="readonly" type="text" name="name_suffix" 
								size="15" maxlength="15" value="{$ATTR_LIST[@name='phone']/value}" id="uaceut9"/>
						</td>
					</tr> -->
					<!-- Email -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAeuEditUser" id="UAeuEmail">
							<label for="uaceut10">Email</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="email" size="35" maxlength="60" value="{$USER/email}" id="uaceut10"/>
							<br/>
							<span class="uportal-text-small">(60 char max)</span>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="table-nav">
							<input class="uportal-button" name="submit" value="Submit" type="submit" title="Submit user changes"/>
							<input type="button" class="uportal-button" name="cancel" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=find'" title="Cancel user changes"/>
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>
</xsl:stylesheet>
