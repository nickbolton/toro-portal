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
package net.unicon.portal.common.service.file;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.portal.common.service.file.FileService;
import org.jasig.portal.MultipartDataSource;
import java.io.*;

public class FileServiceImpl implements FileService {
    public FileServiceImpl() {
    }
    protected static final String repositoryBaseDir = UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.channels.file.FileService.repositoryBaseDir");
    // This method will write the given InputStream to the file
    // <repositoryBaseDir>/<relPath>/filename
    public File uploadFile(File relPath, String filename, InputStream is)
    throws IOException {
        return uploadFile(relPath.getPath(), filename, is);
    }
    public File uploadFile(String relPath, String filename, InputStream is)
    throws IOException {
        File dir = new File(repositoryBaseDir, relPath);
        dir.mkdirs();
        File file = new File(dir, filename);
        file.delete();
        writeFile(is, file);
        return file;
    }
    private void writeFile(InputStream stream, File file)
    throws IOException {
        int numRead = 0;
        byte[] b = new byte[4096];
        FileOutputStream f = new FileOutputStream(file);
        while ((numRead = stream.read(b, 0, 4096)) != -1) {
            f.write(b, 0, numRead);
        }
        stream.close();
        f.close();
    }
    // This method retrieves a file from the repository. If the file
    // does not exist, if returns null.
    public File getFile(File relPath, String filename) {
        return getFile(relPath.getPath(), filename);
    }
    // This method retrieves a file from the repository. If the file
    // does not exist, if returns null.
    public File getFile(String relPath, String filename) {
        File dir = new File(repositoryBaseDir, relPath);
        File file = new File(dir, filename);
        if (!file.exists()) return null;
        return file;
    }
}
