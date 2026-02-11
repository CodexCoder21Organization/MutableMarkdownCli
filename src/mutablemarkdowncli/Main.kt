package mutablemarkdowncli

import community.kotlin.markdown.api.MarkdownService
import foundation.url.protocol.Libp2pPeer
import foundation.url.resolver.UrlProtocol2
import foundation.url.resolver.UrlResolver
import org.apache.commons.cli.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Command-line interface for MutableMarkdownServiceServer.
 *
 * Commands:
 *   upload <file>         Upload a local markdown file to the service
 *   download <name>       Download a file by name to current directory
 *   edit <name>           Edit a file using vim
 *   list                  List all files in the service
 *   delete <name>         Delete a file by name
 *
 * Options:
 *   --server <url>        Server URL (default: url://markdown/)
 *                         Use url://markdown/ for URL protocol (P2P)
 *                         Use http://localhost:8080 for HTTP (local testing)
 *   --output <path>       Output path for download (default: <name>)
 *   --help                Show help
 */
fun main(args: Array<String>) {
    val options = Options().apply {
        addOption(Option.builder("s")
            .longOpt("server")
            .hasArg()
            .argName("url")
            .desc("Server URL (default: url://markdown/)")
            .build())
        addOption(Option.builder("o")
            .longOpt("output")
            .hasArg()
            .argName("path")
            .desc("Output path for download")
            .build())
        addOption(Option.builder("h")
            .longOpt("help")
            .desc("Show help")
            .build())
    }

    val parser = DefaultParser()
    val cmd: CommandLine

    try {
        cmd = parser.parse(options, args)
    } catch (e: ParseException) {
        System.err.println("Error: ${e.message}")
        printUsage(options)
        System.exit(1)
        return
    }

    if (cmd.hasOption("help") || cmd.argList.isEmpty()) {
        printUsage(options)
        return
    }

    val serverUrl = cmd.getOptionValue("server", "url://markdown/")

    val command = cmd.argList[0]
    val commandArgs = cmd.argList.drop(1)

    try {
        // Choose client based on URL scheme
        if (serverUrl.startsWith("url://")) {
            // Use typed SJVM sandboxed client for url:// URLs
            val peerId = "12D3KooWLMyXNfwhcX1YsiNx3hnjk3GGSfsU1fydRa8bzrE6scMT"
            val multiaddr = "/ip4/198.199.106.165/tcp/35000/p2p/$peerId"
            val bootstrapPeer = Libp2pPeer.remote(
                peerId = peerId,
                multiaddresses = listOf(multiaddr),
                advertisedServices = listOf("markdown")
            )
            val urlProtocol = UrlProtocol2(bootstrapPeers = listOf(bootstrapPeer))
            val resolver = UrlResolver(urlProtocol)
            resolver.use {
                val connection = resolver.openSandboxedConnection("url://markdown/", MarkdownService::class)
                connection.use {
                    val service = connection.proxy
                    executeCommandUrl(command, commandArgs, cmd.getOptionValue("output"), service, options)
                }
            }
        } else {
            // Use HTTP client for http:// or https:// URLs
            val client = MarkdownClient(serverUrl)
            executeCommandHttp(command, commandArgs, cmd.getOptionValue("output"), client, options)
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        System.exit(1)
    }
}

private fun executeCommandUrl(
    command: String,
    commandArgs: List<String>,
    outputPath: String?,
    service: MarkdownService,
    options: Options
) {
    when (command) {
        "upload" -> handleUploadUrl(service, commandArgs)
        "download" -> handleDownloadUrl(service, commandArgs, outputPath)
        "edit" -> handleEditUrl(service, commandArgs)
        "list" -> handleListUrl(service)
        "delete" -> handleDeleteUrl(service, commandArgs)
        "health" -> handleHealthUrl()
        else -> {
            System.err.println("Unrecognized command: $command")
            printUsage(options)
            System.exit(1)
        }
    }
}

private fun executeCommandHttp(
    command: String,
    commandArgs: List<String>,
    outputPath: String?,
    client: MarkdownClient,
    options: Options
) {
    when (command) {
        "upload" -> handleUpload(client, commandArgs)
        "download" -> handleDownload(client, commandArgs, outputPath)
        "edit" -> handleEdit(client, commandArgs)
        "list" -> handleList(client)
        "delete" -> handleDelete(client, commandArgs)
        "health" -> handleHealth(client)
        else -> {
            System.err.println("Unrecognized command: $command")
            printUsage(options)
            System.exit(1)
        }
    }
}

private fun printUsage(options: Options) {
    val formatter = HelpFormatter()
    println("MutableMarkdownCli - CLI for MutableMarkdownServiceServer")
    println()
    println("Commands:")
    println("  upload <file>         Upload a local markdown file to the service")
    println("  download <name>       Download a file by name to current directory")
    println("  edit <name>           Edit a file using vim")
    println("  list                  List all files in the service")
    println("  delete <name>         Delete a file by name")
    println("  health                Check server health")
    println()
    println("Server URL formats:")
    println("  url://markdown/       URL protocol (P2P, default)")
    println("  http://localhost:8080 HTTP (for local testing)")
    println()
    formatter.printHelp("markdown-cli [options] <command> [args]", options)
}

// ============================================================================
// URL Protocol handlers (typed SJVM sandboxed client)
// ============================================================================

private fun handleUploadUrl(service: MarkdownService, args: List<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("upload requires a file path argument")
    }

    val filePath = args[0]
    val file = File(filePath)

    if (!file.exists()) {
        throw IllegalArgumentException("File does not exist: $filePath")
    }

    if (!file.isFile) {
        throw IllegalArgumentException("Path is not a file: $filePath")
    }

    val name = file.name
    val content = file.readText()

    // Check if file already exists, update if so
    val existing = service.getFileByName(name)
    if (existing != null) {
        existing.content = content  // Mutable property triggers RPC
        println("Updated: $name (id: ${existing.id})")
    } else {
        val info = service.createFile(name, content)
        println("Uploaded: $name (id: ${info.id})")
    }
}

private fun handleDownloadUrl(service: MarkdownService, args: List<String>, outputPath: String?) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("download requires a file name argument")
    }

    val name = args[0]
    val markdownFile = service.getFileByName(name)
        ?: throw IllegalArgumentException("File not found: $name")

    val outputFile = File(outputPath ?: name)
    outputFile.writeText(markdownFile.content)
    println("Downloaded: $name -> ${outputFile.absolutePath}")
}

