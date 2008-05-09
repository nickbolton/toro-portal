<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:include href="servant_common.xsl"/>

<!-- Include -->

    <xsl:template match="/">
    <!--
    <textarea rows="4" cols="40">
      <xsl:copy-of select = "*"/> 
   	</textarea>
    -->

        <div class="bounding-box1">
			<form action="{$baseActionURL}?servant_command=update" method="post" name="permissionsForm{$servantId}">
				<table cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td  class="table-content-right" style="text-align:left;" width="100%">
							<xsl:apply-templates select="manifest/target">
								<xsl:sort select="label"/>
							</xsl:apply-templates>
						</td>
					</tr>
					<tr>
						<td class="table-nav" colspan="2">
						<input class="uportal-button" name="submit" value="Update" type="submit" title="Submit permission changes for this channel"></input>
						<input type="button" class="uportal-button" value="Cancel" onclick="window.locationhref='{$baseActionURL}?servant_command=cancel'" title="Cancel permission changes for this channel"/></td>
					</tr>
				</table>
			</form>
		</div>
    </xsl:template>

    <xsl:template match="target">
        Please edit the permission settings for the following activities.
        <ul>
            <xsl:apply-templates select="activity">
                <xsl:sort select="./label"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="activity">
        <li>
            <xsl:if test="@allowed = 'Y'">
            <input name="{../@handle}-{@handle}" type="checkbox" class="radio" checked="true" id="permissionAdmin{label}"></input>
            </xsl:if>
            
            <xsl:if test="@allowed != 'Y'">
            <input name="{../@handle}-{@handle}" class="radio" type="checkbox" id="permissionAdmin{label}"></input>
            </xsl:if>
            
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <label for="permissionAdmin{label}"><xsl:value-of select="label"/></label>
            <img height="1" width="3" src="{$SPACER}" alt="" title="" border="0"/>
            <a href="javascript:alert('{description}');void(null);" title="Help for this option"
	            onmouseover="swapImage('permissionAdminHelpImage{@handle}','channel_help_active.gif')" 
	            onmouseout="swapImage('permissionAdminHelpImage{@handle}','channel_help_base.gif')"><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_help_base.gif"
		            alt="'Help' icon: help for '{label}'" 
		            title="'Help' icon: help for '{label}'" 
		            align="absmiddle" name="permissionAdminHelpImage{@handle}" id="permissionAdminHelpImage{@handle}"/></a> 
        </li>
    </xsl:template>

</xsl:stylesheet>
