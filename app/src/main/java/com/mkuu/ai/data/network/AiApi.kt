package com.mkuu.ai.data.network
import com.mkuu.ai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.BufferedSource
import java.io.IOException

data class ChatRequest(val conversationId:String, val message:String, val webSearch:Boolean)
data class ImageResult(val url:String)
class AiApi(private val client: OkHttpClient = OkHttpClient.Builder().build()) {
 private val base = BuildConfig.BACKEND_URL.trimEnd('/')
 suspend fun stream(request: ChatRequest, onDelta:(String)->Unit): Unit = withContext(Dispatchers.IO) {
  val body = """{"conversationId":"${request.conversationId.json()}","message":"${request.message.json()}","webSearch":${request.webSearch}}""".toRequestBody("application/json".toMediaType())
  client.newCall(Request.Builder().url("$base/v1/chat/stream").post(body).build()).execute().use { response ->
   if (!response.isSuccessful) throw IOException("Service unavailable (${response.code})")
   val source=response.body?.source() ?: throw IOException("Empty response")
   readEvents(source,onDelta)
  }
 }
 suspend fun generateImage(prompt:String): String = withContext(Dispatchers.IO) {
  val body = """{"prompt":"${prompt.json()}"}""".toRequestBody("application/json".toMediaType())
  client.newCall(Request.Builder().url("$base/v1/images").post(body).build()).execute().use { response ->
   if (!response.isSuccessful) throw IOException("Image generation unavailable (${response.code})")
   val json=response.body?.string().orEmpty(); Regex("""\"url\":\"([^\"]+)\"""").find(json)?.groupValues?.get(1) ?: throw IOException("Invalid image response")
  }
 }
 private fun readEvents(source: BufferedSource, onDelta:(String)->Unit) { while(!source.exhausted()) { val line=source.readUtf8Line() ?: continue; if(line.startsWith("data: ")) { val value=line.removePrefix("data: "); if(value!="[DONE]") onDelta(value) } } }
 private fun String.json()=replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")
}
