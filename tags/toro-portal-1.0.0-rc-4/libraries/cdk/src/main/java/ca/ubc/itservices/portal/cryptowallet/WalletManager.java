// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WalletManager.java

package ca.ubc.itservices.portal.cryptowallet;

import java.io.File;
import java.io.PrintStream;
import org.apache.log4j.PropertyConfigurator;

// Referenced classes of package ca.ubc.itservices.portal.cryptowallet:
//            IWalletStorage, WalletException, PropertyManager, Wallet

public class WalletManager
{

    protected WalletManager()
    {
    }

    public static synchronized void init()
    {
        if(!initialized)
        {
            try
            {
                Class storageClass = Class.forName(PropertyManager.getProperty("storage.class"));
                storage = (IWalletStorage)storageClass.newInstance();
                storage.init();
            }
            catch(Exception e)
            {
                System.err.println("Storage class " + PropertyManager.getProperty("storage.class") + " exception: " + e);
            }
            String logProps = PropertyManager.getProperty("log4j.properties");
            if(logProps != null)
                PropertyConfigurator.configure((new File(logProps)).toString());
            initialized = true;
        }
    }

    public static Wallet getWallet(byte uid[], byte password[])
    {
        return storage.retrieve(uid, password);
    }

    public static void putWallet(byte uid[], byte password[], Wallet wallet)
        throws WalletException
    {
        storage.store(uid, password, wallet);
    }

    public static void changeWallet(byte uid[], byte oldpassword[], byte newpassword[], Wallet wallet)
        throws WalletException
    {
        storage.change(uid, oldpassword, newpassword, wallet);
    }

    static IWalletStorage storage = null;
    static boolean initialized = false;

}
