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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.PenelopeException;
import net.unicon.penelope.complement.AbstractType;
import net.unicon.penelope.store.AbstractEntityStore;
import net.unicon.penelope.store.ISequencer;

import org.dom4j.Element;

/**
 * RDBMS backed data store for Penelope Choice and Decision collections.
 *
 * @author eandresen
 * @version 2005-02-10
 */
public class RdbmsEntityStore extends AbstractEntityStore {

    // Statements.
    private final String STORE_OPTION_STATEMENT =
            "INSERT INTO penelope_option(id, handle, label, type) "
          + "VALUES(?,?,?,?)";
    private final String EXISTS_OPTION_STATEMENT =
            "SELECT id FROM penelope_option WHERE id = ?";
    private final String RETRIEVE_OPTION_STATEMENT =
            "SELECT id, handle, label, type FROM penelope_option "
          + "WHERE id = ?";
    private final String DELETE_OPTION_STATEMENT =
            "DELETE FROM penelope_option WHERE id = ?";

    private final String STORE_CHOICE_STATEMENT =
            "INSERT INTO penelope_choice(id, handle, label, min_selections, "
          + "max_selections) VALUES(?,?,?,?,?)";
    private final String EXISTS_CHOICE_STATEMENT =
            "SELECT id FROM penelope_choice WHERE id = ?";
    private final String RETRIEVE_CHOICE_STATEMENT =
            "SELECT id, handle, label, min_selections, max_selections "
          + "FROM penelope_choice WHERE id = ?";
    private final String DELETE_CHOICE_STATEMENT =
            "DELETE FROM penelope_choice WHERE id = ?";

    private final String STORE_CHOICE_OPTION_STATEMENT =
            "INSERT INTO penelope_choice_options(choiceid, optionid) "
          + "VALUES(?,?)";
    private final String RETRIEVE_CHOICE_OPTION_STATEMENT =
            "SELECT optionid FROM penelope_choice_options WHERE choiceid = ?";
    private final String DELETE_CHOICE_OPTION_STATEMENT =
            "DELETE FROM penelope_choice_options WHERE choiceid = ?";

    private final String STORE_CHOICE_COLLECTION_STATEMENT =
            "INSERT INTO penelope_choice_collection(id, handle, label) "
          + "VALUES(?,?,?)";
    private final String EXISTS_CHOICE_COLLECTION_STATEMENT =
            "SELECT id FROM penelope_choice_collection WHERE id = ? "
          + "OR handle = ?";
    private final String RETRIEVE_CHOICE_COLLECTION_STATEMENT =
            "SELECT id, handle, label FROM penelope_choice_collection "
          + "WHERE id = ?";
    private final String LOOKUP_CHOICE_COLLECTION_STATEMENT =
            "SELECT id FROM penelope_choice_collection WHERE handle = ?";
    private final String DELETE_CHOICE_COLLECTION_STATEMENT =
            "DELETE FROM penelope_choice_collection WHERE id = ? ";
    private final String RETRIEVE_CHOICE_COLLECTION_DEPENDENTS_STATEMENT =
            "SELECT id FROM penelope_decision_collection "
          + "WHERE collectionid = ?";

    private final String STORE_CHOICE_COLLECTION_CHOICES_STATEMENT =
            "INSERT INTO penelope_choice_collection_choices(collectionid, "
          + "choiceid) VALUES(?,?)";
    private final String RETRIEVE_CHOICE_COLLECTION_CHOICES_STATEMENT =
            "SELECT choiceid FROM penelope_choice_collection_choices "
          + "WHERE collectionid = ?";
    private final String DELETE_CHOICE_COLLECTION_CHOICES_STATEMENT =
            "DELETE FROM penelope_choice_collection_choices "
          + "WHERE collectionid = ?";

    private final String STORE_SELECTION_STATEMENT =
            "INSERT INTO penelope_selection(id, optionid, complement) "
          + "VALUES(?,?,?)";
    private final String EXISTS_SELECTION_STATEMENT =
            "SELECT id FROM penelope_selection WHERE id = ?";
    private final String RETRIEVE_SELECTION_STATEMENT =
            "SELECT id, optionid, complement FROM penelope_selection "
          + "WHERE id = ?";
    private final String DELETE_SELECTION_STATEMENT =
            "DELETE FROM penelope_selection WHERE id = ?";

