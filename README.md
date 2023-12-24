# Aniki Bot

A Discord Bot to posts random NSFW images from various sources and based on tags. Sources include:

- anime.r34.world
- gelbooru.com
- r-34.xyz
- rule34.xxx

## Installation

Use the provided maven wrapper to build the project.

```shell
 $ ./mvnw clean install
```

## Usage

To run the bot you have to provide a registered token. The guild ID can be provided to create the commands for a
particular discord server, or be `-1` for a global command.

```shell
 $ java -jar -Ddiscord.token=${TOKEN} -Ddiscord.guildId=${GUILD_ID} bot.jar
```

## License

[MIT](LICENSE.txt)