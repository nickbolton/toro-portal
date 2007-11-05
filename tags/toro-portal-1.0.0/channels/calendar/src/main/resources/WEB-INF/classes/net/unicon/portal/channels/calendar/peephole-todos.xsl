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
<!-- Include -->
    <xsl:include href="mini-todo.xsl" />
<!-- will need to add privileges-->
    <xsl:key name="calhidden" match="/calendar-system/preference/calendar-hidden" use="@calid" />
<!-- /////////////////////////////////////////////// -->
<!-- peephole Todos -->
    <xsl:template name="Todos">
        <xsl:call-template name="todoLinks" />
        <!--UniAcc: Layout Table -->
        <form method="post" action="{$baseActionURL}">
            <input type="hidden" name="sid">
                <xsl:attribute name="value">
                    <xsl:value-of select="$sid" />
                </xsl:attribute>
            </input>
            <xsl:call-template name="detail-todo-date" />
        </form>
    </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
    <xsl:template name="todoLinks">
    	<div class="task-box">
    		<!--UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <td align="center" class="calendar-text-sm" nowrap="nowrap">
						<form method="post" action="{$baseActionURL}" name="changeCalendarTodo">
                        <span align="left">
                        	<b>&#160;Calendar:&#160;</b><br/>
							<select name="calid" size="1" id="CPE-ToDoS1" onchange="document.changeCalendarTodo.cal.name='do~update';javascript:document.changeCalendarTodo.submit()">
								<option value="all-calendars">All calendars</option>
								<xsl:for-each select="//calendar-system/preference/calendar-group">
									<option value="composite_{@id}">
										<xsl:variable name="gid">
											<xsl:value-of select="concat('composite_',@id)" />
										</xsl:variable>
										<xsl:if test="//calendar-system/view/calendar/@calid = $gid">
											<xsl:attribute name="selected" />
										</xsl:if>
										<xsl:value-of select="concat(@name,' composite view')" />
									</option>
								</xsl:for-each>
								<xsl:for-each select="calendar-system/calendar[@owner=$cur_user]">
									<xsl:sort select='@calname' />
									<xsl:choose>
										<xsl:when test="@calid=//calendar-system/view/calendar/@calid">
											<option selected="selected">
												<xsl:attribute name="value">
													<xsl:value-of select="@calid" />
												</xsl:attribute>
												<xsl:value-of select="@calname" />
											</option>
										</xsl:when>
										<xsl:otherwise>
											<option>
												<xsl:attribute name="value">
													<xsl:value-of select="@calid" />
												</xsl:attribute>
												<xsl:value-of select="@calname" />
											</option>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
								<xsl:for-each select="calendar-system/calendar[@owner!=$cur_user and not(key('calhidden', @calid))]">
									<xsl:sort select="@owner" />
									<xsl:choose>
										<xsl:when test="@calid=//calendar-system/view/calendar/@calid">
											<option selected="selected">
												<xsl:attribute name="value">
													<xsl:value-of select="@calid" />
												</xsl:attribute>
												<xsl:value-of select="@calname" />
											</option>
										</xsl:when>
										<xsl:otherwise>
											<option>
												<xsl:attribute name="value">
													<xsl:value-of select="@calid" />
												</xsl:attribute>
												<xsl:value-of select="@calname" />
											</option>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</select>
						</span>
                        <input type="hidden" name="sid">
                            <xsl:attribute name="value">
                                <xsl:value-of select="$sid" />
                            </xsl:attribute>
                        </input>
                        <input type="hidden" name="cal" value="" />
						</form>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>
    <xsl:template name="detail-todo-date">
