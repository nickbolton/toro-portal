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

package net.unicon.academus.delivery.virtuoso.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;
import net.unicon.academus.domain.IDomainEventHandler;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicFactory;

import net.unicon.portal.util.db.AcademusDBUtil;

public final class ContentAssociationManager {

    static {
        TopicFactory.registerDeleteEventHandler(new TopicDeleteEventHandler());
    }

    /*
     * Public API.
     */

    public static void associate(Topic t, IContentGroup g)
                            throws DeliveryException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (g == null) {
            String msg = "Argument 'g [IContentGroup]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        associate(t, new IContentGroup[] { g });

    }

    public static synchronized void associate(Topic t, IContentGroup[] groups)
                                                throws DeliveryException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (groups == null) {
            String msg = "Argument 'groups' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (groups.length == 0) {
            String msg = "Argument 'groups' must contain at least one element.";
            throw new IllegalArgumentException(msg);
        }

        // Obtain the existing set.
        List existing = Arrays.asList(getAssociations(t));

        // Database Objects.
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            // Obtain a Connection.
            conn = AcademusDBUtil.getDBConnection();

            // Prepare the Statement.
            StringBuffer sql = new StringBuffer();
            sql.append("INSERT INTO topic_content(topic_id, group_name) ")
                                                .append("VALUES (?, ?)");
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, t.getId());

            Iterator it = Arrays.asList(groups).iterator();
            while (it.hasNext()) {
                IContentGroup g = (IContentGroup) it.next();
                if (existing.contains(g)) {
                    continue;
                }
                pstmt.setString(2, g.getName());
                pstmt.executeUpdate();
            }

        } catch (Throwable w) {
            String msg = "ContentAssociationManager failed to make a "
                                        + "requested association.";
            throw new DeliveryException(msg, w);
        } finally {

            // Release the PreparedStatement.
            try {
                if (pstmt != null) pstmt.close();
            } catch (Throwable w) {
                w.printStackTrace();
            }

            // Release the Connection.
            try {
                if (conn != null) AcademusDBUtil.releaseDBConnection(conn);
            } catch (Throwable w) {
                w.printStackTrace();
            }

        }

    }

    public static void disassociate(Topic t, IContentGroup g)
                                throws DeliveryException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (g == null) {
            String msg = "Argument 'g [IContentGroup]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        disassociate(t, new IContentGroup[] { g });

    }

    public static void disassociate(Topic t, IContentGroup[] groups)
                                        throws DeliveryException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (groups == null) {
            String msg = "Argument 'groups' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (groups.length == 0) {
            String msg = "Argument 'groups' must contain at least one element.";
            throw new IllegalArgumentException(msg);
        }

        // Database Objects.
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            // Obtain a Connection.
            conn = AcademusDBUtil.getDBConnection();

            // Prepare the Statement.
            StringBuffer sql = new StringBuffer();
            sql.append("DELETE FROM topic_content WHERE topic_id = ? AND ")
                                            .append("group_name = ?");
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, t.getId());

            Iterator it = Arrays.asList(groups).iterator();
            while (it.hasNext()) {
                IContentGroup g = (IContentGroup) it.next();
                pstmt.setString(2, g.getName());
                pstmt.executeUpdate();
            }

        } catch (Throwable w) {
            String msg = "ContentAssociationManager failed to delete a "
                                        + "requested association.";
            throw new DeliveryException(msg, w);
        } finally {

            // Release the PreparedStatement.
            try {
                if (pstmt != null) pstmt.close();
            } catch (Throwable w) {
                w.printStackTrace();
            }

            // Release the Connection.
            try {
                if (conn != null) AcademusDBUtil.releaseDBConnection(conn);
            } catch (Throwable w) {
                w.printStackTrace();
            }

        }

    }

    public static IContentGroup[] getAssociations(Topic t)
                            throws DeliveryException {

        // Assertions.
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Database Objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List handles = new ArrayList();
        try {

            // Obtain a Connection.
            conn = AcademusDBUtil.getDBConnection();

            // Prepare the Statement.
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT group_name FROM topic_content ")
                            .append("WHERE topic_id = ?");
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, t.getId());

            rs = pstmt.executeQuery();

            // Store handles in a List.
            while (rs.next()) {
                handles.add(rs.getString("group_name"));
            }

        } catch (Throwable w) {
            String msg = "ContentAssociationManager failed to retrieve the "
                                            + "requested associations.";
            throw new DeliveryException(msg, w);
        } finally {

            // Release the PreparedStatement (handles ResultSet).
            try {
                if (pstmt != null) pstmt.close();
            } catch (Throwable w) {
                w.printStackTrace();
            }

            // Release the Connection.
            try {
                if (conn != null) AcademusDBUtil.releaseDBConnection(conn);
            } catch (Throwable w) {
                w.printStackTrace();
            }

        }

        // Change to content groups carefully.
        List rslt = new ArrayList();
        Iterator it = handles.iterator();
        while (it.hasNext()) {
            String handle = (String) it.next();
            try {
                rslt.add(ContentGroupBroker.getContentGroup(handle));
            } catch (IllegalArgumentException iae) {
                // Really don't care how we got here.  This one needs to be pruned.
                pruneAssociation(handle, t);
            }
        }

        return (IContentGroup[]) rslt.toArray(new IContentGroup[0]);

    }

    /*
     * Implementation.
     */

    private ContentAssociationManager() {}

    private static void pruneAssociation(final String handle, Topic t)
                                        throws DeliveryException {

        // Assertions.
        if (handle == null) {
            // NB:  I guess there is some danger we may
            // actually have to deal with this case.
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (t == null) {
            String msg = "Argument 't [Topic]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IContentGroup proxy = new IContentGroup() {
            public String getHandle() { return handle; };
            public String getName() { return handle; };
        };

        disassociate(t, new IContentGroup[] { proxy });

    }

    /*
     * Nested Types.
     */

    private static final class TopicDeleteEventHandler
                    implements IDomainEventHandler {

        /*
         * Public API.
         */

        public TopicDeleteEventHandler() {}

        public void handleEvent(IDomainEntity e) throws DomainException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [IDomainEntity]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Remove all associations.
            Topic t = null;
            try {
                t = (Topic) e;
                IContentGroup[] groups = getAssociations(t);
                if (groups.length > 0) {
                    disassociate(t, groups);
                }
            } catch (Throwable w) {
                String msg = "ContentAssociationManager.TopicDeleteEventHandler "
                                + "failed to remove associations for Topic:  "
                                + (t != null ? t.getName() : "[unknown]");
                throw new DomainException(msg, w);
            }

        }

    }

}