    private final String STORE_DECISION_STATEMENT =
            "INSERT INTO penelope_decision(id, choiceid) "
          + "VALUES(?,?)";
    private final String EXISTS_DECISION_STATEMENT =
            "SELECT id FROM penelope_decision WHERE id = ?";
    private final String RETRIEVE_DECISION_STATEMENT =
            "SELECT id, choiceid FROM penelope_decision WHERE id = ?";
    private final String DELETE_DECISION_STATEMENT =
            "DELETE FROM penelope_decision WHERE id = ?";

    private final String STORE_DECISION_SELECTIONS_STATEMENT =
            "INSERT INTO penelope_decision_selections(decisionid, "
          + "selectionid) VALUES(?,?)";
    private final String RETRIEVE_DECISION_SELECTIONS_STATEMENT =
            "SELECT selectionid FROM penelope_decision_selections "
          + "WHERE decisionid = ?";
    private final String DELETE_DECISION_SELECTIONS_STATEMENT =
            "DELETE FROM penelope_decision_selections WHERE decisionid = ?";

    private final String STORE_DECISION_COLLECTION_STATEMENT =
            "INSERT INTO penelope_decision_collection(id, collectionid) "
          + "VALUES(?,?)";
    private final String EXISTS_DECISION_COLLECTION_STATEMENT =
            "SELECT id FROM penelope_decision_collection WHERE id = ?";
    private final String RETRIEVE_DECISION_COLLECTION_STATEMENT =
            "SELECT id, collectionid FROM penelope_decision_collection "
          + "WHERE id = ?";
    private final String DELETE_DECISION_COLLECTION_STATEMENT =
            "DELETE FROM penelope_decision_collection WHERE id = ?";

    private final String STORE_DECISION_COLLECTION_DECISIONS_STATEMENT =
            "INSERT INTO penelope_decision_collection_decisions("
          + "collectionid, decisionid) VALUES(?,?)";
    private final String RETRIEVE_DECISION_COLLECTION_DECISIONS_STATEMENT =
            "SELECT decisionid "
          + "FROM penelope_decision_collection_decisions "
          + "WHERE collectionid = ?";
    private final String DELETE_DECISION_COLLECTION_DECISIONS_STATEMENT =
            "DELETE FROM penelope_decision_collection_decisions "
          + "WHERE collectionid = ?";

    // Instance Members.
    private final ISequencer seq;
    private final DataSource dataSource;

    /*
     * Public API.
     */

    public RdbmsEntityStore(DataSource ds) {

        // Instance Members.
        this.seq = new RdbmsSequencer(ds);
        this.dataSource = ds;
        
        final String msg = "This EntityStore Implementation is not "
            + "currently supported. Modifications to Penelope options to "
            + "enable Spring generated IComplementTypes has not been ported "
            + "into this version since this implementation is not used "
            + "anywhere. Refer to JIRA Issue AC-108. Specifically, the "
            + "enhancements in AC-108 will require modification of the "
            + "database schema around the 'type' field of 'penelope_option'";
        throw new UnsupportedOperationException(msg);

    }

