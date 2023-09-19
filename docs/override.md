![Logo](images/sweden-connect.png)

# Making Overrides and Customizations to the Application

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

> Example files are available in the [overrides](https://github.com/swedenconnect/bankid-saml-idp/tree/main/bankid-idp/bankid-idp-backend/src/main/resources/local/overrides) directory.

<a name="css-overrides"></a>
#### CSS Overrides

Put your custom CSS in a `.css` file in the override directory specified above.

Most things like colours and borders can be overridden by using the CSS variables found in [main.css](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-frontend/src/assets/main.css):

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

To override an already existing text, look in [messages.js](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-frontend/src/locale/messages.js) and use the path to the message you want
to change.

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
        "title":"my.nocall.title",
        "text":"my.nocall.text",
        "position":"ABOVE",
        "type":"INFO"
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

You can also do the same for [CssOverride](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/overrides/CssOverride.java) and
for [ContentOverride](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-backend/src/main/java/se/swedenconnect/bankid/idp/authn/api/overrides/ContentOverride.java).


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
      <artifactId>bankid-idp-backend</artifactId>
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
@SpringBootApplication(
        exclude = {RedissonAutoConfiguration.class, RedisAutoConfiguration.class},
        scanBasePackageClasses = {
                com.test.Application.class,
                se.swedenconnect.bankid.idp.IdpApplication.class
        }
)
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

#### Example Repository

> TODO Create Example Repository

### Configure Maven Plugin

Also configure the Spring Boot Maven plugin so that the new backend application's main class
is found:

```
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

---

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
