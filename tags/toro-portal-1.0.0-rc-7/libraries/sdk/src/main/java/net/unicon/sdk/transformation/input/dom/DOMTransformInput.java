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
package net.unicon.sdk.transformation.input.dom;

import java.io.BufferedWriter;
import java.io.StringWriter;

import net.unicon.sdk.properties.CommonPropertiesType;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.transformation.DocumentDeclarationException;
import net.unicon.sdk.transformation.ITransformInput;
import net.unicon.sdk.transformation.TransformException;

import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/** 
 * DOMTransformInput contains a DOM to be transformed into 
 * target document type. 
 */
public class DOMTransformInput implements ITransformInput {
    protected Document xmlDOM  = null;
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
    public DOMTransformInput(
                            String inputType, 
                            String outputType, 
                            Document xmlDOM) 
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
        // check xmlDOM 
        if (xmlDOM == null) {
            throw new IllegalArgumentException (
                "Transaction Document must be provided");
        }

        this.inputType = inputType;
        this.outputType = outputType;
        this.xmlDOM = xmlDOM;

    } // end constructor

    /** constructor */
    public DOMTransformInput(Document xmlDOM)
    throws DocumentDeclarationException { 

        /* ASSERTIONS */
        // check xmlDOM 
        if (xmlDOM == null) {
            throw new IllegalArgumentException (
                "Transaction Document must be provided");
        }

        String inputDecl = null;
        try {
            // extract the doctype declaration
            inputDecl = extractInputType(xmlDOM); 
            if (inputDecl == null) {
                inputDecl = defaultInputType; 
            }
        } catch (Throwable t) {
            throw new DocumentDeclarationException(t.getMessage(), t);
        }

        this.inputType = inputDecl;
        this.outputType = defaultOutputType;
        this.xmlDOM = xmlDOM;

    } // end constructor

    /* retrieves the doctype declaration from the document */
    private String extractInputType(Document xmlDOM) throws Exception {
        DocumentType docType = xmlDOM.getDoctype();
        
        if (docType != null) {
            String systemId = docType.getSystemId();
            String publicId = docType.getPublicId();

            if (systemId != null && systemId.length() > 0) {
                return systemId;
            } else if (publicId != null && publicId.length() > 0) {
                return publicId;
            }
        }

        return null;
    } // end extractInputType

    /** 
     * Provides access to the content to be transformed 
     * 
     * @return Content to be transformed as a string. 
     * @throws <code>net.unicon.sdk.transformation.TransformException</code>
     */
    public String getContent() throws TransformException {
        StringWriter sout         = null;
        BufferedWriter buffWriter = null;
        XMLWriter xmlWriter       = null;
        SAXWriter saxWriter       = null;
        DOMReader domReader       = null; 

        try {
            // convert xmlDOM to a string
            sout = new StringWriter();
            buffWriter = new BufferedWriter(sout);

            xmlWriter = new XMLWriter(buffWriter); 
            saxWriter = new SAXWriter(xmlWriter);

            domReader = new DOMReader();
            saxWriter.write(domReader.read(xmlDOM));

            xmlWriter.flush();

            return sout.toString(); 
        } catch (Throwable t) {
            throw new TransformException(t.getMessage(), t);
        } finally {

            if (buffWriter != null) {
                try {
                    buffWriter.close();
                } catch (Exception e) {}
            }

        } // end try block

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

} // end DOMTransformInput class
