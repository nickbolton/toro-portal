<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="yes" />
<!-- Include Files -->
<xsl:include href="common.xsl"/>

<!-- Parameters passed in via resolve command -->
<xsl:param name="uid"/>
<xsl:template match="/">
		<!--<textarea  rows="4" cols="40">
			<xsl:copy-of select="*" />
		</textarea> -->

		<!--<xsl:call-template name = "commonJS" /> -->
		<form name="rosterForm" action="{$baseActionURL}" method="post">
			<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
			<input type="hidden" name="catPageSize" value="{$catPageSize}" />
			<input type="hidden" name="uid" value="{$uid}"></input>
			<input type="hidden" name="confirmParam" value=""></input>
			<input type="hidden" name="command" value="approve"></input>

			<table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<th colspan="2" class="th-top-single">
                		Enrollment Approval
            		</th>
				</tr>
				<tr>
					<td class="table-content-single" style="text-align:center">
						<span class="uportal-channel-warning">Do you want to approve this request <!--from <span class="uportal-channel-strong"><xsl:value-of select="/roster/user[@id = $userIdParam]/firstname"/>&#160;<xsl:value-of select="/roster/user[@id = $userIdParam]/lastname"/></span> --> to join the offering?</span>
					</td>
				</tr>
				<tr>
					<td class="table-nav-gradebook" style="text-align:center;">
						<input type="radio" class="radio" name="commandButton" value="confirm" onclick="document.rosterForm.confirmParam.value = 'yes'" id="rcrr1"/>
						<label for="rcrr1">&#160;Yes</label>
        				<img height="1" width="15" src="{$SPACER}" alt="" title=""/>
						<input type="radio" class="radio" name="commandButton" value="no" checked="checked" onclick="document.rosterForm.confirmParam.value = 'no'" id="rcrr2"/>
						<label for="rcrr2">&#160;No</label>
						<br/>
					</td>
				</tr>
				<tr>
					<td class="table-nav" style="text-align:center">
						<input type="submit" class="uportal-button" value="Submit" title="To submit your response and return to the main view of the roster"/>
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the roster without changing the pending status"/>
					</td>
				</tr>
			</table>
		</form>
	</xsl:template>
</xsl:stylesheet>
