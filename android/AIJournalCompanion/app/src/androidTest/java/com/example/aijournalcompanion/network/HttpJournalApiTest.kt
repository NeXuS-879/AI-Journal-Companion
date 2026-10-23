package com.example.aijournalcompanion.network

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.Emotion
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Loopback HTTP tests exercise Android's actual transport and JSON implementation. */
@RunWith(AndroidJUnit4::class)
class HttpJournalApiTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun allowLocalNetworkForTransportTests() {
        if (Build.VERSION.SDK_INT >= 37) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.ACCESS_LOCAL_NETWORK,
            )
        }
    }

    @Test
    fun postsUtf8JsonAndCanBeCalledFromMainDispatcher() = runBlocking {
        LocalServer(200, """{"emotion":"JOY","advice":" Keep going. "}""").use { server ->
            val text = "  I said \"hello\".\nI feel happy \uD83D\uDE0A  "
            val response = runBlocking(Dispatchers.Main) {
                HttpJournalApi(context, server.baseUrl).analyze(AnalyzeRequest(text))
            }
            assertEquals(Emotion.JOY, response.emotion)
            assertEquals("Keep going.", response.advice)
            val captured = server.request()
            assertEquals("POST /analyze HTTP/1.1", captured.requestLine)
            assertTrue(captured.headers.lowercase().contains("application/json"))
            assertEquals(text.trim(), JSONObject(captured.body).getString("text"))
        }
    }

    @Test
    fun preservesValidationAndServiceStatusCodes() = runBlocking {
        listOf(422, 502, 503, 500, 302).forEach { status ->
            LocalServer(status, """{"detail":"service error"}""").use { server ->
                try {
                    HttpJournalApi(context, server.baseUrl).analyze(AnalyzeRequest("A journal entry"))
                    fail("Expected HTTP $status")
                } catch (exception: ApiException.Http) {
                    assertEquals(status, exception.statusCode)
                }
            }
        }
    }

    @Test
    fun distinguishesTimeoutFromConnectionFailure() = runBlocking {
        LocalServer(200, "{}", delayMillis = 500).use { server ->
            try {
                HttpJournalApi(context, server.baseUrl, readTimeoutMillis = 50)
                    .analyze(AnalyzeRequest("A journal entry"))
                fail("Expected timeout")
            } catch (_: ApiException.Timeout) {
                // Expected: no response headers arrived before the read timeout.
            }
        }
        val unusedPort = ServerSocket(0).use { it.localPort }
        try {
            HttpJournalApi(context, "http://127.0.0.1:$unusedPort/")
                .analyze(AnalyzeRequest("A journal entry"))
            fail("Expected connection failure")
        } catch (_: ApiException.Connection) {
            // Expected: no server is listening.
        }
    }

    @Test
    fun rejectsMalformedOrInvalidSuccessResponses() = runBlocking {
        listOf(
            "not json",
            "[]",
            "{}",
            """{"emotion":"UNKNOWN","advice":"Pause."}""",
            """{"emotion":"JOY","advice":42}""",
            """{"emotion":"JOY","advice":"   "}""",
            """{"emotion":"JOY","advice":"First.\nSecond."}""",
            """{"emotion":"JOY","advice":"Pause.","extra":true}""",
            "x".repeat(16_385),
        ).forEach { body ->
            LocalServer(200, body).use { server ->
                try {
                    HttpJournalApi(context, server.baseUrl).analyze(AnalyzeRequest("A journal entry"))
                    fail("Expected invalid response")
                } catch (_: ApiException.InvalidResponse) {
                    // An HTTP 200 alone does not establish a valid analysis result.
                }
            }
        }
    }

    @Test
    fun deniedLocalPermissionIsReportedBeforeConnecting() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= 37)
        val deniedContext = object : ContextWrapper(context) {
            override fun getApplicationContext(): Context = this
            override fun checkSelfPermission(permission: String): Int =
                PackageManager.PERMISSION_DENIED
        }
        try {
            HttpJournalApi(deniedContext).analyze(AnalyzeRequest("A journal entry"))
            fail("Expected local network permission failure")
        } catch (_: ApiException.PermissionRequired) {
            // Verify explicit feedback without revoking permissions on the test process.
        }
    }

    private data class CapturedRequest(val requestLine: String, val headers: String, val body: String)

    /** Single-request fixture bound only to the emulator loopback address. */
    private class LocalServer(
        status: Int,
        body: String,
        delayMillis: Long = 0,
    ) : Closeable {
        private val socket = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        private val executor = Executors.newSingleThreadExecutor()
        val baseUrl = "http://127.0.0.1:${socket.localPort}/"
        private val result = executor.submit<CapturedRequest> {
            socket.accept().use { connection ->
                connection.soTimeout = 5_000
                val input = connection.getInputStream()
                val headerBytes = ByteArrayOutputStream()
                while (!headerBytes.toString("UTF-8").endsWith("\r\n\r\n")) {
                    val byte = input.read()
                    check(byte != -1) { "Request ended before its headers." }
                    headerBytes.write(byte)
                }
                val headers = headerBytes.toString("UTF-8")
                val length = headers.lineSequence()
                    .first { it.startsWith("Content-Length:", ignoreCase = true) }
                    .substringAfter(':').trim().toInt()
                val payload = ByteArray(length)
                var offset = 0
                while (offset < length) {
                    val count = input.read(payload, offset, length - offset)
                    check(count != -1)
                    offset += count
                }
                if (delayMillis > 0) Thread.sleep(delayMillis)
                val responseBytes = body.toByteArray(Charsets.UTF_8)
                val responseHeaders = "HTTP/1.1 $status Result\r\n" +
                    "Content-Type: application/json; charset=utf-8\r\n" +
                    "Content-Length: ${responseBytes.size}\r\nConnection: close\r\n\r\n"
                connection.getOutputStream().apply {
                    write(responseHeaders.toByteArray(Charsets.UTF_8))
                    write(responseBytes)
                    flush()
                }
                CapturedRequest(headers.lineSequence().first(), headers, payload.toString(Charsets.UTF_8))
            }
        }

        fun request(): CapturedRequest = result.get(5, TimeUnit.SECONDS)

        override fun close() {
            socket.close()
            executor.shutdownNow()
            executor.awaitTermination(2, TimeUnit.SECONDS)
        }
    }
}
