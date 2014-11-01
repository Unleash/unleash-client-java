# The Unleash Client 4 Java

[![Build Status](https://travis-ci.org/finn-no/unleash-client-java.svg?branch=master)](https://travis-ci.org/finn-no/unleash-client-java)

## Create a new a Unleash instance

It is really easy to get a new instance of Unleash. In your app you typically just want one instance, 
and inject that where you need it. You will typically use a dependency injection frameworks such as  
Spring or Guice to manage this. 

You create a new instance with the following command:
```java
ToggleRepository repository = new ToggleRepository(URI.create("http://unelash.finn.no"), 10);
Unleash unleash = new Unleash(toggleRepository);
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