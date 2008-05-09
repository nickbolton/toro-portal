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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
    <xsl:variable name="mini-monthly" select="/calendar-system/mini-monthly" />
    <!--MONTH: Used in the assignation of caption for the calendar table -->
      <xsl:variable name = "MONTH">      
          <xsl:choose>
              <xsl:when test='starts-with($mini-monthly/@date,"1/")'>JANUARY</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"2/")'>FEBRUARY</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"3/")'>MARCH</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"4/")'>APRIL</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"5/")'>MAY</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"6/")'>JUNE</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"7/")'>JULY</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"8/")'>AUGUST</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"9/")'>SEPTEMBER</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"10/")'>OCTOBER</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"11/")'>NOVEMBER</xsl:when>
              <xsl:when test='starts-with($mini-monthly/@date,"12/")'>DECEMBER</xsl:when>
              <xsl:otherwise></xsl:otherwise>
          </xsl:choose>
      </xsl:variable>
    <xsl:template name="mini-monthly">
    	
			<form method="post" action="{$baseActionURL}" id="changeMonthViewForm" name="changeMonthViewForm">
				<!--UniAcc: Layout Table -->
				<table cellspacing="0" cellpadding="0" width="100%" border="0">
					<tr>
						<td align="center">
						
							<xsl:variable name="m">
								<xsl:value-of select="substring-before($mini-monthly/@date,'/')"/>
							</xsl:variable>
							<input type="hidden" name="month" value="{$m}"/>
							<xsl:variable name="y">
								<xsl:value-of select="substring-after(substring-after(substring-before($mini-monthly/@date,'_'),'/'),'/')" />
							</xsl:variable>
							<input type="hidden" name="year" value="20{$y}"/>
							<input type="hidden" name="sid">
								<xsl:attribute name="value">
									<xsl:value-of select="$sid" />
								</xsl:attribute>
							</input>
							<input type="hidden" name="fixChangeMonthView" id="fixChangeMonthView" value="" />							
							<div>
								<table cellpadding="0" cellspacing="0">
									<tr>
										<td nowrap="nowrap" class="calendar-title">
											<a href="javascript:document.getElementById('fixChangeMonthView').name='do~previousDate'; document.getElementById('changeMonthViewForm').submit();" title="Previous">
												<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
											</a>						
											<img height="1" width="4" src="{$SPACER}" border="0" alt="" title="" />
											

											<xsl:value-of select="$MONTH" /> 20<xsl:value-of select="$y"/>	

											<img height="1" width="4" src="{$SPACER}" border="0" alt="" title="" />
											<a href="javascript:document.getElementById('fixChangeMonthView').name='do~nextDate'; document.getElementById('changeMonthViewForm').submit();" title="Next">
												<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
											</a>
										</td>
									</tr>
								</table>
							</div>							
							<!--
							<div class="calendar-title">
								<a href="javascript:document.getElementById('fixChangeMonthView').name='do~previousDate'; document.getElementById('changeMonthViewForm').submit();" title="Previous">
									<img src="{$baseImagePath}/prev_12.gif" border="0" align="absmiddle" alt="Previous" title="Previous" />
								</a>
								
								<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
								<xsl:value-of select="$MONTH" /> 20<xsl:value-of select="$y"/>
								<img height="1" width="5" src="{$SPACER}" border="0" alt="" title="" />
								
								<a href="javascript:document.getElementById('fixChangeMonthView').name='do~nextDate'; document.getElementById('changeMonthViewForm').submit();" title="Next">
									<img src="{$baseImagePath}/next_12.gif" border="0" align="absmiddle" alt="Next" title="Next" />
								</a>							
							</div>
							-->
							<xsl:call-template name="mini-monthly-table">
								<xsl:with-param name="mini-monthly-class">mini-monthly-27</xsl:with-param>
							</xsl:call-template>
							
						</td>
					</tr>
				</table>
			</form>
    </xsl:template>
    
    <xsl:template name="mini-monthly-table">
    	<xsl:param name="mini-monthly-class">mini-monthly-27</xsl:param>
    	
		<table cellspacing="1" cellpadding="0" border="0" class="{$mini-monthly-class}">	
			<tr>
				<th class="weekday-title" scope="col">
					Su
				</th>
				<th class="weekday-title" scope="col">
					Mo
				</th>
				<th class="weekday-title" scope="col">
					Tu
				</th>
				<th class="weekday-title" scope="col">
					We
				</th>
				<th class="weekday-title" scope="col">
					Th
				</th>
				<th class="weekday-title" scope="col">
					Fr
				</th>
				<th class="weekday-title" scope="col">
					Sa
				</th>
			</tr>
		<!-- Fill days of month -->
			<xsl:call-template name="mini-monthly-week">
				<xsl:with-param name="dom" select="1 - $mini-monthly/@wdo1" />
			</xsl:call-template>
			<xsl:call-template name="mini-monthly-week">
				<xsl:with-param name="dom" select="8 - $mini-monthly/@wdo1" />
			</xsl:call-template>
			<xsl:call-template name="mini-monthly-week">
				<xsl:with-param name="dom" select="15 - $mini-monthly/@wdo1" />
			</xsl:call-template>
			<xsl:call-template name="mini-monthly-week">
				<xsl:with-param name="dom" select="22 - $mini-monthly/@wdo1" />
			</xsl:call-template>
			<xsl:call-template name="mini-monthly-week">
				<xsl:with-param name="dom" select="29 - $mini-monthly/@wdo1" />
			</xsl:call-template>
			<xsl:if test="($mini-monthly/@ldom &gt; 30 and $mini-monthly/@wdo1 &gt;= 5) or $mini-monthly/@wdo1 &gt;= 6">
				<xsl:call-template name="mini-monthly-week">
					<xsl:with-param name="dom" select="36 - $mini-monthly/@wdo1" />
				</xsl:call-template>
			</xsl:if>
			<tr>
				<td colspan="8" class="th-calendar" align="center">
					<p style="margin-top: 8px;">
						<xsl:choose>
							<xsl:when test="/calendar-system/mini-entry">
								<a href="{$mdoURL}=update&amp;op=d~{/calendar-system/mini-entry/@date}&amp;calid={$logon-user}&amp;window=event" class="calendar" title="Display day">
									Today's date is <xsl:value-of select="substring-before(/calendar-system/mini-entry/@date,'_')" />
								</a>
							</xsl:when>
							<xsl:when test="/calendar-system/mini-daily">
								<a href="{$mdoURL}=update&amp;op=d~{/calendar-system/mini-daily/@date}&amp;calid={$logon-user}&amp;window=event" class="calendar" title="Display day">
									Today's date is <xsl:value-of select="substring-before(/calendar-system/mini-daily/@date,'_')" />
								</a>
							</xsl:when>
							<xsl:otherwise>
								<a href="{$mdoURL}=update&amp;op=d~{/calendar-system/mini-monthly/@date}&amp;calid={$logon-user}&amp;window=event" class="calendar" title="Display day">
									Today's date is <xsl:value-of select="substring-before(/calendar-system/mini-monthly/@date,'_')" />
								</a>
							</xsl:otherwise>
						</xsl:choose>
					</p>
				</td>
			</tr>
		</table>    
    </xsl:template>
    
    
