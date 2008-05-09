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

package net.unicon.demetrius.fac.rdbms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

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
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.complement.TypeTextConfigurableLimit;
import net.unicon.penelope.store.jvm.JvmEntityStore;

public final class RdbmsResourceFactory extends AbstractResourceFactory {

    //  Instance variables
    private static IChoiceCollection folderMetadata;
    private static IChoiceCollection fileMetadata;
    private static final Integer MAX_FILE_AND_FOLDER_LENGTH = 
        Integer.valueOf(128);
    private static final Integer MAX_DESCRIPTION_LENGTH = 
        Integer.valueOf(1024);
    // Dummy root directory for the factory
    private IRdbmsResource root = null;
    // this id is used to store the reference of the factory to the database and link
    // it to a specific user
    // The files and folders created by the factory are linked to the factory.
    private int id;
    // actual file system path where the files will be stored on the server, passed in
    // through the constructor
    private String path;
    private static DataSource dataSource = null;
    private String url = null;
    
    private static final String CREATE_ROOT_FOLDER_SQL =  "INSERT INTO " +
    		"FSA_FOLDER(FOLDER_ID, FOLDER_NAME, RESOURCEFACTORY_ID" +
    		", CREATE_DATE, MODIFY_DATE) VALUES(?, ?, ?, ?, ?) ";
    
    private static final String GET_ROOT_FOLDER_SQL = "SELECT DESCRIPTION" +
    		", CREATE_DATE FROM FSA_FOLDER WHERE FOLDER_ID = 0 " +
    		"AND RESOURCEFACTORY_ID = ?  ";
    
    private static final String GET_SUB_FOLDER_SQL = "SELECT FOLDER_ID" +
    		", FOLDER_NAME, DESCRIPTION, CREATE_DATE, MODIFY_DATE" +
    		", IS_HIDDEN FROM FSA_FOLDER WHERE PARENT_ID = ? " +
    		"AND RESOURCEFACTORY_ID = ? ORDER BY FOLDER_NAME";
    
    private static final String GET_FOLDER_SQL = "SELECT FOLDER_ID" +
    		", FOLDER_NAME, DESCRIPTION, CREATE_DATE, MODIFY_DATE" +
    		", IS_HIDDEN FROM FSA_FOLDER WHERE FOLDER_ID = ?";
    
    private static final String GET_SUB_FILE_SQL = "SELECT FILE_ID" +
    		", FILE_SIZE, FILE_NAME, DESCRIPTION, CREATE_DATE, IS_HIDDEN" +
    		", MIME_TYPE, MODIFY_DATE FROM FSA_FILE WHERE PARENT_ID = ? " +
    		"AND RESOURCEFACTORY_ID = ? ORDER BY FILE_NAME";
    
    private static final String GET_FILE_SQL = "SELECT FILE_ID, FILE_SIZE" +
    		", FILE_NAME, DESCRIPTION, CREATE_DATE, IS_HIDDEN, MIME_TYPE" +
    		", MODIFY_DATE FROM FSA_FILE WHERE FILE_ID = ?";
    
    private static final String CREATE_FILE_SQL = "INSERT INTO FSA_FILE" +
    		"(FILE_ID, RESOURCEFACTORY_ID, FILE_NAME, DESCRIPTION, MIME_TYPE" +
    		", FILE_SIZE, CREATE_DATE, MODIFY_DATE, PARENT_ID, IS_HIDDEN) " +
    		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CREATE_FOLDER_SQL = "INSERT INTO FSA_FOLDER" +
    		"(FOLDER_ID, FOLDER_NAME, DESCRIPTION, PARENT_ID, CREATE_DATE" +
    		", MODIFY_DATE, RESOURCEFACTORY_ID, IS_HIDDEN) " +
    		"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    
    private final String DELETE_FILE_SQL = "DELETE FROM FSA_FILE " +
    		"WHERE FILE_ID = ?";
    
    private static final String DELETE_FOLDER_SQL = "DELETE FROM FSA_FOLDER " +
    		"WHERE FOLDER_ID = ?";
    
    private static final String GET_FILE_SIZE_SQL = "SELECT SUM(FILE_SIZE) " +
    		"FROM FSA_FILE WHERE PARENT_ID = ? AND RESOURCEFACTORY_ID = ?";
    
    private static final String UPDATE_FILE_SQL = "UPDATE FSA_FILE " +
    		"SET FILE_NAME = ? , DESCRIPTION = ? , MODIFY_DATE = ? " +
    		"WHERE FILE_ID = ?";
    
    private static final String UPDATE_FOLDER_SQL = "UPDATE FSA_FOLDER " +
    		"SET FOLDER_NAME = ? , DESCRIPTION = ? , MODIFY_DATE = ? " +
    		"WHERE FOLDER_ID = ?";
    
    private static final String GET_RESOURCE_FAC = "SELECT FACTORY_ID" +
    		", ACTUAL_PATH, MAX_LIMIT FROM FSA_RESOURCEFACTORY " +
    		"WHERE FACTORY_ID = ?";
    
    private static final String GET_DUPLICATE_RESOURCES = "SELECT NAME" +
    		", ID, TYPE FROM (SELECT FOLDER_NAME AS NAME, FOLDER_ID AS ID" +
    		", 'FOLDER' AS TYPE FROM FSA_FOLDER WHERE PARENT_ID = ? " +
    		"AND RESOURCEFACTORY_ID = ? UNION SELECT FILE_NAME AS NAME" +
    		", FILE_ID AS ID, 'FILE' AS TYPE FROM FSA_FILE " +
    		"WHERE PARENT_ID = ? AND RESOURCEFACTORY_ID = ?) NAMES " +
    		"WHERE UPPER(NAME) = ? ";
    
     
    /**
     * Constructor
     * @param dataSource
     * The <code>DataSource</code> where the simulated files and folders
     * will be stored
     * @param id
     * An identifier given by the Factory creator
     * @param path
     * A <code>String</code> path on the fileSystem where the files will
     * be stored.
     * @param rootName
     * name of the root folder
     * @param limit
     * max size of the factory. The size is 0 for unlimited factory size.
     * @throws IllegalArgumentException
     */
    public RdbmsResourceFactory(int id
            , String path, String rootName, long limit) {

        super(limit);

        // assertions
        if(dataSource == null)
            throw new IllegalArgumentException("The DataSource has not been " +
            		"initialized. Initialize by calling the bootStrap() method " +
            		"on the factory. ");
         if(path == null)
            throw new IllegalArgumentException("Argument 'path' cannot be null");

        this.path = path;
        this.id = id;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            
            conn = getConnection();
            pstmt = conn.prepareStatement(GET_ROOT_FOLDER_SQL);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if(!rs.next()){
                Timestamp now = now();
                // create a root folder for the created factory
                pstmt = conn.prepareStatement(CREATE_ROOT_FOLDER_SQL);
                pstmt.setInt(1, 0);
                pstmt.setString(2, rootName);
                pstmt.setInt(3, id);
                pstmt.setTimestamp(4, now);
                pstmt.setTimestamp(5, now);
                pstmt.executeUpdate();
                
                // initialize the root folder
	            root = new RFolderImpl(0
	                    , rootName
	                    , null
	                    , null
	                    , now
	                    , now
	                    , false
	                    , this
	                    , 0);
            }else{
	            // initialize the root folder
	            root = new RFolderImpl(0
	                    , rootName
	                    , rs.getString("description")
	                    , null
	                    , rs.getDate("create_date")
	                    , rs.getDate("create_date")
	                    , false
	                    , this
	                    , 0);
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to initialize the " +
                    "root folder.";
            throw new RuntimeException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }

        addToCache(new Integer(root.getId()), root);
        init();
        StringBuffer urlSB = new StringBuffer("FSA://");
        urlSB.append(this.getClass().getName()).append("/[").append(rootName)
            .append("]/").append(this.id);
        this.url = urlSB.toString();
    }

