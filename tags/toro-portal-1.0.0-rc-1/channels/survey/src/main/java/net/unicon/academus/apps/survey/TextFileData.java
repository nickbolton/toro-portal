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
package net.unicon.academus.apps.survey;

import java.io.*;

import java.util.*;



import net.unicon.academus.apps.rad.XMLData;



import javax.activation.DataSource;



public class TextFileData extends XMLData {



  public void putFileName(String fileName) {

    putA("FileName",fileName);

  }

  public String getFileName() {

    return (String)getA("FileName");

  }



  public void putFileShow(String fileShow) {

    putA("FileShow",fileShow);

  }

  public String getFileShow() {

    return (String)getA("FileShow");

  }



  public void putVersion(String version) {

    putA("Version",version);

  }

  public String getVersion() {

    return (String)getA("Version");

  }



  public void putContent(String content) {

    putA("Content",content);

  }

  public String getContent() {

    return (String)getA("Content");

  }



  public static TextFileData parse(DataSource fileSource) throws Exception {

    TextFileData td = new TextFileData();

    StringBuffer sb = new StringBuffer();

    InputStream input = fileSource.getInputStream();

    int b;

    while ((b = input.read()) != -1)

      sb.append((char)b);

    td.putFileShow(fileSource.getName());

    td.putContent(sb.toString());

    return td;

  }



  public static String getContentByFilename(String filename) throws IOException {

    StringBuffer sb = new StringBuffer();

    FileInputStream in = new FileInputStream(filename);

    int b;

    while ((b = in.read()) != -1)

      sb.append((char)b);

    return sb.toString();

  }



  public static Vector filterFile(Vector filev) throws Exception {

    for(int j=0; j<filev.size(); j++) {

      TextFileData tdJ = (TextFileData)filev.elementAt(j);

      for(int k=j+1; k<filev.size(); k++) {

        TextFileData tdK = (TextFileData)filev.elementAt(k);

        if(tdJ.getFileShow().equals(tdK.getFileShow()))

          if(Integer.parseInt(tdJ.getVersion()) <= Integer.parseInt(tdK.getVersion())) {

            filev.removeElementAt(j);

            k--;

          }

      }

    }

    return filev;

  }



  public static TextFileData parseFilePath(String path) throws Exception {

    TextFileData td = new TextFileData();

    String file = new java.io.File(path).getName();

    td.putFileShow(file);

    td.putFileName(path);

    td.putVersion("0");

    for(int endIndex=file.length()-1; endIndex>0; endIndex--)

      if(file.charAt(endIndex)=='_') {

        td.putVersion(file.substring(endIndex+2,file.length()));

        td.putFileShow(file.substring(0,endIndex));

        td.putFileName(path);

        break;

      }

    return td;

  }

}

