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

package net.unicon.portal.channels.curriculum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;

// JASIG Tree.
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.MultipartDataSource;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;

// Academus Tree.

// Common Tree.
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.catalog.Catalog;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.FLazyCatalog;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;
import net.unicon.sdk.catalog.UsabilityUtils;
import net.unicon.sdk.catalog.collection.ColCatalogFactory;
import net.unicon.sdk.catalog.db.IDbEntryConvertor;
import net.unicon.sdk.catalog.db.FDbFilterMode;
import net.unicon.sdk.catalog.db.FDbPageMode;
import net.unicon.sdk.catalog.db.FDbSortMode;

// Portal Tree.
import org.jasig.portal.UploadStatus;

import net.unicon.academus.common.XMLAbleEntity;
import net.unicon.academus.delivery.DeliveryAdapter;
import net.unicon.academus.delivery.DeliveryAdapterFactory;
import net.unicon.academus.delivery.DeliveryCurriculum;
import net.unicon.academus.delivery.DeliveryCurriculumImpl;
import net.unicon.academus.domain.lms.Context;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.Topic;
import net.unicon.academus.domain.lms.TopicType;
import net.unicon.academus.domain.lms.User;
import net.unicon.portal.common.BaseOfferingSubChannel;
import net.unicon.portal.common.properties.PortalPropertiesType;
import net.unicon.portal.common.service.file.FileService;
import net.unicon.portal.common.service.file.FileServiceFactory;

import net.unicon.portal.util.db.FDbDataSource;


