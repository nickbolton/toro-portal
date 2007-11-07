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
package net.unicon.sdk.transformation;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.awt.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.transformation.input.dom.DOMTransformInput;
import net.unicon.sdk.transformation.input.TextTransformInput;
import net.unicon.sdk.properties.*;

public class TransformationServiceDriver extends JFrame implements ActionListener {
    public JButton fileButton;
    public JButton testButton;
    public JButton clearButton;

    public JComboBox inputCombo;
    public JComboBox outputCombo;
    public JTextField fileText;

    public JLabel inputLabel;
    public JLabel outputLabel;
    public JLabel fileLabel;

    public JTextArea textArea;
    public JScrollPane scrollPane;

    public JPanel topPanel;
    public JPanel middlePanel;
    public JPanel bottomPanel;

    private int windowWidth  = 850;
    private int windowHeight = 550;
    private int columnWidth  = 40;

    private ITransformationService transformService;
    private NumberFormat nf = null;

    public TransformationServiceDriver() throws Exception {
        this.setTitle("Transformation Service Driver");
        this.setSize(windowWidth, windowHeight);
        this.setResizable(false);

        // Panels
        topPanel    = new JPanel();
        middlePanel = new JPanel();
        bottomPanel = new JPanel();

        // Labels
        inputLabel = new JLabel("   Input Type:");
        outputLabel = new JLabel("   Output Type:");
        fileLabel = new JLabel("   XML File:");

        // Text Fields
        //inputText = new JTextField(15);
        //outputText = new JTextField(15);
        fileText = new JTextField(15);

        // Combo Boxes
        inputCombo = new JComboBox();
        inputCombo.setEditable(true);
        inputCombo.addItem(" ");
        inputCombo.addItem("unicon");
        inputCombo.addItem("ims");
        inputCombo.addActionListener(this);

        outputCombo = new JComboBox();
        outputCombo.setEditable(true);
        outputCombo.addItem(" ");
        outputCombo.addItem("ims");
        outputCombo.addItem("unicon");
        outputCombo.addActionListener(this);

        // Text Area
        textArea = new JTextArea(28, 75);
        textArea.setLineWrap(true);
        scrollPane = new JScrollPane(textArea);
        middlePanel.add(scrollPane, "South");

        // Layout Code
        topPanel.add(inputLabel);
        topPanel.add(inputCombo);
        topPanel.add(outputLabel);
        topPanel.add(outputCombo);
        topPanel.add(fileLabel);
        topPanel.add(fileText);

        fileButton = new JButton("Browse");
        fileButton.addActionListener(this);
        topPanel.add(fileButton);

        testButton = new JButton("Test");
        testButton.addActionListener(this);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);

        bottomPanel.add(testButton, "Center");
        bottomPanel.add(clearButton, "Center");

