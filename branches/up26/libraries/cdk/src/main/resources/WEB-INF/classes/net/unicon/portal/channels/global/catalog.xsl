<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- parameters -->
	<xsl:param name="catPageSize"/>
    <!-- Catalog: an integer indicating the number of data displayed per page -->
    <xsl:param name="catCurrentPage"/>
    <!-- Catalog: an integer indicating the current page the user is on -->
    <xsl:param name="catLastPage"/>
    <!-- Catalog: an integer indicating the last page of the catalog -->
    <xsl:param name="catCurrentCommand"/>
    <!-- Catalog: a string signifying the type of result set we are walking (e.g. normal, enrollment) -->
    <xsl:param name="catChannel"/>
    <!-- Catalog: defined here to prevent errors (as it is referenced below), but is overwritten by the file that imports it -->
    <xsl:param name="firstName"/>
    <!-- Catalog: sticky user search data of first name input -->
    <xsl:param name="lastName"/>
    <!-- Catalog: sticky user search data of last name input -->
    <xsl:param name="userID"/>
    <!-- Catalog: sticky user search data of user ID -->
	<xsl:param name="user_name"/>
    <!-- Catalog: sticky user search data of user ID input in the User Admin channel -->
	<xsl:param name="first_name"/>
    <!-- Catalog: sticky user search data of first name input in the User Admin channel -->
	<xsl:param name="last_name"/>
    <!-- Catalog: sticky user search data of last name input in the User Admin channel -->
	<xsl:param name="email"/>
    <!-- Catalog: sticky user search data of user email input in the User Admin channel -->
    <xsl:param name="searchAndOr"/>
    <!-- Catalog: sticky user search data of and/or selector -->
    <xsl:param name="optId"/>
    <!-- Catalog: sticky offering search data of section ID -->
    <xsl:param name="offName"/>
    <!-- Catalog: sticky offering search data of offering name input -->
    <xsl:param name="searchTopicName"/>
    <!-- Catalog: sticky user offering data of topic name input -->
    <xsl:param name="topicName"/>
    <xsl:param name="title"/>
    <!-- Catalog: sticky search data of curriclum title input -->
    <xsl:param name="description"/>
    <!-- Catalog: sticky search data of curriclum description input -->
    
	
    <xsl:template name="catalog">
        <!-- Catalog Page Navigation
