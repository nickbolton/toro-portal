<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0">

<!-- NOTE: This XSL was made obsolete by usability requirements -->

<!-- Include -->
<xsl:include href="common.xsl"/>
  

<xsl:template match="/">
    <!--<textarea rows="4" cols="40">
        <xsl:copy-of select = "*"/> 
        <parameter name="baseActionURL"><xsl:value-of select="$baseActionURL" /></parameter>   
        <parameter name="targetChannel"><xsl:value-of select="$targetChannel" /></parameter>   
        <parameter name="channelParam"><xsl:value-of select="$channelParam" /></parameter>   
        <parameter name="topicNameParam"><xsl:value-of select="$topicNameParam" /></parameter>   
        <parameter name="offeringNameParam"><xsl:value-of select="$offeringNameParam" /></parameter>   
        <parameter name="offeringDescParam"><xsl:value-of select="$offeringDescParam" /></parameter>   
        <parameter name="offeringNameSearchParam"><xsl:value-of select="$offeringNameSearchParam" /></parameter>   
        <parameter name="searchCommand"><xsl:value-of select="$searchCommand" /></parameter>   
        <parameter name="userEnrollmentModelParam"><xsl:value-of select="$userEnrollmentModelParam" /></parameter>   
        <parameter name="showAvailableCommand"><xsl:value-of select="$showAvailableCommand" /></parameter>   
        <parameter name="currentTopicId"><xsl:value-of select="$currentTopicId" /></parameter>   
        <parameter name="currentTopicName"><xsl:value-of select="$currentTopicName" /></parameter>   
        <parameter name="currentTopicDesc"><xsl:value-of select="$currentTopicDesc" /></parameter>   
        <parameter name="current_command"><xsl:value-of select="$current_command" /></parameter>   
    </textarea> -->
    
    <xsl:apply-templates />
    
</xsl:template>
    
