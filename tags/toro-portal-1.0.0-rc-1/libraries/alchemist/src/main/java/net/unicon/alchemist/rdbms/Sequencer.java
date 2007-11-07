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

package net.unicon.alchemist.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class Sequencer {

    // Static Members.
    private static DataSource defaultDataSource = null;

    // Instance Members.
    private final DataSource dataSource;
    private final String name;
    private final int cacheSize;
    private long max;
    private long current;

    /*
     * Public API.
     */

    /**
     * Sets the default data source for <code>Sequencer</code> instances.  The
     * sequencer codebase <i>must</i> be bootsrtaped in order to use the
     * <code>Parse</code> method of construction.  Calling bootsrap more than
     * once is a no-op.
     */
    public static void bootstrap(DataSource ds) {

        // Assertions.
        if (ds == null) {
            String msg = "Argument 'ds' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Calling bootsrap more than once is a no-op.
        if (defaultDataSource != null) {
            return;
        }

        defaultDataSource = ds;

    }

    public static Sequencer parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("sequencer")) {
            String msg = "Argument 'e' must be a <sequencer> element.";
            throw new IllegalArgumentException(msg);
        }
        if (defaultDataSource == null) {
            String msg = "The sequencer component must be bootsraped before "
                    +                   "the parse method may be called.";
            throw new IllegalStateException(msg);
        }

        // Name.
        Attribute n = e.attribute("name");
        if (n == null) {
            String msg = "Element <sequencer> is missing required attribute "
                                                            + "'name'.";
            throw new IllegalArgumentException(msg);
        }
        String name = n.getValue();

        // CacheSize.
        Attribute z = e.attribute("cache-size");
        if (z == null) {
            String msg = "Element <sequencer> is missing required attribute "
                                                        + "'cache-size'.";
            throw new IllegalArgumentException(msg);
        }
        int cacheSize = 0;
        try {
            cacheSize = Integer.parseInt(z.getValue());
        } catch (NumberFormatException nfe) {
            String msg = "Attribute "
                                                        + "'cache-size'.";
            throw new IllegalArgumentException(msg);
        }

        return new Sequencer(defaultDataSource, name, cacheSize);

    }

    /**
     * Creates a new <code>Sequencer</code> instance using the default
     * datasource.  The <code>bootstrap</code> method must be called before this
     * constructor can be used.
     */
    public Sequencer(String name, int cacheSize) {
        this(defaultDataSource, name, cacheSize);
    }

    public Sequencer(DataSource ds, String name, int cacheSize) {

        // Assertions.
        if (ds == null) {
            String msg = "A DataSource must be specified.";
            throw new IllegalArgumentException(msg);
        }
        if (name == null) {
            String msg = "Argument 'name' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (cacheSize <= 0) {
            String msg = "Argument 'cacheSize' must be greater than zero.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.dataSource = ds;
        this.name = name;
        this.cacheSize = cacheSize;
        this.current = -1;
        this.max = -1;
    }

    /**
     * Returns the next number in the sequence.
     */
    public synchronized long next(){

        // Make sure we have numbers to offer.
        if (current < 0L || max < 0L || current >= max) {
            generateNumbers(this.cacheSize);
        }

        assert current < max : "Unable to acquire new sequences";
        return current++;

    }

    /**
     * Refresh the sequencer to ensure a number of values are available.
     * @param amount the number of sequences to prefetch
     */
    public synchronized void prefetch(int amount) {
        if (current < 0L || max < 0L || current >= max || (max-current) < amount)
            generateNumbers(( amount > this.cacheSize ? amount : this.cacheSize ));
    }

    private synchronized void generateNumbers(int seed) {

        long rslt = 1L;
        String stmt = null;

        // Things to scrub in a finally clause.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs  = null;
        boolean autocommit = false;
        int isolationLevel = 0;

        try {

            conn = dataSource.getConnection();

            // Record the existing auto-commit & transaction isolation level.
            autocommit = conn.getAutoCommit();
            isolationLevel = conn.getTransactionIsolation();

            // Modify the auto-commit & transaction
            // isolation level for our purposes.
            conn.setAutoCommit(false);
            if (isolationLevel != Connection.TRANSACTION_NONE && conn.getMetaData().supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)) {
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            }

            // Evaluate the value to start with.
            pstmt = conn.prepareStatement("SELECT next_index FROM uni_sequence where name = ?");
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                // Existing sequence.
                rslt = rs.getLong(1);
                stmt = "UPDATE uni_sequence SET next_index = ? WHERE name = ?";
            } else {
                // We have a brand new sequence.
                stmt = "INSERT INTO uni_sequence(next_index, name) values(?, ?)";
            }
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            // Write the new value to the table.
            pstmt = conn.prepareStatement(stmt);
            pstmt.setLong(1, rslt + seed);
            pstmt.setString(2, name);
            pstmt.execute();

            conn.commit();

        } catch(Throwable t) {
            String msg = "Sequencer '" + name + "' was unable to load a "
                                                + "set of indices.";
            throw new RuntimeException(msg, t);
        } finally {
            try{

            	// Free the statement.
                if (pstmt != null) {
                    pstmt.close();
                }

                // Free the connection.
                if (conn != null) {
                    // Rollback -- no effect if commit was successful.
                    conn.rollback();
                    // Restore the previous auto-commit & transaction isolation level.
                    conn.setAutoCommit(autocommit);
                    if (isolationLevel != Connection.TRANSACTION_NONE && conn.getMetaData().supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)) {
                        conn.setTransactionIsolation(isolationLevel);
                    }
                    // Release the connection.
                    conn.close();
                }

            }catch(Throwable t){
                String msg = "Sequencer '" + name + "' failed to clean up its "
                                                            + "resources.";
                throw new RuntimeException(msg, t);
            }

        }

        // Now update the state, since we know we succeeded.
        this.current = rslt;
        this.max = rslt + seed;
    }
}
