package com.example.infocard.ui.view

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infocard.data.Card
import com.example.infocard.data.CardProvider
import com.example.infocard.theme.CardBorderGold
import com.example.infocard.theme.MastercardRed
import com.example.infocard.theme.MastercardYellow
import com.example.infocard.theme.TroyCyan
import com.example.infocard.theme.TroyOrange
import com.example.infocard.util.AppLanguage
import com.example.infocard.util.Loc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CardProviderLogo(
    provider: CardProvider,
    scale: Float = 1f,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        when (provider) {
            CardProvider.VISA -> {
                Text(
                    text = "VISA",
                    fontSize = (16 * scale).sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.15f),
                            offset = Offset(0f, 2f),
                            blurRadius = 2f
                        )
                    )
                )
            }
            CardProvider.MASTERCARD -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size((18 * scale).dp)
                            .background(MastercardRed, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size((18 * scale).dp)
                            .background(MastercardYellow.copy(alpha = 0.9f), CircleShape)
                    )
                }
            }
            CardProvider.TROY -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "tr",
                        fontSize = (15 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = TroyCyan,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.15f),
                                offset = Offset(0f, 2f),
                                blurRadius = 2f
                            )
                        )
                    )
                    Text(
                        text = "oy",
                        fontSize = (15 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        color = TroyOrange,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.15f),
                                offset = Offset(0f, 2f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
            CardProvider.UNKNOWN -> {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Card",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size((15 * scale).dp)
                )
            }
        }
    }
}

