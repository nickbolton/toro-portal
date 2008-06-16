<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" indent="yes" />
<!-- Include Files -->
<xsl:include href="common.xsl"/>

<!-- Parameters passed in via confirmUnenroll command -->
<xsl:param name="uid"/>
<xsl:param name="offeringIdParam"/>

<xsl:template match="/">
        <!--<xsl:call-template name = "commonJS" /> -->
        <form name="rosterForm" action="{$baseActionURL}" method="post">
            <input type="hidden" name="targetChannel" value="{$targetChannel}" />
            <input type="hidden" name="catPageSize" value="{$catPageSize}" />
            <input type="hidden" name="uid" value="{$uid}" />
            <input type="hidden" name="offeringIdParam" value="{$offeringIdParam}" />
            <input type="hidden" name="confirmParam" value="" />
            <input type="hidden" name="command" value="unenroll" />
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<th class="th-top-single">
                		Unenroll User
            		</th>
				</tr>
                <tr>
                    <th class="table-content-single-top">
                        <span class="uportal-channel-warning">Do you want to unenroll <span class="uportal-channel-strong">&#034;<xsl:value-of select="/roster/user[@id = $uid]/firstname"/>&#160;<xsl:value-of select="/roster/user[@id = $uid]/lastname"/>&#034;</span> from this offering?</span>
                    </th>
                </tr>
                <tr>
                    <td class="table-nav-gradebook" style="text-align:center;">
                        <input type="radio" class="radio" name="commandButton" value="confirm" onclick="document.rosterForm.confirmParam.value = 'yes'" id="rcur1"/>
                        <label for="rcur1">&#160;Yes</label>
                        <img height="1" width="15" src="{$SPACER}" alt="" title="" border="0"/>
                        <input type="radio" class="radio" name="commandButton" value="no" checked="checked" onclick="document.rosterForm.confirmParam.value = 'no'" id="rcur2"/>
                        <label for="rcur2">&#160;No</label>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav" style="text-align:center">
                        <input type="submit" class="uportal-button" value="Submit" title="To submit your response and return to the main view of the roster"/>
                        <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=page&amp;catPageSize={$catPageSize}'" title="To return to the main view of the roster without unenrolling the user."/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
