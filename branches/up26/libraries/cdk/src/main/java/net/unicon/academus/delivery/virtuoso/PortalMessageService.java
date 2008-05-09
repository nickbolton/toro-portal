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

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.log.LogServiceFactory;
import net.unicon.sdk.log.ILogService;
import net.unicon.sdk.log.LogLevel;
import net.unicon.sdk.cache.jms.JMSMessageProcessor;

import net.unicon.academus.delivery.virtuoso.content.ContentGroupBroker;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.portal.channels.curriculum.CurriculumService;
import net.unicon.portal.channels.curriculum.CurriculumServiceFactory;
import net.unicon.portal.channels.gradebook.GradebookAssessmentService;
import net.unicon.portal.channels.gradebook.GradebookAssessmentServiceFactory;

import java.util.List;

public class  PortalMessageService {

    private static PortalMessageService instance;
    private static ILogService __logService = null;

    private String systemID;
    private VirtuosoMessageProcessor messageProcessor;
    private GradebookAssessmentService service ;
    private CurriculumService curriculumService;

    // Initialize the log service
    static {
        try {
            __logService = LogServiceFactory.instance();
        }
        catch (Throwable t) {
            // couldn't create an ILogService, try this:
            __logService = new ILogService() {
                    public void log(LogLevel logLevel, String message) {}
                    public void log(LogLevel logLevel, Throwable ex) {}
                    public void log(LogLevel logLevel, String message, Throwable ex) {}
            };
        }
    }

    private PortalMessageService(String systemName)
    throws FactoryCreateException {

        try {
            this.systemID = systemName;
            this.service = GradebookAssessmentServiceFactory.getService();
            this.curriculumService = CurriculumServiceFactory.getService();

            // Initialize the message processor
            this.messageProcessor = MessageProcessorFactory.getMessageProcessor();
            this.messageProcessor.initializeProcessor(
                    service, curriculumService, __logService, systemID);

        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
            throw fce;
        }
    }

    public VirtuosoMessageProcessor getMessageProcessor () {

        return this.messageProcessor;
    }

    /**
     * Returns a singleton instance of the PortalMessageService
     * using the system name/ID provided. If the PortalMessageService
     * has already been createdm the system name/ID is ignored.
     * @param systemName The system name this PortalMessageService
     * is associated with.
     */
    public static PortalMessageService getInstance (String systemName)
    throws FactoryCreateException {

        if (instance == null) {
            instance = new PortalMessageService(systemName);
        }
        return instance;
    }

    public static void main (String[] args) throws Exception  {

        if (args.length < 1 || args.length > 3 ) {
            System.out.println("USAGE: java net.unicon.portal.external.service.PortalMessageService <register> [<delivery system>] [<username>]");
            System.exit(0);
        }

        String systemName = null;
        if (args.length < 2) {
            systemName = "Virtuoso";
        }
        else {
            systemName = args[1];
        }

        String username = null;
        if (args.length < 3) {
            username = "admin";
        }
        else {
            username = args[2];
        }
        initService(args[0], systemName, username);
    }

    public static void initService(String topicName, String systemName, String username)
    throws net.unicon.sdk.FactoryCreateException
    {
        PortalMessageService portalService = PortalMessageService.getInstance(systemName);
        VirtuosoMessageProcessor processor = portalService.getMessageProcessor();

        // try successive standard users
        User user = null;
        String[] usernames =   { username, "system", "admin", "demo" };

        boolean done = false;

        for (int j = 0; j < usernames.length && !done; j++) {
            try {
            user = UserFactory.getUser(usernames[j]);
            done = true;
            }
            catch (Exception exc) {
            done = false;
            }
        }

        if (user == null) {
            __logService.log(ILogService.INFO, "Unable to seed database with delivery curriculum and assessments.");
        }
        else {

            List courses = null;
            try {

                // Seed courses in the database.
                courses = portalService.curriculumService.getAvailableCurriculum(ContentGroupBroker.getAvailableContentGroups(), user);
                portalService.curriculumService.deleteDeliveryCourses(systemName);
                portalService.curriculumService.addDeliveryCourses(courses);

            }
            catch (Exception e) {
            e.printStackTrace();
            __logService.log(ILogService.INFO,
                    "Error attempting to initialize delivery curriculum table, check table acad_delivery_curriculum");
            }

            try {
                if (courses != null) {
                    List assessments =
                    portalService.service.getAvailableAssessments(user, courses);
                    portalService.service.deleteDeliveryAssessments(systemName);
                    portalService.service.addDeliveryAssessments(assessments, systemName);
                }
            } catch (Exception e) {
            e.printStackTrace();
            __logService.log(ILogService.INFO,
                    "Error attempting to initialize delivery assessment table, check table acad_delivery_assessment");

            }
        }
        __logService.log(ILogService.INFO, "DONE SEEDING DATABASE WITH DELIVERY DATA");
        try {
            MessageHandlerService.getInstance().register(topicName, processor);
        }
        catch (Exception exc) {
            exc.printStackTrace();
            __logService.log(ILogService.INFO, "Error registering PortalService as a MessageHandlerService, exiting");
        }
    }
}

