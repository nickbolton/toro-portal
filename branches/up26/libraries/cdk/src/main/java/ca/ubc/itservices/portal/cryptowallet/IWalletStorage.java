// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   IWalletStorage.java

package ca.ubc.itservices.portal.cryptowallet;


// Referenced classes of package ca.ubc.itservices.portal.cryptowallet:
//            WalletException, Wallet

public interface IWalletStorage
{

    public abstract void init();

    public abstract Wallet retrieve(byte abyte0[], byte abyte1[]);

    public abstract void store(byte abyte0[], byte abyte1[], Wallet wallet)
        throws WalletException;

    public abstract void change(byte abyte0[], byte abyte1[], byte abyte2[], Wallet wallet)
        throws WalletException;
}
