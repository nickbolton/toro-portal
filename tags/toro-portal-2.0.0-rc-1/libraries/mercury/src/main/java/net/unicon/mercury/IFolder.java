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

import net.unicon.penelope.IDecisionCollection;

/**
 * Abstraction of a folder that contains messages and subfolders.
 * <p>Folders contain messages (both sent and received).  Some message factory
 * implementations may allow users to add arbitrary subfolders to organize
 * their messages.</p>
 */
public interface IFolder extends IMercuryEntity {

    /**
     * Get the folder's identifier.
     * @return this folder's identifier.
     */
    String getIdString();

    /**
     * Get the folder's name.
     * @return this folder's name.
     */
    String getLabel();

    /**
     * Count the number of unread messages in this folder.
     * @return number of unread messages
     */
    int getUnreadCount() throws MercuryException;

    /**
     * Retrieve a list of all subfolders contained in the folder.
     * @return Array containing the subfolders of this folder.
     */
    IFolder[] getSubfolders() throws MercuryException;

    /**
     * Retrieve all messages contained in the folder.
     * @return Array containing the messages stored in the folder.
     */
    IMessage[] getMessages() throws MercuryException;

    /**
     * Get the Message with the specified id from this folder.
     * @param id Message identifier to resolve
     * @return IMessage associated with the given identifier.
     * @throws MercuryException if the message id could not be resolved.
     */
    IMessage getMessage(String id) throws MercuryException;

    /**
     * Return a reference to this folder's parent folder.
     * @return this folder's parent folder, or null if it is top-level
     */
    IFolder getParent() throws MercuryException;

    /**
     * Create a folder beneath this folder.
     * @param name Name of the folder to create.
     * @throws UnsupportedOperationException if folder creation is not supported
     */
    IFolder createSubfolder(String name) throws MercuryException;

    /**
     * Retrieve a subfolder of the current folder with a given label.
     * @param label Label of the folder to retrieve
     * @return IFolder representing the desired subfolder, or null if the
     *         requested folder does not exist.
     * @throws UnsupportedOperationException if folder operations are not supported
     * @see Features
     */
    IFolder getSubfolder(String label) throws MercuryException;

    /**
     * Delete this folder.
     * If <code>recurse</code> is true, then all subfolders and messages
     * contained within this folder will be deleted. If <code>recurse</code> is
     * false and the folder is not empty, an exception will be thrown and the
     * folder will remain in its original state.
     * @param recurse Recursive deletion flag
     * @throws MercuryException if this folder cannot be deleted
     * @throws UnsupportedOperationException if folder deletion is not supported
     */
    void deleteFolder(boolean recurse) throws MercuryException;

    /**
     * Add a message to the folder.
     * @param msg Message to add to the folder.
     */
    void addMessage(IMessage msg) throws MercuryException;

    /**
     * Mark the message in the folder as deleted.
     * @param msg Message to delete from the folder.
     * @return Affirmation of success.
     */
    boolean removeMessage(IMessage msg) throws MercuryException;

    /**
     * Expunge deleted messages from the Folder.
     * @throws MercuryException if the operation fails.
     */
    void expunge() throws MercuryException;

    /**
     * Search the folder for messages that match the specified filters.  The
     * decision collection must have been created against the choice collection
     * provided by the implementing factory's
     * {@link IMessageFactory#getSearchCriteria()} method.
     *
     * @param filters Conditions that must be satisfied for a message to be
     *                included in the result set.
     * @param recurse Boolean affirmation that this search should recurse into
     *                subfolders.
     * @return All messages contained in the folder (and its subfolders if
     *         recursive) that match the specified search filters.
     * @see IMessageFactory#getSearchCriteria()
     */
    IMessage[] search(IDecisionCollection filters, boolean recurse) throws MercuryException;
}
