// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FileWalletStorage.java

package ca.ubc.itservices.portal.cryptowallet;

import java.io.*;
import org.apache.log4j.Category;

// Referenced classes of package ca.ubc.itservices.portal.cryptowallet:
//            Wallet, WalletException, WalletCredentialsException, IWalletStorage, 
//            PropertyManager

public class FileWalletStorage
    implements IWalletStorage
{

    public FileWalletStorage()
    {
        location = null;
    }

    public void init()
    {
        String storage_location = PropertyManager.getProperty("storage.location");
        if(storage_location == null)
        {
            System.err.println("WARNING!  The storage_location property was not set.");
            return;
        }
        location = new File(storage_location);
        if(!location.exists())
            System.err.println("WARNING! The location " + location + " does not exists, or has " + "the wrong permissions.  Cannot use it.");
    }

    public Wallet retrieve(byte userid[], byte password[])
    {
        Wallet wallet = null;
        byte data[] = null;
        byte buffer[] = new byte[1024];
        int nread = 0;
        File walletFile = new File(location, new String(userid));
        FileInputStream fin = null;
        if(!walletFile.exists())
        {
            try
            {
                wallet = new Wallet(userid, password, null);
            }
            catch(WalletException e)
            {
                log.error("Error making empty wallet: " + e);
            }
            return wallet;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try
        {
            fin = new FileInputStream(walletFile);
        }
        catch(Exception e)
        {
            log.error("Problem reading file: " + walletFile + ": " + e);
            return null;
        }
        try
        {
            while((nread = fin.read(buffer)) > 0) 
                bout.write(buffer, 0, nread);
        }
        catch(IOException ioe)
        {
            log.error("Problem reading wallet file: " + walletFile + " with error: " + ioe);
        }
        data = bout.toByteArray();
        try
        {
            wallet = new Wallet(userid, password, data);
        }
        catch(WalletException e)
        {
            log.error("Problem creating wallet: " + walletFile + " with errror: " + e);
        }
        try
        {
            fin.close();
            bout.close();
        }
        catch(IOException ioe)
        {
            log.error("Problem closing wallet file: " + walletFile + " with error: " + ioe);
        }
        return wallet;
    }

    public void store(byte userid[], byte password[], Wallet wallet)
        throws WalletException
    {
        FileOutputStream fout = null;
        byte data[] = null;
        Wallet oldWallet = null;
        File walletFile = new File(location, new String(userid));
        if(walletFile.exists())
        {
            oldWallet = retrieve(userid, password);
            if(!oldWallet.verifyPassword(password))
            {
                log.error("Password given for user " + userid + " does not match wallet on disk.");
                throw new WalletCredentialsException("Invalid credentials");
            }
        }
        data = wallet.getEncryptedData();
        try
        {
            walletFile.delete();
            walletFile.createNewFile();
            fout = new FileOutputStream(walletFile);
            fout.write(data);
            fout.close();
        }
        catch(IOException e)
        {
            log.error("Problem writing to wallet file: " + walletFile + " with error: " + e);
        }
    }

    public void change(byte userid[], byte oldpassword[], byte newpassword[], Wallet newWallet)
        throws WalletException
    {
        FileOutputStream fout = null;
        byte data[] = null;
        Wallet oldWallet = null;
        File walletFile = new File(location, new String(userid));
        oldWallet = retrieve(userid, oldpassword);
        if(!oldWallet.verifyPassword(oldpassword))
        {
            log.error("Password given for user " + userid + " does not match wallet on disk.");
            throw new WalletCredentialsException("Invalid credentials for the previous wallet");
        }
        data = newWallet.getEncryptedData();
        try
        {
            walletFile.delete();
            walletFile.createNewFile();
            fout = new FileOutputStream(walletFile);
            fout.write(data);
            fout.close();
        }
        catch(IOException e)
        {
            log.error("Problem writing to wallet file: " + walletFile + " with error: " + e);
        }
    }

    private File location;
    private static Category log = Category.getInstance("FileWalletStorage");

}
