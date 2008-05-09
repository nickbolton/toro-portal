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
package net.unicon.resource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ComponentCreator {
    public static void main(String[] args) throws Exception {
        String className = args[0];
        Class c = Class.forName(className);
        Method[] methods = c.getDeclaredMethods();
        String newClassName = nameOnly(c.getName()) + "Component";
        String superclassName = "ResourceComponent";

        System.out.println("package net.unicon.resource;");
        System.out.println("");
        System.out.println("public class " + newClassName + " extends " + superclassName + " implements " + c.getName() + " {");
        System.out.println("\tpublic " + newClassName + "(ResourceThing parent, " + c.getName() + " rawResourceComponent) {");
        System.out.println("\t\tsuper(parent, rawResourceComponent);");
        System.out.println("\t}");
        System.out.println("");

        System.out.println("\tprivate " + c.getName() + " myRawResourceComponent() {\n\t\treturn (" + c.getName() + ") rawResourceComponent;\n\t}\n");

        System.out.println("\tvoid closeRawResource() {\n\t\ttry {\n\t\t} catch (ThreadDeath t) {\n\t\t\tif (t instanceof Throwable) {\n\t\t\t\tthrow (ThreadDeath) t;\n\t\t\t}\n\t\t}\n\t}\n");

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int modifiers = method.getModifiers();
            modifiers = modifiers & ~Modifier.ABSTRACT;
            Class returnType = method.getReturnType();
            String name = method.getName();
            Class[] parameterTypes = method.getParameterTypes();
            Class[] exceptions = method.getExceptionTypes();

            System.out.print("\t" + Modifier.toString(modifiers) + " " + typeName(returnType) + " " + name + "(");
            for (int j = 0; j < parameterTypes.length; j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print(typeName(parameterTypes[j]) + " parameter" + j);
            }

            System.out.print(")");
            if (exceptions != null && exceptions.length > 0) {
                System.out.print(" throws ");
                for (int j = 0; j < exceptions.length; j++) {
                    if (j > 0) {
                        System.out.print(", ");
                    }
                    System.out.print(exceptions[j].getName());
                }
            }

            System.out.println(" {");
            System.out.println("\t\tcheckActive();");
            System.out.print("\t\t");
            if (notVoid(returnType)) {
                System.out.print(typeName(returnType) + " answer = ");
            }
            System.out.print("myRawResourceComponent()." + name + "(");
            for (int j = 0; j < parameterTypes.length; j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print("parameter" + j);
            }
            System.out.println(");");
            if (notVoid(returnType)) {
                System.out.println("\t\treturn answer;");
            }
            System.out.println("\t}");
            System.out.println();
        }
   
        System.out.println("}");
    }

    protected static boolean notVoid(Class type) {
        return type != null && !type.getName().equals("void");
    }

    protected static String nameOnly(String qualifiedClassName) {
        int index = qualifiedClassName.lastIndexOf(".");
        return qualifiedClassName.substring(index + 1);
    }

    protected static String typeName(Class type) {
        if (notVoid(type)) {
            if (type.isArray()) {
                return typeName(type.getComponentType()) + "[]";
            } else {
                return type.getName();
            }
        } else {
            return "void";
        }
    }
}
