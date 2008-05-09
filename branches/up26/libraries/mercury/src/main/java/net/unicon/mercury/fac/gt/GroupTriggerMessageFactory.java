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

package net.unicon.mercury.fac.gt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.unicon.alchemist.rdbms.QueryManager;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.mercury.EntityType;
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
import net.unicon.mercury.fac.AddressImpl;
import net.unicon.mercury.fac.rdbms.RdbmsDispatchMessageStatus;
import net.unicon.mercury.fac.rdbms.RdbmsDispatchType;
import net.unicon.mercury.fac.rdbms.RdbmsMessageFactory;
import net.unicon.mercury.fac.rdbms.RdbmsMessageRecipient;
import net.unicon.mercury.fac.rdbms.RdbmsRecipientType;


public class GroupTriggerMessageFactory extends RdbmsMessageFactory {

    private final ICivisFactory cFac;
    private final DataSource ds;
    private static QueryManager qm;

    public synchronized static void bootstrap(String queryFile) {
       if (qm != null)
          return;
       if (queryFile == null) {
          throw new IllegalArgumentException("Arguement 'queryFile' cannot by null");
       }
       qm = new QueryManager(queryFile, RdbmsMessageFactory.class.getName());
       RdbmsMessageFactory.bootstrap(queryFile);
    }
    
    public GroupTriggerMessageFactory(DataSource ds, String attachPath, int layers
            , String username, int expiration, ICivisFactory cFac) {
        
        super(ds, attachPath, layers, username, expiration);
        
        this.cFac = cFac; // Can be null; only needed for delivering messages.
        this.ds = ds;
    }    
    
	public IMessage sendMessage(IRecipient[] recipients, String subject,
			String body, IAttachment[] attachments, Priority priority)
			throws MercuryException {

		// Assertions

		if (recipients == null) {
			throw new IllegalArgumentException(
					"Argument 'recipients' cannot be null.");
		}
		if (recipients.length == 0) {
			throw new IllegalArgumentException("No recipients specified.");
		}
		if (subject == null) {
			throw new IllegalArgumentException(
					"Argument 'subject' cannot be null.");
		}
		if (body == null) {
			throw new IllegalArgumentException(
					"Argument 'body' cannot be null.");
		}
		// attachments can be null.

		IMessage rslt = null;

		Set userRecipients = new HashSet();
		Set groupRecipients = new HashSet();

		// separate out the group and users
		for (int i = 0; i < recipients.length; i++) {
			if (recipients[i].getEntityType().equals(EntityType.USER)) {
				userRecipients.add(recipients[i]);
			} else {
				groupRecipients.add(recipients[i]);
			}
		}
		rslt = super.sendMessage(
				(IRecipient[]) userRecipients.toArray(new IRecipient[0]),
				subject, body, attachments, priority);

		if (groupRecipients.size() > 0) {
			Connection conn = null;
			ConnState connst = null;
			PreparedStatement pstmt = null;
			Set sentSet = new HashSet();
			long messageId = Long.parseLong(rslt.getId());
			boolean autoCommit = true;

			try {
				// Setup connection
				conn = ds.getConnection();
				connst = beginTransaction(conn);

				pstmt = conn.prepareStatement(
                    qm.getQuery("GT_DISPATCH_MESSAGE"));
				pstmt.setLong(1, messageId);

				recipients = (IRecipient[]) groupRecipients
						.toArray(new IRecipient[0]);
				for (int i = 0; i < recipients.length; i++) {
					// Assertions
					if (recipients[i] == null) {
						String msg = "Argument 'recipients [IRecipient[]]' "
								+ "cannot contain null elements.";
						throw new IllegalArgumentException(msg);
					}

					if (!sentSet.add(recipients[i])) {
						continue;
					}

					IRecipientType rType = recipients[i].getType();
					if (!(rType instanceof RdbmsRecipientType)) {
						rType = RdbmsRecipientType.getType(rType.getLabel());
					}
					pstmt.setString(2, recipients[i].getAddress()
							.toNativeFormat());

					pstmt.executeUpdate(); // Recipient added, not committed
				}

				// Finally commit transaction
				conn.commit();

			} catch (Exception t) {
				rollBack(conn); // Message not added, nor any recipients if any fail
				String msg = "GroupTriggerMessageFactory was not able to send message.";
				throw new MercuryException(msg, t);
			} finally {
				closeStatement(pstmt); pstmt = null;
				cleanupTransactionConnection(conn, connst); conn = null;
			}
		}

		return rslt;
	}

    /**
     * Physically deletes the message from the sender and recipients 
     * folders.
     * @param msgId
     */
    private boolean retractMessage(String msgId) throws MercuryException {

		// Assertions
		if (msgId == null) {
			throw new IllegalArgumentException(
					"Argument 'msgId' cannot be null.");
		}

		Connection conn = null;
		PreparedStatement pstmt = null;

		try {
			// delete the message from the group table
            conn = ds.getConnection();
			pstmt = conn.prepareStatement(qm.getQuery("GT_DELETE_GROUP_DISPATCH_MESSAGE"));
			pstmt.setString(1, msgId);
			pstmt.executeUpdate();
		} catch (SQLException se) {
			String m = "GroupTriggerMessageFolder was not able to delete the "
					+ "message with id: " + msgId;
			throw new MercuryException(m, se);
		} finally {
			closeStatement(pstmt);
			cleanupConnection(conn);
		}

		return true;
	}

