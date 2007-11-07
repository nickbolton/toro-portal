<?xml version="1.0"?>
<!--
   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.

   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.

   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" />
	<xsl:include href="common.xsl"/>
<!-- //// RAD.xsl ///////////////////////////////////////// -->
    
<!--variable/params-->
    <xsl:variable name="back" select="/calendar-system/view/@back" />
    <xsl:variable name="window" select="/calendar-system/view/@window" />
    <xsl:variable name="count" select="count(/calendar-system/delete)" />
    <xsl:variable name="notDelete" select="/calendar-system/delete/@ceid" />
    <xsl:template match="calendar-system">
        <form method="post" action="{$baseActionURL}">
<!-- Caption Table -->
			<!--UniAcc: Layout Table -->
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <th class="th-top">Delete Confirmation</th>
                </tr>
                <!--<xsl:if test="delete/@event='true'">
                    <tr>
                        <td class="table-nav">
	                        <strong>Notify Attendees</strong>
	                        &#160; 
	                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
	                        <input type="checkbox" class="radio" name="notification" id="CDA-NotificationC1"/>
	                        <label for="CDA-NotificationC1">&#160;Notification</label>
	                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
	                        <input type="checkbox" class="radio" name="email" id="CDA-EmalC1"/>
	                        <label for="CDA-EmalC1">&#160;Email</label>
                        </td>
                    </tr>
                </xsl:if>
                <tr>
                    <td class="table-nav">
                        <input type="hidden" name="sid">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$sid" />
                            </xsl:attribute>
                        </input>
                        <xsl:if test="$notDelete">
                            <input class="uportal-button" type="submit" name="do~ok" value="OK" />
                        </xsl:if>
                        <input type="hidden" name="window">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$window" />
                            </xsl:attribute>
                        </input>
                        <input class="uportal-button" type="submit" name="go~{$back}" value="Cancel" />
                    </td>
                </tr>
            </table> -->
            <tr>
                <td class="table-nav">
		            <xsl:choose>
		                <xsl:when test="$notDelete">
		<!-- Body -->
		                    <xsl:choose>
		                        <xsl:when test="$count &gt; 1">Event
		                        <strong>
		                            <em>
		                                <xsl:value-of select="delete/@title" />
		                            </em>
		                        </strong>
		                        appears in more than one calendars. Select which ones you wish to delete:
		                        <!--UniAcc: Layout Table -->
		                        <table border="0" width="100%" cellpadding="0" class="uportal-channel-text">
		                            <xsl:for-each select="delete">
		                                <xsl:if test="not(@invitation)">
		                                    <tr>
		                                        <td>
		                                            <xsl:choose>
		                                                <xsl:when test="@right!='read-only' or not(@right)">
		                                                <input type="checkbox" name="{@calid}" id="CDA-fromCalenderC1"/>
		                                                <label for="CDA-fromCalenderC1">Event from calendar&#160;</label> 
														<xsl:text>
														'
														</xsl:text>
		                                                <xsl:value-of select="@calname" />
														<xsl:text>
														'
														</xsl:text>
		                                                </xsl:when>
		                                                <xsl:otherwise>
		                                                <img src="{$SPACER}" height="6" width='4' alt="" title="" />
		                                                <img src="{$baseImagePath}/check_disable_12.gif" alt="disabled checkbox" title="disabled checkbox" />
		                                                &#160;Event from calendar&#160; 
														<xsl:text>
														'
														</xsl:text>
		                                                <xsl:value-of select="@calname" />
														<xsl:text>
														'
														</xsl:text>
		                                                &#160;(read only)</xsl:otherwise>
		                                            </xsl:choose>
		                                        </td>
		                                    </tr>
		                                    <xsl:if test="@recur">
		                                        <tr>
		                                            <td>
		                                                <xsl:choose>
		                                                    <xsl:when test="@right!='read-only' or not(@right)">																		&#160;&#160;&#160;&#160;&#160;&#160; 
		                                                    <input type="checkbox" name="past" id="CDA-pastOccurrC1">
		                                                    	<label for="CDA-pastOccurrC1">Also delete past occurrences</label>
		                                                    </input>
		                                                    </xsl:when>
		                                                    <xsl:otherwise>&#160;&#160;&#160;&#160;&#160;&#160; 
		                                                    <img src="{$SPACER}" height="6" width='4' alt="" title="" />
		                                                    <img src="{$baseImagePath}/check_disable_12.gif" alt="disabled checkbox" title="disabled checkbox" />
		                                                    &#160;Also delete past occurrences</xsl:otherwise>
		                                                </xsl:choose>
		                                            </td>
		                                        </tr>
		                                        <tr>
		                                            <td>
		                                                <xsl:choose>
		                                                    <xsl:when test="@right!='read-only' or not(@right)">																		&#160;&#160;&#160;&#160;&#160;&#160; 
		                                                    <input type="checkbox" name="future" id="CDA-futureOccurrC1">
		                                                    	<label for="CDA-futureOccurrC1">Also delete future occurrences?</label>
		                                                    </input>
		                                                    </xsl:when>
		                                                    <xsl:otherwise>&#160;&#160;&#160;&#160;&#160;&#160; 
		                                                    <img src="{$SPACER}" height="6" width='4' alt="" title="" />
		                                                    <img src="{$baseImagePath}/check_disable_12.gif" alt="disabled checkbox" title="disabled checkbox" />
		                                                    &#160;Also delete future occurrences?</xsl:otherwise>
		                                                </xsl:choose>
		                                            </td>
		                                        </tr>
		                                    </xsl:if>
		                                </xsl:if>
		                            </xsl:for-each>
		                            <xsl:for-each select="delete">
		                                <xsl:if test="@invitation">
		                                    <tr>
		                                        <td>
		                                            <xsl:choose>
		                                                <xsl:when test="@right!='read-only' or not(@right)">&#160;&#160; 
		                                                <input type="checkbox" name="{@calid}" id="CDA-InviteAcceptedC1"/>
		                                                	<label for="CDA-InviteAcceptedC1">Invitation accepted in calendar&#160;</label>
														<xsl:text>
														'
														</xsl:text>
		                                                <xsl:value-of select="@calname" />
														<xsl:text>
														'
														</xsl:text>
		                                                </xsl:when>
		                                                <xsl:otherwise>&#160;&#160;
		                                                <img src="{$SPACER}" height="6" width='4' alt="" title="" />
		                                                <img src="{$baseImagePath}/check_disable_12.gif" alt="disabled checkbox" title="disabled checkbox" />
		                                                &#160;Invitation accepted in calendar&#160; 
														<xsl:text>
														'
														</xsl:text>
		                                                <xsl:value-of select="@calname" />
														<xsl:text>
														'
														</xsl:text>
		                                                &#160;(read only)</xsl:otherwise>
		                                            </xsl:choose>
		                                        </td>
		                                    </tr>
		                                </xsl:if>
		                            </xsl:for-each>
		                        </table>
		                        </xsl:when>
		                        <xsl:otherwise>
		                            <xsl:apply-templates select="delete" />
		                        </xsl:otherwise>
		                    </xsl:choose>
		                </xsl:when>
		                <xsl:otherwise>This event has been deleted: 
		                <strong>
		                    <em>
		                        <xsl:value-of select="@title" />
		                    </em>
		                </strong>
		                </xsl:otherwise>
		            </xsl:choose>
                </td>
            </tr>

			<xsl:if test="delete/@event='true'">
                <tr>
                    <td class="table-nav">
                        <strong>Notify Attendees</strong>
                        &#160; 
                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                        <input type="checkbox" class="radio" name="notification" id="CDA-NotificationC1"/>
                        <label for="CDA-NotificationC1">&#160;Notification</label>
                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
                        <input type="checkbox" class="radio" name="email" id="CDA-EmalC1"/>
                        <label for="CDA-EmalC1">&#160;Email</label>
                    </td>
                </tr>
            </xsl:if>
            <tr>
                <td class="table-nav">
                    <input type="hidden" name="sid">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$sid" />
                        </xsl:attribute>
                    </input>
                    <xsl:if test="$notDelete">
                        <input class="uportal-button" type="submit" name="do~ok" value="OK" />
                    </xsl:if>
                    <input type="hidden" name="window">
                        <xsl:attribute name="value">
                            <xsl:value-of select="$window" />
                        </xsl:attribute>
                    </input>
                    <input class="uportal-button" type="submit" name="go~{$back}" value="Cancel" />
                </td>
            </tr>
        </table>
    </form>
    </xsl:template>
    <xsl:template name="delete-one" match="/calendar-system/delete">
    <input type='hidden' name='{@calid}' value='on' />
    <span class="uportal-channel-warning">Are you sure you want to delete 
        <span style="color:#000000;">
            <xsl:value-of select="@title" />
        </span>
    ?</span>
    <xsl:choose>
        <xsl:when test="@invitation">
            <xsl:if test="@event">
            <strong>
                <em>
                    <xsl:value-of select="@title" />
                </em>
            </strong>
            is an accepted invitation.</xsl:if>
        </xsl:when>
        <xsl:otherwise>
            <xsl:if test="@recur">
                <xsl:choose>
                    <xsl:when test="@event">
                    <strong>
                        <em>
                            <xsl:value-of select="@title" />
                        </em>
                    </strong>
                    is a recurrent event. You can select to also delete its past and future occurences.</xsl:when>
                    <xsl:otherwise>
                    <strong>
                        <em>
                            <xsl:value-of select="@title" />
                        </em>
                    </strong>
                    is a recurrent task. You can select to also delete its past and future occurences.</xsl:otherwise>
                </xsl:choose>
            </xsl:if>
            <xsl:if test="@recur">&#160;&#160; 
            <input type="checkbox" name="past" id="CDA-deletePastC2">
            	<label for="CDA-deletePastC2">Also delete past occurrences?</label>
            </input>
            </xsl:if>
            <xsl:if test="@recur">&#160;&#160; 
            <input type="checkbox" name="future" id="CDA-deleteFutureC2">
            	<label for="CDA-deleteFutureC2">Also delete future occurrences</label>
            </input>
            </xsl:if>
        </xsl:otherwise>
    </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

