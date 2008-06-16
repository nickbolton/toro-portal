<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />
<xsl:strip-space elements="*"/>

<xsl:param name="skin" select="'academus'"/>
<xsl:param name="baseActionURL"/>
<xsl:param name="pageSize"/>
<xsl:param name="currentPage"/>
<xsl:param name="lastPage"/>
<xsl:param name="pageChannel"/>

<!-- Set Variable Names for image links -->
<xsl:variable name = "SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/<xsl:value-of select="$skin"/>/skin</xsl:variable>
<xsl:variable name = "CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/<xsl:value-of select="$skin"/>/controls</xsl:variable>
<xsl:variable name = "NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
<xsl:variable name = "SPACER"><xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>

<!-- rollover JS -->
<xsl:template name="commonJS">
    <script language="JavaScript1.2" type="text/javascript">
        window.focus(); // seems to be necessary to get Mozilla to display image title attributes
        function swapImage(imageTarget,imageSrc)
        {
            if (document.getElementById &amp;&amp; document.getElementById(imageTarget))
            {
                document.getElementById(imageTarget).src = '<xsl:value-of select="$CONTROLS_IMAGE_PATH" />/'+imageSrc;
            }
        }
    </script>
</xsl:template>

<!-- Function to escape a certain character for a given text string.
Requires arguments named "textvalue" and "char".
-->
<xsl:template name="escapeChar">
    <xsl:param name="TEXTVALUE"/>
    <xsl:param name="CHAR"/>
    <xsl:choose>
        <xsl:when test="contains($TEXTVALUE, $CHAR)">
            <xsl:value-of select="substring-before($TEXTVALUE, $CHAR)"/>
            <xsl:text/>\<xsl:value-of disable-output-escaping="yes" select="$CHAR"/><xsl:text/>
            <xsl:call-template name="escapeChar">
                <xsl:with-param name="TEXTVALUE" select="substring-after($TEXTVALUE, $CHAR)"/>
                <xsl:with-param name="CHAR" select="$CHAR"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of disable-output-escaping="yes" select="$TEXTVALUE"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="pageForm">
<form name="{$pageChannel}PageForm" action="{$baseActionURL}" method="post">
  <input type="hidden" name="command" value="main" />
  <input type="hidden" name="sub-command" value="submit-page" />
  <input type="hidden" name="Submit" value="" />
  <input type="hidden" name="currentPage" value="{$currentPage}" />

  <table cellpadding="0" cellspacing="0" border="0">
  <tr>
      <td>

        <table class="uportal-text">
            <xsl:call-template name="pageControlRow" />
        </table>

      </td>
  </tr>
  </table>
</form>
</xsl:template>

<xsl:template name="pageControlRow">
    <tr>
        <td align='center'>
            <xsl:call-template name="pageControls"/>
        </td>
    </tr>
</xsl:template>

