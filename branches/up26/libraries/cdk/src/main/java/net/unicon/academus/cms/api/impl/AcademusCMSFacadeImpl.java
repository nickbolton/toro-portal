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

// Java SDK
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

// Academus CMS

// Academus channels and services
import net.unicon.portal.channels.gradebook.GradebookService;
import net.unicon.portal.channels.gradebook.GradebookServiceFactory;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;

// Academus CMS Facade API
import net.unicon.academus.cms.api.AcademusCMSFacadeContainer;
import net.unicon.academus.cms.api.AcademusCMSFacadeException;
import net.unicon.academus.cms.api.IAcademusCMSFacade;
import net.unicon.academus.cms.api.IAcademusOffering;
import net.unicon.academus.cms.api.IAcademusTopic;
import net.unicon.academus.domain.lms.EnrollmentStatus;
import net.unicon.academus.domain.lms.Memberships;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.OfferingFactory;
import net.unicon.academus.domain.lms.Role;
import net.unicon.academus.domain.lms.RoleFactory;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicFactory;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.academus.domain.lms.UserFactory;
import net.unicon.academus.domain.lms.ensemble.AdjunctOfferingData;
import net.unicon.academus.domain.lms.ensemble.EnsembleService;
import net.unicon.academus.service.calendar.CalendarServiceFactory;

/**
 *  The Academus CMS Facade API implementation. The methods in the API
 *  allow manipulating topic, offering and offering membership information.
 */
public class AcademusCMSFacadeImpl implements IAcademusCMSFacade {

    private static AcademusCMSFacadeImpl facade;
    private static DataSource academusDataSource = null;

    private static final String DATASOURCE_PROPERTY_NAME =
        "net.unicon.portal.util.db.AcademusDBUtil.dbSource";

    private static final String DATASOURCE_PROPERTY_BASE_JNDI_NAME =
        "net.unicon.portal.util.db.AcademusDBUtil.baseJndiContext";

    private static final String DATASOURCE_NAME =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
            getProperty(DATASOURCE_PROPERTY_NAME);

