<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes" />
  <!-- Include Files -->
  <xsl:include href="common.xsl"/>
  <xsl:param name="searchTitle" />
  <xsl:param name="searchDescription" />
  <xsl:param name="searchVersion" />
  <xsl:param name="associationID">2</xsl:param>
  <xsl:param name="ASSESSMENT_COUNT" select="count(/gradebooks/available_assessments/assessment)" />

<xsl:template match="/">
<!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
    </textarea>
-->    
    <xsl:call-template name="links"/>
    <script language="JavaScript" type="text/javascript" src="javascript/common/autoForm.js"></script>
    <form onSubmit="return validator.applyFormRules(this, new CommonRulesObject())" name="searchAssociationForm" action="{$baseActionURL}" method="post">
        <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
        <input type="hidden" name="command" value="searchAssociation"/>
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <th id="selectedAsmtAssoc" class="th" colspan="2">Online Assessment Association</th>
            </tr>
            <tr>
                <td class="table-light-left" align="right">Current Association:</td>
                <td class="table-content-right" align="left">
                    <xsl:choose>
                        <xsl:when test="$associationID = '2'">
                            None
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="gradebooks/available_assessments/assessment[$associationID = @id]/title" />
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
            <tr>
                <th id="searchAsmtAssoc" class="th" colspan="2">Assessment Search</th>
            </tr>
            <tr>
               <td class="table-content-single">
                  Enter search criteria for assessment.<br/>
                  <label for="selectAssocTitle">Title:</label>
                   <input type="text" class="text" name="title" id="selectAssocTitle" size="10" value="{$searchTitle}"/>&#160;
                  <label for="selectAssocDescription">Description:</label>
                   <input type="text" class="text" name="description" id="selectAssocDescription" size="10" value="{$searchDescription}"/>&#160;
                   <!--
                  <label for="selectAssocVersion">Version:</label>
                   <input type="text" class="text" name="keyword" id="selectAssocVersion" size="10" value="{$searchVersion}"/>&#160;-->
                  <input type="submit" class="uportal-button" name="searchSubmit" id="searchSubmit" value="Search" 
                      title="Submit search criteria"/>
               </td>
            </tr>
        </table>
    </form>

    <form name="gradebookForm" action="{$baseActionURL}" method="post">
        <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
        <input type="hidden" name="command" value="setAssociation"/>
        <xsl:choose>
            <xsl:when test="$ASSESSMENT_COUNT &gt; 0">
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
                    <tr>
                        <td headers="selectedAsmtAssoc" class="table-content-single-bottom">
                            <input type="radio" name="onlineAsmt" id="onlineAsmt2" value="">
                                <xsl:if test = "$associationID = '2'">
                                    <xsl:attribute  name = "checked" >checked</xsl:attribute>
                                </xsl:if>
                            </input>
                            <label for="onlineAsmt2">&#160;<strong>None</strong><br/>
                            No online assessment association</label><br/>
                        </td>
                    </tr>
                    <xsl:apply-templates select = "gradebooks/available_assessments/assessment" />
                    <tr>
                        <td headers="selectedAsmtAssoc" class="table-nav">
                            <input type="submit" class="uportal-button" name="submit" id="submit" value="Submit"
                                title="To submit selected association and return to further editing of the column" />
                            <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=setAssociation&amp;associationID={$associationID}'"
                                title="To return to further editing of the column without changing the association" />
                        </td>
                    </tr>
                </table>
                
            </xsl:when>
          
            <xsl:otherwise>
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
            	    <tr>
                        <td class="table-content-single">No matches for your search criteria. Please enter new search criteria.</td>
                    </tr>
                    <tr>
                        <td headers="selectedAsmtAssoc" class="table-nav">
                            <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?command=setAssociation&amp;associationID={$associationID}'"
                                title="To return to further editing of the column without changing the association" />
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </form>

</xsl:template>


<xsl:template match="assessment">
    <xsl:variable name = "BOTTOM_STYLE">
        <xsl:if test = "position() = last()">-bottom</xsl:if>
    </xsl:variable>
    <tr>
        <td headers="AddCurriculum Title" class="table-content-single{$BOTTOM_STYLE}">
                <input type="radio" name="onlineAsmt" id="onlineAsmt{@id}">
                    <xsl:attribute name="value">
                        <xsl:value-of select="@id" />
                    </xsl:attribute>
                    <xsl:if test = "$associationID = @id">
                        <xsl:attribute  name = "checked" >checked</xsl:attribute>
                    </xsl:if>
                </input>
                <label for="onlineAsmt{@id}">&#160;<strong><xsl:value-of select="title" /></strong>
                <!-- Include description if not equal to null -->
                <xsl:if test = "description != 'null'"> <br/> <xsl:value-of select="description" /></xsl:if>
                </label><br/>
        </td>
    </tr>
</xsl:template>


<!--<xsl:template match="course-list">
    <xsl:call-template name="links"/>
<form name="curriculumForm" action="{$baseActionURL}&amp;command=insert" method="post">
<input type="hidden" name="type" value="online" />
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th id="AddCurriculum" class="th">
                Online Assessment Search Results
            </th>
        </tr>
        <xsl:for-each select="deliverycurriculum">
            <xsl:call-template name="searchList" />
        </xsl:for-each>
        
        <xsl:choose>
            <xsl:when test="count(deliverycurriculum) &gt; 0">
            <tr>
                <td headers="AddCurriculum Form Submit" class="table-nav">
                    <input type="submit" class="uportal-button" name="submit" id="submit" value="Submit" />
                    <input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" />
                </td>
            </tr>
            </xsl:when>
          
            <xsl:otherwise>
            <tr><td class="table-content-single">No match found.</td></tr>
            </xsl:otherwise>
        </xsl:choose>
    </table>
</form>
    <xsl:call-template name="catalog" />

</xsl:template> -->

<!--<xsl:template name="searchList">
<tr>
    <td headers="AddCurriculum Title" class="table-content-single">
            <input type="radio" name="onlineCurriculum" id="onlineCurriculum{@curriculumid}">
                <xsl:attribute name="value">
                    <xsl:value-of select="@curriculumid" />
                </xsl:attribute>
            </input>
            <label for="onlineCurriculum{@curriculumid}">&#160;<strong><xsl:value-of select="title" /></strong></label><br/>
            <xsl:value-of select="description" />
    </td>
</tr>
</xsl:template> -->

</xsl:stylesheet>
