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

package net.unicon.academus.domain.sor;

import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.unicon.academus.domain.DomainException;
import net.unicon.academus.domain.IDomainEntity;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.groups.IGroup;
import net.unicon.portal.util.db.AcademusDBUtil;

/**
 * Provides information about the system of record for domain entities.
 */
public final class SystemOfRecordBroker {

    private static Map sorMap = new HashMap();

    // Entity type constants.
    private static final int TYPE_USER = 1000;
    private static final int TYPE_OFFERING = 1001;
    private static final int TYPE_TOPIC = 1002;
    private static final int TYPE_GROUP = 1003;

    /*
     * Public API.
     */

    /**
     * Represents Academus as a system of record.  Entities for which Academus
     * is the system of record do not have external ids.
     */
    public static final ISystemOfRecord ACADEMUS = new ISystemOfRecord() {
        public String getSourceName() { return "Academus"; }
        public AccessType getEntityAccessLevel() { return AccessType.ANY; }
        public void addRecordInfo(IDomainEntity e, String xId) {}
        public void addRecordInfo(IDomainEntity e, String xId, String foreignId) {}
        public void deleteRecordInfo(IDomainEntity e) {}
        public boolean hasGroup(String xId) {
            String msg = "'hasGroup' not meaningful for Academus SOR.";
            throw new UnsupportedOperationException(msg);
        }
        public boolean hasRole(String xId) {
            String msg = "'hasRole' not meaningful for Academus SOR.";
            throw new UnsupportedOperationException(msg);
        }
        public boolean hasTopic(String xId) {
            String msg = "'hasTopic' not meaningful for Academus SOR.";
            throw new UnsupportedOperationException(msg);
        }
        public boolean hasUser(String xId) {
            String msg = "'hasUser' not meaningful for Academus SOR.";
            throw new UnsupportedOperationException(msg);
        }
        public boolean hasOffering(String xId) {
            String msg = "'hasOffering' not meaningful for Academus SOR.";
            throw new UnsupportedOperationException(msg);
        }
        public boolean hasMembership(String xId) {
            String msg = "'hasMembership' not meaningful for Academus SOR.";
            throw new UnsupportedOperationException(msg);
        }
        public long getGroupId(String xId) { return Long.parseLong(xId); }
        public long getRoleId(String xId) { return Long.parseLong(xId); }
        public long getTopicId(String xId) { return Long.parseLong(xId); }
        public long getUserId(String xId) { return Long.parseLong(xId); }
        public long getUserIdByForeignName(String foreignId) { return Long.parseLong(foreignId); }
        public long getMembershipId(String xId) { return Long.parseLong(xId); }
        public long getOfferingId(String xId) { return Long.parseLong(xId); }
    };

    private static final IEntityRecordInfo RECORD = new IEntityRecordInfo() {
        public String getExternalId() { return null; }
        public ISystemOfRecord getSystemOfRecord() { return ACADEMUS; }
    };

