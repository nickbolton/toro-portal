<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes" />
  <!-- Include Files -->
  <xsl:include href="common.xsl"/>

<xsl:template match="/">
    <!-- <textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="workerActionURL"><xsl:value-of select="$workerActionURL" /></parameter>
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
        <parameter name="offeringName"><xsl:value-of select="$offeringName" /></parameter>
        <parameter name="curriculumID"><xsl:value-of select="$curriculumID" /></parameter>
        <parameter name="onlineCurriculumAvailable"><xsl:value-of select="$onlineCurriculumAvailable" /></parameter>
        <parameter name="title"><xsl:value-of select="$title" /></parameter>
        <parameter name="description"><xsl:value-of select="$description" /></parameter>
        <parameter name="catChannel"><xsl:value-of select="$catChannel" /></parameter>
        <parameter name="catCurrentCommand"><xsl:value-of select="$catCurrentCommand" /></parameter>
        <parameter name="addCurriculum"><xsl:value-of select="$addCurriculum" /></parameter>
        <parameter name="removeCurriculum"><xsl:value-of select="$removeCurriculum" /></parameter>
    </textarea> -->
    <xsl:call-template name="links"/>

    <xsl:choose>
        <!-- If at least one course then build each row -->
        <xsl:when test="count(./course-list/curriculum) &gt; 0">
            <xsl:apply-templates />
        </xsl:when>
          <!-- Else give message indicating there is no associated curriculum -->
        <xsl:otherwise>
         There is no curriculum currently associated with this offering.
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="course-list">

	<xsl:if test="count(curriculum[@type = 'online']) &gt; 0">
    	<xsl:apply-templates select="//theme-set" /><!-- The "theme-set" template exists in global/theme-style.xsl and contains the JavaScript necessary for this page -->
    </xsl:if>

    <form name="curriculumForm" action="{$baseActionURL}" method="post" id="curriculumForm">

    <!-- UniAcc: Layout Table -->
    <table cellspacing="0" cellpadding="0" border="0" width="100%">
        <tr>
            <th class="th">Online Curriculum: access content curriculum here</th>
        </tr>
        <xsl:apply-templates select="curriculum[@type = 'online']" />
        <xsl:if test="count(curriculum[@type = 'online']) &gt; 0">
			<tr>
				<td><img border="0" height="15" width="1" src="{$SPACER}" alt="" title="" /></td>
			</tr>
            <tr>
                <td class="table-content-single">
                    <table border="0" cellspacing="0" cellpadding="5" width="100%">
                        <tr>
                            <td colspan="2"><strong>Presentation Overrides</strong></td>
                        </tr>
                        <tr>
                            <td align="right">Structural Theme:</td>
                            <td align="left">
                                <!-- This template exists in global/theme-style.xsl -->
                                <xsl:call-template name="theme-selection">
                                    <xsl:with-param name="FORMNAME">curriculumForm</xsl:with-param>
                                </xsl:call-template>
                            </td>
                        </tr>
                        <tr>
                            <td align="right">Visual Style:</td>
                            <td align="left">
                                <xsl:call-template name="style-selection"/><!-- This template exists in global/theme-style.xsl -->
                            </td>
                        </tr>

                    </table>
                </td>
            </tr>
			<tr>
				<td><img border="0" height="15" width="1" src="{$SPACER}" alt="" title="" /></td>
			</tr>
        </xsl:if>
        <xsl:if test="count(curriculum[@type = 'online']) = 0">
        	<tr>
        		<td><br/></td>
        	</tr>
        </xsl:if>        
        <tr>
            <th class="th">Resources</th>
        </tr>
        <xsl:apply-templates select="curriculum[@type != 'online']" />
    </table>

    </form>

</xsl:template>

