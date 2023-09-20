![Logo](images/sweden-connect.png)

# Making Overrides and Customizations to the Application

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

It is very likely that you need to change parts of the application before you deploy your own
BankID IdP. Changes may be [Customizing the BankID IdP UI](#customizing-the-bankid-idp-ui) or
[extending the BankID backend application](#extending-the-bankid-backend-application) with new,
or changed, features.

<a name="customizing-the-bankid-idp-ui"></a>
## Customizing the BankID IdP UI

### CSS, Message and Content Overrides

Enable customization of the BankID's UI by creating override files (see below), placing them in a
directory, and enter this directory in the BankID configuration as:

```yaml
bankid:
  ...
  ui: 
    override:
      directory-path: "/my/path/to/overrides"
```

> Example files are available in the [overrides](https://github.com/swedenconnect/bankid-saml-idp/tree/main/bankid-idp/env/local/overrides) directory.

<a name="css-overrides"></a>
#### CSS Overrides

Put your custom CSS in a `.css` file in the override directory specified above.

Most things like colours and borders can be overridden by using the CSS variables found in [main.css](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/frontend/src/assets/main.css):

```css
:root {
  --bg-color: #222;
  --fg-color: #bababa;
}
```

To override any CSS without the need for `!important` attributes, start your selectors with `#app`:

```css
#app .copyright {
  font-size: 14px;
}
```

<a name="message-overrides"></a>
#### Message Overrides

Put your custom messages in a `.messages` file in the override directory specified above.

To override an already existing text, look in [messages.js](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/frontend/src/locale/messages.js) and use the path to the message you want to change.

For example, to override the copyright message in the footer:

```json
[
  {
    "code": "bankid.msg.copyright",
    "text": {
      "sv": "Copyright © Tjänsten tillhandahålls av XYZ-myndigheten",
      "en": "Copyright © Service provided by the XYZ authority"
    }
  }
]
```

You can also add your own messages for use in the content overrides described below:

```json
[
  {
    "code": "my.nocall.title",
    "text": {
      "sv": "Vi kommer aldrig att ringa dig",
      "en": "We will never call you"    
    }    
  },
  {
    "code": "my.nocall.text",
    "text": {
      "sv": "Om du har blivit uppringd av någon som ber dig logga in så lägg på!",
      "en": "If you have been called by someone who asks you to log in, hang up!"    
    }    
  }
]
```

<a name="content-overrides"></a>
#### Content Overrides

Put your custom content in a `.content` file in the override directory specified above.

Custom content may be alert boxes that can either be of type `INFO` or type `WARNING`. They can be put in these positions:

- `ABOVE` - above the main content box on all screens
- `BELOW` - below the main content box on all screens
- `DEVICESELECT` - in the top of the main content box on the device select screen
- `QRCODE` - in the top of the main content box on the "Other device" screen, showing the QR code
- `AUTOSTART` - in the top of the main content box on the "This device" screen, trying to autostart the BankID app

To show an informational alert box on top of the main content using the custom messages we added in the last section, do this:

```json
[
  {
    "title": "my.nocall.title",
    "content": [
      {
        "text": "my.nocall.text"
      }
    ],
    "position": "ABOVE",
    "type": "INFO"
  }
]
```

You can also include links in the alert boxes
```json
[
  {
    "title": "custom.devel-version.title",
    "content": [
      {
        "text": "custom.devel-version.text"
      },
      {
        "text": "custom.devel.link.text",
        "link": "https://www.bankid.com/utvecklare/test/skaffa-testbankid/test-bankid-get"
      }
    ],
    "position": "ABOVE",
    "type": "INFO"
  }
]
```

If the codes used in `title` and `text` do not match any custom or built-in message, the codes will be shown as is. This can be used if you don't need any translations.

#### Adding Overrides Programatically

If you are [Extending the BankID Backend Application](#extending-the-bankid-backend-application) you also
have the possibility to define any number of beans for overriding the UI.

For example, to programatically change a message:

```java

@Bean
Supplier<MessageOverride> copyrightMessage() {
  return () -> new MessageOverride("bankid.msg.copyright", Map.of(
      "sv", "Copyright © Tjänsten tillhandahålls av XYZ-myndigheten",
      "en", "Copyright © Service provided by the XYZ authority"));
}
```

You can also do the same for [CssOverride](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/authn/api/overrides/CssOverride.java) and
for [ContentOverride](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/authn/api/overrides/ContentOverride.java).


### HTML Extensions

Currently, the above described mechanisms are the ways you can customize the BankID UI without re-building
the front-end code. 

### Replacement of the Entire Frontend Application

Of course, you will always have the possibility to form the BankID SAML IdP repository and make any changes
to the front-end, but for this you are a bit on your own ...

> Well, not entirely. We also supply documentation for the [BankID IdP Back-end API](idp-api.html).

Also, see [External Front-end](https://github.com/swedenconnect/bankid-saml-idp/tree/main/samples/external-frontend) for a sample how to externalize the front-end application.

<a name="extending-the-bankid-backend-application"></a>
## Extending the BankID Backend Application

The easiest way to create your own Spring Boot backend with added features is to include the
backend JAR as a dependency to your Spring Boot project. Simply add the following dependency
in your POM:

```xml
...
<dependencies>
...

  <dependency>
      <groupId>se.swedenconnect.bankid</groupId>
      <artifactId>bankid-idp</artifactId>
      <version>...</version>
    </dependency>

</dependencies>
...
```

### Creating a new Application Entry Point

To enable scanning of beans for the original application and your custom beans we need to define a new application entry point

#### Example

In this example we will define a new Spring boot application in the package `com.test`

```java
package com.test;

@EnableConfigurationProperties
@SpringBootApplication(exclude = {RedissonAutoConfiguration.class, RedisAutoConfiguration.class},
    scanBasePackageClasses = { com.test.Application.class, 
                               se.swedenconnect.bankid.idp.IdpApplication.class })
public class Application {

  public static void main(String[] args) {
    try {
      OpenSAMLInitializer.getInstance()
          .initialize(
             new OpenSAMLSecurityDefaultsConfig(new SwedishEidSecurityConfiguration()),
             new OpenSAMLSecurityExtensionConfig());
    } 
    catch (final Exception e) {
      throw new RuntimeException(e);
    }
    SpringApplication.run(Application.class, args);
  }
}

```

### Configure Maven Plugin

Also configure the Spring Boot Maven plugin so that the new backend application's main class
is found:

```xml
<build>
  <plugins>

    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <version>${spring.boot.version}</version>
      <executions>
        <execution>
          <id>repackage</id>
          <goals>
            <goal>repackage</goal>
          </goals>
          <configuration>
            <mainClass>com.test.Application</mainClass>
          </configuration>
        </execution>
        <execution>
          <goals>
            <goal>build-info</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
    ....

```

<a name="writing-your-own-session-handling-module"></a>
### Writing Your Own Session Handling Module

The BankID IdP offers two session handling modules: `memory` and `redis`. By extending the
BankID IdP Backend application you can write your own module, for example session handling using 
MySQL.

To implement your own module, study how we have configured the Redis module in the
[RedisSessionConfiguration](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/config/session/RedisSessionConfiguration.java) class.

You need to supply implementations for the following interfaces:

- [TryLockRepository](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/concurrency/TryLockRepository.java) - Locking repository responsible for providing locks by key.

- [SessionDao](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/authn/session/SessionDao.java) - Interface defining session reader and writer. You can use [ServletSessionDao](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/src/main/java/se/swedenconnect/bankid/idp/authn/session/ServletSessionDao.java) and use 
[Spring Session](https://spring.io/projects/spring-session), but a direct read/write implementation is
recommended.

- [MessageReplayChecker](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/replay/MessageReplayChecker.java) - A message replay checker
that is used to protect from replay attacks against the SAML IdP. It is recommended to extend the
[AbstractMessageReplayChecker](https://github.com/swedenconnect/saml-identity-provider/blob/main/saml-identity-provider/src/main/java/se/swedenconnect/spring/saml/idp/authnrequest/validation/AbstractMessageReplayChecker.java) class.

Furthermore, the [Spring Security SAML Identity Provider](https://github.com/swedenconnect/saml-identity-provider) library, on which the BankID IdP is built, writes and reads session data using the Servlet API
(i.e., gets the `HttpSession` from the `HttpServletRequest`). Therefore, you need to use/configure [Spring Session](https://spring.io/projects/spring-session) using a Spring Session module corresponding to your
choice of implementation (for example Spring Session JDBC).

Finally, when you have written your session module, you need to activate it. This is done by
assigning the configuration setting `bankid.session.module` to the name of your module (for example `mysql`).

---

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
