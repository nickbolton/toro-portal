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

package net.unicon.academus.apps.briefcase.migrate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import net.unicon.alchemist.access.rdbms.RdbmsAccessBroker;
import net.unicon.alchemist.rdbms.SimpleDataSource;
import net.unicon.alchemist.access.permissions.DummyAccessType;
import net.unicon.alchemist.access.permissions.DummyCreator;
import net.unicon.alchemist.access.permissions.PermissionsAccessBroker;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;

import org.dom4j.tree.*;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.io.SAXReader;


/**
 * @author ibiswas
 *
 * This class will be used to migrate briefcase data from 
 * Academus 1.5 to Academus 1.6.
 * 
 * Changes:
 * 1. The resource urls will have to be modified in two ways for the following 
 *    reasons:
 *    
 *    a. The briefcase code was refactored into a new package. The urls of the 
 *       resources are dependent on the package structure of the classes. These 
 *       urls will have to be updated.
 *       
 *    b. The structure for shared folder urls has been modified. The shared
 *       resource portion of the url is now completely enclosed in parentheses.
 *       This requires a new unique delimiter to separate the factory from the
 *       shared resource path. '///' is used as the delimiter.  
 * 
 * 2. The OWNER access type assigned to each personal resource factory will 
 *    have to be replaced with the BriefcaseAccessType enumeration.
 * 	  New entries will be inserted for each of the permissions assigned to the 
 *    identity.
 * 
 * 3. Set the uni_sequence table for the RdbmsAccessBroker Counter and 
 * 		FSA_Kernel_Sequencer
 * 
 * 
 * 4. Add impl attribute on <access> element.
 * 
 * 5. Update the <type> element to change the value attribute 
 *    to "handle" and add an attribute value="GRANT" * 
 * 
 * 6. Add impl attribute to specify the UserDirectory impl to user-attribute element
 *                
 * 7. Add access element to specify the AccessType to the RdbmsAccessBroker
 * 
 * 8. Add the civis address book information.
 * 
 * 9. The config file from Academus 1.5 will be updated to work in the 1.6 code base. 
 *    Please use the updated config file in the 1.6 deployment.
 *   
 */
public class BriefcaseMigrator {
    
