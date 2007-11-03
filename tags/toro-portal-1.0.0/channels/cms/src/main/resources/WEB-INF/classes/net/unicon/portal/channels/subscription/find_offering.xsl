<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- NOTE: This XSL was made obsolete by usability requirements -->

<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="/">
	<!-- <textarea rows="4" cols="40">
            <xsl:copy-of select = "*"/>     
    	</textarea> -->
    <xsl:call-template name="links"/>
	
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th class="th">Topic Name</th>
            <th class="th">Offering Name</th>
            <th class="th">Description</th>
            <th class="th" width="100%">Type</th>
			<th class="th" nowrap="nowrap">Status</th>
        </tr>
		<xsl:choose>
        <xsl:when test="count(offering) = 0">
            <tr>
                <th class="table-content-single" colspan="4">No offerings found.</th>
            </tr>                    
        </xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates select="available"/>
			<xsl:apply-templates select="subscribed"/>
		</xsl:otherwise>
		</xsl:choose>
        <!-- Made obsolete by catalog/usability
		<tr>
            <td class="table-nav" colspan="4">
                <form action="{$baseActionURL}" method="post">
					<input type="hidden" name="targetChannel" value="{$targetChannel}"/>
					<input type="button" class="uportal-button" value="New Search"
						onclick="window.locationhref='{$baseActionURL}?command={$searchCommand}'" title="New offering search"/>
					<input type="button" class="uportal-button" value="Cancel"
						onclick="window.locationhref='{$baseActionURL}'" title="Cancel offering search"/>
                </form>
            </td>
        </tr> -->
    </table>
	<xsl:call-template name="catalog" />
</xsl:template>

  <xsl:template match="available">
      <xsl:apply-templates select="offering">
        <xsl:with-param name="status" select="'Unenrolled'"/>
        <xsl:sort select="name"/>
      </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="subscribed">
      <xsl:apply-templates select="offering">
        <xsl:with-param name="status" select="'Enrolled'"/>     
        <xsl:sort select="name"/>
      </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="offering">
    <xsl:param name="status"/>

    <xsl:choose>
      <xsl:when test="(enrollmentModel='Open') and ($status='Unenrolled')">
         <tr>
		 	<td class="table-content-right" style="text-align:center;" width="120">
				<a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}" 
				title="View details for this offering"
				onmouseover="swapImage('offeringAdminDetailsImage{@id}','channel_view_active.gif')" 
				onmouseout="swapImage('offeringAdminDetailsImage{@id}','channel_view_base.gif')"><xsl:value-of select="name"/><img 
				height="1" width="3" src=
				"{$SPACER}"
				alt="" border="0"/><img border="0" src=
				"{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
				alt="" align="absmiddle" name="offeringAdminDetailsImage{position()}" id="offeringAdminDetailsImage{position()}"/></a>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-content" style="text-align:center;" width="120">
				<xsl:value-of select="name"/>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-content" style="text-align:center;" width="100%">
				<xsl:value-of select="description"/>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-content" style="text-align:center;" width="70">
				<xsl:value-of select="enrollmentModel"/>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-light-left" style="text-align:center;" width="70">
            	<a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=subscribe&amp;offeringId={@id}&amp;topicId={$currentTopicId}" 
				title="Enroll in this offering"
            	onmouseover="swapImage('subscriptionViewImage{@id}','channel_edit_active.gif')" 
            	onmouseout="swapImage('subscriptionViewImage{@id}','channel_edit_base.gif')">Unenrolled
            	<img height="1" width="3" src=
				"{$SPACER}"
				alt="" border="0"/><img border="0" src=
				"{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
				alt="" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}" 
				title="Enroll in this offering"/></a>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
            </td>
        </tr>
      </xsl:when>
      <xsl:when test="$status='Enrolled'">
		<tr>
			<td class="table-content-right" style="text-align:center;" width="120">
				<xsl:value-of select="topic"/>
			</td>
            <td class="table-content" style="text-align:center;" width="120">
				<a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}" 
				title="View details for this offering"
				onmouseover="swapImage('offeringAdminDetailsImage{@id}','channel_view_active.gif')" 
				onmouseout="swapImage('offeringAdminDetailsImage{@id}','channel_view_base.gif')"><xsl:value-of select="name"/><img 
				height="1" width="3" src=
				"{$SPACER}"
				alt="" border="0"/><img border="0" src=
				"{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
				alt="" align="absmiddle" name="offeringAdminDetailsImage{position()}" id="offeringAdminDetailsImage{position()}"/></a>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-content" style="text-align:center;" width="100%">
				<xsl:value-of select="description"/>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-content" style="text-align:center;" width="70">
				<xsl:value-of select="enrollmentModel"/>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
			</td>
            <td class="table-option-left" style="text-align:center;" width="70">
            	<a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=unsubscribe&amp;offeringId={@id}&amp;topicId={$currentTopicId}" 
				title="Unenroll from this offering"
            	onmouseover="swapImage('subscriptionViewImage{@id}','channel_edit_active.gif')" 
            	onmouseout="swapImage('subscriptionViewImage{@id}','channel_edit_base.gif')">Enrolled
            	<img height="1" width="3" src=
    			"{$SPACER}"
				alt="" border="0"/><img border="0" src=
				"{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
				alt="" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}" 
				title="Unenroll from this offering"/></a>
				<img height="1" width="1" src=
				"{$SPACER}"
				alt="" border="0"/>
            </td>          
		</tr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>

