package com.example.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LetterItem(
    val char: String,
    val name: String,
    val example: String,
    val primaryColor: Color,
    val darkColor: Color
)

@Composable
fun HomeScreen(
    onLetterSelected: (letterChar: String, letterName: String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Model letters: 'أ' and 'ر' featured first, followed by other letters for rich gameplay
    val letters = listOf(
        LetterItem("أ", "أَلِف", "أَسَد 🦁", Color(0xFFFF7043), Color(0xFFD84315)),
        LetterItem("ر", "رَاء", "رُمَّان 🍎", Color(0xFF26A69A), Color(0xFF00695C)),
        LetterItem("ب", "بَاء", "بَطَّة 🦆", Color(0xFF42A5F5), Color(0xFF1565C0)),
        LetterItem("ت", "تَاء", "تُفَّاح 🍏", Color(0xFFAB47BC), Color(0xFF6A1B9A)),
        LetterItem("ث", "ثَاء", "ثَعْلَب 🦊", Color(0xFFFFA726), Color(0xFFE65100)),
        LetterItem("ج", "جِيم", "جَمَل 🐪", Color(0xFF66BB6A), Color(0xFF2E7D32)),
        LetterItem("ح", "حَاء", "حِصَان 🐴", Color(0xFF29B6F6), Color(0xFF0277BD)),
        LetterItem("خ", "خَاء", "خَرُوف 🐑", Color(0xFFEC407A), Color(0xFFAD1457)),
        LetterItem("د", "دَال", "دُب 🐻", Color(0xFF8D6E63), Color(0xFF4E342E)),
        LetterItem("س", "سِين", "سَمَكَة 🐟", Color(0xFF26C6DA), Color(0xFF00838F)),
        LetterItem("م", "مِيم", "مَوْز 🍌", Color(0xFFFFCA28), Color(0xFFF57F17)),
        LetterItem("ن", "نُون", "نَحْلَة 🐝", Color(0xFF7E57C2), Color(0xFF4527A0))
    )

    // Sky to ice-blue gradient background matching Geometric Balance
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF87CEEB), // Soft Sky Blue
                        Color(0xFFBAE6FD), // Light Sky
                        Color(0xFFE0F2FE)  // Soft Ice Blue
                    )
                )
            )
    ) {
        // Decorative background clouds/bubbles (100% in code)
        CanvasClouds()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar with App Title and Settings Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and playful badge
                Column {
                    Text(
                        text = "مكالمة الحروف 📞✨",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0C4A6E), // text-sky-900
                        modifier = Modifier.testTag("app_title")
                    )
                    Text(
                        text = "المس أي حرف للاتصال به والتحدث معه! 🎈",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1) // text-sky-700
                    )
                }

                // Settings button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.White.copy(alpha = 0.55f), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF0C4A6E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of 3D Neumorphic Letter Buttons
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("letters_grid")
            ) {
                items(letters) { letter ->
                    Neumorphic3DLetterButton(
                        letter = letter,
                        onClick = { onLetterSelected(letter.char, letter.name) }
                    )
                }
            }
        }
    }
}

/**
 * Custom 3D Neumorphic / Extruded Button for children.
 * Features:
 * - Upper highlight reflection
 * - Dark bottom extrusion / bevel shadow
 * - Interactive depression: on click, the top plate drops down by 6dp to simulate a real tactile button!
 */
@Composable
fun Neumorphic3DLetterButton(
    letter: LetterItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press depression animation
    val topPlateOffset by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 0.dp,
        animationSpec = tween(durationMillis = 100),
        label = "press_offset"
    )

    val shape = RoundedCornerShape(22.dp)
    val buttonHeight = 160.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("letter_button_${letter.char}")
    ) {
        // 1. Deep ambient shadow on the floor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight - 8.dp)
                .offset(y = 12.dp)
                .shadow(
                    elevation = if (isPressed) 4.dp else 12.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
        )

        // 2. 3D Bottom Slab / Extrusion (Darker color giving solid depth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight - 6.dp)
                .offset(y = 6.dp)
                .clip(shape)
                .background(letter.darkColor)
        )

        // 3. Top Button Surface that depresses downward when touched
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight - 6.dp)
                .offset(y = topPlateOffset)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            letter.primaryColor,
                            letter.primaryColor.copy(alpha = 0.92f),
                            letter.darkColor.copy(alpha = 0.85f)
                        )
                    )
                )
                // Top border light reflection
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = shape
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Call icon tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Huge 3D Text Letter
                Text(
                    text = letter.char,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.45f),
                            offset = Offset(0f, 6f),
                            blurRadius = 8f
                        )
                    )
                )

                // Letter Name & Example Word
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = letter.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = letter.example,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Playful soft clouds drawn purely with Compose canvas in the background.
 */
@Composable
fun CanvasClouds() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val cloudPaint = Color.White.copy(alpha = 0.12f)

                // Cloud 1
                drawCircle(cloudPaint, radius = 70.dp.toPx(), center = Offset(60.dp.toPx(), 90.dp.toPx()))
                drawCircle(cloudPaint, radius = 95.dp.toPx(), center = Offset(130.dp.toPx(), 80.dp.toPx()))
                drawCircle(cloudPaint, radius = 60.dp.toPx(), center = Offset(190.dp.toPx(), 95.dp.toPx()))

                // Cloud 2
                drawCircle(cloudPaint, radius = 80.dp.toPx(), center = Offset(size.width - 50.dp.toPx(), 220.dp.toPx()))
                drawCircle(cloudPaint, radius = 105.dp.toPx(), center = Offset(size.width - 120.dp.toPx(), 200.dp.toPx()))
                drawCircle(cloudPaint, radius = 70.dp.toPx(), center = Offset(size.width - 180.dp.toPx(), 225.dp.toPx()))
            }
    )
}