    public static void main(String[] args){
        
        
        System.out.println("Starting Briefcase portlet migration of data from Academus 1.5 " +
                " to Academus 1.6");

        if(args.length == 2){
	        try {
	            DataSource ds = new SimpleDataSource();
	            
	            SAXReader reader = new SAXReader();
	            
	            // replace the old urls with the new classpaths
	            System.out.println("Updating the resource urls.");	            
	            updateUrl(ds);
                updateSharedResourceUrls(ds);
	            
	            // replace the OWNER access type.
	            System.out.println("Updating the accessTypes.");	            
	            updateAccessType(ds);
	            
	            // set the uni_sequence table for the RdbmsAccessBroker Counter
	            // and the FSA_Kernel_Sequencer
	            updateSequencers(ds);
	            
	            // read the old config file
	            // and correct the classpaths for all the access brokers.  
	            Document oldDoc = reader.read(args[0]);
	            
	            DOMDocumentFactory dFac = new DOMDocumentFactory();
	            
	            // Get all access brokers
	            List brokers = oldDoc.selectNodes(
                                    "//access-broker");
                Element broker = null; 
                
                // Change access brokers to point to alchemist instead of academus
                for(int i = 0; i < brokers.size(); i++){
                    broker = (Element)brokers.get(i);
                    String implValue = broker.attributeValue("impl").replaceFirst("academus", "alchemist");
                    broker.addAttribute("impl", implValue);                    
                }

                // Get the Access Broker under the personal Drive, and make sure it's a JIT broker
                Element targetsJitAccessBroker = (Element)((Element)oldDoc.selectNodes("/briefcase/drive[@handle='personal']/access-broker[@impl='net.unicon.alchemist.access.jit.JitAccessBroker']").get(0)).detach();

                // Only enter this code if the personal drive section was successful selected
                if(targetsJitAccessBroker != null) {

                	// Create new permissions element, mirroring targets element
                	Element permissionsJitAccessBroker = (Element)targetsJitAccessBroker.clone();
                	
                	// Change handles
                	targetsJitAccessBroker.addAttribute("handle", "personal-resourses-t"); 
                	permissionsJitAccessBroker.addAttribute("handle", "personal-resourses-p"); 
                	
                	// Create new permissions access-broker
                	Element permAccessBroker = dFac.createElement(new QName("access-broker"), 2);
                	permAccessBroker.addAttribute("handle", "personal-jit");
                	permAccessBroker.addAttribute("impl", PermissionsAccessBroker.class.getName());
                	
                	// Create new access element and add it to permAccessBroker
                	Element permAccess = dFac.createElement(new QName("access"), 1);
                	permAccess.addAttribute("impl", BriefcaseAccessType.class.getName());
                	permAccessBroker.add(permAccess);
                	
                	// Create targets and permissions elements and add to the new permissions access-broker
                	Element targets = permAccessBroker.addElement("targets");
                	targets.add(targetsJitAccessBroker);
                	
                	Element permissions = permAccessBroker.addElement("permissions");
                	permissions.add(permissionsJitAccessBroker);
                	
                	// Add new permissions access broker to the drive
                	Element curDrive = (Element)oldDoc.selectNodes("/briefcase/drive[@handle='personal']").get(0);
                	curDrive.add(permAccessBroker);

                	//
                    // Change targets internals
                	//
                	
                	List targetsAccess = targets.selectNodes("access-broker/jit-rule/behavior/access");
                    for(int i = 0; i < targetsAccess.size(); i++){

                    	// Add impl attribute with value of fully-qualified class name
                        ((Element)targetsAccess.get(i)).addAttribute("impl", DummyAccessType.class.getName());
                	
                        // Get all child type elements and remove them
                        List types = ((Element)targetsAccess.get(i)).elements();
                        for(int j = 0; j < types.size(); j++){
                        	((Element)types.get(j)).detach();
                        }
                        
                        // Add a single dummy element
                        Element eType = dFac.createElement(new QName("type"), 2);
                        eType.addAttribute("value", "GRANT");
                        eType.addAttribute("handle", "DUMMY");
                        
                        ((Element)targetsAccess.get(i)).add(eType);
                        
                    }

                    // Add internal access broker's access element
                    Element targetsIAccessBroker = (Element)(targets.selectNodes("access-broker/access-broker").get(0));
                    Element targetsIAccess = dFac.createElement(new QName("access"), 1);
                    targetsIAccess.addAttribute("impl", DummyAccessType.class.getName());
                    targetsIAccessBroker.add(targetsIAccess);
                    
                    //
                    // Change permissions internals
                    //
                    
                    List permissionsAccess = permissions.selectNodes("access-broker/jit-rule/behavior/access");
                    for(int i = 0; i < permissionsAccess.size(); i++){
                    	// Add impl attribute with value of fully-qualified class name
                        ((Element)permissionsAccess.get(i)).addAttribute("impl", BriefcaseAccessType.class.getName());
                	
                        // Get all child type elements and replace them
                        List types = ((Element)permissionsAccess.get(i)).elements();
                        for(int j = 0; j < types.size(); j++){
                    		Attribute value = ((Element)types.get(j)).attribute("value");
                    		String text = value.getValue();
                    		value.setValue("GRANT");
                    		
                    		if(text.equals("0")){
                    			BriefcaseAccessType[] aTypes = BriefcaseAccessType.getInstances();
                    			((Element)types.get(j)).addAttribute("handle", aTypes[0].getName());
                    			
                    			for(int k = 1; k < aTypes.length; k++){
                    				Element eType = dFac.createElement(new QName("type"), 2);
                    				eType.addAttribute("value", "GRANT");
                    				eType.addAttribute("handle", aTypes[k].getName());                                
                    				((Element)permissionsAccess.get(i)).add(eType);
                    			}                            
                    		}else{
                    			((Element)types.get(j)).addAttribute("handle"
                    					, BriefcaseAccessType.getAccessType(Integer.parseInt(text)).getName());
                    		}
                    	}
                                               
                    }

                    // Change other elements in the permissions section
                    List permissionsBehavior = permissions.selectNodes("access-broker/jit-rule/behavior");
                    for(int i = 0; i < permissionsBehavior.size(); i++){
                        Element trigger = (Element)((Element)permissionsBehavior.get(i)).elements("trigger").get(0);
                        Element target = (Element)((Element)permissionsBehavior.get(i)).elements("target").get(0);
                        Element creator = (Element)((Element)permissionsBehavior.get(i)).elements("creator").get(0);
                    	
                        // Insert trigger text into target
                    	target.addAttribute("type", "GROUP");                    	
                        target.addText(trigger.getText());
 
                        // Remove current creator element
                        creator.detach();

                        // Add new current creator element
                        Element eCreator = dFac.createElement(new QName("creator"), 1);
                        eCreator.addAttribute("impl", DummyCreator.class.getName());
                        ((Element)permissionsBehavior.get(i)).add(eCreator);
                    }
                    
                    // Change internal access broker's name
                    Element permissionsIAccessBroker = (Element)(permissions.selectNodes("access-broker/access-broker").get(0));
                    permissionsIAccessBroker.addAttribute("handle", "personal-resources-p-i");

                    // Add internal access broker's access element
                    Element permissionsIAccess = dFac.createElement(new QName("access"), 1);
                    permissionsIAccess.addAttribute("impl", BriefcaseAccessType.class.getName());
                    permissionsIAccessBroker.add(permissionsIAccess);
                    
                }
                
                List access = oldDoc.selectNodes("/briefcase/drive[@handle!='personal']//access");
                for(int i = 0; i < access.size(); i++){
                	
                	// Add impl attribute with value of fully-qualified class name
                	((Element)access.get(i)).addAttribute("impl", BriefcaseAccessType.class.getName());
                	
                	List types = ((Element)access.get(i)).elements();
                	for(int j = 0; j < types.size(); j++){
                		Attribute value = ((Element)types.get(j)).attribute("value");
                		String text = value.getValue();
                		value.setValue("GRANT");
                		
                		if(text.equals("0")){
                			BriefcaseAccessType[] aTypes = BriefcaseAccessType.getInstances();
                			((Element)types.get(j)).addAttribute("handle", aTypes[0].getName());
                			
                			for(int k = 1; k < aTypes.length; k++){
                				Element eType = dFac.createElement(new QName("type"), 2);
                				eType.addAttribute("value", "GRANT");
                				eType.addAttribute("handle", aTypes[k].getName());                                
                				((Element)access.get(i)).add(eType);
                			}                            
                		}else{
                			((Element)types.get(j)).addAttribute("handle"
                					, BriefcaseAccessType.getAccessType(Integer.parseInt(text)).getName());
                		}
                	}
                }
                
                // add impl attribute to specify the UserDirectory impl to user-attribute element
                List userAttr = oldDoc.selectNodes("//user-attribute");
                for(int i = 0; i < userAttr.size(); i++){
                    ((Element)userAttr.get(i)).addAttribute("impl", "net.unicon.academus.apps.briefcase.UserAttributeDirectory");
                }
                
                //replace the resource factory urls  
                List entries = oldDoc.selectNodes("/briefcase/drive[@handle!='personal']//access-broker/entry[@target!='']");
                for(int i = 0; i < entries.size(); i++){
                    ((Element)entries.get(i)).addAttribute("target"
                            , ((Element)entries.get(i)).attributeValue("target")
                            .replaceAll("academus.resource.factory", "demetrius.fac"));
                }
                
                // add access element to specify the AccessType to the RdbmsAccessBroker
                brokers = oldDoc.selectNodes(
                			"/briefcase/drive[@handle!='personal']//access-broker[@impl='" + RdbmsAccessBroker.class.getName() + "']");
                
                for(int i = 0; i < brokers.size(); i++){
                    broker = (Element)brokers.get(i);
                    Element eType = dFac.createElement(new QName("access"), 1);
                    eType.addAttribute("impl", BriefcaseAccessType.class.getName());
                    broker.add(eType);
                }
                
                // add the civis address book information.
                Element drive = (Element)oldDoc.selectSingleNode("briefcase");  
                drive.addComment("Civis implementation to be used to resolve usernames and group paths" +
                        " to academus users and groups. This should not require" +
                        " modification, as it utilized the Academus framework for gathering this" +
                        " information.");
                    
                Element civis = dFac.createElement("civis");
                civis.addAttribute("id", "addressBook");
                civis.addAttribute("impl", "net.unicon.civis.fac.academus.AcademusCivisFactory");
                
                Element restrictor = dFac.createElement("restrictor");
                restrictor.addAttribute("impl", "net.unicon.civis.grouprestrictor.AcademusGroupRestrictor");
                civis.add(restrictor);
                
                drive.add(civis);

                File f = new File(args[1]);
                PrintWriter pw =  new PrintWriter(new FileOutputStream(f));
                pw.write(oldDoc.asXML());
                pw.flush();
                pw.close();
             
	            System.out.println("Done. Enjoy !! \n Remember to the use the migrated config file with the 1.6 deploy.");
	        
	        } catch (Exception e) {            
	            e.printStackTrace();
	        }
        }else{
            usage();
        }
        
        
    }
    
    
    
