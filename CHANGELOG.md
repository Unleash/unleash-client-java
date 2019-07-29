# Changelog

## 3.2.8
- feat: Add support for static context fields (#82)

## 3.2.7
- fix: Bump log4j to version 2.11.2.

## 3.2.6
- fix: Add TypeAdapter for AtomicLong to not break metrics for users with old gson on classpath

## 3.2.5
- fix: Make metric counting fully thread-safe

## 3.2.4
- fix: MetricsBucket MUST be threadsafe.
- fix: Make sure that etag field is never null

## 3.2.3
- fix: Only set etag if it is not empty. Sending empty `if-none-match` seem to cause issues with AWS load balancer.

## 3.2.2
- feat: Added subscriber API

## 3.2.1
- fix: Stop logging 304-response as warn

## 3.2.0
- feat: Implement support for variants
- fix: instanceId cannot be null.

## 3.1.2
- LogManager.getLogger() not supported on jdk11

## 3.1.1
- Add option `synchronousFetchOnInitialisation` to force an inital api-update on init.

## 3.1.0
- Expose list of feature names
- Introduced UNAVAILABLE as possible status in FeatureToggleResponse

## 3.0.0
- This version requires `unleash-server` v3 or higher. 
- Switch hashing to MurmurHash (https://github.com/Unleash/unleash/issues/247)
- Update API endoint paths for Unleash 3.x (https://github.com/Unleash/unleash-client-java/issues/40)

## 2.1.3
- Add sdkVersion in client-register call

## 2.1.3 Expose Feature Toggle Definition
- Exposing the Feature Toggle Definition via the `getFeatureToggleDefinition` method on DefaultUnleash to make it easier to extend it with new functionality. 

## 2.1.2 
- Added options for defining custom headers. 

## 2.1.1 (March 2017)
- Default instanceId should include hostname. 

## 2.1.0 (Febrary 2017)
- Includes implementation of pre-defined activation strategies. 
    - applicationHostname
    - gradualRolloutRandom
    - gradualRolloutSessionId
    - gradualRolloutUserId
    - remoteAddress
    - userWithId
- Implements support for unleash-context to simplify usage of strategies.

## 2.0.0 (January 2017)

- Support multiple strategies. This makes it easy to use multiple activation strategies in combination.
- Client metrics. Gives details about what toggles a specific client application uses, how many times a toggle was evaluated to true / false.
- Client registration. This gives insight about connected clients, instances, strategies they support. 



## 1.0.0 (January 2014)
- Initial public release

