# Introduction #

The Gateway SSO Portlet can itself manage, encypt, and store credentials for use in its replay feature.  The module for accomplishing this is called SsoMultiAuthentication.


# How does SsoMultiAuthentication encrypt the stored credentials? #

When credentials are stored directly into the Gateway SSO Portlet using SsoMultiAuthentication, the portlet uses an EncryptionService utility from a library called "Alchemist" (one of the modules included in Toro, all open source software under the GPL). The EncyptionService uses the DES encyption algorithmm (technically configured as "DES/ECB/PKCS5Padding") with a key configured in encyption.xml, a file which must be readable by the Java process running uPortal and which should not be readable by other users. That key is required to be at least 8 characters in length. Only the first eight characters of the key are meaningful. The plaintext encryption key input to DES is transformed to a 64 bit long DES key, of which only 56 are effectively available as one bit per byte is used for parity. These encrypted passwords are then stored into a table in the portal database. (Citation: [article discussing Java's encryption APIs and DES in particular](http://www.informit.com/articles/article.aspx?p=170967&seqNum=4)).