    /**
     * Command usage.
     *
     * Usage for net.unicon.academus.apps.briefcase.migrate.BriefcaseMigrator:<br>
     *         java -cp &lt;classpath&gt; net.unicon.academus.apps.briefcase.migrate.BriefcaseMigrator [arguments];
     * 
     * <p>In order to update the data in the database, a DataSource must
     * be created. The database specific information has to be set in the 
     * /migrate/migrate.properties file</p>
     * 
     * <p> The migration process needs the config file from Academus 1.5 Briefcase Portlet
     * deployment. The file location needs to be provided as the first argument. 
     * The Briefcase portlet config file will be updated to work with the new deployment. The 
     * second argument needs to specify the path for the updated config file. 
     * </p>
     * 
     */
    public static void usage() {
        System.out.println("Usage for "+BriefcaseMigrator.class.getName()+":");
        System.out.println("\tjava -cp <classpath> "+BriefcaseMigrator.class.getName()+" [arguments] ");
        System.out.println();
        System.out.println("Arguments include: ");

        System.out.println("In order to update the data in the database, a DataSource must "
                + "be created. The database specific information has to be set in the "
                + "/migrate/migrate.properties file");
        System.out.println("The migration process needs the config file from Academus 1.5"
                + " Briefcase Portlet deployment. The file location needs to be provided "
                + "as the first argument.\n The Briefcase portlet config file will be "
                + "updated to work with the new deployment. The second argument needs "
                + "to specify the path for the updated config file.");
        System.out.println();
        
        System.exit(2);
    }
    
