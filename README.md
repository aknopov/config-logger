# SpringBoot Configuration Logger

Java library to log the actual configuration of a Spring Boot application.

## Overview

Ever wondered what actual configuration values are used in your Spring Boot application?
This library logs all configuration values whether they are hardcoded, set in application configuration,
taken from Java properties, or environment variables.

## Usage

1. Define the following variables in application configuration, environment, or properties:

    ```properties
    config.logging.package = com.example
    config.logging.prefix = "Whatever you like"
    ```
1. Make SpringBoot to load the library bean. For example, with `@ComponentScan`:

    ```java
    @SpringBootApplication
    @Import(ConfigurationLogger.class)
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
   ```  
... and see the log:
```dtd 
2025-01-23 17:10:18,810 [main] INFO  logger="c.a.c.ConfigurationLogger", msg="Whatever you like: <unkn> = EXTRA2"
2025-01-23 17:10:18,811 [main] INFO  logger="c.a.c.ConfigurationLogger", msg="Whatever you like: APP_VERSION = 7.7.7"
2025-01-23 17:10:18,811 [main] INFO  logger="c.a.c.ConfigurationLogger", msg="Whatever you like: EXTRA = "
```

### Limitations
1. The library supports only constructor parameters with `@Value` annotation.
2. The library assumes that annotations have `${name:default}`, `${name}` or `constant` format. SpEL expressions are not supported.
In the first two cases, if variable  is not set, the log contains "N/A" value. In the last case "<unkn>" is used as parameter name since it is undefined.