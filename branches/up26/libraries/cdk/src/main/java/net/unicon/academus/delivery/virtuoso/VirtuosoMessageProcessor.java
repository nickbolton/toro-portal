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
package net.unicon.academus.delivery.virtuoso;

import net.unicon.sdk.cache.jms.JMSMessageProcessor;
import net.unicon.sdk.log.ILogService;
import net.unicon.portal.channels.curriculum.CurriculumService;
import net.unicon.portal.channels.gradebook.GradebookAssessmentService;

public interface VirtuosoMessageProcessor extends JMSMessageProcessor {

    /**
     * Initializes a processor with the services and parameters
     * needed to process a JMS Virtuoso message. This method
     * must be invoked before attempting to process a message.
     * @param gradebookService The gradebook service the message
     * processor will use.
     * @param curriculumService The curriculum service the message
     * processor will use.
     * @param logService The log service the message processor
     * will use to log errors and debugs.
     * @param systemID The ID of the system for which the messages
     * will be processed.
     */
    public void initializeProcessor (
            GradebookAssessmentService gradebookService,
                    CurriculumService curriculumService,
                            ILogService logService, String systemID);

}
