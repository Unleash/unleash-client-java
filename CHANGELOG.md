# Changelog

## 3.0.0 (unreleased)
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