<!-- All calendars of logon user -->
		<!--UniAcc: Data Table -->
		<div class="event-box">
			<table width="100%" cellspacing="0" border="0" cellpadding="0">
				<xsl:if test="count(/calendar-system/view/todo) = 0">
					<tr>
						<td colspan="2" class="table-content-single">There are currently no Tasks.</td>
					</tr>
				</xsl:if>
				<xsl:for-each select="/calendar-system/view/todo">
	<!-- entries of calendar -->
					<xsl:variable name="view-calid" select='@calid' />
					<xsl:variable name="everyone-share-write">
						<xsl:for-each select="/calendar-system/calendar[@calid=$view-calid]">
							<xsl:for-each select="ace">
								<xsl:choose>
									<xsl:when test="@cuid='@'">
										<xsl:value-of select="@write" />
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:for-each>
					</xsl:variable>
					<xsl:variable name="user-share-write">
						<xsl:for-each select="/calendar-system/calendar[@calid=$view-calid]">
							<xsl:for-each select="ace">
								<xsl:choose>
									<xsl:when test="@cuid=$logon-user">
										<xsl:value-of select="@write" />
									</xsl:when>
								</xsl:choose>
							</xsl:for-each>
						</xsl:for-each>
					</xsl:variable>
					<xsl:variable name="owner">
						<xsl:for-each select="/calendar-system/calendar[@calid=$view-calid]">
							<xsl:choose>
								<xsl:when test="@owner=$logon-user">true</xsl:when>
								<xsl:otherwise>false</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:variable>
					<xsl:variable name="right" select="key('access',$view-calid)" />
					<xsl:variable name="todo-last">
						<xsl:choose>
							<xsl:when test="position() = last()">yes</xsl:when>
							<xsl:otherwise>no</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:call-template name="entry-todo-daily">
						<xsl:with-param name="calid" select="@calid" />
						<xsl:with-param name="todo-last" select="$todo-last" />
						<xsl:with-param name="everyone-share-write" select='$everyone-share-write' />
						<xsl:with-param name="user-share-write" select='$user-share-write' />
						<xsl:with-param name="right" select='$right' />
						<xsl:with-param name="owner" select='$owner' />
					</xsl:call-template>
				</xsl:for-each>
			</table>
		</div>
    </xsl:template>
    
<!-- Daily entries of calendar, context node is "/calendar-system/calendar" -->
    <xsl:template name="entry-todo-daily">
        <xsl:param name="calid" />
        <xsl:param name="todo-last" />
        <xsl:param name="everyone-share-write" />
        <xsl:param name="user-share-write" />
        <xsl:param name="right" />
        <xsl:param name="owner" />
        <tr>

		<!-- Priority, title -->
            <td align="center" valign="top" style="padding:2px;">
                <xsl:call-template name="priority">
                    <xsl:with-param name="cur_node">todo</xsl:with-param>
                </xsl:call-template>
 			</td>
 			<td nowrap="nowrap" align="left" valign="top" width="100%" class="calendar-text">
                <a href="{$mgoURL}=Todo&amp;ceid={@ceid}&amp;calid={$calid}" title="Edit Task">
                    <xsl:choose>
                        <xsl:when test="@title">
                            <xsl:value-of select="@title" />
                        </xsl:when>
                        <xsl:otherwise>Untitled</xsl:otherwise>
                    </xsl:choose>
                </a>
                <xsl:if test="$owner='true' or $user-share-write='true' or $everyone-share-write='true' or contains($right/@rights,'W')">
 					<a href="{$mgoURL}=Delete&amp;ceid={@ceid}&amp;calid={$calid}&amp;window=todo" title="Delete Task">
                        <img border="0" src="{$SPACER}" width="3" alt="" title="" />
                        <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_active.gif" align="absmiddle" name="calendarTodoDeleteImage{@ceid}{$channelID}" id="calendarTodoDeleteImage{@ceid}{$channelID}" alt="Delete" title="Delete" />
                    </a>
                </xsl:if>
            
