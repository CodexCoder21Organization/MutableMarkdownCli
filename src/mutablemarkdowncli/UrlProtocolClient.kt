package mutablemarkdowncli

import foundation.url.resolver.UrlResolver
import org.json.JSONObject

/**
 * Client for communicating with MutableMarkdownServiceServer via URL protocol.
 *
 * This client uses UrlResolver to discover and communicate with the url://markdown/
 * service over the P2P network.
 */
class UrlProtocolClient(private val serviceUrl: String) : AutoCloseable {
    private val resolver = UrlResolver()

    init {
        // Join the network to participate in gossip and receive announcements
        resolver.joinNetwork()

        // Discover peers from the network
        resolver.discoverPeers(10)

        // Wait for service announcements to be received via gossip
        Thread.sleep(3000)

        // Query for the service
        val serviceName = serviceUrl.removePrefix("url://").removeSuffix("/")
        var matchingPeers = resolver.findPeersForService(serviceName)

        // If no peers found, retry with more discovery
        if (matchingPeers.isEmpty()) {
            for (attempt in 1..5) {
                resolver.discoverPeers(5)
                Thread.sleep(2000)
                matchingPeers = resolver.findPeersForService(serviceName)
                if (matchingPeers.isNotEmpty()) break
            }
        }

        if (matchingPeers.isEmpty()) {
            System.err.println("Warning: No peers found advertising '$serviceName' service")
        }
    }

    /**
     * Health check.
     */
    fun health(): String {
        return callRpc("health", emptyMap())?.optString("result", "OK") ?: "OK"
    }

    /**
     * List all files.
     */
    fun listFiles(): List<FileInfo> {
        val response = callRpc("getAllFiles", emptyMap()) ?: return emptyList()
        val files = response.optJSONArray("files") ?: return emptyList()
        return (0 until files.length()).map { i ->
            val obj = files.getJSONObject(i)
            FileInfo(
                id = obj.getString("id"),
                name = obj.getString("name"),
                lastModified = obj.getLong("lastModified")
            )
        }
    }

    /**
     * Get file by ID.
     */
    fun getFileById(id: String): FileData? {
        val response = callRpc("getFile", mapOf("id" to id)) ?: return null
        if (!response.optBoolean("found", true)) return null
        return FileData(
            id = response.getString("id"),
            name = response.getString("name"),
            content = response.getString("content"),
            lastModified = response.getLong("lastModified")
        )
    }

    /**
     * Get file by name.
     */
    fun getFileByName(name: String): FileData? {
        val response = callRpc("getFileByName", mapOf("name" to name)) ?: return null
        if (!response.optBoolean("found", true)) return null
        if (response.has("error")) return null
        return FileData(
            id = response.getString("id"),
            name = response.getString("name"),
            content = response.getString("content"),
            lastModified = response.getLong("lastModified")
        )
    }

    /**
     * Create a new file.
     */
    fun createFile(name: String, content: String): FileInfo {
        val response = callRpc("createFile", mapOf("name" to name, "content" to content))
            ?: throw RuntimeException("Failed to create file: no response from server")
        return FileInfo(
            id = response.getString("id"),
            name = response.getString("name"),
            lastModified = response.getLong("lastModified")
        )
    }

    /**
     * Update file content.
     */
    fun updateContent(id: String, content: String) {
        callRpc("setContent", mapOf("id" to id, "content" to content))
    }

    /**
     * Update file name.
     */
    fun updateName(id: String, name: String) {
        callRpc("setName", mapOf("id" to id, "name" to name))
    }

    /**
     * Delete file.
     */
    fun deleteFile(id: String) {
        callRpc("deleteFile", mapOf("id" to id))
    }

    private fun callRpc(method: String, params: Map<String, Any?>): JSONObject? {
        return try {
            val result = resolver.sendServiceRpcRequest(serviceUrl, method, params)
            if (result == null) {
                null
            } else {
                JSONObject(result)
            }
        } catch (e: Exception) {
            throw RuntimeException("RPC call to $method failed: ${e.message}", e)
        }
    }

    override fun close() {
        resolver.close()
    }
}
