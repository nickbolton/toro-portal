<?xml version="1.0" encoding="utf-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" />
    <xsl:include href="common.xsl" />
    <xsl:param name="type" />
    <xsl:param name="fieldSeparator" />
    <xsl:param name="multiAnswerSeparator" />
    <xsl:template match="survey-system">

    	
    	<h2 class="page-title">Export Survey Results</h2>
    	
    	<div class="bounding-box1">
            <div class="channel" style="padding:3px 3px 3px 10px;">
                <form method="post" action="{$resourceURL}" name="exportform" id="SurveyExportForm" style="margin:0px;" target="hidden_download">
                <input type="hidden" name="sid" value="{$sid}" />
                <p style="margin:10px 0px 5px 0px;">Select the type of information to export:</p>
                <ul style="list-style:none;margin:0px 0px 10px 0px;padding:0px;">
                    <li>
                        <input type="radio" class="radio" name="type" value="summary" checked="checked" id="SSAE-SummaryR1" />
	                    <label for="SSAE-SummaryR1">Summary</label>   
                        <p class="message" style="margin:3px 0px 3px 30px;padding:0px;">Summarizes the results of all survey responses, by question.</p>
                    </li>
                    <li>
                        <input type="radio" class="radio" name="type" value="respondants" id="SSAE-RespondentsR1" />
	                    <label for="SSAE-RespondentsR1">Respondents</label>                    
                        <p class="message" style="margin:3px 0px 3px 30px;padding:0px;">Contains the summary plus a list of respondents and times they responded to the survey.</p>
                    </li>
                    <li>
                        <input type="radio" class="radio" name="type" value="details" id="SSAE-DetailsR1"/>
	                    <label for="SSAE-DetailsR1">Details</label>                    
                        <p class="message" style="margin:3px 0px 10px 30px;padding:0px;">Contains the summary plus all survey responses by respondent.</p>
                    </li>
                    <li>
                        <div>
                            <input type="radio" class="radio" name="type" value="delimited_details" id="SSAE-DelimitedDetailsR1"/>
                            <label for="SSAE-DelimitedDetailsR1">Details - delimited</label>
                            <p class="message" style="margin:3px 0px 10px 30px;padding:0px;">Contains the summary plus all survey responses by respondent, delimited for importing into another application.</p>
                            <h4 class="uportal-label" style="margin:3px 0px 3px 40px;padding:0px;">Delimiting options:</h4>
                        </div>
                        <ul style="list-style:none;margin-top:5px;">
                            <li>
                                <input type="text" class="text" style="margin:0px 3px 3px 0px;" size="1" maxlength="1" name="fieldSeparator" value="{$fieldSeparator}" id="SSAE-fieldSeparator" onchange="document.getElementById('SSAE-DelimitedDetailsR1').checked=true;"/>
                                <label for="SSAE-fieldSeparator">Field Separator</label>
                            </li>
                            <li>
                                <input type="text" class="text" style="margin:0px 3px 3px 0px;" size="1" maxlength="1" name="multiAnswerSeparator" value="{$multiAnswerSeparator}" id="SSAE-multiAnswerSeparator" onchange="document.getElementById('SSAE-DelimitedDetailsR1').checked=true;"/>
                                <label for="SSAE-multiAnswerSeparator">Multi-Answer Separator</label> 
                            </li>
                        </ul>
                    </li>
                </ul>
                <div style="width:100%;text-align:center;">
                    <input type="submit" class="uportal-button" value="Export" title="To export the selected survey information as a file" />
                    <input type="button" class="uportal-button" name="go~Peephole" value="Close" onclick="window.locationhref='{$baseActionURL}?go~Peephole=Close';" title="To cancel and return to the previous screen" />
                </div>
            </form>
        </div>
        
        </div>

    </xsl:template>
</xsl:stylesheet>

