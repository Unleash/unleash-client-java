# The Unleash Client 4 Java

[![Build Status](https://travis-ci.org/finn-no/unleash-client-java.svg?branch=master)](https://travis-ci.org/finn-no/unleash-client-java) [![Coverage Status](https://coveralls.io/repos/finn-no/unleash-client-java/badge.png?branch=master)](https://coveralls.io/r/finn-no/unleash-client-java?branch=master)

## Create a new a Unleash instance

It is really easy to get a new instance of Unleash. In your app you typically just want one instance, 
and inject that where you need it. You will typically use a dependency injection frameworks such as  
Spring or Guice to manage this. 

You create a new instance with the following command:
```java
URI unleashServer = URI.create("http://unelash.finn.no")
Unleash unleash = new DefaultUnleash(unleashServer);
```

## Awesome feature toggle API

It is really simple to use unleash.

```java
if(unleash.isEnabled("AwesomeFeature")) {
  //do some magic
} else {
  //do old boring stuff
}
```

I need a trueish default value in case the feature toggle is not defined, or the unleash-server is unavailable:
```java
unleash.isEnabled("AwesomeFeature", true)
```

## Development

Build:
```bash
mvn clean install
```

Cobertura coverage reports:
```bash
mvn cobertura:cobertura -DcoberturaFormat=html
```
The generated report will be available at ```target/site/cobertura/index.html```
