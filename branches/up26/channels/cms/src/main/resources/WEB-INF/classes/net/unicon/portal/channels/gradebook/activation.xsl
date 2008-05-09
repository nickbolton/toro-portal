<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
<xsl:include href="common.xsl" />

<xsl:variable name = "GRADEBOOK-ITEM" select = "/gradebooks/gradebook-item[@id = $gradebookItemID]" />
<xsl:variable name = "ACTIVATION_TYPE" select = "$GRADEBOOK-ITEM/@type" />

<xsl:template match="/">
    <!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>
    </textarea>
    -->

    <xsl:call-template name="links" />
    <xsl:call-template name="autoFormJS" />

    <form name="gradebookForm" action="{$baseActionURL}" enctype='multipart/form-data' method="post" onsubmit="return validator.applyFormRules(this, new GradebookRulesObject())">
        <table cellpadding="0" cellspacing="0" border="0">
            <tr>
                <th colspan="2" class="th-top">
                    <xsl:choose>
                        <xsl:when test="$ACTIVATION_TYPE = 2">
                            Activate Assignment Submittal
                        </xsl:when>

                        <xsl:otherwise>
                             Activate Assessment
                        </xsl:otherwise>
                    </xsl:choose>
                </th>
            </tr>

            <xsl:if test="$ACTIVATION_TYPE != 2">
            <tr>
            <td class="table-light-left" style="text-align:right">
                <!-- NOTE: If we add back the ability to change the assessment association, we'll add link back in -->
                <!--<a href="{$baseActionURL}?command=edit&amp;gradebook_itemID={$gradebookItemID}" title="To edit column entry including the assessment association"
                onmouseover="swapImage('gbEditThisColumnImage','channel_edit_active.gif')"
                onmouseout="swapImage('gbEditThisColumnImage','channel_edit_base.gif')">Assessment Association<img
                height="1" width="3" src="{$SPACER}"
                alt="To edit column entry including the assessment association" title="To edit column entry including the assessment association"
                border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                alt="To edit column entry including the assessment association" title="To edit column entry including the assessment association"
                align="absmiddle" name="gbEditThisColumnImage" id="gbEditThisColumnImage"/></a> -->
                Assessment Association
            </td>

            <td class="table-content-right">
                <xsl:choose>
                    <xsl:when test="$GRADEBOOK-ITEM/@association != 'null'">
                        <xsl:value-of select="$GRADEBOOK-ITEM/assessment/title" />
                        <input type="hidden" name="association" value="{$GRADEBOOK-ITEM/@association}"></input>
                   </xsl:when>

                    <xsl:otherwise>
                         None (or N/A)
                    </xsl:otherwise>
                </xsl:choose>
            </td>

            </tr>
            </xsl:if>
            <tr>
                <td class="table-light-left" style="text-align:right">Column Name</td>
                <td class="table-content-right"><xsl:value-of select="$GRADEBOOK-ITEM/title"/></td>
            </tr>

            <tr>
                <td class="table-light-left" style="text-align:right">Activation Link</td>
                <td class="table-content-right"><xsl:choose>
                    <xsl:when test="$ACTIVATION_TYPE = 2">
                        <ul>
                            <li><input type="radio" name="entryLink" value="none" checked="checked"/> No File to Link</li>
                            <li><input type="radio" name="entryLink" value="url"/> Link to URL <input type="text" class="text" size="40" value="" name="entryURL" /></li>
                            <!-- Show Submit button only if they have something to submit (i.e. score, assignment, or feedback) -->
                            <li><input type="radio" name="entryLink" value="file" /> Uploaded File <input type="file" style="text-align: left;" name="uploadedFile" /></li>
                        </ul>
                    </xsl:when>

                    <xsl:otherwise>
                    To the online assessment system
                    </xsl:otherwise>
                </xsl:choose></td>
            </tr>

            <xsl:if test = "$ACTIVATION_TYPE = 1">
            <tr>
                <td class="table-light-left" style="text-align:right">Form Selection<xsl:for-each
                select = "$GRADEBOOK-ITEM/assessment/form">
                <input type="hidden" name="{@id}_max" value="{attributes/attribute[@name='maxScore']/value}"/>
                <input type="hidden" name="{@id}_min" value="{attributes/attribute[@name='minScore']/value}"/>
                </xsl:for-each></td>
                <xsl:choose>
                    <xsl:when test="count($GRADEBOOK-ITEM/assessment/form) &gt; 1">
                        <td class="table-content-right"><select name="assessment_form">
                            <xsl:for-each select = "$GRADEBOOK-ITEM/assessment/form">
                                <option value="{@id}"><xsl:choose><xsl:when test="string-length(normalize-space (./title)) &gt; 0"><xsl:value-of select="./title" /></xsl:when><xsl:otherwise><xsl:value-of select="@id" /></xsl:otherwise></xsl:choose></option>
                            </xsl:for-each>
                        </select></td>
                    </xsl:when>

                      <xsl:when test = "count($GRADEBOOK-ITEM/assessment/form) = 1">
                         <td class="table-content-right">
                             <xsl:if test="$GRADEBOOK-ITEM/assessment/form/title = ''">
                             No Title (or N/A)
                             </xsl:if>
                             <xsl:value-of select="$GRADEBOOK-ITEM/assessment/form/title" /> -
                             <xsl:value-of select="$GRADEBOOK-ITEM/assessment/form/description" />
                             <input type="hidden" name="assessment_form" value="{$GRADEBOOK-ITEM/assessment/form/@id}" />
                         </td>
                      </xsl:when>

                    <xsl:otherwise>
                         <td class="table-content-right"><img height="1" width="3" src=
                        "{$SPACER}"
                        alt="" border="0"/></td>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
            </xsl:if>

            <tr>
                <td class="table-light-left" style="text-align:right">Activation Period</td>
                <td class="table-content-right"><ul>
                    <li>Start Time: <select name="hourStart">
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                            <option>6</option>
                            <option>7</option>
                            <option>8</option>
                            <option selected="selected">9</option>
                            <option>10</option>
                            <option>11</option>
                            <option>12</option>
                        </select> :<select name="minuteStart">
                            <option selected="selected">00</option>
                            <option>05</option>
                            <option>10</option>
                            <option>15</option>
                            <option>20</option>
                            <option>25</option>
                            <option>30</option>
                            <option>35</option>
                            <option>40</option>
                            <option>45</option>
                            <option>50</option>
                            <option>55</option>
                        </select>
                        <select name="ampmStart">
                            <option selected="selected">AM</option>
                            <option>PM</option>
                        </select><br/>Start Date:
                        <select name="monthStart">
                            <option value="1"><xsl:if test = "$currentMonth = 1"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Jan.</option>
                            <option value="2"><xsl:if test = "$currentMonth = 2"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Feb.</option>
                            <option value="3"><xsl:if test = "$currentMonth = 3"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Mar.</option>
                            <option value="4"><xsl:if test = "$currentMonth = 4"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Apr.</option>
                            <option value="5"><xsl:if test = "$currentMonth = 5"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>May</option>
                            <option value="6"><xsl:if test = "$currentMonth = 6"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Jun.</option>
                            <option value="7"><xsl:if test = "$currentMonth = 7"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Jul.</option>
                            <option value="8"><xsl:if test = "$currentMonth = 8"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Aug.</option>
                            <option value="9"><xsl:if test = "$currentMonth = 9"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Sep.</option>
                            <option value="10"><xsl:if test = "$currentMonth = 10"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Oct.</option>
                            <option value="11"><xsl:if test = "$currentMonth = 11"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Nov.</option>
                            <option value="12"><xsl:if test = "$currentMonth = 12"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Dec.</option>
                        </select>
                        <select name="dayStart">
                            <option value="1"><xsl:if test = "$currentDay = 1"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>1</option>
                            <option value="2"><xsl:if test = "$currentDay = 2"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2</option>
                            <option value="3"><xsl:if test = "$currentDay = 3"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>3</option>
                            <option value="4"><xsl:if test = "$currentDay = 4"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>4</option>
                            <option value="5"><xsl:if test = "$currentDay = 5"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>5</option>
                            <option value="6"><xsl:if test = "$currentDay = 6"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>6</option>
                            <option value="7"><xsl:if test = "$currentDay = 7"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>7</option>
                            <option value="8"><xsl:if test = "$currentDay = 8"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>8</option>
                            <option value="9"><xsl:if test = "$currentDay = 9"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>9</option>
                            <option value="10"><xsl:if test = "$currentDay = 10"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>10</option>
                            <option value="11"><xsl:if test = "$currentDay = 11"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>11</option>
                            <option value="12"><xsl:if test = "$currentDay = 12"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>12</option>
                            <option value="13"><xsl:if test = "$currentDay = 13"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>13</option>
                            <option value="14"><xsl:if test = "$currentDay = 14"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>14</option>
                            <option value="15"><xsl:if test = "$currentDay = 15"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>15</option>
                            <option value="16"><xsl:if test = "$currentDay = 16"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>16</option>
                            <option value="17"><xsl:if test = "$currentDay = 17"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>17</option>
                            <option value="18"><xsl:if test = "$currentDay = 18"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>18</option>
                            <option value="19"><xsl:if test = "$currentDay = 19"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>19</option>
                            <option value="20"><xsl:if test = "$currentDay = 20"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>20</option>
                            <option value="21"><xsl:if test = "$currentDay = 21"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>21</option>
                            <option value="22"><xsl:if test = "$currentDay = 22"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>22</option>
                            <option value="23"><xsl:if test = "$currentDay = 23"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>23</option>
                            <option value="24"><xsl:if test = "$currentDay = 24"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>24</option>
                            <option value="25"><xsl:if test = "$currentDay = 25"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>25</option>
                            <option value="26"><xsl:if test = "$currentDay = 26"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>26</option>
                            <option value="27"><xsl:if test = "$currentDay = 27"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>27</option>
                            <option value="28"><xsl:if test = "$currentDay = 28"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>28</option>
                            <option value="29"><xsl:if test = "$currentDay = 29"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>29</option>
                            <option value="30"><xsl:if test = "$currentDay = 30"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>30</option>
                            <option value="31"><xsl:if test = "$currentDay = 31"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>31</option>
                        </select>,
                        <select name="yearStart">
                            <option value="2002"><xsl:if test = "$currentYear = 2002"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2002</option>
                            <option value="2003"><xsl:if test = "$currentYear = 2003"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2003</option>
                            <option value="2004"><xsl:if test = "$currentYear = 2004"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2004</option>
                            <option value="2005"><xsl:if test = "$currentYear = 2005"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2005</option>
                            <option value="2006"><xsl:if test = "$currentYear = 2006"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2006</option>
                            <option value="2007"><xsl:if test = "$currentYear = 2007"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2007</option>
                            <option value="2008"><xsl:if test = "$currentYear = 2008"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2008</option>
                            <option value="2009"><xsl:if test = "$currentYear = 2009"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2009</option>
                            <option value="2010"><xsl:if test = "$currentYear = 2010"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2010</option>
                        </select><br/>
                        <!--or <input name="nowBox" type="checkbox" class="radio" value="now" />Now<br/><br/>-->
                    </li>
                    <li>End Time:&#xA0; <!-- <input name="hourStart" type="text" value="12" size="2" class="text"/> --> <select name="hourEnd">
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                            <option>6</option>
                            <option>7</option>
                            <option>8</option>
                            <option selected="selected">9</option>
                            <option>10</option>
                            <option>11</option>
                            <option>12</option>
                        </select> :<select name="minuteEnd">
                            <option selected="selected">00</option>
                            <option>05</option>
                            <option>10</option>
                            <option>15</option>
                            <option>20</option>
                            <option>25</option>
                            <option>30</option>
                            <option>35</option>
                            <option>40</option>
                            <option>45</option>
                            <option>50</option>
                            <option>55</option>
                        </select>
                        <select name="ampmEnd">
                            <option selected="selected">AM</option>
                            <option>PM</option>
                        </select><br/>End Date:&#xA0;
                        <select name="monthEnd">
                            <option value="1"><xsl:if test = "$currentMonth = 1"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Jan.</option>
                            <option value="2"><xsl:if test = "$currentMonth = 2"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Feb.</option>
                            <option value="3"><xsl:if test = "$currentMonth = 3"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Mar.</option>
                            <option value="4"><xsl:if test = "$currentMonth = 4"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Apr.</option>
                            <option value="5"><xsl:if test = "$currentMonth = 5"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>May</option>
                            <option value="6"><xsl:if test = "$currentMonth = 6"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Jun.</option>
                            <option value="7"><xsl:if test = "$currentMonth = 7"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Jul.</option>
                            <option value="8"><xsl:if test = "$currentMonth = 8"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Aug.</option>
                            <option value="9"><xsl:if test = "$currentMonth = 9"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Sep.</option>
                            <option value="10"><xsl:if test = "$currentMonth = 10"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Oct.</option>
                            <option value="11"><xsl:if test = "$currentMonth = 11"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Nov.</option>
                            <option value="12"><xsl:if test = "$currentMonth = 12"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>Dec.</option>
                        </select>
                        <select name="dayEnd">
                            <option value="1"><xsl:if test = "$currentDay = 1"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>1</option>
                            <option value="2"><xsl:if test = "$currentDay = 2"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2</option>
                            <option value="3"><xsl:if test = "$currentDay = 3"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>3</option>
                            <option value="4"><xsl:if test = "$currentDay = 4"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>4</option>
                            <option value="5"><xsl:if test = "$currentDay = 5"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>5</option>
                            <option value="6"><xsl:if test = "$currentDay = 6"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>6</option>
                            <option value="7"><xsl:if test = "$currentDay = 7"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>7</option>
                            <option value="8"><xsl:if test = "$currentDay = 8"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>8</option>
                            <option value="9"><xsl:if test = "$currentDay = 9"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>9</option>
                            <option value="10"><xsl:if test = "$currentDay = 10"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>10</option>
                            <option value="11"><xsl:if test = "$currentDay = 11"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>11</option>
                            <option value="12"><xsl:if test = "$currentDay = 12"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>12</option>
                            <option value="13"><xsl:if test = "$currentDay = 13"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>13</option>
                            <option value="14"><xsl:if test = "$currentDay = 14"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>14</option>
                            <option value="15"><xsl:if test = "$currentDay = 15"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>15</option>
                            <option value="16"><xsl:if test = "$currentDay = 16"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>16</option>
                            <option value="17"><xsl:if test = "$currentDay = 17"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>17</option>
                            <option value="18"><xsl:if test = "$currentDay = 18"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>18</option>
                            <option value="19"><xsl:if test = "$currentDay = 19"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>19</option>
                            <option value="20"><xsl:if test = "$currentDay = 20"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>20</option>
                            <option value="21"><xsl:if test = "$currentDay = 21"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>21</option>
                            <option value="22"><xsl:if test = "$currentDay = 22"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>22</option>
                            <option value="23"><xsl:if test = "$currentDay = 23"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>23</option>
                            <option value="24"><xsl:if test = "$currentDay = 24"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>24</option>
                            <option value="25"><xsl:if test = "$currentDay = 25"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>25</option>
                            <option value="26"><xsl:if test = "$currentDay = 26"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>26</option>
                            <option value="27"><xsl:if test = "$currentDay = 27"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>27</option>
                            <option value="28"><xsl:if test = "$currentDay = 28"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>28</option>
                            <option value="29"><xsl:if test = "$currentDay = 29"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>29</option>
                            <option value="30"><xsl:if test = "$currentDay = 30"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>30</option>
                            <option value="31"><xsl:if test = "$currentDay = 31"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>31</option>
                        </select>,
                        <select name="yearEnd">
                            <option value="2002"><xsl:if test = "$currentYear = 2002"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2002</option>
                            <option value="2003"><xsl:if test = "$currentYear = 2003"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2003</option>
                            <option value="2004"><xsl:if test = "$currentYear = 2004"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2004</option>
                            <option value="2005"><xsl:if test = "$currentYear = 2005"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2005</option>
                            <option value="2006"><xsl:if test = "$currentYear = 2006"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2006</option>
                            <option value="2007"><xsl:if test = "$currentYear = 2007"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2007</option>
                            <option value="2008"><xsl:if test = "$currentYear = 2008"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2008</option>
                            <option value="2009"><xsl:if test = "$currentYear = 2009"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2009</option>
                            <option value="2010"><xsl:if test = "$currentYear = 2010"><xsl:attribute  name = "selected" >selected</xsl:attribute></xsl:if>2010</option>
                        </select>
                    </li>
                </ul>
            </td>
        </tr>

        <tr>
            <td class="table-light-left" style="text-align:right">Activation For</td>
            <td class="table-content-right">
                <span style="uportal-channel-warning">Hold down SHIFT or CTRL to select multiple people</span><br/>
                <select name="activationFor" multiple="multiple" size="4">
                    <option value="all" selected="selected">All members</option>
                    <xsl:apply-templates select="$GRADEBOOK-ITEM/gradebook-score/user"/>
                </select>
            </td>
        </tr>


        <xsl:if test = "$ACTIVATION_TYPE = 1">
        <tr>
            <td class="table-light-left" style="text-align:right">Activation Comment</td>
            <td class="table-content-right"><textarea name="comment"></textarea></td>
        </tr>
