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

package net.unicon.portal.channels.bookmarks;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

import net.unicon.portal.common.AcademusMultithreadedChannel;
import net.unicon.portal.common.cdm.ChannelDataManager;
import net.unicon.portal.util.PeepholeManager;
import net.unicon.portal.util.RenderingUtil;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedCharacterChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.interactivebusiness.portal.VersionResolver;

/**
 * Bookmarks channel
 * 
 * @author Peter Kharchenko
 * @author Steven Toth
 * @author Bernie Durfee
 * @version $LastChangedRevision$
 */
public class CBookmarks extends AcademusMultithreadedChannel
        implements IMultithreadedCharacterChannel {
    // A DOM document where all the bookmark information will be contained
    protected DocumentImpl m_bookmarksXML;
    private final XSLT xslt;

    // Location of the stylesheet list file
    private final String sslLocation = "CBookmarks.ssl";

    // Define some constants to keep the state of the channel
    private final int VIEWMODE = 0;
    private final int EDITMODE = 1;
    private final int EDITNODEMODE = 3;
    private final int ADDNODEMODE = 4;
    private final int MOVENODEMODE = 5;
    private final int DELETENODEMODE = 6;
    private final int PEEPHOLEMODE = 7;

    // Start out in view mode by default
    private int m_currentState = 7;

    // Keep track of the node that the user is currently working with
    private String m_activeNodeID = null;
    private String m_activeNodeType = null;
    
    public CBookmarks() throws ResourceMissingException {
        super();
        this.xslt = new XSLT(this);
    }

    private DocumentImpl getBookmarkXML(String upId) {
        Connection connection = null;
        // Return the cached bookmarks if they've already been read in
        if (getDocument(upId) != null) { 
        	
        	return (DocumentImpl)super.getDocument(upId); 
        }
        try {
            String inputXML = null;
            // Create a new parser for the incoming bookmarks document
            DOMParser domParser = new DOMParser();
            // Get a connection to the database
            connection = getConnection();
            // Get the current user's ID
            ChannelStaticData staticData = getStaticData(upId);
            int userid = staticData.getPerson().getID();
            // Attempt to retrieve the user's bookmark's
            String query = "SELECT BOOKMARK_XML, PORTAL_USER_ID FROM UPC_BOOKMARKS WHERE PORTAL_USER_ID="
                    + userid;
            Statement stmt = connection.createStatement();
            try {
                // Get the result set
                LogService.instance().log(LogService.DEBUG,
                        "CBookmarks.getBookmarkXML(): " + query);
                ResultSet rs = stmt.executeQuery(query);
                try {
                    if (rs.next()) {
                        // If a result came back then use that for the XML...
                        inputXML = rs.getString("BOOKMARK_XML");
                    }
                } finally {
                    if (rs != null)
                        rs.close();
                }
            } finally {
                if (stmt != null)
                    stmt.close();
            }
            if (inputXML == null || inputXML.length() == 0) {
                // ...or else use the bookmarks from the default user
                inputXML = getDefaultBookmarks(upId);
            }

            // Turn validation on for the DOM parser to make sure it reads the
            // DTD
            domParser
                    .setFeature("http://xml.org/sax/features/validation", true);
            domParser.setEntityResolver(new org.jasig.portal.utils.DTDResolver(
                    "xbel-1.0.dtd"));
            // Parse the XML document containing the user's bookmarks
            domParser.parse(new InputSource(new StringReader(inputXML)));
            // Cache the bookmarks DOM locally
            m_bookmarksXML = (DocumentImpl) domParser.getDocument();
            setDocument(upId, m_bookmarksXML);

            // got the XML from the database
            // reset the folded values in the DOM
            NodeList folderList = m_bookmarksXML.getElementsByTagName("folder");
            for (int i = 0; i < folderList.getLength(); i++) {
                Element folderElement = (Element) folderList.item(i);
                if (folderElement != null
                        && folderElement.getNodeName().equals("folder")) {
                    folderElement.setAttribute("folded", "yes");
                }
            }

        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        } finally {
            // Release the database connection
            if (connection != null) {
                releaseConnection(connection);
            }
        }
        // Return what is cached
        return (m_bookmarksXML);
    }

    private String getDefaultBookmarks(String upId) {
        Connection connection = null;
        String inputXML = null;
        try {
            // Get a connection to the database
            connection = getConnection();
            // Get the bookmarks for the 'default' user
            ChannelStaticData staticData = getStaticData(upId);
            IPerson person = staticData.getPerson();
            String defaultUser = (String) person
                    .getAttribute("uPortalTemplateUserName");
            if (defaultUser == null || defaultUser.equals("")) {
                defaultUser = "system";
            }
            String query = "SELECT BOOKMARK_XML, PORTAL_USER_ID FROM UPC_BOOKMARKS WHERE PORTAL_USER_ID = (SELECT USER_ID FROM UP_USER WHERE USER_NAME = ? )";
                    
            PreparedStatement stmt = connection.prepareStatement(query);
            try {
                // Try to get the 'default' bookmarks from the database
                LogService.instance().log(LogService.DEBUG,
                        "CBookmarks.getDefaultBookmarks(): " + query);
                stmt.setString(1, defaultUser);
                ResultSet rs = stmt.executeQuery();
                try {
                    if (rs.next()) {
                        // Use the 'default' user's bookmarks...
                        inputXML = rs.getString("BOOKMARK_XML");
                    } else {
                        // Generate the XML here as a last resort
                        StringBuffer strbuf = new StringBuffer();
                        strbuf.append("<?xml version=\"1.0\"?>");
                        strbuf
                                .append("<!DOCTYPE xbel PUBLIC \"+//IDN python.org//DTD XML Bookmark Exchange Language 1.0//EN//XML\" \"http://www.python.org/topics/xml/dtds/xbel-1.0.dtd\">");
                        strbuf.append("<xbel>");
                        strbuf.append("  <title>Default Bookmarks</title>");
                        strbuf.append("  <info>");
                        strbuf.append("    <metadata owner=\""
                                + staticData.getPerson().getID() + "\"/>");
                        strbuf.append("  </info>");
                        strbuf.append("</xbel>");
                        inputXML = strbuf.toString();

                        LogService
                                .instance()
                                .log(LogService.WARN,
                                        "CBookmarks.getDefaultBookmarks(): Could not find bookmarks for 'default' user");
                    }
                } finally {
                    if (rs != null)
                        rs.close();
                }
                // Now add a row to the database for the user
                String insert = "INSERT INTO UPC_BOOKMARKS (PORTAL_USER_ID, BOOKMARK_XML) VALUES (? , ?)";
                LogService.instance().log(LogService.DEBUG,
                        "CBookmarks.getDefaultBookmarks(): " + insert);
                
                stmt = connection.prepareStatement(insert);
                stmt.setInt(1, staticData.getPerson().getID());
                stmt.setString(2, inputXML);
                stmt.executeUpdate();
            } finally {
                if (stmt != null)
                    stmt.close();
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
            if (inputXML == null) {
                // ...or else just start with an empty set of bookmarks
                LogService
                        .instance()
                        .log(
                                LogService.ERROR,
                                "CBookmarks.getDefaultBookmarks() - Could not retrieve default bookmark xml, using blank xml.");
                inputXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xbel></xbel>";
            }
        }
        return (inputXML);
    }

    protected void saveXML(String upId) {
        if (m_bookmarksXML == null) {
            return;
        }
        Connection connection = getConnection();
        try {
            StringWriter stringWriter = new StringWriter();
            // Serialize the DOM tree to a string
            XMLSerializer xmlSerializer = new XMLSerializer(stringWriter,
                    new OutputFormat(m_bookmarksXML));
            xmlSerializer.serialize(m_bookmarksXML);
            // Get a connection to the database
            ChannelStaticData staticData = getStaticData(upId);
            String update = "UPDATE UPC_BOOKMARKS SET BOOKMARK_XML = ?"
                    + " WHERE PORTAL_USER_ID = "
                    + staticData.getPerson().getID();
            PreparedStatement pstmt = connection.prepareStatement(update);
            try {
                LogService.instance().log(LogService.DEBUG,
                        "CBookmarks.saveXML(): " + update);
                pstmt.setString(1, stringWriter.toString());
                pstmt.executeUpdate();
            } finally {
                if (pstmt != null)
                    pstmt.close();
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        } finally {
            releaseConnection(connection);
        }
    }

    /**
     * Render the user's layout based on the current state of the channel
     */
    public void renderCharacters(PrintWriter out, String upId) {

        ChannelRuntimeData runtimeData = getRuntimeData(upId);

        ChannelStaticData staticData = getStaticData(upId);
/*
// spill parameters (uncomment to see)...
System.out.println("#### SPILLING PARAMETERS...");
Iterator it = runtimeData.keySet().iterator();
while (it.hasNext()) {
    String key = (String) it.next();
    String value = runtimeData.getParameter(key);
    System.out.println("\t" + key.toString() + "=" + value);
}*/
        // Retrieve the command passed in by the user
        String command = runtimeData.getParameter("command");
        
        // Retrieve the state set by the user
        String state = super.getSheetName(upId);
        if (state == null) {
            m_currentState = PEEPHOLEMODE;
        } else {
            m_currentState = evaluateState(state);
        }
        
        if (command != null) {

            if (command.equals("main")) {

                m_currentState = VIEWMODE;
                m_activeNodeID = null;

            } else if (command.equals("fold")) {
                // Get the ID of the specified folder to close
                String folderID = runtimeData.getParameter("ID");
                if (folderID != null) {
                    Element folderElement = getBookmarkXML(upId)
                            .getElementById(folderID);
                    if (folderElement != null
                            && folderElement.getNodeName().equals("folder")) {
                        folderElement.setAttribute("folded", "yes");
                    }
                }
            } else if (command.equals("unfold")) {
                // Get the ID of the specified folder to open
                String folderID = runtimeData.getParameter("ID");
                if (folderID != null) {
                    Element folderElement = getBookmarkXML(upId)
                            .getElementById(folderID);
                    if (folderElement != null
                            && folderElement.getNodeName().equals("folder")) {
                        folderElement.setAttribute("folded", "no");
                    }
                }
            } else if (command.equals("View")) {
                // Switch to view mode
                m_currentState = VIEWMODE;
                m_activeNodeID = null;
            } else if (command.equals("Edit")) {
                // Switch to edit mode
                m_currentState = EDITMODE;
                m_activeNodeID = null;
            } else if (command.equals("AddBookmark")) {
                if (m_currentState != ADDNODEMODE || !m_activeNodeType.equals("bookmark")) {
                    // Switch to add bookmark mode
                    m_currentState = ADDNODEMODE;
                    m_activeNodeID = null;
                    m_activeNodeType = "bookmark";
                } else {
                    String submitButton = runtimeData
                            .getParameter("SubmitButton");
                    if (submitButton != null && submitButton.equals("Cancel")) {
                        // The user pressed the cancel button so return to view
                        // mode
                        m_activeNodeID = null;
                        m_activeNodeType = null;
                        m_currentState = VIEWMODE;
                    } else if (submitButton != null
                            && submitButton.equals("Add")) {
                        // Check for the incoming parameters
                        String bookmarkTitle = runtimeData
                                .getParameter("BookmarkTitle");
                        String bookmarkURL = runtimeData
                                .getParameter("BookmarkURL");
                        String bookmarkDesc = runtimeData
                                .getParameter("BookmarkDescription");
                        String folderID = runtimeData
                                .getParameter("FolderRadioButton");
                        if (bookmarkTitle == null || bookmarkTitle.length() < 1) {
                        } else if (bookmarkURL == null
                                || bookmarkURL.length() < 1) {
                        } else if (folderID == null || folderID.length() < 1) {
                        } else {
                            Element folderElement;
                            if (folderID.equals("RootLevel")) {
                                folderElement = (Element) m_bookmarksXML
                                        .getElementsByTagName("xbel").item(0);
                            } else {
                                folderElement = m_bookmarksXML
                                        .getElementById(folderID);
                            }
                            if (folderElement == null) {
                            } else {
                                // Build the bookmark XML DOM
                                Element bookmarkElement = m_bookmarksXML
                                        .createElement("bookmark");
                                bookmarkElement.setAttribute("href",
                                        bookmarkURL);
                                bookmarkElement.setAttribute("id",
                                        createUniqueID());
                                // Create the title element
                                Element titleElement = m_bookmarksXML
                                        .createElement("title");
                                titleElement.appendChild(m_bookmarksXML
                                        .createTextNode(bookmarkTitle));
                                bookmarkElement.appendChild(titleElement);
                                // Create the desc element
                                Element descElement = m_bookmarksXML
                                        .createElement("desc");
                                descElement.appendChild(m_bookmarksXML
                                        .createTextNode(bookmarkDesc));
                                bookmarkElement.appendChild(descElement);
                                folderElement.appendChild(bookmarkElement);
                                // Notify the DOM of the new ID
                                m_bookmarksXML.putIdentifier(bookmarkElement
                                        .getAttribute("id"), bookmarkElement);
                                // The user pressed the cancel button so return
                                // to view mode
                                m_activeNodeID = null;
                                m_activeNodeType = null;
                                // Return to view mode
                                m_currentState = VIEWMODE;
                                // Save the user's XML
                                saveXML(upId);
                            }
                        }
                    }
                }
            } else if (command.equals("AddFolder")) {
                if (m_currentState != ADDNODEMODE || !m_activeNodeType.equals("folder")) {
                    // Switch to add bookmark mode
                    m_currentState = ADDNODEMODE;
                    m_activeNodeID = null;
                    m_activeNodeType = "folder";
                } else {
                    String submitButton = runtimeData
                            .getParameter("SubmitButton");
                    if (submitButton != null && submitButton.equals("Cancel")) {
                        // The user pressed the cancel button so return to view
                        // mode
                        m_activeNodeID = null;
                        m_activeNodeType = null;
                        m_currentState = VIEWMODE;
                    } else if (submitButton != null
                            && submitButton.equals("Add")) {
                        // Check for the incoming parameters
                        String folderTitle = runtimeData
                                .getParameter("FolderTitle");
                        String folderID = runtimeData
                                .getParameter("FolderRadioButton");
                        if (folderTitle == null || folderTitle.length() < 1) {
                        } else if (folderID == null || folderID.length() < 1) {
                        } else {
                            Element folderElement;
                            if (folderID.equals("RootLevel")) {
                                folderElement = (Element) m_bookmarksXML
                                        .getElementsByTagName("xbel").item(0);
                            } else {
                                folderElement = m_bookmarksXML
                                        .getElementById(folderID);
                            }
                            if (folderElement == null) {
                            } else {
                                // Build the new folder XML node
                                Element newFolderElement = m_bookmarksXML
                                        .createElement("folder");
                                newFolderElement.setAttribute("id",
                                        createUniqueID());
                                // Create the title element
                                Element titleElement = m_bookmarksXML
                                        .createElement("title");
                                titleElement.appendChild(m_bookmarksXML
                                        .createTextNode(folderTitle));
                                newFolderElement.appendChild(titleElement);
                                folderElement.appendChild(newFolderElement);
                                // Notify the DOM of the new ID
                                m_bookmarksXML.putIdentifier(newFolderElement
                                        .getAttribute("id"), newFolderElement);
                                // The user pressed the cancel button so return
                                // to view mode
                                m_activeNodeID = null;
                                m_activeNodeType = null;
                                // Return to view mode
                                m_currentState = VIEWMODE;
                                // Save the user's XML
                                saveXML(upId);
                            }
                        }
                    }
                }
            } else if (command.equals("MoveNode")) {
                m_activeNodeID = runtimeData.getParameter("ID");
                m_currentState = MOVENODEMODE;
            } else if (command.equals("EditNode")) {
                m_activeNodeID = runtimeData.getParameter("ID");
                m_currentState = EDITNODEMODE;
            } else if (command.equals("EditBookmark")) {
            	 if (m_currentState != EDITNODEMODE || !m_activeNodeType.equals("bookmark")) {
                    // Switch to add bookmark mode
                    m_currentState = EDITNODEMODE;
                    m_activeNodeID = runtimeData.getParameter("ID");
                    m_activeNodeType = "bookmark";
                } else {
                    String submitButton = runtimeData
                            .getParameter("SubmitButton");
                    if (submitButton != null && submitButton.equals("Cancel")) {
                        // The user pressed the cancel button so return to view
                        // mode
                        m_activeNodeID = null;
                        m_activeNodeType = null;
                        m_currentState = VIEWMODE;
                    } else if (submitButton != null
                            && submitButton.equals("Edit")) {
                        // Check for the incoming parameters
                        String bookmarkTitle = runtimeData
                                .getParameter("BookmarkTitle");
                        String bookmarkURL = runtimeData
                                .getParameter("BookmarkURL");
                        String bookmarkDesc = runtimeData
                                .getParameter("BookmarkDescription");
                        String folderID = runtimeData
                                .getParameter("FolderRadioButton");
                        String bookmarkId = m_activeNodeID;
                        if (bookmarkTitle == null || bookmarkTitle.length() < 1) {
                        } else if (bookmarkURL == null
                                || bookmarkURL.length() < 1) {
                        } else if (folderID == null || folderID.length() < 1) {
                        } else {
                            Element folderElement;
                            if (folderID.equals("RootLevel")) {
                                folderElement = (Element) m_bookmarksXML
                                        .getElementsByTagName("xbel").item(0);
                            } else {
                                folderElement = m_bookmarksXML
                                        .getElementById(folderID);
                            }
                            if (folderElement == null) {
                            } else {
                            	Element bookmarkElement = m_bookmarksXML
                                .getElementById(bookmarkId);
                            	if (bookmarkElement != null
                            			&& bookmarkElement.getNodeName()
                                        .equals("bookmark")) {
                            		bookmarkElement.getParentNode()
                                    .removeChild(bookmarkElement);
                        }
                                // Build the bookmark XML DOM
                            	bookmarkElement = m_bookmarksXML
                                .createElement("bookmark");
                            		bookmarkElement.setAttribute("href",
                            				bookmarkURL);
                            			bookmarkElement.setAttribute("id",
                            					bookmarkId);
                                // Create the title element
                                Element titleElement = m_bookmarksXML
                                        .createElement("title");
                                titleElement.appendChild(m_bookmarksXML
                                        .createTextNode(bookmarkTitle));
                                bookmarkElement.appendChild(titleElement);
                                // Create the desc element
                                Element descElement = m_bookmarksXML
                                        .createElement("desc");
                                descElement.appendChild(m_bookmarksXML
                                        .createTextNode(bookmarkDesc));
                                bookmarkElement.appendChild(descElement);
                                folderElement.appendChild(bookmarkElement);
                                // Notify the DOM of the new ID
                                m_bookmarksXML.putIdentifier(bookmarkElement
                                        .getAttribute("id"), bookmarkElement);
                                // The user pressed the cancel button so return
                                // to view mode
                                m_activeNodeID = bookmarkId;
                                m_activeNodeType = null;
                                // Return to view mode
                                m_currentState = VIEWMODE;
                                // Save the user's XML
                                saveXML(upId);
                            }
                        }
                    }
                }
            } else if (command.equals("DeleteBookmark")) {
                if (m_currentState != DELETENODEMODE || !m_activeNodeType.equals("bookmark")) {
                    m_currentState = DELETENODEMODE;
                    m_activeNodeType = "bookmark";
                } else {
                    String submitButton = runtimeData
                            .getParameter("SubmitButton");
                    if (submitButton != null) {
                        if (submitButton.equals("Cancel")) {
                            m_currentState = VIEWMODE;
                            m_activeNodeType = null;
                        } else if (submitButton.equals("Delete")) {
                            // Run through the passed in parameters and delete
                            // the bookmarks
                            Enumeration e = runtimeData.keys();
                            while (e.hasMoreElements()) {
                                String key = (String) e.nextElement();
                                if (key.startsWith("BookmarkCheckbox#")) {
                                    String bookmarkID = key.substring(17);
                                    Element bookmarkElement = m_bookmarksXML
                                            .getElementById(bookmarkID);
                                    if (bookmarkElement != null
                                            && bookmarkElement.getNodeName()
                                                    .equals("bookmark")) {
                                        bookmarkElement.getParentNode()
                                                .removeChild(bookmarkElement);
                                    }
                                }
                            }
                            saveXML(upId);
                            m_currentState = VIEWMODE;
                            m_activeNodeType = null;
                        } else if (submitButton.equals("ConfirmDelete")) {
                        }
                    }
                }
            } else if (command.equals("DeleteFolder")) {
                if (m_currentState != DELETENODEMODE || !m_activeNodeType.equals("folder")) {
                    m_currentState = DELETENODEMODE;
                    m_activeNodeType = "folder";
                } else {
                    String submitButton = runtimeData
                            .getParameter("SubmitButton");
                    if (submitButton != null) {
                        if (submitButton.equals("Cancel")) {
                            m_currentState = VIEWMODE;
                            m_activeNodeType = null;
                        } else if (submitButton.equals("Delete")) {
                            // Run through the passed in parameters and delete the bookmarks
                            Enumeration e = runtimeData.keys();
                            while (e.hasMoreElements()) {
                                String key = (String) e.nextElement();
                                if (key.startsWith("FolderCheckbox#")) {
                                    // The ID should come after the FolderCheckbox# part
                                    String bookmarkID = key.substring(15);
                                    // Find the folder in the DOM tree
                                    Element folderElement = m_bookmarksXML
                                            .getElementById(bookmarkID);
                                    // Remove the folder from the DOM tree
                                    if (folderElement != null
                                            && folderElement.getNodeName()
                                                    .equals("folder")) {
                                        folderElement.getParentNode()
                                                .removeChild(folderElement);
                                    }
                                }
                            }
                            saveXML(upId);
                            m_currentState = VIEWMODE;
                            m_activeNodeType = null;
                        } else if (submitButton.equals("ConfirmDelete")) {
                        }
                    }
                }
            }
        } /*else {
            this.m_currentState = PEEPHOLEMODE;
        }*/
        try {
            // Render content based on the current state of the channel
            switch (m_currentState) {
            case PEEPHOLEMODE:
                renderPeepholeMode(out, upId);
                super.setSheetName(upId, "PEEPHOLEMODE");
                break;
            case VIEWMODE:
                renderViewModeXML(out, upId);
                super.setSheetName(upId, "VIEWMODE");
                break;
            case EDITMODE:
                renderEditModeXML(out, upId);
                super.setSheetName(upId, "EDITMODE");
                break;
            case EDITNODEMODE:
                renderEditNodeXML(out, upId);
                super.setSheetName(upId, "EDITNODEMODE");
                break;
            case ADDNODEMODE:
                renderAddNodeXML(out, upId);
                super.setSheetName(upId, "ADDNODEMODE");
                break;
            case MOVENODEMODE:
                renderMoveNodeXML(out, upId);
                super.setSheetName(upId, "MOVENODEMODE");
                break;
            case DELETENODEMODE:
                renderDeleteNodeXML(out, upId);
                super.setSheetName(upId, "DELETENODEMODE");
                break;
            }
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    /**
     * The caching scheme caches only the latest content of the bookmark
     * channel. It creates a caching entry based on a channel and user ID
     * combination (one cache entry for each bookmark user).
     */
    public ChannelCacheKey generateKey(String upId) {

        ChannelCacheKey k = new ChannelCacheKey();
        ChannelStaticData staticData = getStaticData(upId);
        StringBuffer sbKey = new StringBuffer();

        k.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);

        sbKey.append(this.getClass().getPackage().getName());
        sbKey.append(":userId:");
        sbKey.append(staticData.getPerson().getID());

        k.setKey(sbKey.toString());

        return k;
    }
    
    public void setStaticData (ChannelStaticData sd, String upId)
    throws PortalException {
        super.setStaticData(sd, upId);
        try {
            int publishId = Integer.parseInt(sd.getChannelPublishId());
            ChannelDefinition cd = ChannelRegistryStoreFactory.
                getChannelRegistryStoreImpl().getChannelDefinition(publishId);
            ChannelDataManager.registerChannelUser(sd.getPerson(),
                null, cd, upId);
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
            throw new PortalException(e);
        }

        IAuthorizationPrincipal principal = VersionResolver.getInstance().
            getPrincipalByPortalVersions(sd.getPerson());
        ChannelDataManager.setAuthorizationPrincipal(upId, principal);
    }


    // Renders the bookmark peephole view as the channel's content
    private void renderPeepholeMode(PrintWriter writer, String upId)
            throws PortalException {
        ChannelRuntimeData runtimeData = getRuntimeData(upId);
        String baseActionURL = runtimeData.getBaseActionURL();

        String peepholeView = PeepholeManager.getInstance().getPeephole(
                this.getClass(), baseActionURL);
        writer.print(peepholeView);

    }

    private void renderViewModeXML(PrintWriter out, String upId)
            throws Exception {
        transformXML(out, "view_mode", getBookmarkXML(upId), upId);
    }

    private void renderEditModeXML(PrintWriter out, String upId)
            throws Exception {
        Hashtable parameters = new Hashtable(2);
        parameters.put("NodeType", m_activeNodeType);
        parameters.put("TreeMode", "EditMode");
        transformXML(out, "edit_mode", getBookmarkXML(upId), parameters, upId);
    }

    private void renderEditNodeXML(PrintWriter out, String upId)
            throws Exception {
        Hashtable parameters = new Hashtable(4);
        if (m_activeNodeType == null) {
            LogService
                    .instance()
                    .log(LogService.ERROR,
                            "CBookmarks.renderEditNodeXML(): No active node type has been set");
            renderViewModeXML(out, upId);
        } else if (m_activeNodeType.equals("bookmark")) {
        	DocumentImpl doc = getBookmarkXML(upId);
	        parameters.put("NodeType", m_activeNodeType);
	        parameters.put("NodeId", m_activeNodeID);
	        parameters.put("TreeMode", "EditBookmark");
	        parameters.put("ParentNodeId", getParentId(m_activeNodeID, doc));
	        transformXML(out, "edit_node", doc, parameters, upId);
        }
    }

    private void renderAddNodeXML(PrintWriter out, String upId)
            throws Exception {
        Hashtable parameters = new Hashtable(1);
        if (m_activeNodeType == null) {
            LogService
                    .instance()
                    .log(LogService.ERROR,
                            "CBookmarks.renderAddNodeXML(): No active node type has been set");
            renderViewModeXML(out, upId);
        } else if (m_activeNodeType.equals("bookmark")) {
            parameters.put("EditMode", "AddBookmark");
            transformXML(out, "add_node", getBookmarkXML(upId), parameters,
                    upId);
        } else if (m_activeNodeType.equals("folder")) {
            parameters.put("EditMode", "AddFolder");
            transformXML(out, "add_node", getBookmarkXML(upId), parameters,
                    upId);
        } else {
            LogService.instance().log(
                    LogService.ERROR,
                    "CBookmarks.renderAddNodeXML(): Unknown active node type - "
                            + m_activeNodeType);
            renderViewModeXML(out, upId);
        }
    }

    private void renderMoveNodeXML(PrintWriter out, String upId)
            throws Exception {
        Hashtable parameters = new Hashtable(2);
        parameters.put("NodeType", m_activeNodeType);
        parameters.put("TreeMode", "MoveNode");
        transformXML(out, "move_node", getBookmarkXML(upId), parameters, upId);
    }

    private void renderDeleteNodeXML(PrintWriter out, String upId)
            throws Exception {
        Hashtable parameters = new Hashtable(1);
        if (m_activeNodeType == null) {
            LogService
                    .instance()
                    .log(LogService.ERROR,
                            "CBookmarks.renderDeleteNodeXML(): No active node type has been set");
            renderViewModeXML(out, upId);
        } else if (m_activeNodeType.equals("bookmark")) {
            parameters.put("EditMode", "DeleteBookmark");
            transformXML(out, "delete_node", getBookmarkXML(upId), parameters,
                    upId);
        } else if (m_activeNodeType.equals("folder")) {
            parameters.put("EditMode", "DeleteFolder");
            transformXML(out, "delete_node", getBookmarkXML(upId), parameters,
                    upId);
        } else {
            LogService.instance().log(
                    LogService.ERROR,
                    "CBookmarks.renderDeleteNodeXML(): Unknown active node type - "
                            + m_activeNodeType);
            renderViewModeXML(out, upId);
        }
    }
    
    private String getParentId(String id, DocumentImpl doc){
    	Element bookmarkElement = doc.getElementById(id);
    	String folderId = ((Element)bookmarkElement.getParentNode()).getAttribute("id");
    	return folderId;
    }

    private void transformXML(PrintWriter out, String stylesheetName,
            DocumentImpl inputXML, String upId) throws Exception {
        transformXML(out, stylesheetName, inputXML, null, upId);
    }

    private void transformXML(PrintWriter out, String stylesheetName,
            DocumentImpl inputXML, Hashtable parameters, String upId)
            throws Exception {
        // Create the parameters hashtable if it is null
        if (parameters == null) {
            parameters = new Hashtable(1);
        }
        ChannelRuntimeData runtimeData = getRuntimeData(upId);
        // Add the baseActionURL to the stylesheet parameters
        parameters.put("baseActionURL", runtimeData.getBaseActionURL());

        RenderingUtil.renderDocument(this, getStaticData(upId).getSerializerName(),
            out, inputXML, sslLocation, stylesheetName,
            runtimeData.getBrowserInfo(), parameters);
    }

    private String createUniqueID() {
        String uniqueID = "n" + System.currentTimeMillis();
        while (m_bookmarksXML.getElementById(uniqueID) != null) {
            uniqueID = "n" + System.currentTimeMillis();
        }
        return (uniqueID);
    }

    private static String makeUrlSafe(String url) {
        // Return if the url is correctly formed
        if (url.toLowerCase().startsWith("http://")) {
            return (url);
        }
        // Make sure the URL is well formed
        if (url.toLowerCase().startsWith("http:/")) {
            url = url.substring(0, 6) + "/" + url.substring(7);
            return (url);
        }
        // If it's a mail link then be sure mailto: is on the front
        if (url.indexOf('@') != -1) {
            if (!url.toLowerCase().startsWith("mailto:")) {
                url = "mailto:" + url;
            }
            return (url);
        }
        // Make sure http:// is on the front
        url = "http://" + url;
        return (url);
    }

    private Connection getConnection() {
        try {
            RDBMServices rdbmServices = new RDBMServices();
            return (rdbmServices.getConnection());
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            return (null);
        }
    }

    private void releaseConnection(Connection connection) {
        try {
            RDBMServices rdbmServices = new RDBMServices();
            rdbmServices.releaseConnection(connection);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }
    
    private int evaluateState(String state) {
        if (state.equals("VIEWMODE")) {
            return VIEWMODE;
        } else if (state.equals("EDITMODE")) {
            return EDITMODE;
        } else if (state.equals("EDITNODEMODE")) {
            return EDITNODEMODE;
        } else if (state.equals("ADDNODEMODE")) {
            return ADDNODEMODE;
        } else if (state.equals("MOVENODEMODE")) {
            return MOVENODEMODE;
        } else if (state.equals("DELETENODEMODE")) {
            return DELETENODEMODE;
        } else {
            return this.PEEPHOLEMODE;
        }
    }

} // end CBookmarks
