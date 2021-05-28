# deploy-helper

deploy-helper is a small tool for artifact deployment.

## Requirements

- **Java 15!**
- OpenSSH (or an equivalent) for ssh sessions
    - 'sshpass' if you want to use plaintext password auth

## Purpose

I wrote deploy-helper to help me deploy artifacts to a test network when I'm working on things. I probably could have used my IDE for that, but that's
not fun!

Steps without deploy-helper:

1. Compile
2. Open file explorer
3. Delete old artifact from server
4. Drag and drop new artifact to server

Steps with deploy-helper:

1. Compile
2. Run deploy-helper

## Drawbacks

- It's written in Java
- It doesn't have a lot of features

## Features

- Multiple deploy destinations
- Local commands
- SSH session

## Configuration

The default configuration file `deploy-helper.json` needs to be in your working directory.\
Example config:

```json
{
  "destinations": [
    {
      // Name of the destination
      "name": "service1",
      // Normally you would set a real address or a ip here,
      // but you could also use aliases from your ssh config (like I did here)
      "address": "test-network",
      // The path for our destination (used for the ssh session)
      "path": "/home/user/services/service1/data/",
      // SSH_COMMANDS (ssh first, then commands) or 
      // COMMANDS_SSH (commands first, then ssh)
      "order": "SSH_COMMANDS"
    },
    {
      "name": "service2",
      "address": "test-network",
      "path": "/home/user/services/service1/data/",
      "order": "SSH_COMMANDS"
    }
  ],
  "artifact": {
    // Directory that contains the artifact
    "directory": "./target",
    // Name regex that matches the artifact
    "name": "my-software-[\\d.A-Za-z-]+\\.jar",
    // Sorting operation for possible artifacts
    // LAST_MODIFIED_ASC (oldest file first) or
    // LAST_MODIFIED_DESC (newest file first)
    "sort": "LAST_MODIFIED_DESC"
  },
  "commands": [
    // Can be either a string or an array of strings
    "scp {ARTIFACT_PATH} {SSH_USER}@{DEST_ADDRESS}:{DEST_PATH}{ARTIFACT_NAME}"
  ],
  "ssh": {
    // If port is > 0, the ssh command will be USER@HOST:PORT
    // If the port is <= 0, the ssh command will be USER@HOST
    "port": 0,
    // How long to wait for the connection to establish
    "sleep": "2000",
    // The ssh user
    "user": "user",
    // Plaintext password auth requires 'sshpass'
    // "password": "foobar",
    "commands": [
      // Commands have to be a string
      "rm my-software-*.jar"
    ]
  }
}
```

This example config does the following:

1. Start ssh session
2. Run `rm` command to delete old artifact
3. Exit ssh session
4. Run `scp` command to copy new artifact to server

Note:\
There can only be one artifact.

## Placeholders / Variables

`{DEST_ADDRESS}`: Destination address\
`{DEST_NAME}`: Destination name\
`{DEST_PATH}`: Destination path\
`{SSH_USER}`: SSH user\
`{SSH_PORT}`: SSH port\
`{ARTIFACT_PATH}`: Artifact path\
`{ARTIFACT_NAME}`: Artifact name

## Arguments

| Long | Short | Description |
| --- | --- | --- |
| --config | -c | Override the config location |
| --verbose | -v | If enabled the process output will be redirected to stdout & stderr |
| --destination | -d | Override the destinations (comma seperated list) |

## How to run (examples)

Basic command:\
`java --enable-preview -jar deploy-helper-VERSION.jar OPTIONS`

Binding the command to an alias:\
`alias deploy='java --enable-preview -jar /opt/deploy-helper/deploy-helper-VERSION.jar'`

Running deploy-helper with another config:\
`deploy -c ~/my-deploy-config.json`

Running deploy-helper with specified destinations:\
`deploy -c service1,service4,service5`