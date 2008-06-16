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

package net.unicon.portal.channels.gradebook;

import java.util.List;
import java.util.Collection;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.portal.channels.gradebook.GradebookScore;

public interface GradebookItem extends XMLAbleEntity {
    
    /**
     * Static constant for online assessments
     */
	public static final int TYPE_ASSESSMENT = 1;

    /**
     * Static constant for other types of activations
     */	
	public static final int TYPE_OTHER = 2;
	
    /**
     * Get id of the gradebook item
     * @return <code>int</code> id of the course version
     */

    public int getId();

    /**
     * Get id of the class
     * @return <code>int</code> id of the class
     */

    public int getOfferingId();

    /**
     * Get position of the gradebook item
     * @return <code>int</code> the position number (index starts at 0)
     */

    public int getPosition();

    /**
     * Get the weight of the item for the gradebook
     * @return <code>int</code> the weight of the item
     */

    public int getWeight();

    /**
     * Get the maximum score for the gradebook item
     * @return <code>int</code> the maximum score
     */

    public int maximumScore();

    /**
     * Get the minimum score for the gradebook item
     * @return <code>int</code> the minimum score
     */

    public int minimumScore();

    /**
     * get the type of gradebookitem
     * @return <code>int</code> the type
     */

    public int getType();

    /**
     * Get the gradebook item name or title
     * @return <code>String</code> the title or name of the gradebook item
     */

    public String getName();

    /**
     * Get the gradebook item association string.
     * @return <code>Stirng</code> this value is could be a file, url, or delivery id
     */

    public String getAssociation();

    /**
     * Display feedback
     * @return <code>boolean</code> true of false
     */

    public String displayFeedback();

    public List getGradebookScores();

    public void addGradebookScore(GradebookScore score);

    public void setGradebookScores(List gbScores);

    public void addXMLAbleEntity(XMLAbleEntity obj);

    public void addAllXMLAbleEntities(Collection collection);

    public List getActivations();

}

