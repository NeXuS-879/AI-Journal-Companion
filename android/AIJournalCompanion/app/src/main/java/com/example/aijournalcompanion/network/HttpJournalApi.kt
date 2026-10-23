package com.example.aijournalcompanion.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.aijournalcompanion.BuildConfig
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.AnalyzeResponse
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** HTTP/JSON client for FastAPI. All blocking socket work runs on the IO dispatcher. */
class HttpJournalApi(
    context: Context,
    baseUrl: String = BuildConfig.BACKEND_BASE_URL,
    private val connectTimeoutMillis: Int = 5_000,
    private val readTimeoutMillis: Int = 130_000,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : JournalApi {
    private val applicationContext = context.applicationContext
    private val endpoint = URL("${baseUrl.trimEnd('/')}/analyze")
    private val codec = AnalysisJsonCodec()

    init {
        require(endpoint.protocol in setOf("http", "https"))
        require(endpoint.host.isNotBlank())
        require(endpoint.query == null && endpoint.ref == null && endpoint.userInfo == null)
        require(connectTimeoutMillis > 0 && readTimeoutMillis > 0)
    }

    override suspend fun analyze(request: AnalyzeRequest): AnalyzeResponse =
        withContext(ioDispatcher) {
            ensureActive()
            if (Build.VERSION.SDK_INT >= 37 && applicationContext.checkSelfPermission(
                    Manifest.permission.ACCESS_LOCAL_NETWORK,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw ApiException.PermissionRequired()
            }
            val connection = endpoint.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                // Do not forward sensitive journal text to a redirected destination.
                connection.instanceFollowRedirects = false
                connection.connectTimeout = connectTimeoutMillis
                connection.readTimeout = readTimeoutMillis
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                val body = codec.encode(request).toByteArray(Charsets.UTF_8)
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { it.write(body) }

                val status = connection.responseCode
                ensureActive()
                if (status != HttpURLConnection.HTTP_OK) throw ApiException.Http(status)

                // Bound malformed responses; a valid emotion/advice payload is much smaller.
                val responseBody = connection.inputStream.bufferedReader(Charsets.UTF_8).use {
                    val buffer = CharArray(MAX_RESPONSE_CHARACTERS + 1)
                    var length = 0
                    while (length < buffer.size) {
                        val count = it.read(buffer, length, buffer.size - length)
                        if (count == -1) break
                        length += count
                    }
                    if (length > MAX_RESPONSE_CHARACTERS) throw ApiException.InvalidResponse()
                    String(buffer, 0, length)
                }
                ensureActive()
                codec.decode(responseBody)
            } catch (exception: SocketTimeoutException) {
                ensureActive()
                throw ApiException.Timeout(exception)
            } catch (exception: IOException) {
                ensureActive()
                throw ApiException.Connection(exception)
            } finally {
                connection.disconnect()
            }
        }

    private companion object {
        const val MAX_RESPONSE_CHARACTERS = 16_384
    }
}
