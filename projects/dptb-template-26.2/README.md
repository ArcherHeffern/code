# dptb

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

## Inner workings
* run / route tracking uses Game Messages
* Players online, money, and session tracking uses the Scoreboard
* Session Tracking also uses lifecycle hooks
* While Context recognises AFK periods, the Database ends sessions when AFK, and starts them when no longer AFK