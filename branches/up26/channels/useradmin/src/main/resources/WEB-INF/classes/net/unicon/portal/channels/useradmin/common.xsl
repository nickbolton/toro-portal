<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Include -->
    <xsl:import href="../global/global.xsl"/>
    <xsl:include href="../global/toolbar.xsl"/>
    <!-- parameters -->
    <xsl:param name="baseActionURL"/>
    <xsl:param name="instanceId"/>
    <xsl:param name="current_command"/>
    <xsl:param name="in_use">false</xsl:param>
    <xsl:param name="first_name"/>
    <xsl:param name="last_name"/>
    <xsl:param name="email"/>
    <xsl:param name="user_name"/>
    <xsl:param name="deleteCommand"/>
    <xsl:param name="deleteConfirmationParam"/>
    <xsl:param name="addedUserName"/>
    <xsl:param name="updatedGroupUser"/>
    <xsl:param name="roleLabel"/>
    <xsl:param name="roleID"/>
    <!-- activities -->
    <xsl:param name="addUser"/>
    <xsl:param name="editUser"/>
    <xsl:param name="deleteUser"/>
    <xsl:param name="searchUser"/>
    <xsl:param name="changePassword"/>
    <xsl:param name="editPermissions"/>
    <xsl:param name="changeEmail"/>
    <!-- catalog parameters -->
    <xsl:param name="catChannel">userAdmin</xsl:param> <!-- Catalog: for form id uniqueness and identifying which search inputs to insert - See Catalog templates in global.xsl -->

    <!-- Common -->
    <xsl:template name="links">
    
      	<div class="portlet-toolbar-container">
      	
      		<xsl:choose>
				<xsl:when test="$current_command = 'default'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Edit My Profile</xsl:with-param>
						<xsl:with-param name="imagePath">channel_admin_active</xsl:with-param>								
					</xsl:call-template>							
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Edit My Profile</xsl:with-param>
						<xsl:with-param name="imagePath">channel_admin_active</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/></xsl:with-param>								
					</xsl:call-template>							
				</xsl:otherwise>
			</xsl:choose>
			
      		<xsl:choose>
      			<xsl:when test="$searchUser = 'Y'">
					<xsl:choose>
						<xsl:when test="$current_command = 'search'">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Search Users</xsl:with-param>
								<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
							</xsl:call-template>							
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Search Users</xsl:with-param>
								<xsl:with-param name="imagePath">channel_view_active</xsl:with-param>
								<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=search&amp;servant_command=cancel</xsl:with-param>								
							</xsl:call-template>							
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
			</xsl:choose>      			
      			
			<xsl:choose>
				<xsl:when test="$addUser = 'Y'">
					<xsl:choose>
						<xsl:when test="$current_command = 'add'">
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Add User</xsl:with-param>
								<xsl:with-param name="imagePath">channel_add_active</xsl:with-param>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="channel-link-generic">
								<xsl:with-param name="title">Add User</xsl:with-param>
								<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=add&amp;servant_command=cancel</xsl:with-param>
								<xsl:with-param name="imagePath">channel_add_active</xsl:with-param>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
            </xsl:choose>			      			
            <!--
			<xsl:choose>
				<xsl:when test="$editPermissions = 'Y'">
					<xsl:call-template name="channel-link-generic">
						<xsl:with-param name="title">Edit Preferences</xsl:with-param>
						<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>
						<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?servant_command=editPermissions</xsl:with-param>
					</xsl:call-template>
				</xsl:when>
			</xsl:choose>            
			-->
	  	</div>    	
   
    </xsl:template>
    
    <xsl:template name="page-links">
    	<xsl:choose>
			<xsl:when test="$changePassword = 'Y'">
				<xsl:choose>
					<xsl:when test="$current_command = 'changePassword'">
						<xsl:call-template name="channel-link-generic2">
							<xsl:with-param name="title">Edit My Password</xsl:with-param>
							<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>								
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="channel-link-generic2">
							<xsl:with-param name="title">Edit My Password</xsl:with-param>
							<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>
							<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=changePassword&amp;servant_command=cancel</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
		<div class="tool-divider">|</div>
		<xsl:choose>
			<xsl:when test="$changeEmail = 'Y'">
				<xsl:choose>
					<xsl:when test="$current_command = 'changeEmail'">
						<xsl:call-template name="channel-link-generic2">
							<xsl:with-param name="title">Edit Email</xsl:with-param>
							<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>								
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="channel-link-generic2">
							<xsl:with-param name="title">Edit My Email Address</xsl:with-param>
							<xsl:with-param name="imagePath">channel_edit_active</xsl:with-param>	
							<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?command=changeEmail&amp;servant_command=cancel</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
    </xsl:template>
    
    <xsl:template name="addEditJs">
        <script language="JavaScript" type="text/javascript" src="javascript/admin/UserAdminChannel/autoForm.js"/>
        <script language="JavaScript1.2" type="text/javascript">
        isNumber = function(val)
        {
            if (val == null) return false;
            if (val.search(/^[0-9]+$/) != 0) return false;
            return true;
        }
        </script>
    </xsl:template>
</xsl:stylesheet>
