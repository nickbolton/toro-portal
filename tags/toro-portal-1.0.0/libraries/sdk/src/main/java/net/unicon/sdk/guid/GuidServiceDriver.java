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
package net.unicon.sdk.guid;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class GuidServiceDriver extends JFrame implements ActionListener {
    public JButton testButton;
    public JButton clearButton;
    public JTextField valueText;
    public JLabel valueLabel;

    public JTextArea textArea;
    public JScrollPane scrollPane;

    public JPanel topPanel;
    public JPanel middlePanel;
    public JPanel bottomPanel;

    private int windowWidth  = 640;
    private int windowHeight = 550;
    private int columnWidth  = 40;

    private IGuidService guidService;

    public GuidServiceDriver() throws Exception {
        this.setTitle("GUID Service Driver");
        this.setSize(windowWidth, windowHeight);

        // Panels
        topPanel    = new JPanel();
        middlePanel = new JPanel();
        bottomPanel = new JPanel();

        // Labels
        valueLabel = new JLabel(" Number of GUIDs:");

        //Text Fields
        valueText = new JTextField(15);

        // Text Area
        textArea = new JTextArea(28, 52);
        textArea.setLineWrap(true);
        scrollPane = new JScrollPane(textArea);

        // Layout Code
        middlePanel.add(valueLabel);
        middlePanel.add(valueText);
        middlePanel.add(scrollPane, "South");

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

        // Get reference to IGuidService
        guidService = GuidServiceFactory.getService();

    } // default constructor

    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        if(source == testButton) {
            StringBuffer validBuffer = new StringBuffer();
            StringBuffer errorBuffer = new StringBuffer();
            String valueString  = valueText.getText();

            if(valueString.length() > 0) {

                try {
                    int valueInt = Integer.parseInt(valueText.getText());
                    
                    if (valueInt > 0 && valueInt < 100001) {
                        Map guids = new HashMap();
                        int errorCount = 0;

                        for (int i = 0; i < valueInt; i++) {
                            Guid guid = null;
                            Context ctx = new Context("TEST");
                            guid = guidService.generate(ctx);
                            String value = guid.getValue();

                            if (!guids.containsKey(value)) {
                                guids.put(value, null);
                                validBuffer.append(value).append("\n");
                            } else {
                                errorBuffer.append(value);
                            }
                        } // end for loop

                        errorBuffer.append("\n");
                        errorBuffer.append(errorCount);
                        errorBuffer.append(" GUIDs were duplicates:\n");

                        validBuffer.append("\n");
                    } else {
                        validBuffer.append("\nERROR: Exceeded number of allowable GUIDs.\n");
                    }

                } catch (Exception e) { e.printStackTrace(); }

            } // end if

            // add to results to textarea.
            textArea.append(validBuffer.toString());
            textArea.append(errorBuffer.toString());
        }

        if(source == clearButton) {
            textArea.setText("");
        }

    } // end actionPerformed

    public static void main (String[] args) {

        try {

            JFrame f = new GuidServiceDriver();
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

} // end GuidServiceDriver
