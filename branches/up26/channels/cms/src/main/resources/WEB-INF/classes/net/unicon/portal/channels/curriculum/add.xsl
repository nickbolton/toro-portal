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
    <xsl:call-template name="links"/>
    <xsl:call-template name = "autoFormJS" />    
    <!-- UniAcc: Data Table -->
	<!-- Display search fields for online curriculum only if Virtuoso is available -->
	<xsl:if test = "$onlineCurriculumAvailable = 'true'">
	    <form name="curriculumFormSearch" action="{$baseActionURL}?command=search" method="post">
	    <input type="hidden" name="catPageSize" value="{$catPageSize}" />
	    <table cellpadding="0" cellspacing="0" border="0" width="100%">
	        <tr>
	            <th id="AddCurriculum" class="th">
	                Search Online Curriculum
	            </th>
	        </tr>
	        <tr>
	            <td headers="AddCurriculum" class="table-content-right">
	                    <!-- <xsl:if test="count(./curriculum) &gt; 0">
	                        <li>
	                            <input type="radio" name="type" value="online" id="ccr1"/>
	                            <label for="ccr1">&#160;Online Curriculum</label>
	                            <br/>
	                            <select name="onlineCurriculum">
	                                <xsl:attribute  name = "onchange" >
	                                    document.curriculumForm.type[0].checked = true;if(this.selectedIndex > 0){document.curriculumForm.name.value = this.options[this.selectedIndex].text;}
	                                </xsl:attribute>
	                                <option selected="selected" value="">&#160;</option>
	                                <xsl:for-each select = "./curriculum">
	                                    <option value="{@reference}"><xsl:value-of select="./title" /></option>
	                                </xsl:for-each>
	                            </select>
	                            <br/>
	                            &#160;
	                        </li>
	                    </xsl:if> -->
	                    
	                        <strong>Online Curriculum</strong><br/>
	                        Title:<input type="text" class="text" name="title" id="title" size="10" />
	                        <select name="searchAndOr" id="searchAndOr">
	                            <option value="1">Or</option>
	                            <option value="2">And</option>
	                        </select>
	                        Description:<input type="text" class="text" name="description" id="description" size="10" />
	                        
	                    
	            </td>
	        </tr>
	        <tr>
	            <td headers="AddCurriculum" class="table-nav">
	                <input type="submit" class="uportal-button" name="searchSubmit" id="searchSubmit" value="Search" />
	            </td>
	        </tr>
	    </table>
	    </form>
	</xsl:if>
	
    <form name="curriculumForm" enctype='multipart/form-data' action="{$baseActionURL}?command=insert" 
    method="post" onsubmit="return(checkFormCC(this));">
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th id="AddReference" class="th" colspan="2">
                Add Resource
            </th>
        </tr>
        <tr>
            <td id="Reference" class="table-light-left" style="text-align:right">Resource</td>
            <td id="AddReference" class="table-content-right">
                        <input type="radio" name="type" value="url" onclick="document.curriculumForm.curriculumURL.focus();" id="ccr2" /> 
                        <label for="ccr2">&#160;<strong>Link to URL</strong>&#160;&#160;&#160;</label>
                            <span class="message">
                                Example: "http://www.unicon.net"
                            </span>
                            <br/>        
                        <input title="Link to URL" type="text" class="text" size="40" maxlength="254" value="http://" name="curriculumURL" onchange="selectRadioCC(this);" />
                        <!-- <input type="text" class="text" size="40" value="" name="curriculumURL" onchange="document.curriculumForm.type[1].checked = true;" /> -->                        
                        <br/><br/>
                        <input type="radio" name="type" value="file" onclick="document.curriculumForm.uploadedFile.focus();" id="ccr3" /> 
                        <label for="ccr3">&#160;<strong>Uploaded File</strong></label>
                        <br/>
                        <input title="Uploaded File" type="file" style="text-align: left;" size="25" maxlength="254" name="uploadedFile" onchange="selectRadioCC(this);"/>
                        <br/>
                    
            </td>
        </tr>
        <tr>
            <td class="table-light-left" style="text-align:right" id="Name">Name</td>
            <td class="table-content-right" width="100%" headers="AddCurriculum Name">
                <input title="Name" type="text" class="text" size="55" maxlength="254" name="title"/>
            </td>
        </tr>
        <!--
        <tr>
            <td class="table-light-left" style="text-align:right" nowrap="nowrap">Curriculum Description</td>
            <td class="table-content-right" width="100%"><textarea name="description"><xsl:value-of select="./curriculum/description" /></textarea></td>
        </tr> -->
        <tr>
            <td colspan="2" class="table-nav" style="text-align:center">
                <input class="uportal-button" type="submit" value="Submit" title="To add this curriculum and return to viewing all the offering curricula."/>
                &#032;&#032;&#032;&#032;
                <input class="uportal-button"  type="button" value="Cancel" onclick="window.locationhref='{$baseActionURL}'" title="To cancel and return to viewing all the offering curricula."/>
            </td>
        </tr>
    </table>
    </form>
</xsl:template>

<!--<xsl:template match="curriculum">

</xsl:template> -->

</xsl:stylesheet>
