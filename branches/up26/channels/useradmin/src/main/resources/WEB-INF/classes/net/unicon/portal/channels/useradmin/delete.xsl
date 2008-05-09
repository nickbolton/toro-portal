<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include -->
    <xsl:include href="common.xsl"/>
    <!-- Parameters -->
    <xsl:template match="/">
        <form action="{$baseActionURL}" method="post">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="user_name" value="{$user_name}"/>
            <input type="hidden" name="command" value="delete_confirm"/>
            <!-- UniAcc: Layout Table -->
            <h2 class="page-title">Delete User</h2>
            <div class="bounding-box1">
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td class="table-content-single-top" style="text-align:center">
							<span class="uportal-channel-warning">Are you sure you want to delete the user <span class="uportal-channel-strong">"<xsl:value-of select="$user_name"/>"</span>?</span>
						</td>
					</tr>
					<tr>
						<td class="table-content-single" style="text-align:center">
							<input type="radio" class="radio" name="deleteConfirmation" value="yes" id="uacdr1"/>
							<label for="uacdr1">&#160;Yes</label>
							<img height="1" width="15" src="{$SPACER}" alt="" title="" border="0"/>
							<input checked="checked" type="radio" class="radio" name="deleteConfirmation" value="no" id="uacdr2"/>
							<label for="uacdr2">&#160;No</label>
							<br/>
						</td>
					</tr>
					<tr>
						<td class="table-content-single-bottom" style="text-align:center">
							<input type="submit" class="uportal-button" title="Submit deletion of this user" value="Submit"/>
							<input type="button" class="uportal-button" onclick="window.locationhref='{$baseActionURL}?command=find'" value="Cancel" title="Cancel deletion of this user and return to the main view"/>
						</td>
					</tr>
				</table>
			</div>
        </form>
    </xsl:template>
</xsl:stylesheet>
