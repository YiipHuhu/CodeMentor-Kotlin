package com.example.codementor.chat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
    val temperature: Double = 0.8,
    val max_tokens: Int = 1500
)

data class Message(val role: String, val id: String, val content: String)
data class ChatResponse(val choices: List<Choice>)
data class Choice(val message: ChoiceMessage)
data class ChoiceMessage(val content: String)

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var editText: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var promptContainer: LinearLayout
    private lateinit var messageHistory: MutableList<Message>
    private lateinit var promptContainerTwo: LinearLayout

    private val api: ChatApi by lazy { createChatApi() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        initViews()
        setupRecyclerView()
        setupButtons()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewChat)
        editText = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        promptContainer = findViewById(R.id.promptContainer)
        promptContainerTwo = findViewById(R.id.promptContainerTwo)
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        messageHistory = mutableListOf()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        buttonSend.setOnClickListener {
            val userInput = editText.text.toString().trim()
            if (userInput.isNotEmpty()) sendMessage(userInput)
        }

        listOf(
            R.id.buttonResuma to "Preciso que você crie um resumo para mim, por favor.",
            R.id.buttonExercicios to "Quais exercícios você sugere?",
            R.id.buttoncodigo to "Preciso de ajuda com um código, você pode me ajudar?"
        ).forEach { (buttonId, message) ->
            findViewById<Button>(buttonId).setOnClickListener { sendMessage(message) }
        }
    }

    private fun sendMessage(content: String) {
        val userMessage = createMessage("user", content)
        addMessageToUI(userMessage, true)
        hidePromptContainer()
        fetchResponseFromApi(content)
    }

    private fun fetchResponseFromApi(prompt: String) {
        val typingMessage = addMessageToUI("...", false)
        val chatRequest = ChatRequest(
            context = mapOf("previous_response" to getLastResponse()),
            messages = messageHistory
        )

        lifecycleScope.launch {
            try {
                val response = api.sendMessage(chatRequest)
                removeMessageFromUI(typingMessage)
                response.choices.firstOrNull()?.message?.content?.let {
                    val aiMessage = createMessage("assistant", it)
                    addMessageToUI(aiMessage, false)
                } ?: addMessageToUI("Erro: Nenhuma resposta obtida", false)
            } catch (e: Exception) {
                removeMessageFromUI(typingMessage)
                addMessageToUI("Erro: ${e.localizedMessage}", false)
            }
        }
    }

    private fun createMessage(role: String, content: String): Message {
        val message = Message(role, System.currentTimeMillis().toString(), content)
        messageHistory.add(message)
        return message
    }

    private fun addMessageToUI(message: Any, isUser: Boolean): ChatMessage {
        val chatMessage = when (message) {
            is Message -> ChatMessage(message.content, isUser)
            is String -> ChatMessage(message, isUser)
            else -> throw IllegalArgumentException("Tipo inválido para mensagem")
        }
        adapter.addMessage(chatMessage)
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        return chatMessage
    }

    private fun removeMessageFromUI(chatMessage: ChatMessage) {
        adapter.removeMessage(chatMessage)
    }

    private fun getLastResponse(): String =
        messageHistory.lastOrNull { it.role == "assistant" }?.content.orEmpty()

    private fun hidePromptContainer() {
        promptContainer.visibility = android.view.View.GONE
        promptContainerTwo.visibility = android.view.View.GONE
    }

    private fun createChatApi(): ChatApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://fast-radically-minnow.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ChatApi::class.java)
    }
}
