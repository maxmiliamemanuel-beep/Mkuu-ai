package com.mkuu.ai
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkuu.ai.data.local.MessageEntity
import coil.compose.AsyncImage
import com.mkuu.ai.ui.chat.ChatViewModel
import com.mkuu.ai.ui.theme.MkuuTheme

class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge();setContent { MkuuTheme { val vm:ChatViewModel=viewModel(); ChatScreen(vm) } }} }
@Composable private fun ChatScreen(vm:ChatViewModel){ val s by vm.state.collectAsState(); val list=rememberLazyListState(); LaunchedEffect(s.messages.size,s.streaming){ if(s.messages.isNotEmpty()||s.streaming.isNotEmpty()) list.animateScrollToItem((s.messages.size + if(s.streaming.isNotEmpty()) 1 else 0).coerceAtLeast(0)) }; Scaffold(topBar={TopAppBar(title={Column { Text("MKUU AI"); Text(if(s.webSearch) "Web-aware assistant" else "Private, thoughtful assistance",style=MaterialTheme.typography.labelSmall) }},actions={IconButton(vm::toggleWeb){Icon(Icons.Default.Public,if(s.webSearch)"Web search on" else "Web search off",tint=if(s.webSearch)MaterialTheme.colorScheme.primary else LocalContentColor.current)};IconButton(vm::newConversation){Icon(Icons.Default.Add,"New conversation")}})},bottomBar={Composer(s.draft,s.loading,s.webSearch,vm::updateDraft,vm::send,vm::generateImage,vm::stop,vm::toggleWeb)}) { pad -> Box(Modifier.padding(pad).fillMaxSize()) { if(s.messages.isEmpty()&&!s.loading) EmptyState(vm::newConversation); LazyColumn(state=list,contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) { items(s.messages.size){ Message(s.messages[it], vm::regenerate) }; if(s.streaming.isNotEmpty()) item { Message(MessageEntity("live","","assistant",s.streaming,createdAt=0),vm::regenerate) }; s.error?.let { item { AssistChip(onClick=vm::send,label={Text(it)},leadingIcon={Icon(Icons.Default.Error,"Error")}) } } } } }
@Composable private fun EmptyState(new:()->Unit)=Column(Modifier.fillMaxSize().padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){ Surface(shape=RoundedCornerShape(28.dp),color=MaterialTheme.colorScheme.secondaryContainer){Icon(Icons.Default.AutoAwesome,null,Modifier.padding(22.dp),tint=MaterialTheme.colorScheme.onSecondaryContainer)};Spacer(Modifier.height(20.dp));Text("Your thinking space",style=MaterialTheme.typography.headlineSmall);Text("Ask anything, create images, or search the web when freshness matters.",style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(18.dp));Button(new){Text("Start a conversation")}}
@Composable private fun Message(m:MessageEntity,regenerate:()->Unit){val mine=m.role=="user";val clipboard=LocalClipboardManager.current;Column(Modifier.fillMaxWidth(),horizontalAlignment=if(mine)Alignment.End else Alignment.Start){Text(if(mine)"YOU" else "MKUU",style=MaterialTheme.typography.labelSmall);Surface(color=if(mine)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp)){m.imageUrl?.let { AsyncImage(model=it,contentDescription="Generated image",modifier=Modifier.fillMaxWidth().heightIn(max=360.dp).clip(RoundedCornerShape(12.dp))) };Markdown(m.content);if(!mine) Row { TextButton({clipboard.setText(AnnotatedString(m.content))}){Icon(Icons.Default.ContentCopy,null);Text(" Copy")};TextButton(regenerate){Icon(Icons.Default.Refresh,null);Text(" Regenerate")}}}}}
@Composable private fun Markdown(value:String){ val code=value.startsWith("```")&&value.endsWith("```");Text(if(code)value.removePrefix("```").removeSuffix("```").trim() else value,fontFamily=if(code)FontFamily.Monospace else FontFamily.Default)}
@Composable private fun Composer(draft:String,loading:Boolean,web:Boolean,change:(String)->Unit,send:()->Unit,image:()->Unit,stop:()->Unit,toggle:()->Unit)=Surface(tonalElevation=4.dp){Row(Modifier.navigationBarsPadding().padding(12.dp),verticalAlignment=Alignment.Bottom){IconButton(image){Icon(Icons.Default.Image,"Generate image")};IconButton(toggle){Icon(Icons.Default.Public,"Search web")};OutlinedTextField(draft,change,Modifier.weight(1f),placeholder={Text("Message MKUU AI")},maxLines=5,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Default));Spacer(Modifier.width(8.dp));FilledIconButton(if(loading)stop else send){Icon(if(loading)Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,if(loading)"Stop generating" else "Send")}}}
