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

package net.unicon.penelope.store.rdbms;

import javax.sql.DataSource;

import net.unicon.alchemist.rdbms.Sequencer;
import net.unicon.penelope.store.ISequencer;

/**
 * RdbmsSequencer is used to retrieve persistent sequence numbers from an RDBMS
 * backing. It does so in increments of ALLOCATION_CHUNKS, to reduce the number
 * of database hits.
 *
 * @author eandresen
 * @version 2005-02-10
 */

public final class RdbmsSequencer implements ISequencer {

    // Class members.
    public static final int ALLOCATION_CHUNKS = 10;
    public static final String SEQUENCE_NAME = "RdbmsEntityStore";

    // Instance members.
    //private DataSource dataSource = null;
    //private long current = -1;
    //private long max = -1;
    private Sequencer seq;

    /**
     * Constructor.
     *
     * @param ds DataSource to access.
     */

    /*public RdbmsSequencer(DataSource ds) {

        // Instance Members.
        this.dataSource = ds;

        // EA: Don't initialize until an id is needed.

    }*/
    
    public RdbmsSequencer(DataSource ds) {

        // Instance Members.
        this.seq = new Sequencer(ds, SEQUENCE_NAME, ALLOCATION_CHUNKS);      

    }
    
    /**
     * Get the next available number in the sequence.
     * @return the next int number in the sequence
     */
    public synchronized long next() {
        return seq.next();
    }
    

    /**
     * Get the next available number in the sequence.
     * @return the next int number in the sequence
     */
 /*   public synchronized long next() {
        if (current < 0L || max < 0L) {
            this.initSequencer();
        }

        if (current >= max) {
            this.initSequencer();
        }

        return current++;
    }

    private void initSequencer() {
        String stmt = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs  = null;
        boolean autocommit = false;
        int isolationLevel = 0;
        long rslt = 1L;
        
        try {
            String request_stmt = "SELECT seqid FROM penelope_sequencer";
            String update_stmt = "UPDATE penelope_sequencer SET seqid = ?";
            String insert_stmt = "INSERT INTO penelope_sequencer(seqid) VALUES(?)";

            try{
                conn = dataSource.getConnection();
            } catch(SQLException se) {
                String msg = "Failed to acquire a connection from the dataSource.";
                throw new RuntimeException(msg, se);
            }

            // Record the existing auto-commit & transaction isolation level.
            autocommit = conn.getAutoCommit();
            isolationLevel = conn.getTransactionIsolation();

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            pstmt = conn.prepareStatement(request_stmt);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                rslt = rs.getLong(1);
                stmt = update_stmt;
            } else {
                rslt = 1;
                stmt = insert_stmt;
            }
            pstmt.close();

            pstmt = conn.prepareStatement(stmt);
            pstmt.setLong(1, rslt + ALLOCATION_CHUNKS);
            pstmt.executeUpdate();

            conn.commit();

        } catch(SQLException se) {
            String msg = "EntityStore failed to acquire additional resource ids.";
            throw new RuntimeException(msg, se);

        } finally {
            try {
                if (conn != null) {
                    // Rollback -- should do nothing if the trasaction commited.
                    conn.rollback();
                    // Restore the previous auto-commit & transaction isolation level.
                    conn.setAutoCommit(autocommit);
                    conn.setTransactionIsolation(isolationLevel);
                }
            } catch(SQLException se) {
                String msg = "EntityStore failed to acquire additional resource ids.";
                throw new RuntimeException(msg, se);
            }
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException se) {
                throw new RuntimeException("Database Connection Error", se);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                throw new RuntimeException("Database Connection Error", se);
            }
        }

        this.current = rslt;
        this.max = rslt + ALLOCATION_CHUNKS;
    }
    */
}
