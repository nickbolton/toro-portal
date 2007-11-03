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

package net.unicon.mercury.fac.rdbms;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.unicon.alchemist.MimeTypeMap;
import net.unicon.alchemist.rdbms.QueryManager;
import net.unicon.alchemist.rdbms.Sequencer;
import net.unicon.mercury.Features;
import net.unicon.mercury.FolderNotFoundException;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;
import net.unicon.mercury.SpecialFolder;
import net.unicon.mercury.fac.AbstractFolder;
import net.unicon.mercury.fac.AbstractMessage;
import net.unicon.mercury.fac.AbstractMessageFactory;
import net.unicon.mercury.fac.AddressImpl;
import net.unicon.mercury.fac.AttachmentImpl;
import net.unicon.mercury.fac.BaseAbstractMessage;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeDate;
import net.unicon.penelope.complement.TypeText64;
import net.unicon.penelope.store.jvm.JvmEntityStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RdbmsMessageFactory extends AbstractMessageFactory {
    
    // logger
    private static final Log log =
        LogFactory.getLog(RdbmsMessageFactory.class);

    /*
     * Static Members
     */

    private static DataSource defaultDataSource;
    private static QueryManager qm;
    
    // TODO want to add a caching function per fac for messages 
    //private Map cache = new HashMap();
    
    // TODO Contains the preference options for message factory
    // private static IChoiceCollection preferenceChoices;
    
    // Contains the search criteria choices for this message factory
    private static IChoiceCollection searchCriteria;
    
    // used to check if the class was bootstrapped
    private static boolean bootstrapped = false;
    
    /*
     * Instance Members
     */

    private final DataSource dataSource;
    private final String url;
    private final int layers;
    private final int expiration; 
    private final IAddress owner;    
    private final Features features;
    private RdbmsMessageFolder root;
    private RdbmsMessageFolder outbox;
    private RdbmsMessageFolder save;
    private final String attachPath;
    
    public static final String UNREAD = "T";
    public static final String READ = "F";

    private static Sequencer msgSequencer;
    
    private static Pattern constraintViolationMessagePattern =
        Pattern.compile("([Vv]iolat.*constraint|constraint.*violat)");
	
    /*
     * Public API.
     */
    public synchronized static void bootstrap(String queryFile) {
       if (qm != null)
          return;
       if (queryFile == null) {
          throw new IllegalArgumentException("Arguement 'queryFile' cannot by null");
       }
       qm = new QueryManager(queryFile, RdbmsMessageFactory.class.getName());
    }

    /**
     * Initializes the factory implementation.  <code>RdbmsMessageFactory</code>
     * must be bootstrapped prior to use.  Calling this method more than once is
     * a no-op.
     *
     * @param ds The (default) data source to use in connection with the
     * <code>fromUrl</code> method.
     */
    public synchronized static void bootstrap(DataSource ds) {

        if (bootstrapped)
            return;

        // Assertions
        if (ds == null) {
            String msg = "Argument 'ds [DataSource]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Set the default data source.
        defaultDataSource = ds;
        
        // Prepare the preferences.
        // preferenceChoices = initPreferences();

        IEntityStore store = new JvmEntityStore();
        try {

            // Create options
            IOption oContains = store.createOption(Handle.create("contains"),
                                                   null, TypeText64.INSTANCE);

            IOption oBeforeDate =
                    store.createOption(Handle.create("beforeDate"), null,
                                       TypeDate.INSTANCE);

            IOption oAfterDate = store.createOption(Handle.create("afterDate"),
                                                    null, TypeDate.INSTANCE);

            // Sender
            IChoice cSender = store.createChoice(Handle.create("sender"),
                                                 Label.create("Sender"),
                                                 new IOption[] { oContains },
                                                 0, 1);

            // Recipient
            IChoice cRcpt = store.createChoice(Handle.create("recipient"),
                                               Label.create("Recipient"),
                                               new IOption[] { oContains },
                                               0, 1);

            // Subject
            IChoice cSubject = store.createChoice(Handle.create("subject"),
                                                  Label.create("Subject"),
                                                  new IOption[] { oContains },
                                                  0, 1);

            // Body
            IChoice cBody = store.createChoice(Handle.create("body"),
                                               Label.create("Body"),
                                               new IOption[] { oContains },
                                               0, 1);

            // Date Sent
            IChoice cDateSent = store.createChoice(Handle.create("dateSent"),
                                                   Label.create("Date Sent"),
                                                   new IOption[] { oBeforeDate,
                                                                   oAfterDate },
                                                   0, 1);

            // Search criteria choice collection.
            IChoice[] choices = new IChoice[] {cSender, cRcpt, cSubject, cBody,
                                                                cDateSent};
            searchCriteria = store.createChoiceCollection(
                                        Handle.create("searchCriteria"),
                                        Label.create("Search"),
                                        choices);

        } catch (Throwable t) {
            String msg = "Unable to initialize search criteria for "
                                        + "RdbmsMessageFactory.";
            throw new RuntimeException(msg, t);
        }
        
        bootstrapped = true;
        
    }

    /**
     * Provides the message factory instance described in the <code>url</code>.
     *
     * NOTE: This method is not used by the MessagingPortlet, because of the
     * noted problems surrounding non-static configuration options.
     *
     * @param url A string (url) that uniquely identifies a message factory.
     * @return The message factory described by the specified url.
     */
    public static IMessageFactory fromUrl(String url) {

        // Assertions.
        if(url == null || url.trim().equals("")){
            String msg = "Argument 'url' cannot be null or empty";
            throw new IllegalArgumentException(msg);
        }
        
        assert bootstrapped == true : "The RdbmsMessageFactory needs to be bootstrapped. " +
				"Call the bootStrap() method before using it.";
        
        // check in the cache if the factory already exists
        RdbmsMessageFactory fac = (RdbmsMessageFactory)factoryMap.get(url);

        if(fac == null){
            String domainOwner = url.substring(url.lastIndexOf("[") + 1, url.lastIndexOf("]"));
            // TODO: FIXME: XXX: The URL must include attachPath and layers...
            // but it shouldn't, as that means the messages are forever linked
            // with a specific attachments directory. The
            // RdbmsMessageFactoryCreator should probably always be creating
            // our instances, to allow for configuration file change
            // propogation. If not, then simply add support for them here.
            // NOTE: This method is NOT USED by MessagingPortlet, see RdbmsMessageFactoryCreator.
            fac = new RdbmsMessageFactory(defaultDataSource, "/tmp", 3, domainOwner, 14);
            
            // add the factory to the cache
            factoryMap.put(fac.getUrl(), fac);
        }
        return fac;
        

    }

    /**
     * Creates a new message factory using the default data source.
     */
    public RdbmsMessageFactory(String attachPath, int layers, String domainOwner, int expiration) {
        this(defaultDataSource, attachPath, layers, domainOwner, expiration);
    }

    /**
     * Provides a specialized URL identifier for the factory instance.
     * Refer to {@link #fromUrl(String url)} for URL structure.
     *
     * @return  String URL identifier for the factory.
     * @see     net.unicon.mercury.IMessageFactory#getUrl()
     */
    public String getUrl() {

        return url;
    }

    /**
     * Obtains the features supported by this message factory.
     * Features include things such as subfolder and attachment support.
     *
     * @return  features supported by this message factory.
     * @see net.unicon.IMessageFactory#getFeatures
     */
    public Features getFeatures() {
        return this.features;
    }
    
    /**
     * @return Returns the domainOwner.
     */
    public String getDomainOwner() {
        return owner.toNativeFormat();
    }

    /**
     * Obtains the user's preferences.
     *
     * @return User preferences.
     */
    public IDecisionCollection[] getPreferences() {
        // ToDo:  Implement!
        return new IDecisionCollection[0];
    }

    /**
     * Returns the types of recipients available to this factory.
     *
     * @return  an array of the recipient types.
     */
    public IRecipientType[] getRecipientTypes() {
        return RdbmsRecipientType.ALL_TYPES;
    }

    /**
     * @see net.unicon.mercury.IMessageFactory#getSearchCriteria()
     **/
    public IChoiceCollection getSearchCriteria() {

        return searchCriteria;
    }

    /**
     * Returns a reference to a root message folder. The root folder contains
     * all messages and subfolders for this factory.
     *
     * @return  root message folder.
     * @see net.unicon.mercury.IMessageFactory#getRoot()
     */
    public IFolder getRoot() throws MercuryException {
        
        if (this.root == null) {
            initializeFolders();
        }
        return this.root;
    }
    
    private RdbmsMessageFolder getOutbox() throws MercuryException {
    	if (this.outbox == null) {
    		initializeFolders();
    	}
    	return this.outbox;
    }
    
    private RdbmsMessageFolder getSave() throws MercuryException {
    	if (this.save == null) {
    		initializeFolders();
    	}
    	return this.save;
    }
    
    private void initializeFolders() {
        initializeFolders(true);
    }
    
    private void initializeFolders(boolean retry) {
        // create the root(INBOX)/sent/archived folder if it does not exist
        Connection conn = null;
        ConnState connst = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long folderId;
        
        try {
            conn = dataSource.getConnection();
            connst = beginTransaction(conn);
 
            // check if the root folder already exists
            pstmt = conn.prepareStatement(qm.getQuery("GET_FOLDERS"));
            pstmt.setString(1, getDomainOwner());
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                folderId = rs.getLong("folder_id");
	             if (folderId == SpecialFolder.INBOX_VALUE) {
	                this.root = (RdbmsMessageFolder)createFolder(this,
	                      folderId, rs.getString("folder_label"));
                } else if (folderId == SpecialFolder.OUTBOX_VALUE) {
                   this.outbox = (RdbmsMessageFolder)createFolder(this,
                         folderId, rs.getString("folder_label"));
                } else if (folderId == SpecialFolder.SAVE_VALUE) {
                   this.save = (RdbmsMessageFolder)createFolder(this,
                         folderId, rs.getString("folder_label"));
                }
            }
            closeResultSet(rs); rs = null;
            closeStatement(pstmt); pstmt = null;
            
            if (this.root == null) {
                // Create root folder
                this.root = (RdbmsMessageFolder)createFolder(this, SpecialFolder.INBOX_VALUE,
                        SpecialFolder.INBOX.getLabel());              

                pstmt = conn.prepareStatement(qm.getQuery("CREATE_FOLDER"));
                pstmt.setInt(1, (int)root.getId());
                pstmt.setString(2, getDomainOwner());
                pstmt.setNull(3, Types.BIGINT); // Parent_id of root is null            
                pstmt.setString(4, root.getLabel());
                pstmt.executeUpdate();
            }
            closeStatement(pstmt); pstmt = null;

            if (this.outbox == null) {
                // create sent folder
                this.outbox = (RdbmsMessageFolder)createSubfolder(conn,
                    this.root, SpecialFolder.OUTBOX_VALUE, SpecialFolder.OUTBOX.getLabel());
            }
            
            if (this.save == null) {
                // create archive folder
                this.save = (RdbmsMessageFolder)createSubfolder(conn,
                    this.root, SpecialFolder.SAVE_VALUE, SpecialFolder.SAVE.getLabel());
            }

            conn.commit();

        } catch (SQLException sqle) {
           rollBack(conn);
           if (retry && constraintViolation(sqle.getMessage())) {
              initializeFolders(false);
           } else {
              String msg = "RdbmsMessageFolder was not able to create the root folder";
              throw new RuntimeException(msg, sqle);
           }
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupTransactionConnection(conn, connst);
        }

    }
    
    protected IMessage createMessage(
    		IMessageFactory owner, String msgId, 
            String subject, IAddress sender, IRecipient[] recipients,
            Date dateSent, String body,
            Priority priority, boolean read) {
        return new MessageImpl(owner, msgId, subject, sender, recipients,
                dateSent, body, priority, read);
    }
    
    protected final IMessage createMessage(
    		IMessageFactory owner, String msgId, 
            String subject, IAddress sender, 
            Date dateSent, String body,
            Priority priority, boolean read) {
    	return createMessage(owner, msgId, subject, sender,
    			null, dateSent, body, priority, read);
    }
    
    protected IMessage createSystemMessage(IMessageFactory owner
			, String id
            , IAddress sender
            , String subject
            , Date date
            , String body
            , Priority priority){
        return new SystemMessageImpl(owner, id, sender
                , subject, date, body, priority);
    }
    
    protected IFolder createFolder(IMessageFactory owner, long id, String label){
        return new RdbmsMessageFolder(owner, id, label);
    }
    
    protected IFolder createSystemFolder(IMessageFactory owner, long id, String label){
        return new SystemFolder(owner, id, label);
    }
    
    /**
     * Creates a message and sends it to all of the provided recipients.
     * The message is returned to the caller.
     *
     * @param conn         connection to the data source.
     * @param recipients   users that the message will be sent to. Must not
     *                     contain any null elements.
     * @param subject      message subject
     * @param body         message content
     * @param attachments  attachments to the message. May be null.
     * @return             the message that is created and sent.
     * @throws MercuryException
     * @see net.unicon.mercury.IMessageFactory#sendMessage(IRecipient[],
     *                                         String, String, IAttachment[])
     */
    public IMessage sendMessage(IRecipient[] recipients, String subject,
                    String body, IAttachment[] attachments, Priority priority)
    throws MercuryException {

        // Assertions
        if (recipients == null) {
            throw new IllegalArgumentException("Argument 'recipients' cannot be null.");
        }
        if (subject == null) {
            throw new IllegalArgumentException("Argument 'subject' cannot be null.");
        }
        if (body == null) {
            throw new IllegalArgumentException("Argument 'body' cannot be null.");
        }
        // attachments can be null.

        IMessage rslt = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ConnState connst = null;
        Set sentSet = new HashSet();
        
        try {
            Calendar cal = Calendar.getInstance();

            rslt = createMessage(this,
                            String.valueOf(getMessageSequencer().next()),
                            subject,
                            this.owner,
                            recipients,
                            cal.getTime(),
                            body,
                            priority,
                            false);

            if (attachments != null && attachments.length > 0) {
                sendAttachments(attachments, rslt.getId());
            }

            // Setup connection
            conn = dataSource.getConnection();
            connst = beginTransaction(conn);

            // Add message
            pstmt = conn.prepareStatement(qm.getQuery("SEND_MESSAGE"));

            // Set all parameters
            pstmt.setLong(1, Long.parseLong(rslt.getId()));
            pstmt.setString(2, rslt.getSender().toNativeFormat());
            pstmt.setString(3, rslt.getSubject());
            pstmt.setTimestamp(4, new Timestamp(rslt.getDate().getTime()));
            pstmt.setString(5, rslt.getBody());
            pstmt.setInt(6, priority.toInt()); 
            if(expiration > 0){
                cal.add(Calendar.DATE, expiration);
                pstmt.setTimestamp(7, new Timestamp(cal.getTime().getTime()));
            }else{
                pstmt.setTimestamp(7, null);
            }

            pstmt.executeUpdate(); // Message added, not committed

            // Clear resources for reuse
            closeStatement(pstmt); pstmt = null;
            
            // For sender, and each recipient, add a "copy" of the
            // message into dispatch table
            
            pstmt = conn.prepareStatement(qm.getQuery("DISPATCH_MESSAGE"));

            // Set parameters for sender
            pstmt.setLong(1, Long.parseLong(rslt.getId()));
            pstmt.setString(2, rslt.getSender().toNativeFormat());
            pstmt.setInt(3, RdbmsDispatchType.SENDER.toInt());
            pstmt.setNull(4, Types.INTEGER); // Recipient type has no meaning
            pstmt.setString(5, UNREAD); // Unread to start
            pstmt.setInt(6, (int)SpecialFolder.OUTBOX_VALUE); // Place it in sender's outbox
            
            pstmt.executeUpdate();  // Sender added, not committed

            // Then create each recipients copy
            pstmt.setLong(1, Long.parseLong(rslt.getId()));
            pstmt.setInt(3, RdbmsDispatchType.RECEIVER.toInt());
            pstmt.setString(5, UNREAD); // Unread to start
            pstmt.setInt(6, (int)SpecialFolder.INBOX_VALUE);
            for (int i = 0; i < recipients.length; i++) {

                // Assertions
                if (recipients[i] == null) {
                    String msg = "Argument 'recipients [IRecipient[]]' " +
                    "cannot contain null elements.";
                    throw new IllegalArgumentException(msg);
                }
                
                if(!sentSet.add(recipients[i])){
                    continue;
                }
                
                IRecipientType rType = recipients[i].getType();
                if (!(rType instanceof RdbmsRecipientType)) {
                    rType = RdbmsRecipientType.getType(rType.getLabel());
                }
                pstmt.setString(2, recipients[i].getAddress().toNativeFormat());
                pstmt.setInt(4, ((RdbmsRecipientType)rType).toInt());
                pstmt.executeUpdate();  // Recipient added, not committed
            }

            // Finally commit transaction
            conn.commit();

        } catch (Exception t) {
            rollBack(conn); // Message not added, nor any recipients if any fail
            String msg = "RdbmsMessageFactory was not able to send message: "+t.getMessage();
            log.error(msg, t);
            throw new MercuryException(msg, t);
        } finally {
            closeStatement(pstmt); pstmt = null;
            cleanupTransactionConnection(conn, connst); conn = null;
        }

        return rslt;
    }

    private Sequencer getMessageSequencer() {
        if(msgSequencer == null){
            msgSequencer = new Sequencer(dataSource, "RdbmsMessageSequencer"
                    , 100);
        }
        return msgSequencer;
    }
    
    /**
     * Returns the initial portal login date for the owner of this factory.
     *  
     * @return the date representing the initial portal login of the owner, or 
     *         null if the initial login date cannot be obtained.
     */
    protected Date getInitialLoginDate() {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        Timestamp rslt = null;
            
        try {
            conn = dataSource.getConnection();
            
            pstmt = conn.prepareStatement(qm.getQuery("GET_INITIAL_USAGE"));
            pstmt.setString(1, getDomainOwner());

            rs = pstmt.executeQuery();
            
            if(rs.next()){
                rslt = rs.getTimestamp("initial_usage_date");
            } else {
                rslt = new Timestamp(System.currentTimeMillis());
                insertInitialUsageDate(getDomainOwner(), rslt, conn);
            }
        } 
        catch (SQLException se) {
            String msg = "RdbmsMessageFactory was not able to retrieve the " +
            "initial login date for user: " + getDomainOwner();
            log.error(msg, se);
        } 
        finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupConnection(conn);
        }

        return rslt;
    }
    
    private void insertInitialUsageDate(String username, Timestamp ts, Connection conn)
    throws SQLException {
        PreparedStatement pstmt = null;
        
        try {
            int i=1;
            pstmt = conn.prepareStatement(qm.getQuery("SET_INITIAL_USAGE"));
            pstmt.setString(i++, username);
            pstmt.setTimestamp(i++, ts);
            pstmt.execute();
        } finally {
            closeStatement(pstmt);
        }
    }
    
    /**
     * @see IMessageFactory#getMessage(String)
     */
    public IMessage getMessage(String msgId) throws MercuryException {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        IMessage rslt = null;
        	
        try {
            conn = dataSource.getConnection();
            
            pstmt = conn.prepareStatement(qm.getQuery("GET_MESSAGE"));
            pstmt.setString(1, msgId);
            pstmt.setString(2, getDomainOwner());

            rs = pstmt.executeQuery();
            
            if(rs.next()){
                
                IAddress sender = new AddressImpl(rs.getString("sender"), rs.getString("sender"));                
                String subject = rs.getString("subject");
                String body = rs.getString("body");
                Date sent = rs.getTimestamp("date_sent");
                boolean read = rs.getString("unread").equals(READ);
                int priority = rs.getInt("priority");
                
                rslt = createMessage(this, msgId, 
                        subject, sender, 
                        sent, body,
                        Priority.getInstance(priority),
                        read);
            } 

        } catch (SQLException se) {
            String msg = "RdbmsMessageFolder was not able to retrieve its " +
            "messages";
            throw new RuntimeException(msg, se);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupConnection(conn);
        }

        assert rslt == null : "RdbmsMessageFactory : getMessage(msgId) did not find any message for a given msgId";
        
        return rslt;
    }
    
    /* Protected API
      */
    
    private IMessage[] getMessages(AbstractFolder folder) throws MercuryException {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Set messages = new HashSet();
        Connection conn = null;
        
        try {
            conn = dataSource.getConnection();

            pstmt = conn.prepareStatement(qm.getQuery("GET_MESSAGES"));
            int i = 1;
            pstmt.setInt(i++, (int)folder.getId());
            pstmt.setString(i++, getDomainOwner());
            pstmt.setInt(i++, (int)SpecialFolder.SAVE_VALUE);
            
            rs = pstmt.executeQuery();
            
            IAddress sender;
            String msgId;
            String subject;
            String body;
            Date sent;
            boolean read;
            IAttachment[] attachments;
            int priority;
            int dispatchId;
                
            while (rs.next()) {	                
                    sender = new AddressImpl(rs.getString("sender"), rs.getString("sender"));
                    msgId = rs.getString("msg_id");
                    subject = rs.getString("subject");
                    body = rs.getString("body");
                    sent = rs.getTimestamp("date_sent");
                    read = rs.getString("unread").equals(READ);
                    priority = rs.getInt("priority");
                    
                    // Time to create a new message and prep the rest of
                    // the data
                   messages.add(createMessage(this, msgId, 
                            subject, sender, 
                            sent, body,
                            Priority.getInstance(priority), 
                            read));
            }

        } catch (SQLException se) {
            String msg = "RdbmsMessageFolder was not able to retrieve its " +
            "messages";
            throw new RuntimeException(msg, se);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupConnection(conn);
        }

        return (IMessage[])messages.toArray(new IMessage[messages.size()]);
    }
    
    /* (non-Javadoc)
     * @see net.unicon.mercury.IMessageFactory#getFolder(java.lang.String)
     */
    public IFolder getFolder(String id) throws FolderNotFoundException, MercuryException {
        PreparedStatement pstmt = null;
        Connection conn = null;
        ResultSet rs = null;
        
        try{
           conn = dataSource.getConnection();
	        pstmt = conn.prepareStatement(qm.getQuery("GET_FOLDER"));
	        pstmt.setString(1, this.owner.toNativeFormat());
	        pstmt.setInt(2, (int)Long.parseLong(id)); 				
	        rs = pstmt.executeQuery();
	        
	        if(rs.next()){
	            return createFolder(this, rs.getLong("folder_id"), rs.getString("folder_label"));
	        }
        }catch(SQLException e){
            throw new RuntimeException("RdbmsMessageFactory: Problem getting the folder with id = " + id, e);
        }finally{
            closeResultSet(rs); rs = null;
            closeStatement(pstmt); pstmt = null;
            cleanupConnection(conn); conn = null;
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.unicon.mercury.IMessageFactory#getSpecialFolder(java.lang.String)
     */
    public IFolder getSpecialFolder(SpecialFolder sFolder) {
    	Connection conn = null;
    	IFolder rslt = null;
    	
    	try {
    		conn = dataSource.getConnection();
    		
    		rslt = getSpecialFolder(conn, sFolder);
    	} catch (SQLException ex) {
            throw new RuntimeException("RdbmsMessageFactory: Problem in getting the folder w" +
            		"ith label = " + sFolder.getLabel(), ex);
		} finally {
    		cleanupConnection(conn);
    	}
    	
    	return rslt;
    }
    
    private IFolder getSpecialFolder(Connection conn, SpecialFolder sFolder) {
        
        if(sFolder.equals(SpecialFolder.SYSFOLDER)){
            return createSystemFolder(this, sFolder.toLong(), sFolder.getLabel());
        }
        
        IFolder rslt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
	        pstmt = conn.prepareStatement(qm.getQuery("GET_FOLDER"));
	        pstmt.setString(1, this.owner.toNativeFormat());
	        pstmt.setInt(2, (int)sFolder.toLong());
	        rs = pstmt.executeQuery();
	        
	        if(rs.next()){
	            rslt = createFolder(this, rs.getLong("folder_id")
	                    , rs.getString("folder_label"));
	        }
        }catch(SQLException e){
            throw new RuntimeException("RdbmsMessageFactory: Problem in getting the folder w" +
            		"ith label = " + sFolder.getLabel(), e);
        } finally{
            closeResultSet(rs); rs = null;
            closeStatement(pstmt); pstmt = null;
        }

        return rslt;
    }

    public void move(IMessage msg, IFolder fromFolder, IFolder toFolder) throws MercuryException{
        // Assertions
		assert msg != null : "Argument 'msg' cannot be null.";

      if (fromFolder.getIdString().equals(toFolder.getIdString()))
         return;
		
		PreparedStatement pstmt = null;
		Connection conn = null;
      
		try {
			conn = dataSource.getConnection();

			pstmt = conn.prepareStatement(qm.getQuery("MOVE_MESSAGE_TO_FROM_FOLDER"));
			pstmt.setInt(1, (int)((RdbmsMessageFolder)toFolder).getId());
			pstmt.setInt(2, ~((int)((RdbmsMessageFolder)fromFolder).getId()));
			pstmt.setLong(3, Long.parseLong(msg.getId()));
			pstmt.setString(4, this.owner.toNativeFormat());
			pstmt.executeUpdate();
		} catch (SQLException se) {
			String m = "RdbmsMessageFactory was not able to move a message.";
			throw new RuntimeException(m, se);
		} finally {
		   closeStatement(pstmt);
		   cleanupConnection(conn);
		} 
    }
    
    private IMessage[] getSystemMessages() throws MercuryException{
        List rslt = new ArrayList();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try{
            
            conn = dataSource.getConnection();
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qm.getQuery("GET_ALL_MESSAGES"));
            
            while(rs.next()){
                rslt.add(createSystemMessage(this
                        , rs.getString("msg_id")
                        , new AddressImpl(rs.getString("sender"), rs.getString("sender"))
                        , rs.getString("subject")
                        , rs.getTimestamp("date_sent")
                        , rs.getString("body")
                        , Priority.getInstance(rs.getInt("priority"))));
            }
            
        }catch(Exception e){
            String m = "RdbmsMessageFactory was not able to get all the messages.";
			throw new RuntimeException(m, e);
        }finally{
            closeResultSet(rs);
            closeStatement(stmt);
            cleanupConnection(conn);
        }
        
        return (IMessage[])rslt.toArray(new IMessage[0]);
    }
    
    private IMessage getSystemMessage(String msgId) throws MercuryException{
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        IMessage rslt = null;
        
        try{
            
            conn = dataSource.getConnection();
            
            stmt = conn.prepareStatement(qm.getQuery("GET_SYSTEM_MESSAGE"));
            
            stmt.setString(1, msgId);
            rs = stmt.executeQuery();
            
            if(rs.next()){
                rslt = createSystemMessage(this
                        , rs.getString("msg_id")
                        , new AddressImpl(rs.getString("sender"), rs.getString("sender"))
                        , rs.getString("subject")
                        , rs.getTimestamp("date_sent")
                        , rs.getString("body")
                        , Priority.getInstance(rs.getInt("priority")));
            }
            
        }catch(Exception e){
            String m = "RdbmsMessageFactory was not able to get all the messages.";
			throw new RuntimeException(m, e);
        }finally{
            closeResultSet(rs);
            closeStatement(stmt);
            cleanupConnection(conn);
        }
        
        return rslt;
    }
     
    /*
     * Private API
     */
    
    private IAttachment[] getAttachments(String msgId) {
        
        List rslt = new ArrayList();

        try {
            File folder = getAttachFolder(msgId, false);

            // Only bother if the attachment folder exists.
            if (folder != null) {
                // get only the folders
                // files were not added by the system
                File[] subFolders = folder.listFiles(new FileFilter() {
                        public boolean accept(File path) { return path.isDirectory(); }
                    });

                for(int i = 0; i < subFolders.length; i++) {
                    // The first file is our target.
                    File[] files = subFolders[i].listFiles(new FileFilter() {
                            public boolean accept(File path) { return path.isFile(); }
                        });
                    if (files.length > 0)
                        rslt.add(new RdbmsAttachmentImpl(i, files[0], msgId));
                }
            }
        } catch (MercuryException e) {
            throw new RuntimeException("Error creating attachment", e);
        }  
        return (IAttachment[])rslt.toArray(new IAttachment[rslt.size()]);
    }
    
    private IRecipient[] getRecipients(String msgId){
        
        List rslt = new ArrayList();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try{
            
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(qm.getQuery("GET_RECIPIENTS"));
            
            pstmt.setString(1, msgId);
            pstmt.setInt(2, RdbmsDispatchType.RECEIVER.toInt());
            
            rs = pstmt.executeQuery();
            
            while(rs.next()){
                rslt.add(new RdbmsMessageRecipient(
                        new AddressImpl(rs.getString("dispatch_owner"), rs.getString("dispatch_owner"))
                        , RdbmsRecipientType.getType(rs.getInt("recipient_type"))
                        , rs.getString("unread").equals(READ)));
            }
            
        }catch(Exception e){
            String m = "RdbmsMessageFactory was not able to get the recipients for message " 
                + msgId;
			throw new RuntimeException(m, e);
        }finally{
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupConnection(conn);
        }
          
        return (IRecipient[])rslt.toArray(new IRecipient[rslt.size()]);
    }
    
    private IRecipient[] getRecipients(String msgId, IRecipientType[] types){
        
        if(types.length == 0){
            return getRecipients(msgId);
        }
        
        List rslt = new ArrayList();
        
        StringBuffer strb = new StringBuffer(qm.getQuery("GET_RECIPIENTS_BY_TYPE_PRE"));
        for(int i = 0; i < types.length; i++){
            strb.append('?');
            if(i < types.length - 1){
                strb.append(", ");
            }
        }
        strb.append(qm.getQuery("GET_RECIPIENTS_BY_TYPE_POST"));
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try{
            
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(strb.toString());
            
            int i = 1;
            stmt.setString(i++, msgId);
            stmt.setInt(i++, RdbmsDispatchType.RECEIVER.toInt());
            for (int k = 0; k < types.length; k++) {
               stmt.setInt(i++, ((RdbmsRecipientType)types[i]).toInt());
            }
            rs = stmt.executeQuery();
            
            while(rs.next()){
                rslt.add(new RdbmsMessageRecipient(
                        new AddressImpl(rs.getString("dispatch_owner"), rs.getString("dispatch_owner"))
                        , RdbmsRecipientType.getType(rs.getInt("recipient_type"))
                        , rs.getString("unread").equals(READ)));
            }
            
        }catch(Exception e){
            String m = "RdbmsMessageFactory was not able to get the recipients for message " 
                + msgId;
            throw new RuntimeException(m, e);
        }finally{
            closeResultSet(rs);
            closeStatement(stmt);
            cleanupConnection(conn);
        }
          
        return (IRecipient[])rslt.toArray(new IRecipient[rslt.size()]);
    }
    
    /**
     * Instantiates a factory with a provided identifier.
     *
     * @param id
     *     identifier for the factory. Cannot be negative.
     */
    public RdbmsMessageFactory(DataSource ds, String attachPath,
                               int layers, String domainOwner, int expires) {

        // Assertions.
        if (ds == null){
            throw new IllegalArgumentException("Argument 'ds [DataSource]' cannot be null.");
        }            
        if (bootstrapped != true){
            throw new IllegalArgumentException("The RdbmsMessageFactory needs to be bootstrapped. " +
        							"Call the bootStrap() method before using it.");
        }
        
        // Instance Members.
        this.dataSource = ds;
        
        // check if the path exists, if not create the path
        File path = new File(attachPath);
        if(!path.exists()){
            boolean success = path.mkdirs();
            if(!success){
                throw new IllegalArgumentException("MessageFactory : There was " +
                		" an error in creating the attachment path. " + attachPath);
            }
        }
        
        this.attachPath = attachPath;
        this.layers = layers;
        this.expiration = expires;

        this.owner = new AddressImpl(domainOwner, domainOwner);

        
        this.features = new Features(this,
                                Features.ATTACHMENTS |
                                Features.SUBFOLDERS |
                                Features.RECIPIENTDETAIL |
                                Features.SYSTEMVIEW);
        
        // initialize the url
        StringBuffer rslt = new StringBuffer();
        rslt.append("MSG://").append(RdbmsMessageFactory.class.getName())
                            .append("/").append("[")
                            .append(domainOwner).append("]");

        this.url = rslt.toString();        
        
    }

    /**
     * Initializes the preference choices for the message factory.
     *
     * @return  an <code>IChoiceCollection</code> containing the
     *          preference choices for this factory.
     * @throws  IllegalStateException if the preference choices cannot be
     *          established.
     */
