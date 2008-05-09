<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" />
    <xsl:include href="common.xsl" />
    <xsl:include href="../../global/toolbar.xsl"/>
    <xsl:template match="survey-system">
        <xsl:variable name="NumOfSurvey" select="count(Form/Survey)" />
        <xsl:variable name="NumOfElection" select="count(Form/Survey[@Type = 'Election' and not(@LastReply)])" />
        <xsl:variable name="NumOfOther" select="count(Form/Survey[not(@Type = 'Election')])" />

		<div class="portlet-toolbar-container">
			<xsl:call-template name="channel-link-generic">
				<xsl:with-param name="title">Refresh</xsl:with-param>
				<xsl:with-param name="URL"><xsl:value-of select="$baseActionURL"/>?channel_command=refresh</xsl:with-param>
				<xsl:with-param name="imagePath">channel_refresh_active</xsl:with-param>
			</xsl:call-template>
		</div>
		<div class="page-title">View Surveys</div>
				
        <xsl:choose>
            <xsl:when test="$NumOfSurvey=0">  

				<div class="bounding-box1">
					<table cellpadding='0' cellspacing='0' width='100%' border='0'>
						<tr>
							<td nowrap="nowrap" class="table-content" align="center">
								There are no running surveys.
							</td>
						</tr>
					</table>
				</div>               
				
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$NumOfElection=0 and $NumOfOther=0">

						<div class="bounding-box1">
							<table cellpadding='0' cellspacing='0' width='100%' border='0'>
								<tr>
									<td nowrap="nowrap" class="table-content" align="center">
										There are no running surveys.
									</td>
								</tr>
							</table>
						</div> 

                    </xsl:when>
                    <xsl:otherwise>
                        <div class="bounding-box1">
                        	<table cellpadding="1" cellspacing="0" width="100%" border="0">
                        	    <xsl:apply-templates select="Form/Survey" />
                        	</table>
                        </div>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="Form/Survey">
 		
			<xsl:choose>
				<xsl:when test="@Type = 'Election'">
					<xsl:if test="not(@LastReply)">
						<tr>
							<td nowrap="nowrap" class="table-content">
								<!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
								<a href="{$goURL}=Survey&amp;Init=Yes&amp;FormId={../@FormId}&amp;SurveyId={@SurveyId}">
									<xsl:value-of select="@DistributionTitle" /> ,&#160;
								<xsl:value-of select="substring-before(@Sent,'_')" />
								&#160;
								<xsl:call-template name="t24to12">
									<xsl:with-param name="hour" select="substring-after(@Sent,'_')" />
								</xsl:call-template>
								&#160;
								<xsl:if test="@LastReply">(Last Replied:&#160;
								<xsl:value-of select="substring-before(@LastReply,'_')" />
								&#160;
								<xsl:call-template name="t24to12">
									<xsl:with-param name="hour" select="substring-after(@LastReply,'_')" />
								</xsl:call-template>
								)</xsl:if>
								</a>
							</td>
						</tr>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<tr>
						<td nowrap="nowrap" class="table-content">
							<!-- add uP_root=me to focus channel (i.e. not thru CSCR) -->
							<a href="{$goURL}=Survey&amp;Init=Yes&amp;FormId={../@FormId}&amp;SurveyId={@SurveyId}">
							<xsl:choose>
								<xsl:when test="@DistributionTitle != ''">
									<xsl:value-of select="@DistributionTitle" /> ,&#160;
								</xsl:when>	
								<xsl:otherwise>
									<xsl:value-of select="../@Title" /> ,&#160;
								</xsl:otherwise>
							</xsl:choose>
							<xsl:value-of select="substring-before(@Sent,'_')" />
							&#160;
							<xsl:call-template name="t24to12">
								<xsl:with-param name="hour" select="substring-after(@Sent,'_')" />
							</xsl:call-template>
							<xsl:choose>
								<xsl:when test="@Type='Anonymous'">&#160;(Anonymous)</xsl:when>
								<xsl:otherwise>
									<xsl:if test="@LastReply">&#160;(Last Replied:&#160;
									<xsl:value-of select="substring-before(@LastReply,'_')" />
									&#160;
									<xsl:call-template name="t24to12">
										<xsl:with-param name="hour" select="substring-after(@LastReply,'_')" />
									</xsl:call-template>
									)</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
							</a>
						</td>
					</tr>
				</xsl:otherwise>
			</xsl:choose>

	</xsl:template>
    
    
    <xsl:template name="t24to12">
        <xsl:param name="hour" />
        <xsl:variable name="h" select='substring-before($hour,":")' />
        <xsl:variable name="m" select='substring-after($hour,":")' />
        <xsl:choose>
            <xsl:when test="$h &gt; 12">
                <xsl:value-of select='concat($h - 12,":",$m," pm")' />
            </xsl:when>
            <xsl:when test="$h = 12">
                <xsl:value-of select='concat($h,":",$m," pm")' />
            </xsl:when>
            <xsl:when test="$h = 0">
                <xsl:value-of select='concat("12",":",$m," am")' />
            </xsl:when>
            <xsl:when test="12 &gt; $h &gt; 0">
                <xsl:value-of select='concat($h,":",$m," am")' />
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

