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

package net.unicon.mercury.fac.email;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.search.SearchTerm;

import net.unicon.mercury.fac.AbstractFolder;
import net.unicon.mercury.fac.AbstractMessageFactory;
import net.unicon.mercury.FolderNotFoundException;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.MercuryException;
import net.unicon.penelope.IDecisionCollection;

/**
 * Email folder class which bridges the JavaMail type 'Folder' to the Mercury
 * type 'IFolder'.
 *
 * It is important to distinguish between the 'IFolder' and 'Folder' types; the
 * former is provided by Mercury, and the latter by JavaMail. They are in no
 * way compatible and have greatly differing semantics.
 *
 * @author eandresen
 * @version 2005-02-15
 * @see IFolder
 */
public class EmailFolder extends AbstractFolder
{
    private final Folder folder;
    private boolean needexpunge = false;

    /**
     * Construct a Mercury Folder that bridges a JavaMail Folder.
     *
     * @param owner Owning factory
     * @param folder JavaMail folder to wrap around
     *
     * @throws JavaMailException if the Folder doesn't exist or has an internal error
     */
    public EmailFolder(EmailMessageFactory owner, Folder folder)
                        throws FolderNotFoundException, MercuryException
    {
        // Always pass null as the parent to AbstractFolder; we override getParent().
        super(owner, /* not used */0, folder.getName(), null);

        // Assert folder existance.
        try {
            if (!folder.exists())
                throw new FolderNotFoundException("Folder doesn't exist.");
        } catch (MessagingException me) {
            throw new JavaMailException("Unable to determine folder existance.", me);
        }

        this.folder = folder;
    }

    public String getIdString() {
        return this.folder.getFullName();
    }

    /**
     * Get the parent of this folder.
     *
     * @return a reference to this Folder's parent, or null if it is
     *         parentless.
     */
    public IFolder getParent() throws MercuryException {
        IFolder rslt = null;

        try {
            Folder f = this.folder.getParent();
            // EA: Apparently, even the Sun implementations differ slightly in
            // their getParent() rules. The IMAP store will return a folder
            // with an empty name, instead of null.
            if (f != null && !f.getFullName().equals("")) {
                // Use getFolder because of caching.
                rslt = ((EmailMessageFactory)getOwner()).getFolder(f.getFullName());
            }
        } catch (MessagingException me) {
            throw new JavaMailException("Unable to acquire parent folder.", me);
        }

        return rslt;
    }

    /**
     * Add a message to the underlying Folder.
     *
     * @param msg EmailMessage to add to the folder.
     * @throws JavaMailException if appendMessages fails on the underlying Folder
     */
    public synchronized void addMessage(IMessage msg) throws MercuryException {
        EmailMessage emsg = null;
        if (!(msg instanceof EmailMessage)) {
            emsg = new EmailMessage(getOwner(), /* folder */ null, msg);
        } else {
            emsg = (EmailMessage)msg;
        }

        // EA: JavaMail API claims this works when the folder isn't opened,
        // but this is false. It must be open in Read-Write mode.
        ensureOpen(Folder.READ_WRITE);
        try {
            folder.appendMessages(new Message[] { emsg.getMessage() });
        } catch (MethodNotSupportedException me) {
            // EA: POP3 throws this exception.
            throw new JavaMailException(
                    "appendMessages not supported for this Store", me);
        } catch (MessagingException me) {
            throw new JavaMailException("appendMessages failed", me);
        }
    }

    /**
     * Expunge deleted messages from the Folder.
     * @throws MercuryException if the operation fails.
     */
    public synchronized void expunge() throws MercuryException {
        try {
            if (folder.isOpen()) {
                folder.close(true);
                needexpunge = false;
            }
        } catch (MessagingException me) {
            throw new JavaMailException("Expunge failed", me);
        }
    }

