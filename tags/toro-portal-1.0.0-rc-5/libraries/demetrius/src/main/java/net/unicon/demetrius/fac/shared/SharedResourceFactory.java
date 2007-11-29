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

package net.unicon.demetrius.fac.shared;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.StringTokenizer;

import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

public class SharedResourceFactory extends AbstractResourceFactory {

	// Instance variables
	private IFolder root;
	private final IResourceFactory factory;
	private final String url;
	private final String rootName;
	
	/**
	 * public API
	 */
	public SharedResourceFactory(IFolder shared, String rootName) {
	    
	    // Call the super constructor with no size limit. Size limit will be
	    // enforced by the original factory.
	    super(0);
		factory = shared.getOwner();
		root = (IFolder)new SharedResource(shared, this, null);
		
		// Creates the Url
		String temp = shared.getUrl();
        
		StringBuffer sb = new StringBuffer();
		sb.append("FSA://").append(this.getClass().getName());		
		sb.append("/[").append(rootName).append("](");

        // '///' is the delimiter used to separate the factory from the
        // shared resource path.
        temp = temp.replaceFirst("////", "///");
		sb.append(temp);
        sb.append(')');
		url = sb.toString();
		this.rootName = shared.getName()+" ("+rootName+")";
	}

	public IFolder getRoot() {
		return root;
	}

	public IFolder createFolder(IDecisionCollection dc, IFolder parent) 
            throws DemetriusException { 
		return (IFolder)new SharedResource(factory.createFolder(dc, 
				(IFolder)((SharedResource)parent).getResource()), this, 
				(SharedResource)parent);
	}

	public IFile addFile(IDecisionCollection dc, IFolder parent, InputStream is)
            throws DemetriusException {
		return (IFile)new SharedResource(factory.addFile(dc, 
				(IFolder)((SharedResource)parent).getResource(), is),
				this, (SharedResource)parent);
	}

	public void updateResource(IDecisionCollection dc, IResource r) 
            throws DemetriusException {
		factory.updateResource(dc, ((SharedResource)r).getResource());
	}

	protected IResource[] getContents(IFolder f, ResourceType[] types) 
            throws DemetriusException {
		return f.getContents(types); 
	}
	
	protected IResource[] getContents(IFolder f) throws DemetriusException {
		return f.getContents(); 
	}

	protected InputStream getInputStream(IFile f) throws DemetriusException {
		return f.getInputStream();
	}

	public IChoiceCollection getResourceMetadata(ResourceType type) 
            throws DemetriusException {
		return factory.getResourceMetadata(type);
	}

	public IDecisionCollection getMetadata(IResource r) 
            throws DemetriusException {
		return factory.getMetadata(((SharedResource)r).getResource());
	}

	public void delete(IResource r) throws DemetriusException {
		if (!r.equals(root)) {
			factory.delete(((SharedResource)r).getResource());
		} else {
			throw new IllegalArgumentException("The ROOT may not be deleted.");
		}
	}

	public void move(IResource r, IFolder destinationFolder, boolean overWrite) 
            throws DemetriusException {
		if (!r.equals(root)) {
			factory.move(r, 
			(IFolder)((SharedResource)destinationFolder).getResource(), 
            overWrite);
		} else {
			throw new IllegalArgumentException("The ROOT may not be moved.");
		} 
	}

	public void copy(IResource r, IFolder destinationFolder, boolean overWrite) 
            throws DemetriusException {
		factory.copy(r,
			(IFolder)((SharedResource)destinationFolder).getResource(), 
            overWrite);

	}

	public IResource getResource(String relativePath) 
            throws DemetriusException {
		if(relativePath == null){
			throw new IllegalArgumentException(
                    "The relative path for which to search must not be null."); 
		}
		if(relativePath.equals("/") || relativePath.trim().equals("")) {
			return this.getRoot();
		}
		StringTokenizer tokenizer = new StringTokenizer(relativePath, "/");
		String[] path = new String[tokenizer.countTokens()];
		int j = 0;
		while (tokenizer.hasMoreTokens()) {
			path[j] = tokenizer.nextToken();
			j++;
		}
		return getResource(path, this.getRoot());
	}
	
	protected IResource getResource(String[] relativePath, IFolder searchFrom) 
            throws DemetriusException {
		if(searchFrom == null){
			throw new IllegalArgumentException(
                    "The folder to search from must not be null."); 
		}
		if(relativePath == null){
			throw new IllegalArgumentException(
                    "The relative path for which to search must not be null."); 
		}

		IResource[] contents = searchFrom.getContents();
		IResource result = null;
		for (int i = 0; i < contents.length; i++) {
			if (relativePath[0].equals(contents[i].getName()) 
					&& relativePath.length > 1) {
				String[] newPath = new String[relativePath.length-1];
				System.arraycopy(relativePath, 1, newPath, 0, newPath.length);
				if (contents[i].getType().equals(ResourceType.FOLDER)){
					result = getResource(newPath, (IFolder)contents[i]);
				}
			} else if (relativePath[0].equals(contents[i].getName())) {
				result = contents[i];
			}
		}
		return result;
	}

	protected long getSize(IResource r) {
		return r.getSize();
	}
	
	protected long getNumFiles(IFolder r) {
		return r.getNumFiles();
	}
	
	protected long getNumFolders(IFolder r) {
		return r.getNumFolders();
	}
	
	public String getUrl() {
		return url;
	}
	
