<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="resourceURL">default</xsl:param>

    <xsl:output method="html" indent="yes"/>
	
	<xsl:include href="../global/toolbar.xsl"/>
	
    <xsl:param name="current_command"/>
    <xsl:param name="offeringName"/>
    <xsl:param name="curriculumID"/>
    <xsl:param name="criteria"></xsl:param>
    <xsl:param name="isNew">false</xsl:param>
    <xsl:param name="isOpen">false</xsl:param>
    <!-- Permissions -->
    <xsl:param name="addCurriculum"/>
    <xsl:param name="removeCurriculum"/>

    <!-- Set Variable Names for image links -->
    <xsl:variable name="SKIN_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/skin</xsl:variable>
    <xsl:variable name="CONTROLS_IMAGE_PATH">media/org/jasig/portal/layout/tab-column/nested-tables/academus/controls</xsl:variable>
    <xsl:variable name="NAV_IMAGE_PATH">media/net/unicon/portal/channels/Navigation</xsl:variable>
    <xsl:variable name="GRADEBOOK_HEADER_PATH">media/net/unicon/flash/academus</xsl:variable>
    <xsl:variable name="SPACER">
        <xsl:value-of select="$SKIN_IMAGE_PATH"/>/transparent.gif</xsl:variable>

     <!-- Javascript include for form field validation -->             
     <xsl:template name="autoFormJS">                     
         <script language="JavaScript" type="text/javascript" src="javascript/AddressbookChannel/autoForm.js"/>             
     </xsl:template>

    <!-- Common -->
    <xsl:template name="links">
    	<xsl:param name="isServantChannelDisplay">no</xsl:param>
        <!--UniAcc: Layout Table -->
        
        <xsl:if test = "$isServantChannelDisplay='no'">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="portlet-toolbar-container" nowrap="nowrap">
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_options.gif" align="absmiddle" alt="Channel navigation section" title="Channel navigation section"/>
                </td>
                <td class="portlet-toolbar-container" valign="middle" width="100%" nowrap="nowrap">

                    <xsl:choose>
                        <xsl:when test="$sid = 'Personal'">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">View Addresses</xsl:with-param>
							</xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">View Addresses</xsl:with-param>
								<xsl:with-param name="URL"><xsl:value-of select="$goURL"/>=Personal&amp;uP_root=me</xsl:with-param>
							</xsl:call-template>                        
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:choose>
                        <xsl:when test="($sid = 'Contact') and ($isNew = 'true')">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Add Contact</xsl:with-param>
							</xsl:call-template>                         
						</xsl:when>
                        <xsl:when test="($sid = 'Contact') and ($isNew = 'false')">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Edit Contact</xsl:with-param>
							</xsl:call-template>                         
						</xsl:when>						
                        <xsl:otherwise>
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Add Contact</xsl:with-param>
								<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?sid=Personal&amp;do=newContact&amp;uP_root=me</xsl:with-param>
							</xsl:call-template>                        
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:choose>
                        <xsl:when test="($sid = 'Folder') and ($isNew = 'true')">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Add Group</xsl:with-param>
							</xsl:call-template>                        
                        </xsl:when>
                        <xsl:when test="($sid = 'Folder') and ($isNew = 'false')">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Edit Group</xsl:with-param>
							</xsl:call-template>                        
                        </xsl:when>                        
                        <xsl:otherwise>
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Add Group</xsl:with-param>
								<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?sid=Personal&amp;do=newFolder&amp;uP_root=me</xsl:with-param>
							</xsl:call-template>                        
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:choose>
                        <xsl:when test="$sid = 'Search'">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Search</xsl:with-param>
							</xsl:call-template>                        
                        </xsl:when>
                        <xsl:otherwise>
							<!--
							<form action="{$baseActionURL}" name="AddressBookForm" method="post">
							-->
                            <input type="hidden" name="sid" value="Peephole"/>
                            <input type="hidden" name="uP_root" value="me"/>
                            <input type="hidden" name="default" value="do~search"/>
                            <input type="hidden" name="criteria" value=""/>
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Search</xsl:with-param>
								<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?sid=Peephole&amp;do=search&amp;uP_root=me</xsl:with-param>
							</xsl:call-template>                            
                            <img border="0" height="1" width="3" src="{$SPACER}" alt="" title=""/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </tr>
        </table>
        </xsl:if>
    </xsl:template>
    
<xsl:template name="page-links">

	<xsl:choose>
		<xsl:when test="$sid = 'Import'">
			<xsl:call-template name="channel-link-generic2">
				<xsl:with-param name="title">Import Contacts</xsl:with-param>
			</xsl:call-template> 		
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="channel-link-generic2">
				<xsl:with-param name="title">Import Contacts</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$goURL"/>=Import&amp;uP_root=me</xsl:with-param>
			</xsl:call-template> 		
		</xsl:otherwise>
	</xsl:choose>

	<xsl:call-template name="channel-link-generic2">
		<xsl:with-param name="title">Export Contacts</xsl:with-param>
		<xsl:with-param name="URL"><xsl:value-of select="$resourceURL"/></xsl:with-param>
	</xsl:call-template> 
	
</xsl:template>  

</xsl:stylesheet>
