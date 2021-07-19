# MessageWatcher

This is a Discord bot which censors messages that contain specific words or links instead of deleting them completely
like other bots.

## Setup

This section only applies if you want to host the bot yourself. The following text is written for linux based systems.
Windows or Mac systems might need slightly different setup steps and a different startup and stop script. This bot runs
on Java 11, so you need to install the Java 11 JDK for your system.

### Tokens & APIs

#### Discord bot token

To use this bot you need a Discord bot token. To create one open the
[Discord Developer Portal](https://discord.com/developers/applications)
and add a new application by clicking on the "New Application" button. \
After creating the application you should see general information about your application. If not please select your
application in the list of applications. Now switch to the "Bot" tab on the left and click the
"Add Bot" button that shows up. \
You should now see information about the bot you created, and a button called "Copy" under a header that says "Token".
Click that button to copy the token of your bot. You will need this token in the [Environment section](#environment). \
Never give this token to anyone as that is all one needs to control your bot!

#### Adding the bot to your server

Once more in the
[Discord Developer Portal](https://discord.com/developers/applications)
select your application. Now switch to the "OAuth2" tab on the left and in the list of scopes select "bot". Now scroll
down and select all needed permissions:

```text
Kick Members
Ban Members
Manage Webhooks
View Channels
Send Messages
Manage Messages
Embed Links
Attach Files
Read Message History
Mention everyone
Use External Emojis
Add Reactions
```

Back up in the scopes section on that site there is now a link you can use to add the bot to your server with the
selected permissions. To use that link just copy and paste it into a new browser tab or window. \
You can give the bot the administrator permission to decrease the amount of permissions to update but keep in mind that
it literally means the bot can do anything. That means if anyone has access to your token to control the bot he can do
pretty much anything to your server. However, even without the administrator permission the bot has enough permissions
to interfere a lot with your server so even if you do not give the bot the administrator permissions you need to keep
your token very secure.

#### Additional information about the bot

* Not granting the bot the needed permissions might lead to unknown behaviour.
* Commands can only be used by users with administrator permission or users with a mod role in channels the bot has
  access to.
* Censors will only happen in channels the bot has access to and can send messages in.
* The censor will not censor words inside of links to prevent breaking links.

### Environment

The environment variables carry some information for the bot to use. To get your bot running you must create a file
called `.env` in the same location where this file is located and add the following text to it:

```dotenv
DB_ROOT_PASSWORD=
DB_USER=
DB_PASSWORD=

DC_TOKEN=

CMD_PREFIX=
EMBED_COLOR_R=
EMBED_COLOR_G=
EMBED_COLOR_B=

BOT_ACTIVITY=
BOT_ACTIVITY_TEXT=
BOT_ACTIVITY_STREAMING_URL=
```

In the following sections each of these settings will be explained. For each of them the following points need to be
met:

* Do not write text in multiple lines after the equal sign (`=`).
* Make sure your lines do not have trailing spaces or tabs!
* Encapsulate text with spaces in it with quotation marks (`"`).

Settings that have to be set are marked with `[REQUIRED]`. If you leave these blank the program will not work correctly
and mostly will not even start completely.

#### [REQUIRED] DB_ROOT_PASSWORD

The root password of the database. Make sure to use a secure password!

#### [REQUIRED] DB_USER and DB_PASSWORD

Username and password for the database to make sure no one else can access your database. Make sure to use a secure
password! \
If you change these values after the first run the program will not work as the database got set up with your old
values, so your new credentials are not correct, and the connection will be refused!

#### [REQUIRED] DC_TOKEN

This is the place for the Discord token mentioned in
[Discord bot token](#discord-bot-token). Never share this token with anyone!

#### CMD_PREFIX

This is the prefix the bot needs to react to a command. If this value is set to `?` the bot will only perform the "help"
command if a user writes
`?help`. If no value is set the bot has no prefix, so it only reacts to a message that starts with the actual command
like `help`. Do not use spaces in the prefix!

#### EMBED_COLOR_R

Discord bots can send embedded messages. These messages have a colored line on the left side. That line color is encoded
in RGB so this value represents the **R**ed part of that color. Please use a value between 0-255, otherwise results may
vary. If this value is not set it defaults to 222 which results in an orange color if [EMBED_COLOR_G](#embed_color_g)
and [EMBED_COLOR_B](#embed_color_b) also have no value set.

#### EMBED_COLOR_G

This value represents the **G**reen part of the color. Please use a value between 0-255, otherwise results may vary. If
this value is not set it defaults to 105 which results in an orange color if [EMBED_COLOR_R](#embed_color_r)
and [EMBED_COLOR_B](#embed_color_b) also have no value set.

#### EMBED_COLOR_B

This value represents the **B**lue part of the color. Please use a value between 0-255, otherwise results may vary. If
this value is not set it defaults to 12 which results in an orange color if [EMBED_COLOR_R](#embed_color_r)
and [EMBED_COLOR_G](#embed_color_g) also have no value set.

#### BOT_ACTIVITY

Discord bots can display an activity in the member list. Discord offers a few activities a bot can use which are:

* **listening** (to xyz)
* **playing** (xyz)
* **watching** (xyz)
* **streaming** (xyz)
* **competing** (in xyz)

If you want to display an activity you can use one of the bold printed words. If you use an activity that is not in this
list the activity will default to "**watching** user roles". If you do not want to set an activity just leave this field
blank or remove it completely. \
If you want to use an activity you also need to set a
[BOT_ACTIVITY_TEXT](#bot_activity_text). Otherwise, no activity will be displayed. If you want to use the streaming
activity make sure to also set the
[BOT_ACTIVITY_STREAMING_URL](#bot_activity_streaming_url) as if there is no valid URL set the bot will not display an
activity.

#### BOT_ACTIVITY_TEXT

This value replaces the `xyz` in the list shown in
[BOT_ACTIVITY](#bot_activity) with custom text to further customize your bot activity. A basic example would
be `user roles` which results in
"**watching** user roles" if you also set the fitting activity. If you do not set a text no activity will be shown at
all. Maximum length of this text is 128 characters!

#### BOT_ACTIVITY_STREAMING_URL

A link to a stream, only needs to be set if the streaming activity is used. Has to be valid according to Discord
standards, so it needs to include the "http(s)://" at the start. Discord only supports twitch and YouTube links at the
moment.

## Starting and stopping the bot

To start the bot you can just run the provided `start.sh` file like this:

```shell
sh start.sh
```

To stop the bot you can use:

```shell
sh stop.sh
```

For these scripts to work make sure to not delete the file `pid.txt` while the program is running. If `stop.sh` does not
work for some reason you can also search for the `java` process and kill it manually.

## Credits

* [leThrax](https://github.com/leThrax) for the original idea to code a bot which later included this functionality and
  first efforts to implement the original bot.
* [MinnDevelopment](https://github.com/MinnDevelopment),
  [DV8FromTheWorld](https://github.com/DV8FromTheWorld) and other contributors for developing and contributing to the
  [JDA library](https://github.com/DV8FromTheWorld/JDA)
  which this bot uses for the Discord side of things.

---

## Developer information

The following information is meant for people who want to add functionality to the bot or contribute in any other way.
You do not need to understand anything below to use this program.

### Profiles

This program currently offers a few profiles. The default profile (production), and the developer profile called "dev"
are probably the most important ones. The debug profile has debug outputs and other features that make developing and
debugging easier. To change the profile to the developer profile open the `.env` file and add the following line:

```dotenv
SPRING_PROFILES_ACTIVE=dev
```

The same effect can be achieved by changing the run configuration of your IDE to use that profile.

The database creation process in the `dev` profile will try to import a file called `data.sql` in the `resources` folder
on startup. It will crash if that file is not present so either disable the auto import in `application.yml` or create
the file yourself. The file can be used for sample data.

### Adding commands

To add a command to the bot there are a few steps to perform. First create the command class in
`com.motorbesitzen.messagewatcher.bot.command.impl`. The command class needs to extend `CommandImpl`. The command needs
to be a `@Service` and needs to have its command set as a value in lowercase. So a command like `help` would be the
following:

```java

@Service("help")
public class Help extends CommandImpl {
  // ...
}
```

Your IDE or the compiler should notify you about the methods you need to implement for a command to function.

### Adding event listeners

Event listeners do not need a name and thus no special value. Just annotate the listener class as a service and make
sure it extends the `ListenerAdapter`. Override at least one of the `ListenerAdapter` methods so your event listener
actually does something.

```java

@Service
public class SomeEventListener extends ListenerAdapter {
  // ...
}
```