    private static void updateUrl(DataSource ds){
        
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ds.getConnection();
            
            stmt = conn.createStatement();
            
            stmt.executeUpdate("UPDATE ACCESS_ENTRY SET TARGET = replace(target" +
            		", 'academus.resource.factory', 'demetrius.fac' ) ");
            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            if(rs != null) closeResultSet(rs); rs = null;
            if(stmt != null) closeStatement(stmt); stmt = null;
            if(pstmt != null) closeStatement(pstmt); pstmt = null;
            if(conn != null) closeConnection(conn); conn = null;
        }
        
    }
    
    private static void updateSharedResourceUrls(DataSource ds){
        
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        final String SELECT = 
            "SELECT target, access_entry_id FROM access_entry WHERE access_controller_id=2";
        final String UPDATE = 
            "UPDATE access_entry SET target = ? WHERE access_entry_id = ?";
        
        try {
            conn = ds.getConnection();
            
            // Get shared resource urls
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SELECT);
            
            pstmt = conn.prepareStatement(UPDATE);
            
            StringBuffer url = new StringBuffer();
            int index = 0;
            
            while(rs.next()){
                
                url.append(rs.getString("target"));
                // Assumes localdatapath portion of target does not contain ')'
                // Safe assumption because Shared folder functionality of 
                // briefcase would be broken if it did
                index = url.indexOf(")");
                url.replace(index, index + 1, "///");
                url.append(")");
                pstmt.setString(1, url.toString());
                pstmt.setLong(2, rs.getLong("access_entry_id"));
                
                pstmt.executeUpdate();                
                
                // Clear resources
                pstmt.clearParameters();
                url.setLength(0);
            }          
            
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            if(rs != null) closeResultSet(rs); rs = null;
            if(stmt != null) closeStatement(stmt); stmt = null;
            if(pstmt != null) closeStatement(pstmt); pstmt = null;
            if(conn != null) closeConnection(conn); conn = null;
        }
        
    }
    
    private static void updateAccessType(DataSource ds){
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        
        final String INSERT = "INSERT INTO ACCESS_TYPES (ACCESS_ENTRY_ID, ACCESS_TYPE, STATUS)" +
        		" VALUES (?, ?, ?)" ;
        
        final String DELETE = "DELETE FROM ACCESS_TYPES WHERE ACCESS_ENTRY_ID = ?" ;
 
        try {
            conn = ds.getConnection();
            
            stmt = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            rs = stmt.executeQuery("SELECT access_entry_id FROM access_types WHERE access_type = 0 ");
            
            long accessEntryId = -1;
            
            while(rs.next()){
                
                accessEntryId = rs.getLong("access_entry_id");
                
                // remove the previous entries 
                pstmt = conn.prepareStatement(DELETE);
                pstmt.setLong(1, accessEntryId);
                
                pstmt.execute();
                
                // insert all the access_types allowed for owner
                BriefcaseAccessType[] types = BriefcaseAccessType.getInstances();
                for(int i = 0; i < types.length; i++){
                    pstmt = conn.prepareStatement(INSERT);
                    pstmt.setLong(1, accessEntryId);
                    pstmt.setLong(2, types[i].toInt());
                    pstmt.setString(3, "T");
                    
                    pstmt.execute();
                }
            }             
            
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            if(rs != null) closeResultSet(rs); rs = null;
            if(stmt != null) closeStatement(stmt); stmt = null;
            if(conn != null) closeConnection(conn); conn = null;
        }
        
        
        
    }
    
    private static void updateSequencers(DataSource ds){
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ds.getConnection();
            
            stmt = conn.createStatement();

            // initialize the RdbmsAccessBroker sequencer
            rs = stmt.executeQuery("SELECT count(name) AS count_name FROM UNI_SEQUENCE WHERE NAME='RdbmsAccessBroker'");
            
            if(rs.next()){ 
            	
            	int count = rs.getInt("count_name");

            	if(count > 0) {
            		stmt.executeUpdate("UPDATE UNI_SEQUENCE SET NEXT_INDEX = " +
            				"(SELECT MAX(ACCESS_ENTRY_ID) + 1 FROM ACCESS_ENTRY) " +
            		"WHERE NAME='RdbmsAccessBroker'");
            	} else {
            		stmt.executeUpdate("INSERT INTO UNI_SEQUENCE (NAME, NEXT_INDEX) " +
            		" SELECT 'RdbmsAccessBroker', MAX(ACCESS_ENTRY_ID) + 1 FROM ACCESS_ENTRY ");
            	}
            }
            
            // initialize the FSA_Kernel_Sequencer sequencer
            rs = stmt.executeQuery("SELECT count(name) AS count_name FROM UNI_SEQUENCE WHERE NAME='FSA_Kernel_Sequencer'");
            
            if(rs.next()){ 
            	int count = rs.getInt("count_name");
            	
            	if(count > 0) {
            		stmt.executeUpdate("UPDATE UNI_SEQUENCE SET NEXT_INDEX = " +
            				"(SELECT MAX(ACCESS_ENTRY_ID) + 1 FROM ACCESS_ENTRY) " +
            		"WHERE NAME='FSA_Kernel_Sequencer'");
            	}else{
            		stmt.executeUpdate("INSERT INTO UNI_SEQUENCE (NAME, NEXT_INDEX) " +
            		" SELECT 'FSA_Kernel_Sequencer', MAX(ID) + 1 FROM FSA_KERNEL_SEQUENCER ");
            	}
            }            
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            if(rs != null) closeResultSet(rs); rs = null;
            if(stmt != null) closeStatement(stmt); stmt = null;
            if(conn != null) closeConnection(conn); conn = null;
        }

    }
    
    private static void closeConnection(Connection conn){
        if(conn == null){
            return;
        }
        
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }        
    }
    
    private static void closeResultSet(ResultSet rs){
        if(rs == null){
            return;
        }
        
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }        
    }
    
    private static void closeStatement(Statement stmt){
        if(stmt == null){
            return;
        }
        
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }        
    }
    
}