<!-- Fill days of week -->
    <xsl:template name="mini-monthly-week">
        <xsl:param name="dom" />
        <xsl:variable name="m" select='substring-before($mini-monthly/@date,"/")' />
        <xsl:variable name="d" select='substring-before(substring-after($mini-monthly/@date,"/"),"/")' />
        <xsl:variable name="y" select='substring-after(substring-after($mini-monthly/@date,"/"),"/")' />
        
        <tr align="center">
            <!--
            <td align="left" class="th-calendar">

                <xsl:variable name="fdow">
                    <xsl:choose>
                        <xsl:when test="$dom &gt; 0 and $mini-monthly/@ldom &gt;= $dom">
                            <xsl:value-of select='concat($m,"/",$dom,"/",$y)' />
                        </xsl:when>
                        <xsl:when test="1 + $dom &gt; 0 and $mini-monthly/@ldom &gt;= 1+ $dom">
                            <xsl:value-of select='concat($m,"/", 1 + $dom,"/",$y)' />
                        </xsl:when>
                        <xsl:when test="2 + $dom &gt; 0 and $mini-monthly/@ldom &gt;= 2+ $dom">
                            <xsl:value-of select='concat($m,"/", 2 + $dom,"/",$y)' />
                        </xsl:when>
                        <xsl:when test="3 + $dom &gt; 0 and $mini-monthly/@ldom &gt;= 3+ $dom">
                            <xsl:value-of select='concat($m,"/", 3 + $dom,"/",$y)' />
                        </xsl:when>
                        <xsl:when test="4 + $dom &gt; 0 and $mini-monthly/@ldom &gt;= 4+ $dom">
                            <xsl:value-of select='concat($m,"/", 4 + $dom,"/",$y)' />
                        </xsl:when>
                        <xsl:when test="5 + $dom &gt; 0 and $mini-monthly/@ldom &gt;= 5+ $dom">
                            <xsl:value-of select='concat($m,"/", 5 + $dom,"/",$y)' />
                        </xsl:when>
                        <xsl:when test="6 + $dom &gt; 0 and $mini-monthly/@ldom &gt;= 6+ $dom">
                            <xsl:value-of select='concat($m,"/", 6 + $dom,"/",$y)' />
                        </xsl:when>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$calid">
                        <a href="{$mdoURL}=update&amp;op=w~{$fdow}&amp;calid={$calid}&amp;window=event" title="">
                            <img border="0" src="{$SPACER}" alt="" title="" />
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_weekview.gif" alt="View Weekly" title="View Weekly" />
                            <img border="0" src="{$SPACER}" alt="" title="" />
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="{$mdoURL}=update&amp;op=w~{$fdow}&amp;calid={$logon-user}&amp;window=event" title="">
                            <img border="0" src="{$SPACER}" alt="" title="" />
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/calendar_weekview.gif" alt="View Weekly" title="View Weekly" />
                            <img border="0" src="{$SPACER}" alt="" title="" />
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            -->
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="$dom" />
                <xsl:with-param name="dow">1</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="1 + $dom" />
            </xsl:call-template>
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="2 + $dom" />
            </xsl:call-template>
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="3 + $dom" />
            </xsl:call-template>
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="4 + $dom" />
            </xsl:call-template>
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="5 + $dom" />
            </xsl:call-template>
            <xsl:call-template name="mini-monthly-day">
                <xsl:with-param name="dom" select="6 + $dom" />
                <xsl:with-param name="dow" select="7" />
            </xsl:call-template>
        </tr>
    </xsl:template>
