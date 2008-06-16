<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!-- Import the bookmark tree stylesheet -->
    <xsl:import href="bookmark_tree_html.xsl"/>

    <xsl:import href="common.xsl"/>

    <!-- Make sure that XHTML is being output to facilitate caching -->
    <xsl:output method="xml" indent="yes"/>

    <!-- Take the baseActionURL and the location of the images as parameters -->
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="imagesURL">media/net/unicon/channels/bookmarks/</xsl:param>

    <!-- Find out whether you are editing a folder or a bookmark -->
    <xsl:param name="EditMode">EditBookmark</xsl:param>
	<xsl:param name="NodeId">null</xsl:param>
	<xsl:param name="ParentNodeId">null</xsl:param>

    <xsl:template match="/">
		<!--<form>
			<textarea>
				<xsl:copy-of select = "*"/>
				<parameters>
					<parameter name="EditMode"><xsl:value-of select="$EditMode" /></parameter>
					<parameter name="NodeId"><xsl:value-of select="$NodeId" /></parameter>
					<parameter name="ParentNodeId"><xsl:value-of select="$ParentNodeId" /></parameter>
				</parameters>
			</textarea>
		</form> -->

        <xsl:call-template name="autoFormJS"/>
        <div class="page-title">Edit Bookmark</div>
        <form action="{$baseActionURL}?command={$EditMode}" method="post" onsubmit="return(checkBookmarkFormSubmit(this));">
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <th class="th-top-single">Select Hierarchy</th>
                </tr>
            </table>
            <xsl:call-template name="BookmarkTree">
                <xsl:with-param name="TreeMode">
                    <xsl:value-of select="$EditMode"/>
                </xsl:with-param>
            </xsl:call-template>
            <!--UniAcc: Layout Table -->
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <th class="th-top-single">Edit <xsl:value-of select="substring-after($EditMode,'Edit')"/></th>
                </tr>
            </table>
            <xsl:choose>
                <xsl:when test="$EditMode='EditFolder'">
                    <!--UniAcc: Layout Table -->
                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-EditFolderName">Folder Name:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <input type="text" name="FolderTitle" class="text" id="CBookmark-EditFolderName"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-nav" colspan="2">
                            	<!-- This form keys on SubmitButton instead of submitValue -->
 								<input type="hidden" name="SubmitButton" value="Edit"/>
                                <input type="submit" name="submitValue" value="Edit" onclick="this.form.isCancel=false;" class="uportal-button"/>
                                <input type="submit" name="submitValue" value="Cancel" onclick="this.form.SubmitButton.value=this.value;this.form.isCancel=true;" class="uportal-button"/>
                            </td>
                        </tr>
                    </table>
                </xsl:when>
                <xsl:when test="$EditMode='EditBookmark'">
                    <!--UniAcc: Layout Table -->
                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-EditBookmarkTitleT1">Bookmark Title:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <input type="text" name="BookmarkTitle" class="text" id="CBookmark-EditBookmarkTitleT1" value="{//bookmark[@id=$NodeId]/title}" maxlength="80"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-EditBookmarkurlT1">URL:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <input type="text" name="BookmarkURL" class="text" id="CBookmark-EditBookmarkurlT1" value="{//bookmark[@id=$NodeId]/@href}"/>
                                <br/>
                                <span class="uportal-text-small">You must include &quot;http://&quot; for the URL to be valid.</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="table-light-left" nowrap="nowrap">
                                <label for="CBookmark-EditBookmarkDescTA1">Description:</label>
                            </td>
                            <td class="table-content-right" width="100%">
                                <textarea rows="5" cols="20" name="BookmarkDescription" class="text" id="CBookmark-EditBookmarkDescTA1">
									<xsl:value-of select="//bookmark[@id=$NodeId]/desc"/>
								</textarea> 
                            </td>
                        </tr>
                        <tr>
                            <td class="table-nav" colspan="2">
                            	<!-- This form seems to key on submitValue instead of SubmitButton -->
							 	<input type="hidden" name="SubmitButton" value="Edit"/>
                                <input type="submit" name="submitValue" value="Submit" onclick="this.form.isCancel=false;" class="uportal-button"></input>
                                <input type="submit" name="submitValue" value="Cancel" onclick="this.form.SubmitButton.value=this.value;this.form.isCancel=true;" class="uportal-button"></input>
                            </td>
                        </tr>
                    </table>
                </xsl:when>
            </xsl:choose>
        </form>
    </xsl:template>
</xsl:stylesheet>
