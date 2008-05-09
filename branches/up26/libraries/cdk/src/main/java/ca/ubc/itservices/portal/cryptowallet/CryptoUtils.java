// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CryptoUtils.java

package ca.ubc.itservices.portal.cryptowallet;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.log4j.Category;

// Referenced classes of package ca.ubc.itservices.portal.cryptowallet:
//            WalletCredentialsException, WalletEncryptionException, WalletFormatException, WalletException, 
//            PropertyManager

public class CryptoUtils
{

    public CryptoUtils()
    {
        encryptionAlgorithm = PropertyManager.getProperty("crypto.encryption.algorithm");
        hashAlgorithm = PropertyManager.getProperty("crypto.hash.algorithm");
    }

    public Object decrypt(SecretKey key, BigInteger input)
        throws WalletException
    {
        SealedObject sealedObject = null;
        Object result = null;
        ByteArrayInputStream finalIn = new ByteArrayInputStream(input.toByteArray());
        try
        {
            byte salt[] = new byte[8];
            finalIn.read(salt);
            int iterations = 20;
            java.security.spec.AlgorithmParameterSpec aps = new PBEParameterSpec(salt, iterations);
            Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
            cipher.init(2, key, aps);
            ObjectInputStream in = new ObjectInputStream(finalIn);
            sealedObject = (SealedObject)in.readObject();
            in.close();
            finalIn.close();
            result = sealedObject.getObject(cipher);
        }
        catch(IOException ioe)
        {
            log.error("IOException while trying to decrypt: " + ioe);
            throw new WalletCredentialsException("Invalid credentials or problem reading wallet data");
        }
        catch(NoSuchAlgorithmException nsae)
        {
            log.error("Algorithm " + encryptionAlgorithm + " does not exist: " + nsae);
            throw new WalletEncryptionException("Algorithm " + encryptionAlgorithm + " does not exist");
        }
        catch(NoSuchPaddingException nspe)
        {
            log.error("Padding requested is not available: " + nspe);
            throw new WalletEncryptionException("Padding requested is not available");
        }
        catch(InvalidKeyException ike)
        {
            log.error("Problems with the key given: " + ike);
            throw new WalletEncryptionException("Problems with the key given");
        }
        catch(InvalidAlgorithmParameterException iape)
        {
            log.error("The parameters passed to this algorithm are wrong (is it a PBE type?): " + iape);
            throw new WalletEncryptionException("The parameters passed to this algorithm are wrong (is it a PBE type?)");
        }
        catch(ClassNotFoundException cnfe)
        {
            log.error("Resultant decrypted Object is not available in your CLASSPATH: " + cnfe);
            throw new WalletFormatException("Resultant decrypted Object is not available in your CLASSPATH");
        }
        catch(IllegalBlockSizeException ibse)
        {
            log.error("Illegal block size found while decrypting object: " + ibse);
            throw new WalletEncryptionException("Illegal block size found while decrypting object");
        }
        catch(BadPaddingException bpe)
        {
            log.error("The padding used on the encrypted object is bad: " + bpe);
            throw new WalletEncryptionException("The padding used on the encrypted object is bad");
        }
        catch(Exception e)
        {
            log.error("Unknown exception caught: " + e);
            throw new RuntimeException(e.toString());
        }
        return result;
    }

    public byte[] generateSalt(byte password[])
        throws WalletException
    {
        try
        {
            byte salt[] = new byte[8];
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            md.update(password);
            byte digest[] = md.digest();
            System.arraycopy(digest, 0, salt, 0, 8);
            return salt;
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new WalletException("Problem generating salt due to missing algorithm: " + hashAlgorithm + " exception: " + e);
        }
    }

