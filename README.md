# Aniki Bot

A Discord Bot to posts random NSFW images from various sources and based on tags. Sources include:

- [anime.r34.world](https://anime.r34.world)
- [gelbooru.com](https://gelbooru.com)
- [r-34.xyz](https://r-34.xyz)
- [rule34.xxx](https://rule34.xxx)

## Installation

Use the provided maven wrapper to build the project.

```shell
 $ ./mvnw clean install
```

There are a couple of maven profiles to note:

 - `coverage`: Run unit tests with the JaCoCo coverage plugin.
 - `integration-tests`: Runs specific unit tests tagged with the `integration-tests` tags

## Usage

To run the bot you have to provide a registered token. The guild ID can be provided to create the commands for a
particular discord server, or be `-1` for a global command. Apart from the token & guild id, there is also the 
`discord.deniedIds` & `discord.disallowedTags` properties which can be set the same way. Both of these properties can 
accept a comma seperated list of values to deny certain users access to the bot, and disallow the use of certain tags.

```shell
 $ java -jar \
     -Ddiscord.token=${TOKEN} \
     -Ddiscord.guildId=${GUILD_ID} \
     -Ddiscord.deniedIds=123456,654321 \
     -Ddiscord.disallowedTags=furry,genshin impact \
     bot.jar
```

## Bot usage

The bot currently has one command the `/aniki` command. It has two optional options, the `source` which allows you to
fetch a post from a certain source. And the `tags` option which allows the user to fetch posts with certain tags. An 
example of running the command is:

```
/aniki source=rule34 tags=nier automata
```

## License

[MIT](LICENSE.txt)