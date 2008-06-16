<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	
	<!-- Include Files -->
	<xsl:include href="common.xsl"/>
	<xsl:template match="/">
		<!-- <textarea rows="4" cols="100">
        <xsl:copy-of select = "*"/>
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>
        <parameter name="in_use"><xsl:value-of select="$in_use" /></parameter>
        <parameter name="first_name"><xsl:value-of select="$first_name" /></parameter>
        <parameter name="last_name"><xsl:value-of select="$last_name" /></parameter>
        <parameter name="email"><xsl:value-of select="$email" /></parameter>
        <parameter name="user_name"><xsl:value-of select="$user_name" /></parameter>
        <parameter name="deleteCommand"><xsl:value-of select="$deleteCommand" /></parameter>
        <parameter name="deleteConfirmationParam"><xsl:value-of select="$deleteConfirmationParam" /></parameter>
        <parameter name="addUser"><xsl:value-of select="$addUser" /></parameter>
        <parameter name="editUser"><xsl:value-of select="$editUser" /></parameter>
        <parameter name="deleteUser"><xsl:value-of select="$deleteUser" /></parameter>
        <parameter name="searchUser"><xsl:value-of select="$searchUser" /></parameter>
    </textarea> -->

    <xsl:call-template name="links"/>
    <!-- UniAcc: Layout Table -->
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <xsl:choose>
            <xsl:when test="$addedUserName != ''">
                <tr>
                    <td class="table-content-single-top">
                        <span class="uportal-channel-warning">The user (<span class="uportal-channel-strong">"<xsl:value-of select="$addedUserName"/>"</span>) was successfully added.</span>
                    </td>
                </tr>
            </xsl:when>
            <xsl:when test="$updatedGroupUser != ''">
                <tr>
                    <td class="table-content-single-top">
                        <span class="uportal-channel-warning">The user (<span class="uportal-channel-strong">"<xsl:value-of select="$updatedGroupUser"/>"</span>) had successful group modification.</span>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
        <tr>
            <td class="table-content-single-bottom">          
	            <h2 class="page-title">Edit My Profile</h2>	
            	<div class="page-toolbar-container">
            		<xsl:call-template name="page-links"/>
               	</div>
            </td>
		</tr>
   </table>
  </xsl:template>
</xsl:stylesheet>