public final class CurriculumUsabilityChannel extends BaseOfferingSubChannel
                                implements IMultithreadedMimeResponse {

    public CurriculumUsabilityChannel () {
        super();
    }

    /** Channel Handle found in Channels.xml */
    protected static final String CHANNEL_HANDLE = "CurriculumUsabilityChannel";

    // Commands
    protected static final String ADD_COMMAND     = "add";
    protected static final String INSERT_COMMAND  = "insert";
    protected static final String DELETE_COMMAND  = "delete";
    protected static final String CONFIRM_COMMAND = "confirm";
    protected static final String REMOVE_CONF_COMMAND  = "remove";
    protected static final String SEARCH_COMMAND  = "search";

    // Stylesheets
    protected static final String ADD_SHEET      = "add";
    protected static final String REMOVE_SHEET   = "remove";
    protected static final String INACTIVE_SHEET = "inactive";
    protected static final String MAIN_SHEET     = "main";
    protected static final String SEARCH_SHEET   = "search";

    // Paramaters
    protected static final String CURR_ID_PARAM_KEY = "id";
    protected static final String TITLE_PARAM_KEY   = "title";
    protected static final String CURRICULUM_ID_KEY = "curriculumID";
    protected static final String TITLE_PARAM_DEFAULT_VAL = "";
    protected static final String DESCRIPTION_PARAM_DEFAULT_VAL = "";
    protected static final String DESCRIPTION_PARAM_KEY   = "description";
    protected static final String ANDOR_PARAM_KEY         = "searchAndOr";
    protected static final String PAGE_SIZE_PARAM_KEY     = "catPageSize";
    protected static final String PAGE_NUM_PARAM_KEY      = "catSelectPage";

    protected CurriculumService currService = null;
    protected static final int maxFileSize = PropertiesManager.getPropertyAsInt(
                "org.jasig.portal.RequestParamWrapper.file_upload_max_size");
    protected static final String SIZE_FORMAT = "##0.##";
    protected static DecimalFormat fileSizeFormatter = new DecimalFormat(SIZE_FORMAT);

    /**
     *
     */
    public void buildXML(String upId) throws Exception {

        ChannelRuntimeData runtimeData = super.getRuntimeData(upId);


/* spill parameters (uncomment to see)...
System.out.println("#### SPILLING PARAMETERS...");
Iterator it = runtimeData.keySet().iterator();
while (it.hasNext()) {
    String key = (String) it.next();
    String value = runtimeData.getParameter(key);
    System.out.println("\t"+key.toString()+"="+value);
}
*/

        /* check upload status before continuing */
        UploadStatus uploadStatus =
            (UploadStatus)runtimeData.getObjectParameter("upload_status");
        if (uploadStatus != null
                && uploadStatus.getStatus() == UploadStatus.FAILURE) {
            StringBuffer errMsg = new StringBuffer(
            "File exceeds max size limit of ");
            errMsg.append(uploadStatus.getFormattedMaxSize());
            setErrorMsg(upId, errMsg.toString());
            return;
        }

        // SSL Location.
        super.setSSLLocation(upId, "CurriculumChannel.ssl");
        super.setSheetName(upId, MAIN_SHEET);   // Default.

        Map curriculumParams = getXSLParameters(upId);

        curriculumParams.put("onlineCurriculumAvailable", "" + UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.delivery.DeliveryAdapter"));

        // User object.
        User user = super.getDomainUser(upId);

        // Context object for the current user
        Context context = user.getContext();

        // Offering Object from the Context Object
        Offering offering  = context.getCurrentOffering(TopicType.ACADEMICS);

        String command = runtimeData.getParameter("command");

        /* Getting Database Connection */
        Connection conn = null;
        try {
        conn = super.getDBConnection();

        List courses = new ArrayList();
        List instructorCourses  = new ArrayList();

        currService = CurriculumServiceFactory.getService();

        if (command != null && offering != null) {

            curriculumParams.put("offeringName", offering.getName());

            if (command.equals(ADD_COMMAND)) {

                //------------------------
                // ADD CURRICULUM FORM
                //------------------------

                super.setSheetName(upId, ADD_SHEET);

                //display the search field if the online curiculum is available
                curriculumParams.put("onlineCurriculumAvailable",

                "" + UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getPropertyAsBoolean("net.unicon.academus.delivery.DeliveryAdapter"));

                // Getting available curriculum if delivery system = true
                // KG: add sheet changed to no longer display a dropdown
            }
            else if (command.equals(REMOVE_CONF_COMMAND)) {

                //------------------------
                // DELETE CURRICULUM (Confirmation Page)
                //------------------------

                super.setSheetName(upId, REMOVE_SHEET);

                // Passing data into the XSL
                curriculumParams.put(CURR_ID_PARAM_KEY,
                                     runtimeData.getParameter(CURR_ID_PARAM_KEY));
                curriculumParams.put(TITLE_PARAM_KEY,
                                     runtimeData.getParameter(TITLE_PARAM_KEY));
            }
            else if (command.equals(INSERT_COMMAND)) {
                //System.out.println("CUC::INSERT");

                //------------------------
                // ADD SUBMIT
                //------------------------

                this.addCurriculum(runtimeData, offering, upId, conn);

                // Dirty cache for all users in the offering
                super.broadcastUserOfferingDirtyChannel(user, offering,
                    CHANNEL_HANDLE, false);
            }
            else if (command.equals(SEARCH_COMMAND)) {
                //System.out.println("CUC::SEARCH");

                //------------------------------
                // KG: SEARCH for CURRICULUM CONTENT
                //------------------------------

                // title, description, and and/or are on the search form
                String title = runtimeData.getParameter(TITLE_PARAM_KEY);
                String desc  = runtimeData.getParameter(DESCRIPTION_PARAM_KEY);
                String andor = runtimeData.getParameter(ANDOR_PARAM_KEY);

                // page size and current page ceom from the nav area of the form

                int pageSize = UsabilityUtils.evaluatePageSize(runtimeData.getParameter(PAGE_SIZE_PARAM_KEY));
                int pageNum  = UsabilityUtils.evaluateCurrentPage(runtimeData.getParameter(PAGE_NUM_PARAM_KEY));

                courses = searchCurriculum(offering, upId, title, desc, andor, pageSize, pageNum);

                super.setSheetName(upId, SEARCH_SHEET);
            }
            else if (command.equals(DELETE_COMMAND)) {
                //System.out.println("CUC::DELETE");

                //------------------------
                // DELETE CURRICULUM
                //------------------------
                String idStr   = runtimeData.getParameter(CURR_ID_PARAM_KEY);
                String confirm = runtimeData.getParameter("commandButton");

                // Looking for a Yes for deleting curriculum
                if (idStr != null && confirm.equals(CONFIRM_COMMAND)) {

                    int id = Integer.parseInt(idStr);
                    currService.deleteCurriculum(id, offering, conn);
                    super.removeChannelAttribute(upId, idStr);

                    // Dirty cache for all users in the offering
                    super.broadcastUserOfferingDirtyChannel(user, offering,
                        CHANNEL_HANDLE, false);
                }
            }

            // Getting upto date curriculum data and cache it in channel cache
            if (command.equals(INSERT_COMMAND) || command.equals(DELETE_COMMAND)) {
                courses = currService.getCurriculum(offering, user, true, conn);
                // Curricula with reference links which include Instructor Notes
                instructorCourses = currService.getCurriculum(offering, user, true, conn, true);
                this.cacheCurriculumObjects(courses, upId);
            }

        } else if (offering == null) {
            //Getting empty sheet
            super.setSheetName(upId, "empty");
        } else {
	        courses = currService.getCurriculum(offering, user, true, conn);
	        // Curricula with reference links which include Instructor Notes
	        instructorCourses = currService.getCurriculum(offering, user, true, conn, true);
            this.cacheCurriculumObjects(courses, upId);
        }

        StringBuffer stuff = new StringBuffer("<?xml version=\"1.0\"?>");

        // Add course list to the output.
        stuff.append("<course-list>");
        if (courses != null) {
            for (int ix = 0; ix < courses.size(); ix++ ) {
                stuff.append(((XMLAbleEntity) courses.get(ix)).toXML());
            }
        }
        
        // Adding course list that contains Instructor Notes reference links
        if (!instructorCourses.isEmpty()) {
	        stuff.append("<instructor-course-list>");
	        for (int ix = 0; ix < courses.size(); ix++ ) {
	            stuff.append(((XMLAbleEntity) instructorCourses.get(ix)).toXML());
	        }
	        stuff.append("</instructor-course-list>");
        }

        stuff.append("</course-list>");

// System.out.println(stuff.toString());

        setXML(upId, stuff.toString());

        } finally {
            super.releaseDBConnection(conn);
        }

    }

    // KG - return a list of course objects in the catalog

    protected List searchCurriculum(Offering o, String upId, String title, String desc,
                        String andor, int pageSize, int pageNum) throws Exception {

        // Construct filter modes.
        List filterModes  = new ArrayList();

        if (title != null && title.trim().length() != 0) {

            final String tStr = title;
            IFilterMode tFilter = new ColCatalogFactory.IColFilterMode() {
                public Object[] filter(Object[] entries) {

                    // Assertions.
                    if (entries == null) {
                        String msg = "Argument 'entries' cannot be null.";
                        throw new IllegalArgumentException(msg);
                    }

                    List rslt = new ArrayList();
                    Iterator it = Arrays.asList(entries).iterator();
                    while (it.hasNext()) {
                        DeliveryCurriculum c = (DeliveryCurriculum) it.next();
                        if (c.getTitle().toUpperCase().indexOf(tStr.toUpperCase()) != -1) {
                            rslt.add(c);
                        }
                    }
                    return rslt.toArray();
                }
            };
            filterModes.add(tFilter);

        }

        if (desc != null && desc.trim().length() != 0) {

            final String dStr = desc;
            IFilterMode dFilter = new ColCatalogFactory.IColFilterMode() {
                public Object[] filter(Object[] entries) {

                    // Assertions.
                    if (entries == null) {
                        String msg = "Argument 'entries' cannot be null.";
                        throw new IllegalArgumentException(msg);
                    }

                    List rslt = new ArrayList();
                    Iterator it = Arrays.asList(entries).iterator();
                    while (it.hasNext()) {
                        DeliveryCurriculum c = (DeliveryCurriculum) it.next();
                        if (c.getDescription().toUpperCase().indexOf(dStr.toUpperCase()) != -1) {
                            rslt.add(c);
                        }
                    }
                    return rslt.toArray();
                }
            };
            filterModes.add(dFilter);
        }

        // Filter that is applied when using OR
        if (filterModes.size() == 2 && andor != null && UsabilityUtils.evaluateSearchAndOr(andor) == UsabilityUtils.SEARCH_OR) {
            
        	// Separates filterMode list into 2 lists
        	List filterModes2  = new ArrayList();
        	IFilterMode orFilter = (IFilterMode)filterModes.remove(1);
        	filterModes2.add(orFilter);
        	
        	ColCatalogFactory fac = new ColCatalogFactory();
            IPageMode pg = fac.createPageMode(pageSize, pageNum);
            
            // Uses 2 different catalogs each with that applies its own filterMode list
            Catalog catalog = new FLazyCatalog(createCurriculumDataSource(o, super.getDomainUser(upId)));
            Catalog catalog2 = new FLazyCatalog(createCurriculumDataSource(o, super.getDomainUser(upId)));
            catalog = catalog.subCatalog(
                                new ISortMode [] { new TitleSortMode() },
                                (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
                                pg);
            catalog2 = catalog2.subCatalog(
                    new ISortMode [] { new TitleSortMode() },
                    (IFilterMode[]) filterModes2.toArray(new IFilterMode[0]),
                    pg);            

            try {
                List elm = catalog.elements();
                List elm2 = catalog2.elements();
                
                // Performs a union operation of both element lists
                Iterator it = elm2.iterator();
                while (it.hasNext()) {
                	Object obj = it.next();
                	if (obj instanceof DeliveryCurriculum)	{
                		DeliveryCurriculum dc = (DeliveryCurriculum) obj;
                    	if (!elm.contains(dc))
                    		elm.add(obj);	
                	} else {
                		System.out.println("obj is not an instance of DeliveryCurriculum");
                	}
                }
                return elm;
            } catch (CatalogException ce) {
                ce.printStackTrace(System.out);
                return null;
            }
        	
        } else {
        	// Filters that are applied when title, description, and title AND description are selected
	        ColCatalogFactory fac = new ColCatalogFactory();
	        IPageMode pg = fac.createPageMode(pageSize, pageNum);
	        Catalog catalog = new FLazyCatalog(createCurriculumDataSource(o, super.getDomainUser(upId)));
	        catalog = catalog.subCatalog(
	                            new ISortMode [] { new TitleSortMode() },
	                            (IFilterMode[]) filterModes.toArray(new IFilterMode[0]),
	                            pg);
	
	        try {
	
	            /* XXX
	            // Share info w/ the xsl.
	            Map xslParams = getXSLParameters(upId);
	            xslParams.put("catCurrentCommand", "search");
	            xslParams.put("catCurrentPage", new Integer(pageNum));
	            xslParams.put("catLastPage", new Integer(pageMode.getPageCount()));
	            */
	
	            List elm = catalog.elements();
	            return elm;
	
	        } catch (CatalogException ce) {
	
	            ce.printStackTrace(System.out);
	
	            return null;
	        }
        }
    }

    private IDataSource createCurriculumDataSource(Offering o, User u) throws Exception {

        Topic t = o.getTopic();
        DeliveryAdapter da = DeliveryAdapterFactory.getAdapter();
        //IContentGroup[] groups = ContentAssociationManager.getAssociations(t);
        //String[] handles = new String[groups.length];
        //for (int i=0; i < handles.length; i++) {
        //    handles[i] = groups[i].getHandle();
        //}
        List courses = da.getAllCourses(new String[0], u);
        Object[] data = courses.toArray();
        ColCatalogFactory fac = new ColCatalogFactory();
        return fac.createDataSource(data);

    }

    private static final class TitleSortMode implements ColCatalogFactory.IColSortMode {

        /*
         * Public API.
         */

        public TitleSortMode() {}

        public Object[] sort(Object[] entries) {

            // Assertions.
            if (entries == null) {
                String msg = "Argument 'entries' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Load into map.
            Map m = new HashMap();
            Iterator it = Arrays.asList(entries).iterator();
            while (it.hasNext()) {
                Object o = it.next();
                DeliveryCurriculum c = (DeliveryCurriculum) o;
                m.put(c.getTitle(), c);
            }

            // Sort.
            List l = new ArrayList(m.keySet());
            Collections.sort(l);

            // (Re)compose.
            String[] keys = (String[]) l.toArray(new String[0]);
            Object[] rslt = new Object[l.size()];
            for (int i=0; i < keys.length; i++) {
                rslt[i] = m.get(keys[i]);
            }

            return rslt;

        }

    }

    protected static final File curriculumDir = new File(UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getProperty("net.unicon.portal.curriculum.curriculumFileDir"));

    /**
     * @param <code>net.unicon.portal.domain.Offering</code> - an offering
     * @return <code>String</code>
     * @exception <{Exception}>
     * @see <{net.unicon.portal.domain.Offering}>
     */

    public String exportChannel(Offering offering)

    throws Exception {

        return "";

    }

    /**
     * @param <code>org.w3c.dom.Document</code> - a dom document
     * @param <code>net.unicon.portal.domain.Offering</code> - an offering
     * @return <code>Map</code>
     * @exception <{Exception}>
     * @see <{net.unicon.portal.domain.Offering}>
     * @see <{org.w3c.dom.Document}>
     */

    public synchronized Map importChannel(Offering offering)

    throws Exception {

        return new HashMap();

    }

    private void cacheCurriculumObjects(List currList, String upId) {

        if (currList != null) {

            Curriculum currObj = null;

            for (int ix = 0; ix < currList.size(); ++ix) {

                currObj = (Curriculum) currList.get(ix);

                super.putChannelAttribute(upId, currObj.getId(), currObj);

            }

        }

    }

    public String getContentType(String upId) {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        String contentType = null;

        if (currIDstr != null) {

            Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

            contentType = curriculum.getContentType();

        }

        return contentType;

    }

    public InputStream getInputStream(String upId) throws IOException {

        ChannelRuntimeData rd = super.getRuntimeData(upId);

        // currIDstr.
        String currIDstr = rd.getParameter(CURRICULUM_ID_KEY);
        if (currIDstr == null) {
            throw new IOException("No id for file given");
        }

        Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

        String type = curriculum.getType();

        InputStream is = null;

        if (type.equals(Curriculum.ONLINE)) {

            // Getting User object

            Connection conn = null;
            User user = super.getDomainUser(upId);

            try {

                // Theme.
                String theme = curriculum.getTheme();
                if (rd.getParameter("theme") != null && rd.getParameter("theme").trim().length() != 0) {
                    theme = rd.getParameter("theme");
                }

                // Style.
                String style = curriculum.getStyle();
                if (rd.getParameter("style") != null && rd.getParameter("style").trim().length() != 0) {
                    style = rd.getParameter("style");
                }

                currService = CurriculumServiceFactory.getService();

                conn = super.getDBConnection();

// ToDo:  The following may be breaking functionality -- fix!
                String urlref = currService.getCurriculumLink(curriculum.getReference(),
                                                            user, null, conn, false);


                URL url = new URL(urlref);

                is = url.openStream();

            } catch (Exception e) {

                throw new IOException("Unable to Initalize Service");

            } finally {
                super.releaseDBConnection(conn);
            }

        }

        else if (type.equals(Curriculum.FILE)) {

            File baseDir = new File(curriculumDir, "" + curriculum.getOfferingID());

            try {

                File file = FileServiceFactory.getService().getFile(baseDir, curriculum.getReference());

                if (file == null) {

                    throw new IOException ("File " + getName(upId) + " not found.");

                }

                is = new FileInputStream(file);

            } catch (FactoryCreateException fce) {

                fce.printStackTrace();

                throw new IOException ("Error getting FileService");

            }

        }

        return is;

    }

    public void downloadData(OutputStream out, String upId) throws IOException {

    }

    public String getName(String upId) {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        String fileName = null;

        if (currIDstr != null) {

            Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

            fileName = curriculum.getReference();

        }

        return fileName;

    }

    public Map getHeaders(String upId) {

        String currIDstr = super.getRuntimeData(upId).getParameter(CURRICULUM_ID_KEY);

        if (currIDstr != null) {

            Curriculum curriculum = (Curriculum) super.getChannelAttribute(upId, currIDstr);

            String type = curriculum.getType();

            if ((Curriculum.FILE).equals(type)) {

                HashMap headers = new HashMap();

                headers.put("Content-Disposition", "attachment; filename=\"" +

                getName(upId) + "\"");

                return headers;

            }

        }

        return null;

    }

    private void addCurriculum(ChannelRuntimeData runtimeData, Offering offering,
                            String upId, Connection conn) throws Exception {

        String name        = runtimeData.getParameter("title");
        String description = runtimeData.getParameter("description");
        String type        = runtimeData.getParameter("type");
        String contentType = null;
        String reference   = null;

        // For online items.
        String theme = null;
        String style = null;
        List themes = null;
        boolean themeStyleMatch = false;
        
        if (type.equals("url")) {

            // Adding a URL of curriculum
            reference = runtimeData.getParameter("curriculumURL");

        } else if (type.equals("file")) {


            // Adding a File suchas PPT, etc for curriculum
            Object[] x = runtimeData.getObjectParameterValues("uploadedFile");

            if (x == null || x.length <= 0) {

                setErrorMsg(upId, "Error uploading file.");
                return;
            }

            MultipartDataSource content = (MultipartDataSource) x[0];

            InputStream is = content.getInputStream();
            reference      = content.getName();
            contentType    = content.getContentType();

            // Checking if file is under the max size limit

            /*
            if (is.available() > maxFileSize) {
                StringBuffer errMsg = new StringBuffer(
                "File exceeds max size limit of ");
                errMsg.append(getMaxUploadSize());
                setErrorMsg(upId, errMsg.toString());
                return;
            }
            */

            // Checking the integirty of the file

            if (reference == null || "".equals(reference)) {

                setErrorMsg(upId, "No filename given for uploaded file.");
                return;
            }

            // Verify file type

            if (contentType == null || "".equals(contentType)) {

                LogService.log(LogService.ERROR,
                "GradebookChannel::parseActivationData() : no contentType: "
                + contentType);
                setErrorMsg(upId, "Error uploading file.");
                return;
            }

            if (is == null) {

                setErrorMsg(upId, "Error uploading file.");
                return;
            }

            FileService us = FileServiceFactory.getService();
            File dir = new File(curriculumDir,
            "" + (int) offering.getId());
            File uploadedFile = us.uploadFile(dir, reference, is);
            is.close();

        } else {

            return;
        }

        if (name != null && type != null && reference != null) {

            currService.saveCurriculum(name, description, type, reference, contentType, offering, theme, style, conn);
        }
    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      LogService.log(LogService.ERROR, "CurriculumUsabilityChannel::reportDownloadError(): " + e.getMessage());
    }
}

