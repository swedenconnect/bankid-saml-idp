# Configuring session / lock management

## Module Selection

### Redis (recommended)

We have configured a customizer that extends the spring redis configuration to simplify TLS configuration

Application.yml
```yaml
session:
  module: redis # Select module
spring:
  redis:
    host: host for redis instance
    port: port for redis instance
    password: password for redis instance
    ssl: true 
    tls: # Configuration Extension
      p12KeyStorePath: path for keystore
      p12KeyStorePassword: password for keystore
      p12TrustStorePath: path for truststore
      p12TrustStorePassword: password for truststore
      enableHostnameVerification: true # If you want to verify certificate hostname or not
```
### In memory (Not recommended for production)
```yaml
session:
  module: memory
```

### Implement your own module

You can implement your own module, if you want to be able to use something else than redis, e.g. psql, mysql

To implement your own module, please see how we have configured the redis module in RedisSessionConfiguration.
The main takeaways is that you need to have an implementation for the following
- LockRepository
- SessionReader (you can use fallback implementation for spring session, but a direct read/write implementation is recommended)
- SessionWriter (you can use fallback implementation for spring session, but a direct read/write implementation is recommended)
- Spring Session Configuration