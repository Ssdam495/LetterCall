package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences

@Composable
fun SettingsScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(AppPreferences.getApiKey(context)) }
    var serverIp by remember { mutableStateOf(AppPreferences.getServerIp(context)) }
    var isError by remember { mutableStateOf(false) }

    val geometricGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF87CEEB), // Soft Sky Blue
            Color(0xFFBAE6FD), // Sky tint
            Color(0xFFE0F2FE)  // Soft Ice Blue
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(geometricGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Friendly Icon Header with frosted border
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.55f))
                    .border(2.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Settings",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "إعدادات مكالمة الحروف",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0C4A6E), // text-sky-900
                textAlign = TextAlign.Center
            )

            Text(
                text = "أدخل مفتاح Gemini API لتفعيل الذكاء الاصطناعي والتحدث مع الحروف",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0369A1), // text-sky-700
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Frosted Settings Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.White, RoundedCornerShape(24.dp))
                    .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x1A0284C7), spotColor = Color(0x260C4A6E))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            isError = false
                        },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Key Icon",
                                tint = Color(0xFF0284C7)
                            )
                        },
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text("يرجى إدخال مفتاح API للمتابعة", color = Color(0xFFD32F2F))
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { serverIp = it },
                        label = { Text("عنوان الخادم Server IP (اختياري)") },
                        placeholder = { Text("http://192.168.1.1:8000") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = "Server IP Icon",
                                tint = Color(0xFF64748B)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_ip_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy note
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔒 خصوصية وأمان طفلك:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C4A6E)
                    )
                    Text(
                        text = "يتم حفظ المفتاح محلياً على جهازك فقط. الكاميرا والميكروفون يعملان محلياً ولا يتم تسجيل أي فيديو أو صوت خارجي.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (apiKey.isBlank()) {
                        isError = true
                    } else {
                        AppPreferences.saveSettings(context, apiKey, serverIp)
                        onSaved()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF0369A1))
                    .testTag("save_settings_button")
            ) {
                Text(
                    text = "حفظ ومتابعة 🚀",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
