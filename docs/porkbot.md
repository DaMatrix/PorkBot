---
layout: default
---

# PorkBot
A Discord bot

<a href="https://discordapp.com/oauth2/authorize?client_id=287894637165936640&scope=bot&permissions=37014592" target="_blank">Invite</a>

<a href="https://github.com/DaMatrix/PorkBot" target="_blank">Source code</a>

## Commands

#### Categories

[Minecraft](#minecraft)

[Music](#music)

[Misc](#misc)

# Minecraft<a name="minecraft"></a>

| Command name | Description                                                            | Usage                                          | Example                                                                | Note                                                          |
|--------------|------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------|
| help         | Sends a link to this page                                              | `..help`                                       | `..help`                                                               |                                                               |
| invite       | Sends the bot invite link                                              | `..invite`                                     | `..invite`                                                             |                                                               |
| botinfo      | Gets some information about the bot                                    | `..botinfo`                                    | `..botinfo`                                                            |                                                               |
| ping         | Shows the bot's latency                                                | `..ping`                                       | `..ping`                                                               |                                                               |
| say          | Makes the bot send a message                                           | `..say <message you want to say>`              | `..say Hello World!`                                                   |                                                               |
| mcavatar     | Shows the face of a Minecraft skin                                     | `..mcavatar <username>`                        | `..mcavatar Notch`                                                     |                                                               |
| mchead       | Shows the head of a Minecraft skin                                     | `..mchead <username>`                          | `..mchead Notch`                                                       |                                                               |
| mcskin       | Shows a Minecraft skin                                                 | `..mcskin <username>`                          | `..mcskin Notch`                                                       |                                                               |
| skinsteal    | Shows a raw Minecraft skin                                             | `..skinsteal <username>`                       | `..skinsteal Notch`                                                    |                                                               |
| mcping       | Pings a Minecraft server and gets some info about it                   | `..mcping <ip>` OR `..mcping <ip:port>`        | `..mcping 2b2t.org` OR `..mcping 2b2t.org:25565`                       |                                                               |
| mcmotd       | Gets the current MOTD of a Minecraft server                            | `..mcmotd <ip>` OR `..mcmotd <ip:port>`        | `..mcmotd 2b2t.org` OR `..mcmotd 2b2t.org:25565`                       |                                                               |
| mccount      | Gets the current player count on a Minecraft server                    | `..mccount <ip>` OR `..mccount <ip:port>`      | `..mccount 2b2t.org` OR `..mccount 2b2t.org:25565`                     |                                                               |
| mcicon       | Gets the icon of a Minecraft server                                    | `..mcicon <ip>` OR `..mcicon <ip:port>`        | `..mcicon 2b2t.org` OR `..mcicon 2b2t.org:25565`                       |                                                               |
| mclatency    | Gets the latency (ping) to a Minecraft server                          | `..mclatency <ip>` OR `..mclatency <ip:port>`  | `..mclatency 2b2t.org` OR `..mclatency 2b2t.org:25565`                 |                                                               |
| mcversion    | Gets a Minecraft server's version                                      | `..mcversion <ip>` OR `..mcverision <ip:port>` | `..mcversion 2b2t.org` OR `..mcversion 2b2t.org:25565`                 | Returns protocol version number too                           |
| mcquery      | Gets more information about a Minecraft server                         | `..mcquery <ip>` OR `..mcquery <ip:port>`      | `..mcquery bepis.pepsi.team` OR `..mcquery mc.glowstone.net:25565` | Only works if `enable-query` is `true` in `server.properties` |
| peping       | Pings a Minecraft: Pocket Edition server and gets some info about it   | `..peping <ip>` OR `..peping <ip:port>`        | `..peping play.2p2e.net` OR `..peping play.2p2e.net:19132`                 |                                                               |
| pequery      | Queries a Minecraft: Pocket Edition server and gets more info about it | `..pequery <ip>` OR `..pequery <ip:port>`      | `..pequery play.2p2e.net` OR `..pequery play.2p2e.net:19132`               | Only works if `enable-query` is `true` in `server.properties` |
| pecount      | Gets the current player count on a Minecraft: Pocket Edition server    | `..pecount <ip>` OR `..pecount <ip:port>`      | `..pecount play.2p2e.net` OR `..pecount play.2p2e.net:19132`               |                                                               |
| pelatency    | Gets the latency (ping) to a Minecraft: Pocket Edition server          | `..pelatency <ip>` OR `..pelatency <ip:port>`  | `..pelatency play.2p2e.net` OR `..pelatency play.2p2e.net:19132`           |                                                               |
| peversion    | Gets a Minecraft: Pocket Edition server's version                      | `..peversion <ip>` OR `..peversion <ip:port>`  | `..peversion play.2p2e.net` OR `..peversion play.2p2e.net:19132`           |                                                               |
| pemotd       | Gets the current MOTD of a Minecraft: Pocket Edition server            | `..pemotd <ip>` OR `..pemotd <ip:port>`        | `..pemotd play.2p2e.net` OR `..pemotd play.2p2e.net:19132`                 |                                                               |
| mcstatus     | Gets the status of Mojang servers                                      | `..mcstatus`                                   | `..mcstatus`                                                           |                                                               |
| mcuuid       | Gets the UUID associated with a Minecraft username                     | `..mcuuid <player name>`                       | `..mcuuid Notch`                                                       |                                                               |
| offlineuuid  | Gets the UUID that a player would have on an offline-mode server       | `..offlineuuid <player name>`                  | `..offlineuuid Notch`                                                  |                                                               |


# Music<a name="music"></a>

| Command name | Description                                                            | Usage                                          | Example                                                                | Note                                                          |
|--------------|------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------|
| play         | Adds a song to the playlist.                                           | `..play <URL>` or `..play <search terms>`      | `..play nyan cat 10 hours`                                                     |                                                               |
| queue        | Shows the songs in the playlist.                                       | `..queue`                                      | `..queue`                                                              |                                                               |
| shuffle      | Shuffles the playlist                                                  | `..shuffle`                                    | `..shuffle`                                                            |                                                               |
| skip         | Skips the currently playing song                                       | `..skip`                                       | `..skip`                                                               |                                                               |
| stop         | Clears the playlist and stops playing                                  | `..stop`                                       | `..stop`                                                               |                                                               |


# Misc<a name="misc"></a>

| Command name | Description                                                            | Usage                                          | Example                                                                | Note                                                          |
|--------------|------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------|
| thonk        | ![thonk](https://cdn.discordapp.com/emojis/324070259265110016.png)     | `..thonk`                                      | `..thonk`                                                              |                                                               |
| dice         | Rolls a die!                                                           | `..dice`                                       | `..dice`                                                               |                                                               |
| emojiid      | Gets the ID of an emoji                                                | `..emojiid`                                    | `..emojiid`                                                            | In format of `<:emotename:numberid>`                          |
| interject    | Sends the Linux/GNU copypasta                                          | `..interject`                                  | `..interject`                                                          |                                                               |
