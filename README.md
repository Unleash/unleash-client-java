# The Unleash Client 4 Java

[![Build Status](https://travis-ci.org/Unleash/unleash-client-java.svg?branch=master)](https://travis-ci.org/Unleash/unleash-client-java)
[![Coverage Status](https://coveralls.io/repos/github/Unleash/unleash-client-java/badge.svg?branch=master)](https://coveralls.io/github/Unleash/unleash-client-java?branch=master)

## Create a new a Unleash instance

It is really easy to get a new instance of Unleash. In your app you typically just want one instance, 
and inject that where you need it. You will typically use a dependency injection frameworks such as  
Spring or Guice to manage this. 

You create a new instance with the following command:
```java
URI unleashServer = URI.create("http://unleash.herokuapp.com/features")
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

Calling `unleash.isEnabled("AwesomeFeature")` is the equvivalent of calling `unleash.isEnabled("AwesomeFeature", false)`. Which means that it will return `false` if it cannot find the named toggle. 

If you want it to default to true, you can pass `true` as the second argument:
```java
unleash.isEnabled("AwesomeFeature", true)
```

By default unleash-client fetches the feature toggles from unleash-server every 10s, and stores the result in `unleash-repo.json` which is located in the `java.io.tmpdir` directory. This means that if the unleash-server becomes unavailable, the unleash-client will still be able to toggle the features based on the values stored in `unleash-repo.json`. As a result of this, the second argument of `isEnabled` will be returned in two cases:
* When `unleash-repo.json` does not exists
* When the named feature toggle does not exist in `unleash-repo.json`

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
