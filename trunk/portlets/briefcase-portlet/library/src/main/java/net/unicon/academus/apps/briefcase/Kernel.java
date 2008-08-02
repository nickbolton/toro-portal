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

package net.unicon.academus.apps.briefcase;

import java.io.File;
import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.alchemist.rdbms.Sequencer;


/**
 * @author ibiswas
 *
 * This class is used to control the creation of the resourceFactories in the database
 * and retrieve instances of the RdbmsResourceFactory by FactoryId.
 * Used in the RdbmsFactoryCreator and FsFactoryCreator
 */
public class Kernel {

    // instance variables
    private String rootPath = null;
    private long id = 0;
    private static Sequencer seq = null;
    private static int HASH_LEVELS = 4;
    private static DataSource dataSource = null;
    private static final int CACHE_SIZE = 1;

    /**
     * Constructor
     * @param id
     * id of the <code>IResourceFactory</code> that the kernel represents
     * @param rootPath
     * <code>String</code> actual path where the files and folders will bestored on the FileSystem
     */
    private Kernel(long id, String rootPath){
        this.id = id;
        this.rootPath = rootPath;
    }

    /**
     * Returns an instance of the object
     * @param baseRootPath
     * <code>String</code> File System path where the files and folders will be stored.
     * @return instnace of the <code>Kernel</code> object
     */
    public static synchronized Kernel create(String baseRootPath){

        // Assertions
        if (baseRootPath == null){
            throw new IllegalArgumentException("Argument 'baseRootPath' cannot be null");
        }

        // initialize the sequencer
        if(seq == null){
            seq = new Sequencer(getDataSource(), "FSA_Kernel_Sequencer", CACHE_SIZE);
        }

        // add the factory to the kernel database
        long id = seq.next();
        
        return new Kernel(id, getActualPath(baseRootPath, id));
    }

    /*
    // function to add the factory to the database.
    private static boolean addToDb(long id, String basePath){
        String sql = "Insert into fsa_kernel_sequencer(id) values(?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try{

            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.execute();
            return true;
        }catch(SQLException se){
            // pass through & try again...
       }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return false;
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

*/
    /**
     * @return Returns the id.
     */
    public long getId() {
        return id;
    }
    /**
     * @return Returns the rootPath.
     */
    public String getRootPath() {
        return rootPath;
    }

    public static void bootStrap(DataSource ds) {
        synchronized(Kernel.class) {
            if (dataSource == null) {
                dataSource = ds;
            }
        }
    }

    private static DataSource getDataSource(){
        if (dataSource == null) {
            synchronized(Kernel.class) {
                if (dataSource == null) {
                    try{
                        dataSource = AcademusFacadeContainer
                                        .retrieveFacade(true)
                                        .getAcademusDataSource();
                    }catch(AcademusFacadeException afe){
                    	LogFactory.getLog(Kernel.class).error("Unable to access DataSource from the facade.", afe);
                    }
                }
            }
        }
        return dataSource;
    }

    // function used to calculate the actual root path from the given root path where the
    // file system will start storing the resources
    private static String getActualPath(final String path, long factory_id) {
        
        // create the directories if they don't exist
        new File(path).mkdirs();
        String hexId = Long.toHexString(factory_id);

        if (hexId.length() < HASH_LEVELS)
            for (int i = hexId.length(); i < HASH_LEVELS; i++)
                hexId = "0"+hexId;

        String nPath = path;
        for (int i = 0; i < HASH_LEVELS; i++) {
            nPath = nPath+"/"+hexId.charAt(i);
        }
        nPath = nPath+"/fac"+factory_id;

        new File(nPath).mkdirs();
        return nPath;
    }

}
