<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>
<xsl:output method='html'/>

<xsl:variable name='summary'/>
<xsl:param name='mode'/>

<xsl:template match='xml-form'>
  <xsl:apply-templates select='page'>
    <xsl:with-param name='response' select='view/response'/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match='page' name='form'>
  <xsl:param name='response'/>
  <xsl:param name='disabled'/>

  <table width='100%' cellpadding='3' cellspacing='2'>
    <xsl:if test='@title'><tr class='uportal-background-dark'><td colspan='2' class='uportal-channel-title'><xsl:value-of select='@title'/></td></tr></xsl:if>
    <xsl:for-each select='question'>
      <tr>
        <td width='20' align='center' valign='middle' class='uportal-background-med'><font class='uportal-channel-title'><i><xsl:value-of select='@question-id'/></i></font></td>
        <td class='uportal-background-light'>
          <table cellpadding='0' cellspacing='0'>
            <tr><td class='uportal-channel-text' colspan="2"><xsl:value-of select='text()'/></td></tr>
            <xsl:for-each select='input'>
              <xsl:if test='string-length(normalize-space(text())) > 0'>
                <tr><td>&#160;</td><td class='uportal-channel-text'><xsl:value-of select='text()'/></td></tr>
              </xsl:if>
              <tr><td>&#160;</td><td>
                <xsl:choose>
                  <xsl:when test='$mode="summary"'>
                    <xsl:call-template name='summary'>
                      <xsl:with-param name='response' select='$response'/>
                      <xsl:with-param name='disabled' select='$disabled'/>
                    </xsl:call-template>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name='individual'>
                      <xsl:with-param name='response' select='$response'/>
                      <xsl:with-param name='disabled' select='$disabled'/>
                    </xsl:call-template>
                  </xsl:otherwise>
                </xsl:choose>
              </td></tr>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </xsl:for-each>
  </table>
</xsl:template>

<xsl:template name='individual'>
  <xsl:param name='response'/>
  <xsl:param name='disabled'/>

  <xsl:variable name='page-id' select='../../@page-id'/>              
  <xsl:variable name='question-id' select='../@question-id'/>
  <xsl:variable name='input-id' select='@input-id'/>
				
  <xsl:if test='data/@form="Row" or data/@form="Column"'>
    <xsl:variable name='type'>
      <xsl:choose>
        <xsl:when test='data/@type="Choice"'>radio</xsl:when>
        <xsl:otherwise>checkbox</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:for-each select='data/entry'>
      <xsl:variable name='data-id' select='@data-id'/>
      <xsl:variable name='data-ref'>
        <xsl:choose>
          <xsl:when test='$type="radio"'><xsl:value-of select='../entry[position()=1]/@data-id'/></xsl:when>
          <xsl:otherwise><xsl:value-of select='@data-id'/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test='$response and $response/response[@page-id=$page-id and @question-id=$question-id and @input-id=$input-id]/@data-id=$data-id'>
          <xsl:choose>
            <xsl:when test='$disabled'><input type='{$type}' name='{$page-id}.{$question-id}.{$input-id}.{$data-ref}' value='{$data-id}' checked='checked' disabled='{$disabled}'/><font class='uportal-channel-text'>
<!--Begin: Updating for HREF -->
				<xsl:choose>
					<xsl:when test="@href"><a href="{@href}" target="new"><xsl:value-of select='text()'/></a></xsl:when>
					<xsl:otherwise><xsl:value-of select='text()'/></xsl:otherwise>
				</xsl:choose>
<!--End: Updating for HREF -->
            		</font></xsl:when>
            <xsl:otherwise><input type='{$type}' name='{$page-id}.{$question-id}.{$input-id}.{$data-ref}' value='{$data-id}' checked='checked'/><font class='uportal-channel-text'>
<!--Begin: Updating for HREF -->
				<xsl:choose>
					<xsl:when test="@href"><a href="{@href}" target="new"><xsl:value-of select='text()'/></a></xsl:when>
					<xsl:otherwise><xsl:value-of select='text()'/></xsl:otherwise>
				</xsl:choose>
<!--End: Updating for HREF -->
            		</font></xsl:otherwise> 
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test='$disabled'><input type='{$type}' name='{$page-id}.{$question-id}.{$input-id}.{$data-ref}' value='{$data-id}' disabled='{$disabled}'/><font class='uportal-chanel-text'>
<!--Begin: Updating for HREF -->
				<xsl:choose>
					<xsl:when test="@href"><a href="{@href}" target="new"><xsl:value-of select='text()'/></a></xsl:when>
					<xsl:otherwise><xsl:value-of select='text()'/></xsl:otherwise>
				</xsl:choose>
<!--End: Updating for HREF -->
            		</font></xsl:when>
            <xsl:otherwise><input type='{$type}' name='{$page-id}.{$question-id}.{$input-id}.{$data-ref}' value='{$data-id}'/><font class='uportal-channel-text'>
<!--Begin: Updating for HREF -->
				<xsl:choose>
					<xsl:when test="@href"><a href="{@href}" target="new"><xsl:value-of select='text()'/></a></xsl:when>
					<xsl:otherwise><xsl:value-of select='text()'/></xsl:otherwise>
				</xsl:choose>
