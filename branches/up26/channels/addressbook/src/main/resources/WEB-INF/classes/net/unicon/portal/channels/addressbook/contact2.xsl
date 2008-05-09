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
    <xsl:import href = "contact.xsl" />
    <xsl:output method="html"/>

    <xsl:template match="/">

        <form method="post" action="{$baseActionURL}" name="addressBookForm" id="addressBookForm">

            <xsl:call-template name="links">
            	<xsl:with-param name="isServantChannelDisplay">yes</xsl:with-param>
			</xsl:call-template>
        </form>

        <xsl:apply-templates/>

    </xsl:template>

</xsl:stylesheet>


