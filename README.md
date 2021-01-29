# MessageWatcher

This is a Discord bot which censors messages with specific words or links
it instead of deleting them completely like other bots.

To add a running version of this bot to your server use the following link:
https://discord.com/api/oauth2/authorize?client_id=804052505804800050&permissions=388160&scope=bot

## Setup

This section only applies if you want to host the bot yourself.
The following text is written for linux based systems. Windows or Mac systems 
might need slightly different setup steps. This bot runs on Java 11, so you 
need to install the Java 11 JDK for your system.

### Tokens & APIs

#### Discord bot token

To use this bot you need a Discord bot token. To create one open the
[Discord Developer Portal](https://discord.com/developers/applications)
and add a new application by clicking on the "New Application" button. \
After creating the application you should see general information about 
your application. If not please select your application in the list of 
applications. Now switch to the "Bot" tab on the left and click the
"Add Bot" button that shows up. \
You should now see information about the bot you created, and a button 
called "Copy" under a header that says "Token".
Click that button to copy the token of your bot. You will need this token in 
the [Environment section](#environment). \
Never give this token to anyone as that is all one needs to control your bot!

#### Adding the bot to your server

Once more in the
[Discord Developer Portal](https://discord.com/developers/applications)
select your application. Now switch to the "OAuth2" tab on the left and in the list of scopes select "bot". Now scroll
down and select all needed permissions:

```text
View Channels
Send Messages
Manage Messages
Embed Links
Attach Files
Read Message History
Use External Emojis
Add Reactions
```

Back up in the scopes section on that site there is now a link you can use to 
add the bot to your server with the selected permissions. To use that link 
just copy and paste it into a new browser tab or window. \
You can give the bot the administrator permission to decrease the amount of permissions to update but keep in mind that it literally means the bot can do anything. That
means if anyone has access to your token to control the bot he can do pretty much anything to your server. However, even
without the administrator permission the bot has enough permissions to interfere a lot with your server so even if you
do not give the bot the administrator permissions you need to keep your token very secure.

#### Additional information about the bot

* Not granting the bot the needed permissions might lead to unknown behaviour.
* Commands can only be used by users with administrator permission or users 
  with a mod role in channels the bot has access to.
* Censors will only happen in channels the bot has access to and can send 
  messages in.
* The censor will not censor words inside of links to prevent breaking links.  

### Environment

The environment variables carry some information for the bot to use. To get your bot running you must create a file
called `.env` in the same location where this file is located and add the following text to it:

```dotenv
DB_DATABASE=
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
* While it also works without, you should encapsulate text with spaces in it with quotation marks.

Settings that have to be set are marked with `[REQUIRED]`. If you leave these blank the program will not work correctly
and mostly will not even start completely.

#### DB_DATABASE

This setting defines the name of the database. If you do not set a value the default name "database" will be chosen. If
you change that value later on and do not rename the files in `/data/` accordingly the program will create a new
database!

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
standards, so it needs to include the "https(s)://" at the start. Discord only supports twitch and YouTube links at the
moment.

### Database

#### Creation

On the first start the program has to create the database which needs some action on your side. To perform that setup,
add the following line to the `.env` file:

```dotenv
SPRING_PROFILES_ACTIVE=firststart
```

Afterwards start the bot as described in
[starting and stopping the bot](#starting-and-stopping-the-bot). Wait until the bot is shown as online in Discord and
stop the program as described in the same section. Now remove the line you added to the `.env` file. \
Your database is now set up, and you can start and stop the program as you like. However, do not add the line back
to `.env` as that will lead to the program creating the database again and thus deleting all your data!

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
work for some reason you can also do

```shell
./gradlew --stop
```

which stops all running Gradle programs with the same Gradle version, so be sure that no other Gradle programs are
running or restart them afterwards.

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

This program currently offers two profiles. The default profile, and a developer profile called "dev" which has debug
outputs and other features that make developing and debugging easier. To change the profile open the `.env` file and add
the following line:

```dotenv
SPRING_PROFILES_ACTIVE=dev
```
If you want to use the H2 web console you need to add the following dependency 
to the `build.gradle`:
```
implementation 'org.springframework.boot:spring-boot-starter-web'
```
The database creation process in the `dev` profile will try to import a file
called `data.sql` in the `resources` folder on startup. It will crash if that 
file is not present so either disable the auto import in `application.yml` or
create the file yourself. The file can be used for sample data.

### Adding commands

To add a command to the bot there are a few steps to perform. First add a new entry ot the `CommandInfo` class. The next
step is to create the command class in `com.motorbesitzen.messagewatcher.bot.command.impl`. The command class needs to
extend `CommandImpl`. Afterwards add a method to `CommandBeanConfig` to provide a bean of the command. All classes
provide more information on what is needed so read them before performing any steps.

### Decisions

#### Why does this program use `Long` for the Discord IDs?

The Java part of this program takes Discord IDs as `Long` so the maximum ID is 9223372036854775807. If Discord does not
change its system and still uses the Twitter snowflake ID system then this ID will be reached around June 2084. If for
whatever reason Discord, the used technologies or this code should still be around at that time the code has to be
changed to accept `BigInteger` to avoid overflows while handling IDs as Discord uses the full 2<sup>64</sup>
range while the Java `Long` only uses 2<sup>63</sup>-1. 