    /**
     * Remove a message from the underlying Folder.
     *
     * @param msg EmailMessage to remove from the Folder.
     * @throws MercuryException if the operation fails.
     */
    public synchronized boolean removeMessage(IMessage msg)
                                throws MercuryException {
        boolean rslt = false;

        if (!(msg instanceof EmailMessage))
            throw new IllegalArgumentException(
                    "Argument 'msg' must be of the underlying type "
                  + "EmailMessage.");
        EmailMessage emsg = (EmailMessage)msg;
        if (!emsg.getOwner().equals(getOwner())) 
            throw new IllegalArgumentException(
                    "Argument 'msg' does not belong to this container.");

        // EA: Must be open in Read-Write mode.
        ensureOpen(Folder.READ_WRITE);

        // EA: The JavaMail equivalent is setting the DELETE flag on the
        // contained message. This does not, however, remove it from the
        // folder's contents until expunged. Calling expunge() may suffice.
        emsg.setDeleted();
        needexpunge = true;
        rslt = true;

        return rslt;
    }

    /**
     * Retrieve messages from the Folder.
     *
     * @return Array of messages contained in the folder.
     * @throws MercuryException if getMessages() fails on the Folder
     */
    public IMessage[] getMessages() throws MercuryException {
        Message[] msgs = null;

        try {
            ensureOpen(Folder.READ_ONLY);
            msgs = folder.getMessages();

            // Shouldn't return null, but...
            if (msgs == null)
                msgs = new Message[0];
        } catch (MessagingException me) {
            throw new JavaMailException("getMessages() failed", me);
        }

        List rslt = new ArrayList(msgs.length);

        for (int i = 0; i < msgs.length; i++) {
            EmailMessage msg = new EmailMessage(getOwner(), this, msgs[i]);
            rslt.add(msg);
        }

        return (IMessage[])rslt.toArray(new IMessage[0]);
    }

    /**
     * Retrieve the list of subfolders from the Folder.
     *
     * @return Array of subfolders.
     * @throws JavaMailException if list() fails on the underlying Folder.
     * @throws UnsupportedOperationException if subfolders are not supported
     */
    public IFolder[] getSubfolders() throws MercuryException {
        if (!getOwner().getFeatures().allowSubfolders())
            throw new UnsupportedOperationException(
                        "Subfolders are not supported");

        Folder[] folders = null;

        try {
            ensureOpen(Folder.READ_ONLY);
            folders = folder.list();

            // Shouldn't return null, but...
            if (folders == null)
                folders = new Folder[0];
        } catch (MessagingException me) {
            // EA: XXX: POP3 throws this (unfortunately not MethodNotSupportedException)
            // Try to swallow that one instance.
            if (me.getMessage().equals("not a directory"))
                folders = new Folder[0];
            else
                throw new JavaMailException("list() failed", me);
        }

        List rslt = new ArrayList(folders.length);

        try {
            for (int i = 0; i < folders.length; i++)
                rslt.add(((EmailMessageFactory)getOwner())
                            .getFolder(folders[i].getFullName()));
        } catch (MercuryException me) {
            throw new JavaMailException("Failed to list subfolders", me);
        }

        return (IFolder[])rslt.toArray(new IFolder[0]);
    }

    public IFolder getSubfolder(String label) throws MercuryException {
        if (!getOwner().getFeatures().allowSubfolders())
            throw new UnsupportedOperationException(
                        "Subfolders are not supported");

        IFolder rslt = null;

        try {
            ensureOpen(Folder.READ_ONLY);
            Folder f = folder.getFolder(label);

            if (f.exists())
                rslt = ((EmailMessageFactory)getOwner()).getFolder(f.getFullName());
        } catch (MessagingException me) {
            // EA: XXX: POP3 throws this (unfortunately not MethodNotSupportedException)
            // Try to swallow that one instance.
            if (me.getMessage().equals("not a directory"))
                rslt = null;
            else
                throw new JavaMailException("getFolder() failed", me);
        }

        return rslt;
    }

    /**
     * Create a folder beneath this folder.
     * @param name Name of the folder to create.
     * @throws JavaMailException if the operation fails
     * @throws UnsupportedOperationException if subfolders are not supported
     */
    public IFolder createSubfolder(String name) throws MercuryException
    {
        if (!getOwner().getFeatures().allowSubfolders())
            throw new UnsupportedOperationException(
                        "Subfolders are not supported");

        assert name != null : "Argument 'name' cannot be null.";
        if (name.trim().equals(""))
            throw new IllegalArgumentException(
                        "Argument 'name' cannot be an empty string.");
        // Further name validation?

        EmailFolder rslt = null;

        try {
            Folder f = this.folder.getFolder(name);
            assert f != null;
            rslt = ((EmailMessageFactory)getOwner()).createFolder(f);
        } catch (MessagingException me) {
            throw new JavaMailException("Failed to create subfolder", me);
        }

        if (rslt == null)
            throw new JavaMailException(
                        "Failed to create subfolder: Unknown error");

        return rslt;
    }

