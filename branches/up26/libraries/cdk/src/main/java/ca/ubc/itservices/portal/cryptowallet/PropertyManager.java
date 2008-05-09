// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PropertyManager.java

package ca.ubc.itservices.portal.cryptowallet;

import java.io.IOException;
import java.util.Properties;
import org.jasig.portal.utils.ResourceLoader;

public class PropertyManager
{

    public PropertyManager()
    {
    }

    public static String getProperty(String name)
    {
        return props.getProperty(name);
    }

    static Class _mthclass$(String x0)
    {
        try
        {
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x1)
        {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private static Properties props;
    public static final String PROPS_FILE = "/properties/cryptowallet.properties";

    static 
    {
        props = null;
        props = new Properties();
        java.io.InputStream propsIn = null;
        try
        {
            propsIn = ResourceLoader.getResourceAsStream(ca.ubc.itservices.portal.cryptowallet.PropertyManager.class, "/properties/cryptowallet.properties");
            props.load(propsIn);
        }
        catch(IOException e)
        {
            props.setProperty("error", "Error loading the cryptowallet.properties file: " + e);
        }
        catch(NullPointerException nullpointerexception)
        {
            props.setProperty("error", "NullPointer with propsIn being " + propsIn);
        }
        catch(ExceptionInInitializerError exceptionininitializererror)
        {
            props.setProperty("error", "ExceptionInInitializeError with propsIn being " + propsIn);
        }
        catch(Exception exception)
        {
            props.setProperty("error", "Exception with propsIn being " + propsIn);
        }
    }
}