@Composable
fun CardView(
    card: Card,
    language: AppLanguage,
    roll: Double,
    pitch: Double,
    modifier: Modifier = Modifier,
    maskNumber: Boolean = false,
    isPreview: Boolean = false,
    onTap: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Tap scale bounce animation states
    var isBouncing by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isBouncing) 1.045f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "CardScaleBounce"
    )

    // Tap border gold glow animation
    var showFlash by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (showFlash) CardBorderGold else Color.White.copy(alpha = 0.2f),
        animationSpec = tween(durationMillis = 520),
        label = "BorderFlash"
    )
    val borderWidth by animateFloatAsState(
        targetValue = if (showFlash) 2.5f else 1.0f,
        animationSpec = tween(durationMillis = 520),
        label = "BorderWidth"
    )

    // Sheen diagonal sweep animation
    var triggerSheen by remember { mutableStateOf(false) }
    val sheenMultiplier by animateFloatAsState(
        targetValue = if (triggerSheen) 1.35f else -1.2f,
        animationSpec = tween(durationMillis = 520, easing = LinearEasing),
        label = "SheenSweep"
    )

    val maxTilt = 6f
    val currentRoll = if (isPreview) 0.0 else roll
    val currentPitch = if (isPreview) 0.0 else pitch

    val density = LocalDensity.current.density

    // Card Colors setup (custom color takes precedence over preset theme)
    val cardColors = remember(card.customColorHex, card.theme) {
        val hex = card.customColorHex
        if (hex != null) {
            val baseColor = try {
                val colorInt = hex.trimStart('#').toLong(16)
                Color(colorInt or 0xFF000000)
            } catch (e: Exception) {
                Color(0xFF6D28D9)
            }
            // Darkened counterpart
            val darkenedColor = baseColor.darkened()
            listOf(baseColor, darkenedColor)
        } else {
            card.theme.colors
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = (-currentPitch * maxTilt).toFloat()
                rotationY = (-currentRoll * maxTilt).toFloat()
                cameraDistance = 12f * density
                scaleX = cardScale
                scaleY = cardScale
            }
            .fillMaxWidth()
            .height(195.dp)
            .shadow(
                elevation = if (showFlash) 14.dp else 10.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = if (showFlash) CardBorderGold.copy(alpha = 0.5f) else Color.Black
            )
            .background(
                brush = Brush.linearGradient(
                    colors = cardColors,
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(borderWidth.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isPreview && onTap != null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isBouncing = true
                showFlash = true
                triggerSheen = true

                coroutineScope.launch {
                    delay(150)
                    isBouncing = false
                    delay(380)
                    showFlash = false
                    // Reset sheen silently
                    triggerSheen = false
                }
                onTap?.invoke()
            }
    ) {
        // Metallic/Glassmorphic shine overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        // Parallax background vector shapes
        CardDecorations(
            roll = currentRoll,
            pitch = currentPitch,
            modifier = Modifier.fillMaxSize()
        )

        // Sweeping diagonal sheen flash
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val sheenWidth = width * 0.45f
            val xOffset = width * sheenMultiplier

            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(xOffset, 0f)
                    lineTo(xOffset + sheenWidth, 0f)
                    lineTo(xOffset + sheenWidth - (height * 0.4f), height)
                    lineTo(xOffset - (height * 0.4f), height)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFBBF24).copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.2f),
                            Color(0xFFFBBF24).copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        start = Offset(xOffset, 0f),
                        end = Offset(xOffset + sheenWidth, height)
                    )
                )
            }
        }

        // Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Card Name & Metallic Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = card.name.uppercase(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp
                )

                // Golden Chip Mockup
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 28.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300)),
                                start = Offset(0f, 0f),
                                end = Offset.Infinite
                            ),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeColor = Color.Black.copy(alpha = 0.12f)
                        val w = size.width
                        val h = size.height

                        // Chip lines
                        drawLine(strokeColor, Offset(w * 0.32f, 0f), Offset(w * 0.32f, h), 1.dp.toPx())
                        drawLine(strokeColor, Offset(w * 0.68f, 0f), Offset(w * 0.68f, h), 1.dp.toPx())
                        drawLine(strokeColor, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1.dp.toPx())
                    }
                }
            }

            // Card Number
            Text(
                text = formatCardNumber(card.number, maskNumber),
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // Apply drop shadow effect to text
                    }
            )

            // Optional Cardholder Name
            if (!card.holderName.isNullOrBlank()) {
                Text(
                    text = card.holderName!!.trim().uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Footer: Expiry, CVV, and Brand Logo
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Expiry Date
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = Loc.VALID_THRU.get(language),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = card.expiry.ifEmpty { "MM/YY" },
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // CVV
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "CVV",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (card.cvv.isEmpty()) "***" else (if (maskNumber) "***" else card.cvv),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Card Provider Logo
                CardProviderLogo(
                    provider = card.provider,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// Parallax card decorations (moving stripes and circles)
@Composable
fun CardDecorations(
    roll: Double,
    pitch: Double,
    modifier: Modifier = Modifier
) {
    // Inverse parallax offsets for background components
    val parallaxX = (-roll * 12).toFloat()
    val parallaxY = (-pitch * 12).toFloat()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Diagonal accent stripe (does not move with parallax for static depth structure)
        val stripePath = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.7f, 0f)
            lineTo(w * 0.75f, 0f)
            lineTo(0f, h * 1.07f)
            close()
        }
        drawPath(
            path = stripePath,
            color = Color.White.copy(alpha = 0.04f)
        )

        // Translucent circles with gyroscopic offsets
        drawCircle(
            color = Color.White.copy(alpha = 0.04f),
            radius = 70.dp.toPx(),
            center = Offset(w - 60.dp.toPx() + parallaxX, h - 70.dp.toPx() + parallaxY)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.02f),
            radius = 100.dp.toPx(),
            center = Offset(-50.dp.toPx() + parallaxX, -80.dp.toPx() + parallaxY)
        )
    }
}

private fun formatCardNumber(number: String, maskNumber: Boolean): String {
    val cleaned = number.replace(" ", "")
    val builder = StringBuilder()
    val displayCount = 16

    for (i in 0 until displayCount) {
        if (i < cleaned.length) {
            if (maskNumber && i >= 12) {
                builder.append("*")
            } else {
                builder.append(cleaned[i])
            }
        } else {
            builder.append("*")
        }

        if ((i + 1) % 4 == 0 && i < displayCount - 1) {
            builder.append(" ")
        }
    }
    return builder.toString()
}

// Utility to mathematically darken a Color
fun Color.darkened(): Color {
    return Color(
        red = this.red * 0.45f,
        green = this.green * 0.45f,
        blue = this.blue * 0.45f,
        alpha = this.alpha
    )
}
