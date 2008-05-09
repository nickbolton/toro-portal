<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes" />


<xsl:param name="viewCommand"/>
<xsl:param name="unenrollCommand"/>
<xsl:param name="viewAllCommand"/>
<xsl:param name="enrollViewCommand"/>
<xsl:param name="importCommand"/>
<xsl:param name="submitCommand"/>
<xsl:param name="editPermissionsCommand"/>
<xsl:param name="userIdParam"/>

  <xsl:template match="user">
<xsl:call-template name="links"/>
<xsl:call-template name="autoFormJS"/>

<form name="rosterMainForm" action="{$baseActionURL}" method="post">
<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
<input type="hidden" name="catPageSize" value="{$catPageSize}" />
<input type="hidden" name="command" value="{$submitCommand}"></input>

<table>
<tr>
<th class="uportal-background-med">Property</th>
<th class="uportal-background-med">Value</th>
</tr>

<tr>
<td class="uportal-background-light">User ID</td>
<td class="uportal-background-light" style="text-align:center">
  <xsl:value-of select="@id"/>
</td>
</tr>

<tr>
<td class="uportal-background-light">User Name</td>
<td class="uportal-background-light" style="text-align:center">
  <xsl:value-of select="lastname"/>, <xsl:value-of select="firstname"/>
</td>
</tr>

<tr>
<td class="uportal-background-light">User Status</td>
<td class="uportal-background-light" style="text-align:center">
  <xsl:value-of select="status"/>
</td>
</tr>

<tr>
<td class="uportal-background-light">User Type</td>
<td class="uportal-background-light" style="text-align:center">
  <select name="userTypeOptions1" onchange="window.locationhref={$baseActionURL}?command={$editPermissionsCommand}&amp;{$userIdParam}={@id}">
    <xsl:apply-templates select="permissionType"/>
    <option>User Defined</option>
  </select>
</td>
</tr>

<tr>
<td class="uportal-background-light" style="vertical-align:top;">List of Channels</td>
<td class="uportal-background-light"><ul>
<li>Announcement Channel<ul>
<li><input name="viewAnnouncements" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Messages <a href="javascript:alert('This explains what the permission means... blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah');void(null);"><img border="0" height="16" width="16" src="{$CONTROLS_IMAGE_PATH}/help.gif" alt="This explains what the permission means... blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah"/></a></li>
<li><input name="addAnnouncements" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Add Messages</li>
<li><input name="editAnnouncements" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Edit Messages</li>
<li><input name="deleteAnnouncements" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete Messages</li>
</ul></li>
<li>Bookmark Channel<ul>
<li><input name="viewBookmarks" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Bookmarks and Folders</li>
<li><input name="addBookmarks" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Add Bookmarks and Folders</li>
<li><input name="deleteBookmarks" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete Bookmarks and Folders</li>
</ul></li>
<li>Calendar Channel<ul>
<li><input name="viewCalendar" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Calendar</li>
<li><input name="addEventsCalendar" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Add Events</li>
<li><input name="addRemindersCalendar" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Add Reminders</li>
<li><input name="editEvents" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Edit Events</li>
<li><input name="deleteEvents" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete Events</li>
</ul></li>
<li>Communication Tools Channel<ul>
<li><input name="viewChat" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Chat</li>
<li><input name="postChat" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Post to Chat</li>
<li><input name="moderateChat" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Moderate Chat</li>
</ul></li>
<li>Dropbox Channel<ul>
<li><input name="viewMember" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Member Specific</li>
<li><input name="viewOffering" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>View Offering Specific</li>
<li><input name="viewAll" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>View All</li>
<li><input name="DownloadMember" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>Download Member Specific</li>
<li><input name="DownloadOffering" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Download Offering Specific</li>
<li><input name="DownloadAll" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Download All</li>
<li><input name="UploadMember" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>Upload Member Specific</li>
<li><input name="UploadOffering" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Upload Offering Specific</li>
<li><input name="UploadAll" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Upload All</li>
<li><input name="DeleteMember" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>Delete Member Specific</li>
<li><input name="DeleteOffering" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete Offering Specific</li>
<li><input name="DeleteAll" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete All</li>
</ul></li>
<li>Forum Channel<ul>
<li><input name="viewForum" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Messages</li>
<li><input name="postForum" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Post Messages</li>
<li><input name="ModerateForum" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Moderate Messages</li>
<li><input name="deleteForum" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete Messages</li>
</ul></li>
<li>Gradebook Channel<ul>
<li><input name="viewOwnGrades" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Own Grades</li>
<li><input name="viewAllGrades" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View All Grades</li>
<li><input name="editScores" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Edit Scores</li>
<li><input name="addAssignment" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Add Assignment</li>
<li><input name="editAssignment" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Edit Assignment</li>
<li><input name="deleteAssignment" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Delete Assignment</li>
</ul></li>
<li>Notebook Channel</li>
<li>Roster Channel<ul>
<li><input name="viewMemberGeneralInfo" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Members General Info</li>
<li><input name="viewMembersSpecificInfo" type="checkbox" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" checked="checked"/>View Members Specific Info</li>
<li><input name="enrollMembers" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Enroll Members</li>
<li><input name="editUserPermissions" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Edit User Type &amp; Permissions</li>
<li><input name="unenrollMembers" onclick="rosterForm.userTypeOptions1.options[4].selected = true;" type="checkbox"/>Un-enroll Members</li>
</ul></li>
</ul></td>
</tr>

