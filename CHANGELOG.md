# Changelog

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
- Client metrics. Gives details about what toggles a specific client application uses, how many times a toggle was evaluated to true / false. Everything presented in the UI. 
- Client registration. This gives insight about connected clients, instances, strategies they support. 
- Client Application overview. Based on metrics and client registrations.



## 1.0.0 (January 2014)
- Initial public release