    public SecretKey generateKey(byte passphrase[])
        throws WalletException
    {
        SecretKey key = null;
        try
        {
            String pphrase = new String(passphrase);
            char pphraseChars[] = pphrase.toCharArray();
            java.security.spec.KeySpec ks = new PBEKeySpec(pphraseChars);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(encryptionAlgorithm);
            key = skf.generateSecret(ks);
        }
        catch(InvalidKeySpecException ikse)
        {
            log.error("Problems generating key specification: " + ikse);
            throw new WalletEncryptionException("Problems generating key specification.");
        }
        catch(NoSuchAlgorithmException nsae)
        {
            log.error("Algorithm " + encryptionAlgorithm + " does not exist: " + nsae);
            throw new WalletEncryptionException("Algorithm " + encryptionAlgorithm + " does not exist");
        }
        return key;
    }

    public BigInteger encrypt(SecretKey key, byte salt[], Object target)
        throws WalletException
    {
        if(target == null)
            throw new NullPointerException("Target for encryption cannot be null.");
        ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
        BigInteger result = null;
        try
        {
            java.security.spec.AlgorithmParameterSpec aps = new PBEParameterSpec(salt, 20);
            Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
            cipher.init(1, key, aps);
            finalOut.write(salt);
            ObjectOutputStream out = new ObjectOutputStream(finalOut);
            SealedObject sealedTarget = new SealedObject((Serializable)target, cipher);
            out.writeObject(sealedTarget);
            result = new BigInteger(finalOut.toByteArray());
            out.close();
            finalOut.close();
        }
        catch(IOException ioe)
        {
            log.error("IOException while trying to encrypt: " + ioe);
            throw new WalletCredentialsException("Invalid credentials or problem reading wallet data");
        }
        catch(NoSuchAlgorithmException nsae)
        {
            log.error("Algorithm " + encryptionAlgorithm + " does not exist: " + nsae);
            throw new WalletEncryptionException("Algorithm " + encryptionAlgorithm + " does not exist");
        }
        catch(NoSuchPaddingException nspe)
        {
            log.error("Padding requested is not available: " + nspe);
            throw new WalletEncryptionException("Padding requested is not available");
        }
        catch(InvalidKeyException ike)
        {
            log.error("Problems with the key given: " + ike);
            throw new WalletEncryptionException("Problems with the key given");
        }
        catch(InvalidAlgorithmParameterException iape)
        {
            log.error("The parameters passed to this algorithm are wrong (is it a PBE type?): " + iape);
            throw new WalletEncryptionException("The parameters passed to this algorithm are wrong (is it a PBE type?)");
        }
        catch(IllegalBlockSizeException ibse)
        {
            log.error("Illegal block size found while encrypting object: " + ibse);
            throw new WalletEncryptionException("Illegal block size found while encrypting object");
        }
        catch(Exception e)
        {
            log.error("Unknown exception caught: " + e);
            throw new RuntimeException(e.toString());
        }
        return result;
    }

    public BigInteger generateHash(Object target)
    {
        byte objBytes[] = null;
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(bout);
            objOut.writeObject(target);
            objBytes = bout.toByteArray();
            bout.close();
            objOut.close();
            bout = null;
            objOut = null;
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            md.update(objBytes);
            objBytes = md.digest();
        }
        catch(IOException ioe)
        {
            log.error("Problem converting object to bytes: " + ioe);
        }
        catch(NoSuchAlgorithmException nsae)
        {
            log.error("The algorithm " + encryptionAlgorithm + " is not available: " + nsae);
        }
        return new BigInteger(objBytes);
    }

    private static final int SALT_ITERATIONS = 20;
    private static final int SALT_LENGTH = 8;
    private static Category log = Category.getInstance("CryptoUtils");
    private String encryptionAlgorithm;
    private String hashAlgorithm;

    static 
    {
        try
        {
            Class providerClass = Class.forName(PropertyManager.getProperty("crypto.provider"));
            Security.addProvider((Provider)providerClass.newInstance());
        }
        catch(Exception e)
        {
            System.err.println("Cryptography Provider " + PropertyManager.getProperty("crypto.provider") + " exception: " + e);
        }
    }
}
