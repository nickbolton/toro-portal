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
package net.unicon.sdk.transformation.input;

import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.transformation.DocumentDeclarationException;
import net.unicon.sdk.transformation.ITransformInput;
import net.unicon.sdk.transformation.OutputDeclHandler;
import net.unicon.sdk.transformation.TransformException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/** 
 * TextTransformInput contains a string of XML to be transformed into 
 * target document type. 
 */
public class TextTransformInput implements ITransformInput {
    protected String xmlContent = null;
    protected String inputType = null;
    protected String outputType = null;
    private static String defaultInputType = null; 
    private static String defaultOutputType = null; 

    static {
        try {
            defaultOutputType =  
                UniconPropertiesFactory.getManager(
                    CommonPropertiesType.SERVICE).getProperty(
                        "net.unicon.sdk.transformation.ITransformInput.defaultOutputType");
            defaultInputType =  
                UniconPropertiesFactory.getManager(
                    CommonPropertiesType.SERVICE).getProperty(
                        "net.unicon.sdk.transformation.ITransformInput.defaultInputType");
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** constructor */
    public TextTransformInput(
                            String inputType,
                            String outputType, 
                            String xmlContent)
    { 

        /* ASSERTIONS */
        // check inputType 
        if (!(inputType != null && inputType.length() > 0)) {
            throw new IllegalArgumentException (
                "Transform input type must be provided");
        }
        // check outputType 
        if (!(outputType != null && outputType.length() > 0)) {
            throw new IllegalArgumentException (
                "Transform ouput type must be provided");
        }
        // check xmlContent 
        if (!(xmlContent != null && xmlContent.length() > 0)) {
            throw new IllegalArgumentException (
                "Transaction XML content must be provided");
        }

        this.inputType = inputType;
        this.outputType = outputType;
        this.xmlContent = xmlContent;

    } // end constructor

    /** constructor */
    public TextTransformInput(String xmlContent, EntityResolver entityResolver)
    throws DocumentDeclarationException { 

        /* ASSERTIONS */
        // check xmlContent 
        if (!(xmlContent != null && xmlContent.length() > 0)) {
            throw new IllegalArgumentException (
                "Transaction XML content must be provided");
        }

        String inputDecl = null;
        try {
            // extract the doctype declaration
            inputDecl = extractInputType(xmlContent, entityResolver); 
            if (inputDecl == null) {
                inputDecl = defaultInputType; 
            }
        } catch (Throwable t) {
            throw new DocumentDeclarationException(t.getMessage(), t);
        }

        this.inputType = inputDecl;
        this.outputType = defaultOutputType;
        this.xmlContent = xmlContent;

    } // end constructor
    
    public TextTransformInput(String xmlContent)
    throws DocumentDeclarationException { 
        this(xmlContent, null);
    }

    /* retrieves the doctype declaration from the document */
    private String extractInputType(String xmlContent, EntityResolver entityResolver) throws Exception {
        XMLReader xmlReader =
            SAXParserFactory.newInstance().
                newSAXParser().getXMLReader();

        OutputDeclHandler handler = new OutputDeclHandler();
        xmlReader.setEntityResolver(handler);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        if (entityResolver != null) {
            xmlReader.setEntityResolver(entityResolver);
        }

        StringReader strReader = new StringReader(xmlContent);
        InputSource is = new InputSource(strReader);

        xmlReader.parse(is);

        return handler.getAvailableId();
    } // end extractInputType

    /** 
     * Provides access to the content to be transformed 
     * 
     * @return Content to be transformed as a string. 
     * @throws <code>net.unicon.sdk.transformation.TransformException</code>
     */
    public String getContent() throws TransformException {
        return xmlContent;
    } // end getContent 

    /** 
     * Provides access to the target document declaration type uri
     * 
     * @return Target output (target) document declaration type uri
     */
    public String getOutputType() {
        return outputType;
    }

    /** 
     * Provides access to the input document declaration type uri
     * 
     * @return Input document declaration type uri
     */
    public String getInputType() {
        return inputType;
    }

} // end TextTransformInput class