    public IChoiceCollection parseChoiceCollection(Element e)
                                throws PenelopeException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("choice-collection")) {
            String msg = "Argument 'e' must be an <choice-collection> element.";
            throw new IllegalArgumentException(msg);
        }

        // Parse the entities.
        IChoiceCollection rslt = null;
        try {
            rslt = ChoiceCollectionImpl.parse(e, this);
        } catch (Throwable t) {
            String msg = "Unable to construct a choice collection from the "
                                                        + "specified XML.";
            throw new EntityCreateException(msg, t);
        }

        // ...

        return rslt;

    }
    
    
    public IOption createOption(Handle handle, Label label,
                    IComplementType complementType)
                    throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IOption rslt = new OptionImpl(id, this, handle, label, complementType);

        // ...

        return rslt;

    }

    public IChoice createChoice(Handle handle, Label label, IOption[] options,
                                    int minSelections, int maxSelections)
                                    throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IChoice rslt = new ChoiceImpl(id, this, handle, label, options,
                                    minSelections, maxSelections);

        // ...

        return rslt;

    }

    public IChoiceCollection createChoiceCollection(Handle handle, Label label,
                            IChoice[] choices) throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IChoiceCollection rslt = new ChoiceCollectionImpl(id, this, handle,
                                                        label, choices);

        // ...

        return rslt;

    }

    public ISelection createSelection(IOption option, IComplement complement)
                                            throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        ISelection rslt = new SelectionImpl(id, this, option, complement);

        // ...

        return rslt;

    }

    public IDecision createDecision(Label l, IChoice choice,
                                ISelection[] selections)
                                throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IDecision rslt = new DecisionImpl(id, this, l, choice, selections);

        // ...

        return rslt;

    }

    public IDecisionCollection createDecisionCollection(
                            IChoiceCollection choiceCollection,
                            IDecision[] decisions)
                            throws EntityCreateException {

        // NB:  Validation to be handled by AbstractEntityStore.

        long id = seq.next();
        IDecisionCollection rslt = new DecisionCollectionImpl(id, this,
                                        choiceCollection, decisions);

        // ...

        return rslt;

    }

    public IChoiceCollection getChoiceCollection(Handle handle) {
        Connection conn = getConnection();
        IChoiceCollection rslt = null;

        try {
            rslt = getChoiceCollection(handle, conn);
        } finally {
            if (conn != null) closeConnection(conn);
        }

        return rslt;
    }

    public boolean existsChoiceCollection(Handle handle) {
        boolean rslt = false;
        Connection conn = getConnection();

        try {
            rslt = existsChoiceCollection(handle, conn);
        } finally {
            if (conn != null) closeConnection(conn);
        }

        return rslt;
    }

    public IChoiceCollection getChoiceCollection(long id) {
        Connection conn = getConnection();
        IChoiceCollection rslt = null;

        try {
            rslt = getChoiceCollection(id, conn);
        } finally {
            if (conn != null) closeConnection(conn);
        }

        return rslt;
    }

    public void storeChoiceCollection(IChoiceCollection c) {
        Connection conn = getConnection();
        boolean autocommit = false;

        try {
            autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            storeChoiceCollection(c, conn);

            conn.commit();
        } catch(SQLException se) {
            String msg = "Error storing ChoiceColleciton.";
            throw new RuntimeException(msg, se);
        } finally {
            try {
                if (conn != null) {
                    // Rollback -- should do nothing if the trasaction commited.
                    conn.rollback();
                    // Restore the previous auto-commit setting
                    conn.setAutoCommit(autocommit);
                }
            } catch(SQLException se) {
                String msg = "Database connectivity problem.";
                throw new RuntimeException(msg, se);
            }
            if (conn != null) closeConnection(conn);
        }
    }

    /**
     * Delete a ChoiceCollection.
     * This will cascade into deletion of any DecisionCollections that
     * reference this choice collection.
     */
    public void deleteChoiceCollection(IChoiceCollection c) {
        Connection conn = getConnection();
        boolean autocommit = false;

        try {
            autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            deleteChoiceCollection(c, conn);

            conn.commit();
        } catch(SQLException se) {
            String msg = "Error deleting ChoiceColleciton.";
            throw new RuntimeException(msg, se);
        } finally {
            try {
                if (conn != null) {
                    // Rollback -- should do nothing if the trasaction commited.
                    conn.rollback();
                    // Restore the previous auto-commit setting
                    conn.setAutoCommit(autocommit);
                }
            } catch(SQLException se) {
                String msg = "Database connectivity problem.";
                throw new RuntimeException(msg, se);
            }
            if (conn != null) closeConnection(conn);
        }
    }

    public IDecisionCollection getDecisionCollection(long id) {
        Connection conn = getConnection();
        IDecisionCollection rslt = null;

        try {
            rslt = getDecisionCollection(id, conn);
        } finally {
            if (conn != null) closeConnection(conn);
        }

        return rslt;
    }

    public void storeDecisionCollection(IDecisionCollection d) {
        Connection conn = getConnection();
        boolean autocommit = false;

        try {
            autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            storeDecisionCollection(d, conn);

            conn.commit();
        } catch(SQLException se) {
            String msg = "Error storing DecisionColleciton.";
            throw new RuntimeException(msg, se);
        } finally {
            try {
                if (conn != null) {
                    // Rollback -- should do nothing if the trasaction commited.
                    conn.rollback();
                    // Restore the previous auto-commit setting
                    conn.setAutoCommit(autocommit);
                }
            } catch(SQLException se) {
                String msg = "Database connectivity problem.";
                throw new RuntimeException(msg, se);
            }
            if (conn != null) closeConnection(conn);
        }
    }

    /**
     * Delete a DecisionCollection.
     * This will not delete the associated ChoiceCollection.
     */
    public void deleteChoiceCollection(IDecisionCollection d) {
        Connection conn = getConnection();
        boolean autocommit = false;

        try {
            autocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            deleteDecisionCollection(d, conn);

            conn.commit();
        } catch(SQLException se) {
            String msg = "Error deleting DecisionColleciton.";
            throw new RuntimeException(msg, se);
        } finally {
            try {
                if (conn != null) {
                    // Rollback -- should do nothing if the trasaction commited.
                    conn.rollback();
                    // Restore the previous auto-commit setting
                    conn.setAutoCommit(autocommit);
                }
            } catch(SQLException se) {
                String msg = "Database connectivity problem.";
                throw new RuntimeException(msg, se);
            }
            if (conn != null) closeConnection(conn);
        }
    }

    /*
     * Protected API.
     */

    protected IOption getOption(long id, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        IOption rslt = null;

        try{
            pstmt = conn.prepareStatement(RETRIEVE_OPTION_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                rslt = new OptionImpl(
                        id,
                        this,
                        Handle.create(rs.getString("handle")),
                        Label.create(rs.getString("label")),
                        AbstractType.getInstance(rs.getString("type")));
            }

        } catch(PenelopeException pe) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Option.";
            throw new RuntimeException(msg, pe);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Option.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected void storeOption(IOption o, Connection conn) {
        PreparedStatement pstmt = null;
        try{
            if (existsOption(o, conn)) {
                // Our job is already done.
                return;
            }

            pstmt = conn.prepareStatement(STORE_OPTION_STATEMENT);
            pstmt.setLong(1, o.getId());
            pstmt.setString(2, o.getHandle().getValue());
            Label l = o.getLabel();
            pstmt.setString(3, (l != null ? l.getValue() : null));
            pstmt.setString(4, o.getComplementType().getClass().getName());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Option.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected void deleteOption(IOption o, Connection conn) {
        PreparedStatement pstmt = null;
        try{
            if (!existsOption(o, conn)) {
                // Our job is already done.
                return;
            }

            pstmt = conn.prepareStatement(DELETE_OPTION_STATEMENT);
            pstmt.setLong(1, o.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to delete the " +
                    "given Option.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected boolean existsOption(IOption o, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(EXISTS_OPTION_STATEMENT);
            pstmt.setLong(1, o.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to check for the " +
                    "given Option.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected IChoice getChoice(long id, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List options = new ArrayList();
        IChoice rslt = null;

        try{
            pstmt = conn.prepareStatement(RETRIEVE_CHOICE_OPTION_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                options.add(getOption(rs.getLong("optionid"), conn));
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement(RETRIEVE_CHOICE_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rslt = new ChoiceImpl(
                        id,
                        this,
                        Handle.create(rs.getString("handle")),
                        Label.create(rs.getString("label")),
                        (IOption[])options.toArray(new IOption[0]),
                        rs.getInt("min_selections"),
                        rs.getInt("max_selections"));
            }
            rs.close();
            pstmt.close();

        } catch (EntityCreateException e) {
            String msg = "Error creating Choice object.";
            throw new RuntimeException(msg, e);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Choice.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected void storeChoice(IChoice c, Connection conn) {
        IOption[] options = c.getOptions();
        PreparedStatement pstmt = null;

        try{
            if (existsChoice(c, conn)) {
                // Our job is already done.
                return;
            }

            // Make sure all of the referenced options exist in the database.
            for (int i = 0; i < options.length; i++) {
                storeOption(options[i], conn);
            }

            pstmt = conn.prepareStatement(STORE_CHOICE_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.setString(2, c.getHandle().getValue());
            Label l = c.getLabel();
            pstmt.setString(3, (l != null ? l.getValue() : null));
            pstmt.setInt(4, c.getMinSelections());
            pstmt.setInt(5, c.getMaxSelections());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            pstmt = conn.prepareStatement(STORE_CHOICE_OPTION_STATEMENT);
            pstmt.setLong(1, c.getId());
            for (int i = 0; i < options.length; i++) {
                pstmt.setLong(2, options[i].getId());
                pstmt.executeUpdate();
            }
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Choice.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

    }

    protected void deleteChoice(IChoice c, Connection conn) {
        IOption[] options = c.getOptions();
        PreparedStatement pstmt = null;

        try{
            if (!existsChoice(c, conn)) {
                // Our job is already done.
                return;
            }

            pstmt = conn.prepareStatement(DELETE_CHOICE_OPTION_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            // Make sure all of the referenced options are deleted from the
            // database.
            for (int i = 0; i < options.length; i++) {
                deleteOption(options[i], conn);
            }

            pstmt = conn.prepareStatement(DELETE_CHOICE_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to delete the " +
                    "given Choice.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

    }

    protected boolean existsChoice(IChoice c, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(EXISTS_CHOICE_STATEMENT);
            pstmt.setLong(1, c.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Choice.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected IChoiceCollection getChoiceCollection(Handle handle, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long id = -1;
        IChoiceCollection rslt = null;

        try{
            pstmt = conn.prepareStatement(LOOKUP_CHOICE_COLLECTION_STATEMENT);
            pstmt.setString(1, handle.getValue());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getLong("id");
            }
            rs.close();
            pstmt.close();

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Choice Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        if (id > 0)
            rslt = getChoiceCollection(id);

        return rslt;
    }

    protected IChoiceCollection getChoiceCollection(long id, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List choices = new ArrayList();
        IChoiceCollection rslt = null;

        try{
            pstmt = conn.prepareStatement(RETRIEVE_CHOICE_COLLECTION_CHOICES_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                choices.add(getChoice(rs.getLong("choiceid"), conn));
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement(RETRIEVE_CHOICE_COLLECTION_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String label = rs.getString("label");
                rslt = new ChoiceCollectionImpl(
                        id,
                        this,
                        Handle.create(rs.getString("handle")),
                        (label != null ? Label.create(label) : null),
                        (IChoice[])choices.toArray(new IChoice[0]));
            }
            rs.close();
            pstmt.close();

        } catch (EntityCreateException e) {
            String msg = "Error creating ChoiceCollection object.";
            throw new RuntimeException(msg, e);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Choice Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected void storeChoiceCollection(IChoiceCollection c, Connection conn) {
        IChoice[] choices = c.getChoices();
        PreparedStatement pstmt = null;

        try{
            if (existsChoiceCollection(c, conn)) {
                // Our job is already done.
                return;
            }

            // Make sure all of the referenced choices exist in the database.
            for (int i = 0; i < choices.length; i++) {
                storeChoice(choices[i], conn);
            }

            pstmt = conn.prepareStatement(STORE_CHOICE_COLLECTION_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.setString(2, c.getHandle().getValue());
            Label l = c.getLabel();
            pstmt.setString(3, (l != null ? l.getValue() : null));
            pstmt.executeUpdate();
            closeStatement(pstmt);

            pstmt = conn.prepareStatement(
                        STORE_CHOICE_COLLECTION_CHOICES_STATEMENT);
            pstmt.setLong(1, c.getId());
            for (int i = 0; i < choices.length; i++) {
                pstmt.setLong(2, choices[i].getId());
                pstmt.executeUpdate();
            }
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Choice Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected void deleteChoiceCollection(IChoiceCollection c, Connection conn) {
        IChoice[] choices = c.getChoices();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            if (!existsChoiceCollection(c, conn)) {
                // Our job is already done.
                return;
            }

            // Delete any decision collections that reference this choice collection.
            pstmt = conn.prepareStatement(
                        RETRIEVE_CHOICE_COLLECTION_DEPENDENTS_STATEMENT);
            pstmt.setLong(1, c.getId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                deleteDecisionCollection(
                        getDecisionCollection(rs.getLong("id"), conn),
                        conn);
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement(
                        DELETE_CHOICE_COLLECTION_CHOICES_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            // Make sure all of the referenced choices are deleted from the
            // database.
            for (int i = 0; i < choices.length; i++) {
                deleteChoice(choices[i], conn);
            }

            pstmt = conn.prepareStatement(DELETE_CHOICE_COLLECTION_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to delete the " +
                    "given Choice Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            closeStatement(pstmt);
        }
    }

    protected boolean existsChoiceCollection(Handle h,
                                           Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(LOOKUP_CHOICE_COLLECTION_STATEMENT);
            pstmt.setString(1, h.getValue());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to check for the " +
                    "given Choice Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected boolean existsChoiceCollection(IChoiceCollection c,
                                           Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(EXISTS_CHOICE_COLLECTION_STATEMENT);
            pstmt.setLong(1, c.getId());
            pstmt.setString(2, c.getHandle().getValue());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to check for the " +
                    "given Choice Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected ISelection getSelection(long id, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ISelection rslt = null;
        IOption option = null;
        IComplement complement = null;

        try{
            pstmt = conn.prepareStatement(RETRIEVE_SELECTION_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                option = getOption(rs.getLong("optionid"), conn);
                complement = option.getComplementType()
                                   .fromByteArray(
                                           rs.getBytes("complement"));

                rslt = new SelectionImpl(id, this, option, complement);
            }
            rs.close();
            pstmt.close();

        } catch (EntityCreateException e) {
            String msg = "Error creating Selection object.";
            throw new RuntimeException(msg, e);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Selection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected void storeSelection(ISelection s, Connection conn) {
        IOption option = s.getOption();
        PreparedStatement pstmt = null;

        try{
            if (existsSelection(s, conn)) {
                // Our job is already done.
                return;
            }

            // Make sure all of the referenced option exists in the database.
            storeOption(option, conn);

            pstmt = conn.prepareStatement(STORE_SELECTION_STATEMENT);
            pstmt.setLong(1, s.getId());
            pstmt.setLong(2, option.getId());
            pstmt.setBytes(3, s.getComplement().toByteArray()); // TODO: Does this handle 'null'?
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Selection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected void deleteSelection(ISelection s, Connection conn) {
        IOption option = s.getOption();
        PreparedStatement pstmt = null;

        try{
            if (!existsSelection(s, conn)) {
                // Our job is already done.
                return;
            }

            pstmt = conn.prepareStatement(DELETE_SELECTION_STATEMENT);
            pstmt.setLong(1, s.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to delete the " +
                    "given Selection.";
            throw new RuntimeException(msg, se);
        } finally {
            closeStatement(pstmt);
        }
    }

    protected boolean existsSelection(ISelection s, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(EXISTS_SELECTION_STATEMENT);
            pstmt.setLong(1, s.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Selection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected IDecision getDecision(long id, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        IDecision rslt = null;
        IChoice option = null;
        List selections = new ArrayList();

        try{
            pstmt = conn.prepareStatement(RETRIEVE_DECISION_SELECTIONS_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                selections.add(getSelection(rs.getLong("selectionid"), conn));
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement(RETRIEVE_DECISION_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rslt = new DecisionImpl(
                        id,
                        this,
                        /* label */ null, // XXX: No getLabel() in IDecision
                        getChoice(rs.getLong("choiceid"), conn),
                        (ISelection[])selections.toArray(new ISelection[0]));
            }
            rs.close();
            pstmt.close();

        } catch (EntityCreateException e) {
            String msg = "Error creating Decision object.";
            throw new RuntimeException(msg, e);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Decision.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected void storeDecision(IDecision d, Connection conn) {
        ISelection[] selections = d.getSelections();
        PreparedStatement pstmt = null;

        try{
            if (existsDecision(d, conn)) {
                // Our job is already done.
                return;
            }

            // Make sure all of the referenced choices exist in the database.
            storeChoice(d.getChoice(), conn);
            for (int i = 0; i < selections.length; i++) {
                storeSelection(selections[i], conn);
            }

            pstmt = conn.prepareStatement(STORE_DECISION_STATEMENT);
            pstmt.setLong(1, d.getId());
            pstmt.setLong(2, d.getChoice().getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            pstmt = conn.prepareStatement(
                        STORE_DECISION_SELECTIONS_STATEMENT);
            pstmt.setLong(1, d.getId());
            for (int i = 0; i < selections.length; i++) {
                pstmt.setLong(2, selections[i].getId());
                pstmt.executeUpdate();
            }
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Decision.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected void deleteDecision(IDecision d, Connection conn) {
        ISelection[] selections = d.getSelections();
        PreparedStatement pstmt = null;

        try{
            if (!existsDecision(d, conn)) {
                // Our job is already done.
                return;
            }

            pstmt = conn.prepareStatement(
                        DELETE_DECISION_SELECTIONS_STATEMENT);
            pstmt.setLong(1, d.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            // Make sure all of the referenced selections are deleted from the
            // database.
            for (int i = 0; i < selections.length; i++) {
                deleteSelection(selections[i], conn);
            }

            pstmt = conn.prepareStatement(DELETE_DECISION_STATEMENT);
            pstmt.setLong(1, d.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to delete the " +
                    "given Decision.";
            throw new RuntimeException(msg, se);
        } finally {
            closeStatement(pstmt);
        }
    }

    protected boolean existsDecision(IDecision d, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(EXISTS_DECISION_STATEMENT);
            pstmt.setLong(1, d.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Decision.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected IDecisionCollection getDecisionCollection(long id, Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        IDecisionCollection rslt = null;
        List decisions = new ArrayList();

        try{
            pstmt = conn.prepareStatement(RETRIEVE_DECISION_COLLECTION_DECISIONS_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                decisions.add(getDecision(rs.getLong("decisionid"), conn));
            }
            rs.close();
            pstmt.close();

            pstmt = conn.prepareStatement(RETRIEVE_DECISION_COLLECTION_STATEMENT);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                rslt = new DecisionCollectionImpl(
                        id,
                        this,
                        getChoiceCollection(rs.getLong("collectionid"), conn),
                        (IDecision[])decisions.toArray(new IDecision[0]));
            }
            rs.close();
            pstmt.close();

        } catch (EntityCreateException e) {
            String msg = "Error creating Decision object.";
            throw new RuntimeException(msg, e);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to retrieve the " +
                    "requested Decision Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected void storeDecisionCollection(IDecisionCollection d, Connection conn) {
        IDecision[] decisions = d.getDecisions();
        PreparedStatement pstmt = null;

        try{
            if (existsDecisionCollection(d, conn)) {
                // Our job is already done.
                return;
            }

            // Make sure all of the referenced choices exist in the database.
            storeChoiceCollection(d.getChoiceCollection(), conn);
            for (int i = 0; i < decisions.length; i++) {
                storeDecision(decisions[i], conn);
            }

            pstmt = conn.prepareStatement(STORE_DECISION_COLLECTION_STATEMENT);
            pstmt.setLong(1, d.getId());
            pstmt.setLong(2, d.getChoiceCollection().getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            pstmt = conn.prepareStatement(
                        STORE_DECISION_COLLECTION_DECISIONS_STATEMENT);
            pstmt.setLong(1, d.getId());
            for (int i = 0; i < decisions.length; i++) {
                pstmt.setLong(2, decisions[i].getId());
                pstmt.executeUpdate();
            }
            closeStatement(pstmt);
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Decision Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected void deleteDecisionCollection(IDecisionCollection d, Connection conn) {
        IDecision[] decisions = d.getDecisions();
        PreparedStatement pstmt = null;

        try{
            if (!existsDecisionCollection(d, conn)) {
                // Our job is already done.
                return;
            }

            pstmt = conn.prepareStatement(
                        DELETE_DECISION_COLLECTION_DECISIONS_STATEMENT);
            pstmt.setLong(1, d.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

            // Make sure all of the referenced decisions are removed from the database.
            for (int i = 0; i < decisions.length; i++) {
                deleteDecision(decisions[i], conn);
            }

            pstmt = conn.prepareStatement(DELETE_DECISION_COLLECTION_STATEMENT);
            pstmt.setLong(1, d.getId());
            pstmt.executeUpdate();
            closeStatement(pstmt);

        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to delete the " +
                    "given Decision Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }
    }

    protected boolean existsDecisionCollection(IDecisionCollection dc,
                                             Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(EXISTS_DECISION_COLLECTION_STATEMENT);
            pstmt.setLong(1, dc.getId());
            rs = pstmt.executeQuery();
            if (rs.next())
                rslt = true;
            rs.close();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to store the " +
                    "given Decision Collection.";
            throw new RuntimeException(msg, se);
        } finally {
            if (pstmt != null) closeStatement(pstmt);
        }

        return rslt;
    }

    protected ISequencer sequencer() {
        return seq;
    }

    /*
     * Private API.
     */

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch(SQLException se) {
            String msg = "RdbmsEntityStore was not able to get a connection.";
            throw new RuntimeException(msg, se);
        }
    }

    private static void closeStatement(Statement stmt) {

        if (stmt == null) {
            return;
        }

        try {
            stmt.close();
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }

    }

    private static void closeConnection(Connection conn) {

        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }
    }

}
