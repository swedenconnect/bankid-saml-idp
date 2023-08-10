![Logo](images/sweden-connect.png)

# Monitoring the Application

The BankID IdP application uses the Spring Boot Actuator feature that enables monitoring over HTTP.
See the [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.monitoring) for details.

> TODO: How to configure and default settings

## Health Monitoring

> TODO: Explain about configuration about the Health-endpoint and go through the different components ...

```
{
  "status": "UP",
  "components": {
    "api": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 994662584320,
        "free": 793263566848,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.13"
      }
    },
    "rpCertificate": {
      "status": "UP",
      "details": {
        "test-my-eid": {
          "expirationDate": "2024-08-18T21:59:59.000+00:00",
          "expired": false,
          "expiresSoon": false
        }
      }
    },
    "samlMetadata": {
      "status": "UP",
      "details": {
        "test-my-eid": [
          {
            "entityId": "http://sandbox.swedenconnect.se/testmyeid",
            "metadataPresent": true
          },
          {
            "entityId": "http://sandbox.swedenconnect.se/testmyeid-sign",
            "metadataPresent": true
          }
        ]
      }
    }
  }
}
```

## Metrics Monitoring with Prometheus

> TODO

-----

Copyright &copy; 2023, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).