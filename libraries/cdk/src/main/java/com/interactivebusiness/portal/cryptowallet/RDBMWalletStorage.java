// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RDBMWalletStorage.java

package com.interactivebusiness.portal.cryptowallet;

import ca.ubc.itservices.portal.cryptowallet.*;
import java.io.ByteArrayInputStream;
import java.sql.*;
import org.apache.log4j.Category;

public class RDBMWalletStorage
    implements IWalletStorage
{

    public RDBMWalletStorage()
    {
    }

    public void init()
    {
        try
        {
            jdbcDriver = PropertyManager.getProperty("storage.jdbcDriver");
            jdbcURL = PropertyManager.getProperty("storage.jdbcUrl");
            userName = PropertyManager.getProperty("storage.jdbcUser");
            password = PropertyManager.getProperty("storage.jdbcPassword");
            Class.forName(jdbcDriver);
        }
        catch(ClassNotFoundException cnfe)
        {
            log.error("RDBMWalletStorage::init(); ClassNotFoundException: " + cnfe);
        }
    }

    private static Connection getConnection()
    {
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(jdbcURL, userName, password);
        }
        catch(Exception e)
        {
            log.error("RDBMWalletStorage::getConnection(); Exception: " + e);
        }
        return conn;
    }

    public Wallet retrieve(byte userid[], byte password[])
    {
        Wallet wallet = null;
        byte data[] = null;
        data = getDataBytes(new String(userid));
        if(data == null)
            try
            {
                wallet = new Wallet(userid, password, null);
            }
            catch(WalletException we)
            {
                log.error("RDBMWalletStorage::retrieve(); Problem creating wallet for userID: " + new String(userid) + " with WalletException: " + we);
                return null;
            }
        else
            try
            {
                wallet = new Wallet(userid, password, data);
            }
            catch(WalletException we)
            {
                log.error("RDBMWalletStorage::retrieve(); Problem creating wallet for userID: " + new String(userid) + " with WalletException: " + we);
                return null;
            }
        return wallet;
    }

    private byte[] getDataBytes(String userID)
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try
        {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT WALLET FROM CRYPTO_WALLET WHERE USER_ID=?");
            stmt.setString(1, userID);
            rs = stmt.executeQuery();
            if(rs != null && rs.next())
            {
                byte dataBytes[] = rs.getBytes(1);
                rs.close();
                rs = null;
                byte abyte0[] = dataBytes;
                return abyte0;
            }
        }
        catch(SQLException sqe)
        {
            log.error("RDBMWalletStorage::getDataBytes(); SQLException: " + sqe);
            byte abyte1[] = null;
            return abyte1;
        }
        finally
        {
            if(rs != null)
                try
                {
                    rs.close();
                }
                catch(SQLException sqlexception) { }
            if(stmt != null)
                try
                {
                    stmt.close();
                }
                catch(SQLException sqlexception1) { }
        }
        return null;
    }

    private void setDataBytes(String userID, byte dataBytes[])
        throws SQLException, Exception
    {
        PreparedStatement stmt = null;
        Connection conn = null;
        try
        {
            conn = getConnection();
            stmt = conn.prepareStatement("DELETE FROM CRYPTO_WALLET WHERE USER_ID = ?");
            stmt.setString(1, userID);
            stmt.executeUpdate();
            stmt = conn.prepareStatement("INSERT INTO CRYPTO_WALLET VALUES (?, ?)");
            stmt.setString(1, userID);
            ByteArrayInputStream bis = new ByteArrayInputStream(dataBytes);
            stmt.setBinaryStream(2, bis, dataBytes.length);
            stmt.executeUpdate();
        }
        catch(SQLException sqe)
        {
            log.error("RDBMWalletStorage::setDataBytes(); SQLException: " + sqe);
            throw sqe;
        }
        catch(Exception e)
        {
            log.error("RDBMWalletStorage::setDataBytes(); Exception: " + e);
            throw e;
        }
        finally
        {
            if(stmt != null)
                try
                {
                    stmt.close();
                }
                catch(SQLException sqlexception) { }
        }
    }

    public void store(byte userid[], byte password[], Wallet wallet)
        throws WalletException
    {
        byte data[] = null;
        try
        {
            Wallet oldWallet = retrieve(userid, password);
            if(oldWallet != null && !oldWallet.verifyPassword(password))
            {
                log.error("RDBMWalletStorage::store(); Credentials have changed, must recreate wallet\n");
                throw new WalletCredentialsException("Invalid credentials");
            }
            data = wallet.getEncryptedData();
            setDataBytes(new String(userid), data);
        }
        catch(SQLException sqe)
        {
            log.error("RDBMWalletStorage::store(); SQLException: " + sqe);
        }
        catch(Exception e)
        {
            log.error("RDBMWalletStorage::store(); Exception: " + e);
        }
    }

    public void change(byte abyte0[], byte abyte1[], byte abyte2[], Wallet wallet)
        throws WalletException
    {
    }

    private static Category log = Category.getInstance("RDBMWalletStorage");
    private static String jdbcDriver;
    private static String jdbcURL;
    private static String userName;
    private static String password;

}
