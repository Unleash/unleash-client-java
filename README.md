# The Unleash Client 4 Java

[![Build Status](https://travis-ci.org/Unleash/unleash-client-java.svg?branch=master)](https://travis-ci.org/Unleash/unleash-client-java)
[![Coverage Status](https://coveralls.io/repos/github/Unleash/unleash-client-java/badge.svg?branch=master)](https://coveralls.io/github/Unleash/unleash-client-java?branch=master)

This is the java client for Unleash. Read more about the [Unleash project](https://github.com/finn-no/unleash)
**Version 3.x of the client requires `unleash-server` >= v3.x**

## Getting started
You will require unleash on your class path, pop it in to your pom:

```xml
<dependency>
    <groupId>no.finn.unleash</groupId>
    <artifactId>unleash-client-java</artifactId>
    <version>Latest version here</version>
</dependency>
```


### Create a new Unleash instance

It is easy to get a new instance of Unleash. In your app you typically *just want one instance of Unelash*, and inject that where you need it. You will typically use a dependency injection frameworks such as Spring or Guice to manage this. 

To create a new instance of Unleash you need to pass in a config object:
```java

UnleashConfig config = UnleashConfig.builder()
            .appName("java-test")
            .instanceId("instance x")
            .unleashAPI("http://unleash.herokuapp.com/api/")
            .build();

Unleash unleash = new DefaultUnleash(config);
```

### Awesome feature toggle API

It is really simple to use unleash.

```java
if(unleash.isEnabled("AwesomeFeature")) {
  //do some magic
} else {
  //do old boring stuff
}
```

Calling `unleash.isEnabled("AwesomeFeature")` is the equvivalent of calling `unleash.isEnabled("AwesomeFeature", false)`. 
Which means that it will return `false` if it cannot find the named toggle. 

If you want it to default to `true` instead, you can pass `true` as the second argument:

```java
unleash.isEnabled("AwesomeFeature", true)
```

### Activation strategies

The Java client comes with implementations for the built-in activation strategies 
provided by unleash. 

- DefaultStrategy
- UserWithIdStrategy
- GradualRolloutRandomStrategy
- GradualRolloutUserWithIdStrategy
- GradualRolloutSessionIdStrategy
- RemoteAddressStrategy
- ApplicationHostnameStrategy

Read more about the strategies in [activation-strategy.md](https://github.com/Unleash/unleash/blob/master/docs/activation-strategies.md).

#### Custom strategies
You may also specify and implement your own strategy. The specification must be registered in the Unleash UI and 
you must register the strategy implementation when you wire up unleash. 

```java
Strategy s1 = new MyAwesomeStrategy();
Strategy s2 = new MySuperAwesomeStrategy();
Unleash unleash return new DefaultUnleash(config, s1, s2);

```

### Unleash context

In order to use some of the common activation strategies you must provide a [unleash-context](https://github.com/Unleash/unleash/blob/master/docs/unleash-context.md).
This client SDK provides two ways of provide the unleash-context:

#### 1. As part of isEnabled call
This is the simplest and most explicit way of providing the unleash context. 
You just add it as an argument to the `isEnabled` call. 


```java
UnleashContext context = UnleashContext.builder()
  .userId("user@mail.com").build();

unleash.isEnabled("someToggle", unleashContext);
``` 


#### 2. Via a UnleashContextProvider
This is a bit more advanced approach, where you configure a unleash-context provider. 
By doing this you do not have rebuild or pass the unleash-context object to every 
place you are calling `unleash.isEnabled`. 

The provider typically binds the context to the same thread as the request. 
If you are using Spring the `UnleashContextProvider` will typically be a 
'request scoped' bean. 


```java
UnleashContextProvider contextProvider = new MyAwesomeContextProvider();

UnleashConfig config = new UnleashConfig.Builder()
            .appName("java-test")
            .instanceId("instance x")
            .unleashAPI("http://unleash.herokuapp.com/api/")
            .unleashContextProvider(contextProvider)
            .build();

Unleash unleash = new DefaultUnleash(config);

// Anywhere in the code unleash will get the unleash context from your registered provider. 
unleash.isEnabled("someToggle");
``` 

### Custom HTTP headers
If you want the client to send custom HTTP Headers with all requests to the Unleash API 
you can define that by setting them via the `UnleashConfig`. 

```java
UnleashConfig unleashConfig = UnleashConfig.builder()
                .appName("my-app")
                .instanceId("my-instance-1")
                .unleashAPI(unleashAPI)
                .customHttpHeader("Authorization", "12312Random")
                .build();
```

### Subscriber API
*(Introduced in 3.2.2)*

Sometimes you want to know when Unleash updates internally. This can be achieved by registering a subscriber. An example on how to configure a custom subscriber is shown below. Have a look at [UnleashSubscriber.java](https://github.com/Unleash/unleash-client-java/blob/master/src/main/java/no/finn/unleash/event/UnleashSubscriber.java) to get a complete overview of all methods you can override.


```java
UnleashConfig unleashConfig = UnleashConfig.builder()
    .appName("my-app")
    .instanceId("my-instance-1")
    .unleashAPI(unleashAPI)
    .subscriber(new UnleashSubscriber() {
        @Override
        public void onReady(UnleashReady ready) {
            System.out.println("Unleash is ready");
        }
        @Override
        public void togglesFetched(FeatureToggleResponse toggleResponse) {
            System.out.println("Fetch toggles with status: " + toggleResponse.getStatus());
        }

        @Override
        public void togglesBackedUp(ToggleCollection toggleCollection) {
            System.out.println("Backup stored.");
        }

    })
    .build();
```

### Options 

- **appName** - Required. Should be a unique name identifying the client application using Unleash. 
- **synchronousFetchOnInitialisation** - Allows the user to specify that the unleash-client should do one synchronous fetch to the `unleash-api` at initialisation. This will slow down the initialisation (the client must wait for a http response). If the `unleash-api` is unavailable the client will silently move on and assume the api will be available later. 


## Local backup
By default unleash-client fetches the feature toggles from unleash-server every 10s, and stores the 
result in `unleash-repo.json` which is located in the `java.io.tmpdir` directory. This means that if 
the unleash-server becomes unavailable, the unleash-client will still be able to toggle the features 
based on the values stored in `unleash-repo.json`. As a result of this, the second argument of 
`isEnabled` will be returned in two cases:

* When `unleash-repo.json` does not exists
* When the named feature toggle does not exist in `unleash-repo.json`


## Unit testing
You might want to control the state of the toggles during unit-testing.
Unleash do come with a ```FakeUnleash``` implementation for doing this. 

Some examples on how to use it below:


```java
// example 1: everything on
FakeUnleash fakeUnleash = new FakeUnleash();
fakeUnleash.enableAll();

assertThat(fakeUnleash.isEnabled("unknown"), is(true));
assertThat(fakeUnleash.isEnabled("unknown2"), is(true));

// example 2
FakeUnleash fakeUnleash = new FakeUnleash();
fakeUnleash.enable("t1", "t2");

assertThat(fakeUnleash.isEnabled("t1"), is(true));
assertThat(fakeUnleash.isEnabled("t2"), is(true));
assertThat(fakeUnleash.isEnabled("unknown"), is(false));

// example 3: variants
FakeUnleash fakeUnleash = new FakeUnleash();
fakeUnleash.enable("t1", "t2");
fakeUnleash.setVariant("t1", new Variant("a", (String) null, true));

assertThat(fakeUnleash.getVariant("t1").getName(), is("a"));
```

Se more in [FakeUnleashTest.java](https://github.com/Unleash/unleash-client-java/blob/master/src/test/java/no/finn/unleash/FakeUnleashTest.java)

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
