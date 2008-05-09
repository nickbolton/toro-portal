<?xml version="1.0"?>
<!--
 
 Copyright (c) 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 This software is the confidential and proprietary information of
 Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 You shall not disclose such Confidential Information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with IBS.
 
 IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 THIS SOFTWARE OR ITS DERIVATIVES.

 $Log: 
  1    Channels  1.0         8/6/2002 4:54:01 PM  Freddy Lopez    
 $
 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:include href="common.xsl"/>

    <xsl:param name="baseActionURL">default</xsl:param>
  

    <xsl:template match="AddTopic">
		<xsl:call-template name="autoFormJS"/>
		
        <form action="{$baseActionURL}?action=assignApprover" method="post" onSubmit="if (!CClassifiedsRulesObject.prototype.isCancel) return validator.applyFormRules(this, new CClassifiedsRulesObject())">
            <!--UniAcc: Layout Table -->
            <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                <tr>
                    <th class="th-top-single">Create Classifieds Topic</th>
                </tr>

                <xsl:if test="//topic_name/@error">
                    <tr>
                        <td class="table-content-top">
                            <font class="uportal-channel-warning">ERROR ENCOUNTERED: Please fix field items with red (*)</font>
                        </td>
                    </tr>
                </xsl:if>
                <tr>
                    <td class="table-content-single">
                        <xsl:if test="//topic_name/@error">
                            <span class="uportal-channel-warning">*</span>
                            <img src="{$SPACER}" border="0" height="10" width="5" alt="" title=""/>
                        </xsl:if>
                        <label for="CClass-AddTopNameT1">Topic Name:</label>
                        <input type="text" size="20" maxlength="25" name="newTopicName" class="uportal-input-text" id="CClass-AddTopNameT1">
                        <xsl:attribute  name = "value" >
                            <xsl:value-of select="topic_name"/>                            
                        </xsl:attribute>
                        </input>
                    </td>
                </tr>
                <tr>
                    <td class="table-content-single">
                        <label for="CClass-AddTopDescTA1">Description: (100 characters max.)</label><br/>
                        <textarea name="description" row="2" cols="50" class="uportal-input-text" id="CClass-AddTopDescTA1">
                        	<xsl:value-of select="topic_description"/>
                        </textarea>
                    </td>
                </tr>
                <tr class="uportal-background-light">
                    <td class="table-content-single-bottom" style="text-align:center;">
<!--                        <input type="submit" name="create" value="Next" class="uportal-button"/>	-->
                        <input type="submit" name="create" value="Add Approver" class="uportal-button"/>
                        <img src="{$SPACER}" border="0" height="10" width="10" alt="" title=""/>
                        <input type="submit" name="cancel" value="Cancel" class="uportal-button" onclick="CClassifiedsRulesObject.prototype.isCancel=true;" />
                    </td>
                </tr>
            </table>
        </form>
    </xsl:template>
</xsl:stylesheet>
