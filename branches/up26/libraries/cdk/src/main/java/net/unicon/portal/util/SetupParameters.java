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
package net.unicon.portal.util;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;

import net.unicon.sdk.util.XmlUtils;

import org.jasig.portal.utils.XML;

public class SetupParameters {
    SetupParameters(File baseDir, String target) throws Exception {
        File classesDir = new File( new File(baseDir, "WEB-INF"), "classes");
        File propertiesDir = new File(classesDir, "properties");
        File globalDir = new File(propertiesDir, "global");
        File finalPropertiesFile = new File(globalDir, "global_" + target + ".properties");
        File tomcatHomeDir = null;
        Properties p = new Properties();
        if (!propertiesDir.exists()) {
            System.err.println("Directory " + propertiesDir.getPath() +
            " does not exist!");
            return;
        }
        if (!finalPropertiesFile.exists()) {
            System.err.println("Properties file " +
            finalPropertiesFile.getPath() + " does not exist!");
            return;
        }
        InputStream is = new FileInputStream(finalPropertiesFile);
        p.load(is);
        is.close();
        tomcatHomeDir = new File(p.getProperty("TOMCAT_HOME"));
        Map props = new HashMap();
        File propFile = null;
        System.out.println("Setting up properties...");
        // academus-lms.properties
        System.out.println("academus-lms.properties");
        props.clear();
        propFile = new File(propertiesDir, "academus-lms.properties");
        backupProperties(propFile);
        setProperty(props, "net.unicon.portal.chat.servername", p, "CHAT_SERVER");
        p.setProperty("CHAT_CODEBASE",
        "http://" + p.getProperty("CHAT_SERVER") + ":8080/portal/applets/");
        setProperty(props, "net.unicon.portal.chat.codebase", p, "CHAT_CODEBASE");
        setProperty(props, "net.unicon.portal.chat.portnumber", p, "CHAT_PORT");
        setProperty(props, "net.unicon.portal.util.Debug.debugEnabled", p, "PORTAL_DEBUG");
        setProperty(props, "net.unicon.portal.virtuoso.rmi.server.hostname", p, "VIRTUOSO_SERVER");
        setProperty(props, "JMSProviderURLSubscriber", p, "JMS_SERVER");
        setProperty(props, "JMSSubscriberName", p, "JMS_SUBSCRIBER_NAME");
        setProperty(props, "net.unicon.portal.channels.file.FileService.repositoryBaseDir", p, "FILE_SERVICE_DIRECTORY");
        setProperty(props, "net.unicon.portal.channels.admin.OfferingAdminChannel.workDir", p, "TEMP_DIR");
        replaceProperties(propFile, props);
        // forum.properties
        System.out.println("forum.properties");
        propFile = new File(propertiesDir, "forum.properties");
        backupProperties(propFile);
        replaceWebappsDir(propFile, "ForumLocaleFile", tomcatHomeDir.getPath());
        replaceWebappsDir(propFile, "UserListFile", tomcatHomeDir.getPath());
        // academus-portal.properties
        System.out.println("academus-portal.properties");
        props.clear();
        propFile = new File(propertiesDir, "academus-portal.properties");
        backupProperties(propFile);
        setProperty(props, "DebugLevel", p, "UNICON_DEBUG_LEVEL");
        replaceProperties(propFile, props);
        replaceWebappsDir(propFile, "DebugPropertyFile", tomcatHomeDir.getPath());
        // portal.properties
        System.out.println("portal.properties");
        props.clear();
        propFile = new File(propertiesDir, "portal.properties");
        backupProperties(propFile);
        setProperty(props, "org.jasig.portal.utils.XSLT.stylesheet_set_caching", p, "PORTAL_SS_CACHE");
        setProperty(props, "org.jasig.portal.utils.XSLT.stylesheet_root_caching", p, "PORTAL_SS_CACHE");
        setProperty(props, "org.jasig.portal.PortalSessionManager.allow_repeated_requests", p, "PORTAL_REPEATED_REQUESTS");
        setProperty(props, "org.jasig.portal.PortalSessionManager.sessionTimeout", p, "PORTAL_SESSION_TIMEOUT");
        replaceProperties(propFile, props);
        // rdbm.properties
        System.out.println("rdbm.properties");
        props.clear();
        propFile = new File(propertiesDir, "rdbm.properties");
        backupProperties(propFile);
        setProperty(props, "jdbcDriver", p, "JDBC_DRIVER");
        setProperty(props, "jdbcUrl", p, "JDBC_URL");
        setProperty(props, "jdbcUser", p, "JDBC_LOGIN");
        setProperty(props, "jdbcPassword", p, "JDBC_PASSWORD");
        setProperty(props, "useUniconPool", p, "USE_UNICON_POOL");
        setProperty(props, "useProtoPool", p, "USE_PROTO_POOL");
        setProperty(props, "initialSize", p, "PROTO_INIT_SIZE");
        setProperty(props, "maxSize", p, "PROTO_MAX_SIZE");
        setProperty(props, "growBlock", p, "PROTO_GROW_BY");
        replaceProperties(propFile, props);
        // system.init
        System.out.println("system.init");
        propFile = new File(propertiesDir, "system.init");
        backupFile(propFile);
        globalReplace(propFile, "^.*webapps", tomcatHomeDir.getPath() +
        System.getProperty("file.separator") + "webapps");
        // PersonDirs.xml
        System.out.println("PersonDirs.xml");
        propFile = new File(propertiesDir, "PersonDirs.xml");
        backupXML(propFile);
        if (p.get("LDAP_SERVER") != null) {
            setupLdapPersonDir(propFile, p);
        } else {
            setupJdbcPersonDir(propFile, p);
        }
        // poolman.xml
        System.out.println("poolman.xml");
        propFile = new File(classesDir, "poolman.xml");
        backupXML(propFile);
        setupPoolman(propFile, p);
    }
    protected void setProperty(Map targetProps, String targetName,
    Properties sourceProps, String sourceName) {
        String sourceProp = sourceProps.getProperty(sourceName);
        if (sourceProp == null || "".equals(sourceProp.trim())) {
            return;
        }
        System.out.println("Setting " + targetName + "=" + sourceProp);
        targetProps.put(targetName, sourceProps.getProperty(sourceName));
    }
    protected void setupPoolman(File file, Properties p)
    throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        Document doc = readXML(file);
        if (doc == null) return;
        XPathAPI xpath = new XPathAPI();
        int i = 0;
        Node node = null;
        NodeList nl = null;
        // set JDBC drivers
        nl = xpath.selectNodeList(doc.getDocumentElement(),
        "/poolman/datasource/driver");
        for (i = 0; i < nl.getLength(); i++) {
            node = nl.item(i).getFirstChild();
            node.setNodeValue((String)p.get("JDBC_DRIVER"));
        }
        // set JDBC urls
        nl = xpath.selectNodeList(doc.getDocumentElement(),
        "/poolman/datasource/url");
        for (i = 0; i < nl.getLength(); i++) {
            node = nl.item(i).getFirstChild();
            node.setNodeValue((String)p.get("JDBC_URL"));
        }
        // set JDBC usernames
        nl = xpath.selectNodeList(doc.getDocumentElement(),
        "/poolman/datasource/username");
        for (i = 0; i < nl.getLength(); i++) {
            node = nl.item(i).getFirstChild();
            node.setNodeValue((String)p.get("JDBC_LOGIN"));
        }
        // set JDBC usernames
        nl = xpath.selectNodeList(doc.getDocumentElement(),
        "/poolman/datasource/password");
        for (i = 0; i < nl.getLength(); i++) {
            node = nl.item(i).getFirstChild();
            node.setNodeValue((String)p.get("JDBC_PASSWORD"));
        }
        storeXML(file, doc);
    }
    protected void setupJdbcPersonDir(File file, Properties p)
    throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        Document doc = readXML(file);
        if (doc == null) return;
        XPathAPI xpath = new XPathAPI();
        // set JDBC driver
        Node node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/driver").getFirstChild();
        node.setNodeValue((String)p.get("JDBC_DRIVER"));
        // set JDBC url
        node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/url").getFirstChild();
        node.setNodeValue((String)p.get("JDBC_URL"));
        // set JDBC logonid
        node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/logonid").getFirstChild();
        node.setNodeValue((String)p.get("JDBC_LOGIN"));
        // set JDBC logonpassword
        node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/logonpassword").getFirstChild();
        node.setNodeValue((String)p.get("JDBC_PASSWORD"));
        storeXML(file, doc);
    }
    protected void setupLdapPersonDir(File file, Properties p)
    throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        Document doc = readXML(file);
        if (doc == null) return;
        XPathAPI xpath = new XPathAPI();
        Node personDirsInfoNode = xpath.selectSingleNode(
        doc.getDocumentElement(), "/PersonDirs/PersonDirInfo");
        // remove JDBC driver node
        Node node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/driver");
        personDirsInfoNode.removeChild(node);
        // remove JDBC url node
        node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/url");
        personDirsInfoNode.removeChild(node);
        // remove JDBC logonid node
        node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/logonid");
        personDirsInfoNode.removeChild(node);
        // remove JDBC logonpassword node
        node = xpath.selectSingleNode(doc.getDocumentElement(),
        "/PersonDirs/PersonDirInfo/logonpassword");
        personDirsInfoNode.removeChild(node);
        // add LDAP url node
        StringBuffer urlSB = new StringBuffer("ldap://");
        urlSB.append((String)p.get("LDAP_SERVER")).append(":");
        urlSB.append((String)p.get("LDAP_PORT")).append("/");
        urlSB.append((String)p.get("LDAP_DN"));
        XmlUtils.addNewNode(doc, personDirsInfoNode, "url", null,
        urlSB.toString());
        // add LDAP logonid  node
        XmlUtils.addNewNode(doc, personDirsInfoNode, "logonid", null, "");
        // add LDAP logonpassword  node
        XmlUtils.addNewNode(doc, personDirsInfoNode, "logonpassword", null, "");
        // add LDAP uidquery  node
        XmlUtils.addNewNode(doc, personDirsInfoNode, "uidquery", null,
        "(uid={0})");
        // add LDAP usercontext  node
        XmlUtils.addNewNode(doc, personDirsInfoNode, "uidquery", null, "");
        storeXML(file, doc);
    }
    protected void globalReplace(File file, String pattern, String replace)
    throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[4096];
        int charsRead = 0;
        FileReader reader = new FileReader(file);
        while ( (charsRead = reader.read(buf, 0, 4096)) >= 0 ) {
            sb.append(buf, 0, charsRead);
        }
        reader.close();
        file.delete();
        String result = sb.toString().replaceAll(pattern, replace);
        FileWriter writer = new FileWriter(file);
        writer.write(result, 0, result.length());
        writer.close();
    }
    protected void storePropertyFile(File file, Properties properties)
    throws Exception {
        OutputStream os = new FileOutputStream(file);
        properties.store(os, null);
        os.close();
    }
    protected void replaceProperties(File file, Map newProps)
    throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(file);
        p.load(is);
        is.close();
        p.putAll(newProps);
        storePropertyFile(file, p);
    }
    protected void replaceWebappsDir(File file, String key, String dir)
    throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(file);
        p.load(is);
        is.close();
        String value = (String)p.get(key);
        if (value == null || "".equals(value)) return;
        int pos = value.indexOf("webapps");
        if (pos < 0) return;
        p.put(key, dir + value.substring(pos - 1));
        storePropertyFile(file, p);
    }
    protected void backupProperties(File file) throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        Properties p = new Properties();
        InputStream is = new FileInputStream(file);
        p.load(is);
        is.close();
        File backup = new File(file.getPath() + ".bak");
        storePropertyFile(backup, p);
    }
    protected void storeXML(File file, Document doc) throws Exception {
        FileWriter writer = new FileWriter(file);
        String xml = XML.serializeNode(doc);
        writer.write(xml, 0, xml.length());
        writer.close();
    }
    protected Document readXML(File file) throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return null;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new InputSource(new FileInputStream(file)));
    }
    protected void backupXML(File file) throws Exception {
        Document doc = readXML(file);
        if (doc == null) return;
        File backup = new File(file.getPath() + ".bak");
        storeXML(backup, doc);
    }
    protected void backupFile(File file) throws Exception {
        if (file == null) {
            System.err.println("file is null!");
            return;
        }
        if (!file.exists()) {
            System.err.println("File " + file.getPath() + " does not exist!");
            return;
        }
        char[] buf = new char[4096];
        int charsRead = 0;
        File backup = new File(file.getPath() + ".bak");
        FileReader reader = new FileReader(file);
        FileWriter writer = new FileWriter(backup);
        while ( (charsRead = reader.read(buf, 0, 4096)) >= 0) {
            writer.write(buf, 0, charsRead);
        }
        reader.close();
        writer.close();
    }
    public static void main(String[] args) throws Exception {
        String baseDir = null;
        String target = null;
        if (args.length < 2) {
            System.out.println("Usage SetupParameters <base directory> <target>");
            return;
        }
        if (args.length >= 2) {
            baseDir = args[0];
            target = args[1];
        }
        File baseDirFile = new File(baseDir);
        if (!baseDirFile.exists()) {
            System.err.println("Directory " + baseDir + " does not exist!");
            return;
        }
        new SetupParameters(baseDirFile, target);
    }
}