	/**
	 * Delivers the messages sent to the groups that the user belongs to.
	 * @param msgId
	 */
	private boolean deliverMessages() throws MercuryException {
	   return deliverMessages(3);
   }
   
	private boolean deliverMessages(int remaining) throws MercuryException {
		Connection conn = null;
		ConnState connst = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs = null;
		boolean autoCommit = false;
		IGroup[] groups = getGroups(super.getDomainOwner());
		if (groups.length == 0)
			return true;
		
		long msgId = 0;		// default value

		try {
            StringBuffer GET_GROUP_MESSAGE = new StringBuffer(
                qm.getQuery("GT_GET_GROUP_MESSAGE_PRE"));
            
            conn = ds.getConnection();
            connst = beginTransaction(conn);

            // get the messages sent to the users groups
            for (int i = 0; i < groups.length - 1; i++) {
                GET_GROUP_MESSAGE.append("?, ");
            }
            GET_GROUP_MESSAGE.append("? ");

            GET_GROUP_MESSAGE.append(qm.getQuery("GT_GET_GROUP_MESSAGE_POST"));

            pstmt = conn.prepareStatement(GET_GROUP_MESSAGE.toString());

            int i = 0;
            for (; i < groups.length; i++) {
                pstmt.setString(i + 1, groups[i].getPath().toUpperCase());
            }
            
            // Filter delivery to include only messages sent after the 
            // initial login date for non-local (i.e. LDAP) users, or
            // the creation date of local users.
            Date initialLoginDate = getInitialLoginDate();
            if (initialLoginDate == null) {
                // If there is no initial login date, then the user precedes
                // initial login recording, so he/she should get all messages 
                // sent to his/her groups at any time (since the epoch should 
                // be sufficient)
                initialLoginDate = new Date(0L);
            }
            pstmt.setTimestamp(++i, new Timestamp(initialLoginDate.getTime()));            

            pstmt.setString(++i, getDomainOwner());
            pstmt.setInt(++i, RdbmsDispatchType.RECEIVER.toInt());

            rs = pstmt.executeQuery();

            pstmt2 = conn.prepareStatement(qm.getQuery("DISPATCH_MESSAGE"));
            pstmt2.setString(2, super.getDomainOwner());
            pstmt2.setInt(3, RdbmsDispatchType.RECEIVER.toInt());
            pstmt2.setInt(4, RdbmsRecipientType.TO.toInt());
            pstmt2.setString(5, RdbmsMessageFactory.UNREAD); // Unread to start
            pstmt2.setLong(6, SpecialFolder.INBOX_VALUE); // Place it in sender's outbox

            while (rs.next()) {
                pstmt2.setLong(1, rs.getLong("msg_id"));
                pstmt2.executeUpdate(); // Recipient added, not committed
            }

            conn.commit();
		} catch (SQLException se) {
			rollBack(conn);
         if (remaining > 0 && constraintViolation(se.getMessage())) {
            deliverMessages(remaining-1);
         } else {
				String m = "GroupTriggerMessageFolder was not able to deliver the "
						+ "messages to : " + super.getDomainOwner();
				throw new MercuryException(m, se);
         }
		} finally {
			closeResultSet(rs);
			closeStatement(pstmt);
			closeStatement(pstmt2);
			cleanupTransactionConnection(conn, connst);
		}

		return true;
	}

