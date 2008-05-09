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
package net.unicon.sdk.transformation.trax;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.unicon.sdk.transformation.ITransformInput;
import net.unicon.sdk.transformation.ITransformationService;
import net.unicon.sdk.transformation.TransformException;
import net.unicon.sdk.transformation.TransformMappingHandler;
import net.unicon.sdk.transformation.TransformResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/** 
 * TraxTransformationService class is used to transform one type of XML 
 * to another type of format.
 */
public class TraxTransformationService implements ITransformationService {
    //private static Map transformMappings = null;
    
    /* Instance Members */
    private Map transformMappings = null;

    /*
    static {
        try {
            transformMappings = initTransformMappings();
        } catch (Exception e) { e.printStackTrace(); }
    }
    /*

    /** 
     * Constructs an instance of this object for a specific 
     * transformation mapping.
     * 
     * @param transformFilePath is a path to a valid TransformMapping.xml file.
     * @throws <code>net.unicon.sdk.transformation.TransformException</code>
     */
    public TraxTransformationService(String transformFilePath) 
    throws TransformException {

        /* ASSERTIONS */
        // check transformFilePath 
        if (!(transformFilePath != null 
                && transformFilePath.trim().length() > 0)) {
            throw new IllegalArgumentException(
                "Configuration file path must be provided");
        }

        try {
            this.transformMappings = initTransformMappings(transformFilePath);
        } catch (Throwable t) { 
            throw new TransformException(t.getMessage(), t);
        }

    } // end constructor(transformFilePath)

    /**
     * Transforms XML into a target format
     *
     * @param transformInput object that represents the XML to be transformed.
     * @return TransformResult object that represents the transformed format.
     * @throws <code>net.unicon.sdk.transformation.TransformException</code>
     * @see <{ITransformInput}>
     * @see <{TransformResult}>
     */
    public TransformResult transform(ITransformInput transformInput)
    throws TransformException {

        /* ASSERTIONS */
        // check transformInput 
        if (transformInput == null) {
            throw new IllegalArgumentException(
                "TransformInput must be provided");
        }

        try {
            // check inputType
            String inputType = transformInput.getInputType();
            if (!(inputType != null && inputType.trim().length() > 0)) {
                throw new Exception("input type must be provided");
            }

            // check outputType
            String outputType = transformInput.getOutputType();
            if (!(outputType != null && outputType.trim().length() > 0)) {
                throw new Exception("output type must be provided");
            }

            // check inputContent 
            String inputContent = transformInput.getContent();
            if (!(inputContent != null && inputContent.trim().length() > 0)) {
                throw new Exception("input content must be provided");
            }

            // get target XSL file using a unique key of inputType+outputType 
            String key     = inputType+outputType;
            String xslPath = null;

            if (transformMappings.containsKey(key)) {
                xslPath = (String)transformMappings.get(key);
            }

            if (xslPath == null) {
                StringBuffer errorMsg = new StringBuffer("");
                errorMsg.append("transformation does not exist for ");
                errorMsg.append("input type of \"").append(inputType);
                errorMsg.append("\" ");
                errorMsg.append("and output type of \"").append(outputType);
                errorMsg.append("\".");
                throw new Exception(errorMsg.toString());
            }

            //File xslFile  = ResourceLoader.getResourceAsFile(
                //TraxTransformationService.class, xslPath); 
            File xslFile = new File(xslPath); 
            String output = renderInput(inputContent, xslFile);

            return new TransformResult(output);
        } catch (Throwable t) {
            throw new TransformException(t.getMessage(), t);
        }
        
    } // end transform

    /* renderInput method applies the xsl file to the input XML */ 
    private String renderInput(String input, File xslFile)
    throws 
        IOException, 
        TransformerException, 
        TransformerConfigurationException, 
        TransformerFactoryConfigurationError
    {

        BufferedWriter outputWriter = null;
        try {
            // Create the Transformer
            TransformerFactory  factory = TransformerFactory.newInstance();
            Source xslSource = new StreamSource(xslFile);
            Transformer transformer = factory.newTransformer(xslSource);

            // Create Source for XML to be transformed
            BufferedReader inputReader = new BufferedReader(
                new StringReader(input));
            Source source = new StreamSource(inputReader);

            // Create Result for transformed format to be written to
            StringWriter sout = new StringWriter();
            outputWriter = new BufferedWriter(sout);
            Result result = new StreamResult(outputWriter);

            // Now do the work
            transformer.transform(source, result);
            String output = sout.toString();

            return output;
        } finally {
            if (outputWriter != null) {
                try {
                    outputWriter.close();
                } catch (Exception e) {}
            }
        } // end try block

    } // end renderInput 

    /* get mapping info from TransformMappings.xml */
    //private static Map initTransformMappings(String path)
    private Map initTransformMappings(String path)
    throws SAXException, IOException, ParserConfigurationException {

        InputSource is = new InputSource(
            TraxTransformationService.class.getResourceAsStream(path));

        XMLReader xmlReader =
        SAXParserFactory.newInstance().
            newSAXParser().getXMLReader();

        TransformMappingHandler handler = new TransformMappingHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);

        xmlReader.parse(is);

        return handler.getTransformMappings();
    } // end initTransformMappings

} // end TraxTransformationService
