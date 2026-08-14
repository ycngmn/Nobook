package com.ycngmn.nobook.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * BYOK (Bring-Your-Own-Key) AI providers supported by the sidebar. The user
 * supplies and stores their own API key locally; Nobook never bundles or
 * shares any key, and no Facebook session data is read or forwarded.
 */
enum class AiProvider(val label: String) {
    OPENAI("OpenAI (GPT)"),
    GEMINI("Google Gemini"),
    ANTHROPIC("Anthropic Claude"),
    GROQ("Groq (Llama / Mixtral)")
}

data class ChatMessage(val role: String, val content: String)

private const val PREFS_NAME = "nobook_prefs"
private const val KEY_PROVIDER = "ai_provider"
private const val KEY_API_KEY_PREFIX = "ai_api_key_"
private const val KEY_MODEL_PREFIX = "ai_model_"

fun defaultModelFor(provider: AiProvider): String = when (provider) {
    AiProvider.OPENAI -> "gpt-4o-mini"
    AiProvider.GEMINI -> "gemini-1.5-flash"
    AiProvider.ANTHROPIC -> "claude-3-5-sonnet-20241022"
    AiProvider.GROQ -> "llama-3.3-70b-versatile"
}

/**
 * Holds chat history for the current sidebar session and performs the
 * BYOK HTTP call to whichever provider the user selected. Uses plain
 * [HttpURLConnection] (no extra network dependency) on [Dispatchers.IO].
 */
class AiChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    /** Sends [userText] to [provider] using [apiKey]/[model], appending the reply to history. */
    fun sendMessage(provider: AiProvider, apiKey: String, model: String, userText: String) {
        if (userText.isBlank()) return
        if (apiKey.isBlank()) {
            _error.value = "Chua nhap API key cho ${'$'}{provider.label}. Bam 'API key' de cau hinh."
            return
        }
        val history = _messages.value + ChatMessage("user", userText)
        _messages.value = history
        _isLoading.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { callAi(provider, apiKey, model, history) }
            }
            _isLoading.value = false
            result.onSuccess { reply ->
                _messages.value = _messages.value + ChatMessage("assistant", reply)
            }.onFailure { e ->
                _error.value = e.message ?: "Loi khong xac dinh khi goi API."
            }
        }
    }

    private fun callAi(provider: AiProvider, apiKey: String, model: String, history: List<ChatMessage>): String =
        when (provider) {
            AiProvider.OPENAI -> callOpenAiCompatible("https://api.openai.com/v1/chat/completions", apiKey, model, history)
            AiProvider.GROQ -> callOpenAiCompatible("https://api.groq.com/openai/v1/chat/completions", apiKey, model, history)
            AiProvider.ANTHROPIC -> callAnthropic(apiKey, model, history)
            AiProvider.GEMINI -> callGemini(apiKey, model, history)
        }

    private fun postJson(endpoint: String, headers: Map<String, String>, body: JSONObject): Pair<Int, String> {
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        conn.doOutput = true
        conn.connectTimeout = 20000
        conn.readTimeout = 30000
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        return code to text
    }

    private fun callOpenAiCompatible(endpoint: String, apiKey: String, model: String, history: List<ChatMessage>): String {
        val messagesArray = JSONArray()
        history.forEach { m ->
            messagesArray.put(JSONObject().apply {
                put("role", m.role)
                put("content", m.content)
            })
        }
        val body = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
        }
        val (code, text) = postJson(endpoint, mapOf("Authorization" to "Bearer $apiKey"), body)
        if (code !in 200..299) throw RuntimeException("HTTP $code: $text")
        return JSONObject(text)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun callAnthropic(apiKey: String, model: String, history: List<ChatMessage>): String {
        val messagesArray = JSONArray()
        history.forEach { m ->
            messagesArray.put(JSONObject().apply {
                put("role", m.role)
                put("content", m.content)
            })
        }
        val body = JSONObject().apply {
            put("model", model)
            put("max_tokens", 1024)
            put("messages", messagesArray)
        }
        val (code, text) = postJson(
            "https://api.anthropic.com/v1/messages",
            mapOf("x-api-key" to apiKey, "anthropic-version" to "2023-06-01"),
            body
        )
        if (code !in 200..299) throw RuntimeException("HTTP $code: $text")
        return JSONObject(text).getJSONArray("content").getJSONObject(0).getString("text")
    }

    private fun callGemini(apiKey: String, model: String, history: List<ChatMessage>): String {
        val contents = JSONArray()
        history.forEach { m ->
            contents.put(JSONObject().apply {
                put("role", if (m.role == "assistant") "model" else "user")
                put("parts", JSONArray().put(JSONObject().put("text", m.content)))
            })
        }
        val body = JSONObject().apply { put("contents", contents) }
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val (code, text) = postJson(endpoint, emptyMap(), body)
        if (code !in 200..299) throw RuntimeException("HTTP $code: $text")
        return JSONObject(text)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }
}

