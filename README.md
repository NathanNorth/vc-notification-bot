# vc-notification-bot
Also known as the FOMO Bot and the Stalking Bot, the vc-notification-bot is a dead simple tool to help you keep track of when people join voice channels in Discord built using the shiny new [Slash Commands API](https://discord.com/developers/docs/interactions/slash-commands). Simply use `/watch <channel>` to add a channel to your watchlist, and `/unwatch <channel>` to do the opposite.

vc-notification-bot is built reactively using [Discord4J](https://github.com/Discord4J/Discord4J) and a [R2DBC](https://r2dbc.io/) managed PostgreSQL database backend.