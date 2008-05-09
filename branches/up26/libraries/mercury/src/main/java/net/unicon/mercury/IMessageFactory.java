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

package net.unicon.mercury;

import java.io.InputStream;

import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

public interface IMessageFactory {

    /**
     * Retrieve the URL of the IMessageFactory.
     * @return URL of this message factory.
     */
    String getUrl();

    /**
     * Return a <code>Features</code> containing the factory's supported
     * featureset.
     * @return Features containing the factory's supported featureset.
     * @see Features
     */
    Features getFeatures();

    /**
     * Retrieve the user preferences.
     * @return User preferences.
     */
    IDecisionCollection[] getPreferences();

    /**
     * Retrieve the available recipient types for this MessageFactory.
     *
     * @return an array containing all allowable <code>IRecipientType</code>s
     *         for this <code>IMessageFactory</code>.
     */
    IRecipientType[] getRecipientTypes();

    /**
     * Provides the criteria which clients may use to serch the message factory
     * for messages.
     *
     * @return A choice collection that defines available search criteria.
     * @see #search(IFolder[],IDecisionCollection)
     */
    IChoiceCollection getSearchCriteria();

    /**
     * Searches the specified folder(s) for messages that match the specified
     * filters.  The decision collection must have been created against the
     * choice collection instance available through the
     * {@link #getSearchCriteria()} method.  This operation always performs a
     * shallow search (i.e. subfolders are not searched).
     *
     * @param where One or more folders to search for messages.
     * @param filters Conditions that must be satisfied for a message to be
     * included in the result set.
     * @return All messages contained in the specified folder(s) that match the
     * specified search constraints.
     * @see #getSearchCriteria()
     */
    IMessage[] search(IFolder[] where, IDecisionCollection filters) throws MercuryException;

    /**
     * Obtains the root folder for the message factory.
     * @return The root folder for the message factory.
     * @throws MercuryException if the default folder could not be retrieved.
     * @see IFolder
     */
    IFolder getRoot() throws MercuryException;

    /**
     * Obtain an instance of the specified folder.
     * @param id Identifier for the folder to acquire
     * @throws FolderNotFoundException if the named folder doesn't exist
     * @throws MercuryException on general error
     */
    IFolder getFolder(String id) throws FolderNotFoundException, MercuryException;

    /**
     * Create an attachment to be used within the Mercury component.
     * @param filename Filename (without path information)
     * @param type MIME Type of the attachment
     * @param stream InputStream containing the file's contents.
     * @return IAttachment representing the given data.
     * @see IAttachment
     */
    IAttachment createAttachment(String filename, String type,
                InputStream stream) throws MercuryException;

    /**
     * Send a message with no attachments.
     * @param recipients Message recipients.
     * @param subject Message subject.
     * @param body Message body.
     * @param priority Message priority.
     * @return Sender's copy of the sent message.
     * @throws MercuryException if any part of the process fails
     */
    IMessage sendMessage(IRecipient[] recipients, String subject, String body,
                         Priority priority) throws MercuryException;

    /**
     * Send a message.
     * @param recipients Message recipients.
     * @param subject Message subject.
     * @param body Message body.
     * @param attachments File attachments.
     * @param priority Message priority.
     * @return Sender's copy of the sent message.
     * @throws MercuryException if any part of the process fails
     */
    IMessage sendMessage(IRecipient[] recipients, String subject, String body,
                         IAttachment[] attachments, Priority priority)
                         throws MercuryException;

    /**
     * Send a message using a draft.
     * @param draft Draft message to finalize and send
     * @return Sender's copy of the sent message.
     * @see DraftMessage
     */
    IMessage sendMessage(DraftMessage draft) throws MercuryException;

    /**
     * Resolve a message identifier string into the corresponding IMessage
     * object.
     * @param id Message identifier to resolve
     * @return Message identified by <tt>id</tt>
     * @throws MercuryException if the id could not be resolved.
     */
    IMessage getMessage(String id) throws MercuryException;

    /**
     * Perform any necessary last-minute cleanup. This can be used by the
     * implementing class as a way to clean up any resources that were left
     * open.
     */
    void cleanup();
    
    /**
     * Returns the folder depending on the label.
     * The pre-defined special folders are in the SpecialFolder class.  
     * @param label String object label of the folder
     * @return IFolder with the given label.
     */
    IFolder getSpecialFolder(SpecialFolder sFolder) throws MercuryException;
    
    /**
     * Moves the message in the given folder.
     * @param msg	IMessage that will be moved
     * @param fromFolder folder from where the message exists
     * @param toFolder  Folder where the message will be moved
     * @throws MercuryException
     */
    void move(IMessage msg, IFolder fromFolder, IFolder toFolder) throws MercuryException;
    
    
}
