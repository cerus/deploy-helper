<br>
<h2 align="center">deploy-helper</h3>

<p align="center">
    deploy-helper is a small tool for artifact deployment targetted at software developers.
</p>
<p align="center">
   <a href="https://github.com/cerus/deploy-helper" align="center">
       <img align="center" width="90%" height="90%" src="https://i.imgur.com/qumITZf.png"></img>
   </a>
   
    
   <p align="center"><img src="https://img.shields.io/github/license/cerus/deploy-helper" alt="GitHub"> <img src="https://img.shields.io/codacy/grade/0d786807a3104bce869089c34c922945/master" alt="Codacy branch grade"> <a href="https://github.com/cerus/deploy-helper/issues"><img src="https://img.shields.io/github/issues/cerus/deploy-helper" alt="GitHub issues"></a> <a href="https://github.com/cerus/deploy-helper/releases/latest"><img src="https://img.shields.io/github/v/release/cerus/deploy-helper" alt="GitHub release (latest by date)"></a> <img src="https://img.shields.io/github/stars/cerus/deploy-helper" alt="GitHub Repo stars"> <a href="https://github.com/sponsors/cerus"><img src="https://img.shields.io/github/sponsors/cerus" alt="GitHub Sponsors"></a></p>
</p>

## Table of contents

- [Requirements](#requirements)
- [Purpose](#purpose)
- [Drawbacks](#drawbacks)
- [Features](#features)
- [Configuration](#configuration)
- [Placeholders / Variables](#placeholders--variables)
- [Arguments](#arguments)
- [Installation](#installation)
- [How to run (examples)](#how-to-run-examples)
- [Building from source](#building-from-source)
- [Contributing](#contributing)

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
4. Drag and drop the new artifact to server

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

```json5
{
  // Required
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
  // Required
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
  // Optional
  "commands": [
    // Can be either a string or an array of strings
    "scp {ARTIFACT_PATH} {SSH_USER}@{DEST_ADDRESS}:{DEST_PATH}{ARTIFACT_NAME}"
  ],
  // Optional
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

You should also add `deploy-helper.json` to your `.gitignore` file.

> **Note:** There can only be one artifact.

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

## Installation

Installing deploy-helper is as easy as downloading the Jar from the latest release (in theory). However, you should probably keep the Jar at some sort of central location.

This is how I 'installed' deploy-helper on my Linux machine with Zsh:

1. `sudo mkdir /opt/deploy-helper`
2. `sudo chown max:max /opt/deploy-helper`
3. `mv ~/Downloads/deploy-helper-*.jar /opt/deploy-helper`
4. `echo "alias deploy='java --enable-preview -jar /opt/deploy-helper/deploy-helper-VERSION.jar'" >> ~/.zshrc`

Then simply reopen your terminal (or `source ~/.zshrc`) and you're done!

## How to run (examples)

Basic command:\
`java --enable-preview -jar deploy-helper-VERSION.jar OPTIONS`

Binding the command to an alias:\
`alias deploy='java --enable-preview -jar /opt/deploy-helper/deploy-helper-VERSION.jar'`

Running deploy-helper with another config:\
`deploy -c ~/my-deploy-config.json`

Running deploy-helper with specified destinations:\
`deploy -d service1,service4,service5`

## Building from source

**Prerequisites:**
- Java 15
- Maven
- Git
- A terminal

**Steps:**
1. Open your terminal
2. Clone the repository `git clone https://github.com/cerus/deploy-helper.git`
3. Go into the new folder `cd deploy-helper`
4. Run Maven `mvn clean package`
5. Get your fresh Jar from the target directory

## Contributing

Please follow the [contribution guidelines](CONTRIBUTING.md)