/** FAB that opens the AI Assistant [ModalBottomSheet]. Place inside a [androidx.compose.foundation.layout.Box] overlay. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSidebarFab(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    var provider by rememberSaveable {
        mutableStateOf(
            runCatching { AiProvider.valueOf(prefs.getString(KEY_PROVIDER, AiProvider.OPENAI.name)!!) }
                .getOrDefault(AiProvider.OPENAI)
        )
    }
    var apiKey by rememberSaveable { mutableStateOf(prefs.getString(KEY_API_KEY_PREFIX + provider.name, "") ?: "") }
    var model by rememberSaveable {
        mutableStateOf(prefs.getString(KEY_MODEL_PREFIX + provider.name, defaultModelFor(provider)) ?: defaultModelFor(provider))
    }

    fun reloadForProvider(p: AiProvider) {
        apiKey = prefs.getString(KEY_API_KEY_PREFIX + p.name, "") ?: ""
        model = prefs.getString(KEY_MODEL_PREFIX + p.name, defaultModelFor(p)) ?: defaultModelFor(p)
    }

    FloatingActionButton(onClick = { sheetOpen = true }, modifier = modifier) {
        Text("AI", style = MaterialTheme.typography.titleMedium)
    }

    if (sheetOpen) {
        val vm: AiChatViewModel = viewModel()
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { sheetOpen = false }, sheetState = sheetState) {
            AiChatContent(
                vm = vm,
                provider = provider,
                apiKey = apiKey,
                model = model,
                onOpenSettings = { settingsOpen = true }
            )
        }
    }

    if (settingsOpen) {
        AiSettingsDialog(
            provider = provider,
            apiKey = apiKey,
            model = model,
            onProviderChange = { p ->
                provider = p
                reloadForProvider(p)
            },
            onApiKeyChange = { apiKey = it },
            onModelChange = { model = it },
            onSave = {
                prefs.edit()
                    .putString(KEY_PROVIDER, provider.name)
                    .putString(KEY_API_KEY_PREFIX + provider.name, apiKey)
                    .putString(KEY_MODEL_PREFIX + provider.name, model)
                    .apply()
                settingsOpen = false
            },
            onDismiss = { settingsOpen = false }
        )
    }
}

@Composable
private fun AiChatContent(
    vm: AiChatViewModel,
    provider: AiProvider,
    apiKey: String,
    model: String,
    onOpenSettings: () -> Unit
) {
    val messages by vm.messages.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp, max = 560.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Assistant · ${'$'}{provider.label}", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onOpenSettings) { Text("API key") }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(msg.content, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhap cau hoi...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank()) {
                        vm.clearError()
                        vm.sendMessage(provider, apiKey, model, input)
                        input = ""
                    }
                })
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = {
                if (input.isNotBlank()) {
                    vm.clearError()
                    vm.sendMessage(provider, apiKey, model, input)
                    input = ""
                }
            }) { Text("Gui") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiSettingsDialog(
    provider: AiProvider,
    apiKey: String,
    model: String,
    onProviderChange: (AiProvider) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var localModel by remember(model) { mutableStateOf(model) }
    var localKey by remember(apiKey) { mutableStateOf(apiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cau hinh AI (BYOK)") },
        text = {
            Column {
                Text("Nha cung cap", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = provider.label,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuDefaults.let { }
                    androidx.compose.material3.ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AiProvider.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.label) },
                                onClick = {
                                    onProviderChange(p)
                                    localModel = defaultModelFor(p)
                                    onModelChange(defaultModelFor(p))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = localKey,
                    onValueChange = { localKey = it; onApiKeyChange(it) },
                    label = { Text("API key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = localModel,
                    onValueChange = { localModel = it; onModelChange(it) },
                    label = { Text("Model") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "API key duoc luu cuc bo tren may (SharedPreferences), khong gui ve server nao khac ngoai dung nha cung cap AI ban chon.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Luu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huy") } }
    )
}
