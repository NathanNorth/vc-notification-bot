# vc-notification-bot
Also known as the FOMO Bot and the Stalking Bot, the vc-notification-bot is a dead simple tool to help you keep track of when people join voice channels in Discord built using the shiny new [Slash Commands API](https://discord.com/developers/docs/interactions/slash-commands). 

Simply use `/watch <channel>` to add a channel to your watchlist, and `/unwatch <channel>` to do the opposite.

vc-notification-bot is built reactively using [Discord4J](https://github.com/Discord4J/Discord4J) and a [R2DBC](https://r2dbc.io/) managed PostgreSQL database backend.

# nitty-gritty
The nofitication/FOMO/stalking bot is released as both a compiled jar (without dependencies) and a docker compose that builds from source. Should you want to use the jar it requires being in the same directory as a `keys.txt` file that contains all the fields in the [example file](https://github.com/NathanNorth/vc-notification-bot/blob/master/docker-self-contained/keys.txt) (you'll need to specify credentials to your own PostgreSQL database in the keys).