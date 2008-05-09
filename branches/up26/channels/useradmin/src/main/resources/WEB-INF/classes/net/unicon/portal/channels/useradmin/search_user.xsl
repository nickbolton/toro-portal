<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
        <xsl:call-template name="links"/>
        <h2 class="page-title">Search for User</h2>
        
        <div class="bounding-box1">
			<form action="{$baseActionURL}?command=find" method="post">
				<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
				<input type="hidden" name="resetCatalog" value="true"/>
				<!-- UniAcc: Layout Table -->
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<!-- User Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap">
							<label for="auact1">User Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="user_name" size="35" maxlength="35" id="auact1"/>
							<span class="uportal-text-small"> (35 char max)</span>
						</td>
					</tr>
					<!-- First Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap">
							<label for="auact2">First Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="first_name" size="15" maxlength="15" id="auact2"/>
							<span class="uportal-text-small"> (15 char max)</span>
						</td>
					</tr>
					<!-- Last Name -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap">
							<label for="auact3">Last Name</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="last_name" size="15" maxlength="15" id="auact3"/>
							<span class="uportal-text-small"> (15 char max)</span>
						</td>
					</tr>
					<!-- Email -->
					<tr>
						<td class="table-light-left" style="text-align:right;" nowrap="nowrap">
							<label for="auact4">Email</label>
						</td>
						<td class="table-content-right" style="text-align:left;" width="100%">
							<input class="text" type="text" name="email" size="35" maxlength="60" id="auact4"/>
							<span class="uportal-text-small"> (60 char max)</span>
						</td>
					</tr>
					<tr>
						<td colspan="2" class="table-nav">
							<input class="uportal-button" name="submit" value="Submit" type="submit" title="Submit user search"/>
							<input type="button" class="uportal-button" name="cancel" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="Cancel user search"/>
						</td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>
</xsl:stylesheet>