/*
    private static IChoiceCollection initPreferences()
                                     throws IllegalStateException {

        // ToDo Use RdbmsEntityStore once it is completed...
//      IEntityStore store = new RdbmsEntityStore(dataSource);
        IEntityStore store = new JvmEntityStore();
        IChoiceCollection rslt = null;

        // Create the choice collection for the preference choices.
        try {

            // ToDo Finish Implementation - Create options/choices ...
            IOption opt1 = store.createOption(
                    Handle.create("option1?"),
                    Label.create("option1?"),
                    TypeText64.INSTANCE
                );

            IChoice c1 = store.createChoice(Handle.create("choice1?"),
                                            Label.create("choice1?"),
                                            new IOption[] { opt1 }, 0, 1);

            // Preferences Choice Collection...
            rslt = store.createChoiceCollection(
                   Handle.create("preferenceChoices"),
                   Label.create("Notification Preferences"),
                   new IChoice[] { c1 }
                );

        } catch (EntityCreateException ex) {
            String msg = "RdbmsMessageFactory failed to initialize properly.";
            throw new IllegalStateException (msg);
        }

        return rslt;

    }
*/

    private synchronized void addMessage(RdbmsMessageFolder folder, IMessage msg) 
    	throws MercuryException {
		// Assertions
		if (msg == null){
		    throw new IllegalArgumentException("Argument 'msg' cannot be null.");
		}
		
		PreparedStatement pstmt = null;
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(qm.getQuery("ADD_MESSAGE"));
			pstmt.setInt(1, (int)folder.getId());
			pstmt.setLong(2, Long.parseLong(msg.getId()));
			pstmt.setString(3, this.owner.toNativeFormat());
			pstmt.executeUpdate();
		} catch (SQLException se) {
			String m = "RdbmsMessageFolder was not able to add a message.";
			throw new RuntimeException(m, se);
		} finally {
		   closeStatement(pstmt);
		   cleanupConnection(conn);
		} 

    }
    
    private synchronized boolean removeMessage(RdbmsMessageFolder folder, IMessage msg)  
    				throws MercuryException {
		// Assertions
		if (msg == null){
		    throw new IllegalArgumentException("Argument 'msg' cannot be null.");
		}
		
		// remove the message from the dispatch table. 
		PreparedStatement pstmt = null;
		Connection conn = null;
		
		try{
		    conn = dataSource.getConnection();
		    pstmt = conn.prepareStatement(qm.getQuery("REMOVE_MESSAGE"));
		    pstmt.setInt(1, ~((int)folder.getId()));
		    pstmt.setLong(2, Long.parseLong(msg.getId()));
		    pstmt.setString(3, this.owner.toNativeFormat());
		    pstmt.executeUpdate();
		}catch(SQLException e){
		    throw new RuntimeException("RdbmsMessageFactory : Error " +
		    		"in deleting the message from hg_dispatch table.", e);
		}finally{
		   closeStatement(pstmt);
		   cleanupConnection(conn);
		}
		
		return true;
	}
    
    /**
     * 
     * @param conn
     * @param msg
     * @param seen
     * @throws MercuryException
     */
    private synchronized void setRead(IMessage msg, 
                                      boolean seen) throws MercuryException {
        
        // Assertions
        if (msg == null){
		    throw new IllegalArgumentException("Argument 'msg' cannot be null.");
		}

        PreparedStatement pstmt = null;
        Connection conn = null;
        ConnState connst = null;

        try {
            conn = dataSource.getConnection();
            connst = beginTransaction(conn);
            pstmt = conn.prepareStatement(qm.getQuery("SET_READ"));

            pstmt.setString(1, (seen ? READ : UNREAD));
            pstmt.setLong(2, Long.parseLong(msg.getId()));
            pstmt.setString(3, getDomainOwner());
            pstmt.setInt(4, RdbmsDispatchType.RECEIVER.toInt());

            pstmt.executeUpdate();
            conn.commit();
        
        } catch (SQLException se) {
            rollBack(conn);
            String m = "RdbmsMessageFolder was not able set read status of a" +
                       "message.";
            throw new RuntimeException(m, se);
        } finally {
            closeStatement(pstmt);
            cleanupTransactionConnection(conn, connst);
        }        
    }
    
    private void expunge(RdbmsMessageFolder folder) throws MercuryException {
       // This is now a no-op. Messages are deleted immediately from folders.
    }
    
    private IMessage[] search(IFolder folder, IDecisionCollection filters, 
            boolean recurse) throws MercuryException {
		// ToDo Complete Implementation
		throw new UnsupportedOperationException();
/*
        List rslt = new ArrayList();
        Connection conn = null;
        
        try{
            conn = dataSource.getConnection();
            rslt = search(conn, folder, filters, recurse);
        }catch(SQLException e){
            throw new RuntimeException("RdbmsMessageFactory was unable to get the DB connection.", e);
        }finally{
            cleanupConnection(conn);
        }
        
        return (IMessage[])rslt.toArray(new IMessage[rslt.size()]);
*/
    }   
    
