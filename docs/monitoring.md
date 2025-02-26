![Logo](images/sweden-connect.png)

# Monitoring the BankID IdP Application

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The BankID IdP application uses the Spring Boot Actuator feature that enables monitoring over HTTP.
See the [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.monitoring) for details.

<a name="health-monitoring"></a>
## Health Monitoring

The `health`-endpoint of the BankID IdP delivers information about a number of sub-components of the
IdP. If all sub-components reports the status "UP", the overall status is "UP" and everything is fine.
However, if the status is something else, the monitoring team should act. 

> **Note:** See the [Management and Supervision](configuration.html#management-and-supervision)
section on the configuration page for how to configure the health-endpoint to fit your needs.

### Health Indicators

Below follows a listing of all health indicators and the type of errors or warnings that can be
reported.

#### Diskspace

The `diskSpace`-indicator monitors the available diskspace and reports errors if not enough diskspace
remains.

#### Ping

A silly indicator. The `ping`-indicator always reports "UP".

#### Redis

If Redis is being used, the `redis`-indicator checks that the Redis server is up and running.

#### Relying Party Certificate

The `rpCertificate`-indicator monitors all the BankID Relying Party-certificates and reports an
error if any of them has expired. If a certificate is about to expire (based on the `bankid.health.rp-certificate-warn-threshold` setting) a warning will be reported.

The operations team should act on warnings and order, and install, new RP certificates in good time
before they expire.

#### SAML Metadata

The `samlMetadata`-indicator ensures that the IdP has access to valid SAML-metadata for all
configured Relying Parties. If the SAML metadata for a configured RP is not available, "DOWN"
will be reported.

The operations team should act on this, and contact the SAML federation operator or the
Relying Party's support.


### Example

Below follows an example result of a call to the `health`-endpoint (`curl -k https://<domain>:8444/actuator/health | jq`):

```json
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

<a name="metrics-monitoring-with-prometheus"></a>
## Metrics Monitoring with Prometheus

The BankID IdP supports metrics monitoring with Prometheus. Read more about this feature
at [https://www.callicoder.com/spring-boot-actuator-metrics-monitoring-dashboard-prometheus-grafana/](https://www.callicoder.com/spring-boot-actuator-metrics-monitoring-dashboard-prometheus-grafana/).

-----

Copyright &copy; 2023-2025, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
