/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version 
 * 2 of the GPL, you may redistribute this Program in connection 
 * with Free/Libre and Open Source Software ("FLOSS") applications 
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */
package net.unicon.academus.domain.assessment;

import java.util.Date;
import java.util.Map;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.portal.common.service.activation.Activation;


public class ActivationInstance implements XMLAbleEntity {

    private Activation     activation;
    private UserActivation userActivation;
    
    /**
     * <p>
     * Constructor
     * </p><p>
     *
     * </p>
     */
    public ActivationInstance(Activation activation, UserActivation userAct) {
        this.activation = activation;
        this.userActivation = userAct;
    }

    /**
     * <p>
     * Returns the activation Id for the activation.
     * </p><p>
     *
     * @return a int with the activation id for the activation.
     * </p>
     */
    public int getActivationID() {
        return activation != null ? activation.getActivationID() : -1;
    }

    /**
     * <p>
     * Returns the start date for the activation.
     * </p><p>
     * 
     * @return a Date with the activation start date.
     */
    public Date getStartDate() {
        return activation != null ? activation.getStartDate() : null;
    }
    
    /**
     * <p>
     * Returns the end date for the activation.
     * </p><p>
     * 
     * @return a Date with the activation end date.
     */
    public Date getEndDate() {
        return activation != null ? activation.getEndDate() : null;
    }
    
    /**
     * <p>
     * Returns the start time for the activation.
     * </p><p>
     * 
     * @return a long with the activation start time.
     */
    public long getStartTime() {
        return activation != null ? activation.getStartTime() : 0;
    }
    
    /**
     * <p>
     * Returns the end time for the activation.
     * </p><p>
     * 
     * @return a long with the activation end time.
     */
    public long getEndTime() {
        return activation != null ? activation.getEndTime() : 0;
    }

    /**
     * <p>
     * Returns the assessment Id for the activation.
     * </p><p>
     *
     * @return a String with the assessment id for the activation.
     * </p>
     */
    public String getAssessmentId() {
        // XXX This methods should be in a new 
        // activation (ie AssessmentActivation which would
        // extend from activation but have assessment
        // type methods like this. In addition, we would
        // not need a type since the class type would give
        // that away.  Also, there should be a factory that
        // hands out these objects to the gradebook and assessment
        // that way those classess could just call set methods instead
        // of making there own values for the attributes.  
        // ) class instead of this code.
        String rtnId = null;
        if (activation != null) {
            Map attr = activation.getAttributes();
            if (attr != null) {
                rtnId = (String) attr.get("assessment"); 
            }
        }
        return rtnId;
    }
    
    /**
     * <p>
     * Returns the assessment comments for the activation.
     * </p><p>
     *
     * @return a String with the assessment comments for the activation.
     * </p>
     */
    public String getComments() {
        // XXX This methods should be in a new 
        // activation (ie AssessmentActivation which would
        // extend from activation but have assessment
        // type methods like this. In addition, we would
        // not need a type since the class type would give
        // that away.  Also, there should be a factory that
        // hands out these objects to the gradebook and assessment
        // that way those classess could just call set methods instead
        // of making there own values for the attributes.  
        // ) class instead of this code.
        String rtnComments = null;
        if (activation != null) {
            Map attr = activation.getAttributes();
            if (attr != null) {
                rtnComments = (String) attr.get("comments"); 
            }
        }
        return rtnComments;
    }
    
    /**
     * <p>
     * Returns the number of attemps allowed for this activation.
     * </p><p>
     *
     * @return a int with the number of attempts allowed for the activation.
     * </p>
     */
    public int getAllowedAttempts() {
        // XXX This methods should be in a new 
        // activation (ie AssessmentActivation which would
        // extend from activation but have assessment
        // type methods like this. In addition, we would
        // not need a type since the class type would give
        // that away.  Also, there should be a factory that
        // hands out these objects to the gradebook and assessment
        // that way those classess could just call set methods instead
        // of making there own values for the attributes.  
        // ) class instead of this code.
        int rtnAllowedAttempts = 0;
        
        if (activation != null) {
            Map attr = activation.getAttributes();
            if (attr != null) {
                String attempts = (String) attr.get("attempts");
                try {
                    rtnAllowedAttempts = Integer.parseInt(attempts);
                } catch (Exception e) {
                    System.out.println("ActivationInstace.getAllowedAttempts() : Parse Int err");
                }
            }
        }
        return rtnAllowedAttempts;
    }

    /**
     * <p>
     * Returns the number of attemps tried for this user.
     * </p><p>
     *
     * @return a int with the number of attempts allowed for the activation.
     * </p>
     */
    public int getUserAttempts() {
        return userActivation != null ? userActivation.getAttemptsTaken() : 0;
    }

    /**
     * <p>
     * Returns the username for this activation.
     * </p><p>
     *
     * @return a String with the username of the activation.
     * </p>
     */
    public String getUsername() {
        return userActivation != null ? userActivation.getUsername() : null;
    }

    /**
     * <p>
     * Returns is the user can attempt this assessment.
     * </p><p>
     *
     * @return a boolean if the user can attempt this activation.
     * </p>
     */
    public boolean canAttempt() {
        int userAttempts = this.getUserAttempts();
        int allowedAttempts = this.getAllowedAttempts();
        return (userAttempts < allowedAttempts);
    }
    
    /**
     * <p>
     * Returns an XML Representation of the assessment instance
     * </p><p>
     *
     * @return a String with the XML representation of the activation instance.
     * </p>
     */
    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();

        xmlBuff.append("<activation-instance");
        xmlBuff.append(">");
        if (activation != null) {
            xmlBuff.append(activation.toXML());
        }

        if (userActivation != null) {
            xmlBuff.append(userActivation.toXML());
        }
        xmlBuff.append("</activation-instance>");
        return xmlBuff.toString();
    }
}

