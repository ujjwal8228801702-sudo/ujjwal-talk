package com.ujjwal.talk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UjjwalVoiceScreen() }
    }
}

@Composable
private fun UjjwalVoiceScreen() {
    var phone by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("अपना मोबाइल नंबर डालकर यूज़र खोजें") }

    val navy = Color(0xFF071326)
    val panel = Color(0xFF11243D)
    val cyan = Color(0xFF35D6E8)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = cyan,
            background = navy,
            surface = panel,
            onSurface = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF102B4B), navy, Color(0xFF050B16))
                    )
                )
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = Color(0xFF123E5B)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("UV", color = cyan, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(Modifier.width(13.dp))
                    Column {
                        Text("UJJWAL VOICE", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("PRIVATE INTERNET CALLING", color = Color(0xFF8FAFCB), fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(shape = CircleShape, color = Color(0xFF173A37)) {
                        Text(" BETA ", color = Color(0xFF7BE3B0), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
                    }
                }

                Spacer(Modifier.height(38.dp))

                Box(
                    modifier = Modifier
                        .size(142.dp)
                        .background(Color(0xFF123A58), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .background(Color(0xFF176080), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☎", color = Color.White, fontSize = 55.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("आवाज़ से जुड़ें", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text(
                    "अपने Ujjwal Voice अकाउंट वाले व्यक्ति का नंबर डालें",
                    color = Color(0xFFADC1D7),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(30.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = panel.copy(alpha = 0.96f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("नया कॉल", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("मोबाइल नंबर से खोजें", color = Color(0xFF9CB2C9), fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { input -> phone = input.filter { it.isDigit() }.take(10) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("10 अंकों का मोबाइल नंबर") },
                            leadingIcon = { Text("+91", color = cyan, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyan,
                                unfocusedBorderColor = Color(0xFF36506A),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = cyan
                            )
                        )

                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                notice = if (phone.length == 10) {
                                    "नंबर सही है। यूज़र खोजने के लिए सर्वर कनेक्शन अगला चरण है।"
                                } else {
                                    "कृपया 10 अंकों का सही मोबाइल नंबर डालें।"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text("यूज़र खोजें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(notice, color = Color(0xFFB9D3E8), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureLabel("🔒", "निजी कॉल")
                    FeatureLabel("🎙️", "केवल आवाज़")
                    FeatureLabel("🇮🇳", "हिंदी")
                }

                Spacer(Modifier.weight(1f))
                Text(
                    "अभी शुरुआती स्क्रीन तैयार है • कॉल सर्वर और अकाउंट लॉगिन अगले चरण में जुड़ेंगे",
                    color = Color(0xFF71869D),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureLabel(symbol: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(symbol, fontSize = 19.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color(0xFF9CB2C9), fontSize = 11.sp)
    }
}