    /**
     * Delete this folder.
     * If <code>recurse</code> is true, then all subfolders and messages
     * contained within this folder will be deleted. If <code>recurse</code> is
     * false and the folder is not empty, an exception will be thrown and the
     * folder will remain in its original state.
     * @param recurse Recursive deletion flag
     * @throws JavaMailException if the folder delete operation failed
     * @throws MercuryException if this folder was not deleted
     * @throws UnsupportedOperationException if subfolders are not supported
     */
    public void deleteFolder(boolean recurse) throws MercuryException
    {
        if (!getOwner().getFeatures().allowSubfolders())
            throw new UnsupportedOperationException(
                        "Subfolders are not supported");

        boolean rslt = false;

        if (getParent() == null)
            throw new MercuryException("The toplevel folder cannot be deleted.");

        try {
            if (folder.isOpen())
                folder.close(false);

            rslt = folder.delete(recurse);
        } catch (MessagingException me) {
            throw new JavaMailException(
                        "Failed to delete folder", me);
        }

        if (!rslt)
            throw new MercuryException("Failed to delete folder");
    }

    /**
     * Get the number of unread messages in the Folder.
     *
     * @return Number of unread messages in the Folder.
     * @throws JavaMailException if getUnreadMessageCount() fails.
     */
    public int getUnreadCount() throws MercuryException {
        int rslt = -1;

        // EA: for POP3, getUnreadMessageCount() is effectively the same as
        // getMessages().length
        try {
            ensureOpen(Folder.READ_ONLY);
            rslt = folder.getUnreadMessageCount();
        } catch (MessagingException me) {
            throw new JavaMailException("getUnreadMessageCount() failed", me);
        }

        return rslt;
    }

    /**
     * Compare the semantic equivilency of two objects.
     * @param obj Object to compare against
     * @return Boolean affirmation that the parameter is semantically
     * equivilent to this object.
     */
    public boolean equals(Object obj) {
        boolean rslt = false;

        if (obj instanceof EmailFolder) {
            EmailFolder f = (EmailFolder)obj;

            // Two EmailFolders are equal to each other if:
            //  Their owners are equal
            //  Their Folder's full names are equal
            rslt = getOwner().equals(f.getOwner())
                   && this.getFolder().getFullName()
                       .equals(f.getFolder().getFullName());
        }

        return rslt;
    }

    /**
     * Get the Message with the specified id from this folder.
     *
     * <p>This will attempt to validate the message number selection using the
     * value of the Message's Message-Id header. If the header is not
     * available, this validation is skipped. If it is available, this will
     * throw an exception if the headers do not match for the provided message
     * number.</p>
     *
     * @param id Message id to resolve
     * @return Message associated with the given identifier.
     * @throws MercuryException if the message id could not be verifiably
     * resolved.
     */
    public IMessage getMessage(String id) throws MercuryException {
        assert id != null;

        if (id.equals("")) {
            throw new IllegalArgumentException("Argument 'id' cannot be empty.");
        }

        ensureOpen(Folder.READ_ONLY);

        int msgnum = 0;
        String tmp[] = id.split(":::", 3);
        EmailMessage rslt = null;

        if (tmp.length < 2) {
            throw new IllegalArgumentException(
                    "Argument 'id' is not a valid EmailMessageFactory "
                  + "message identifier: "+id);
        }

        if (!tmp[0].equals(folder.getFullName())) {
            throw new IllegalArgumentException(
                    "Argument 'id' corresponds to a different folder: "+id);
        }

        try {
            msgnum = Integer.parseInt(tmp[1]);
        } catch (Exception e) {
            throw new MercuryException(
                    "Failed to parse message id: "+id);
        }

        try {
            rslt = new EmailMessage(getOwner(), this, folder.getMessage(msgnum));
        } catch (Exception ex) {
            throw new MercuryException(
                    "The requested Message id could not be acquired: "+id, ex);
        }

        if (tmp.length == 3) {
            // Verify the Message-Id header; this checks for cases that the
            // underlying storage was modified between actions.
            if (!rslt.checkMessageId(tmp[2])) {
                // TODO: Try a manual step-through looking for a match? Wait
                // until the necessity of such a method is assessed.
                throw new MercuryException(
                        "The Message-Id header of the specified message did "
                      + "not match the provided Message-Id value.");
            }
        }

        if (rslt == null)
            throw new MercuryException(
                    "Message not found: "+id);

        return rslt;
    }

