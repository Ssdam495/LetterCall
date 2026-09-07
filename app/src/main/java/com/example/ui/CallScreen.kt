package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.audio.SpeechHelper
import com.example.data.AppPreferences
import com.example.data.ChatMessage
import com.example.data.GeminiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CallStatus {
    RINGING,
    TALKING,
    LISTENING,
    THINKING
}

@Composable
fun CallScreen(
    letterChar: String,
    letterName: String,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var callStatus by remember { mutableStateOf(CallStatus.RINGING) }
    var letterSubtitle by remember { mutableStateOf("جارٍ الاتصال بحرف $letterChar...") }
    var childSpokenText by remember { mutableStateOf("") }
    var callSeconds by remember { mutableIntStateOf(0) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }

    val chatHistory = remember { mutableStateListOf<ChatMessage>() }
    val apiKey = remember { AppPreferences.getApiKey(context) }
    val geminiService = remember { GeminiService(apiKey) }

    // Speech helper for STT and TTS
    var speechHelperRef by remember { mutableStateOf<SpeechHelper?>(null) }
    val speechHelper = remember {
        SpeechHelper(
            context = context,
            onTtsSpeakingChanged = { speaking ->
                isSpeaking = speaking
                if (speaking) {
                    callStatus = CallStatus.TALKING
                }
            },
            onSpeechRecognized = { recognizedText ->
                if (!isMuted) {
                    childSpokenText = recognizedText
                    callStatus = CallStatus.THINKING
                    letterSubtitle = "أفكر في إجابة رائعة..."
                    chatHistory.add(ChatMessage("user", recognizedText))

                    coroutineScope.launch {
                        val reply = geminiService.sendMessage(
                            letterName = letterName,
                            letterChar = letterChar,
                            history = chatHistory.toList(),
                            userMessage = recognizedText
                        )
                        chatHistory.add(ChatMessage("model", reply))
                        letterSubtitle = reply
                        speechHelperRef?.speak(reply)
                    }
                }
            },
            onListeningStateChanged = { listening ->
                if (listening && !isSpeaking && !isMuted) {
                    callStatus = CallStatus.LISTENING
                }
            },
            onErrorOccurred = { _ ->
                if (callStatus == CallStatus.LISTENING && !isMuted) {
                    coroutineScope.launch {
                        delay(1500)
                        if (callStatus == CallStatus.LISTENING && !isSpeaking && !isMuted) {
                            speechHelperRef?.startListening()
                        }
                    }
                }
            }
        ).also {
            speechHelperRef = it
        }
    }

    // Call duration timer
    LaunchedEffect(callStatus) {
        while (true) {
            delay(1000)
            if (callStatus != CallStatus.RINGING) {
                callSeconds++
            }
        }
    }

    // Call start lifecycle: Ringing -> Letter Greeting
    LaunchedEffect(Unit) {
        speechHelper.playRingingTone {
            callStatus = CallStatus.THINKING
            letterSubtitle = "الحرف يتصل بك الآن..."
            coroutineScope.launch {
                val greeting = geminiService.getLetterGreeting(letterName, letterChar)
                chatHistory.add(ChatMessage("model", greeting))
                letterSubtitle = greeting
                speechHelper.speak(greeting)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.release()
        }
    }

    // Geometric Balance: Sky to Ice-blue vertical gradient background
    val geometricBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF87CEEB), // Soft Sky Blue
            Color(0xFFBAE6FD), // Sky light tint
            Color(0xFFE0F2FE)  // Soft Ice Blue
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(geometricBackground)
    ) {
        // Geometric subtle floating accents in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle ambient circular glow
            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = 160.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.44f)
            )
        }

        // Column containing Header, Body, and Controls
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==================== TOP BAR: Geometric Balance Status & Timer ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Info with pulsing indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PulsingStatusDot(isActive = callStatus != CallStatus.RINGING)
                    Text(
                        text = if (callStatus == CallStatus.RINGING) "اتصال بالحرف..." else "مكالمة جارية: $letterName",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0C4A6E) // text-sky-900
                    )
                }

                // Monospace Call Timer
                Text(
                    text = if (callStatus == CallStatus.RINGING) "00:00" else formatDuration(callSeconds),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0C4A6E) // text-sky-900
                )
            }

            // ==================== MAIN CONTENT AREA ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Self Camera Preview: Top Corner with Geometric 3D Border and shadow
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                ) {
                    ChildCameraPreview(
                        useFrontCamera = useFrontCamera
                    )
                }

                // Center Letter & Dialogue Subtitle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Character: The 3D Letter with Geometric Aura
                    GeometricTalkingLetter(
                        letterChar = letterChar,
                        isSpeaking = isSpeaking,
                        callStatus = callStatus
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // AI Transcription / Subtitle: Glass Pill
                    AnimatedVisibility(
                        visible = letterSubtitle.isNotBlank(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = Color(0x1A0284C7),
                                    spotColor = Color(0x260C4A6E)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White.copy(alpha = 0.55f))
                                .border(
                                    width = 1.5.dp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .testTag("ai_transcription_pill"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = letterSubtitle,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D3E50), // #2D3E50 from design
                                    textAlign = TextAlign.Center,
                                    lineHeight = 26.sp
                                )

                                if (callStatus == CallStatus.LISTENING && childSpokenText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "أنت: $childSpokenText",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0284C7),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Badge (Speaking / Listening)
                    GeometricStatusBadge(callStatus = callStatus, isMuted = isMuted)
                }
            }

            // ==================== CALL CONTROLS FOOTER ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Mute / Unmute Control (Glass Circle)
                    GlassCircleButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "كتم الميكروفون",
                        isActive = isMuted,
                        onClick = {
                            isMuted = !isMuted
                            if (isMuted) {
                                speechHelper.stopListening()
                            } else {
                                speechHelper.startListening()
                            }
                        },
                        modifier = Modifier.testTag("mute_button")
                    )

                    Spacer(modifier = Modifier.width(28.dp))

                    // 2. End Call Button: 3D Red Button (.btn-3d-red)
                    Neumorphic3DEndCallButton(
                        onClick = {
                            speechHelper.release()
                            onEndCall()
                        },
                        modifier = Modifier.testTag("end_call_button")
                    )

                    Spacer(modifier = Modifier.width(28.dp))

                    // 3. Camera Flip / Toggle (Glass Circle)
                    GlassCircleButton(
                        icon = Icons.Default.Cameraswitch,
                        contentDescription = "تبديل الكاميرا",
                        isActive = false,
                        onClick = {
                            useFrontCamera = !useFrontCamera
                        },
                        modifier = Modifier.testTag("camera_switch_button")
                    )
                }

                // Bottom Home Indicator
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .width(128.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF0C4A6E).copy(alpha = 0.20f))
                )
            }
        }
    }
}

