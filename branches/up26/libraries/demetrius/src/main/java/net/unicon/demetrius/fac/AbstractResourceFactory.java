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

package net.unicon.demetrius.fac;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.alchemist.MimeTypeMap;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.ResourceType;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;


/**
 * @author ibiswas and Glenda
 */

public abstract class AbstractResourceFactory implements IResourceFactory {

    private Map cache;
    private final long sizeLimit;

    protected static final Log LOG = LogFactory.getLog(AbstractResourceFactory.class);
    public static final String DELIMITER = "/";

    public static IResourceFactory fromUrl(String url) 
            throws DemetriusException{
        
        // Assertions.
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IResourceFactory rslt = null;
        try {

            String[] tokens = url.split("/");
            String className = tokens[2];
            Class c = Class.forName(className);
            Method m = c.getDeclaredMethod("fromUrl", new Class[] {
                                                String.class });
            rslt = (IResourceFactory) m.invoke(null, new Object[] { url });

        } catch (Throwable t) {
            String msg = "Unable to evaluate the specified entity:  " + url;
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

    public static IResource resourceFromUrl(String url) 
            throws DemetriusException {

        // Assertions.
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IResource rslt = null;
        try {            
            
            String[] tokens = url.split(DELIMITER, 4);
            String className = tokens[2];
            Class c = Class.forName(className);
            Method m = c.getDeclaredMethod("fromUrl", new Class[] {
                                                String.class });
            
            if (url.matches(".*\\(FSA://.*\\).*")) { // Shared resource url
                tokens = url.split("\\)////", 2);
                tokens[0] = tokens[0] + ")";                
            } else {  // Non-shared resource url
                tokens = url.split("////", 2);
            }            
            
            String facUrl = tokens[0];
            IResourceFactory fac = (IResourceFactory) m.invoke(null,
                                        new Object[] { facUrl });
            String path = "";
            if (tokens.length == 2) {
                path = tokens[1];
            }

            rslt = fac.getResource(path);

        } catch (Throwable t) {
            String msg = "Unable to evaluate the specified entity:  " + url;
            throw new RuntimeException(msg, t);
        }

        if(rslt != null){
            return rslt;
        }else{
            throw new DemetriusException(
                    "Resource with the given url was not found. " + url);
        }

    }

    /**
     * Sorts the resource array using the sort method
     * @param r
     * An array of <code>IResource</code>
     * @param sm
     * The <code>SortMethod</code> that will be used for sorting the resources
     * @return the sorted <code>IResource</code> array
     */
    public static IResource[] sortResources(IResource[] r, Comparator c){
        
        // separate the files and folders
        List files = new ArrayList();
        List folders = new ArrayList();
        for(int i = 0; i < r.length; i++){
            if(r[i].getType() == ResourceType.FOLDER){
                folders.add(r[i]);
            }else{
                files.add(r[i]);
            }

        }
        // sort the folders
        IResource[] resources = new IResource[0];
        resources = (IResource[])folders.toArray(resources);
        Arrays.sort(resources, c);
        System.arraycopy(resources, 0, r, 0, resources.length);
        int numFolders = resources.length;
        // sort the files
        resources = new IResource[0];
        resources = (IResource[])files.toArray(resources);
        Arrays.sort(resources, c);
        System.arraycopy(resources, 0, r, numFolders, resources.length);

        return r;
    }

    public AbstractResourceFactory(long limit) {
        this.sizeLimit = limit;
        cache = new WeakHashMap();

    }

    public abstract IFolder getRoot();

    public long getSizeLimit() {
        return this.sizeLimit;
    }
    
    public long getSizeLimit(IResource r) {
    	throw new UnsupportedOperationException(
    		"Getting size limit on resource not supported for this resource factory");
    }

    protected void addToCache(Integer seq, IResource r) {
        cache.put(seq, r);
    }

    protected IResource getFromCache(Integer key) {
        return (IResource)cache.get(key);
    }

    protected IResource removeFromCache(Integer key) {
        return (IResource)cache.remove(key);
    }

    protected abstract long getSize(IResource r);

    protected abstract long getNumFolders(IFolder f);

    protected abstract long getNumFiles(IFolder f);

    protected void setMetadata(IResource r, IDecisionCollection dc) 
            throws DemetriusException {
        if (dc == null) {
            throw new IllegalArgumentException(
                    "Decision collection may not be null.");
        }

        if (r.getType() == ResourceType.FOLDER) {
            IDecision dtemp = dc.getDecision("foldername");
            String stemp = 
                (String) dtemp.getSelections()[0].getComplement().getValue();
            ((AbstractResource)r).setName(stemp);
        } else if (r.getType() == ResourceType.FILE) {
            IDecision dtemp = dc.getDecision("filename");
            String stemp = 
                (String) dtemp.getSelections()[0].getComplement().getValue();
            ((AbstractResource)r).setName(stemp);
        }
    }

    public IResource getResource(String relativePath) throws DemetriusException {
        if (relativePath == null) {
            String msg = "Argument 'relativePath' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        if(relativePath.equals(DELIMITER) || relativePath.trim().equals("")) {
            return this.getRoot();
        }
        StringTokenizer tokenizer = new StringTokenizer(relativePath, DELIMITER);
        String[] path = new String[tokenizer.countTokens()];
        int j = 0;
        while (tokenizer.hasMoreTokens()) {
            path[j] = tokenizer.nextToken();
            j++;
        }

        return getResource(path, this.getRoot());
    }
    
    public boolean isAvailable(IResource r) {
    	return true;
    }

    protected IResource getResource(String[] relativePath, IFolder searchFrom) 
            throws DemetriusException {
        if(searchFrom == null){
            throw new IllegalArgumentException("getFolder: The folder to " +
                                               "search from must not be null.");
        }
        if(relativePath == null){
            throw new IllegalArgumentException("getFolder: The relative " +
                                "path for which to search must not be null.");
        }

        IResource[] contents = getContents(searchFrom);
        IResource result = null;
        if (contents != null) {
            for (int i = 0; i < contents.length; i++) {
                if (relativePath[0].equals(contents[i].getName())
                        && relativePath.length > 1) {
                    String[] newPath = new String[relativePath.length-1];
                    System.arraycopy(relativePath, 1, newPath, 0, 
                            newPath.length);
                    if (contents[i].getType().equals(ResourceType.FOLDER)){
                        return getResource(newPath, (IFolder)contents[i]);
                    }
                } else if (relativePath[0].equals(contents[i].getName())) {
                    return contents[i];
                }
            }
        } else {
            System.out.println("SearchFrom: " + searchFrom.getUrl());
            System.out.println("Contents null");
        }
        return result;
    }

    public boolean equals(Object o) {
        if(!(o instanceof IResourceFactory))
            return false;
        return this.getUrl().equals(((IResourceFactory)o).getUrl());
    }

    public int hashCode() {
        return this.getUrl().hashCode();
    }

    public abstract String getUrl();

    public abstract IFolder createFolder(IDecisionCollection dc, IFolder parent)
            throws DemetriusException;

    public abstract IFile addFile(IDecisionCollection dc, IFolder parent, 
            InputStream f) throws DemetriusException;

    protected abstract IResource[] getContents(IFolder f, ResourceType[] type) 
            throws DemetriusException;

    protected abstract IResource[] getContents(IFolder f) 
            throws DemetriusException;

    protected abstract InputStream getInputStream(IFile f) 
            throws DemetriusException;

    public abstract void delete(IResource r) throws DemetriusException;

    public abstract IChoiceCollection getResourceMetadata(ResourceType type) 
            throws DemetriusException;

    public abstract void move(IResource r, IFolder f, boolean overwrite) 
            throws DemetriusException;

    public abstract void copy(IResource r, IFolder f, boolean overwrite) 
            throws DemetriusException;

    protected static abstract class AbstractResource implements IResource {

        // Instance Members.
        private String name = null;
        private IResourceFactory owner = null;
        private long size = -1;
        private Date dateModified = null;
        private String mimeType = null;

        // derived member
        private String[] path = null;
        private IFolder parent = null;
        private boolean isHidden = false;

        public AbstractResource(String name, IFolder parent){
             // Assertions.
            if (name == null) {
                String msg = "Argument 'name' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (parent == null) {
                String msg = "Argument 'parent' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            this.name = name;
            this.parent = parent;
            this.path = parent.getPath(true);
        }


        public AbstractResource (String name,
                                 IResourceFactory owner,
                                 long size,
                                 Date dateModified,
                                 IFolder parent,
                                 boolean isHidden,
                                 String mimeType) {
            this.name   = name;
            this.owner  = owner;
            this.size   = size;
            this.dateModified = dateModified;
            this.parent = parent;
            this.isHidden = isHidden;
            this.mimeType = mimeType;

            if (parent == null) {
                this.path = new String[0];
            } else {
                this.path = new String[parent.getPath(true).length];
                System.arraycopy(parent.getPath(true),
                                 0,
                                 this.path,
                                 0,
                                 this.path.length);
            }
        }

        public String getName() {
            return name;
        }

        public IResourceFactory getOwner() {
            return owner;
        }

        public long getSize() {
            return ((AbstractResourceFactory)getOwner()).getSize(this);
        }

        public Date getDateModified() {
            return dateModified;
        }

        public boolean isHidden() {
            return isHidden;
        }

        public IFolder getParent() {
            return parent;
        }

        public abstract ResourceType getType();

        public String getUrl(){

            StringBuffer rslt = new StringBuffer();

            rslt.append(owner.getUrl()).append("////");

            StringBuffer endPart = new StringBuffer();
            IResource r = this;
            while (r != null && r != owner.getRoot()) {
                endPart.insert(0, r.getName());
                endPart.insert(0, DELIMITER);
                r = r.getParent();
            }

            return rslt.append(endPart).toString();

        }


        public String getPath(String delimiter, boolean includeResource) {
            // Assertions.
            if(delimiter == null){
                String msg = "Argument 'delimiter' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            StringBuffer strb = new StringBuffer();
            IResource temp = this;
            while(temp != null) {
                strb.insert(0, delimiter);
                strb.insert(0, temp.getName());
                temp = (IResource)temp.getParent();
            }
            return strb.toString();
        }

        public String getRelativePath(String delimiter, 
                boolean includeResource) {

            // Assertions.
            if(delimiter == null){
                String msg = "Argument 'delimiter' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            StringBuffer strb = new StringBuffer();
            IResource temp = this;
            while(temp.getParent() != null) {
                strb.insert(0, delimiter);
                strb.insert(0, temp.getName());
                temp = (IResource)temp.getParent();
            }
            return strb.toString();
        }


        public String[] getPath(boolean includeResource) {
            if(includeResource){
                String[] newPath = new String[path.length + 1];
                System.arraycopy(path, 0, newPath, 0, path.length);
                newPath[path.length] = this.getName();
                return newPath;
            } else {
                return path;
            }
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setHidden(boolean hidden) {
            this.isHidden = hidden;
        }
        public void setSize(long size) {
            this.size = size;
        }

        public String getMimeType() {
            // mimetype for the folder is always set to "Folder"
            // in the constructor.
            if (mimeType != null) {
                return mimeType;
            }
            // for file, get the extension of the file if it exists
            if(this.name.indexOf(".") > 0){
                this.mimeType = this.name.substring(this.name.lastIndexOf(".")
                                                    + 1);
                return mimeType;
            }
            // if the file extension does not exist, set it to "File"
            this.mimeType = "File";
            return mimeType;
        }
        
        public boolean isAvailable() {
        	return owner.isAvailable(this);
        }

        public boolean equals(Object o){
            if(!(o instanceof IResource)){
                return false;
            }

            return ((IResource)o).getUrl().equals(this.getUrl());
        }

        public int hashCode(){
            return this.getUrl().hashCode();
        }

        private String getContentType(String fileName){
            return MimeTypeMap.getContentType(fileName);
        }

    }

    protected static class FileImpl extends AbstractResource 
            implements IFile, Cloneable {

        private static final ResourceType type = ResourceType.FILE;
        private String contentType = null;

        public FileImpl(String name, IFolder parent, String mimeType){
            super(name, parent);
        }

        public FileImpl (String name,
                         IResourceFactory owner,
                         long size,
                         Date dateModified,
                         IFolder parent,
                         boolean isHidden,
                         String mimeType)
        {
            super(name, owner, size, dateModified, parent, isHidden, mimeType);
            this.contentType = super.getContentType(name);
        }

        public ResourceType getType(){
            return type;
        }

        /**
         *
         */
        public InputStream getInputStream() throws DemetriusException {
            return ((AbstractResourceFactory)this.getOwner())
                    .getInputStream(this);
        }

        public String getContentType(){
            return contentType;
        }

    }

    protected static class FolderImpl extends AbstractResource 
            implements IFolder {

        private static final ResourceType type = ResourceType.FOLDER;

        public FolderImpl(String name, IFolder parent){
            super(name, parent);
        }

        public FolderImpl (String name,
                           IResourceFactory owner,
                           long size,
                           Date dateModified,
                           IFolder parent,
                           boolean isHidden) {
            super(name, owner, size, dateModified, parent, isHidden, "Folder");
        }

        public ResourceType getType() {
            return type;
        }

        public long getNumFolders() {
            return ((AbstractResourceFactory)getOwner()).getNumFolders(this);
        }

        public long getNumFiles() {
            return ((AbstractResourceFactory)getOwner()).getNumFiles(this);
        }

        public IResource[] getContents(ResourceType[] type) 
                throws DemetriusException {
            return ((AbstractResourceFactory)this.getOwner()).getContents(this,
                    type);
        }

        public IResource[] getContents() throws DemetriusException {
            return ((AbstractResourceFactory)super.owner).getContents(this);
        }

        public String getUrl() {
            return super.getUrl();
        }

    }

}
