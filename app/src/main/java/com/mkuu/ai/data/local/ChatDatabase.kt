package com.mkuu.ai.data.local
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations") data class ConversationEntity(@PrimaryKey val id: String, val title: String, val createdAt: Long, val updatedAt: Long)
@Entity(tableName = "messages", foreignKeys = [ForeignKey(entity = ConversationEntity::class, parentColumns = ["id"], childColumns = ["conversationId"], onDelete = ForeignKey.CASCADE)], indices = [Index("conversationId")]) data class MessageEntity(@PrimaryKey val id: String, val conversationId: String, val role: String, val content: String, val sourcesJson: String = "", val imageUrl: String? = null, val createdAt: Long)
@Dao interface ChatDao { @Query("SELECT * FROM conversations ORDER BY updatedAt DESC") fun conversations(): Flow<List<ConversationEntity>>; @Query("SELECT * FROM messages WHERE conversationId=:id ORDER BY createdAt") fun messages(id: String): Flow<List<MessageEntity>>; @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun conversation(item: ConversationEntity); @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun message(item: MessageEntity); @Query("UPDATE conversations SET updatedAt=:time WHERE id=:id") suspend fun touch(id:String,time:Long) }
@Database(entities=[ConversationEntity::class, MessageEntity::class], version=1, exportSchema=false) abstract class ChatDatabase: RoomDatabase() { abstract fun dao(): ChatDao; companion object { fun create(context: Context)=Room.databaseBuilder(context, ChatDatabase::class.java,"mkuu.db").fallbackToDestructiveMigration().build() } }
