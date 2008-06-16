<?xml version="1.0" encoding="UTF-8"?>

<!--

   Copyright (c) 2002, Dan Phong Ltd (IBS-DP). All Rights Reserved.

   This software is the confidential and proprietary information of
   Dan Phong Ltd (IBS-DP)  ("Confidential Information"). You shall not
   disclose such Confidential Information and shall use it only in
   accordance with the terms of the license agreement you entered into
   with IBS-DP or its authorized distributors.

   IBS-DP MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
   OF THE SOFTWARE, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT
   LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. IBS-DP SHALL NOT BE LIABLE
   FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
   OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="lib.xsl"></xsl:import>
    <xsl:output method="html"/>
    <xsl:include href="common.xsl"/>
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="sid">default</xsl:param>
    <xsl:param name="goURL"/>
    <xsl:param name="doURL"/>
    <xsl:param name="baseImagePath">media/net/unicon/portal/channels</xsl:param>
    <!--////////////////////////////////////////////// -->
    <!-- new edit-->
    <xsl:param name="isShowCheckbox">true</xsl:param>
    <!-- For Portal-->
    <xsl:param name="openGroup">0</xsl:param>
    <!-- For Personal -->
    <xsl:param name="openFolder">-1</xsl:param>
    <!-- For Common -->
    <xsl:param name="nPages">10</xsl:param>
    <xsl:param name="curPage">2</xsl:param>
    <xsl:param name="start">0</xsl:param>
    <xsl:param name="end">0</xsl:param>
    <xsl:param name="total">0</xsl:param>
    <xsl:param name="maxResult">50</xsl:param>
    <xsl:param name="choose-radio">Personal</xsl:param>
    <xsl:param name="groupsOnly">true</xsl:param>
    <xsl:param name="singleSelection">true</xsl:param>
    <xsl:param name="name"/>
    <xsl:param name="title"/>
    <xsl:param name="department"/>
    <xsl:param name="email"/>

    <xsl:param name="portal">true</xsl:param>
    <xsl:param name="contact">true</xsl:param>
    <xsl:param name="search">true</xsl:param>

    <!--////////////////////////////////////////////// -->
    <xsl:key name="expand" match="expanded" use="."/>
    <xsl:key name="gselect" match="selected[@itype='G' and @ientity='u']" use="@iid"/>
    <xsl:key name="eselect" match="selected[@itype='E' and @ientity='u']" use="@iid"/>
    <xsl:key name="cselect" match="selected[@ientity='a']" use="@iid"/>
    <xsl:key name="pselect" match="selected[@ientity='p']" use="@iid"/>
    <xsl:key name="rselect" match="permitted" use="@iid"/>
    
	<!-- ESCAPE-CHARACTERS -->
	<xsl:template name="escape-characters">
		<xsl:param name="string" />
		<xsl:variable name = "backslash-escaped"><xsl:call-template name="replace-string">
			<xsl:with-param name="string" select="$string" />
				<xsl:with-param name="replace" select='"\"' />
				<xsl:with-param name="with" select='"\\"' />
			</xsl:call-template></xsl:variable>
		<xsl:call-template name="replace-string">
			<xsl:with-param name="string" select="$backslash-escaped" />
			<xsl:with-param name="replace" select='"&apos;"' />
			<xsl:with-param name="with" select='"\&apos;"' />
		</xsl:call-template>
	</xsl:template>
	
	<!-- REPLACE STRING -->
	<xsl:template name="replace-string">
	   <xsl:param name="string" />
	   <xsl:param name="replace" />
	   <xsl:param name="with" />
	   <xsl:choose>
	      <!-- If the string contains the replace string -->
	      <xsl:when test='contains($string, $replace)'>
	         <!-- Then output the value before the replace string -->
	         <xsl:value-of select="substring-before($string, $replace)" />
	         <!-- Output what it will be replaced with (i.e. with string) -->
	         <xsl:value-of select="$with" />
	         <!-- Call this template recursively passing in the remainder of the string -->
	         <xsl:call-template name="replace-string">
	            <xsl:with-param name="string" select="substring-after($string, $replace)" />
	            <xsl:with-param name="replace" select="$replace" />
	            <xsl:with-param name="with" select="$with" />
	         </xsl:call-template>
	      </xsl:when>
	      <!-- Else -->
	      <xsl:otherwise>
	         <!-- Simply output the string, as is -->
	         <xsl:value-of select="$string" />
	      </xsl:otherwise>
	   </xsl:choose>
	</xsl:template>
	
    <xsl:template match="addressbook-system">
        <script language="JavaScript1.2" type="text/javascript">
             // This script prevents an open search by checking to see if the input is empty. It is called from the onsubmit of the form below.
             OpenSearchCheck = function() {
                 if (window.validateForm &amp;&amp; document.getElementById("addressbook-Search").name.value == "" &amp;&amp; document.getElementById("addressbook-Search").title.value == "" &amp;&amp; document.getElementById("addressbook-Search").department.value == "" &amp;&amp; document.getElementById("addressbook-Search").email.value == "") {
                     alert("The search fields are empty; you must provide search criteria");
                     window.validateForm = false;
                     return false;
                 } else {
                     window.validateForm = false;
                     return true;
                 }
             }
             // Every below was added to support the copying of functions from one window to
             // another so that client-side optimizations would work
             AddressbookFunctions = ["initializeAddressbook", "OpenSearchCheck"];
             // if channelFunctionsArray is already defined, just add to it
             if (window.channelFunctionsArray)
             {
                 channelFunctionsArray[channelFunctionsArray.length] = AddressbookFunctions;
             }
             // else create channelFunctionsArray with this as first entry
             else
             {
                 channelFunctionsArray = [AddressbookFunctions]; // create 2-D array
             }
             // create initialize method so that it can be initialized outside of page load
             initializeAddressbook = function()
             {
             }
         </script>
      <form method="post" action="{$baseActionURL}" name="addressbook-Search" id="addressbook-Search" onsubmit="return OpenSearchCheck();">
		<input type="hidden" name="sid" value="{$sid}"/>
		
		<div class="survey-editor-subtitle">Select Users And Groups</div>
		<xsl:if test="$choose-radio='Search'">
			<input type="image" style="border:0" name="do~search" border="0"
			src="{$baseImagePath}/rad/transparent.gif"/>
			<input type="hidden" name="default" value="do~search"/>
		</xsl:if>
		
		<div class="addr-tab-container">
			<xsl:choose>
				<xsl:when test="$portal='false'"></xsl:when>
				<xsl:when test="$choose-radio='Portal'">
					<div class="addr-tab-left addr-tab-selected-shell">
						<div class="addr-tab-right">
							<div class="addr-tab-selected">
								Browse
							</div>
						</div>
					</div>				
				</xsl:when>
				<xsl:otherwise>
				  	<div class="addr-tab-left">
						<div class="addr-tab-right">
							<div class="addr-tab">
								<input type="submit" value="Browse" class="image" name="do~portalCheck" border="0" alt="Select" id="AS-SelectPersonalI1"/>
							</div>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
			
		  	<xsl:choose>
				<xsl:when test="$search='false'"></xsl:when>
				<xsl:when test="$choose-radio='Search'">
					<div class="addr-tab-left addr-tab-selected-shell">
						<div class="addr-tab-right">
							<div class="addr-tab addr-tab-selected">
								Search
							</div>
						</div>
					</div>				
				</xsl:when>
				<xsl:otherwise>
				  	<div class="addr-tab-left">
						<div class="addr-tab-right">
							<div class="addr-tab">
								<input type="submit" value="Search" class="image" name="do~searchCheck" border="0" alt="Select" id="AS-SelectPersonalI1"/>
							</div>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:choose>
				<xsl:when test="$contact='false'"></xsl:when>
				<xsl:when test="$choose-radio='Personal'">
					<div class="addr-tab-left addr-tab-selected-shell">
						<div class="addr-tab-right">
							<div class="addr-tab addr-tab-selected">
								Personal Contacts
							</div>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="addr-tab-left">
						<div class="addr-tab-right">
							<div class="addr-tab">
								<input type="submit" value="Personal Contacts" class="image" name="do~personalCheck" border="0" alt="Select" id="AS-SelectPersonalI1"/>
							</div>
						</div>
					</div>		
				</xsl:otherwise>
			</xsl:choose>		
			
		</div>
		<!-- Body -->
		<table cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td valign="top">
					<xsl:call-template name="selecting"/>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<!-- Right: selected items -->
					<xsl:if test="$singleSelection='false'">
					  
					  <div class="addr-selected-header">My Selections</div>

					  <div class="bounding-box3">
						<table cellpadding="0" cellspacing="0">
							<xsl:apply-templates select="selected"/>					
						</table>
					  </div>			 
					</xsl:if>
				</td>
			</tr>
			<tr>
				<td align="center">
					<div align="center" class="submit-container">
						<input type="submit" name="do~ok" value="Submit Selections"/>
						<input type="submit" name="do~cancel" value="Cancel"/>
					</div>				
				</td>
			</tr>
		</table>
      </form>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <!-- Selecting template -->
    <xsl:template name="selecting">

	  <div class="select-container">
	
			  <!-- /////////////////////// -->
			  <!-- Mode personal contacts -->
			  <xsl:if test="$choose-radio='Personal'">
			  	<div class="bounding-box1">
					<table cellpadding="0" cellspacing="0" width='100%'>
						<tr>
							<td class="th-top-single" nowrap="nowrap">Personal Listing</td>
						</tr>
						<tr>
							<td><xsl:call-template name="personal"/></td>
						</tr>
						<!-- Open folder -->
						<tr>
							<td class="th-top-single" nowrap="nowrap">Contacts</td>
						</tr>
						<tr>
							<td><xsl:call-template name="address"/></td>
						</tr>
					</table>
				</div>
			  </xsl:if>          


		  <!-- Portal -->

			<!-- /////////////////////// -->
			<!-- Mode Portal listing -->
			<xsl:if test="$choose-radio='Portal'">
				<div class="bounding-box1">
					<table cellpadding="0" cellspacing="1" align="center" width="100%">
						<tr>
							<td class="addr-browse-header" width="50%" nowrap="nowrap">Groups</td>
							<xsl:if test="$groupsOnly = 'false'">
								<td class="addr-browse-header" nowrap="nowrap">Users</td>
							</xsl:if>
						</tr>
						<tr>
							<td valign="top">
								<xsl:call-template name="portal"/>
							</td>
							<xsl:if test="$groupsOnly = 'false'">
							<!-- Entities -->
								<td valign="top" class="border-left addr-group-selected">
									<xsl:call-template name="users"/>
								</td>
							</xsl:if>
						</tr>
						<!--
						<tr>
						  <td colspan="2" class="table-nav" style="text-align:center">
							<input type="submit" class="uportal-button" value="Select" name="do~selecting" title="To select checked users" alt="To select checked users"/>
						  </td>
						</tr>					
						-->
					</table>
				</div>
			</xsl:if>      


		  <!-- Search -->

			<!-- /////////////////////// -->
			<!-- Mode Search -->
			<xsl:if test="$choose-radio='Search'">
				<div class="bounding-box1">
					
					
					  <img align="absmiddle" src="{$SPACER}" width="18" height="0" border="0"/>


					  
					<table cellpadding="0" cellspacing="0" width="100%" align="center">
						<!-- Search criteria -->
						<tr>
							<td class="th-top-single" style="text-align:left" nowrap="nowrap">Search Criteria</td>
						</tr>
						<tr>
							<td>
								<xsl:call-template name="criteria"/>
							</td>
						</tr>
						<!-- Search results -->
						<tr>
							<td class="th-top-single" style="text-align:left" nowrap="nowrap">Search Results</td>
						</tr>
						<tr>
							<td>
								<xsl:call-template name="results"/>
							</td>
						</tr>
					</table>
				</div>
			</xsl:if>

		</div>
      
    </xsl:template>
    <!--////////////////////////////////////////////// -->
    <!-- criteria template -->
    <xsl:template name="criteria">
      <!--UniAcc: Layout Table -->
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="table-light-left" style="text-align:right" nowrap="nowrap">
            <label for="addressbook-SearchNameT1">Name</label>
          </td>
          <td class="table-content-right" width="100%">
            <input class="text" type="text" name="name" value="{$name}" id="addressbook-SearchNameT1"/>
          </td>
        </tr>
        <tr>
          <td class="table-light-left" style="text-align:right" nowrap="nowrap">
            <label for="addressbook-SearchTitleT1">Title</label>
          </td>
          <td class="table-content-right" width="100%">
            <input class="text" type="text" name="title" value="{$title}" id="addressbook-SearchTitleT1"/>
          </td>
        </tr>
        <tr>
          <td class="table-light-left" style="text-align:right" nowrap="nowrap">
            <label for="addressbook-SearchDeptT1">Department</label>
          </td>
          <td class="table-content-right" width="100%">
            <input class="text" type="text" name="department" value="{$department}" id="addressbook-SearchDeptT1"/>
          </td>
        </tr>
        <tr>
          <td class="table-light-left" style="text-align:right" nowrap="nowrap">
            <label for="addressbook-SearchEmailT1">Email</label>
          </td>
          <td class="table-content-right" width="100%">
            <input class="text" type="text" name="email" value="{$email}" id="addressbook-SearchEmailT1"/>
          </td>
        </tr>
        <tr>
        	<td class="table-light-left" style="text-align:right" nowrap="nowrap">
        		Search Within
        	</td>
        	<td class="table-content-right">
				<input type="checkbox" name="portal" value="portal" id="AS-SearchPortalC1">
				<xsl:if test="search/@portal='y'">
					<xsl:attribute name="checked">checked</xsl:attribute>
				</xsl:if>
				</input>
				<label for="AS-SearchPortalC1"><xsl:text>portal&#160;</xsl:text></label>
				
				<input type="checkbox" name="campus" value="campus" id="AS-SearchCampusC1">
				<xsl:if test="search/@campus='y'">
					<xsl:attribute name="checked">checked</xsl:attribute>
				</xsl:if>
				</input>
				<label for="AS-SearchCampusC1"><xsl:text>campus&#160;</xsl:text></label>

				<xsl:if test="$contact!='false'">
                    <input type="checkbox" name="contact" value="contact" id="AS-SearchPersonalC1">
                    <xsl:if test="search/@contact='y'">
                        <xsl:attribute name="checked">checked</xsl:attribute>
                    </xsl:if>
                    </input>
                    <label for="AS-SearchPersonalC1"><xsl:text>personal&#160;</xsl:text></label>  
				</xsl:if>
			</td>
		</tr>
        <tr>
          <td class="table-light-left" style="text-align:right" nowrap="nowrap">
            <label for="addressbook-SearchMaxResultsS1">Max Results</label>
          </td>
          <td class="table-content-right" width="100%">
            <select name="max-results" id="addressbook-SearchMaxResultsS1">
              <option value="10"><xsl:if test="$maxResult='10'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>10</option>
              <option value="20"><xsl:if test="$maxResult='20'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>20</option>
              <option value="50"><xsl:if test="$maxResult='50'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>50</option>
              <option value="100"><xsl:if test="$maxResult='100'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>100</option>
              <option value="150"><xsl:if test="$maxResult='150'"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>150</option>
            </select>
          </td>
        </tr>
        <tr>
        	<td></td>
          <td>
            <input type="submit" class="uportal-button" value="Search" name="do~search" title="To submit this information and return to the view of the address book" alt="To submit this information and return to the view of the address book" onclick="window.validateForm = true"/>
          </td>
        </tr>
      </table>
    </xsl:template>
    <!--////////////////////////////////////////////// -->
    <!-- Search results template -->
    <xsl:template name="results">
    	
      <table cellpadding="0" cellspacing="0" border="0" width="100%" class="bounding-box3">
        <!-- Draw current page -->
        <xsl:apply-templates select="entity | group | contact | campus" mode="search"/>
        <!-- Select button and page number -->
        <tr>
          <td colspan="2" class="bounding-box3" style="text-align:center">
            <xsl:if test="number($nPages) &gt; 0">
              <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
              <xsl:call-template name="pageSearch"/>
            </xsl:if>
          </td>
        </tr>
      </table>
    </xsl:template>
    <!--////////////////////////////////////////////// -->
    <!-- Search results page navigation template -->
    <xsl:template name="pageSearch">
      <!-- Previous -->
      <xsl:if test="number($curPage) &gt; 0">
        <input type="image" align="absmiddle" name="do~prev&amp;p={$curPage}" border="0" src="{$baseImagePath}/rad/prev_12.gif" title="Previous" alt="Previous"/>
      </xsl:if>
      <xsl:if test="number($curPage) &lt; 1">
        <img src="{$baseImagePath}/rad/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/>
      </xsl:if>
      <!-- curPage/nPages -->
      <xsl:text>&#160;</xsl:text>
      <xsl:value-of select="number($start)+1"/>&#160;-&#160;<xsl:value-of select="$end"/><xsl:text>&#160;of&#160;</xsl:text><xsl:value-of select="$total"/><xsl:text>&#160;</xsl:text>
      <!-- Next -->
      <xsl:if test="number($curPage) &lt; number($nPages) - 1">
        <input type="image" align="absmiddle" border="0" name="do~next&amp;p={$curPage}" src="{$baseImagePath}/rad/next_12.gif" title="Next" alt="Next"/>
      </xsl:if>
      <xsl:if test="number($curPage) &gt; number($nPages) - 2">
        <img src="{$baseImagePath}/rad/next_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/>
      </xsl:if>
      <img src="{$baseImagePath}/rad/transparent.gif" border="0" width="1" alt="" title=""/>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <!-- Search results page navigation template -->
    <xsl:template name="pageNavigation">
      <!-- Previous -->
      <xsl:if test="number($curPage) &gt; 0">
        <input type="image" align="absmiddle" name="do~prev&amp;p={$curPage}" border="0" src="{$baseImagePath}/rad/prev_12.gif" title="Previous" alt="Previous"/>
      </xsl:if>
      <xsl:if test="number($curPage) &lt; 1">
        <img src="{$baseImagePath}/rad/prev_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/>
      </xsl:if>
      <!-- curPage/nPages -->
      <xsl:text>&#160;</xsl:text>
      <xsl:value-of select="number($curPage)+1"/>/<xsl:value-of select="$nPages"/><xsl:text>&#160;</xsl:text>
      <!-- Next -->
      <xsl:if test="number($curPage) &lt; number($nPages) - 1">
        <input type="image" align="absmiddle" border="0" name="do~next&amp;p={$curPage}" src="{$baseImagePath}/rad/next_12.gif" title="Next" alt="Next"/>
      </xsl:if>
      <xsl:if test="number($curPage) &gt; number($nPages) - 2">
        <img src="{$baseImagePath}/rad/next_disabled_12.gif" border="0" align="absmiddle" alt="" title=""/>
      </xsl:if>
      <img src="{$baseImagePath}/rad/transparent.gif" border="0" width="1" alt="" title=""/>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <!-- portal template -->
    <xsl:template name="portal">
      <table border="0" width="100%" cellpadding="0" cellspacing="0">
        <xsl:apply-templates select="group" mode="portal">
          <xsl:with-param name="prefix"/>
        </xsl:apply-templates>
      </table>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <!-- personal template -->
    <xsl:template name="personal">
      <table border="0" width="100%" cellpadding="0" cellspacing="0">
        <!-- Special folder (all) -->
        <xsl:choose>
          <!--Opening folder -->
          <xsl:when test="$openFolder='-1'">
            <td class="table-content-right" nowrap="nowrap">
               <xsl:text>&#160;</xsl:text>
               <img src="{$baseImagePath}/rad/folder_open_16.gif" align="absmiddle" border="0" alt="Open Folder" title="Open Folder"/>
               <xsl:text>&#160;</xsl:text>
               All
               <xsl:text>&#160;</xsl:text>
            </td>
          </xsl:when>
          <!--Closing folder -->
          <xsl:otherwise>
            <td class="table-content-right" nowrap="nowrap">
              <xsl:text>&#160;</xsl:text>
              <input type="image" name="do~open&amp;folderid=-1" border="0" src="{$baseImagePath}/rad/folder_closed_16.gif" alt="Closed Folder" title="Closed Folder"/>
              <xsl:text>&#160;</xsl:text>
              All
              <xsl:text>&#160;</xsl:text>
            </td>
          </xsl:otherwise>
        </xsl:choose>
        <!-- Others folders -->
        <xsl:apply-templates select="folder"/>
      </table>
    </xsl:template>
    
    <!--////////////////////////////////////////////// -->
    <!-- selected template -->
    <xsl:template match="selected">
      <xsl:variable name = "iname-escaped"><xsl:call-template name = "escape-characters" >
       	<xsl:with-param name="string" select="@iname"></xsl:with-param>
      </xsl:call-template></xsl:variable>
      <tr>
        <!-- Check box for selecting -->
        <td class="table-light-left" style="text-align:right" width="1%">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <!-- Contact -->
            <xsl:when test="@ientity='a'">
				<input type="hidden" name="" id="AS-SelectedCB{@iname}"/>
				<input type="image" name="do~deselecting" src="{$baseImagePath}/rad/checked_12.gif"
					onclick="document.getElementById('AS-SelectedCB{$iname-escaped}').name='_contact{@iid}';"/> <!-- escape-characters--> 
            </xsl:when>
            <!-- Campus -->
            <xsl:when test="@ientity='p'">
				<input type="hidden" name="" id="AS-SelectedCB{@iname}"/>
				<input type="image" name="do~deselecting" src="{$baseImagePath}/rad/checked_12.gif"
					onclick="document.getElementById('AS-SelectedCB{$iname-escaped}').name='_campus{@iid}';"/>   
            </xsl:when>
            <!-- Portal group -->
            <xsl:when test="@ientity='u' and @itype='G'">
				<input type="hidden" name="" id="AS-SelectedCB{@iname}"/>
				<input type="image" name="do~deselecting" src="{$baseImagePath}/rad/checked_12.gif"
					onclick="document.getElementById('AS-SelectedCB{$iname-escaped}').name='_group{@iid}';"/>   
            </xsl:when>
            <xsl:when test="@ientity='g' and @itype='G'">
				<input type="hidden" name="" id="AS-SelectedCB{@iname}"/>
				<input type="image" name="do~deselecting" src="{$baseImagePath}/rad/checked_12.gif"
					onclick="document.getElementById('AS-SelectedCB{$iname-escaped}').name='_group{@iid}';"/> 
            </xsl:when>
            <!-- Portal user -->
            <xsl:when test="@ientity='u' and @itype='E'">
				<input type="hidden" name="" id="AS-SelectedCB{@iname}"/>
				<input type="image" name="do~deselecting" src="{$baseImagePath}/rad/checked_12.gif"
					onclick="document.getElementById('AS-SelectedCB{$iname-escaped}').name='_entity{@iid}';"/> 
            </xsl:when>
          </xsl:choose>
        </td>
        <!-- selection name -->
        <td class="table-content-right" align="left" width="99%">
            <label for="AS-SelectedCB{@iname}">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <!-- Contact -->
            <xsl:when test="@ientity='a'">
              <img src="{$baseImagePath}/rad/shadow_16.gif" border="0" align="absmiddle" alt="Personal Contact" title="Personal Contact"/>
            </xsl:when>
            <!-- Campus -->
            <xsl:when test="@ientity='p'">
              <img src="{$baseImagePath}/rad/shadow_16.gif" border="0" align="absmiddle" alt="Campus Contact" title="Campus Contact"/>
            </xsl:when>
            <!-- Portal group -->
            <xsl:when test="@ientity='u' and @itype='G'">
              <img src="{$baseImagePath}/rad/persons_16.gif" border="0" align="absmiddle" alt="Portal Group" title="Portal Group"/>
            </xsl:when>
            <xsl:when test="@ientity='g' and @itype='G'">
              <img src="{$baseImagePath}/rad/persons_16.gif" border="0" align="absmiddle" alt="Portal Group" title="Portal Group"/>
            </xsl:when>
            <!-- Portal user -->
            <xsl:when test="@ientity='u' and @itype='E'">
              <img src="{$baseImagePath}/rad/person_16.gif" border="0" align="absmiddle" alt="Portal User" title="Portal User"/>
            </xsl:when>
          </xsl:choose>
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:value-of select="@iname"/>
          <xsl:text>&#160;</xsl:text>
            </label>
        </td>
      </tr>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <!-- Addresses in open folder template -->
    <xsl:template name="address">
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <!-- Draw current page -->
        <xsl:apply-templates select="contact" mode="search"/>
        <!-- Select button and page number -->
        <tr>
          <td colspan="2" class="table-nav" style="text-align:center">
            <input type="submit" class="uportal-button" value="Select" name="do~selecting" title="To select checked users" alt="To select checked users"/>
            <xsl:if test="number($nPages) &gt; 0">
              <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
              <xsl:call-template name="pageNavigation"/>
            </xsl:if>
          </td>
        </tr>
      </table>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <!-- Users in open portal group template -->
    <xsl:template name="users">
      <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <!-- Draw current page -->
        <xsl:apply-templates select="entities/entity" mode="portal"/>
        <!-- Select button and page number -->
		<tr>
			<td colspan="2" align="center" class="table-nav">
				<xsl:if test="number($nPages) &gt; 0">
				  <xsl:call-template name="pageNavigation"/>
				</xsl:if>        
		    </td>
		</tr>
      </table>
    </xsl:template>
    
    
    <!--////////////////////////////////////////////// -->
    <xsl:template match="folder">
      <tr>
        <xsl:choose>
          <xsl:when test="$openFolder=@id">
            <!--Opening folder -->
            <td class="table-content-right" nowrap="nowrap">
              <xsl:text>&#160;</xsl:text>
              <img src="{$baseImagePath}/rad/folder_open_16.gif" align="absmiddle" border="0" alt="Open Folder" title="Open Folder"/>
              <xsl:text>&#160;</xsl:text>
              <xsl:value-of select="text()"/>
              <xsl:text>&#160;</xsl:text>
            </td>
          </xsl:when>
          <xsl:otherwise>
            <!--Closed folder -->
            <td class="table-content-right" nowrap="nowrap">
              <xsl:text>&#160;</xsl:text>
              <input type="image" name="do~open&amp;folderid={@id}" border="0" src="{$baseImagePath}/rad/folder_closed_16.gif" alt="Closed Folder" title="Closed Folder"/>
              <xsl:text>&#160;</xsl:text>
              <xsl:value-of select="text()"/>
              <xsl:text>&#160;</xsl:text>
             </td>
          </xsl:otherwise>
        </xsl:choose>
      </tr>
    </xsl:template>
    
    
    <!-- ///////////////////////////////////////// -->
    <!-- Template for group  -->
    <xsl:template match="group" mode="portal">
        <xsl:param name="prefix"/>
        <!-- ///////////////////////////////////////// -->
	    <xsl:variable name = "iname-escaped"><xsl:call-template name = "escape-characters" >
	      <xsl:with-param name="string" select="@iname"></xsl:with-param>
	    </xsl:call-template></xsl:variable>
        <xsl:variable name="expand" select="key('expand',@iid)"/>
        <tr>
        	<xsl:choose>
        		<xsl:when test="@iid=$openGroup">
        			<xsl:attribute name="class">addr-group-selected</xsl:attribute>
        		</xsl:when>
        	</xsl:choose>
          <!-- Allow selection... -->
          <xsl:if test="$isShowCheckbox='true'">
            <!-- Check box for selecting -->
            <td class="table-light-left" style="text-align:right" width="1%">

              <xsl:choose>
                <!-- single mode of groups only -->
                <xsl:when test="$groupsOnly = 'true' and $singleSelection='true'">
                  <xsl:if test="key('rselect',@iid)">
                     <input name="selectedGroup" value="{@iid}" type="radio">
                     	<xsl:if test="key('gselect', @iid)"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
                  	 </input>
                  </xsl:if>
                </xsl:when>
                <!-- multiple mode -->
                <xsl:otherwise>
                  <xsl:if test="key('rselect',@iid)">
                    <xsl:if test="not(key('gselect', @iid))">
                    	<input type="hidden" name="" id="AS-UsersC{@iname}"/>
                    	<input type="image" name="do~selecting" src="{$baseImagePath}/rad/check_12.gif"
                    		onclick="document.getElementById('AS-UsersC{$iname-escaped}').name='group{@iid}';"/>
                    </xsl:if>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>
            </td>
          </xsl:if>

          <td class="table-content-right" nowrap="nowrap" width="100%">
                <!-- Prefix: + | - | empty -->
                <xsl:copy-of select="$prefix"/>
                <xsl:if test="@groups='0'">
                    <img src="{$baseImagePath}/rad/tree_empty_16.gif" border="0" align="absmiddle" alt="Node" title="Node"/>
                </xsl:if>
                <xsl:if test="@groups!='0'">
                    <xsl:if test="$expand">
                        <input class="nopad" type="image" name="do~collapse&amp;key={@iid}" src="{$baseImagePath}/rad/tree_minus_16.gif" align="absmiddle" alt="Open Node" title="Open Node"/>
                    </xsl:if>
                    <xsl:if test="not($expand)">
                        <input class="nopad" type="image" name="do~expand&amp;key={@iid}" src="{$baseImagePath}/rad/tree_plus_16.gif" align="absmiddle" alt="Closed Node" title="Closed Node"/>
                    </xsl:if>
                </xsl:if>
                <xsl:text>&#160;</xsl:text>
                <!-- Display name of group -->
                
                <xsl:choose>
                    <xsl:when test="@iid=$openGroup">
                        <img src="{$baseImagePath}/rad/folder_open_16.gif" border="0" align="absmiddle" alt="Open Folder" title="Open Folder"/>
                        <xsl:text>&#160;&#160;</xsl:text>
                        <label for="AS-UsersC{@iname}"><strong><xsl:value-of select="@iname"/></strong> </label>                        
                    </xsl:when>
                    <xsl:otherwise>
                        <input class="nopad" type="image" name="do~open&amp;key={@iid}" src="{$baseImagePath}/rad/folder_closed_16.gif" align="absmiddle" alt="Closed Folder" title="Closed Folder"/>
                        <xsl:text>&#160;</xsl:text>
                        <input class="image nopad" type="submit" name="do~open&amp;key={@iid}" value="{@iname}"/>
                    </xsl:otherwise>
                </xsl:choose>
                
          </td>
        </tr>
        <!-- Children -->
        <xsl:if test="$expand">
            <xsl:apply-templates select="group" mode="portal">
                <xsl:with-param name="prefix">
                    <xsl:copy-of select="$prefix"/>
                    <img src="{$baseImagePath}/rad/tree_space_16.gif" width="20" height="1" align="absmiddle" border="0" alt="" title=""/>
                </xsl:with-param>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    <!-- ///////////////////////////////////////// -->
    <!-- Template for contact (non-portal users) -->
    <xsl:template match="contact" mode="search">
      <tr>
        <!-- Check box for selecting -->
        <td class="table-light-left" style="text-align:right" width="1%">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <xsl:when test="key('cselect', @id)"></xsl:when>
            <xsl:otherwise><input type="checkbox" name="contact{@id}" value="{name}" id="AS-UsersC{@id}"/></xsl:otherwise>
          </xsl:choose>
        </td>
        <!-- icon and contact name -->
        <td class="table-content-right" nowrap="nowrap" width="99%">
        <label for="AS-UsersC{@id}">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <img src="{$baseImagePath}/rad/shadow_16.gif" border="0" align="absmiddle" alt="Personal Contact" title="Personal Contact"/>
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:value-of select="name"/>
          <xsl:text>&#160;</xsl:text>
        </label>
        </td>
      </tr>
    </xsl:template>
    <!-- ///////////////////////////////////////// -->
    <!-- Template for campus -->
    <xsl:template match="campus" mode="search">
      <tr>
        <!-- Check box for selecting -->
        <td class="table-light-left" style="text-align:right" width="1%">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <xsl:when test="key('pselect', @id)"></xsl:when>
            <xsl:otherwise><input type="checkbox" name="campus{@id}" value="{name}" id="AS-UsersC{@iname}"/></xsl:otherwise>
          </xsl:choose>
        </td>
        <!-- icon and contact name -->
        <td class="table-content-right" nowrap="nowrap" width="99%">
        <label for="AS-UsersC{@iname}">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <img src="{$baseImagePath}/rad/shadow_16.gif" border="0" align="absmiddle" alt="Campus Contact" title="Campus Contact"/>
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:value-of select="name"/>
          <xsl:text>&#160;</xsl:text>
        </label>
        </td>
      </tr>
    </xsl:template>
    <!-- ///////////////////////////////////////// -->
    <!-- Template for group  -->
    <xsl:template match="group" mode="search">
      <tr>
        <!-- Check box for selecting -->
        <td class="table-light-left" style="text-align:right" width="1%">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <xsl:when test="key('gselect', @iid) or not(key('rselect', @iid))"></xsl:when>
            <xsl:otherwise><input type="checkbox" name="group{@iid}" value="{@iname}" id="AS-UsersC{@iname}"/></xsl:otherwise>
          </xsl:choose>
        </td>
        <!-- icon and name -->
        <td class="table-content-right" nowrap="nowrap" width="99%">
        <label for="AS-UsersC{@iname}">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <img src="{$baseImagePath}/rad/persons_16.gif" border="0" align="absmiddle" alt="Portal Group" title="Portal Group"/>
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:value-of select="@iname"/>
          <xsl:text>&#160;</xsl:text>
        </label>
        </td>
      </tr>
    </xsl:template>
    <!-- ///////////////////////////////////////// -->
    <!-- Template for entity  -->
    <xsl:template match="entity" mode="search">
      <tr>
        <!-- Check box for selecting -->
        <td class="table-light-left" style="text-align:right" width="1%">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <xsl:when test="key('eselect', @iid)"></xsl:when>
            <xsl:otherwise><input type="checkbox" name="entity{@iid}" value="{@iname}" id="AS-UsersC{@iname}"/></xsl:otherwise>
          </xsl:choose>
        </td>
        <!-- icon and name -->
        <td class="table-content-right" nowrap="nowrap" width="99%">
        <label for="AS-UsersC{@iname}">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <img src="{$baseImagePath}/rad/person_16.gif" border="0" align="absmiddle" alt="Portal User" title="Portal User"/>
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:value-of select="@iname"/>
          <xsl:text>&#160;</xsl:text>
        </label>
        </td>
      </tr>
    </xsl:template>
    <!-- ///////////////////////////////////////// -->
    <!-- Template for entity  -->
    <xsl:template match="entity" mode="portal">
	  <xsl:variable name = "iname-escaped"><xsl:call-template name = "escape-characters" >
	    <xsl:with-param name="string" select="@iname"></xsl:with-param>
	  </xsl:call-template></xsl:variable>
      <tr>
        <!-- Check box for selecting -->
        <td class="table-light-left" style="text-align:right" width="1%">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:choose>
            <xsl:when test="key('eselect', @iid)"></xsl:when>
            <xsl:otherwise>
				<input type="hidden" name="" id="AS-UsersC{@iname}"/>
				<input type="image" name="do~selecting" src="{$baseImagePath}/rad/check_12.gif"
					onclick="document.getElementById('AS-UsersC{$iname-escaped}').name='entity{@iid}';"/>    
            </xsl:otherwise>
          </xsl:choose>
        </td>
        <!-- icon and name -->
        <td class="table-content-right" nowrap="nowrap" width="99%">
        <label for="AS-UsersC{@iname}">
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <img src="{$baseImagePath}/rad/person_16.gif" border="0" align="absmiddle" alt="Portal User" title="Portal User"/>
          <img align="absmiddle" src="{$SPACER}" width="6" border="0" alt="" title=""/>
          <xsl:value-of select="@iname"/>
          <xsl:text>&#160;</xsl:text>
        </label>
        </td>
      </tr>
    </xsl:template>
</xsl:stylesheet>