Requires parameters catPageSize, catCurrentPage, and catLastPage.
Divides a larger set of data into smaller displayed subsets, or pages.
These pages form a catalog; the abbreviated form "cat" is used as a prefix in the naming convention for connectivity and uniqueness.
The number (integer) of displayed data per page is held in the "catPageSize" parameter (a hidden input in the form).
The number of users per page can be set by the user from a drop-down selector (named "catSelectPageSize").
The selected option of the drop-down is the current user-defined catPageSize (or the default if the user has not made a selection).
This is determined by evaluating the catPageSize parameter. For consistency, the integer 0 is used for "All".
Template catPageNav builds the page navigation menu, which consists of a drop-down menu (showing current page out of total pages), and a Go To button.
Next, Previous, First, and Last buttons are always displayed whether active or inactive.
The Next and Last buttons are inactive when the user is on the last page [catCurrentPage=catLastPage] or when there is only one page [catPageSize=0(All)].
The Previous and First buttons are inactive when the user is on the first page [catCurrentPage=1] or when there is only one page [catPageSize=0(All)].
 -->
        <xsl:param name="catSearchFlag"/><!-- Local param whose value is sent by the calling template; evaluated to determine if the search portion of the catalog is visible. A "N" value will not render the search fields -->
        <script language="JavaScript" type="text/javascript" src="javascript/common/autoForm.js"></script>

        <form name="{$catChannel}CatPageForm" action="{$baseActionURL}" method="post" onsubmit="return validator.applyFormRules(this, new CommonRulesObject())">
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="targetChannel" value="{$targetChannel}"/>
            <input type="hidden" name="command" value="{$catCurrentCommand}"/>
            <input type="hidden" name="catPageSize" value="{$catPageSize}"/>
            <!-- UniAcc: Layout Table -->
            <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                    <td class="table-nav">
                        <!-- UniAcc: Layout Table -->
                        <table class="uportal-text" width="100%">
                            <xsl:call-template name="catPages">
                                <xsl:with-param name="catSearchFlag"/>
                                <xsl:with-param name="catChannel" select="$catChannel"/>
                            </xsl:call-template>                            
                            <tr>
                                <td>
                                    <img height="3" width="1" src="{$SPACER}" alt="" title="" border="0"/>
                                </td>
                            </tr>
                            <xsl:choose>
                                <xsl:when test="$catSearchFlag = 'N'">
                                    <xsl:call-template name="catSearchNo"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="catSearchYes"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </table>
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
    
	
    <xsl:template name="catPages">
        <xsl:param name="catChannel"/>
        <tr>
            <td align="center" valign="middle">
                <xsl:call-template name="catPageNav"/>
                <!-- Page list and navigation button -->
                <img height="1" width="20" src="{$SPACER}" alt="" title="" border="0"/>
                <!-- Changing the number of data displayed per page -->
            <label for="{$catChannel}catSelectPageSize">Per page:</label>
            <select name="catSelectPageSize" id="{$catChannel}catSelectPageSize">
                    <option value="5">
                        <xsl:if test="$catPageSize = '5'">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>5</option>
                    <option value="10">
                        <xsl:if test="$catPageSize = '10'">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>10</option>
                    <option value="15">
                        <xsl:if test="$catPageSize = '15'">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>15</option>
                    <option value="25">
                        <xsl:if test="$catPageSize = '25'">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>25</option>
                    <option value="40">
                        <xsl:if test="$catPageSize = '40'">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>40</option>
					<!-- TT# 04192; Remove All from being selected as it can return huge amounts of data that cause serious performance issues -->
                    <!-- <option value="0">
                        <xsl:if test="$catPageSize = '0'">
                            <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>All</option> -->
                </select>
                <!-- image button for submitting a change in the number of users per page -->
                <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif" name="{$catChannel}CatPageSizeSubmit" id="{$catChannel}CatPageSizeSubmit" alt="Change the number displayed per page" title="Change the number displayed per page" align="absmiddle" onmouseover="swapImage('{$catChannel}CatPageSizeSubmit','channel_edit_active.gif')" onmouseout="swapImage('{$catChannel}CatPageSizeSubmit','channel_edit_base.gif')" onClick="document.{$catChannel}CatPageForm.catPageSize.value = document.{$catChannel}CatPageForm.catSelectPageSize.value; document.{$catChannel}CatPageForm.catSelectPage.selectedIndex = 0"/>
            </td>
        </tr>
    </xsl:template>
    
	
    <xsl:template name="catPageNav">
        <!-- Builds the navigation for multiple pages -->
        <!-- First and Previous controls -->
        <xsl:choose>
            <xsl:when test="$catPageSize != 0 and $catCurrentPage &gt; 1">
                <!-- Go to first page -->
                <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_first_base.gif" name="{$catChannel}CatFirstPage" id="{$catChannel}CatFirstPage" alt="Go to the first page" title="Go to the first page" align="absmiddle" onmouseover="swapImage('{$catChannel}CatFirstPage','channel_page_first_active.gif')" onmouseout="swapImage('{$catChannel}CatFirstPage','channel_page_first_base.gif')" onclick="document.{$catChannel}CatPageForm.catSelectPage.selectedIndex = 0"/>
                <!-- Go to previous page -->
                <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_prev_base.gif" name="{$catChannel}CatPrevPage" id="{$catChannel}CatPrevPage" alt="Go to the previous page" title="Go to the previous page" align="absmiddle" onmouseover="swapImage('{$catChannel}CatPrevPage','channel_page_prev_active.gif')" onmouseout="swapImage('{$catChannel}CatPrevPage','channel_page_prev_base.gif')" onclick="document.{$catChannel}CatPageForm.catSelectPage.selectedIndex = {$catCurrentPage} - 2"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- Go to first page -->
                <img src="{$CONTROLS_IMAGE_PATH}/channel_page_first_inactive.gif" align="absmiddle" alt="Go to first page button, inactive because you are currently on the first page" title="Go to first page button, inactive because you are currently on the first page"/>
                <img height="1" width="5" src="{$SPACER}" alt="" title="" border="0"/>
                <!-- Go to previous page -->
                <img src="{$CONTROLS_IMAGE_PATH}/channel_page_prev_inactive.gif" align="absmiddle" alt="Previous page button, inactive because you are currently on the first page" title="Previous page button, inactive because you are currently on the first page"/>
            </xsl:otherwise>
        </xsl:choose>
        <!-- Page list -->
    Page
    <xsl:call-template name="catPageMenu"/>
        <xsl:choose>
            <xsl:when test="$catPageSize = 0">
                <!-- Go to page -->
                <img src="{$CONTROLS_IMAGE_PATH}/channel_page_go_inactive.gif" align="absmiddle" alt="Go to page button, inactive there is only one page" title="Go to page button, inactive because there is only one page"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- Go to page -->
                <input type="image" class="image" align="absmiddle" src="{$CONTROLS_IMAGE_PATH}/channel_page_go_base.gif" name="{$catChannel}CatGoToPage" id="{$catChannel}CatGoToPage" alt="Go to page button, to go to the page selected from the drop-down menu" title="Go to page button, to go to the page selected from the drop-down menu" onmouseover="swapImage('{$catChannel}CatGoToPage','channel_page_go_active.gif')" onmouseout="swapImage('{$catChannel}CatGoToPage','channel_page_go_base.gif')">
        </input>
            </xsl:otherwise>
        </xsl:choose>
        <!-- Next and Last controls -->
        <xsl:choose>
            <xsl:when test="$catPageSize != 0 and $catCurrentPage &lt; $catLastPage">
                <!-- Go to next page -->
                <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_next_base.gif" name="{$catChannel}CatNextPage" id="{$catChannel}CatNextPage" alt="Next page button, to go to the next page" title="Next page button, to go to the next page" align="absmiddle" onmouseover="swapImage('{$catChannel}CatNextPage','channel_page_next_active.gif')" onmouseout="swapImage('{$catChannel}CatNextPage','channel_page_next_base.gif')" onclick="document.{$catChannel}CatPageForm.catSelectPage.selectedIndex = {$catCurrentPage}"/>
                <!-- Go to last page -->
                <input type="image" class="image" src="{$CONTROLS_IMAGE_PATH}/channel_page_last_base.gif" name="{$catChannel}CatLastPage" id="{$catChannel}CatLastPage" alt="Last page button, to go to the last page of users" title="Last page button, to go to the last page of users" align="absmiddle" onmouseover="swapImage('{$catChannel}CatLastPage','channel_page_last_active.gif')" onmouseout="swapImage('{$catChannel}CatLastPage','channel_page_last_base.gif')" onclick="document.{$catChannel}CatPageForm.catSelectPage.selectedIndex = {$catLastPage} - 1"/>
            </xsl:when>
            <xsl:otherwise>
                <img height="1" width="5" src="{$SPACER}" alt="" title="" border="0"/>
                <!-- Go to next page -->
                <img src="{$CONTROLS_IMAGE_PATH}/channel_page_next_inactive.gif" align="absmiddle" alt="Next page button, inactive because you are currently on the last page" title="Next page button, inactive because you are currently on the last page"/>
                <!-- Go to last page -->
                <img height="1" width="5" src="{$SPACER}" alt="" title="" border="0"/>
                <img src="{$CONTROLS_IMAGE_PATH}/channel_page_last_inactive.gif" align="absmiddle" alt="Go to last page button, inactive because you are currently on the last page" title="Go to last page button, inactive because you are currently on the last page"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
	
    <xsl:template name="catPageMenu">
            <!-- Builds the drop-down selector of pages -->
            <xsl:variable name="lastPage">
            <xsl:choose>
                <xsl:when test="$catLastPage = '0'">
                    <xsl:text>1</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text/><xsl:value-of select="$catLastPage"/><xsl:text/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <select name="catSelectPage">
          <xsl:call-template name="catPageMenuOption">
            <xsl:with-param name="catPageNumber" select="1"/>
          </xsl:call-template>
        </select> of <xsl:value-of select="$lastPage"/>
    </xsl:template>
    
	
    <xsl:template name="catPageMenuOption">
        <!-- Builds each option within the drop-down of catPageMenu -->
        <xsl:param name="catPageNumber"/>
        <option>
            <xsl:if test="$catCurrentPage = $catPageNumber">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$catPageNumber"/>
        </option>
        <xsl:if test="$catPageNumber &lt; $catLastPage">
            <!-- Recursive call if it has not reached the max number of pages -->
            <xsl:call-template name="catPageMenuOption">
                <xsl:with-param name="catPageNumber" select="$catPageNumber + 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
	
    <xsl:template name="catSearchYes">
        <!-- Builds the catalog search form -->
        <tr>
            <td align="center" class="table-content-iso">
                <xsl:if test="$catChannel = 'gradebook' or $catChannel = 'roster'">
                    <xsl:call-template name="catUserSearch"/>
                </xsl:if>
				<xsl:if test="$catChannel = 'userAdmin'">
                    <xsl:call-template name="catUserAdminSearch"/>
                </xsl:if>
                <xsl:if test="$catChannel = 'nav' or $catChannel = 'sub' or $catChannel = 'offAdmin'">
                    <xsl:call-template name="catOfferingSearch"/>
                </xsl:if>
                <xsl:if test="$catChannel = 'topicAdmin'">
                    <xsl:call-template name="catTopicSearch"/>
                </xsl:if>
                <xsl:if test="$catChannel = 'curr'">
                    <xsl:call-template name="catCurriculumSearch"/>
                </xsl:if>
				<xsl:if test = "$catChannel != ''">
	            	<input type="submit" class="uportal-button" name="catSearchSubmit" id="catSearchSubmit" value="Search" title="Submit search criteria"/>
				</xsl:if>
            </td>
        </tr>
    </xsl:template>
    
	
    <xsl:template name="catSearchNo">
        <!-- Catalog hidden inputs when search form is not visible -->
        <tr>
            <td>
                <xsl:if test="$catChannel = 'roster'">
                    <xsl:call-template name="catUserSearchHidden"/>
                </xsl:if>
            </td>
        </tr>
    </xsl:template>
	
	
    <xsl:template name="catUserSearch">
        <!-- Called from template catSearchYes. Catalog search inputs for a user search -->
        <label for="{$catChannel}firstName">First Name:</label>
        <input type="text" class="text" name="firstName" id="{$catChannel}firstName" size="10">
            <xsl:attribute name="value"><xsl:value-of select="$firstName"/></xsl:attribute>
        </input>
        <select name="searchAndOr" id="searchAndOr" title="Search Operator">
            <option value="1">
                <xsl:if test="$searchAndOr != 2">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                Or
            </option>
            <option value="2">
                <xsl:if test="$searchAndOr = 2">
                    <xsl:attribute name="selected">selected</xsl:attribute>
                </xsl:if>
                And
            </option>
        </select>
		<img height="1" width="10" src="{$SPACER}" alt="" title="" border="0"/>
    	<label for="{$catChannel}lastName">Last Name:</label>
    	<input type="text" class="text" name="lastName" id="{$catChannel}lastName" size="10">
            <xsl:attribute name="value"><xsl:value-of select="$lastName"/></xsl:attribute>
        </input>
    </xsl:template>
	
	
    <xsl:template name="catUserSearchHidden">
        <!-- Called from template catSearchNo. Catalog hidden inputs for a user search -->
        <input type="hidden" name="firstName" id="firstName">
            <xsl:attribute name="value"><xsl:value-of select="$firstName"/></xsl:attribute>
        </input>
        <input type="hidden" name="lastName" id="lastName">
            <xsl:attribute name="value"><xsl:value-of select="$lastName"/></xsl:attribute>
        </input>
        <input type="hidden" name="userID" id="userID">
            <xsl:attribute name="value"><xsl:value-of select="$userID"/></xsl:attribute>
        </input>
    </xsl:template>
	
	
	<xsl:template name="catUserAdminSearch">
        <!-- Called from template catSearchYes. Catalog search inputs for a user search on the User Admin channel -->
        <label for="{$catChannel}firstName">First Name:</label>
        <input type="text" class="text" name="first_name" id="{$catChannel}firstName" size="10">
            <xsl:attribute name="value"><xsl:value-of select="$first_name"/></xsl:attribute>
        </input>
		<img height="1" width="10" src="{$SPACER}" alt="" title="" border="0"/>
    	<label for="{$catChannel}lastName">Last Name:</label>
    	<input type="text" class="text" name="last_name" id="{$catChannel}lastName" size="10">
            <xsl:attribute name="value"><xsl:value-of select="$last_name"/></xsl:attribute>
        </input>
		<br/>
		<label for="{$catChannel}userID">User Name:</label>
    	<input type="text" class="text" name="user_name" id="{$catChannel}userID" size="10">
            <xsl:attribute name="value"><xsl:value-of select="$user_name"/></xsl:attribute>
        </input>
		<img height="1" width="10" src="{$SPACER}" alt="" title="" border="0"/>
		<label for="{$catChannel}userEmail">User Email:</label>
    	<input type="text" class="text" name="email" id="{$catChannel}userEmail" size="10">
            <xsl:attribute name="value"><xsl:value-of select="$email"/></xsl:attribute>
        </input>
    </xsl:template>
	
	
    <xsl:template name="catOfferingSearch">
        <!-- Called from template catSearchYes. Catalog search inputs for an offering search -->
        <label for="{$catChannel}topicName">Topic:</label>
        <input type="text" class="text" name="topicName" id="{$catChannel}topicName" size="8" maxlength="80">
            <xsl:attribute name="value"><xsl:value-of select="$topicName"/></xsl:attribute>
        </input>
		<img height="1" width="10" src="{$SPACER}" alt="" title="" border="0"/>
        <label for="{$catChannel}offName">Offering:</label>
        <input type="text" class="text" name="offName" id="{$catChannel}offName" size="8" maxlength="80">
            <xsl:attribute name="value"><xsl:value-of select="$offName"/></xsl:attribute>
        </input>
		<img height="1" width="10" src="{$SPACER}" alt="" title="" border="0"/>
        <label for="{$catChannel}optId">Offering ID:</label>
        <input type="text" class="text" name="optId" id="{$catChannel}optId" size="8" maxlength="10">
            <xsl:attribute name="value"><xsl:value-of select="$optId"/></xsl:attribute>
        </input>
    </xsl:template>
	
	
    <xsl:template name="catTopicSearch">
        <!-- Called from template catSearchYes. Catalog search inputs for a topic search -->
        <label for="{$catChannel}searchTopicName">Topic:</label>
        <input type="text" class="text" name="searchTopicName" id="{$catChannel}searchTopicName" size="8" maxlength="80">
            <xsl:attribute name="value"><xsl:value-of select="$searchTopicName"/></xsl:attribute>
        </input>
    </xsl:template>
	
	
    <xsl:template name="catCurriculumSearch">
        <!-- Called from template catSearchYes. Catalog search inputs for a curriculum search -->
        <label for="{$catChannel}title">Title:</label>
        <input type="text" class="text" name="title" id="{$catChannel}title" size="8" maxlength="80">
            <xsl:attribute name="value"><xsl:value-of select="$title"/></xsl:attribute>
        </input>
        <select name="searchAndOr" id="{$catChannel}searchAndOr" title="Search Operator">
            <option value="1">
            <xsl:if test="$searchAndOr != 2">
                    <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
			Or
            </option>
            <option value="2">
            <xsl:if test="$searchAndOr = 2">
                    <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
			And
            </option>
        </select>
		<img height="1" width="10" src="{$SPACER}" alt="" title="" border="0"/>
        <label for="{$catChannel}description">Description:</label>
        <input type="text" class="text" name="description" id="{$catChannel}description" size="8">
            <xsl:attribute name="value"><xsl:value-of select="$description"/></xsl:attribute>
        </input>
    </xsl:template>
	
</xsl:stylesheet>