    /**
     * Obtains the system of record with the specified name.
     *
     * @param sourceName The name of the desired system of record.
     * @return A system of record for domain entities.
     * @throws DomainException If the lookup failed or if the specified system
     * of record does not exist.
     */
    public static ISystemOfRecord getSystemOfRecord(String sourceName)
                                            throws DomainException {

        // Assertions.
        if (sourceName == null) {
            String msg = "Argument 'sourceName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Look in the map first.
        if (sorMap.containsKey(sourceName)) {
            return (ISystemOfRecord) sorMap.get(sourceName);
        }

        // Employ double-locking for getting the sor from the db.
        ISystemOfRecord rslt = null;
        synchronized (SystemOfRecordBroker.class) {

            if (sorMap.containsKey(sourceName)) {
                rslt = (ISystemOfRecord) sorMap.get(sourceName);
            } else {

                // Database objects.
                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;

                // Obtain a connection.
                try {
                    conn = AcademusDBUtil.getDBConnection();
                } catch (Exception e) {
                    String msg = "SystemOfRecordBroker failed to obtain a "
                                                            + "Connection";
                    throw new DomainException(msg, e);
                }

                // Lookup the requested sor.
                try {

                    // sql.
                    StringBuffer sql = new StringBuffer();
                    sql.append("Select client_id, entity_access_level ")
                            .append("From client_organization ")
                            .append("Where external_source = ?");

                    // Access the record.
                    pstmt = conn.prepareStatement(sql.toString());
                    pstmt.setString(1, sourceName);
                    rs = pstmt.executeQuery();

                    // Is it there?
                    if (rs.next()) {

                        // Create the rslt & add it to the map.
                        long id = rs.getLong("client_id");
                        int lvl = rs.getInt("entity_access_level");
                        AccessType t = AccessType.getInstance(lvl);
                        rslt = new SystemOfRecordImpl(id, sourceName, t);
                        sorMap.put(sourceName, rslt);

                    } else {

                        // There isn't an sor with the specified
                        // sourceName -- throw an exception.
                        String msg = "Unable to locate system of record with "
                                            + "the specified sourceName:  "
                                            + sourceName;
                        throw new DomainException(msg);

                    }

                } catch (SQLException sqle) {
                    String msg = "Error reading the database while attepting "
                                        + "to lookup a system of record.";
                    throw new DomainException(msg, sqle);
                } finally {

                    // Close the ResultSet
                    try {
                        if (rs != null) {
                            rs.close();
                            rs = null;
                        }
                    } catch (SQLException sqle) {
                        String msg = "SystemOfRecordBroker was unable to close "
                                                            + "a ResultSet";
                        throw new DomainException(msg, sqle);
                    }
                    // Close the PreparedStatement
                    try {
                        if (pstmt != null) {
                            pstmt.close();
                            pstmt = null;
                        }
                    } catch (SQLException sqle) {
                        String msg = "SystemOfRecordBroker was unable to close "
                                                    + "a PreparedStatement";
                        throw new DomainException(msg, sqle);
                    }
                    // Release the connection.
                    try {
                        AcademusDBUtil.releaseDBConnection(conn);
                    } catch (Exception e) {
                        String msg = "SystemOfRecordBroker failed to release "
                                                        + "its Connection";
                        throw new DomainException(msg, e);
                    }

                }

            }

        }

        return rslt;
    }

    /**
     * Obtains system of record information for the specified domain entity.
     * The result will be either Academus --
     * <code>SystemOfRecordBroker.ACADEMUS</code> -- or an external system of
     * record.
     *
     * @param e A domain entity.
     * @return System of record information.
     * @throws DomainException If the requested lookup failed.
     */
    public static IEntityRecordInfo getRecordInfo(IDomainEntity e)
    throws DomainException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [IDomainEntity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Get SQL components for entity type lookup.
        EntitySqlInfo esi = getEntitySqlInfo(e);

        // Database objects.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Obtain a connection.
        try {
            conn = AcademusDBUtil.getDBConnection();
        } catch (Exception x) {
            String msg = "SystemOfRecordBroker failed to obtain a "
                                                    + "Connection";
            throw new DomainException(msg, x);
        }

        // Lookup & create the record.
        IEntityRecordInfo rslt = null;
        try {

            // sql.
            StringBuffer sql = new StringBuffer();
            sql.append("Select ent.external_id, org.external_source ")
                    .append("From ").append(esi.getTableName()).append(" ")
                    .append("ent, client_organization org ")
                    .append("Where ent.client_id = org.client_id ")
                    .append("And ").append(esi.getKeyField()).append(" = ?");

            // Access the record.
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setLong(1, esi.getInternalId());
            rs = pstmt.executeQuery();

            // Is there an external system of record?
            if (rs.next()) {

                // Prepare the record
                String xId = rs.getString("external_id");
                String sName = rs.getString("external_source");
                rslt = new EntityRecordInfoImpl(xId, getSystemOfRecord(sName));

            } else {

                // Academus is the system of record.
                rslt = RECORD;

            }

        } catch (SQLException sqle) {
            String msg = "Error reading the database while attepting "
                                + "to lookup entity record info.";
            throw new DomainException(msg, sqle);
        } finally {

            // Close the ResultSet
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException sqle) {
                String msg = "SystemOfRecordBroker was unable to close "
                                                    + "a ResultSet";
                throw new DomainException(msg, sqle);
            }
            // Close the PreparedStatement
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException sqle) {
                String msg = "SystemOfRecordBroker was unable to close "
                                            + "a PreparedStatement";
                throw new DomainException(msg, sqle);
            }
            // Release the connection.
            try {
                AcademusDBUtil.releaseDBConnection(conn);
            } catch (Exception x) {
                String msg = "SystemOfRecordBroker failed to release its "
                                                        + "Connection";
                throw new DomainException(msg, x);
            }

        }

