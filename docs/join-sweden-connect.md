![Logo](images/sweden-connect.png)

# Adding Your BankID IdP to Sweden Connect

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

-----

## About

This page will discuss the steps, both technical and administrative, that your organization will
need to go through in order to add your BankID IdP to the Sweden Connect SAML federation.

> If you plan to add your IdP to another federation, this resource may still be useful to get
an understanding of SAML federations.

Note: The information on this page refers that you want to add the official Sweden Connect
federation (QA and production). There is also a sandbox-federation that you can add without
any contracts being signed or audits being made, see [https://sandbox.swedenconnect.se](https://sandbox.swedenconnect.se/home/).

We recommend that you start your journey towards Sweden Connect by using the Sandbox environment.

## Joining Sweden Connect

In order to add your IdP to Sweden Connect your organization needs to sign the [Sweden Connect
agreement(s)](https://www.digg.se/digitala-tjanster/e-legitimering/e-legitimering-for-dig-som-offentlig-aktor). 

> See [https://www.swedenconnect.se/anslut](https://www.swedenconnect.se/anslut).

A Swedish public organization that has added its service provider(s) to the Sweden Connect
federation in order to obtain support for authentication for eIDAS and Swedish eID will also have 
the right to add its own Identity Provider. This Identity Provider will be seen as belonging to
the organization, and should not serve all service providers of the federation.

Freja eID plus has an IdP in the Sweden Connect federation that all service providers
that have signed the [Valfrihetssystem 2017](https://www.digg.se/digitala-tjanster/avtal/avtal-valfrihetssystem-2017/regelverk-valfrihetssystem-2017-e-legitimering) agreement can make use of.
However, BankID, does not provide a general SAML IdP within the federation, so the compromise
will be that each service provider that wish to use BankID within the Sweden Connect-context can
add a BankID IdP under its own name/organization. This IdP will then be used by the organization's
service providers.

Note: It is perfectly legal for an organization to add a BankID IdP under its own
Sweden Connect-contract and let other service providers make use of the IdP. However, Digg, that
is the federation operator, has no responsibility regarding these authentications. It will be
an issue between the IdP operator and the service providers.

## The LoA 3 Issue

BankID as a eID provider is certified according to Level of Assurance 3 (LoA 3/tillitsnivå 3),
but that certification applies only to the actual issuance of eID:s. 

This means that unless the actual BankID IdP has been certified according to LoA 3 the 
"uncertified loa3" URI should be used in assertions issued by your BankID IdP. 

In order to issue LoA 3 assertions your IdP has to be audited and certified according to LoA 3.
Read more about how to request that your IdP is LoA 3-audited at: [https://www.digg.se/digitala-tjanster/e-legitimering/e-legitimering-for-dig-som-leverantor/idp-leverantor](https://www.digg.se/digitala-tjanster/e-legitimering/e-legitimering-for-dig-som-leverantor/idp-leverantor). 

## Configuring the BankID IdP for Sweden Connect

The [Configuration of the BankID SAML IdP](configuration.html) page is a complete reference
of how the BankID IdP should be configured. This section contains additional information to
know about when adding your BankID IdP to Sweden Connect Metadata.

The IdP must declare a [Service Entity Category](https://docs.swedenconnect.se/technical-framework/latest/06_-_Entity_Categories_for_the_Swedish_eID_Framework.html#service-contract-categories) in its metadata. This construct provides a SAML metadata mechanism to connect service providers with the IdP:s that
they are allowed to communicate with. The mechanism will also be essential if a central discovery
service is set up within the federation.

For example, the Freja eID plus IdP declares the `http://id.swedenconnect.se/contract/sc/eid-choice-2017` entity category in its metadata, and all SP:s that have signed the [Valfrihetssystem 2017](https://www.digg.se/digitala-tjanster/avtal/avtal-valfrihetssystem-2017/regelverk-valfrihetssystem-2017-e-legitimering) does the same in their metadata (this is checked by the federation operator when metadata
is added).

**Example:**

Suppose that your organization is registered as "Example Organization" within the Sweden Connect
federation and that you have bilateral agreements (i.e., outside of regular Sweden Connect-agreements)
with the organizations "Acme" and "FooBar" to use your IdP for BankID authentication. Then, you
declare the service entity catgegory `http://id.swedenconnect.se/contract/example-org/bankid` and
add it to your IdP metadata. Next, the service providers, your own and those from "Acme" and "FooBar",
add the same entity category to their SAML SP metadata.

This will result in that we now can see by looking at the aggregated SAML metadata which SP:s that
are using your SAML BankID IdP.

## Auditing of Your BankID IdP

Before any SAML IdP can be added to the Sweden Connect federation (QA or production) it must be 
audited with respect to fulfilment of the 
[Sweden Connect Specifications](https://docs.swedenconnect.se/technical-framework/).

Send a mail to [operations@swedenconnect.se](mailto:operations@swedenconnect.se) and request that your IdP is audited by Digg.

> Note: If you have built your BankID IdP using the [https://github.com/swedenconnect/bankid-saml-idp](https://github.com/swedenconnect/bankid-saml-idp) repository and has not made any significant
changes to the SAML-support in the IdP, this will be a very quick and easy auditing.

## Publishing SAML Metadata 

When your development is done, testing the the [sandbox environment](https://sandbox.swedenconnect.se/home/) has been successful, all contracts have been signed and auditing has been made you can now
publish your IdP to the Sweden Connect QA federation, and later also to production.

Extract your SAML metadata from your IdP. By default settings it will be available at:

```
https://<your-domain/bankid/idp/saml2/metadata
```

Save this file and mail it to [operations@swedenconnect.se](mailto:operations@swedenconnect.se) and
request that it is added to QA/production. The operations team will make the necessary checks concerning
that all the necessary contracts/agreements are in place, that you are eligible to 
upload metadata for your organization and finally that the SAML metadata itself is correct.

After that your SAML metadata will be published to the Sweden Connect federation metadata and your
SAML Service Providers can send authentication requests to your IdP.

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