<xsl:template match="subscription">
  
  <xsl:call-template name="links"/>
  
      <form method="post" action="{$baseActionURL}">
        <input type="hidden" name="targetChannel" value="{$targetChannel}"></input>
        <input type="hidden" name="command" value="{$showAvailableCommand}"></input>
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
              <th colspan="2" class="th"><img height="1" width="4" src="{$SPACER}" alt="" border="0"/></th>
          </tr>
          <tr>
            <td class="table-light-left-top" nowrap="nowrap">Topic Name:</td>
            <td class="table-content-right-top" width="100%">
            <xsl:choose>
                <xsl:when test="count(topic) = 0">
                      Currently, there are no topics.
                  </xsl:when>
                  <xsl:otherwise>
                    <select name="topicId" onchange="" size="1">
                        <xsl:apply-templates select="topic">
                             <xsl:sort select="name"/>
                        </xsl:apply-templates>
                      </select>
                      <input type="submit" class="uportal-button" value="Display"/>
                </xsl:otherwise>
            </xsl:choose>
            </td>
          </tr>
          <tr>
            <td class="table-light-left" nowrap="nowrap">Description:</td>
            <td class="table-content-right" width="100%"><xsl:value-of select="$currentTopicDesc"/></td>
          </tr>
          <tr>
              <td class="table-light-left-bottom" nowrap="nowrap" style="vertical-align:top;">Offerings:</td>
            <xsl:choose>
              <xsl:when test="count(available/offering/enrollmentModel[text() = 'Open']) = 0 and count(subscribed/offering) = 0">
            <td class="table-content-right-bottom" width="100%">
                Currently, there are no offerings.
            </td>
            </xsl:when>
              <xsl:otherwise>
            <td class="table-content-right-bottom" width="100%" style="padding-top:0px; padding-bottom:0px; padding-left:0px; padding-right:0px;">
                        
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
                <tr>
                      <th class="th-thin-left" nowrap="nowrap">
                        Topic Name<br/>
                        <img height="1" width="120" src="{$SPACER}" alt="" border="0"/>
                    </th>
                      <th class="th-thin" width="100%">Offering Name</th>
                      <th class="th-thin" nowrap="nowrap">Type</th>
                      <th class="th-thin-right" nowrap="nowrap">Status</th>
                      <!-- <th class="th" nowrap="nowrap">Options</th> -->
                </tr>
                    
                <xsl:apply-templates select="available"/>
                <xsl:apply-templates select="subscribed"/>
                <xsl:apply-templates select = "requested" />
                        
                </table>
                        
            </td>
            </xsl:otherwise>
            </xsl:choose>        
          </tr>
        </table>
       </form>
  </xsl:template>
  
  <xsl:template match="topic">
    <xsl:choose>
      <xsl:when test="@id = $currentTopicId">
        <option value="{@id}" selected="selected"><xsl:value-of select="name"/></option>
      </xsl:when>
      <xsl:otherwise>
        <option value="{@id}"><xsl:value-of select="name"/></option>
      </xsl:otherwise>
    </xsl:choose>
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

  <xsl:template match="requested">
      <xsl:apply-templates select="offering">
        <xsl:with-param name="status" select="'Requested'"/>     
        <xsl:sort select="name"/>
      </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="offering">
    <xsl:param name="status"/>

    <xsl:choose>
      <xsl:when test="(enrollmentModel='Open') and ($status='Unenrolled') and ($subscribe='Y')">
         <tr>
            <td class="table-light" style="text-align:center;" width="120">
                <xsl:value-of select="name"/>
            </td>
            <td class="table-light" style="text-align:center;" width="100%">
                <xsl:value-of select="description"/>
            </td>
            <td class="table-light" style="text-align:center;" width="70">
                <xsl:value-of select="enrollmentModel"/>
            </td>
            <td class="table-light" style="text-align:center;" width="90">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=subscribe&amp;offeringId={@id}&amp;topicId={$currentTopicId}&amp;enrollmentStatus=ENROLLED" 
                title="Enroll in this offering"
                onmouseover="swapImage('subscriptionViewImage{@id}','channel_add_active.gif')" 
                onmouseout="swapImage('subscriptionViewImage{@id}','channel_add_base.gif')">Unenrolled
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
                alt="" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}" 
                title="Enroll in this offering"/></a>
            </td>
        </tr>
      </xsl:when>
      <xsl:when test="(enrollmentModel='Request/Approve') and ($status='Unenrolled') and ($subscribe='Y')">
         <tr>
            <td class="table-light" style="text-align:center;" width="120">
                <xsl:value-of select="name"/>
            </td>
            <td class="table-light" style="text-align:center;" width="100%">
                <xsl:value-of select="description"/>
            </td>
            <td class="table-light" style="text-align:center;" width="70">
                <xsl:value-of select="enrollmentModel"/>
            </td>
            <td class="table-light" style="text-align:center;" width="90">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=subscribe&amp;offeringId={@id}&amp;topicId={$currentTopicId}&amp;enrollmentStatus=PENDING" 
                title="Send request to instructor to enroll in this offering"
                onmouseover="swapImage('subscriptionViewImage{@id}','channel_add_active.gif')" 
                onmouseout="swapImage('subscriptionViewImage{@id}','channel_add_base.gif')">Unenrolled
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_add_base.gif"
                alt="Send request to instructor to enroll in this offering" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}" 
                title="Send request to instructor to enroll in this offering"/></a>
            </td>
        </tr>
      </xsl:when>
      <xsl:when test="$subscribe!='Y' and $status='Unenrolled' and ((enrollmentModel='Open') or(enrollmentModel='Request/Approve'))">
         <tr>
            <td class="table-light" style="text-align:center;" width="120">
                <xsl:value-of select="name"/>
            </td>
            <td class="table-light" style="text-align:center;" width="100%">
                <xsl:value-of select="description"/>
            </td>
            <td class="table-light" style="text-align:center;" width="70">
                <xsl:value-of select="enrollmentModel"/>
            </td>
            <td class="table-light" style="text-align:center;" width="90">
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                unenrolled
            </td>
        </tr>
      </xsl:when>

      <xsl:when test="(enrollmentModel='Request/Approve') and ($status='Requested')">
         <tr>
            <td class="table-light" style="text-align:center;" width="120">
                <xsl:value-of select="name"/>
            </td>
            <td class="table-light" style="text-align:center;" width="100%">
                <xsl:value-of select="description"/>
            </td>
            <td class="table-light" style="text-align:center;" width="70">
                <xsl:value-of select="enrollmentModel"/>
            </td>
            <td class="table-light" style="text-align:center;" width="90">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=unenroll&amp;offeringId={@id}&amp;topicId={$currentTopicId}&amp;enrollmentStatus=PENDING" 
                title="Cancel request to enroll in this offering"
                onmouseover="swapImage('subscriptionViewImage{@id}','channel_delete_active.gif')" 
                onmouseout="swapImage('subscriptionViewImage{@id}','channel_delete_base.gif')">Pending Approval
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                alt="Cancel request to enroll in this offering" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}" 
                title="Cancel request to enroll in this offering"/></a>
            </td>
        </tr>
      </xsl:when>
      <xsl:when test="$status='Enrolled' and $unsubscribe='Y'">
        <tr>
            <td class="table-content" style="text-align:center;" width="120">
                <xsl:value-of select="name"/>
            </td>
            <td class="table-content" style="text-align:center;" width="100%">
                <xsl:value-of select="description"/>
            </td>
            <td class="table-content" style="text-align:center;" width="70">
                <xsl:value-of select="enrollmentModel"/>
            </td>
            <td class="table-content" style="text-align:center;" width="90">
                <a href="{$baseActionURL}?catPageSize={$catPageSize}&amp;command=unenroll&amp;offeringId={@id}&amp;topicId={$currentTopicId}&amp;enrollmentStatus=ENROLLED" 
                title="Unenroll from this offering"
                onmouseover="swapImage('subscriptionViewImage{@id}','channel_delete_active.gif')" 
                onmouseout="swapImage('subscriptionViewImage{@id}','channel_delete_base.gif')">Enrolled
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/><img border="0" src="{$CONTROLS_IMAGE_PATH}/channel_delete_base.gif"
                alt="" align="absmiddle" name="subscriptionViewImage{@id}" id="subscriptionViewImage{@id}" 
                title="Unenroll from this offering"/></a>
            </td>          
        </tr>
      </xsl:when>
      <xsl:when test="$unsubscribe!='Y'and $status='Enrolled'">
         <tr>
            <td class="table-light" style="text-align:center;" width="120">
                <xsl:value-of select="name"/>
            </td>
            <td class="table-light" style="text-align:center;" width="100%">
                <xsl:value-of select="description"/>
            </td>
            <td class="table-light" style="text-align:center;" width="70">
                <xsl:value-of select="enrollmentModel"/>
            </td>
            <td class="table-light" style="text-align:center;" width="90">
                <img height="1" width="3" src="{$SPACER}" alt="" border="0"/>
                enrolled
            </td>
        </tr>
      </xsl:when>

    </xsl:choose>
  </xsl:template>

  
</xsl:stylesheet>
