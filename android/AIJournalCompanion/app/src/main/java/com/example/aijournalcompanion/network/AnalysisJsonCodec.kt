package com.example.aijournalcompanion.network

import com.example.aijournalcompanion.network.model.AnalyzeRequest
import com.example.aijournalcompanion.network.model.AnalyzeResponse
import com.example.aijournalcompanion.network.model.Emotion
import org.json.JSONException
import org.json.JSONObject

/** JSON handling is separate from transport and never coerces invalid field types. */
internal class AnalysisJsonCodec {
    fun encode(request: AnalyzeRequest): String =
        JSONObject().put("text", request.text.trim()).toString()

    fun decode(body: String): AnalyzeResponse {
        try {
            val json = JSONObject(body)
            require(json.keys().asSequence().toSet() == setOf("emotion", "advice"))
            val emotion = json.get("emotion")
            val advice = json.get("advice")
            require(emotion is String && advice is String)
            return AnalyzeResponse(Emotion.valueOf(emotion), advice.trim())
        } catch (exception: JSONException) {
            throw ApiException.InvalidResponse(exception)
        } catch (exception: IllegalArgumentException) {
            throw ApiException.InvalidResponse(exception)
        }
    }
}
