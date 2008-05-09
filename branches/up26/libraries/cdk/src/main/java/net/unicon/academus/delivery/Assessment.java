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

package net.unicon.academus.delivery;

import java.util.List;
import java.util.Map;

import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.XMLAbleEntity;

public interface Assessment extends EntityObject, XMLAbleEntity {

    /**
     * returns the assessment id
     * @return <code>String</code> of the assessment id
     */

    public String getAssessmentID();

    /**
     * returns the langauge of the assesment
     * @return <code>String</code> language in "en", "fr", "es", etc.
     */

    public String getLanguage();

    /**
     * returns the name of the assesment
     * @return <code>String</code> name or title.
     */

    public String getName();

    /**
     * returns the description of the assessment
     * @return <code>String</code> the description
     */

    public String getDescription();

    /*

     * set the List of activation of subforms

     * @param <code>List</code> of AssessmentForm Objects

     */

    public void setAssessmentForms(List asslist);

    /*

     * returns the List of activation of subforms

     * @return <code>List</code> of AssessmentForm Objects

     */

    public List getAssessmentForms();

    /**
     * set the attributes of the activation
     * @param <code>Map<code> of the attributes of the activation
     */

    //public void setAttributes(Map attributes);

    /**
     * returns the attributes of the activation
     * @return <code>Map<code> of the attributes of the activation
     */

    //public Map getAttributes();

}

