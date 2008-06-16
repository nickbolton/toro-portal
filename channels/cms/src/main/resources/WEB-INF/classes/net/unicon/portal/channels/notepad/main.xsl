<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <xsl:template match="/">
        <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/>
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>     
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>     
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>     
        <parameter name="ID"><xsl:value-of select="$ID" /></parameter>     
        <parameter name="addCommand"><xsl:value-of select="$addCommand" /></parameter>     
        <parameter name="editCommand"><xsl:value-of select="$editCommand" /></parameter>     
        <parameter name="deleteCommand"><xsl:value-of select="$deleteCommand" /></parameter>     
        <parameter name="deleteConfirmationParam"><xsl:value-of select="$deleteConfirmationParam" /></parameter>     
        <parameter name="sortby"><xsl:value-of select="$sortby" /></parameter>     
        <parameter name="order"><xsl:value-of select="$order" /></parameter>     
    </textarea>
 -->
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="user-notes">
        <xsl:call-template name="links"/>

        <!-- UniAcc: Data Table -->
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <xsl:choose>
                    <xsl:when test="$sortby = '' or ($sortby = 'date' and $order='asc')">
                        <th class="th-left" id="Date">
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortup_selected.gif" alt="Triangle pointing up showing content is currently sorted by date in ascending order" title="Triangle pointing up showing content is currently sorted by date in ascending order" align="absmiddle" name="noteDateSortUpImage" id="noteDateSortUpImage"/>
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                            <a href="{$baseActionURL}?sortby=date&amp;order=desc" title="Sort by date in descending order" onmouseover="swapImage('noteDateSortDownImage','channel_sortdown_active.gif')" onmouseout="swapImage('noteDateSortDownImage','channel_sortdown_base.gif')" class="sort">
                                Date
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortdown_base.gif" alt="Triangle pointing down linking to content sorted by date in descending order" title="Triangle pointing down linking to content sorted by date in descending order" align="absmiddle" name="noteDateSortDownImage" id="noteDateSortDownImage"/>
                            </a>
                        </th>
                    </xsl:when>
                    <xsl:when test="$sortby = 'date' and $order='desc'">
                        <th class="th-left" id="Date">
                            <a href="{$baseActionURL}?sortby=date&amp;order=asc" title="Sort by date in ascending order" onmouseover="swapImage('noteDateSortUpImage','channel_sortup_active.gif')" onmouseout="swapImage('noteDateSortUpImage','channel_sortup_base.gif')" class="sort">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortup_base.gif" alt="Triangle pointing up linking to content sorted by date in ascending order" title="Triangle pointing up linking to content sorted by date in ascending order" align="absmiddle" name="noteDateSortUpImage" id="noteDateSortUpImage"/>
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                Date
                            </a>
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortdown_selected.gif" alt="Triangle pointing down showing content is currently sorted by date in descending order" title="Triangle pointing down showing content is currently sorted by date in descending order" align="absmiddle" name="noteDateSortDownImage" id="noteDateSortDownImage"/>
                        </th>
                    </xsl:when>
                    <xsl:otherwise>
                        <th class="th-left" id="Date">
                            <a href="{$baseActionURL}?sortby=date&amp;order=asc" title="Sort by date in ascending order" onmouseover="swapImage('noteDateSortUpImage','channel_sortup_active.gif')" onmouseout="swapImage('noteDateSortUpImage','channel_sortup_base.gif')" class="sort">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortup_base.gif" alt="Triangle pointing up linking to content sorted by date in ascending order" title="Triangle pointing up linking to content sorted by date in ascending order" align="absmiddle" name="noteDateSortUpImage" id="noteDateSortUpImage"/>
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                Date
                            </a>
                            <a href="{$baseActionURL}?sortby=date&amp;order=desc" title="Sort by date in descending order" onmouseover="swapImage('noteDateSortDownImage','channel_sortdown_active.gif')" onmouseout="swapImage('noteDateSortDownImage','channel_sortdown_base.gif')">
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortdown_base.gif" alt="Triangle pointing down linking to content sorted by date in descending order" title="Triangle pointing down linking to content sorted by date in descending order" align="absmiddle" name="noteDateSortDownImage" id="noteDateSortDownImage"/>
                            </a>
                        </th>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="$sortby = 'title' and $order='asc'">
                        <th class="th-right" id="Title">
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortup_selected.gif" alt="Triangle pointing up showing content is currently sorted by title in ascending order" title="Triangle pointing up showing content is currently sorted by title in ascending order" align="absmiddle" name="noteTitleSortUpImage" id="noteTitleSortUpImage"/>
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                            <a href="{$baseActionURL}?sortby=title&amp;order=desc" title="Sort by title in descending order" onmouseover="swapImage('noteTitleSortDownImage','channel_sortdown_active.gif')" onmouseout="swapImage('noteTitleSortDownImage','channel_sortdown_base.gif')" class="sort">
                                Title
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortdown_base.gif" alt="Triangle pointing down linking to content sorted by title in descending order" title="Triangle pointing down linking to content sorted by title in descending order" align="absmiddle" name="noteTitleSortDownImage" id="noteTitleSortDownImage"/>
                            </a>
                        </th>
                    </xsl:when>
                    <xsl:when test="$sortby = 'title' and $order='desc'">
                        <th class="th-right" id="Title">
                            <a href="{$baseActionURL}?sortby=title&amp;order=asc" title="Sort by title in ascending order" onmouseover="swapImage('noteTitleSortUpImage','channel_sortup_active.gif')" onmouseout="swapImage('noteTitleSortUpImage','channel_sortup_base.gif')" class="sort">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortup_base.gif" alt="Triangle pointing up linking to content sorted by title in ascending order" title="Triangle pointing up linking to content sorted by date in ascending order" align="absmiddle" name="noteTitleSortUpImage" id="noteTitleSortUpImage"/>
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                Title
                            </a>
                            <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                            <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortdown_selected.gif" alt="Triangle pointing down showing content is currently sorted by title in descending order" title="Triangle pointing down showing content is currently sorted by title in descending order" align="absmiddle" name="noteTitleSortDownImage" id="noteTitleSortDownImage"/>
                        </th>
                    </xsl:when>
                    <xsl:otherwise>
                        <th class="th-right" id="Title">
                            <a href="{$baseActionURL}?sortby=title&amp;order=asc" title="Sort by title in ascending order" onmouseover="swapImage('noteTitleSortUpImage','channel_sortup_active.gif')" onmouseout="swapImage('noteTitleSortUpImage','channel_sortup_base.gif')" class="sort">
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortup_base.gif" alt="Triangle pointing up linking to content sorted by title in ascending order" title="Triangle pointing up linking to content sorted by title in ascending order" align="absmiddle" name="noteTitleSortUpImage" id="noteTitleSortUpImage"/>
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                Title
                            </a>
                            <a href="{$baseActionURL}?sortby=title&amp;order=desc" title="Sort by title in descending order" onmouseover="swapImage('noteTitleSortDownImage','channel_sortdown_active.gif')" onmouseout="swapImage('noteTitleSortDownImage','channel_sortdown_base.gif')">
                                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_sortdown_base.gif" alt="Triangle pointing down linking to content sorted by title in descending order" title="Triangle pointing down linking to content sorted by title in descending order" align="absmiddle" name="noteTitleSortDownImage" id="noteTitleSortDownImage"/>
                            </a>
                        </th>
                    </xsl:otherwise>
                </xsl:choose>
            </tr>
            <xsl:apply-templates select="note"/>
        </table>
    </xsl:template>
    <xsl:template match="note">
        <xsl:variable name="bottomStyle">
            <xsl:if test="position() = last()">-bottom</xsl:if>
        </xsl:variable>
        <tr>
            <td class="table-light-left{$bottomStyle}" align="center" nowrap="nowrap" headers="Date">
                <xsl:value-of select="@date"/>
            </td>
            <td class="table-content-right{$bottomStyle}" headers="Title">
                <a href="{$baseActionURL}?command=view&amp;ID={@id}&amp;sortby={$sortby}&amp;order={$order}" title="View note" onmouseover="swapImage('noteViewImage{@id}','channel_view_active.gif')" onmouseout="swapImage('noteViewImage{@id}','channel_view_base.gif')">
                    <xsl:value-of select="note-title"/>
                    <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif" alt="'View' icon: view of '{note-title}' note" title="'View' icon: view of '{note-title}' note" align="absmiddle" name="noteViewImage{@id}" id="noteViewImage{@id}"/>
                </a>
                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                <a href="{$baseActionURL}?command=edit&amp;ID={@id}&amp;sortby={$sortby}&amp;order={$order}" title="Edit note" onmouseover="swapImage('noteEditImage{@id}','channel_edit_active.gif')" onmouseout="swapImage('noteEditImage{@id}','channel_edit_base.gif')">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" alt="'Edit' icon: edit '{note-title}' note" title="'Edit' icon: edit '{note-title}' note" align="absmiddle" name="noteEditImage{@id}" id="noteEditImage{@id}"/>
                </a>
                <img height="1" width="3" src="{$SPACER}" border="0" alt="" title=""/>
                <a href="{$baseActionURL}?command=delete&amp;ID={@id}&amp;sortby={$sortby}&amp;order={$order}" title="Delete note" onmouseover="swapImage('noteDeleteImage{@id}','channel_delete_active.gif')" onmouseout="swapImage('noteDeleteImage{@id}','channel_delete_base.gif')">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif" alt="'Delete' icon: deletion of '{note-title}' note" title="'Delete' icon: deletion of '{note-title}' note" align="absmiddle" name="noteDeleteImage{@id}" id="noteDeleteImage{@id}"/>
                </a>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