    /**
     * @see IFolder#search(IDecisionCollection,boolean)
     */
    public IMessage[] search(IDecisionCollection filters, boolean recurse)
                             throws MercuryException
    {
        assert filters != null : "Argument 'filters' cannot be null.";

        if (!getOwner().getFeatures().allowSubfolders())
            recurse = false;

        SearchTerm filter =
            ((EmailMessageFactory)getOwner()).convertFilters(filters);
        List rslt = search(filter, recurse);

        return (IMessage[])rslt.toArray(new IMessage[0]);
    }

    /*
     * Protected API.
     */

    /**
     * Retrieve the underlying JavaMail Folder.
     */
    protected Folder getFolder() {
        return folder;
    }

    /**
     * Search the folder for messages that match the specified filters.
     * @param filter Condition that must be satisfied for a message to be
     *               included in the result set.
     * @param recurse Boolean affirmation that this search should recurse into
     *                subfolders.
     * @return All messages contained in the folder (and its subfolders if
     *         recursive) that match the specified search filters.
     * @see #search(IDecisionCollection,boolean)
     */
    protected List search(SearchTerm filter, boolean recurse)
                          throws MercuryException
    {
        List rslt = new LinkedList();

        try {
            Message[] results = folder.search(filter);
            for (int i = 0; i < results.length; i++) {
                rslt.add(new EmailMessage(getOwner(), this, results[i]));
            }
        } catch (MessagingException me) {
            throw new JavaMailException(
                    "The JavaMail Search operation failed", me);
        }

        // EA: Features checked by caller.
        if (recurse) {
            IFolder[] sf = getSubfolders();
            for (int i = 0; i < sf.length; i++) {
                rslt.addAll(((EmailFolder)sf[i]).search(filter, recurse));
            }
        }

        return rslt;
    }

    /*
     * Implementation.
     */

    String getFullName() {
        return this.folder.getFullName();
    }

    boolean isOpen() {
        return folder.isOpen();
    }

    synchronized void close() throws MercuryException {
        if (folder.isOpen()) {
            try {
                folder.close(needexpunge);
            } catch (MessagingException me) {
                throw new MercuryException(
                        "Failed to close folder", me);
            }
            needexpunge = false;
        }
    }

    /**
     * Ensure that the underlying JavaMail Folder is open and in the desired
     * mode.
     *
     * <p>This will close and re-open the folder if the required mode differs
     * from the current mode.</p>
     *
     * @param mode Open mode required for the Folder.
     */
    synchronized void ensureOpen(int mode) throws MercuryException
    {
        // EA: POP3 must ALWAYS be open in READ_WRITE due to the
        // implementation's handling of message caching. Any time a close() is
        // called on the Sun POP3 implementation, the message cache is flushed,
        // so unless the cache is repopulated with the same object references
        // as the Message's the user already has, any attempts to set flags
        // will have no effect.
        if (mode != Folder.READ_WRITE &&
                ((EmailMessageFactory)getOwner()).getAccount()
                                                 .getStoreAccount()
                                                 .getProtocol()
                                                 .equals("pop3"))
            mode = Folder.READ_WRITE;
        try {
            if (!folder.isOpen()) {
                folder.open(mode);
            } else if (folder.getMode() != mode) {
                folder.close(false);
                folder.open(mode);
            }
        } catch (MessagingException me) {
            throw new JavaMailException("Unable to open folder", me);
        }
    }
}
