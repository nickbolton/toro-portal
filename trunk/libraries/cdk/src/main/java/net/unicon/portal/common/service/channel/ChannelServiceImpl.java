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

package net.unicon.portal.common.service.channel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.io.IOException;
import java.io.StringReader;
import java.io.File;
import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.PropertiesManager;

import net.unicon.academus.common.DOMAbleEntity;
import net.unicon.academus.common.EntityFactory;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.service.channel.ChannelService;
import net.unicon.portal.channels.notes.base.NoteImpl;
import net.unicon.portal.channels.classfolder.ClassFolderUtil;
import net.unicon.portal.channels.classfolder.ClassFolders;
import net.unicon.portal.channels.notes.Note;
import net.unicon.portal.common.properties.*;
import net.unicon.sdk.properties.*;
import net.unicon.portal.channels.BaseSubChannel;
import net.unicon.sdk.time.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.util.xml.NodeToString;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;  // DOM Level 2
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

public class ChannelServiceImpl implements ChannelService {

    private static final int sizeLimit = PropertiesManager.getPropertyAsInt("org.jasig.portal.RequestParamWrapper.file_upload_max_size");

    private static final int CF_XML_DB_SIZE = 3000;
//  the old way of getting dates
//    private static final String DB_CURRENT_TIME = UniconPropertiesFactory.getManager(PortalPropertiesType.PORTAL).getProperty("net.unicon.portal.db.DBcurrDate");

    private static final String getAllNotesSQL =
    "select note_id,user_id,body,update_date from notes where user_id = ?";

    private static final String getNoteSQL =
    "select note_id,user_id,body,update_date from notes where note_id = ?";

    private static final String updateNoteSQL =
    (new StringBuffer()
    .append("update notes set body = ?, update_date = ?")
    .append(" where note_id = ?")).toString();

    private static final String deleteNoteSQL =
    "delete from notes where note_id = ?";

    private static final String insertNoteSQL =
    (new StringBuffer()
    .append("insert into notes (user_id,body, update_date) values (?,?,?)"))
    .toString();

    // RelatedResources/ClassFolders queries
    private static final String insertClassFoldersSQL =
    "insert into class_folders (offering_id,ordering,xml) values (?, ?, ?)";

    private static final String deleteClassFoldersSQL =
    "delete from class_folders where offering_id = ?";

    private static final String getClassFoldersSQL    =
    "select xml from class_folders where offering_id = ? order by ordering";

    // following line commented out because of no apparent need - Alex
    // 09-DEC-2002
    //private static final String lockClassFoldersSQL =
    //  "lock table class_folders IN ROW EXCLUSIVE MODE";

    public ChannelServiceImpl() { }