private fun handleEditUrl(service: MarkdownService, args: List<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("edit requires a file name argument")
    }

    val name = args[0]
    val markdownFile = service.getFileByName(name)

    // Create temp file
    val tempFile = File.createTempFile("markdown-edit-", ".md")
    tempFile.deleteOnExit()

    if (markdownFile != null) {
        tempFile.writeText(markdownFile.content)
    } else {
        tempFile.writeText("")
    }

    // Get last modified time before editing
    val lastModifiedBefore = tempFile.lastModified()

    // Open editor
    val editor = System.getenv("EDITOR") ?: "vim"
    val process = ProcessBuilder(editor, tempFile.absolutePath)
        .inheritIO()
        .start()
    val exitCode = process.waitFor()

    if (exitCode != 0) {
        throw RuntimeException("Editor exited with code $exitCode")
    }

    // Check if file was modified
    if (tempFile.lastModified() == lastModifiedBefore) {
        println("No changes made, skipping save")
        return
    }

    // Read updated content
    val newContent = tempFile.readText()

    // Save back to server
    if (markdownFile != null) {
        markdownFile.content = newContent  // Mutable property triggers RPC
        println("Updated: $name")
    } else {
        val info = service.createFile(name, newContent)
        println("Created: $name (id: ${info.id})")
    }

    // Clean up temp file
    tempFile.delete()
}

private fun handleListUrl(service: MarkdownService) {
    val files = service.getAllFiles()

    if (files.isEmpty()) {
        println("No files found")
        return
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    println("Files:")
    println("-".repeat(80))
    println(String.format("%-36s  %-30s  %s", "ID", "Name", "Last Modified"))
    println("-".repeat(80))
    for (file in files) {
        val lastModified = if (file.lastModified > 0) {
            dateFormat.format(Date(file.lastModified))
        } else {
            "N/A"
        }
        println(String.format("%-36s  %-30s  %s", file.id, file.name, lastModified))
    }
    println("-".repeat(80))
    println("Total: ${files.size} file(s)")
}

private fun handleDeleteUrl(service: MarkdownService, args: List<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("delete requires a file name argument")
    }

    val name = args[0]
    val file = service.getFileByName(name)
        ?: throw IllegalArgumentException("File not found: $name")

    service.deleteFile(file)
    println("Deleted: $name (id: ${file.id})")
}

