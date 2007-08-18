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
package net.unicon.alchemist.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.transform.sax.SAXSource;

import org.apache.jetspeed.util.OverwriteProperties;
import org.dom4j.Branch;
import org.dom4j.CDATA;
import org.dom4j.CharacterData;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.NamespaceStack;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class MergeConfiguration {
    
    private static final String CHANGE_START_COMMENT = "DO NOT REMOVE - MERGECONFIGURATION BEGIN";
    private static final String CHANGE_END_COMMENT = "DO NOT REMOVE - MERGECONFIGURATION END";
    
    private File file;
    private File target;
    
    public void setFile(String file) {
        this.file = new File(file);
    }

    public void setTarget(String target) {
        this.target = new File(target);
    }

    public void execute() throws Exception {
        if (!file.exists()) {
            System.out.println("Configuration file does not exist: " + file.getAbsolutePath());
            System.exit(-1);
        }
        if (!target.exists()) {
            System.out.println("Target directory does not exist: " + target.getAbsolutePath());
            System.exit(-1);
        }
        Document doc = getChanges();
        for ( Iterator i = doc.getRootElement().elementIterator( "file" ); i.hasNext(); ) {
            Element targetFileElement = (Element) i.next();
            mergeChanges(targetFileElement);
        }
    }
    
    private void mergeChanges(Element targetFileElement) throws Exception {
        File path = new File(target, targetFileElement.attribute("path").getText());
        String absolutePath = path.getAbsolutePath();
        
        if (absolutePath.endsWith("WEB-INF/web.xml")) {
            mergeWebChanges(targetFileElement, path);
        } else if (absolutePath.endsWith(".xml")) {
            mergeXmlChanges(targetFileElement, path);
        } else if (absolutePath.endsWith(".properties")) {
            mergePropertiesChanges(targetFileElement, path);
        } else {
            throw new RuntimeException("Unsupported configuration file type: " + path + ". Only '.properties' and '.xml' files are supported.");
        }
    }
    
    private void mergePropertiesChanges(Element el, File path) throws Exception {
        System.out.println("Merging properties changes to " + path.getAbsolutePath());
        backupOriginalFile(path);
        
        // create temp properties file to merge from
        Properties props = new Properties();
        for ( Iterator i = el.elementIterator( "add-or-replace" ); i.hasNext(); ) {
            Element replace = (Element) i.next();
            String name = replace.elementText("name");
            String value = replace.elementText("value");
            props.setProperty(name, value);
        }
        
        File mergeFrom = new File(path.getParentFile(), path.getName()+"_mergeFrom.properties");
        props.store(new FileOutputStream(mergeFrom), "");
        
        try {
	        OverwriteProperties overwriter = new OverwriteProperties();
	        overwriter.setBaseProperties(path);
	        overwriter.setProperties(mergeFrom);
	        overwriter.setIncludeRoot(new File("."));
	        overwriter.setVerbose(false);
	        overwriter.execute();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            throw new RuntimeException(sw.toString());
        } finally {
            mergeFrom.delete();
        }
    }
    
    private void mergeXmlChanges(Element el, File path) throws Exception {
        System.out.println("Merging xml changes to " + path.getAbsolutePath());
        backupOriginalFile(path);
        
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new DTDResolver(target));
        Document doc = reader.read(new URL("file:"+path.getAbsolutePath()));
        Element source = doc.getRootElement();
        
        processElementValueReplacements(el, source);
        processNodeReplacements(el, source);
        //processAddToNodes(el, source);
        processNodeAddOrReplace(el, source);
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(new FileWriter(path), format);
        writer.write(doc);
        writer.close();
    }
    
    private void processNodeAddOrReplace(Element el, Element source) {
        List list = el.selectNodes("add-or-replace[@where='end']");
        if (list == null) return;

        removeOldNodes(source);
        
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Element replace = (Element)itr.next();
            String xpath = replace.elementText("xpath");
            
            addNode(source, replace, xpath);
        }
    }

    private void removeOldNodes(Element source) {
        // remove any previous nodes added by academus
        ListIterator contentItr = source.content().listIterator();
        boolean sawBegin = false;
        boolean sawEnd = false;
        while (!sawEnd && contentItr.hasNext()) {
            Node node = (Node)contentItr.next();
            
            if (!sawBegin) {
                sawBegin = node instanceof Comment && ((Comment)node).getText().equals(CHANGE_START_COMMENT);
                if (sawBegin) {
	                contentItr.remove();
                }
            } else {
                contentItr.remove();
                sawEnd = node instanceof Comment && ((Comment)node).getText().equals(CHANGE_END_COMMENT);
            }
        }
    }
        
    private void addNode(Element source, Element replace, String xpath) {
        Element newContent = DocumentHelper.createElement("newContent");
        Comment prefixComment = DocumentHelper.createComment(CHANGE_START_COMMENT);
        Comment suffixComment = DocumentHelper.createComment(CHANGE_END_COMMENT);
        newContent.add(prefixComment);
        newContent.appendContent((Branch)replace.element("value"));
        newContent.add(suffixComment);
        
        Element sourceEl = (Element)source.selectSingleNode(xpath);
        if (sourceEl == null) {
            throw new RuntimeException("xpath expression doesn't resolve to a node: " + xpath);
        }
        
        sourceEl.appendContent(newContent);
    }

    private void processNodeAddOrReplace2(Element el, Element source) {
        List list = el.selectNodes("add-or-replace-node[@type='node']");
        if (list == null) return;
        
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Element replace = (Element)itr.next();
            String xpath = replace.elementText("xpath");
            Element replacementElement = (Element)replace.element("value").elements().get(0);
            
            Element sourceEl = (Element)source.selectSingleNode(xpath);
            if (sourceEl != null) {
                System.out.println("Replacing node at path: " + xpath);
                replaceElement(source, replacementElement);
            } else {
                addNode(source, replace, xpath);
            }
        }
    }
    
    private void processNodeReplacements(Element el, Element source) {
        List list = el.selectNodes("replace[@type='node']");
        if (list == null) return;
        
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Element replace = (Element)itr.next();
            String xpath = replace.elementText("xpath");
            Element replacementElement = (Element)replace.element("value").elements().get(0);
            
            Element sourceEl = (Element)source.selectSingleNode(xpath);
            if (sourceEl == null) {
                throw new RuntimeException("xpath expression doesn't resolve to a node: " + xpath);
            }
            System.out.println("Replacing node at path: " + xpath);
            replaceElement(sourceEl, replacementElement);
        }
    }

    private void replaceElement(Element sourceEl, Element replacementElement) {
        if (!sourceEl.getName().equals(replacementElement.getName())) {
            throw new RuntimeException("replacement element must have the same name: " + sourceEl.getName() + " and " + replacementElement.getName() + " differ.");
        }
        
        // remove all the existing
        sourceEl.attributes().clear();
        
        // remove all child elements
        sourceEl.elements().clear();
        
        // add all the new attributes
        sourceEl.appendAttributes(replacementElement);
        
        // add all the new child elements
        Iterator eItr = new ArrayList(replacementElement.elements()).iterator();
        while (eItr.hasNext()) {
            Element newEl = (Element)eItr.next();
            replacementElement.remove(newEl);
            sourceEl.add(newEl);
        }
    }
    
    private void processElementValueReplacements(Element el, Element source) {
        List list = el.selectNodes("replace[@type='element_value']");
        if (list == null) return;
        
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Element replace = (Element)itr.next();
            String xpath = replace.elementText("xpath");
            String value = replace.elementText("value");
            
            Element sourceEl = (Element)source.selectSingleNode(xpath);
            if (sourceEl == null) {
                throw new RuntimeException("xpath expression doesn't resolve to a node: " + xpath);
            }
            
            System.out.println("Replacing text at path: " + xpath + " with: " + value);
            
            sourceEl.setText(value);
        }
    }
    
    private void mergeWebChanges(Element el, File path) throws Exception {
        System.out.println("Merging web.xml changes to " + path.getAbsolutePath());
        backupOriginalFile(path);
        
        
        OutputFormat format = OutputFormat.createPrettyPrint();
        
        SAXReader reader = new SAXReader();
        reader.setIncludeInternalDTDDeclarations(true);
        Document document = reader.read(new URL("file:"+path.getAbsolutePath()));
        SAXSource source = new DocumentSource(document);
        
        List<Element> filters = (List<Element>)el.selectNodes("add-filter/filter");
        List<Element> filterMappings = (List<Element>)el.selectNodes("add-filter-mapping/filter-mapping");
        List<Element> servlets = (List<Element>)el.selectNodes("add-servlet/servlet");
        List<Element> servletMappings = (List<Element>)el.selectNodes("add-servlet-mapping/servlet-mapping");
        List<Element> errorPages = (List<Element>)el.selectNodes("add-error-page/error-page");
        
        
        // remove previous changes
        StringWriter cleansedDocumentWriter = new StringWriter();
        WebXmlCleanserFilter cleaner = new WebXmlCleanserFilter(new XMLWriter(cleansedDocumentWriter));
        cleaner.parse(source.getInputSource());
        
        // add new changes
        try {
        Document cleansedDocument = reader.read(new StringReader(cleansedDocumentWriter.toString()));
        source = new DocumentSource(cleansedDocument);
        SAXWriter saxWriter = new WebXmlAdditionsWriter(new XMLWriter(new FileOutputStream(path), format), filters, filterMappings, servlets, servletMappings, errorPages);
        saxWriter.parse(source.getInputSource());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    private Document getChanges() throws MalformedURLException, DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new URL("file:"+file.getAbsolutePath()));
        return document;
    }
    
    
    private void backupOriginalFile(File path) throws IOException {
        File backupDir = new File(path.getParentFile(), "merge_backups");
        backupDir.mkdirs();
        File originalBackup = new File(backupDir, path.getName());
        if (!originalBackup.exists()) {
			// Create channel on the source
            FileChannel srcChannel = new FileInputStream(path).getChannel();
        
            // Create channel on the destination
            FileChannel dstChannel = new FileOutputStream(originalBackup).getChannel();
        
            // Copy file contents from source to destination
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        
            // Close the channels
            srcChannel.close();
            dstChannel.close();
        }
    }
    
    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Usage: MergeConfiguration <merge file> <target relative path>");
            System.out.println("e.g.  MergeConfiguration $TOMCAT_HOME/webapps/portal/WEB-INF/classes/properties/configurationChanges.xml $TOMCAT_HOME/webapps/portal");
            System.exit(-1);
        }
        String mergeFile = args[0];
        String targetPath = args[1];
        
        try {
	        MergeConfiguration mupc = new MergeConfiguration();
	        mupc.setFile(mergeFile);
	        mupc.setTarget(targetPath);
	        mupc.execute();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static class DTDResolver implements EntityResolver {
        private File target;
        
        public DTDResolver(File target) {
            this.target = target;
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            URL systemIdUrl = new URL(systemId);
            File dtdPath = new File(new File(target, "dtd"), new File(systemIdUrl.getPath()).getName());
            if (dtdPath.exists()) {
                return new InputSource(new FileReader(dtdPath));
            }
            return null;
        }
        
    }
    
    private static class WebXmlAdditionsWriter extends DtdAwareSAXWriter {
        
        private List<Element> filters;
        private List<Element> filterMappings;
        private List<Element> servlets;
        private List<Element> servletMappings;
        private List<Element> errorPages;
        private Map<String, List<List<Element>>> mappings;
        private Set<List<Element>> processed = new HashSet<List<Element>>();
        private Comment beginComment;
        private Comment endComment;
        
        
        public WebXmlAdditionsWriter(XMLWriter xmlWriter, List<Element> filters, List<Element> filterMappings, List<Element> servlets, List<Element> servletMappings, List<Element> errorPages) {
            super(xmlWriter);
            
            this.filters = filters;
            this.filterMappings = filterMappings;
            this.servlets = servlets;
            this.servletMappings = servletMappings;
            this.errorPages = errorPages;
            
            this.beginComment = DocumentHelper.createComment(CHANGE_START_COMMENT);
            this.endComment = DocumentHelper.createComment(CHANGE_END_COMMENT);
        }
        
        private Map<String, List<List<Element>>> getMappings() {
            
            if (mappings == null) {
                mappings = new HashMap<String, List<List<Element>>>();
            
	            addMapping("filter-mapping", filters);
	            addMapping("listener", filters);
	            addMapping("servlet", filters);
	            
	            addMapping("listener", filterMappings);
	            addMapping("servlet", filterMappings);
	            
	            addMapping("servlet-mapping", servlets);
	            
	            addMapping("session-config", servletMappings);
	            addMapping("mime-mapping", servletMappings);
	            addMapping("welcome-file-list", servletMappings);
	            addMapping("error-page", servletMappings);
	            addMapping("taglib", servletMappings);
	            addMapping("resource-env-ref", servletMappings);
	            addMapping("resource-ref", servletMappings);
	            addMapping("security-constraint", servletMappings);
	            addMapping("login-config", servletMappings);
	            addMapping("security-role", servletMappings);
	            addMapping("env-entry", servletMappings);
	            addMapping("ejb-ref", servletMappings);
	            addMapping("ejb-local-ref", servletMappings);
	            
	            addMapping("taglib", errorPages);
	            addMapping("resource-env-ref", errorPages);
	            addMapping("resource-ref", errorPages);
	            addMapping("security-constraint", errorPages);
	            addMapping("login-config", errorPages);
	            addMapping("security-role", errorPages);
	            addMapping("env-entry", errorPages);
	            addMapping("ejb-ref", errorPages);
	            addMapping("ejb-local-ref", errorPages);
            }
            
            return mappings;
        }
        
        private void addMapping(String tagname, List<Element> elements) {
            List<List<Element>> mapping = mappings.get(tagname);
            if (mapping == null) {
                mapping = new LinkedList<List<Element>>();
                mappings.put(tagname, mapping);
            }
            mapping.add(elements);
        }

        @Override
        protected void startElement(Element element, AttributesImpl namespaceAttributes) throws SAXException {
            List<List<Element>> mapping = getMappings().get(element.getName());
            if (mapping != null) {
                boolean wroteComment = false;
                for (List<Element> elements : mapping) {
                    if (!processed.contains(elements) && elements.size() > 0) {
                        processed.add(elements);
                        for (Element addedElement : elements) {
                            if (!wroteComment) {
                                super.write(beginComment);
                                wroteComment = true;
                            }
                            super.write(addedElement);
                        }
                    }
                }
                if (wroteComment) {
	                super.write(endComment);
                }
            }
            super.startElement(element, namespaceAttributes);
        }

    }
    
    private static class WebXmlCleanserFilter extends DtdAwareSAXWriter {
        
        private DocumentType documentType;
        private boolean inComment = false;
        
        
        public WebXmlCleanserFilter(XMLWriter xmlWriter) {
            super(xmlWriter);
        }
        
        public DocumentType getDocType() {
            return documentType;
        }

        @Override
        protected void startElement(Element element, AttributesImpl attributes)
        throws SAXException {
            if (inComment) return;
            super.startElement(element, attributes);
        }

        @Override
        public void write(CDATA cdata) throws SAXException {
            if (inComment) return;
            super.write(cdata);
        }

        @Override
        public void write(Comment comment) throws SAXException {
            if (CHANGE_START_COMMENT.equals(comment.getText())) {
                inComment = true;
                return;
            } else if (CHANGE_END_COMMENT.equals(comment.getText())) {
                inComment = false;
                return;
            }
            if (inComment) return;
            super.write(comment);
        }

        @Override
        protected void write(Element element, NamespaceStack stack)
        throws SAXException {
            if (inComment) return;
            super.write(element, stack);
        }

        @Override
        public void write(Element element) throws SAXException {
            if (inComment) return;
            super.write(element);
        }

        @Override
        public void write(Entity entity) throws SAXException {
            if (inComment) return;
            super.write(entity);
        }

        @Override
        public void write(Node node) throws SAXException {
            if (inComment) return;
            super.write(node);
        }

        @Override
        public void write(ProcessingInstruction processingInstruction)
        throws SAXException {
            if (inComment) return;
            super.write(processingInstruction);
        }

        @Override
        public void write(String s) throws SAXException {
            if (inComment) return;
            super.write(s);
        }

        @Override
        public void writeClose(Element element) throws SAXException {
            if (inComment) return;
            super.writeClose(element);
        }

        @Override
        public void writeOpen(Element element) throws SAXException {
            if (inComment) return;
            super.writeOpen(element);
        }
        
    }
    
    private static class DtdAwareSAXWriter extends SAXWriter {
        
        private String dtdName;
        private String publicId;
        private String systemId;
        
        public DtdAwareSAXWriter(XMLWriter xmlWriter) {
            super(xmlWriter, xmlWriter);
        }
        
        @Override
        protected void startDocument() throws SAXException {
            super.startDocument();
            LexicalHandler lexicalHandler = getLexicalHandler();
            if (lexicalHandler != null) {
                lexicalHandler.startDTD(dtdName, publicId, systemId);
                lexicalHandler.endDTD();
            }
        }

        @Override
        public void write(Document document) throws SAXException {
            int startingPos = document.asXML().indexOf("<!DOCTYPE");
            if (startingPos >= 0) {
                int endingPos = document.asXML().substring(startingPos).indexOf(">");
                String docTypeDeclaration = document.asXML().substring(startingPos, startingPos+endingPos+1);
                StringTokenizer tok = new StringTokenizer(docTypeDeclaration, " ");
                tok.nextToken();
                dtdName = tok.nextToken();
                
                startingPos = docTypeDeclaration.indexOf('"');
                endingPos = docTypeDeclaration.substring(startingPos+1).indexOf('"');
                publicId = docTypeDeclaration.substring(startingPos+1, startingPos+endingPos+1);
                
                endingPos = docTypeDeclaration.lastIndexOf('"');
                startingPos = docTypeDeclaration.substring(0, endingPos-1).lastIndexOf('"');
                systemId = docTypeDeclaration.substring(startingPos+1, endingPos);
                
            }
            super.write(document);
        }
        
    }
}
