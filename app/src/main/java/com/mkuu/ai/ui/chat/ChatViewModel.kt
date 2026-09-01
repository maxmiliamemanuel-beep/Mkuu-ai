package com.mkuu.ai.ui.chat
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mkuu.ai.data.local.*
import com.mkuu.ai.data.network.AiApi
import com.mkuu.ai.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(val conversationId:String?=null,val messages:List<MessageEntity> = emptyList(),val draft:String="",val streaming:String="",val loading:Boolean=false,val error:String?=null,val webSearch:Boolean=false)
class ChatViewModel(app:Application):AndroidViewModel(app) { private val repo=ChatRepository(ChatDatabase.create(app),AiApi()); private val _state=MutableStateFlow(ChatUiState()); val state=_state.asStateFlow(); private var messagesJob:Job?=null; private var generation:Job?=null
 fun newConversation()=viewModelScope.launch { select(repo.createConversation().id) }
 fun select(id:String) { messagesJob?.cancel(); _state.update { it.copy(conversationId=id,messages=emptyList(),streaming="",error=null) }; messagesJob=viewModelScope.launch { repo.messages(id).collect { list->_state.update { s->s.copy(messages=list) } } } }
 fun updateDraft(value:String)=_state.update { it.copy(draft=value) }; fun toggleWeb()=_state.update { it.copy(webSearch=!it.webSearch) }
 fun send() { val s=_state.value; if(s.loading||s.draft.isBlank()) return; generation=viewModelScope.launch { val id=s.conversationId ?: repo.createConversation().id.also(::select); val text=s.draft; _state.update { it.copy(draft="",loading=true,error=null,streaming="") }; runCatching { repo.ask(id,text,s.webSearch) { delta->_state.update { it.copy(streaming=delta) } } }.onFailure { _state.update { it.copy(error="Unable to reach MKUU AI. Check your connection and try again.") } }; _state.update { it.copy(loading=false,streaming="") } } }
 fun generateImage() { val s=_state.value; if(s.loading||s.draft.isBlank()) return; generation=viewModelScope.launch { val id=s.conversationId ?: repo.createConversation().id.also(::select); val prompt=s.draft; _state.update { it.copy(draft="",loading=true,error=null) }; runCatching { repo.generateImage(id,prompt) }.onFailure { _state.update { it.copy(error="Image generation failed. Try again.") } }; _state.update { it.copy(loading=false) } } }
 fun stop(){ generation?.cancel(); _state.update { it.copy(loading=false,streaming="") } }
 fun regenerate(){ _state.value.messages.lastOrNull { it.role=="user" }?.let { updateDraft(it.content); send() } }
}