private fun handleHealthUrl() {
    // Health check succeeds if we got this far - the sandboxed connection
    // was established successfully, which means the service is reachable.
    println("Server health: OK")
}

// ============================================================================
// HTTP handlers (for local testing)
// ============================================================================

private fun handleUpload(client: MarkdownClient, args: List<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("upload requires a file path argument")
    }

    val filePath = args[0]
    val file = File(filePath)

    if (!file.exists()) {
        throw IllegalArgumentException("File does not exist: $filePath")
    }

    if (!file.isFile) {
        throw IllegalArgumentException("Path is not a file: $filePath")
    }

    val name = file.name
    val content = file.readText()

    // Check if file already exists, update if so
    val existing = client.getFileByName(name)
    if (existing != null) {
        client.updateContent(existing.id, content)
        println("Updated: $name (id: ${existing.id})")
    } else {
        val info = client.createFile(name, content)
        println("Uploaded: $name (id: ${info.id})")
    }
}

private fun handleDownload(client: MarkdownClient, args: List<String>, outputPath: String?) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("download requires a file name argument")
    }

    val name = args[0]
    val file = client.getFileByName(name)
        ?: throw IllegalArgumentException("File not found: $name")

    val outputFile = File(outputPath ?: name)
    outputFile.writeText(file.content)
    println("Downloaded: $name -> ${outputFile.absolutePath}")
}

private fun handleEdit(client: MarkdownClient, args: List<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("edit requires a file name argument")
    }

    val name = args[0]
    val file = client.getFileByName(name)

    // Create temp file
    val tempFile = File.createTempFile("markdown-edit-", ".md")
    tempFile.deleteOnExit()

    if (file != null) {
        tempFile.writeText(file.content)
    } else {
        tempFile.writeText("")
    }

    // Get last modified time before editing
    val lastModifiedBefore = tempFile.lastModified()

    // Open vim
    val editor = System.getenv("EDITOR") ?: "vim"
    val process = ProcessBuilder(editor, tempFile.absolutePath)
        .inheritIO()
        .start()
    val exitCode = process.waitFor()

    if (exitCode != 0) {
        throw RuntimeException("Editor exited with code $exitCode")
    }

    // Check if file was modified
    if (tempFile.lastModified() == lastModifiedBefore) {
        println("No changes made, skipping save")
        return
    }

    // Read updated content
    val newContent = tempFile.readText()

    // Save back to server
    if (file != null) {
        client.updateContent(file.id, newContent)
        println("Updated: $name")
    } else {
        val info = client.createFile(name, newContent)
        println("Created: $name (id: ${info.id})")
    }

    // Clean up temp file
    tempFile.delete()
}

private fun handleList(client: MarkdownClient) {
    val files = client.listFiles()

    if (files.isEmpty()) {
        println("No files found")
        return
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    println("Files:")
    println("-".repeat(80))
    println(String.format("%-36s  %-30s  %s", "ID", "Name", "Last Modified"))
    println("-".repeat(80))
    for (file in files) {
        val lastModified = if (file.lastModified > 0) {
            dateFormat.format(Date(file.lastModified))
        } else {
            "N/A"
        }
        println(String.format("%-36s  %-30s  %s", file.id, file.name, lastModified))
    }
    println("-".repeat(80))
    println("Total: ${files.size} file(s)")
}

private fun handleDelete(client: MarkdownClient, args: List<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("delete requires a file name argument")
    }

    val name = args[0]
    val file = client.getFileByName(name)
        ?: throw IllegalArgumentException("File not found: $name")

    client.deleteFile(file.id)
    println("Deleted: $name (id: ${file.id})")
}

private fun handleHealth(client: MarkdownClient) {
    val result = client.health()
    println("Server health: $result")
}