	protected String getRootName(){
	    return this.rootName;
	}
	
	public static IResourceFactory fromUrl(String facUrl) 
            throws DemetriusException {
	    
        // Assertions.
        if(facUrl == null || facUrl.trim().equals("")){
            throw new IllegalArgumentException("The Factory"
                    				+ " Url must not be null or empty.");
        }

        IResourceFactory rf = null;
        IResourceFactory srf = null;     
        String rName = facUrl.substring(facUrl.indexOf("[") + 1, 
                facUrl.indexOf("]"));
        String subUrl = facUrl.substring(facUrl.indexOf("(") + 1, 
                facUrl.lastIndexOf(")"));
        
        String[] tokens = subUrl.split("///");
        String innerFacUrl = tokens[0];
        String sharedPath = "";
        if (tokens.length == 2) {
            sharedPath = tokens[1];
        }
        
        tokens = innerFacUrl.split("/", 4);
        Class[] args = {String.class};
        Object[] params = { innerFacUrl };
        try {
            Class c = Class.forName(tokens[2]);
            Method mthd = c.getDeclaredMethod("fromUrl", args);
            rf = (IResourceFactory)mthd.invoke(null, params);
        } catch (Exception e) {
            e.printStackTrace();
        } 

        // Make sure a valid response was created.
        if (rf == null) {
            String msg = "Unable to construct a resource factory from the "
                                        + "specified url:  " + subUrl;
            throw new RuntimeException(msg);
        } 
        
        IFolder folder = (IFolder)rf.getResource(sharedPath);
        
        if (folder != null) {
            // Construct the Shared Resource Factory
            srf = new SharedResourceFactory(folder, rName);
        } else {
            String msg = "Unable to construct a shared resource factory from " +
            		"the specified url:  " + facUrl;
            throw new RuntimeException(msg);
        }

        return srf;
	}
	
	public class SharedResource implements IResource, IFolder, IFile{
		private final IResource resource;
		private final IResourceFactory sharedFactory;
		private SharedResource sharedParent;
		
		public SharedResource (IResource r, 
							   IResourceFactory sharedOwner, 
							   SharedResource parent) {
			resource = r;
			sharedFactory = sharedOwner;
			sharedParent = parent;
		}
		
		public IFolder getParent() {
			return sharedParent;
		}

		public String getName() {
			if (sharedParent == null) {
				return getRootName();
			} else {
				return resource.getName();
			}
		}
		
		public IResourceFactory getOwner() {
			return sharedFactory;
		}
		
		public ResourceType getType() {
			return resource.getType();
		}
		
		public long getSize() {
			return resource.getSize();
		}

		public Date getDateModified(){
			return resource.getDateModified();
		}

		public boolean isHidden() {
			return resource.isHidden();
		}		

		public String getPath(String delimiter, boolean includeResource) {
			return getRelativePath(delimiter, includeResource);
		}
		
		public String getRelativePath(String delimiter, 
		        boolean includeResource) {
			StringBuffer strb = new StringBuffer();
			if(delimiter == null){
				delimiter = DELIMITER;
			}
			SharedResource temp = this;
			while(temp.getParent() != null) {
				strb.insert(0, temp.getName()+ delimiter);
				temp = (SharedResource)temp.getParent();
				
			}
			return strb.toString();
		}

		public String[] getPath(boolean includeResource) {
			return resource.getPath(includeResource);
		}

		public String getMimeType() {
			return resource.getMimeType();
		}

		public long getNumFolders() {
		    if(resource instanceof IFolder){
		        return ((IFolder)resource).getNumFolders();
		    }
		    return 0;
		}

		public long getNumFiles() {
		    if(resource instanceof IFolder){
		        return ((IFolder)resource).getNumFiles();
		    }
		    return 0;
		}
		
		public String getUrl() {
			StringBuffer sb = new StringBuffer();
			sb.append(sharedFactory.getUrl());
			sb.append("////");
			sb.append(getRelativePath("/", true));
			return sb.toString();
		}

		public IResource[] getContents(ResourceType[] type) 
                throws DemetriusException {
		    if(resource instanceof IFolder){
				IResource[] results = ((IFolder)resource).getContents(type);
				IResource[] sharedResults = new IResource[results.length];
				for (int i = 0; i < results.length; i++) {
					sharedResults[i] = 
                        (IResource)new SharedResource(results[i], 
													  sharedFactory,
													  this);
				}
				return sharedResults;
		    }
		    return new IResource[0]; 
		}

		public IResource[] getContents() throws DemetriusException {
		    if(resource instanceof IFolder){
				IResource[] results = ((IFolder)resource).getContents();
				IResource[] sharedResults = new IResource[results.length];
				for (int i = 0; i < results.length; i++) {
					sharedResults[i] = 
                        (IResource)new SharedResource(results[i],
						  		 					  sharedFactory,
													  this);
				}
				return sharedResults;
		    }
		    return new IResource[0]; 
		}

		public InputStream getInputStream() throws DemetriusException {
		    if(resource instanceof IFile){
		        return ((IFile)resource).getInputStream();
		    }
		    return null;
		}
		
		public IResource getResource() {
			return resource;
		}
		
		public String getContentType(){
			if(resource instanceof IFile)
				return ((IFile)resource).getContentType();
			else
				return null;
		}

		public boolean isAvailable() {
			return true;
		}
		
	}
}
