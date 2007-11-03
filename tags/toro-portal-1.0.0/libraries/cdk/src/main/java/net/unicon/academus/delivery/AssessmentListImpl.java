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
import java.util.ArrayList;

import net.unicon.academus.common.EntityObject;
import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.Assessment;
import net.unicon.academus.delivery.AssessmentList;

public class AssessmentListImpl implements AssessmentList {

    protected List assessmentList;

    public AssessmentListImpl (List assessmentList) {

        if (assessmentList == null) {

            assessmentList = new ArrayList();

        } else {

            this.assessmentList = assessmentList;

        }

    }

    /**
     * Gets a <code>List</code> of <code>net.unicon.academus.delivery.Assessment</code> objects.
     * @return <code>List</code> of <code>Assessment/<code>
     * @see <{Assessment}>
     */

    public List getList() {

        return this.assessmentList;

    }

    /**
     * Gets the number of Assessments
     * @return <code>int</code> number of of Assessment objects
     */

    public int getSize() {

        return assessmentList.size();

    }

    /**
     * Gets an <code>Assessment</code> object at the corresponding index
     * @param <code>int</code> index
     * @return <code>net.unicon.academus.delivery.Assessment</code> object
     * @see <{Assessment}>
     */

    public Assessment getAssessment(int index) {

        return (Assessment) this.assessmentList.get(index);

    }

    public String toXML() {

        StringBuffer xml = new StringBuffer();

        xml.append("<available_assessments>");

        if (assessmentList != null) {

            for (int ix = 0; ix < this.assessmentList.size(); ++ix ) {

                xml.append(((XMLAbleEntity)this.assessmentList.get(ix)).toXML());

            }

        }

        xml.append("</available_assessments>");

        return xml.toString();

    }

}
