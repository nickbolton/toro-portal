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

package net.unicon.academus.delivery.virtuoso.theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

public final class ThemeBroker {

    private static Map themes = null;

    /*
     * Public API.
     */

    public static void bootstrap(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("theme-set")) {
            String msg = "Argument 'e [Element]' must be a <theme-set> "
                                                        + "element.";
            throw new IllegalArgumentException(msg);
        }

        Iterator it = null;

        // Parse the themes.
        List l = new ArrayList();
        it = e.selectNodes("theme").iterator();
        while (it.hasNext()) {
            Element t = (Element) it.next();
            l.add(StructuralThemeImpl.parse(t));
        }

        // Load the themes map.
        themes = new HashMap();
        it = l.iterator();
        while (it.hasNext()) {
            IStructuralTheme t = (IStructuralTheme) it.next();
            themes.put(t.getHandle(), t);
        }

    }

    public static boolean isInitialized() {
        return themes != null ? true : false;
    }

    public static IStructuralTheme[] getThemes() {

        // Assertions.
        if (themes == null) {
            String msg = "The ThemeBroker has not been initialized.";
            throw new IllegalStateException(msg);
        }

        return (IStructuralTheme[]) themes.values().toArray(
                                new IStructuralTheme[0]);

    }

    public static IStructuralTheme getTheme(String handle) {

        // Assertions.
        if (themes == null) {
            String msg = "The ThemeBroker has not been initialized.";
            throw new IllegalStateException(msg);
        }
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!themes.containsKey(handle)) {
            String msg = "This collection does not contain the specified "
                                                + "theme:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (IStructuralTheme) themes.get(handle);

    }

    /*
     * Implementation.
     */

    private ThemeBroker() {}

    /*
     * Nested Types.
     */

    private static final class StructuralThemeImpl implements IStructuralTheme {

        // Instance Members.
        private final String handle;
        private final IVisualStyle[] styles;

        /*
         * Public API.
         */

        public static IStructuralTheme parse(Element e) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("theme")) {
                String msg = "Argument 'e [Element]' must be a <theme> "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Handle.
            String handle = e.valueOf("@handle");
            if (handle == null || handle.trim().equals("")) {
                String msg = "Each <theme> element must include a '@handle' "
                                                            + "attribute.";
                throw new RuntimeException(msg);    // should change type...
            }

            // Styles.
            List l = new ArrayList();
            Iterator it = e.selectNodes("style").iterator();
            while (it.hasNext()) {
                Element s = (Element) it.next();
                l.add(VisualStyleImpl.parse(s));
            }
            IVisualStyle[] styles = (IVisualStyle[]) l.toArray(new IVisualStyle[0]);

            // Create & Return.
            return new StructuralThemeImpl(handle, styles);

        }

        public StructuralThemeImpl(String handle, IVisualStyle[] styles) {

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (styles == null) {
                String msg = "Argument 'styles' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (styles.length == 0) {
                String msg = "Argument 'styles' must contain at least one "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = handle;
            this.styles = new IVisualStyle[styles.length];
            System.arraycopy(styles, 0, this.styles, 0, styles.length);

        }

        public String getHandle() {
            return handle;
        }

        public IVisualStyle[] getStyles() {
            IVisualStyle[] rslt = new IVisualStyle[styles.length];
            System.arraycopy(styles, 0, rslt, 0, styles.length);
            return rslt;
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<theme ");

            // Handle.
            rslt.append("handle=\"");
            rslt.append(handle);
            rslt.append("\">");

            // Styles.
            Iterator it = Arrays.asList(styles).iterator();
            while (it.hasNext()) {
                IVisualStyle s = (IVisualStyle) it.next();
                rslt.append(s.toXml());
            }

            // End.
            rslt.append("</theme>");
            return rslt.toString();

        }

    }

    private static final class VisualStyleImpl implements IVisualStyle {

        // Instance Members.
        private final String handle;

        /*
         * Public API.
         */

        public static IVisualStyle parse(Element e) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("style")) {
                String msg = "Argument 'e [Element]' must be a <style> "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Handle.
            String handle = e.valueOf("@handle");
            if (handle == null || handle.trim().equals("")) {
                String msg = "Each <style> element must include a '@handle' "
                                                            + "attribute.";
                throw new RuntimeException(msg);    // should change type...
            }

            // Create & Return.
            return new VisualStyleImpl(handle);

        }

        public VisualStyleImpl(String handle) {

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = handle;

        }

        public String getHandle() {
            return handle;
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<style ");

            // Handle.
            rslt.append("handle=\"");
            rslt.append(handle);
            rslt.append("\" ");

            // End.
            rslt.append("/>");
            return rslt.toString();

        }

    }

}
