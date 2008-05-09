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
package net.unicon.academus.delivery.virtuoso;

import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.properties.UniconPropertiesFactory;

import java.lang.reflect.Constructor;

public class MessageProcessorFactory {

    // Retrieve property that determines message processor implementation
    private static final String MESSAGE_PROCESSOR =
            UniconPropertiesFactory.getManager(VirtuosoPropertiesType.JMS)
                    .getProperty("JMSMessageProcessor");

    /**
     * Returns the configured message processor implementation.
     * @return The configured message processor implementation.
     */
    public static VirtuosoMessageProcessor getMessageProcessor()
    throws FactoryCreateException {

        VirtuosoMessageProcessor processor = null;

        try {
            // Initialize message processor to be used
            Class processorClass = Class.forName(MESSAGE_PROCESSOR);
            Constructor processorConstructor =
                    processorClass.getConstructor(new Class[0]);

            processor =
                    (VirtuosoMessageProcessor) processorConstructor.newInstance(new Object[0]);

        } catch (Exception e) {

            StringBuffer errorMsg = new StringBuffer(64);
            errorMsg.append("Unable to instantiate message processor ");
            errorMsg.append("with class name:");
            errorMsg.append(MESSAGE_PROCESSOR);
            errorMsg.append(".");

            throw new FactoryCreateException(errorMsg.toString(), e);
        }

        return processor;
    }
}
