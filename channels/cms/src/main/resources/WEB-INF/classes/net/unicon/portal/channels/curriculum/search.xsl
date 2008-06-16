<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes" />
  <!-- Include Files -->
  <xsl:include href="common.xsl"/>

<xsl:template match="/">
    <!--
    <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
    </textarea>
     -->

    <xsl:apply-templates />
</xsl:template>

<xsl:template match="course-list">
   
<xsl:if test="count(deliverycurriculum) &gt; 0">
	<xsl:apply-templates select="//theme-set" />
</xsl:if>

<xsl:call-template name="links"/>

<form name="curriculumForm" id="curriculumForm" action="{$baseActionURL}?command=insert" method="post">
<input type="hidden" name="type" value="online" />
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th id="AddCurriculum" class="th">
                Online Curriculum Search Results
            </th>
        </tr>
        <xsl:for-each select="deliverycurriculum">
            <xsl:call-template name="searchList" />
        </xsl:for-each>

        <xsl:choose>
            <xsl:when test="count(deliverycurriculum) &gt; 0">
                <tr>
                    <td class="table-content-single">
                        <table border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td colspan="3" align="left">Presentation Details</td>
                            </tr>
                            <tr>
                                <td align="right">Title (Display Name):</td>
                                <td width="4"><img border="0" height="1" width="1" src="{$SPACER}" alt="" title="" /></td>
                                <td align="left"><input type="text" size="20" maxlength="255" name="title" value="" /></td>
                            </tr>
                            <tr>
                                <td align="right">Structural Theme:</td>
                                <td width="4"><img border="0" height="1" width="1" src="{$SPACER}" alt="" title="" /></td>
                                <td align="left"><select name="theme" onchange="loadStyles()">
                                    <xsl:for-each select="//theme-set/theme">
                                        <xsl:call-template name="theme" />
                                    </xsl:for-each>
                                </select></td>
                            </tr>
                            <tr>
                                <td align="right">Visual Style:</td>
                                <td width="4"><img border="0" height="1" width="1" src="{$SPACER}" alt="" title="" /></td>
                                <td align="left">
									<select name="style">
										<xsl:for-each select="//theme-set/theme[position()=1]/style">
											<option><xsl:value-of select="@handle" /></option>
										</xsl:for-each>
									</select>
								</td>
                            </tr>
                        </table>
                    </td>
                </tr>
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

<!-- The JS script is expecting data, and throws an error if there is none. Thus, check to see if there is data before intializing the script. -->
<xsl:if test="count(deliverycurriculum) &gt; 0">
	<script language="JavaScript">initializeCurriculumChannel()</script>
</xsl:if>

</xsl:template>

<xsl:template name="searchList">
<tr>
    <td headers="AddCurriculum Title" class="table-content-single">
            <input type="radio" name="onlineCurriculum" id="onlineCurriculum{@curriculumid}" onclick="setTitleText()">
                <xsl:attribute name="value">
                    <xsl:value-of select="@curriculumid" />
                </xsl:attribute>
            </input>
            <label for="onlineCurriculum{@curriculumid}">&#160;<strong><xsl:value-of select="title" /></strong></label><br/>
            <xsl:value-of select="description" />
    </td>
</tr>
</xsl:template>

<xsl:template match="theme-set">
    <script language="JavaScript">
        var initializeCurriculumChannel = function() {}
		
		var loadStyles = function() {
            // Clear the styles select.
            var optns = document.getElementById("curriculumForm").style.options;
            while (optns.length > 0) optns[0] = null;

            // Load new values.
        <xsl:for-each select="theme">
            if (document.getElementById("curriculumForm").theme.selectedIndex == <xsl:value-of select="position()" /> -1) {
            <xsl:for-each select="style">
                optns[optns.length] = new Option("<xsl:value-of select="@handle" />", "<xsl:value-of select="@handle" />");
            </xsl:for-each>
            }
        </xsl:for-each>

        }
        var setTitleText = function() {

            var title;
            
           	if (document.getElementById("curriculumForm").onlineCurriculum.length &gt; 1) {

	            // Find the selected curriculum.
	            var sel;
	            for (var i=0; i &lt; document.getElementById("curriculumForm").onlineCurriculum.length; i++) {
	                if (document.getElementById("curriculumForm").onlineCurriculum[i].checked == "1") {
	                    sel = document.getElementById("curriculumForm").onlineCurriculum[i];
	                    break;
	                }
	            }
	
		        <xsl:for-each select="//deliverycurriculum">
		            if (sel.value == "<xsl:value-of select="@curriculumid" />") {
		                title = "<xsl:value-of select="title" />";
		            }
		        </xsl:for-each>
		        
	        } else {

		        <xsl:for-each select="//deliverycurriculum">
		                title = "<xsl:value-of select="title" />";
		        </xsl:for-each>
	        }
	        
            document.getElementById("curriculumForm").title.value = title;

        }
        var curriculumChannelFunctions = ["initializeCurriculumChannel", "setTitleText", "loadStyles"];
        if (window.channelFunctionsArray) {
            channelFunctionsArray[channelFunctionsArray.length] = curriculumChannelFunctions;
        } else {
            // else create channelFunctionsArray with this as first entry
            channelFunctionsArray = [curriculumChannelFunctions]; // create 2-D array
        }
    </script>
</xsl:template>

</xsl:stylesheet>
