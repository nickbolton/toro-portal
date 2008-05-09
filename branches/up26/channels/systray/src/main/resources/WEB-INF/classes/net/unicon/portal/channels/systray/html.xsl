<?xml version='1.0' encoding='utf-8' ?>
<!-- ========== LEGAL, NOTES, AND INSTRUCTIONS ========== -->
<!--
Copyright (c) 2004 Unicon, Inc.  All rights reserved.
Any reproduction or re-use of this code is governed by the End User License Agreement between Unicon, Inc. and the purchasing institution.

-->
<!-- ========== END LEGAL, NOTES, AND INSTRUCTIONS ========== -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- ========== VARAIBLES AND PARAMETERS ========== -->
	<xsl:output method="html" indent="yes"/>
	<xsl:param name="baseActionURL">default</xsl:param>
	<xsl:param name="messagingPortlet_id"></xsl:param>
	<xsl:variable name="mediaPath" select="'media/net/unicon/portal/channels/systray/'"/>
	<!-- ========== END VARAIBLES AND PARAMETERS ========== -->

<!-- Example input:
    <systray>
        <username>jdoe</username>
        <messages>3</messages>
    </systray>
-->

	<xsl:template match="systray">
        <xsl:apply-templates/>
	</xsl:template>

    <xsl:template match="username">
        <!--<div id="userIdentification">
            <xsl:value-of select="."/><span id="logout">[<a id="logout-link" href="Logout">Sign out</a>]</span>
        </div> -->
    </xsl:template>

    <xsl:template match="messages">
        <!-- Using this in the Quiklink list for Academus 2.0 -->
        <xsl:if test="$messagingPortlet_id != ''">
            <li class="qlink-li first">
                <a id="qNotifications" class="qlink-link first" href="{$baseActionURL}?uP_root={$messagingPortlet_id}">
                <xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?uP_root=<xsl:value-of select="$messagingPortlet_id"/></xsl:attribute>
                    <span class="qlink-label">Notifications </span>
                    <xsl:if test="./text() > 0">
                        <span class="qlink-alert-link">(<xsl:value-of select="."/> new)</span>
                    </xsl:if>
                </a>
            </li>
        </xsl:if>
        <!-- <xsl:if test="./text() > 0">
            <div class="notificationContainer">
                <div id="notificationAlert">
                    <p><h3><span>Notification Alert:</span></h3>
                        You have <xsl:value-of select="."/> new <a href="{$baseActionURL}?uP_root={$messagingPortlet_id}">message(s)</a>.</p>
                </div>
            </div>
            <hr class="hide" />
        </xsl:if> -->
    </xsl:template>

</xsl:stylesheet>
