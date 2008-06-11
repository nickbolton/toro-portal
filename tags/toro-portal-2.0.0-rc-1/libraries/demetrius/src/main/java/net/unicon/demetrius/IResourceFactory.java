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

package net.unicon.demetrius;

import java.io.InputStream;

import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

/**
 * @author ibiswas
 */
public interface IResourceFactory {

    /**
     * @return
     */
    public long getSizeLimit();
    
    public long getSizeLimit(IResource r);

    /**
     * Returns the root of the <code>IResourceFactory</code>.
     * @return
     *  The <code>IFolder</code> root the system initialized by the constructor.
     */
    public IFolder getRoot();

    /**
     * Creates a new <code>IFolder</code> under the specified parent.
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param parent
     *  The <code>IFolder</code> parent of the folder to be created.
     * @return
     *  The <code>IFolder</code> interface of the folder created in the system. Returns <code>null</code>
     *  if the folder was not created.
     * @throws DemetriusException
     */
    public IFolder createFolder(IDecisionCollection dc, IFolder parent) throws DemetriusException;

    /**
     * Uploads a new <code>IFile</code> under the specified parent folder.
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param parent
     *  The <code>IFolder</code> parent of the folder to be created.
     * @param f
     *  The <code>File</code> to be uploaded into the file system.
     * @return
     *  The <code>IFile</code> interface of the file uploaded in the system. Returns <code>null</code>
     *  if the folder was not created.
     * @throws DemetriusException
     */
    public IFile addFile(IDecisionCollection dc, IFolder parent, InputStream is) throws DemetriusException;

    /**
     * Updates the information of the given resource using the DecisionCollection
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param r
     * The <code>IResource</code> that will be updated
     * @throws DemetriusException
     */
    public void updateResource(IDecisionCollection dc, IResource r) throws DemetriusException;

    /**
     * returns the resource metadata for the given type of resource
     * @param type
     * <code>ResourceType</code> which tells which type of resource
     * @return <code>IChoiceCollection</code> of the resource metadata
     * @throws DemetriusException
     */
    public IChoiceCollection getResourceMetadata(ResourceType type) throws DemetriusException;

    /**
     * Returns the metadata for the resource.
     * @param r
     * <code>IResource</code> object whose metadata is required
     * @return <code>IDecisionCollection</code> metadata of the resource
     * @throws DemetriusException
     */
    public IDecisionCollection getMetadata(IResource r) throws DemetriusException;

    /**
     * Deletes the specified resource from the filesystem
     * @param r
     * <code>IResource</code> that needs to be deleted
     * @throws DemetriusException
     */
    public void delete(IResource r) throws DemetriusException;

    /**
     * Moves the given resource to the folder and checks if an existing duplicate resource is to be overwritten
     * @param r
     * <code>IResource</code> that will be moved
     * @param f
     * <code>IFolder</code> where the resource will be moved
     * @param overWrite
     * Determines whether the duplicate resource will be overwritten or not
     * @throws DemetriusException
     */
    public void move(IResource r, IFolder destinationFolder, boolean overWrite) throws DemetriusException;

    /**
     * Makes a copy of the resource and puts it in the destination folder. The function also checks if there is an existing duplicate resource and allows overwriting.
     * @param r
     * <code>IResource</code> that will be copied
     * @param f
     * <code>IFolder</code> where the resource will be copied to
     * @param overWrite
     * Determines whether the duplicate resource will be overwritten
     * @throws DemetriusException
     */
    public void copy(IResource r, IFolder destinationFolder, boolean overWrite) throws DemetriusException;

    /**
     * Sorts the resource array using the sort method
     * @param r
     * An array of <code>IResource</code>
     * @param sm
     * The <code>SortMethod</code> that will be used for sorting the resources
     * @return the sorted <code>IResource</code> array
     */
    //public IResource[] sortResources(IResource[] r, SortMethod sm);

    /**
     * Gets an IResource that matches the relative path given.
     * @param relativePath
     *  A String representing the relative path of the folder to find.
     * @return the <code>IResource</code> that corresponds to the given path.
     * @throws DemetriusException
     */
    public IResource getResource(String relativePath) throws DemetriusException;

    /**
     * Gets factory url.
     * @return the <code>String</code> url.
     */
    public String getUrl();

    /**
     * Indicates whether some factory object is equal to this one.
     * @param o
     *  The reference object with which to compare.
     * @return <code>true</code> if this factory has the same Url as the obj
     * argument; <code>false</code> otherwise.
     */
    public boolean equals(Object o);
    
    /**
     * Returns whether the given resource is availble to the caller.
     */
    public boolean isAvailable(IResource r);
}
