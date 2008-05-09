<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>

    <!-- INCLUDE FILES -->
    <xsl:include href="common.xsl"/>

    <!-- VARIABLES -->
    <xsl:variable name = "GB_ITEM" select = "/gradebooks/gradebook-item[@id = $gradebookItemID]" />
    <xsl:variable name = "FORM" select = "$GB_ITEM/assessment/form" />
    <xsl:variable name = "TYPE" select = "$GB_ITEM/@type" />
    <xsl:variable name = "USER" select = "$GB_ITEM/gradebook-score/user" />
        <!-- Virtuoso variables used on the question request URL -->
        <xsl:variable name = "THEME"><!-- <xsl:value-of select="/gradebooks/theme-set//theme/@handle"/> --></xsl:variable>
        <xsl:variable name = "STYLE"><!-- <xsl:value-of select="/gradebooks/theme-set//theme/style/@handle"/> --></xsl:variable>
        <xsl:variable name = "SMV"/>

    <!-- PARENT TEMPLATE -->
    <xsl:template match="/">
        <xsl:call-template name="links"/>

        <!--
        <textarea rows="4" cols="100">
            <xsl:copy-of select = "*"/>
            <parameter name="gradebookItemID"><xsl:value-of select="$gradebookItemID" /></parameter>
            <parameter name="username"><xsl:value-of select="$username" /></parameter>
            <parameter name="viewAll"><xsl:value-of select="$viewAll" /></parameter>
            <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
        </textarea>
         -->

        <xsl:apply-templates select="//theme-set" /><!-- The "theme-set" template exists in global/theme-style.xsl and contains the JavaScript necessary for this page -->

        <xsl:choose>
            <!-- Send all <form> elements for processing if user has permissions and has selected to view all -->
            <xsl:when test="($viewAll = 'Y' or (count(/gradebooks/gradebook-item) = count(/gradebooks/gradebook-item/gradebook-score))) and $current_command = 'all_question_details'">
                <xsl:apply-templates select="$FORM"/>
                <xsl:apply-templates select="$GB_ITEM"/>
            </xsl:when>
            <!-- Else send only the <form> element for the selected user -->
            <xsl:otherwise>
                <xsl:variable name="FORM_ID" select="$GB_ITEM/gradebook-score/user[@username = $username]/../submission/results/@formref_id"/>
                <xsl:apply-templates select="$FORM[@id = $FORM_ID]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- END PARENT TEMPLATE -->

