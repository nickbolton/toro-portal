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

package net.unicon.demetrius.fac.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeTextConfigurableLimit;
import net.unicon.penelope.store.jvm.JvmEntityStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author gtrujillo
 */
public class FsResourceFactory extends AbstractResourceFactory {
    
    private static final Log log = LogFactory.getLog(FsResourceFactory.class);
    private static final Integer MAX_FILE_AND_FOLDER_LENGTH = 
        Integer.valueOf(128);
    // Instance variables
    private static IChoiceCollection folderMetadata;
    private static IChoiceCollection fileMetadata;
    private final IFolder root;
    private final String dataSource;
    private final String url;

    /**
     * Class contstructor which calls the <code>init()</code> method
     * to initialize the metadata <code>ChoiceCollection</code>.
     * @param dataSource
     *  The <code>String</code> dataSource that denotes the path of the root 
     *  folder.
     */
    public FsResourceFactory(String dataSource, String rootName, long limit) {

        super(limit);

        // Assertions.
        if (dataSource == null) {
            String msg = "Argument 'dataSource' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rootName == null) {
            String msg = "Argument 'rootName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        this.dataSource = dataSource;
        // Gets the date for the root IFolder
        File rootDir = new File(dataSource);
        if (!rootDir.exists()) {
            String msg = "The specified path does not exist:  " + dataSource;
            throw new IllegalArgumentException(msg);
        }
        long dateLong = rootDir.lastModified();

        this.root = new FolderImpl (rootName, this, 0, new Date(dateLong), null,
                false);
        StringBuffer sb = new StringBuffer();
        sb.append("FSA://").append(this.getClass().getName()).append("/[");
        sb.append(rootName).append(":").append(limit).append("]")
          .append(DELIMITER);
        sb.append(dataSource);
        url = sb.toString();
        this.init();
    }

    public void init() throws IllegalStateException {

        IEntityStore store = new JvmEntityStore();
        folderMetadata = null;
        fileMetadata = null;

        // Create the choice collection for the folder metadata choices.
        try {

            // Folder Name ...
            List nOptions = new ArrayList();
            IOption name = store.createOption(
                    Handle.create("name"),
                    Label.create("Name:"),
                    TypeTextConfigurableLimit.createInstance(
                            MAX_FILE_AND_FOLDER_LENGTH)
                    );
            nOptions.add(name);

            IChoice c1 = store.createChoice(
                    Handle.create("foldername"),
                    Label.create("Folder Name:"),
                    (IOption[]) nOptions.toArray(new IOption[0]),
                    1,
                    1);

            // Folder Choice Collection...
            folderMetadata = store.createChoiceCollection(
                    Handle.create("folder"),
                    Label.create("Folder Information"),
                    new IChoice[] { c1 });

            // File Name ...
            List fnOptions = new ArrayList();
            IOption fname = store.createOption(
                    Handle.create("name"),
                    Label.create("Name:"),
                    TypeTextConfigurableLimit.createInstance(
                            MAX_FILE_AND_FOLDER_LENGTH)
                );
            fnOptions.add(fname);

            IChoice fc1 = store.createChoice(
                    Handle.create("filename"),
                    Label.create("File Name:"),
                    (IOption[]) fnOptions.toArray(new IOption[0]),
                    1,
                    1);

            // File Choice Collection...
            fileMetadata = store.createChoiceCollection(
                    Handle.create("file"),
                    Label.create("File Information"),
                    new IChoice[] { fc1 });

        } catch (EntityCreateException ece) {
            String msg = "FsResourceFactory failed to initialize properly.";
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Returns the root of the <code>FsResourceFactory</code>.
     * @return
     *  The <code>IFolder</code> root the system initialized by the constructor.
     */
    public IFolder getRoot(){
        return root;
    }

    /**
     * Creates a new <code>IFolder</code> under the specified parent.
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param parent
     *  The <code>IFolder</code> parent of the folder to be created.
     * @return
     *  The <code>IFolder</code> interface of the folder created in the system.
     *  Returns <code>null</code> if the folder was not created.
     */
    public IFolder createFolder(IDecisionCollection dc, IFolder parent) 
            throws DemetriusException {

        // Assertions.
        if (dc == null) {
            throw new IllegalArgumentException("Decision " +
                    "collection may not be null when creating a folder.");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent may not" +
                                            " be null when creating a folder.");
        }

        boolean folderCreated = false;
        IFolder folder = null;

        Date current = new Date(System.currentTimeMillis());

        folder = new FolderImpl(null, this, 0, current, parent, false);
        setMetadata(folder, dc);

        File ioFolder = new File(evaluateFsPath(dataSource, folder));
        folderCreated = ioFolder.mkdir();

        if (folderCreated) {
            return folder;
        } else {
            String msg = "Folder not created or already exists.";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Uploads a new <code>IFile</code> under the specified parent folder.
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param parent
     *  The <code>IFolder</code> parent of the folder to be created.
     * @param f
     *  The <code>InputStream</code> of the file to be uploaded into the
     *  file system.
     * @return
     *  The <code>IFile</code> interface of the file uploaded in the system.
     *  Throws an exception if the folder was not created.
     */
    public IFile addFile(IDecisionCollection dc, IFolder parent, InputStream is)
            throws DemetriusException {
        // assertions
        if (dc == null) {
            throw new IllegalArgumentException("Decision " +
                    "collection may not be null when uploading a file.");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent folder " +
                    "must be specified when uploading a file.");
        }
        if (is == null) {
            throw new IllegalArgumentException("Source Inputstream" +
                    " may not be null.");
        }

        IDecision dtemp = dc.getDecision(fileMetadata.getChoice(
                Handle.create("filename")));
        String name = null;
        
        if (dtemp != null) {      
            name = (String) dtemp.getSelections()[0].getComplement().getValue();
        } else {    
        	name = (String) dc.getDecisions()[0].getFirstSelectionValue();
    	}

        boolean fileCreated = false;
        File ioFile = new File(evaluateFsPath(dataSource, parent), name);

        long max = getSizeLimit();
        long size = root.getSize();
        int bytes = 0;
        try {
            fileCreated = ioFile.createNewFile();
            
            if (!fileCreated) {
                throw new IllegalArgumentException(
                        "File not created. File with same name already exists. ");
            }
            
            FileOutputStream fos = new FileOutputStream(ioFile);
            byte[] buffer = new byte[500];
            int bytes_read;

            // Reads the InputStream 500 bytes at a time while it checks if
            // adding file will put the factory over the max size limit.
            while ((bytes_read = is.read(buffer)) != -1
                    && !(max > 0 && (size + bytes > max))) {
                fos.write(buffer, 0, bytes_read);
                bytes += bytes_read;
            }

            if (is.read(buffer) != -1) {
                is.close();
                fos.close();
                ioFile.delete();
                throw new IllegalArgumentException("Adding the file '" + name
                        + "' will exceed maximum size: " + max);
            }

            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }

        } catch (IOException ioe) {
            throw new DemetriusException("File not created. ", ioe);
        }


        // creates a new IFile with the uploaded file's properties
        IFile file = new FileImpl(null, this, ioFile.length(),
                new Date(ioFile.lastModified()), parent, false, null);
        // uses an IDecisionCollection to set the metadata of the IFile
        setMetadata(file, dc);
        return file;

    }

    /**
     * Returns the contents of a given folder.
     * @param f
     *  The <code>IFolder</code> for which the contents will be returned.
     * @param rt
     *  The <code>ResourceType[]</code> denotes types of resources to return.
     * @return
     *  An array of <code>IResource</code> objects for the given folder.
     */
    public IResource[] getContents(IFolder f, ResourceType[] rt) 
            throws DemetriusException{

        // Assertions.
        if (f == null) {
            String msg = "Argument 'f [IFolder]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rt == null) {
            String msg = "Argument 'rt' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IResource[] contents = new IResource[0];
        File[] files = null;
        File[] fileTypes = null;
        File[] folderTypes = null;
        File dir = new File(evaluateFsPath(dataSource, f));

        for (int i = 0; i < rt.length; i++) {
            if (rt[i].equals(ResourceType.FOLDER)) {
                folderTypes = dir.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();}
                });
                if(folderTypes != null){
                    files = folderTypes;
                }else{
                    throw new DemetriusException(
                          "The abstract pathname does not denote a directory.");
                }
            }
            if (rt[i].equals(ResourceType.FILE)) {
                fileTypes = dir.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isFile();}
                });
                files = fileTypes;
            }
        }

        if (rt.length > 1) {
            files = new File[folderTypes.length + fileTypes.length];
            System.arraycopy(folderTypes, 0, files, 0, folderTypes.length);
            System.arraycopy(fileTypes, 0, files, folderTypes.length, 
                    fileTypes.length);
        }


        contents = new IResource[files.length];
        File curr = null;
        for (int i = 0; i <files.length; i++) {
            curr = files[i];
            if (curr.isDirectory()){
                IResource resource = new FolderImpl(curr.getName(),
                                                    this,
                                                    0,
                                                    new Date(curr.lastModified()),
                                                    f,
                                                    curr.isHidden());
                contents[i] = resource;

            } else {
                IResource resource = new FileImpl(curr.getName(),
                                                  this, curr.length(),
                                                  new Date(curr.lastModified()),
                                                  f,
                                                  curr.isHidden(),
                                                  null);
                contents[i] = resource;
            }
        }

        return contents;
    }

    /**
     * Returns the contents of a given folder.
     * @param f
     *  The <code>IFolder</code> for which the contents will be returned.
     * @return
     *  An array of <code>IResource</code> objects for the given folder.
     */
    public IResource[] getContents(IFolder f) {
        // assertions
        if (f == null) {
            throw new IllegalArgumentException("The given " +
                                                "resource must not be null.");
        }

        IResource[] contents = new IResource[0];
        File dir = new File(evaluateFsPath(dataSource, f));;

        File[] folderTypes = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();}
        });

        File[] fileTypes = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();}
        });

        File[] files = null;
        if (folderTypes != null && fileTypes != null) {
            files = new File[folderTypes.length + fileTypes.length];
            System.arraycopy(folderTypes, 0, files, 0, folderTypes.length);
            System.arraycopy(fileTypes, 0, files, folderTypes.length, 
                    fileTypes.length);
        } else if (fileTypes != null) {
            files = fileTypes;
        } else if (folderTypes != null) {
            files = folderTypes;
        }

        if (files != null) {
            contents = new IResource[files.length];
            File curr = null;
            for (int i = 0; i <files.length; i++) {
                curr = files[i];
                if (curr.isDirectory()){
                    IResource resource = new FolderImpl(curr.getName(),
                                                        this,
                                                        0,
                                                        new Date(curr.lastModified()),
                                                        f,
                                                        curr.isHidden());
                    contents[i] = resource;

                } else {
                    IResource resource = new FileImpl(curr.getName(),
                                                      this,
                                                      curr.length(),
                                                      new Date(curr.lastModified()),
                                                      f,
                                                      curr.isHidden(),
                                                      null);
                    contents[i] = resource;
                }
            }
        }

        return contents;
    }

    public String getDataSource() {
        return this.dataSource;
    }

    protected long getSize(IResource r) {

        // assertions
        if (r == null) {
            throw new IllegalArgumentException("The given resource" +
                                                " must not be null.");
        }

        if (r.getParent() == null) {
            return getSize(new File(dataSource));
        }
        return getSize(new File(evaluateFsPath(dataSource, r)));
    }

    private long getSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            File[] contents = f.listFiles();
            if (contents != null) {
                for (int i = 0; i < contents.length; i++) {
                    size += getSize(contents[i]);
                }
            }
        } else {
            size = f.length();
        }
        return size;
    }

    protected long getNumFiles(IFolder r) {

        // assertions
        if (r == null) {
            throw new IllegalArgumentException("The given " +
                                                "resource must not be null.");
        }

        File f = new File(evaluateFsPath(dataSource, r));;
        long numFiles = 0;

        File[] files = f.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();}
        });
        if (files != null) {
            numFiles = files.length;
        }
        return numFiles;
    }

    protected long getNumFolders(IFolder r) {

        // Assertions
        if (r == null) {
            throw new IllegalArgumentException("The given " +
                                                "resource must not be null.");
        }

        File f = new File(evaluateFsPath(dataSource, r));
        int numFolders = 0;

        File[] files = f.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();}
        });
        if (files != null) {
            numFolders = files.length;
        }
        return numFolders;
    }

    protected InputStream getInputStream(IFile f) throws DemetriusException {
        if(f == null){
            throw new IllegalArgumentException("The given " +
                                                "resource must not be null.");
        }

        InputStream fis = null;
        try{
            fis = new FileInputStream(new File(evaluateFsPath(dataSource, f)));

        }catch(FileNotFoundException fnfe){
            throw new DemetriusException("File not found. ", fnfe);
        }
        return fis;
    }

    public IChoiceCollection getResourceMetadata(ResourceType rt) 
            throws DemetriusException {
        IChoiceCollection cc = null;

        if (rt == ResourceType.FOLDER) {
            return folderMetadata;
        } else if (rt == ResourceType.FILE) {
            return fileMetadata;
        } else {
            throw new IllegalArgumentException(
                    "Resource metadata is file or folder specific.");
        }
    }

    public void updateResource(IDecisionCollection dc, IResource r)  
            throws DemetriusException{
        // Creates a File with the given resource's path
        File resource = new File(evaluateFsPath(dataSource, r));

        // assertions
        if (dc == null) {
            throw new IllegalArgumentException("Decision " +
                    "collection may not be null when updating a resource.");
        }
        if (resource.exists() == false) {
            throw new IllegalArgumentException("The given " +
                    "resource must exist in the file system to update.");
        }

        String fileName = r.getName();          // original file name

        // Sets the new name for the IResource
        setMetadata(r, dc);

        // check if file is not being renamed to the same name
        if(fileName.equalsIgnoreCase(r.getName())){
            return;
        }

        // check if the renamed resource exists on the file system
        File renamedResource = new File(evaluateFsPath(dataSource, r));

        if(renamedResource.exists()){
            throw new DemetriusException("Resource with new name " +
            "already exists.");
        }

        // Renames the File
        boolean renamed = resource.renameTo(
                new File(evaluateFsPath(dataSource, r)));

        if (!renamed) {
            throw new DemetriusException("The given " +
            "resource was not updated.");
        }

    }

    public void delete(IResource r) throws DemetriusException {
        boolean deleted = false;
        File resource = new File(evaluateFsPath(dataSource, r));

        // Assertions
        if (!resource.exists()) {
            throw new DemetriusException("The given resource " +
                                    "must exist in the file system to delete.");
        }

        // Delete the resource if it is a file
        if (resource.isFile()) {
            deleted = resource.delete();
        }

        // If the resource is a directory...
        if (resource.isDirectory()) {
            // get the contents of the directory
            IResource[] subresources = getContents((IFolder)r);

            // if it contains no subresources, delete the resource
            if (subresources.length == 0) {
                deleted = resource.delete();
            } else {
                // otherwise, call delete on each subresource
                for (int i = 0; i < subresources.length; i ++) {
                    delete(subresources[i]);
                }
                // when all of the subresources have been deleted,
                // delete the resource.
                deleted = resource.delete();
            }
        }

        if (deleted) {
            if (log.isInfoEnabled()) {
	            log.info("Resource  " + r.getName()
                                + " was successfully deleted.");
            }
        } else {
            throw new DemetriusException("Resource could not be deleted.");
        }

    }

    public IDecisionCollection getMetadata(IResource r) 
            throws DemetriusException {
        IChoiceCollection cc = null;
        IDecisionCollection dc = null;

        try {
            if (r.getType() == ResourceType.FILE) {
                cc = this.getResourceMetadata(ResourceType.FILE);
            } else {
                cc = this.getResourceMetadata(ResourceType.FOLDER);
            }
            IEntityStore store = cc.getOwner();
            List dec = new ArrayList();
            Iterator choices = Arrays.asList(cc.getChoices()).iterator();
            while (choices.hasNext()) {
                IChoice c = (IChoice) choices.next();
                List sel = new ArrayList();
                String handle = "name";
                if (handle != null && !handle.trim().equals("")) {
                    IOption o = c.getOption(Handle.create(handle));
                    IComplement p = o.getComplementType().parse(r.getName());
                    sel.add(store.createSelection(o, p));
                }
                ISelection[] selections = (ISelection[])sel.toArray(new ISelection[0]);
                dec.add(store.createDecision(null, c, selections));
            }
            IDecision[] decisions = (IDecision[]) dec.toArray(new IDecision[0]);
            dc = store.createDecisionCollection(cc, decisions);

        } catch (EntityCreateException ece) {
            ece.printStackTrace();
        }
        return dc;
    }

    public void move(IResource r, IFolder f, boolean overWrite) 
            throws DemetriusException {

        // Assertions
        if (r == null) {
            throw new IllegalArgumentException("Resource r may not "
                    + "be null when copying a resource.");
        }
        if (f == null) {
            throw new IllegalArgumentException("Destination folder f "
                    + "may not be null when copying a resource.");
        }

        // Checks to see if the resource and destination folder are in the same
        // Resource Factory
        if (r.getOwner().equals(f.getOwner())) {

            // Creates Files with the given resource's and destination folder's
            // paths
            File resource = new File(evaluateFsPath(dataSource, r));
            File folder = new File(evaluateFsPath(dataSource, f));

            // Assertions
            if (!resource.exists()){
                throw new DemetriusException(
                        "The given resource must exist in the file "
                                + "system to move the resource.");
            }
            if (!folder.exists()){
                throw new DemetriusException(
                        "The given destination folder must exist in the file "
                                + "system to move the resource.");
            }
            if (!overWrite && this.getResource(
                    f.getRelativePath(DELIMITER, true) + r.getName()) != null) {
                throw new DemetriusException(
                        "Resource not moved because it already exists.");
            }  else {
                StringBuffer filepath = new StringBuffer();
                filepath.append(folder.getPath());
                filepath.append(File.separator);
                filepath.append(resource.getName());
                File moved = new File(filepath.toString());

                // Renames the File
                boolean renamed = resource.renameTo(moved);
                if (renamed) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resource was moved to: "+ dataSource
                                + moved.getPath());
                    }
                }
                else {
                    throw new DemetriusException(
                            "Resource not moved because it already exists.");
                }
            }
        } else {
            // The Resource Factories are not the same.
            if (r.getType().equals(ResourceType.FILE)) {
                if (!overWrite && this.getResource(f.getRelativePath(DELIMITER,
                        true) + r.getName()) != null) {
                    
                    throw new DemetriusException(
                            "File not moved because it already exists.");
                }  else {
                    InputStream in = ((IFile)r).getInputStream();
                    IDecisionCollection dc = r.getOwner().getMetadata(r);
                    this.addFile(dc, f,  in);
                    r.getOwner().delete(r);
                }
            } else if (r.getType().equals(ResourceType.FOLDER)) {
                // Check for collision
                if (!overWrite && this.getResource(f.getRelativePath(DELIMITER,
                        true) + r.getName()) != null) {
                    
                    throw new DemetriusException(
                            "Folder not moved because it already exists.");
                }
                else {
                    IDecisionCollection dc = r.getOwner().getMetadata(r);
                    IFolder newParent = this.createFolder(dc, f);
                    IResource[] resources = ((IFolder)r).getContents();
                    for (int i = 0; i < resources.length; i++) {
                        this.copy(resources[i], newParent, true);
                    }
                    r.getOwner().delete(r);
                }
            }
        }
    }

    public void copy(IResource r, IFolder f, boolean overWrite) 
            throws DemetriusException {

        // Assertions
        if (r == null) {
            throw new IllegalArgumentException("Resource r may not " +
            "be null when copying a resource.");
        }
        if (f == null) {
            throw new IllegalArgumentException("Destination folder f " +
            "may not be null when copying a resource.");
        }

        // Checks to see if the resource and destination folder are in the same
        // Resource Factory
        if (r.getOwner().getUrl().equals(f.getOwner().getUrl())) {

            // Creates Files with the given resource's and destination folder's
            // paths
            File resource = new File(evaluateFsPath(dataSource, r));
            // destination folder
            File folder =  new File(evaluateFsPath(dataSource, f));

            // assertions
            if (!resource.exists() || !folder.exists()) {
                throw new DemetriusException("The given" +
                        " resource and destination folder must exist in the " +
                        "file system to copy the resource.");
            }
            if (resource.isFile()) {
                try {
                    if (!overWrite && this.getResource(
                            f.getRelativePath(DELIMITER, true) + 
                            r.getName()) != null) {
                        
                        throw new DemetriusException(
                                "File not copied because it already exists.");
                    }  else {
                        StringBuffer copypath = new StringBuffer();
                        copypath.append(folder.getPath());
                        copypath.append(File.separator);
                        copypath.append(resource.getName());
                        File copy = new File(copypath.toString());
                        FileInputStream fis = new FileInputStream(resource);
                        FileOutputStream fos = new FileOutputStream(copy);
                        byte[] buffer = new byte[500];
                        int bytes_read;
                        while((bytes_read = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytes_read);
                        }
                        if (fis != null) {
                            fis.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    }
                } catch (IOException ioe) {
                    throw new DemetriusException("File could not be copied. " ,
                            ioe);
                }
            }
            if (resource.isDirectory()) {
                if (!overWrite && this.getResource(f.getRelativePath(DELIMITER,
                        true) + r.getName()) != null) {
                    
                    throw new DemetriusException(
                            "Folder not copied because it already exists.");
                }
                else {
                    boolean folderCreated = false;
                    IFolder newfolder = new FolderImpl(r.getName(),
                                                       this,
                                                       0,
                                                       r.getDateModified(),
                                                       f,
                                                       false);
                    File newdir = new File(evaluateFsPath(dataSource, 
                            newfolder));
                    IResource[] subresources = getContents((IFolder)r);
                    folderCreated = newdir.mkdir();

                    if (folderCreated) {
                        // if there are subresources, call copy recursively
                        if (subresources.length > 0) {
                            for (int i = 0; i < subresources.length; i ++) {
                                copy(subresources[i], newfolder, overWrite);
                            }
                        }
                    } else {
                        throw new DemetriusException(
                                "There was a problem n copying the Folder.");
                    }
                }
            }
        } else {
            // The Resource Factories are not the same.
            if (r.getType().equals(ResourceType.FILE)) {
                if (!overWrite && this.getResource(f.getRelativePath(DELIMITER,
                        true) + r.getName()) != null) {
                    
                    throw new DemetriusException(
                            "File not copied because it already exists.");
                }  else {
                    InputStream in = ((IFile)r).getInputStream();
                    IDecisionCollection dc = r.getOwner().getMetadata(r);
                    this.addFile(dc, f,  in);
                }
            } else if (r.getType().equals(ResourceType.FOLDER)) {
                // Check for collision
                if (!overWrite && this.getResource(
                        f.getRelativePath(DELIMITER, true) + 
                        r.getName()) != null) {
                    
                    throw new DemetriusException(
                            "Folder not copied because it already exists.");
                }
                else {
                    IDecisionCollection dc = r.getOwner().getMetadata(r);
                    IResource[] resources = ((IFolder)r).getContents();
                    IFolder newParent = this.createFolder(dc, f);
                    for (int i = 0; i < resources.length; i++) {
                        this.copy(resources[i], newParent, true);
                    }

                }
            }
        }
    }

    public String getUrl() {
        return url;
    }

    private static String evaluateFsPath(String dataSource, IResource r) {

        // Assertions.
        if (dataSource == null) {
            String msg = "Argument 'dataSource' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (r == null) {
            String msg = "Argument 'r [IResource]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        StringBuffer rslt = new StringBuffer();
        IResource loop = r;
        while (loop.getParent() != null) {
            rslt.insert(0, loop.getName());
            rslt.insert(0, DELIMITER);
            loop = loop.getParent();
        }
        rslt.insert(0, dataSource);

        return rslt.toString();

    }

    public static IResourceFactory fromUrl(String facUrl) 
            throws DemetriusException {

        // Assertions.
        if(facUrl == null || facUrl.trim().equals("")){
            throw new IllegalArgumentException("The Factory"
                                    + " Url must not be null or empty.");
        }

        IResourceFactory rf = null;

        String rootVars = facUrl.substring(facUrl.lastIndexOf("[") + 1, facUrl
                .lastIndexOf("]"));
        String[] facValues = rootVars.split(":");
        String rootName = facValues[0];
        long limit = 0;
        if(facValues.length == 2){
            limit = Long.parseLong(facValues[1]);
        }
        String ds = facUrl.substring(facUrl.lastIndexOf("]/") + 2, facUrl
                .length());

        rf = new FsResourceFactory(ds.replaceAll("////", ""), rootName, limit);

        // Make sure a valid response was created.
        if (rf == null) {
            String msg = "Unable to construct a resource factory from the "
                                        + "specified url:  " + facUrl;
            throw new DemetriusException(msg);
        }

        return rf;
    }

}
