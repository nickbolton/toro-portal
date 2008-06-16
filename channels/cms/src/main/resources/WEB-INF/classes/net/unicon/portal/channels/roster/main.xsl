<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />

<xsl:include href="common.xsl"/>

<xsl:template match="/">
      <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>
        <parameter name="catPageSize"><xsl:value-of select="$catPageSize"/></parameter>
        <parameter name="catCurrentPage"><xsl:value-of select="$catCurrentPage"/></parameter>
        <parameter name="catLastPage"><xsl:value-of select="$catLastPage"/></parameter>
        <parameter name="catCurrentCommand"><xsl:value-of select="$catCurrentCommand"/></parameter>
        <parameter name="catChannel"><xsl:value-of select="$catChannel"/></parameter>
        <parameter name="firstName"><xsl:value-of select="$firstName"/></parameter>
        <parameter name="lastName"><xsl:value-of select="$lastName"/></parameter>
        <parameter name="userId"><xsl:value-of select="$userId"/></parameter>
        <parameter name="searchAndOr"><xsl:value-of select="$searchAndOr"/></parameter>
        <parameter name="offName"><xsl:value-of select="$offName"/></parameter>
        <parameter name="topicName"><xsl:value-of select="$topicName"/></parameter>
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>
        <parameter name="displayCommand"><xsl:value-of select="$displayCommand" /></parameter>
        <parameter name="editOfferingPermissions"><xsl:value-of select="$editOfferingPermissions" /></parameter>   
        <parameter name="editOfferingPermissionsCommand"><xsl:value-of select="$editOfferingPermissionsCommand" /></parameter>   
        <parameter name="editUserPermissions"><xsl:value-of select="$editUserPermissions" /></parameter>   
        <parameter name="editUserPermissionsCommand"><xsl:value-of select="$editUserPermissionsCommand" /></parameter>   
        <parameter name="enrollCommand"><xsl:value-of select="$enrollCommand" /></parameter>   
        <parameter name="enrollUser"><xsl:value-of select="$enrollUser" /></parameter>   
        <parameter name="enrollViewCommand"><xsl:value-of select="$enrollViewCommand" /></parameter>   
        <parameter name="importCommand"><xsl:value-of select="$importCommand" /></parameter>   
        <parameter name="offeringId"><xsl:value-of select="$offeringId" /></parameter>   
        <parameter name="pageCommand"><xsl:value-of select="$pageCommand" /></parameter>   
        <parameter name="resolveCommand"><xsl:value-of select="$resolveCommand" /></parameter>   
        <parameter name="roleId"><xsl:value-of select="$roleId" /></parameter>   
        <parameter name="roleName"><xsl:value-of select="$roleName" /></parameter>   
        <parameter name="searchCommand"><xsl:value-of select="$searchCommand" /></parameter>
        <parameter name="showViewAll"><xsl:value-of select="$showViewAll" /></parameter>
        <parameter name="skin"><xsl:value-of select="$skin" /></parameter>
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>   
        <parameter name="type"><xsl:value-of select="$type" /></parameter>   
        <parameter name="unenrollCommand"><xsl:value-of select="$unenrollCommand" /></parameter>   
        <parameter name="unenrollUser"><xsl:value-of select="$unenrollUser" /></parameter>   
        <parameter name="updateOfferingPermissionsCommand"><xsl:value-of select="$updateOfferingPermissionsCommand" /></parameter>   
        <parameter name="updateUserPermissionsCommand"><xsl:value-of select="$updateUserPermissionsCommand" /></parameter>   
        <parameter name="userId"><xsl:value-of select="$userId" /></parameter>   
        <parameter name="userIdParam"><xsl:value-of select="$userIdParam" /></parameter>   
        <parameter name="viewMemberCommand"><xsl:value-of select="$viewMemberCommand" /></parameter>   
        <parameter name="viewUserInfo"><xsl:value-of select="$viewUserInfo" /></parameter>   
        <parameter name="confirmUnenrollCommand"><xsl:value-of select="$confirmUnenrollCommand" /></parameter>
        <parameter name="searchUserCommand"><xsl:value-of select="$searchUserCommand"/></parameter>
    </textarea> -->

    <xsl:apply-templates />