<!--End: Updating for HREF -->
            		</font></xsl:otherwise> 
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test='../@form="Column"'><br/></xsl:if>
    </xsl:for-each>
  </xsl:if>    

  <xsl:if test='data/@form="Droplist"'>
    <xsl:choose>
      <xsl:when test='$disabled'>
        <select class='uportal-input-text' name='{$page-id}.{$question-id}.{$input-id}.{data/entry[position()=1]/@data-id}' size='1' disabled='{$disabled}'>
          <option value=' '>Choose One</option>
          <xsl:for-each select='data/entry'>
            <xsl:variable name='data-id' select='@data-id'/>					  
            <xsl:choose>
              <xsl:when test='$response and $response/response[@page-id=$page-id and @question-id=$question-id and @input-id=$input-id]/@data-id=$data-id'>
                <option value='{@data-id}' selected='selected'><xsl:value-of select='text()'/></option>
              </xsl:when>
              <xsl:otherwise>
                <option value='{@data-id}'><xsl:value-of select='text()'/></option>
              </xsl:otherwise> 
            </xsl:choose>       
          </xsl:for-each>
        </select>    
      </xsl:when>
      <xsl:otherwise>
        <select class='uportal-input-text' name='{$page-id}.{$question-id}.{$input-id}.{data/entry[position()=1]/@data-id}' size='1'>
          <option value=' '>Choose One</option>
          <xsl:for-each select='data/entry'>
            <xsl:variable name='data-id' select='@data-id'/>					  
            <xsl:choose>
              <xsl:when test='$response and $response/response[@page-id=$page-id and @question-id=$question-id and @input-id=$input-id]/@data-id=$data-id'>
                <option value='{@data-id}' selected='selected'><xsl:value-of select='text()'/></option>
              </xsl:when>
              <xsl:otherwise>
                <option value='{@data-id}'><xsl:value-of select='text()'/></option>
              </xsl:otherwise> 
            </xsl:choose>       
          </xsl:for-each>
        </select>             
      </xsl:otherwise> 
   </xsl:choose>              
  </xsl:if>
          
  <xsl:if test='data/@type="Text"'>
    <xsl:variable name='response-text'>
      <xsl:if test='$response'>
        <xsl:value-of select='$response/response[@page-id=$page-id and @question-id=$question-id and @input-id=$input-id]/@data-text'/>
      </xsl:if>
    </xsl:variable>
    <xsl:if test='data/@form="Single"'>
      <xsl:choose>
        <xsl:when test='$disabled'>
          <input class='uportal-input-text' type='text' name='{$page-id}.{$question-id}.{@input-id}' size='40' value='{$response-text}' disabled='{$disabled}'/>
        </xsl:when>
        <xsl:otherwise>
          <input class='uportal-input-text' type='text' name='{$page-id}.{$question-id}.{@input-id}' size='40' value='{$response-text}'/>
        </xsl:otherwise>
      </xsl:choose>                                 
    </xsl:if>
    <xsl:if test='data/@form="Multiple"'>
      <xsl:choose>
        <xsl:when test='$disabled'>
          <textarea class='uportal-input-text' name='{$page-id}.{$question-id}.{@input-id}' wrap='virtual' rows='4' cols='40' disabled='{$disabled}'><xsl:value-of select='$response-text'/></textarea>
        </xsl:when>
        <xsl:otherwise>
          <textarea class='uportal-input-text' name='{$page-id}.{$question-id}.{@input-id}' wrap='virtual' rows='4' cols='40'><xsl:value-of select='$response-text'/></textarea>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:if>

</xsl:template>

<xsl:template name='summary'>
  <xsl:param name='response'/>
  <xsl:param name='disabled'/>

  <xsl:variable name='page-id' select='../../@page-id'/>              
  <xsl:variable name='question-id' select='../@question-id'/>
  <xsl:variable name='input-id' select='@input-id'/>

  <xsl:choose>
    <xsl:when test='data/@type="Choice" or data/@type="Check"'>
      <xsl:variable name='type'>
        <xsl:choose>
          <xsl:when test='data/@type="Choice"'>radio</xsl:when>
          <xsl:otherwise>checkbox</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <table cellpadding='0' cellspacing='0'>
        <xsl:for-each select='data/entry'>
          <xsl:variable name='data-id' select='@data-id'/>
          <tr>
            <td>
              <input type='{$type}' disabled='disabled'/><font class='uportal-channel-text'><xsl:value-of select='text()'/>&#160;&#160;</font>
            </td>
            <xsl:call-template name='statistics'>
              <xsl:with-param name='statistics' select='$summary/statistics[@page-id=$page-id and @question-id=$question-id and @input-id=$input-id and @data-id=$data-id]'/>
            </xsl:call-template>
          </tr>
        </xsl:for-each>
      </table>
    </xsl:when>

    <xsl:otherwise>
      <table class='uportal-background-dark' cellpadding='3' cellspacing='0'>
        <tr><td class='uportal-channel-text'>&#160;Not Applicable&#160;</td></tr>
      </table>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name='statistics'>
  <xsl:param name='statistics'/>
  <xsl:variable name='number-responses'>
    <xsl:choose>
      <xsl:when test='$statistics'><xsl:value-of select='$statistics/@number-responses'/></xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name='percent'>
    <xsl:choose>
      <xsl:when test='$statistics'><xsl:value-of select='$statistics/@percent'/></xsl:when>
      <xsl:otherwise>0%</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <td width='45' align='right' class='uportal-background-med'>&#160;<font class='uportal-channel-text'><xsl:value-of select='$number-responses'/></font>&#160;</td>
  <td width='45' align='right' class='uportal-background-dark'>&#160;<font class='uportal-channel-text'><xsl:value-of select='$percent'/></font>&#160;</td>
</xsl:template>

</xsl:stylesheet>