---
layout: default
---

# PorkBot
A Discord bot

[Invite](https://discordapp.com/oauth2/authorize?client_id=287894637165936640&scope=bot&permissions=67226625&redirect_uri=http://www.daporkchop.tk/porkbot)

[Source code](https://github.com/DaMatrix/PorkBot)

## Commands

#### Categories

[Minecraft](#minecraft)

[Misc](#misc)

# Minecraft<a name="minecraft"></a>

| Command name | Description                                                            | Usage                                          | Example                                                                | Note                                                          |
|--------------|------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------|
| help         | Sends a link to this page                                              | `..help`                                       | `..help`                                                               |                                                               |
| invite       | Sends the bot invite link                                              | `..invite`                                     | `..invite`                                                             |                                                               |
| botinfo      | Gets some information about the bot                                    | `..botinfo`                                    | `..botinfo`                                                            |                                                               |
| ping         | Shows the bot's latency                                                | `..ping`                                       | `..ping`                                                               |                                                               |
| say          | Makes the bot send a message                                           | `..say <message you want to say>`              | `..say Hello World!`                                                   |                                                               |
| mcavatar     | Shows the face of a Minecraft skin                                     | `..mcavatar <username>`                        | `..mcavatar Notch`                                                     | Also works with UUID instead of username                      |
| mchead       | Shows the head of a Minecraft skin                                     | `..mchead <username>`                          | `..mchead Notch`                                                       | Also works with UUID instead of username                      |
| mcskin       | Shows a Minecraft skin                                                 | `..mcskin <username>`                          | `..mcskin Notch`                                                       | Also works with UUID instead of username                      |
| mcping       | Pings a Minecraft server and gets some info about it                   | `..mcping <ip>` OR `..mcping <ip:port>`        | `..mcping 2b2t.org` OR `..mcping 2b2t.org:25565`                       |                                                               |
| mcmotd       | Gets the current MOTD of a Minecraft server                            | `..mcmotd <ip>` OR `..mcmotd <ip:port>`        | `..mcmotd 2b2t.org` OR `..mcmotd 2b2t.org:25565`                       |                                                               |
| mccount      | Gets the current player count on a Minecraft server                    | `..mccount <ip>` OR `..mccount <ip:port>`      | `..mccount 2b2t.org` OR `..mccount 2b2t.org:25565`                     |                                                               |
| mcicon       | Gets the icon of a Minecraft server                                    | `..mcicon <ip>` OR `..mcicon <ip:port>`        | `..mcicon 2b2t.org` OR `..mcicon 2b2t.org:25565`                       |                                                               |
| mclatency    | Gets the latency (ping) to a Minecraft server                          | `..mclatency <ip>` OR `..mclatency <ip:port>`  | `..mclatency 2b2t.org` OR `..mclatency 2b2t.org:25565`                 |                                                               |
| mcversion    | Gets a Minecraft server's version                                      | `..mcversion <ip>` OR `..mcverision <ip:port>` | `..mcversion 2b2t.org` OR `..mcversion 2b2t.org:25565`                 | Returns protocol version number too                           |
| mcquery      | Gets more information about a Minecraft server                         | `..mcquery <ip>` OR `..mcquery <ip:port>`      | `..mcquery home.daporkchop.tk` OR `..mcquery home.daporkchop.tk:25565` | Only works if `enable-query` is `true` in `server.properties` |
| peping       | Pings a Minecraft: Pocket Edition server and gets some info about it   | `..peping <ip>` OR `..peping <ip:port>`        | `..peping sg.lbsg.com` OR `..peping sg.lbsg.com:19132`                 |                                                               |
| pequery      | Queries a Minecraft: Pocket Edition server and gets more info about it | `..pequery <ip>` OR `..pequery <ip:port>`      | `..pequery sg.lbsg.com` OR `..pequery sg.lbsg.com:19132`               | Only works if `enable-query` is `true` in `server.properties` |
| pecount      | Gets the current player count on a Minecraft: Pocket Edition server    | `..pecount <ip>` OR `..pecount <ip:port>`      | `..pecount sg.lbsg.com` OR `..pecount sg.lbsg.com:19132`               |                                                               |
| pelatency    | Gets the latency (ping) to a Minecraft: Pocket Edition server          | `..pelatency <ip>` OR `..pelatency <ip:port>`  | `..pelatency sg.lbsg.com` OR `..pelatency sg.lbsg.com:19132`           |                                                               |
| peversion    | Gets a Minecraft: Pocket Edition server's version                      | `..peversion <ip>` OR `..peversion <ip:port>`  | `..peversion sg.lbsg.com` OR `..peversion sg.lbsg.com:19132`           |                                                               |
| pemotd       | Gets the current MOTD of a Minecraft: Pocket Edition server            | `..pemotd <ip>` OR `..pemotd <ip:port>`        | `..pemotd sg.lbsg.com` OR `..pemotd sg.lbsg.com:19132`                 |                                                               |
| mcstatus     | Gets the status of Mojang servers                                      | `..mcstatus`                                   | `..mcstatus`                                                           |                                                               |
| mcuuid       | Gets the UUID associated with a Minecraft username                     | `..mcuuid <player name>`                       | `..mcuuid Notch`                                                       |                                                               |
| offlineuuid  | Gets the UUID that a player would have on an offline-mode server       | `..offlineuuid <player name>`                  | `..offlineuuid Notch`                                                  |                                                               |


# Misc<a name="misc"></a>

| Command name | Description                                                            | Usage                                          | Example                                                                | Note                                                          |
|--------------|------------------------------------------------------------------------|------------------------------------------------|------------------------------------------------------------------------|---------------------------------------------------------------|
| thonk        | ![thonk](https://cdn.discordapp.com/emojis/324070259265110016.png)     | `..thonk`                                      | `..thonk`                                                              |                                                               |
| dice         | Rolls a die!                                                           | `..dice`                                       | `..dice`                                                               |                                                               |
| emojiid      | Gets the ID of an emoji                                                | `..emojiid`                                    | `..emojiid`                                                            | In format of `<:emotename:numberid>`                          |
| interject    | Sends the Linux/GNU copypasta                                          | `..interject`                                  | `..interject`                                                          |                                                               |