/* Incomplete -- see above
    private List search(Connection conn, RdbmsMessageFolder folder, IDecisionCollection filters, 
            boolean recurse) throws MercuryException {

		// Assertions
		if (filters == null){
		    throw new IllegalArgumentException("Argument 'filters' cannot be null.");
		}
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List rslt = new ArrayList();
		
		try {
		
		    if (recurse) {
			    RdbmsMessageFolder[] subfolders = (RdbmsMessageFolder[])folder.getSubfolders(); 
			    for (int i = 0; i < subfolders.length; i++) {                    
			        rslt.addAll(search(conn, subfolders[i], filters, recurse));                    
			    }
			}

			final String qry = "SELECT  M.MSG_ID, M.SENDER, M.SUBJECT" +
					", M.DATE_SENT, M.BODY, M.ATTACHMENTS_URL, D.DISPATCH_ID " +
					"FROM HG_MESSAGE AS M, HG_DISPATCH AS D " +
					"WHERE M.MSG_ID=D.MSG_ID AND D.FOLDER_ID = ? ";
			
		// append search criteria            
			StringBuffer stmt = new StringBuffer(qry);
			
			IDecision[] decisions = filters.getDecisions();
			
			
			for (int i = 0; i < decisions.length; i++) { // Add search criteria
			    
			    
			    // ToDo Complete this...
			    
			}

			pstmt = conn.prepareStatement(stmt.toString());
			pstmt.setLong(1, folder.getId());
			
			// Need to get msg_id's for each message returned by the search,
			// then do one more query to obtain all recipients for each of those
			// messages
			
			rs = pstmt.executeQuery();
			
			MessageImpl tmpMsg = null;
			
			while (rs.next()) {
			    // ToDo Finish This
			//    tmpMsg = new RdbmsMessage(this.getOwner(),
			//                              rs.getLong("dispatch_id"),
			//                              rs.getLong("msg_id"),
			//                              rs.getString("subject"),
			//                              rs.getString("sender"),
			//                              
			//                              
			//                              );
			    
			    rslt.add(tmpMsg);
    
		}
		
		
		} catch (SQLException se) {
		
			String msg = "RdbmsMessageFolder was not able to be searched " +
			             "successfully";
			throw new RuntimeException(msg, se);
		
		} finally {
            closeResultSet(rs);
            closeStatement(pstmt);
		}

		return rslt;
		
	}
*/
    
    private IFolder createSubfolder(RdbmsMessageFolder folder, String label) {
    	Connection conn = null;
    	ConnState connst = null;
      PreparedStatement pstmt = null;
    	IFolder rslt = null;

    	try {
            conn = dataSource.getConnection();
            
            pstmt = conn.prepareStatement(qm.getQuery("CREATE_FOLDER_NEXTID"));
            pstmt.setInt(1, (int)folder.getId());
            pstmt.setString(2, label);
            pstmt.setString(3, this.owner.toNativeFormat());
            pstmt.executeUpdate();

        } catch (SQLException ex) {
           String msg = "RdbmsMessageFolder was not able to create a subfolder labeled: " + label;
           throw new RuntimeException(msg, ex);
        } finally {
           closeStatement(pstmt);
           cleanupConnection(conn);
        }
        return rslt;
    }
        
    private synchronized IFolder createSubfolder(Connection conn,
        RdbmsMessageFolder folder, long id, String label) throws SQLException {
        
        // Assertions
        if (label == null){
            throw new IllegalArgumentException("Argument 'name' cannot be null.");
        }
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RdbmsMessageFolder rslt = null;
        
        try {
            rslt = (RdbmsMessageFolder)createFolder(this, id, label);

            pstmt = conn.prepareStatement(qm.getQuery("CREATE_FOLDER"));
            pstmt.setInt(1, (int)rslt.getId());
            pstmt.setString(2, this.owner.toNativeFormat());
            pstmt.setInt(3, (int)folder.getId());
            pstmt.setString(4, rslt.getLabel());

            pstmt.executeUpdate();
        } finally {
            closeStatement(pstmt);
        }

        return rslt;         
    }
    
    private synchronized void deleteFolder(RdbmsMessageFolder folder,
            								boolean recurse) 
            								throws MercuryException {
		// ToDo Finish Implementation
		throw new UnsupportedOperationException();
		
/*
        // Assertions
		
		PreparedStatement pstmt = null;
		Connection conn = null;
        ConnState connst = null;
		
		try {
            conn = dataSource.getConnection();
            connst = beginTransaction(conn);
		
			if (recurse) { // Delete this and all subfolders, and all messages  
			
			    deleteAllMessages(conn, folder);
			
			    RdbmsMessageFolder[] subfolders = 
			        (RdbmsMessageFolder[])folder.getSubfolders();
			
				for (int i = 0; i < subfolders.length; i++) {
				    deleteFolder(subfolders[i], recurse);
				}
				
			} else { //Check that this folder is empty, if not then error
				//stmt.append("SELECT COUNT(folder_id)")
				//.append(" FROM hg_dispatch")
				//.append(" WHERE folder_id = ?");
			
			
			}
		
			//If folder is empty, then delete it.
			final String stmt = "";
			pstmt = conn.prepareStatement(stmt.toString());
			
			//pstmt.setLong(2, Long.parseLong(msg.getId()));
			
			//pstmt.execute();
		
            conn.commit();
		} catch (SQLException se) {
            rollBack(conn);
			String m = "RdbmsMessageFolder was not able to delete folder " +
			"with id: " + folder.getId();
			throw new RuntimeException(m, se);
		} finally {
            closeStatement(pstmt);
            cleanupTransactionConnection(conn, connst);
		}        
*/
	}

    /**
     * Physically deletes the message from the sender and recipients 
     * folders.
     * @param msgId
     */
    private synchronized boolean retractMessage(String msgId) {

		// Assertions
		if (msgId == null){
		    throw new IllegalArgumentException("Argument 'msgId' cannot be null.");
		}
		
		Connection conn = null;
        ConnState connst = null;
		PreparedStatement pstmt = null;
      
		try {
		   conn = dataSource.getConnection();
		   connst = beginTransaction(conn);
	        
			// delete all message recipient list
			pstmt = conn.prepareStatement(qm.getQuery("DELETE_DISPATCH_MESSAGE"));
			pstmt.setString(1, msgId);			
			pstmt.executeUpdate();
			closeStatement(pstmt); pstmt = null;

			// delete the message
			pstmt = conn.prepareStatement(qm.getQuery("DELETE_MESSAGE"));
			pstmt.setString(1, msgId);			
			pstmt.executeUpdate();
			closeStatement(pstmt); pstmt = null;
			
			conn.commit();
		} catch (SQLException se) {
            rollBack(conn);
			String m = "RdbmsMessageFolder was not able to delete the " +
			"message with id: " + msgId;
			throw new RuntimeException(m, se);
		} finally {
            closeStatement(pstmt);
            cleanupTransactionConnection(conn, connst);
		}
		
		return true;
	}    
    
    /**
     * Retrieve a list of all subfolders contained in the folder.
     * 
     * @param  conn  connection to the data source.
     * @return Array containing the subfolders of this folder. This returns 
     *         zero length array if no subfolders exist.
     */
    private IFolder[] getSubfolders(RdbmsMessageFolder folder) throws MercuryException {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        IFolder[] rslt = null;
        Connection conn = null;
        
        try {

            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(qm.getQuery("GET_SUBFOLDERS"));
            pstmt.setInt(1, (int)folder.getId());
            pstmt.setString(2, this.owner.toNativeFormat());

            rs = pstmt.executeQuery();
            
            // Extract subfolders
            ArrayList tmpFolders = new ArrayList();
            IFolder tmpFolder = null;
            
            while (rs.next()) {
                
                tmpFolder = createFolder(this,
                      rs.getLong("folder_id"),
                      rs.getString("folder_label"));
                tmpFolders.add(tmpFolder);
            }
            
            // Create subfolder array           
            rslt = (IFolder[])tmpFolders.toArray(
                                         new IFolder[tmpFolders.size()]);
        } catch (SQLException se) {
            String msg = "RdbmsMessageFolder was not able to retrieve its " +
                         "subfolders.";
            throw new RuntimeException(msg, se);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupConnection(conn);
        }

        return rslt;         
    }
    
    private int getUnreadCount(RdbmsMessageFolder folder) throws MercuryException {
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        int rslt = 0;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(qm.getQuery("GET_UNREAD_COUNT"));
            int i = 1;
            pstmt.setInt(i++, (int)folder.getId());
            pstmt.setString(i++, owner.toNativeFormat()); // dispatch_owner
            pstmt.setString(i++, UNREAD); // unread
            pstmt.setInt(i++, RdbmsDispatchType.RECEIVER.toInt());
            pstmt.setInt(i++, (int)SpecialFolder.SAVE_VALUE);
            rs = pstmt.executeQuery();

            while (rs.next()) {
               rslt += rs.getInt("count");
            }
           
        } catch (SQLException se) {
            String msg = "RdbmsMessageFolder was not able to retrieve its " +
                         "subfolders.";
            throw new RuntimeException(msg, se);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            cleanupConnection(conn);
        }
        
        return rslt; 
    }
    
    /**
     * Sends the attachments by storing them in the filesystem.
     *
     * @param  attachments  attachments to a message. Cannot be null.
     */
    private void sendAttachments(IAttachment[] attachments, String msgId) {

        // Assertions
        if (attachments == null){
            throw new IllegalArgumentException("Argument 'attachments' cannot be null.");
        }

        if (attachments.length == 0)
            return;

        File folder = getAttachFolder(msgId, true);
        File attachFolder = null;
        File file = null;

        try {
            // store the attachments in the given msgfolder
            for (int i = 0; i < attachments.length; i++) {
                attachFolder = new File(folder, String.valueOf(i));
                attachFolder.mkdir();

                file = new File(attachFolder, attachments[i].getName());
                FileOutputStream out = new FileOutputStream(file);
                copyStream(attachments[i].getInputStream(), out);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save the attachment in the RdbmsMessageFactory.", e);
        }
        
    }
    
    
    /**
     * This method generates a folder path to store the attachments based on the message id
     * for example : Consider the number of hashing layers as 3 
     * the attachment for message with id 123 will be stored in <root-directory>/1/2/3/123
     * the attachment for message with id 1 will be stored in <root-directory>/1/0/0/1
     * the attachment for message with id 12 will be stored in <root-directory>/1/2/0/12
     * the attachment for message with id 12345 will be stored in <root-directory>/1/2/3/12345
     * @param msgId The message identifier to retrieve attachments for
     * @param create Boolean affirmation that the folder should be created if it does not exist.
     * @return 
     */
    private File getAttachFolder(String msgId, boolean create) {
        
        //Assertions
        if (msgId.equals("")){
            throw new IllegalArgumentException("Argument 'msgId' can not be empty.");
        }

        String fpath = null;
        if (layers > 0) {
            String paddedId = msgId;
            if (paddedId.length() < layers)
                for (int i = paddedId.length(); i < layers; i++)
                    paddedId = paddedId+"0";

            fpath = ""+paddedId.charAt(0);
            for (int i = 1; i < layers; i++) {
                fpath = fpath+File.separator+paddedId.charAt(i);
            }
            fpath = fpath+File.separator+msgId;
        } else {
            fpath = msgId;
        }

        File folder = new File(this.attachPath+File.separator+fpath);

        if (!folder.exists()) {
            if (create)
                folder.mkdirs();
            else
                folder = null;
        } else if (!folder.isDirectory()) {
            throw new IllegalStateException("The requested path exists but is not a directory: "+fpath);
        }

        return folder;
    }
    
    protected static class MessageImpl extends AbstractMessage implements IMessage {
        
        /*
         * Class members
         */
       
        /*
         * Instance members
         */
        private IAttachment[] attachments;
        private IRecipient[] recipients;

        /*
         * Public API
         */
        
        public MessageImpl(IMessageFactory owner, String msgId, 
                            String subject, IAddress sender, IRecipient[] recipients,
                            Date dateSent, String body,
                            Priority priority, boolean read) {
            super(owner, msgId, sender, new IRecipient[0], 
                  subject, dateSent, body, new IAttachment[0], priority, false);
            this.attachments = null;
            this.recipients = recipients;
            this.read = read;
        }
        
        public boolean isUnread() throws MercuryException {
        	return !this.read;
        }

        /**
         * @see net.unicon.mercury.IMessage#setRead(boolean)
         */
        public void setRead(boolean seen) throws MercuryException {
            ((RdbmsMessageFactory)getOwner()).setRead(this, seen); 
            this.read = seen;   
        }

        public IRecipient[] getRecipients() throws MercuryException {
        	if (this.recipients == null)
	            this.recipients = ((RdbmsMessageFactory)this.getOwner()).getRecipients(getId());
        	return this.recipients;
        }

        public IRecipient[] getRecipients(IRecipientType[] types) throws MercuryException {
            return ((RdbmsMessageFactory)this.getOwner()).getRecipients(getId(), types);        
        }

        public IAttachment[] getAttachments() throws MercuryException {
            if (this.attachments == null)
                this.attachments = ((RdbmsMessageFactory)this.getOwner()).getAttachments(getId());
            return this.attachments;
        }
        
        public boolean equals(Object o){
            if(!(o instanceof IMessage)){
                return false;
            }
            try {
                if(this.id.equals(((IMessage)o).getId())){
                    return true;
                }
            } catch (MercuryException e) {
                throw new RuntimeException("Error in getting the message id ", e);
            }
            return false;
        }
        
        public int hashCode(){
            return this.id.hashCode();
        }
        
    }
    
    protected static class RdbmsMessageFolder extends AbstractFolder {

        /*
         * Class members
         */
        
        /*
         * Instance members
         */

        
        /*
         * Public API
         */

        /**
         * Adds a message to this folder.
         * 
         * @param msg  message to be added to this folder. Cannot be null.
         * @see net.unicon.mercury.IFolder#addMessage(net.unicon.mercury.IMessage)
         */
        public void addMessage(IMessage msg) throws MercuryException {

            // Assertions
            if (msg == null){
                throw new IllegalArgumentException("Argument 'msg' cannot be null." );   
            }
            
            ((RdbmsMessageFactory)getOwner()).addMessage(this, msg);

        }

        /**
         * @see net.unicon.mercury.IFolder#removeMessage(net.unicon.mercury.IMessage)
         */
        public boolean removeMessage(IMessage msg) throws MercuryException {
            
            // Assertions
            if (msg == null){
                throw new IllegalArgumentException("Argument 'msg' cannot be null." );   
            }
            
            return ((RdbmsMessageFactory)getOwner()).removeMessage(this, msg);
        }

        /**
         * @see net.unicon.mercury.IFolder#expunge()
         */
        public void expunge() throws MercuryException {

            ((RdbmsMessageFactory)getOwner()).expunge(this);
        }
 
        /**
         * @see net.unicon.mercury.IFolder#getMessage(String)
         */
        public IMessage getMessage(String id) throws MercuryException {
            
            return ((RdbmsMessageFactory)getOwner()).getMessage(id);
        }

        /**
         * @see net.unicon.mercury.IFolder#getMessages()
         */
        public IMessage[] getMessages() throws MercuryException {
            
            return ((RdbmsMessageFactory)getOwner()).getMessages(this);
        }

        /**
         * @see net.unicon.mercury.IFolder#getSubfolders()
         */
        public IFolder[] getSubfolders() throws MercuryException {
            
            return ((RdbmsMessageFactory)getOwner()).getSubfolders(this);
        }
      
        /**
         * @see net.unicon.mercury.IFolder#createSubfolder(java.lang.String)
         */
        public IFolder createSubfolder(String name) throws MercuryException {
            
            // Assertions
            if (name == null){
                throw new IllegalArgumentException("Argument 'name' cannot be null." );   
            }
            
            return ((RdbmsMessageFactory)getOwner()).createSubfolder(this, name);
        }

        /**
         * @see net.unicon.mercury.IFolder#deleteFolder(boolean)
         */
        public void deleteFolder(boolean recurse) throws MercuryException {
           
            ((RdbmsMessageFactory)getOwner()).deleteFolder(this, recurse);
        }
        
        public int getUnreadCount() throws MercuryException {
            
            return ((RdbmsMessageFactory)getOwner()).getUnreadCount(this);
        }

        /**
         * @see net.unicon.mercury.IFolder#search(
         *                         net.unicon.penelope.IDecisionCollection, boolean)
         */
        public IMessage[] search(IDecisionCollection filters, boolean recurse)
                        throws MercuryException {
            
            // Assertions
            if (filters == null){
                throw new IllegalArgumentException("Argument 'filters' cannot be null." );   
            }
            
            return ((RdbmsMessageFactory)getOwner()).search(this, filters, recurse);
        }
        
        /*
         * Protected API
         */
        

        /*
         * Implementation
         */
        
        /**
         * Private constructor that forces use of factory methods.
         * 
         * @param owner
         * @param ds
         * @param id
         * @param label
         */    
        public RdbmsMessageFolder(IMessageFactory owner, long id, String label) {
            super(owner, id, label);           
        }

        /* (non-Javadoc)
         * @see net.unicon.mercury.IFolder#getSubfolder(java.lang.String)
         */
        public IFolder getSubfolder(String label) throws MercuryException {
            // TODO FIXME Auto-generated method stub
            return null;
        }
    } 
    
    protected static class RdbmsAttachmentImpl extends AttachmentImpl{

        private final String msgId;
        private final File file;
        
        /**
         * @param owner
         * @param filename
         * @param mimetype
         * @param stream
         */
        public RdbmsAttachmentImpl(int id, File file, String msgId) throws MercuryException {
            super(id, file.getName(), MimeTypeMap.getContentType(file.getName()), (InputStream)null);
            this.msgId = msgId;
            this.file = file;
        }
        
        public InputStream getInputStream() {
            try {
                return new FileInputStream(this.file);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to retrieve attachment contents", ex);
            }
        }
        
        public String getMsgId(){
            return this.msgId;
        }

        public long getSize() {
        	return file.length();
        }
    }    
   
    protected static class SystemMessageImpl extends BaseAbstractMessage {

        
        // instance members
        private final String msgId;
        private final IAddress sender;
        private final String subject;
        private final Date date;
        private final String body;
        private final Priority priority;
        
        /**
         * @param owner
         */
        public SystemMessageImpl(IMessageFactory owner
                			, String id
    			            , IAddress sender
    			            , String subject
    			            , Date date
    			            , String body
    			            , Priority priority) {
            super(owner);
            
            // assertions
            if(id == null){
                throw new IllegalArgumentException("Argument 'id' can not be null.");
            }
            if(id.equals("")){
                throw new IllegalArgumentException("Argument 'id' can not be empty.");
            }
            if(sender == null){
                throw new IllegalArgumentException("Argument 'sender' can not be null.");
            }
            if(subject == null){
                throw new IllegalArgumentException("Argument 'subject' can not be null.");
            }
            if(date == null){
                throw new IllegalArgumentException("Argument 'date' can not be null.");
            }
            if(body == null){
                throw new IllegalArgumentException("Argument 'body' can not be null.");
            }
            if(priority == null){
                throw new IllegalArgumentException("Argument 'priority' can not be null.");
            }
            
            this.msgId = id;
            this.sender = sender;
            this.subject = subject;
            this.date = date;
            this.body = body;
            this.priority = priority;        
            
        }

        public String getId() throws MercuryException {
            return msgId;
        }

        public IAddress getSender() throws MercuryException {
            return this.sender;
        }

        public IRecipient[] getRecipients() throws MercuryException {
            return ((RdbmsMessageFactory)this.getOwner()).getRecipients(this.msgId);
        }

        public IRecipient[] getRecipients(IRecipientType[] types) throws MercuryException {
            return ((RdbmsMessageFactory)this.getOwner()).getRecipients(this.msgId, types);        
        }

        public String getSubject() throws MercuryException {
            return this.subject;
        }

        public Date getDate() throws MercuryException {
            return date;
        }

        public String getAbstract() throws MercuryException {
            String rslt = getBody();
            if (rslt != null && rslt.length() > 50)
                rslt = rslt.substring(0, 50);

            return rslt;
        }

        public String getBody() throws MercuryException {
            return body;
        }

        public IAttachment[] getAttachments() throws MercuryException {
            return ((RdbmsMessageFactory)this.getOwner()).getAttachments(this.msgId);
        }

        public Priority getPriority() throws MercuryException {
            return this.priority;
        }

        public boolean isUnread() throws MercuryException {
            return false;
        }

        public boolean isDeleted() throws MercuryException {
            return false;
        }

        public void setRead(boolean seen) throws MercuryException {
            // don't do anything.            
        }

    }

    protected static class SystemFolder extends RdbmsMessageFolder {
        
       public SystemFolder(IMessageFactory owner, long id, String label) {
            super(owner, id, label);           
        }
        
        public void addMessage(IMessage msg) throws MercuryException {
            throw new UnsupportedOperationException("The method addMessage() is " +
            		"not supported by SystemFolder.");
        }
        
        public String getIdString() {
            return this.getLabel();
        }
        
        /**
         * @see net.unicon.mercury.IFolder#removeMessage(net.unicon.mercury.IMessage)
         */
        public boolean removeMessage(IMessage msg) throws MercuryException {
            
            // Assertions
            if (msg == null){
                throw new IllegalArgumentException("Argument 'msg' cannot be null." );   
            }
            
            return ((RdbmsMessageFactory)getOwner()).retractMessage(msg.getId());
        }

        /**
         * @see net.unicon.mercury.IFolder#expunge()
         */
        public void expunge() throws MercuryException {
            // will not do anything here.
            // the retractMessage method does a delete and expunge internally.
        }
 
        /**
         * @see net.unicon.mercury.IFolder#getMessages()
         */
        public IMessage[] getMessages() throws MercuryException {
            return ((RdbmsMessageFactory)getOwner()).getSystemMessages();
        }
        
        /**
         * @see net.unicon.mercury.IFolder#getMessages()
         */
        public IMessage getMessage(String msgId) throws MercuryException {
            return ((RdbmsMessageFactory)getOwner()).getSystemMessage(msgId);
        }

        /**
         * @see net.unicon.mercury.IFolder#getSubfolders()
         */
        public IFolder[] getSubfolders() throws MercuryException {
            
            throw new UnsupportedOperationException("The method getSubfolders() is " +
    		"not supported by SystemFolder.");
        }
      
        /**
         * @see net.unicon.mercury.IFolder#createSubfolder(java.lang.String)
         */
        public IFolder createSubfolder(String name) throws MercuryException {
            
            throw new UnsupportedOperationException("The method createSubfolder() is " +
    		"not supported by SystemFolder.");
        }

        /**
         * @see net.unicon.mercury.IFolder#deleteFolder(boolean)
         */
        public void deleteFolder(boolean recurse) throws MercuryException {
           
            throw new UnsupportedOperationException("The method deleteFolder() is " +
    		"not supported by SystemFolder.");
        }
        
        public int getUnreadCount() throws MercuryException {            
            throw new UnsupportedOperationException("The method getUnreadCount() is " +
    		"not supported by SystemFolder.");
        }

        /**
         * @see net.unicon.mercury.IFolder#search(
         *                         net.unicon.penelope.IDecisionCollection, boolean)
         */
        public IMessage[] search(IDecisionCollection filters, boolean recurse)
                        throws MercuryException {
            
            // Assertions
            if (filters == null){
                throw new IllegalArgumentException("Argument 'filters' cannot be null." );   
            }
            
            return ((RdbmsMessageFactory)getOwner()).search(this, filters, recurse);
        }
        
        /*
         * Protected API
         */       
    }

    /**
     * Closes SQL Statement object to free resources.
     *
     * @param stmt
     *     SQL Statement to be closed. Ignores null
     *     arguments passed to this method.
     * @throws RuntimeException
     *     if there is an error closing the Statement.
     */
    protected void closeStatement(Statement stmt) {

        if (stmt == null) {
            return;
        }

        try {
            stmt.close();
        } catch (Throwable t) {
            throw new RuntimeException("Error closing SQL statement.", t);
        }

    }
    
    /**
     * Closes SQL ResultSet object to free resources.
     *
     * @param rs
     *     SQL ResultSet to be closed. Ignores null
     *     arguments passed to this method.
     * @throws RuntimeException
     *     if there is an error closing the ResultSet.
     */
    protected void closeResultSet(ResultSet rs) {

        if (rs == null) {
            return;
        }

        try {
            rs.close();
        } catch (Throwable t) {
            throw new RuntimeException("Error closing SQL ResultSet.", t);
        }

    }

    /**
     * Rolls back the provided connection.
     *
     * @param  conn
     *     a Connection to be rolled back. Cannot be null.
     */
    protected void rollBack(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                String msg = "Error during rollback. Data may be corrupted.";
                throw new RuntimeException(msg, e);
            }
        }
    }

    /**
     * Closes database Connection object to free resources.
     * Closing a Connection also closes any Statements and
     * ResultSets associated with it.
     *
     * @param conn
     *     database Connection to be closed. Ignores null
     *     arguments passed to this method.
     * @throws RuntimeException
     *     if there is an error closing the database connection.
     */
    protected void cleanupConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Throwable t) {
                throw new RuntimeException("Error closing DB Connection.", t);
            }
        }
    }

    protected void cleanupTransactionConnection(Connection conn,
        ConnState connst) {
        cleanupTransactionConnection(conn, connst, true, false);
    }
    
    protected void cleanupTransactionConnection(Connection conn,
        ConnState connst, boolean close) {
        cleanupTransactionConnection(conn, connst, close, false);
    }
    
    protected void cleanupTransactionConnection(Connection conn, ConnState connst,
        boolean close, boolean serialized) {
        if (conn == null) {
            return;
        }

        try {
            if (connst != null) {
                if (serialized) {
                    conn.setTransactionIsolation(connst.isoLevel);
                }
                conn.setAutoCommit(connst.autoCommit);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error cleaning up DB Connection.", ex);
        } finally {
            if (close) {
                cleanupConnection(conn);
            }
        }
    }
    
    protected ConnState beginTransaction(Connection conn)
    throws SQLException {
        return beginTransaction(conn, false);
    }
    
    protected ConnState beginTransaction(Connection conn, boolean serialized)
    throws SQLException {
        ConnState rslt = new ConnState();
        if (serialized) {
	        rslt.isoLevel = conn.getTransactionIsolation();
	        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }
        rslt.autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        return rslt;
    }
    
    protected boolean constraintViolation(String msg) {
        return constraintViolationMessagePattern.matcher(msg).find();
    }

    protected static class ConnState {
        public boolean autoCommit;
        public int isoLevel;
    }
    
    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int r = 0;

        try {
            while ((r = in.read(b)) >= 0) {
                out.write(b, 0, r);
            }
        } finally {
        	try { out.close(); }
        	finally { in.close(); }
        }
    }
    
}
