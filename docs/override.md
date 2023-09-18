![Logo](images/sweden-connect.png)

# Making Overrides and Customizations to the Application

---

It is very likely that you need to change parts of the application before you deploy your own
BankID IdP. Changes may be [changing the BankID IdP UI](#changing-the-bankid-idp-ui) or
[extending the BankID backend application](#extending-the-bankid-backend-application) with new,
or changed, features.

<a name="changing-the-bankid-idp-ui"></a>

## Changing the BankID IdP UI

### CSS, message and content Overrides

Add a directory for the overrides in `bankid-idp/bankid-idp-frontend/pom.xml` like this:

```yaml
bankid:
  override:
    directory-path: '/my/path/to/overrides'
```

(Example files are available in `bankid-idp/bankid-idp-backend/src/main/resources/local/overrides`)

#### CSS Overrides

Put your custom CSS in a `.css` file in the override directory specified above.

Most things like colors and borders can be overridden by using the CSS variables found in [main.css](https://github.com/swedenconnect/bankid-saml-idp/blob/main/bankid-idp/bankid-idp-frontend/src/assets/main.css):

```CSS
:root {
  --bg-color: #222;
  --fg-color: #bababa;
}
```

To override any CSS without the need for `!important` attributes, start your selectors with `#App`:

```CSS
#app .copyright {
  font-size: 14px;
}
```

#### Message Overrides

Put your custom messages in a `.messages` file in the override directory specified above.

To override an already existing text in the product, look in messages.js and use the path to the message you want to change.

For example, to override the copyright message in the footer:

```JSON
[
  {
    "sv": "Copyright © Tjänsten tillhandahålls av XYZ-myndigheten",
    "en": "Copyright © Service provided by XYZ-myndigheten",
    "code": "bankid.msg.copyright"
  }
]
```

You can also add your own messages for use in the content overrides described below:

```JSON
[
  {
    "sv": "Vi kommer aldrig att ringa dig",
    "en": "We will never call you",
    "code": "my.nocall.title"
  },
  {
    "sv": "Om du har blivit uppgrind av någon som ber dig logga in så lägg på",
    "en": "If you have been called by soneone who asks you to log in, hang up",
    "code": "my.nocall.text"
  }
]
```

#### Content Overrides

Put your custom content in a `.content` file in the override directory specified above.

Custom content are alert boxes that can either be of type `INFO` or type `WARNING`. They can be put in these positions:

- `ABOVE`, above the main content box on all screens
- `BELOW`, below the main content box on all screens
- `DEVICESELECT`, in the top of the main content box on the device select screen
- `QRCODE`, in the top of the main content box on the "Other device" screen, showing the QR code
- `AUTOSTART`, in the top of the main content box on the "This device" screen, trying to autostart the BankID app

To show an informational alert box on top of the main content using the custom messages we added in the last section, do this:

```JSON
[
    {
        "title":"my.nocall.title",
        "text":"my.nocall.text",
        "position":"ABOVE",
        "type":"INFO"
    }
]
```

If the codes used in `title` and `text` don't match any custom or built-in message, the codes will be shown as is. This can be used if you don't need any translations.

### HTML Extensions

> TODO

### Replacement of the Entire Frontend Application

> TODO

<a name="extending-the-bankid-backend-application"></a>

## Extending the BankID Backend Application

The easiest way to create your own Spring Boot backend with added features is to include the
backend JAR as a dependency to your Spring Boot project. Simply add the following dependency
in your POM:

```

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

### Creating a new application entry point

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
    } catch (Exception e) {
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