<tr>
<td colspan="2" style="text-align:center"><nobr><input type="submit" value="Submit"/><input type="button" value="Cancel" onclick="window.locationhref={$baseActionURL}"/></nobr></td>
</tr>

</table>
</form>

<table>
<tr>
<th colspan="4" class="uportal-background-med">User Search</th>
</tr>

<form action="enroll_roster_view.html" method="post" onsubmit="return validator.applyFormRules(this, new RosterRulesObject())">
<input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
<input type="hidden" name="catPageSize" value="{$catPageSize}" />
<tr>
<td class="uportal-background-light">Search Field</td>
<td class="uportal-background-light"><select name="field1">
<option selected="selected"><nobr/></option>
<option>User ID</option>
<option>First Name</option>
<option>Middle Initial</option>
<option>Last Name</option>
<option>Username</option>
<option>Password</option>
<option>User Type</option>
<option>Email Address</option>
<option>Gender</option>
<option>Address 1</option>
<option>Address 2</option>
<option>City</option>
<option>State</option>
<option>Zip Code</option>
<option>Home Phone Number</option>
<option>Day Phone Number</option>
<option>Mobile Phone Number</option>
</select></td>
<td class="uportal-background-light"><select name="field1">
<option selected="selected"><nobr/></option>
<option>User ID</option>
<option>First Name</option>
<option>Middle Initial</option>
<option>Last Name</option>
<option>Username</option>
<option>Password</option>
<option>User Type</option>
<option>Email Address</option>
<option>Gender</option>
<option>Address 1</option>
<option>Address 2</option>
<option>City</option>
<option>State</option>
<option>Zip Code</option>
<option>Home Phone Number</option>
<option>Day Phone Number</option>
<option>Mobile Phone Number</option>
</select></td>
</tr>

<tr>
<td class="uportal-background-light">Search Value</td>
<td class="uportal-background-light"><input name="searchValue1" type="text" value=""/></td>
<td class="uportal-background-light"><input name="searchValue2" type="text" value=""/></td>
</tr>
 
<tr>
<td class="uportal-background-light">Search How?</td>
<td class="uportal-background-light"><select name="how1">
<option selected="selected"><nobr/></option>
<option>Contains</option>
<option>Begin With</option>
<option>Ends With</option>
</select></td>
<td class="uportal-background-light"><select name="how2">
<option selected="selected"><nobr/></option>
<option>Contains</option>
<option>Begin With</option>
<option>Ends With</option>
</select></td>
</tr>
 
<tr>
<td colspan="3" style="text-align:center"><input type="submit" value="Search"/></td>
</tr>
</form>
</table>

<xsl:call-template name="links"/>
  </xsl:template>

  <xsl:template name="links">
    <p class="uportal-channel-text">Options:
      <a href="{$baseActionURL}?command={$viewCommand}&amp;{$userIdParam}={@id}">View</a> | 
      <a href="{$baseActionURL}?command={$unenrollCommand}&amp;{$userIdParam}={@id}">Un-enroll</a> | 
      <a href="{$baseActionURL}?command={$viewAllCommand}">List All</a> | 
      <a href="{$baseActionURL}?command={$enrollViewCommand}">Enroll</a> | 
      <a href="{$baseActionURL}?command={$importCommand}">Import</a>
    </p>
  </xsl:template>

  <xsl:template match="permissionType">
    <option selected="selected">
      <xsl:if test="default = 'true'">
        <xsl:attribute name="selected">selected</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="."/>
    </option>
  </xsl:template>
</xsl:stylesheet>
