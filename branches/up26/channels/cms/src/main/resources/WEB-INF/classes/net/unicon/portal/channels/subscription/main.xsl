<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
        <!-- <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>   
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>   
        <parameter name="channelParam"><xsl:value-of select="$channelParam" /></parameter>   
        <parameter name="topicNameParam"><xsl:value-of select="$topicNameParam" /></parameter>   
        <parameter name="offeringNameParam"><xsl:value-of select="$offeringNameParam" /></parameter>   
        <parameter name="offeringDescParam"><xsl:value-of select="$offeringDescParam" /></parameter>   
        <parameter name="offeringNameSearchParam"><xsl:value-of select="$offeringNameSearchParam" /></parameter>   
        <parameter name="searchCommand"><xsl:value-of select="$searchCommand" /></parameter>   
        <parameter name="userEnrollmentModelParam"><xsl:value-of select="$userEnrollmentModelParam" /></parameter>   
        <parameter name="showAvailableCommand"><xsl:value-of select="$showAvailableCommand" /></parameter>   
        <parameter name="currentTopicId"><xsl:value-of select="$currentTopicId" /></parameter>   
        <parameter name="currentTopicName"><xsl:value-of select="$currentTopicName" /></parameter>   
        <parameter name="currentTopicDesc"><xsl:value-of select="$currentTopicDesc" /></parameter>   
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
    </textarea> -->
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="subscription">
        <xsl:call-template name="links"/>
        <form method="post" action="{$baseActionURL}?command=search">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="catPageSize" value="{$catPageSize}"/>
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th" id="SCOfferingSearch">
                          Offering Search
                    </th>
                </tr>
                <tr>
                    <td class="table-light-left-top" nowrap="nowrap" headers="SCOfferingSearch">
                        <label for="topicName">Topic Name:</label>
                    </td>
                    <td class="table-content-right-top" width="100%">
                        <input type="text" class="text" name="topicName" id="topicName" size="8"/>
                    </td>
                </tr>
                <tr>
                    <td class="table-light-left" nowrap="nowrap" headers="SCOfferingSearch">
                        <label for="offName">Offering Name:</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="text" class="text" name="offName" id="offName" size="8"/>
                    </td>
                </tr>
                <!-- Search by description is currently not supported
          <tr>
            <td class="table-light-left" nowrap="nowrap">Offering Description:</td>
            <td class="table-content-right" width="100%">
                <input type="text" class="text" name="offDesc" id="offDesc" size="8" />
            </td>
          </tr> -->
                <tr>
                    <td class="table-light-left" nowrap="nowrap" headers="SCOfferingSearch">
                        <label for="optId">Offering ID:</label>
                    </td>
                    <td class="table-content-right" width="100%">
                        <input type="text" class="text" name="optId" id="optId" size="8"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" class="table-nav">
                        <input class="uportal-button" type="submit" name="submit" value="Search" title="Submit offering search"/>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
