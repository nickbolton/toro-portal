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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

import net.unicon.academus.api.AcademusDataSource;
import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.jit.ICreator;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.fac.rdbms.RdbmsResourceFactory;

import org.dom4j.Element;

public class RdbmsFactoryCreator implements ICreator {


    // instance variable
    private String rootPath = null;
    private static Sequencer seq = null;
    private static DataSource dataSource = new AcademusDataSource();
    private String rootName = null;
    private long maxLimit = 0;
    
    private static final String CREATE_FACTORY_SQL = "Insert into fsa_resourceFactory " +
    		"(factory_id, actual_path, max_limit) values( ?, ?, ?)";
    
    private static final String FACTORY_SEQ_SQL = "select max(factory_id) as id from fsa_resourceFactory";
    
    private static final String GET_FACTORY_SQL = "SELECT factory_id, actual_path, max_limit FROM FSA_resourceFactory WHERE factory_id = ?";

    /**
     * Returns an instance of the class by parsing the <code>Element</code>.
     * @param e XML <code>Element</code>
     * @return an instance of the class
     */
    public static ICreator parse(Element e){
        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("creator")) {
            String msg = "Argument 'e [Element]' must be a <creator> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // Root Path.
        Element ePath = (Element) e.selectSingleNode("seed-path");
        if (ePath == null) {
            String msg = "Element <creator> is missing required child "
                                                    + "element <seed-path>.";
            throw new IllegalArgumentException(msg);
        }
        String seedPath = ePath.getText();

        // Root Name.
        Element eName = (Element) e.selectSingleNode("root-name");
        if (eName == null) {
            String msg = "Element <creator> is missing required child "
                                                    + "element <root-name>.";
            throw new IllegalArgumentException(msg);
        }
        String rootName = eName.getText();

        // Size.
        Element eSize = (Element) e.selectSingleNode("size");
        if (eSize == null) {
            String msg = "Element <creator> is missing required child "
                                                    + "element <size>.";
            throw new IllegalArgumentException(msg);
        }
        long size = Long.parseLong(eSize.getText()) * 1024 * 1024;

        // bootstrap the RdbmsResourceFactory 
        RdbmsResourceFactory.bootStrap(dataSource);        
        
        return new RdbmsFactoryCreator(seedPath, rootName, size);
    }

    /* (non-Javadoc)
     * @see net.unicon.academus.access.jit.ICreator#create()
     */
    public Object create(Identity ident) {
        if(seq == null){
            initSeq(dataSource);
        }

        Kernel kernel = Kernel.create(rootPath);

        IResourceFactory rf = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql;
        int id;
        // add the resource factory to the table
        boolean added = false;
        do{
            id = seq.next();
//System.out.println("Id for factory " + id);
            added = addToDb( id, kernel.getRootPath());
        } while(added == false);

        // create an instance of the factory
        rf = new RdbmsResourceFactory(id, kernel.getRootPath()
                , rootName, maxLimit);

        return rf;
    }

    // this function is used to add the resource Factory into the table
    // if the resource could not be added, it returns a false
    private boolean addToDb(int id, String actualPath){

        // assertions
        if(actualPath == null){
            throw new IllegalArgumentException("Argument 'actualPath' cannot be null");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try{
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(CREATE_FACTORY_SQL);
            pstmt.setInt(1, id);
            pstmt.setString(2, actualPath);
            pstmt.setLong(3, this.maxLimit);
            pstmt.execute();
            return true;
        }catch(SQLException se){

        }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
        return false;
    }

    // this function is used to initialize the sequencer for the factory creator
    private static void initSeq(DataSource dataSource){

        // assertions
        if(dataSource == null){
            throw new IllegalArgumentException("Argument 'ds' cannot be null");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(FACTORY_SEQ_SQL);
            rs = pstmt.executeQuery();
            if(rs.next()){
                seq = new Sequencer(rs.getInt("id"));
            }
            else{
                seq = new Sequencer();
            }

        } catch(SQLException se){
            String msg = "FactoryCreator was not able to get a unique id for the sequencer.";
            throw new RuntimeException(msg, se);
        }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }
    }

    // function used to get the timestamp for the surrent time.
    // should be replaced by the AcademusDbUtil time stamper
    private static Timestamp now(){
        // TODO need to get the timestamp from the database
        return new Timestamp(System.currentTimeMillis());
    }

   /**
    * Returns an instance of the <code>RdbmsResourceFactory</code> from the database
    * @param ds <code>Datasource</code> to the database
    * @param factory_id the id of the factory whose instnace needs to be retrieved
    * @return an instance of <code>IResourceFactory</code>.
    * throws an exception if the factory with the given id was not found.
    */
    public static IResourceFactory getInstance(int factory_id, String rootName){

        IResourceFactory rf = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            conn = dataSource.getConnection();            
            pstmt = conn.prepareStatement(GET_FACTORY_SQL);
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
            throw new RuntimeException(msg, se);
        }finally{
            if (pstmt != null) closeStatement(pstmt);
            if (conn != null) closeConnection(conn);
        }

        return rf;

    }

    private RdbmsFactoryCreator(String path, String rootName, long size){
        // Assertions.
        if (path == null) {
            String msg = "Argument 'path' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rootName == null) {
            String msg = "Argument 'rootName' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        this.rootPath = path;
        this.rootName = rootName;
        this.maxLimit = size;
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
}
