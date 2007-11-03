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

import java.sql.Connection;
import java.util.List;
import java.io.InputStream;
import java.io.File;

import net.unicon.academus.delivery.Assessment;
import net.unicon.academus.delivery.AssessmentList;
import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;

public interface GradebookService {

    /**
     * Gets the gradebook entries for a user or the whole offering of users.
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param filters - filters for Gradebook Entries if they exist.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of Gradebook Entries.
     * @see <{net.unicon.portal.domain.Offering}>
     * @see <{net.unicon.portal.domain.User}>
     */

    public List getGradebookEntries(
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn);

    /**
     * Gets the gradebook entries for a user or the whole offering of users.
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of Gradebook Entries.
     * @see <{net.unicon.portal.domain.Offering}>
     * @see <{net.unicon.portal.domain.User}>
     */

    public List getGradebookEntries(
                                User user,
                                Offering offering,
                                boolean forAllUsers,
                                Connection conn);

    public List getGradebookPageEntries(
                                    Offering offering,
                                    IFilterMode[] filters,
                                    IPageMode page,
                                    Connection conn);

    /**
     * Returns a specific gradebook entries
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param gradebookItemID - the gradebook entry to return
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of a single Gradebook Entry.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public List getGradebookEntry(
                                User user,
                                Offering offering,
                                int gradebookItemId,
                                boolean forAllUsers,
                                Connection conn);

    /**
     * Returns a specific gradebook entries
     * @param user - a domain object user
     * @param offering - a domain object offering.
     * @param gradebookItemID - the gradebook entry to return
     * @param forAllUsers - determines if you return all the users or just the one passed in.
     * @param filters - filters for Gradebook Entry if they exist.
     * @param conn - a database Connection.
     * @return <code>java.util.List</code> - returns a list of a single Gradebook Entry.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public List getGradebookEntry(
                                User user,
                                Offering offering,
                                int gradebookItemId,
                                boolean forAllUsers,
                                IFilterMode[] filters,
                                Connection conn);

    /**
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public int insertGradebookItem(
                                User user,
                                Offering offering,
                                int type,
                                int weight,
                                int position,
                                String name,
                                int maxScore,
                                int minScore,
                                String feedback,
                                String association,
                                Connection conn);

    /**
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public boolean deleteGradebookItem(
                                    User user,
                                    Offering offering,
                                    int gbItemID,
                                    int currentPosition,
                                    Connection conn);

    /**
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public boolean deleteGradebookItems(
                                    Offering offering,
                                    Connection conn);
    /**
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public boolean saveGradebookItem(
                                int gbItemID,
                                Offering offering,
                                int type,
                                int weight,
                                int position,
                                int oldPosition,
                                String name,
                                int maxScore,
                                int minScore,
                                String feedback,
                                String association,
                                Connection conn);

    /**
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public boolean updateGradebookMeanAndMedian(
                                            int gbItemID,
                                            Offering offering,
                                            int mean,
                                            int median,
                                            Connection conn);

     /**
      * @param offering - a domain object offering.
      * @param conn - a database Connection.
      * @return success - returns a boolean if successful.
      * @see <{net.unicon.portal.domain.Offering}>
      */
     public boolean insertUserScores(
                                 Offering offering,
                                 User userName,
                                 Connection conn);

    /**
     * Updates the users score in the gradebook.
     * @param gbItemID - the gradebook Item ID.
     * @param username - the username to be updated.
     * @param score - the new score.
     * @param Offering - the offering the user belongs in.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public boolean updateUserScore(
                                int gbItemID,
                                String userName,
                                int score,
                                Offering offering,
                                Connection conn);

    /**
     * @param offering - a domain object offering.
     * @param conn - a database Connection.
     * @return success - returns a boolean if successful.
     * @see <{net.unicon.portal.domain.Offering}>
     */
    public boolean recalculateStatistics(Offering offering, Connection conn);

    public void addGradebookScoreDetails(
                                    List gradebooks,
                                    User user,
                                    int gradbookItemID,
                                    boolean forAllUsers,
                                    Offering o,
                                    Connection conn) throws DeliveryException;

    public GradebookSubmission getGradebookSubmission(
                                                int gradebookScoreID,
                                                Connection conn);

    public GradebookFeedback getGradebookFeedback(
                                            int gradebookScoreID,
                                            Connection conn);

    public void updateSubmissionDetails(
                                    String username,
                                    int gbItemID,
                                    String submissionFileName,
                                    String submissionComment,
                                    String submissionContentType,
                                    InputStream submissionStream,
                                    Connection conn);

    public void updateFeedbackDetails(
                                String username,
                                int gbItemID,
                                String feedbackFileName,
                                String feedbackComment,
                                String feedbackContentType,
                                InputStream feedbackStream,
                                Connection conn);

    public void updateWeight(
                        int weight,
                        int gbItemID,
                        Connection conn);

    public List getAllGradebookSubmissions(
                                        Offering offering,
                                        Connection conn);

    public List getAllGradebookFeedbacks(
                                    Offering offering,
                                    Connection conn);

    public boolean updateMaxAndMinScore(
                                    int gbItemID,
                                    int minScore,
                                    int maxScore,
                                    Connection conn);

} // end GradebookService