        getContentPane().add(topPanel,    "North");
        getContentPane().add(middlePanel, "Center");
        getContentPane().add(bottomPanel, "South");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(Window e) {
                System.exit(0);
            }
        });

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dem = tk.getScreenSize();
        int screenHeight = dem.height;
        int screenWidth  = dem.width;
        setLocation((screenWidth - windowWidth )/2,
                    (screenHeight - windowHeight)/2);

        // Get reference to ITransformationService
        transformService = TransformationServiceFactory.getService(
            "/home/unicon/mfreestone/src/sdk/build/properties/TransformMappings.xml");

        // Create NumberFormat object for displaying process time
        nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        int numDigits = Long.toString(Long.MAX_VALUE).length();
        nf.setMinimumIntegerDigits(numDigits);
        nf.setMaximumIntegerDigits(numDigits);

    } // default constructor

    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        // Perform the test
        if(source == testButton) {
            StringBuffer resultBuffer = new StringBuffer();
            String inputString   = (String)inputCombo.getSelectedItem();
            String outputString  = (String)outputCombo.getSelectedItem();
            String fileString    = fileText.getText();
            String processTime   = null;

            //if(inputString.trim().length() > 0 
                //&& outputString.trim().length() > 0
                //&& fileString.trim().length() > 0) {

                try {
                    /* 
                        Perform the call to ITransformationService 
                        and get results.
                    */
                    resultBuffer.append("\n\nINPUT XML:\n");
                    String xmlString = this.getXmlContent(fileString);
                    resultBuffer.append(xmlString);

                    /*
                    Document xmlDOM = this.getXmlDOM(xmlString);

                    ITransformInput transformInput = 
                        new DOMTransformInput(xmlDOM); 
                    */
                    ITransformInput transformInput = 
                        new TextTransformInput(xmlString); 

                    // do the transformation
                    long startTime = System.currentTimeMillis();

                    TransformResult transformResult = 
                        transformService.transform(transformInput);

                    long endTime = System.currentTimeMillis();
                    //processTime = nf.format((endTime-startTime));

                    // get the results 
                    String resultString = transformResult.getContent();
                    resultBuffer.append("\n\nOUTPUT XML:\n").
                        append(resultString).append("\n");

                    // get process time in millis
                    resultBuffer.append("\nProcess Time in Milliseconds: ");
                    //resultBuffer.append(processTime).append("\n");
                    resultBuffer.append((endTime-startTime)).append("\n");

                } catch (Exception e) { 
                    e.printStackTrace(); 
                    resultBuffer.append("\n\nError: " + e.getMessage() + "\n");
                }

            //} else {
                //resultBuffer.append("\nPlease complete all fields!");
            //}

            // add results to textarea.
            textArea.append(resultBuffer.toString());
            return;
        }
        // Clear the textarea of the test results
        if(source == clearButton) {
            textArea.setText("");
            return;
        }
        // Get the XML file to perform test against 
        if(source == fileButton) {
            JFileChooser chooser = new JFileChooser();

            XmlFileFilter filter = new XmlFileFilter();
            chooser.setFileFilter(filter);

            int selected = chooser.showOpenDialog(getContentPane());

            if (selected == JFileChooser.APPROVE_OPTION) {
                File xmlFile = chooser.getSelectedFile();
                String xmlPath = xmlFile.getAbsolutePath();
                fileText.setText(xmlPath);
                return;
            } else if (selected == JFileChooser.CANCEL_OPTION) {
                return;
            }

        } // end if

    } // end actionPerformed

    private String getXmlContent(String xmlPath) throws Exception {
        // read in the XML file contents
        BufferedReader reader = null;
        try {
            StringBuffer buff = new StringBuffer();
            reader = new BufferedReader(
                new FileReader(new File(xmlPath)) );
            int numRead;
            char[] b = new char[4096];

            while ((numRead = reader.read(b, 0, 4096)) != -1) {
                buff.append(b, 0, numRead);
            }

            return buff.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {}
            }
        }
    } // end getXmlContent

    private Document getXmlDOM(String xmlString) throws Exception {
        DOMParser parser = new DOMParser();
        StringReader reader = new StringReader(xmlString);
        InputSource is = new InputSource(reader);
        parser.parse(is);
        Document doc = parser.getDocument();

        return doc;
    } // end getXmlDOM

    /**
     * A convenience implementation of FileFilter that filters out
     * all files except for those type extensions that it knows about.
     */
    class XmlFileFilter extends javax.swing.filechooser.FileFilter {

        /** default constructor */
        public XmlFileFilter() {}
        /**
         * Return true if this file should be shown in the directory pane,
         * false if it shouldn't.
         */
        public boolean accept(File f) {
            if(f != null) {
                if(f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if(extension != null && "xml".equalsIgnoreCase(extension)) {
                    return true;
                }
            }

            return false;
        } // end accept

        /**
         * Return the extension portion of the file's name.
         */
        public String getExtension(File f) {
            if (f != null) {
                String filename = f.getName();
                int i = filename.lastIndexOf('.');

                if (i > 0 && i < filename.length()-1) {
                    return filename.substring(i+1).toLowerCase();
                }
            }

            return null;
        } // end getExtension

        /**
         * Returns the human readable description of this filter. For
         * example: "XML and XSL Files (*.xml, *.xsl)"
         */
        public String getDescription() {
            return "Extensible Markup Language (*.xml)";
        }

    } // end XmlFileFilter class

    public static void main (String[] args) {

        try {
            JFrame f = new TransformationServiceDriver();
            f.show();

            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.addWindowListener(new WindowAdapter() {
                public void windowClosing(Window e) {
                    System.exit(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end main

} // end TransformationServiceDriver