    private static final String DATASOURCE_BASE_JNDI_NAME =
        UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).
            getProperty(DATASOURCE_PROPERTY_BASE_JNDI_NAME);


    private static final int MEMBER_ROLE = 7;

    private AcademusCMSFacadeImpl () throws Exception {

        // Lookup Academus data source
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup(DATASOURCE_BASE_JNDI_NAME);
        academusDataSource = (DataSource) envCtx.lookup(DATASOURCE_NAME);

    }

    /**
     * Registers this facade in the Academus CMS facade
     * container.
     * @throws Exception If registration fails.
     */
    public static void register() throws Exception {

        if (facade == null) {

            // Instantiating the facade as a singleton
            // and registering it in the Academus CMSFacade
            // container
            facade = new AcademusCMSFacadeImpl();
            AcademusCMSFacadeContainer.registerFacade(facade);
        }
    }

    /**
     * Retrieves all the available topics.
     * @return All the existing academus topic instances.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusTopic[] getAllTopics () throws AcademusCMSFacadeException {

        IAcademusTopic[] academusTopics = null;
        List topics = null;

        try {

            topics = TopicFactory.getTopics(TopicType.ACADEMICS);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while retrieving");
            errorMsg.append(" all topics from the Academus CMS.");

            throw new AcademusCMSFacadeException(errorMsg.toString(), e);
        }

        int topicsSize = topics.size();
        academusTopics = new AcademusTopic[topicsSize];

        for (int index = 0; index < topicsSize; index++) {

            Topic topic = (Topic) topics.get(index);
            academusTopics[index] = new AcademusTopic(topic);
        }

        return academusTopics;
    }

    /**
     * Retrieves the specified topic.
     * @param topicId The topic Id of the topic to be retrieved.
     * @return The Academus topic instance with the specified
     * topic id.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusTopic getTopic (long id)
    throws AcademusCMSFacadeException {

        IAcademusTopic academusTopic = null;
        Topic topic = null;

        try {

            topic = TopicFactory.getTopic(id);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while retrieving");
            errorMsg.append("topic with id \"");
            errorMsg.append(Long.toString(id));
            errorMsg.append("\".");

            throw new AcademusCMSFacadeException(errorMsg.toString(), e);
        }

        academusTopic = new AcademusTopic (topic);

        return academusTopic;
    }

    /**
     * Retrieves the offering associated with the given
     * offering id.
     * @param offeringId The offering id of the offering
     * to be retrieved.
     * @return The offering associated with the given
     * offering id.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusOffering getOffering (long offeringId)
    throws AcademusCMSFacadeException {

        IAcademusOffering academusOffering = null;
        Offering offering = null;
        AdjunctOfferingData offeringData = null;

        try {

            offering = OfferingFactory.getOffering(offeringId);
            offeringData =
                EnsembleService.getAdjunctOfferingData(offeringId);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while retrieving");
            errorMsg.append("offering with id \"");
            errorMsg.append(Long.toString(offeringId));
            errorMsg.append("\".");

            throw new AcademusCMSFacadeException(errorMsg.toString(), e);
        }

        academusOffering = new AcademusOffering (offering, offeringData);

        return academusOffering;
    }

    /**
     * Retrieves all active offerings available under
     * the specified topic.
     * @param topicId The topic id of the topic of which the offerings
     * will retrieved.
     * @param includeAdjunctData Determines if the adjunct offering data
     * will be retrieved along with every offering. If set to false
     * any parameters that are consider adjunct offering data will
     * not be retrieved.
     * @return All the offerings of the specified topic.
     * @throws AcademusCMSFacadeException
     */
    public IAcademusOffering[] getAllOfferings (
            long topicId, boolean includeAdjunctData)
                    throws AcademusCMSFacadeException {

        IAcademusOffering[] academusOfferings = null;
        AdjunctOfferingData[] offeringDataSet = null;
        List offerings = null;
        Map offeringDataTable = null;

        try {

            Topic topic = TopicFactory.getTopic(topicId);

            // Retrieve only active offerings
            offerings = OfferingFactory.getOfferings(topic, Offering.ACTIVE);

            if (includeAdjunctData) {
                 offeringDataSet = EnsembleService.getOfferingData(topicId);

                // Adjust hash table size based on total offerings
                int tableSize = (offeringDataSet.length * 4) / 3;
                offeringDataTable = new Hashtable(tableSize);

                // Build offering data lookup table
                for (int index = 0; index < offerings.size(); index++) {

                    AdjunctOfferingData offeringData =
                            offeringDataSet[index];

                    long offeringId = offeringData.getOfferingId();
                    String offeringIdStr = Long.toString(offeringId);
                    offeringDataTable.put(offeringIdStr, offeringData);
                }
            }

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while retrieving");
            errorMsg.append(" all offerings for the topic with id \"");
            errorMsg.append(topicId);
            errorMsg.append("\".");

            throw new AcademusCMSFacadeException(errorMsg.toString(), e);
        }

        int offeringsSize = offerings.size();
        academusOfferings = new AcademusOffering[offeringsSize];

        for (int index = 0; index < offeringsSize; index++) {

            Offering offering = (Offering) offerings.get(index);

            // Determine if adjuct offering data needs to included
            if (!includeAdjunctData) {

                academusOfferings[index] = new AcademusOffering(offering);

            } else {

                long offeringId = offering.getId();
                String offeringIdStr = Long.toString(offeringId);

                AdjunctOfferingData offeringData = null;

                // Look up for matching offering data entry
                if (offeringDataTable.containsKey(offeringIdStr)) {
                    offeringData =
                        (AdjunctOfferingData) offeringDataTable.get(offeringIdStr);

                } else {

                    offeringData = new AdjunctOfferingData(
                            offeringId, false, false);
                }

                academusOfferings[index] =
                        new AcademusOffering(offering, offeringData);
            }
        }

        return academusOfferings;
    }

    /**
     * Enrolls the given user to the specified offering.
     * @param username The username of the user to be enrolled.
     * @param offeringId The id of the offering the user will
     * be enrolled in.
     * @throws AcademusCMSFacadeException
     */
    public void enrollUser (String username, long offeringId)
    throws AcademusCMSFacadeException {

        Connection conn = null;

        try {

            User user = UserFactory.getUser(username);
            Offering offering = OfferingFactory.getOffering(offeringId);
            Role role = RoleFactory.getRole(MEMBER_ROLE);

            // Enroll only if not already enrolled
            if (!Memberships.isEnrolled(user, offering, EnrollmentStatus.ENROLLED)
                    && !Memberships.isEnrolled(user, offering, EnrollmentStatus.PENDING)) {

                Memberships.add(user, offering, role);

                // Add the user to the calendar of the offering
                CalendarServiceFactory.getService().
                        addUser(offering, user);

                conn = academusDataSource.getConnection();

                GradebookService gb = GradebookServiceFactory.getService();

                /**
                 * Insert scores for new user and recalculate
                 * all gradebook statistics
                 */
                gb.insertUserScores(offering, user, conn);
                gb.recalculateStatistics(offering, conn);
            }

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(128);
            errorMsg.append("An error occured while enrolling user ");
            errorMsg.append("with username \"");
            errorMsg.append(username);
            errorMsg.append("\" into offering with id \"");
            errorMsg.append(offeringId);
            errorMsg.append("\"");

            throw new AcademusCMSFacadeException(errorMsg.toString(), e);

        } finally {
            try {
                if(conn!=null) conn.close();
            } catch (Exception e) {

                /**
                 * Operation completed but resources not cleaned up
                 * Log error but continue execution
                 */
                StringBuffer errorMsg = new StringBuffer(128);
                errorMsg.append("An error occured while ");
                errorMsg.append("cleaning up database resources.");
                System.out.println(errorMsg.toString());
                e.printStackTrace();
            }
        }
    }
}

