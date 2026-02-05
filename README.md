# MutableMarkdownCli

Command-line interface for interacting with MutableMarkdownServiceServer.

## Overview

MutableMarkdownCli provides commands to upload, download, edit, list, and delete markdown files stored on a MutableMarkdownServiceServer.

## Building

```bash
# Build fat JAR (executable)
./scripts/build.bash mutablemarkdowncli.buildFatJar output.jar

# Build Maven artifact
./scripts/build.bash mutablemarkdowncli.buildMaven

# Launch directly
./scripts/build.bash --launch mutablemarkdowncli.buildFatJar
```

## Running

### Running the Fat JAR

```bash
java -jar markdown-cli.jar [options] <command> [args]
```

## Commands

| Command | Description |
|---------|-------------|
| `upload <file>` | Upload a local markdown file to the service |
| `download <name>` | Download a file by name to the current directory |
| `edit <name>` | Edit a file using vim (or EDITOR env var) |
| `list` | List all files in the service |
| `delete <name>` | Delete a file by name |
| `health` | Check server health |

## Options

| Option | Description | Default |
|--------|-------------|---------|
| `-s, --server <url>` | Server URL | `http://localhost:8080` |
| `-o, --output <path>` | Output path for download | `<name>` |
| `-h, --help` | Show help | - |

## Examples

### Start the server first

```bash
HTTP_PORT=8080 java -jar mutable-markdown-server.jar
```

### Upload a file

```bash
java -jar markdown-cli.jar upload README.md
# Output: Uploaded: README.md (id: 550e8400-e29b-41d4-a716-446655440000)
```

### List all files

```bash
java -jar markdown-cli.jar list
# Output:
# Files:
# --------------------------------------------------------------------------------
# ID                                    Name                            Last Modified
# --------------------------------------------------------------------------------
# 550e8400-e29b-41d4-a716-446655440000  README.md                       2024-02-02 10:30:00
# --------------------------------------------------------------------------------
# Total: 1 file(s)
```

### Download a file

```bash
java -jar markdown-cli.jar download README.md
# Output: Downloaded: README.md -> /current/dir/README.md

# Or specify output path:
java -jar markdown-cli.jar -o /tmp/readme.md download README.md
# Output: Downloaded: README.md -> /tmp/readme.md
```

### Edit a file

```bash
java -jar markdown-cli.jar edit README.md
# Opens vim with the file content
# After saving and quitting vim, changes are uploaded to the server
# Output: Updated: README.md
```

### Delete a file

```bash
java -jar markdown-cli.jar delete README.md
# Output: Deleted: README.md (id: 550e8400-e29b-41d4-a716-446655440000)
```

### Connect to a different server

```bash
java -jar markdown-cli.jar --server http://example.com:8080 list
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `EDITOR` | Editor to use for `edit` command (default: `vim`) |

## Maven Coordinates

```
community.kotlin.markdown:cli:0.0.1
```

## Related Projects

- **MutableMarkdownServiceServer** - The server this CLI connects to
- **MutableMarkdownApi** - Interface definitions
