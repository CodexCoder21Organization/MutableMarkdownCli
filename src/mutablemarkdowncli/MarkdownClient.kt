package mutablemarkdowncli

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * HTTP client for communicating with MutableMarkdownServiceServer.
 */
class MarkdownClient(private val baseUrl: String) {

    /**
     * Health check.
     */
    fun health(): String {
        return get("/health").optString("result", "OK")
    }

    /**
     * List all files.
     */
    fun listFiles(): List<FileInfo> {
        val response = get("/files")
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
        val response = get("/file?id=${encode(id)}")
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
        val response = get("/file?name=${encode(name)}")
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
        val body = JSONObject().apply {
            put("name", name)
            put("content", content)
        }
        val response = post("/file", body.toString())
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
        val body = JSONObject().apply {
            put("content", content)
        }
        put("/file?id=${encode(id)}", body.toString())
    }

    /**
     * Update file name.
     */
    fun updateName(id: String, name: String) {
        val body = JSONObject().apply {
            put("name", name)
        }
        put("/file?id=${encode(id)}", body.toString())
    }

    /**
     * Delete file.
     */
    fun deleteFile(id: String) {
        delete("/file?id=${encode(id)}")
    }

    private fun get(path: String): JSONObject {
        val url = URL("$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        return readResponse(conn)
    }

    private fun post(path: String, body: String): JSONObject {
        val url = URL("$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
        return readResponse(conn)
    }

    private fun put(path: String, body: String): JSONObject {
        val url = URL("$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
        return readResponse(conn)
    }

    private fun delete(path: String): JSONObject {
        val url = URL("$baseUrl$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        return readResponse(conn)
    }

    private fun readResponse(conn: HttpURLConnection): JSONObject {
        val responseCode = conn.responseCode
        val stream = if (responseCode >= 400) conn.errorStream else conn.inputStream
        val responseBody = stream?.bufferedReader()?.readText() ?: "{}"
        if (responseCode >= 400) {
            val errorJson = try { JSONObject(responseBody) } catch (e: Exception) { JSONObject() }
            throw RuntimeException(errorJson.optString("error", "HTTP error $responseCode"))
        }
        return JSONObject(responseBody)
    }

    private fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")
}

data class FileInfo(
    val id: String,
    val name: String,
    val lastModified: Long
)

data class FileData(
    val id: String,
    val name: String,
    val content: String,
    val lastModified: Long
)
