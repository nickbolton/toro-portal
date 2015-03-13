# Gateway/SSO Portlet Documentation #

## Introduction ##

While documentation specific to this opensource incarnation of this code is presently sparse, [Unicon](http://www.unicon.net) hosts [a knowledgebase](http://www.unicon.net/support/knowledgebase) including [articles about the Gateway SSO Portlet](http://www.unicon.net/taxonomy/term/40).  The [Academus product documentation](http://www.unicon.net/academus) may be loosely relevant as well.

## Using the Gateway/SSO Portlet in uPortal ##


### Passwords cached at login ###
As its name suggests, The Gateway/SSO Portlet commonly empowers users to access additional web applications without having to re-enter their credentials (username and password).  This behavior is often achieved using the portal credentials and "replaying" them in against another application.

In uPortal, a Portlet may obtain the user's password only if `security.properties` has been configured to use a 'Caching' `ISecurityContext` implementation, such as one of the following:

  * CacheLdapSecurityContext
  * CacheSecurityContext

Once this step has been done, the Portlet will receive the password 'like magic' -- it is not necessary to configure a 'password' attribute in `personDirectory.xml`.  This magic is a feature of the CPortletAdapter IChannel whereby uPortal implements its JSR-168 support.  If no caching security context is in place, then the {password} token in URLs called by the Gateway SSO Portlet, as configured in its XML configuration files, will resolve to _null_.

The most common way Gateway SSO Portlet is used is to replay the credentials presented by the user at login.  While a full discussion of _security.properties_ configuration is beyond the scope of this wee little wiki page, here's an example of a working _security.properties_ that will cache the passwords presented at login, both for uPortal-internal users, and for users in LDAP.

```
# Security Properties
#
# Copyright 2000-2007 the original author or authors.

# Licensed under the New BSD License (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

# http://www.opensource.org/licenses/bsd-license.php

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# At the root is a UnionSecurityContextFactory.  This is to implement
# authenticating the union of users able to authenticate using portal-local
# credentials, and users able to authenticate via LDAP.
#
root=org.jasig.portal.security.provider.UnionSecurityContextFactory

# One "path" within the root UnionSecurityContextFactory is the simple "db"
# path, where usernames and passwords are authenticated against portal-local
# user accounts stored in the uPortal database.  The SimpleSecurityContextFactory
# implements this flavor of authentication
#
root.db=org.jasig.portal.security.provider.SimpleSecurityContextFactory

# Behind the simple database-backed authentication is a cache security context to
# capture and remember the user's password for the duration of the session, so that
# it can be used in such places as the GatewaySSOPortlet.
#
root.db.cache=org.jasig.portal.security.provider.CacheSecurityContextFactory

# Another "path" within the root UnionSecurityContextFactory is an LDAP path.
# This one is named ldap_faculty because it is a particularly configured LDAP security
# context to point at a "Faculty" LDAP connection as specified in ldap.xml, not using the
# default LDAP connection.  This demonstrates that security contexts within a union that are 
# cached can also be particularly configured.
# 
root.ldap_faculty=org.jasig.portal.security.provider.SimpleLdapSecurityContextFactory

# Often this bit of configuration is included later, but Andrew Petro likes to include
# it right close to where it is being used.  This says "There's a security context property
# applicable to the security context factory named "root.ldap_faculty" named "connection" with value
# "Faculty".  Specifically, this tells that instance of SimpleLdapSecurityContextFactory to use
# LDAP connections according to the configuration named "Faculty" in ldap.xml.
securityContextProperty.root.ldap_faculty.connection=Faculty

# This version of root.ldap_faculty will insure that all the successfully validated credentials are cached.
# Use this instead of SimpleLdapSecurityContextFactory if cached LDAP credentials is desired
root.ldap_faculty=org.jasig.portal.security.provider.CacheLdapSecurityContextFactory


# Answers what tokens are examined in the request for each context during authentication.
# A subcontext only needs to set its tokens if it differs from those of the root context.
# Here all the contexts from root on down use "userName" for the principal, and "password"
# for the credential, so they only need to be declared for the root and they're inherited
# throughout the tree.
#
principalToken.root=userName
credentialToken.root=password


# In theory you could have an authorization provider other than the default implementation.
# In practice, this line has to be in security.properties but it's very unlikely that you
# will need to change it.
#
authorizationProvider=org.jasig.portal.security.provider.AuthorizationServiceFactoryImpl
```

And here is a simpler, bare minimum to enable Gateway SSO password forwarding, _security.properties_:

```
# Security Properties
#
# Copyright 2000-2007 the original author or authors.

# Licensed under the New BSD License (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

# http://www.opensource.org/licenses/bsd-license.php

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# At the root is the SimpleSecurityContextFactory.  This is to implement
# authenticating users against portal-internal accounts (usernames and password hashes
# stored in the uPortal database).
#
root=org.jasig.portal.security.provider.SimpleSecurityContextFactory


# Behind the simple database-backed authentication is a cache security context to
# capture and remember the user's password for the duration of the session, so that
# it can be used in such places as the GatewaySSOPortlet.
#
root.cache=org.jasig.portal.security.provider.CacheSecurityContextFactory


# Here all the contexts from root on down use "userName" for the principal, and "password"
# for the credential, so they only need to be declared for the root and they're inherited
# throughout the tree.
#
principalToken.root=userName
credentialToken.root=password

# In theory you could have an authorization provider other than the default implementation.
# In practice, this line has to be in security.properties but it's very unlikely that you
# will need to change it.
#
authorizationProvider=org.jasig.portal.security.provider.AuthorizationServiceFactoryImpl

```

### User-entered Credentials ###

You can still use the user-entered-credentials features of Gateway SSO Portlet without caching the credentials presented at the time of login to the portal.  Under this configuration, the portlet will prompt for username and password and remember this encrypted in the portal database for replay on subsequent logins to achieve authentication into the gatewayed applications.  These credentials can be shared across several instances of the portlet where they are the same across different systems, with the user only having to enter them to one of the portlet instances to achieve use of the credentials in all.

### Using Gateway SSO without passwords ###

You can use the Gateway SSO Portlet without caching any credentials at all if the workflows you are implementing do not involve authentication or if authentication can be transparently achieved by such means of [Central Authentication Service](http://www.ja-sig.org/products/cas/) single sign on or PubCookie single sign on.

### Making the Gateway SSO Portlet aware of the username ###

The Gateway SSO Portlet relies on a user attribute named **user.login.id** to understand the logged in user's username.  This user attribute must be configured in uPortal's Person Directory subsystem so that it exists at all, and it must be provisioned into instances of the Gateway SSO Portlet via the portlet's _portlet.xml_ portlet deployment descriptor.  Typically, the _portlet.xml_ that came with your Gateway SSO Portlet is already configured to do this.  Recent versions of uPortal also have the necessary **user.login.id** person attribute configuration already applied, but slightly older releases did not include this required configuration.

#### For uPortal 2.5.3 (or Earlier) and 2.6.0 RC1 ####

You will need to configure Person Attributes to support the **user.login.id** attribute.  Toro-portlets requires it.

Note that recent versions of uPortal allow configuration of user attributes via either or both of a legacy domain-specific properties file (_PersonDirs.xml_) and a more recent domain-general Spring configuration file (_personDirectory.xml_).  Changes to _PersonDirs.xml_ in uPortal 2.6 will by default have no effect unless _personDirectory.xml_ is edited to enable parsing and using the legacy configuration format.

  * [See here](https://www.ja-sig.org/svn/up2/branches/rel-2-5-3-patches/properties/PersonDirs.xml) for an example in uPortal 2.5.3 or earlier
  * [See here](https://www.ja-sig.org/svn/up2/tags/rel-2-6-1-GA/properties/personDirectory.xml) for an example in uPortal 2.6.0 RC1