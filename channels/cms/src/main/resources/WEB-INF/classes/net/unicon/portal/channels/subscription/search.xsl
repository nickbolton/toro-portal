<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
    <!--
        <textarea rows="4" cols="40">
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
        <!-- UniAcc: Data Table -->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <th class="th" nowrap="nowrap" id="SCsName">
                    Topic Name<br/>
                    <img height="1" width="120" src="{$SPACER}" alt="" title="" border="0"/>
                </th>
                <th class="th" width="100%" id="SCsDesc">
                    Offering Name
                </th>
                <th class="th" nowrap="nowrap" id="SCsType">
                    Type
                </th>
                <th class="th" nowrap="nowrap" id="SCsStatus">
                    Status
                </th>
            </tr>
            <xsl:choose>
                <!-- No matching search results -->
                <!-- Find out why it is testing on Open enrollment only
            <xsl:when test="count(available/offering/enrollmentModel[text() = 'Open']) = 0 and count(subscribed/offering) = 0"> -->
                <xsl:when test="count(available/offering) = 0 and count(subscribed/offering) = 0 and count(requested/offering) = 0">
                    <tr>
                        <td class="table-content-right-bottom" colspan="4">
                            There are no matches to your search criteria.
                        </td>
                    </tr>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Build table of search results -->
                    <xsl:apply-templates select="available"/>
                    <xsl:apply-templates select="subscribed"/>
                    <xsl:apply-templates select="requested"/>
                </xsl:otherwise>
            </xsl:choose>
        </table>
        <xsl:call-template name="catalog"/>
        <!-- Catalog for paging and search called from global.xsl -->
    </xsl:template>
    <xsl:template match="available">
        <xsl:apply-templates select="offering">
            <xsl:with-param name="status" select="'Unenrolled'"/>
            <xsl:sort select="name"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="subscribed">
        <xsl:apply-templates select="offering">
            <xsl:with-param name="status" select="'Enrolled'"/>
            <xsl:sort select="name"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="requested">
        <xsl:apply-templates select="offering">
            <xsl:with-param name="status" select="'Requested'"/>
            <xsl:sort select="name"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="offering">
        <xsl:param name="status"/>
        <xsl:choose>
            <xsl:when test="(enrollmentModel='Open') and ($status='Unenrolled') and ($subscribe='Y')">
                <tr>
                    <td class="table-light" style="text-align:center;" width="120" headers="SCsName">
                        <xsl:value-of select="name"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="100%" headers="SCsDesc">
                        <xsl:value-of select="description"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="70" headers="SCsType">
                        <xsl:value-of select="enrollmentModel"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="90" headers="SCsStatus">
                        <a href="{$baseActionURL}?catSelectPage={$catSelectPage}&amp;catPageSize={$catPageSize}&amp;command=subscribe&amp;offeringId={@id}&amp;enrollmentStatus=ENROLLED&amp;topicName={$topicName}&amp;offName={$offName}&amp;optId={$optId}" title="To enroll in this offering" onmouseover="swapImage('subscriptionViewImage{@id}','channel_add_active.gif')" onmouseout="swapImage('subscriptionViewImage{@id}','channel_add_base.gif')">
                            Unenrolled
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Enroll' Icon: Enroll in this offering" title="'Enroll' Icon: Enroll in this offering" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}"/>
                        </a>
                    </td>
                </tr>
            </xsl:when>
            <xsl:when test="(enrollmentModel='Request/Approve') and ($status='Unenrolled') and ($subscribe='Y')">
                <tr>
                    <td class="table-light" style="text-align:center;" width="120" headers="SCsName">
                        <xsl:value-of select="name"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="100%" headers="SCsDesc">
                        <xsl:value-of select="description"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="70" headers="SCsType">
                        <xsl:value-of select="enrollmentModel"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="90" headers="SCsStatus">
                        <a href="{$baseActionURL}?catSelectPage={$catSelectPage}&amp;catPageSize={$catPageSize}&amp;command=subscribe&amp;offeringId={@id}&amp;enrollmentStatus=PENDING&amp;topicName={$topicName}&amp;offName={$offName}&amp;optId={$optId}" title="To send request to instructor to enroll in this offering" onmouseover="swapImage('subscriptionViewImage{@id}','channel_add_active.gif')" onmouseout="swapImage('subscriptionViewImage{@id}','channel_add_base.gif')">
                            Unenrolled
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="'Enroll' Icon: Send request to instructor to enroll in this offering" title="'Enroll' Icon: Send request to instructor to enroll in this offering" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}"/>
                        </a>
                    </td>
                </tr>
            </xsl:when>
            <xsl:when test="$status='Unenrolled' and (enrollmentModel='sis' or ($subscribe!='Y' and ((enrollmentModel='Open') or (enrollmentModel='Request/Approve'))))">
                <tr>
                    <td class="table-light" style="text-align:center;" width="120" headers="SCsName">
                        <xsl:value-of select="name"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="100%" headers="SCsDesc">
                        <xsl:value-of select="description"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="70" headers="SCsType">
                        <xsl:value-of select="enrollmentModel"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="90" headers="SCsStatus">
                        Unenrolled
                    </td>
                </tr>
            </xsl:when>
            <xsl:when test="(enrollmentModel='Request/Approve') and ($status='Requested')">
                <tr>
                    <td class="table-light" style="text-align:center;" width="120" headers="SCsName">
                        <xsl:value-of select="name"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="100%" headers="SCsDesc">
                        <xsl:value-of select="description"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="70" headers="SCsType">
                        <xsl:value-of select="enrollmentModel"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="90" headers="SCsStatus">
                        <a href="{$baseActionURL}?catSelectPage={$catSelectPage}&amp;catPageSize={$catPageSize}&amp;command=unenroll&amp;offeringId={@id}&amp;topicId={$currentTopicId}&amp;enrollmentStatus=PENDING&amp;topicName={$topicName}&amp;offName={$offName}&amp;optId={$optId}" title="To cancel request to enroll in this offering" onmouseover="swapImage('subscriptionViewImage{@id}','channel_delete_active.gif')" onmouseout="swapImage('subscriptionViewImage{@id}','channel_delete_base.gif')">
                            Pending Approval
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Cancel' Icon: Cancel request to enroll in this offering" title="'Cancel' Icon: Cancel request to enroll in this offering" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}"/>
                        </a>
                    </td>
                </tr>
            </xsl:when>
            <xsl:when test="$status='Enrolled' and $unsubscribe='Y' and enrollmentModel!='sis'">
                <tr>
                    <td class="table-content" style="text-align:center;" width="120" headers="SCsName">
                        <xsl:value-of select="name"/>
                    </td>
                    <td class="table-content" style="text-align:center;" width="100%" headers="SCsDesc">
                        <xsl:value-of select="description"/>
                    </td>
                    <td class="table-content" style="text-align:center;" width="70" headers="SCsType">
                        <xsl:value-of select="enrollmentModel"/>
                    </td>
                    <td class="table-content" style="text-align:center;" width="90" headers="SCsStatus">
                        <a href="{$baseActionURL}?catSelectPage={$catSelectPage}&amp;catPageSize={$catPageSize}&amp;command=unenroll&amp;offeringId={@id}&amp;topicId={$currentTopicId}&amp;enrollmentStatus=ENROLLED&amp;topicName={$topicName}&amp;offName={$offName}&amp;optId={$optId}" title="To unenroll from this offering" onmouseover="swapImage('subscriptionViewImage{@id}','channel_delete_active.gif')" onmouseout="swapImage('subscriptionViewImage{@id}','channel_delete_base.gif')">
                            Enrolled
                            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Unenroll' Icon: Unenroll from this offering" title="'Unenroll' Icon: Unenroll from this offering" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}"/>
                        </a>
                    </td>
                </tr>
            </xsl:when>
            <xsl:when test="$status='Enrolled' and (enrollmentModel='sis' or $unsubscribe!='Y')">
                <tr>
                    <td class="table-light" style="text-align:center;" width="120" headers="SCsName">
                        <xsl:value-of select="name"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="100%" headers="SCsDesc">
                        <xsl:value-of select="description"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="70" headers="SCsType">
                        <xsl:value-of select="enrollmentModel"/>
                    </td>
                    <td class="table-light" style="text-align:center;" width="90" headers="SCsStatus">
                        Enrolled
                    </td>
                </tr>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
