package com.example.codementor

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
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
    private var previousResponse: String = ""
    private lateinit var buttonSend: ImageButton
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
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // Tempo para estabelecer conexão
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // Tempo para ler os dados
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // Tempo para enviar os dados
            // finalmente resolvido erro de timeout desse chat
            .hostnameVerifier { _, _ -> true } // Ignora problemas com hostname
            .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        recyclerView = findViewById(R.id.recyclerViewChat)
        editText = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        adapter = ChatAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonSend.setOnClickListener {
            val userMessage = editText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                adapter.addMessage(ChatMessage(userMessage, true))
                recyclerView.scrollToPosition(adapter.itemCount - 1)
                editText.text.clear()
                sendMessageToApi(userMessage)
            }
        }
    }

    private fun sendMessageToApi(message: String) {
        val context = mapOf("previous_response" to previousResponse)
        val chatRequest = ChatRequest(
            context = context,
            messages = listOf(Message(role = "user", id = "1", content = message)),
            temperature = 0.8,
            max_tokens = 1500
        )

        lifecycleScope.launch {
            try {
                val response = api.sendMessage(chatRequest)
                if (response.choices.isNotEmpty()) {
                    val aiMessage = response.choices.firstOrNull()?.message?.content
                        ?: "Resposta vazia da IA"

                    // Atualize o contexto com a nova resposta da IA
                    previousResponse = aiMessage

                    // Adicione a mensagem ao adapter
                    adapter.addMessage(ChatMessage(aiMessage, false))
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                } else {
                    adapter.addMessage(ChatMessage("Erro: Nenhuma escolha retornada", false))
                }
            } catch (e: Exception) {
                adapter.addMessage(ChatMessage("Erro: ${e.localizedMessage}", false))
            }
        }
    }
}
