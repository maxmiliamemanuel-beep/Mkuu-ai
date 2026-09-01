package com.mkuu.ai.data.repository
import com.mkuu.ai.data.local.*
import com.mkuu.ai.data.network.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
class ChatRepository(private val db:ChatDatabase, private val api:AiApi) {
 fun messages(id:String):Flow<List<MessageEntity>> = db.dao().messages(id)
 suspend fun createConversation():ConversationEntity { val now=System.currentTimeMillis(); return ConversationEntity(UUID.randomUUID().toString(),"New conversation",now,now).also { db.dao().conversation(it) } }
 suspend fun ask(id:String,text:String,web:Boolean,onDelta:(String)->Unit):MessageEntity { val now=System.currentTimeMillis(); db.dao().message(MessageEntity(UUID.randomUUID().toString(),id,"user",text,createdAt=now)); db.dao().touch(id,now); val response=StringBuilder(); api.stream(ChatRequest(id,text,web)) { response.append(it); onDelta(response.toString()) }; return MessageEntity(UUID.randomUUID().toString(),id,"assistant",response.toString(),createdAt=System.currentTimeMillis()).also { db.dao().message(it) } }
 suspend fun generateImage(id:String,prompt:String):MessageEntity { val url=api.generateImage(prompt); return MessageEntity(UUID.randomUUID().toString(),id,"assistant","Generated image: $prompt",imageUrl=url,createdAt=System.currentTimeMillis()).also { db.dao().message(it); db.dao().touch(id,System.currentTimeMillis()) } }
}