<!--
        <tr>
            <td class="table-light-left" style="text-align:right">Duration to<br/>Complete (hrs)</td>
            <td class="table-content-right">
                <input type="text" class="text" size="2" maxlength="1" value="1" name="duration">
                    <xsl:attribute  name = "onchange" >if (isNaN(this.value)) {this.value = 1}</xsl:attribute>
                </input>
            </td>
        </tr>-->
        </xsl:if>

        <tr>
            <td class="table-light-left" style="text-align:right">Attempts Permitted</td>
            <td class="table-content-right">
                <input type="text" class="text" size="2" maxlength="2" value="1" name="attempts">
                </input>
             </td>
        </tr>
        <!-- PushToCalendar is missing functionality to implement it in the Java.  Removing it until fixed. -->
        <!--
        <tr>
            <td class="table-light-left" style="text-align:right">Push to Calendar</td>
            <td class="table-content-right">
                <input type="checkbox" name="PushToCalendar" value="push"/>
            </td>
        </tr>
        -->

        <xsl:apply-templates select="//adjunct/choice-collection" mode="adjunct-activation-data"/>
        <!--xsl:for-each select="//adjunct/choice-collection">
            <xsl:call-template name="adjunct-activation-data" />
        </xsl:for-each-->

        <tr>
            <td colspan="2" class="table-content-single-bottom" style="text-align:center">
                <input type="submit" class="uportal-button" value="Submit" title="To submit your confirmation response and return to the main view of the gradebook"/>
                <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To return to the main view of the gradebook without deleting this item"/>
            </td>
        </tr>
        <xsl:call-template name="sublinks">
            <xsl:with-param name="commandDefault">insert_activation</xsl:with-param>
        </xsl:call-template>
        </table>
        <input type="hidden" name="name" value="{$GRADEBOOK-ITEM/title}"/>
        <!--<input type="hidden" name="gradebook_itemID" value="{$gradebookItemID}"/> -->
        <input type="hidden" name="type" value="{$ACTIVATION_TYPE}" />
    </form>
