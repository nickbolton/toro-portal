<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Include Files -->
<xsl:include href="common.xsl"/>

<xsl:template match="offeringAdmin">
    <xsl:call-template name="links"/>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <th class="th">Topic Name</th>
            <th class="th">Offering Name</th>
            <th class="th">Description</th>
            <th class="th" width="100%">Enrollment</th>
        </tr>
        <xsl:choose>
        <xsl:when test="count(offering) = 0">
            <tr>
                <th class="table-content-single" colspan="4">No offerings found.</th>
            </tr>                    
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates select="offering"/>
        </xsl:otherwise>
        </xsl:choose>
    </table>
	<xsl:call-template name="catalog" /> <!-- Catalog for paging and search called from global.xsl -->

</xsl:template>

<xsl:template match="offering">

<tr>
    <td class="table-light-left" nowrap="nowrap">
        <xsl:value-of select="topic/name"/>
        <img height="1" width="1" src="{$SPACER}" alt="" border="0" />
    </td>
    <td class="table-content" nowrap="nowrap">
        <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$viewCommand}&amp;ID={@id}" 
		title="View details for this offering"
        onmouseover="swapImage('offeringAdminDetailsImage{@id}','channel_view_active.gif')" 
        onmouseout="swapImage('offeringAdminDetailsImage{@id}','channel_view_base.gif')">
	        <xsl:value-of select="name" /><img height="1" width="3" src="{$SPACER}"
			alt="" border="0" /><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_view_base.gif"
	        alt="'View' icon to view details for this offering"
			title="'View' icon to view details for this offering"
			align="absmiddle" 
			name="offeringAdminDetailsImage{position()}" 
			id="offeringAdminDetailsImage{position()}" />
		</a>
        <xsl:choose>
            <xsl:when test="$editOffering = 'Y'">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$editCommand}&amp;ID={@id}" 
				title="Edit this offering"
                onmouseover="swapImage('offeringAdminEditImage{@id}','channel_edit_active.gif')" 
                onmouseout="swapImage('offeringAdminEditImage{@id}','channel_edit_base.gif')">
	                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" 
					src="{$CONTROLS_IMAGE_PATH}/channel_edit_base.gif"
	                alt="'Edit' icon to edit this offering" 
					title="'Edit' icon to edit this offering"
					align="absmiddle" 
					name="offeringAdminEditImage{@id}" 
					id="offeringAdminEditImage{@id}"/>
				</a>
            </xsl:when>
            <xsl:otherwise>
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" 
				src="{$CONTROLS_IMAGE_PATH}/channel_edit_inactive.gif"
                alt="Inactive 'Edit' icon indicating insufficient permission to edit offerings"
				title="Inactive 'Edit' icon indicating insufficient permission to edit offerings" 
				align="absmiddle" />
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="$deleteOffering = 'Y'">
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command={$deleteCommand}&amp;ID={@id}" 
				title="Delete this offering"
                onmouseover="swapImage('offeringAdminDeleteImage{@id}','channel_delete_active.gif')" 
                onmouseout="swapImage('offeringAdminDeleteImage{@id}','channel_delete_base.gif')">
					<img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
	                alt="'Delete' icon to delete this offering" 
					title="'Delete' icon to delete this offering"
					align="absmiddle" 
					name="offeringAdminDeleteImage{@id}" 
					id="offeringAdminDeleteImage{@id}"/>
				</a>
            </xsl:when>
            <xsl:otherwise>
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" 
				src="{$CONTROLS_IMAGE_PATH}/channel_delete_inactive.gif"
                alt="Inactive 'Delete' icon indicating insufficient permission to delete offerings" 
				title="Inactive 'Delete' icon indicating insufficient permission to delete offerings"
				align="absmiddle" />
            </xsl:otherwise>
        </xsl:choose>
        <img height="1" width="1" src="{$SPACER}" alt="" border="0"/>
    </td>
    <td class="table-content" nowrap="nowrap">
        <xsl:value-of select="description"/>
    </td>
    <td class="table-content" nowrap="nowrap">
        <xsl:value-of select="enrollmentModel"/>
        <img height="1" width="1" src="{$SPACER}" alt="" border="0"/>
    </td>
</tr>
</xsl:template>

</xsl:stylesheet>

