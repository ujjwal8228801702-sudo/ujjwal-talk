
package com.ujjwal.talk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val TOKEN_ENDPOINT =
    "https://ujjwal-talk.ujjwal8228801702.workers.dev"

private const val GROUP_ID = "UJJWAL10"

class MainActivity : ComponentActivity() {

    private var room: Room? = null
    private val http = OkHttpClient()

    private var connected by mutableStateOf(false)
    private var status by mutableStateOf("ग्रुप से जुड़ने के लिए Connect दबाएँ")
    private var busy by mutableStateOf(false)
    private var groupPassword by mutableStateOf("")
    private var memberName by mutableStateOf("Member")

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) connectGroup()
        else status = "माइक्रोफोन अनुमति जरूरी है"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UjjwalTalkScreen(
                connected = connected,
                status = status,
                busy = busy,
                password = groupPassword,
                onPassword = { groupPassword = it },
                name = memberName,
                onName = { memberName = it },
                onConnect = { ensurePermissionAndConnect() },
                onTalkStart = { setTalking(true) },
                onTalkEnd = { setTalking(false) }
            )
        }
    }

    private fun ensurePermissionAndConnect() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            connectGroup()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun connectGroup() {
        if (groupPassword.isBlank()) {
            status = "Group Password डालें"
            return
        }

        lifecycleScope.launch {
            busy = true
            status = "कनेक्ट हो रहा है…"

            try {
                val payload = JSONObject()
                    .put("groupId", GROUP_ID)
                    .put("password", groupPassword)
                    .put("identity", memberName.ifBlank { "Member" })

                val body = payload.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(TOKEN_ENDPOINT)
                    .post(body)
                    .build()

                val (httpCode, text) = withContext(Dispatchers.IO) {
                    http.newCall(request).execute().use { response ->
                        response.code to response.body?.string().orEmpty()
                    }
                }

                if (httpCode !in 200..299) {
                    error("Server HTTP $httpCode: $text")
                }

                val json = JSONObject(text)
                val wsUrl = json.getString("url")
                val token = json.getString("token")

                val newRoom = LiveKit.create(applicationContext)
                newRoom.connect(wsUrl, token)
                newRoom.localParticipant.setMicrophoneEnabled(false)

                room = newRoom
                connected = true
                status = "Connected • Ujjwal Permanent Group"

                ContextCompat.startForegroundService(
                    this@MainActivity,
                    Intent(this@MainActivity, TalkService::class.java)
                )

            } catch (e: Exception) {
                status = "कनेक्शन नहीं हुआ: ${e.message ?: "नेटवर्क जाँचें"}"
            } finally {
                busy = false
            }
        }
    }

    private fun setTalking(enabled: Boolean) {
        lifecycleScope.launch {
            try {
                room?.localParticipant?.setMicrophoneEnabled(enabled)
            } catch (_: Exception) {
                status = "माइक शुरू नहीं हो पाया"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Room lifecycle अभी पूरी तरह background service में स्थानांतरित नहीं है।
    }
}

@Composable
private fun UjjwalTalkScreen(
    connected: Boolean,
    status: String,
    busy: Boolean,
    password: String,
    onPassword: (String) -> Unit,
    name: String,
    onName: (String) -> Unit,
    onConnect: () -> Unit,
    onTalkStart: () -> Unit,
    onTalkEnd: () -> Unit
) {
    val navy = Color(0xFF06142F)
    val blue = Color(0xFF087CFF)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = blue,
            background = navy,
            surface = Color(0xFF10264D)
        )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF071A43), navy)
                    )
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Radio,
                    null,
                    tint = Color.Cyan,
                    modifier = Modifier.size(42.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    "Ujjwal Talk",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(Modifier.weight(1f))

                Icon(
                    Icons.Default.Settings,
                    null,
                    tint = Color.LightGray
                )
            }

            Text(
                "WALKIE TALKIE • PRIVATE VOICE GROUP",
                color = Color(0xFF8DC8FF),
                fontSize = 11.sp
            )

            Spacer(Modifier.height(28.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF102B52)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "PERMANENT GROUP",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "UJJWAL10  •  अधिकतम 10 सदस्य",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Text(
                        if (connected) "● Connected" else "● Disconnected",
                        color = if (connected) Color.Green
                        else Color(0xFFFFC04D)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = onName,
                        label = { Text("आपका नाम") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPassword,
                        label = { Text("Group Password") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = onConnect,
                        enabled = !busy && !connected,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (busy) "Connecting…"
                            else if (connected) "Connected"
                            else "Connect to Group"
                        )
                    }

                    Text(
                        status,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Surface(
                modifier = Modifier
                    .size(220.dp)
                    .pointerInput(connected) {
                        detectTapGestures(
                            onPress = {
                                if (connected) {
                                    onTalkStart()
                                    tryAwaitRelease()
                                    onTalkEnd()
                                }
                            }
                        )
                    },
                shape = CircleShape,
                color = if (connected) Color(0xFF0756B8)
                else Color(0xFF17345F),
                border = androidx.compose.foundation.BorderStroke(
                    5.dp,
                    Color.Cyan
                ),
                shadowElevation = 18.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(62.dp)
                        )

                        Text(
                            "PUSH TO TALK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "दबाकर बोलें",
                            color = Color.Yellow,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "बोलते समय बटन दबाकर रखें",
                color = Color(0xFF9CCBFF),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                if (connected)
                    "ऊपर गोल माइक को दबाकर रखें और बोलें"
                else
                    "पहले ग्रुप से कनेक्ट करें",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Ujjwal Talk • Version 1.0 • Hindi",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