<xsl:template name="pageControls">

    <xsl:choose>
    <xsl:when test="$currentPage &gt; 1">
        <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_first_base.gif"
        name="Submit" id="{$pageChannel}FirstPage" alt="Go to the first page"
        title="Go to the first page" align="absmiddle"
        onmouseover="swapImage('{$pageChannel}FirstPage','channel_page_first_active.gif')"
        onmouseout="swapImage('{$pageChannel}FirstPage','channel_page_first_base.gif')"
        onclick="document.{$pageChannel}PageForm.Submit.value = 'first'" />
        <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_prev_base.gif"
        name="Submit" id="{$pageChannel}PrevPage" alt="Go to the previous page"
        title="Go to the previous page" align="absmiddle"
        onmouseover="swapImage('{$pageChannel}PrevPage','channel_page_prev_active.gif')"
        onmouseout="swapImage('{$pageChannel}PrevPage','channel_page_prev_base.gif')"
        onclick="document.{$pageChannel}PageForm.Submit.value = 'previous'" />
    </xsl:when>
    <xsl:otherwise>
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_first_inactive.gif" align="absmiddle"
        alt="Go to first page button, inactive because you are currently on the first page"
        title="Go to first page button, inactive because you are currently on the first page" />
        <img height="1" width="5" src="{$SPACER}" alt="" border="0"/>
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_prev_inactive.gif" align="absmiddle"
        alt="Previous page button, inactive because you are currently on the first page"
        title="Previous page button, inactive because you are currently on the first page" />
    </xsl:otherwise>
    </xsl:choose>

    Page<xsl:call-template name="pageMenu"/>
    <xsl:choose>
    <xsl:when test="$lastPage = 1">
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_go_inactive.gif" align="absmiddle"
        alt="Go to page button, inactive because there is only one page"
        title="Go to page button, inactive because there is only one page" />
    </xsl:when>
    <xsl:otherwise>
        <input type="image" class="image" align="absmiddle"
        src="{$CONTROLS_IMAGE_PATH}/channel_page_go_base.gif"
        name="Submit" id="{$pageChannel}GoToPage"
        alt="Go to page button, to go to the page selected from the drop-down menu"
        title="Go to page button, to go to the pageselected from the drop-down menu"
        onmouseover="swapImage('{$pageChannel}GoToPage','channel_page_go_active.gif')"
        onmouseout="swapImage('{$pageChannel}GoToPage','channel_page_go_base.gif')"
        onclick="document.{$pageChannel}PageForm.Submit.value = 'goto'" />
    </xsl:otherwise>
    </xsl:choose>

    <xsl:choose>
    <xsl:when test="$currentPage &lt; $lastPage">
        <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_next_base.gif"
        name="Submit" id="{$pageChannel}NextPage" alt="Next page button, to go to the next page"
        title="Next page button, to go to the next page" align="absmiddle"
        onmouseover="swapImage('{$pageChannel}NextPage','channel_page_next_active.gif')"
        onmouseout="swapImage('{$pageChannel}NextPage','channel_page_next_base.gif')"
        onclick="document.{$pageChannel}PageForm.Submit.value = 'next'" />
        <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_last_base.gif"
        name="Submit" id="{$pageChannel}LastPage" alt="Last page button, to go to the last page of users"
        title="Last page button, to go to the last page of users" align="absmiddle"
        onmouseover="swapImage('{$pageChannel}LastPage','channel_page_last_active.gif')"
        onmouseout="swapImage('{$pageChannel}LastPage','channel_page_last_base.gif')"
        onclick="document.{$pageChannel}PageForm.Submit.value = 'last'" />
    </xsl:when>
    <xsl:otherwise>
        <img height="1" width="5" src="{$SPACER}" alt="" border="0"/>
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_next_inactive.gif" align="absmiddle"
        alt="Next page button, inactive because you are currently on the last page"
        title="Next page button, inactive because you are currently on the last page" />
        <img height="1" width="5" src="{$SPACER}" alt="" border="0"/>
        <img src="{$CONTROLS_IMAGE_PATH}/channel_page_last_inactive.gif" align="absmiddle"
        alt="Go to last page button, inactive because you are currently on the last page"
        title="Go to last page button, inactive because you are currently on the last page" />
    </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="pageMenu">
<!-- Builds the drop-down selector of pages -->
    <select name="selectedPageNumber">
      <xsl:call-template name="pageMenuOption">
        <xsl:with-param name="pageNumber" select="1"/>
      </xsl:call-template>
    </select> of <xsl:value-of select="$lastPage"/>
</xsl:template>

<xsl:template name="pageMenuOption">
<!-- Builds each option within the drop-down of pageMenu -->
    <xsl:param name="pageNumber"/>
    <option>
      <xsl:if test="$currentPage = $pageNumber">
        <xsl:attribute name="selected">selected</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="$pageNumber"/>
    </option>
    <xsl:if test="$pageNumber &lt; $lastPage"> <!-- Recursive call if it has not reached the max number of pages -->
      <xsl:call-template name="pageMenuOption">
        <xsl:with-param name="pageNumber" select="$pageNumber + 1"/>
      </xsl:call-template>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