/**
 * Pulsing Red Status Indicator Dot
 */
@Composable
fun PulsingStatusDot(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (isActive) Color(0xFFEF4444).copy(alpha = alpha) else Color(0xFF0284C7))
    )
}

/**
 * Child Camera Preview: Rounded 3D geometric card matching design .camera-circle
 */
@Composable
fun ChildCameraPreview(
    useFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val cornerShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .width(106.dp)
            .height(144.dp)
            .shadow(
                elevation = 14.dp,
                shape = cornerShape,
                ambientColor = Color(0x26000000),
                spotColor = Color(0x33000000)
            )
            .clip(cornerShape)
            .background(Color(0xFFE2E8F0)) // slate-200
            .border(4.dp, Color.White, cornerShape)
            .testTag("child_camera_preview"),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = if (useFrontCamera && cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Failed to bind camera", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = if (useFrontCamera && cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Camera update error", e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "الكاميرا",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الكاميرا",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * The Letter Mascot in "Geometric Balance" Theme:
 * - Behind: Circular aura in white/40 with 4px white border
 * - Letter text in vibrant #FF6B6B with 3D text shadow in #FF9494
 * - Animated cartoon eyes and synced talking mouth
 */
@Composable
fun GeometricTalkingLetter(
    letterChar: String,
    isSpeaking: Boolean,
    callStatus: CallStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "letter_mascot_anim")

    // Idle breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    // Talking bounce & wiggle
    val talkingWobble by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talking_wobble"
    )

    // Talking mouth open/close cycle
    val mouthAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouth_anim"
    )

    val currentScale = if (isSpeaking) breathingScale * 1.06f else breathingScale
    val currentRotation = if (isSpeaking) talkingWobble else 0f

    Box(
        modifier = modifier
            .size(230.dp)
            .scale(currentScale)
            .rotate(currentRotation)
            .testTag("animated_letter_mascot"),
        contentAlignment = Alignment.Center
    ) {
        // Geometric Circular Aura: w-48 h-48 bg-white/40 border-4 border-white/60
        Box(
            modifier = Modifier
                .size(196.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.40f))
                .border(4.dp, Color.White.copy(alpha = 0.60f), CircleShape)
        )

        // The 3D Letter Character
        Text(
            text = letterChar,
            fontSize = 145.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF6B6B), // #FF6B6B from design
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0xFFFF9494), // #FF9494 from .letter-3d
                    offset = Offset(4f, 6f),
                    blurRadius = 6f
                )
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        // Cartoon Eyes & Animated Mouth
        Canvas(modifier = Modifier.size(196.dp)) {
            val eyeRadius = 12.dp.toPx()
            val pupilRadius = 5.5.dp.toPx()
            val leftEyeCenter = Offset(size.width * 0.36f, size.height * 0.36f)
            val rightEyeCenter = Offset(size.width * 0.64f, size.height * 0.36f)

            // White Sclera
            drawCircle(Color.White, radius = eyeRadius, center = leftEyeCenter)
            drawCircle(Color.White, radius = eyeRadius, center = rightEyeCenter)

            // Dark Pupils
            drawCircle(Color(0xFF1E293B), radius = pupilRadius, center = leftEyeCenter)
            drawCircle(Color(0xFF1E293B), radius = pupilRadius, center = rightEyeCenter)

            // Specular Glint
            drawCircle(Color.White, radius = 2.5.dp.toPx(), center = leftEyeCenter - Offset(2.dp.toPx(), 2.dp.toPx()))
            drawCircle(Color.White, radius = 2.5.dp.toPx(), center = rightEyeCenter - Offset(2.dp.toPx(), 2.dp.toPx()))

            // Mouth
            val mouthCenter = Offset(size.width * 0.5f, size.height * 0.72f)
            if (isSpeaking) {
                // Animated open laughing mouth
                val mouthHeight = 8.dp.toPx() + (mouthAnim * 16.dp.toPx())
                drawRoundRect(
                    color = Color(0xFFB91C1C),
                    topLeft = Offset(mouthCenter.x - 20.dp.toPx(), mouthCenter.y - mouthHeight / 2),
                    size = Size(40.dp.toPx(), mouthHeight),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
                // Tongue
                drawCircle(
                    color = Color(0xFFFF80AB),
                    radius = 7.dp.toPx(),
                    center = Offset(mouthCenter.x, mouthCenter.y + mouthHeight / 4)
                )
            } else {
                // Smile Arc
                drawArc(
                    color = Color(0xFF2D3E50),
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(mouthCenter.x - 18.dp.toPx(), mouthCenter.y - 10.dp.toPx()),
                    size = Size(36.dp.toPx(), 18.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }
}

/**
 * Geometric Status Badge
 */
@Composable
fun GeometricStatusBadge(callStatus: CallStatus, isMuted: Boolean) {
    val (label, tint) = when {
        isMuted -> Pair("الميكروفون مكتوم 🔇", Color(0xFFEF4444))
        callStatus == CallStatus.TALKING -> Pair("الحرف يتحدث 🔊", Color(0xFF0369A1))
        callStatus == CallStatus.LISTENING -> Pair("أنا أستمع لك... تحدث يا بطل 🎤", Color(0xFF059669))
        callStatus == CallStatus.THINKING -> Pair("الحرف يفكر... 💭", Color(0xFF7C3AED))
        else -> Pair("متصل 📞", Color(0xFF0369A1))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.50f))
            .border(1.dp, Color.White.copy(alpha = 0.70f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

/**
 * Glass Circle Control Button for Mute / Camera Switch
 */
@Composable
fun GlassCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsStateTarget(if (isPressed) 0.90f else 1f)

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x26000000)
            )
            .clip(CircleShape)
            .background(if (isActive) Color(0xFFFFCDD2) else Color.White.copy(alpha = 0.55f))
            .border(2.dp, Color.White, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) Color(0xFFB91C1C) else Color(0xFF075985), // sky-800
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * 3D Red End Call Button matching .btn-3d-red:
 * - Base slab #B91C1C
 * - Surface #FF4B4B
 * - Inner highlight & shadow
 * - Active translation down 4dp on press
 */
@Composable
fun Neumorphic3DEndCallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topPlateOffset by animateDpAsState(
        targetValue = if (isPressed) 5.dp else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "end_call_press"
    )

    val buttonSize = 78.dp

    Box(
        modifier = modifier
            .size(buttonSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Drop shadow on ground
        Box(
            modifier = Modifier
                .size(buttonSize - 4.dp)
                .offset(y = 8.dp)
                .shadow(
                    elevation = if (isPressed) 4.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x33000000),
                    spotColor = Color(0x4D000000)
                )
        )

        // Base 3D Extrusion Slab: #B91C1C
        Box(
            modifier = Modifier
                .size(buttonSize - 4.dp)
                .offset(y = 6.dp)
                .clip(CircleShape)
                .background(Color(0xFFB91C1C)) // #B91C1C from .btn-3d-red
        )

        // Top Button Plate: #FF4B4B with white gloss border
        Box(
            modifier = Modifier
                .size(buttonSize - 4.dp)
                .offset(y = topPlateOffset)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF5E5E),
                            Color(0xFFFF4B4B),
                            Color(0xFFE53935)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "إنهاء المكالمة",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun animateFloatAsStateTarget(targetValue: Float): androidx.compose.runtime.State<Float> {
    return androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 100),
        label = "anim_float"
    )
}

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