	private IRecipient[] getGroupRecipients(String msgId) {
		List rslt = new ArrayList();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = ds.getConnection();

			pstmt = conn.prepareStatement(qm.getQuery("GT_GET_RECIPIENT"));
			pstmt.setString(1, msgId);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				rslt.add(new RdbmsMessageRecipient(new AddressImpl(rs
						.getString("GROUP_PATH"), rs.getString("GROUP_PATH")),
						RdbmsRecipientType.TO, false, EntityType.GROUP));
			}

		} catch (Exception e) {
			throw new RuntimeException(
					"Failed retrieving group recipients for msg_id " + msgId, e);
		} finally {
			closeResultSet(rs);
			closeStatement(pstmt);
			cleanupConnection(conn);
		}

		return (IRecipient[]) rslt.toArray(new IRecipient[0]);
	}

	private IGroup[] getGroups(String username) throws MercuryException {
      if (cFac == null) {
         throw new MercuryException("Unable to enumerate user groups; no civis factory registered.");
      }
		return cFac.getPerson(username).getGroups();
	}
	
	protected IMessage createMessage(
			IMessageFactory owner, String msgId,
			String subject, IAddress sender,
			IRecipient[] recipients, Date dateSent,
			String body, Priority priority,
			boolean read) {
		return new GroupTriggerMessage(owner, msgId,
				subject, sender, recipients, dateSent,
				body, priority, read);
	}
	
	protected IMessage createSystemMessage(
			IMessageFactory owner, String id,
			IAddress sender, String subject,
			Date date, String body,
			Priority priority)
	{
        return new GroupTriggerSystemMessage(owner, id, sender
                , subject, date, body, priority);
	}

    protected IFolder createFolder(IMessageFactory owner, long id, String label){
        return new GroupTriggerMessageFolder(owner, id, label);
    }
    
    protected IFolder createSystemFolder(IMessageFactory owner, long id, String label){
        return new GroupTriggerSystemFolder(owner, id, label);
    }
    
    protected static class GroupTriggerMessageFolder extends RdbmsMessageFolder {
        
        public GroupTriggerMessageFolder(IMessageFactory owner, long id, String label) {
            super(owner, id, label);           
        }

        public int getUnreadCount() throws MercuryException {
            
            // deliver the messages sent to the users groups
            ((GroupTriggerMessageFactory)this.getOwner()).deliverMessages();
            return super.getUnreadCount();
        }

        public IMessage[] getMessages() throws MercuryException {
            
            // deliver messages sent to the users groups
            if(this.getLabel().equals(SpecialFolder.INBOX.getLabel())){
                ((GroupTriggerMessageFactory)this.getOwner()).deliverMessages();
            }
            return super.getMessages();
        }

       public boolean removeMessage(IMessage msg) throws MercuryException {
            
            // Assertions 
            if(msg == null){
                throw new IllegalArgumentException("Argument 'msg' cannot be null.");                
            }
            
            // remove the message from hg_group_message when the folder 
            // is a SYSTEM_FOLDER
            if(getLabel().equals(SpecialFolder.SYSFOLDER.getLabel())){
                ((GroupTriggerMessageFactory)this.getOwner()).retractMessage(msg.getId());
            }
            
            return super.removeMessage(msg);
        }
                              
    }
    
    protected static class GroupTriggerSystemFolder extends SystemFolder {
        
        public GroupTriggerSystemFolder(IMessageFactory owner, long id, String label) {
            super(owner, id, label);           
        }

       public boolean removeMessage(IMessage msg) throws MercuryException {
            
            // Assertions 
            if(msg == null){
                throw new IllegalArgumentException("Argument 'msg' cannot be null.");                
            }
            
            // remove the message from hg_group_message when the folder 
            // is a SYSTEM_FOLDER
            ((GroupTriggerMessageFactory)this.getOwner()).retractMessage(msg.getId());
            
            return super.removeMessage(msg);
        }
                              
    }
    
    protected static class GroupTriggerMessage extends MessageImpl {
        public GroupTriggerMessage(IMessageFactory owner, String msgId, 
                            String subject, IAddress sender, IRecipient[] recipients,
                            Date dateSent, String body,
                            Priority priority, boolean read) {
        	super(owner, msgId, subject, sender,
        		  recipients, dateSent, body,
        	      priority, read);
        }
 
        public IRecipient[] getRecipients() throws MercuryException {
            // get the group recipients
            IRecipient[] recs1 = ((GroupTriggerMessageFactory)getOwner()).getGroupRecipients(getId());
            IRecipient[] recs2 = super.getRecipients();
            IRecipient[] rslt =  new IRecipient[recs1.length + recs2.length];
            
            System.arraycopy(recs1, 0, rslt, 0, recs1.length);
            System.arraycopy(recs2, 0, rslt, recs1.length, recs2.length);
            
            return rslt;
        }
        
        /* (non-Javadoc)
         * @see net.unicon.mercury.fac.AbstractMessage#getRecipients(net.unicon.mercury.IRecipientType[])
         */
        public IRecipient[] getRecipients(IRecipientType[] types)
                throws MercuryException {
        	// TODO: This is "wrong", but the implementation currently only 
        	// has one recipient type, so it is fine.
            return getRecipients();
        }
    }

    protected static class GroupTriggerSystemMessage extends SystemMessageImpl {
        
        public GroupTriggerSystemMessage(IMessageFactory owner
    			, String id
	            , IAddress sender
	            , String subject
	            , Date date
	            , String body
	            , Priority priority){
        
            super(owner, id, sender
		            , subject
		            , date
		            , body
		            , priority);
        }
 
        public IRecipient[] getRecipients() throws MercuryException {
            // get the group recipients
            IRecipient[] recs1 = ((GroupTriggerMessageFactory)getOwner()).getGroupRecipients(getId());
            IRecipient[] recs2 = super.getRecipients();
            IRecipient[] rslt =  new IRecipient[recs1.length + recs2.length];
            
            System.arraycopy(recs1, 0, rslt, 0, recs1.length);
            System.arraycopy(recs2, 0, rslt, recs1.length, recs2.length);
            
            return rslt;
        }
        
        /* (non-Javadoc)
         * @see net.unicon.mercury.fac.AbstractMessage#getRecipients(net.unicon.mercury.IRecipientType[])
         */
        public IRecipient[] getRecipients(IRecipientType[] types)
                throws MercuryException {
        	// TODO: This is "wrong", but the implementation currently only 
        	// has one recipient type, so it is fine.
            return getRecipients();
        }
    }
}
