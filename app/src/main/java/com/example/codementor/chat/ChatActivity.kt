package com.example.codementor.chat

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codementor.R
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    @POST("/v1/chat/completions")
    suspend fun sendMessage(@Body body: ChatRequest): ChatResponse
}

data class ChatRequest(
    val context: Map<String, String>,
    val messages: List<Message>,
    val temperature: Double,
    val max_tokens: Int
)

data class Message(val role: String, val id: String, val content: String)

data class ChatResponse(
    val choices: List<Choice>
)
data class Choice(
    val index: Int,
    val message: ChoiceMessage
)
data class ChoiceMessage(
    val role: String,
    val content: String
)

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var editText: EditText
    private lateinit var buttonSend: ImageButton
    private val messageHistory = mutableListOf<Message>() // Lista para armazenar o histórico de mensagens

    private val api: ChatApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://fast-radically-minnow.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ChatApi::class.java)
    }
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
        recyclerView = findViewById(R.id.recyclerViewChat)
        editText = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        adapter = ChatAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonSend.setOnClickListener {
            val userMessage = editText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                val userMessageObject = Message(role = "user", id = generateMessageId(), content = userMessage)
                messageHistory.add(userMessageObject) // Adiciona ao histórico
                adapter.addMessage(ChatMessage(userMessage, true))
                recyclerView.scrollToPosition(adapter.itemCount - 1)
                editText.text.clear()
                sendMessageToApi()
            }
        }
    }

    private fun sendMessageToApi() {
        // Mensagem temporária pro feedback visual
        val typingMessage = ChatMessage("...", isUser = false)
        adapter.addMessage(typingMessage)
        recyclerView.scrollToPosition(adapter.itemCount - 1)

        val chatRequest = ChatRequest(
            context = mapOf("previous_response" to getLastResponse()),
            messages = messageHistory,
            temperature = 0.8,
            max_tokens = 1500
        )

        lifecycleScope.launch {
            try {
                val response = api.sendMessage(chatRequest)
                // Remover aqueles ... da mensagem de feedback visual
                adapter.removeMessage(typingMessage)

                if (response.choices.isNotEmpty()) {
                    val aiMessage = response.choices.firstOrNull()?.message?.content
                        ?: "Resposta vazia da IA"

                    // Adiciona a resposta da IA ao histórico
                    val aiMessageObject = Message(role = "assistant", id = generateMessageId(), content = aiMessage)
                    messageHistory.add(aiMessageObject)

                    // Adiciona a mensagem ao adapter
                    adapter.addMessage(ChatMessage(aiMessage, false))
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                } else {
                    adapter.addMessage(ChatMessage("Erro: Nenhuma escolha retornada", false))
                }
            } catch (e: Exception) {
                // Remove a mensagem de feedback e adiciona uma mensagem de erro
                adapter.removeMessage(typingMessage)
                adapter.addMessage(ChatMessage("Erro: ${e.localizedMessage}", false))
            }
        }
    }


    private fun getLastResponse(): String {
        // Retorna o conteúdo da última mensagem da IA, ou uma string vazia se não houver
        return messageHistory.lastOrNull { it.role == "assistant" }?.content ?: ""
    }

    private fun generateMessageId(): String {
        // Gera um ID único para cada mensagem (pode ser adaptado conforme necessário)
        return System.currentTimeMillis().toString()
    }
}