<!-- Start time -->
            	<div>
					<!-- &#160;&#160; -->
					<xsl:value-of select="substring-before(@start,'_')" />
					&#160;&#160;
					<xsl:call-template name="t24to12">
						<xsl:with-param name="hour" select='substring-after(@start,"_")' />
					</xsl:call-template>
				</div>
            </td>
		<!-- completion (todo) -->
		<!--
            <td align="left" valign="top" class="table-content" headers="CPT-Complete">
                <xsl:if test="$owner='true' or $everyone-share-write='true' or $user-share-write='true' or contains($right/@rights,'W')">
                    <input type="checkbox" class="radio" id="CPT-ClearC{@ceid}">
                        <xsl:attribute name="name">
                            <xsl:value-of select="concat('chk:',@ceid,'@',$calid)" />
                        </xsl:attribute>
                    </input>
                    <label for="CPT-ClearC{@ceid}">Clear To-do</label>
                </xsl:if>
            </td>
        
        <xsl:if test="$todo-last='yes'">
            <tr>
                <td class="table-nav" colspan="3">
                    <input type="submit" class="uportal-button" name="do~complete" border="0" title="Apply" value="Apply Completions" />
                </td>
            </tr>
        </xsl:if>
        -->
        </tr>
    </xsl:template>
<!-- /////////////////////////////////////////////////////////////////////////////////////////////// -->
<!-- background for todos past due -->
    <xsl:template name="pasted">
        <xsl:param name="cur_date">3/14/02_18:30</xsl:param>
        <xsl:param name="date">8/25/78_06:30</xsl:param>
<!-- the analysis of date -->
        <xsl:variable name="yy">
            <xsl:value-of select="substring-before(substring-after(substring-after($date,'/'),'/'),'_')" />
        </xsl:variable>
        <xsl:variable name="dd">
            <xsl:value-of select="substring-before(substring-after($date,'/'),'/')" />
        </xsl:variable>
        <xsl:variable name="mm">
            <xsl:value-of select="substring-before($date,'/')" />
        </xsl:variable>
        <xsl:variable name="hh">
            <xsl:value-of select="substring-before(substring-after($date,'_'),':')" />
        </xsl:variable>
        <xsl:variable name="mn">
            <xsl:value-of select="substring-after(substring-after($date,'_'),':')" />
        </xsl:variable>
<!-- the analysis of current date -->
        <xsl:variable name="cur_yy">
            <xsl:value-of select="substring-before(substring-after(substring-after($cur_date,'/'),'/'),'_')" />
        </xsl:variable>
        <xsl:variable name="cur_dd">
            <xsl:value-of select="substring-before(substring-after($cur_date,'/'),'/')" />
        </xsl:variable>
        <xsl:variable name="cur_mm">
            <xsl:value-of select="substring-before($cur_date,'/')" />
        </xsl:variable>
        <xsl:variable name="cur_hh">
            <xsl:value-of select="substring-before(substring-after($cur_date,'_'),':')" />
        </xsl:variable>
        <xsl:variable name="cur_mn">
            <xsl:value-of select="substring-after(substring-after($cur_date,'_'),':')" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$cur_yy = $yy">
                <xsl:choose>
                    <xsl:when test="$cur_mm = $mm">
                        <xsl:choose>
                            <xsl:when test="$cur_dd = $dd">
                                <xsl:choose>
                                    <xsl:when test="$cur_hh = $hh">
                                        <xsl:choose>
                                            <xsl:when test="$cur_mn &gt;=$mn">
												uportal-background-light
                                            </xsl:when>
                                            <xsl:otherwise>
												uportal-background-content
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$cur_hh &gt; $hh">
										uportal-background-light
                                    </xsl:when>
                                    <xsl:otherwise>
										uportal-background-content
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:when test="$cur_dd &gt; $dd">
								uportal-background-light
                            </xsl:when>
                            <xsl:otherwise>
								uportal-background-content
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="$cur_mm &gt; $mm">
						uportal-background-light
                    </xsl:when>
                    <xsl:otherwise>
						uportal-background-content
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$cur_yy &gt; $yy">
				uportal-background-light
            </xsl:when>
            <xsl:otherwise>
				uportal-background-content
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

