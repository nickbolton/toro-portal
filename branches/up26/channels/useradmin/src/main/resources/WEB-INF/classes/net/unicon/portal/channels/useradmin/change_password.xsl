<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<xsl:template match="/">
        <!-- <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>
            <parameter name="user_name"><xsl:value-of select="$user_name" /></parameter>     
            <parameter name="first_name"><xsl:value-of select="$first_name" /></parameter>
            <parameter name="last_name"><xsl:value-of select="$last_name" /></parameter>
            <parameter name="email"><xsl:value-of select="$email" /></parameter>     
            <parameter name="roleLabel"><xsl:value-of select="$roleLabel" /></parameter>   
        </textarea> -->
		<xsl:call-template name="addEditJs"/>
		<xsl:call-template name="links"/>
		<h2 class="page-title">Edit Password</h2>
		
		<div class="bounding-box1">
			<form onSubmit="return validator.applyFormRules(this, new UserAdminRulesObject())" name="userAdminForm{$instanceId}" action="{$baseActionURL}?command=updatePassword" method="post">
				<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
				<!-- UniAcc: Data Table -->
				<table cellpadding="0" cellspacing="0" border="0" width="100%">

					<!-- User Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							User Name
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							<xsl:value-of select="$user_name"/>
							<input type="hidden" name="user_name" value="{$user_name}"/>
						</td>
					</tr>
					<!-- First Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpFirstName">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							First Name
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpFirstName">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							<xsl:value-of select="$first_name"/>
							<input type="hidden" name="first_name" value="{$first_name}"/>
						</td>
					</tr>
					<!-- Last Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpLastName">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							Last Name
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpLastName">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							<xsl:value-of select="$last_name"/>
							<input type="hidden" name="last_name" value="{$last_name}"/>
						</td>
					</tr>
					<!-- Email -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpEmail">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							Email
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpEmail">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							<xsl:value-of select="$email"/>
							<input type="hidden" name="email" value="{$email}"/>
							<br/>
						</td>
					</tr>					
					<!-- Password -->
					<tr>
						<td  class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpPass">
							<label for="uaccpt1">Current Password</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpPass">
							<input type="password" class="text" name="currentPassword" size="35" maxlength="35" id="uaccpt1"/>
						</td>
					</tr>
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpPass">
							<label for="uaccpt2">New Password</label>
						</td>						
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpPass">
							<input type="password" class="text" name="newPassword" size="35" maxlength="35" id="uaccpt2"/>
						</td>
					</tr>
					<tr>
						<td  class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpPass">
							<label for="uaccpt3">Verify New Password</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpPass">
							<input type="password" class="text" name="verifiedPassword" size="35" maxlength="35" id="uaccpt3"/>
						</td>
					</tr>

					<!--			
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap" headers="UAcpChangePass" id="UAcpPermmissions">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							Permissions
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%" headers="UAcpChangePass UAcpPermmissions">
							<img height="1" width="1" src="{$SPACER}" alt="" title="" border="0"/>
							<xsl:value-of select="$roleLabel"/>
						</td>
					</tr>-->
					<tr>
						<td colspan="2" class="table-nav">
							<input class="uportal-button" name="submit" value="Submit" type="submit" title="Submit password change"/>
							<input type="button" class="uportal-button" name="cancel" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel password change"/>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</xsl:template>
</xsl:stylesheet>
