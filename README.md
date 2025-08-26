# mattermost-moderator-bot
The bot listens specified channels in Mattermost Community Edition and deletes all messages from users not in the allowed list.
This allows to organize "read-only" channels which are possible only in the payable version of Mattermost.

The bot application can be started either as standalone java application or via docker container.

## How to setup application to be run
### Prerequisites
1. You need Mattermost server application setup and running on some host
2. You need create/select Bot to operate on behalf of and get access token for it.

### Properties file
&#x1F4A1; Do not store sensitive settings right in the provided "./settings.props" file for security reasons!

First of all - you need to prepare properties file ([java properties](https://en.wikipedia.org/wiki/.properties)) with mandatory settings.
* **MATTERMOST_URL** - is a domain name where mattermost server application is running. User only domain names - without "https" scheme.
```properties
MATTERMOST_URL=mymattermost.domain.com
```
* **MATTERMOST_TOKEN** - is a bot access token
```properties
MATTERMOST_TOKEN=fsd7fh2o1ijd0s8fgyg1h208
```
* **ALLOWED_EMAIL[\$UNIQUE_TEXT\$] = \$EMAIL\$** - you need provide at least one email address in the allowed emails list to allow at least one user to post messages.
As java properties file is a store of key-value pairs, the keys must be unique. To allow multiple-value keys in 
a simple way - following format is used for the allowed emails:r

So, to set two emails in allowed list use following example
```properties
ALLOWED_EMAIL[1]=first-email@example.com
ALLOWED_EMAIL[2]=second-email@example.com
```
* **MONITORED_CHANNEL[\$UNIQUE_TEXT\$] = \$CHANNEL_NAME\$** - like allowed emails - a multiple values list of channel names (not ids) to be monitored by the bot.
In case no channels are specified - then all channels will be monitored by the bot (not recommended).
```properties
MONITORED_CHANNEL[1]=newschannel
MONITORED_CHANNEL[2]=town-square
```

How to find channel name:
1. Go to mattermost client
2. Find necessary channel
3. Click on the channel header and select "Rename Channel"
4. Find "URL" parameter in the opened dialog. The last editable part of the URL will be the name of the channel.

### Running application
As soon as properties file is ready - you can run bot application
#### As Java app
```shell
  mvn clean package assembly:single
  java -jar ./target/mattermost-moderator-bot-1.0.jar \$PATH_TO_PROPERTIES_FILE\$
```

#### Using docker
```shell
    sudo docker build -t mbot:0.0.1 .
    sudo dockerun --mount type=bind,source=/tmp/my_external_file_with.properties,target=/tmp/settings.props mbot:0.0.1 -d  
```

Note: target file "/tmp/settings.props" is a hardcoded path.