</xsl:template>

<xsl:template match="choice-collection" mode="adjunct-activation-data">

    <!-- The context node is <choice-collection> -->

    <tr>
        <th colspan="2" class="th-top"><xsl:value-of select="label" /></th>
    </tr>

    <tr>
        <td class="table-light-left" style="text-align:right;vertical-align:top;"><img height="1" width="1" src="{$SPACER}" alt="" title="" border="0" /></td>
        <td class="table-content-right" width="100%" valign="top">
            <xsl:choose>
                <xsl:when test="count(choice[position() = 1]/option) = 0">
                    None available
                </xsl:when>

                <xsl:otherwise>
                    <!-- The choice-decision collection rendering is done through a XSL module initiated in the global.xsl file -->
                    <table border="0" cellspacing="0" cellpadding="2"><tr>
                        <xsl:apply-templates select=".">
                            <xsl:with-param name="INPUT_TYPE">selection</xsl:with-param>
                            <xsl:with-param name="WRAPPER">td</xsl:with-param>
                        </xsl:apply-templates>
                    </tr></table>
                </xsl:otherwise>
            </xsl:choose>

        </td>
    </tr>

</xsl:template>

<xsl:template match="user">
        <option value="{@username}">
            <xsl:value-of select="last_name"/>, <xsl:value-of select="first_name"/>
        </option>
</xsl:template>

</xsl:stylesheet>




