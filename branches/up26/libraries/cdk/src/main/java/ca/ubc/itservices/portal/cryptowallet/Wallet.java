// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Wallet.java

package ca.ubc.itservices.portal.cryptowallet;

import java.math.BigInteger;
import java.util.*;
import javax.crypto.SecretKey;
import org.apache.log4j.Category;

// Referenced classes of package ca.ubc.itservices.portal.cryptowallet:
//            CryptoUtils, WalletException

public class Wallet
{

    public Wallet(byte userid[], byte password[], byte data[])
        throws WalletException
    {
        dataMap = null;
        this.userid = null;
        pbeKey = null;
        pbeSalt = null;
        crypto = new CryptoUtils();
        pbeKey = crypto.generateKey(password);
        pbeSalt = crypto.generateSalt(password);
        this.userid = userid;
        if(data == null || data.length <= 0)
        {
            dataMap = new HashMap();
            return;
        } else
        {
            updateEncryptedData(data);
            verifyPassword(password);
            return;
        }
    }

    public byte[] getEncryptedData()
        throws WalletException
    {
        byte data[] = null;
        BigInteger result = crypto.encrypt(pbeKey, pbeSalt, dataMap);
        data = result.toByteArray();
        return data;
    }

    public boolean containsKey(Object key)
        throws WalletException
    {
        if(key == null)
        {
            throw new NullPointerException("Key cannot be null.");
        } else
        {
            BigInteger hashedKey = crypto.generateHash(key);
            return dataMap.containsKey(hashedKey);
        }
    }

    public boolean isEmpty()
    {
        return dataMap.isEmpty();
    }

    public Object get(Object key)
        throws WalletException
    {
        if(key == null)
            throw new NullPointerException("Key cannot be null.");
        BigInteger hashKey = crypto.generateHash(key);
        BigInteger encryptedValue = (BigInteger)dataMap.get(hashKey);
        if(encryptedValue == null)
        {
            return null;
        } else
        {
            Object decryptedValue = crypto.decrypt(pbeKey, encryptedValue);
            return decryptedValue;
        }
    }

    public void put(Object key, Object value)
        throws WalletException
    {
        if(key == null)
            throw new NullPointerException("The key cannot be null");
        if(value == null)
        {
            throw new NullPointerException("The value cannot be null");
        } else
        {
            BigInteger encryptedValue = crypto.encrypt(pbeKey, pbeSalt, value);
            BigInteger hashKey = crypto.generateHash(key);
            dataMap.put(hashKey, encryptedValue);
            return;
        }
    }

    public void remove(Object key)
        throws WalletException
    {
        if(key == null)
        {
            throw new NullPointerException("The key value cannot be null");
        } else
        {
            BigInteger hashKey = crypto.generateHash(key);
            dataMap.remove(hashKey);
            return;
        }
    }

    public void putAll(Map from)
        throws WalletException
    {
        if(from == null)
            throw new NullPointerException("The from Map cannot be null.");
        if(from.size() <= 0)
            return;
        Object key;
        Object value;
        for(Iterator i = from.keySet().iterator(); i.hasNext(); put(key, value))
        {
            key = i.next();
            value = from.get(key);
        }

    }

    public int size()
    {
        if(dataMap == null)
            return 0;
        else
            return dataMap.size();
    }

    public Wallet changePassword(byte newpassword[])
        throws WalletException
    {
        Wallet newWallet = new Wallet(userid, newpassword, null);
        Set keys = dataMap.keySet();
        for(Iterator i = keys.iterator(); i.hasNext();)
        {
            Object key = i.next();
            if((key instanceof String) && "password_hash".equals((String)key))
            {
                newWallet.dataMap.put("password_hash", crypto.generateHash(new BigInteger(newpassword)));
            } else
            {
                BigInteger value = (BigInteger)dataMap.get(key);
                Object newValue = crypto.decrypt(pbeKey, value);
                SecretKey newPbeKey = crypto.generateKey(newpassword);
                byte newPbeSalt[] = crypto.generateSalt(newpassword);
                newValue = crypto.encrypt(newPbeKey, newPbeSalt, newValue);
                newWallet.dataMap.put(key, newValue);
            }
        }

        return newWallet;
    }

    private void updateEncryptedData(byte encryptedData[])
        throws WalletException
    {
        BigInteger bin = new BigInteger(encryptedData);
        dataMap = (Map)crypto.decrypt(pbeKey, bin);
    }

    public boolean verifyPassword(byte password[])
        throws WalletException
    {
        BigInteger pwInteger = new BigInteger(password);
        BigInteger testHash = crypto.generateHash(pwInteger);
        BigInteger originalHash = (BigInteger)dataMap.get("password_hash");
        if(originalHash == null)
        {
            log.warn("Wallet has no password_hash value in it.  Creating.");
            dataMap.put("password_hash", testHash);
            return true;
        }
        return testHash.equals(originalHash);
    }

    private Map dataMap;
    private byte userid[];
    private SecretKey pbeKey;
    private byte pbeSalt[];
    private static Category log = Category.getInstance("Wallet");
    private CryptoUtils crypto;

}