<!-- ================================= CHILD TEMPLATES ================================= -->

    <!-- Generate a new table for each <form> in the XML, and the header rows -->
    <xsl:template match="form">
        <xsl:variable name="FORM_ID" select="@id"/>
        <xsl:variable name = "QUESTIONS" select = "questions/question" />
        <xsl:variable name="COLUMNS" select="count($QUESTIONS)+3"/>

        <!-- generate <table> only if there is at least one matching <results> element for a user who does not have permission -->
        <xsl:if test="count(../../gradebook-score/submission/results[@formref_id = $FORM_ID]) &gt; 0">
            <!-- UniAcc: Data Table -->
            <table cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <th colspan="{$COLUMNS}" class="th">Online Assessment Details
                        <xsl:choose>
                            <!-- If Form title is provided, use it -->
                            <xsl:when test="title and title!=''">
                                 - <xsl:value-of select="title"/>
                            </xsl:when>
                            <!-- Else if Exam title is provided, use it -->
                            <xsl:when test="../title and ../title!=''">
                                 - <xsl:value-of select="../title"/>
                            </xsl:when>
                            <xsl:otherwise></xsl:otherwise>
                        </xsl:choose>
                    </th>
                </tr>
                <tr>
                    <td align="center" class="table-light-left" style="font-weight:bold" scope="col">Question</td>
                    <xsl:for-each select="$QUESTIONS">
                        <td align="center" class="gradebook-data">
                            Q<xsl:number value="position()"/>&#160; <xsl:value-of select="@type"/>
                        </td>
                    </xsl:for-each>
                    <td align="center" class="table-light-right" style="font-weight:bold">Score</td>
                    <td align="center" class="table-light-right" style="font-weight:bold">Personalized Feedback</td>
                </tr>
                <xsl:apply-templates select="../../gradebook-score/submission/results[@formref_id = $FORM_ID]">
                    <xsl:with-param name="FEEDBACK" select="../../gradebook-score/feedback" />
                </xsl:apply-templates>
                <xsl:if test="/gradebooks/theme-set">
                <tr>
                    <td class="table-nav" colspan="{$COLUMNS}">
                        <form id="gradebookForm"><br/>
                        <table border="0" cellspacing="0" cellpadding="0">
                           <tr>
                               <td colspan="3" align="left">Presentation Overrides (make selection then click above):</td>
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
                       </form>
                    </td>
                </tr>
                </xsl:if>
                <!--<tr>
                <td colspan="{$COLUMNS}" class="table-nav" style="text-align:center">
                    <input type="button" class="uportal-button" value="Cancel">
                        <xsl:attribute name = "onclick" >window.locationhref='<xsl:choose>
                         Make Cancel button return to details view when user came from that view -->
                <!--<xsl:when test="$current_command = 'question_details'"><xsl:value-of select="$baseActionURL" />&amp;command=details&amp;gradebook_itemID=<xsl:value-of select="$gradebookItemID" />&amp;username=<xsl:value-of select="$username" /></xsl:when>
                           Otherwise, return to the main view
                        <xsl:otherwise><xsl:value-of select="$baseActionURL" /></xsl:otherwise>
                        </xsl:choose>'</xsl:attribute>

                    </input>
                </td>
            </tr>-->
                <!-- <xsl:call-template name="sublinks"/> -->
            </table>
        </xsl:if>
    </xsl:template>


    <!-- Generate the content rows for the table -->
    <xsl:template match="results">
        <xsl:param name="CURRENT_RESULT" select="." />
        <xsl:param name="FEEDBACK" />
        <xsl:variable name="RESULT" select="../@filename"/>
        <xsl:variable name="LFORM" select="@formref_id"/>
        <xsl:variable name = "OFFERING" select = "$GB_ITEM/@offeringID" />
        <xsl:variable name = "COURSE_STRING" select = "$GB_ITEM/@association" />
        <xsl:variable name = "COURSE" select = "substring-before($COURSE_STRING,'|')" />
        <xsl:variable name = "EXAM" select = "substring-after($COURSE_STRING,'|')" />
        <!-- Output row only if it is for the particular user, or if the user has permission to view all and they selected to view all question details -->
        <xsl:if test = "($viewAll = 'Y' and $current_command = 'all_question_details') or (../../user/@username = $username)">
        <tr>
            <td align="center" class="gradebook-user-left">
                <xsl:value-of select="../../user/last_name"/>, <xsl:value-of select="../../user/first_name"/>
            </td>
            <xsl:for-each select="questions/question">
                <!-- Store Question ID of default question so that the users response will line up with the default question order -->
                <xsl:variable name="Q_ID" select="@id"/>
                <xsl:variable name="USERNAME" select="../../../../user/@username"/>
                <xsl:variable name = "REFERENCE_LINK">AIRender?rslt=<xsl:value-of select="$RESULT" />&amp;theme=<xsl:value-of select="$THEME" />&amp;style=<xsl:value-of select="$STYLE" />&amp;user=<xsl:value-of select="$USERNAME" />&amp;item=<xsl:value-of select="$Q_ID" />&amp;lform=<xsl:value-of select="$LFORM" />&amp;offering=<xsl:value-of select="$OFFERING" />&amp;course=<xsl:value-of select="$COURSE" />&amp;exam=<xsl:value-of select="$EXAM" />
                </xsl:variable>
                <xsl:variable name = "HREF">javascript:resolveLink('gradebookForm', '<xsl:value-of select="$REFERENCE_LINK" />', 'assessment', '&amp;')</xsl:variable>
                <td align="center" class="table-content">
                    <xsl:choose>
                        <xsl:when test="$TYPE = '1'">
                            <a href="{$HREF}" title="View the details for this assessment item - opens in a new window">
                                <xsl:value-of select="$CURRENT_RESULT/questions/question[@id=$Q_ID]/@score"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$CURRENT_RESULT/questions/question[@id=$Q_ID]/@score"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:for-each>
            <td align="center" class="gradebook-user-right">
                <xsl:value-of select="sum(./questions/question/@score)"/>
            </td>
            <td align="center" class="gradebook-user-right">
                <xsl:if test="$FEEDBACK">
                    <a href="javascript:resolveLink('gradebookForm', '{$FEEDBACK/@filename}', 'feedback', ',')" title="View Personalized Feedback">view</a>
                </xsl:if>
            </td>
        </tr>
        </xsl:if>
        <!-- If the last <results> then generate a row showing the max score -->
        <xsl:if test="position() = last()">
            <tr>
                <td align="center" class="table-light-left-bottom" style="font-weight:bold">Max Score</td>
                <!--<td align="center" class="gradebook-data">10</td> -->
                <xsl:variable name="FORM_ID" select="@formref_id"/>
                <xsl:for-each select="questions/question">
                    <td align="center" class="gradebook-data-bottom">
                        <xsl:value-of select="@max_score"/>
                    </td>
                </xsl:for-each>
                <td align="center" class="table-light-right-bottom">
                    <xsl:value-of select="sum(questions/question/@max_score)"/>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>


    <!-- Template description goes here -->
    <xsl:template match="gradebook-item">
        <xsl:variable name="RESULT_COUNT" select="count(gradebook-score/submission/results)"/>
        <xsl:if test="$RESULT_COUNT != count(gradebook-score/submission)">
            <!--|<xsl:value-of select="count(gradebook-score/submission/results)" />|<xsl:value-of select="count(gradebook-score/submission)" />|  -->
            <!-- UniAcc: Data Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="5" class="th-top-single" id="onlineAss">Online Assignment Submissions</th>
                </tr>
                <tr>
                    <td align="center" class="table-light-left">&#160;</td>
                    <td align="center" class="table-content" id="GBSub">Submission</td>
                    <td align="center" class="table-content" id="GBFeed">Personalized Feedback</td>
                    <td align="center" class="table-content-right" style="font-weight:bold" id="GBScore">Score</td>
                </tr>
                <xsl:apply-templates select="gradebook-score/submission"/>
                <tr>
                    <td colspan="3" align="right" class="table-light-left-bottom" style="font-weight:bold" headers="onlineAss">Max Score</td>
                    <td align="center" class="table-content-right-bottom">
                        <xsl:value-of select="@max_score"/>&#160;</td>
                </tr>
                <!-- <xsl:call-template name="sublinks"/> -->
            </table>
        </xsl:if>
        <xsl:if test="count(gradebook-score/submission) != count(gradebook-score)">
            <!--|<xsl:value-of select="count(gradebook-score/submission)" />|<xsl:value-of select="count(gradebook-score)" />| -->
            <!-- UniAcc: Data Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <th colspan="2" class="th" id="onlineAss">No Online Assignment/Assessment Details</th>
                </tr>
                <tr>
                    <td align="right" class="table-light-left" width="80%">&#160;</td>
                    <td align="center" class="table-content-right" style="font-weight:bold" width="20%" headers="onlineAss" id="Score">Score</td>
                </tr>
                <xsl:apply-templates select="gradebook-score"/>
                <tr>
                    <td align="right" class="table-light-left-bottom" style="font-weight:bold" headers="onlineAss" id="maxScore">Max Score</td>
                    <td align="center" class="table-content-right-bottom" headers="onlineAss masScore">
                        <xsl:value-of select="@max_score"/>&#160;</td>
                </tr>
                <!-- <xsl:call-template name="sublinks"/> -->
            </table>
        </xsl:if>
    </xsl:template>


    <!-- Template description goes here -->
    <xsl:template match="submission">
        <xsl:if test="not(boolean(results))">
            <tr>
                <td align="center" class="gradebook-user-left">
                    <xsl:value-of select="../user/last_name"/>, <xsl:value-of select="../user/first_name"/>
                </td>
                <td align="center" class="table-content" headers="GBSub">
                    <xsl:choose>
                        <xsl:when test="./@filename != '' and ./@filename != 'null'">
                            <a href="{$workerActionURL}&amp;filename={./@filename}&amp;fileType=submission&amp;gradebookScoreId={../@id}" title="To access or download the submitted file" target="_blank">
                                <xsl:value-of select="./@filename"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>None</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td align="center" class="table-content" headers="GBFeed">
                    <xsl:choose>
                        <xsl:when test="../feedback/@filename != '' and ../feedback/@filename != 'null' and not((../../@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
                            <a href="{$workerActionURL}&amp;filename={../feedback/@filename}&amp;fileType=feedback&amp;gradebookScoreId={../@id}" title="To access or download the feedback file" target="_blank">
                                <xsl:value-of select="../feedback/@filename"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>None</xsl:otherwise>
                    </xsl:choose>
                </td>
                <td align="center" class="gradebook-user-right" headers="GBScore">
                    <a href="{$baseActionURL}?command=details&amp;gradebook_itemID={../../@id}&amp;username={../user/@username}" title="To submit assignments and feedback, and view details">
                        <xsl:choose>
                            <xsl:when test="number(../@score) &gt; -1 and not((../../@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
                                <xsl:value-of select="../@score"/>
                            </xsl:when>
                            <xsl:otherwise>--</xsl:otherwise>
                        </xsl:choose>
                    </a>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>


    <!-- Template description goes here -->
    <xsl:template match="gradebook-score">
        <xsl:if test="not(boolean(submission))">
            <tr>
                <td align="center" class="gradebook-user" headers="onlineAss" id="{user/last_name},{user/first_name}">
                    <xsl:value-of select="user/last_name"/>, <xsl:value-of select="user/first_name"/>
                </td>
                <td align="center" class="gradebook-user-right" headers="onlineAss {user/last_name},{user/first_name}">
                    <a href="{$baseActionURL}?command=details&amp;gradebook_itemID={../@id}&amp;username={user/@username}" title="To submit assignments and feedback, and view details">
                        <xsl:choose>
                            <xsl:when test="(number(@score) &gt; -1) and not((../@feedback = 'no') and ($accessHiddenFeedback = 'N'))">
                                <xsl:value-of select="@score"/>
                            </xsl:when>
                            <xsl:otherwise>--</xsl:otherwise>
                        </xsl:choose>
                    </a>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>


</xsl:stylesheet>
