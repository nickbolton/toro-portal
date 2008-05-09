<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    
    <xsl:template match="/">
        <!-- <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>
            This is the offering ID:<xsl:value-of select="$offeringId" />     
        </textarea> -->
        <!--<xsl:call-template name = "commonJS" /> -->
        <xsl:call-template name="links"/>
        <!-- <xsl:variable name="TYPE">
            <xsl:choose>
                <xsl:when test="/subscription/requested/offering[@id = $offeringId]">
                requested
            </xsl:when>
                <xsl:otherwise>
                subscribed
            </xsl:otherwise>
            </xsl:choose>
        </xsl:variable> -->
        <form name="subscriptionForm" action="{$baseActionURL}" method="post">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="command" value="unsubscribe"/>
            <input type="hidden" name="confirmParam" value=""/>
            <input type="hidden" name="enrollmentStatus" value="{$enrollmentStatus}"/>
            <input type="hidden" name="offeringId" value="{$offeringId}"/>
            <input type="hidden" name="topicId" value="{$topicId}"/>
            <input type="hidden" name="catPageSize" value="{$catPageSize}"/>
            <input type="hidden" name="offName" value="{$offName}"/>
            <input type="hidden" name="topicName" value="{$topicName}"/>
            <input type="hidden" name="optId" value="{$optId}"/>
            <input type="hidden" name="catSelectPage" value="{$catSelectPage}"/>
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th class="th">
                        <xsl:choose>
                            <xsl:when test="/subscription/requested/offering[@id = $offeringId]">
                                Confirm Request Cancellation
                            </xsl:when>
                            <xsl:otherwise>
                                Confirm Unenroll
                            </xsl:otherwise>
                        </xsl:choose>
                    </th>
                </tr>
                <tr>
                    <td class="table-content-single-top" style="text-align:center">
                        <xsl:choose>
                            <xsl:when test="/subscription/requested/offering[@id = $offeringId]">
                                <span class="uportal-channel-warning">Are you sure you want to cancel your request to join <xsl:value-of select="/subscription/requested/offering[@id = $offeringId]/description"/>?</span>
                            </xsl:when>
                            <xsl:otherwise>
                                <span class="uportal-channel-warning">Are you sure you want to unenroll from <xsl:value-of select="/subscription/subscribed/offering[@id = $offeringId]/description"/>?</span>
                            </xsl:otherwise>
                        </xsl:choose>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav-gradebook" style="text-align:center;">
                        <input type="radio" class="radio" name="commandButton" value="yes" onclick="document.subscriptionForm.confirmParam.value = 'yes'" id="scur1"/>
                        <label for="scur1">&#160;Yes</label>
                        <img height="1" width="15" src="{$SPACER}" alt="" title="" border="0"/>
                        <input type="radio" class="radio" name="commandButton" checked="checked" value="no" onclick="document.subscriptionForm.confirmParam.value = 'no'" id="scur2"/>
                        <label for="scur2">&#160;No</label>
                        <br/>
                    </td>
                </tr>
                <tr>
                    <td class="table-nav" style="text-align:center">
                        <input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to viewing offering subscription"/>
                        <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to viewing offering subscriptions without changing anything"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
