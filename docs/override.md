![Logo](images/sweden-connect.png)

# Making Overrides and Customizations to the Application

-----

It is very likely that you need to change parts of the application before you deploy your own
BankID IdP. Changes may be [changing the BankID IdP UI](#changing-the-bankid-idp-ui) or
[extending the BankID backend application](#extending-the-bankid-backend-application) with new, 
or changed, features.

<a name="changing-the-bankid-idp-ui"></a>
## Changing the BankID IdP UI

### CSS Overrides

> TODO

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

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
