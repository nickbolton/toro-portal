<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

    <!-- INCLUDE FILES -->
    <xsl:include href="common.xsl" />

    <!-- VARIABLES -->
    <xsl:variable name = "ACTIVATIONS" select = "/gradebooks/gradebook-item/activation[@time_status!='expired']" />
    <xsl:variable name = "ATTRIBUTE" select = "$ACTIVATIONS/attributes/attribute" />

<!-- PARENT TEMPLATE -->
<xsl:template match="/">
    <!-- <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>
        <parameter name="workerActionURL"><xsl:value-of select="$workerActionURL" /></parameter>
        <parameter name="username"><xsl:value-of select="$username" /></parameter>
    </textarea> -->
    <xsl:call-template name="links" />

	<!-- COMMENT TO FIX MYSTERIOUS TRIPLING BUG IN XALAN 2.7 where content is repeated 3x (tt05330) -->
	<xsl:comment>
		<xsl:value-of select="count($ACTIVATIONS)" />
	</xsl:comment>
	
    <xsl:apply-templates select="//theme-set" /><!-- The "theme-set" template exists in global/theme-style.xsl and contains the JavaScript necessary for this page -->

<!-- Display table of activations only if there are activations for the user, or they have permission
 to view all and there are activations for someone -->
<xsl:choose>
    <xsl:when test="(($viewAllUserActivations = 'Y') and (count($ACTIVATIONS) &gt; 0)) or
                    ((count($ACTIVATIONS/user-list[@allusers = 'true']) +
                    count($ACTIVATIONS/user-list/user[@username = $username])) &gt; 0)">
    <form id="gradebookForm" name="gradebookForm" action="{$baseActionURL}" method="post">
        <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
        <table cellpadding="0" cellspacing="0" border="0">
            <tr>
                <th class="th-top-left">Column Name</th>
                <th class="th-top">Start Time &amp; Date</th>
                <th class="th-top">End Time &amp; Date</th>
                <th class="th-top-right">Activation Link</th>
            </tr>
            <xsl:apply-templates select="$ACTIVATIONS" />
            <xsl:if test="//theme-set"> <!-- Test for online assessment and Virtuoso information-->
                <tr>
                    <td class="table-content-single" colspan="4">
                        <table border="0" cellspacing="0" cellpadding="0">
                               <tr>
                                   <td colspan="3" align="left">Presentation Overrides (make selection then click Activation Link):</td>
                               </tr>
                            <tr>
                                <td align="right">Structural Theme:</td>
                                <td width="4"><img border="0" height="1" width="1" src="{$SPACER}" alt="" title="" /></td>
                                <td align="left">
                                    <!-- This template exists in global/theme-style.xsl -->
                                    <xsl:call-template name="theme-selection">
                                        <xsl:with-param name="FORMNAME">gradebookForm</xsl:with-param>
                                    </xsl:call-template>
                                </td>
                            </tr>
                            <tr>
                                <td align="right">Visual Style:</td>
                                <td width="4"><img border="0" height="1" width="1" src="{$SPACER}" alt="" title="" /></td>
                                <td align="left">
                                    <xsl:call-template name="style-selection"/><!-- This template exists in global/theme-style.xsl -->
                                </td>
                            </tr>
                           </table>
                    </td>
                </tr>
            </xsl:if>
            <tr>
                <td colspan="10" class="table-nav" style="text-align:center">
                    <input class="uportal-button" type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the gradebook."/>
                </td>
            </tr>
            <!-- Comment out subnav check until page gets pass enough enough to build subnav
            <xsl:choose> -->
                <!-- Include dependent nav if user has permissions to use at least of the dependent nav options and there is at least 1 column to select -->
                <!--<xsl:when test="not($editItemScores = 'N' and $editItem = 'N' and $deleteItem = 'N') and (count(/gradebooks/gradebook-item) &gt; 0)">
                 <xsl:call-template name="sublinks">
                    <xsl:with-param name="commandDefault">all_activations</xsl:with-param>
                 </xsl:call-template>
                </xsl:when> -->
                <!-- Otherwise need to include some expected hidden fields that the dependent links usually added -->
                <!--<xsl:otherwise> -->
                 <tr>
                    <td colspan="100" class="gradebook-empty-right">
                        <input type="hidden" name="command" value="all_activations"></input>
                        <input type="hidden" name="gradebook_itemID" value="{$gradebookItemID}"></input>
                        <img height="1" width="3" src="{$SPACER}" alt="" border="0" />
                    </td>
                 </tr>
                <!--</xsl:otherwise>
            </xsl:choose> -->
        </table>
    </form>
    </xsl:when>

    <xsl:otherwise>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="table-content-single-top">
                    <span class="uportal-channel-strong">
                    There are currently no assessment or assignment activations for this offering.
                    </span>
                </td>
            </tr>
            <tr>
                <td class="table-content-single-bottom">
                    Click <a href="{$baseActionURL}" title="To return to the main view of the gradebook.">here</a> to return to the main view of the gradebook.
                </td>
            </tr>

        </table>

    </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="activation">
    <xsl:variable name = "bottomStyle">
        <xsl:if test = "position() = last()">
            -bottom
        </xsl:if>
    </xsl:variable>

    <xsl:variable name = "HREF">
        <xsl:choose>
            <xsl:when test="@type = 'ASSESSMENT'">javascript:resolveLink('gradebookForm', '<xsl:value-of select="reference-link/url" />', 'assessment', '&amp;')</xsl:when>
            <xsl:when test="@type='ASSIGNMENT'">
                <xsl:choose>
                    <xsl:when test="attributes/attribute[@name='link_type']/value='url'">
                        <xsl:value-of select="attributes/attribute[@name = 'link_uri']/value" />
                    </xsl:when>
                    <xsl:when test="attributes/attribute[@name='link_type']/value='file'">
                        <xsl:value-of select="$workerActionURL" />&amp;fileType=activation&amp;activationId=<xsl:value-of select="@id" />
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="attributes/attribute[@name = 'link_uri']/value" /></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name = "TARGET">
        <xsl:choose>
            <xsl:when test="@type = 'ASSESSMENT' and reference-link/url='default'">_self</xsl:when>
            <xsl:otherwise>_blank</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- Display row if activation is for all users, the accessing user, or if the accessing user has permissions to see all -->
    <xsl:if test = "(user-list/@allusers = 'true') or (user-list/user/@username = $username) or ($viewAllUserActivations = 'Y')">
        <tr>
            <td class="table-content-left{$bottomStyle}">
                <xsl:choose>
                    <xsl:when test="$viewActivationDetails = 'Y'">
                        <a href="{$baseActionURL}?command=view_activation&amp;activation_id={@id}"
                            title="To view all activation details"
                            onmouseover="swapImage('gbViewActivationDetailsImage{position()}','channel_view_active.gif')"
                            onmouseout="swapImage('gbViewActivationDetailsImage{position()}','channel_view_base.gif')">
                            <xsl:value-of select="../title"/>
                            <img height="1" width="3"
                            src="{$SPACER}"
                            alt="" border="0"/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
                                 alt="To view all activation details" title="To view all activation details" align="absmiddle"
                                 name="gbViewActivationDetailsImage{position()}" id="gbViewActivationDetailsImage{position()}"/>
                        </a>
                    </xsl:when>

                    <xsl:otherwise>
                        <xsl:value-of select="../title"/>
                        <img height="1" width="3"
                        src="{$SPACER}"
                        alt="" border="0"/>
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif"
                         alt="Not permitted to view the activation details" title="Not permitted to view the activation details" align="absmiddle"/>
                    </xsl:otherwise>
                </xsl:choose>
                <img height="1" width="3"
                    src="{$SPACER}"
                    alt="" border="0"/>
                <xsl:choose>
                    <xsl:when test="$deleteActivation = 'Y'">
                        <a href="{$baseActionURL}?command=deactivate&amp;activation_id={@id}" title="To delete this activation record"
                            onmouseover="swapImage('gbDeleteActivationImage{position()}','channel_delete_active.gif')"
                            onmouseout="swapImage('gbDeleteActivationImage{position()}','channel_delete_base.gif')">
                            <img border="0"
                            src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                            alt="To delete this activation record" title="To delete this activation record" align="absmiddle"
                            name="gbDeleteActivationImage{position()}" id="gbDeleteActivationImage{position()}"/>
                        </a>
                    </xsl:when>

                    <xsl:otherwise>
                     <img border="0"
                            src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
                            alt="Not permitted to delete activation records" title="Not permitted to delete activation records" align="absmiddle"/>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td class="table-content{$bottomStyle}"><xsl:value-of select="start_time"/>, <xsl:value-of select="start_date"/></td>
            <td class="table-content{$bottomStyle}"><xsl:value-of select="end_time"/>, <xsl:value-of select="end_date"/></td>
            <td class="table-content-right{$bottomStyle}">
                <xsl:choose>
                    <!-- ACTIVATION IS STILL IN THE FUTURE -->
                    <xsl:when test = "@time_status='future'">
                        Not available yet
                    </xsl:when>
                    <!-- DIRECT TO PORTAL ASSESSMENT CHANNEL UNTIL ABLE TO FOCUS IT FROM GRADEBOOK -->
                    <xsl:when test = "@type = 'ASSESSMENT' and reference-link/url='default'">
                        Go to the <span class="uportal-channel-emphasis">Portal Assessment Channel</span>
                    </xsl:when>
                    <!-- CAN BE IMPLEMENTED WHEN THE GRADEBOOK IS ABLE TO FOCUS THE PORTAL ASSESSMENT CHANNEL -->
                    <xsl:when test="@type = 'ASSESSMENT' and reference-link">
                        <a href="{$HREF}" title="To launch this assessment">To Assessment</a>
                        <!-- Previous implementation substituted with the above line in the Academus 1.4 Virtuoso integration on 6/15/04 -->
                    </xsl:when>
                    <!-- Old implementation still works -->
                    <xsl:when test="@type = 'ASSESSMENT'">
                        <a href="{$HREF}" target="{$TARGET}" title="To launch this assessment">To Assessment</a>
                    </xsl:when>
                    <xsl:when test="attributes/attribute[@name = 'link_type']/value = 'file'">
                        <a href="{$HREF}" target="{$TARGET}" title="To access/download this file">To Assignment File</a>
                    </xsl:when>
                    <xsl:when test="attributes/attribute[@name = 'link_type']/value = 'url'">
                        <a href="{$HREF}" target="{$TARGET}"  title="To launch this activation url">To Assignment URL</a>
                    </xsl:when>
                    <xsl:otherwise>&#160;</xsl:otherwise>
                </xsl:choose>
            </td>
        </tr>
    </xsl:if>
</xsl:template>
</xsl:stylesheet>