</xsl:template>

  <xsl:template match="roster">
  
    <xsl:call-template name="links"/>

	<table cellpadding="0" cellspacing="0" border="0" width="100%">
	    <tr>
	        <!-- Make ID column visible based on permissions -->
	        <xsl:if test="$viewUserInfo = 'Y'">
	            <th class="th-top-left" scope="col">ID
	            </th>
	        </xsl:if>
	        <th class="th-top" scope="col">Name</th>
	        <th class="th-top" scope="col">Type</th>
	        <th class="th-top-right" scope="col">Status</th>
	    </tr>
	    <xsl:apply-templates select="user"/>
	    <xsl:if test="count(user) = 0">
	        <tr><td colspan="4" class="table-content-single-bottom" align="center"><xsl:value-of select="$emptyListMessage" /></td></tr>
	    </xsl:if>
	</table>

    
    <xsl:choose>
    <xsl:when test="$catCurrentCommand = 'enrollView'">
        <xsl:call-template name="catalog">
            <xsl:with-param name="catSearchFlag">N</xsl:with-param>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
        <xsl:call-template name="catalog" />
    </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="user">
      <!-- Variable to hold bottom style information to build correct style class name -->
    <xsl:variable name = "tagSuffix">
         <xsl:choose>
             <xsl:when test="position() = last()">-bottom</xsl:when>
           
             <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
    </xsl:variable>     
    <!-- Variable to hold left style information to build correct style class name -->
    <xsl:variable name = "firstColumn">
         <xsl:choose>
             <xsl:when test="$viewUserInfo = 'N'">-left</xsl:when>
           
             <xsl:otherwise></xsl:otherwise>
         </xsl:choose>
    </xsl:variable> 
    <!-- nameContent contains standard content for name column (regardless of permissions) -->    
    <xsl:variable name = "nameContent">
        <xsl:value-of select="lastname"/>, <xsl:value-of select="firstname"/>
        <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
    </xsl:variable>

    <tr>
    <!-- Make ID column visible based on permissions -->
        
        <xsl:if test = "$viewUserInfo = 'Y'">
            <td class="table-light-left{$tagSuffix}" align="center">
                <xsl:value-of select="@id"/>
            </td>
        </xsl:if>

          <td class="table-content{$firstColumn}{$tagSuffix}" align="left" width="100%">
            <xsl:choose>
              <xsl:when test="$viewUserInfo = 'Y' and $catCurrentCommand = 'page'">
                <a href="{$baseActionURL}?command={$viewMemberCommand}&amp;uid={@id}&amp;catPageSize={$catPageSize}" title="View this member's information"
                  onmouseover="swapImage('rosterViewMember{position()}Image','channel_view_active.gif')" 
                  onmouseout="swapImage('rosterViewMember{position()}Image','channel_view_base.gif')">
                <xsl:copy-of select = "$nameContent"/><img border="0" src=
                "{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
                alt="View this members information" title="View this member's information" align="absmiddle"
                name="rosterViewMember{position()}Image" id="rosterViewMember{position()}Image"/></a>
              </xsl:when>
              <xsl:when test="$catCurrentCommand = 'enrollView'">
                <xsl:copy-of select = "$nameContent"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select = "$nameContent"/><img border="0" src=
                "{$CONTROLS_IMAGE_PATH}/channel_view_inactive.gif"
                alt="Not permitted to view member information" title="Not permitted to view member information" align="absmiddle"/>
              </xsl:otherwise>
            </xsl:choose>
        </td>

      <!-- The stylesheet in the next TD will need to be dynamic, based upon the user's status -->
      <xsl:choose>
        <xsl:when test="status = 'Enrolled'">
          <td class="table-content{$tagSuffix}" align="center" nowrap="nowrap">

            <xsl:choose>
              <xsl:when test="$editUserPermissions = 'Y'">
                <a href="{$baseActionURL}?command={$editUserPermissionsCommand}&amp;uid={@id}&amp;catPageSize={$catPageSize}" title="Edit this member's permissions"
                  onmouseover="swapImage('rosterEditMemberPermissions{position()}Image','channel_edit_active.gif')" 
                  onmouseout="swapImage('rosterEditMemberPermissions{position()}Image','channel_edit_base.gif')">
                    <xsl:value-of select="role"/>
                    <img height="1" width="3" src="{$SPACER}"
                    alt="" border="0"/>
                    <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
                    alt="Edit this member's permissions" title="Edit this member's permissions" align="absmiddle"
                    name="rosterEditMemberPermissions{position()}Image" id="rosterEditMemberPermissions{position()}Image"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="role"/>
                <img height="1" width="3" src="{$SPACER}"
                alt="" border="0"/>
                <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
                alt="Not permitted to edit member permissions" title="Not permitted to edit member permissions" align="absmiddle"/>
              </xsl:otherwise>
            </xsl:choose>
   
          </td>
          <td class="enrolled{$tagSuffix}" align="center" nowrap="nowrap">
            <xsl:choose>
              <xsl:when test="$unenrollUser = 'Y' and $enrollmentModel != 'sis'">
                <a href="{$baseActionURL}?command={$confirmUnenrollCommand}&amp;uid={@id}&amp;offeringIdParam={$offeringId}&amp;catPageSize={$catPageSize}" title="Unenroll this member"
                  onmouseover="swapImage('rosterUnenrollMember{position()}Image','channel_delete_active.gif')" 
                  onmouseout="swapImage('rosterUnenrollMember{position()}Image','channel_delete_base.gif')"><xsl:value-of select="status"/>
                <img height="1" width="3" src="{$SPACER}"
                alt="" border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                alt="Unenroll this member" title="Unenroll this member" align="absmiddle"
                name="rosterUnenrollMember{position()}Image" id="rosterUnenrollMember{position()}Image"/></a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="status"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </xsl:when>
        <xsl:when test="status = 'Unenrolled'">
            <td class="table-content{$tagSuffix}" align="center">
              <form name="rosterEnrollForm{position()}" action="{$baseActionURL}" method="post" style="margin-bottom:0px"><!--// REMOVE STYLE ATTRIBUTE IF/WHEN GLOBAL CSS DEFS HANDLE IT. //-->
                <input type="hidden" name="targetChannel" value="{$targetChannel}" />
                <input type="hidden" name="catPageSize" value="{$catPageSize}" />
                <input type="hidden" name="command" value="{$enrollCommand}" />
                <input type="hidden" name="uid" value="{@id}" /><!--// User to enroll. //-->
                <input type="hidden" name="catSelectPage" value="{$catCurrentPage}" />
                <input type="hidden" name="firstName" value="{$firstName}" />
                <input type="hidden" name="lastName" value="{$lastName}" />
                <input type="hidden" name="userID" value="{$userID}" /><!--// Search/filter parameter. //-->
              <select name="roleId{@id}">
                <xsl:for-each select="/roster/role">
                  <xsl:choose>
                    <xsl:when test="@id = /roster/@defaultRole">
                      <option selected="selected" value="{@id}"><xsl:value-of select="."/></option>
                    </xsl:when>
                    <xsl:otherwise>
                      <option value="{@id}"><xsl:value-of select="."/></option>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </select>
              </form>
            </td>
            <td class="enrolled{$tagSuffix}" align="center" nowrap="nowrap">
              <xsl:choose>
                <xsl:when test="$enrollUser = 'Y' and $enrollmentModel != 'sis'">
                  <a href="javascript:document.rosterEnrollForm{position()}.submit()" title="Enroll this member"
                      onmouseover="swapImage('rosterEnrollMember{position()}Image','channel_add_active.gif')" 
                      onmouseout="swapImage('rosterEnrollMember{position()}Image','channel_add_base.gif')"><xsl:value-of select="status"/> 
                  <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                  <img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif" alt="Enroll this member" title="Enroll this member" align="absmiddle" name="rosterEnrollMember{position()}Image" id="rosterEnrollMember{position()}Image"/></a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="status"/>
                </xsl:otherwise>
              </xsl:choose>
            </td>
        </xsl:when>
        <xsl:when test="status = 'Pending'">
            <td class="table-content{$tagSuffix}" align="center">
                <xsl:value-of select="role"/>
            </td>
            <td class="pending{$tagSuffix}" align="center">
              <xsl:choose>
                <xsl:when test="$enrollUser = 'Y'">
                  <a href="{$baseActionURL}?command={$resolveCommand}&amp;uid={@id}&amp;offeringIdParam={$offeringId}&amp;catPageSize={$catPageSize}"
                      onmouseover="swapImage('rosterEnrollMember{position()}Image','channel_edit_active.gif')" 
                      onmouseout="swapImage('rosterEnrollMember{position()}Image','channel_edit_base.gif')"><xsl:value-of select="status"/> 
                  <img height="1" width="3" src=
            "/portal/{$SPACER}"
            alt="" border="0"/><img border="0" src=
            "/portal/{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
            alt="Resolve the pending request to join this offering" title="Resolve the pending request to join this offering" align="absmiddle"
            name="rosterEnrollMember{position()}Image" id="rosterEnrollMember{position()}Image"/></a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="status"/>
                </xsl:otherwise>
              </xsl:choose>
            </td>
        </xsl:when>
        <xsl:otherwise>
          <td class="table-content{$tagSuffix}" align="center">
            <xsl:value-of select="role"/>
          </td>
          <td class="table-content{$tagSuffix}" align="center">
              <xsl:value-of select="status"/>
          </td>
        </xsl:otherwise>
      </xsl:choose>
    </tr>

  </xsl:template>
  
</xsl:stylesheet>
