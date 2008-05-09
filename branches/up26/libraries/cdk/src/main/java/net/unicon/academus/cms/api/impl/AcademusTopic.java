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

package net.unicon.academus.cms.api.impl;

// Academus CMS

// Academus CMS Facade
import net.unicon.academus.cms.api.IAcademusTopic;
import net.unicon.academus.domain.lms.Topic;

/**
 *  The Academus CMS Facade Topic implementation.
 */
public class AcademusTopic implements IAcademusTopic {

    private long id;
    private String name;
    private String description;

    /**
	 * Academus topic constructor.
	 * @param topic A topic instance that will be used
	 * to initialize this Academus topic instance.
	 */
    public AcademusTopic (Topic topic) {

        this.id = topic.getId();
        this.name = topic.getName();
        this.description = topic.getDescription();
    }

    /**
	 * Returns this topic's id.
	 * @return The topic id of this topic.
	 */
    public long getId () {

        return this.id;
    }

    /**
	 * Returns this topic's name.
	 * @return The topic name of this topic.
	 */
    public String getName () {

        return this.name;
    }

    /**
	 * Returns this topic's description.
	 * @return The topic description of this topic.
	 */
    public String getDescription() {

        return this.description;
    }
}