    public static void bootStrap(DataSource ds){
        if(dataSource == null){
            dataSource = ds;
        }
    }


    /**
     * This function initailizes the metadata for the resources
     * @throws IllegalStateException
     */
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
                    1
                );

            // Folder Description ...
            List dOptions = new ArrayList();
            IOption description = store.createOption(
                    Handle.create("description"),
                    Label.create("Description:"),
                    TypeTextConfigurableLimit.createInstance(
                            MAX_DESCRIPTION_LENGTH)
                );
            dOptions.add(description);

            IChoice c2 = store.createChoice(
                    Handle.create("folderdescription"),
                    Label.create("Folder Description:"),
                    (IOption[]) dOptions.toArray(new IOption[0]),
                    0,
                    1
                );

            // Folder Hidden ...
            IOption isHidden = store.createOption(
                    Handle.create("hidden"),
                    Label.create("Hidden"),
                    TypeNone.INSTANCE
                );

            IChoice c3 = store.createChoice(
                    Handle.create("folderhidden"),
                    Label.create("Hidden:"),
                    new IOption[] { isHidden },
                    0,
                    1
                );

            // Folder Choice Collection...
            folderMetadata = store.createChoiceCollection(
                    Handle.create("folder"),
                    Label.create("Folder Information"),
                    new IChoice[] { c1, c2, c3 }
                );

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
                    1
                );

            // File Description ...
            List fdOptions = new ArrayList();
            IOption fdescription = store.createOption(
                    Handle.create("description"),
                    Label.create("Description:"),
                    TypeTextConfigurableLimit.createInstance(
                            MAX_DESCRIPTION_LENGTH)
                );
            fdOptions.add(fdescription);

            IChoice fc2 = store.createChoice(
                    Handle.create("filedescription"),
                    Label.create("File Description:"),
                    (IOption[]) fdOptions.toArray(new IOption[0]),
                    0,
                    1
                );

            // File Hidden ...
            IOption fHidden = store.createOption(
                    Handle.create("hidden"),
                    Label.create("Hidden"),
                    TypeNone.INSTANCE
                );

            IChoice fc3 = store.createChoice(
                    Handle.create("filehidden"),
                    Label.create("Hidden:"),
                    new IOption[] { fHidden },
                    0,
                    1
                );

            // File Choice Collection...
            fileMetadata = store.createChoiceCollection(
                    Handle.create("file"),
                    Label.create("File Information"),
                    new IChoice[] { fc1, fc2, fc3 }
                );

        } catch (EntityCreateException ece) {
            String msg = "RdbmsResourceFactory failed to initialize properly.";
            throw new IllegalStateException (msg);
        }
    }

    /**
     * Gets the root for this factory
     * @return <code>IFolder</code> root
     */
    public IFolder getRoot(){
        return (IFolder)root;
    }


    /**
     * Uploads a new <code>InputStream</code> under the specified parent folder.
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param parent
     *  The <code>IFolder</code> parent of the folder to be created.
     * @param f
     *  The <code>File</code> to be uploaded into the file system.
     * @return
     *  The <code>IFile</code> interface of the file uploaded in the system.
     * Returns <code>null</code>
     *  if the folder was not created.
     */
    public IFile addFile(IDecisionCollection dc, IFolder parent, InputStream fis) throws DemetriusException {
        // assertions
        if(dc == null){
            throw new IllegalArgumentException ("Argument DecisionCollection cannot be null. ");
        }
        if(parent == null){
            throw new IllegalArgumentException ("Argument Parent Folder cannot be null. ");
        }
        if(fis == null){
            throw new IllegalArgumentException ("Argument 'fis [InputStream]' cannot be null. ");
        }
        
        boolean fileCreated = false;

        // get a unique resource id for the new file
        int file_id = getUniqueResourceId();

        // create an IFile object. This object is needed to set the metadata from the
        // decisionCollection
        IRdbmsResource file = new RFileImpl(file_id, null, this, 0, now(),
                parent, false, null, null, now());
        setMetadata(file, dc);

        // if a resource with the same name already exists in the destination folder
        if(isDuplicate(parent, file.getName())){
            throw new IllegalArgumentException("Folder or File with same name already exists");
        }

        // copy the file contents from the source file to the destination file.
        OutputStream fos = null;
        File newFile = null;
        try{
            newFile = new File(new StringBuffer().append(path)
                    .append(DELIMITER)
                    .append(file_id)
                    .append(((RFileImpl)file).getExt()).toString());

            fileCreated = newFile.createNewFile();
            // if the new file could not be created
            if(!fileCreated){
                throw new IllegalArgumentException("Could not upload the file ");
            }
            
            int bytesRead = 0;
            int bytes = 0;
            byte[] contents = new byte[500];
            long max = getSizeLimit();
            long size = root.getSize();

            // open the new file for writing into it
            fos = new FileOutputStream(newFile);
            
            while((bytesRead = fis.read(contents)) != -1
                    && !(max > 0 && (size + bytes + bytesRead > max))) {
                fos.write(contents, 0, bytesRead);
                bytes += bytesRead;
            }
            
            if (fis.read(contents) != -1) {
                fis.close();
                fos.close();
                String name = file.getName(); 
                delete(file);
                throw new IllegalArgumentException("Adding the file '" + name
                        + "' will exceed maximum size: " + max);
            }
            ((RFileImpl)file).setSize(bytes);
        }catch(FileNotFoundException fnfe){
            throw new DemetriusException("File to upload could not be found ", fnfe);
        }
        catch(IOException ioe){
            throw new DemetriusException("Could not upload the file ", ioe);
        }
        finally{
            try{
                if(fis != null) fis.close();
            }catch(IOException ioe){
                String msg = "RdbmsResourceFactory was not able to upload the file successfully. ";
                throw new DemetriusException(msg, ioe);
            }

            try{
                if(fos != null) fos.close();
            }catch(IOException ioe){
                String msg = "RdbmsResourceFactory was not able to upload the file successfully. ";
                throw new DemetriusException(msg, ioe);
            }

        }

        // if the new file was created successfully, add the file information into the database.
        // add the created object into the cache
        addToCache(new Integer(file_id), file);
        Connection conn = null;
        PreparedStatement pstmt = null;
        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(CREATE_FILE_SQL);
            pstmt.setInt(1, file_id);
            pstmt.setInt(2, id);
            pstmt.setString(3, file.getName());
            pstmt.setString(4, file.getDescription());
            pstmt.setString(5, ((IFile)file).getMimeType());
            pstmt.setLong(6, file.getSize());
            pstmt.setTimestamp(7, new Timestamp(file.getDateUploaded().getTime()));
            pstmt.setTimestamp(8, new Timestamp(file.getDateModified().getTime()));
            pstmt.setInt(9, ((IRdbmsResource)parent).getId());
            pstmt.setBoolean(10, file.isHidden());
            pstmt.executeUpdate();

        }catch(SQLException se){
            // delete the saved file from the fileSystem
            newFile.delete();
            String msg = "RdbmsResourceFactory was not able to upload the file successfully. ";
            throw new DemetriusException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return (IFile)file;
    }

    /**
     * Creates a new <code>IFolder</code> under the specified parent.
     * @param dc
     *  An <code>IDecisionCollection</code> for the applicable metadata.
     * @param parent
     *  The <code>IFolder</code> parent of the folder to be created.
     * @return
     *  The <code>IFolder</code> interface of the folder created in the system.
     * Returns <code>null</code>
     *  if the folder was not created.
     */
    public IFolder createFolder(IDecisionCollection dc, IFolder parent) throws DemetriusException {

        // assertions
        if(dc == null){
            throw new IllegalArgumentException ("Argument DecisionCollection cannot be null. ");
        }
        if(parent == null){
            throw new IllegalArgumentException ("Argument Parent Folder cannot be null. ");
        }

        int folder_id = getUniqueResourceId();
        IRdbmsResource folder = new RFolderImpl(folder_id, null, null, parent, now(), now(),
                false, this, 0);
        setMetadata(folder, dc);

        if(isDuplicate(parent, folder.getName())){
            throw new IllegalArgumentException("Folder or File with same name already exists");

        }

        // if the resource with same name does not exist, create the folder and add to cache.
        addToCache(new Integer(folder_id), folder);

        // add the folder information into the database
        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement(CREATE_FOLDER_SQL);
            pstmt.setInt(1, folder.getId());
            pstmt.setString(2, folder.getName());
            pstmt.setString(3, folder.getDescription());
            pstmt.setInt(4, ((IRdbmsResource)parent).getId());
            pstmt.setTimestamp(5, new Timestamp(folder.getDateUploaded().getTime()));
            pstmt.setTimestamp(6, new Timestamp(folder.getDateModified().getTime()));
            pstmt.setInt(7, id);
            pstmt.setBoolean(8, folder.isHidden());
            pstmt.executeUpdate();

        }
        catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to create the " +
                    "folder successfully. ";
            throw new DemetriusException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }

        return (IFolder)folder;
    }

    /**
     * Returns the contents of a given folder.
     * @param f
     *  The <code>IFolder</code> for which the contents will be returned.
     * @param type
     *  The <code>ResourceType</code> denotes which types of resources to return.
     * @return
     *  An array of <code>IResource</code> objects for the given folder.
     */
    protected IResource[] getContents(IFolder f, ResourceType[] type) throws DemetriusException {

        //assertions
        if(f == null){
            throw new IllegalArgumentException("Argument 'f' cannot be null");
        }
        if(type == null){
            throw new IllegalArgumentException("Argument 'type' cannot be null");
        }

        IResource[] rslt = new IResource[0];
        List contents  = new ArrayList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int index = 0;
        int parentFolderSize = 0;
        conn = getConnection();

        try{
            for(int i = 0; i < type.length; i++){
                // get all the folders
                if(ResourceType.FOLDER.equals(type[i])){
                    pstmt = conn.prepareStatement(GET_SUB_FOLDER_SQL);
                    pstmt.setInt(1, ((IRdbmsResource)f).getId());
                    pstmt.setInt(2, id);

                    rs = pstmt.executeQuery();

                    while(rs.next()){
                        // check if the resource is in the cache
                        IResource resource = getFromCache(new Integer(rs.getInt("folder_id")));
                        if(resource == null){
                            // if resource is not in the cache, create the folder objectand
                            // add to the cache.
                            resource = new RFolderImpl(rs.getInt("folder_id")
                                    , rs.getString("folder_name")
                                    , rs.getString("description")
                                    , f
                                    , rs.getTimestamp("create_Date")
                                    , rs.getTimestamp("modify_Date")
                                    , rs.getBoolean("is_hidden")
                                    , this, 0);
                            ((AbstractResource)resource).setSize(this.getSize(rs.getInt("folder_id")));
                            addToCache(new Integer(rs.getInt("folder_id")), resource);
                        } // end if
                        // add the folder object to return
                        contents.add(resource);
                    } // end while
                }// end if
                // get all the files
                if(ResourceType.FILE.equals(type[i])){
                    if(conn == null){
                        conn = getConnection();
                    }
                    index = 0;
                    
                    pstmt = conn.prepareStatement(GET_SUB_FILE_SQL);
                    pstmt.setInt(1, ((IRdbmsResource)f).getId());
                    pstmt.setInt(2, id);
                    rs = pstmt.executeQuery();

                    while(rs.next()){
                        // check if the resource is in the cache
                        IResource resource = getFromCache(
                                new Integer(rs.getInt("file_id")));
                        // if resource is not in the cache, create the folder
                        // object and add to the cache.
                        if(resource == null){
                            resource = new RFileImpl(rs.getInt("file_id")
                                    , rs.getString("file_name")
                                    , this
                                    , rs.getLong("file_size")
                                    , rs.getTimestamp("modify_Date")
                                    , f
                                    , rs.getBoolean("is_hidden")
                                    , rs.getString("mime_type")
                                    , rs.getString("description")
                                    , rs.getTimestamp("create_Date"));
                            addToCache(new Integer(rs.getInt("file_id"))
                                    , resource);
                        } //end if
                        // add the file object to return
                        contents.add(resource);
                    } // end while
                }// end if
            }// end for
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get the " +
                    "contents of the given folder.";
            throw new DemetriusException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        if(contents.size() > 0){
            rslt = (IResource[])contents.toArray(new IResource[0]);
        }
        // if the folder does not have any contents or the factory does not exist
        return rslt;
    }

    protected IResource[] getContents(IFolder f) throws DemetriusException {
        return getContents(f, ResourceType.getAllInstances());
    }

    /**
     * Returns the file contents for the given file as an input stream
     * @param f
     * An <code>IFile</code> object whose contents are needed
     * @return <code>InputStream</code> of the file contents
     * @throws DemetriusException
     */
    protected InputStream getInputStream(IFile f) throws DemetriusException {
        if(f == null){
            throw new IllegalArgumentException("Argument IFile cannot be null");
        }

        InputStream fis = null;
        try{
            fis = new FileInputStream(new File(new StringBuffer().append(path)
                    .append(DELIMITER)
                    .append(((IRdbmsResource)f).getId())
                    .append(((RFileImpl)f).getExt()).toString()));
        }catch(FileNotFoundException fnfe){
            throw new DemetriusException("The file was not found.", fnfe);
        }
        return fis;
    }

    /**
     * Returns the metadata for the resource.
     * @param r
     * <code>IResource</code> object whose metadata is required
     * @return <code>IDecisionCollection</code> metadata of the resource
     */
    public IDecisionCollection getMetadata(IResource r){

        // assertions
        if(r == null){
            throw new IllegalArgumentException("Argument IResource cannot be null ");
        }

        IChoiceCollection cc = null;
        IDecisionCollection dc = null;
        if (r.getType().equals(ResourceType.FILE)) {
            cc = this.getResourceMetadata(ResourceType.FILE);
        } else {
            cc = this.getResourceMetadata(ResourceType.FOLDER);
        }
        String[] values = new String[3];
        // initialize values from the resource object
        values[0] = r.getName();
        values[1] = ((IRdbmsResource)r).getDescription();
        values[2] = "visible";
        if(r.isHidden()){
            values[2] = "hidden";
        }
        String[] optionLabels = new String[] { "name", "description", "hidden"};
        String[] choiceLabels = new String[] { "filename", "filedescription", "filehidden"};
        //int i = 0;

        try {

            IEntityStore store = cc.getOwner();

            List dec = new ArrayList();
            String handle= null;
            for (int j = 0; j < choiceLabels.length; j++) {
                IChoice c = cc.getChoice(Handle.create(choiceLabels[j]));
                List sel = new ArrayList();
                handle = optionLabels[j];
                if (handle != null && !handle.trim().equals("")) {
                    IOption o = c.getOption(Handle.create(handle));
                    IComplement p = null;
                    if(handle.equals(optionLabels[2])){
                        p = o.getComplementType().parse(null);
                    }
                    else{
                        p = o.getComplementType().parse(values[j]);
                    }
                    sel.add(store.createSelection(o, p));
                }

                ISelection[] selections =
                    (ISelection[]) sel.toArray(new ISelection[0]);
                dec.add(store.createDecision(null, c, selections));                
            }
            IDecision[] decisions = (IDecision[]) dec.toArray(new IDecision[0]);
            dc = store.createDecisionCollection(cc, decisions);

        } catch (EntityCreateException ece) {
        	throw new RuntimeException("getMetadata " + ece.getMessage() , ece);
        }
        return dc;
    }

    /**
     * returns the resource metadata for the given type of resource
     * @param type
     * <code>ResourceType</code> which tells which type of resource
     * @return <code>IChoiceCollection</code> of the resource metadata
     */
    public IChoiceCollection getResourceMetadata(ResourceType type) {

        //assertions
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' cannot be null.");
        }

        IChoiceCollection cc = null;
        if (type.equals(ResourceType.FOLDER)) {
            if (folderMetadata == null) {
                init();
            }
            cc = folderMetadata;
        } else {
            if (fileMetadata == null) {
                init();
            }
            cc = fileMetadata;
        }

        // returns the choice collection based on the type provided
        return cc;
    }

    /**
     * Deletes the specified resource from the filesystem
     * @param r
     * <code>IResource</code> that needs to be deleted
     * @throws DemetriusException
     */
    public void delete(IResource r) throws DemetriusException{

        // assertions
        if (r == null) {
            throw new IllegalArgumentException("Arguement resource cannot be null");
        }

        // remove the object from the cache
        removeFromCache(new Integer(((IRdbmsResource)r).getId()));
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        // For files
        if(r.getType().equals(ResourceType.FILE)){

            // delete from FS
            File file = new File(new StringBuffer().append(path)
                    .append(DELIMITER)
                    .append(((IRdbmsResource)r).getId())
                    .append(((RFileImpl)r).getExt()).toString());

            boolean fileDeleted = file.delete();
            if(!fileDeleted){
                throw new IllegalArgumentException("File could not be deleted from the storage");
            }

            //   delete file from database
            
            try{
                conn = getConnection();
                pstmt = conn.prepareStatement(DELETE_FILE_SQL);
                pstmt.setInt(1, ((IRdbmsResource)r).getId());
                pstmt.executeUpdate();
            }
            catch(SQLException se){
                String msg = "RdbmsResourceFactory was not able to delete the given file.";
                throw new DemetriusException(msg, se);
            }
            finally{
                if (pstmt != null) closeStatement(pstmt);
                if (conn != null) closeConnection(conn);
            }
        }else{
            // resource to delete is a folder
            conn = getConnection();
            try{
                // delete all the files under the parent folder
                IResource[] resources =
                    getContents((IFolder)r, new ResourceType[]{ResourceType.FILE});
                for(int i = 0; i < resources.length; i++){
                    delete(resources[i]);
                }

                // delete all the folders under the parent folder
                resources =
                    getContents((IFolder)r, new ResourceType[]{ResourceType.FOLDER});
                for(int i = 0; i < resources.length; i++){
                    delete(resources[i]);
                }
                // delete the given folder                
                pstmt = conn.prepareStatement(DELETE_FOLDER_SQL);
                pstmt.setInt(1, ((IRdbmsResource)r).getId());
                pstmt.executeUpdate();
            }
            catch(SQLException se){
                String msg = "RdbmsResourceFactory was not able to delete the " +
                        "contents of the given folder.";
                throw new DemetriusException(msg, se);
            }
            finally{
                if (pstmt != null) closeStatement(pstmt);
                if (conn != null) closeConnection(conn);
            }
        }
    }

    /**
     * Updates the information of the given resource using the DecisionCollection
     * @param dc DecisionCollection object that will be used to gt ethe resource information from
     * @param r resource that will be updated
     */
    public void updateResource(IDecisionCollection dc, IResource r) throws DemetriusException {
        if(dc == null){
            throw new IllegalArgumentException("Argument dc cannot be null");
        }
        if(r == null){
            throw new IllegalArgumentException("Argument Resource cannot be null");
        }
        String fileName = r.getName();
        setMetadata(r, dc);
        // don't do anything if the file name has not been modified.
        if(fileName.equalsIgnoreCase(r.getName())){
            return;
        }
        // if a resource with the same name exists
        if(isDuplicate(r.getParent(), r.getName())){
            throw new IllegalArgumentException("File or folder with same name already exists " +
                    "in the system");
        }

        // update the resource information in the database
        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection();
            
            if(r.getType().equals(ResourceType.FILE)){
                pstmt = conn.prepareStatement(UPDATE_FILE_SQL);
            }else{
                pstmt = conn.prepareStatement(UPDATE_FOLDER_SQL);
            }
            pstmt.setString(1, r.getName());
            pstmt.setString(2, ((IRdbmsResource)r).getDescription());
            pstmt.setTimestamp(3, now());
            pstmt.setInt(4, ((IRdbmsResource)r).getId());
            pstmt.executeUpdate();
        }

        catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to update the " +
                    "given resource.";
            throw new DemetriusException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        
        // update the cache
        addToCache(new Integer(((IRdbmsResource)r).getId()), r);      
    }

    /**
     * Sets the object variables by getting information from the DecisionCollection
     * @param r Resource that needs to be updated
     * @param dc IDecisionCollection object that contains information about the resource
     */
    protected void setMetadata(IResource r, IDecisionCollection dc) throws DemetriusException {
        if (dc == null) {
            throw new IllegalArgumentException("Decision collection may not be null.");
        }
        if (r == null) {
            throw new IllegalArgumentException("Argument Resource cannot be null");
        }

        // call the parent method
        super.setMetadata(r, dc);

        if(r.getType().equals(ResourceType.FOLDER)){
            IDecision dtemp = null;
            boolean hdn = false;    // default...
            try{
                dtemp = dc.getDecision(getResourceMetadata(ResourceType.FOLDER)
                    .getChoice(Handle.create("folderhidden")));
                if (dtemp.getSelections().length > 0) {
                    hdn = dtemp.getSelections()[0].getOption().getHandle()
                    .getValue().equals("hidden");
                }
            }catch(Exception e){
                // could not find the decision in the decisionCollection
                // will be using the default value for folderHidden
            }
            ((AbstractResource)r).setHidden(hdn);

            String description = null;
            try{
                dtemp = dc.getDecision(getResourceMetadata(ResourceType.FOLDER)
                    .getChoice(Handle.create("folderdescription")));
                if (dtemp.getSelections().length > 0) {
                    description = (String) dtemp.getSelections()[0].getComplement().getValue();
                }
            }catch(Exception e){
                // could not find the decision in the decisionCollection
                // will be using the default value for folderDescription
            }
            ((IRdbmsResource)r).setDescription(description);
        }
        else{
            boolean hdn = false;    // default...
            IDecision dtemp = null;
            try{
                dtemp = dc.getDecision(getResourceMetadata(ResourceType.FILE)
                    .getChoice(Handle.create("filehidden")));
                if (dtemp.getSelections().length > 0) {
                    hdn = dtemp.getSelections()[0].getOption().getHandle().getValue().equals("hidden");
                }
            }catch(Exception e){
                // could not find the decision in the decisionCollection
                // will be using the default value for filehidden
            }
            ((AbstractResource)r).setHidden(hdn);

            String description = null;
            try{
                dtemp = dc.getDecision(getResourceMetadata(ResourceType.FILE)
                    .getChoice(Handle.create("filedescription")));
                if (dtemp.getSelections().length > 0) {
                    description = (String) dtemp.getSelections()[0].getComplement().getValue();
                }
            }catch(Exception e){
                // could not find the decision in the decisionCollection
                // will be using the default value for filedescription
            }
            ((IRdbmsResource)r).setDescription(description);
        }
        
        //      update the cache
        addToCache(new Integer(((IRdbmsResource)r).getId()), r);
    }
    
    /**
     * Returns an instance of the <code>RdbmsResourceFactory</code> from the database
     * @param ds <code>Datasource</code> to the database
     * @param factory_id the id of the factory whose instnace needs to be retrieved
     * @return an instance of <code>IResourceFactory</code>.
     * throws an exception if the factory with the given id was not found.
     */
     public static IResourceFactory getInstance(int factory_id, String rootName) throws DemetriusException {

         IResourceFactory rf = null;
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;

         try{
             conn = dataSource.getConnection();
             
             pstmt = conn.prepareStatement(GET_RESOURCE_FAC);
             pstmt.setInt(1, factory_id);
             rs = pstmt.executeQuery();
             if(rs.next()){
                 rf = new RdbmsResourceFactory(rs.getInt("factory_id")
                         , rs.getString("actual_path"), rootName, rs.getLong("max_limit"));
             }else{
                 throw new IllegalArgumentException("No resourceFactory found " +
                         "with the given Factory Id");
             }
         }
         catch(SQLException se){
             String msg = "FactoryCreator was not able to get the Factory instance.";
             throw new DemetriusException(msg, se);
         }finally{
             if (pstmt != null) closeStatement(pstmt);
             if (conn != null) closeConnection(conn);
         }

         return rf;

     }

    // gets a connecton to the given dataSource
    private Connection getConnection(){
        try{
            return dataSource.getConnection();
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get a connection.";
            throw new RuntimeException(msg, se);
        }
    }

    // gets the current date
    private Timestamp now(){
        return new Timestamp(System.currentTimeMillis());
    }

    // get the size of the resource
    protected long getSize(IResource r) {
        try{
	        if(r.getType().equals(ResourceType.FOLDER)){
	            return getSize(((IRdbmsResource)r).getId());
	        }else{
	            return getFileSize((IFile)r);
	        }
        }catch(Exception e){
            throw new RuntimeException("Error in getting the resource size. ", e);
        }
    }

    private long getFileSize(IFile f){
        File file = new File(new StringBuffer().append(path)
                .append(DELIMITER)
                .append(((RFileImpl)f).getId())
                .append(((RFileImpl)f).getExt()).toString());
        return file.length();
    }

    // Entry point for the recursive function, to share Connection objects
    private long getSize(int folderId) throws DemetriusException{
        long rslt;
        Connection conn = getConnection();
        try {
            rslt = getSize(folderId, conn);
        } finally {
            if (conn != null) closeConnection(conn);
        }
        return rslt;
    }

    // recursive function that gets the size of the resource tree
    private long getSize(int folderId, Connection conn) throws DemetriusException {
        int size = 0;
        // count the size of all the files        
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            pstmt = conn.prepareStatement(GET_FILE_SIZE_SQL);
            pstmt.setInt(1, folderId);
            pstmt.setInt(2, id);
            rs = pstmt.executeQuery();

            if(rs.next()){
                size += rs.getInt(1);
            }
            if(pstmt != null) closeStatement(pstmt);

            // get the size for all the subfolders
            pstmt = conn.prepareStatement(GET_SUB_FOLDER_SQL);
            pstmt.setInt(1, folderId);
            pstmt.setInt(2, id);
            rs = pstmt.executeQuery();

            while(rs.next()){
                size += getSize(rs.getInt("folder_id"), conn);
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get the size of the given folder.";
            throw new DemetriusException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
        }

        return size;
    }

    // function to check if a resource with the same name already exists in the parent folder
    private boolean isDuplicate(IFolder r, String name) throws DemetriusException{

        // assertions
        if(r == null){
            throw new IllegalArgumentException("Argument 'r' cannot be null");
        }
        if(name == null){
            throw new IllegalArgumentException("Argument 'name' cannot be null");
        }

        Connection conn = getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean rslt = false;

        try{
            pstmt = conn.prepareStatement(GET_DUPLICATE_RESOURCES);
            pstmt.setInt(1, ((IRdbmsResource)r).getId());
            pstmt.setInt(2, id);
            pstmt.setInt(3, ((IRdbmsResource)r).getId());
            pstmt.setInt(4, id);
            pstmt.setString(5, name.toUpperCase());
            rs = pstmt.executeQuery();

            if(rs.next()){
                rslt = true;
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to check for duplicate resources.";
            throw new DemetriusException(msg, se);
        }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }

        return rslt;
    }

    // Returns the duplicate object
    // this is used in themove and copy functions when the moving and
    // copying by replacing the duplicate resource
    protected IResource getDuplicate(IFolder f, String name) throws DemetriusException{

        // assertions
        if(f == null){
            throw new IllegalArgumentException("Argument 'f' cannot be null");
        }
        if(name == null){
            throw new IllegalArgumentException("Argument 'name' cannot be null");
        }

        Connection conn = getConnection();
        PreparedStatement pstmt = null;
        PreparedStatement pstmtDuplicate = null;
        ResultSet rs = null;
        IResource duplicate = null;

        try{
            pstmt = conn.prepareStatement(GET_DUPLICATE_RESOURCES);
            pstmt.setInt(1, ((IRdbmsResource)f).getId());
            pstmt.setInt(2, id);
            pstmt.setInt(3, ((IRdbmsResource)f).getId());
            pstmt.setInt(4, id);
            pstmt.setString(5, name.toUpperCase());
            rs = pstmt.executeQuery();

            if(rs.next()){
                int duplicateId = rs.getInt("id");
                duplicate = (IResource)getFromCache(new Integer(duplicateId));
                ResultSet rsDuplicate = null;

                if(duplicate == null){
                    if(rs.getString("type").equalsIgnoreCase("file")){
                        pstmtDuplicate = conn.prepareStatement(GET_FILE_SQL);
                        pstmtDuplicate.setInt(1, duplicateId);
                        rsDuplicate = pstmtDuplicate.executeQuery();

                        if(rsDuplicate.next()){
                            duplicate = new RFileImpl(duplicateId
                                    , rsDuplicate.getString("file_name")
                                    , this
                                    , rsDuplicate.getLong("file_size")
                                    , rsDuplicate.getTimestamp("modify_Date")
                                    , f
                                    , rsDuplicate.getBoolean("is_hidden")
                                    , rsDuplicate.getString("mime_type")
                                    , rsDuplicate.getString("description")
                                    , rsDuplicate.getTimestamp("create_Date"));
                            addToCache(new Integer(duplicateId), duplicate);
                        }
                    }else{
                        pstmtDuplicate = conn.prepareStatement(GET_FOLDER_SQL);
                        pstmtDuplicate.setInt(1, duplicateId);
                        rsDuplicate = pstmtDuplicate.executeQuery();

                        if(rsDuplicate.next()){
                            duplicate = new RFolderImpl(duplicateId
                                    , rsDuplicate.getString("folder_name")
                                    , rsDuplicate.getString("description")
                                    , f
                                    , rsDuplicate.getTimestamp("create_Date")
                                    , rsDuplicate.getTimestamp("modify_Date")
                                    , rsDuplicate.getBoolean("is_hidden")
                                    , this
                                    , 0);
                            ((AbstractResource)duplicate).setSize(this.getSize(duplicateId));
                            addToCache(new Integer(duplicateId), duplicate);
                            }
                    }
                }
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get the duplicate resource.";
            throw new DemetriusException(msg, se);
        }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (pstmtDuplicate != null) closeStatement(pstmtDuplicate);
            if (conn != null) closeConnection(conn);
        }

        return duplicate;
    }


    /*private synchronized int getUniqueFolderId(){
        Connection conn = null;
        if(maxFolderId == 0){
            try{
                // set the sequencer for Folder and File
                conn = getConnection();
                PreparedStatement pStmt =
                conn.prepareStatement("Select max(folder_id) from FSA_folder");

                ResultSet rs = pStmt.executeQuery();
                if (!rs.next()){
                    maxFolderId = 1;
                }
                else{
                    maxFolderId = rs.getInt("max");
                }

            }catch(SQLException se){

            }
            finally{
                closeConn(conn);
            }
        }
        return maxFolderId++;
    }

    private synchronized int getUniqueFileId(){
        Connection conn = null;
        if(maxFileId == 0){
            try{
                conn = getConnection();
                PreparedStatement pStmt =
                conn.prepareStatement("Select max(file_id) from FSA_file");

                ResultSet rs = pStmt.executeQuery();
                if (!rs.next()){
                    maxFileId = 1;
                }
                else{
                    maxFileId = rs.getInt("max");
                }
                pStmt.close();
                rs.close();

            }catch(SQLException se){

            }finally{
                closeConn(conn);
            }
        }
        return maxFileId++;
    }*/

    // returns a unique resourceId for the given factory.
    private synchronized int getUniqueResourceId() throws DemetriusException{

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs  = null;
        int rslt = 0;

        try{
            conn = getConnection();
            pstmt = conn.prepareStatement("select max(id) as maxId from (select distinct" +
                    " folder_id as id from FSA_folder union select distinct " +
                    "file_id as id  from FSA_file) alIds");
            rs = pstmt.executeQuery();

            if (rs.next()){
                rslt = rs.getInt("maxId") + 1;
            }
            else{
                rslt = 1;
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get a unique resource id.";
            throw new DemetriusException(msg, se);
        }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
            rs = null;
            pstmt = null;
            conn = null;
        }
        return rslt;
    }

    /**
     * Moves the given resource to the folder and checks if an existing
     * duplicate resource is to be overwritten
     * @param r
     * <code>IResource</code> that will be moved
     * @param f
     * <code>IFolder</code> where the resource will be moved
     * @param overWrite
     * Determines whether the duplicate resource will be overwritten or not
     */
    public void move(IResource r, IFolder f, boolean overWrite) throws DemetriusException {
        // assertions
        if(r == null){
            throw new IllegalArgumentException("Argument resource cannot be null");
        }
        if(f == null){
            throw new IllegalArgumentException("Argument folder to move to cannot be null");
        }
        // if moving a folder into the same folder
        if(r.getUrl().equalsIgnoreCase(f.getUrl())){
            throw new IllegalArgumentException("You cannot copy a folder into the same Folder ");
        }

        // if the folders are the same
        if(r.getParent().getUrl().equalsIgnoreCase(f.getUrl())){
            throw new IllegalArgumentException("You cannot copy resources within the same Folder ");
        }

        // get the duplicate resource, will be null if there are no duplicates
        IResource duplicateResource = getDuplicate(f, r.getName());

        if(duplicateResource != null  && !overWrite){
            throw new IllegalArgumentException("File or folder with same name already exists ");
        }

        // delete the duplicate resource
        if(duplicateResource != null){
            delete(duplicateResource);
        }
        //check if the owners are the same
        if(r.getOwner().getUrl().equalsIgnoreCase(f.getOwner().getUrl())){
            // call the private move method
            move(r, f);
        }else{
            // if the owners are not the same
            if(r.getType().equals(ResourceType.FILE)){
                // adding a file into the parent folder
                addFile(r.getOwner().getMetadata(r), f, ((IFile)r).getInputStream());
            }
            else{
                // if the resource is a folder
                //add the folder in the parent folder
                IFolder parent = createFolder(r.getOwner().getMetadata(r), f);

                //get the files in the folder and add it
                IResource[] contents = ((IFolder)r).getContents(new ResourceType[] {ResourceType.FILE});
                for(int i = 0; i < contents.length; i++){
                    addFile(contents[i].getOwner().getMetadata(contents[i])
                            , parent, ((IFile)contents[i]).getInputStream());
                }
                // get all the folders and copy them
                contents = ((IFolder)r).getContents(new ResourceType[] {ResourceType.FOLDER});
                for(int i = 0; i < contents.length; i++){
                    move(contents[i], parent, overWrite);
                }
            }
            // delete the resource from the original parent
            r.getOwner().delete(r);
        }
    }


    /**
     * moves the resource within the same factory
     * @param r
     * @param f
     * @throws DemetriusException
     */
    private void move(IResource r, IFolder f) throws DemetriusException{
        // assertions
        if(r == null){
            throw new IllegalArgumentException("Argument resource cannot be null");
        }
        if(f == null){
            throw new IllegalArgumentException("Argument folder to move to cannot be null");
        }

        // move the resource
        String moveResourceSql = null;
        PreparedStatement pstmt = null;
        Connection conn = null;

        try{
            if(r.getType().equals(ResourceType.FILE)){
                moveResourceSql = "update FSA_file set parent_id = ? where file_id = ?";
            }
            else{
                moveResourceSql = "update FSA_folder set parent_id = ? where folder_id = ?";
            }
            conn = getConnection();
            pstmt = conn.prepareStatement(moveResourceSql);
            pstmt.setInt(1, ((IRdbmsResource)f).getId());
            pstmt.setInt(2, ((IRdbmsResource)r).getId());
            pstmt.executeUpdate();

        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to move the contents " +
                    "of the given resource.";
            throw new DemetriusException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
    }

    /**
     * Makes a copy of the resource and puts it in the destination folder. The function also checks if there is an existing duplicate resource and allows overwriting.
     * @param r
     * <code>IResource</code> that will be copied
     * @param f
     * <code>IFolder</code> where the resource will be copied to
     * @param overWrite
     * Determines whether the duplicate resource will be overwritten
     */
    public void copy(IResource r, IFolder f, boolean overWrite) throws DemetriusException {

        // assertions
        if(r == null){
            throw new IllegalArgumentException("Argument resource cannot be null");
        }
        if(f == null){
            throw new IllegalArgumentException("Argument folder to move to cannot be null");
        }

        //if moving a folder into the same folder
        if(r.getUrl().equalsIgnoreCase(f.getUrl())){
            throw new IllegalArgumentException("You cannot copy a folder into the same Folder ");
        }

        // if the folders are the same
        if(r.getParent().getUrl().equalsIgnoreCase(f.getUrl())){
            throw new IllegalArgumentException("You cannot copy resources within the same Folder ");
        }

        IResource duplicateResource = getDuplicate(f, r.getName());
        if(duplicateResource != null  && !overWrite){
            throw new IllegalArgumentException("File or folder with same name already exists ");
        }
        // delete the duplicate resource
        if(duplicateResource != null){
            delete(duplicateResource);
        }

        if(r.getOwner().getUrl().equalsIgnoreCase(f.getOwner().getUrl())){
            // call the private copy method
            copy(r, f);
        }else{
            if(r.getType().equals(ResourceType.FILE)){
                addFile(r.getOwner().getMetadata(r), f, ((IFile)r).getInputStream());
            }
            else{
                // if the resource is a folder
                //add the folder in the parent folder
                IFolder parent = createFolder(r.getOwner().getMetadata(r), f);

                //get the files in the folder and add it
                IResource[] contents = ((IFolder)r).getContents(new ResourceType[] {ResourceType.FILE});
                for(int i = 0; i < contents.length; i++){
                    addFile(contents[i].getOwner().getMetadata(contents[i])
                            , parent, ((IFile)contents[i]).getInputStream());
                }
                // get all the folders and copy them
                contents = ((IFolder)r).getContents(new ResourceType[] {ResourceType.FOLDER});
                for(int i = 0; i < contents.length; i++){
                    copy(contents[i], parent, overWrite);
                }
            }
        }
    }

    /**
     * Makes a copy of the resource and puts it in the destination folder.
     * This works when the owners of the resource to copy and the destination folder
     * are the same.
     * @param r
     * <code>IResource</code> that will be copied
     * @param f
     * <code>IFolder</code> where the resource will be copied to
     * @param overWrite
     * Determines whether the duplicate resource will be overwritten
     */
    private void copy(IResource r, IFolder f) throws DemetriusException {

        // assertions
        if(r == null){
            throw new IllegalArgumentException("Argument resource cannot be null");
        }
        if(f == null){
            throw new IllegalArgumentException("Argument folder to move to cannot be null");
        }

        // make a copy of the resource and add to the destination folder
        String copyResourceSql = null;

        if(r.getType().equals(ResourceType.FILE)){
            copyResourceSql = "insert into FSA_file(file_id, file_name, " +
                    "description, mime_type, file_size, create_date, modify_date" +
                    ", parent_id, resourceFactory_id, is_hidden) select ?, " +
                    "file_name, description, mime_type, file_size, ?, ?, ?, " +
                    "resourceFactory_id, is_hidden from FSA_file where file_id = ?";
            Connection conn = null;
            PreparedStatement pstmt = null;

            try{
                int file_id = getUniqueResourceId();
                conn = getConnection();
                pstmt = conn.prepareStatement(copyResourceSql);
                pstmt.setInt(1, file_id);
                pstmt.setTimestamp(2, now());
                pstmt.setTimestamp(3, now());
                pstmt.setInt(4, ((IRdbmsResource)f).getId());
                pstmt.setInt(5, ((IRdbmsResource)r).getId());
                pstmt.executeUpdate();

                // save the file on the FS
                InputStream fis =
                    new FileInputStream(new File(new StringBuffer().append(path)
                        .append(DELIMITER)
                        .append(((IRdbmsResource)r).getId())
                        .append(((RFileImpl)r).getExt()).toString()));
                OutputStream fos =
                    new FileOutputStream(new File(new StringBuffer().append(path)
                        .append(DELIMITER)
                        .append(file_id)
                        .append(((RFileImpl)r).getExt()).toString()));
                byte[] contents = new byte[2000];
                while(fis.read(contents) > 0){
                    fos.write(contents);
                }

            }catch(SQLException se){
                String msg = "RdbmsResourceFactory was not able to get copy the contents of " +
                        "the given resource.";
                throw new DemetriusException(msg, se);
            }
            catch(FileNotFoundException fnfe){
                String msg = "RdbmsResourceFactory was not able to find the contents of " +
                        "the given resource.";
                throw new DemetriusException(msg, fnfe);
            }
            catch(IOException io){
                String msg = "RdbmsResourceFactory got an IO exception.";
                throw new DemetriusException(msg, io);
            }
            finally{
                if (pstmt != null) closeStatement(pstmt);
                if (conn != null) closeConnection(conn);
            }
        }
        else{
            // make a copy of the current folder
            copyResourceSql = "insert into FSA_folder(folder_id, folder_name" +
                    ", description , create_date, modify_date, parent_id, " +
                    "resourceFactory_id, is_Hidden) select ?, folder_name, " +
                    "description, ?, ?, ?, resourceFactory_id, is_hidden  from " +
                    "FSA_folder where folder_id = ? ";
            Connection conn = null;
            PreparedStatement pstmt = null;
            IResource parent = null;
            try{
                int folder_id = getUniqueResourceId();
                Timestamp time = now();
                conn = getConnection();
                pstmt = conn.prepareStatement(copyResourceSql);
                pstmt.setInt(1, folder_id);
                pstmt.setTimestamp(2, time);
                pstmt.setTimestamp(3, time);
                pstmt.setInt(4, ((IRdbmsResource)f).getId());
                pstmt.setInt(5, ((IRdbmsResource)r).getId());
                pstmt.executeUpdate();

                parent = new RFolderImpl(folder_id, r.getName()
                        , ((IRdbmsResource)r).getDescription()
                        , f, time, time, r.isHidden(), this, r.getSize());
                addToCache(new Integer(folder_id), parent);

            }catch(SQLException se){
                String msg = "RdbmsResourceFactory was not able to copy the contents of the given folder.";
                throw new DemetriusException(msg, se);
            }
            finally{
                if (pstmt != null) closeStatement(pstmt);
                if (conn != null) closeConnection(conn);
            }

            // copy all the files
            IResource[] files =
                getContents((IFolder)r, new ResourceType[]{ResourceType.FILE});
            for(int i = 0; i < files.length; i++){
                copy(files[i], (IFolder)parent, true);
            }

            // copy all the folders under the parent folder
            IResource[] folders =
                getContents((IFolder)r, new ResourceType[]{ResourceType.FOLDER});
            for(int i = 0; i < folders.length; i++){
                copy(folders[i], (IFolder)parent, true);
            }
        }
    }

    /**
     *
     */
    public String getUrl() {
        return url;
    }

    public static IResourceFactory fromUrl(String facUrl) throws DemetriusException {

        // Assertions.
        if(facUrl == null || facUrl.trim().equals("")){
            throw new IllegalArgumentException("The Factory"
                                    + " Url must not be null or empty.");
        }

        IResourceFactory rf = null;
        String rootName = facUrl.substring(facUrl.lastIndexOf("[") + 1, facUrl
                .lastIndexOf("]"));
        String ds = facUrl.substring(facUrl.lastIndexOf("]/") + 2, facUrl
                .length());
        rf = getInstance(Integer.parseInt(ds), rootName);

        // Make sure a valid response was created.
        if (rf == null) {
            String msg = "Unable to construct a resource factory from the "
                                        + "specified url:  " + facUrl;
            throw new DemetriusException(msg);
        }

        return rf;
    }

    // gets the number of folders in the given folder
    protected long getNumFolders(IFolder f){
        return getNumFolders(((IRdbmsResource)f).getId());
    }

    /*// calculates the number of folders in the  given folder Id
    // all the folders in the tree
    private long getNumFolders(int folderId){
        long count = 0;
        String folderCountSql = "Select * from FSA_folder where parent_id = ?";
        Connection conn = getConnection();
        PreparedStatement pStmt = null;
        try{
            pStmt = conn.prepareStatement(folderCountSql);
            pStmt.setInt(1, folderId);

            ResultSet rs = pStmt.executeQuery();
            while(rs.next()){
                // add the folder
                count ++;
                count += getNumFolders(rs.getInt("folder_id"));

            }
            rs.close();
            pStmt.close();
        }catch(SQLException se){
            se.printStackTrace();
        }
        finally{
            closeConn(conn);
        }

        return count;
    }*/

    // calculates the number of folders in the that are in the given folder Id
    // all the folders at the next level only
    private long getNumFolders(int folderId){

        long count = 0;
        String folderCountSql = "Select count(*) as numFolders from FSA_folder " +
                "where parent_id = ?";
        Connection conn = getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            pstmt = conn.prepareStatement(folderCountSql);
            pstmt.setInt(1, folderId);
            rs = pstmt.executeQuery();
            if(rs.next()){
                count = rs.getInt("numFolders");
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get the number " +
                    "of folders in the given folder.";
            throw new RuntimeException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return count;
    }

    // get the number of files in the given folder
    protected long getNumFiles(IFolder f) {
        return getNumFiles(((IRdbmsResource)f).getId());        
    }

/*  // calculates the number of files in the given folder
    // all the files in the tree
    private long getNumFiles(int folderId){
        int count = 0;
        String resourceSql = "Select count(*) from FSA_file where parent_id = ?";
        Connection conn = getConnection();
        PreparedStatement pStmt = null;
        try{

            // count all the files in the folder
            pStmt = conn.prepareStatement(resourceSql);
            pStmt.setInt(1, folderId);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next()){
                count += rs.getInt(1);
            }
            rs.close();
            pStmt.close();

            // get all the folders and get the files in them
            resourceSql = "Select * from FSA_folder where parent_id = ?";

            pStmt = conn.prepareStatement(resourceSql);
            pStmt.setInt(1, folderId);
            rs = pStmt.executeQuery();
            while(rs.next()){
                count += getNumFiles(rs.getInt(1));
            }
            rs.close();
            pStmt.close();

        }catch(SQLException se){
            se.printStackTrace();
        }
        finally{
            closeConn(conn);
        }

        return count;
    }*/

    // calculates the number of files in the given folder
    // all the files at the next level
    private long getNumFiles(int folderId){

        int count = 0;
        String resourceSql = "Select count(*) from FSA_file where parent_id = ?";
        Connection conn = getConnection();
        PreparedStatement pstmt = null;
        try{

            // count all the files in the folder
            pstmt = conn.prepareStatement(resourceSql);
            pstmt.setInt(1, folderId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                count = rs.getInt(1);
            }
        }catch(SQLException se){
            String msg = "RdbmsResourceFactory was not able to get the number " +
                    "of files in the given folder.";
            throw new RuntimeException(msg, se);
        }
        finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return count;
    }

    private static void closeStatement(Statement stmt){

        if (stmt == null) {
            return;
        }

        try {
            stmt.close();
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }

    }

    private static void closeConnection(Connection conn){

        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (Throwable t) {
            throw new RuntimeException("DB Connection error.", t);
        }

    }

    protected interface IRdbmsResource extends IResource{

        public String getDescription();

        public void setDescription(String desc);

        public int getId();

        public Date getDateUploaded();

    }

    // Rdbms Implementation of IRdbmsResource
    public static class RFileImpl extends FileImpl implements IRdbmsResource {
        private String description =  null;
        private Date dateUploaded = null;
        private int id = 0;
        private long file_size = 0;

        public RFileImpl(String name, String description, IFolder parent
                , String mimeType){
            super(name, parent, mimeType);
            this.description = description;
        }

        public RFileImpl(int file_id, String name, IResourceFactory owner
                , long size, Date dateModified, IFolder parent, boolean isHidden
                , String mimeType, String description, Date dateUploaded){
            super(name, owner, size, dateModified, parent, isHidden, mimeType);
            this.description = description;
            this.dateUploaded = dateUploaded;
            this.id = file_id;
            this.setSize(size);
        }

        /**
         * Accessor method to return the File Description
         * @return description of the resource
         */
        public String getDescription(){
            if( this.description == null)
                return "";
            return description;
        }

        /**
         * Accessor method to get the date the file was uploaded
         * @return Date the file was uploaded
         */
        public Date getDateUploaded(){
            return dateUploaded;
        }

        /**
         * Accessor method to get the id of the resource
         * @return id of the resource object
         */
        public int getId(){
            return id;
        }

        /**
         * Setter method to set the resource description
         * @param desc  description for the resource
         */
        public void setDescription(String desc){
            this.description = desc;
        }

        // method to get the ext of the file object
        protected String getExt(){
            String name = getName();
            return name.substring(name.lastIndexOf("."));
        }

        public long getSize(){
            return this.file_size;
        }

        public void setSize(long value){
            this.file_size = value;
        }

    }

    // Rdbms Implementation of IRdbmsResource
    public static class RFolderImpl extends FolderImpl implements IRdbmsResource{

        private String description =  null;
        private Date dateUploaded = null;
        private int id;

        public RFolderImpl(int id, String name, String description
                , IFolder parent){
            super(name, parent);
            this.description = description;
            this.id = id;
        }

        public RFolderImpl(int folder_id, String name, String description
                , IFolder parent, Date createDate, Date modifyDate, boolean isHidden
                , IResourceFactory owner, long size){
            super(name, owner, size, modifyDate, parent, isHidden);
            this.description = description;
            this.dateUploaded = createDate;
            this.id = folder_id;
        }

        /**
         * Accessor method to return the resource Description
         * @return description of the resource
         */
        public String getDescription(){
            if( this.description == null)
                return "";
            return description;
        }

        /**
         * Accessor method to return the date the folder was created
         * @return dateUploaded of the resource
         */
        public Date getDateUploaded(){
            return dateUploaded;
        }

        /**
         * Accessor method to return the resource id
         * @return id of the resource
         */
        public int getId() {
            return id;
        }

        /**
         * Setter method to set the resource description
         * @param desc  description for the resource
         */
        public void setDescription(String desc){
            this.description = desc;
        }

        /**
         * Accessor method to get the  size of the resource
         * @return size of the resource
         */
        public long getSize(){
            try {
                return ((RdbmsResourceFactory)getOwner()).getSize(id);
            } catch (DemetriusException e) {
                throw new RuntimeException("Error in getting the size of the folder " + this.getName(), e);
            }
        }

        /**
         * Accessor method to return the number of folders in the resource
         * @return return the number of folders in the resource
         */
        public long getNumFolders(){
            return ((RdbmsResourceFactory)getOwner()).getNumFolders(id);
        }

        /**
         * Accessor method to return the number of files in the resource
         * @return return the number of files in the resource
         */
        public long getNumFiles(){
            return ((RdbmsResourceFactory)getOwner()).getNumFiles(id);
        }

    }



}
