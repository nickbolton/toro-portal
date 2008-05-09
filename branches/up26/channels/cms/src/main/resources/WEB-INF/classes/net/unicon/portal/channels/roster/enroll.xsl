<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:include href="common.xsl"/>
<xsl:output method="html" indent="yes" />

  <xsl:template match="roster">
  
<xsl:call-template name="links"/>
<table>
	<tr>
		<th class="uportal-background-med" scope="col">First Name &amp; Initial</th>
		<th class="uportal-background-med" scope="col">Last Name</th>
		<th class="uportal-background-med" scope="col">User ID</th>
		<th class="uportal-background-med" scope="col">User Status</th>
		<th colspan="3" class="uportal-background-med" scope="col">Options</th>
	</tr>
</table>

<form method="post">
<input type="hidden" name="targetChannel" value="{$targetChannel}" />
<input type="hidden" name="catPageSize" value="{$catPageSize}" />
<table>
	<tr>
		<td>
			<label for="rcen1">Results Page:</label> <select id="rcen1">
				<option selected="selected">1</option>
				<option>2</option>
				<option>3</option>
				<option>4</option>
				<option>5</option>
				<option>All</option>
			</select> of 5
		</td>
		<td><input type="submit" value="Go" title="Go to the selected page of results" /></td>
		<td><input type="submit" value="Previous" title="Go to the previous page of results"/></td>
		<td><input type="submit" value="Next" title="Go to the next page of results"/></td>
	</tr>
</table>
</form>

<xsl:call-template name="userSearch"/>

<xsl:call-template name="links"/>
  </xsl:template>

  <xsl:template match="user">
    <tr>
      <td class="uportal-background-light" scope="row">
        <xsl:value-of select="firstname"/>
      </td>
      <td class="uportal-background-light" scope="row">
        <xsl:value-of select="lastname"/>
      </td>
      <td class="uportal-background-light" scope="row">
        <xsl:value-of select="@id"/>
      </td>
      <td class="uportal-background-light" scope="row">
        <xsl:value-of select="status"/>
      </td>
      <td class="uportal-background-light" scope="row">
        <a href="{$baseActionURL}?command={$viewCommand}&amp;{$userIdParam}=@id}" title="View user">View</a>
      </td>
      <td class="uportal-background-light" scope="row">
        <a href="{$baseActionURL}?command={$editPermissionsCommand}&amp;{$userIdParam}=@id}" title="View user's permissions">Permissions</a>
      </td>
      <td class="uportal-background-light" scope="row">
        <a href="{$baseActionURL}?{$enrollCommand}&amp;{$userIdParam}=@id" title="Enroll user">Enroll</a>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="links">
    <p class="uportal-channel-text">Options:
      <a href="{$baseActionURL}?command={$enrollViewCommand}" title="List All">List All</a> | 
      <a href="{$baseActionURL}?command={$importCommand}" title="Import">Import</a>
    </p>
  </xsl:template>

</xsl:stylesheet>