    // ClassFolders related stuff
    public ClassFolders getClassFolder(Offering offering, Connection conn) {
        PreparedStatement pstmt = null;
        // following line commented out because of no apparent need - Alex
        // 09-DEC-2002
        //PreparedStatement pstmtLock = null;
        ResultSet rs = null;
        ClassFolders rval = null;
        try {
            pstmt = conn.prepareStatement(getClassFoldersSQL);
            // following line commented out because of no apparent need - Alex
            // 09-DEC-2002
            //pstmtLock = conn.prepareStatement(lockClassFoldersSQL);
            int i = 0;
            int offeringID = (int) offering.getId();
            pstmt.setInt(++i, offeringID);

            // following line commented out because of no apparent need - Alex
            // 09-DEC-2002
            //pstmtLock.execute();
            rs = pstmt.executeQuery();

            // What you get back is a set of ordered strings from the DB that are
            // concatenated back together to make the XML tree
            StringBuffer xmlSB = new StringBuffer();

            while (rs.next()) {
                xmlSB.append(rs.getString(1));
            }

            // quick test stuff
            //StringBuffer xmlSB = new StringBuffer();
            //xmlSB.append("<related-resources offering_id=\"1\">\n" +
            //             "<class-folders>\n</class-folders>\n</related-resources>");
            // ClassFolders uses a factory - will write a factory manager
            // later to do this muckety-muck

            String factoryName = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("ClassFoldersEntityFactory");
            Class factoryClass = Class.forName(factoryName);
            Method factoryMethod = factoryClass.getMethod("getEntityFactory", null);
            EntityFactory factory = (EntityFactory)(factoryMethod.invoke(null, null));

            Class entityClass = Class.forName("net.unicon.portal.channels.classfolder.ClassFolders");

            Object[] objs = new Object[2];
            objs[0] = xmlSB.toString();
            objs[1] = "" + offeringID;
            rval = (ClassFolders)factory.createEntity(entityClass, objs);
        }
        catch (SQLException se) {
            se.printStackTrace();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        finally {
            try {
                if (rs != null) rs.close();
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }

            try {
                if (pstmt != null) pstmt.close();
            }
            catch (SQLException se2) {
                se2.printStackTrace();
            }
        }
        return rval;
    }

    protected Document getClassFolderDOM(Offering offering, Connection conn)
    throws Exception {
        try {
            DocumentBuilderFactory dbf = null;
            DocumentBuilder db = null;
            ClassFolders cfs   = null;
            Document cfsDOM    = null;

            // we're going to need DOM stuff
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();

            // get the class folders for this offering
            cfs = getClassFolder(offering, conn);
            if (cfs instanceof DOMAbleEntity) {
                cfsDOM = ((DOMAbleEntity)cfs).toDocument();
            }
            else { // got to build our own DOM for cfs
                cfsDOM = db.parse(new InputSource(new StringReader(cfs.toXML())));
            }

            return cfsDOM;
        }
        catch (Exception exc) {
            exc.printStackTrace();
            throw exc;
        }
    }

    /*
     * Get the filename associcated with the given oid.
     */
    public String getFileNameByOID(Offering offering, String oid, Connection conn) throws Exception {

        String file = null;

        try {

            Document dom = getClassFolderDOM(offering, conn);

            Node classFolderNode = findNodeByOID(dom, oid, "");

            XPathAPI xpath = new XPathAPI();

            if (classFolderNode == null) {

                return null;

            } else {

                Node node = xpath.selectSingleNode(classFolderNode, "filename");

                NodeToString ns = new NodeToString(node);

                ns.setPrintTopLevelTag(false);
                ns.setPrintNewLines(false);

                file = ns.toString();

                //file = DOMUtil.getNodeText(node);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return file;

    }

    protected Node findNodeByOID(Document dom, String oid, String tag)

    throws Exception {

        Node addNode = null;

        XPathAPI xpath = new XPathAPI();

        String xpString = null;

        try {

            if (oid != null) {

                if (tag == null || tag.trim().length() == 0) {

                    xpString = "//*[@oid='" + oid + "']";

                }

                else {

                    xpString = "//*/" + tag + "[@oid='" + oid + "']";

                }

                addNode = xpath.selectSingleNode(dom.getDocumentElement(), xpString);

            }

        }

        catch (Exception exc) {

            exc.printStackTrace();

            throw exc;

        }

        return addNode;

    }

    protected void dbDeleteClassFolder(int offering_id,

    boolean commitFlag,

    Connection conn)

    throws SQLException {

        PreparedStatement pdel = null;

        try {

            pdel = conn.prepareStatement(deleteClassFoldersSQL);

            pdel.setInt(1, offering_id);

            pdel.executeUpdate();  // doesn't matter if anything was deleted or not

            if (commitFlag) {

                conn.commit();

            }

            pdel.close();

            pdel = null;

        }

        catch (SQLException se) {

            se.printStackTrace();

        }

        finally {

            try {

                if (pdel != null) {

                    pdel.close();

                }

            }

            catch (SQLException se2) {

                se2.printStackTrace();

            }

        }

    }

    protected void dbInsertClassFolder(int offering_id,

    String xmlString,

    boolean commitFlag,

    Connection conn)

    throws SQLException {

        PreparedStatement pins = null;

        try {
	    String buf = null;
            // break up the XML and write to DB
            int bufsize  = xmlString.length();
            int bufindex = 0;
            int counter  = 1;

            pins = conn.prepareStatement(insertClassFoldersSQL);
            while (bufsize > CF_XML_DB_SIZE) {
                pins.setInt(1, offering_id);
                pins.setInt(2, counter);
		buf = xmlString.substring(bufindex, counter * CF_XML_DB_SIZE);
		pins.setString(3, buf);

                pins.executeUpdate();
                pins.clearParameters();

		counter++;
                bufsize -= CF_XML_DB_SIZE;
                bufindex += CF_XML_DB_SIZE;

            }

            // write the last part of the string
	    if (bufsize > 0) {
		pins.setInt(1, offering_id);
		pins.setInt(2, counter++);
		buf = xmlString.substring(bufindex);
		pins.setString(3, buf);
		pins.executeUpdate();
	    }
        
            if (commitFlag) {
                conn.commit();
            }

            pins.close();
            pins = null;

        }

        catch (SQLException se) {

            se.printStackTrace();

        }

        finally {

            try {

                if (pins != null) {

                    pins.close();

                }

            }

            catch (SQLException se2) {

                se2.printStackTrace();

            }

        }

    }

    /** Adds a folder/document/url to the class folder for this offering by adding the item at the end of the specified path. */

    public String addClassFolder(Offering offering,

    String oid,

    String type,

    String name,

    String desc,

    String href_or_file,

    Map args,

    Object uFile,

    Connection conn) {

        DocumentBuilderFactory dbf = null;

        DocumentBuilder db = null;

        Document cfsDOM    = null;

        String xmlString   = null;

        int offering_id    = (int)offering.getId();

        String filename    = null;

        String filetype    = null;

        try {

            // we're going to need DOM stuff

            dbf = DocumentBuilderFactory.newInstance();

            db = dbf.newDocumentBuilder();

            cfsDOM = getClassFolderDOM(offering, conn);

            // put the DOM's together

            // 1. Find where to hang this node off of

            // 2. generate a new oid

            // 3. add the node

            // 4. write to DB

            // find the node to hang the cfi off of. If oid == null then off root

            // even if there is no ClassFolder for this offering at this time, there

            // will be a root node (empty ClassFolder document)

            Node addNode = findNodeByOID(cfsDOM, oid, "folder");

            // if we couldn't find the node, add to the top-level

            if (addNode == null) {

                // shouldn't happen; says we are adding to a non-existent node

                // if so, add to the root

                XPathAPI xpath = new XPathAPI();

                addNode = xpath.selectSingleNode(cfsDOM.getDocumentElement(),

                "class-folders");

            }

            // generate a new oid

            String newOID = offering_id + "-";

            newOID = ClassFolderUtil.genOID(newOID, null);

            args.put(newOID, "N");  // by default do not fold

            if (type.equals("file")) {

                // I know, string concats, very bad

                String dir = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.relatedresources.uploadDir") + File.separator + offering_id;

                MultipartDataSource ds =

                ClassFolderUtil.uploadFile(dir, newOID, uFile);

                if (ds == null || ds.getName() == null || ds.getName().length() == 0) {

                    //return "ERROR: uploading file";

                    return null;

                }

                if (ds.getInputStream().available() > sizeLimit) {

                    throw new IOException("File exceeds max size limit!");

                }

                filetype = ds.getContentType();

                href_or_file = ds.getName();

            }

            // create the XML for the Class Folder Item

            String cfiXML = ClassFolderUtil.createXML(newOID,

            type, name, desc, href_or_file, filetype);

            Document cfiDOM = db.parse(new InputSource(new StringReader(cfiXML)));

            // import the new node into the document

            Node newNode = cfsDOM.importNode(cfiDOM.getDocumentElement(), true);

            // finally we can add the node

            addNode.appendChild(newNode);

            // now we write the XML back to the DB

            NodeToString nts = new NodeToString(cfsDOM);
            nts.setPrintNewLines(false);
            xmlString = nts.toString();

            // we have to delete and add as the xml break up may differ even if this

            // is an existing class offering

            dbDeleteClassFolder(offering_id, false, conn);

            dbInsertClassFolder(offering_id, xmlString, true, conn);

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        // return the modified DOM to the caller so it can render the tree

        // without having to hit the DB again

        return xmlString;

    }

    /**
     * Deletes a folder/document/url from the class folder. If the object
     * is a folder, ensures all items underneath it's subtree are removed.
     */

    public String deleteClassFolder(Offering offering,

    String oid,

    Connection conn) {

        DocumentBuilderFactory dbf = null;

        DocumentBuilder db = null;

        Document cfsDOM    = null;

        String xmlString   = null;

        int offering_id    = (int)offering.getId();

        try {

            // we're going to need DOM stuff

            dbf = DocumentBuilderFactory.newInstance();

            db = dbf.newDocumentBuilder();

            cfsDOM = getClassFolderDOM(offering, conn);

            // put the DOM's together

            // 1. Find this node

            // 2. delete from DB

            // 3. return new XML

            // find the node

            Node delNode = findNodeByOID(cfsDOM, oid, "");

            if (delNode != null) { // shouldn't happen

                Node parNode = delNode.getParentNode();

                // delete the node

                if (parNode != null) { // definitely should never happen

                    parNode.removeChild(delNode);

                }

                // If the object is a file, delete the file from the filesystem

                if ("file".equals(delNode.getNodeName())) {

                    Node fNode = delNode.getFirstChild();

                    // filename is the 3rd child always

                    fNode = fNode.getNextSibling();

                    fNode = fNode.getNextSibling();

                    // get all the child text nodes and create the file name

                    StringBuffer fileSB = new StringBuffer();

                    NodeList nl = fNode.getChildNodes();

                    for (int i = 0; i < nl.getLength(); i++) {

                        fileSB.append((String)(nl.item(i).getNodeValue()));

                    }

                    // I know, string concats, very bad

                    String dir = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.relatedresources.uploadDir") + File.separator + offering_id;

                    ClassFolderUtil.deleteUploadedFile(dir, oid + '_' + fileSB.toString().trim());

                }

            }

            // now we write the XML back to the DB

            NodeToString nts = new NodeToString(cfsDOM);
            nts.setPrintNewLines(false);
            xmlString = nts.toString();

            // we have to delete and add as the xml break up may differ even if this

            // is an existing class offering

            dbDeleteClassFolder(offering_id, false, conn);

            dbInsertClassFolder(offering_id, xmlString, true, conn);

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return xmlString;

    }

    /** Update the item at the end of the specified path with the new item */

    public String updateClassFolder(Offering offering,

    String oid,

    String name,

    String desc,

    String url,

    Connection conn) {

        DocumentBuilderFactory dbf = null;

        DocumentBuilder db = null;

        Document cfsDOM    = null;

        String xmlString   = null;

        int offering_id    = (int)offering.getId();

        try {

            // we're going to need DOM stuff

            dbf = DocumentBuilderFactory.newInstance();

            db = dbf.newDocumentBuilder();

            cfsDOM = getClassFolderDOM(offering, conn);

            // 1. Find the node to edit and modify

            // 2. delete/insert DB

            // 3. return new XML

            // find the node

            Node editNode = findNodeByOID(cfsDOM, oid, "");

            if (editNode != null) { // shouldn't happen

                XPathAPI xpath = new XPathAPI();

                Node textNode = null;

                if (name != null && name.trim().length() > 0) {

                    // get the <name> node

                    textNode = xpath.selectSingleNode(editNode, "name");

                    if (textNode != null) {

                        textNode = textNode.getFirstChild(); // text

                        if (textNode != null) {

                            textNode.setNodeValue(name);

                        }

                    }

                }

                if (url != null && url.trim().length() > 0) {

                    // get the <name> node

                    textNode = xpath.selectSingleNode(editNode, "hyperlink");

                    if (textNode != null) {

                        textNode = textNode.getFirstChild(); // text

                        if (textNode != null) {

                            textNode.setNodeValue(url);

                        }

                    }

                }

                // same for description - but get second node

                // get the <desc> node

                textNode = xpath.selectSingleNode(editNode, "description");

                if (textNode != null) {

                    textNode = textNode.getFirstChild(); // text

                    if (textNode != null) {

                        textNode.setNodeValue(desc);

                    }

                }

            }

            // now we write the XML back to the DB

            NodeToString nts = new NodeToString(cfsDOM);
            nts.setPrintNewLines(false);
            xmlString = nts.toString();

            // we have to delete and add as the xml break up may differ even if this

            // is an existing class offering

            dbDeleteClassFolder(offering_id, false, conn);

            dbInsertClassFolder(offering_id, xmlString, true, conn);

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return xmlString;

    }

    /** toggle the folded = Y/N flag on the folder and all subelements */

    public String toggleClassFolder(Offering offering,

    Map args,

    String oid,

    Connection conn) {

        String xmlString = null;

        try {

            // get the class folders, modify the args folded map to change the entire subtree

            Document cfsDOM = getClassFolderDOM(offering, conn);

            NodeToString nts = new NodeToString(cfsDOM);
            nts.setPrintNewLines(false);
            xmlString = nts.toString();

            // first find the Node with the given oid

            Element toggleRootElem = (Element)findNodeByOID(cfsDOM, oid, "");

            if (toggleRootElem == null) { // should never happen

                return xmlString;

            }

            // get the Node's toggle key and find out if it is folded or not

            String keyValue = toggleRootElem.getAttribute("folded");

            if (keyValue != null) {

                keyValue = keyValue.trim();

                keyValue = keyValue.substring(1, keyValue.length() - 1);

            }

            String foldedValue = (String)args.get(keyValue);

            if (foldedValue == null) {

                // nothing in args, presume unfolded. If XSL changes to initially fold, then

                // this has to be flipped.

                foldedValue = "N";

            }

            // toggle the folded flag

            if (foldedValue.equalsIgnoreCase("N")) {

                foldedValue = "Y";

            }

            else {

                foldedValue = "N";

            }

            // add the root node

            args.put(keyValue, foldedValue);

            // if folded, unfold (expand) it and all descendants

            // else unfolded, so fold (collapse) on up

            XPathAPI xpath = new XPathAPI();

            NodeIterator toggleNodes = xpath.selectNodeIterator(toggleRootElem, ".//*[@oid]");

            Node nextNode = toggleNodes.nextNode();

            while (nextNode != null) {

                keyValue = ((Element)nextNode).getAttribute("folded");

                if (keyValue != null) { // should always be the case

                    keyValue = keyValue.trim();

                    keyValue = keyValue.substring(1, keyValue.length() - 1);

                    args.put(keyValue, foldedValue);

                }

                nextNode = toggleNodes.nextNode();

            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        // now when we return we'll have the xmlString from the DB with

        // squigglies and an updated set of args

        return xmlString;

    }

    public Note getNote(int noteID, Connection conn) throws SQLException {

        Note note = null;

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        try {

            pstmt = conn.prepareStatement(getNoteSQL);

            int i = 1;

            pstmt.setInt(i++, noteID);

            rs = pstmt.executeQuery();

            int userID = 0;

            String body = null;

            Date date = null;

            if (rs != null && rs.next()) {

                i = 2;

                userID = rs.getInt(i++);

                body = rs.getString(i++);

                date = rs.getDate(i++);

                note = new NoteImpl(noteID, userID, body, date.toString());

            }

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            try {
            if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
            if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return note;

    }

    public boolean updateNote(int noteID, String body, Connection conn)
    throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(updateNoteSQL);
            int i = 1;

            pstmt.setString(i++, body);
            pstmt.setTimestamp(i++, ts.getTimestamp());
            pstmt.setInt(i++, noteID);
            result = pstmt.executeUpdate() == 1;

        } 
	catch (SQLException se) {
            se.printStackTrace();
        } 
	catch (FactoryCreateException fce) {
            fce.printStackTrace();
        }
	finally {
            try {
            if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
            if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean addNote(int userID, String body, Connection conn)
    throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            TimeService ts = TimeServiceFactory.getService();
            pstmt = conn.prepareStatement(insertNoteSQL);
            int i = 1;

            pstmt.setInt(i++, userID);
            pstmt.setString(i++, body);
            pstmt.setTimestamp(i++, ts.getTimestamp());

            result = pstmt.executeUpdate() == 1;

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (FactoryCreateException fce) {
            fce.printStackTrace();
        } finally {
            try {
            if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
            if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean deleteNote(int noteID, Connection conn) throws SQLException {

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        boolean result = false;

        try {

            pstmt = conn.prepareStatement(deleteNoteSQL);

            int i = 1;

            pstmt.setInt(i++, noteID);

            result = pstmt.executeUpdate() == 1;

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            try {
            if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
            if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return result;

    }

    public List getAllNotes(int userId, Connection conn) throws SQLException {

        List notes = new ArrayList();

        PreparedStatement pstmt = null;

        ResultSet rs = null;

        try {

            pstmt = conn.prepareStatement(getAllNotesSQL);

            int i = 1;

            pstmt.setInt(i++, userId);

            rs = pstmt.executeQuery();

            int noteID = 0;

            int userID = 0;

            String body = null;

            Date date = null;

            while (rs != null && rs.next()) {

                i = 1;

                noteID = rs.getInt(i++);

                userID = rs.getInt(i++);

                body = rs.getString(i++);

                date = rs.getDate(i++);

                notes.add(new NoteImpl(noteID, userID, body, date.toString()));

            }

        } catch (SQLException se) {

            se.printStackTrace();

        } finally {

            try {
            if (rs != null) rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
            if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return notes;

    }
}

