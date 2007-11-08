/*



 *******************************************************************************



 *



 * File:       UPortalChannel.java



 *



 * Copyright:  �2002 Unicon, Inc. All Rights Reserved



 *



 * This source code is the confidential and proprietary information of Unicon.



 * No part of this work may be modified or used without the prior written



 * consent of Unicon.



 *



 *******************************************************************************



 */



package net.unicon.portal.channels.campusannouncement.common;

import java.io.StringWriter;
import java.util.Hashtable;

import net.unicon.portal.channels.campusannouncement.domain.Universe;
import net.unicon.portal.channels.rad.Servant;
import net.unicon.portal.cscr.CscrChannelRuntimeData;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.XSLT;
import org.xml.sax.ContentHandler;

abstract public class UPortalChannel implements IChannel {

    protected ChannelStaticData staticData;



    protected ChannelRuntimeData runtimeData;



    protected Hashtable xslParameters = new Hashtable();



    protected String sslLocation;



    protected String sheetName;



    protected String xml;



    protected String errorMessage;


    //added by Jing
    private Servant m_servant;
    protected boolean servant_display;


    public UPortalChannel() {



    }



    public Universe getUniverse() {



        return Universe.getUniverse();



    }



    public IPerson getPerson() {



        return staticData.getPerson();



    }



    public ChannelRuntimeProperties getRuntimeProperties() {



        return new ChannelRuntimeProperties();



    }



    public void receiveEvent(PortalEvent event) {
    }



    public void setStaticData(ChannelStaticData staticData) throws PortalException {



        this.staticData = staticData;
        //added by Jing - for permission servant purpose
        try{
        int upId = Integer.parseInt(getStaticData().getChannelPublishId());
          ChannelDefinition cd = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(upId);

            net.unicon.portal.common.cdm.ChannelDataManager.registerChannelUser(
                getPerson(), null, cd, upId + "");
          }
        catch (Exception e){
            throw new PortalException(e);
        }
    }



    public ChannelStaticData getStaticData() {



        return staticData;



    }



    public void setRuntimeData(ChannelRuntimeData runtimeData) throws PortalException {
        CscrChannelRuntimeData ccrd = new CscrChannelRuntimeData( this.staticData.getChannelPublishId(), runtimeData );
        
        this.runtimeData = ccrd;
        if (staticData.get("AddressbookServant") != null)
        {
          getAddressBook(runtimeData);
          setKillCache (true);
        }
        //when click 'cancel' button
        if(runtimeData.getHttpRequestMethod().equals("GET") && runtimeData.getParameter("command") == null)
          setKillCache (true);

    }



    public ChannelRuntimeData getRuntimeData() {



        return runtimeData;



    }



    public void setupXSLParameters() throws PortalException {



        xslParameters.put("baseActionURL", getRuntimeData().getBaseActionURL());



        xslParameters.put("pageSize", "" + getPageSize());



        xslParameters.put("currentPage", "" + getCurrentPageNumber());



    }



    protected void buildErrorPage() {



        sslLocation = "/net/unicon/portal/channels/error/error.ssl";



        sheetName = "error";



        xml = "<errorPage><message>" + errorMessage + "</message></errorPage>";



    }



    public void renderXML(ContentHandler out) throws PortalException {



        long startTime = System.currentTimeMillis();



        try {


            setupXSLParameters();

            buildXML(out);

            //added by Jing
            if (servant_display)
            {
                servant_display=false;
                return;
            }

            // If Addressbook Servant is active, then call AddressBook
            /*
            m_servant = (Servant) getStaticData().get("AddressbookServant");
            if (m_servant != null)
            {
               servantRenderXML(out);
                return;
            }
            */
            if (errorMessage != null) {



                buildErrorPage();



            }



            //ContentHandler contentHandler = out;



            //boolean useSerialization = AcademusPropertiesFactory.getManager(PropertiesType.LMS).getPropertyAsBoolean("useDummy");
            //boolean useSerialization = true;//UniconPropertiesFactory.getManager(PortalPropertiesType.LMS).getPropertyAsBoolean("useDummy");



            StringWriter serializationWriter = null;



            /*if (useSerialization) {



                serializationWriter = new StringWriter();



                HTMLSerializer serializer = new HTMLSerializer();



                //no such method in uPortal 2.1

                //serializer.setBypassEscaping(true);



                serializer.setOutputCharStream(serializationWriter);



                serializer.asDocumentHandler();



                contentHandler = new ChannelSAXStreamFilter(serializer);



            }*/

            XSLT xsl = new XSLT(this);

            //xsl.setTarget(contentHandler);
            xsl.setTarget(out);



            xsl.setXML(xml);

            xsl.setXSL(sslLocation, sheetName, runtimeData.getBrowserInfo());



            xsl.setStylesheetParameters(xslParameters);



            xsl.transform();



            /*if (useSerialization) {



                out.startElement("", "deuce", "deuce", new AttributesImpl());



                char[] chars = serializationWriter.toString().toCharArray();



                out.characters(chars, 0, chars.length);



                out.endElement("", "deuce", "deuce");



            }*/



        } catch (Throwable t) {



            if (t instanceof ThreadDeath) {



                throw (ThreadDeath) t;



            }

            throw new PortalException(new Exception(t));
        }
    }



    abstract protected void buildXML(ContentHandler out) throws Exception;
    abstract public void setKillCache (boolean b);


    protected int getPageSize() {



        int pageSize = 10;



        String pageSizeString = getRuntimeData().getParameter("pageSize");



        if (pageSizeString != null && pageSizeString.length() > 0) {



            pageSize = (pageSizeString.equals("All")) ? 0 : Integer.parseInt(pageSizeString);



        }



        return pageSize;



    }



    protected int getCurrentPageNumber() {



        String pageNumberString = getRuntimeData().getParameter("currentPage");



        return (pageNumberString != null) ? Integer.parseInt(pageNumberString) : 1;



    }



    protected void getAddressBook (ChannelRuntimeData rd) {
     // Create communication with AddressBook
        m_servant = getAddressBookServant (rd);
        if (!m_servant.isFinished()) {
            m_servant.setStaticData(staticData);
            m_servant.setRuntimeData(rd);
        }
    }


    private Servant getAddressBookServant (ChannelRuntimeData rd) {
        if (staticData.get("AddressbookServant") == null) {
            String name = "net.unicon.portal.channels.addressbook.Select";
            m_servant = new Servant ();
            m_servant.start(name);
            // "do" and "go" are RAD reserved parameter names; if you use them in
            // your own XSLs, you must remove them for the initial call to a RAD
            // servant
            rd.remove("do");
            rd.remove("go");
            // Tien 1120 for Unicon
            rd.put("sources", "portal,campus");
            //
            staticData.put("AddressbookServant", m_servant);
        }

       else {
            m_servant = (Servant)  staticData.get("AddressbookServant");
        }
        return m_servant;
    }

}



