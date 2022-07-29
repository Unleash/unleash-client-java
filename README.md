# Unleash Client SDK for Java
This is the Unleash Client SDK for Java. It is compatible with the [Unleash-hosted.com SaaS](https://www.unleash-hosted.com/) offering and [Unleash Open-Source](https://github.com/unleash/unleash).


[![Build Status](https://github.com/Unleash/unleash-client-java/workflows/Build/badge.svg)](https://github.com/Unleash/unleash-client-java/actions)
[![Coverage Status](https://coveralls.io/repos/github/Unleash/unleash-client-java/badge.svg?branch=main)](https://coveralls.io/github/Unleash/unleash-client-java?branch=main)
[![Maven Central](https://img.shields.io/maven-central/v/io.getunleash/unleash-client-java)](https://mvnrepository.com/artifact/io.getunleash/unleash-client-java)


## Getting started

This section shows you how to get started quickly and explains some common configuration scenarios. For a full overview of Unleash configuration options, check out [the _Configuration options_ section](#configuration-options).

### Add the Unleash client to your class path

You will require unleash on your class path, pop it in to your pom:

```xml
<dependency>
    <groupId>io.getunleash</groupId>
    <artifactId>unleash-client-java</artifactId>
    <version>Latest version here</version>
</dependency>
```


### Create a new Unleash instance

It is easy to get a new instance of Unleash. In your app you typically *just want one instance of Unleash*, and inject that where you need it. You will typically use a dependency injection frameworks such as Spring or Guice to manage this.

To create a new instance of Unleash you need to pass in a config object:
```java

UnleashConfig config = UnleashConfig.builder()
            .appName("java-test")
            .instanceId("instance x")
            .unleashAPI("http://unleash.herokuapp.com/api/")
            .customHttpHeader("Authorization", "API token")
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

Calling `unleash.isEnabled("AwesomeFeature")` is the equivalent of calling `unleash.isEnabled("AwesomeFeature", false)`.
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

Read more about the strategies in [the activation strategies documentation](https://docs.getunleash.io/user_guide/activation_strategy).

#### Custom strategies
You may also specify and implement your own strategy. The specification must be registered in the Unleash UI and
you must register the strategy implementation when you wire up unleash.

```java
Strategy s1 = new MyAwesomeStrategy();
Strategy s2 = new MySuperAwesomeStrategy();
Unleash unleash return new DefaultUnleash(config, s1, s2);

```

### Unleash context

In order to use some of the common activation strategies you must provide an [Unleash context](https://docs.getunleash.io/user_guide/unleash_context).
This client SDK provides two ways of provide the unleash-context:

#### 1. As part of isEnabled call
This is the simplest and most explicit way of providing the unleash context.
You just add it as an argument to the `isEnabled` call.


```java
UnleashContext context = UnleashContext.builder()
  .userId("user@mail.com").build();

unleash.isEnabled("someToggle", context);
```


#### 2. Via an `UnleashContextProvider`
This is a more advanced approach, where you configure an Unleash context provider.
With a context provider, you don't need to rebuild or pass the Unleash context to every `unleash.isEnabled` call.


The provider typically binds the context to the same thread as the request.
If you use Spring, the `UnleashContextProvider` will typically be a 'request scoped' bean.


```java
UnleashContextProvider contextProvider = new MyAwesomeContextProvider();

UnleashConfig config = new UnleashConfig.Builder()
            .appName("java-test")
            .instanceId("instance x")
            .unleashAPI("http://unleash.herokuapp.com/api/")
            .customHttpHeader("Authorization", "API token")
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

### Dynamic custom HTTP headers
If you need custom http headers that change during the lifetime of the client, a provider can be defined via the `UnleashConfig`.
```java
public class CustomHttpHeadersProviderImpl implements CustomHttpHeadersProvider {
    @Override
    public Map<String, String> getCustomHeaders() {
        String token = "Acquire or refresh token";
        return new HashMap() {{ put("Authorization", "Bearer "+token); }};
    }
}
```
```java
CustomHttpHeadersProvider provider = new CustomHttpHeadersProviderImpl();

UnleashConfig unleashConfig = UnleashConfig.builder()
                .appName("my-app")
                .instanceId("my-instance-1")
                .unleashAPI(unleashAPI)
                .customHttpHeader("Authorization", "API token")
                .customHttpHeadersProvider(provider)
                .build();
```


### Subscriber API
*(Introduced in 3.2.2)*

Sometimes you want to know when Unleash updates internally. This can be achieved by registering a subscriber. An example on how to configure a custom subscriber is shown below. Have a look at [UnleashSubscriber.java](https://github.com/Unleash/unleash-client-java/blob/main/src/main/java/io/getunleash/event/UnleashSubscriber.java) to get a complete overview of all methods you can override.


```java
UnleashConfig unleashConfig = UnleashConfig.builder()
    .appName("my-app")
    .instanceId("my-instance-1")
    .unleashAPI(unleashAPI)
    .customHttpHeader("Authorization", "API token")
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
- **disablePolling** - Stops the client from polling. If used without synchronousFetchOnInitialisation will cause the client to never fetch toggles from the `unleash-api`.
- **fetchTogglesInterval** - Sets the interval (in seconds) between each poll to the `unleash-api`. Set this to `0` to do a single fetch and then stop refreshing while the process lives.
### HTTP Proxy with Authentication
The Unleash Java client uses `HttpURLConnection` as HTTP Client which already recognizes the common JVM proxy settings such as `http.proxyHost` and
`http.proxyPort`. So if you are using a Proxy without authentication, everything works out of the box. However, if you have to use Basic Auth
authentication with your proxy, the related settings such as `http.proxyUser` and `http.proxyPassword` do not get recognized by default. In order
to enable support for basic auth against a http proxy, you can simply enable the following option on the configuration builder:

```java
UnleashConfig config = UnleashConfig.builder()
    .appName("my-app")
    .unleashAPI("http://unleash.org")
    .customHttpHeader("Authorization", "API token")
    .enableProxyAuthenticationByJvmProperties()
    .build();
```

### Toggle fetcher
The Unleash Java client now supports using your own toggle fetcher.
The Config builder has been expanded to accept a `io.getunleash.util.UnleashFeatureFetcherFactory` which should be a `Function<UnleashConfig, FeatureFetcher>`.
If you want to use OkHttp instead of HttpURLConnection you'll need a dependency on okhttp

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.9+</version>
</dependency>
```

Then you can change your config to
```java
UnleashConfig config = UnleashConfig.builder()
    .appName("my-app")
    .unleashAPI("http://unleash.org")
    .customHttpHeader("Authorization", "API token")
    .unleashFeatureFetcherFactory(OkHttpFeatureFetcher::new)
    .build();
```

This will then start using OkHttp instead of HttpURLConnection.

## Local backup
By default unleash-client fetches the feature toggles from unleash-server every 10s, and stores the
result in `unleash-repo.json` which is located in the `java.io.tmpdir` directory. This means that if
the unleash-server becomes unavailable, the unleash-client will still be able to toggle the features
based on the values stored in `unleash-repo.json`. As a result of this, the second argument of
`isEnabled` will be returned in two cases:

* When `unleash-repo.json` does not exists
* When the named feature toggle does not exist in `unleash-repo.json`

## Bootstrapping
* Unleash supports bootstrapping from a JSON string.
* Configure your own custom provider implementing the `ToggleBootstrapProvider` interface's single method `String read()`.
  This should return a JSON string in the same format returned from `/api/client/features`
* Example bootstrap files can be found in the json files located in [src/test/resources](src/test/resources)
* Our assumption is this can be use for applications deployed to ephemeral containers or more locked down file systems where Unleash's need to write the backup file is not desirable or possible.

### Provided Bootstrappers
#### ToggleBootstrapFileProvider
* Unleash comes configured with a `ToggleBootstrapFileProvider` which implements the `ToggleBootstrapProvider` interface.
* It is the default implementation used if not overridden via the `setToggleBootstrapProvider` on UnleashConfig.
##### Configure ToggleBootstrapFileProvider
* The `ToggleBootstrapFileProvider` reads the file located at the path defined by the `UNLEASH_BOOTSTRAP_FILE` environment variable.
* It supports both `classpath:` paths and absolute file paths.

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

Se more in [FakeUnleashTest.java](https://github.com/Unleash/unleash-client-java/blob/main/src/test/java/io/getunleash/FakeUnleashTest.java)

## Development

Build:
```bash
mvn clean install
```

Jacoco coverage reports:
```bash
mvn jacoco:report
```
The generated report will be available at ```target/site/jacoco/index.html```

## Formatting

* We're using AOSP Java code style, enforced by [spotless](https://github.com/diffplug/spotless).
  For formatting, use `mvn spotless:apply` and spotless will do the job for you
* It should apply spotless automatically upon compilation for you, but only on code that is changed since origin/main
* For Intelij you can use https://plugins.jetbrains.com/plugin/8527-google-java-format
* For VSCode you can use https://marketplace.visualstudio.com/items?itemName=wx-chevalier.google-java-format
* For VIM there's https://github.com/google/vim-codefmt

## Releasing

### Deployment

 - You'll need an account with Sonatype's JIRA - https://issues.sonatype.org
 - In addition your account needs access to publish under io.getunleash

### GPG signing

 - You'll need gpg installed and a configured gpg key for signing the artifacts

#### Example settings.xml

 - In ~/.m2/settings.xml put
```xml
<settings>
    ...
    <profiles>
        ...
        <profile>
            <id>ossrh</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <gpg.executable>gpg</gpg.executable> <!-- Where to find gpg -->
                <gpg.passphrase>[PASSPHRASE FOR YOUR GPG KEY]</gpg.passphrase>
            </properties>
        </profile>
    </profiles>
    ...
    <servers>
        ...
        <server>
            <id>sonatype-nexus-snapshots</id>
            <username>[YOUR_SONATYPE_JIRA_USERNAME]</username>
            <password>[YOUR_SONATYPE_JIRA_PASSWORD]</password>
        </server>
        <server>
            <id>ossrh</id>
            <username>[YOUR_SONATYPE_JIRA_USERNAME]</username>
            <password>[YOUR_SONATYPE_JIRA_PASSWORD]</password>
        </server>
        <server>
            <id>sonatype-nexus-staging</id>
            <username>[YOUR_SONATYPE_JIRA_USERNAME]</username>
            <password>[YOUR_SONATYPE_JIRA_PASSWORD]</password>
        </server>
    </servers>
</settings>
```

#### More information

- https://central.sonatype.org/pages/ossrh-guide.html

## Configuration options

The `UnleashConfigBuilder` class (created via `UnleashConfig.builder()`) exposes a set of builder methods to configure your Unleash client. The available options are listed below with a description of what they do. For the full signatures, take a look at the [`UnleashConfig` class definition](src/main/java/io/getunleash/util/UnleashConfig.java).


| Method name                                | Description                                                                                                                                                                                                                                      | Required | Default value                                                                                                        |
|--------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|----------------------------------------------------------------------------------------------------------------------|
| `appName`                                  | The name of the application as shown in the Unleash UI. Registered applications are listed on the Applications page.                                                                                                                             | Yes      | `null`                                                                                                               |
| `backupFile`                               | The path to the file where [local backups](#local-backup) get stored.                                                                                                                                                                            | No       | Synthesized from your system's `java.io.tmpdir` and your `appName`: `"<java.io.tmpdir>/unleash-<appName>-repo.json"` |
| `customHttpHeader`                         | Add a [custom HTTP header](#custom-http-headers) to the list of HTTP headers that will the client sends to the Unleash API. Each method call will add a new header. Note: in most cases, you'll need to use this method to provide an API token. | No       | N/A                                                                                                                  |
| `customHttpHeadersProvider`                | Add a custom HTTP header provider. Useful for [dynamic custom HTTP headers](#dynamic-custom-http-headers).                                                                                                                                       | No       | `null`                                                                                                               |
| `disablePolling`                           | A boolean indicating whether the client should poll the unleash api for updates to toggles                                                                                                                                                       |
| `disableMetrics`                           | A boolean indicating whether the client should disable sending usage metrics to the Unleash server.                                                                                                                                              | No       | `false`                                                                                                              |
| `enableProxyAuthenticationByJvmProperties` | Enable support for [using JVM properties for HTTP proxy authentication](#http-proxy-with-authentication).                                                                                                                                        | No       | `false`                                                                                                              |
| `environment`                              | The name of the current environment.                                                                                                                                                                                                             | No       | `null`                                                                                                               |
| `fallbackStrategy`                         | A strategy implementation that the client can use if it doesn't recognize the strategy type returned from the server.                                                                                                                            | No       | `null`                                                                                                               |
| `fetchTogglesInterval`                     | How often (in seconds) the client should check for toggle updates. Set to `0` if you want to only check once                                                                                                                                     | No       | `10`                                                                                                                 |
| `instanceId`                               | A unique(-ish) identifier for your instance. Typically a hostname, pod id or something similar. Unleash uses this to separate metrics from the client SDKs with the same `appName`.                                                              | Yes      | `null`                                                                                                               |
| `namePrefix`                               | If provided, the client will only fetch toggles whose name starts with the provided value.                                                                                                                                                       | No       | `null`                                                                                                               |
| `projectName`                              | If provided, the client will only fetch toggles from the specified project. (This can also be achieved with an API token).                                                                                                                       | No       | `null`                                                                                                               |
| `proxy`                                    | A `Proxy` object. Use this to configure a third-party proxy that sits between your client and the Unleash server.                                                                                                                                | No       | `null`                                                                                                               |
| `scheduledExecutor`                        | A custom executor to control timing and running of tasks (such as fetching toggles, sending metrics).                                                                                                                                            | No       | [`UnleashScheduledExecutorImpl`](src/main/java/io/getunleash/util/UnleashScheduledExecutorImpl.java)                 |
| `sendMetricsInterval`                      | How often (in seconds) the client should send metrics to the Unleash server. Ignored if you disable metrics with the `disableMetrics` method.                                                                                                    | No       | `60`                                                                                                                 |
| `subscriber`                               | [Register a subscriber to Unleash client events](#subscriber-api).                                                                                                                                                                               | No       | `null`                                                                                                               |
| `synchronousFetchOnInitialisation`         | Whether the client should fetch toggle configuration synchronously (in a blocking manner).                                                                                                                                                       | No       | `false`                                                                                                              |
| `toggleBootstrapProvider`                  | Add a [bootstrap provider](#bootstrapping) (must implement the `ToggleBootstrapProvider` interface)                                                                                                                                              | No       |                                                                                                                      |
| `unleashAPI`                               | The URL of the Unleash API.                                                                                                                                                                                                                      | Yes      | `null`                                                                                                               |
| `unleashContextProvider`                   | An [Unleash context provider used to configure Unleash](#2-via-an-unleashcontextprovider).                                                                                                                                                       | No       | `null`                                                                                                               |

When you have set all the desired options, initialize the configuration with the `build` method.
You can then pass the configuration to the Unleash client constructor.
As an example:

```java

UnleashConfig config = UnleashConfig.builder()
            .appName("your app name")
            .instanceId("instance id")
            .unleashAPI("http://unleash.herokuapp.com/api/")
            .customHttpHeader("Authorization", "API token")
            // ... more configuration options
            .build();

Unleash unleash = new DefaultUnleash(config);
```
