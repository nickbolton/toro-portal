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
<!--variable/params-->
    <xsl:variable name="back" select="/calendar-system/view/@back" />
    <xsl:variable name="windowVar" select="/calendar-system/view/@window" />
    <xsl:variable name="notDelete" select="/calendar-system/delete/@ceid" />
    
    <xsl:template match="calendar-system">
        <xsl:apply-templates select="delete" />
    </xsl:template>
    
    <xsl:template match="delete">
        <form method="post" action="{$baseActionURL}">        
            <input type="hidden" name="sid" value="{$sid}" />
<!-- Caption Table -->
			<!--UniAcc: Layout Table -->
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <th class="th-top">Delete Confirmation</th>
                </tr>
                <tr>
                    <td class="table-content-single" style="text-align:center;">
                        <xsl:choose>
                            <xsl:when test="$notDelete">
<!-- Body -->
                                <span class="uportal-channel-warning">Are you sure you want to delete 
	                                <span style="color:#000000;">
	                                    <xsl:value-of select="@title" />
	                                </span>
                                ?</span>
                                <xsl:choose>
                                    <xsl:when test='@invitation'>
                                        <xsl:if test="$windowVar='event'">
                                        <strong>
                                            <em>
                                                <xsl:value-of select="@title" />
                                            </em>
                                        </strong>
                                        is an accepted invitation.</xsl:if>
                                        <input type='hidden' name="past" value='true' />
                                        <input type='hidden' name="future" value='true' />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:if test="@recur">
                                        	<br/>
                                            <xsl:choose>
                                                <xsl:when test="$windowVar='event'">
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
                                        <br/>
                                        <xsl:if test="@recur">
                                            <input type="checkbox" class="radio" name="past" id="CD-deletePastC1">
                                            	<label for="CD-deletePastC1">Also delete past occurrences?</label>
                                            </input>
                                        </xsl:if>
                                        <xsl:if test="@recur">
                                        	<br/>
                                            <input type="checkbox" class="radio" name="future" id="CD-deleteFutureC1">
                                            	<label for="CD-deleteFutureC1">Also delete future occurrences?</label>
                                            </input>
                                        </xsl:if>
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
                <xsl:if test="$windowVar='event'">
                    <tr>
                        <td class="table-nav">
	                        <strong>Notify Attendees</strong>
	                        &#160; 
	                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
	                        <input type="checkbox" class="radio" name="notification" id="CD-NotificationC1"/>
	                        <label for="CD-NotificationC1">&#160;Notification</label>
	                        <img src="{$SPACER}" border="0" width="5" alt="" title="" />
	                        <input type="checkbox" class="radio" name="email" id="CD-EmailC1"/>
	                        <label for="CD-EmailC1">&#160;Email</label>
                        </td>
                    </tr>
                </xsl:if>
<!--</xsl:if>-->
                <tr>
                    <td class="table-nav">
                        <xsl:if test="$notDelete">
                            <input class="uportal-button" type="submit" name="do~ok" value="OK" />
                        </xsl:if>
                        <input type="hidden" name="window">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$windowVar" />
                            </xsl:attribute>
                        </input>
                        <input class="uportal-button" type="submit" name="go~{$back}" value="Cancel" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>