<!-- Day -->
    <xsl:template name="mini-monthly-day">
        <xsl:param name="dom" />
        <xsl:param name="dow" />
        <xsl:variable name="m" select='substring-before($mini-monthly/@date,"/")' />
        <xsl:variable name="d" select='substring-before(substring-after($mini-monthly/@date,"/"),"/")' />
        <xsl:variable name="y" select='substring-after(substring-after($mini-monthly/@date,"/"),"/")' />
        <xsl:variable name="yy" select='substring-before(substring-after(substring-after($mini-monthly/@date,"/"),"/"),"_")' />
        <xsl:variable name="cur_m" select='substring-before($mini-daily/@date,"/")' />
        <xsl:variable name="cur_d" select='substring-before(substring-after($mini-daily/@date,"/"),"/")' />
        <xsl:variable name="cur_yy" select='substring-before(substring-after(substring-after($mini-daily/@date,"/"),"/"),"_")' />
        <xsl:variable name="hightlight">
            <xsl:choose>
                <xsl:when test="$dom &gt; 0 and $mini-monthly/@ldom &gt;= $dom">
                    <xsl:choose>
                        <xsl:when test="$cur_yy=$yy and $cur_m=$m and $cur_d=$dom">
							current-day
                        </xsl:when>
                        <xsl:otherwise>
                            weekday
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    not-a-day
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <td align="center" class="{$hightlight} mini-monthly-cell">

            <xsl:choose>
                <xsl:when test="$dom &gt; 0 and $mini-monthly/@ldom &gt;= $dom">
                    <xsl:choose>
                        <xsl:when test="$calid">
                            <xsl:choose>
                                <xsl:when test="$cur_yy=$yy and $cur_m=$m and $cur_d=$dom">
                                    <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$calid}&amp;window=event" class="calendar-event" title="Display day">
                                        <xsl:value-of select="$dom" />
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$calid}&amp;window=event" class="calendar" title="Display day">
                                        <xsl:value-of select="$dom" />
                                    </a>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="$cur_yy=$yy and $cur_m=$m and $cur_d=$dom">
                                    <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$logon-user}&amp;window=event" class="calendar-event" title="Display day">
                                        <xsl:value-of select="$dom" />
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <a href="{$mdoURL}=update&amp;op=d~{concat($m,'/',$dom,'/',$y)}&amp;calid={$logon-user}&amp;window=event" class="calendar" title="Display day">
                                        <xsl:value-of select="$dom" />
                                    </a>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <img border="0" src="{$SPACER}" alt="" title="" />
                </xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>
</xsl:stylesheet>

