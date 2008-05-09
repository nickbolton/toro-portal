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
package net.unicon.mercury.cache.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.cache.IMercuryListener;
import net.unicon.mercury.cache.MercuryCacheValidator;
import net.unicon.mercury.cache.MercuryCacheValidatorContainer;
import net.unicon.sdk.cache.jms.JMSCacheMessagePublisher;
import net.unicon.sdk.cache.jms.TextMessageHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;

public class MercuryCacheValidatorImpl implements MercuryCacheValidator {
	private static final Log log =
        LogFactory.getLog(MercuryCacheValidatorImpl.class);
	private static final MercuryCacheValidator INSTANCE = new MercuryCacheValidatorImpl();
	public static MercuryCacheValidator getInstance() { return INSTANCE; }
	
	private Map listeners;
	private Map targetMappings;
	
	private MercuryCacheValidatorImpl() {
		this.listeners = new HashMap();
		this.targetMappings = new HashMap();
	}
	
	public static void register() {
		MercuryCacheValidatorContainer.register(INSTANCE);
		if (PropertiesManager.getPropertyAsBoolean(
	            "org.jasig.portal.concurrency.multiServer")) {
			// Not a bug: This is used to reference JMSCacheMessagePublisher statically,
			// to ensure that it is configured in this context, which has its dependencies available.
			log.debug("Initializing JMSCacheMessagePublisher: "+JMSCacheMessagePublisher.class.getName());
		}
	}
	
	public void registerListener(IMercuryListener listener, String target) {
      String[] allTargets = getAllTargets(target);
		if (log.isDebugEnabled()) {
         StringBuffer buf = new StringBuffer();
         buf.append('[');
         for (int i = 0; i < allTargets.length-1; i++) {
            buf.append(allTargets[i]).append(", ");
         }
         buf.append(allTargets[allTargets.length-1])
            .append(']');
			log.debug("Registering IMercuryListener ["+
               listener.getClass().getName()+"] for user: "+
               target+" All Targets: "+buf.toString());
      }

      synchronized(this) {
	      targetMappings.put(listener, allTargets);
	
	      Set targlisteners = null;
	      for (int i = 0; i < allTargets.length; i++) {
				targlisteners = (Set)listeners.get(allTargets[i]);
				if (targlisteners == null) {
					targlisteners = new HashSet();
					listeners.put(allTargets[i], targlisteners);
				}
				targlisteners.add(listener);
	      }
      }
	}
	
	public synchronized void unregisterListener(IMercuryListener listener, String target) {
      String[] allTargets = (String[])targetMappings.get(listener);
      if (allTargets != null) {
			if (log.isDebugEnabled()) {
	         StringBuffer buf = new StringBuffer();
	         buf.append('[');
	         for (int i = 0; i < allTargets.length-1; i++) {
	            buf.append(allTargets[i]).append(", ");
	         }
	         buf.append(allTargets[allTargets.length-1])
	            .append(']');
				log.debug("Unregistering IMercuryListener ["+
	               listener.getClass().getName()+"] for user: "+
	               target+" All Targets: "+buf.toString());
	      }
         
	      Set targlisteners = null;
	      for (int i = 0; i < allTargets.length; i++) {
				targlisteners = (Set)listeners.get(allTargets[i]);
				if (targlisteners != null) {
					targlisteners.remove(listener);
					if (targlisteners.isEmpty())
						listeners.remove(allTargets[i]);
				}
	      }
      } else if (log.isDebugEnabled()) {
         log.debug("Unregistering IMercuryListener ["+
               listener.getClass().getName()+"] for user: "+
               target+" but no know mappings exist.");
      }

      targetMappings.remove(listener);
	}
   
   private String[] getAllTargets(String username) {
      IAcademusGroup[] groups = null;
      Set rslt = new HashSet();
      rslt.add(username);

      try {
         groups = AcademusFacadeContainer.retrieveFacade().getAllContainingGroups(username);
      } catch (AcademusFacadeException e) {
         log.error("Error during facade retrieval", e);
      }

      if (groups != null && groups.length > 0) {
	      try {
	         for (int i = 0; i < groups.length; i++) {
	            rslt.add(groups[i].getGroupPaths(null, false)[0]);
	         }
	      } catch (AcademusFacadeException e) {
	         log.error("Error during group path resoltuion", e);
	      }
      }
      
      return (String[])rslt.toArray(new String[0]);
   }
	
	public void markAsDirty(IMessage mesg) throws MercuryException {
		Set targets = new HashSet();
		IRecipient[] recips = mesg.getRecipients();
    	for (int i = 0; i < recips.length; i++) {
    		targets.add(recips[i].getAddress().toNativeFormat());
    	}
    	targets.add(mesg.getSender().toNativeFormat());
    	markAsDirty((String[])targets.toArray(new String[targets.size()]));
	}

	public void markAsDirty(String[] targets) {
		markAsDirty(targets, true);
	}
	
	void markAsDirty(String[] targets, boolean needJms) {
		for (int i = 0; i < targets.length; i++) {
			markAsDirty(targets[i]);
		}
		
		if (needJms && PropertiesManager.getPropertyAsBoolean(
	            "org.jasig.portal.concurrency.multiServer")) {
			MercuryMessageHelper mh = new MercuryMessageHelper();
			mh.setTargets(targets);
			publishTextMessage(mh);
		}
	}

	private void markAsDirty(String target) {
		if (log.isDebugEnabled())
			log.debug("MercuryCacheValidator::markAsDirty(): Dirtying target: "+target);
		Set targlisteners = (Set)listeners.get(target);
		if (targlisteners != null) {
			Iterator it = targlisteners.iterator();
			IMercuryListener l = null;
			while (it.hasNext()) {
				l = (IMercuryListener)it.next();
				l.markAsDirty();
			}
		}
	}

    private void publishTextMessage(TextMessageHelper messageHelper) {
        try {
            JMSCacheMessagePublisher.publishAsynchTextMessage(
                messageHelper.getTextMessage(),
                messageHelper.getClass().getName());
        } catch (JMSException e) {
            log.error("Failed to publish JMS Text Message", e);
        }
    }
}