<xsl:template match="curriculum">

    <xsl:variable name = "bottomStyle">
        <xsl:if test = "position() = last()">
            -bottom
        </xsl:if>
    </xsl:variable>

    <xsl:variable name = "HREF">
        <xsl:choose>
            <xsl:when test="@type = 'online'">javascript:resolveLink('curriculumForm', '<xsl:value-of select="@reference" />', 'curriculum', ',')</xsl:when>
            <xsl:when test="@type = 'file'"><xsl:value-of select="$workerActionURL" />&amp;curriculumID=<xsl:value-of select="@id" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="@reference" /></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
	
	<xsl:variable name = "CURRENT_ID">
		<xsl:value-of select="@id" />
	</xsl:variable>
	
	<xsl:variable name = "HREF_INSTRUCTOR">
        javascript:resolveLink('curriculumForm', '<xsl:value-of select="../instructor-course-list/curriculum[@id=$CURRENT_ID]/@reference" />', 'curriculum', ',')
    </xsl:variable>
	
    <xsl:variable name = "TARGET">
        <xsl:choose>
            <xsl:when test="@type = 'online'">_self</xsl:when>
            <xsl:otherwise>_blank</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <tr>
        <td class="table-content-single{$bottomStyle}">
        <xsl:choose>
            <!-- If HREF is not null ok to make it a link -->
            <xsl:when test="$HREF != 'null'">
                <img border="0" src="{$NAV_IMAGE_PATH}/{@type}.gif" alt="{@type}" title="" />
                <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
                <a href="{$HREF}" target="{$TARGET}" title="View '{title}' curriculum" onmouseover="swapImage('curriculumGoImage{position()}','channel_view_active.gif')" onmouseout="swapImage('curriculumGoImage{position()}','channel_view_base.gif')">
                    <xsl:value-of select="title" />
                    <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to '{title}' curriculum" title="'View' icon linking to '{title}' curriculum" align="absmiddle" name="curriculumGoImage{position()}" id="curriculumGoImage{position()}"/>
                </a>
            </xsl:when>
            <!-- Otherwise, if HREF is null then online curriculum is unavailable for some reason (i.e. Virtuoso is temporarily down) -->
            <xsl:otherwise>
                    <img border="0" src="{$NAV_IMAGE_PATH}/{@type}.gif" alt="{@type}" title="" />
                    <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
                    <xsl:value-of select="title" />
                    <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif"
                        alt="Inactive 'View' icon indicating the '{title}' curriculum is currently unavailable"
                        title="Inactive 'View' icon indicating the '{title}' curriculum is currently unavailable"
                        align="absmiddle" name="curriculumGoImage{position()}" id="curriculumGoImage{position()}"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
        <xsl:when test="$removeCurriculum = 'Y'">
            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
            <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=remove&amp;id={@id}&amp;title={./title}" title="Delete '{title}' from the offering" onmouseover="swapImage('curriculumDeleteImage{position()}','channel_delete_active.gif')" onmouseout="swapImage('curriculumDeleteImage{position()}','channel_delete_base.gif')">
                <img border="0" src= "{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Delete' icon linking to the Delete Confirmation view for '{title}' curriculum" title="'Delete' icon linking to the Delete Confirmation view for '{title}' curriculum" align="absmiddle" name="curriculumDeleteImage{position()}" id="curriculumDeleteImage{position()}"/>
            </a>
        </xsl:when>
        <xsl:otherwise>
            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif" alt="Inactive 'Delete' icon indicating permission to delete '{title}' is not granted" title="Inactive 'Delete' icon indicating permission to delete '{title}' is not granted" align="absmiddle" />
        </xsl:otherwise>
        </xsl:choose>
		<br/>
		<!-- Testing viewInstructorNotes permissions -->
		<xsl:if test="$viewInstructorNotes = 'Y' and ../instructor-course-list/curriculum[@id=$CURRENT_ID] and @type = 'online'">
				<img border="0" height="1" width="19" src="{$SPACER}" alt="" title="" />
	              <a href="{$HREF_INSTRUCTOR}" target="{$TARGET}" title="View '{title}' curriculum with Instructor Notes" onmouseover="swapImage('curriculumInstructorGoImage{position()}','channel_view_active.gif')" onmouseout="swapImage('curriculumInstructorGoImage{position()}','channel_view_base.gif')">
	                  <xsl:value-of select="title" /> (Instructor Notes)
	                  <img border="0" height="1" width="3" src="{$SPACER}" alt="" title="" />
	                  <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon linking to '{title}' curriculum with Instructor Notes" title="'View' icon linking to '{title}' curriculum with Instructor Notes" align="absmiddle" name="curriculumInstructorGoImage{position()}" id="curriculumInstructorGoImage{position()}"/>
	              </a>
		</xsl:if>
        </td>
    </tr>
		
</xsl:template>

</xsl:stylesheet>
