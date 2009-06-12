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
package com.interactivebusiness.portal.channel.utils;

import org.jasig.portal.PortalException;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.security.IPerson;

import org.xml.sax.ContentHandler;

import java.util.Map;


public class WalletAgent implements IAuthentication {

  WalletBase walletBase;

  public WalletAgent() {
    walletBase = new WalletBase();
  }

  public Map getCredentials (String sApp){
    return walletBase.getWalletCredentials(sApp);
  }

  public Map getCredentials (IPerson p){
    return walletBase.getWalletCredentials(p);
  }

  public boolean storeCredentials (String sApp, Map mCredentials){
    return walletBase.storeWalletCredentials(sApp, mCredentials);
  }

  public void removeCredentials (String sApp){
    walletBase.removeWalletCredentials(sApp);
  }

  public void setStaticData(ChannelStaticData sd) throws PortalException{
    walletBase.setStaticData(sd);
  }

  public void setRuntimeData(ChannelRuntimeData rd) throws PortalException{
    walletBase.setRuntimeData(rd);
  }

  public void receiveEvent(PortalEvent ev){
    walletBase.receiveEvent(ev);
  }

  public ChannelRuntimeProperties getRuntimeProperties(){
    return walletBase.getRuntimeProperties();
  }

  public void renderXML(ContentHandler out) throws PortalException {
    walletBase.renderXML(out);
  }

  public boolean isFinished (){
    return walletBase.isFinished();
  }

  public Object[] getResults (){
    return walletBase.getResults();
  }

  public void setChannelStaticData (ChannelStaticData csd) {
  //stub
  }

}