        return rslt;
    }

    /**
     * Obtains table and entity type sql components for
     * the specified domain entity.
     *
     * @param e A domain entity.
     * @return Entity Sql components.
     * @throws DomainException If the entity type is unknown.
     */
    private static EntitySqlInfo getEntitySqlInfo(IDomainEntity e)
    throws DomainException {

        // Map the entity type to an int.
        int eType = 0;
        if (e instanceof User) {
            eType = TYPE_USER;
        } else if (e instanceof Offering) {
            eType = TYPE_OFFERING;
        } else if (e instanceof Topic) {
            eType = TYPE_TOPIC;
        } else if (e instanceof IGroup) {
            eType = TYPE_GROUP;
        }

        // Table & type-specific sql components.
        long academusId  = -1;
        String tableName = "";
        String keyField  = "";

        switch (eType) {
            case TYPE_USER:
                User u     = (User) e;
                academusId = u.getId();
                tableName  = "client_user_key";
                keyField   = "user_id";
                break;
            case TYPE_OFFERING:
                Offering o = (Offering) e;
                academusId = o.getId();
                tableName  = "client_offering_key";
                keyField   = "offering_id";
                break;
            case TYPE_TOPIC:
                Topic t    = (Topic) e;
                academusId = t.getId();
                tableName  = "client_topic_key";
                keyField   = "topic_id";
                break;
            case TYPE_GROUP:
                IGroup g   = (IGroup) e;
                academusId = g.getGroupId();
                tableName  = "client_group_key";
                keyField   = "group_id";
                break;
            default:
                String msg = "Unknown domain entity type:  " + e.getClass();
                throw new DomainException(msg);
        }

        EntitySqlInfo rslt =
            new EntitySqlInfo(academusId, tableName, keyField);

        return rslt;
    }

    /*
     * Implementation.
     */

    private SystemOfRecordBroker() {}

    private static class SystemOfRecordImpl implements ISystemOfRecord {

        // Instance Members.
        private long id;
        private String sourceName;
        private AccessType lvl;

        /*
         * Public API.
         */

        public SystemOfRecordImpl(long id, String sourceName, AccessType lvl) {

            // Assertions.
            if (id <= 0) {
                String msg = "Argument 'id' must be provided.";
                throw new IllegalArgumentException(msg);
            }
            if (sourceName == null) {
                String msg = "Argument 'sourceName' must be provided.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.id = id;
            this.sourceName = sourceName;
            this.lvl = lvl;

        }

        public String getSourceName() {
            return sourceName;
        }

        public AccessType getEntityAccessLevel() {
            return lvl;
        }

        public boolean hasGroup(String xId) throws DomainException {
            return (__getInternalId(xId, "group") == -1L)?false:true;
        }

        public boolean hasRole(String xId) throws DomainException {
            return (__getInternalId(xId, "role") == -1L)?false:true;
        }

        public boolean hasTopic(String xId) throws DomainException {
            return (__getInternalId(xId, "topic") == -1L)?false:true;
        }

        public boolean hasUser(String xId) throws DomainException {
            return (__getInternalId(xId, "user") == -1L)?false:true;
        }

        public boolean hasOffering(String xId) throws DomainException {
            return (__getInternalId(xId, "offering") == -1L)?false:true;
        }

        public boolean hasMembership(String xId) throws DomainException {
            return (__getInternalId(xId, "membership") == -1L)?false:true;
        }

        /**
         * Provides an internal system id for the given external id.
         *
         * @param xId An external id for a group.
         * @return The internal id for a group.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getGroupId(String xId) throws DomainException {
            long rslt = __getInternalId(xId, "group");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "group id:  " + xId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * Provides an internal system id for the given external id.
         *
         * @param xId An external id for a role.
         * @return The internal id for a role.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getRoleId(String xId) throws DomainException {
            long rslt = __getInternalId(xId, "role");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "role id:  " + xId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * Provides an internal system id for the given external id.
         *
         * @param xId An external id for a topic.
         * @return The internal id for a topic.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getTopicId(String xId) throws DomainException {
            long rslt = __getInternalId(xId, "topic");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "topic id:  " + xId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * Provides an internal system id for the given external id.
         *
         * @param xId An external id for a user.
         * @return The internal id for a user.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getUserId(String xId) throws DomainException {
            long rslt = __getInternalId(xId, "user");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "user id:  " + xId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * Provides an internal system id for the given external (foreign) name.
         *
         * @param foreignId An external (foreign) name for a user.
         * @return The internal id for a user.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getUserIdByForeignName(String foreignId) throws DomainException {
            long rslt = __getInternalIdByForeignName(foreignId, "user");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "foreign name:  " + foreignId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * Provides an internal system id for the given external id.
         *
         * @param xId An external id for a membership.
         * @return The internal id for a membership.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getMembershipId(String xId) throws DomainException {
            long rslt = __getInternalId(xId, "membership");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "membership id:  " + xId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * Provides an internal system id for the given external id.
         *
         * @param xId An external id for an offering.
         * @return The internal id for an offering.
         * @throws DomainException If the internal id lookup failed.
         */
        public long getOfferingId(String xId) throws DomainException {
            long rslt = __getInternalId(xId, "offering");
            if (rslt == -1L) {
                String msg = "No sor record entry exists for the specified "
                                                + "offering id:  " + xId;
                throw new DomainException(msg);
            }
            return rslt;
        }

        /**
         * This private method takes advantage of the fact that all the
         * id mapping tables for entities follow the same schema and the
         * same naming pattern.
         * @param xId
         * @param objectType
         * @return
         * @throws DomainException
         */
        private long __getInternalId(String xId, String objectType)
                                        throws DomainException {

            long internalID = -1;

            // Database objects.
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            // Obtain a connection.
            try {
                conn = AcademusDBUtil.getDBConnection();
            } catch (Exception x) {
                String msg = "SystemOfRecordBroker failed to obtain a "
                                                        + "Connection";
                throw new DomainException(msg, x);
            }

            try {
                // sql.
                StringBuffer sql = new StringBuffer();
                sql.append("select ");
                sql.append(objectType);
                sql.append("_id from client_");
                sql.append(objectType);
                sql.append("_key where client_id = ? ")
                         .append("and external_id = ?");

                // Access the record.
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setLong(1, this.id);
                pstmt.setString(2, xId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Get the internal id if it exists.
                    internalID = rs.getLong(objectType + "_id");
                }

            } catch (SQLException sqle) {
                String msg = "Error reading the database while attepting "
                                    + "to lookup entity record info.";
                throw new DomainException(msg, sqle);
            } finally {
                // Close the ResultSet
                try {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                } catch (SQLException sqle) {
                    String msg = "SystemOfRecordBroker was unable to close "
                                                        + "a ResultSet";
                    throw new DomainException(msg, sqle);
                }
                // Close the PreparedStatement
                try {
                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (SQLException sqle) {
                    String msg = "SystemOfRecordBroker was unable to close "
                                                + "a PreparedStatement";
                    throw new DomainException(msg, sqle);
                }
                // Release the connection.
                try {
                    AcademusDBUtil.releaseDBConnection(conn);
                } catch (Exception x) {
                    String msg = "SystemOfRecordBroker failed to release its "
                                                            + "Connection";
                    throw new DomainException(msg, x);
                }
            }

            return internalID;
        } // end getInternalId

        private long __getInternalIdByForeignName(String foreignId,
                            String objectType) throws DomainException {

            long internalID = -1;

            // Database objects.
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            // Obtain a connection.
            try {
                conn = AcademusDBUtil.getDBConnection();
            } catch (Exception x) {
                String msg = "SystemOfRecordBroker failed to obtain a "
                                                        + "Connection";
                throw new DomainException(msg, x);
            }

            try {
                // sql.
                StringBuffer sql = new StringBuffer();
                sql.append("select ");
                sql.append(objectType);
                sql.append("_id from client_");
                sql.append(objectType);
                sql.append("_key where client_id = ? ")
                         .append("and foreign_id = ?");

                // Access the record.
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setLong(1, this.id);
                pstmt.setString(2, foreignId);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Get the internal id if it exists.
                    internalID = rs.getLong(objectType + "_id");
                }

            } catch (SQLException sqle) {
                String msg = "Error reading the database while attepting "
                                    + "to lookup entity record info.";
                throw new DomainException(msg, sqle);
            } finally {
                // Close the ResultSet
                try {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                } catch (SQLException sqle) {
                    String msg = "SystemOfRecordBroker was unable to close "
                                                        + "a ResultSet";
                    throw new DomainException(msg, sqle);
                }
                // Close the PreparedStatement
                try {
                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (SQLException sqle) {
                    String msg = "SystemOfRecordBroker was unable to close "
                                                + "a PreparedStatement";
                    throw new DomainException(msg, sqle);
                }
                // Release the connection.
                try {
                    AcademusDBUtil.releaseDBConnection(conn);
                } catch (Exception x) {
                    String msg = "SystemOfRecordBroker failed to release its "
                                                            + "Connection";
                    throw new DomainException(msg, x);
                }
            }

            return internalID;
        } // end getInternalId

        /**
         * Persists record information for the specified domain entity.
         *
         * @param e A domain entity.
         * @param xId An external id for the given domain entity.
         * @throws DomainException If the request to add the domain entity failed.
         */
        public void addRecordInfo(IDomainEntity e, String xId)
                                    throws DomainException {
            addRecordInfo(e, xId, null);
        }

        /**
         * Persists record information for the specified domain entity.
         *
         * @param e A domain entity.
         * @param xId An external id for the given domain entity.
         * @param foreignId A (relevant) foreign name for the entity.
         * @throws DomainException If the request to add the domain entity failed.
         */
        public void addRecordInfo(IDomainEntity e, String xId, String foreignId)
        throws DomainException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [IDomainEntity]' must be provided.";
                throw new IllegalArgumentException(msg);
            }
            if (!(xId != null && xId.trim().length() > 0)) {
                String msg = "Argument 'xId' must be provided.";
                throw new IllegalArgumentException(msg);
            }
            // NB:  foreignId is nullable.

            // Purge existing record info where applicable.
            deleteRecordInfo(e);

            // Get SQL components for entity type persistance.
            EntitySqlInfo esi = getEntitySqlInfo(e);

            // Database objects.
            Connection conn = null;
            PreparedStatement pstmt = null;

            // Obtain a connection.
            try {
                conn = AcademusDBUtil.getDBConnection();
            } catch (Exception x) {
                String msg = "SystemOfRecordBroker failed to obtain a "
                                                        + "Connection";
                throw new DomainException(msg, x);
            }

            try {
                // sql.
                StringBuffer sql = new StringBuffer();
                sql.append("insert into ")
                        .append(esi.getTableName()).append("(")
                        .append("client_id, external_id, ")
                        .append(esi.getKeyField()).append(", foreign_id) ")
                        .append("values(?, ?, ?, ?)");

                // write the record.
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setLong(1, this.id);
                pstmt.setString(2, xId);
                pstmt.setLong(3, esi.getInternalId());
                pstmt.setString(4, foreignId);
                pstmt.executeUpdate();

            } catch (SQLException sqle) {
                String msg = "Error attepting to write entity record info "
                    + "to the database.";
                throw new DomainException(msg, sqle);
            } finally {
                // Close the PreparedStatement
                try {
                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (SQLException sqle) {
                    String msg = "SystemOfRecordBroker was unable to close "
                                                + "a PreparedStatement";
                    throw new DomainException(msg, sqle);
                }
                // Release the connection.
                try {
                    AcademusDBUtil.releaseDBConnection(conn);
                } catch (Exception x) {
                    String msg = "SystemOfRecordBroker failed to release its "
                                                            + "Connection";
                    throw new DomainException(msg, x);
                }
            }
        } // end addRecordInfo

        /**
         * Deletes record information for the specified domain entity.
         *
         * @param e A domain entity.
         * @throws DomainException If the requested delete failed.
         */
        public void deleteRecordInfo(IDomainEntity e)
        throws DomainException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [IDomainEntity]' must be provided.";
                throw new IllegalArgumentException(msg);
            }

            // Get SQL components for delete of entity record information.
            EntitySqlInfo esi = getEntitySqlInfo(e);

            // Database objects.
            Connection conn = null;
            PreparedStatement pstmt = null;

            // Obtain a connection.
            try {
                conn = AcademusDBUtil.getDBConnection();
            } catch (Exception x) {
                String msg = "SystemOfRecordBroker failed to obtain a "
                                                        + "Connection";
                throw new DomainException(msg, x);
            }

            try {
                // sql.
                StringBuffer sql = new StringBuffer();
                sql.append("delete from ")
                        .append(esi.getTableName()).append(" ")
                        .append("where ")
                        .append(esi.getKeyField()).append(" = ?");

                // write the record.
                pstmt = conn.prepareStatement(sql.toString());
                pstmt.setLong(1, esi.getInternalId());
                pstmt.executeUpdate();

            } catch (SQLException sqle) {
                String msg = "Error attepting to delete entity record info"
                    + "from the database.";
                throw new DomainException(msg, sqle);
            } finally {
                // Close the PreparedStatement
                try {
                    if (pstmt != null) {
                        pstmt.close();
                        pstmt = null;
                    }
                } catch (SQLException sqle) {
                    String msg = "SystemOfRecordBroker was unable to close "
                                                + "a PreparedStatement";
                    throw new DomainException(msg, sqle);
                }
                // Release the connection.
                try {
                    AcademusDBUtil.releaseDBConnection(conn);
                } catch (Exception x) {
                    String msg = "SystemOfRecordBroker failed to release its "
                                                            + "Connection";
                    throw new DomainException(msg, x);
                }
            }
        } // end deleteRecordInfo

    } // end SystemOfRecordImpl inner class

    private static class EntityRecordInfoImpl implements IEntityRecordInfo {

        // Instance Members.
        private String externalId;
        private ISystemOfRecord sor;

        /*
         * Public API.
         */

        public EntityRecordInfoImpl(String externalId, ISystemOfRecord sor) {

            // Assertions.
            if (externalId == null) {
                String msg = "Argument 'externalId' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (sor == null) {
                String msg = "Argument 'sor' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.externalId = externalId;
            this.sor = sor;

        }

        public String getExternalId() {
            return externalId;
        }

        public ISystemOfRecord getSystemOfRecord() {
            return sor;
        }

    }

    private static class EntitySqlInfo {

        // Instance Members.
        private long internalId;
        private String tableName;
        private String keyField;

        /*
         * Public API.
         */

        public EntitySqlInfo(
                             long internalId,
                             String tableName,
                             String keyField) {

            // Assertions.
            if (!(tableName != null && tableName.trim().length() > 0)) {
                String msg = "Argument 'tableName' must be provided.";
                throw new IllegalArgumentException(msg);
            }
            if (!(keyField != null && keyField.trim().length() > 0)) {
                String msg = "Argument 'keyField' must be provided.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.internalId = internalId;
            this.tableName = tableName;
            this.keyField = keyField;
        }

        public long getInternalId() {
            return internalId;
        }

        public String getTableName() {
            return tableName;
        }

        public String getKeyField() {
            return keyField;
        }

    } // end EntitySqlInfo inner class

} // end SystemOfRecordBroker class
